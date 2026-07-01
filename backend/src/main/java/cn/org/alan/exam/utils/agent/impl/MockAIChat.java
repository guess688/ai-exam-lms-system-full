package cn.org.alan.exam.utils.agent.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.org.alan.exam.utils.agent.AIChat;

public class MockAIChat implements AIChat {

    private static final String SCORE_RESULT = "\u8bc4\u5206\u7ed3\u679c";
    private static final String QUESTION_ID = "\u9898\u76eeID";
    private static final String TOTAL_SCORE = "\u9898\u76ee\u603b\u5206";
    private static final String FINAL_SCORE = "\u6700\u7ec8\u5f97\u5206";
    private static final String DEDUCTION_DETAIL = "\u6263\u5206\u660e\u7ec6";

    @Override
    public String getChatResponse(String msg) {
        JSONArray questions = parseQuestions(msg);
        JSONArray scores = new JSONArray();
        for (int i = 0; i < questions.size(); i++) {
            JSONObject question = questions.getJSONObject(i);
            JSONObject score = new JSONObject();
            score.put(QUESTION_ID, readString(question, QUESTION_ID, "questionId", String.valueOf(i + 1)));
            score.put(FINAL_SCORE, readInt(question, TOTAL_SCORE, "totalScore", 0));
            score.put(DEDUCTION_DETAIL, "Mock AI scoring is enabled.");
            scores.add(score);
        }

        JSONObject result = new JSONObject();
        result.put(SCORE_RESULT, scores);
        return "```json\n" + result + "\n```";
    }

    private JSONArray parseQuestions(String msg) {
        try {
            return JSONUtil.parseArray(msg);
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    private String readString(JSONObject object, String primaryKey, String fallbackKey, String defaultValue) {
        String value = object.getStr(primaryKey);
        if (value == null) {
            value = object.getStr(fallbackKey);
        }
        return value == null ? defaultValue : value;
    }

    private Integer readInt(JSONObject object, String primaryKey, String fallbackKey, Integer defaultValue) {
        Integer value = object.getInt(primaryKey);
        if (value == null) {
            value = object.getInt(fallbackKey);
        }
        return value == null ? defaultValue : value;
    }
}
