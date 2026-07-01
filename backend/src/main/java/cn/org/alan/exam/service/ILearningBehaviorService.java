package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.ai.LearningBehaviorEvaluationVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface ILearningBehaviorService {

    Result<LearningBehaviorEvaluationVO> getMyEvaluation();

    Result<IPage<LearningBehaviorEvaluationVO>> pageStudentEvaluations(Integer pageNum,
                                                                       Integer pageSize,
                                                                       Integer gradeId,
                                                                       String realName);

    Result<LearningBehaviorEvaluationVO> getStudentEvaluation(Integer studentId);
}
