package cn.org.alan.exam.model.form.ai;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("AI generated question edit form")
public class AiGeneratedQuestionForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Integer courseId;

    @NotNull
    private Integer chapterId;

    @NotNull
    private Integer knowledgePointId;

    @NotBlank
    private String questionType;

    @NotBlank
    private String difficulty;

    @NotBlank
    private String content;

    private String analysis;

    private List<OptionItem> options;

    @Data
    public static class OptionItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty("A/B/C/D")
        private String label;

        @ApiModelProperty("Option content")
        private String content;

        @ApiModelProperty("True if this option is correct")
        private Boolean isRight;
    }
}
