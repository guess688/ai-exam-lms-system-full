package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ApiModel("AI report")
@TableName("ai_report")
public class AiReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
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

    @ApiModelProperty("AI input JSON")
    private String inputJson;

    @ApiModelProperty("AI output text")
    private String outputText;

    @ApiModelProperty("AI output JSON")
    private String outputJson;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("Create time")
    private LocalDateTime createTime;
}
