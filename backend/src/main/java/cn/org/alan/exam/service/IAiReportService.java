package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.ai.AiReportVO;

import java.util.List;

public interface IAiReportService {

    Result<AiReportVO> generateStudentReport(Integer examId, Integer studentId);

    Result<List<AiReportVO>> listStudentReports(Integer examId, Integer studentId);

    Result<AiReportVO> generateClassReport(Integer examId, Integer gradeId);

    Result<List<AiReportVO>> listClassReports(Integer examId, Integer gradeId);

    Result<AiReportVO> regenerateReport(Integer reportId);
}
