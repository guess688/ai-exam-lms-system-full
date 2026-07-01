package cn.org.alan.exam.model.form.course;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("Course form")
public class CourseForm {

    @NotBlank(message = "name is required")
    @ApiModelProperty("Course name")
    private String name;

    @ApiModelProperty("Course description")
    private String description;

    @ApiModelProperty("Cover URL")
    private String coverUrl;

    @ApiModelProperty("Teacher ID, only admin can assign")
    private Integer teacherId;

    @NotNull(message = "gradeId is required")
    @ApiModelProperty("Visible class ID")
    private Integer gradeId;

    @ApiModelProperty("Status: 1 enabled, 0 disabled")
    private Integer status;
}
