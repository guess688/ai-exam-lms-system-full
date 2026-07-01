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
import java.time.LocalDateTime;

@Data
@ApiModel("Learning task")
@TableName("t_learning_task")
public class LearningTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("Task ID")
    private Integer id;

    private String title;

    private String description;

    private String taskType;

    private Integer courseId;

    private Integer chapterId;

    private Integer knowledgePointId;

    private String targetType;

    private Integer targetClassId;

    private Integer targetStudentId;

    private Integer relatedExamId;

    private Integer relatedPaperId;

    private Integer relatedVideoId;

    private LocalDateTime deadline;

    private Integer publisherId;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
