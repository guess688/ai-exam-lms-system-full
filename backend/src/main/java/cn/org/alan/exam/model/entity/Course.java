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
@ApiModel("Course")
@TableName("t_course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Course ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("Course name")
    private String name;

    @ApiModelProperty("Course description")
    private String description;

    @ApiModelProperty("Cover URL")
    private String coverUrl;

    @ApiModelProperty("Teacher ID")
    private Integer teacherId;

    @ApiModelProperty("Visible class ID")
    private Integer gradeId;

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
