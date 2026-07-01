package cn.org.alan.exam.model.vo.course;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("Course chapter view object")
public class CourseChapterVO {

    private Integer id;
    private Integer courseId;
    private String courseName;
    private String title;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @ApiModelProperty("Knowledge point count")
    private Integer knowledgePointCount;
}
