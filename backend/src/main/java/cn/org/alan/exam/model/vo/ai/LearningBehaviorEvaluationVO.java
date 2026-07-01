package cn.org.alan.exam.model.vo.ai;

import cn.org.alan.exam.model.vo.analysis.MasteryAnalysisVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LearningBehaviorEvaluationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer studentId;

    private String studentName;

    private Integer gradeId;

    private String gradeName;

    private Integer recentExamCount;

    private List<ScoreTrendPointVO> scoreTrend;

    private String scoreTrendStatus;

    private Double wrongRepeatRate;

    private Integer repeatedWrongQuestionCount;

    private MasteryChangeVO chapterMasteryChange;

    private MasteryChangeVO knowledgeMasteryChange;

    private Double videoCompletionRate;

    private LocalDateTime lastVideoStudyTime;

    private Double taskCompletionRate;

    private Double onTimeCompletionRate;

    private Boolean needAttention;

    private String attentionLevel;

    private List<BehaviorSignalVO> ruleSignals;

    private List<String> suggestions;

    private String aiSummary;

    private Object aiResult;

    private LocalDateTime generatedAt;

    private List<MasteryAnalysisVO> latestChapterMastery;

    private List<MasteryAnalysisVO> latestKnowledgeMastery;

    @Data
    public static class ScoreTrendPointVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Integer examId;

        private String examName;

        private Integer score;

        private Integer grossScore;

        private Double rate;

        private LocalDateTime submittedAt;
    }

    @Data
    public static class MasteryChangeVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private String dimension;

        private Double latestAverageRate;

        private Double previousAverageRate;

        private Double changeRate;

        private String status;
    }

    @Data
    public static class BehaviorSignalVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private String type;

        private String level;

        private String message;

        private String suggestion;
    }
}
