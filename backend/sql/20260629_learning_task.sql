-- Learning task module for exam, practice, video, wrong-question and review assignments.

CREATE TABLE IF NOT EXISTS `t_learning_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Task ID',
  `title` varchar(150) NOT NULL COMMENT 'Task title',
  `description` text DEFAULT NULL COMMENT 'Task description',
  `task_type` varchar(32) NOT NULL COMMENT 'EXAM/PRACTICE/VIDEO/WRONG_QUESTION/REVIEW',
  `course_id` int(11) DEFAULT NULL COMMENT 'Course ID',
  `chapter_id` int(11) DEFAULT NULL COMMENT 'Chapter ID',
  `knowledge_point_id` int(11) DEFAULT NULL COMMENT 'Knowledge point ID',
  `target_type` varchar(16) NOT NULL COMMENT 'CLASS/STUDENT',
  `target_class_id` int(11) DEFAULT NULL COMMENT 'Target class ID',
  `target_student_id` int(11) DEFAULT NULL COMMENT 'Target student ID',
  `related_exam_id` int(11) DEFAULT NULL COMMENT 'Related exam ID',
  `related_paper_id` int(11) DEFAULT NULL COMMENT 'Related paper or repo ID',
  `related_video_id` int(11) DEFAULT NULL COMMENT 'Related course video ID',
  `deadline` datetime DEFAULT NULL COMMENT 'Deadline',
  `publisher_id` int(11) NOT NULL COMMENT 'Publisher user ID',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (`id`),
  KEY `idx_learning_task_type` (`task_type`),
  KEY `idx_learning_task_target` (`target_type`, `target_class_id`, `target_student_id`),
  KEY `idx_learning_task_course` (`course_id`, `chapter_id`, `knowledge_point_id`),
  KEY `idx_learning_task_exam` (`related_exam_id`),
  KEY `idx_learning_task_video` (`related_video_id`),
  KEY `idx_learning_task_publisher` (`publisher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Learning task table';

CREATE TABLE IF NOT EXISTS `t_learning_task_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Task record ID',
  `task_id` int(11) NOT NULL COMMENT 'Task ID',
  `student_id` int(11) NOT NULL COMMENT 'Student user ID',
  `status` varchar(32) NOT NULL DEFAULT 'TODO' COMMENT 'TODO/IN_PROGRESS/COMPLETED',
  `progress_rate` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT 'Progress percentage',
  `finish_time` datetime DEFAULT NULL COMMENT 'Finish time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_task_student` (`task_id`, `student_id`),
  KEY `idx_learning_task_record_student` (`student_id`),
  KEY `idx_learning_task_record_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Learning task record table';
