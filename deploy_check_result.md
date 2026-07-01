# Docker 部署阶段一检查结果

检查时间：2026-06-30

本阶段目标：只检查当前 SpringBoot + Vue 项目的部署信息，不修改业务代码，不实施 Docker 改造。

## 一、后端检查结果

| 检查项 | 当前结果 |
| --- | --- |
| SpringBoot 启动类位置 | `backend/src/main/java/cn/org/alan/exam/ExamApplication.java` |
| 启动类标识 | `@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})`，入口为 `SpringApplication.run(ExamApplication.class, args)` |
| `pom.xml` 位置 | `backend/pom.xml` |
| Spring Boot 版本 | `2.1.3.RELEASE` |
| Java 版本 | 项目配置为 Java 8：`<java.version>1.8</java.version>`、`maven.compiler.source/target=1.8` |
| Maven Wrapper | 未发现 `backend/mvnw` / `backend/mvnw.cmd` |
| Maven 构建命令 | 建议使用 `cd backend && mvn clean package -DskipTests` |
| jar 包输出路径 | 按 `artifactId=exam`、`version=1.0-SNAPSHOT`，构建后应为 `backend/target/exam-1.0-SNAPSHOT.jar` |
| 当前 jar 是否存在 | 当前 `backend/target` 下未发现 `exam-1.0-SNAPSHOT.jar`，只有 `classes`、`generated-sources`、`maven-status` 等构建中间目录 |
| 后端默认端口 | `8080`，配置在 `application-dev.yml` 和 `application-prod.yml` |
| 主配置文件 | `backend/src/main/resources/application.yml` |
| 当前默认 profile | `application.yml` 中为 `spring.profiles.active: dev` |
| 开发配置文件 | `backend/src/main/resources/application-dev.yml` |
| 生产配置文件 | `backend/src/main/resources/application-prod.yml` |
| 数据库连接配置位置 | `application-dev.yml`、`application-prod.yml` 的 `spring.datasource` |
| 当前数据库名 | `db_exam` |
| 当前数据库连接写法 | dev/prod 都是 `jdbc:mysql://127.0.0.1:3306/db_exam?...` |
| Redis 是否使用 | 使用。`pom.xml` 包含 `spring-boot-starter-data-redis`、`spring-session-data-redis`，且存在 `RedisConfig` 与 `StringRedisTemplate` 使用 |
| Redis 配置位置 | `application-dev.yml`、`application-prod.yml` 的 `spring.redis` |
| JWT 配置位置 | `application-dev.yml`、`application-prod.yml` 的 `jwt.secret`、`jwt.expiration`、`jwt.refreshThreshold` |
| JWT 读取位置 | `backend/src/main/java/cn/org/alan/exam/utils/JwtUtil.java` 通过 `@Value("${jwt.*}")` 读取 |
| AI 配置位置 | `application-dev.yml`、`application-prod.yml` 的 `ai.provider/baseUrl/apiKey/model/enabled/mockEnabled/questionGeneratorServiceUrl` |
| AI 配置绑定类 | `backend/src/main/java/cn/org/alan/exam/config/AiProperties.java` |
| Prompt 资源位置 | `backend/src/main/resources/prompts` |
| 文件上传接口 | `backend/src/main/java/cn/org/alan/exam/controller/FileController.java`，路径为 `/api/upload/image`、`/api/upload/file` |
| 文件上传存储配置位置 | `application-dev.yml`、`application-prod.yml` 的 `online-exam.storage.type`、`oss.*`、`minio.*` |
| 当前上传存储方式 | 当前配置为 `online-exam.storage.type: minio`，也支持 `aliyun` |
| 当前是否有本地 `/uploads` 配置 | 未发现本地 uploads 目录或 `upload.path` 配置；当前返回 MinIO/OSS URL |
| 现有后端 Dockerfile | 存在 `backend/Dockerfile`，但当前写法为 `openjdk:17` + `ADD target/exam-1.0-SNAPSHOT.jar exam.jar`，后续需要按标准方案重新评估 |

### 后端 Docker 化风险点

1. `application-prod.yml` 中仍写死 `127.0.0.1` 数据库、Redis、MinIO 地址，不适合容器网络。
2. `application-dev.yml` 中存在明文第三方 AI/平台配置；后续 Docker 镜像不能打入真实密钥。
3. `application-prod.yml` 中 JWT、数据库、Redis、OSS、MinIO 配置仍有明文默认值，后续应改为环境变量。
4. 项目配置 Java 8，但本机 Maven 当前运行在 Java 24；现有 `backend/Dockerfile` 使用 OpenJDK 17。后续 Docker 构建需统一 JDK 版本策略。
5. 当前上传文件走 MinIO/OSS，没有本地 `/uploads` volume 方案；若按部署原则提供 `/uploads/`，需要补本地文件存储实现或增加 MinIO 服务访问设计。

## 二、前端检查结果

| 检查项 | 当前结果 |
| --- | --- |
| `package.json` 位置 | `frontend/package.json` |
| 前端框架 | Vue 2.6.10 + Vue Router 3.0.6 + Vuex 3.1.0 + Element UI 2.13.2 |
| 包管理器 | 项目脚本以 npm 为主；未发现 `package-lock.json`、`yarn.lock`、`pnpm-lock.yaml` |
| 本机 Node/npm | 当前本机 `node v20.19.4`、`npm 11.5.2` |
| 开发命令 | `cd frontend && npm run dev` |
| 生产构建命令 | `cd frontend && npm run build:prod` |
| dist 输出目录 | `frontend/dist`，由 `vue.config.js` 的 `outputDir: 'dist'` 配置 |
| 静态资源目录 | `frontend/dist/static`，由 `vue.config.js` 的 `assetsDir: 'static'` 配置 |
| publicPath | 当前为 `publicPath: './'` |
| axios/request 主封装 | `frontend/src/utils/request.js` |
| 另一个请求工具 | `frontend/src/utils/request1.js` 存在，但当前 API 文件主要引用 `@/utils/request` |
| API baseURL | `request.js` 使用 `process.env.VUE_APP_BASE_API` |
| 生产 API 配置 | `frontend/.env.production` 中 `VUE_APP_BASE_API = '/api'` |
| 开发 API 配置 | `frontend/.env.development` 中 `VUE_APP_BASE_API = '/api'` |
| staging API 配置 | `frontend/.env.staging` 中 `VUE_APP_BASE_API = '/stage-api'` |
| 开发代理 | `frontend/vue.config.js` 中 `/api` 代理到 `http://127.0.0.1:8080` |
| 是否写死 localhost / 127.0.0.1 | 有。`vue.config.js` 开发代理写死 `127.0.0.1:8080`；`frontend/src/utils/websocket.js` 写死 `ws://localhost:8080/websocket` |
| 是否写死服务器 IP | 前端源码未发现业务 API 服务器 IP，但存在外部图片/OSS 示例 URL |
| Vue Router 是否 history 模式 | 否。`mode: 'history'` 被注释，当前为默认 hash 模式 |
| 生产根路径部署适配 | 生产 API 已是 `/api`，但 `publicPath: './'` 更偏相对路径部署；后续 Nginx 根路径部署需确认是否改为 `/` |

### 前端 Docker 化风险点

1. 生产 API 已统一走 `/api`，符合 Nginx 反向代理方向。
2. WebSocket 地址目前写死 `ws://localhost:8080/websocket`，容器/公网部署会失效，后续应改为基于当前域名或环境变量生成。
3. `vue.config.js` 开发代理写死 `127.0.0.1`，不影响 dist 产物，但不适合容器内开发代理。
4. 当前没有锁文件，Docker 构建时依赖版本可重复性不足，后续建议生成并固定 `package-lock.json` 或明确包管理器。

## 三、数据库检查结果

| 检查项 | 当前结果 |
| --- | --- |
| 初始化 SQL 目录 | `backend/sql` |
| 基础库 SQL | `backend/sql/db_exam.sql`，另有重复内容 `backend/sql/db_exam_sql.txt` |
| 新增课程体系 SQL | `backend/sql/20260628_course_content.sql` |
| 题目课程/章节/知识点/难度 SQL | `backend/sql/20260629_question_course_difficulty.sql` |
| 网课 SQL | `backend/sql/20260629_online_course.sql` |
| 学习任务 SQL | `backend/sql/20260629_learning_task.sql` |
| AI 基础设施 SQL | `backend/sql/20260630_ai_infrastructure.sql` |
| AI 报告 SQL | `backend/sql/20260630_ai_report.sql` |
| AI 出题审核 SQL | `backend/sql/20260630_ai_generated_question.sql` |
| 演示数据 SQL | `backend/sql/20260630_demo_data.sql` |
| 是否存在统一 `init.sql` | 未发现单文件 `init.sql` |
| 数据库名 | `db_exam`，来自 SQL 注释 `Source Schema: db_exam` 和后端 datasource 配置 |
| `CREATE DATABASE` | `db_exam.sql` 未包含 `CREATE DATABASE`，需要先创建数据库或由 Docker MySQL 环境变量创建 |
| 当前本地 MySQL | `.runtime` 中运行的是 MySQL 5.7.44，本次目标 Docker 架构要求 MySQL 8 |
| 当前本地实际表数量 | 只读查询本地 `db_exam` 得到 40 张表 |
| 当前本地核心演示数据 | `t_user=3`、`t_role=3`、`t_grade=1`、`t_course=1`、`t_course_chapter=1`、`t_knowledge_point=1`、`t_question=12`、`t_exam=2`、`t_user_exams_score=2`、`t_course_video=1`、`t_learning_task=1` |

### SQL 中已发现的表

基础表来自 `db_exam.sql`：

- `t_category`
- `t_certificate`
- `t_certificate_user`
- `t_discussion`
- `t_exam`
- `t_exam_grade`
- `t_exam_qu_answer`
- `t_exam_question`
- `t_exam_repo`
- `t_exercise_record`
- `t_grade`
- `t_grade_exercise`
- `t_like`
- `t_log`
- `t_manual_score`
- `t_notice`
- `t_notice_grade`
- `t_option`
- `t_question`
- `t_reply`
- `t_repo`
- `t_role`
- `t_user`
- `t_user_book`
- `t_user_daily_login_duration`
- `t_user_exams_score`
- `t_user_exercise_record`
- `t_user_grade`

新增模块表来自迁移脚本：

- `t_course`
- `t_course_chapter`
- `t_knowledge_point`
- `t_course_video`
- `t_course_material`
- `t_student_video_progress`
- `t_learning_task`
- `t_learning_task_record`
- `t_ai_call_log`
- `t_ai_question_review`
- `ai_report`
- `ai_generated_question`

### 表结构完整性判断

1. 当前基础考试、用户、班级、题库、试卷、成绩、错题、课程、网课、学习任务、AI 报告、AI 出题相关表结构在 SQL 文件中均可找到。
2. 未发现独立的 `exam_chapter_analysis`、`exam_knowledge_analysis`、`student_chapter_analysis`、`student_knowledge_analysis` 建表脚本。
3. 代码中 `backend/src/main/resources/mapper/ExamAnalysisMapper.xml` 已通过实时聚合查询实现章节掌握、知识点掌握、错题排名、难度分析和图表数据，因此当前实现不是新增统计结果表方案。
4. Docker 初始化时不能只挂载 `db_exam.sql`；还需要按顺序执行所有 `202606xx_*.sql` 迁移和演示数据脚本，或后续生成一份统一 `init.sql`。

## 四、阶段一结论

当前项目可以进入 Docker 标准化改造，但需要先处理以下部署适配点：

1. 后端生产配置需要改为环境变量读取，至少包括数据库、Redis、JWT、AI、OSS/MinIO/上传配置。
2. 前端生产 API 已是 `/api`，适合 Nginx 统一入口；WebSocket 地址仍需改造。
3. 数据库初始化脚本分散，后续应生成 Docker 使用的统一初始化目录或统一 `init.sql`。
4. 当前上传文件不是本地 `/uploads`，与目标架构的 `/uploads/` volume 访问路径不完全一致，需要在下一阶段明确存储实现。
5. Redis 已被项目使用，Docker 架构应启用 Redis 容器。

## 五、本阶段输出

### 修改文件清单

- 无业务代码修改。

### 新增文件清单

- `deploy_check_result.md`

### 配置项说明

- 本阶段仅检查配置，未新增配置项。
- 已确认后续需要环境变量化的配置：`SPRING_DATASOURCE_*`、`SPRING_REDIS_*`、`JWT_SECRET`、`AI_*`、`MINIO_*` / `OSS_*`、上传路径或存储类型。

### 构建命令

本阶段未执行构建。当前项目可用构建命令如下：

```bash
cd backend
mvn clean package -DskipTests

cd ../frontend
npm run build:prod
```

### 启动命令

本阶段未新增启动命令。当前非 Docker 启动方式可参考：

```bash
cd backend
mvn spring-boot:run

cd frontend
npm run dev
```

后续 Docker 阶段应提供 `docker compose up -d --build`。

### 测试方式

本阶段执行的是只读检查：

- 文件扫描：`rg --files`、`rg -n`
- 配置读取：`application.yml`、`application-dev.yml`、`application-prod.yml`、`package.json`、`vue.config.js`
- 本地数据库只读检查：查询 `information_schema.tables` 和关键演示表行数

### 已知问题

1. `application-prod.yml` 还不适合直接用于 Docker 生产部署。
2. 前端 WebSocket 地址写死 localhost。
3. 当前没有统一 Docker 初始化 SQL。
4. 当前没有前端依赖锁文件。
5. 项目 Java 编译目标、现有 Dockerfile JDK、本机 Maven JDK 三者不一致，需要下一阶段统一。
