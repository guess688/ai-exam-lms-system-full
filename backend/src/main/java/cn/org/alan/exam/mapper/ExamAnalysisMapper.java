package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.vo.analysis.DifficultyAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.MasteryAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.WrongQuestionAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.ChartDataVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExamAnalysisMapper {

    List<MasteryAnalysisVO> selectStudentChapterAnalysis(@Param("examId") Integer examId,
                                                         @Param("userId") Integer userId);

    List<MasteryAnalysisVO> selectStudentKnowledgeAnalysis(@Param("examId") Integer examId,
                                                           @Param("userId") Integer userId);

    List<MasteryAnalysisVO> selectClassChapterAnalysis(@Param("examId") Integer examId,
                                                       @Param("gradeId") Integer gradeId);

    List<MasteryAnalysisVO> selectClassKnowledgeAnalysis(@Param("examId") Integer examId,
                                                         @Param("gradeId") Integer gradeId);

    List<WrongQuestionAnalysisVO> selectWrongQuestions(@Param("examId") Integer examId,
                                                       @Param("gradeId") Integer gradeId);

    List<DifficultyAnalysisVO> selectDifficultyAnalysis(@Param("examId") Integer examId,
                                                        @Param("gradeId") Integer gradeId,
                                                        @Param("userId") Integer userId);

    List<ChartDataVO> selectClassScoreChart(@Param("examId") Integer examId,
                                            @Param("gradeId") Integer gradeId);

    List<ChartDataVO> selectScoreSegmentChart(@Param("examId") Integer examId,
                                              @Param("gradeId") Integer gradeId);

    List<ChartDataVO> selectStudentScoreTrend(@Param("userId") Integer userId);
}
