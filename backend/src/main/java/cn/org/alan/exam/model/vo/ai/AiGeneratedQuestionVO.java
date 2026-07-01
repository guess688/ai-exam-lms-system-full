package cn.org.alan.exam.model.vo.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ApiModel("AI generated question view object")
public class AiGeneratedQuestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer teacherId;

    private Integer courseId;

    private Integer chapterId;

    private Integer knowledgePointId;

    private String questionType;

    private String difficulty;

    private String contentJson;

    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
