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
@ApiModel("AI call log")
@TableName("t_ai_call_log")
public class AiCallLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("AI call log ID")
    private Integer id;

    @ApiModelProperty("AI feature type")
    private String featureType;

    @ApiModelProperty("AI provider")
    private String provider;

    @ApiModelProperty("AI model")
    private String model;

    @ApiModelProperty("Mock mode flag")
    private Integer mockEnabled;

    @ApiModelProperty("Call success flag")
    private Integer success;

    @ApiModelProperty("Duration in milliseconds")
    private Long durationMs;

    @ApiModelProperty("Request time")
    private LocalDateTime requestTime;

    @ApiModelProperty("Sanitized request summary")
    private String requestSummary;

    @ApiModelProperty("Prompt template")
    private String promptTemplate;

    @ApiModelProperty("Raw AI response JSON")
    private String rawResponse;

    @ApiModelProperty("Parsed AI result")
    private String parsedResult;

    @ApiModelProperty("Error message")
    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("Create time")
    private LocalDateTime createTime;

    @ApiModelProperty("Update time")
    private LocalDateTime updateTime;
}
