USE db_exam;

-- Customer demo data for AI 智能考试与网课学情分析系统.
-- Run after all schema migrations in backend/sql.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @demo_password := '$2a$10$/ZdKFY15AWNLOeTqAp91a.uDa0JDioj1wVYGgpn.HKMYh9vq0Uh4S';

START TRANSACTION;

-- Keep built-in accounts usable for demos.
UPDATE `t_user`
SET `password` = @demo_password, `status` = 1, `is_deleted` = 0
WHERE `user_name` IN ('admin', 'teacher', 'student');

INSERT INTO `t_role` (`id`, `role_name`, `code`) VALUES
(1, '学生', 'student'),
(2, '教师', 'teacher'),
(3, '管理员', 'admin')
ON DUPLICATE KEY UPDATE
  `role_name` = VALUES(`role_name`),
  `code` = VALUES(`code`);

INSERT INTO `t_user` (`id`, `user_name`, `real_name`, `password`, `avatar`, `role_id`, `grade_id`, `create_time`, `status`, `is_deleted`) VALUES
(9001, 'demo_admin', '演示管理员', @demo_password, NULL, 3, NULL, NOW(), 1, 0),
(9002, 'demo_teacher', '演示教师', @demo_password, NULL, 2, NULL, NOW(), 1, 0),
(9003, 'demo_student', '演示学生', @demo_password, NULL, 1, 9001, NOW(), 1, 0),
(9004, 'demo_student2', '演示学生二', @demo_password, NULL, 1, 9001, NOW(), 1, 0)
ON DUPLICATE KEY UPDATE
  `real_name` = VALUES(`real_name`),
  `password` = VALUES(`password`),
  `role_id` = VALUES(`role_id`),
  `grade_id` = VALUES(`grade_id`),
  `status` = 1,
  `is_deleted` = 0;

INSERT INTO `t_grade` (`id`, `grade_name`, `user_id`, `code`, `is_deleted`) VALUES
(9001, 'AI 演示班', 9002, 'AI-DEMO-CLASS', 0)
ON DUPLICATE KEY UPDATE
  `grade_name` = VALUES(`grade_name`),
  `user_id` = VALUES(`user_id`),
  `code` = VALUES(`code`),
  `is_deleted` = 0;

INSERT INTO `t_user_grade` (`id`, `u_id`, `g_id`, `is_deleted`) VALUES
(9001, 9002, 9001, 0),
(9002, 163, 9001, 0)
ON DUPLICATE KEY UPDATE
  `is_deleted` = 0;

INSERT INTO `t_category` (`id`, `name`, `parent_id`, `sort`, `create_time`, `is_deleted`) VALUES
(9001, 'AI 演示题库分类', 0, 99, NOW(), 0)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `sort` = VALUES(`sort`),
  `is_deleted` = 0;

INSERT INTO `t_repo` (`id`, `user_id`, `title`, `category_id`, `create_time`, `is_deleted`, `is_exercise`) VALUES
(9001, 9002, 'AI 演示题库', 9001, NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `category_id` = VALUES(`category_id`),
  `is_deleted` = 0,
  `is_exercise` = 0;

INSERT INTO `t_course` (`id`, `name`, `description`, `cover_url`, `teacher_id`, `grade_id`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(9001, '初中数学 AI 演示课', '用于演示课程、章节、知识点、题库、网课和 AI 学情分析的样例课程。', 'https://example.com/demo-course-cover.png', 9002, 9001, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `description` = VALUES(`description`),
  `teacher_id` = VALUES(`teacher_id`),
  `grade_id` = VALUES(`grade_id`),
  `status` = 1,
  `update_time` = NOW(),
  `is_deleted` = 0;

INSERT INTO `t_course_chapter` (`id`, `course_id`, `title`, `description`, `sort_order`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(9001, 9001, '第一章 一元一次方程', '方程概念、等式性质和基础求解。', 1, 1, NOW(), NOW(), 0),
(9002, 9001, '第二章 几何初步', '线段、角度、平行线和基本推理。', 2, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `description` = VALUES(`description`),
  `sort_order` = VALUES(`sort_order`),
  `status` = 1,
  `update_time` = NOW(),
  `is_deleted` = 0;

INSERT INTO `t_knowledge_point` (`id`, `course_id`, `chapter_id`, `name`, `description`, `sort_order`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(9001, 9001, 9001, '等式性质', '利用等式两边同加、同减、同乘、同除非零数保持等式成立。', 1, 1, NOW(), NOW(), 0),
(9002, 9001, 9001, '一元一次方程求解', '移项、合并同类项、系数化为 1。', 2, 1, NOW(), NOW(), 0),
(9003, 9001, 9002, '平行线性质', '同位角、内错角、同旁内角的基本关系。', 1, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `description` = VALUES(`description`),
  `sort_order` = VALUES(`sort_order`),
  `status` = 1,
  `update_time` = NOW(),
  `is_deleted` = 0;

INSERT INTO `t_question` (`id`, `qu_type`, `image`, `content`, `create_time`, `analysis`, `repo_id`, `course_id`, `chapter_id`, `knowledge_point_id`, `difficulty`, `user_id`, `is_deleted`) VALUES
(9001, '1', NULL, '等式两边同时加上同一个数，等式是否仍然成立？', NOW(), '等式两边进行相同的加法变形，等式仍成立。', 9001, 9001, 9001, 9001, 'EASY', 9002, 0),
(9002, '1', NULL, '方程 2x + 3 = 11 的解是？', NOW(), '移项得 2x = 8，因此 x = 4。', 9001, 9001, 9001, 9002, 'MEDIUM', 9002, 0),
(9003, '2', NULL, '下列哪些步骤可用于解一元一次方程？', NOW(), '移项、合并同类项、系数化为 1 都是一元一次方程常用步骤。', 9001, 9001, 9001, 9002, 'MEDIUM', 9002, 0),
(9004, '3', NULL, '若 a = b，则 a + 5 = b + 5。', NOW(), '等式两边同加 5，等式仍成立。', 9001, 9001, 9001, 9001, 'EASY', 9002, 0),
(9005, '4', NULL, '两条平行直线被第三条直线所截时，下列说法一定正确的是？', NOW(), '平行线被截时，同位角相等、内错角相等，同旁内角互补。', 9001, 9001, 9002, 9003, 'HARD', 9002, 0),
(9006, '1', NULL, '若两直线平行，同旁内角之和为多少度？', NOW(), '两直线平行时，同旁内角互补，和为 180 度。', 9001, 9001, 9002, 9003, 'HARD', 9002, 0)
ON DUPLICATE KEY UPDATE
  `qu_type` = VALUES(`qu_type`),
  `content` = VALUES(`content`),
  `analysis` = VALUES(`analysis`),
  `repo_id` = VALUES(`repo_id`),
  `course_id` = VALUES(`course_id`),
  `chapter_id` = VALUES(`chapter_id`),
  `knowledge_point_id` = VALUES(`knowledge_point_id`),
  `difficulty` = VALUES(`difficulty`),
  `user_id` = VALUES(`user_id`),
  `is_deleted` = 0;

INSERT INTO `t_option` (`id`, `qu_id`, `is_right`, `image`, `content`, `sort`, `is_deleted`) VALUES
(9101, 9001, 1, NULL, '成立', 1, 0),
(9102, 9001, 0, NULL, '不成立', 2, 0),
(9103, 9001, 0, NULL, '只在整数范围内成立', 3, 0),
(9104, 9001, 0, NULL, '无法判断', 4, 0),
(9105, 9002, 0, NULL, '3', 1, 0),
(9106, 9002, 1, NULL, '4', 2, 0),
(9107, 9002, 0, NULL, '5', 3, 0),
(9108, 9002, 0, NULL, '7', 4, 0),
(9109, 9003, 1, NULL, '移项', 1, 0),
(9110, 9003, 1, NULL, '合并同类项', 2, 0),
(9111, 9003, 1, NULL, '系数化为 1', 3, 0),
(9112, 9003, 0, NULL, '随意改变未知数取值', 4, 0),
(9113, 9004, 1, NULL, '正确', 1, 0),
(9114, 9004, 0, NULL, '错误', 2, 0),
(9115, 9005, 1, NULL, '同位角相等', 1, 0),
(9116, 9005, 1, NULL, '内错角相等', 2, 0),
(9117, 9005, 1, NULL, '同旁内角互补', 3, 0),
(9118, 9005, 0, NULL, '任意两个角都相等', 4, 0),
(9119, 9006, 0, NULL, '90 度', 1, 0),
(9120, 9006, 0, NULL, '120 度', 2, 0),
(9121, 9006, 1, NULL, '180 度', 3, 0),
(9122, 9006, 0, NULL, '360 度', 4, 0)
ON DUPLICATE KEY UPDATE
  `qu_id` = VALUES(`qu_id`),
  `is_right` = VALUES(`is_right`),
  `content` = VALUES(`content`),
  `sort` = VALUES(`sort`),
  `is_deleted` = 0;

INSERT INTO `t_exam` (`id`, `title`, `exam_duration`, `passed_score`, `gross_score`, `max_count`, `user_id`, `certificate_id`, `radio_count`, `radio_score`, `multi_count`, `multi_score`, `judge_count`, `judge_score`, `saq_count`, `saq_score`, `start_time`, `end_time`, `create_time`, `is_deleted`) VALUES
(9001, 'AI 演示章节测验', 60, 36, 60, 5, 9002, NULL, 3, 1000, 1, 1000, 1, 1000, 0, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), 0),
(9002, 'AI 演示巩固测验', 45, 36, 60, 5, 9002, NULL, 3, 1000, 1, 1000, 1, 1000, 0, 0, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), NOW(), 0)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `exam_duration` = VALUES(`exam_duration`),
  `gross_score` = VALUES(`gross_score`),
  `user_id` = VALUES(`user_id`),
  `start_time` = VALUES(`start_time`),
  `end_time` = VALUES(`end_time`),
  `is_deleted` = 0;

INSERT INTO `t_exam_grade` (`id`, `exam_id`, `grade_id`) VALUES
(9001, 9001, 9001),
(9002, 9002, 9001)
ON DUPLICATE KEY UPDATE
  `exam_id` = VALUES(`exam_id`),
  `grade_id` = VALUES(`grade_id`);

INSERT INTO `t_exam_repo` (`id`, `exam_id`, `repo_id`) VALUES
(9001, 9001, 9001),
(9002, 9002, 9001)
ON DUPLICATE KEY UPDATE
  `exam_id` = VALUES(`exam_id`),
  `repo_id` = VALUES(`repo_id`);

INSERT INTO `t_exam_question` (`id`, `exam_id`, `question_id`, `score`, `sort`, `type`) VALUES
(9001, 9001, 9001, 10, 1, 1),
(9002, 9001, 9002, 10, 2, 1),
(9003, 9001, 9003, 10, 3, 2),
(9004, 9001, 9004, 10, 4, 3),
(9005, 9001, 9005, 10, 5, 4),
(9006, 9001, 9006, 10, 6, 1),
(9011, 9002, 9001, 10, 1, 1),
(9012, 9002, 9002, 10, 2, 1),
(9013, 9002, 9003, 10, 3, 2),
(9014, 9002, 9004, 10, 4, 3),
(9015, 9002, 9005, 10, 5, 4),
(9016, 9002, 9006, 10, 6, 1)
ON DUPLICATE KEY UPDATE
  `exam_id` = VALUES(`exam_id`),
  `question_id` = VALUES(`question_id`),
  `score` = VALUES(`score`),
  `sort` = VALUES(`sort`),
  `type` = VALUES(`type`);

INSERT INTO `t_exam_qu_answer` (`id`, `user_id`, `exam_id`, `question_id`, `question_type`, `answer_id`, `answer_content`, `checkout`, `is_sign`, `is_right`, `ai_score`, `ai_reason`) VALUES
(9001, 9003, 9001, 9001, 1, '9101', NULL, NULL, 0, 1, NULL, NULL),
(9002, 9003, 9001, 9002, 1, '9105', NULL, NULL, 0, 0, NULL, NULL),
(9003, 9003, 9001, 9003, 2, '9109,9110,9111', NULL, NULL, 0, 1, NULL, NULL),
(9004, 9003, 9001, 9004, 3, '9113', NULL, NULL, 0, 1, NULL, NULL),
(9005, 9003, 9001, 9005, 4, '9115,9116', NULL, NULL, 0, 0, NULL, NULL),
(9006, 9003, 9001, 9006, 1, '9120', NULL, NULL, 0, 0, NULL, NULL),
(9011, 9004, 9001, 9001, 1, '9101', NULL, NULL, 0, 1, NULL, NULL),
(9012, 9004, 9001, 9002, 1, '9106', NULL, NULL, 0, 1, NULL, NULL),
(9013, 9004, 9001, 9003, 2, '9109,9110,9111', NULL, NULL, 0, 1, NULL, NULL),
(9014, 9004, 9001, 9004, 3, '9113', NULL, NULL, 0, 1, NULL, NULL),
(9015, 9004, 9001, 9005, 4, '9115,9116,9117', NULL, NULL, 0, 1, NULL, NULL),
(9016, 9004, 9001, 9006, 1, '9120', NULL, NULL, 0, 0, NULL, NULL)
ON DUPLICATE KEY UPDATE
  `answer_id` = VALUES(`answer_id`),
  `is_right` = VALUES(`is_right`),
  `question_type` = VALUES(`question_type`);

INSERT INTO `t_user_exams_score` (`id`, `user_id`, `exam_id`, `total_time`, `user_time`, `user_score`, `limit_time`, `count`, `state`, `create_time`, `whether_mark`) VALUES
(9001, 9003, 9001, 60, 38, 30, NOW(), 0, 1, NOW(), -1),
(9002, 9004, 9001, 60, 34, 50, NOW(), 0, 1, NOW(), -1),
(9003, 9003, 9002, 45, 31, 46, DATE_SUB(NOW(), INTERVAL 5 DAY), 0, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), -1)
ON DUPLICATE KEY UPDATE
  `total_time` = VALUES(`total_time`),
  `user_time` = VALUES(`user_time`),
  `user_score` = VALUES(`user_score`),
  `limit_time` = VALUES(`limit_time`),
  `state` = VALUES(`state`),
  `whether_mark` = VALUES(`whether_mark`);

INSERT INTO `t_user_book` (`id`, `exam_id`, `user_id`, `qu_id`, `create_time`) VALUES
(9001, 9001, 9003, 9002, NOW()),
(9002, 9001, 9003, 9005, NOW()),
(9003, 9001, 9003, 9006, NOW()),
(9004, 9001, 9004, 9006, NOW())
ON DUPLICATE KEY UPDATE
  `exam_id` = VALUES(`exam_id`),
  `user_id` = VALUES(`user_id`),
  `qu_id` = VALUES(`qu_id`),
  `create_time` = VALUES(`create_time`);

INSERT INTO `t_course_video` (`id`, `course_id`, `chapter_id`, `title`, `description`, `video_url`, `duration`, `cover_url`, `sort_order`, `status`, `create_time`, `update_time`, `is_deleted`) VALUES
(9001, 9001, 9001, '等式性质精讲', '演示学生补学等式性质。', 'https://example.com/videos/equation-basic.mp4', 720, 'https://example.com/videos/equation-basic.png', 1, 1, NOW(), NOW(), 0),
(9002, 9001, 9002, '平行线性质突破', '演示薄弱章节的视频学习任务。', 'https://example.com/videos/parallel-lines.mp4', 900, 'https://example.com/videos/parallel-lines.png', 1, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `description` = VALUES(`description`),
  `video_url` = VALUES(`video_url`),
  `duration` = VALUES(`duration`),
  `status` = 1,
  `update_time` = NOW(),
  `is_deleted` = 0;

INSERT INTO `t_course_material` (`id`, `course_id`, `chapter_id`, `title`, `file_url`, `file_type`, `sort_order`, `create_time`, `update_time`, `is_deleted`) VALUES
(9001, 9001, 9001, '一元一次方程复习讲义', 'https://example.com/materials/equation-review.pdf', 'pdf', 1, NOW(), NOW(), 0),
(9002, 9001, 9002, '平行线性质练习清单', 'https://example.com/materials/parallel-lines.pdf', 'pdf', 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `file_url` = VALUES(`file_url`),
  `file_type` = VALUES(`file_type`),
  `update_time` = NOW(),
  `is_deleted` = 0;

INSERT INTO `t_student_video_progress` (`id`, `student_id`, `course_id`, `chapter_id`, `video_id`, `watched_seconds`, `duration`, `progress_rate`, `completed`, `last_watch_time`, `create_time`, `update_time`) VALUES
(9001, 9003, 9001, 9001, 9001, 700, 720, 97.22, 1, NOW(), NOW(), NOW()),
(9002, 9003, 9001, 9002, 9002, 360, 900, 40.00, 0, NOW(), NOW(), NOW()),
(9003, 9004, 9001, 9001, 9001, 720, 720, 100.00, 1, NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `watched_seconds` = VALUES(`watched_seconds`),
  `duration` = VALUES(`duration`),
  `progress_rate` = VALUES(`progress_rate`),
  `completed` = VALUES(`completed`),
  `last_watch_time` = VALUES(`last_watch_time`),
  `update_time` = NOW();

INSERT INTO `t_learning_task` (`id`, `title`, `description`, `task_type`, `course_id`, `chapter_id`, `knowledge_point_id`, `target_type`, `target_class_id`, `target_student_id`, `related_exam_id`, `related_paper_id`, `related_video_id`, `deadline`, `publisher_id`, `status`, `create_time`, `update_time`) VALUES
(9001, '完成 AI 演示章节测验', '请在截止时间前完成章节测验。', 'EXAM', 9001, 9001, NULL, 'CLASS', 9001, NULL, 9001, NULL, NULL, DATE_ADD(NOW(), INTERVAL 7 DAY), 9002, 1, NOW(), NOW()),
(9002, '观看平行线性质突破视频', '结合错题情况完成章节补学视频。', 'VIDEO', 9001, 9002, 9003, 'CLASS', 9001, NULL, NULL, NULL, 9002, DATE_ADD(NOW(), INTERVAL 5 DAY), 9002, 1, NOW(), NOW()),
(9003, '等式性质错题订正', '完成错题本中的相关错题订正。', 'WRONG_QUESTION', 9001, 9001, 9001, 'STUDENT', NULL, 9003, NULL, 9001, NULL, DATE_ADD(NOW(), INTERVAL 3 DAY), 9002, 1, NOW(), NOW()),
(9004, 'AI 推荐练习：平行线性质', '根据薄弱知识点推荐 3 道中低难度练习。', 'PRACTICE', 9001, 9002, 9003, 'CLASS', 9001, NULL, NULL, 9001, NULL, DATE_ADD(NOW(), INTERVAL 4 DAY), 9002, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `description` = VALUES(`description`),
  `task_type` = VALUES(`task_type`),
  `deadline` = VALUES(`deadline`),
  `publisher_id` = VALUES(`publisher_id`),
  `status` = 1,
  `update_time` = NOW();

INSERT INTO `t_learning_task_record` (`id`, `task_id`, `student_id`, `status`, `progress_rate`, `finish_time`, `create_time`, `update_time`) VALUES
(9001, 9001, 9003, 'COMPLETED', 100.00, NOW(), NOW(), NOW()),
(9002, 9002, 9003, 'IN_PROGRESS', 40.00, NULL, NOW(), NOW()),
(9003, 9003, 9003, 'TODO', 0.00, NULL, NOW(), NOW()),
(9004, 9004, 9003, 'TODO', 0.00, NULL, NOW(), NOW()),
(9011, 9001, 9004, 'COMPLETED', 100.00, NOW(), NOW(), NOW()),
(9012, 9002, 9004, 'COMPLETED', 100.00, NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `status` = VALUES(`status`),
  `progress_rate` = VALUES(`progress_rate`),
  `finish_time` = VALUES(`finish_time`),
  `update_time` = NOW();

INSERT INTO `t_ai_call_log` (`id`, `feature_type`, `provider`, `model`, `mock_enabled`, `success`, `duration_ms`, `request_time`, `request_summary`, `prompt_template`, `raw_response`, `parsed_result`, `error_message`, `create_time`, `update_time`) VALUES
(9001, 'STUDENT_REPORT', 'mock', 'mock-model', 1, 1, 120, NOW(), 'demo student report input', 'student_report_prompt', '{"summary":"mock"}', '{"overall":"基础题较稳定，平行线性质需要加强。"}', NULL, NOW(), NOW()),
(9002, 'CLASS_REPORT', 'mock', 'mock-model', 1, 1, 130, NOW(), 'demo class report input', 'class_report_prompt', '{"summary":"mock"}', '{"overall":"班级基础掌握较好，困难题区分度明显。"}', NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `request_time` = VALUES(`request_time`),
  `success` = VALUES(`success`),
  `parsed_result` = VALUES(`parsed_result`),
  `update_time` = NOW();

INSERT INTO `ai_report` (`id`, `report_type`, `student_id`, `class_id`, `exam_id`, `title`, `input_json`, `output_text`, `output_json`, `create_time`) VALUES
(9001, 'STUDENT_REPORT', 9003, 9001, 9001, '演示学生个人学情报告', '{"student":"demo_student","exam":"AI 演示章节测验","score":30,"classAverage":40}', '本次考试基础章节掌握较稳定，平行线性质和困难题表现需要继续巩固。建议先复盘错题，再观看对应网课视频并完成推荐练习。', '{"overall":"基础稳定","weakKnowledge":["平行线性质"],"recommend":["完成平行线性质视频","完成推荐练习"]}', NOW()),
(9002, 'CLASS_REPORT', NULL, 9001, 9001, 'AI 演示班班级学情报告', '{"class":"AI 演示班","exam":"AI 演示章节测验","average":40}', '班级在等式性质相关题目上表现较好，平行线性质和困难题正确率偏低。建议下节课安排平行线性质错题讲评，并发布中等难度巩固练习。', '{"overall":"基础较好","weakChapters":["第二章 几何初步"],"teachingAdvice":["讲评高频错题","发布中等难度练习"]}', NOW())
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `input_json` = VALUES(`input_json`),
  `output_text` = VALUES(`output_text`),
  `output_json` = VALUES(`output_json`),
  `create_time` = VALUES(`create_time`);

INSERT INTO `ai_generated_question` (`id`, `teacher_id`, `course_id`, `chapter_id`, `knowledge_point_id`, `question_type`, `difficulty`, `content_json`, `status`, `create_time`, `update_time`) VALUES
(9001, 9002, 9001, 9002, 9003, 'SINGLE', 'MEDIUM', '{"content":"若两直线平行，内错角之间的关系是？","options":[{"label":"A","content":"相等","correct":true},{"label":"B","content":"互补","correct":false},{"label":"C","content":"无固定关系","correct":false},{"label":"D","content":"都为直角","correct":false}],"answer":"A","analysis":"平行线被第三条直线所截，内错角相等。"}', 'PENDING', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `content_json` = VALUES(`content_json`),
  `status` = VALUES(`status`),
  `update_time` = NOW();

INSERT INTO `t_ai_question_review` (`id`, `ai_call_log_id`, `repo_id`, `course_id`, `chapter_id`, `knowledge_point_id`, `qu_type`, `question_type`, `difficulty`, `content`, `options_json`, `answer_json`, `analysis`, `raw_json`, `status`, `review_comment`, `reviewer_id`, `create_user_id`, `create_time`, `update_time`) VALUES
(9001, 9001, 9001, 9001, 9002, 9003, 1, 'SINGLE', 'MEDIUM', '若两直线平行，内错角之间的关系是？', '[{"content":"相等","isRight":1},{"content":"互补","isRight":0},{"content":"无固定关系","isRight":0},{"content":"都为直角","isRight":0}]', '{"answer":"相等"}', '平行线被第三条直线所截，内错角相等。', '{"source":"demo"}', 'PENDING', NULL, NULL, 9002, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `content` = VALUES(`content`),
  `options_json` = VALUES(`options_json`),
  `answer_json` = VALUES(`answer_json`),
  `analysis` = VALUES(`analysis`),
  `status` = VALUES(`status`),
  `update_time` = NOW();

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
