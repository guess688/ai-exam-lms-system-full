package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.ai.AiResponseVO;

import java.util.Map;

public interface IAiService {

    Result<AiResponseVO> generateStudentReport(Map<String, Object> input);

    Result<AiResponseVO> generateClassReport(Map<String, Object> input);

    Result<AiResponseVO> generateTeachingPlan(Map<String, Object> input);

    Result<AiResponseVO> generatePaperReview(Map<String, Object> input);

    Result<AiResponseVO> generateQuestions(Map<String, Object> input);

    Result<AiResponseVO> recommendPractice(Map<String, Object> input);

    Result<AiResponseVO> evaluateLearningBehavior(Map<String, Object> input);
}
