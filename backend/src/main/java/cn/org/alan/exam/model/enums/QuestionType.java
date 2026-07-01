package cn.org.alan.exam.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionType {
    SINGLE_CHOICE_QUESTIONS(1, "单选题"),
    MULTIPLE_CHOICE_QUESTIONS(2, "多选题"),
    TRUE_OR_FALSE_QUESTIONS(3, "判断题"),
    INDEFINITE_CHOICE_QUESTIONS(4, "不定项选择题");

    private final int code;
    private final String desc;
}
