package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.ai.PracticeRecommendationGenerateForm;
import cn.org.alan.exam.model.form.ai.PracticeRecommendationPublishForm;
import cn.org.alan.exam.model.vo.ai.PracticeRecommendationVO;
import cn.org.alan.exam.service.IAiPracticeRecommendationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "AI practice recommendation APIs")
@RestController
@RequestMapping("/api/ai/practice-recommendations")
public class AiPracticeRecommendationController {

    @Resource
    private IAiPracticeRecommendationService aiPracticeRecommendationService;

    @ApiOperation("Generate AI practice recommendation")
    @PostMapping("/generate")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<PracticeRecommendationVO> generateRecommendation(@Validated @RequestBody PracticeRecommendationGenerateForm form) {
        return aiPracticeRecommendationService.generateRecommendation(form);
    }

    @ApiOperation("Publish recommended practice as learning task")
    @PostMapping("/publish")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> publishRecommendation(@Validated @RequestBody PracticeRecommendationPublishForm form) {
        return aiPracticeRecommendationService.publishRecommendation(form);
    }
}
