package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.CourseChapter;
import cn.org.alan.exam.model.form.course.CourseChapterForm;
import cn.org.alan.exam.model.vo.course.CourseChapterVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ICourseChapterService extends IService<CourseChapter> {

    Result<String> addChapter(CourseChapterForm form);

    Result<String> updateChapter(Integer id, CourseChapterForm form);

    Result<String> disableChapter(Integer id);

    Result<List<CourseChapterVO>> listByCourse(Integer courseId, Integer status);
}
