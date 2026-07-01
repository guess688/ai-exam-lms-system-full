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
@TableName("t_ai_question_review")
public class AiQuestionReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("Review ID")
    private Integer id;

    @ApiModelProperty("AI call log ID")
    private Integer aiCallLogId;

    @ApiModelProperty("Question repo ID")
    private Integer repoId;

    @ApiModelProperty("Course ID")
    private Integer courseId;

    @ApiModelProperty("Chapter ID")
    private Integer chapterId;

    @ApiModelProperty("Knowledge point ID")
    private Integer knowledgePointId;

    @ApiModelProperty("Question type code")
    private Integer quType;

    @ApiModelProperty("Question type text")
    private String questionType;

    @ApiModelProperty("Difficulty")
    private String difficulty;

    @ApiModelProperty("Question content")
    private String content;

    @ApiModelProperty("Options JSON for main question module")
    private String optionsJson;

    @ApiModelProperty("Answer JSON")
    private String answerJson;

    @ApiModelProperty("Analysis")
    private String analysis;

    @ApiModelProperty("Raw single question JSON")
    private String rawJson;

    @ApiModelProperty("PENDING/APPROVED/REJECTED")
    private String status;

    @ApiModelProperty("Review comment")
    private String reviewComment;

    @ApiModelProperty("Reviewer ID")
    private Integer reviewerId;

    @ApiModelProperty("Creator ID")
    private Integer createUserId;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("Create time")
    private LocalDateTime createTime;

    @ApiModelProperty("Update time")
    private LocalDateTime updateTime;
}
