package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.AiReportMapper;
import cn.org.alan.exam.mapper.CourseChapterMapper;
import cn.org.alan.exam.mapper.CourseVideoMapper;
import cn.org.alan.exam.mapper.ExamAnalysisMapper;
import cn.org.alan.exam.mapper.ExamMapper;
import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.mapper.ExamQuestionMapper;
import cn.org.alan.exam.mapper.GradeMapper;
import cn.org.alan.exam.mapper.KnowledgePointMapper;
import cn.org.alan.exam.mapper.LearningTaskMapper;
import cn.org.alan.exam.mapper.LearningTaskRecordMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.StudentVideoProgressMapper;
import cn.org.alan.exam.mapper.UserBookMapper;
import cn.org.alan.exam.mapper.UserExamsScoreMapper;
import cn.org.alan.exam.mapper.UserGradeMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.AiReport;
import cn.org.alan.exam.model.entity.CourseChapter;
import cn.org.alan.exam.model.entity.CourseVideo;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.entity.ExamQuestion;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.KnowledgePoint;
import cn.org.alan.exam.model.entity.LearningTask;
import cn.org.alan.exam.model.entity.LearningTaskRecord;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.StudentVideoProgress;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.entity.UserBook;
import cn.org.alan.exam.model.entity.UserExamsScore;
import cn.org.alan.exam.model.vo.ai.AiReportVO;
import cn.org.alan.exam.model.vo.ai.AiResponseVO;
import cn.org.alan.exam.model.vo.analysis.ChartDataVO;
import cn.org.alan.exam.model.vo.analysis.DifficultyAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.MasteryAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.WrongQuestionAnalysisVO;
import cn.org.alan.exam.service.IAiReportService;
import cn.org.alan.exam.service.IAiService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiReportServiceImpl implements IAiReportService {

    private static final String STUDENT_REPORT = "STUDENT_REPORT";
    private static final String CLASS_REPORT = "CLASS_REPORT";

    @Resource
    private IAiService aiService;
    @Resource
    private AiReportMapper aiReportMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserGradeMapper userGradeMapper;
    @Resource
    private GradeMapper gradeMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private ExamAnalysisMapper examAnalysisMapper;
    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private UserBookMapper userBookMapper;
    @Resource
    private CourseChapterMapper courseChapterMapper;
    @Resource
    private KnowledgePointMapper knowledgePointMapper;
    @Resource
    private StudentVideoProgressMapper studentVideoProgressMapper;
    @Resource
    private CourseVideoMapper courseVideoMapper;
    @Resource
    private LearningTaskRecordMapper learningTaskRecordMapper;
    @Resource
    private LearningTaskMapper learningTaskMapper;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public Result<AiReportVO> generateStudentReport(Integer examId, Integer studentId) {
        if (examId == null) {
            return Result.failed("Exam id is required");
        }
        User student = loadStudentWithPermission(studentId);
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return Result.failed("Exam not found");
        }
        UserExamsScore score = loadStudentScore(examId, student.getId());
        if (score == null) {
            return Result.failed("Student has no finished score for this exam");
        }

        Map<String, Object> input = buildStudentReportInput(exam, student, score);
        Result<AiResponseVO> aiResult = aiService.generateStudentReport(input);
        AiResponseVO response = aiResult == null ? null : aiResult.getData();
        if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            String message = response == null ? "AI report generation failed" : response.getFriendlyMessage();
            return Result.failed(StringUtils.hasText(message) ? message : "AI report generation failed");
        }

        AiReport report = new AiReport();
        report.setReportType(STUDENT_REPORT);
        report.setStudentId(student.getId());
        report.setClassId(student.getGradeId());
        report.setExamId(exam.getId());
        report.setTitle(buildReportTitle(exam));
        report.setInputJson(toJson(input));
        report.setOutputText(response.getContent());
        report.setOutputJson(toJson(response.getParsedResult()));
        report.setCreateTime(LocalDateTime.now());
        aiReportMapper.insert(report);
        return Result.success("Generate student AI learning report success", toVO(report));
    }

    @Override
    public Result<List<AiReportVO>> listStudentReports(Integer examId, Integer studentId) {
        User student = loadStudentWithPermission(studentId);
        LambdaQueryWrapper<AiReport> wrapper = new LambdaQueryWrapper<AiReport>()
                .eq(AiReport::getReportType, STUDENT_REPORT)
                .eq(AiReport::getStudentId, student.getId())
                .orderByDesc(AiReport::getCreateTime)
                .orderByDesc(AiReport::getId);
        if (examId != null) {
            wrapper.eq(AiReport::getExamId, examId);
        }
        List<AiReportVO> result = aiReportMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success("Query student AI learning report history success", result);
    }

    @Override
    public Result<AiReportVO> generateClassReport(Integer examId, Integer gradeId) {
        if (examId == null) {
            return Result.failed("Exam id is required");
        }
        Grade grade = loadGradeWithPermission(gradeId);
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return Result.failed("Exam not found");
        }
        List<Integer> scores = classScores(examId, grade.getId());
        if (scores.isEmpty()) {
            return Result.failed("This class has no finished score for this exam");
        }

        Map<String, Object> input = buildClassReportInput(exam, grade, scores);
        Result<AiResponseVO> aiResult = aiService.generateClassReport(input);
        AiResponseVO response = aiResult == null ? null : aiResult.getData();
        if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            String message = response == null ? "AI class report generation failed" : response.getFriendlyMessage();
            return Result.failed(StringUtils.hasText(message) ? message : "AI class report generation failed");
        }

        AiReport report = new AiReport();
        report.setReportType(CLASS_REPORT);
        report.setClassId(grade.getId());
        report.setExamId(exam.getId());
        report.setTitle(buildClassReportTitle(exam, grade));
        report.setInputJson(toJson(input));
        report.setOutputText(response.getContent());
        report.setOutputJson(toJson(response.getParsedResult()));
        report.setCreateTime(LocalDateTime.now());
        aiReportMapper.insert(report);
        return Result.success("Generate class AI learning report success", toVO(report));
    }

    @Override
    public Result<List<AiReportVO>> listClassReports(Integer examId, Integer gradeId) {
        Grade grade = loadGradeWithPermission(gradeId);
        LambdaQueryWrapper<AiReport> wrapper = new LambdaQueryWrapper<AiReport>()
                .eq(AiReport::getReportType, CLASS_REPORT)
                .eq(AiReport::getClassId, grade.getId())
                .orderByDesc(AiReport::getCreateTime)
                .orderByDesc(AiReport::getId);
        if (examId != null) {
            wrapper.eq(AiReport::getExamId, examId);
        }
        List<AiReportVO> result = aiReportMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success("Query class AI learning report history success", result);
    }

    @Override
    public Result<AiReportVO> regenerateReport(Integer reportId) {
        if (reportId == null) {
            return Result.failed("Report id is required");
        }
        AiReport report = aiReportMapper.selectById(reportId);
        if (report == null) {
            return Result.failed("AI learning report not found");
        }
        if (STUDENT_REPORT.equals(report.getReportType())) {
            loadStudentWithPermission(report.getStudentId());
            return generateStudentReport(report.getExamId(), report.getStudentId());
        }
        if (CLASS_REPORT.equals(report.getReportType())) {
            loadGradeWithPermission(report.getClassId());
            return generateClassReport(report.getExamId(), report.getClassId());
        }
        return Result.failed("Unsupported AI learning report type");
    }

    private Map<String, Object> buildStudentReportInput(Exam exam, User student, UserExamsScore score) {
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, exam.getId()));
        Map<Integer, Question> questionMap = loadExamQuestionMap(examQuestions);
        Set<Integer> courseIds = collectCourseIds(questionMap.values());
        List<MasteryAnalysisVO> chapterAnalysis = examAnalysisMapper.selectStudentChapterAnalysis(exam.getId(), student.getId());
        List<MasteryAnalysisVO> knowledgeAnalysis = examAnalysisMapper.selectStudentKnowledgeAnalysis(exam.getId(), student.getId());
        List<DifficultyAnalysisVO> difficultyAnalysis = examAnalysisMapper.selectDifficultyAnalysis(exam.getId(), null, student.getId());
        fillMasteryLevels(chapterAnalysis);
        fillMasteryLevels(knowledgeAnalysis);
        fillDifficultyLevels(difficultyAnalysis);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("student", buildStudentSection(student));
        input.put("exam", buildExamSection(exam, score, student));
        input.put("chapterAccuracy", chapterAnalysis);
        input.put("knowledgePointAccuracy", knowledgeAnalysis);
        input.put("wrongQuestions", buildWrongQuestions(exam.getId(), student.getId(), questionMap));
        input.put("difficultyPerformance", difficultyAnalysis);
        input.put("answerTime", buildAnswerTime(score));
        input.put("videoProgress", buildVideoProgress(student.getId(), courseIds));
        input.put("learningTaskCompletion", buildLearningTaskCompletion(student, exam.getId(), courseIds));
        input.put("requiredSections", requiredStudentReportSections());
        return input;
    }

    private Map<String, Object> buildClassReportInput(Exam exam, Grade grade, List<Integer> scores) {
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, exam.getId()));
        Map<Integer, Question> questionMap = loadExamQuestionMap(examQuestions);
        Set<Integer> courseIds = collectCourseIds(questionMap.values());

        List<MasteryAnalysisVO> chapterAnalysis = examAnalysisMapper.selectClassChapterAnalysis(exam.getId(), grade.getId());
        List<MasteryAnalysisVO> knowledgeAnalysis = examAnalysisMapper.selectClassKnowledgeAnalysis(exam.getId(), grade.getId());
        List<WrongQuestionAnalysisVO> wrongQuestions = examAnalysisMapper.selectWrongQuestions(exam.getId(), grade.getId());
        List<DifficultyAnalysisVO> difficultyAnalysis = examAnalysisMapper.selectDifficultyAnalysis(exam.getId(), grade.getId(), null);
        List<ChartDataVO> scoreSegments = examAnalysisMapper.selectScoreSegmentChart(exam.getId(), grade.getId());
        fillMasteryLevels(chapterAnalysis);
        fillMasteryLevels(knowledgeAnalysis);
        fillDifficultyLevels(difficultyAnalysis);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("exam", buildClassExamSection(exam, grade, scores));
        input.put("scoreSegmentDistribution", scoreSegments);
        input.put("chapterAverageAccuracy", chapterAnalysis);
        input.put("knowledgePointAverageAccuracy", knowledgeAnalysis);
        input.put("highFrequencyWrongQuestions", wrongQuestions);
        input.put("difficultyAccuracy", difficultyAnalysis);
        input.put("videoCompletion", buildClassVideoCompletion(grade.getId(), courseIds));
        input.put("learningTaskCompletion", buildClassTaskCompletion(grade.getId(), exam.getId(), courseIds));
        input.put("constraints", requiredClassReportConstraints());
        input.put("requiredSections", requiredClassReportSections());
        return input;
    }

    private Map<String, Object> buildClassExamSection(Exam exam, Grade grade, List<Integer> scores) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("examId", exam.getId());
        section.put("examName", exam.getTitle());
        section.put("classId", grade.getId());
        section.put("className", grade.getGradeName());
        section.put("participantCount", scores.size());
        section.put("grossScore", valueOrZero(exam.getGrossScore()));
        section.put("passedScore", resolvePassedScore(exam));
        section.put("averageScore", average(scores));
        section.put("highestScore", max(scores));
        section.put("lowestScore", min(scores));
        section.put("passRate", passRate(scores, resolvePassedScore(exam)));
        section.put("excellentRate", excellentRate(scores, valueOrZero(exam.getGrossScore())));
        return section;
    }

    private Map<String, Object> buildClassVideoCompletion(Integer gradeId, Set<Integer> courseIds) {
        List<Integer> studentIds = classStudentIds(gradeId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentCount", studentIds.size());
        if (studentIds.isEmpty()) {
            result.put("videoCount", 0);
            result.put("expectedRecords", 0);
            result.put("completedRecords", 0);
            result.put("completionRate", 0D);
            return result;
        }

        List<CourseVideo> videos = new ArrayList<>();
        if (!courseIds.isEmpty()) {
            videos = courseVideoMapper.selectList(new LambdaQueryWrapper<CourseVideo>()
                    .in(CourseVideo::getCourseId, courseIds)
                    .eq(CourseVideo::getStatus, 1));
        }
        Set<Integer> videoIds = videos.stream()
                .map(CourseVideo::getId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LambdaQueryWrapper<StudentVideoProgress> wrapper = new LambdaQueryWrapper<StudentVideoProgress>()
                .in(StudentVideoProgress::getStudentId, studentIds);
        if (!courseIds.isEmpty()) {
            wrapper.in(StudentVideoProgress::getCourseId, courseIds);
        }
        if (!videoIds.isEmpty()) {
            wrapper.in(StudentVideoProgress::getVideoId, videoIds);
        }
        List<StudentVideoProgress> progressRows = studentVideoProgressMapper.selectList(wrapper);

        int expectedRecords = videoIds.isEmpty() ? progressRows.size() : studentIds.size() * videoIds.size();
        int completedRecords = 0;
        Set<String> completedPairs = new LinkedHashSet<>();
        for (StudentVideoProgress row : progressRows) {
            if (Integer.valueOf(1).equals(row.getCompleted())) {
                String key = String.valueOf(row.getStudentId()) + ":" + String.valueOf(row.getVideoId());
                if (completedPairs.add(key)) {
                    completedRecords++;
                }
            }
        }

        result.put("videoCount", videoIds.size());
        result.put("expectedRecords", expectedRecords);
        result.put("completedRecords", completedRecords);
        result.put("completionRate", rate(completedRecords, expectedRecords));
        return result;
    }

    private Map<String, Object> buildClassTaskCompletion(Integer gradeId, Integer examId, Set<Integer> courseIds) {
        List<Integer> studentIds = classStudentIds(gradeId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentCount", studentIds.size());
        if (studentIds.isEmpty()) {
            result.put("totalRecords", 0);
            result.put("completedRecords", 0);
            result.put("completionRate", 0D);
            result.put("taskTypeStats", new ArrayList<Map<String, Object>>());
            return result;
        }

        List<LearningTaskRecord> records = learningTaskRecordMapper.selectList(new LambdaQueryWrapper<LearningTaskRecord>()
                .in(LearningTaskRecord::getStudentId, studentIds));
        int totalRecords = 0;
        int completedRecords = 0;
        Map<String, int[]> typeStats = new LinkedHashMap<>();
        for (LearningTaskRecord record : records) {
            LearningTask task = learningTaskMapper.selectById(record.getTaskId());
            if (task == null || !isClassTaskRelevant(task, gradeId, examId, courseIds)) {
                continue;
            }
            totalRecords++;
            boolean completed = isTaskRecordCompleted(record);
            if (completed) {
                completedRecords++;
            }
            String type = StringUtils.hasText(task.getTaskType()) ? task.getTaskType() : "UNKNOWN";
            int[] stats = typeStats.computeIfAbsent(type, key -> new int[2]);
            stats[0]++;
            if (completed) {
                stats[1]++;
            }
        }

        List<Map<String, Object>> taskTypeStats = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : typeStats.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("taskType", entry.getKey());
            item.put("total", entry.getValue()[0]);
            item.put("completed", entry.getValue()[1]);
            item.put("completionRate", rate(entry.getValue()[1], entry.getValue()[0]));
            taskTypeStats.add(item);
        }

        result.put("totalRecords", totalRecords);
        result.put("completedRecords", completedRecords);
        result.put("completionRate", rate(completedRecords, totalRecords));
        result.put("taskTypeStats", taskTypeStats);
        return result;
    }

    private boolean isClassTaskRelevant(LearningTask task, Integer gradeId, Integer examId, Set<Integer> courseIds) {
        if (examId != null && examId.equals(task.getRelatedExamId())) {
            return true;
        }
        if (gradeId != null && gradeId.equals(task.getTargetClassId())) {
            return true;
        }
        return task.getCourseId() != null && courseIds.contains(task.getCourseId());
    }

    private boolean isTaskRecordCompleted(LearningTaskRecord record) {
        if ("COMPLETED".equalsIgnoreCase(record.getStatus())) {
            return true;
        }
        if (record.getFinishTime() != null) {
            return true;
        }
        return record.getProgressRate() != null && record.getProgressRate().doubleValue() >= 100D;
    }

    private Map<String, Object> buildStudentSection(User student) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("anonymousId", "student-" + student.getId());
        section.put("classId", student.getGradeId());
        return section;
    }

    private Map<String, Object> buildExamSection(Exam exam, UserExamsScore score, User student) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("examId", exam.getId());
        section.put("examName", exam.getTitle());
        section.put("grossScore", exam.getGrossScore());
        section.put("studentScore", valueOrZero(score.getUserScore()));
        section.put("scoreRate", rate(valueOrZero(score.getUserScore()), valueOrZero(exam.getGrossScore())));
        section.put("classAverage", classAverage(exam.getId(), student.getGradeId()));
        section.put("studentRank", studentRank(exam.getId(), student.getGradeId(), valueOrZero(score.getUserScore())));
        section.put("submittedAt", timeText(score.getLimitTime()));
        return section;
    }

    private Map<String, Object> buildAnswerTime(UserExamsScore score) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("usedSeconds", valueOrZero(score.getUserTime()));
        section.put("totalSeconds", valueOrZero(score.getTotalTime()));
        section.put("usedRate", rate(valueOrZero(score.getUserTime()), valueOrZero(score.getTotalTime())));
        return section;
    }

    private List<Map<String, Object>> buildWrongQuestions(Integer examId, Integer studentId, Map<Integer, Question> questionMap) {
        List<UserBook> books = userBookMapper.selectList(new LambdaQueryWrapper<UserBook>()
                .eq(UserBook::getExamId, examId)
                .eq(UserBook::getUserId, studentId));
        List<ExamQuAnswer> answers = examQuAnswerMapper.selectList(new LambdaQueryWrapper<ExamQuAnswer>()
                .eq(ExamQuAnswer::getExamId, examId)
                .eq(ExamQuAnswer::getUserId, studentId));

        Set<Integer> wrongIds = new LinkedHashSet<>();
        for (UserBook book : books) {
            if (book.getQuId() != null) {
                wrongIds.add(book.getQuId());
            }
        }
        Map<Integer, ExamQuAnswer> answerMap = new HashMap<>();
        for (ExamQuAnswer answer : answers) {
            if (answer.getQuestionId() == null) {
                continue;
            }
            answerMap.put(answer.getQuestionId(), answer);
            if (!Integer.valueOf(1).equals(answer.getIsRight())) {
                wrongIds.add(answer.getQuestionId());
            }
        }
        if (wrongIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> missingIds = wrongIds.stream()
                .filter(id -> !questionMap.containsKey(id))
                .collect(Collectors.toList());
        if (!missingIds.isEmpty()) {
            List<Question> missingQuestions = questionMapper.selectBatchIds(missingIds);
            for (Question question : missingQuestions) {
                questionMap.put(question.getId(), question);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Integer questionId : wrongIds) {
            Question question = questionMap.get(questionId);
            if (question == null) {
                continue;
            }
            ExamQuAnswer answer = answerMap.get(questionId);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("questionId", questionId);
            item.put("questionType", question.getQuType());
            item.put("content", truncate(question.getContent(), 220));
            item.put("difficulty", normalizeDifficulty(question.getDifficulty()));
            item.put("chapterId", question.getChapterId());
            item.put("chapterName", chapterName(question.getChapterId()));
            item.put("knowledgePointId", question.getKnowledgePointId());
            item.put("knowledgePointName", knowledgePointName(question.getKnowledgePointId()));
            item.put("studentAnswer", answer == null ? null : answer.getAnswerId());
            item.put("studentAnswerText", answer == null ? null : truncate(answer.getAnswerContent(), 220));
            item.put("analysis", truncate(question.getAnalysis(), 220));
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> buildVideoProgress(Integer studentId, Set<Integer> courseIds) {
        LambdaQueryWrapper<StudentVideoProgress> wrapper = new LambdaQueryWrapper<StudentVideoProgress>()
                .eq(StudentVideoProgress::getStudentId, studentId)
                .orderByDesc(StudentVideoProgress::getLastWatchTime)
                .orderByDesc(StudentVideoProgress::getUpdateTime);
        if (!courseIds.isEmpty()) {
            wrapper.in(StudentVideoProgress::getCourseId, courseIds);
        }
        List<StudentVideoProgress> rows = studentVideoProgressMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        Map<Integer, CourseVideo> videoCache = new HashMap<>();
        for (StudentVideoProgress row : rows) {
            CourseVideo video = null;
            if (row.getVideoId() != null) {
                video = videoCache.get(row.getVideoId());
                if (video == null) {
                    video = courseVideoMapper.selectById(row.getVideoId());
                    if (video != null) {
                        videoCache.put(row.getVideoId(), video);
                    }
                }
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("videoId", row.getVideoId());
            item.put("videoTitle", video == null ? null : video.getTitle());
            item.put("courseId", row.getCourseId());
            item.put("chapterId", row.getChapterId());
            item.put("chapterName", chapterName(row.getChapterId()));
            item.put("watchedSeconds", valueOrZero(row.getWatchedSeconds()));
            item.put("duration", valueOrZero(row.getDuration()));
            item.put("progressRate", row.getProgressRate());
            item.put("completed", Integer.valueOf(1).equals(row.getCompleted()));
            item.put("lastWatchTime", timeText(row.getLastWatchTime()));
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> buildLearningTaskCompletion(User student, Integer examId, Set<Integer> courseIds) {
        List<LearningTaskRecord> records = learningTaskRecordMapper.selectList(new LambdaQueryWrapper<LearningTaskRecord>()
                .eq(LearningTaskRecord::getStudentId, student.getId())
                .orderByDesc(LearningTaskRecord::getUpdateTime)
                .orderByDesc(LearningTaskRecord::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (LearningTaskRecord record : records) {
            LearningTask task = learningTaskMapper.selectById(record.getTaskId());
            if (task == null || !isTaskRelevant(task, student, examId, courseIds)) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("taskId", task.getId());
            item.put("title", task.getTitle());
            item.put("taskType", task.getTaskType());
            item.put("courseId", task.getCourseId());
            item.put("chapterId", task.getChapterId());
            item.put("knowledgePointId", task.getKnowledgePointId());
            item.put("relatedExamId", task.getRelatedExamId());
            item.put("relatedPaperId", task.getRelatedPaperId());
            item.put("relatedVideoId", task.getRelatedVideoId());
            item.put("deadline", timeText(task.getDeadline()));
            item.put("status", record.getStatus());
            item.put("progressRate", record.getProgressRate());
            item.put("finishTime", timeText(record.getFinishTime()));
            result.add(item);
        }
        return result;
    }

    private boolean isTaskRelevant(LearningTask task, User student, Integer examId, Set<Integer> courseIds) {
        if (examId != null && examId.equals(task.getRelatedExamId())) {
            return true;
        }
        if (task.getCourseId() != null && courseIds.contains(task.getCourseId())) {
            return true;
        }
        if (student.getId().equals(task.getTargetStudentId())) {
            return true;
        }
        return student.getGradeId() != null && student.getGradeId().equals(task.getTargetClassId());
    }

    private Map<Integer, Question> loadExamQuestionMap(List<ExamQuestion> examQuestions) {
        Map<Integer, Question> result = new HashMap<>();
        List<Integer> questionIds = examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            return result;
        }
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        for (Question question : questions) {
            result.put(question.getId(), question);
        }
        return result;
    }

    private Set<Integer> collectCourseIds(Collection<Question> questions) {
        Set<Integer> result = new LinkedHashSet<>();
        for (Question question : questions) {
            if (question.getCourseId() != null) {
                result.add(question.getCourseId());
            }
        }
        return result;
    }

    private UserExamsScore loadStudentScore(Integer examId, Integer studentId) {
        return userExamsScoreMapper.selectOne(new LambdaQueryWrapper<UserExamsScore>()
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getUserId, studentId)
                .eq(UserExamsScore::getState, 1)
                .orderByDesc(UserExamsScore::getId)
                .last("limit 1"));
    }

    private User loadStudentWithPermission(Integer studentId) {
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer currentUserId = SecurityUtil.getUserId();
        Integer targetStudentId = studentId;
        if (roleCode == 1) {
            targetStudentId = currentUserId;
        }
        if (targetStudentId == null) {
            throw new ServiceRuntimeException("Student user id is required");
        }
        User student = userMapper.selectById(targetStudentId);
        if (student == null) {
            throw new ServiceRuntimeException("Student not found");
        }
        if (roleCode == 2) {
            List<Integer> gradeIds = userGradeMapper.getGradeIdListByUserId(currentUserId);
            if (student.getGradeId() == null || !gradeIds.contains(student.getGradeId())) {
                throw new ServiceRuntimeException("No permission to access this student report");
            }
        }
        return student;
    }

    private Grade loadGradeWithPermission(Integer gradeId) {
        if (gradeId == null) {
            throw new ServiceRuntimeException("Class id is required");
        }
        Grade grade = gradeMapper.selectById(gradeId);
        if (grade == null) {
            throw new ServiceRuntimeException("Class not found");
        }
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode == 1) {
            throw new ServiceRuntimeException("No permission to access class report");
        }
        if (roleCode == 2 && !userGradeMapper.getGradeIdListByUserId(SecurityUtil.getUserId()).contains(gradeId)) {
            throw new ServiceRuntimeException("No permission to access this class report");
        }
        return grade;
    }

    private Double classAverage(Integer examId, Integer gradeId) {
        List<Integer> scores = classScores(examId, gradeId);
        if (scores.isEmpty()) {
            return 0D;
        }
        double total = 0D;
        for (Integer score : scores) {
            total += valueOrZero(score);
        }
        return round(total / scores.size());
    }

    private Integer studentRank(Integer examId, Integer gradeId, Integer studentScore) {
        List<Integer> scores = classScores(examId, gradeId);
        if (scores.isEmpty()) {
            return 1;
        }
        scores.sort(Comparator.reverseOrder());
        int rank = 1;
        for (Integer score : scores) {
            if (valueOrZero(score) > valueOrZero(studentScore)) {
                rank++;
            }
        }
        return rank;
    }

    private List<Integer> classScores(Integer examId, Integer gradeId) {
        if (gradeId == null) {
            return new ArrayList<>();
        }
        List<UserExamsScore> rows = userExamsScoreMapper.selectList(new LambdaQueryWrapper<UserExamsScore>()
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 1)
                .in(UserExamsScore::getWhetherMark, -1, 1));
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> result = new ArrayList<>();
        Map<Integer, User> userCache = new HashMap<>();
        for (UserExamsScore row : rows) {
            if (row.getUserId() == null) {
                continue;
            }
            User user = userCache.get(row.getUserId());
            if (user == null) {
                user = userMapper.selectById(row.getUserId());
                if (user != null) {
                    userCache.put(row.getUserId(), user);
                }
            }
            if (user != null && gradeId.equals(user.getGradeId())) {
                result.add(valueOrZero(row.getUserScore()));
            }
        }
        return result;
    }

    private List<Integer> classStudentIds(Integer gradeId) {
        if (gradeId == null) {
            return new ArrayList<>();
        }
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getGradeId, gradeId)
                .eq(User::getStatus, 1));
        return users.stream()
                .map(User::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
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
        if (value >= 85D) {
            return "掌握优秀";
        }
        if (value >= 70D) {
            return "掌握良好";
        }
        if (value >= 60D) {
            return "基本掌握";
        }
        return "掌握薄弱";
    }

    private String chapterName(Integer chapterId) {
        if (chapterId == null) {
            return null;
        }
        CourseChapter chapter = courseChapterMapper.selectById(chapterId);
        return chapter == null ? null : chapter.getTitle();
    }

    private String knowledgePointName(Integer knowledgePointId) {
        if (knowledgePointId == null) {
            return null;
        }
        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(knowledgePointId);
        return knowledgePoint == null ? null : knowledgePoint.getName();
    }

    private List<String> requiredStudentReportSections() {
        List<String> sections = new ArrayList<>();
        Collections.addAll(sections,
                "本次考试整体表现",
                "优势章节",
                "薄弱章节",
                "薄弱知识点",
                "错题原因分析",
                "网课学习行为评价",
                "后续复习建议",
                "推荐练习方向");
        return sections;
    }

    private List<String> requiredClassReportSections() {
        List<String> sections = new ArrayList<>();
        Collections.addAll(sections,
                "班级整体表现",
                "班级优势章节",
                "班级薄弱章节",
                "班级薄弱知识点",
                "高频错题分析",
                "题目难度表现分析",
                "网课学习情况分析",
                "后续教学建议",
                "建议布置的练习方向");
        return sections;
    }

    private List<String> requiredClassReportConstraints() {
        List<String> constraints = new ArrayList<>();
        Collections.addAll(constraints,
                "不要生成家校沟通反馈",
                "不要出现过度标签化学生的表达",
                "重点服务教师教学改进");
        return constraints;
    }

    private String buildReportTitle(Exam exam) {
        return (StringUtils.hasText(exam.getTitle()) ? exam.getTitle() : "考试") + " - AI 学情报告";
    }

    private String buildClassReportTitle(Exam exam, Grade grade) {
        String examName = StringUtils.hasText(exam.getTitle()) ? exam.getTitle() : "考试";
        String gradeName = StringUtils.hasText(grade.getGradeName()) ? grade.getGradeName() : "班级";
        return examName + " - " + gradeName + " AI 班级学情报告";
    }

    private AiReportVO toVO(AiReport report) {
        AiReportVO vo = new AiReportVO();
        vo.setId(report.getId());
        vo.setReportType(report.getReportType());
        vo.setStudentId(report.getStudentId());
        vo.setClassId(report.getClassId());
        vo.setExamId(report.getExamId());
        vo.setTitle(report.getTitle());
        vo.setOutputText(report.getOutputText());
        vo.setOutputJson(report.getOutputJson());
        vo.setCreateTime(report.getCreateTime());
        return vo;
    }

    private String normalizeDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            return "MEDIUM";
        }
        String value = difficulty.trim().toUpperCase();
        if ("EASY".equals(value) || "MEDIUM".equals(value) || "HARD".equals(value)) {
            return value;
        }
        return "MEDIUM";
    }

    private Integer valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Integer resolvePassedScore(Exam exam) {
        if (exam.getPassedScore() != null) {
            return exam.getPassedScore();
        }
        return (int) Math.round(valueOrZero(exam.getGrossScore()) * 0.6D);
    }

    private Double average(List<Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0D;
        }
        double total = 0D;
        for (Integer score : scores) {
            total += valueOrZero(score);
        }
        return round(total / scores.size());
    }

    private Integer max(List<Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0;
        }
        return Collections.max(scores);
    }

    private Integer min(List<Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0;
        }
        return Collections.min(scores);
    }

    private Double passRate(List<Integer> scores, Integer passedScore) {
        if (scores == null || scores.isEmpty()) {
            return 0D;
        }
        int count = 0;
        for (Integer score : scores) {
            if (valueOrZero(score) >= valueOrZero(passedScore)) {
                count++;
            }
        }
        return rate(count, scores.size());
    }

    private Double excellentRate(List<Integer> scores, Integer grossScore) {
        if (scores == null || scores.isEmpty() || valueOrZero(grossScore) == 0) {
            return 0D;
        }
        int count = 0;
        for (Integer score : scores) {
            if (rate(valueOrZero(score), grossScore) >= 85D) {
                count++;
            }
        }
        return rate(count, scores.size());
    }

    private Double rate(Integer numerator, Integer denominator) {
        if (denominator == null || denominator == 0) {
            return 0D;
        }
        return round(valueOrZero(numerator) * 100D / denominator);
    }

    private Double round(Double value) {
        return Math.round(value * 100D) / 100D;
    }

    private String timeText(LocalDateTime time) {
        return time == null ? null : time.toString();
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}
