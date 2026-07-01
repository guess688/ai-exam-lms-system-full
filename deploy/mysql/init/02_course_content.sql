USE db_exam;

-- Course, chapter and knowledge point tables for AI exam LMS phase 1.
-- The project uses t_* table names, so these tables intentionally follow that convention.

CREATE TABLE IF NOT EXISTS `t_course` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Course ID',
  `name` varchar(100) NOT NULL COMMENT 'Course name',
  `description` text DEFAULT NULL COMMENT 'Course description',
  `cover_url` varchar(500) DEFAULT NULL COMMENT 'Cover URL',
  `teacher_id` int(11) NOT NULL COMMENT 'Owner teacher ID',
  `grade_id` int(11) NOT NULL COMMENT 'Visible class ID, references t_grade.id by convention',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
  PRIMARY KEY (`id`),
  KEY `idx_course_teacher_id` (`teacher_id`),
  KEY `idx_course_grade_id` (`grade_id`),
  KEY `idx_course_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Course table';

CREATE TABLE IF NOT EXISTS `t_course_chapter` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Chapter ID',
  `course_id` int(11) NOT NULL COMMENT 'Course ID',
  `title` varchar(150) NOT NULL COMMENT 'Chapter title',
  `description` text DEFAULT NULL COMMENT 'Chapter description',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
  PRIMARY KEY (`id`),
  KEY `idx_chapter_course_id` (`course_id`),
  KEY `idx_chapter_status` (`status`),
  KEY `idx_chapter_sort` (`course_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Course chapter table';

CREATE TABLE IF NOT EXISTS `t_knowledge_point` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Knowledge point ID',
  `course_id` int(11) NOT NULL COMMENT 'Course ID',
  `chapter_id` int(11) NOT NULL COMMENT 'Chapter ID',
  `name` varchar(150) NOT NULL COMMENT 'Knowledge point name',
  `description` text DEFAULT NULL COMMENT 'Knowledge point description',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
  PRIMARY KEY (`id`),
  KEY `idx_kp_course_id` (`course_id`),
  KEY `idx_kp_chapter_id` (`chapter_id`),
  KEY `idx_kp_status` (`status`),
  KEY `idx_kp_sort` (`chapter_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Knowledge point table';
