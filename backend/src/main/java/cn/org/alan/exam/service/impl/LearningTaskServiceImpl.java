package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.CourseChapterMapper;
import cn.org.alan.exam.mapper.CourseMapper;
import cn.org.alan.exam.mapper.CourseVideoMapper;
import cn.org.alan.exam.mapper.ExamMapper;
import cn.org.alan.exam.mapper.ExerciseRecordMapper;
import cn.org.alan.exam.mapper.GradeMapper;
import cn.org.alan.exam.mapper.KnowledgePointMapper;
import cn.org.alan.exam.mapper.LearningTaskMapper;
import cn.org.alan.exam.mapper.LearningTaskRecordMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.RepoMapper;
import cn.org.alan.exam.mapper.UserBookMapper;
import cn.org.alan.exam.mapper.UserGradeMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.Course;
import cn.org.alan.exam.model.entity.CourseChapter;
import cn.org.alan.exam.model.entity.CourseVideo;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExerciseRecord;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.KnowledgePoint;
import cn.org.alan.exam.model.entity.LearningTask;
import cn.org.alan.exam.model.entity.LearningTaskRecord;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.entity.UserBook;
import cn.org.alan.exam.model.form.task.LearningTaskForm;
import cn.org.alan.exam.model.vo.task.LearningTaskRecordVO;
import cn.org.alan.exam.model.vo.task.LearningTaskVO;
import cn.org.alan.exam.service.ICourseService;
import cn.org.alan.exam.service.ILearningTaskService;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LearningTaskServiceImpl extends ServiceImpl<LearningTaskMapper, LearningTask> implements ILearningTaskService {

    private static final String TARGET_CLASS = "CLASS";
    private static final String TARGET_STUDENT = "STUDENT";
    private static final String STATUS_TODO = "TODO";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Resource
    private LearningTaskMapper learningTaskMapper;
    @Resource
    private LearningTaskRecordMapper learningTaskRecordMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserGradeMapper userGradeMapper;
    @Resource
    private GradeMapper gradeMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private CourseChapterMapper courseChapterMapper;
    @Resource
    private KnowledgePointMapper knowledgePointMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private RepoMapper repoMapper;
    @Resource
    private CourseVideoMapper courseVideoMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private ExerciseRecordMapper exerciseRecordMapper;
    @Resource
    private UserBookMapper userBookMapper;
    @Resource
    private ICourseService courseService;

    @Override
    @Transactional
    public Result<String> createTask(LearningTaskForm form) {
        LearningTask task = buildTaskFromForm(new LearningTask(), form);
        task.setPublisherId(SecurityUtil.getUserId());
        task.setUpdateTime(LocalDateTime.now());
        learningTaskMapper.insert(task);

        List<User> students = resolveTargetStudents(task);
        if (students.isEmpty()) {
            throw new ServiceRuntimeException("No target students found");
        }
        for (User student : students) {
            LearningTaskRecord record = new LearningTaskRecord();
            record.setTaskId(task.getId());
            record.setStudentId(student.getId());
            record.setStatus(STATUS_TODO);
            record.setProgressRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            record.setUpdateTime(LocalDateTime.now());
            learningTaskRecordMapper.insert(record);
        }
        return Result.success("Create learning task success");
    }

    @Override
    @Transactional
    public Result<String> updateTask(Integer id, LearningTaskForm form) {
        LearningTask existing = findManageableTask(id);
        LearningTask update = buildTaskFromForm(new LearningTask(), form);
        update.setId(id);
        update.setPublisherId(existing.getPublisherId());
        update.setUpdateTime(LocalDateTime.now());
        learningTaskMapper.updateById(update);

        boolean targetChanged = !Objects.equals(existing.getTargetType(), update.getTargetType())
                || !Objects.equals(existing.getTargetClassId(), update.getTargetClassId())
                || !Objects.equals(existing.getTargetStudentId(), update.getTargetStudentId());
        if (targetChanged) {
            learningTaskRecordMapper.delete(new LambdaQueryWrapper<LearningTaskRecord>()
                    .eq(LearningTaskRecord::getTaskId, id)
                    .ne(LearningTaskRecord::getStatus, STATUS_COMPLETED));
            Set<Integer> existingStudentIds = learningTaskRecordMapper.selectList(new LambdaQueryWrapper<LearningTaskRecord>()
                            .eq(LearningTaskRecord::getTaskId, id))
                    .stream()
                    .map(LearningTaskRecord::getStudentId)
                    .collect(Collectors.toSet());
            for (User student : resolveTargetStudents(update)) {
                if (existingStudentIds.contains(student.getId())) {
                    continue;
                }
                LearningTaskRecord record = new LearningTaskRecord();
                record.setTaskId(id);
                record.setStudentId(student.getId());
                record.setStatus(STATUS_TODO);
                record.setProgressRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                record.setUpdateTime(LocalDateTime.now());
                learningTaskRecordMapper.insert(record);
            }
        }
        return Result.success("Update learning task success");
    }

    @Override
    @Transactional
    public Result<String> disableTask(Integer id) {
        LearningTask task = findManageableTask(id);
        LearningTask update = new LearningTask();
        update.setId(task.getId());
        update.setStatus(0);
        update.setUpdateTime(LocalDateTime.now());
        learningTaskMapper.updateById(update);
        return Result.success("Disable learning task success");
    }

    @Override
    public Result<IPage<LearningTaskVO>> pageTasks(Integer pageNum, Integer pageSize, String title, String taskType, Integer status) {
        Page<LearningTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LearningTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(LearningTask::getTitle, title);
        }
        if (StringUtils.hasText(taskType)) {
            wrapper.eq(LearningTask::getTaskType, normalize(taskType));
        }
        if (status != null) {
            wrapper.eq(LearningTask::getStatus, status);
        }
        if (SecurityUtil.getRoleCode() == 2) {
            wrapper.eq(LearningTask::getPublisherId, SecurityUtil.getUserId());
        }
        wrapper.orderByDesc(LearningTask::getCreateTime).orderByDesc(LearningTask::getId);
        IPage<LearningTask> taskPage = learningTaskMapper.selectPage(page, wrapper);
        return Result.success("Query learning task success", taskPage.convert(this::toTaskVO));
    }

    @Override
    public Result<List<LearningTaskRecordVO>> listTaskRecords(Integer taskId) {
        findManageableTask(taskId);
        List<LearningTaskRecordVO> records = learningTaskRecordMapper.selectList(new LambdaQueryWrapper<LearningTaskRecord>()
                        .eq(LearningTaskRecord::getTaskId, taskId)
                        .orderByAsc(LearningTaskRecord::getStatus)
                        .orderByDesc(LearningTaskRecord::getUpdateTime))
                .stream()
                .map(this::toRecordVO)
                .collect(Collectors.toList());
        return Result.success("Query learning task records success", records);
    }

    @Override
    public Result<List<LearningTaskVO>> listMyTasks(String recordStatus, String taskType) {
        Integer studentId = SecurityUtil.getUserId();
        LambdaQueryWrapper<LearningTaskRecord> recordWrapper = new LambdaQueryWrapper<LearningTaskRecord>()
                .eq(LearningTaskRecord::getStudentId, studentId);
        if (StringUtils.hasText(recordStatus)) {
            recordWrapper.eq(LearningTaskRecord::getStatus, normalize(recordStatus));
        }
        recordWrapper.orderByAsc(LearningTaskRecord::getStatus).orderByDesc(LearningTaskRecord::getCreateTime);
        List<LearningTaskRecord> records = learningTaskRecordMapper.selectList(recordWrapper);
        if (records.isEmpty()) {
            return Result.success("Query my learning task success", Collections.emptyList());
        }
        Set<Integer> taskIds = records.stream().map(LearningTaskRecord::getTaskId).collect(Collectors.toSet());
        LambdaQueryWrapper<LearningTask> taskWrapper = new LambdaQueryWrapper<LearningTask>()
                .in(LearningTask::getId, taskIds)
                .eq(LearningTask::getStatus, 1);
        if (StringUtils.hasText(taskType)) {
            taskWrapper.eq(LearningTask::getTaskType, normalize(taskType));
        }
        List<LearningTask> tasks = learningTaskMapper.selectList(taskWrapper);
        return Result.success("Query my learning task success", tasks.stream()
                .map(task -> toTaskVO(task, findRecord(records, task.getId())))
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public Result<String> confirmReviewTask(Integer taskId) {
        LearningTask task = findVisibleStudentTask(taskId);
        if (!"REVIEW".equals(task.getTaskType())) {
            throw new ServiceRuntimeException("Only review task can be confirmed manually");
        }
        completeRecord(taskId, SecurityUtil.getUserId(), ONE_HUNDRED);
        return Result.success("Confirm review task success");
    }

    @Override
    public void syncExamTaskCompleted(Integer studentId, Integer examId) {
        if (studentId == null || examId == null) {
            return;
        }
        learningTaskMapper.selectList(new LambdaQueryWrapper<LearningTask>()
                        .eq(LearningTask::getTaskType, "EXAM")
                        .eq(LearningTask::getRelatedExamId, examId)
                        .eq(LearningTask::getStatus, 1))
                .forEach(task -> completeRecord(task.getId(), studentId, ONE_HUNDRED));
    }

    @Override
    public void syncPracticeTaskProgress(Integer studentId, Integer repoId) {
        if (studentId == null || repoId == null) {
            return;
        }
        BigDecimal progress = calculatePracticeProgress(studentId, repoId);
        learningTaskMapper.selectList(new LambdaQueryWrapper<LearningTask>()
                        .eq(LearningTask::getTaskType, "PRACTICE")
                        .eq(LearningTask::getRelatedPaperId, repoId)
                        .eq(LearningTask::getStatus, 1))
                .forEach(task -> updateRecordProgress(task.getId(), studentId, progress));
    }

    @Override
    public void syncVideoTaskProgress(Integer studentId, Integer videoId, BigDecimal progressRate) {
        if (studentId == null || videoId == null || progressRate == null) {
            return;
        }
        BigDecimal taskProgress = progressRate.compareTo(BigDecimal.valueOf(90)) >= 0 ? ONE_HUNDRED : progressRate;
        learningTaskMapper.selectList(new LambdaQueryWrapper<LearningTask>()
                        .eq(LearningTask::getTaskType, "VIDEO")
                        .eq(LearningTask::getRelatedVideoId, videoId)
                        .eq(LearningTask::getStatus, 1))
                .forEach(task -> updateRecordProgress(task.getId(), studentId, taskProgress));
    }

    @Override
    public void syncWrongQuestionTaskProgress(Integer studentId, Integer examId) {
        if (studentId == null) {
            return;
        }
        LambdaQueryWrapper<LearningTask> wrapper = new LambdaQueryWrapper<LearningTask>()
                .eq(LearningTask::getTaskType, "WRONG_QUESTION")
                .eq(LearningTask::getStatus, 1);
        if (examId != null) {
            wrapper.and(query -> query.eq(LearningTask::getRelatedExamId, examId)
                    .or()
                    .isNull(LearningTask::getRelatedExamId));
        }
        List<LearningTask> tasks = learningTaskMapper.selectList(wrapper);
        for (LearningTask task : tasks) {
            int remain = countWrongQuestions(studentId, task.getRelatedExamId());
            if (remain == 0) {
                completeRecord(task.getId(), studentId, ONE_HUNDRED);
            } else {
                updateRecordProgress(task.getId(), studentId, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }
        }
    }

    private LearningTask buildTaskFromForm(LearningTask task, LearningTaskForm form) {
        String taskType = normalize(form.getTaskType());
        String targetType = normalize(form.getTargetType());
        validateTaskType(taskType, form);
        validateTarget(targetType, form);
        validateCourseScope(form);

        BeanUtils.copyProperties(form, task);
        task.setTaskType(taskType);
        task.setTargetType(targetType);
        if ("VIDEO".equals(taskType) && task.getRelatedVideoId() != null) {
            CourseVideo video = courseVideoMapper.selectById(task.getRelatedVideoId());
            if (video != null) {
                task.setCourseId(video.getCourseId());
                task.setChapterId(video.getChapterId());
            }
        }
        task.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        if (TARGET_CLASS.equals(targetType)) {
            task.setTargetStudentId(null);
        } else {
            task.setTargetClassId(null);
        }
        return task;
    }

    private void validateTaskType(String taskType, LearningTaskForm form) {
        switch (taskType) {
            case "EXAM":
                if (form.getRelatedExamId() == null || examMapper.selectById(form.getRelatedExamId()) == null) {
                    throw new ServiceRuntimeException("Exam task requires a valid exam");
                }
                break;
            case "PRACTICE":
                if (form.getRelatedPaperId() == null || repoMapper.selectById(form.getRelatedPaperId()) == null) {
                    throw new ServiceRuntimeException("Practice task requires a valid question repo");
                }
                break;
            case "VIDEO":
                if (form.getRelatedVideoId() == null || courseVideoMapper.selectById(form.getRelatedVideoId()) == null) {
                    throw new ServiceRuntimeException("Video task requires a valid video");
                }
                break;
            case "WRONG_QUESTION":
            case "REVIEW":
                break;
            default:
                throw new ServiceRuntimeException("Unsupported task type");
        }
    }

    private void validateTarget(String targetType, LearningTaskForm form) {
        if (TARGET_CLASS.equals(targetType)) {
            if (form.getTargetClassId() == null || gradeMapper.selectById(form.getTargetClassId()) == null) {
                throw new ServiceRuntimeException("Class target requires a valid class");
            }
            ensureTeacherCanUseGrade(form.getTargetClassId());
            return;
        }
        if (TARGET_STUDENT.equals(targetType)) {
            User student = form.getTargetStudentId() == null ? null : userMapper.selectById(form.getTargetStudentId());
            if (student == null || !Integer.valueOf(1).equals(student.getRoleId())) {
                throw new ServiceRuntimeException("Student target requires a valid student");
            }
            if (student.getGradeId() != null) {
                ensureTeacherCanUseGrade(student.getGradeId());
            }
            return;
        }
        throw new ServiceRuntimeException("Unsupported target type");
    }

    private void validateCourseScope(LearningTaskForm form) {
        if (form.getCourseId() != null) {
            courseService.getManageableCourse(form.getCourseId());
        }
        if (form.getChapterId() != null) {
            CourseChapter chapter = courseChapterMapper.selectById(form.getChapterId());
            if (chapter == null) {
                throw new ServiceRuntimeException("Chapter does not exist");
            }
            if (form.getCourseId() != null && !form.getCourseId().equals(chapter.getCourseId())) {
                throw new ServiceRuntimeException("Chapter does not belong to this course");
            }
        }
        if (form.getKnowledgePointId() != null) {
            KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(form.getKnowledgePointId());
            if (knowledgePoint == null) {
                throw new ServiceRuntimeException("Knowledge point does not exist");
            }
            if (form.getChapterId() != null && !form.getChapterId().equals(knowledgePoint.getChapterId())) {
                throw new ServiceRuntimeException("Knowledge point does not belong to this chapter");
            }
        }
    }

    private List<User> resolveTargetStudents(LearningTask task) {
        if (TARGET_STUDENT.equals(task.getTargetType())) {
            User student = userMapper.selectById(task.getTargetStudentId());
            return student == null ? Collections.emptyList() : Collections.singletonList(student);
        }
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRoleId, 1)
                .eq(User::getGradeId, task.getTargetClassId())
                .eq(User::getStatus, 1));
    }

    private LearningTask findManageableTask(Integer id) {
        LearningTask task = learningTaskMapper.selectById(id);
        if (task == null) {
            throw new ServiceRuntimeException("Learning task does not exist");
        }
        if (SecurityUtil.getRoleCode() == 3 || task.getPublisherId().equals(SecurityUtil.getUserId())) {
            return task;
        }
        throw new ServiceRuntimeException("No permission to manage this learning task");
    }

    private LearningTask findVisibleStudentTask(Integer id) {
        LearningTask task = learningTaskMapper.selectById(id);
        if (task == null || !Integer.valueOf(1).equals(task.getStatus())) {
            throw new ServiceRuntimeException("Learning task does not exist");
        }
        LearningTaskRecord record = learningTaskRecordMapper.selectOne(new LambdaQueryWrapper<LearningTaskRecord>()
                .eq(LearningTaskRecord::getTaskId, id)
                .eq(LearningTaskRecord::getStudentId, SecurityUtil.getUserId()));
        if (record == null) {
            throw new ServiceRuntimeException("No permission to view this learning task");
        }
        return task;
    }

    private void ensureTeacherCanUseGrade(Integer gradeId) {
        if (SecurityUtil.getRoleCode() == 3) {
            return;
        }
        if (SecurityUtil.getRoleCode() != 2) {
            throw new ServiceRuntimeException("No permission for this class");
        }
        List<Integer> gradeIds = userGradeMapper.getGradeIdListByUserId(SecurityUtil.getUserId());
        if (gradeIds == null || !gradeIds.contains(gradeId)) {
            throw new ServiceRuntimeException("No permission for this class");
        }
    }

    private void completeRecord(Integer taskId, Integer studentId, BigDecimal progressRate) {
        updateRecordProgress(taskId, studentId, progressRate.max(ONE_HUNDRED));
    }

    private void updateRecordProgress(Integer taskId, Integer studentId, BigDecimal progressRate) {
        LearningTaskRecord record = learningTaskRecordMapper.selectOne(new LambdaQueryWrapper<LearningTaskRecord>()
                .eq(LearningTaskRecord::getTaskId, taskId)
                .eq(LearningTaskRecord::getStudentId, studentId));
        if (record == null || STATUS_COMPLETED.equals(record.getStatus())) {
            return;
        }
        BigDecimal normalized = normalizeRate(progressRate);
        String status = normalized.compareTo(ONE_HUNDRED) >= 0 ? STATUS_COMPLETED
                : normalized.compareTo(BigDecimal.ZERO) > 0 ? STATUS_IN_PROGRESS : STATUS_TODO;
        LocalDateTime now = LocalDateTime.now();
        LearningTaskRecord update = new LearningTaskRecord();
        update.setId(record.getId());
        update.setProgressRate(normalized);
        update.setStatus(status);
        update.setUpdateTime(now);
        if (STATUS_COMPLETED.equals(status)) {
            update.setFinishTime(now);
        }
        learningTaskRecordMapper.updateById(update);
    }

    private BigDecimal calculatePracticeProgress(Integer studentId, Integer repoId) {
        int total = questionMapper.selectCount(new LambdaQueryWrapper<Question>()
                .eq(Question::getRepoId, repoId)).intValue();
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        int answered = exerciseRecordMapper.selectCount(new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getUserId, studentId)
                .eq(ExerciseRecord::getRepoId, repoId)).intValue();
        return BigDecimal.valueOf(answered).multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                .min(ONE_HUNDRED);
    }

    private int countWrongQuestions(Integer studentId, Integer examId) {
        LambdaQueryWrapper<UserBook> wrapper = new LambdaQueryWrapper<UserBook>()
                .eq(UserBook::getUserId, studentId);
        if (examId != null) {
            wrapper.eq(UserBook::getExamId, examId);
        }
        return userBookMapper.selectCount(wrapper).intValue();
    }

    private LearningTaskRecord findRecord(List<LearningTaskRecord> records, Integer taskId) {
        return records.stream()
                .filter(record -> taskId.equals(record.getTaskId()))
                .findFirst()
                .orElse(null);
    }

    private LearningTaskVO toTaskVO(LearningTask task) {
        return toTaskVO(task, null);
    }

    private LearningTaskVO toTaskVO(LearningTask task, LearningTaskRecord record) {
        LearningTaskVO vo = new LearningTaskVO();
        BeanUtils.copyProperties(task, vo);
        vo.setTaskTypeName(taskTypeName(task.getTaskType()));
        vo.setTargetTypeName(TARGET_CLASS.equals(task.getTargetType()) ? "班级" : "学生");
        fillNames(task, vo);
        if (record != null) {
            vo.setRecordStatus(record.getStatus());
            vo.setProgressRate(record.getProgressRate());
            vo.setFinishTime(record.getFinishTime());
        }
        List<LearningTaskRecord> records = learningTaskRecordMapper.selectList(new LambdaQueryWrapper<LearningTaskRecord>()
                .eq(LearningTaskRecord::getTaskId, task.getId()));
        int total = records.size();
        int completed = (int) records.stream().filter(item -> STATUS_COMPLETED.equals(item.getStatus())).count();
        vo.setTotalCount(total);
        vo.setCompletedCount(completed);
        vo.setCompletionRate(total == 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(completed).multiply(ONE_HUNDRED).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        return vo;
    }

    private LearningTaskRecordVO toRecordVO(LearningTaskRecord record) {
        LearningTaskRecordVO vo = new LearningTaskRecordVO();
        BeanUtils.copyProperties(record, vo);
        LearningTask task = learningTaskMapper.selectById(record.getTaskId());
        if (task != null) {
            vo.setTaskTitle(task.getTitle());
        }
        User user = userMapper.selectById(record.getStudentId());
        if (user != null) {
            vo.setStudentName(displayUserName(user));
        }
        vo.setStatusName(recordStatusName(record.getStatus()));
        return vo;
    }

    private void fillNames(LearningTask task, LearningTaskVO vo) {
        Course course = task.getCourseId() == null ? null : courseMapper.selectById(task.getCourseId());
        if (course != null) vo.setCourseName(course.getName());
        CourseChapter chapter = task.getChapterId() == null ? null : courseChapterMapper.selectById(task.getChapterId());
        if (chapter != null) vo.setChapterTitle(chapter.getTitle());
        KnowledgePoint knowledgePoint = task.getKnowledgePointId() == null ? null : knowledgePointMapper.selectById(task.getKnowledgePointId());
        if (knowledgePoint != null) vo.setKnowledgePointName(knowledgePoint.getName());
        Grade grade = task.getTargetClassId() == null ? null : gradeMapper.selectById(task.getTargetClassId());
        if (grade != null) vo.setTargetClassName(grade.getGradeName());
        User student = task.getTargetStudentId() == null ? null : userMapper.selectById(task.getTargetStudentId());
        if (student != null) vo.setTargetStudentName(displayUserName(student));
        Exam exam = task.getRelatedExamId() == null ? null : examMapper.selectById(task.getRelatedExamId());
        if (exam != null) vo.setRelatedExamTitle(exam.getTitle());
        Repo repo = task.getRelatedPaperId() == null ? null : repoMapper.selectById(task.getRelatedPaperId());
        if (repo != null) vo.setRelatedPaperTitle(repo.getTitle());
        CourseVideo video = task.getRelatedVideoId() == null ? null : courseVideoMapper.selectById(task.getRelatedVideoId());
        if (video != null) vo.setRelatedVideoTitle(video.getTitle());
        User publisher = task.getPublisherId() == null ? null : userMapper.selectById(task.getPublisherId());
        if (publisher != null) vo.setPublisherName(displayUserName(publisher));
    }

    private String displayUserName(User user) {
        return StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUserName();
    }

    private BigDecimal normalizeRate(BigDecimal rate) {
        if (rate == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal normalized = rate.max(BigDecimal.ZERO).min(ONE_HUNDRED);
        return normalized.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String taskTypeName(String taskType) {
        if ("EXAM".equals(taskType)) return "考试任务";
        if ("PRACTICE".equals(taskType)) return "练习任务";
        if ("VIDEO".equals(taskType)) return "网课任务";
        if ("WRONG_QUESTION".equals(taskType)) return "错题订正任务";
        if ("REVIEW".equals(taskType)) return "复习任务";
        return taskType;
    }

    private String recordStatusName(String status) {
        if (STATUS_COMPLETED.equals(status)) return "已完成";
        if (STATUS_IN_PROGRESS.equals(status)) return "进行中";
        return "待完成";
    }
}
