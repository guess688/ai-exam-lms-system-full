package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.course.CourseMaterialForm;
import cn.org.alan.exam.model.form.course.CourseVideoForm;
import cn.org.alan.exam.model.form.course.VideoProgressForm;
import cn.org.alan.exam.model.vo.course.ChapterLearningVO;
import cn.org.alan.exam.model.vo.course.CourseMaterialVO;
import cn.org.alan.exam.model.vo.course.CourseVideoVO;
import cn.org.alan.exam.model.vo.course.StudentVideoProgressVO;
import cn.org.alan.exam.service.ICourseLearningService;
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

@Api(tags = "Course learning APIs")
@RestController
@RequestMapping("/api/course-learning")
public class CourseLearningController {

    @Resource
    private ICourseLearningService courseLearningService;

    @ApiOperation("Create course video")
    @PostMapping("/videos")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addVideo(@Validated @RequestBody CourseVideoForm form) {
        return courseLearningService.addVideo(form);
    }

    @ApiOperation("Update course video")
    @PutMapping("/videos/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateVideo(@PathVariable("id") @NotNull Integer id,
                                      @Validated @RequestBody CourseVideoForm form) {
        return courseLearningService.updateVideo(id, form);
    }

    @ApiOperation("Disable course video")
    @DeleteMapping("/videos/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteVideo(@PathVariable("id") @NotNull Integer id) {
        return courseLearningService.deleteVideo(id);
    }

    @ApiOperation("List course videos")
    @GetMapping("/videos")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<CourseVideoVO>> listVideos(@RequestParam("courseId") Integer courseId,
                                                  @RequestParam(value = "chapterId", required = false) Integer chapterId,
                                                  @RequestParam(value = "status", required = false) Integer status) {
        return courseLearningService.listVideos(courseId, chapterId, status);
    }

    @ApiOperation("Create course material")
    @PostMapping("/materials")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addMaterial(@Validated @RequestBody CourseMaterialForm form) {
        return courseLearningService.addMaterial(form);
    }

    @ApiOperation("Update course material")
    @PutMapping("/materials/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateMaterial(@PathVariable("id") @NotNull Integer id,
                                         @Validated @RequestBody CourseMaterialForm form) {
        return courseLearningService.updateMaterial(id, form);
    }

    @ApiOperation("Delete course material")
    @DeleteMapping("/materials/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteMaterial(@PathVariable("id") @NotNull Integer id) {
        return courseLearningService.deleteMaterial(id);
    }

    @ApiOperation("List course materials")
    @GetMapping("/materials")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<CourseMaterialVO>> listMaterials(@RequestParam("courseId") Integer courseId,
                                                        @RequestParam(value = "chapterId", required = false) Integer chapterId) {
        return courseLearningService.listMaterials(courseId, chapterId);
    }

    @ApiOperation("Course learning detail")
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<ChapterLearningVO>> getCourseLearning(@PathVariable("courseId") @NotNull Integer courseId) {
        return courseLearningService.getCourseLearning(courseId);
    }

    @ApiOperation("Report student video progress")
    @PostMapping("/progress")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<StudentVideoProgressVO> reportProgress(@Validated @RequestBody VideoProgressForm form) {
        return courseLearningService.reportProgress(form);
    }

    @ApiOperation("List my video progress")
    @GetMapping("/progress/my")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<List<StudentVideoProgressVO>> listMyProgress(@RequestParam(value = "courseId", required = false) Integer courseId) {
        return courseLearningService.listMyProgress(courseId);
    }

    @ApiOperation("List course video progress")
    @GetMapping("/progress/course")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<StudentVideoProgressVO>> listCourseProgress(@RequestParam("courseId") Integer courseId,
                                                                   @RequestParam(value = "chapterId", required = false) Integer chapterId,
                                                                   @RequestParam(value = "videoId", required = false) Integer videoId) {
        return courseLearningService.listCourseProgress(courseId, chapterId, videoId);
    }
}
