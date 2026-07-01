package cn.org.alan.exam.model.form.course;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("Video progress report form")
public class VideoProgressForm {

    @NotNull(message = "videoId is required")
    @ApiModelProperty("Video ID")
    private Integer videoId;

    @NotNull(message = "watchedSeconds is required")
    @Min(value = 0, message = "watchedSeconds cannot be negative")
    @ApiModelProperty("Watched seconds")
    private Integer watchedSeconds;

    @Min(value = 0, message = "duration cannot be negative")
    @ApiModelProperty("Actual duration in seconds, optional")
    private Integer duration;
}
