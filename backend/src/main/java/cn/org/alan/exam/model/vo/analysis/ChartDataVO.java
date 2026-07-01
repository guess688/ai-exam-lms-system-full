package cn.org.alan.exam.model.vo.analysis;

import lombok.Data;

@Data
public class ChartDataVO {

    private String name;

    private Integer value;

    private Double rate;

    private Integer total;

    private Integer correct;
}
