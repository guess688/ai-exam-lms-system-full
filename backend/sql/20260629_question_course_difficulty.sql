-- Add teaching-content metadata and normalized difficulty to questions.
-- Run once after 20260628_course_content.sql.

ALTER TABLE `t_question`
  ADD COLUMN `course_id` int(11) DEFAULT NULL COMMENT 'Course ID' AFTER `repo_id`,
  ADD COLUMN `chapter_id` int(11) DEFAULT NULL COMMENT 'Chapter ID' AFTER `course_id`,
  ADD COLUMN `knowledge_point_id` int(11) DEFAULT NULL COMMENT 'Knowledge point ID' AFTER `chapter_id`,
  ADD COLUMN `difficulty` varchar(16) NOT NULL DEFAULT 'MEDIUM' COMMENT 'EASY/MEDIUM/HARD' AFTER `knowledge_point_id`;

UPDATE `t_question`
SET `difficulty` = 'MEDIUM'
WHERE `difficulty` IS NULL OR `difficulty` = '';

CREATE INDEX `idx_question_course` ON `t_question` (`course_id`);
CREATE INDEX `idx_question_chapter` ON `t_question` (`chapter_id`);
CREATE INDEX `idx_question_knowledge_point` ON `t_question` (`knowledge_point_id`);
CREATE INDEX `idx_question_difficulty` ON `t_question` (`difficulty`);
