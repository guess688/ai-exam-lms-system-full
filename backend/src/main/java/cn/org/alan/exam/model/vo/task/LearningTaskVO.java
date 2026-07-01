package cn.org.alan.exam.model.vo.task;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("Learning task view object")
public class LearningTaskVO {

    private Integer id;
    private String title;
    private String description;
    private String taskType;
    private String taskTypeName;
    private Integer courseId;
    private String courseName;
    private Integer chapterId;
    private String chapterTitle;
    private Integer knowledgePointId;
    private String knowledgePointName;
    private String targetType;
    private String targetTypeName;
    private Integer targetClassId;
    private String targetClassName;
    private Integer targetStudentId;
    private String targetStudentName;
    private Integer relatedExamId;
    private String relatedExamTitle;
    private Integer relatedPaperId;
    private String relatedPaperTitle;
    private Integer relatedVideoId;
    private String relatedVideoTitle;
    private LocalDateTime deadline;
    private Integer publisherId;
    private String publisherName;
    private Integer status;
    private Integer totalCount;
    private Integer completedCount;
    private BigDecimal completionRate;
    private String recordStatus;
    private BigDecimal progressRate;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
