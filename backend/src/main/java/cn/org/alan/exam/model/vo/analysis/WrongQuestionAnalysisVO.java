package cn.org.alan.exam.model.vo.analysis;

import lombok.Data;

@Data
public class WrongQuestionAnalysisVO {

    private Integer questionId;

    private String title;

    private String chapterName;

    private String knowledgePointName;

    private String difficulty;

    private Integer total;

    private Integer wrong;

    private Double rate;
}
