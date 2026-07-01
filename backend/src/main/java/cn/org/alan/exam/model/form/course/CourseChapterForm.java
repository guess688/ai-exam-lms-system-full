package cn.org.alan.exam.model.form.course;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("Course chapter form")
public class CourseChapterForm {

    @NotNull(message = "courseId is required")
    @ApiModelProperty("Course ID")
    private Integer courseId;

    @NotBlank(message = "title is required")
    @ApiModelProperty("Chapter title")
    private String title;

    @ApiModelProperty("Chapter description")
    private String description;

    @ApiModelProperty("Sort order")
    private Integer sortOrder;

    @ApiModelProperty("Status: 1 enabled, 0 disabled")
    private Integer status;
}
