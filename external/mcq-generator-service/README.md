# MCQ Generator Service Adapter

This is a lightweight adapter for `references/mcq_generator`.

It exposes:

- `POST /generate-questions`

The service returns the same JSON shape expected by the main SpringBoot question review queue. It does not write to the official question bank.

Run:

```bash
pip install -r requirements.txt
python app.py
```

Default behavior is mock mode. Set `MCQ_USE_LITELLM=true` and configure provider credentials supported by LiteLLM to call a real model.

Request example:

```json
{
  "courseId": 1,
  "chapterId": 2,
  "knowledgePointId": 3,
  "subject": "Physics",
  "questionType": "MULTIPLE",
  "difficulty": "MEDIUM",
  "count": 2,
  "optionCount": 4,
  "correctAnswerCount": 2
}
```
