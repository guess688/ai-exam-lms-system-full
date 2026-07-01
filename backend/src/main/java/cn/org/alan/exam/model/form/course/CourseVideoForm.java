package cn.org.alan.exam.model.form.course;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("Course video form")
public class CourseVideoForm {

    @NotNull(message = "courseId is required")
    @ApiModelProperty("Course ID")
    private Integer courseId;

    @NotNull(message = "chapterId is required")
    @ApiModelProperty("Chapter ID")
    private Integer chapterId;

    @NotBlank(message = "title is required")
    @ApiModelProperty("Video title")
    private String title;

    @ApiModelProperty("Video description")
    private String description;

    @NotBlank(message = "videoUrl is required")
    @ApiModelProperty("Video URL")
    private String videoUrl;

    @Min(value = 0, message = "duration cannot be negative")
    @ApiModelProperty("Duration in seconds")
    private Integer duration;

    @ApiModelProperty("Cover URL")
    private String coverUrl;

    @ApiModelProperty("Sort order")
    private Integer sortOrder;

    @ApiModelProperty("Status: 1 enabled, 0 disabled")
    private Integer status;
}
