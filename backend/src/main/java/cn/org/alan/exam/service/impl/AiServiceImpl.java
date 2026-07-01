package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.config.AiProperties;
import cn.org.alan.exam.mapper.AiCallLogMapper;
import cn.org.alan.exam.mapper.AiQuestionReviewMapper;
import cn.org.alan.exam.model.entity.AiCallLog;
import cn.org.alan.exam.model.entity.AiQuestionReview;
import cn.org.alan.exam.model.vo.ai.AiResponseVO;
import cn.org.alan.exam.service.IAiService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class AiServiceImpl implements IAiService {

    private static final int MAX_LOG_TEXT_LENGTH = 8000;
    private static final int MAX_SUMMARY_LENGTH = 2000;

    private final RestTemplate restTemplate = new RestTemplate();

    @Resource
    private AiProperties aiProperties;
    @Resource
    private AiPromptManager aiPromptManager;
    @Resource
    private AiCallLogMapper aiCallLogMapper;
    @Resource
    private AiQuestionReviewMapper aiQuestionReviewMapper;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public Result<AiResponseVO> generateStudentReport(Map<String, Object> input) {
        return invoke("STUDENT_REPORT", "student_report_prompt", input, buildMockStudentReport(input));
    }

    @Override
    public Result<AiResponseVO> generateClassReport(Map<String, Object> input) {
        return invoke("CLASS_REPORT", "class_report_prompt", input, buildMockClassReport(input));
    }

    @Override
    public Result<AiResponseVO> generateTeachingPlan(Map<String, Object> input) {
        return invoke("TEACHING_PLAN", "teaching_plan_prompt", input, buildMockTeachingPlan(input));
    }

    @Override
    public Result<AiResponseVO> generatePaperReview(Map<String, Object> input) {
        return invoke("PAPER_REVIEW", "paper_review_prompt", input, buildMockPaperReview(input));
    }

    @Override
    public Result<AiResponseVO> generateQuestions(Map<String, Object> input) {
        return invoke("QUESTION_GENERATION", "question_generation_prompt", input, buildMockQuestions(input));
    }

    @Override
    public Result<AiResponseVO> recommendPractice(Map<String, Object> input) {
        return invoke("PRACTICE_RECOMMEND", "practice_recommend_prompt", input, buildMockPractice(input));
    }

    @Override
    public Result<AiResponseVO> evaluateLearningBehavior(Map<String, Object> input) {
        return invoke("LEARNING_BEHAVIOR", "learning_behavior_prompt", input, buildMockLearningBehavior(input));
    }

    private Result<AiResponseVO> invoke(String featureType, String promptKey, Map<String, Object> input, Map<String, Object> mockResult) {
        long start = System.currentTimeMillis();
        LocalDateTime requestTime = LocalDateTime.now();
        String prompt = aiPromptManager.getPrompt(promptKey);
        Object sanitizedInput = sanitize(input);
        AiCallLog callLog = buildBaseLog(featureType, requestTime, prompt, sanitizedInput);

        try {
            if (aiProperties.isMockMode()) {
                AiResponseVO response = buildResponse(featureType, true, true, mockResult, "AI mock response generated");
                response.setRawJson(toJson(mockResult));
                fillSuccessLog(callLog, response.getRawJson(), toJson(mockResult));
                saveLog(callLog, start);
                response.setLogId(callLog.getId());
                attachQuestionReviewInfo(featureType, response, sanitizedInput);
                return Result.success("AI mock response generated", response);
            }
            if (!aiProperties.isAiEnabled()) {
                return failGracefully(callLog, start, featureType, "AI 服务未启用，请在配置中设置 ai.enabled=true");
            }
            validateRealConfig();
            AiResponseVO response = callRealModel(featureType, prompt, sanitizedInput);
            fillSuccessLog(callLog, response.getRawJson(), truncate(toJson(response.getParsedResult()), MAX_LOG_TEXT_LENGTH));
            saveLog(callLog, start);
            response.setLogId(callLog.getId());
            attachQuestionReviewInfo(featureType, response, sanitizedInput);
            return Result.success("AI response generated", response);
        } catch (Exception e) {
            log.warn("AI call failed. featureType={}, provider={}, model={}",
                    featureType, aiProperties.getProvider(), aiProperties.getModel(), e);
            return failGracefully(callLog, start, featureType, "AI 服务暂时不可用，请稍后重试");
        }
    }

    private AiResponseVO callRealModel(String featureType, String prompt, Object sanitizedInput) throws Exception {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", aiProperties.getModel());
        requestBody.put("temperature", 0.3);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", prompt);
        messages.add(systemMessage);

        Map<String, String> userMessage = new LinkedHashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", toJson(sanitizedInput));
        messages.add(userMessage);
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey());

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                resolveChatCompletionsUrl(),
                new HttpEntity<Map<String, Object>>(requestBody, headers),
                String.class);

        String rawJson = responseEntity.getBody();
        String content = extractContent(rawJson);
        Object parsedResult = parseContent(content);
        AiResponseVO response = new AiResponseVO();
        response.setFeatureType(featureType);
        response.setMock(false);
        response.setSuccess(true);
        response.setContent(content);
        response.setRawJson(rawJson);
        response.setParsedResult(parsedResult);
        response.setFriendlyMessage("AI response generated");
        return response;
    }

    private AiCallLog buildBaseLog(String featureType, LocalDateTime requestTime, String prompt, Object sanitizedInput) {
        AiCallLog callLog = new AiCallLog();
        callLog.setFeatureType(featureType);
        callLog.setProvider(aiProperties.getProvider());
        callLog.setModel(aiProperties.getModel());
        callLog.setMockEnabled(aiProperties.isMockMode() ? 1 : 0);
        callLog.setRequestTime(requestTime);
        callLog.setRequestSummary(truncate(toJson(sanitizedInput), MAX_SUMMARY_LENGTH));
        callLog.setPromptTemplate(truncate(prompt, MAX_LOG_TEXT_LENGTH));
        callLog.setUpdateTime(LocalDateTime.now());
        return callLog;
    }

    private void fillSuccessLog(AiCallLog callLog, String rawResponse, String parsedResult) {
        callLog.setSuccess(1);
        callLog.setRawResponse(truncate(rawResponse, MAX_LOG_TEXT_LENGTH));
        callLog.setParsedResult(truncate(parsedResult, MAX_LOG_TEXT_LENGTH));
        callLog.setErrorMessage(null);
    }

    private Result<AiResponseVO> failGracefully(AiCallLog callLog, long start, String featureType, String message) {
        callLog.setSuccess(0);
        callLog.setErrorMessage(truncate(message, 1000));
        saveLog(callLog, start);

        AiResponseVO response = new AiResponseVO();
        response.setLogId(callLog.getId());
        response.setFeatureType(featureType);
        response.setMock(aiProperties.isMockMode());
        response.setSuccess(false);
        response.setContent("");
        response.setRawJson("");
        response.setParsedResult(new LinkedHashMap<String, Object>());
        response.setFriendlyMessage(message);
        return Result.success(message, response);
    }

    private void saveLog(AiCallLog callLog, long start) {
        callLog.setDurationMs(System.currentTimeMillis() - start);
        callLog.setUpdateTime(LocalDateTime.now());
        try {
            aiCallLogMapper.insert(callLog);
        } catch (Exception e) {
            log.warn("Save AI call log failed. featureType={}", callLog.getFeatureType(), e);
        }
    }

    private void attachQuestionReviewInfo(String featureType, AiResponseVO response, Object sanitizedInput) {
        if (!"QUESTION_GENERATION".equals(featureType) || response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            return;
        }
        int savedCount = saveQuestionReviewCandidates(response, sanitizedInput);
        if (response.getParsedResult() instanceof Map) {
            Map<String, Object> parsed = asStringMap(response.getParsedResult());
            parsed.put("reviewStatus", "PENDING");
            parsed.put("reviewSavedCount", savedCount);
            parsed.put("needTeacherReview", true);
            response.setParsedResult(parsed);
        }
    }

    private int saveQuestionReviewCandidates(AiResponseVO response, Object sanitizedInput) {
        if (!(response.getParsedResult() instanceof Map)) {
            return 0;
        }
        Map<String, Object> parsed = asStringMap(response.getParsedResult());
        Object questionsObject = parsed.get("questions");
        if (!(questionsObject instanceof Collection)) {
            return 0;
        }
        Map<String, Object> inputMap = asStringMap(sanitizedInput);
        int savedCount = 0;
        for (Object item : (Collection<?>) questionsObject) {
            Map<String, Object> questionMap = asStringMap(item);
            if (questionMap.isEmpty()) {
                continue;
            }
            try {
                AiQuestionReview review = buildQuestionReview(response.getLogId(), questionMap, inputMap);
                aiQuestionReviewMapper.insert(review);
                savedCount++;
            } catch (Exception e) {
                log.warn("Save AI question review item failed. logId={}", response.getLogId(), e);
            }
        }
        return savedCount;
    }

    private AiQuestionReview buildQuestionReview(Integer logId, Map<String, Object> questionMap, Map<String, Object> inputMap) {
        AiQuestionReview review = new AiQuestionReview();
        review.setAiCallLogId(logId);
        review.setRepoId(firstInteger(questionMap, inputMap, "repoId", "repo_id", "paperId", "paper_id"));
        review.setCourseId(firstInteger(questionMap, inputMap, "courseId", "course_id"));
        review.setChapterId(firstInteger(questionMap, inputMap, "chapterId", "chapter_id"));
        review.setKnowledgePointId(firstInteger(questionMap, inputMap, "knowledgePointId", "knowledge_point_id"));
        String type = firstString(questionMap, inputMap, "type", "questionType", "question_type", "quType", "qu_type");
        Integer quType = mapQuestionType(type);
        review.setQuType(quType);
        review.setQuestionType(normalizeQuestionType(type, quType));
        review.setDifficulty(normalizeDifficulty(firstString(questionMap, inputMap, "difficulty")));
        review.setContent(firstString(questionMap, inputMap, "content", "question", "stem"));
        Object answerObject = firstObject(questionMap, "answer", "correctAnswer", "correct_answer", "answers");
        List<String> answers = normalizeAnswers(answerObject);
        review.setOptionsJson(toJson(buildMainProjectOptions(questionMap, quType, answers)));
        review.setAnswerJson(toJson(answers));
        review.setAnalysis(firstString(questionMap, inputMap, "analysis", "explanation"));
        review.setRawJson(truncate(toJson(questionMap), MAX_LOG_TEXT_LENGTH));
        review.setStatus("PENDING");
        review.setCreateUserId(currentUserId());
        review.setUpdateTime(LocalDateTime.now());
        return review;
    }

    private List<Map<String, Object>> buildMainProjectOptions(Map<String, Object> questionMap, Integer quType, List<String> answers) {
        List<Map<String, Object>> result = new ArrayList<>();
        Object optionsObject = firstObject(questionMap, "options", "optionList", "choices");
        if ((optionsObject == null || isEmptyCollection(optionsObject)) && Integer.valueOf(3).equals(quType)) {
            List<String> options = new ArrayList<>();
            options.add("True");
            options.add("False");
            optionsObject = options;
        }
        if (optionsObject instanceof Map) {
            int index = 1;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) optionsObject).entrySet()) {
                String label = String.valueOf(entry.getKey()).trim();
                result.add(buildOption(index++, stripOptionLabel(String.valueOf(entry.getValue())), isAnswer(label, entry.getValue(), answers)));
            }
            return result;
        }
        if (optionsObject instanceof Collection) {
            int index = 1;
            for (Object optionObject : (Collection<?>) optionsObject) {
                Map<String, Object> optionMap = asStringMap(optionObject);
                String label = getString(optionMap, "label");
                String content = getString(optionMap, "content");
                Object rightObject = optionMap.get("isRight");
                if (!StringUtils.hasText(label)) {
                    label = optionLabel(index);
                }
                if (!StringUtils.hasText(content)) {
                    content = stripOptionLabel(String.valueOf(optionObject));
                }
                boolean right = rightObject != null ? isTruthy(rightObject) : isAnswer(label, content, answers);
                result.add(buildOption(index++, content, right));
            }
        }
        return result;
    }

    private Map<String, Object> buildOption(int sort, String content, boolean right) {
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("sort", sort);
        option.put("content", content);
        option.put("isRight", right ? 1 : 0);
        return option;
    }

    private List<String> normalizeAnswers(Object answerObject) {
        List<String> answers = new ArrayList<>();
        if (answerObject == null) {
            return answers;
        }
        if (answerObject instanceof Collection) {
            for (Object item : (Collection<?>) answerObject) {
                addAnswerTokens(answers, String.valueOf(item));
            }
            return answers;
        }
        addAnswerTokens(answers, String.valueOf(answerObject));
        return answers;
    }

    private void addAnswerTokens(List<String> answers, String answerText) {
        if (!StringUtils.hasText(answerText)) {
            return;
        }
        String clean = answerText.replace("[", "").replace("]", "").replace("\"", "").trim();
        String[] parts = clean.split("\\s+and\\s+|,|/|;");
        for (String part : parts) {
            String value = part.trim();
            if (StringUtils.hasText(value)) {
                answers.add(value);
            }
        }
    }

    private boolean isAnswer(String label, Object content, List<String> answers) {
        if (answers == null || answers.isEmpty()) {
            return false;
        }
        String normalizedLabel = normalizeAnswerText(label);
        String normalizedContent = normalizeAnswerText(String.valueOf(content));
        for (String answer : answers) {
            String normalizedAnswer = normalizeAnswerText(answer);
            if (normalizedAnswer.equals(normalizedLabel) || normalizedAnswer.equals(normalizedContent)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTruthy(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }
        String text = String.valueOf(value).trim();
        return "1".equals(text) || "true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text);
    }

    private boolean isEmptyCollection(Object value) {
        return value instanceof Collection && ((Collection<?>) value).isEmpty();
    }

    private String normalizeAnswerText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replaceFirst("^[A-Za-z][\\.|、|:|：]\\s*", "")
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    private String stripOptionLabel(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceFirst("^[A-Za-z][\\.|、|:|：]\\s*", "").trim();
    }

    private String optionLabel(int index) {
        return String.valueOf((char) ('A' + index - 1));
    }

    private Integer mapQuestionType(String type) {
        if (!StringUtils.hasText(type)) {
            return 1;
        }
        String normalized = type.trim().toUpperCase(Locale.ROOT);
        if ("1".equals(normalized) || normalized.contains("SINGLE")) {
            return 1;
        }
        if ("2".equals(normalized) || normalized.contains("MULTIPLE")) {
            return 2;
        }
        if ("3".equals(normalized) || normalized.contains("JUDGE") || normalized.contains("TRUE_FALSE") || normalized.contains("TRUEFALSE")) {
            return 3;
        }
        if ("4".equals(normalized) || normalized.contains("INDEFINITE")) {
            return 4;
        }
        return 1;
    }

    private String normalizeQuestionType(String type, Integer quType) {
        if (Integer.valueOf(1).equals(quType)) {
            return "SINGLE";
        }
        if (Integer.valueOf(2).equals(quType)) {
            return "MULTIPLE";
        }
        if (Integer.valueOf(3).equals(quType)) {
            return "JUDGE";
        }
        if (Integer.valueOf(4).equals(quType)) {
            return "INDEFINITE";
        }
        return StringUtils.hasText(type) ? type : "SINGLE";
    }

    private String normalizeDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            return "MEDIUM";
        }
        String normalized = difficulty.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("EASY")) {
            return "EASY";
        }
        if (normalized.contains("HARD")) {
            return "HARD";
        }
        return "MEDIUM";
    }

    private Object firstObject(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return map.get(key);
            }
        }
        return null;
    }

    private String firstString(Map<String, Object> first, Map<String, Object> second, String... keys) {
        for (String key : keys) {
            String value = getString(first, key);
            if (StringUtils.hasText(value)) {
                return value;
            }
            value = getString(second, key);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private Integer firstInteger(Map<String, Object> first, Map<String, Object> second, String... keys) {
        for (String key : keys) {
            Integer value = getInteger(first, key);
            if (value != null) {
                return value;
            }
            value = getInteger(second, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String getString(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return null;
        }
        return String.valueOf(map.get(key));
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> asStringMap(Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(value instanceof Map)) {
            return result;
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private Integer currentUserId() {
        try {
            return SecurityUtil.getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private void validateRealConfig() {
        if (!StringUtils.hasText(aiProperties.getBaseUrl())) {
            throw new IllegalStateException("ai.baseUrl is required");
        }
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw new IllegalStateException("ai.apiKey is required");
        }
        if (!StringUtils.hasText(aiProperties.getModel())) {
            throw new IllegalStateException("ai.model is required");
        }
    }

    private String resolveChatCompletionsUrl() {
        String baseUrl = aiProperties.getBaseUrl();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (baseUrl.endsWith("/chat/completions")) {
            return baseUrl;
        }
        return baseUrl + "/chat/completions";
    }

    private String extractContent(String rawJson) throws Exception {
        if (!StringUtils.hasText(rawJson)) {
            return "";
        }
        JsonNode root = objectMapper.readTree(rawJson);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (!contentNode.isMissingNode()) {
            return contentNode.asText();
        }
        return rawJson;
    }

    private Object parseContent(String content) {
        if (!StringUtils.hasText(content)) {
            return new LinkedHashMap<String, Object>();
        }
        try {
            return objectMapper.readValue(content, new TypeReference<Object>() {
            });
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("content", content);
            return result;
        }
    }

    private AiResponseVO buildResponse(String featureType, boolean mock, boolean success, Map<String, Object> parsedResult, String message) {
        AiResponseVO response = new AiResponseVO();
        response.setFeatureType(featureType);
        response.setMock(mock);
        response.setSuccess(success);
        response.setContent(String.valueOf(parsedResult.get("summary")));
        response.setParsedResult(parsedResult);
        response.setFriendlyMessage(message);
        return response;
    }

    private Object sanitize(Object value) {
        if (value == null) {
            return new LinkedHashMap<String, Object>();
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            Map<String, Object> sanitized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (isSensitiveKey(key)) {
                    sanitized.put(key, "[REDACTED]");
                } else {
                    sanitized.put(key, sanitize(entry.getValue()));
                }
            }
            return sanitized;
        }
        if (value instanceof Collection) {
            List<Object> sanitized = new ArrayList<>();
            for (Object item : (Collection<?>) value) {
                sanitized.add(sanitize(item));
            }
            return sanitized;
        }
        if (value instanceof String) {
            return truncate((String) value, 1000);
        }
        return value;
    }

    private boolean isSensitiveKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return normalized.contains("apikey")
                || normalized.contains("api_key")
                || normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("phone")
                || normalized.contains("mobile")
                || normalized.contains("email")
                || normalized.contains("idcard")
                || normalized.contains("identity")
                || normalized.contains("address")
                || normalized.contains("studentid")
                || normalized.contains("student_id")
                || normalized.contains("userid")
                || normalized.contains("user_id")
                || normalized.contains("realname")
                || normalized.contains("real_name")
                || normalized.contains("username")
                || normalized.contains("user_name");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private Map<String, Object> buildMockStudentReport(Map<String, Object> input) {
        Map<String, Object> result = baseMock("学生个人学情报告");
        result.put("overallPerformance", "本次考试整体表现已根据成绩、班级平均分、排名和答题用时生成 mock 评价。");
        result.put("strengthChapters", listOf("正确率较高的章节可继续保持，并作为后续复习的稳定得分点。"));
        result.put("weakChapters", listOf("正确率低于 70% 的章节建议优先复盘教材、视频和错题。"));
        result.put("weakKnowledgePoints", listOf("薄弱知识点建议按基础题、中等题、错题回练三个层次巩固。"));
        result.put("wrongQuestionReasonAnalysis", listOf("错题可能来自概念混淆、审题遗漏或选项排除不充分。"));
        result.put("videoLearningBehavior", "网课观看进度和完成率已纳入学习行为评价，未完成视频建议补齐到 90% 以上。");
        result.put("reviewSuggestions", listOf("先订正错题并写出错因", "再复习薄弱章节", "最后进行同知识点变式练习"));
        result.put("practiceRecommendations", listOf("薄弱知识点基础练习", "中等难度巩固练习", "高频错题回练"));
        result.put("chapterAnalysis", input == null ? null : input.get("chapterAccuracy"));
        result.put("knowledgePointAnalysis", input == null ? null : input.get("knowledgePointAccuracy"));
        result.put("wrongQuestions", input == null ? null : input.get("wrongQuestions"));
        result.put("difficultyPerformance", input == null ? null : input.get("difficultyPerformance"));
        result.put("videoProgress", input == null ? null : input.get("videoProgress"));
        result.put("learningTaskCompletion", input == null ? null : input.get("learningTaskCompletion"));
        return result;
    }

    private Map<String, Object> buildMockClassReport(Map<String, Object> input) {
        Map<String, Object> result = baseMock("班级学情报告");
        result.put("overallPerformance", "班级整体表现已根据均分、通过率、优秀率和分数段分布生成 mock 评价。");
        result.put("strengthChapters", listOf("章节平均正确率较高的内容可作为后续教学迁移基础。"));
        result.put("weakChapters", listOf("章节平均正确率低于 70% 的内容建议安排集中讲评和随堂练习。"));
        result.put("weakKnowledgePoints", listOf("薄弱知识点需要结合高频错题做概念辨析和变式训练。"));
        result.put("highFrequencyWrongQuestionAnalysis", listOf("高频错题优先分析题干关键词、易混选项和解题路径。"));
        result.put("difficultyPerformanceAnalysis", "难度表现用于判断基础巩固、中档提升和困难题拓展的教学比例。");
        result.put("videoLearningAnalysis", "网课完成率已纳入班级学习情况分析，可用于安排课前补学或课后巩固。");
        result.put("teachingSuggestions", listOf("先讲评共性错题", "再补充薄弱知识点例题", "最后发布针对性练习任务"));
        result.put("practiceDirections", listOf("薄弱章节基础题", "中等难度变式题", "高频错题同类题"));
        result.put("scoreOverview", input == null ? null : input.get("exam"));
        result.put("scoreSegmentDistribution", input == null ? null : input.get("scoreSegmentDistribution"));
        result.put("chapterAverageAccuracy", input == null ? null : input.get("chapterAverageAccuracy"));
        result.put("knowledgePointAverageAccuracy", input == null ? null : input.get("knowledgePointAverageAccuracy"));
        result.put("highFrequencyWrongQuestions", input == null ? null : input.get("highFrequencyWrongQuestions"));
        result.put("difficultyAccuracy", input == null ? null : input.get("difficultyAccuracy"));
        result.put("videoCompletion", input == null ? null : input.get("videoCompletion"));
        result.put("learningTaskCompletion", input == null ? null : input.get("learningTaskCompletion"));
        return result;
    }

    private Map<String, Object> buildMockTeachingPlan(Map<String, Object> input) {
        Map<String, Object> result = baseMock("教学文案");
        result.put("outline", listOf("目标导入", "错题归因", "例题讲解", "课堂练习", "课后任务"));
        return result;
    }

    private Map<String, Object> buildMockPaperReview(Map<String, Object> input) {
        Map<String, Object> result = baseMock("试卷讲评稿");
        result.put("sections", listOf("整体表现", "章节正确率", "高频错题", "复习建议"));
        return result;
    }

    private Map<String, Object> buildMockQuestions(Map<String, Object> input) {
        Map<String, Object> result = baseMock("AI 自动出题结果");
        List<Map<String, Object>> questions = new ArrayList<>();
        int count = parseInt(input == null ? null : input.get("count"), 1);
        String type = stringOrDefault(input == null ? null : input.get("questionType"), "SINGLE");
        String difficulty = stringOrDefault(input == null ? null : input.get("difficulty"), "MEDIUM");
        Integer courseId = parseInt(input == null ? null : input.get("courseId"), null);
        Integer chapterId = parseInt(input == null ? null : input.get("chapterId"), null);
        Integer knowledgePointId = parseInt(input == null ? null : input.get("knowledgePointId"), null);
        for (int i = 1; i <= count; i++) {
            Map<String, Object> question = new LinkedHashMap<>();
            question.put("type", type);
            question.put("quType", mapQuestionType(type));
            question.put("difficulty", difficulty);
            question.put("courseId", courseId);
            question.put("chapterId", chapterId);
            question.put("knowledgePointId", knowledgePointId);
            question.put("content", "这是 mock 模式生成的第 " + i + " 道示例题，教师审核后才能入库。");
            List<Map<String, Object>> options = new ArrayList<>();
            options.add(optionMock("A", "示例正确选项", true));
            options.add(optionMock("B", "示例干扰选项", false));
            options.add(optionMock("C", "示例干扰选项", false));
            options.add(optionMock("D", "示例干扰选项", false));
            if ("JUDGE".equalsIgnoreCase(type)) {
                options.clear();
                options.add(optionMock("A", "正确", true));
                options.add(optionMock("B", "错误", false));
            }
            if ("MULTIPLE".equalsIgnoreCase(type)) {
                options.get(2).put("isRight", true);
            }
            question.put("options", options);
            question.put("answer", collectMockAnswers(options));
            question.put("analysis", "mock 解析内容：根据课程、章节和知识点生成，需教师审核后入库。");
            questions.add(question);
        }
        result.put("questions", questions);
        result.put("needTeacherReview", true);
        return result;
    }

    private Map<String, Object> optionMock(String label, String content, boolean right) {
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("label", label);
        option.put("content", content);
        option.put("isRight", right);
        return option;
    }

    private List<String> collectMockAnswers(List<Map<String, Object>> options) {
        List<String> answers = new ArrayList<>();
        for (Map<String, Object> option : options) {
            if (Boolean.TRUE.equals(option.get("isRight"))) {
                answers.add(String.valueOf(option.get("label")));
            }
        }
        return answers;
    }

    private String stringOrDefault(Object value, String defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return defaultValue;
        }
        return String.valueOf(value);
    }

    private Integer parseInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Map<String, Object> buildMockPractice(Map<String, Object> input) {
        Map<String, Object> result = baseMock("推荐练习");
        result.put("summary", "已根据薄弱知识点、错题和难度表现生成推荐练习，建议先完成基础巩固，再进行中等难度变式训练。");
        result.put("recommendedKnowledgePoints", input == null ? null : input.get("weakKnowledgePoints"));
        result.put("difficultyPlan", listOf("EASY 优先用于补齐基础", "MEDIUM 用于巩固迁移"));
        result.put("recommendedQuestions", input == null ? null : input.get("recommendedQuestions"));
        result.put("taskTitleAdvice", "薄弱知识点推荐练习");
        result.put("deadlineAdvice", "建议 7 天内完成");
        result.put("reviewAdvice", listOf("先回看薄弱章节", "完成推荐练习", "订正错题并记录错因"));
        return result;
    }

    private Map<String, Object> buildMockLearningBehavior(Map<String, Object> input) {
        Map<String, Object> result = baseMock("学习行为评价");
        result.put("behaviorLevel", "良好");
        result.put("signals", listOf("任务完成及时", "视频进度稳定", "错题订正仍需加强"));
        return result;
    }

    private Map<String, Object> baseMock(String title) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("title", title);
        result.put("summary", title + " mock 内容已生成");
        result.put("generatedAt", LocalDateTime.now().toString());
        result.put("source", "mock");
        return result;
    }

    private List<String> listOf(String... values) {
        List<String> list = new ArrayList<>();
        if (values == null) {
            return list;
        }
        for (String value : values) {
            list.add(value);
        }
        return list;
    }
}
