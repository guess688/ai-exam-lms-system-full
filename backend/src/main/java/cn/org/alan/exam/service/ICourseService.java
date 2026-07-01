package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Course;
import cn.org.alan.exam.model.form.course.CourseForm;
import cn.org.alan.exam.model.vo.course.CourseVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ICourseService extends IService<Course> {

    Result<String> addCourse(CourseForm form);

    Result<String> updateCourse(Integer id, CourseForm form);

    Result<String> disableCourse(Integer id);

    Result<IPage<CourseVO>> pagingCourses(Integer pageNum, Integer pageSize, String name, Integer gradeId, Integer status);

    Result<List<CourseVO>> listMyCourses(String name);

    Result<CourseVO> getCourseDetail(Integer id);

    Course getManageableCourse(Integer id);

    Course getVisibleCourse(Integer id);
}
