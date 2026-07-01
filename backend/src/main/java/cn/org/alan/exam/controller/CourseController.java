package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.course.CourseForm;
import cn.org.alan.exam.model.vo.course.CourseVO;
import cn.org.alan.exam.service.ICourseService;
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

@Api(tags = "Course management APIs")
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Resource
    private ICourseService courseService;

    @ApiOperation("Create course")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addCourse(@Validated @RequestBody CourseForm form) {
        return courseService.addCourse(form);
    }

    @ApiOperation("Update course")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateCourse(@PathVariable("id") @NotNull Integer id,
                                       @Validated @RequestBody CourseForm form) {
        return courseService.updateCourse(id, form);
    }

    @ApiOperation("Disable course")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> disableCourse(@PathVariable("id") @NotNull Integer id) {
        return courseService.disableCourse(id);
    }

    @ApiOperation("Page course list")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<CourseVO>> pagingCourses(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                 @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                 @RequestParam(value = "name", required = false) String name,
                                                 @RequestParam(value = "gradeId", required = false) Integer gradeId,
                                                 @RequestParam(value = "status", required = false) Integer status) {
        return courseService.pagingCourses(pageNum, pageSize, name, gradeId, status);
    }

    @ApiOperation("Student visible course list")
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<List<CourseVO>> listMyCourses(@RequestParam(value = "name", required = false) String name) {
        return courseService.listMyCourses(name);
    }

    @ApiOperation("Course detail")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<CourseVO> getCourseDetail(@PathVariable("id") @NotNull Integer id) {
        return courseService.getCourseDetail(id);
    }
}
