# AI Prompt 与 Schema

## Prompt 文件

主项目 Prompt 位于 `backend/src/main/resources/prompts`：

| 文件 | AiService 方法 | 用途 |
|---|---|---|
| `student_report_prompt.txt` | `generateStudentReport` | 学生个人学情报告 |
| `class_report_prompt.txt` | `generateClassReport` | 班级学情报告 |
| `teaching_plan_prompt.txt` | `generateTeachingPlan` | 教学文案 |
| `paper_review_prompt.txt` | `generatePaperReview` | 试卷讲评稿 |
| `question_generation_prompt.txt` | `generateQuestions` | AI 自动出题 |
| `practice_recommend_prompt.txt` | `recommendPractice` | 推荐练习 |
| `learning_behavior_prompt.txt` | `evaluateLearningBehavior` | 学习行为评价 |

## AI 出题 Schema

入参建议：

```json
{
  "courseId": 1,
  "chapterId": 2,
  "knowledgePointId": 3,
  "repoId": 1,
  "questionType": "INDEFINITE",
  "difficulty": "HARD",
  "count": 3,
  "optionCount": 4,
  "correctAnswerCount": 2
}
```

出参必须包含：

- `needTeacherReview=true`
- `questions[].type`: `SINGLE` / `MULTIPLE` / `INDEFINITE` / `JUDGE`
- `questions[].quType`: 1 / 2 / 4 / 3
- `questions[].difficulty`: `EASY` / `MEDIUM` / `HARD`
- `questions[].courseId`
- `questions[].chapterId`
- `questions[].knowledgePointId`
- `questions[].content`
- `questions[].options`
- `questions[].answer`
- `questions[].analysis`

生成题目保存流程：

1. `/api/ai/questions` 调用 `AiService.generateQuestions`。
2. AI 返回原始 JSON 和解析结果写入 `t_ai_call_log`。
3. `questions` 数组逐条写入 `t_ai_question_review`，状态为 `PENDING`。
4. 教师审核通过后，后续阶段再转换为 `t_question` + `t_option`。

## Mock 示例

Mock 示例位于：

- `backend/src/main/resources/prompts/examples/question_generation_mock.json`
- `backend/src/main/resources/prompts/examples/student_report_mock.json`
- `backend/src/main/resources/prompts/examples/class_report_mock.json`
- `backend/src/main/resources/prompts/examples/practice_recommend_mock.json`
- `backend/src/main/resources/prompts/examples/learning_behavior_mock.json`
- `backend/src/main/resources/prompts/examples/teaching_plan_mock.json`
- `backend/src/main/resources/prompts/examples/paper_review_mock.json`

## 数据隐私

- Prompt 要求不输出学生隐私字段。
- `AiService` 会对 token、password、phone、email、studentId、userId、realName 等字段做脱敏或最小化传递。
- API Key 仍通过环境变量传入，不写死到代码。
