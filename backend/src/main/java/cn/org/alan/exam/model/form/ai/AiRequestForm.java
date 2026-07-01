package cn.org.alan.exam.model.form.ai;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@ApiModel("AI request form")
public class AiRequestForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Business input data")
    private Map<String, Object> input;
}
