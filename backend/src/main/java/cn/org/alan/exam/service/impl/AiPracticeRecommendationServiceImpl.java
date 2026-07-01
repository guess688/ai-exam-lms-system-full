package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.CourseMapper;
import cn.org.alan.exam.mapper.ExamAnalysisMapper;
import cn.org.alan.exam.mapper.ExamMapper;
import cn.org.alan.exam.mapper.ExamQuestionMapper;
import cn.org.alan.exam.mapper.GradeMapper;
import cn.org.alan.exam.mapper.KnowledgePointMapper;
import cn.org.alan.exam.mapper.OptionMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.RepoMapper;
import cn.org.alan.exam.mapper.UserGradeMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.Course;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamQuestion;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.KnowledgePoint;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.ai.PracticeRecommendationGenerateForm;
import cn.org.alan.exam.model.form.ai.PracticeRecommendationPublishForm;
import cn.org.alan.exam.model.form.task.LearningTaskForm;
import cn.org.alan.exam.model.vo.ai.AiResponseVO;
import cn.org.alan.exam.model.vo.ai.PracticeRecommendationVO;
import cn.org.alan.exam.model.vo.analysis.DifficultyAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.MasteryAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.WrongQuestionAnalysisVO;
import cn.org.alan.exam.service.IAiPracticeRecommendationService;
import cn.org.alan.exam.service.IAiService;
import cn.org.alan.exam.service.ILearningTaskService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiPracticeRecommendationServiceImpl implements IAiPracticeRecommendationService {

    private static final String TARGET_CLASS = "CLASS";
    private static final String TARGET_STUDENT = "STUDENT";
    private static final double WEAK_RATE = 60D;

    @Resource
    private IAiService aiService;
    @Resource
    private ILearningTaskService learningTaskService;
    @Resource
    private ExamAnalysisMapper examAnalysisMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private OptionMapper optionMapper;
    @Resource
    private RepoMapper repoMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private KnowledgePointMapper knowledgePointMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserGradeMapper userGradeMapper;
    @Resource
    private GradeMapper gradeMapper;

    @Override
    public Result<PracticeRecommendationVO> generateRecommendation(PracticeRecommendationGenerateForm form) {
        String targetType = normalizeTargetType(form.getTargetType());
        if (targetType == null) {
            return Result.failed("Target type must be CLASS or STUDENT");
        }
        Exam exam = examMapper.selectById(form.getExamId());
        if (exam == null) {
            return Result.failed("Exam not found");
        }
        Integer gradeId = resolveGradeId(targetType, form.getGradeId(), form.getStudentId());
        Integer studentId = TARGET_STUDENT.equals(targetType) ? resolveStudentId(form.getStudentId()) : null;

        List<MasteryAnalysisVO> chapterAnalysis;
        List<MasteryAnalysisVO> knowledgeAnalysis;
        List<DifficultyAnalysisVO> difficultyAnalysis;
        List<WrongQuestionAnalysisVO> wrongQuestions = new ArrayList<>();
        if (TARGET_STUDENT.equals(targetType)) {
            chapterAnalysis = examAnalysisMapper.selectStudentChapterAnalysis(form.getExamId(), studentId);
            knowledgeAnalysis = examAnalysisMapper.selectStudentKnowledgeAnalysis(form.getExamId(), studentId);
            difficultyAnalysis = examAnalysisMapper.selectDifficultyAnalysis(form.getExamId(), null, studentId);
        } else {
            chapterAnalysis = examAnalysisMapper.selectClassChapterAnalysis(form.getExamId(), gradeId);
            knowledgeAnalysis = examAnalysisMapper.selectClassKnowledgeAnalysis(form.getExamId(), gradeId);
            difficultyAnalysis = examAnalysisMapper.selectDifficultyAnalysis(form.getExamId(), gradeId, null);
            wrongQuestions = examAnalysisMapper.selectWrongQuestions(form.getExamId(), gradeId);
        }
        fillMasteryLevels(chapterAnalysis);
        fillMasteryLevels(knowledgeAnalysis);
        fillDifficultyLevels(difficultyAnalysis);

        List<MasteryAnalysisVO> weakKnowledge = weakOrLowest(knowledgeAnalysis);
        List<MasteryAnalysisVO> weakChapters = weakOrLowest(chapterAnalysis);
        List<String> difficultyPlan = difficultyPlan(difficultyAnalysis);
        List<PracticeRecommendationVO.RecommendedQuestionVO> questions = recommendQuestions(form.getExamId(), weakKnowledge, difficultyPlan,
                form.getQuestionCount() == null ? 12 : form.getQuestionCount());

        PracticeRecommendationVO vo = new PracticeRecommendationVO();
        vo.setExamId(form.getExamId());
        vo.setTargetType(targetType);
        vo.setGradeId(gradeId);
        vo.setStudentId(studentId);
        vo.setRecommendedChapters(weakChapters);
        vo.setRecommendedKnowledgePoints(weakKnowledge);
        vo.setDifficultyPerformance(difficultyAnalysis);
        vo.setRecommendedQuestions(questions);
        vo.setRecommendedDifficulty(String.join("/", difficultyPlan));
        vo.setSuggestedTaskTitle(buildTaskTitle(exam, weakKnowledge));
        vo.setSuggestedDeadline(LocalDateTime.now().plusDays(7));
        Object aiResult = callAiRecommendation(exam, targetType, weakChapters, weakKnowledge, difficultyAnalysis, wrongQuestions, questions);
        vo.setAiResult(aiResult);
        vo.setReason(extractReason(aiResult, weakKnowledge, difficultyPlan));
        vo.setSuggestedTaskDescription(buildTaskDescription(vo));
        return Result.success("Generate practice recommendation success", vo);
    }

    @Override
    @Transactional
    public Result<String> publishRecommendation(PracticeRecommendationPublishForm form) {
        String targetType = normalizeTargetType(form.getTargetType());
        if (targetType == null) {
            return Result.failed("Target type must be CLASS or STUDENT");
        }
        Integer gradeId = resolveGradeId(targetType, form.getGradeId(), form.getStudentId());
        Integer studentId = TARGET_STUDENT.equals(targetType) ? resolveStudentId(form.getStudentId()) : null;
        if (form.getQuestionIds() == null || form.getQuestionIds().isEmpty()) {
            return Result.failed("Please select recommended questions before publishing");
        }

        List<Question> selectedQuestions = questionMapper.selectBatchIds(form.getQuestionIds());
        if (selectedQuestions == null || selectedQuestions.isEmpty()) {
            return Result.failed("Selected recommended questions not found");
        }

        Repo repo = createPracticeRepo(form.getTitle());
        cloneQuestionsToRepo(selectedQuestions, repo.getId());

        LearningTaskForm taskForm = new LearningTaskForm();
        taskForm.setTitle(form.getTitle());
        taskForm.setDescription(form.getDescription());
        taskForm.setTaskType("PRACTICE");
        taskForm.setCourseId(form.getCourseId());
        taskForm.setChapterId(form.getChapterId());
        taskForm.setKnowledgePointId(form.getKnowledgePointId());
        taskForm.setTargetType(targetType);
        taskForm.setTargetClassId(TARGET_CLASS.equals(targetType) ? gradeId : null);
        taskForm.setTargetStudentId(TARGET_STUDENT.equals(targetType) ? studentId : null);
        taskForm.setRelatedExamId(form.getExamId());
        taskForm.setRelatedPaperId(repo.getId());
        taskForm.setDeadline(form.getDeadline());
        taskForm.setStatus(1);
        return learningTaskService.createTask(taskForm);
    }

    private List<PracticeRecommendationVO.RecommendedQuestionVO> recommendQuestions(Integer examId,
                                                                                   List<MasteryAnalysisVO> weakKnowledge,
                                                                                   List<String> difficultyPlan,
                                                                                   Integer limit) {
        Set<Integer> knowledgeIds = weakKnowledge.stream()
                .map(MasteryAnalysisVO::getId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Integer> courseIds = examCourseIds(examId);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (!knowledgeIds.isEmpty()) {
            wrapper.in(Question::getKnowledgePointId, knowledgeIds);
        }
        if (!courseIds.isEmpty()) {
            wrapper.in(Question::getCourseId, courseIds);
        }
        wrapper.in(Question::getDifficulty, difficultyPlan)
                .orderByAsc(Question::getDifficulty)
                .orderByDesc(Question::getCreateTime)
                .last("limit " + Math.max(1, limit));
        List<Question> questions = questionMapper.selectList(wrapper);
        return questions.stream().map(this::toRecommendedQuestionVO).collect(Collectors.toList());
    }

    private Object callAiRecommendation(Exam exam,
                                        String targetType,
                                        List<MasteryAnalysisVO> weakChapters,
                                        List<MasteryAnalysisVO> weakKnowledge,
                                        List<DifficultyAnalysisVO> difficultyAnalysis,
                                        List<WrongQuestionAnalysisVO> wrongQuestions,
                                        List<PracticeRecommendationVO.RecommendedQuestionVO> questions) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("examId", exam.getId());
        input.put("examName", exam.getTitle());
        input.put("targetType", targetType);
        input.put("weakChapters", weakChapters);
        input.put("weakKnowledgePoints", weakKnowledge);
        input.put("difficultyPerformance", difficultyAnalysis);
        input.put("highFrequencyWrongQuestions", wrongQuestions);
        input.put("recommendedQuestions", questions);
        Result<AiResponseVO> result = aiService.recommendPractice(input);
        AiResponseVO response = result == null ? null : result.getData();
        return response == null ? null : response.getParsedResult();
    }

    private Repo createPracticeRepo(String title) {
        Repo repo = new Repo();
        repo.setTitle(title + " - 推荐练习题库");
        repo.setIsExercise(1);
        repo.setUserId(SecurityUtil.getUserId());
        repoMapper.insert(repo);
        return repo;
    }

    private void cloneQuestionsToRepo(List<Question> originals, Integer repoId) {
        for (Question original : originals) {
            Question copy = new Question();
            BeanUtils.copyProperties(original, copy);
            copy.setId(null);
            copy.setRepoId(repoId);
            copy.setUserId(SecurityUtil.getUserId());
            copy.setCreateTime(null);
            copy.setIsDeleted(0);
            questionMapper.insert(copy);
            List<Option> options = optionMapper.selectAllByQuestionId(original.getId());
            for (Option option : options) {
                Option optionCopy = new Option();
                BeanUtils.copyProperties(option, optionCopy);
                optionCopy.setId(null);
                optionCopy.setQuId(copy.getId());
                optionCopy.setIsDeleted(0);
                optionMapper.insert(optionCopy);
            }
        }
    }

    private Set<Integer> examCourseIds(Integer examId) {
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId));
        if (examQuestions.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<Integer> questionIds = examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return questionMapper.selectBatchIds(questionIds).stream()
                .map(Question::getCourseId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private PracticeRecommendationVO.RecommendedQuestionVO toRecommendedQuestionVO(Question question) {
        PracticeRecommendationVO.RecommendedQuestionVO vo = new PracticeRecommendationVO.RecommendedQuestionVO();
        vo.setId(question.getId());
        vo.setQuType(question.getQuType());
        vo.setContent(question.getContent());
        vo.setCourseId(question.getCourseId());
        vo.setChapterId(question.getChapterId());
        vo.setKnowledgePointId(question.getKnowledgePointId());
        vo.setDifficulty(question.getDifficulty());
        Course course = question.getCourseId() == null ? null : courseMapper.selectById(question.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        KnowledgePoint knowledgePoint = question.getKnowledgePointId() == null ? null : knowledgePointMapper.selectById(question.getKnowledgePointId());
        if (knowledgePoint != null) {
            vo.setKnowledgePointName(knowledgePoint.getName());
        }
        return vo;
    }

    private Integer resolveGradeId(String targetType, Integer gradeId, Integer studentId) {
        if (TARGET_CLASS.equals(targetType)) {
            if (gradeId == null || gradeMapper.selectById(gradeId) == null) {
                throw new ServiceRuntimeException("Class id is required");
            }
            ensureTeacherCanUseGrade(gradeId);
            return gradeId;
        }
        User student = userMapper.selectById(studentId);
        if (student == null || !Integer.valueOf(1).equals(student.getRoleId())) {
            throw new ServiceRuntimeException("Student target requires a valid student");
        }
        if (student.getGradeId() != null) {
            ensureTeacherCanUseGrade(student.getGradeId());
        }
        return student.getGradeId();
    }

    private Integer resolveStudentId(Integer studentId) {
        if (studentId == null) {
            throw new ServiceRuntimeException("Student id is required");
        }
        return studentId;
    }

    private void ensureTeacherCanUseGrade(Integer gradeId) {
        if (SecurityUtil.getRoleCode() == 3) {
            return;
        }
        List<Integer> gradeIds = userGradeMapper.getGradeIdListByUserId(SecurityUtil.getUserId());
        if (gradeIds == null || !gradeIds.contains(gradeId)) {
            throw new ServiceRuntimeException("No permission for this class");
        }
    }

    private List<MasteryAnalysisVO> weakOrLowest(List<MasteryAnalysisVO> rows) {
        List<MasteryAnalysisVO> weak = rows.stream()
                .filter(item -> item.getRate() == null || item.getRate() < WEAK_RATE)
                .sorted(Comparator.comparing(item -> item.getRate() == null ? 0D : item.getRate()))
                .limit(5)
                .collect(Collectors.toList());
        if (!weak.isEmpty()) {
            return weak;
        }
        return rows.stream()
                .sorted(Comparator.comparing(item -> item.getRate() == null ? 0D : item.getRate()))
                .limit(3)
                .collect(Collectors.toList());
    }

    private List<String> difficultyPlan(List<DifficultyAnalysisVO> rows) {
        Double easy = rateOf(rows, "EASY");
        Double medium = rateOf(rows, "MEDIUM");
        Double hard = rateOf(rows, "HARD");
        if (easy >= 70D && medium >= 70D && hard < 60D) {
            return Arrays.asList("MEDIUM", "HARD");
        }
        return Arrays.asList("EASY", "MEDIUM");
    }

    private Double rateOf(List<DifficultyAnalysisVO> rows, String difficulty) {
        return rows.stream()
                .filter(item -> difficulty.equalsIgnoreCase(item.getDifficulty()))
                .map(DifficultyAnalysisVO::getRate)
                .findFirst()
                .orElse(0D);
    }

    private String buildTaskTitle(Exam exam, List<MasteryAnalysisVO> weakKnowledge) {
        String topic = weakKnowledge.isEmpty() ? "薄弱点" : weakKnowledge.get(0).getName();
        return exam.getTitle() + " - " + topic + " 推荐练习";
    }

    private String buildTaskDescription(PracticeRecommendationVO vo) {
        return vo.getReason() + "\n推荐难度：" + vo.getRecommendedDifficulty()
                + "\n推荐题量：" + (vo.getRecommendedQuestions() == null ? 0 : vo.getRecommendedQuestions().size());
    }

    private String extractReason(Object aiResult, List<MasteryAnalysisVO> weakKnowledge, List<String> difficultyPlan) {
        if (aiResult instanceof Map) {
            Object summary = ((Map<?, ?>) aiResult).get("summary");
            if (summary != null && StringUtils.hasText(String.valueOf(summary))) {
                return String.valueOf(summary);
            }
        }
        String point = weakKnowledge.isEmpty() ? "薄弱知识点" : weakKnowledge.get(0).getName();
        return "根据正确率低于 60% 或相对较低的知识点，优先推荐 " + point + " 相关的 "
                + String.join("/", difficultyPlan) + " 练习。";
    }

    private void fillMasteryLevels(List<MasteryAnalysisVO> rows) {
        for (MasteryAnalysisVO row : rows) {
            row.setLevel(masteryLevel(row.getRate()));
        }
    }

    private void fillDifficultyLevels(List<DifficultyAnalysisVO> rows) {
        for (DifficultyAnalysisVO row : rows) {
            row.setLevel(masteryLevel(row.getRate()));
        }
    }

    private String masteryLevel(Double rate) {
        double value = rate == null ? 0D : rate;
        if (value >= 85D) return "掌握优秀";
        if (value >= 70D) return "掌握良好";
        if (value >= 60D) return "基本掌握";
        return "掌握薄弱";
    }

    private String normalizeTargetType(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (TARGET_CLASS.equals(normalized) || TARGET_STUDENT.equals(normalized)) {
            return normalized;
        }
        return null;
    }
}
