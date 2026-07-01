package cn.org.alan.exam.model.vo.course;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("Chapter learning view object")
public class ChapterLearningVO {

    private Integer id;
    private Integer courseId;
    private String title;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Integer questionCount;
    private Integer examCount;
    private Integer videoCount;
    private Integer completedVideoCount;
    private BigDecimal progressRate;
    private List<KnowledgePointVO> knowledgePoints;
    private List<CourseVideoVO> videos;
    private List<CourseMaterialVO> materials;
}
