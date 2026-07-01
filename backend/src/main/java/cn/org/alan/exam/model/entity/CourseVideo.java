package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ApiModel("Course video")
@TableName("t_course_video")
public class CourseVideo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Video ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("Course ID")
    private Integer courseId;

    @ApiModelProperty("Chapter ID")
    private Integer chapterId;

    @ApiModelProperty("Video title")
    private String title;

    @ApiModelProperty("Video description")
    private String description;

    @ApiModelProperty("Video URL")
    private String videoUrl;

    @ApiModelProperty("Duration in seconds")
    private Integer duration;

    @ApiModelProperty("Cover URL")
    private String coverUrl;

    @ApiModelProperty("Sort order")
    private Integer sortOrder;

    @ApiModelProperty("Status: 1 enabled, 0 disabled")
    private Integer status;

    @ApiModelProperty("Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("Update time")
    private LocalDateTime updateTime;

    @TableLogic
    @ApiModelProperty("Logic delete flag")
    private Integer isDeleted;
}
