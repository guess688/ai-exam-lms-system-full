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
@ApiModel("Course material")
@TableName("t_course_material")
public class CourseMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Material ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("Course ID")
    private Integer courseId;

    @ApiModelProperty("Chapter ID")
    private Integer chapterId;

    @ApiModelProperty("Material title")
    private String title;

    @ApiModelProperty("File URL")
    private String fileUrl;

    @ApiModelProperty("File type")
    private String fileType;

    @ApiModelProperty("Sort order")
    private Integer sortOrder;

    @ApiModelProperty("Create time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("Update time")
    private LocalDateTime updateTime;

    @TableLogic
    @ApiModelProperty("Logic delete flag")
    private Integer isDeleted;
}
