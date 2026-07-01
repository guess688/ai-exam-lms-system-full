package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ExamAnalysisMapper;
import cn.org.alan.exam.mapper.ExamMapper;
import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.mapper.GradeMapper;
import cn.org.alan.exam.mapper.LearningTaskMapper;
import cn.org.alan.exam.mapper.LearningTaskRecordMapper;
import cn.org.alan.exam.mapper.StudentVideoProgressMapper;
import cn.org.alan.exam.mapper.UserExamsScoreMapper;
import cn.org.alan.exam.mapper.UserGradeMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.LearningTask;
import cn.org.alan.exam.model.entity.LearningTaskRecord;
import cn.org.alan.exam.model.entity.StudentVideoProgress;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.entity.UserExamsScore;
import cn.org.alan.exam.model.vo.ai.AiResponseVO;
import cn.org.alan.exam.model.vo.ai.LearningBehaviorEvaluationVO;
import cn.org.alan.exam.model.vo.analysis.MasteryAnalysisVO;
import cn.org.alan.exam.service.IAiService;
import cn.org.alan.exam.service.ILearningBehaviorService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class LearningBehaviorServiceImpl implements ILearningBehaviorService {

    private static final int RECENT_EXAM_LIMIT = 5;
    private static final double LOW_RATE = 60D;
    private static final double LOW_TASK_RATE = 70D;
    private static final double WRONG_REPEAT_RATE = 20D;

    @Resource
    private IAiService aiService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private GradeMapper gradeMapper;
    @Resource
    private UserGradeMapper userGradeMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private ExamAnalysisMapper examAnalysisMapper;
    @Resource
    private StudentVideoProgressMapper studentVideoProgressMapper;
    @Resource
    private LearningTaskMapper learningTaskMapper;
    @Resource
    private LearningTaskRecordMapper learningTaskRecordMapper;

    @Override
    public Result<LearningBehaviorEvaluationVO> getMyEvaluation() {
        User student = loadStudentWithPermission(SecurityUtil.getUserId());
        return Result.success("Query my learning behavior evaluation success", buildEvaluation(student, true));
    }

    @Override
    public Result<IPage<LearningBehaviorEvaluationVO>> pageStudentEvaluations(Integer pageNum,
                                                                              Integer pageSize,
                                                                              Integer gradeId,
                                                                              String realName) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getRoleId, 1)
                .eq(User::getStatus, 1)
                .orderByDesc(User::getCreateTime)
                .orderByDesc(User::getId);
        List<Integer> visibleGradeIds = visibleGradeIds();
        if (SecurityUtil.getRoleCode() == 2 && visibleGradeIds.isEmpty()) {
            return Result.success("Query student learning behavior evaluations success", emptyPage(pageNum, pageSize));
        }
        if (gradeId != null) {
            ensureGradeVisible(gradeId);
            wrapper.eq(User::getGradeId, gradeId);
        } else if (SecurityUtil.getRoleCode() == 2) {
            wrapper.in(User::getGradeId, visibleGradeIds);
        }
        if (realName != null && realName.trim().length() > 0) {
            wrapper.like(User::getRealName, realName.trim());
        }

        Page<User> userPage = userMapper.selectPage(new Page<>(safePageNum(pageNum), safePageSize(pageSize)), wrapper);
        Page<LearningBehaviorEvaluationVO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<LearningBehaviorEvaluationVO> records = userPage.getRecords().stream()
                .map(user -> buildEvaluation(user, false))
                .collect(Collectors.toList());
        resultPage.setRecords(records);
        return Result.success("Query student learning behavior evaluations success", resultPage);
    }

    @Override
    public Result<LearningBehaviorEvaluationVO> getStudentEvaluation(Integer studentId) {
        User student = loadStudentWithPermission(studentId);
        return Result.success("Query student learning behavior evaluation success", buildEvaluation(student, true));
    }

    private LearningBehaviorEvaluationVO buildEvaluation(User student, boolean includeAi) {
        Grade grade = student.getGradeId() == null ? null : gradeMapper.selectById(student.getGradeId());
        List<UserExamsScore> recentScores = recentScores(student.getId());
        List<LearningBehaviorEvaluationVO.ScoreTrendPointVO> scoreTrend = buildScoreTrend(recentScores);
        Integer latestExamId = latestExamId(recentScores);
        Integer previousExamId = previousExamId(recentScores);

        List<MasteryAnalysisVO> latestChapter = latestExamId == null ? new ArrayList<>()
                : examAnalysisMapper.selectStudentChapterAnalysis(latestExamId, student.getId());
        List<MasteryAnalysisVO> latestKnowledge = latestExamId == null ? new ArrayList<>()
                : examAnalysisMapper.selectStudentKnowledgeAnalysis(latestExamId, student.getId());
        fillMasteryLevels(latestChapter);
        fillMasteryLevels(latestKnowledge);

        LearningBehaviorEvaluationVO.MasteryChangeVO chapterChange = masteryChange(
                "CHAPTER", latestChapter, previousExamId == null ? new ArrayList<>()
                        : examAnalysisMapper.selectStudentChapterAnalysis(previousExamId, student.getId()));
        LearningBehaviorEvaluationVO.MasteryChangeVO knowledgeChange = masteryChange(
                "KNOWLEDGE", latestKnowledge, previousExamId == null ? new ArrayList<>()
                        : examAnalysisMapper.selectStudentKnowledgeAnalysis(previousExamId, student.getId()));

        WrongRepeatStats wrongRepeat = wrongRepeatStats(student.getId(), recentScores);
        VideoStats videoStats = videoStats(student.getId());
        TaskStats taskStats = taskStats(student.getId());

        List<LearningBehaviorEvaluationVO.BehaviorSignalVO> signals = ruleSignals(
                scoreTrend, latestChapter, wrongRepeat, videoStats, taskStats);
        List<String> suggestions = suggestions(signals);
        boolean needAttention = signals.stream().anyMatch(signal -> "ATTENTION".equals(signal.getLevel()));

        LearningBehaviorEvaluationVO vo = new LearningBehaviorEvaluationVO();
        vo.setStudentId(student.getId());
        vo.setStudentName(student.getRealName() == null ? student.getUserName() : student.getRealName());
        vo.setGradeId(student.getGradeId());
        vo.setGradeName(grade == null ? null : grade.getGradeName());
        vo.setRecentExamCount(recentScores.size());
        vo.setScoreTrend(scoreTrend);
        vo.setScoreTrendStatus(scoreTrendStatus(scoreTrend));
        vo.setWrongRepeatRate(wrongRepeat.repeatRate);
        vo.setRepeatedWrongQuestionCount(wrongRepeat.repeatedQuestionCount);
        vo.setChapterMasteryChange(chapterChange);
        vo.setKnowledgeMasteryChange(knowledgeChange);
        vo.setVideoCompletionRate(videoStats.completionRate);
        vo.setLastVideoStudyTime(videoStats.lastStudyTime);
        vo.setTaskCompletionRate(taskStats.completionRate);
        vo.setOnTimeCompletionRate(taskStats.onTimeRate);
        vo.setNeedAttention(needAttention);
        vo.setAttentionLevel(attentionLevel(signals));
        vo.setRuleSignals(signals);
        vo.setSuggestions(suggestions);
        vo.setLatestChapterMastery(latestChapter);
        vo.setLatestKnowledgeMastery(latestKnowledge);
        vo.setGeneratedAt(LocalDateTime.now());
        vo.setAiSummary(defaultSummary(signals));

        if (includeAi) {
            applyAiSummary(vo);
        }
        return vo;
    }

    private void applyAiSummary(LearningBehaviorEvaluationVO vo) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("student", anonymousStudent(vo));
        input.put("metrics", metrics(vo));
        input.put("scoreTrend", vo.getScoreTrend());
        input.put("chapterMasteryChange", vo.getChapterMasteryChange());
        input.put("knowledgeMasteryChange", vo.getKnowledgeMasteryChange());
        input.put("ruleSignals", vo.getRuleSignals());
        input.put("suggestions", vo.getSuggestions());
        input.put("constraints", constraints());
        try {
            Result<AiResponseVO> result = aiService.evaluateLearningBehavior(input);
            AiResponseVO response = result == null ? null : result.getData();
            if (response == null) {
                return;
            }
            vo.setAiResult(response.getParsedResult());
            String summary = extractSummary(response);
            if (summary != null && summary.trim().length() > 0) {
                vo.setAiSummary(summary);
            }
        } catch (Exception ignored) {
            vo.setAiSummary(defaultSummary(vo.getRuleSignals()));
        }
    }

    private Map<String, Object> anonymousStudent(LearningBehaviorEvaluationVO vo) {
        Map<String, Object> student = new LinkedHashMap<>();
        student.put("anonymousId", "student-" + vo.getStudentId());
        student.put("gradeId", vo.getGradeId());
        return student;
    }

    private Map<String, Object> metrics(LearningBehaviorEvaluationVO vo) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("recentExamCount", vo.getRecentExamCount());
        metrics.put("scoreTrendStatus", vo.getScoreTrendStatus());
        metrics.put("wrongRepeatRate", vo.getWrongRepeatRate());
        metrics.put("repeatedWrongQuestionCount", vo.getRepeatedWrongQuestionCount());
        metrics.put("videoCompletionRate", vo.getVideoCompletionRate());
        metrics.put("lastVideoStudyTime", vo.getLastVideoStudyTime());
        metrics.put("taskCompletionRate", vo.getTaskCompletionRate());
        metrics.put("onTimeCompletionRate", vo.getOnTimeCompletionRate());
        metrics.put("needAttention", vo.getNeedAttention());
        metrics.put("attentionLevel", vo.getAttentionLevel());
        return metrics;
    }

    private List<String> constraints() {
        List<String> result = new ArrayList<>();
        result.add("使用建议型、改进型表达");
        result.add("不要给学生贴固定标签");
        result.add("不要使用过度负面语言");
        result.add("重点说明下一步可以做什么");
        return result;
    }

    private String extractSummary(AiResponseVO response) {
        Object parsed = response.getParsedResult();
        if (parsed instanceof Map) {
            Object summary = ((Map<?, ?>) parsed).get("summary");
            if (summary != null) {
                return String.valueOf(summary);
            }
        }
        return response.getContent();
    }

    private List<UserExamsScore> recentScores(Integer studentId) {
        List<UserExamsScore> rows = userExamsScoreMapper.selectList(new LambdaQueryWrapper<UserExamsScore>()
                .eq(UserExamsScore::getUserId, studentId)
                .eq(UserExamsScore::getState, 1)
                .in(UserExamsScore::getWhetherMark, -1, 1)
                .orderByDesc(UserExamsScore::getLimitTime)
                .orderByDesc(UserExamsScore::getCreateTime)
                .orderByDesc(UserExamsScore::getId)
                .last("limit " + RECENT_EXAM_LIMIT));
        Collections.reverse(rows);
        return rows;
    }

    private List<LearningBehaviorEvaluationVO.ScoreTrendPointVO> buildScoreTrend(List<UserExamsScore> scores) {
        List<LearningBehaviorEvaluationVO.ScoreTrendPointVO> result = new ArrayList<>();
        for (UserExamsScore score : scores) {
            Exam exam = score.getExamId() == null ? null : examMapper.selectById(score.getExamId());
            LearningBehaviorEvaluationVO.ScoreTrendPointVO point = new LearningBehaviorEvaluationVO.ScoreTrendPointVO();
            point.setExamId(score.getExamId());
            point.setExamName(exam == null ? "考试" : exam.getTitle());
            point.setScore(valueOrZero(score.getUserScore()));
            point.setGrossScore(exam == null ? 0 : valueOrZero(exam.getGrossScore()));
            point.setRate(rate(point.getScore(), point.getGrossScore()));
            point.setSubmittedAt(score.getLimitTime() == null ? score.getCreateTime() : score.getLimitTime());
            result.add(point);
        }
        return result;
    }

    private Integer latestExamId(List<UserExamsScore> scores) {
        return scores.isEmpty() ? null : scores.get(scores.size() - 1).getExamId();
    }

    private Integer previousExamId(List<UserExamsScore> scores) {
        return scores.size() < 2 ? null : scores.get(scores.size() - 2).getExamId();
    }

    private LearningBehaviorEvaluationVO.MasteryChangeVO masteryChange(String dimension,
                                                                       List<MasteryAnalysisVO> latest,
                                                                       List<MasteryAnalysisVO> previous) {
        LearningBehaviorEvaluationVO.MasteryChangeVO vo = new LearningBehaviorEvaluationVO.MasteryChangeVO();
        vo.setDimension(dimension);
        vo.setLatestAverageRate(averageMastery(latest));
        vo.setPreviousAverageRate(previous == null || previous.isEmpty() ? null : averageMastery(previous));
        if (vo.getPreviousAverageRate() == null) {
            vo.setChangeRate(null);
            vo.setStatus("暂无对比");
            return vo;
        }
        double change = round(vo.getLatestAverageRate() - vo.getPreviousAverageRate());
        vo.setChangeRate(change);
        if (change > 3D) {
            vo.setStatus("有所提升");
        } else if (change < -3D) {
            vo.setStatus("有所回落");
        } else {
            vo.setStatus("基本稳定");
        }
        return vo;
    }

    private double averageMastery(List<MasteryAnalysisVO> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0D;
        }
        double total = 0D;
        int count = 0;
        for (MasteryAnalysisVO row : rows) {
            total += row.getRate() == null ? 0D : row.getRate();
            count++;
        }
        return round(total / count);
    }

    private WrongRepeatStats wrongRepeatStats(Integer studentId, List<UserExamsScore> recentScores) {
        WrongRepeatStats stats = new WrongRepeatStats();
        List<Integer> examIds = recentScores.stream()
                .map(UserExamsScore::getExamId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        if (examIds.isEmpty()) {
            return stats;
        }
        List<ExamQuAnswer> wrongAnswers = examQuAnswerMapper.selectList(new LambdaQueryWrapper<ExamQuAnswer>()
                .eq(ExamQuAnswer::getUserId, studentId)
                .in(ExamQuAnswer::getExamId, examIds)
                .ne(ExamQuAnswer::getIsRight, 1));
        Map<Integer, Integer> wrongCounts = new HashMap<>();
        for (ExamQuAnswer answer : wrongAnswers) {
            if (answer.getQuestionId() == null) {
                continue;
            }
            wrongCounts.put(answer.getQuestionId(), wrongCounts.getOrDefault(answer.getQuestionId(), 0) + 1);
        }
        int repeated = (int) wrongCounts.values().stream().filter(count -> count > 1).count();
        stats.repeatedQuestionCount = repeated;
        stats.repeatRate = rate(repeated, wrongCounts.size());
        return stats;
    }

    private VideoStats videoStats(Integer studentId) {
        VideoStats stats = new VideoStats();
        List<StudentVideoProgress> rows = studentVideoProgressMapper.selectList(new LambdaQueryWrapper<StudentVideoProgress>()
                .eq(StudentVideoProgress::getStudentId, studentId)
                .orderByDesc(StudentVideoProgress::getLastWatchTime)
                .orderByDesc(StudentVideoProgress::getUpdateTime));
        if (rows.isEmpty()) {
            return stats;
        }
        int completed = 0;
        for (StudentVideoProgress row : rows) {
            if (Integer.valueOf(1).equals(row.getCompleted())) {
                completed++;
            }
            if (stats.lastStudyTime == null) {
                stats.lastStudyTime = row.getLastWatchTime() == null ? row.getUpdateTime() : row.getLastWatchTime();
            }
        }
        stats.completionRate = rate(completed, rows.size());
        return stats;
    }

    private TaskStats taskStats(Integer studentId) {
        TaskStats stats = new TaskStats();
        List<LearningTaskRecord> records = learningTaskRecordMapper.selectList(new LambdaQueryWrapper<LearningTaskRecord>()
                .eq(LearningTaskRecord::getStudentId, studentId));
        stats.total = records.size();
        if (records.isEmpty()) {
            return stats;
        }
        int completed = 0;
        int onTime = 0;
        for (LearningTaskRecord record : records) {
            LearningTask task = record.getTaskId() == null ? null : learningTaskMapper.selectById(record.getTaskId());
            boolean isCompleted = isCompleted(record);
            if (isCompleted) {
                completed++;
                if (isOnTime(record, task)) {
                    onTime++;
                }
            }
        }
        stats.completionRate = rate(completed, records.size());
        stats.onTimeRate = rate(onTime, records.size());
        return stats;
    }

    private boolean isCompleted(LearningTaskRecord record) {
        if ("COMPLETED".equalsIgnoreCase(record.getStatus())) {
            return true;
        }
        if (record.getFinishTime() != null) {
            return true;
        }
        return record.getProgressRate() != null && record.getProgressRate().compareTo(BigDecimal.valueOf(100)) >= 0;
    }

    private boolean isOnTime(LearningTaskRecord record, LearningTask task) {
        if (task == null || task.getDeadline() == null) {
            return true;
        }
        return record.getFinishTime() != null && !record.getFinishTime().isAfter(task.getDeadline());
    }

    private List<LearningBehaviorEvaluationVO.BehaviorSignalVO> ruleSignals(
            List<LearningBehaviorEvaluationVO.ScoreTrendPointVO> scoreTrend,
            List<MasteryAnalysisVO> latestChapter,
            WrongRepeatStats wrongRepeat,
            VideoStats videoStats,
            TaskStats taskStats) {
        List<LearningBehaviorEvaluationVO.BehaviorSignalVO> signals = new ArrayList<>();

        if (isContinuousDecline(scoreTrend)) {
            signals.add(signal("SCORE_TREND", "ATTENTION",
                    "近期成绩有连续回落，需要适当关注学习节奏。",
                    "建议先复盘最近两次考试的错题和薄弱章节，再安排小题量巩固练习。"));
        } else if (isStableRising(scoreTrend)) {
            signals.add(signal("SCORE_TREND", "GOOD",
                    "近期成绩稳定上升，当前学习节奏是有效的。",
                    "建议继续保持复盘和练习频率，并逐步增加中等或困难题训练。"));
        } else if (scoreTrend.size() < 2) {
            signals.add(signal("SCORE_TREND", "INFO",
                    "近期考试数据还不多，暂时以本次掌握情况和学习过程作为主要参考。",
                    "建议完成后续考试或练习后再观察趋势。"));
        }

        boolean hasWeakChapter = latestChapter.stream().anyMatch(item -> item.getRate() == null || item.getRate() < LOW_RATE);
        if (videoStats.completionRate < LOW_RATE && hasWeakChapter) {
            signals.add(signal("VIDEO_REVIEW", "ATTENTION",
                    "网课完成率和部分章节正确率都偏低，建议优先补学对应章节。",
                    "可以先回看薄弱章节视频，再完成该章节的基础题和中等题。"));
        }

        if (taskStats.total > 0 && taskStats.completionRate < LOW_TASK_RATE) {
            signals.add(signal("TASK_COMPLETION", "IMPROVE",
                    "学习任务完成率不高，执行节奏还有提升空间。",
                    "建议把任务拆成更小的每日目标，并优先完成截止时间较近的任务。"));
        }

        if (wrongRepeat.repeatedQuestionCount > 0 && wrongRepeat.repeatRate >= WRONG_REPEAT_RATE) {
            signals.add(signal("WRONG_REPEAT", "IMPROVE",
                    "部分错题重复出现，说明相关知识点还需要再次巩固。",
                    "建议记录错因，隔天重新作答同类题，确认不是只记住答案。"));
        }

        if (signals.isEmpty()) {
            signals.add(signal("OVERALL", "GOOD",
                    "当前学习行为整体比较平稳，没有明显需要额外关注的信号。",
                    "建议保持现有学习节奏，并定期复盘错题和薄弱知识点。"));
        }
        return signals;
    }

    private LearningBehaviorEvaluationVO.BehaviorSignalVO signal(String type, String level, String message, String suggestion) {
        LearningBehaviorEvaluationVO.BehaviorSignalVO signal = new LearningBehaviorEvaluationVO.BehaviorSignalVO();
        signal.setType(type);
        signal.setLevel(level);
        signal.setMessage(message);
        signal.setSuggestion(suggestion);
        return signal;
    }

    private List<String> suggestions(List<LearningBehaviorEvaluationVO.BehaviorSignalVO> signals) {
        Set<String> result = new LinkedHashSet<>();
        for (LearningBehaviorEvaluationVO.BehaviorSignalVO signal : signals) {
            if (signal.getSuggestion() != null) {
                result.add(signal.getSuggestion());
            }
        }
        return new ArrayList<>(result);
    }

    private boolean isContinuousDecline(List<LearningBehaviorEvaluationVO.ScoreTrendPointVO> scoreTrend) {
        if (scoreTrend.size() < 3) {
            return false;
        }
        int size = scoreTrend.size();
        double a = valueOrZero(scoreTrend.get(size - 3).getRate());
        double b = valueOrZero(scoreTrend.get(size - 2).getRate());
        double c = valueOrZero(scoreTrend.get(size - 1).getRate());
        return a > b && b > c;
    }

    private boolean isStableRising(List<LearningBehaviorEvaluationVO.ScoreTrendPointVO> scoreTrend) {
        if (scoreTrend.size() < 3) {
            return false;
        }
        int size = scoreTrend.size();
        double a = valueOrZero(scoreTrend.get(size - 3).getRate());
        double b = valueOrZero(scoreTrend.get(size - 2).getRate());
        double c = valueOrZero(scoreTrend.get(size - 1).getRate());
        return a < b && b < c;
    }

    private String scoreTrendStatus(List<LearningBehaviorEvaluationVO.ScoreTrendPointVO> scoreTrend) {
        if (isContinuousDecline(scoreTrend)) {
            return "连续回落";
        }
        if (isStableRising(scoreTrend)) {
            return "稳定上升";
        }
        if (scoreTrend.size() < 2) {
            return "数据不足";
        }
        return "基本稳定";
    }

    private String attentionLevel(List<LearningBehaviorEvaluationVO.BehaviorSignalVO> signals) {
        boolean attention = signals.stream().anyMatch(signal -> "ATTENTION".equals(signal.getLevel()));
        if (attention) {
            return "需要关注";
        }
        boolean improve = signals.stream().anyMatch(signal -> "IMPROVE".equals(signal.getLevel()));
        if (improve) {
            return "建议跟进";
        }
        return "状态良好";
    }

    private String defaultSummary(List<LearningBehaviorEvaluationVO.BehaviorSignalVO> signals) {
        if (signals == null || signals.isEmpty()) {
            return "当前学习行为数据有限，建议继续积累考试、网课和任务完成记录后再观察变化。";
        }
        return signals.stream()
                .map(LearningBehaviorEvaluationVO.BehaviorSignalVO::getMessage)
                .collect(Collectors.joining(" "));
    }

    private void fillMasteryLevels(List<MasteryAnalysisVO> rows) {
        for (MasteryAnalysisVO row : rows) {
            double value = row.getRate() == null ? 0D : row.getRate();
            if (value >= 85D) {
                row.setLevel("掌握优秀");
            } else if (value >= 70D) {
                row.setLevel("掌握良好");
            } else if (value >= 60D) {
                row.setLevel("基本掌握");
            } else {
                row.setLevel("建议巩固");
            }
        }
    }

    private User loadStudentWithPermission(Integer studentId) {
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer currentUserId = SecurityUtil.getUserId();
        Integer targetStudentId = Integer.valueOf(1).equals(roleCode) ? currentUserId : studentId;
        if (targetStudentId == null) {
            throw new ServiceRuntimeException("Student id is required");
        }
        User student = userMapper.selectById(targetStudentId);
        if (student == null || !Integer.valueOf(1).equals(student.getRoleId())) {
            throw new ServiceRuntimeException("Student not found");
        }
        if (Integer.valueOf(2).equals(roleCode)) {
            ensureGradeVisible(student.getGradeId());
        }
        return student;
    }

    private List<Integer> visibleGradeIds() {
        if (!Integer.valueOf(2).equals(SecurityUtil.getRoleCode())) {
            return new ArrayList<>();
        }
        List<Integer> gradeIds = userGradeMapper.getGradeIdListByUserId(SecurityUtil.getUserId());
        return gradeIds == null ? new ArrayList<>() : gradeIds;
    }

    private void ensureGradeVisible(Integer gradeId) {
        if (gradeId == null) {
            throw new ServiceRuntimeException("Class id is required");
        }
        if (Integer.valueOf(3).equals(SecurityUtil.getRoleCode())) {
            return;
        }
        List<Integer> gradeIds = visibleGradeIds();
        if (!gradeIds.contains(gradeId)) {
            throw new ServiceRuntimeException("No permission for this class");
        }
    }

    private Page<LearningBehaviorEvaluationVO> emptyPage(Integer pageNum, Integer pageSize) {
        Page<LearningBehaviorEvaluationVO> page = new Page<>(safePageNum(pageNum), safePageSize(pageSize), 0);
        page.setRecords(new ArrayList<>());
        return page;
    }

    private long safePageNum(Integer value) {
        return value == null || value < 1 ? 1L : value.longValue();
    }

    private long safePageSize(Integer value) {
        if (value == null || value < 1) {
            return 10L;
        }
        return Math.min(value, 50);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0D : value;
    }

    private double rate(double numerator, double denominator) {
        if (denominator <= 0D) {
            return 0D;
        }
        return round(numerator * 100D / denominator);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static class WrongRepeatStats {
        private double repeatRate = 0D;
        private int repeatedQuestionCount = 0;
    }

    private static class VideoStats {
        private double completionRate = 0D;
        private LocalDateTime lastStudyTime;
    }

    private static class TaskStats {
        private int total = 0;
        private double completionRate = 0D;
        private double onTimeRate = 0D;
    }
}
