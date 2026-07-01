# mcq_generator 复用计划

## 审查结论

`references/mcq_generator` 是轻量 Python 出题工具，核心由 `PromptBuilder`、`QuestionGenerator`、`MCQGenerator` 和 `BinaryQuestionGenerator` 组成。它的技术栈与主项目 SpringBoot 不一致，但功能边界清晰、依赖少，适合封装为独立服务或迁移 Prompt/schema。

本次采用两种集成方式：

- 封装独立服务：`external/mcq-generator-service`
- 同步 Prompt/schema：`backend/src/main/resources/prompts/question_generation_prompt.txt`

## 可复用内容

| 来源 | 可复用内容 | 集成方式 | 主项目位置 |
|---|---|---|---|
| `prompt_builder.py` | 难度、数量、选项数、正确答案数量、质量规则 | 抽取 Prompt 设计后重写 | `question_generation_prompt.txt` |
| `mcq_generator.py` | 多选题解析、动态选项、多正确答案思路 | 参考重写 | `AiServiceImpl` 待审核保存逻辑 |
| `binary_question_generator.py` | True/False 出题结构 | 参考重写 | JUDGE 题型 schema |
| CLI 参数 | field/subfield/difficulty/count/options/correct-answers | 迁移为 JSON 入参 | `/api/ai/questions` 与外部服务 `/generate-questions` |

## 主项目入参设计

```json
{
  "courseId": 1,
  "chapterId": 2,
  "knowledgePointId": 3,
  "repoId": 1,
  "subject": "Physics",
  "questionType": "SINGLE",
  "difficulty": "MEDIUM",
  "count": 5,
  "optionCount": 4,
  "correctAnswerCount": 1
}
```

## 主项目输出格式

输出必须能被题库模块解析，并进入 `t_ai_question_review`：

```json
{
  "summary": "Generated candidate questions.",
  "needTeacherReview": true,
  "questions": [
    {
      "type": "SINGLE",
      "quType": 1,
      "difficulty": "MEDIUM",
      "courseId": 1,
      "chapterId": 2,
      "knowledgePointId": 3,
      "content": "Question stem",
      "options": [
        { "label": "A", "content": "Correct option", "isRight": true },
        { "label": "B", "content": "Distractor", "isRight": false }
      ],
      "answer": ["A"],
      "analysis": "Explanation"
    }
  ]
}
```

## 外部服务

已新增 `external/mcq-generator-service`：

- `POST /generate-questions`
- 默认 mock 模式
- `MCQ_USE_LITELLM=true` 时可用 LiteLLM 调真实模型
- 返回 `needTeacherReview=true`

运行方式：

```bash
cd external/mcq-generator-service
pip install -r requirements.txt
python app.py
```

## 风险与限制

- 原项目未提供明确独立 LICENSE 文件，需按引用仓库实际授权记录管理。
- Python 服务当前作为可选外部服务，不参与 SpringBoot 启动。
- 正式入库仍需后续新增教师审核页面或审核接口，本次只实现候选题落待审核表。
