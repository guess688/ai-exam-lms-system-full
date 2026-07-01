# references 复用说明

## 复用原则

`references` 目录是候选复用源码池。由于参考项目技术栈差异较大，不能盲目复制整套系统。所有复用必须先评估：

- 技术栈是否兼容。
- 依赖和运行成本是否可控。
- 许可证和二次开发许可是否满足项目交付。
- 能否拆分为 Prompt、schema、页面结构、独立服务或业务规则。
- 是否会破坏当前 Spring Boot + Vue 主项目架构。

经过评估后，可用内容优先进入：

```text
external/
```

或者按主项目技术栈改造进入：

```text
backend/
frontend/
```

## Frappe LMS

| 内容 | 复用方式 |
| --- | --- |
| 课程、章节、内容组织模型 | 抽取字段设计和业务规则后在 Spring Boot 中重写 |
| 学习进度逻辑 | 参考完成率和章节进度设计 |
| 课程详情页和后台课程管理交互 | 参考页面结构和信息层级 |
| Python/Frappe 服务代码 | 不直接混入主项目 |

## Moodle

| 内容 | 复用方式 |
| --- | --- |
| Course/Quiz/Grade/Question bank 结构 | 抽取数据模型和业务规则 |
| Completion 完成度逻辑 | 参考后重写为轻量版观看进度和任务完成规则 |
| Report 学习报告逻辑 | 参考统计维度，不直接复制 PHP 代码 |
| Moodle 运行时和插件体系 | 不集成，改造成本高 |

## LearnHouse

| 内容 | 复用方式 |
| --- | --- |
| 网课页面设计 | 参考页面结构和交互 |
| 课程详情与学生学习页 | 参考信息组织方式 |
| 任务/作业模块 | 参考状态模型和完成规则 |
| 前端组件 | 技术栈差异较大，优先重写 |

## Studyield

| 内容 | 复用方式 |
| --- | --- |
| 学习报告 Prompt | 迁移到 `backend/src/main/resources/prompts` |
| 练习生成 Prompt | 迁移并适配课程、章节、知识点、题型、难度 |
| 学习反馈输出结构 | 抽取 JSON schema 后改造 |
| 知识点掌握分析逻辑 | 参考后结合本项目考试记录实现 |

相关适配材料：

```text
external/ai-learning-adapted/
docs/studyield_reuse_plan.md
docs/ai_prompt_schema.md
```

## mcq_generator

| 内容 | 复用方式 |
| --- | --- |
| AI 出题 Prompt | 迁移到主项目 Prompt 目录 |
| 输入参数设计 | 适配课程、章节、知识点、题型、难度、数量 |
| 输出 JSON 格式 | 转换为主项目题库模块可解析结构 |
| 独立 Python 服务 | 如后续需要，可封装到 `external/mcq-generator-service` |

当前主项目实现要求：

- 支持单选题、多选题、不定项选择题、判断题。
- 输出进入 AI 待审核题目表。
- 教师审核后才进入正式题库。

相关适配材料：

```text
docs/mcq_generator_reuse_plan.md
backend/src/main/resources/prompts/question_generation_prompt.txt
```

## 许可证说明

用户已说明所有模型和参考内容已取得二次开发许可。工程落地仍建议：

- 在客户交付材料中保留来源说明。
- 不复制不需要的完整系统代码。
- 对 AGPL/GPL 项目来源的直接代码复用保持清晰边界。
- 优先复用 Prompt、schema、业务规则和页面交互，再按主项目技术栈实现。
