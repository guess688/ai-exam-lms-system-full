package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.CourseChapterMapper;
import cn.org.alan.exam.mapper.CourseMapper;
import cn.org.alan.exam.mapper.KnowledgePointMapper;
import cn.org.alan.exam.model.entity.Course;
import cn.org.alan.exam.model.entity.CourseChapter;
import cn.org.alan.exam.model.entity.KnowledgePoint;
import cn.org.alan.exam.model.form.course.CourseChapterForm;
import cn.org.alan.exam.model.vo.course.CourseChapterVO;
import cn.org.alan.exam.service.ICourseChapterService;
import cn.org.alan.exam.service.ICourseService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseChapterServiceImpl extends ServiceImpl<CourseChapterMapper, CourseChapter> implements ICourseChapterService {

    @Resource
    private CourseChapterMapper courseChapterMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private KnowledgePointMapper knowledgePointMapper;
    @Resource
    private ICourseService courseService;

    @Override
    @Transactional
    public Result<String> addChapter(CourseChapterForm form) {
        courseService.getManageableCourse(form.getCourseId());
        CourseChapter chapter = new CourseChapter();
        BeanUtils.copyProperties(form, chapter);
        chapter.setStatus(defaultStatus(form.getStatus()));
        chapter.setSortOrder(defaultSortOrder(form.getSortOrder()));
        chapter.setUpdateTime(LocalDateTime.now());
        int rows = courseChapterMapper.insert(chapter);
        if (rows == 0) {
            throw new ServiceRuntimeException("Create chapter failed");
        }
        return Result.success("Create chapter success");
    }

    @Override
    @Transactional
    public Result<String> updateChapter(Integer id, CourseChapterForm form) {
        CourseChapter existing = findChapter(id);
        courseService.getManageableCourse(existing.getCourseId());
        courseService.getManageableCourse(form.getCourseId());

        CourseChapter chapter = new CourseChapter();
        BeanUtils.copyProperties(form, chapter);
        chapter.setId(id);
        chapter.setStatus(defaultStatus(form.getStatus()));
        chapter.setSortOrder(defaultSortOrder(form.getSortOrder()));
        chapter.setUpdateTime(LocalDateTime.now());
        int rows = courseChapterMapper.updateById(chapter);
        if (rows == 0) {
            throw new ServiceRuntimeException("Update chapter failed");
        }
        if (!existing.getCourseId().equals(form.getCourseId())) {
            knowledgePointMapper.update(null, new LambdaUpdateWrapper<KnowledgePoint>()
                    .set(KnowledgePoint::getCourseId, form.getCourseId())
                    .set(KnowledgePoint::getUpdateTime, LocalDateTime.now())
                    .eq(KnowledgePoint::getChapterId, id));
        }
        return Result.success("Update chapter success");
    }

    @Override
    @Transactional
    public Result<String> disableChapter(Integer id) {
        CourseChapter chapter = findChapter(id);
        courseService.getManageableCourse(chapter.getCourseId());
        CourseChapter update = new CourseChapter();
        update.setId(id);
        update.setStatus(0);
        update.setUpdateTime(LocalDateTime.now());
        int rows = courseChapterMapper.updateById(update);
        if (rows == 0) {
            throw new ServiceRuntimeException("Disable chapter failed");
        }
        knowledgePointMapper.update(null, new LambdaUpdateWrapper<KnowledgePoint>()
                .set(KnowledgePoint::getStatus, 0)
                .set(KnowledgePoint::getUpdateTime, LocalDateTime.now())
                .eq(KnowledgePoint::getChapterId, id));
        return Result.success("Disable chapter success");
    }

    @Override
    public Result<List<CourseChapterVO>> listByCourse(Integer courseId, Integer status) {
        courseService.getVisibleCourse(courseId);
        LambdaQueryWrapper<CourseChapter> wrapper = new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId);
        if (SecurityUtil.getRoleCode() == 1) {
            wrapper.eq(CourseChapter::getStatus, 1);
        } else if (status != null) {
            wrapper.eq(CourseChapter::getStatus, status);
        }
        wrapper.orderByAsc(CourseChapter::getSortOrder).orderByAsc(CourseChapter::getId);
        List<CourseChapterVO> chapters = courseChapterMapper.selectList(wrapper).stream()
                .map(this::toChapterVO)
                .collect(Collectors.toList());
        return Result.success("Query chapter success", chapters);
    }

    private CourseChapter findChapter(Integer id) {
        CourseChapter chapter = courseChapterMapper.selectById(id);
        if (chapter == null) {
            throw new ServiceRuntimeException("Chapter does not exist");
        }
        return chapter;
    }

    private Integer defaultStatus(Integer status) {
        return status == null ? 1 : status;
    }

    private Integer defaultSortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }

    private CourseChapterVO toChapterVO(CourseChapter chapter) {
        CourseChapterVO vo = new CourseChapterVO();
        BeanUtils.copyProperties(chapter, vo);
        Course course = courseMapper.selectById(chapter.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        vo.setKnowledgePointCount(knowledgePointMapper.selectCount(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getChapterId, chapter.getId())).intValue());
        return vo;
    }
}
