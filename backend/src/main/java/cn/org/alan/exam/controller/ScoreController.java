package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.analysis.ChartDataVO;
import cn.org.alan.exam.model.vo.analysis.DifficultyAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.MasteryAnalysisVO;
import cn.org.alan.exam.model.vo.analysis.WrongQuestionAnalysisVO;
import cn.org.alan.exam.model.vo.score.GradeScoreVO;
import cn.org.alan.exam.model.vo.score.QuestionAnalyseVO;
import cn.org.alan.exam.model.vo.score.UserScoreVO;
import cn.org.alan.exam.service.IExamAnalysisService;
import cn.org.alan.exam.service.IExamQuAnswerService;
import cn.org.alan.exam.service.IUserExamsScoreService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成绩管理
 *
 * @Author WeiJin
 * @Version
 * @Date 2024/3/25 11:19 AM
 */
@Api(tags = "成绩相关接口")
@RestController
@RequestMapping("/api/score")
public class ScoreController {

    @Resource
    private IUserExamsScoreService iUserExamsScoreService;
    @Resource
    private IExamQuAnswerService iExamQuAnswerService;
    @Resource
    private IExamAnalysisService examAnalysisService;

    /**
     * 分页获取成绩信息
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param gradeId  班级Id
     * @param examId   考试Id
     * @param realName 真实姓名
     * @return 响应结果
     */
    @ApiOperation("分页获取成绩信息")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<UserScoreVO>> pagingScore(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                  @RequestParam(value = "gradeId") Integer gradeId,
                                                  @RequestParam(value = "examId") Integer examId,
                                                  @RequestParam(value = "realName", required = false) String realName) {
        return iUserExamsScoreService.pagingScore(pageNum, pageSize, gradeId, examId, realName);
    }

    /**
     * 获取某场考试某题作答情况
     *
     * @param examId     考试id
     * @param questionId 试题id
     * @return 响应结果
     */
    @ApiOperation("获取某场考试某题作答情况")
    @GetMapping("/question/{examId}/{questionId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<QuestionAnalyseVO> questionAnalyse(@PathVariable("examId") Integer examId,
                                                     @PathVariable("questionId") Integer questionId) {
        return iExamQuAnswerService.questionAnalyse(examId, questionId);
    }

    /**
     * 根据班级分析考试情况
     *
     * @param pageNum   页码
     * @param pageSize  每页记录数
     * @param examTitle 考试名称
     * @return 响应结果
     */
    @ApiOperation("根据班级分析考试情况")
    @GetMapping("/getExamScore")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<GradeScoreVO>> getExamScoreInfo(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "examTitle", required = false) String examTitle,
            @RequestParam(value = "gradeId", required = false) Integer gradeId) {
        return iUserExamsScoreService.getExamScoreInfo(pageNum, pageSize, examTitle, gradeId);
    }

    /**
     * 成绩导出
     *
     * @param response 响应对象
     * @param examId   考试id
     * @param gradeId  班级id
     */
    @ApiOperation("成绩导出")
    @GetMapping("/export/{examId}/{gradeId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public void scoreExport(HttpServletResponse response, @PathVariable("examId") Integer examId, @PathVariable("gradeId") Integer gradeId) {
        iUserExamsScoreService.exportScores(response, examId, gradeId);
    }

    @ApiOperation("查询某学生某次考试章节掌握情况")
    @GetMapping("/analysis/student/chapter")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<MasteryAnalysisVO>> studentChapterAnalysis(@RequestParam("examId") Integer examId,
                                                                  @RequestParam(value = "userId", required = false) Integer userId) {
        return examAnalysisService.getStudentChapterAnalysis(examId, userId);
    }

    @ApiOperation("查询某学生某次考试知识点掌握情况")
    @GetMapping("/analysis/student/knowledge")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<MasteryAnalysisVO>> studentKnowledgeAnalysis(@RequestParam("examId") Integer examId,
                                                                    @RequestParam(value = "userId", required = false) Integer userId) {
        return examAnalysisService.getStudentKnowledgeAnalysis(examId, userId);
    }

    @ApiOperation("查询某班级某次考试章节掌握情况")
    @GetMapping("/analysis/class/chapter")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<MasteryAnalysisVO>> classChapterAnalysis(@RequestParam("examId") Integer examId,
                                                               @RequestParam("gradeId") Integer gradeId) {
        return examAnalysisService.getClassChapterAnalysis(examId, gradeId);
    }

    @ApiOperation("查询某班级某次考试知识点掌握情况")
    @GetMapping("/analysis/class/knowledge")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<MasteryAnalysisVO>> classKnowledgeAnalysis(@RequestParam("examId") Integer examId,
                                                                 @RequestParam("gradeId") Integer gradeId) {
        return examAnalysisService.getClassKnowledgeAnalysis(examId, gradeId);
    }

    @ApiOperation("查询某次考试薄弱章节排名")
    @GetMapping("/analysis/weak-chapters")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<MasteryAnalysisVO>> weakChapterRanking(@RequestParam("examId") Integer examId,
                                                             @RequestParam("gradeId") Integer gradeId) {
        return examAnalysisService.getWeakChapterRanking(examId, gradeId);
    }

    @ApiOperation("查询某次考试薄弱知识点排名")
    @GetMapping("/analysis/weak-knowledge")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<MasteryAnalysisVO>> weakKnowledgeRanking(@RequestParam("examId") Integer examId,
                                                               @RequestParam("gradeId") Integer gradeId) {
        return examAnalysisService.getWeakKnowledgeRanking(examId, gradeId);
    }

    @ApiOperation("查询某次考试高频错题")
    @GetMapping("/analysis/wrong-questions")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<WrongQuestionAnalysisVO>> wrongQuestionRanking(@RequestParam("examId") Integer examId,
                                                                     @RequestParam("gradeId") Integer gradeId) {
        return examAnalysisService.getWrongQuestionRanking(examId, gradeId);
    }

    @ApiOperation("查询不同难度题目的正确率")
    @GetMapping("/analysis/difficulty")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<DifficultyAnalysisVO>> difficultyAnalysis(@RequestParam("examId") Integer examId,
                                                                 @RequestParam(value = "gradeId", required = false) Integer gradeId,
                                                                 @RequestParam(value = "userId", required = false) Integer userId) {
        return examAnalysisService.getDifficultyAnalysis(examId, gradeId, userId);
    }

    @ApiOperation("查询班级学生成绩图表数据")
    @GetMapping("/analysis/class-scores")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<ChartDataVO>> classScoreChart(@RequestParam("examId") Integer examId,
                                                     @RequestParam("gradeId") Integer gradeId) {
        return examAnalysisService.getClassScoreChart(examId, gradeId);
    }

    @ApiOperation("查询分数段图表数据")
    @GetMapping("/analysis/score-segments")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<ChartDataVO>> scoreSegmentChart(@RequestParam("examId") Integer examId,
                                                       @RequestParam("gradeId") Integer gradeId) {
        return examAnalysisService.getScoreSegmentChart(examId, gradeId);
    }

    @ApiOperation("查询学生历次成绩趋势")
    @GetMapping("/analysis/student-score-trend")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<ChartDataVO>> studentScoreTrend(@RequestParam(value = "userId", required = false) Integer userId) {
        return examAnalysisService.getStudentScoreTrend(userId);
    }

}
