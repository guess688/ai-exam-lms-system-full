# 可直接集成候选清单

说明：这里的“可直接集成候选”包含四类落地方式：复制代码、迁移模型、封装服务、重写。用户已确认 references 涉及项目/模块均取得二次开发许可；因此本表的风险等级主要反映技术栈、依赖、维护和集成成本，授权状态不再作为当前阶段阻塞项。当前审查结论仍是不建议直接复制整套模块；低风险候选主要是字段设计、JSON 合约、Prompt 思路、页面交互和独立服务封装。

| 模块来源 | 可复用内容 | 复用方式 | 目标位置 | 需要修改的地方 | 风险等级 |
| --- | --- | --- | --- | --- | --- |
| Frappe LMS `lms_course` | 课程标题、简介、分类、标签、讲师、封面、发布状态、章节列表、报名/证书等字段设计 | 迁移模型 / 重写 | `backend` 课程表 migration、课程实体、课程管理接口 | 字段命名改为主项目风格；去除 Frappe DocType、权限和发布机制 | 低 |
| Frappe LMS `course_chapter`、`course_lesson` | 课程-章节-课时层级，Lesson 关联正文、测验、视频、作业、资料 | 迁移模型 / 重写 | `backend` 的 `course_chapter`、`course_lesson`、`course_resource`；`frontend` 课程章节页 | 改为 MyBatis/JPA 模型；资源类型枚举适配主项目；补 SQL migration | 低 |
| Frappe LMS `lms_quiz` | 测验次数、通过率、时长、随机题、负分、题目表 | 迁移模型 / 重写 | `backend` 练习/小测服务，复用现有试卷/考试服务 | 与现有 `exam/paper/question` 模型做兼容映射；不引入 Frappe 评分代码 | 中 |
| Frappe LMS `lms_assignment` | 作业类型、提交、评分、参考答案、课程/课时关联 | 迁移模型 / 重写 | `backend` 学习任务模块；`frontend` 任务发布和提交页面 | 精简为考试任务、练习任务、网课任务、错题订正任务、复习任务 | 中 |
| Frappe LMS `lms_course_progress`、`lms_video_watch_duration` | 课时完成状态、课程/章节/课时维度进度、视频观看时长 | 迁移模型 / 重写 | `backend` 学习进度表；`frontend` 学生网课进度页 | 改成学生、班级、课程、章节、视频维度；加入完成率统计 | 低 |
| Frappe LMS 课程详情和后台课程编辑页 | 课程详情、课程编辑、课程发布、学生进度页面的信息架构 | 重写 | `frontend/src/views` 或现有路由目录下的课程管理/学习页 | 使用现有 Vue、Element UI/本项目 UI 风格、接口封装和路由守卫 | 低 |
| Moodle `mod/quiz` | Quiz 时间窗口、答题时长、尝试次数、评分方式、题目槽位、反馈策略 | 迁移模型 / 重写 | `backend` 自动组卷、练习、考试规则服务 | 保留主项目考试流程；只补规则字段和服务逻辑 | 中 |
| Moodle Question bank | 题目分类、题库条目、题目版本、答案、题目引用、答题步骤模型 | 迁移模型 / 重写 | `backend` 题库扩展表、题目版本或审核记录表 | 先做课程/章节/知识点/难度扩展；版本化可后置 | 中 |
| Moodle Completion | 手动/自动完成、活动完成、课程完成、通过成绩、ALL/ANY 聚合 | 重写 | `backend` 网课进度和学习任务完成规则 | 简化为视频进度、资料查看、考试/练习完成、错题订正完成 | 中 |
| Moodle Progress/Completion Report | 完成率、活动进度、筛选维度和统计口径 | 重写 | `backend` 统计接口；`frontend` 图表分析页 | 用 ECharts 或现有图表库实现；不引入 Moodle ReportBuilder | 低 |
| LearnHouse Course/Chapter/Activity 模型 | Course、Chapter、Activity 的内容类型枚举、锁定状态、发布状态、extra metadata | 迁移模型 / 重写 | `backend` 课程内容模型；`frontend` 课程学习页 | 去掉多组织/支付/企业锁定；活动类型压缩为视频、资料、测验、作业 | 中 |
| LearnHouse 学生学习页 | 课程目录、当前活动、上一节/下一节、完成按钮、锁定提示、作业入口 | 重写 | `frontend` 网课学习页 | 改成 Vue；复用主项目导航、权限、API 封装 | 低 |
| LearnHouse 作业模块 | 任务类型、自动评分白名单、提交状态、重试、答案展示、任务表现统计 | 迁移模型 / 重写 | `backend` 学习任务/练习任务；`frontend` 任务详情和统计页 | 与项目目标中的学习任务类型合并，暂不实现代码题沙箱 | 中 |
| LearnHouse Analytics | `course_view`、`activity_view`、`activity_completed`、`time_on_activity` 事件及漏斗/完成率指标 | 重写 | `backend` 学习行为日志表和统计服务；`frontend` 学情分析页 | 不引入 Tinybird；用 MySQL 聚合或后续独立分析表 | 中 |
| LearnHouse AI Course Planning | 课程规划 JSON：课程、章节、活动、suggested blocks | 重写 | `backend` AI 课程/章节生成草稿接口；`frontend` 教师 AI 助手页面 | Prompt 中文化；输出进入教师确认流程，不直接写入正式课程 | 中 |
| Studyield `AiService` | OpenRouter/OpenAI 双客户端、模型配置、JSON response format、fallback 思路 | 重写 | `backend` AI 服务层配置和 Provider 抽象 | 改为 Java/SpringBoot；API Key 走配置/环境变量；支持 mock 模式 | 中 |
| Studyield `quiz-generator` | 题型、难度、重点主题、题目解释的 JSON 合约 | 重写 | `backend` AI 自动出题 DTO、Prompt、审核队列表 | 题型改为单选、多选、不定项、判断；加入课程/章节/知识点 | 中 |
| Studyield `exam-clone` | 试卷风格分析、按模板生成题、topic/difficulty 表现、adaptive questions | 重写 | `backend` AI 出题、AI 推荐练习、难度表现分析 | 不做“整卷克隆”优先级；抽题目生成和个性化练习逻辑 | 高 |
| Studyield review queue | 错题复习队列、SM-2 变体、next_review_at、ease_factor | 重写 | `backend` 错题订正/复习任务模块 | 与现有错题本打通；避免游戏化/排行榜扩散 | 中 |
| Studyield Teach-back | 准确性、清晰度、完整性、理解程度、误区、建议、追问的报告结构 | 重写 | `backend` AI 学生个人报告、学习行为评价；`frontend` 报告页 | 改成考试/网课学情上下文；Prompt 中文化；输出可追溯 | 中 |
| Studyield Learning Paths | 学习路径步骤 JSON：study/quiz/practice/review、预计时间、完成状态 | 重写 | `backend` AI 推荐练习/复习任务；`frontend` 学习任务页面 | 与任务发布模块合并，不单独做完整学习路径产品 | 中 |
| Studyield Knowledge Base | 文档切块、embedding、RAG 搜索、资料问答设计 | 重写 / 封装服务 | 后续可放 `external/rag-service` 或 `backend` AI 扩展 | 当前阶段不优先；若实现需评估 Qdrant/向量库运维成本 | 高 |
| mcq_generator `prompt_builder.py` | MCQ 质量要求、难度定义、动态选项、多个正确答案、判断题 Prompt | 封装服务 / 复制代码 / 重写 | `external/mcq-generator-service` 或 `backend` AI 出题服务 | 授权状态归档；Prompt 中文化；输出改成主项目题目审核 DTO；补 HTTP 服务化和异常处理 | 中 |
| mcq_generator `QuestionConfig` 和 CLI 参数 | field、subfield、difficulty、count、options、correct_answers、provider、model、max_tokens | 封装服务 / 重写 | `backend` AI 出题请求 DTO；可选 `external` Python HTTP API | 增加课程、章节、知识点、题型；API Key 走环境变量 | 中 |
| mcq_generator JSON 输出 | `field`、`subfield`、`generated_at`、`question_count`、`questions`，题目含 options、correct_answer | 迁移模型 / 重写 | `backend` AI 题目草稿表、教师审核接口 | 增加解析校验、难度、题型、解析、知识点、来源模型 | 中 |

## 仍不建议盲目复制整套代码的内容

| 来源 | 原因 |
| --- | --- |
| Frappe LMS 后端 DocType/Python 代码 | 强依赖 Frappe 运行时、权限和数据库约定 |
| Moodle PHP 代码 | 技术栈完全不同，插件体系过重 |
| LearnHouse React/Next 组件 | 前端框架、路由、状态管理和 UI 体系不同 |
| Studyield NestJS 模块源码 | 技术栈不同，依赖较重；适合抽 AI 合约、Prompt 和业务规则 |
| mcq_generator 源码 | 可作为 `external` 独立服务候选；不建议直接塞进 SpringBoot 业务代码 |
