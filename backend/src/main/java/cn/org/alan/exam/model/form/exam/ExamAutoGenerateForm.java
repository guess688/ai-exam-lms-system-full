package cn.org.alan.exam.model.form.exam;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamAutoGenerateForm {

    @NotBlank(message = "Exam title is required")
    @Size(min = 3, max = 20, message = "Exam title length must be 3-20")
    private String title;

    @NotNull(message = "Exam duration is required")
    @Min(value = 0, message = "Exam duration must be greater than 0")
    private Integer examDuration;

    private Integer maxCount;

    @NotNull(message = "Passed score is required")
    @Min(value = 0, message = "Passed score must be greater than or equal to 0")
    private Integer passedScore;

    private Integer totalScore;

    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime endTime;

    @NotBlank(message = "Class ids are required")
    @Pattern(regexp = "^\\d+(,\\d+)*$|^\\d+$", message = "Class ids format must be 1,2,3")
    private String gradeIds;

    private Integer repoId;

    private Integer certificateId;

    @NotNull(message = "Course is required")
    private Integer courseId;

    private List<Integer> chapterIds;

    private List<Integer> knowledgePointIds;

    @NotNull(message = "Single choice count is required")
    @Min(value = 0)
    private Integer radioCount;

    @NotNull(message = "Single choice score is required")
    @Min(value = 0)
    private Integer radioScore;

    @NotNull(message = "Multiple choice count is required")
    @Min(value = 0)
    private Integer multiCount;

    @NotNull(message = "Multiple choice score is required")
    @Min(value = 0)
    private Integer multiScore;

    @NotNull(message = "True or false count is required")
    @Min(value = 0)
    private Integer judgeCount;

    @NotNull(message = "True or false score is required")
    @Min(value = 0)
    private Integer judgeScore;

    @Min(value = 0)
    private Integer indefiniteCount;

    @Min(value = 0)
    private Integer indefiniteScore;

    @Min(value = 0)
    private Integer saqCount;

    @Min(value = 0)
    private Integer saqScore;

    @Min(value = 0)
    private Integer easyCount;

    @Min(value = 0)
    private Integer mediumCount;

    @Min(value = 0)
    private Integer hardCount;

    @Min(value = 0)
    private Integer easyRatio;

    @Min(value = 0)
    private Integer mediumRatio;

    @Min(value = 0)
    private Integer hardRatio;
}
