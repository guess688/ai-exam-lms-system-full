package cn.org.alan.exam.model.form.task;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@ApiModel("Learning task form")
public class LearningTaskForm {

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    @NotBlank(message = "taskType is required")
    private String taskType;

    private Integer courseId;

    private Integer chapterId;

    private Integer knowledgePointId;

    @NotBlank(message = "targetType is required")
    private String targetType;

    private Integer targetClassId;

    private Integer targetStudentId;

    private Integer relatedExamId;

    private Integer relatedPaperId;

    private Integer relatedVideoId;

    private LocalDateTime deadline;

    private Integer status;
}
