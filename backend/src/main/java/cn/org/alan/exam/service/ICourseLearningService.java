package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.CourseVideo;
import cn.org.alan.exam.model.form.course.CourseMaterialForm;
import cn.org.alan.exam.model.form.course.CourseVideoForm;
import cn.org.alan.exam.model.form.course.VideoProgressForm;
import cn.org.alan.exam.model.vo.course.ChapterLearningVO;
import cn.org.alan.exam.model.vo.course.CourseMaterialVO;
import cn.org.alan.exam.model.vo.course.CourseVideoVO;
import cn.org.alan.exam.model.vo.course.StudentVideoProgressVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ICourseLearningService extends IService<CourseVideo> {

    Result<String> addVideo(CourseVideoForm form);

    Result<String> updateVideo(Integer id, CourseVideoForm form);

    Result<String> deleteVideo(Integer id);

    Result<List<CourseVideoVO>> listVideos(Integer courseId, Integer chapterId, Integer status);

    Result<String> addMaterial(CourseMaterialForm form);

    Result<String> updateMaterial(Integer id, CourseMaterialForm form);

    Result<String> deleteMaterial(Integer id);

    Result<List<CourseMaterialVO>> listMaterials(Integer courseId, Integer chapterId);

    Result<List<ChapterLearningVO>> getCourseLearning(Integer courseId);

    Result<StudentVideoProgressVO> reportProgress(VideoProgressForm form);

    Result<List<StudentVideoProgressVO>> listMyProgress(Integer courseId);

    Result<List<StudentVideoProgressVO>> listCourseProgress(Integer courseId, Integer chapterId, Integer videoId);
}
