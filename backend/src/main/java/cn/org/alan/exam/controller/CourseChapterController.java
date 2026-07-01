package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.course.CourseChapterForm;
import cn.org.alan.exam.model.vo.course.CourseChapterVO;
import cn.org.alan.exam.service.ICourseChapterService;
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

@Api(tags = "Course chapter management APIs")
@RestController
@RequestMapping("/api/course-chapters")
public class CourseChapterController {

    @Resource
    private ICourseChapterService courseChapterService;

    @ApiOperation("Create chapter")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addChapter(@Validated @RequestBody CourseChapterForm form) {
        return courseChapterService.addChapter(form);
    }

    @ApiOperation("Update chapter")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateChapter(@PathVariable("id") @NotNull Integer id,
                                        @Validated @RequestBody CourseChapterForm form) {
        return courseChapterService.updateChapter(id, form);
    }

    @ApiOperation("Disable chapter")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> disableChapter(@PathVariable("id") @NotNull Integer id) {
        return courseChapterService.disableChapter(id);
    }

    @ApiOperation("List chapters by course")
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<CourseChapterVO>> listByCourse(@PathVariable("courseId") @NotNull Integer courseId,
                                                      @RequestParam(value = "status", required = false) Integer status) {
        return courseChapterService.listByCourse(courseId, status);
    }
}
