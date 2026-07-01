package cn.org.alan.exam.model.vo.course;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("Student video progress view object")
public class StudentVideoProgressVO {

    private Integer id;
    private Integer studentId;
    private String studentName;
    private Integer courseId;
    private String courseName;
    private Integer chapterId;
    private String chapterTitle;
    private Integer videoId;
    private String videoTitle;
    private Integer watchedSeconds;
    private Integer duration;
    private BigDecimal progressRate;
    private Integer completed;
    private LocalDateTime lastWatchTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
