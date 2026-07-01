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
@ApiModel("AI generated question review item")
@TableName("ai_generated_question")
public class AiGeneratedQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("Generated question ID")
    private Integer id;

    @ApiModelProperty("Teacher user ID")
    private Integer teacherId;

    @ApiModelProperty("Course ID")
    private Integer courseId;

    @ApiModelProperty("Chapter ID")
    private Integer chapterId;

    @ApiModelProperty("Knowledge point ID")
    private Integer knowledgePointId;

    @ApiModelProperty("SINGLE/MULTIPLE/INDEFINITE/JUDGE")
    private String questionType;

    @ApiModelProperty("EASY/MEDIUM/HARD")
    private String difficulty;

    @ApiModelProperty("Generated and editable question JSON")
    private String contentJson;

    @ApiModelProperty("PENDING/APPROVED/REJECTED")
    private String status;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("Create time")
    private LocalDateTime createTime;

    @ApiModelProperty("Update time")
    private LocalDateTime updateTime;
}
