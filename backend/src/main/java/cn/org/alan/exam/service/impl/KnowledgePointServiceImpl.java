package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.CourseChapterMapper;
import cn.org.alan.exam.mapper.CourseMapper;
import cn.org.alan.exam.mapper.KnowledgePointMapper;
import cn.org.alan.exam.model.entity.Course;
import cn.org.alan.exam.model.entity.CourseChapter;
import cn.org.alan.exam.model.entity.KnowledgePoint;
import cn.org.alan.exam.model.form.course.KnowledgePointForm;
import cn.org.alan.exam.model.vo.course.KnowledgePointVO;
import cn.org.alan.exam.service.ICourseService;
import cn.org.alan.exam.service.IKnowledgePointService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgePointServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint> implements IKnowledgePointService {

    @Resource
    private KnowledgePointMapper knowledgePointMapper;
    @Resource
    private CourseChapterMapper courseChapterMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private ICourseService courseService;

    @Override
    @Transactional
    public Result<String> addKnowledgePoint(KnowledgePointForm form) {
        CourseChapter chapter = findChapter(form.getChapterId());
        courseService.getManageableCourse(chapter.getCourseId());
        KnowledgePoint knowledgePoint = new KnowledgePoint();
        BeanUtils.copyProperties(form, knowledgePoint);
        knowledgePoint.setCourseId(chapter.getCourseId());
        knowledgePoint.setStatus(defaultStatus(form.getStatus()));
        knowledgePoint.setSortOrder(defaultSortOrder(form.getSortOrder()));
        knowledgePoint.setUpdateTime(LocalDateTime.now());
        int rows = knowledgePointMapper.insert(knowledgePoint);
        if (rows == 0) {
            throw new ServiceRuntimeException("Create knowledge point failed");
        }
        return Result.success("Create knowledge point success");
    }

    @Override
    @Transactional
    public Result<String> updateKnowledgePoint(Integer id, KnowledgePointForm form) {
        KnowledgePoint existing = findKnowledgePoint(id);
        courseService.getManageableCourse(existing.getCourseId());
        CourseChapter chapter = findChapter(form.getChapterId());
        courseService.getManageableCourse(chapter.getCourseId());

        KnowledgePoint knowledgePoint = new KnowledgePoint();
        BeanUtils.copyProperties(form, knowledgePoint);
        knowledgePoint.setId(id);
        knowledgePoint.setCourseId(chapter.getCourseId());
        knowledgePoint.setStatus(defaultStatus(form.getStatus()));
        knowledgePoint.setSortOrder(defaultSortOrder(form.getSortOrder()));
        knowledgePoint.setUpdateTime(LocalDateTime.now());
        int rows = knowledgePointMapper.updateById(knowledgePoint);
        if (rows == 0) {
            throw new ServiceRuntimeException("Update knowledge point failed");
        }
        return Result.success("Update knowledge point success");
    }

    @Override
    @Transactional
    public Result<String> disableKnowledgePoint(Integer id) {
        KnowledgePoint knowledgePoint = findKnowledgePoint(id);
        courseService.getManageableCourse(knowledgePoint.getCourseId());
        KnowledgePoint update = new KnowledgePoint();
        update.setId(id);
        update.setStatus(0);
        update.setUpdateTime(LocalDateTime.now());
        int rows = knowledgePointMapper.updateById(update);
        if (rows == 0) {
            throw new ServiceRuntimeException("Disable knowledge point failed");
        }
        return Result.success("Disable knowledge point success");
    }

    @Override
    public Result<List<KnowledgePointVO>> listByChapter(Integer chapterId, Integer status) {
        CourseChapter chapter = findChapter(chapterId);
        courseService.getVisibleCourse(chapter.getCourseId());
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getChapterId, chapterId);
        if (SecurityUtil.getRoleCode() == 1) {
            wrapper.eq(KnowledgePoint::getStatus, 1);
        } else if (status != null) {
            wrapper.eq(KnowledgePoint::getStatus, status);
        }
        wrapper.orderByAsc(KnowledgePoint::getSortOrder).orderByAsc(KnowledgePoint::getId);
        List<KnowledgePointVO> knowledgePoints = knowledgePointMapper.selectList(wrapper).stream()
                .map(this::toKnowledgePointVO)
                .collect(Collectors.toList());
        return Result.success("Query knowledge point success", knowledgePoints);
    }

    private CourseChapter findChapter(Integer id) {
        CourseChapter chapter = courseChapterMapper.selectById(id);
        if (chapter == null) {
            throw new ServiceRuntimeException("Chapter does not exist");
        }
        return chapter;
    }

    private KnowledgePoint findKnowledgePoint(Integer id) {
        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(id);
        if (knowledgePoint == null) {
            throw new ServiceRuntimeException("Knowledge point does not exist");
        }
        return knowledgePoint;
    }

    private Integer defaultStatus(Integer status) {
        return status == null ? 1 : status;
    }

    private Integer defaultSortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }

    private KnowledgePointVO toKnowledgePointVO(KnowledgePoint knowledgePoint) {
        KnowledgePointVO vo = new KnowledgePointVO();
        BeanUtils.copyProperties(knowledgePoint, vo);
        Course course = courseMapper.selectById(knowledgePoint.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        CourseChapter chapter = courseChapterMapper.selectById(knowledgePoint.getChapterId());
        if (chapter != null) {
            vo.setChapterTitle(chapter.getTitle());
        }
        return vo;
    }
}
