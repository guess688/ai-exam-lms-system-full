package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.CourseChapterMapper;
import cn.org.alan.exam.mapper.CourseMapper;
import cn.org.alan.exam.mapper.GradeMapper;
import cn.org.alan.exam.mapper.KnowledgePointMapper;
import cn.org.alan.exam.mapper.UserGradeMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.Course;
import cn.org.alan.exam.model.entity.CourseChapter;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.KnowledgePoint;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.course.CourseForm;
import cn.org.alan.exam.model.vo.course.CourseVO;
import cn.org.alan.exam.service.ICourseService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    @Resource
    private CourseMapper courseMapper;
    @Resource
    private CourseChapterMapper courseChapterMapper;
    @Resource
    private KnowledgePointMapper knowledgePointMapper;
    @Resource
    private GradeMapper gradeMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserGradeMapper userGradeMapper;

    @Override
    @Transactional
    public Result<String> addCourse(CourseForm form) {
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        ensureGradeExists(form.getGradeId());
        ensureTeacherCanUseGrade(roleCode, userId, form.getGradeId());

        Course course = new Course();
        BeanUtils.copyProperties(form, course);
        course.setTeacherId(resolveTeacherId(roleCode, userId, form.getTeacherId()));
        course.setStatus(defaultStatus(form.getStatus()));
        course.setUpdateTime(LocalDateTime.now());

        int rows = courseMapper.insert(course);
        if (rows == 0) {
            throw new ServiceRuntimeException("Create course failed");
        }
        return Result.success("Create course success");
    }

    @Override
    @Transactional
    public Result<String> updateCourse(Integer id, CourseForm form) {
        Course existing = getManageableCourse(id);
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        ensureGradeExists(form.getGradeId());
        ensureTeacherCanUseGrade(roleCode, userId, form.getGradeId());

        Course course = new Course();
        BeanUtils.copyProperties(form, course);
        course.setId(id);
        course.setTeacherId(resolveTeacherIdForUpdate(roleCode, userId, form.getTeacherId(), existing.getTeacherId()));
        course.setStatus(defaultStatus(form.getStatus()));
        course.setUpdateTime(LocalDateTime.now());

        int rows = courseMapper.updateById(course);
        if (rows == 0) {
            throw new ServiceRuntimeException("Update course failed");
        }
        return Result.success("Update course success");
    }

    @Override
    @Transactional
    public Result<String> disableCourse(Integer id) {
        getManageableCourse(id);
        Course course = new Course();
        course.setId(id);
        course.setStatus(0);
        course.setUpdateTime(LocalDateTime.now());
        int rows = courseMapper.updateById(course);
        if (rows == 0) {
            throw new ServiceRuntimeException("Disable course failed");
        }

        courseChapterMapper.update(null, new LambdaUpdateWrapper<CourseChapter>()
                .set(CourseChapter::getStatus, 0)
                .set(CourseChapter::getUpdateTime, LocalDateTime.now())
                .eq(CourseChapter::getCourseId, id));
        knowledgePointMapper.update(null, new LambdaUpdateWrapper<KnowledgePoint>()
                .set(KnowledgePoint::getStatus, 0)
                .set(KnowledgePoint::getUpdateTime, LocalDateTime.now())
                .eq(KnowledgePoint::getCourseId, id));
        return Result.success("Disable course success");
    }

    @Override
    public Result<IPage<CourseVO>> pagingCourses(Integer pageNum, Integer pageSize, String name, Integer gradeId, Integer status) {
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = buildCourseQuery(name, gradeId, status);

        if (roleCode == 2) {
            List<Integer> gradeIds = getTeacherGradeIds(userId);
            wrapper.and(query -> {
                query.eq(Course::getTeacherId, userId);
                if (!gradeIds.isEmpty()) {
                    query.or().in(Course::getGradeId, gradeIds);
                }
            });
        } else if (roleCode == 1) {
            Integer studentGradeId = SecurityUtil.getGradeId();
            wrapper.eq(Course::getStatus, 1)
                    .eq(Course::getGradeId, studentGradeId);
        }

        wrapper.orderByDesc(Course::getCreateTime).orderByDesc(Course::getId);
        IPage<Course> coursePage = courseMapper.selectPage(page, wrapper);
        return Result.success("Query course success", coursePage.convert(this::toCourseVO));
    }

    @Override
    public Result<List<CourseVO>> listMyCourses(String name) {
        Integer gradeId = SecurityUtil.getGradeId();
        if (gradeId == null) {
            throw new ServiceRuntimeException("Student has no class");
        }
        LambdaQueryWrapper<Course> wrapper = buildCourseQuery(name, gradeId, 1)
                .orderByDesc(Course::getCreateTime)
                .orderByDesc(Course::getId);
        List<CourseVO> courses = courseMapper.selectList(wrapper).stream()
                .map(this::toCourseVO)
                .collect(Collectors.toList());
        return Result.success("Query course success", courses);
    }

    @Override
    public Result<CourseVO> getCourseDetail(Integer id) {
        Course course = getVisibleCourse(id);
        return Result.success("Query course success", toCourseVO(course));
    }

    @Override
    public Course getManageableCourse(Integer id) {
        Course course = findCourse(id);
        if (!canManageCourse(course)) {
            throw new ServiceRuntimeException("No permission to manage this course");
        }
        return course;
    }

    @Override
    public Course getVisibleCourse(Integer id) {
        Course course = findCourse(id);
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode == 3 || (roleCode == 2 && canManageCourse(course))) {
            return course;
        }
        Integer gradeId = SecurityUtil.getGradeId();
        if (roleCode == 1 && Integer.valueOf(1).equals(course.getStatus()) && course.getGradeId() != null && course.getGradeId().equals(gradeId)) {
            return course;
        }
        throw new ServiceRuntimeException("No permission to view this course");
    }

    private LambdaQueryWrapper<Course> buildCourseQuery(String name, Integer gradeId, Integer status) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Course::getName, name);
        }
        if (gradeId != null) {
            wrapper.eq(Course::getGradeId, gradeId);
        }
        if (status != null) {
            wrapper.eq(Course::getStatus, status);
        }
        return wrapper;
    }

    private Course findCourse(Integer id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new ServiceRuntimeException("Course does not exist");
        }
        return course;
    }

    private boolean canManageCourse(Course course) {
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode == 3) {
            return true;
        }
        if (roleCode != 2) {
            return false;
        }
        Integer userId = SecurityUtil.getUserId();
        if (userId.equals(course.getTeacherId())) {
            return true;
        }
        return course.getGradeId() != null && getTeacherGradeIds(userId).contains(course.getGradeId());
    }

    private List<Integer> getTeacherGradeIds(Integer userId) {
        List<Integer> gradeIds = userGradeMapper.getGradeIdListByUserId(userId);
        return gradeIds == null ? Collections.emptyList() : gradeIds;
    }

    private void ensureGradeExists(Integer gradeId) {
        if (gradeMapper.selectById(gradeId) == null) {
            throw new ServiceRuntimeException("Class does not exist");
        }
    }

    private void ensureTeacherCanUseGrade(Integer roleCode, Integer userId, Integer gradeId) {
        if (roleCode == 2 && !getTeacherGradeIds(userId).contains(gradeId)) {
            throw new ServiceRuntimeException("No permission for this class");
        }
    }

    private Integer resolveTeacherId(Integer roleCode, Integer userId, Integer formTeacherId) {
        if (roleCode == 3 && formTeacherId != null) {
            if (userMapper.selectById(formTeacherId) == null) {
                throw new ServiceRuntimeException("Teacher does not exist");
            }
            return formTeacherId;
        }
        return userId;
    }

    private Integer resolveTeacherIdForUpdate(Integer roleCode, Integer userId, Integer formTeacherId, Integer existingTeacherId) {
        if (roleCode == 3 && formTeacherId != null) {
            return resolveTeacherId(roleCode, userId, formTeacherId);
        }
        return existingTeacherId;
    }

    private Integer defaultStatus(Integer status) {
        return status == null ? 1 : status;
    }

    private CourseVO toCourseVO(Course course) {
        CourseVO vo = new CourseVO();
        BeanUtils.copyProperties(course, vo);
        Grade grade = course.getGradeId() == null ? null : gradeMapper.selectById(course.getGradeId());
        if (grade != null) {
            vo.setGradeName(grade.getGradeName());
        }
        User teacher = course.getTeacherId() == null ? null : userMapper.selectById(course.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(StringUtils.hasText(teacher.getRealName()) ? teacher.getRealName() : teacher.getUserName());
        }
        vo.setChapterCount(courseChapterMapper.selectCount(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, course.getId())).intValue());
        vo.setKnowledgePointCount(knowledgePointMapper.selectCount(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getCourseId, course.getId())).intValue());
        return vo;
    }
}
