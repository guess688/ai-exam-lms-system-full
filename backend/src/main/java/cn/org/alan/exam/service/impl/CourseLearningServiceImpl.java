package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.CourseChapterMapper;
import cn.org.alan.exam.mapper.CourseMapper;
import cn.org.alan.exam.mapper.CourseMaterialMapper;
import cn.org.alan.exam.mapper.CourseVideoMapper;
import cn.org.alan.exam.mapper.ExamQuestionMapper;
import cn.org.alan.exam.mapper.KnowledgePointMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.StudentVideoProgressMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.Course;
import cn.org.alan.exam.model.entity.CourseChapter;
import cn.org.alan.exam.model.entity.CourseMaterial;
import cn.org.alan.exam.model.entity.CourseVideo;
import cn.org.alan.exam.model.entity.ExamQuestion;
import cn.org.alan.exam.model.entity.KnowledgePoint;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.StudentVideoProgress;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.course.CourseMaterialForm;
import cn.org.alan.exam.model.form.course.CourseVideoForm;
import cn.org.alan.exam.model.form.course.VideoProgressForm;
import cn.org.alan.exam.model.vo.course.ChapterLearningVO;
import cn.org.alan.exam.model.vo.course.CourseMaterialVO;
import cn.org.alan.exam.model.vo.course.CourseVideoVO;
import cn.org.alan.exam.model.vo.course.KnowledgePointVO;
import cn.org.alan.exam.model.vo.course.StudentVideoProgressVO;
import cn.org.alan.exam.service.ICourseLearningService;
import cn.org.alan.exam.service.ICourseService;
import cn.org.alan.exam.service.ILearningTaskService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CourseLearningServiceImpl extends ServiceImpl<CourseVideoMapper, CourseVideo> implements ICourseLearningService {

    private static final BigDecimal COMPLETE_THRESHOLD = BigDecimal.valueOf(90);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Resource
    private CourseVideoMapper courseVideoMapper;
    @Resource
    private CourseMaterialMapper courseMaterialMapper;
    @Resource
    private StudentVideoProgressMapper studentVideoProgressMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private CourseChapterMapper courseChapterMapper;
    @Resource
    private KnowledgePointMapper knowledgePointMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ICourseService courseService;
    @Resource
    private ILearningTaskService learningTaskService;

    @Override
    @Transactional
    public Result<String> addVideo(CourseVideoForm form) {
        courseService.getManageableCourse(form.getCourseId());
        validateChapterBelongsToCourse(form.getCourseId(), form.getChapterId());

        CourseVideo video = new CourseVideo();
        BeanUtils.copyProperties(form, video);
        video.setDuration(defaultDuration(form.getDuration()));
        video.setSortOrder(defaultSortOrder(form.getSortOrder()));
        video.setStatus(defaultStatus(form.getStatus()));
        video.setUpdateTime(LocalDateTime.now());

        int rows = courseVideoMapper.insert(video);
        if (rows == 0) {
            throw new ServiceRuntimeException("Create course video failed");
        }
        return Result.success("Create course video success");
    }

    @Override
    @Transactional
    public Result<String> updateVideo(Integer id, CourseVideoForm form) {
        CourseVideo existing = findVideo(id);
        courseService.getManageableCourse(existing.getCourseId());
        courseService.getManageableCourse(form.getCourseId());
        validateChapterBelongsToCourse(form.getCourseId(), form.getChapterId());

        CourseVideo video = new CourseVideo();
        BeanUtils.copyProperties(form, video);
        video.setId(id);
        video.setDuration(defaultDuration(form.getDuration()));
        video.setSortOrder(defaultSortOrder(form.getSortOrder()));
        video.setStatus(defaultStatus(form.getStatus()));
        video.setUpdateTime(LocalDateTime.now());

        int rows = courseVideoMapper.updateById(video);
        if (rows == 0) {
            throw new ServiceRuntimeException("Update course video failed");
        }
        return Result.success("Update course video success");
    }

    @Override
    @Transactional
    public Result<String> deleteVideo(Integer id) {
        CourseVideo video = findVideo(id);
        courseService.getManageableCourse(video.getCourseId());

        CourseVideo update = new CourseVideo();
        update.setId(id);
        update.setStatus(0);
        update.setUpdateTime(LocalDateTime.now());
        int rows = courseVideoMapper.updateById(update);
        if (rows == 0) {
            throw new ServiceRuntimeException("Disable course video failed");
        }
        return Result.success("Disable course video success");
    }

    @Override
    public Result<List<CourseVideoVO>> listVideos(Integer courseId, Integer chapterId, Integer status) {
        if (courseId == null) {
            throw new ServiceRuntimeException("courseId is required");
        }
        courseService.getVisibleCourse(courseId);
        if (chapterId != null) {
            validateChapterBelongsToCourse(courseId, chapterId);
        }

        LambdaQueryWrapper<CourseVideo> wrapper = new LambdaQueryWrapper<CourseVideo>()
                .eq(CourseVideo::getCourseId, courseId);
        if (chapterId != null) {
            wrapper.eq(CourseVideo::getChapterId, chapterId);
        }
        if (SecurityUtil.getRoleCode() == 1) {
            wrapper.eq(CourseVideo::getStatus, 1);
        } else if (status != null) {
            wrapper.eq(CourseVideo::getStatus, status);
        }
        wrapper.orderByAsc(CourseVideo::getSortOrder).orderByAsc(CourseVideo::getId);

        Map<Integer, StudentVideoProgress> progressMap = getStudentProgressMap(courseId);
        List<CourseVideoVO> videos = courseVideoMapper.selectList(wrapper).stream()
                .map(video -> toVideoVO(video, progressMap.get(video.getId())))
                .collect(Collectors.toList());
        return Result.success("Query course video success", videos);
    }

    @Override
    @Transactional
    public Result<String> addMaterial(CourseMaterialForm form) {
        courseService.getManageableCourse(form.getCourseId());
        validateChapterBelongsToCourse(form.getCourseId(), form.getChapterId());

        CourseMaterial material = new CourseMaterial();
        BeanUtils.copyProperties(form, material);
        material.setFileType(normalizeFileType(form.getFileType(), form.getFileUrl()));
        material.setSortOrder(defaultSortOrder(form.getSortOrder()));
        material.setUpdateTime(LocalDateTime.now());

        int rows = courseMaterialMapper.insert(material);
        if (rows == 0) {
            throw new ServiceRuntimeException("Create course material failed");
        }
        return Result.success("Create course material success");
    }

    @Override
    @Transactional
    public Result<String> updateMaterial(Integer id, CourseMaterialForm form) {
        CourseMaterial existing = findMaterial(id);
        courseService.getManageableCourse(existing.getCourseId());
        courseService.getManageableCourse(form.getCourseId());
        validateChapterBelongsToCourse(form.getCourseId(), form.getChapterId());

        CourseMaterial material = new CourseMaterial();
        BeanUtils.copyProperties(form, material);
        material.setId(id);
        material.setFileType(normalizeFileType(form.getFileType(), form.getFileUrl()));
        material.setSortOrder(defaultSortOrder(form.getSortOrder()));
        material.setUpdateTime(LocalDateTime.now());

        int rows = courseMaterialMapper.updateById(material);
        if (rows == 0) {
            throw new ServiceRuntimeException("Update course material failed");
        }
        return Result.success("Update course material success");
    }

    @Override
    @Transactional
    public Result<String> deleteMaterial(Integer id) {
        CourseMaterial material = findMaterial(id);
        courseService.getManageableCourse(material.getCourseId());
        int rows = courseMaterialMapper.deleteById(id);
        if (rows == 0) {
            throw new ServiceRuntimeException("Delete course material failed");
        }
        return Result.success("Delete course material success");
    }

    @Override
    public Result<List<CourseMaterialVO>> listMaterials(Integer courseId, Integer chapterId) {
        if (courseId == null) {
            throw new ServiceRuntimeException("courseId is required");
        }
        courseService.getVisibleCourse(courseId);
        if (chapterId != null) {
            validateChapterBelongsToCourse(courseId, chapterId);
        }

        LambdaQueryWrapper<CourseMaterial> wrapper = new LambdaQueryWrapper<CourseMaterial>()
                .eq(CourseMaterial::getCourseId, courseId);
        if (chapterId != null) {
            wrapper.eq(CourseMaterial::getChapterId, chapterId);
        }
        wrapper.orderByAsc(CourseMaterial::getSortOrder).orderByAsc(CourseMaterial::getId);
        List<CourseMaterialVO> materials = courseMaterialMapper.selectList(wrapper).stream()
                .map(this::toMaterialVO)
                .collect(Collectors.toList());
        return Result.success("Query course material success", materials);
    }

    @Override
    public Result<List<ChapterLearningVO>> getCourseLearning(Integer courseId) {
        courseService.getVisibleCourse(courseId);
        LambdaQueryWrapper<CourseChapter> chapterWrapper = new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId);
        if (SecurityUtil.getRoleCode() == 1) {
            chapterWrapper.eq(CourseChapter::getStatus, 1);
        }
        chapterWrapper.orderByAsc(CourseChapter::getSortOrder).orderByAsc(CourseChapter::getId);

        Map<Integer, StudentVideoProgress> progressMap = getStudentProgressMap(courseId);
        List<ChapterLearningVO> chapters = courseChapterMapper.selectList(chapterWrapper).stream()
                .map(chapter -> toChapterLearningVO(chapter, progressMap))
                .collect(Collectors.toList());
        return Result.success("Query course learning success", chapters);
    }

    @Override
    @Transactional
    public Result<StudentVideoProgressVO> reportProgress(VideoProgressForm form) {
        if (SecurityUtil.getRoleCode() != 1) {
            throw new ServiceRuntimeException("Only students can report video progress");
        }
        Integer studentId = SecurityUtil.getUserId();
        CourseVideo video = findVideo(form.getVideoId());
        if (!Integer.valueOf(1).equals(video.getStatus())) {
            throw new ServiceRuntimeException("Course video is disabled");
        }
        courseService.getVisibleCourse(video.getCourseId());

        Integer duration = resolveDuration(form.getDuration(), video.getDuration());
        Integer watchedSeconds = clampWatchedSeconds(form.getWatchedSeconds(), duration);
        StudentVideoProgress existing = studentVideoProgressMapper.selectOne(new LambdaQueryWrapper<StudentVideoProgress>()
                .eq(StudentVideoProgress::getStudentId, studentId)
                .eq(StudentVideoProgress::getVideoId, video.getId()));

        if (existing != null) {
            watchedSeconds = Math.max(defaultDuration(existing.getWatchedSeconds()), watchedSeconds);
            duration = Math.max(defaultDuration(existing.getDuration()), duration);
        }

        BigDecimal progressRate = calculateProgressRate(watchedSeconds, duration);
        Integer completed = progressRate.compareTo(COMPLETE_THRESHOLD) >= 0 ? 1 : 0;
        LocalDateTime now = LocalDateTime.now();

        StudentVideoProgress progress = existing == null ? new StudentVideoProgress() : existing;
        progress.setStudentId(studentId);
        progress.setCourseId(video.getCourseId());
        progress.setChapterId(video.getChapterId());
        progress.setVideoId(video.getId());
        progress.setWatchedSeconds(watchedSeconds);
        progress.setDuration(duration);
        progress.setProgressRate(progressRate);
        progress.setCompleted(completed);
        progress.setLastWatchTime(now);
        progress.setUpdateTime(now);

        if (existing == null) {
            studentVideoProgressMapper.insert(progress);
        } else {
            studentVideoProgressMapper.updateById(progress);
        }
        learningTaskService.syncVideoTaskProgress(studentId, video.getId(), progressRate);
        return Result.success("Report video progress success", toProgressVO(progress));
    }

    @Override
    public Result<List<StudentVideoProgressVO>> listMyProgress(Integer courseId) {
        if (SecurityUtil.getRoleCode() != 1) {
            throw new ServiceRuntimeException("Only students can query my video progress");
        }
        if (courseId != null) {
            courseService.getVisibleCourse(courseId);
        }

        LambdaQueryWrapper<StudentVideoProgress> wrapper = new LambdaQueryWrapper<StudentVideoProgress>()
                .eq(StudentVideoProgress::getStudentId, SecurityUtil.getUserId());
        if (courseId != null) {
            wrapper.eq(StudentVideoProgress::getCourseId, courseId);
        }
        wrapper.orderByDesc(StudentVideoProgress::getLastWatchTime).orderByDesc(StudentVideoProgress::getId);
        List<StudentVideoProgressVO> progress = studentVideoProgressMapper.selectList(wrapper).stream()
                .map(this::toProgressVO)
                .collect(Collectors.toList());
        return Result.success("Query my video progress success", progress);
    }

    @Override
    public Result<List<StudentVideoProgressVO>> listCourseProgress(Integer courseId, Integer chapterId, Integer videoId) {
        if (courseId == null) {
            throw new ServiceRuntimeException("courseId is required");
        }
        courseService.getManageableCourse(courseId);
        if (chapterId != null) {
            validateChapterBelongsToCourse(courseId, chapterId);
        }
        if (videoId != null) {
            CourseVideo video = findVideo(videoId);
            if (!courseId.equals(video.getCourseId())) {
                throw new ServiceRuntimeException("Video does not belong to this course");
            }
        }

        LambdaQueryWrapper<StudentVideoProgress> wrapper = new LambdaQueryWrapper<StudentVideoProgress>()
                .eq(StudentVideoProgress::getCourseId, courseId);
        if (chapterId != null) {
            wrapper.eq(StudentVideoProgress::getChapterId, chapterId);
        }
        if (videoId != null) {
            wrapper.eq(StudentVideoProgress::getVideoId, videoId);
        }
        wrapper.orderByDesc(StudentVideoProgress::getLastWatchTime).orderByDesc(StudentVideoProgress::getId);
        List<StudentVideoProgressVO> progress = studentVideoProgressMapper.selectList(wrapper).stream()
                .map(this::toProgressVO)
                .collect(Collectors.toList());
        return Result.success("Query course video progress success", progress);
    }

    private ChapterLearningVO toChapterLearningVO(CourseChapter chapter, Map<Integer, StudentVideoProgress> progressMap) {
        ChapterLearningVO vo = new ChapterLearningVO();
        BeanUtils.copyProperties(chapter, vo);
        List<KnowledgePointVO> knowledgePoints = listKnowledgePoints(chapter.getId());
        List<CourseVideo> videos = listChapterVideos(chapter.getCourseId(), chapter.getId());
        List<CourseMaterialVO> materials = listChapterMaterials(chapter.getCourseId(), chapter.getId());
        List<Question> questions = listChapterQuestions(chapter.getCourseId(), chapter.getId());

        List<CourseVideoVO> videoVOS = videos.stream()
                .map(video -> toVideoVO(video, progressMap.get(video.getId())))
                .collect(Collectors.toList());
        int completedCount = (int) videoVOS.stream()
                .filter(video -> Integer.valueOf(1).equals(video.getCompleted()))
                .count();

        vo.setKnowledgePoints(knowledgePoints);
        vo.setVideos(videoVOS);
        vo.setMaterials(materials);
        vo.setQuestionCount(questions.size());
        vo.setExamCount(countChapterExams(questions));
        vo.setVideoCount(videoVOS.size());
        vo.setCompletedVideoCount(completedCount);
        vo.setProgressRate(calculateProgressRate(completedCount, videoVOS.size()));
        return vo;
    }

    private List<KnowledgePointVO> listKnowledgePoints(Integer chapterId) {
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getChapterId, chapterId);
        if (SecurityUtil.getRoleCode() == 1) {
            wrapper.eq(KnowledgePoint::getStatus, 1);
        }
        wrapper.orderByAsc(KnowledgePoint::getSortOrder).orderByAsc(KnowledgePoint::getId);
        return knowledgePointMapper.selectList(wrapper).stream()
                .map(this::toKnowledgePointVO)
                .collect(Collectors.toList());
    }

    private List<CourseVideo> listChapterVideos(Integer courseId, Integer chapterId) {
        LambdaQueryWrapper<CourseVideo> wrapper = new LambdaQueryWrapper<CourseVideo>()
                .eq(CourseVideo::getCourseId, courseId)
                .eq(CourseVideo::getChapterId, chapterId);
        if (SecurityUtil.getRoleCode() == 1) {
            wrapper.eq(CourseVideo::getStatus, 1);
        }
        wrapper.orderByAsc(CourseVideo::getSortOrder).orderByAsc(CourseVideo::getId);
        return courseVideoMapper.selectList(wrapper);
    }

    private List<CourseMaterialVO> listChapterMaterials(Integer courseId, Integer chapterId) {
        return courseMaterialMapper.selectList(new LambdaQueryWrapper<CourseMaterial>()
                        .eq(CourseMaterial::getCourseId, courseId)
                        .eq(CourseMaterial::getChapterId, chapterId)
                        .orderByAsc(CourseMaterial::getSortOrder)
                        .orderByAsc(CourseMaterial::getId))
                .stream()
                .map(this::toMaterialVO)
                .collect(Collectors.toList());
    }

    private List<Question> listChapterQuestions(Integer courseId, Integer chapterId) {
        return questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getCourseId, courseId)
                .eq(Question::getChapterId, chapterId));
    }

    private int countChapterExams(List<Question> questions) {
        if (questions.isEmpty()) {
            return 0;
        }
        Set<Integer> questionIds = questions.stream()
                .map(Question::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (questionIds.isEmpty()) {
            return 0;
        }
        return (int) examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                        .in(ExamQuestion::getQuestionId, questionIds))
                .stream()
                .map(ExamQuestion::getExamId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    private Map<Integer, StudentVideoProgress> getStudentProgressMap(Integer courseId) {
        if (SecurityUtil.getRoleCode() != 1) {
            return Collections.emptyMap();
        }
        return studentVideoProgressMapper.selectList(new LambdaQueryWrapper<StudentVideoProgress>()
                        .eq(StudentVideoProgress::getStudentId, SecurityUtil.getUserId())
                        .eq(StudentVideoProgress::getCourseId, courseId))
                .stream()
                .collect(Collectors.toMap(StudentVideoProgress::getVideoId, Function.identity(), (left, right) -> right));
    }

    private CourseVideoVO toVideoVO(CourseVideo video, StudentVideoProgress progress) {
        CourseVideoVO vo = new CourseVideoVO();
        BeanUtils.copyProperties(video, vo);
        Course course = courseMapper.selectById(video.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        CourseChapter chapter = courseChapterMapper.selectById(video.getChapterId());
        if (chapter != null) {
            vo.setChapterTitle(chapter.getTitle());
        }
        if (progress != null) {
            vo.setWatchedSeconds(progress.getWatchedSeconds());
            vo.setProgressRate(progress.getProgressRate());
            vo.setCompleted(progress.getCompleted());
            vo.setLastWatchTime(progress.getLastWatchTime());
        } else {
            vo.setWatchedSeconds(0);
            vo.setProgressRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            vo.setCompleted(0);
        }
        return vo;
    }

    private CourseMaterialVO toMaterialVO(CourseMaterial material) {
        CourseMaterialVO vo = new CourseMaterialVO();
        BeanUtils.copyProperties(material, vo);
        Course course = courseMapper.selectById(material.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        CourseChapter chapter = courseChapterMapper.selectById(material.getChapterId());
        if (chapter != null) {
            vo.setChapterTitle(chapter.getTitle());
        }
        return vo;
    }

    private StudentVideoProgressVO toProgressVO(StudentVideoProgress progress) {
        StudentVideoProgressVO vo = new StudentVideoProgressVO();
        BeanUtils.copyProperties(progress, vo);
        User user = userMapper.selectById(progress.getStudentId());
        if (user != null) {
            vo.setStudentName(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUserName());
        }
        Course course = courseMapper.selectById(progress.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        CourseChapter chapter = courseChapterMapper.selectById(progress.getChapterId());
        if (chapter != null) {
            vo.setChapterTitle(chapter.getTitle());
        }
        CourseVideo video = courseVideoMapper.selectById(progress.getVideoId());
        if (video != null) {
            vo.setVideoTitle(video.getTitle());
        }
        return vo;
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

    private CourseVideo findVideo(Integer id) {
        CourseVideo video = courseVideoMapper.selectById(id);
        if (video == null) {
            throw new ServiceRuntimeException("Course video does not exist");
        }
        return video;
    }

    private CourseMaterial findMaterial(Integer id) {
        CourseMaterial material = courseMaterialMapper.selectById(id);
        if (material == null) {
            throw new ServiceRuntimeException("Course material does not exist");
        }
        return material;
    }

    private void validateChapterBelongsToCourse(Integer courseId, Integer chapterId) {
        CourseChapter chapter = courseChapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new ServiceRuntimeException("Chapter does not exist");
        }
        if (!courseId.equals(chapter.getCourseId())) {
            throw new ServiceRuntimeException("Chapter does not belong to this course");
        }
    }

    private Integer resolveDuration(Integer reportDuration, Integer videoDuration) {
        if (reportDuration != null && reportDuration > 0) {
            return reportDuration;
        }
        return defaultDuration(videoDuration);
    }

    private Integer clampWatchedSeconds(Integer watchedSeconds, Integer duration) {
        Integer watched = defaultDuration(watchedSeconds);
        if (duration != null && duration > 0) {
            return Math.min(watched, duration);
        }
        return watched;
    }

    private BigDecimal calculateProgressRate(Integer numerator, Integer denominator) {
        if (denominator == null || denominator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal rate = BigDecimal.valueOf(defaultDuration(numerator))
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
        return rate.compareTo(ONE_HUNDRED) > 0 ? ONE_HUNDRED.setScale(2, RoundingMode.HALF_UP) : rate;
    }

    private Integer defaultDuration(Integer duration) {
        return duration == null ? 0 : Math.max(duration, 0);
    }

    private Integer defaultSortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }

    private Integer defaultStatus(Integer status) {
        return status == null ? 1 : status;
    }

    private String normalizeFileType(String fileType, String fileUrl) {
        if (StringUtils.hasText(fileType)) {
            return fileType.trim();
        }
        if (!StringUtils.hasText(fileUrl)) {
            return "link";
        }
        String cleanUrl = fileUrl.split("\\?")[0];
        int dotIndex = cleanUrl.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < cleanUrl.length() - 1) {
            return cleanUrl.substring(dotIndex + 1).toLowerCase();
        }
        return "link";
    }
}
