# AI 智能考试与网课学情分析系统

基于现有 Spring Boot + Vue 在线考试系统二次开发，面向学校、培训机构和企业内训场景，提供考试管理、题库组卷、网课学习、学习任务、成绩统计、章节/知识点掌握分析以及 AI 学情报告、AI 出题、AI 推荐练习等能力。

## 核心功能

- 用户与权限：管理员、教师、学生三类角色，复用原有登录、角色、班级和权限体系。
- 教学内容：课程、章节、知识点三层模型，作为题库、试卷、网课和 AI 分析的统一业务主线。
- 题库与考试：支持单选题、多选题、不定项选择题、判断题；题目支持课程、章节、知识点、难度标记。
- 自动组卷：教师可按课程、章节、知识点、题型、难度和题量自动抽题生成试卷。
- 成绩与分析：支持成绩统计、章节掌握、知识点掌握、高频错题、薄弱点排名、难度表现分析。
- 图表展示：教师端和学生端提供成绩分布、正确率、错题排名、趋势等 ECharts 图表。
- 轻量网课：支持课程视频、资料、学生观看进度、完成率统计。
- 学习任务：支持考试、练习、网课、错题订正、复习任务发布和完成记录。
- AI 能力：统一 AI 服务层，支持 mock 模式和真实模型配置；覆盖个人学情报告、班级学情报告、AI 出题、推荐练习、学习行为评价等。
- 审核机制：AI 生成题目先进入待审核列表，教师确认后才可入库。

## 二次开发原则

- 不推翻原项目架构，优先复用已有考试系统能力。
- 每次只完成一个阶段，避免一次性大范围重构。
- 新增表提供 SQL migration 或初始化 SQL。
- 新增接口兼容现有权限体系。
- 新增前端页面复用现有 UI 风格、路由结构和接口封装方式。
- 保证原有考试、答题、判分和成绩流程继续运行。
- AI API Key 不写死在代码中，使用配置文件占位或环境变量注入。
- `references` 目录只作为候选复用源码池，经过评估后再迁移到 `external` 或主项目。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Spring Boot 2.1.x、Spring Security、MyBatis-Plus、MapStruct、Lombok |
| 数据库 | MySQL、Redis |
| 文件存储 | MinIO / OSS 配置兼容 |
| 前端 | Vue 2、Vue Router、Vuex、Element UI、Axios |
| 图表 | ECharts 4 |
| AI | 统一 `AiService`，支持 mock provider、OpenAI 兼容接口或外部出题服务 |

## 环境要求

- JDK 8
- Maven 3.6+
- MySQL 5.7+ 或 8.x
- Redis 5+
- Node.js 14 或 16 推荐
- npm 6+；如依赖安装失败，可使用 `npm install --legacy-peer-deps`

## 目录结构

```text
ai-exam-lms-system/
  backend/      Spring Boot 后端
  frontend/     Vue 前端
  docs/         交付、设计、部署和演示文档
  external/     已评估可迁移的外部能力
  references/   开源参考仓库，仅作学习、评估和二次开发参考
```

## 数据库初始化步骤

1. 创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS db_exam DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 按顺序执行初始化 SQL：

```bash
mysql -uroot -p db_exam < backend/sql/db_exam.sql
mysql -uroot -p db_exam < backend/sql/20260628_course_content.sql
mysql -uroot -p db_exam < backend/sql/20260629_question_course_difficulty.sql
mysql -uroot -p db_exam < backend/sql/20260629_online_course.sql
mysql -uroot -p db_exam < backend/sql/20260629_learning_task.sql
mysql -uroot -p db_exam < backend/sql/20260630_ai_infrastructure.sql
mysql -uroot -p db_exam < backend/sql/20260630_ai_report.sql
mysql -uroot -p db_exam < backend/sql/20260630_ai_generated_question.sql
mysql -uroot -p db_exam < backend/sql/20260630_demo_data.sql
```

3. 确认 Redis 已启动，默认地址为 `127.0.0.1:6379`。

Docker 部署时，MySQL 初始化脚本已整理到 `deploy/mysql/init/`，会按文件名顺序执行：

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

MySQL Docker 初始化脚本只会在 `mysql_data` volume 为空时执行。如果已经启动过 MySQL 容器，后续修改 SQL 不会自动重新导入。需要重新初始化演示库时，先备份重要数据，再执行：

```bash
cd deploy
docker compose down -v
docker compose up -d --build
```

当前 Docker Compose 默认使用 MySQL 8.0。如果遇到旧 SQL 或驱动兼容问题，可在评估后临时切换为 MySQL 5.7，但正式部署建议优先修正兼容问题。

## 后端启动步骤

1. 修改后端配置：

文件：`backend/src/main/resources/application-dev.yml`

重点检查：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/db_exam?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: your_mysql_password
  redis:
    host: 127.0.0.1
    port: 6379
```

2. 启动后端：

```bash
cd backend
mvn clean package -DskipTests
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

默认服务地址：`http://localhost:8080`

## 前端启动步骤

```bash
cd frontend
npm install --legacy-peer-deps
npm run dev
```

默认访问地址：`http://localhost:9528`

如需修改后端 API 地址，请检查 `frontend/vue.config.js` 或前端请求封装中的代理配置。

## AI 配置说明

后端统一使用以下配置项：

```yaml
ai:
  provider: ${AI_PROVIDER:mock}
  baseUrl: ${AI_BASE_URL:https://api.openai.com/v1}
  apiKey: ${AI_API_KEY:}
  model: ${AI_MODEL:gpt-4o-mini}
  enabled: ${AI_ENABLED:false}
  mockEnabled: ${AI_MOCK_ENABLED:true}
  questionGeneratorServiceUrl: ${AI_QUESTION_SERVICE_URL:}
```

推荐通过环境变量注入真实密钥：

```bash
set AI_ENABLED=true
set AI_MOCK_ENABLED=false
set AI_PROVIDER=openai-compatible
set AI_BASE_URL=https://your-provider.example.com/v1
set AI_API_KEY=your_api_key
set AI_MODEL=your_model
```

不要把真实 API Key 写入代码仓库。演示环境建议先使用 mock 模式。

## Mock AI 模式

默认配置为：

```yaml
ai.enabled: false
ai.mockEnabled: true
```

mock 模式不会调用真实大模型，会返回可演示的示例内容，适合客户演示、离线开发和接口联调。相关功能包括：

- AI 学生个人学情报告
- AI 班级学情报告
- AI 自动出题
- AI 推荐练习
- 学习行为评价

## 测试账号

执行 `backend/sql/db_exam.sql` 和 `backend/sql/20260630_demo_data.sql` 后可使用：

| 角色 | 用户名 | 密码 | 用途 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `123456` | 用户、班级、系统基础数据管理 |
| 教师 | `teacher` | `123456` | 课程、题库、试卷、考试、网课、任务、AI 功能演示 |
| 学生 | `student` | `123456` | 在线考试、查看成绩、网课学习、任务、个人 AI 报告 |
| 管理员演示号 | `demo_admin` | `123456` | 客户演示备用账号 |
| 教师演示号 | `demo_teacher` | `123456` | 客户演示备用账号 |
| 学生演示号 | `demo_student` | `123456` | 客户演示备用账号 |

## 系统截图占位

交付材料可在以下位置补充截图：

```text
docs/screenshots/login.png
docs/screenshots/dashboard.png
docs/screenshots/course-management.png
docs/screenshots/exam-analysis.png
docs/screenshots/ai-report.png
docs/screenshots/ai-question-review.png
```

建议截图顺序：登录页、教师工作台、课程管理、题库管理、自动组卷、考试分析图表、AI 班级报告、学生个人报告。

## 常见问题

**1. 前端登录失败怎么办？**

先确认后端 `8080` 已启动、数据库和 Redis 可连接、前端代理指向正确。若本地验证码不便演示，可在配置中将 `online-exam.login.captcha.enabled` 调整为 `false` 后重启后端。

**2. 依赖安装失败怎么办？**

前端项目基于 Vue 2，推荐 Node.js 14 或 16。安装时可使用 `npm install --legacy-peer-deps`。

**3. AI 功能是否必须配置真实模型？**

不必须。默认 mock 模式可完整演示功能流程。真实模型只需通过环境变量配置，不需要修改业务代码。

**4. 演示数据会影响正式数据吗？**

演示数据使用 `9000` 段 ID，集中在 `backend/sql/20260630_demo_data.sql`。正式环境可以不执行该脚本。

**5. references 目录里的开源项目是否会直接混入主项目？**

不会盲目复制。后续开发以 `backend` 和 `frontend` 为主，`references` 仅作为学习和二次开发参考；经过评估可复用的 Prompt、schema、页面结构或独立服务会先放入 `external` 或按主项目技术栈重写。

## 交付文档

- `docs/需求说明.md`
- `docs/数据库设计.md`
- `docs/接口说明.md`
- `docs/AI功能说明.md`
- `docs/部署说明.md`
- `docs/演示流程.md`
- `docs/references复用说明.md`
