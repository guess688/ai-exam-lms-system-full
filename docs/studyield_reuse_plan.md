# Studyield 复用计划

## 审查结论

`references/studyield` 是 AGPL-3.0 的 NestJS + React AI 学习平台。技术栈与主项目 SpringBoot + Vue 不一致，不适合直接复制服务或页面。适合抽取 Prompt、JSON schema、学习分析结构和练习推荐规则，在主项目中重写。

本次已将可复用资产放入：

- `external/ai-learning-adapted`
- `backend/src/main/resources/prompts`

## 可复用内容

| 模块 | 参考内容 | 集成方式 | 主项目落点 |
|---|---|---|---|
| `backend/src/modules/ai/ai.controller.ts` | JSON quiz/flashcard 生成 Prompt 结构 | 抽取 schema | `question_generation_prompt.txt` |
| `backend/src/modules/quiz/quiz-generator.service.ts` | 题型、难度、解释字段 | 抽取重写 | AI 出题 Prompt |
| `backend/src/modules/analytics/analytics.service.ts` | accuracy、weakTopics、活动统计 | 抽取指标 | 学生/班级报告 Prompt |
| `backend/src/modules/teach-back/teach-back.service.ts` | accuracy/clarity/completeness/understanding 评价结构 | 抽取评价维度 | 学习行为评价 Prompt |
| `backend/src/modules/learning-paths/learning-paths.service.ts` | study/quiz/practice/review 步骤设计 | 抽取任务结构 | 推荐练习 Prompt |

## 已同步到主项目的方法

`AiService` 已具备并继续使用以下方法：

- `generateStudentReport(input)`
- `generateClassReport(input)`
- `recommendPractice(input)`
- `evaluateLearningBehavior(input)`

这些方法对应的 Prompt 已更新为结构化 JSON 输出，mock 模式下可返回示例内容。

## 迁移后的输出结构

学生报告：

- `summary`
- `masteryLevel`
- `scoreOverview`
- `strengths`
- `weaknesses`
- `chapterAnalysis`
- `knowledgePointAnalysis`
- `wrongQuestionAdvice`
- `practicePlan`

班级报告：

- `scoreOverview`
- `weakChapters`
- `weakKnowledgePoints`
- `commonMistakes`
- `difficultyPerformance`
- `teachingSuggestions`

练习推荐：

- `recommendedKnowledgePoints`
- `difficultyPlan`
- `questionTypePlan`
- `dailyPracticePlan`
- `reviewAdvice`

学习行为评价：

- `behaviorLevel`
- `positiveSignals`
- `riskSignals`
- `taskCompletion`
- `videoLearning`
- `practiceBehavior`
- `teacherAdvice`
- `studentAdvice`

## 风险与限制

- Studyield 的 TS 服务代码不直接进入主项目，避免引入 NestJS/React 依赖。
- 输出 schema 已适配主项目课程、章节、知识点、考试分析和学习任务模型。
- 后续如要生成正式学习路径，可以再映射到 `learning_task` 或新增学习计划表。
