package cn.org.alan.exam.model.form.ai;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("Practice recommendation generation form")
public class PracticeRecommendationGenerateForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Integer examId;

    @NotBlank
    private String targetType;

    private Integer gradeId;

    private Integer studentId;

    private Integer questionCount;
}
