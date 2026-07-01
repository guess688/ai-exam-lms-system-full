CREATE TABLE IF NOT EXISTS `ai_report` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'AI report ID',
  `report_type` varchar(64) NOT NULL COMMENT 'STUDENT_REPORT/CLASS_REPORT/TEACHING_PLAN/PAPER_REVIEW/LEARNING_BEHAVIOR',
  `student_id` int DEFAULT NULL COMMENT 'Student user ID',
  `class_id` int DEFAULT NULL COMMENT 'Class or grade ID',
  `exam_id` int DEFAULT NULL COMMENT 'Exam ID',
  `title` varchar(200) NOT NULL COMMENT 'Report title',
  `input_json` longtext COMMENT 'AI input JSON',
  `output_text` longtext COMMENT 'AI output text',
  `output_json` longtext COMMENT 'AI output JSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`),
  KEY `idx_ai_report_type` (`report_type`),
  KEY `idx_ai_report_student_exam` (`student_id`, `exam_id`),
  KEY `idx_ai_report_class_exam` (`class_id`, `exam_id`),
  KEY `idx_ai_report_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI generated report table';
