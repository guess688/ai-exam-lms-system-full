package cn.org.alan.exam.model.form.course;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("Course material form")
public class CourseMaterialForm {

    @NotNull(message = "courseId is required")
    @ApiModelProperty("Course ID")
    private Integer courseId;

    @NotNull(message = "chapterId is required")
    @ApiModelProperty("Chapter ID")
    private Integer chapterId;

    @NotBlank(message = "title is required")
    @ApiModelProperty("Material title")
    private String title;

    @NotBlank(message = "fileUrl is required")
    @ApiModelProperty("File URL")
    private String fileUrl;

    @ApiModelProperty("File type")
    private String fileType;

    @ApiModelProperty("Sort order")
    private Integer sortOrder;
}
