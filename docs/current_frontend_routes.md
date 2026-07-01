# Current Frontend Routes

路由文件：`frontend/src/router/index.js`

前端路由模式：默认 hash 模式，`mode: 'history'` 当前注释未启用。

路由守卫：`frontend/src/permission.js`

- 设置页面标题。
- 使用 NProgress。
- 将访问页面写入 `menu/ADD_TAG`。
- 旧版 Token 白名单逻辑目前大段注释，实际权限主要依赖路由 meta、登录状态和页面侧逻辑。

## 请求与代理

- `.env.development`：`VUE_APP_BASE_API=/api`
- `.env.production`：`VUE_APP_BASE_API=/api`
- `vue.config.js` 开发代理：`/api -> http://127.0.0.1:8080`
- Axios 主封装：`src/utils/request.js`
- Token 存储：`src/utils/auth.js`
- 用户 Store：`src/store/modules/user.js`

## 路由表

| 路径 | 名称 | 页面标题 | 角色 | 组件 |
| --- | --- | --- | --- | --- |
| `/login` | - | 登录页 | 公开 | `@/views/login/index` |
| `/register` | - | 注册页 | 公开 | `@/views/login/register` |
| `/404` | - | 404 | 公开 | `@/views/404` |
| `/index` | `Dashboard` | 主页 | 登录用户 | `@/views/dashboard/index` |
| `/user-management` | `user-management` | 用户管理 | `teacher`、`admin` | `@/views/user/index` |
| `/myself` | `myself` | 个人中心 | `teacher`、`admin`、`student` | `@/views/user/myself` |
| `/change-password` | `change-password` | 修改密码 | `teacher`、`admin`、`student` | `@/views/user/updatePassword.vue` |
| `/class-management` | `class-management` | 班级管理 | `teacher`、`admin` | `@/views/class/index` |
| `/discussion-management` | `discussion-management` | 讨论管理 | `teacher`、`student` | `@/views/discuss/index.vue` |
| `/discussion-detail/discussion-detail` | `discussion-detail` | 讨论详情 | `teacher`、`student` | `@/views/discuss/detail.vue` |
| `/exam-details/exam-details` | `exam-details` | 考试详情 | `teacher`、`admin` | `@/views/exam/details.vue` |
| `/discussion-block` | `discussion-block` | 投屏模式 | `teacher`、`admin` | `@/views/discuss/block.vue` |
| `/prepare-exam` | `prepare-exam` | 准备考试 | `teacher`、`admin`、`student` | `@/views/exam/examInformation.vue` |
| `/text-center` | `text-center` | 试卷中心 | `student` | `@/views/exam/student/index` |
| `/start-exam` | `start-exam` | 开始考试 | `teacher`、`admin`、`student` | `@/views/exam/index.vue` |
| `/exercise-center` | `exercise-center` | 刷题中心 | `student` | `@/views/exercise/index` |
| `/start-exercise` | `start-exercise` | 开始刷题 | `teacher`、`admin`、`student` | `@/views/exercise/exercise.vue` |
| `/record` | `record` | 考试记录 | `student` | Layout 父路由 |
| `/exam-record` | `exam-record` | 考试记录 | `student` | `@/views/record/exam/index.vue` |
| `/exercise-record` | `exercise-record` | 刷题记录 | `student` | `@/views/record/exercise/index.vue` |
| `/exam-record-detail` | `exam-record-detail` | 考试记录查看 | `teacher`、`admin`、`student` | `@/views/record/exam/newk` |
| `/exercise-record-detail` | `exercise-record-detail` | 刷题记录查看 | `teacher`、`admin`、`student` | `@/views/record/exercise/newk` |
| `/wrong-book` | `wrong-book` | 错题本 | `student` | `@/views/userbook/index` |
| `/rebrush` | `rebrush` | 重刷 | `teacher`、`admin`、`student` | `@/views/userbook/rebrush.vue` |
| `/exam-management` | `exam-management` | 考试管理 | `teacher`、`admin` | `@/views/exam/teacher/index` |
| `/exam-add` | `exam-add` | 考试添加 | `teacher`、`admin` | `@/views/exam/examAdd` |
| `/repo-management` | `repo-management` | 题库管理 | `teacher`、`admin` | `@/views/repo/index` |
| `/questions-management` | `questions-management` | 试题管理 | `teacher`、`admin` | `@/views/question/index` |
| `/questions-add` | `questions-add` | 试题添加 | `teacher`、`admin` | `@/views/question/add.vue` |
| `/certificate/certificate-management` | `certificate-management` | 证书管理 | `admin` | `@/views/certificate/CertMgr` |
| `/mycertificate` | `mycertificate` | 我的证书 | `student` | `@/views/certificate/myCertificates` |
| `/score-analysis/score-analysis` | `score-analysis` | 成绩分析 | `teacher` | `@/views/score/index` |
| `/detail/user-score` | `user-score` | 用户成绩 | `teacher`、`admin` | `@/views/score/detail` |
| `/answer-manage/marking-management` | `marking-management` | 阅卷管理 | `teacher` | `@/views/answer/index` |
| `/answer/answer-show` | `answer-show` | 答卷查看 | `teacher`、`admin` | `@/views/answer/answerck` |
| `/makeTest` | `makeTest` | 批改试卷 | `teacher`、`admin` | `@/views/answer/makeTest` |
| `/notice-management` | `notice-management` | 公告管理 | `teacher`、`admin` | `@/views/notice/notice` |
| `/login-log` | `login-log` | 登录日志 | `teacher`、`admin`、`student` | `@/views/log/index` |
| `*` | - | 404 fallback | 公开 | redirect `/404` |

说明：

- 部分子路由 `path` 以 `/` 开头，会作为绝对路径解析。
- 部分子路由未以 `/` 开头，会与父级路径组合，例如 `/score-analysis/score-analysis`。
- `hidden: true` 的路由不会显示在侧边栏，但仍可通过代码跳转访问。

## 前端页面模块

| 目录 | 作用 |
| --- | --- |
| `views/login` | 登录、注册 |
| `views/dashboard` | 首页与角色首页 |
| `views/user` | 用户管理、个人中心、修改密码 |
| `views/class` | 班级管理 |
| `views/repo` | 题库管理 |
| `views/question` | 试题管理、试题添加 |
| `views/exam` | 考试管理、试卷中心、考试详情、开始考试 |
| `views/exercise` | 刷题中心、开始刷题 |
| `views/record` | 考试记录、练习记录 |
| `views/userbook` | 错题本、重刷 |
| `views/score` | 成绩分析、用户成绩详情 |
| `views/answer` | 阅卷管理、答卷查看、批改试卷 |
| `views/certificate` | 证书管理、我的证书 |
| `views/notice` | 公告管理 |
| `views/discuss` | 讨论管理、讨论详情、投屏模式 |
| `views/log` | 登录日志 |

## 适合新增页面的路由分组建议

后续新增功能建议继续沿用现有 Layout + `meta.roles` 风格：

| 新功能 | 建议前端目录 | 建议角色 |
| --- | --- | --- |
| 课程管理 | `views/course` | `teacher`、`admin` |
| 章节管理 | `views/chapter` 或 `views/course/chapter` | `teacher`、`admin` |
| 知识点管理 | `views/knowledge` | `teacher`、`admin` |
| 自动组卷 | `views/paper-rule` 或复用 `views/exam` | `teacher`、`admin` |
| 学情分析 | `views/learning-analysis` | `teacher`、`admin`、部分学生视图 |
| 网课学习 | `views/course-player` | `student` |
| 学习任务 | `views/learning-task` | `teacher`、`admin`、`student` |
| AI 报告/讲评 | `views/ai-report` | `teacher`、`admin`、`student` |
| AI 出题审核 | `views/ai-question-review` | `teacher`、`admin` |
