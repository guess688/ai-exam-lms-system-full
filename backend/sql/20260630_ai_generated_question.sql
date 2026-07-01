CREATE TABLE IF NOT EXISTS `ai_generated_question` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'AI generated question ID',
  `teacher_id` int NOT NULL COMMENT 'Teacher user ID',
  `course_id` int NOT NULL COMMENT 'Course ID',
  `chapter_id` int NOT NULL COMMENT 'Chapter ID',
  `knowledge_point_id` int NOT NULL COMMENT 'Knowledge point ID',
  `question_type` varchar(32) NOT NULL COMMENT 'SINGLE/MULTIPLE/INDEFINITE/JUDGE',
  `difficulty` varchar(16) NOT NULL COMMENT 'EASY/MEDIUM/HARD',
  `content_json` longtext NOT NULL COMMENT 'Generated and editable question JSON',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  KEY `idx_ai_generated_question_teacher` (`teacher_id`),
  KEY `idx_ai_generated_question_scope` (`course_id`, `chapter_id`, `knowledge_point_id`),
  KEY `idx_ai_generated_question_type` (`question_type`, `difficulty`),
  KEY `idx_ai_generated_question_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI generated question review queue';
