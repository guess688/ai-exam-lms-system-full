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
@ApiModel("Course chapter")
@TableName("t_course_chapter")
public class CourseChapter implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Chapter ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("Course ID")
    private Integer courseId;

    @ApiModelProperty("Chapter title")
    private String title;

    @ApiModelProperty("Chapter description")
    private String description;

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
