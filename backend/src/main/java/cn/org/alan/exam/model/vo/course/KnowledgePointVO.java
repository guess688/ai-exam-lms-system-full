package cn.org.alan.exam.model.vo.course;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("Knowledge point view object")
public class KnowledgePointVO {

    private Integer id;
    private Integer courseId;
    private String courseName;
    private Integer chapterId;
    private String chapterTitle;
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
