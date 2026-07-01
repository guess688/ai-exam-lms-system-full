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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("Student video progress")
@TableName("t_student_video_progress")
public class StudentVideoProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Progress ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("Student user ID")
    private Integer studentId;

    @ApiModelProperty("Course ID")
    private Integer courseId;

    @ApiModelProperty("Chapter ID")
    private Integer chapterId;

    @ApiModelProperty("Video ID")
    private Integer videoId;

    @ApiModelProperty("Watched seconds")
    private Integer watchedSeconds;

    @ApiModelProperty("Duration in seconds")
    private Integer duration;

    @ApiModelProperty("Progress percentage")
    private BigDecimal progressRate;

    @ApiModelProperty("1 completed, 0 not completed")
    private Integer completed;

    @ApiModelProperty("Last watch time")
    private LocalDateTime lastWatchTime;

    @ApiModelProperty("Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("Update time")
    private LocalDateTime updateTime;
}
