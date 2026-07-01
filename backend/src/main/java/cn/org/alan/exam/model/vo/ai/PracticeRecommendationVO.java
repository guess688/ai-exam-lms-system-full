package cn.org.alan.exam.model.vo.ai;

import cn.org.alan.exam.model.vo.analysis.DifficultyAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.MasteryAnalysisVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PracticeRecommendationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer examId;

    private String targetType;

    private Integer gradeId;

    private Integer studentId;

    private List<MasteryAnalysisVO> recommendedChapters;

    private List<MasteryAnalysisVO> recommendedKnowledgePoints;

    private List<RecommendedQuestionVO> recommendedQuestions;

    private List<DifficultyAnalysisVO> difficultyPerformance;

    private String recommendedDifficulty;

    private String reason;

    private String suggestedTaskTitle;

    private String suggestedTaskDescription;

    private LocalDateTime suggestedDeadline;

    private Object aiResult;

    @Data
    public static class RecommendedQuestionVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Integer id;

        private Integer quType;

        private String content;

        private Integer courseId;

        private String courseName;

        private Integer chapterId;

        private String chapterTitle;

        private Integer knowledgePointId;

        private String knowledgePointName;

        private String difficulty;
    }
}
