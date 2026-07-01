package cn.org.alan.exam.service.impl;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class AiPromptManager {

    private static final String PROMPT_PATH = "classpath:prompts/";

    private static final Map<String, String> DEFAULT_PROMPTS = new HashMap<>();

    static {
        DEFAULT_PROMPTS.put("student_report_prompt", "请基于输入的考试、章节、知识点和学习行为数据，生成学生个人学情报告，输出 JSON。");
        DEFAULT_PROMPTS.put("class_report_prompt", "请基于输入的班级考试和掌握度数据，生成班级学情报告，输出 JSON。");
        DEFAULT_PROMPTS.put("teaching_plan_prompt", "请基于输入的课程、章节、薄弱点和教学目标，生成教学文案，输出 JSON。");
        DEFAULT_PROMPTS.put("paper_review_prompt", "请基于输入的试卷、错题和难度表现数据，生成试卷讲评稿，输出 JSON。");
        DEFAULT_PROMPTS.put("question_generation_prompt", "请基于输入的课程、章节、知识点、题型、难度和数量要求生成题目，输出 JSON。");
        DEFAULT_PROMPTS.put("practice_recommend_prompt", "请基于输入的学生薄弱知识点和题库范围，推荐练习方案，输出 JSON。");
        DEFAULT_PROMPTS.put("learning_behavior_prompt", "请基于输入的学习任务、视频进度和考试表现，评价学习行为，输出 JSON。");
    }

    @Resource
    private ResourceLoader resourceLoader;

    public String getPrompt(String promptKey) {
        org.springframework.core.io.Resource resource = resourceLoader.getResource(PROMPT_PATH + promptKey + ".txt");
        if (!resource.exists()) {
            return DEFAULT_PROMPTS.get(promptKey);
        }
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return DEFAULT_PROMPTS.get(promptKey);
        }
    }
}
