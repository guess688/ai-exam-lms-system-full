# LMS adapted assets

本目录用于保存从 `references` 中评估后可迁移或可改写的 LMS 资产。本阶段没有直接复制 Frappe LMS、LearnHouse、Moodle 的业务代码，原因是三者技术栈、权限模型和数据访问方式都与当前 SpringBoot + Vue 主项目不同；本次只迁移字段设计、目录结构和学习进度规则。

## 本次适配结论

| 来源 | 参考内容 | 本项目落点 | 集成方式 |
| --- | --- | --- | --- |
| `references/frappe-lms` | Course Chapter、Course Lesson、LMS Course Progress、视频 URL 嵌入与观看时长记录 | `t_course_video`、`t_student_video_progress`、学生课程详情页视频播放区 | 抽取模型和业务规则后在 SpringBoot + Vue 重写 |
| `references/learnhouse` | Course -> Chapter -> Activity 结构、活动资源 URL、学习页中内容与进度并列展示 | 章节下统一返回视频、资料、知识点、练习题数量、测验数量 | 抽取页面信息架构后在 Vue 重写 |
| `references/moodle` | `course_sections`、`course_modules`、completion 逻辑、课程资源和测验同属章节 | `ChapterLearningVO` 聚合章节资源；`completed >= 90%` 作为轻量完成规则 | 抽取数据模型和完成度规则后重写 |
| `references/learnhouse` | Assignment/Activity 分配、学习活动完成状态、课程活动入口 | `t_learning_task`、`t_learning_task_record`、学生任务列表 | 抽取任务状态和页面结构后在 Vue + SpringBoot 重写 |
| `references/frappe-lms` | Quiz、Assignment、Lesson Progress 和学生进度汇总 | 学习任务类型、完成率、教师任务完成情况表 | 抽取业务规则后重写 |
| `references/moodle` | Assignment availability、completion tracking、activity completion | `deadline`、`status`、`progress_rate`、`finish_time` | 抽取状态模型后重写 |

## 当前映射

- `Course Lesson` / `Activity(video)` -> `t_course_video`
- `Course resource/file` -> `t_course_material`
- `LMS Course Progress` / completion -> `t_student_video_progress`
- `course_sections` -> 已有 `t_course_chapter`
- `course_modules` -> `ChapterLearningVO.videos/materials/questionCount/examCount`
- `Assignment` / `Activity` -> `t_learning_task`
- `Activity completion` -> `t_learning_task_record`
- `Completion status` -> `TODO`、`IN_PROGRESS`、`COMPLETED`

## 不直接复制的内容

- Frappe LMS 的 Doctype、Frappe ORM、Vue 3 组件和 Tailwind 样式。
- LearnHouse 的 Next.js/React 组件、SCORM 企业版实现和媒体流服务。
- Moodle 的 PHP 插件、复杂完成度状态机和全量课程模块体系。

这些内容改造成本高，且会破坏当前项目的 Spring Security、MyBatis-Plus、Element UI 和既有路由风格。本阶段保留为后续功能扩展参考。
