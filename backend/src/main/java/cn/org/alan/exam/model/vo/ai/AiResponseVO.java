package cn.org.alan.exam.model.vo.ai;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("AI response")
public class AiResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("AI call log ID")
    private Integer logId;

    @ApiModelProperty("AI feature type")
    private String featureType;

    @ApiModelProperty("Mock mode flag")
    private Boolean mock;

    @ApiModelProperty("Business success flag")
    private Boolean success;

    @ApiModelProperty("AI generated content")
    private String content;

    @ApiModelProperty("Raw AI response JSON")
    private String rawJson;

    @ApiModelProperty("Parsed AI result")
    private Object parsedResult;

    @ApiModelProperty("Friendly message")
    private String friendlyMessage;
}
