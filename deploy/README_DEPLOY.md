# AI 智能考试与网课学情分析系统 Docker 部署说明

本文档用于将当前 SpringBoot + Vue 项目部署为标准 Docker 五容器架构。

## 一、部署架构

本项目采用前后端分离与基础服务独立部署：

1. `frontend`：使用 Nginx 托管 Vue 打包后的 `dist`，作为系统唯一 Web 入口。
2. `backend`：运行 SpringBoot jar，提供考试、课程、网课、学习任务、AI 等接口。
3. `mysql`：MySQL 数据库，保存业务数据、演示数据、AI 报告和题库数据。
4. `redis`：缓存服务，用于项目运行期缓存能力。
5. `minio`：对象存储服务，用于文件、图片、视频、课程资料等上传资源。

访问链路：

```text
浏览器
  -> frontend(Nginx)
      -> /              Vue 页面
      -> /api/          反向代理到 backend:8080
      -> /api/websocket WebSocket 反向代理到 backend:8080

backend
  -> mysql:3306
  -> redis:6379
  -> minio:9000
```

## 二、环境要求

- Linux / WSL2 / 云服务器
- Docker
- Docker Compose
- Git

建议服务器至少准备 2C4G 内存。首次构建会下载 Maven、Node、Nginx、MySQL、Redis、MinIO 镜像，网络较慢时请耐心等待。

## 三、快速启动

进入部署目录并复制环境变量模板：

```bash
cd deploy
cp .env.example .env
```

编辑 `.env`，至少修改生产密码和密钥后启动：

```bash
docker compose build --no-cache
docker compose up -d
```

首次启动 MySQL 会自动执行 `deploy/mysql/init/` 下的初始化 SQL。

## 四、查看状态

```bash
docker compose ps
```

确认 `frontend`、`backend`、`mysql`、`redis`、`minio` 均处于运行状态。

## 五、查看日志

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
docker compose logs -f redis
docker compose logs -f minio
```

如果首次启动失败，优先查看 `mysql`、`backend`、`frontend` 日志。

## 六、访问地址

前端系统：

```text
http://服务器IP:APP_PORT
```

例如 `.env` 中 `APP_PORT=10005`：

```text
http://服务器IP:10005
```

MinIO 控制台：

```text
http://服务器IP:MINIO_CONSOLE_PORT
```

例如 `.env` 中 `MINIO_CONSOLE_PORT=9001`：

```text
http://服务器IP:9001
```

## 七、默认账号

如果已执行 `deploy/mysql/init/09_demo_data.sql`，可使用以下演示账号：

| 类型 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |
| 教师 | `teacher` | `123456` |
| 学生 | `student` | `123456` |
| 演示管理员 | `demo_admin` | `123456` |
| 演示教师 | `demo_teacher` | `123456` |
| 演示学生 | `demo_student` | `123456` |
| 演示学生 | `demo_student2` | `123456` |

MinIO 控制台账号来自 `.env`：

| 类型 | 配置项 |
| --- | --- |
| 账号 | `MINIO_ROOT_USER` |
| 密码 | `MINIO_ROOT_PASSWORD` |

如果数据库中没有测试账号，请确认初始化脚本是否执行，或重新导入演示数据。

## 八、AI 配置

AI 配置位于 `deploy/.env`：

```env
AI_PROVIDER=openai-compatible
AI_BASE_URL=
AI_API_KEY=
AI_MODEL=
AI_ENABLED=false
AI_MOCK_ENABLED=true
```

说明：

1. `AI_MOCK_ENABLED=true` 时使用 mock 数据，不调用真实模型接口，适合演示和本地测试。
2. `AI_ENABLED=true` 且 `AI_MOCK_ENABLED=false` 时调用真实接口。
3. `AI_BASE_URL` 配置模型服务地址，例如兼容 OpenAI 协议的接口地址。
4. `AI_API_KEY` 配置真实模型密钥。
5. `AI_MODEL` 配置模型名称。
6. 真实 `AI_API_KEY` 不得提交到 GitHub，也不要写入镜像。

## 九、JWT 配置

JWT 配置位于 `deploy/.env`：

```env
JWT_SECRET=change_me_to_a_long_random_secret
```

要求：

1. 生产环境必须修改 `JWT_SECRET`，建议使用足够长的随机字符串。
2. 如果多个后端服务共享登录态，必须使用同一个 `JWT_SECRET`。
3. `JWT_SECRET` 不得泄露，不得提交到 GitHub。

## 十、MinIO 配置

MinIO 配置位于 `deploy/.env`：

```env
MINIO_API_PORT=9000
MINIO_CONSOLE_PORT=9001
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=change_me_minio_password
MINIO_BUCKET=exam-files
MINIO_EXTERNAL_ENDPOINT=http://localhost:9000
```

说明：

1. `MINIO_ENDPOINT` 是后端容器内部访问地址，在 `docker-compose.yml` 中通常为 `http://minio:9000`。
2. `MINIO_EXTERNAL_ENDPOINT` 是浏览器或用户访问文件时使用的外部地址，生产环境应改为服务器公网地址或域名。
3. `MINIO_BUCKET` 是默认 bucket，`minio-init` 服务会自动创建。
4. MinIO 数据保存在 `minio_data` volume 中。
5. 上传失败或文件无法访问时，优先检查 `MINIO_EXTERNAL_ENDPOINT`、bucket 权限和 MinIO 日志。

## 十一、数据库初始化说明

初始化 SQL 位于：

```text
deploy/mysql/init/
```

执行顺序：

```text
00_create_database.sql
01_db_exam.sql
02_course_content.sql
03_question_course_difficulty.sql
04_online_course.sql
05_learning_task.sql
06_ai_infrastructure.sql
07_ai_report.sql
08_ai_generated_question.sql
09_demo_data.sql
```

MySQL Docker 只会在数据卷为空时执行初始化脚本。如果 `mysql_data` volume 已经存在，后续修改 SQL 不会自动重新执行。

如需重置数据库，请先确认没有重要数据，然后执行：

```bash
docker compose down -v
docker compose up -d
```

当前 Compose 使用 MySQL 8.0。如果遇到旧 SQL 或驱动兼容问题，可在评估后临时切换为 MySQL 5.7，但正式部署建议优先修正兼容问题。

## 十二、常用命令

重新构建镜像：

```bash
docker compose build --no-cache
```

后台启动：

```bash
docker compose up -d
```

停止容器：

```bash
docker compose down
```

停止并删除数据卷：

```bash
docker compose down -v
```

重启后端：

```bash
docker compose restart backend
```

查看后端日志：

```bash
docker compose logs -f backend
```

查看 Docker 磁盘占用：

```bash
docker system df
```

清理构建缓存：

```bash
docker builder prune -a -f
```

## 十三、常见问题

### 1. 前端能打开但接口 404

检查 Nginx 是否将 `/api/` 转发到 `backend:8080`：

```bash
docker compose logs -f frontend
docker compose logs -f backend
```

确认前端请求路径为 `/api/...`，不要请求 `localhost`、`127.0.0.1` 或固定服务器 IP。

### 2. 后端连不上 MySQL

检查 `.env` 中数据库配置：

```env
MYSQL_DATABASE=db_exam
MYSQL_USERNAME=exam_user
MYSQL_PASSWORD=change_me_user
```

在容器内后端连接地址应为 `mysql:3306`，不要配置为 `127.0.0.1`。

查看日志：

```bash
docker compose logs -f mysql
docker compose logs -f backend
```

### 3. Redis 连接失败

检查 `.env` 中 `REDIS_PASSWORD` 与后端环境变量是否一致。为空时 Redis 无密码启动；不为空时 Redis 使用 `requirepass`。

查看日志：

```bash
docker compose logs -f redis
docker compose logs -f backend
```

### 4. MinIO 上传失败

检查：

1. `MINIO_ROOT_USER`
2. `MINIO_ROOT_PASSWORD`
3. `MINIO_BUCKET`
4. 后端内部 endpoint 是否为 `http://minio:9000`
5. `minio-init` 是否成功创建 bucket

查看日志：

```bash
docker compose logs -f minio
docker compose logs -f minio-init
docker compose logs -f backend
```

### 5. 文件 URL 无法访问

检查 `MINIO_EXTERNAL_ENDPOINT`。该地址是浏览器访问文件的地址，生产环境不要保留 `http://localhost:9000`，应改为：

```text
http://服务器IP:MINIO_API_PORT
```

或外部网关、域名地址。

### 6. MySQL 初始化脚本没有执行

MySQL 只在 `mysql_data` volume 为空时执行 `/docker-entrypoint-initdb.d` 下的 SQL。若之前启动过容器，需要重置：

```bash
docker compose down -v
docker compose up -d
```

注意：`down -v` 会删除数据库数据。

### 7. 登录失败或 token 无效

检查：

1. 数据库演示账号是否初始化。
2. `JWT_SECRET` 是否修改后导致旧 token 失效。
3. 浏览器是否仍保留旧 token，可清理 localStorage 后重试。
4. 多个后端实例是否使用同一个 `JWT_SECRET`。

### 8. WebSocket 连接失败

检查前端 WebSocket 地址是否为当前域名下的 `/api/websocket`，并确认 Nginx 已配置 `Upgrade` 和 `Connection` 请求头。

查看日志：

```bash
docker compose logs -f frontend
docker compose logs -f backend
```

### 9. AI 接口不可用

如果只是演示，确认：

```env
AI_ENABLED=false
AI_MOCK_ENABLED=true
```

如果调用真实模型，确认：

```env
AI_ENABLED=true
AI_MOCK_ENABLED=false
AI_BASE_URL=真实服务地址
AI_API_KEY=真实密钥
AI_MODEL=模型名称
```

真实密钥不要提交到 GitHub。

### 10. 端口被占用

修改 `.env` 中端口：

```env
APP_PORT=10005
MYSQL_PORT=3306
REDIS_PORT=6379
MINIO_API_PORT=9000
MINIO_CONSOLE_PORT=9001
```

然后重新启动：

```bash
docker compose up -d
```

### 11. 前端页面空白

检查：

1. 前端镜像是否构建成功。
2. `npm run build:prod` 是否成功。
3. Nginx 是否正确托管 `/usr/share/nginx/html`。
4. 浏览器控制台是否有资源 404。

查看日志：

```bash
docker compose logs -f frontend
```

### 12. Docker 构建很慢

可能原因：

1. 首次拉取基础镜像较慢。
2. Maven 下载依赖较慢。
3. npm 下载依赖较慢。
4. 未使用依赖缓存。

可以清理旧缓存后重试：

```bash
docker builder prune -a -f
docker compose build --no-cache
```

也可以后续配置 Maven 或 npm 镜像源来加速。

## 十四、工程云或子路径部署说明

项目内部优先按 `/` 根路径部署。

`/docker-10005` 这类子路径建议由外层网关转发到 `APP_PORT`，例如：

```text
外层网关 /docker-10005 -> http://127.0.0.1:10005/
```

如果必须让项目自身支持 `/docker-10005`，需要同步修改：

1. Vue `publicPath`
2. Vue Router `base`
3. Nginx `location`
4. API `baseURL`
5. WebSocket URL

不建议在项目内部优先复杂化子路径部署，除非部署环境明确要求。
