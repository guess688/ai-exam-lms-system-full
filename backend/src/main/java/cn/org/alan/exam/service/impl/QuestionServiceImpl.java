package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.QuestionConverter;
import cn.org.alan.exam.mapper.CourseChapterMapper;
import cn.org.alan.exam.mapper.ExerciseRecordMapper;
import cn.org.alan.exam.mapper.KnowledgePointMapper;
import cn.org.alan.exam.mapper.OptionMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.model.entity.CourseChapter;
import cn.org.alan.exam.model.entity.ExerciseRecord;
import cn.org.alan.exam.model.entity.KnowledgePoint;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.form.question.QuestionExcelFrom;
import cn.org.alan.exam.model.form.question.QuestionFrom;
import cn.org.alan.exam.model.vo.question.QuestionVO;
import cn.org.alan.exam.service.ICourseService;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.utils.SecurityUtil;
import cn.org.alan.exam.utils.excel.ExcelUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    private static final Set<String> ALLOWED_DIFFICULTIES = new HashSet<>(Arrays.asList("EASY", "MEDIUM", "HARD"));

    @Resource
    private QuestionConverter questionConverter;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private OptionMapper optionMapper;
    @Resource
    private ExerciseRecordMapper exerciseRecordMapper;
    @Resource
    private ICourseService courseService;
    @Resource
    private CourseChapterMapper courseChapterMapper;
    @Resource
    private KnowledgePointMapper knowledgePointMapper;

    @Override
    @Transactional
    public Result<String> addSingleQuestion(QuestionFrom questionFrom) {
        String validationError = validateQuestionForm(questionFrom, true);
        if (validationError != null) {
            return Result.failed(validationError);
        }

        List<Option> options = questionFrom.getOptions();
        Question question = questionConverter.fromToEntity(questionFrom);
        questionMapper.insert(question);

        options.forEach(option -> option.setQuId(question.getId()));
        optionMapper.insertBatch(options);
        return Result.success("Add question success");
    }

    @Override
    @Transactional
    public Result<String> deleteBatchByIds(String ids) {
        List<Integer> qIdList = Arrays.stream(ids.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        LambdaUpdateWrapper<ExerciseRecord> updateWrapper = new LambdaUpdateWrapper<ExerciseRecord>()
                .in(ExerciseRecord::getQuestionId, qIdList);
        exerciseRecordMapper.delete(updateWrapper);
        optionMapper.deleteBatchIds(qIdList);
        questionMapper.deleteBatchIds(qIdList);
        return Result.success("Delete question success");
    }

    @Override
    public Result<IPage<QuestionVO>> pagingQuestion(Integer pageNum, Integer pageSize, String title, Integer type, Integer repoId,
                                                    Integer courseId, Integer chapterId, Integer knowledgePointId, String difficulty) {
        IPage<QuestionVO> page = new Page<>(pageNum, pageSize);
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        page = questionMapper.selectQuestionPage(page, userId, roleCode, title, type, repoId,
                courseId, chapterId, knowledgePointId, normalizeDifficulty(difficulty, false));
        return Result.success("Query question success", page);
    }

    @Override
    public Result<QuestionVO> querySingle(Integer id) {
        QuestionVO result = questionMapper.selectSingle(id);
        return Result.success("Query question success", result);
    }

    @Override
    @Transactional
    public Result<String> updateQuestion(QuestionFrom questionFrom) {
        String validationError = validateQuestionForm(questionFrom, true);
        if (validationError != null) {
            return Result.failed(validationError);
        }

        Question question = questionConverter.fromToEntity(questionFrom);
        questionMapper.updateById(question);

        for (Option option : questionFrom.getOptions()) {
            option.setQuId(question.getId());
            if (option.getId() == null) {
                optionMapper.insert(option);
            } else {
                optionMapper.updateById(option);
            }
        }
        return Result.success("Update question success");
    }

    @SneakyThrows(Exception.class)
    @Override
    @Transactional
    public Result<String> importQuestion(Integer id, MultipartFile file) {
        if (!ExcelUtils.isExcel(Objects.requireNonNull(file.getOriginalFilename()))) {
            throw new ServiceRuntimeException("File is not a valid Excel file");
        }

        try {
            List<QuestionExcelFrom> questionExcelFroms = ExcelUtils.readMultipartFile(file, QuestionExcelFrom.class);
            List<QuestionFrom> list = QuestionExcelFrom.converterQuestionFrom(questionExcelFroms);

            for (QuestionFrom questionFrom : list) {
                Question question = questionConverter.fromToEntity(questionFrom);
                question.setRepoId(id);
                question.setDifficulty(normalizeDifficulty(questionFrom.getDifficulty(), true));
                questionMapper.insert(question);

                List<Option> options = questionFrom.getOptions();
                final int[] count = {0};
                options.forEach(option -> {
                    option.setSort(++count[0]);
                    option.setQuId(question.getId());
                });
                if (!options.isEmpty()) {
                    optionMapper.insertBatch(options);
                }
            }

            return Result.success("Import question success");
        } catch (ServiceRuntimeException e) {
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            return Result.failed("Import question failed: " + e.getMessage());
        }
    }

    private String validateQuestionForm(QuestionFrom questionFrom, boolean requireCourseBinding) {
        String difficulty = normalizeDifficulty(questionFrom.getDifficulty(), false);
        if (difficulty == null) {
            return "Question difficulty must be EASY, MEDIUM or HARD";
        }
        questionFrom.setDifficulty(difficulty);

        String bindingError = validateCourseBinding(questionFrom, requireCourseBinding);
        if (bindingError != null) {
            return bindingError;
        }

        return validateOptionRules(questionFrom);
    }

    private String validateCourseBinding(QuestionFrom questionFrom, boolean required) {
        Integer courseId = questionFrom.getCourseId();
        Integer chapterId = questionFrom.getChapterId();
        Integer knowledgePointId = questionFrom.getKnowledgePointId();
        boolean allEmpty = courseId == null && chapterId == null && knowledgePointId == null;
        if (allEmpty && !required) {
            return null;
        }
        if (courseId == null || chapterId == null || knowledgePointId == null) {
            return "Question must bind course, chapter and knowledge point";
        }

        courseService.getManageableCourse(courseId);
        CourseChapter chapter = courseChapterMapper.selectById(chapterId);
        if (chapter == null || !courseId.equals(chapter.getCourseId())) {
            return "Chapter does not belong to selected course";
        }

        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(knowledgePointId);
        if (knowledgePoint == null
                || !courseId.equals(knowledgePoint.getCourseId())
                || !chapterId.equals(knowledgePoint.getChapterId())) {
            return "Knowledge point does not belong to selected course chapter";
        }
        return null;
    }

    private String validateOptionRules(QuestionFrom questionFrom) {
        Integer quType = questionFrom.getQuType();
        List<Option> options = activeOptions(questionFrom.getOptions());
        if (quType == null) {
            return "Question type is required";
        }
        if (options.size() < 2) {
            return "Objective question must have at least two options";
        }

        long rightCount = options.stream().filter(this::isRightOption).count();
        if (quType == 1 && rightCount != 1) {
            return "Single choice question must have exactly one right answer";
        }
        if (quType == 2 && rightCount < 1) {
            return "Multiple choice question must have at least one right answer";
        }
        if (quType == 3 && rightCount != 1) {
            return "True or false question must have exactly one right answer";
        }
        if (quType == 4 && rightCount < 1) {
            return "Indefinite choice question must have at least one right answer";
        }
        return null;
    }

    private List<Option> activeOptions(List<Option> options) {
        if (options == null) {
            return Collections.emptyList();
        }
        return options.stream()
                .filter(option -> !Integer.valueOf(1).equals(option.getIsDeleted()))
                .collect(Collectors.toList());
    }

    private boolean isRightOption(Option option) {
        return Integer.valueOf(1).equals(option.getIsRight());
    }

    private String normalizeDifficulty(String difficulty, boolean defaultToMedium) {
        if (difficulty == null || difficulty.trim().isEmpty()) {
            return defaultToMedium ? "MEDIUM" : null;
        }
        String normalized = difficulty.trim().toUpperCase(Locale.ROOT);
        return ALLOWED_DIFFICULTIES.contains(normalized) ? normalized : null;
    }
}
