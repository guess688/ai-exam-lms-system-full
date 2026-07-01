package cn.org.alan.exam.model.vo.course;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("Course view object")
public class CourseVO {

    private Integer id;
    private String name;
    private String description;
    private String coverUrl;
    private Integer teacherId;
    private String teacherName;
    private Integer gradeId;
    private String gradeName;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @ApiModelProperty("Chapter count")
    private Integer chapterCount;

    @ApiModelProperty("Knowledge point count")
    private Integer knowledgePointCount;
}
