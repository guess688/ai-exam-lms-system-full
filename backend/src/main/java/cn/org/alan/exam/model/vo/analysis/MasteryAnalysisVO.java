package cn.org.alan.exam.model.vo.analysis;

import lombok.Data;

@Data
public class MasteryAnalysisVO {

    private Integer id;

    private String name;

    private Integer total;

    private Integer correct;

    private Double rate;

    private String level;
}
