package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.LearningTask;
import cn.org.alan.exam.model.form.task.LearningTaskForm;
import cn.org.alan.exam.model.vo.task.LearningTaskRecordVO;
import cn.org.alan.exam.model.vo.task.LearningTaskVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface ILearningTaskService extends IService<LearningTask> {

    Result<String> createTask(LearningTaskForm form);

    Result<String> updateTask(Integer id, LearningTaskForm form);

    Result<String> disableTask(Integer id);

    Result<IPage<LearningTaskVO>> pageTasks(Integer pageNum, Integer pageSize, String title, String taskType, Integer status);

    Result<List<LearningTaskRecordVO>> listTaskRecords(Integer taskId);

    Result<List<LearningTaskVO>> listMyTasks(String recordStatus, String taskType);

    Result<String> confirmReviewTask(Integer taskId);

    void syncExamTaskCompleted(Integer studentId, Integer examId);

    void syncPracticeTaskProgress(Integer studentId, Integer repoId);

    void syncVideoTaskProgress(Integer studentId, Integer videoId, BigDecimal progressRate);

    void syncWrongQuestionTaskProgress(Integer studentId, Integer examId);
}
