# references 到主项目功能映射

本文件把参考仓库中的可用思想映射到“AI 智能考试与网课学情分析系统”的目标功能，作为后续阶段开发索引。

## 功能映射总表

| 主项目目标功能 | 主要参考来源 | 可参考内容 | 推荐落地位置 | 落地方式 |
| --- | --- | --- | --- | --- |
| 课程体系 | Frappe LMS、LearnHouse、Moodle | 课程字段、发布状态、课程介绍、标签、作者/讲师、课程章节结构 | `backend` 课程模块、`frontend` 课程管理页 | 抽模型后重写 |
| 章节体系 | Frappe LMS、LearnHouse | 章节顺序、章节描述、章节下课时/活动列表、锁定状态 | `backend` 章节表、`frontend` 章节编辑器 | 抽模型后重写 |
| 知识点体系 | Moodle Question bank、Studyield analytics | 题库分类、topic performance、知识点表现统计 | `backend` 知识点表、题目关联表 | 重写 |
| 题目难度 | Moodle Quiz、Studyield、mcq_generator | easy/medium/hard 难度、难度分布、难度表现 | 题库实体和 SQL migration | 重写 |
| 单选/多选/不定项/判断题 | mcq_generator、Studyield Quiz、Moodle Question bank | 动态选项、多个正确答案、判断题输出格式 | 题型枚举、题目校验、AI 题目草稿 | 重写 |
| 自动组卷 | Moodle Quiz slots、Frappe Quiz、Studyield Quiz | 题目槽位、随机题、按难度和主题生成/抽题 | `backend` 组卷服务 | 重写 |
| 考后章节掌握度 | Moodle report/completion、Studyield analytics | completion、topicPerformance、attempt analysis | `backend` 学情统计接口 | 重写 |
| 考后知识点掌握度 | Moodle Question bank、Studyield analytics | topic correct/total/percentage | `backend` 知识点统计服务 | 重写 |
| 高频错题 | 主项目错题本、Studyield review queue | 错题复习队列、next_review_at、错题优先级 | 现有错题模块扩展、学习任务模块 | 重写 |
| 题目难度表现 | Studyield exam-clone analytics、Moodle Quiz | difficultyPerformance | `backend` 考试统计服务、`frontend` 图表页 | 重写 |
| 图表分析页面 | LearnHouse Analytics、Moodle Reports、Studyield dashboard | 成绩分布、活动漏斗、完成率、趋势、表现矩阵 | `frontend` 学情分析页面 | 参考交互后重写 |
| 课程视频 | Frappe Lesson、LearnHouse Activity | YouTube/hosted video、视频活动、观看时长 | `backend` 课程资源和观看进度；`frontend` 学习页 | 重写 |
| 章节视频/资料 | Frappe Lesson、LearnHouse Activity | PDF/文档/视频/富文本活动类型 | `backend` 课程资源表、文件接口 | 重写 |
| 学生观看进度 | Frappe video watch duration、LearnHouse analytics | watch_time、time_on_activity、activity_completed | `backend` 学习行为日志和进度表 | 重写 |
| 学习任务发布 | Frappe Assignment、LearnHouse Assignment、Studyield Learning Paths | 任务类型、截止时间、完成状态、复习步骤 | `backend` 学习任务模块、`frontend` 任务页面 | 重写 |
| AI 服务层 | Studyield AiService、LearnHouse AI、mcq_generator LiteLLM | Provider 配置、mock/真实模型、JSON 输出、流式生成思路 | `backend` AI service/provider | 重写 |
| AI 学生个人学情报告 | Studyield Teach-back、analytics | 准确性、清晰度、完整性、理解程度、误区、建议 | `backend` AI 报告接口、`frontend` 报告页 | Prompt 重写 |
| AI 班级学情报告 | Moodle reports、Studyield analytics | 班级/群体统计、弱项、高频错题、趋势 | `backend` 班级学情报告服务 | Prompt 重写 |
| AI 教学文案 | LearnHouse Course Planning、Studyield Learning Paths | 课程规划、章节活动、学习步骤 | `backend` AI 教学文案接口 | Prompt 重写 |
| AI 试卷讲评稿 | Studyield Quiz/Exam explanation、mcq_generator explanation prompt | 题目解析、答题表现、常见误区 | `backend` AI 讲评稿服务 | Prompt 重写 |
| AI 自动出题 | mcq_generator、Studyield Quiz/Exam Clone | 出题参数、JSON 输出、难度、题型、解释 | `backend` AI 题目草稿和审核队列 | 重写或独立服务 |
| AI 推荐练习 | Studyield adaptive questions、review queue | 根据正确率选择难度、错题复习间隔 | `backend` 推荐练习服务 | 重写 |
| 学习行为评价 | LearnHouse analytics、Studyield analytics/Teach-back | 学习时长、完成率、尝试次数、趋势、反馈结构 | `backend` 学习行为评价服务 | 重写 |

## 数据模型映射建议

| 待新增/扩展模型 | 参考来源 | 建议核心字段 |
| --- | --- | --- |
| `course` | Frappe LMS、LearnHouse | id、name/title、description、cover、status、teacher_id、created_at、updated_at |
| `course_chapter` | Frappe LMS、LearnHouse | id、course_id、title/name、description、sort_order、status |
| `knowledge_point` | Moodle Question categories、Studyield topic | id、course_id、chapter_id、name、description、parent_id、sort_order |
| `course_resource` | Frappe Lesson、LearnHouse Activity | id、course_id、chapter_id、type、title、url/file_id、duration、sort_order、published |
| `learning_progress` | Frappe Course Progress、LearnHouse Trail/Analytics | student_id、course_id、chapter_id、resource_id、status、progress、watch_seconds、completed_at |
| `learning_task` | Frappe Assignment、LearnHouse Assignment、Studyield Learning Path | type、target_id、student/class scope、deadline、status、completion_rule |
| `question` 扩展 | Moodle Question bank、mcq_generator | course_id、chapter_id、knowledge_point_id、difficulty、question_type、audit_status |
| `ai_question_draft` | mcq_generator、Studyield Quiz | prompt、provider、model、raw_output、normalized_question、audit_status、teacher_id |
| `exam_learning_stat` | Studyield analytics、Moodle reports | exam_id、student_id/class_id、chapter_accuracy、knowledge_accuracy、difficulty_performance |
| `ai_report` | Studyield Teach-back | report_type、target_type、target_id、input_snapshot、content、provider、generated_at |

## 接口映射建议

| 主项目接口方向 | 参考来源 | 说明 |
| --- | --- | --- |
| `course` CRUD | Frappe/LearnHouse | 课程管理、发布状态、章节列表 |
| `course/chapter` CRUD | Frappe/LearnHouse | 章节排序、章节资源管理 |
| `knowledge-point` CRUD | Moodle | 课程/章节下知识点管理 |
| `question` 扩展接口 | Moodle/mcq_generator | 查询时支持课程、章节、知识点、题型、难度过滤 |
| `paper/auto-generate` | Moodle Quiz slots、Frappe Quiz | 按章节、知识点、题型、难度组卷 |
| `learning-progress` | Frappe/LearnHouse | 视频、资料、章节完成状态上报 |
| `learning-task` | Frappe/LearnHouse/Studyield | 发布任务、查询任务、完成任务 |
| `analysis/student` | Moodle/Studyield | 学生章节/知识点/难度/错题/趋势 |
| `analysis/class` | Moodle/Studyield | 班级维度统计和报告输入 |
| `ai/questions/generate` | mcq_generator/Studyield | 生成题目草稿，不直接入题库 |
| `ai/questions/review` | Studyield review queue 思路 | 教师审核、修改、入库 |
| `ai/reports/student` | Studyield Teach-back | 个人学情报告 |
| `ai/reports/class` | Moodle reports/Studyield analytics | 班级学情报告 |

## 前端页面映射建议

| 页面 | 参考来源 | 建议内容 |
| --- | --- | --- |
| 课程管理列表 | Frappe Course Dashboard、LearnHouse dashboard courses | 课程状态、章节数、学生数、操作入口 |
| 课程编辑页 | Frappe CourseEditor、LearnHouse EditCourseStructure | 基本信息、章节、资源、发布设置 |
| 章节/知识点管理页 | Moodle categories、Frappe chapter | 左侧课程章节树，右侧知识点和题目绑定 |
| 网课学习页 | LearnHouse Activity page、Frappe Lesson page | 目录、视频/资料区、完成按钮、上一节下一节 |
| 学习任务页 | LearnHouse Assignment、Studyield Learning Path | 任务类型、截止时间、完成状态、入口 |
| 学情分析页 | LearnHouse Analytics、Moodle report、Studyield analytics | 成绩分布、章节正确率、知识点正确率、错题排行、趋势 |
| AI 出题审核页 | Studyield Exam Clone、mcq_generator JSON | 草稿列表、题目预览、编辑、审核入库 |
| AI 报告页 | Studyield Teach-back | 报告摘要、掌握度、薄弱点、建议、推荐练习 |

## references 到 external 的进入规则

1. 只有独立工具或服务才进入 `external`。
2. 进入 `external` 前必须归档二次开发授权状态，并确认第三方依赖。
3. `external` 中的服务必须提供清晰启动方式、环境变量、HTTP 接口和输出 schema。
4. 与主项目强耦合的业务逻辑优先直接在 `backend` 或 `frontend` 中重写。
5. 不把 Moodle、Frappe、LearnHouse、Studyield 的完整运行时放入主项目。

## 近期推荐映射路线

1. 使用 Frappe/LearnHouse 的课程结构设计课程、章节、资源表。
2. 使用 Moodle 的题库分类和 Quiz 思路扩展题目属性与自动组卷规则。
3. 使用 Studyield 的 analytics 思路生成章节、知识点、难度表现统计。
4. 使用 LearnHouse 的学习页交互设计 Vue 网课页面。
5. 使用 mcq_generator/Studyield 的 JSON 合约设计 AI 题目草稿和审核流程。
6. 使用 Studyield Teach-back 的评价维度设计 AI 学情报告 Prompt。
