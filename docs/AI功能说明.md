# AI 功能说明

## 设计目标

AI 模块采用统一服务层，先保证 mock 模式可演示、真实模型可切换，再逐步增强提示词、结构化解析和业务闭环。所有 AI 输出都应服务教学改进，不生成家校沟通反馈，不使用过度负面或标签化表达。

## 配置项

```yaml
ai:
  provider: ${AI_PROVIDER:mock}
  baseUrl: ${AI_BASE_URL:https://api.openai.com/v1}
  apiKey: ${AI_API_KEY:}
  model: ${AI_MODEL:gpt-4o-mini}
  enabled: ${AI_ENABLED:false}
  mockEnabled: ${AI_MOCK_ENABLED:true}
  questionGeneratorServiceUrl: ${AI_QUESTION_SERVICE_URL:}
```

## Mock 模式

默认使用 mock 模式：

- 不调用真实模型。
- 不需要 API Key。
- 返回稳定示例内容，便于客户演示和前后端联调。
- AI 调用日志仍会记录功能类型、耗时、成功状态和脱敏摘要。

## 真实模型模式

真实模式建议通过环境变量启用：

```bash
set AI_ENABLED=true
set AI_MOCK_ENABLED=false
set AI_PROVIDER=openai-compatible
set AI_BASE_URL=https://your-provider.example.com/v1
set AI_API_KEY=your_api_key
set AI_MODEL=your_model
```

安全要求：

- 不提交真实 API Key。
- 日志不记录完整 API Key。
- 学生隐私数据最小化传递。
- AI 返回内容保存前保留原始 JSON 和解析结果，便于追溯。

## Prompt 管理

Prompt 统一放在：

```text
backend/src/main/resources/prompts/
```

当前覆盖：

| Prompt | 用途 |
| --- | --- |
| `student_report_prompt.txt` | 学生个人学情报告 |
| `class_report_prompt.txt` | 班级学情报告 |
| `teaching_plan_prompt.txt` | 教学文案 |
| `paper_review_prompt.txt` | 试卷讲评稿 |
| `question_generation_prompt.txt` | AI 自动出题 |
| `practice_recommend_prompt.txt` | 推荐练习 |
| `learning_behavior_prompt.txt` | 学习行为评价 |

## AI 学情报告

### 学生个人报告

输入数据包括：

- 学生匿名标识或姓名
- 考试名称、总分、班级平均分、排名
- 章节正确率、知识点正确率
- 错题列表和难度表现
- 答题用时
- 网课观看进度
- 学习任务完成情况

输出内容包括：

- 整体表现
- 优势章节
- 薄弱章节
- 薄弱知识点
- 错题原因分析
- 网课学习行为评价
- 后续复习建议
- 推荐练习方向

### 班级报告

输入数据包括：

- 班级名称、考试名称、参加人数
- 平均分、最高分、最低分、及格率、优秀率
- 分数段分布
- 章节和知识点平均正确率
- 高频错题
- 难度正确率
- 网课完成率和任务完成率

输出面向教师教学改进，包括薄弱点、讲评建议、练习方向，不包含家校沟通反馈。

## AI 自动出题

教师输入：

- 课程、章节、知识点
- 题型
- 难度
- 数量
- 额外要求

AI 输出必须包含：

- 题干
- 题型
- 选项
- 正确答案
- 答案解析
- 难度
- 所属课程、章节、知识点

题目不会直接进入正式题库，而是进入待审核表。教师可编辑后确认入库或拒绝。

## AI 推荐练习

推荐策略采用“规则 + AI 文案”：

- 系统先根据正确率低于 60% 的知识点筛选候选题。
- 默认优先推荐简单和中等题。
- 基础较好但困难题错误多时，推荐中等和困难题。
- AI 负责生成推荐理由和任务说明。

教师确认后，可一键发布为学习任务。

## 学习行为评价

规则评价先行，AI 负责自然语言总结。规则包括：

- 成绩连续下降：提示需要关注。
- 网课完成率低且对应章节正确率低：提示补学。
- 任务完成率低：提示改进学习执行。
- 错题重复出现：提示重新巩固知识点。
- 成绩稳定上升：提示保持当前学习节奏。

表达限制：

- 不使用过度负面语言。
- 不给学生贴固定标签。
- 以建议、改进和支持为主。
