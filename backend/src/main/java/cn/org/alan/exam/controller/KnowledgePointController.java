package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.course.KnowledgePointForm;
import cn.org.alan.exam.model.vo.course.KnowledgePointVO;
import cn.org.alan.exam.service.IKnowledgePointService;
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
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "Knowledge point management APIs")
@RestController
@RequestMapping("/api/knowledge-points")
public class KnowledgePointController {

    @Resource
    private IKnowledgePointService knowledgePointService;

    @ApiOperation("Create knowledge point")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addKnowledgePoint(@Validated @RequestBody KnowledgePointForm form) {
        return knowledgePointService.addKnowledgePoint(form);
    }

    @ApiOperation("Update knowledge point")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateKnowledgePoint(@PathVariable("id") @NotNull Integer id,
                                               @Validated @RequestBody KnowledgePointForm form) {
        return knowledgePointService.updateKnowledgePoint(id, form);
    }

    @ApiOperation("Disable knowledge point")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> disableKnowledgePoint(@PathVariable("id") @NotNull Integer id) {
        return knowledgePointService.disableKnowledgePoint(id);
    }

    @ApiOperation("List knowledge points by chapter")
    @GetMapping("/chapter/{chapterId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<KnowledgePointVO>> listByChapter(@PathVariable("chapterId") @NotNull Integer chapterId,
                                                        @RequestParam(value = "status", required = false) Integer status) {
        return knowledgePointService.listByChapter(chapterId, status);
    }
}
