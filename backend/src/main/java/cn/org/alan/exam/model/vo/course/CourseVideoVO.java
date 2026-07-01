package cn.org.alan.exam.model.vo.course;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("Course video view object")
public class CourseVideoVO {

    private Integer id;
    private Integer courseId;
    private String courseName;
    private Integer chapterId;
    private String chapterTitle;
    private String title;
    private String description;
    private String videoUrl;
    private Integer duration;
    private String coverUrl;
    private Integer sortOrder;
    private Integer status;
    private Integer watchedSeconds;
    private BigDecimal progressRate;
    private Integer completed;
    private LocalDateTime lastWatchTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
