import json
import os
from http.server import BaseHTTPRequestHandler, HTTPServer


QUESTION_TYPE_TO_QUTYPE = {
    "SINGLE": 1,
    "MULTIPLE": 2,
    "JUDGE": 3,
    "INDEFINITE": 4,
}


def normalize_type(value):
    value = str(value or "SINGLE").upper()
    if value in QUESTION_TYPE_TO_QUTYPE:
        return value
    if "TRUE" in value or "JUDGE" in value:
        return "JUDGE"
    if "INDEFINITE" in value:
        return "INDEFINITE"
    if "MULTIPLE" in value:
        return "MULTIPLE"
    return "SINGLE"


def normalize_difficulty(value):
    value = str(value or "MEDIUM").upper()
    if "EASY" in value:
        return "EASY"
    if "HARD" in value:
        return "HARD"
    return "MEDIUM"


def build_prompt(payload):
    question_type = normalize_type(payload.get("questionType") or payload.get("type"))
    difficulty = normalize_difficulty(payload.get("difficulty"))
    count = int(payload.get("count") or payload.get("questionCount") or 1)
    option_count = int(payload.get("optionCount") or 4)
    correct_count = int(payload.get("correctAnswerCount") or (2 if question_type in ["MULTIPLE", "INDEFINITE"] else 1))
    topic = payload.get("knowledgePointName") or payload.get("chapterTitle") or payload.get("subject") or "the provided knowledge point"

    return f"""Generate {count} {question_type} questions about {topic}.

Difficulty: {difficulty}
Options per choice question: {option_count}
Correct answers for MULTIPLE: {correct_count}

Rules:
- Return JSON only.
- Candidate questions require teacher review before entering the official question bank.
- Use type SINGLE, MULTIPLE, INDEFINITE, or JUDGE.
- Use difficulty EASY, MEDIUM, or HARD.
- For SINGLE, exactly one option isRight=true.
- For MULTIPLE, at least two options are isRight=true.
- For INDEFINITE, one or more options may be correct.
- For JUDGE, use True/False options.

Schema:
{{
  "summary": "text",
  "needTeacherReview": true,
  "questions": [
    {{
      "type": "{question_type}",
      "quType": {QUESTION_TYPE_TO_QUTYPE[question_type]},
      "difficulty": "{difficulty}",
      "courseId": {json.dumps(payload.get("courseId"))},
      "chapterId": {json.dumps(payload.get("chapterId"))},
      "knowledgePointId": {json.dumps(payload.get("knowledgePointId"))},
      "content": "question stem",
      "options": [{{"label": "A", "content": "option", "isRight": true}}],
      "answer": ["A"],
      "analysis": "brief explanation"
    }}
  ]
}}"""


def mock_questions(payload):
    question_type = normalize_type(payload.get("questionType") or payload.get("type"))
    difficulty = normalize_difficulty(payload.get("difficulty"))
    count = max(1, min(int(payload.get("count") or payload.get("questionCount") or 1), 20))
    questions = []
    for index in range(count):
        if question_type == "JUDGE":
            options = [
                {"label": "A", "content": "True", "isRight": True},
                {"label": "B", "content": "False", "isRight": False},
            ]
            answer = ["A"]
        else:
            options = [
                {"label": "A", "content": "Correct option", "isRight": True},
                {"label": "B", "content": "Plausible distractor", "isRight": False},
                {"label": "C", "content": "Another distractor", "isRight": question_type in ["MULTIPLE", "INDEFINITE"]},
                {"label": "D", "content": "Incorrect option", "isRight": False},
            ]
            answer = ["A", "C"] if question_type in ["MULTIPLE", "INDEFINITE"] else ["A"]
        questions.append(
            {
                "type": question_type,
                "quType": QUESTION_TYPE_TO_QUTYPE[question_type],
                "difficulty": difficulty,
                "courseId": payload.get("courseId"),
                "chapterId": payload.get("chapterId"),
                "knowledgePointId": payload.get("knowledgePointId"),
                "content": f"Mock generated question {index + 1}.",
                "options": options,
                "answer": answer,
                "analysis": "Mock explanation for teacher review.",
            }
        )
    return {"summary": f"Generated {len(questions)} candidate questions.", "needTeacherReview": True, "questions": questions}


def generate_with_litellm(payload):
    import litellm

    model = payload.get("model") or os.getenv("MCQ_MODEL", "openai/gpt-4o-mini")
    prompt = build_prompt(payload)
    response = litellm.completion(model=model, messages=[{"role": "user", "content": prompt}], max_tokens=int(payload.get("maxTokens") or 4096))
    content = response.choices[0].message.content.strip()
    if content.startswith("```json"):
        content = content[7:]
    elif content.startswith("```"):
        content = content[3:]
    if content.endswith("```"):
        content = content[:-3]
    data = json.loads(content.strip())
    data["needTeacherReview"] = True
    return data


class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path != "/generate-questions":
            self.send_response(404)
            self.end_headers()
            return
        length = int(self.headers.get("Content-Length", 0))
        payload = json.loads(self.rfile.read(length) or b"{}")
        try:
            if os.getenv("MCQ_USE_LITELLM", "false").lower() == "true":
                result = generate_with_litellm(payload)
            else:
                result = mock_questions(payload)
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps(result, ensure_ascii=False).encode("utf-8"))
        except Exception as exc:
            self.send_response(500)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"message": str(exc), "needTeacherReview": True, "questions": []}).encode("utf-8"))


if __name__ == "__main__":
    port = int(os.getenv("PORT", "8010"))
    HTTPServer(("0.0.0.0", port), Handler).serve_forever()
