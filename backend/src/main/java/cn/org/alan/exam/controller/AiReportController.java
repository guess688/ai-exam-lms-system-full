package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.ai.AiReportVO;
import cn.org.alan.exam.service.IAiReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "AI report APIs")
@RestController
@RequestMapping("/api/ai/reports")
public class AiReportController {

    @Resource
    private IAiReportService aiReportService;

    @ApiOperation("Generate student AI learning report")
    @PostMapping("/student")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<AiReportVO> generateStudentReport(@RequestParam("examId") Integer examId,
                                                    @RequestParam(value = "studentId", required = false) Integer studentId) {
        return aiReportService.generateStudentReport(examId, studentId);
    }

    @ApiOperation("Query student AI learning report history")
    @GetMapping("/student/history")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<AiReportVO>> listStudentReports(@RequestParam(value = "examId", required = false) Integer examId,
                                                       @RequestParam(value = "studentId", required = false) Integer studentId) {
        return aiReportService.listStudentReports(examId, studentId);
    }

    @ApiOperation("Generate class AI learning report")
    @PostMapping("/class")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<AiReportVO> generateClassReport(@RequestParam("examId") Integer examId,
                                                  @RequestParam("gradeId") Integer gradeId) {
        return aiReportService.generateClassReport(examId, gradeId);
    }

    @ApiOperation("Query class AI learning report history")
    @GetMapping("/class/history")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<AiReportVO>> listClassReports(@RequestParam(value = "examId", required = false) Integer examId,
                                                     @RequestParam("gradeId") Integer gradeId) {
        return aiReportService.listClassReports(examId, gradeId);
    }

    @ApiOperation("Regenerate AI learning report")
    @PostMapping("/{id}/regenerate")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<AiReportVO> regenerateReport(@PathVariable("id") Integer reportId) {
        return aiReportService.regenerateReport(reportId);
    }
}
