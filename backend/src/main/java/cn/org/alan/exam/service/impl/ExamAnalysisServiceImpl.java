package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ExamAnalysisMapper;
import cn.org.alan.exam.mapper.UserGradeMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.vo.analysis.ChartDataVO;
import cn.org.alan.exam.model.vo.analysis.DifficultyAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.MasteryAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.WrongQuestionAnalysisVO;
import cn.org.alan.exam.service.IExamAnalysisService;
import cn.org.alan.exam.utils.SecurityUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

@Service
public class ExamAnalysisServiceImpl implements IExamAnalysisService {

    @Resource
    private ExamAnalysisMapper examAnalysisMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserGradeMapper userGradeMapper;

    @Override
    public Result<List<MasteryAnalysisVO>> getStudentChapterAnalysis(Integer examId, Integer userId) {
        Integer targetUserId = resolveStudentUserId(userId);
        List<MasteryAnalysisVO> result = examAnalysisMapper.selectStudentChapterAnalysis(examId, targetUserId);
        fillMasteryLevels(result);
        return Result.success("Query student chapter analysis success", result);
    }

    @Override
    public Result<List<MasteryAnalysisVO>> getStudentKnowledgeAnalysis(Integer examId, Integer userId) {
        Integer targetUserId = resolveStudentUserId(userId);
        List<MasteryAnalysisVO> result = examAnalysisMapper.selectStudentKnowledgeAnalysis(examId, targetUserId);
        fillMasteryLevels(result);
        return Result.success("Query student knowledge analysis success", result);
    }

    @Override
    public Result<List<MasteryAnalysisVO>> getClassChapterAnalysis(Integer examId, Integer gradeId) {
        Integer targetGradeId = resolveTeacherGradeId(gradeId);
        List<MasteryAnalysisVO> result = examAnalysisMapper.selectClassChapterAnalysis(examId, targetGradeId);
        fillMasteryLevels(result);
        return Result.success("Query class chapter analysis success", result);
    }

    @Override
    public Result<List<MasteryAnalysisVO>> getClassKnowledgeAnalysis(Integer examId, Integer gradeId) {
        Integer targetGradeId = resolveTeacherGradeId(gradeId);
        List<MasteryAnalysisVO> result = examAnalysisMapper.selectClassKnowledgeAnalysis(examId, targetGradeId);
        fillMasteryLevels(result);
        return Result.success("Query class knowledge analysis success", result);
    }

    @Override
    public Result<List<MasteryAnalysisVO>> getWeakChapterRanking(Integer examId, Integer gradeId) {
        Integer targetGradeId = resolveTeacherGradeId(gradeId);
        List<MasteryAnalysisVO> result = examAnalysisMapper.selectClassChapterAnalysis(examId, targetGradeId);
        fillMasteryLevels(result);
        result.sort(Comparator.comparing(item -> item.getRate() == null ? 0D : item.getRate()));
        return Result.success("Query weak chapter ranking success", result);
    }

    @Override
    public Result<List<MasteryAnalysisVO>> getWeakKnowledgeRanking(Integer examId, Integer gradeId) {
        Integer targetGradeId = resolveTeacherGradeId(gradeId);
        List<MasteryAnalysisVO> result = examAnalysisMapper.selectClassKnowledgeAnalysis(examId, targetGradeId);
        fillMasteryLevels(result);
        result.sort(Comparator.comparing(item -> item.getRate() == null ? 0D : item.getRate()));
        return Result.success("Query weak knowledge ranking success", result);
    }

    @Override
    public Result<List<WrongQuestionAnalysisVO>> getWrongQuestionRanking(Integer examId, Integer gradeId) {
        Integer targetGradeId = resolveTeacherGradeId(gradeId);
        return Result.success("Query wrong question ranking success", examAnalysisMapper.selectWrongQuestions(examId, targetGradeId));
    }

    @Override
    public Result<List<DifficultyAnalysisVO>> getDifficultyAnalysis(Integer examId, Integer gradeId, Integer userId) {
        Integer targetUserId = null;
        Integer targetGradeId = null;
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode == 1 || userId != null) {
            targetUserId = resolveStudentUserId(userId);
        } else {
            targetGradeId = resolveTeacherGradeId(gradeId);
        }
        List<DifficultyAnalysisVO> result = examAnalysisMapper.selectDifficultyAnalysis(examId, targetGradeId, targetUserId);
        for (DifficultyAnalysisVO item : result) {
            item.setLevel(masteryLevel(item.getRate()));
        }
        return Result.success("Query difficulty analysis success", result);
    }

    @Override
    public Result<List<ChartDataVO>> getClassScoreChart(Integer examId, Integer gradeId) {
        Integer targetGradeId = resolveTeacherGradeId(gradeId);
        return Result.success("Query class score chart success", examAnalysisMapper.selectClassScoreChart(examId, targetGradeId));
    }

    @Override
    public Result<List<ChartDataVO>> getScoreSegmentChart(Integer examId, Integer gradeId) {
        Integer targetGradeId = resolveTeacherGradeId(gradeId);
        return Result.success("Query score segment chart success", examAnalysisMapper.selectScoreSegmentChart(examId, targetGradeId));
    }

    @Override
    public Result<List<ChartDataVO>> getStudentScoreTrend(Integer userId) {
        Integer targetUserId = resolveStudentUserId(userId);
        return Result.success("Query student score trend success", examAnalysisMapper.selectStudentScoreTrend(targetUserId));
    }

    private Integer resolveStudentUserId(Integer userId) {
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer currentUserId = SecurityUtil.getUserId();
        if (roleCode == 1) {
            return currentUserId;
        }
        if (userId == null) {
            throw new ServiceRuntimeException("Student user id is required");
        }
        if (roleCode == 2) {
            User user = userMapper.selectById(userId);
            if (user == null || !userGradeMapper.getGradeIdListByUserId(currentUserId).contains(user.getGradeId())) {
                throw new ServiceRuntimeException("No permission to query this student analysis");
            }
        }
        return userId;
    }

    private Integer resolveTeacherGradeId(Integer gradeId) {
        if (gradeId == null) {
            throw new ServiceRuntimeException("Grade id is required");
        }
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode == 1) {
            Integer currentGradeId = SecurityUtil.getGradeId();
            if (!gradeId.equals(currentGradeId)) {
                throw new ServiceRuntimeException("No permission to query this class analysis");
            }
            return currentGradeId;
        }
        if (roleCode == 2 && !userGradeMapper.getGradeIdListByUserId(SecurityUtil.getUserId()).contains(gradeId)) {
            throw new ServiceRuntimeException("No permission to query this class analysis");
        }
        return gradeId;
    }

    private void fillMasteryLevels(List<MasteryAnalysisVO> result) {
        for (MasteryAnalysisVO item : result) {
            item.setLevel(masteryLevel(item.getRate()));
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
}
