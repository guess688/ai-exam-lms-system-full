# AI Exam LMS 工程云镜像导入部署说明

## 1. 部署方式说明

这套方式用于“本地编译镜像，云端直接导入镜像”：

- 本地完成 backend/frontend 镜像构建。
- 本地导出 backend、frontend、mysql、redis、minio、minio/mc 镜像到 tar 包。
- 云端通过 `docker load` 导入镜像。
- 云端复制并修改 `deploy/.env`。
- 云端使用 `docker compose -f docker-compose.image.yml up -d` 启动。
- 云端不需要 Maven、npm、GitHub clone 或源码编译。

## 2. 本地构建镜像

在 Windows 本地项目根目录执行：

```powershell
cd D:\ai-exam-lms-system
powershell -ExecutionPolicy Bypass -File deploy/build-images.ps1
```

## 3. 本地导出镜像

```powershell
powershell -ExecutionPolicy Bypass -File deploy/save-images.ps1
```

导出文件：

```text
deploy/images/ai-exam-lms-images-1.0.0.tar
```

## 4. 需要上传到云端的文件

```text
deploy/docker-compose.image.yml
deploy/.env.example
deploy/nginx/
deploy/mysql/
deploy/load-images.sh
deploy/start-image-deploy.sh
deploy/stop-image-deploy.sh
deploy/README_IMAGE_DEPLOY.md
deploy/images/ai-exam-lms-images-1.0.0.tar
```

## 5. 云端导入镜像

在云端 `deploy` 目录执行：

```bash
cd deploy
chmod +x load-images.sh start-image-deploy.sh stop-image-deploy.sh
./load-images.sh
```

## 6. 云端配置

```bash
cp .env.example .env
vim .env
```

必须修改生产密码、JWT、MinIO 密码和需要启用的 AI 配置。不要提交 `deploy/.env`。

## 7. 云端启动

```bash
./start-image-deploy.sh
```

查看后端日志：

```bash
docker compose -f docker-compose.image.yml logs -f backend
```

停止服务但保留数据 volume：

```bash
./stop-image-deploy.sh
```

需要删除 MySQL、Redis、MinIO 数据时，人工确认后再执行：

```bash
docker compose -f docker-compose.image.yml down -v
```

## 8. 云端访问

```text
http://服务器IP:${APP_PORT}/
```

## 9. 工程云路径说明

如果工程云分配 `/docker-10005` 和端口 `10005`，建议由外层网关转发：

```text
/docker-10005 -> http://127.0.0.1:10005/
```

并由外层网关剥离 `/docker-10005` 前缀。项目内部继续按 `/` 运行。

如果网关不剥离前缀，需要额外做 `BASE_PATH` 适配。

## 10. 后续修改配置

修改 `deploy/.env` 后执行：

```bash
docker compose -f docker-compose.image.yml up -d --force-recreate backend frontend
```

## 11. 可以只改 .env 的配置

- `APP_PORT`
- `JWT_SECRET`
- `AI_ENABLED`
- `AI_MOCK_ENABLED`
- `AI_BASE_URL`
- `AI_API_KEY`
- `AI_MODEL`
- `MYSQL_PASSWORD`
- `REDIS_PASSWORD`
- `MINIO_EXTERNAL_ENDPOINT`
- `MINIO_ROOT_PASSWORD`

## 12. 需要重新构建镜像的修改

- Java 后端代码
- Vue 前端代码
- `pom.xml`
- `package.json`
- `Dockerfile`
- 前端 `publicPath`
- Nginx 配置如果没有挂载到宿主机

当前 `docker-compose.image.yml` 已挂载：

```text
./nginx/nginx.conf:/etc/nginx/conf.d/default.conf
```

因此云端可以直接修改 `deploy/nginx/nginx.conf` 后重建前端容器，不需要重新构建前端镜像。

## 13. 安全注意

- 不提交 `deploy/.env`。
- 不把真实 `JWT_SECRET` 写进镜像。
- 不把真实 `AI_API_KEY` 写进镜像。
- 不把真实 AccessKey 写进镜像。
- 不把现场真实数据打进镜像。
- MySQL、Redis、MinIO 数据使用 Docker volume。
- `deploy/images/*.tar` 不提交 Git。

## 14. 常见问题

### docker load 后找不到镜像

确认导入的是正确文件：

```bash
ls -lh images/ai-exam-lms-images-1.0.0.tar
docker images | grep -E "ai-exam-lms|mysql|redis|minio"
```

### docker compose 报找不到 image

先执行：

```bash
./load-images.sh
```

确认存在：

```text
ai-exam-lms-backend:1.0.0
ai-exam-lms-frontend:1.0.0
```

### 端口被占用

修改 `deploy/.env` 中的端口，例如：

```text
APP_PORT=10005
MYSQL_PORT=3306
REDIS_PORT=6379
MINIO_API_PORT=9000
MINIO_CONSOLE_PORT=9001
```

### MySQL 初始化脚本没有执行

MySQL 初始化脚本只在 `mysql_data` 为空时执行。需要重新初始化数据库时，确认数据可删除后执行：

```bash
docker compose -f docker-compose.image.yml down -v
docker compose -f docker-compose.image.yml up -d
```

### MinIO 文件 URL 无法访问

确认 `deploy/.env` 中的外部地址能被浏览器访问：

```text
MINIO_EXTERNAL_ENDPOINT=http://服务器IP:9000
```

### 登录 401/403

确认后端容器读取的是同一个 `JWT_SECRET`，修改后重建后端和前端容器：

```bash
docker compose -f docker-compose.image.yml up -d --force-recreate backend frontend
```

### WebSocket 连接失败

确认 `deploy/nginx/nginx.conf` 仍然保留 `Upgrade` 和 `Connection` 头，并确认前端访问路径为 `/api/websocket`。

### 工程云 /docker-10005 访问 404

优先确认工程云网关是否剥离 `/docker-10005` 前缀。项目内部默认以 `/` 运行；如果网关不剥离前缀，需要做 `BASE_PATH` 适配。
