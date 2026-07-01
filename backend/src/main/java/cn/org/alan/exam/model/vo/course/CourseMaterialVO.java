package cn.org.alan.exam.model.vo.course;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("Course material view object")
public class CourseMaterialVO {

    private Integer id;
    private Integer courseId;
    private String courseName;
    private Integer chapterId;
    private String chapterTitle;
    private String title;
    private String fileUrl;
    private String fileType;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
