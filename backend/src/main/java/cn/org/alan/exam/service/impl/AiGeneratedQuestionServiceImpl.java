package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.config.AiProperties;
import cn.org.alan.exam.mapper.AiGeneratedQuestionMapper;
import cn.org.alan.exam.mapper.CourseChapterMapper;
import cn.org.alan.exam.mapper.KnowledgePointMapper;
import cn.org.alan.exam.model.entity.AiGeneratedQuestion;
import cn.org.alan.exam.model.entity.Course;
import cn.org.alan.exam.model.entity.CourseChapter;
import cn.org.alan.exam.model.entity.KnowledgePoint;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.model.form.ai.AiGeneratedQuestionForm;
import cn.org.alan.exam.model.form.ai.AiQuestionGenerateForm;
import cn.org.alan.exam.model.form.question.QuestionFrom;
import cn.org.alan.exam.model.vo.ai.AiGeneratedQuestionVO;
import cn.org.alan.exam.model.vo.ai.AiResponseVO;
import cn.org.alan.exam.service.IAiGeneratedQuestionService;
import cn.org.alan.exam.service.IAiService;
import cn.org.alan.exam.service.ICourseService;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiGeneratedQuestionServiceImpl implements IAiGeneratedQuestionService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final Set<String> DIFFICULTIES = new HashSet<>(Arrays.asList("EASY", "MEDIUM", "HARD"));

    private final RestTemplate restTemplate = new RestTemplate();

    @Resource
    private IAiService aiService;
    @Resource
    private AiProperties aiProperties;
    @Resource
    private ICourseService courseService;
    @Resource
    private IQuestionService questionService;
    @Resource
    private AiGeneratedQuestionMapper aiGeneratedQuestionMapper;
    @Resource
    private CourseChapterMapper courseChapterMapper;
    @Resource
    private KnowledgePointMapper knowledgePointMapper;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Result<List<AiGeneratedQuestionVO>> generateQuestions(AiQuestionGenerateForm form) {
        String questionType = normalizeQuestionType(form.getQuestionType());
        if (questionType == null) {
            return Result.failed("Question type must be SINGLE, MULTIPLE, INDEFINITE or JUDGE");
        }
        String difficulty = normalizeDifficulty(form.getDifficulty());
        if (difficulty == null) {
            return Result.failed("Difficulty must be EASY, MEDIUM or HARD");
        }

        Course course = courseService.getManageableCourse(form.getCourseId());
        CourseChapter chapter = validateChapter(form.getChapterId(), course.getId());
        KnowledgePoint knowledgePoint = validateKnowledgePoint(form.getKnowledgePointId(), course.getId(), chapter.getId());

        Map<String, Object> input = buildAiInput(form, questionType, difficulty, course, chapter, knowledgePoint);
        Object generatedResult;
        try {
            generatedResult = callQuestionGenerator(input);
        } catch (ServiceRuntimeException e) {
            return Result.failed(e.getMessage());
        }
        List<Object> generatedItems = extractQuestionItems(generatedResult);
        if (generatedItems.isEmpty()) {
            return Result.failed("AI did not return any questions");
        }

        List<AiGeneratedQuestionVO> result = new ArrayList<>();
        for (Object item : generatedItems) {
            Map<String, Object> normalized = normalizeGeneratedQuestion(item, questionType, difficulty, course, chapter, knowledgePoint);
            AiGeneratedQuestion generatedQuestion = new AiGeneratedQuestion();
            generatedQuestion.setTeacherId(SecurityUtil.getUserId());
            generatedQuestion.setCourseId(course.getId());
            generatedQuestion.setChapterId(chapter.getId());
            generatedQuestion.setKnowledgePointId(knowledgePoint.getId());
            generatedQuestion.setQuestionType(questionType);
            generatedQuestion.setDifficulty(difficulty);
            generatedQuestion.setContentJson(toJson(normalized));
            generatedQuestion.setStatus(STATUS_PENDING);
            generatedQuestion.setUpdateTime(LocalDateTime.now());
            aiGeneratedQuestionMapper.insert(generatedQuestion);
            result.add(toVO(generatedQuestion));
        }
        return Result.success("AI questions generated into review queue", result);
    }

    private Object callQuestionGenerator(Map<String, Object> input) {
        if (StringUtils.hasText(aiProperties.getQuestionGeneratorServiceUrl())) {
            try {
                return restTemplate.postForObject(resolveQuestionGeneratorUrl(), input, Map.class);
            } catch (Exception e) {
                throw new ServiceRuntimeException("AI question generator service is unavailable");
            }
        }
        Result<AiResponseVO> aiResult = aiService.generateQuestions(input);
        AiResponseVO response = aiResult == null ? null : aiResult.getData();
        if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            String message = response == null ? "AI question generation failed" : response.getFriendlyMessage();
            throw new ServiceRuntimeException(StringUtils.hasText(message) ? message : "AI question generation failed");
        }
        return response.getParsedResult();
    }

    private String resolveQuestionGeneratorUrl() {
        String url = aiProperties.getQuestionGeneratorServiceUrl().trim();
        if (url.endsWith("/generate-questions")) {
            return url;
        }
        if (url.endsWith("/")) {
            return url + "generate-questions";
        }
        return url + "/generate-questions";
    }

    @Override
    public Result<IPage<AiGeneratedQuestionVO>> paging(Integer pageNum, Integer pageSize, String status,
                                                       Integer courseId, Integer chapterId, Integer knowledgePointId,
                                                       String questionType, String difficulty) {
        LambdaQueryWrapper<AiGeneratedQuestion> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(AiGeneratedQuestion::getStatus, status.trim().toUpperCase(Locale.ROOT));
        }
        if (courseId != null) {
            wrapper.eq(AiGeneratedQuestion::getCourseId, courseId);
        }
        if (chapterId != null) {
            wrapper.eq(AiGeneratedQuestion::getChapterId, chapterId);
        }
        if (knowledgePointId != null) {
            wrapper.eq(AiGeneratedQuestion::getKnowledgePointId, knowledgePointId);
        }
        String normalizedType = normalizeQuestionType(questionType);
        if (normalizedType != null) {
            wrapper.eq(AiGeneratedQuestion::getQuestionType, normalizedType);
        }
        String normalizedDifficulty = normalizeDifficulty(difficulty);
        if (normalizedDifficulty != null) {
            wrapper.eq(AiGeneratedQuestion::getDifficulty, normalizedDifficulty);
        }
        if (SecurityUtil.getRoleCode() == 2) {
            wrapper.eq(AiGeneratedQuestion::getTeacherId, SecurityUtil.getUserId());
        }
        wrapper.orderByDesc(AiGeneratedQuestion::getCreateTime).orderByDesc(AiGeneratedQuestion::getId);

        IPage<AiGeneratedQuestion> page = aiGeneratedQuestionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return Result.success("Query AI generated questions success", page.convert(this::toVO));
    }

    @Override
    @Transactional
    public Result<AiGeneratedQuestionVO> updateGeneratedQuestion(Integer id, AiGeneratedQuestionForm form) {
        AiGeneratedQuestion existing = loadManageable(id);
        if (!STATUS_PENDING.equals(existing.getStatus())) {
            return Result.failed("Only pending AI generated questions can be edited");
        }
        String questionType = normalizeQuestionType(form.getQuestionType());
        String difficulty = normalizeDifficulty(form.getDifficulty());
        if (questionType == null || difficulty == null) {
            return Result.failed("Question type or difficulty is invalid");
        }
        Course course = courseService.getManageableCourse(form.getCourseId());
        CourseChapter chapter = validateChapter(form.getChapterId(), course.getId());
        KnowledgePoint knowledgePoint = validateKnowledgePoint(form.getKnowledgePointId(), course.getId(), chapter.getId());

        Map<String, Object> content = contentFromEditForm(form, questionType, difficulty, course, chapter, knowledgePoint);
        existing.setCourseId(course.getId());
        existing.setChapterId(chapter.getId());
        existing.setKnowledgePointId(knowledgePoint.getId());
        existing.setQuestionType(questionType);
        existing.setDifficulty(difficulty);
        existing.setContentJson(toJson(content));
        existing.setUpdateTime(LocalDateTime.now());
        aiGeneratedQuestionMapper.updateById(existing);
        return Result.success("Update AI generated question success", toVO(existing));
    }

    @Override
    @Transactional
    public Result<String> approve(Integer id, Integer repoId) {
        if (repoId == null) {
            return Result.failed("Repo id is required before approving question");
        }
        AiGeneratedQuestion generatedQuestion = loadManageable(id);
        if (!STATUS_PENDING.equals(generatedQuestion.getStatus())) {
            return Result.failed("Only pending AI generated questions can be approved");
        }
        QuestionFrom questionFrom = toQuestionForm(generatedQuestion, repoId);
        Result<String> addResult = questionService.addSingleQuestion(questionFrom);
        if (addResult == null || addResult.getCode() == null || addResult.getCode() != 1) {
            return Result.failed(addResult == null ? "Approve question failed" : addResult.getMsg());
        }

        generatedQuestion.setStatus(STATUS_APPROVED);
        generatedQuestion.setUpdateTime(LocalDateTime.now());
        aiGeneratedQuestionMapper.updateById(generatedQuestion);
        return Result.success("AI generated question approved into question bank");
    }

    @Override
    @Transactional
    public Result<String> reject(Integer id) {
        AiGeneratedQuestion generatedQuestion = loadManageable(id);
        if (STATUS_APPROVED.equals(generatedQuestion.getStatus())) {
            return Result.failed("Approved question cannot be rejected");
        }
        generatedQuestion.setStatus(STATUS_REJECTED);
        generatedQuestion.setUpdateTime(LocalDateTime.now());
        aiGeneratedQuestionMapper.updateById(generatedQuestion);
        return Result.success("AI generated question rejected");
    }

    private Map<String, Object> buildAiInput(AiQuestionGenerateForm form, String questionType, String difficulty,
                                             Course course, CourseChapter chapter, KnowledgePoint knowledgePoint) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("courseId", course.getId());
        input.put("courseName", course.getName());
        input.put("chapterId", chapter.getId());
        input.put("chapterTitle", chapter.getTitle());
        input.put("knowledgePointId", knowledgePoint.getId());
        input.put("knowledgePointName", knowledgePoint.getName());
        input.put("questionType", questionType);
        input.put("quType", quType(questionType));
        input.put("difficulty", difficulty);
        input.put("count", form.getCount());
        input.put("optionCount", form.getOptionCount() == null ? 4 : form.getOptionCount());
        input.put("extraRequirement", form.getExtraRequirement());
        input.put("language", "zh-CN");
        input.put("needTeacherReview", true);
        return input;
    }

    private List<Object> extractQuestionItems(Object parsedResult) {
        Map<String, Object> parsed = asMap(parsedResult);
        Object questions = parsed.get("questions");
        if (!(questions instanceof Collection)) {
            return new ArrayList<>();
        }
        return new ArrayList<>((Collection<?>) questions);
    }

    private Map<String, Object> normalizeGeneratedQuestion(Object item, String questionType, String difficulty,
                                                           Course course, CourseChapter chapter, KnowledgePoint knowledgePoint) {
        Map<String, Object> raw = asMap(item);
        String itemType = normalizeQuestionType(firstString(raw, "type", "questionType"));
        String effectiveType = itemType == null ? questionType : itemType;
        String itemDifficulty = normalizeDifficulty(firstString(raw, "difficulty"));
        String effectiveDifficulty = itemDifficulty == null ? difficulty : itemDifficulty;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", effectiveType);
        result.put("quType", quType(effectiveType));
        result.put("difficulty", effectiveDifficulty);
        result.put("courseId", course.getId());
        result.put("courseName", course.getName());
        result.put("chapterId", chapter.getId());
        result.put("chapterTitle", chapter.getTitle());
        result.put("knowledgePointId", knowledgePoint.getId());
        result.put("knowledgePointName", knowledgePoint.getName());
        result.put("content", firstString(raw, "content", "question", "stem"));
        List<Map<String, Object>> options = normalizeOptions(raw.get("options"), raw.get("answer"), effectiveType);
        result.put("options", options);
        result.put("answer", answerLabels(options));
        result.put("analysis", firstString(raw, "analysis", "explanation"));
        return result;
    }

    private Map<String, Object> contentFromEditForm(AiGeneratedQuestionForm form, String questionType, String difficulty,
                                                    Course course, CourseChapter chapter, KnowledgePoint knowledgePoint) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", questionType);
        result.put("quType", quType(questionType));
        result.put("difficulty", difficulty);
        result.put("courseId", course.getId());
        result.put("courseName", course.getName());
        result.put("chapterId", chapter.getId());
        result.put("chapterTitle", chapter.getTitle());
        result.put("knowledgePointId", knowledgePoint.getId());
        result.put("knowledgePointName", knowledgePoint.getName());
        result.put("content", form.getContent());
        List<Map<String, Object>> options = new ArrayList<>();
        if (form.getOptions() != null) {
            int index = 0;
            for (AiGeneratedQuestionForm.OptionItem option : form.getOptions()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("label", StringUtils.hasText(option.getLabel()) ? option.getLabel().trim().toUpperCase(Locale.ROOT) : label(index));
                item.put("content", option.getContent());
                item.put("isRight", Boolean.TRUE.equals(option.getIsRight()));
                options.add(item);
                index++;
            }
        }
        result.put("options", options);
        result.put("answer", answerLabels(options));
        result.put("analysis", form.getAnalysis());
        return result;
    }

    private QuestionFrom toQuestionForm(AiGeneratedQuestion generatedQuestion, Integer repoId) {
        Map<String, Object> content = parseContentJson(generatedQuestion.getContentJson());
        QuestionFrom form = new QuestionFrom();
        form.setRepoId(repoId);
        form.setCourseId(generatedQuestion.getCourseId());
        form.setChapterId(generatedQuestion.getChapterId());
        form.setKnowledgePointId(generatedQuestion.getKnowledgePointId());
        form.setDifficulty(generatedQuestion.getDifficulty());
        form.setQuType(quType(generatedQuestion.getQuestionType()));
        form.setContent(firstString(content, "content", "question", "stem"));
        form.setAnalysis(firstString(content, "analysis", "explanation"));

        List<Map<String, Object>> optionMaps = normalizeOptions(content.get("options"), content.get("answer"), generatedQuestion.getQuestionType());
        List<Option> options = new ArrayList<>();
        int sort = 0;
        for (Map<String, Object> optionMap : optionMaps) {
            Option option = new Option();
            option.setContent(firstString(optionMap, "content"));
            option.setImage(firstString(optionMap, "image"));
            option.setIsRight(Boolean.TRUE.equals(optionMap.get("isRight")) ? 1 : 0);
            option.setSort(sort++);
            options.add(option);
        }
        form.setOptions(options);
        return form;
    }

    private List<Map<String, Object>> normalizeOptions(Object optionsObject, Object answerObject, String questionType) {
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> answerSet = answerSet(answerObject);
        if (optionsObject instanceof Collection) {
            int index = 0;
            for (Object item : (Collection<?>) optionsObject) {
                Map<String, Object> option = asMap(item);
                String label = firstString(option, "label", "key", "sort");
                if (!StringUtils.hasText(label)) {
                    label = label(index);
                }
                String content = firstString(option, "content", "text", "value");
                if (!StringUtils.hasText(content) && item instanceof String) {
                    content = stripLeadingLabel((String) item);
                }
                boolean isRight = booleanValue(option.get("isRight"))
                        || booleanValue(option.get("correct"))
                        || answerSet.contains(label.toUpperCase(Locale.ROOT));
                Map<String, Object> normalized = new LinkedHashMap<>();
                normalized.put("label", label.toUpperCase(Locale.ROOT));
                normalized.put("content", content);
                normalized.put("isRight", isRight);
                result.add(normalized);
                index++;
            }
        }
        if (result.isEmpty() && "JUDGE".equals(questionType)) {
            result.add(option("A", "正确", answerSet.isEmpty() || answerSet.contains("A") || answerSet.contains("TRUE") || answerSet.contains("正确")));
            result.add(option("B", "错误", answerSet.contains("B") || answerSet.contains("FALSE") || answerSet.contains("错误")));
        }
        return result;
    }

    private Map<String, Object> option(String label, String content, boolean isRight) {
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("label", label);
        option.put("content", content);
        option.put("isRight", isRight);
        return option;
    }

    private Set<String> answerSet(Object answerObject) {
        Set<String> answers = new LinkedHashSet<>();
        if (answerObject instanceof Collection) {
            for (Object item : (Collection<?>) answerObject) {
                addAnswer(answers, item);
            }
        } else {
            addAnswer(answers, answerObject);
        }
        return answers;
    }

    private void addAnswer(Set<String> answers, Object answer) {
        if (answer == null) {
            return;
        }
        String text = String.valueOf(answer).trim();
        if (!StringUtils.hasText(text)) {
            return;
        }
        for (String item : text.split(",")) {
            if (StringUtils.hasText(item)) {
                answers.add(item.trim().toUpperCase(Locale.ROOT));
            }
        }
    }

    private List<String> answerLabels(List<Map<String, Object>> options) {
        return options.stream()
                .filter(option -> Boolean.TRUE.equals(option.get("isRight")))
                .map(option -> String.valueOf(option.get("label")))
                .collect(Collectors.toList());
    }

    private AiGeneratedQuestion loadManageable(Integer id) {
        AiGeneratedQuestion generatedQuestion = aiGeneratedQuestionMapper.selectById(id);
        if (generatedQuestion == null) {
            throw new ServiceRuntimeException("AI generated question not found");
        }
        if (SecurityUtil.getRoleCode() == 2 && !SecurityUtil.getUserId().equals(generatedQuestion.getTeacherId())) {
            throw new ServiceRuntimeException("No permission to manage this AI generated question");
        }
        return generatedQuestion;
    }

    private CourseChapter validateChapter(Integer chapterId, Integer courseId) {
        CourseChapter chapter = courseChapterMapper.selectById(chapterId);
        if (chapter == null || !courseId.equals(chapter.getCourseId())) {
            throw new ServiceRuntimeException("Chapter does not belong to selected course");
        }
        return chapter;
    }

    private KnowledgePoint validateKnowledgePoint(Integer knowledgePointId, Integer courseId, Integer chapterId) {
        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(knowledgePointId);
        if (knowledgePoint == null
                || !courseId.equals(knowledgePoint.getCourseId())
                || !chapterId.equals(knowledgePoint.getChapterId())) {
            throw new ServiceRuntimeException("Knowledge point does not belong to selected course chapter");
        }
        return knowledgePoint;
    }

    private AiGeneratedQuestionVO toVO(AiGeneratedQuestion entity) {
        AiGeneratedQuestionVO vo = new AiGeneratedQuestionVO();
        vo.setId(entity.getId());
        vo.setTeacherId(entity.getTeacherId());
        vo.setCourseId(entity.getCourseId());
        vo.setChapterId(entity.getChapterId());
        vo.setKnowledgePointId(entity.getKnowledgePointId());
        vo.setQuestionType(entity.getQuestionType());
        vo.setDifficulty(entity.getDifficulty());
        vo.setContentJson(entity.getContentJson());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private String normalizeQuestionType(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim().toUpperCase(Locale.ROOT);
        if ("1".equals(text) || "SINGLE".equals(text) || "SINGLE_CHOICE".equals(text)) {
            return "SINGLE";
        }
        if ("2".equals(text) || "MULTIPLE".equals(text) || "MULTIPLE_CHOICE".equals(text)) {
            return "MULTIPLE";
        }
        if ("3".equals(text) || "JUDGE".equals(text) || "TRUE_FALSE".equals(text)) {
            return "JUDGE";
        }
        if ("4".equals(text) || "INDEFINITE".equals(text) || "UNCERTAIN".equals(text)) {
            return "INDEFINITE";
        }
        return null;
    }

    private Integer quType(String questionType) {
        String normalized = normalizeQuestionType(questionType);
        if ("SINGLE".equals(normalized)) {
            return 1;
        }
        if ("MULTIPLE".equals(normalized)) {
            return 2;
        }
        if ("JUDGE".equals(normalized)) {
            return 3;
        }
        if ("INDEFINITE".equals(normalized)) {
            return 4;
        }
        return null;
    }

    private String normalizeDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            return null;
        }
        String normalized = difficulty.trim().toUpperCase(Locale.ROOT);
        return DIFFICULTIES.contains(normalized) ? normalized : null;
    }

    private Map<String, Object> parseContentJson(String contentJson) {
        try {
            return objectMapper.readValue(contentJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map) {
            Map<?, ?> raw = (Map<?, ?>) value;
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : raw.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
        return new LinkedHashMap<>();
    }

    private String firstString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private boolean booleanValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value == null) {
            return false;
        }
        String text = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        return "1".equals(text) || "true".equals(text) || "yes".equals(text);
    }

    private String label(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private String stripLeadingLabel(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceFirst("^[A-Ha-h][\\.、\\)]\\s*", "");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}
