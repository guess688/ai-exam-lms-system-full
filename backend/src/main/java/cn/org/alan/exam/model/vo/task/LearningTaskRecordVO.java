package cn.org.alan.exam.model.vo.task;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("Learning task record view object")
public class LearningTaskRecordVO {

    private Integer id;
    private Integer taskId;
    private String taskTitle;
    private Integer studentId;
    private String studentName;
    private String status;
    private String statusName;
    private BigDecimal progressRate;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
