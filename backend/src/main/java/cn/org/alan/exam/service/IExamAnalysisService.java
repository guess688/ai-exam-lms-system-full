package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.analysis.ChartDataVO;
import cn.org.alan.exam.model.vo.analysis.DifficultyAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.MasteryAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.WrongQuestionAnalysisVO;

import java.util.List;

public interface IExamAnalysisService {

    Result<List<MasteryAnalysisVO>> getStudentChapterAnalysis(Integer examId, Integer userId);

    Result<List<MasteryAnalysisVO>> getStudentKnowledgeAnalysis(Integer examId, Integer userId);

    Result<List<MasteryAnalysisVO>> getClassChapterAnalysis(Integer examId, Integer gradeId);

    Result<List<MasteryAnalysisVO>> getClassKnowledgeAnalysis(Integer examId, Integer gradeId);

    Result<List<MasteryAnalysisVO>> getWeakChapterRanking(Integer examId, Integer gradeId);

    Result<List<MasteryAnalysisVO>> getWeakKnowledgeRanking(Integer examId, Integer gradeId);

    Result<List<WrongQuestionAnalysisVO>> getWrongQuestionRanking(Integer examId, Integer gradeId);

    Result<List<DifficultyAnalysisVO>> getDifficultyAnalysis(Integer examId, Integer gradeId, Integer userId);

    Result<List<ChartDataVO>> getClassScoreChart(Integer examId, Integer gradeId);

    Result<List<ChartDataVO>> getScoreSegmentChart(Integer examId, Integer gradeId);

    Result<List<ChartDataVO>> getStudentScoreTrend(Integer userId);
}
