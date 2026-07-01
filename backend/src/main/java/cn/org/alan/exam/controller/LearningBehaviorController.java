package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.ai.LearningBehaviorEvaluationVO;
import cn.org.alan.exam.service.ILearningBehaviorService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@Api(tags = "Learning behavior evaluation APIs")
@RestController
@RequestMapping("/api/ai/learning-behavior")
public class LearningBehaviorController {

    @Resource
    private ILearningBehaviorService learningBehaviorService;

    @ApiOperation("Query my learning behavior evaluation")
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<LearningBehaviorEvaluationVO> getMyEvaluation() {
        return learningBehaviorService.getMyEvaluation();
    }

    @ApiOperation("Query student learning behavior evaluation list")
    @GetMapping("/students")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<LearningBehaviorEvaluationVO>> pageStudentEvaluations(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "gradeId", required = false) Integer gradeId,
            @RequestParam(value = "realName", required = false) String realName) {
        return learningBehaviorService.pageStudentEvaluations(pageNum, pageSize, gradeId, realName);
    }

    @ApiOperation("Query one student learning behavior evaluation")
    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<LearningBehaviorEvaluationVO> getStudentEvaluation(@PathVariable("studentId") @NotNull Integer studentId) {
        return learningBehaviorService.getStudentEvaluation(studentId);
    }
}
