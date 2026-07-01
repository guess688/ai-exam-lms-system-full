package cn.org.alan.exam.model.vo.analysis;

import lombok.Data;

@Data
public class DifficultyAnalysisVO {

    private String difficulty;

    private String name;

    private Integer total;

    private Integer correct;

    private Double rate;

    private String level;
}
