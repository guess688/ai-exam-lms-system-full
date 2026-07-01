package cn.org.alan.exam.model.form.course;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("Knowledge point form")
public class KnowledgePointForm {

    @ApiModelProperty("Course ID, inferred from chapter when saving")
    private Integer courseId;

    @NotNull(message = "chapterId is required")
    @ApiModelProperty("Chapter ID")
    private Integer chapterId;

    @NotBlank(message = "name is required")
    @ApiModelProperty("Knowledge point name")
    private String name;

    @ApiModelProperty("Knowledge point description")
    private String description;

    @ApiModelProperty("Sort order")
    private Integer sortOrder;

    @ApiModelProperty("Status: 1 enabled, 0 disabled")
    private Integer status;
}
