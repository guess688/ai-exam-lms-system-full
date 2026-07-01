package cn.org.alan.exam.model.form.ai;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("Practice recommendation publish form")
public class PracticeRecommendationPublishForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Integer examId;

    @NotBlank
    private String targetType;

    private Integer gradeId;

    private Integer studentId;

    private Integer courseId;

    private Integer chapterId;

    private Integer knowledgePointId;

    @NotBlank
    private String title;

    private String description;

    private LocalDateTime deadline;

    @NotEmpty
    private List<Integer> questionIds;
}
