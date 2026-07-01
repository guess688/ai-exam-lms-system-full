package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.ai.AiRequestForm;
import cn.org.alan.exam.model.vo.ai.AiResponseVO;
import cn.org.alan.exam.service.IAiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

@Api(tags = "AI service APIs")
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Resource
    private IAiService aiService;

    @ApiOperation("Generate student learning report")
    @PostMapping("/student-report")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<AiResponseVO> generateStudentReport(@RequestBody AiRequestForm form) {
        return aiService.generateStudentReport(inputOf(form));
    }

    @ApiOperation("Generate class learning report")
    @PostMapping("/class-report")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<AiResponseVO> generateClassReport(@RequestBody AiRequestForm form) {
        return aiService.generateClassReport(inputOf(form));
    }

    @ApiOperation("Generate teaching plan")
    @PostMapping("/teaching-plan")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<AiResponseVO> generateTeachingPlan(@RequestBody AiRequestForm form) {
        return aiService.generateTeachingPlan(inputOf(form));
    }

    @ApiOperation("Generate paper review")
    @PostMapping("/paper-review")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<AiResponseVO> generatePaperReview(@RequestBody AiRequestForm form) {
        return aiService.generatePaperReview(inputOf(form));
    }

    @ApiOperation("Generate questions")
    @PostMapping("/questions")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<AiResponseVO> generateQuestions(@RequestBody AiRequestForm form) {
        return aiService.generateQuestions(inputOf(form));
    }

    @ApiOperation("Recommend practice")
    @PostMapping("/practice-recommend")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<AiResponseVO> recommendPractice(@RequestBody AiRequestForm form) {
        return aiService.recommendPractice(inputOf(form));
    }

    @ApiOperation("Evaluate learning behavior")
    @PostMapping("/learning-behavior")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<AiResponseVO> evaluateLearningBehavior(@RequestBody AiRequestForm form) {
        return aiService.evaluateLearningBehavior(inputOf(form));
    }

    private Map<String, Object> inputOf(AiRequestForm form) {
        if (form == null || form.getInput() == null) {
            return new LinkedHashMap<>();
        }
        return form.getInput();
    }
}
