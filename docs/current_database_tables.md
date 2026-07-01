# Current Database Tables

SQL 文件：`backend/sql/db_exam.sql`

来源信息：

- 源库：`db_exam`
- 源 MySQL：`5.7.44`
- 导出工具：Navicat
- 文件编码：UTF-8
- 表数量：28
- 字符集：多数表为 `utf8mb4` / `utf8mb4_bin`

## 表概览

| 表 | 对应实体 | 主键 | 业务含义 | 关键字段 |
| --- | --- | --- | --- | --- |
| `t_category` | `Category` | `id` | 题库分类 | `name`、`parent_id`、`sort`、`create_time`、`is_deleted` |
| `t_certificate` | `Certificate` | `id` | 证书模板 | `certificate_name`、`image`、`certification_nuit`、`create_time`、`is_deleted` |
| `t_certificate_user` | `CertificateUser` | `id` | 用户证书记录 | `user_id`、`exam_id`、`code`、`certificate_id`、`create_time` |
| `t_discussion` | `Discussion` | `id` | 班级讨论主题 | `user_id`、`grade_id`、`title`、`content`、`create_time` |
| `t_exam` | `Exam` | `id`、`passed_score` | 考试/试卷主体 | `title`、`exam_duration`、`gross_score`、各题型数量/分值、`start_time`、`end_time` |
| `t_exam_grade` | `ExamGrade` | `id` | 考试与班级关联 | `exam_id`、`grade_id` |
| `t_exam_qu_answer` | `ExamQuAnswer` | `id` | 学生考试答题记录 | `user_id`、`exam_id`、`question_id`、`answer_id`、`answer_content`、`is_right`、`ai_score`、`ai_reason` |
| `t_exam_question` | `ExamQuestion` | `id` | 考试与试题关联 | `exam_id`、`question_id`、`score`、`sort`、`type` |
| `t_exam_repo` | `ExamRepo` | `id` | 考试与题库关联 | `exam_id`、`repo_id` |
| `t_exercise_record` | `ExerciseRecord` | `id` | 刷题答题明细 | `repo_id`、`question_id`、`user_id`、`answer`、`question_type`、`options`、`is_right` |
| `t_grade` | `Grade` | `id` | 班级 | `grade_name`、`user_id`、`code`、`is_deleted` |
| `t_grade_exercise` | `GradeExercise` | `id` | 班级刷题任务关联 | `repo_id`、`grade_id`、`user_id`、`create_time` |
| `t_like` | `Like` | `id` | 讨论/回复点赞 | `discussion_id`、`reply_id`、`user_id`、`create_time` |
| `t_log` | `Log` | `id` | 登录/行为日志 | `place`、`behavior`、`device`、`user_id`、`create_time` |
| `t_manual_score` | `ManualScore` | `id` | 人工阅卷记录 | `exam_qu_answer_id`、`user_id`、`score`、`create_time` |
| `t_notice` | `Notice` | `id` | 公告 | `title`、`image`、`content`、`user_id`、`is_public`、`is_deleted` |
| `t_notice_grade` | `NoticeGrade` | `id` | 公告与班级关联 | `notice_id`、`grade_id`、`is_deleted` |
| `t_option` | `Option` | `id` | 题目选项 | `qu_id`、`is_right`、`image`、`content`、`sort`、`is_deleted` |
| `t_question` | `Question` | `id` | 试题 | `qu_type`、`image`、`content`、`analysis`、`repo_id`、`user_id`、`is_deleted` |
| `t_reply` | `Reply` | `id` | 讨论回复 | `discussion_id`、`user_id`、`parent_id`、`content`、`create_time` |
| `t_repo` | `Repo` | `id` | 题库 | `user_id`、`title`、`category_id`、`is_exercise`、`create_time`、`is_deleted` |
| `t_role` | `Role` | `id` | 角色 | `role_name`、`code` |
| `t_user` | `User` | `id` | 用户 | `user_name`、`real_name`、`password`、`avatar`、`role_id`、`grade_id`、`status`、`is_deleted` |
| `t_user_book` | `UserBook` | `id` | 错题本 | `exam_id`、`user_id`、`qu_id`、`create_time` |
| `t_user_daily_login_duration` | `UserDailyLoginDuration` | `id` | 用户每日在线时长 | `user_id`、`login_date`、`total_seconds` |
| `t_user_exams_score` | `UserExamsScore` | `id` | 用户考试成绩 | `user_id`、`exam_id`、`total_time`、`user_time`、`user_score`、`limit_time`、`count`、`state`、`whether_mark` |
| `t_user_exercise_record` | `UserExerciseRecord` | `id` | 用户刷题进度 | `user_id`、`repo_id`、`total_count`、`exercise_count`、`create_time` |
| `t_user_grade` | `UserGrade` | `id` | 用户与班级/教师关联 | `u_id`、`g_id`、`is_deleted` |

## 核心关系

### 用户与权限

- `t_user.role_id -> t_role.id`
- `t_user.grade_id -> t_grade.id`
- `t_user_grade` 额外表达用户与班级/教师的关联关系，字段命名为 `u_id`、`g_id`，后续扩展前需要确认真实含义。

### 班级与考试

- `t_exam_grade.exam_id -> t_exam.id`
- `t_exam_grade.grade_id -> t_grade.id`
- 教师创建考试后通过 `t_exam_grade` 分发给班级。

### 题库与试题

- `t_repo.category_id -> t_category.id`
- `t_question.repo_id -> t_repo.id`
- `t_option.qu_id -> t_question.id`
- 现有题型枚举：`1=单选题`、`2=多选题`、`3=判断题`、`4=简答题`。

### 考试与试题

- `t_exam_repo.exam_id -> t_exam.id`
- `t_exam_repo.repo_id -> t_repo.id`
- `t_exam_question.exam_id -> t_exam.id`
- `t_exam_question.question_id -> t_question.id`
- `t_exam_question.score` 保存该题在本次考试中的分值。

### 答题、成绩与错题

- `t_exam_qu_answer` 保存考试答题明细。
- `t_user_exams_score` 保存考试总成绩和状态，`state=0` 表示考试中，`state=1` 表示已交卷。
- `t_user_book` 保存错题本。
- `t_manual_score` 保存人工评分。
- `t_exercise_record` 和 `t_user_exercise_record` 保存刷题明细与刷题进度。

## 后续扩展需要新增或调整的表

建议后续通过 SQL migration 或 init SQL 增量提供：

| 目标 | 建议新增/调整 |
| --- | --- |
| 课程体系 | `t_course`、`t_course_chapter`、`t_knowledge_point` |
| 题目扩展 | 在 `t_question` 增加 `course_id`、`chapter_id`、`knowledge_point_id`、`difficulty`，或用关联表支持多知识点 |
| 不定项选择题 | 扩展 `qu_type` 枚举，或增加题型字典表 |
| 自动组卷 | `t_paper_rule`、`t_paper_rule_detail`，或在考试创建表单中保存规则快照 |
| 学情统计 | `t_learning_mastery_student`、`t_learning_mastery_grade`、`t_question_error_stat`，也可先用视图/查询生成 |
| 网课 | `t_course_video`、`t_course_material`、`t_video_watch_progress` |
| 学习任务 | `t_learning_task`、`t_learning_task_target`、`t_learning_task_progress` |
| AI 服务 | `t_ai_prompt_template`、`t_ai_generation_record`、`t_ai_question_review`、`t_ai_report` |

## 注意事项

- `t_exam` 当前主键是 `PRIMARY KEY (id, passed_score)`，与常规单主键设计不同；后续改造前需要确认是否历史遗留。
- 当前没有课程、章节、知识点、难度字段。
- 当前没有不定项选择题枚举。
- SQL 文件包含初始化数据，导入时要确认是否覆盖现有环境。
- 后续不要直接修改原始导出 SQL，建议新建 migration 文件。
