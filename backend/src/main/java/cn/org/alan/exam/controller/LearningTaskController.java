package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.task.LearningTaskForm;
import cn.org.alan.exam.model.vo.task.LearningTaskRecordVO;
import cn.org.alan.exam.model.vo.task.LearningTaskVO;
import cn.org.alan.exam.service.ILearningTaskService;
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
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "Learning task APIs")
@RestController
@RequestMapping("/api/learning-tasks")
public class LearningTaskController {

    @Resource
    private ILearningTaskService learningTaskService;

    @ApiOperation("Create learning task")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> createTask(@Validated @RequestBody LearningTaskForm form) {
        return learningTaskService.createTask(form);
    }

    @ApiOperation("Update learning task")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateTask(@PathVariable("id") @NotNull Integer id,
                                     @Validated @RequestBody LearningTaskForm form) {
        return learningTaskService.updateTask(id, form);
    }

    @ApiOperation("Disable learning task")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> disableTask(@PathVariable("id") @NotNull Integer id) {
        return learningTaskService.disableTask(id);
    }

    @ApiOperation("Page learning task list")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<LearningTaskVO>> pageTasks(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                   @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                   @RequestParam(value = "title", required = false) String title,
                                                   @RequestParam(value = "taskType", required = false) String taskType,
                                                   @RequestParam(value = "status", required = false) Integer status) {
        return learningTaskService.pageTasks(pageNum, pageSize, title, taskType, status);
    }

    @ApiOperation("List learning task records")
    @GetMapping("/{taskId}/records")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<LearningTaskRecordVO>> listTaskRecords(@PathVariable("taskId") @NotNull Integer taskId) {
        return learningTaskService.listTaskRecords(taskId);
    }

    @ApiOperation("List my learning tasks")
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<List<LearningTaskVO>> listMyTasks(@RequestParam(value = "recordStatus", required = false) String recordStatus,
                                                    @RequestParam(value = "taskType", required = false) String taskType) {
        return learningTaskService.listMyTasks(recordStatus, taskType);
    }

    @ApiOperation("Confirm review task completed")
    @PostMapping("/{taskId}/confirm-review")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> confirmReviewTask(@PathVariable("taskId") @NotNull Integer taskId) {
        return learningTaskService.confirmReviewTask(taskId);
    }
}
