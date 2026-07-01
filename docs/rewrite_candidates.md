# 参考重写候选清单

本文件记录不适合直接复制，但值得抽取模型、规则、Prompt、接口契约或交互后在 SpringBoot + Vue 主项目中重写的模块。

## 高优先级重写

| 来源项目 | 模块/能力 | 重写原因 | 主项目目标 |
| --- | --- | --- | --- |
| Frappe LMS | 课程、章节、课时模型 | 字段贴近目标，但 Frappe DocType 不兼容 | 新增课程、章节、知识点体系 |
| Frappe LMS | 课程进度和视频观看时长 | 模型简单，适合快速落地 | 网课进度、完成率统计 |
| Moodle | Question bank 分类和题目引用 | Moodle 模型成熟，但 PHP 和表结构过重 | 题库增加课程、章节、知识点、难度 |
| Moodle | Quiz slots、attempts、feedback | 规则成熟，适合抽象成考试/练习规则 | 按章节、知识点、题型、难度自动组卷 |
| Moodle | Completion 完成度 | 完成度口径成熟，适合简化 | 网课任务、考试任务、练习任务完成统计 |
| LearnHouse | 学生课程学习页 | 页面体验完整，但 React/Next 不兼容 | Vue 网课学习页面 |
| LearnHouse | Assignment 数据模型和评分口径 | 作业模型可映射到学习任务 | 学习任务发布模块 |
| Studyield | AI 出题 JSON 合约 | 贴合目标，但 NestJS 技术栈不适合直接嵌入 | AI 自动出题、教师审核入库 |
| Studyield | topic/difficulty performance | 指标贴合学情分析目标 | 章节、知识点、难度表现统计 |
| Studyield | Teach-back 反馈结构 | 报告维度可直接转化为学情报告维度 | AI 学生个人学情报告、学习行为评价 |
| mcq_generator | 出题参数和输出 JSON | 体量小，已获二开授权，可封装或按主项目重写 | AI 题目草稿 DTO 和 Prompt |

## 中优先级重写

| 来源项目 | 模块/能力 | 重写原因 | 主项目目标 |
| --- | --- | --- | --- |
| Frappe LMS | Quiz 负分、随机题、通过率、答题历史 | 目标初期只需自动组卷和考试统计 | 后续练习/小测增强 |
| Frappe LMS | Assignment 提交和评分 | 只需要学习任务，不需要完整作业系统 | 任务发布与任务完成统计 |
| Moodle | Gradebook 成绩项和聚合 | 完整 gradebook 太重 | 成绩分布、班级均分、趋势统计 |
| Moodle | Progress/Completion Report | 报表框架太重 | 图表分析页面 |
| LearnHouse | AI Course Planning | 有用但不是考试系统一期刚需 | AI 教学文案、课程草稿生成 |
| LearnHouse | Analytics 漏斗和留存指标 | 当前可先做考试/网课核心指标 | 后续学习行为分析增强 |
| Studyield | Learning Paths | 和学习任务有重叠 | AI 推荐练习、复习任务生成 |
| Studyield | Review queue/SM-2 | 可增强错题复习，但需和现有错题本融合 | 错题订正任务、复习任务 |
| Studyield | Knowledge Base/RAG | 运维成本高 | 后续资料问答或报告依据增强 |
| mcq_generator | 相似题、解析、前置知识 Prompt | 可以增强题目讲解 | AI 试卷讲评稿、推荐练习 |

## 低优先级或暂不重写

| 来源项目 | 模块/能力 | 原因 |
| --- | --- | --- |
| Moodle | 完整 ReportBuilder | 过重，超出阶段目标 |
| Moodle | 完整插件系统 | 不符合 SpringBoot + Vue 底座原则 |
| Frappe LMS | 证书、批次、直播、SCORM | 当前目标未优先要求 |
| LearnHouse | 支付、SSO、多组织、企业功能 | 与目标无关 |
| LearnHouse | Playground、代码执行、协作白板 | 依赖复杂，不是当前优先功能 |
| Studyield | 订阅、游戏化、排行榜、徽章 | 不属于核心考试与学情分析 |
| Studyield | Code sandbox、Deep research | 运维复杂，阶段目标未要求 |

## 建议重写顺序

1. 课程/章节/知识点基础表和接口。
2. 题库扩展：课程、章节、知识点、难度、题型兼容。
3. 自动组卷：章节、知识点、题型、难度条件。
4. 学情统计：学生/班级章节正确率、知识点正确率、错题排行、难度表现。
5. 轻量网课：视频、资料、观看进度、完成率。
6. 学习任务：考试、练习、网课、错题订正、复习。
7. AI 服务层：mock/provider 配置、AI 出题草稿、教师审核。
8. AI 报告和讲评稿：基于已有考试和学习统计生成。

## 重写原则

- 不从 references 直接搬整套系统。
- 先抽字段、枚举、状态机、统计口径和 Prompt 输出结构。
- 所有新增表必须有 SQL migration 或 init SQL。
- 所有接口必须兼容当前权限体系。
- 前端必须复用现有 Vue 路由、接口封装和 UI 风格。
- AI 生成题目必须进入教师审核队列，审核后才能入库。
