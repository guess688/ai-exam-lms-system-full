package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.ai.AiGeneratedQuestionForm;
import cn.org.alan.exam.model.form.ai.AiQuestionGenerateForm;
import cn.org.alan.exam.model.vo.ai.AiGeneratedQuestionVO;
import cn.org.alan.exam.service.IAiGeneratedQuestionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "AI generated question review APIs")
@RestController
@RequestMapping("/api/ai/generated-questions")
public class AiGeneratedQuestionController {

    @Resource
    private IAiGeneratedQuestionService aiGeneratedQuestionService;

    @ApiOperation("Generate AI questions into review queue")
    @PostMapping("/generate")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<AiGeneratedQuestionVO>> generateQuestions(@Validated @RequestBody AiQuestionGenerateForm form) {
        return aiGeneratedQuestionService.generateQuestions(form);
    }

    @ApiOperation("Page AI generated question review queue")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<AiGeneratedQuestionVO>> paging(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                       @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                       @RequestParam(value = "status", required = false) String status,
                                                       @RequestParam(value = "courseId", required = false) Integer courseId,
                                                       @RequestParam(value = "chapterId", required = false) Integer chapterId,
                                                       @RequestParam(value = "knowledgePointId", required = false) Integer knowledgePointId,
                                                       @RequestParam(value = "questionType", required = false) String questionType,
                                                       @RequestParam(value = "difficulty", required = false) String difficulty) {
        return aiGeneratedQuestionService.paging(pageNum, pageSize, status, courseId, chapterId, knowledgePointId, questionType, difficulty);
    }

    @ApiOperation("Edit AI generated question")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<AiGeneratedQuestionVO> updateGeneratedQuestion(@PathVariable("id") Integer id,
                                                                @Validated @RequestBody AiGeneratedQuestionForm form) {
        return aiGeneratedQuestionService.updateGeneratedQuestion(id, form);
    }

    @ApiOperation("Approve AI generated question into official question bank")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> approve(@PathVariable("id") Integer id, @RequestParam("repoId") Integer repoId) {
        return aiGeneratedQuestionService.approve(id, repoId);
    }

    @ApiOperation("Reject AI generated question")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> reject(@PathVariable("id") Integer id) {
        return aiGeneratedQuestionService.reject(id);
    }
}
