# Project Scan

扫描时间：2026-06-28

本次扫描只读取现有代码与配置，不修改业务代码。当前项目以 `backend` 和 `frontend` 为主开发目录，`references` 仅作为参考源码池。

## 根目录结构

```text
D:/ai-exam-lms-system/
  backend/                 SpringBoot 后端主项目
  frontend/                Vue 前端主项目
  references/              LMS / AI 学习平台参考仓库
  external/                后续评估后可迁移的外部模块暂存区
  docs/                    项目文档
  README.md                项目目标与全局开发原则
```

## 后端目录结构

后端根目录：`backend`

```text
backend/
  pom.xml
  sql/db_exam.sql
  src/main/java/cn/org/alan/exam/
    ExamApplication.java
    common/
    config/
    controller/
    converter/
    filter/
    mapper/
    model/
      dto/
      entity/
      enums/
      form/
      vo/
    service/
      impl/
    task/
    utils/
    websocket/
  src/main/resources/
    application.yml
    application-dev.yml
    application-prod.yml
    mapper/*.xml
```

### 后端启动入口

- SpringBoot 启动类：`backend/src/main/java/cn/org/alan/exam/ExamApplication.java`
- 主类：`cn.org.alan.exam.ExamApplication`
- 关键注解：
  - `@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})`
  - `@EnableAsync`
  - `@EnableScheduling`
  - `@EnableKnife4j`
  - `@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800 * 2)`

### 后端配置文件

- 主配置：`backend/src/main/resources/application.yml`
  - `spring.application.name=exam`
  - `spring.profiles.active=dev`
- 开发配置：`backend/src/main/resources/application-dev.yml`
  - 服务端口：`8080`
  - 数据库：`jdbc:mysql://127.0.0.1:3306/db_exam`
  - 用户名：`root`
  - 密码：配置文件中存在明文，文档不展开
  - Redis：`127.0.0.1:6379`
  - MyBatis-Plus：逻辑删除、SQL stdout 日志、类型别名包
  - 文件存储：OSS / MinIO
  - AI 配置：Coze、Dify、LLM、Embedding、Milvus
  - 自定义配置：`online-exam.chat-platform.type`、`online-exam.storage.type`、验证码、AI 自动阅卷开关
- 生产配置：`backend/src/main/resources/application-prod.yml`
  - 同样配置 MySQL、Redis、OSS、MinIO、注册开关

安全提醒：当前配置文件中存在数据库密码、OSS 密钥、Coze/Dify/LLM API Key 等敏感值。后续开发 AI 服务层时，应改为环境变量或外部配置文件，不要继续扩散到文档或代码。

### 后端技术栈

- Spring Boot `2.1.3.RELEASE`
- Java 编译目标：`1.8`
- Spring MVC
- Spring Security + 自定义 `VerifyTokenFilter`
- Spring Session + Redis
- MyBatis-Plus `3.5.5`
- MySQL Connector `8.0.33`
- Druid
- Lombok
- MapStruct
- EasyExcel / Apache POI
- Swagger2 / Knife4j
- JWT：`java-jwt`
- Hutool
- Aliyun OSS / MinIO
- WebSocket
- AI 相关依赖：Coze SDK、LangChain4j、Milvus

### 后端配置类

| 文件 | 作用 |
| --- | --- |
| `config/SecurityConfig.java` | Spring Security 规则，放行 `/api/auths/**`、Swagger、Knife4j、WebSocket，其余请求需认证 |
| `filter/VerifyTokenFilter.java` | 自定义 Token 校验过滤器，结合 Redis/session 处理 JWT |
| `config/CorsConfig.java` | CORS 跨域过滤器 |
| `config/MybatisPlusConfig.java` | MyBatis-Plus 分页插件 |
| `config/RedisConfig.java` | RedisTemplate 序列化配置 |
| `config/SwaggerConfig.java` | Swagger API 文档配置 |
| `config/WebsocketConfig.java` | WebSocket 注册配置 |
| `config/OnlineExamConfig.java` | `online-exam` 自定义配置绑定 |

## 前端目录结构

前端根目录：`frontend`

```text
frontend/
  package.json
  vue.config.js
  .env.development
  .env.production
  .env.staging
  src/
    api/
    assets/
    components/
    icons/
    layout/
    router/index.js
    store/
    styles/
    utils/
    views/
    App.vue
    main.js
    permission.js
```

### 前端启动与构建

- 包管理配置：`frontend/package.json`
- 开发启动：`npm run dev`
- 生产构建：`npm run build:prod`
- 阶段构建：`npm run build:stage`
- 默认开发端口：`9527`
- 开发代理：`/api -> http://127.0.0.1:8080`

### 前端技术栈

- Vue `2.6.10`
- Vue CLI `4.4.4`
- Vue Router `3.0.6`
- Vuex `3.1.0`
- Element UI `2.13.2`
- Axios `0.18.1`
- ECharts `4.9.0`
- Quill / vue-quill-editor
- js-cookie、jwt-decode、crypto-js
- html2canvas、jsPDF

### 前端关键文件

| 文件 | 作用 |
| --- | --- |
| `src/main.js` | Vue 应用入口 |
| `src/router/index.js` | 静态路由与角色 meta 配置 |
| `src/permission.js` | 路由前置/后置守卫，处理页面标题、标签页和进度条 |
| `src/utils/request.js` | 主 Axios 实例，使用 `Authorization` 请求头，约定后端 `Result.code === 1` 为成功 |
| `src/utils/request1.js` | 另一套请求封装，主要支持上传/下载与 `code === 0` 响应风格，当前与主封装并存 |
| `src/utils/auth.js` | Cookie/localStorage 中的 Token 与角色信息处理 |
| `src/store/modules/user.js` | 登录、登出、用户信息、角色映射 |
| `src/api/*.js` | 各业务模块接口封装 |
| `src/views/*` | 页面视图目录 |
| `src/components/*` | 通用组件、考试组件、题库选择、班级选择、上传组件等 |

## 数据库 SQL 概览

- SQL 文件：`backend/sql/db_exam.sql`
- 来源标记：Navicat 导出
- 源数据库：`db_exam`
- 源 MySQL 版本：`5.7.44`
- 字符集：`utf8mb4`
- 当前识别表数：28

主要表域：

| 域 | 表 |
| --- | --- |
| 用户/权限 | `t_user`、`t_role` |
| 班级 | `t_grade`、`t_user_grade` |
| 题库/分类/试题 | `t_category`、`t_repo`、`t_question`、`t_option` |
| 试卷/考试 | `t_exam`、`t_exam_repo`、`t_exam_grade`、`t_exam_question`、`t_exam_qu_answer` |
| 成绩/记录 | `t_user_exams_score`、`t_exercise_record`、`t_user_exercise_record`、`t_manual_score` |
| 错题本 | `t_user_book` |
| 练习任务 | `t_grade_exercise` |
| 证书 | `t_certificate`、`t_certificate_user` |
| 公告/讨论 | `t_notice`、`t_notice_grade`、`t_discussion`、`t_reply`、`t_like` |
| 统计/日志 | `t_log`、`t_user_daily_login_duration` |

详细表结构见 `docs/current_database_tables.md`。

## 已有核心模块说明

| 模块 | Controller | Entity / Table | Mapper | Service | 前端页面 / API |
| --- | --- | --- | --- | --- | --- |
| 登录/注册/验证码 | `AuthController` | `User` / `t_user` | `UserMapper`、`RoleMapper` | `IAuthService` | `views/login/*`、`api/user.js` |
| 用户管理 | `UserController` | `User` / `t_user` | `UserMapper` | `IUserService` | `views/user/*`、`api/user.js` |
| 角色 | 无单独 Controller | `Role` / `t_role` | `RoleMapper` | `IRoleService` | 登录后角色映射 |
| 班级 | `GradeController` | `Grade`、`UserGrade` | `GradeMapper`、`UserGradeMapper` | `IGradeService` | `views/class/index.vue`、`api/class_.js` |
| 分类 | `CategoryController` | `Category` / `t_category` | `CategoryMapper` | `ICategoryService` | `api/category.js` |
| 题库 | `RepoController` | `Repo` / `t_repo` | `RepoMapper` | `IRepoService` | `views/repo/index.vue`、`api/repo.js` |
| 试题/选项 | `QuestionController` | `Question`、`Option` | `QuestionMapper`、`OptionMapper` | `IQuestionService`、`IOptionService` | `views/question/*`、`api/question.js` |
| 考试/试卷 | `ExamController` | `Exam`、`ExamRepo`、`ExamGrade`、`ExamQuestion`、`ExamQuAnswer` | `ExamMapper`、`Exam*Mapper` | `IExamService`、`IExamQuestionService`、`IExamQuAnswerService` | `views/exam/*`、`api/exam.js` |
| 刷题/练习 | `ExerciseController` | `ExerciseRecord`、`UserExerciseRecord`、`GradeExercise` | `ExerciseRecordMapper`、`UserExerciseRecordMapper` | `IExerciseRecordService` | `views/exercise/*`、`api/exercise.js` |
| 考试/练习记录 | `RecordController` | `Exam`、`ExerciseRecord` | `ExamMapper`、`ExerciseRecordMapper` | `IExerciseRecordService` | `views/record/*`、`api/record.js` |
| 成绩分析 | `ScoreController` | `UserExamsScore`、`ExamQuAnswer` | `UserExamsScoreMapper`、`ExamQuAnswerMapper` | `IUserExamsScoreService`、`IExamQuAnswerService` | `views/score/*`、`api/score.js` |
| 错题本 | `UserBookController` | `UserBook` / `t_user_book` | `UserBookMapper` | `IUserBookService` | `views/userbook/*`、`api/userbook.js` |
| 阅卷/人工评分 | `AnswerController` | `ManualScore`、`ExamQuAnswer` | `ManualScoreMapper`、`ExamQuAnswerMapper` | `IManualScoreService` | `views/answer/*`、`api/answer.js` |
| AI 自动阅卷 | 无单独 Controller | `ExamQuAnswer` | `ExamQuAnswerMapper` | `IAutoScoringService` | 被考试/阅卷流程调用 |
| 证书 | `CertificateController` | `Certificate`、`CertificateUser` | `CertificateMapper`、`CertificateUserMapper` | `ICertificateService` | `views/certificate/*`、`api/certificate.js` |
| 公告 | `NoticeController` | `Notice`、`NoticeGrade` | `NoticeMapper`、`NoticeGradeMapper` | `INoticeService` | `views/notice/*`、`api/notice.js` |
| 讨论/回复/点赞 | `DiscussionController`、`ReplyController`、`LikeController` | `Discussion`、`Reply`、`Like` | `DiscussionMapper`、`ReplyMapper`、`LikeMapper` | `IDiscussionService`、`IReplyService`、`ILikeService` | `views/discuss/*`、`api/discussion.js` |
| 首页统计 | `StatController` | 多表统计 | `StatMapper` | `IStatService` | `views/dashboard/*`、`api/stat.js` |
| 登录日志 | `LogController` | `Log` / `t_log` | `LogMapper` | `ILogService` | `views/log/index.vue`、`api/log.js` |
| 文件上传 | `FileController` | 无单独业务表 | - | `IFileService` | `components/FileUpload/*` |

## 可以复用的功能

1. 登录、注册、验证码、JWT、Redis Session、角色识别。
2. 用户、角色、班级、教师加入/退出班级、学生加入/退出班级。
3. 题库、分类、试题、选项、Excel 导入、图片上传。
4. 试卷/考试创建、按题型配置数量和分值、绑定班级、开始考试、提交答案。
5. 客观题自动判分、简答题人工阅卷、已有 AI 自动阅卷服务入口。
6. 成绩分页、班级成绩、成绩导出、试题答题分析。
7. 错题本、重刷、考试记录、练习记录。
8. 首页统计、日志、公告、讨论、证书。
9. 前端 Element UI 管理后台布局、角色路由、Axios 封装、ECharts 能力。

## 需要新增的功能

1. 课程、章节、知识点数据模型与管理页面。
2. 题目扩展字段：课程、章节、知识点、难度。
3. 不定项选择题支持，并明确与多选题的判分规则差异。
4. 按章节、知识点、题型、难度自动组卷。
5. 学生/班级章节掌握度、知识点掌握度、高频错题、难度表现统计。
6. 图表分析页面：成绩分布、章节正确率、知识点正确率、错题排名、成绩趋势。
7. 轻量网课：课程视频、章节视频、资料、观看进度、完成率。
8. 学习任务发布：考试、练习、网课、错题订正、复习。
9. AI 服务层统一封装：mock 模式、真实大模型配置、Prompt 管理、调用日志。
10. AI 学情报告、班级报告、教学文案、试卷讲评稿、自动出题、推荐练习、学习行为评价。
11. AI 生成题目教师审核流，审核通过后才能入库。
12. 配置安全治理：数据库密码、OSS Key、AI Key 外置化。

## 推荐后续开发顺序

1. 原项目运行基线：确认后端启动、前端启动、数据库导入、登录/考试主流程可跑通。
2. 数据库规范化补充：建立后续 SQL migration 目录和命名规范。
3. 课程、章节、知识点基础表与管理接口/页面。
4. 题目字段扩展：关联课程/章节/知识点/难度，兼容旧题目。
5. 自动组卷：先支持章节、知识点、题型、难度筛选，再接入考试创建。
6. 学情统计：先基于考试答案生成学生/班级掌握度，再扩展图表页面。
7. 轻量网课：视频、资料、观看进度、完成率。
8. 学习任务：统一任务模型，串联考试、练习、网课、错题订正、复习。
9. AI 服务层：先 mock，再真实模型；先报告/讲评稿，再 AI 出题。
10. AI 出题审核与推荐练习：确保教师审核后入库，避免 AI 题目直接污染题库。

每个阶段完成后输出：修改文件清单、数据库变化、接口清单、前端页面变化、运行测试方式、已知问题。
