# Current API Overview

后端 Controller 目录：`backend/src/main/java/cn/org/alan/exam/controller`

前端 API 封装目录：`frontend/src/api`

前端基础路径：`.env.*` 中 `VUE_APP_BASE_API=/api`

后端 Controller 路径大多已包含 `/api` 前缀，例如 `/api/exams`。开发环境代理在 `frontend/vue.config.js` 中将 `/api` 转发到 `http://127.0.0.1:8080`。

## 通用约定

- 通用响应类：`cn.org.alan.exam.common.result.Result`
- 成功：`code=1`
- 失败：`code=0`
- 主前端请求封装：`frontend/src/utils/request.js`
  - 自动携带 `Authorization` 请求头。
  - 响应拦截器以 `res.code !== 1` 判断失败。
  - 如果响应头有新的 `authorization`，会更新本地 Token。
- 另一套封装：`frontend/src/utils/request1.js`
  - 以 `code === 0` 判断成功。
  - 同时封装 `upload`、`download`。
  - 与主封装风格不一致，后续新增接口建议优先使用 `request.js`，避免继续扩大双风格。

## 安全与鉴权

- 配置文件：`backend/src/main/java/cn/org/alan/exam/config/SecurityConfig.java`
- 放行路径：
  - `/api/auths/**`
  - Swagger / Knife4j 相关路径
  - `/doc.html`
  - `/ws/**`
  - `/ws-app/**`
- 其他接口需要认证。
- Token 校验过滤器：`backend/src/main/java/cn/org/alan/exam/filter/VerifyTokenFilter.java`
- 前端登录后在 `frontend/src/store/modules/user.js` 中根据 JWT payload 的 `roleId` 映射：
  - `1 -> student`
  - `2 -> teacher`
  - `3 -> admin`

## 后端 Controller 总览

| Controller | Base Path | 主要接口 | 对应模块 |
| --- | --- | --- | --- |
| `AuthController` | `/api/auths` | `POST /login`、`DELETE /logout`、`POST /register`、`GET /captcha`、`POST /verifyCode/{code}`、`POST /track-presence` | 登录、注册、验证码、在线时长 |
| `UserController` | `/api/user` | `GET /info`、`POST /`、`PUT /`、`DELETE /{ids}`、`POST /import`、`PUT /grade/join`、`GET /paging`、`PUT /uploadAvatar` | 用户管理、个人信息、加入班级 |
| `GradeController` | `/api/grades` | `GET /paging`、`POST /add`、`PUT /update/{id}`、`DELETE /delete/{id}`、`PATCH /remove/{ids}`、`GET /list`、`GET /teacher/join`、`DELETE /teacher/exit/{gradeId}`、`PUT /user/exit` | 班级管理 |
| `CategoryController` | `/api/category` | `POST /`、`PUT /{id}`、`DELETE /{id}`、`GET /tree`、`GET /first-level`、`GET /children/{parentId}` | 题库分类 |
| `RepoController` | `/api/repo` | `POST /`、`PUT /{id}`、`DELETE /{id}`、`GET /list`、`GET /paging`、`GET /category/{categoryId}` | 题库管理 |
| `QuestionController` | `/api/questions` | `POST /single`、`DELETE /batch/{ids}`、`GET /paging`、`GET /single/{id}`、`PUT /{id}`、`POST /import/{id}`、`POST /uploadImage` | 试题与选项 |
| `ExamController` | `/api/exams` | `POST /`、`GET /start`、`PUT /{id}`、`DELETE /{ids}`、`GET /paging`、`GET /question/list/{examId}`、`GET /question/single`、`GET /collect/{id}`、`GET /detail`、`GET /grade`、`PUT /cheat/{examId}`、`POST /full-answer`、`GET /hand-exam/{examId}`、`GET /details/{examId}` | 考试创建、考试作答、交卷、查卷 |
| `ExerciseController` | `/api/exercises` | `GET /{repoId}`、`POST /fillAnswer`、`GET /getRepo`、`GET /question/{id}`、`GET /answerInfo/{repoId}/{quId}` | 刷题中心 |
| `RecordController` | `/api/records` | `GET /exam/paging`、`GET /exam/detail`、`GET /exercise/paging`、`GET /exercise/detail` | 考试/练习记录 |
| `ScoreController` | `/api/score` | `GET /paging`、`GET /question/{examId}/{questionId}`、`GET /getExamScore`、`GET /export/{examId}/{gradeId}` | 成绩分析与导出 |
| `AnswerController` | `/api/answers` | `GET /detail`、`PUT /correct`、`GET /exam/page`、`GET /exam/stu` | 阅卷管理、答卷详情 |
| `UserBookController` | `/api/userbooks` | `GET /paging`、`GET /question/list/{examId}`、`GET /question/single/{quId}`、`POST /full-book` | 错题本与重刷 |
| `CertificateController` | `/api/certificate` | `POST /`、`GET /paging`、`PUT /{id}`、`DELETE /delete/{id}`、`GET /paging/my` | 证书管理 |
| `NoticeController` | `/api/notices` | `POST /`、`DELETE /{ids}`、`PUT /{noticeId}`、`GET /paging`、`GET /new` | 公告管理 |
| `DiscussionController` | `/api/discussion` | `POST /add`、`DELETE /delete/{id}`、`GET /query/page/owner`、`GET /query/detail/{id}`、`GET /query/page/student` | 讨论主题 |
| `ReplyController` | `/api/reply` | `POST /add`、`DELETE /delete/{id}`、`GET /query/{orderBy}/{id}` | 讨论回复 |
| `LikeController` | `/api/like` | `POST /doLike` | 点赞 |
| `StatController` | `/api/stat` | `GET /student`、`GET /exam`、`GET /allCounts`、`GET /daily` | 首页统计 |
| `LogController` | `/api/log` | `GET /` | 日志分页 |
| `FileController` | `/api/upload` | `POST /image` | 图片上传 |

## 核心模块文件定位

| 模块 | Entity | Mapper | Service | Controller |
| --- | --- | --- | --- | --- |
| 用户 | `User.java` | `UserMapper.java` / `UserMapper.xml` | `IUserService.java` / `UserServiceImpl.java` | `UserController.java` |
| 角色 | `Role.java` | `RoleMapper.java` / `RoleMapper.xml` | `IRoleService.java` / `RoleServiceImpl.java` | 无单独 Controller |
| 班级 | `Grade.java`、`UserGrade.java` | `GradeMapper.java`、`UserGradeMapper.java` | `IGradeService.java` / `GradeServiceImpl.java` | `GradeController.java` |
| 分类 | `Category.java` | `CategoryMapper.java` | `ICategoryService.java` | `CategoryController.java` |
| 题库 | `Repo.java` | `RepoMapper.java` / `RepoMapper.xml` | `IRepoService.java` / `RepoServiceImpl.java` | `RepoController.java` |
| 试题 | `Question.java`、`Option.java` | `QuestionMapper.java`、`OptionMapper.java` | `IQuestionService.java`、`IOptionService.java` | `QuestionController.java` |
| 试卷/考试 | `Exam.java`、`ExamRepo.java`、`ExamGrade.java`、`ExamQuestion.java` | `ExamMapper.java`、`ExamRepoMapper.java`、`ExamGradeMapper.java`、`ExamQuestionMapper.java` | `IExamService.java`、`IExamRepoService.java`、`IExamGradeService.java`、`IExamQuestionService.java` | `ExamController.java` |
| 答题 | `ExamQuAnswer.java` | `ExamQuAnswerMapper.java` | `IExamQuAnswerService.java` | `ExamController.java`、`AnswerController.java` |
| 成绩 | `UserExamsScore.java` | `UserExamsScoreMapper.java` / `UserExamsScoreMapper.xml` | `IUserExamsScoreService.java` | `ScoreController.java` |
| 错题本 | `UserBook.java` | `UserBookMapper.java` / `UserBookMapper.xml` | `IUserBookService.java` | `UserBookController.java` |
| 练习 | `ExerciseRecord.java`、`UserExerciseRecord.java`、`GradeExercise.java` | `ExerciseRecordMapper.java`、`UserExerciseRecordMapper.java`、`GradeExerciseMapper.java` | `IExerciseRecordService.java` | `ExerciseController.java`、`RecordController.java` |

## 前端 API 封装映射

| 文件 | 对应后端模块 |
| --- | --- |
| `api/user.js` | `/api/user`、`/api/auths`、`/api/grades/user/exit` |
| `api/class_.js` | `/api/grades` |
| `api/category.js` | `/api/category` |
| `api/repo.js` | `/api/repo` |
| `api/question.js` | `/api/questions` |
| `api/exam.js` | `/api/exams` |
| `api/exercise.js` | `/api/exercises` |
| `api/record.js` | `/api/records` |
| `api/score.js` | `/api/score` |
| `api/answer.js` | `/api/answers` |
| `api/userbook.js` | `/api/userbooks` |
| `api/certificate.js` | `/api/certificate` |
| `api/notice.js` | `/api/notices` |
| `api/discussion.js` | `/api/discussion`、`/api/reply`、`/api/like` |
| `api/reply.js` | `/api/reply` |
| `api/like.js` | `/api/like` |
| `api/stat.js` | `/api/stat` |
| `api/log.js` | `/api/log` |

## 已发现的小问题/后续注意

- `frontend/src/api/class_.js` 中删除班级 URL 为 `/grades/delete//` + id，存在双斜杠；当前只记录，不修改。
- `frontend/src/api/exam.js` 中 `exams//details//${examId}` 存在双斜杠；当前只记录，不修改。
- 前端同时存在 `request.js` 和 `request1.js` 两套响应码约定，后续新增接口应统一风格。
- 后端配置文件中存在明文敏感配置，后续应外置化。
