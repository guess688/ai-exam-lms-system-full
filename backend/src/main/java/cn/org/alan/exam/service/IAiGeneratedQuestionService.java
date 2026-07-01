package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.ai.AiGeneratedQuestionForm;
import cn.org.alan.exam.model.form.ai.AiQuestionGenerateForm;
import cn.org.alan.exam.model.vo.ai.AiGeneratedQuestionVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface IAiGeneratedQuestionService {

    Result<List<AiGeneratedQuestionVO>> generateQuestions(AiQuestionGenerateForm form);

    Result<IPage<AiGeneratedQuestionVO>> paging(Integer pageNum, Integer pageSize, String status,
                                                Integer courseId, Integer chapterId, Integer knowledgePointId,
                                                String questionType, String difficulty);

    Result<AiGeneratedQuestionVO> updateGeneratedQuestion(Integer id, AiGeneratedQuestionForm form);

    Result<String> approve(Integer id, Integer repoId);

    Result<String> reject(Integer id);
}
