# references 开源项目可直接集成性审查

审查日期：2026-06-28

本审查面向当前主项目 `backend` + `frontend`，即 SpringBoot + Vue 在线考试系统。结论先行：用户已确认 references 涉及项目/模块均取得二次开发许可，授权不再作为当前阶段阻塞项；但当前可直接复制进主项目的代码仍然很少。最佳策略是以主项目为底座，优先复用已有登录、角色、班级、题库、试卷、考试、判分、成绩和错题能力；从 references 中抽取课程/学习进度/AI 出题/分析报告的字段、接口契约、Prompt 和页面交互，再按 SpringBoot + Vue 的技术栈重写。`mcq_generator` 可以作为独立 Python AI 工具候选，建议放入 `external` 封装服务或按其接口在 Java AI 服务中重写。

## 审查标准

| 代码 | 判断标准 | 本项目处理方式 |
| --- | --- | --- |
| A | 技术栈一致，或代码足够独立，可直接复制/迁移 | 当前基本没有无风险候选 |
| B | Python AI 工具或分析工具，适合独立服务 | `mcq_generator`、部分 Studyield AI 思路可作为候选 |
| C | 技术栈不同但领域模型成熟，抽取数据模型和业务规则后重写 | Frappe LMS、Moodle、Studyield |
| D | 前端框架不同，只参考页面和交互 | LearnHouse、Frappe LMS、Studyield |
| E | 依赖复杂、改造成本高、和阶段需求无关 | Moodle 插件体系、Frappe 运行时、LearnHouse 企业能力等 |

## 总体结论

| 项目 | 技术栈 | 直接集成性 | 推荐结论 |
| --- | --- | --- | --- |
| Frappe LMS | Frappe/Python + Frappe UI/Vue | 低 | 抽课程、章节、课时、测验、作业、学习进度字段后重写；页面只参考 |
| Moodle | PHP + Moodle 插件体系 | 很低 | 抽题库、测验、成绩、完成度和报告规则；不复制代码 |
| LearnHouse | FastAPI + SQLModel + Next.js/React | 中低 | 参考网课页面、课程详情、学习页、作业和分析交互；后端模型按主项目重写 |
| Studyield | NestJS + React/Vite + PostgreSQL/Qdrant/ClickHouse | 中 | Prompt、JSON 合约、AI 出题、复习队列、学习反馈可参考重写；二开授权已确认，直接复用风险主要来自技术栈和依赖复杂度 |
| mcq_generator | Python + LiteLLM CLI | 中 | 二开授权已确认，适合封装为独立服务；也可按接口和输出格式在主项目重写 |

## Frappe LMS 审查

主要依据：

- `references/frappe-lms/lms/lms/doctype/lms_course/lms_course.json`
- `references/frappe-lms/lms/lms/doctype/course_chapter/course_chapter.json`
- `references/frappe-lms/lms/lms/doctype/course_lesson/course_lesson.json`
- `references/frappe-lms/lms/lms/doctype/lms_quiz/lms_quiz.json`
- `references/frappe-lms/lms/lms/doctype/lms_question/lms_question.json`
- `references/frappe-lms/lms/lms/doctype/lms_assignment/lms_assignment.json`
- `references/frappe-lms/lms/lms/doctype/lms_course_progress/lms_course_progress.json`
- `references/frappe-lms/lms/lms/doctype/lms_video_watch_duration/lms_video_watch_duration.json`
- `references/frappe-lms/frontend/src/pages/*`

| 审查项 | 发现 | 集成判断 |
| --- | --- | --- |
| 课程模型 | `LMS Course` 包含标题、介绍、分类、标签、讲师、封面、视频链接、发布状态、课程章节、报名和证书字段 | C。可迁移字段思想，不能直接使用 Frappe DocType |
| 章节/课程内容模型 | `Course Chapter` 关联课程，`Course Lesson` 支持正文、Quiz、YouTube、作业、SCORM、文件类型 | C。适合转成 `course`、`course_chapter`、`course_lesson` 或 `course_resource` |
| Quiz/测验模型 | `LMS Quiz` 有尝试次数、答案展示、通过率、时长、随机题、负分、题目表 | C。可映射到主项目组卷/练习/小测规则 |
| Assignment/作业模型 | 作业支持文档、PDF、URL、图片、文本；有提交表、评分配置和参考答案 | C。适合学习任务/作业任务模块重写 |
| 学习进度模型 | `LMS Course Progress` 记录 member、course、chapter、lesson、status；视频观看时长单独记录 | C。可作为网课进度表和完成率统计设计参考 |
| 视频/课程页面结构 | `Lesson.vue`、`CourseDetail.vue`、`CourseOverview.vue`、`StudentCourseProgress.vue` 等页面覆盖课程详情、课时学习、学生进度 | D。只参考交互和信息架构，不迁移组件 |
| 后台课程管理页面 | `CourseDashboard.vue`、`CourseEditor.vue`、`CourseForm.vue`、`CoursePublishSettings.vue` 等 | D。适合参考后台课程编辑、发布设置、章节编排流程 |
| 可抽取前端组件/字段/接口 | 字段设计价值高；前端依赖 Frappe Resource API 和组件生态 | C/D。抽字段和页面流程，接口按 SpringBoot Controller 重建 |

不建议直接集成 Frappe 运行时、DocType Python 控制器、Frappe 权限模型或 Frappe 前端资源 API。它们会强行引入另一套框架，和当前主项目架构冲突。

## Moodle 审查

主要依据：

- `references/moodle/public/mod/quiz/db/install.xml`
- `references/moodle/public/lib/db/install.xml`
- `references/moodle/public/completion/*`
- `references/moodle/public/report/*`
- `references/moodle/public/reportbuilder/*`

| 审查项 | 发现 | 集成判断 |
| --- | --- | --- |
| Course 课程结构 | Moodle 课程体系庞大，涉及上下文、模块、分区、活动、角色、插件 | C/E。只抽课程、活动、分区概念；不搬 Moodle 课程系统 |
| Quiz 测验结构 | `quiz` 表含时间窗口、时长、尝试次数、评分方法、答题行为、随机/分页/反馈规则；`quiz_slots` 管理题目槽位 | C。适合重写考试规则、练习规则和组卷槽位 |
| Grade 成绩结构 | Moodle gradebook 非常完整但复杂，适配成本高 | C/E。只参考成绩项、聚合方式、通过线概念 |
| Question bank 题库结构 | 有 `question_categories`、`question_bank_entries`、`question_versions`、`question_references`、`question_answers`、`question_attempts` 等 | C。题库分类、版本、题目引用和答题步骤适合抽象重写 |
| Completion 完成度逻辑 | 支持手动/自动完成、活动完成、课程完成、通过成绩、时长、角色、日期等条件；支持 ALL/ANY 聚合 | C。适合用于课程视频/资料/考试任务完成度设计 |
| Report 学习报告逻辑 | `report/progress`、`report/completion`、`report/stats`、`reportbuilder` 覆盖完成进度和统计报表 | C/E。进度报表思想可用；ReportBuilder 不适合迁移 |

Moodle 的 PHP 代码和插件机制不应进入 SpringBoot + Vue 主项目。推荐只把它作为“成熟 LMS 规则库”参考，抽取最小可落地的数据结构和统计口径。

## LearnHouse 审查

主要依据：

- `references/learnhouse/apps/api/src/db/courses/courses.py`
- `references/learnhouse/apps/api/src/db/courses/chapters.py`
- `references/learnhouse/apps/api/src/db/courses/activities.py`
- `references/learnhouse/apps/api/src/db/courses/assignments.py`
- `references/learnhouse/apps/api/src/services/ai/courseplanning.py`
- `references/learnhouse/apps/api/src/services/analytics/events.py`
- `references/learnhouse/apps/api/src/db/tinybird/endpoints/*.pipe`
- `references/learnhouse/apps/web/app/orgs/[orgslug]/(withmenu)/course/[courseuuid]/*`
- `references/learnhouse/apps/web/app/orgs/[orgslug]/dash/*`

| 审查项 | 发现 | 集成判断 |
| --- | --- | --- |
| 网课页面设计 | 课程列表、课程详情、章节展开、学习活动页、活动类型图标、进度状态完整 | D。适合参考 Vue 页面布局和交互 |
| 课程详情页面 | 展示课程介绍、学习目标、作者、缩略图、章节与活动列表、课程行为按钮 | D。复用信息架构，不迁移 React 组件 |
| 学生学习页面 | 学习页支持上一节/下一节、完成标记、作业提交、AI 侧边栏、锁定状态、课程结束页 | D。适合指导 `课程学习页` 需求拆解 |
| 任务/作业模块 | Assignment 支持任务类型、自动评分、截止时间、重试、答案展示、提交状态、任务级成绩 | C。数据模型和评分口径值得重写 |
| AI 相关页面或功能 | AI 课程规划、活动内容生成、课程内 AI 问答、playgrounds；依赖 Redis、LLM provider、Next 流式 UI | B/C/D。Prompt 和 JSON 合约可参考；实现应重写 |
| 学习分析页面 | 使用事件 `course_view`、`activity_view`、`activity_completed`、`time_on_activity`，Tinybird 查询包括漏斗、完成率、留存、内容类型效果等 | C/D。指标口径可重写；不引入 Tinybird |

LearnHouse 的后端虽然也是 Python，但依赖 FastAPI、SQLModel、Redis、Tinybird、Next.js 和大量 React 组件，不能直接嵌入当前系统。它最适合作为网课 UI/交互蓝本。

## Studyield 审查

主要依据：

- `references/studyield/backend/src/modules/ai/ai.service.ts`
- `references/studyield/backend/src/modules/quiz/quiz-generator.service.ts`
- `references/studyield/backend/src/modules/exam-clone/exam-clone.service.ts`
- `references/studyield/backend/src/modules/exam-clone/exam-clone.controller.ts`
- `references/studyield/backend/src/modules/analytics/analytics.service.ts`
- `references/studyield/backend/src/modules/teach-back/teach-back.service.ts`
- `references/studyield/backend/src/modules/learning-paths/learning-paths.service.ts`
- `references/studyield/backend/src/modules/knowledge-base/knowledge-base.service.ts`
- `references/studyield/frontend/src/config/api.ts`

| 审查项 | 发现 | 集成判断 |
| --- | --- | --- |
| AI 出题 | `quiz-generator` 支持多题型、难度、重点主题、解释；`exam-clone` 支持按试卷风格生成题目 | C。Prompt 和 JSON 合约可重写进 Java AI 服务 |
| AI 练习生成 | 支持 quiz generate、adaptive questions、generate-from-template、practice attempt | C。适合转成“AI 推荐练习/自动出题/错题复习”设计 |
| 知识点掌握分析 | Exam analytics 统计 topicPerformance、difficultyPerformance、recentAttempts、improvementTrend | C。可映射到章节/知识点/难度表现统计 |
| 学习反馈报告 | Teach-back 使用准确性、清晰度、完整性、理解程度、误区、建议、追问等维度 | C。适合 AI 学情报告 Prompt 设计 |
| Prompt 模板 | 多处强制 JSON 输出，结构清晰；AI Service 支持 OpenRouter/OpenAI 配置和 JSON response format | C。Prompt 结构可参考，但不直接复制 |
| 数据输入输出 JSON | 题目、测评、学习路径、讲解反馈、复习队列均有清晰 JSON 结构 | C。可抽契约后重写 DTO |

注意：Studyield 根目录 LICENSE 是 AGPL-3.0，但 README 和 `backend/package.json` 出现 Apache-2.0 描述，本地许可证信息存在差异。用户已确认取得二次开发许可，后续按授权文件归档处理；工程上仍不建议整体迁移 NestJS/React 运行时。

## mcq_generator 审查

主要依据：

- `references/mcq_generator/pyproject.toml`
- `references/mcq_generator/README.md`
- `references/mcq_generator/docs/CLI_USAGE.md`
- `references/mcq_generator/src/mcq_generator/prompt_builder.py`
- `references/mcq_generator/src/mcq_generator/mcq_generator.py`
- `references/mcq_generator/src/mcq_generator/binary_question_generator.py`
- `references/mcq_generator/scripts/mcq_generate_cli.py`
- `references/mcq_generator/scripts/binary_question_cli.py`

| 审查项 | 发现 | 集成判断 |
| --- | --- | --- |
| AI 出题 Prompt | PromptBuilder 有竞赛题质量要求、难度说明、偏见规避、选项数量、正确答案数量规则 | B/C。二开授权已确认，可作为独立服务候选；也可迁移 Prompt 思路后重写 |
| 多选题/不定项生成 | 支持 `num_correct_answers`，支持多个正确答案、All of the Above、None of the Above | B/C。适合映射到单选、多选、不定项选择题 |
| 难度/学科/数量/选项数量 | CLI 参数覆盖 field、subfield、difficulty、count、options、correct_answers、provider、model、max_tokens | B。适合封装成 HTTP 入参 |
| 题目输出格式 | MCQ JSON 包含 field、subfield、generated_at、question_count、questions；Binary JSON 包含 metadata 和 questions | B/C。可作为 AI 题目草稿 DTO 参考 |
| 真实集成风险 | 代码使用 LiteLLM 和文本解析正则，稳定性依赖模型输出格式；还需要服务化、鉴权、日志和异常处理 | 中。授权已确认，主要风险转为工程稳定性和服务化成本 |

后续可放入 `external/mcq-generator-service` 封装成独立 Python 服务，由 SpringBoot 调用；也可以按它的接口和输出格式在 `backend` 中重新实现。

## 明确不建议使用的内容

| 来源 | 不建议内容 | 原因 |
| --- | --- | --- |
| Frappe LMS | Frappe DocType 运行时、Frappe 权限、Frappe Resource API | 会引入第二套后端框架和权限体系 |
| Moodle | PHP 插件、ReportBuilder、完整 gradebook、完整 completion subsystem | 过重，重构成本远高于抽规则重写 |
| LearnHouse | Next.js 页面代码、Tinybird、企业支付/SSO、多组织运营能力 | 技术栈和阶段目标不匹配 |
| Studyield | NestJS 模块整体迁移、Qdrant/ClickHouse 全套、游戏化/订阅/排行榜 | 依赖复杂，主项目阶段目标不需要 |
| mcq_generator | 直接嵌入主项目业务代码 | Python/LiteLLM 与 SpringBoot 技术栈不同，建议放入 `external` 或重写 |

## 后续落地建议

1. 先在主项目建课程、章节、知识点、资源、学习进度的最小数据模型。
2. 在题库表上扩展课程、章节、知识点、难度、题型约束，保证原考试流程不受影响。
3. 建学习统计口径：章节正确率、知识点正确率、难度表现、错题排行、成绩趋势。
4. 建 AI 服务层接口和 mock 实现，再接入真实大模型配置。
5. AI 出题先进入教师审核队列，不直接入库。
6. references 中候选代码进入 `external` 前必须写清原始许可证、二开授权状态、依赖、运行方式、接口协议和替换成本。
