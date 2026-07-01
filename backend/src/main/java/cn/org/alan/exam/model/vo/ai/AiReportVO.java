package cn.org.alan.exam.model.vo.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ApiModel("AI report view object")
public class AiReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("AI report ID")
    private Integer id;

    @ApiModelProperty("Report type")
    private String reportType;

    @ApiModelProperty("Student user ID")
    private Integer studentId;

    @ApiModelProperty("Class/grade ID")
    private Integer classId;

    @ApiModelProperty("Exam ID")
    private Integer examId;

    @ApiModelProperty("Report title")
    private String title;

    @ApiModelProperty("AI output text")
    private String outputText;

    @ApiModelProperty("AI output JSON")
    private String outputJson;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("Create time")
    private LocalDateTime createTime;
}
