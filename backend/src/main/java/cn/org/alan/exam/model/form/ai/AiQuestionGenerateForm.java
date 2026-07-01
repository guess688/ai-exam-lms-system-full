package cn.org.alan.exam.model.form.ai;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("AI question generation form")
public class AiQuestionGenerateForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @ApiModelProperty("Course ID")
    private Integer courseId;

    @NotNull
    @ApiModelProperty("Chapter ID")
    private Integer chapterId;

    @NotNull
    @ApiModelProperty("Knowledge point ID")
    private Integer knowledgePointId;

    @NotBlank
    @ApiModelProperty("SINGLE/MULTIPLE/INDEFINITE/JUDGE or 1/2/4/3")
    private String questionType;

    @NotBlank
    @ApiModelProperty("EASY/MEDIUM/HARD")
    private String difficulty;

    @NotNull
    @Min(1)
    @Max(20)
    @ApiModelProperty("Question count")
    private Integer count;

    @Min(2)
    @Max(8)
    @ApiModelProperty("Option count")
    private Integer optionCount;

    @ApiModelProperty("Extra requirement")
    private String extraRequirement;
}
