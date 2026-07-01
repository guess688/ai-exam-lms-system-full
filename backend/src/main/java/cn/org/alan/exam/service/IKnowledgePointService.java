package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.KnowledgePoint;
import cn.org.alan.exam.model.form.course.KnowledgePointForm;
import cn.org.alan.exam.model.vo.course.KnowledgePointVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IKnowledgePointService extends IService<KnowledgePoint> {

    Result<String> addKnowledgePoint(KnowledgePointForm form);

    Result<String> updateKnowledgePoint(Integer id, KnowledgePointForm form);

    Result<String> disableKnowledgePoint(Integer id);

    Result<List<KnowledgePointVO>> listByChapter(Integer chapterId, Integer status);
}
