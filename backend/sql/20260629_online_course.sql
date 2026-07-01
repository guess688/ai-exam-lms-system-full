-- Lightweight online-course tables.
-- Run after 20260628_course_content.sql.

CREATE TABLE IF NOT EXISTS `t_course_video` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Video ID',
  `course_id` int(11) NOT NULL COMMENT 'Course ID',
  `chapter_id` int(11) NOT NULL COMMENT 'Chapter ID',
  `title` varchar(150) NOT NULL COMMENT 'Video title',
  `description` text DEFAULT NULL COMMENT 'Video description',
  `video_url` varchar(800) NOT NULL COMMENT 'Video URL',
  `duration` int(11) NOT NULL DEFAULT 0 COMMENT 'Duration in seconds',
  `cover_url` varchar(500) DEFAULT NULL COMMENT 'Cover URL',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
  PRIMARY KEY (`id`),
  KEY `idx_course_video_course` (`course_id`),
  KEY `idx_course_video_chapter` (`chapter_id`),
  KEY `idx_course_video_status` (`status`),
  KEY `idx_course_video_sort` (`course_id`, `chapter_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Course video table';

CREATE TABLE IF NOT EXISTS `t_course_material` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Material ID',
  `course_id` int(11) NOT NULL COMMENT 'Course ID',
  `chapter_id` int(11) NOT NULL COMMENT 'Chapter ID',
  `title` varchar(150) NOT NULL COMMENT 'Material title',
  `file_url` varchar(800) NOT NULL COMMENT 'Material file URL',
  `file_type` varchar(50) DEFAULT NULL COMMENT 'Material file type',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
  PRIMARY KEY (`id`),
  KEY `idx_course_material_course` (`course_id`),
  KEY `idx_course_material_chapter` (`chapter_id`),
  KEY `idx_course_material_sort` (`course_id`, `chapter_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Course material table';

CREATE TABLE IF NOT EXISTS `t_student_video_progress` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Progress ID',
  `student_id` int(11) NOT NULL COMMENT 'Student user ID',
  `course_id` int(11) NOT NULL COMMENT 'Course ID',
  `chapter_id` int(11) NOT NULL COMMENT 'Chapter ID',
  `video_id` int(11) NOT NULL COMMENT 'Course video ID',
  `watched_seconds` int(11) NOT NULL DEFAULT 0 COMMENT 'Watched seconds',
  `duration` int(11) NOT NULL DEFAULT 0 COMMENT 'Duration in seconds',
  `progress_rate` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT 'Progress percentage',
  `completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '1 completed, 0 not completed',
  `last_watch_time` datetime DEFAULT NULL COMMENT 'Last watch time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_video_progress` (`student_id`, `video_id`),
  KEY `idx_video_progress_course` (`course_id`),
  KEY `idx_video_progress_chapter` (`chapter_id`),
  KEY `idx_video_progress_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student video progress table';
