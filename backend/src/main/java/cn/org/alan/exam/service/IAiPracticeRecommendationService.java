package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.ai.PracticeRecommendationGenerateForm;
import cn.org.alan.exam.model.form.ai.PracticeRecommendationPublishForm;
import cn.org.alan.exam.model.vo.ai.PracticeRecommendationVO;

public interface IAiPracticeRecommendationService {

    Result<PracticeRecommendationVO> generateRecommendation(PracticeRecommendationGenerateForm form);

    Result<String> publishRecommendation(PracticeRecommendationPublishForm form);
}
