# Repository Sources

本文档记录本项目主仓库和参考仓库的用途、技术栈、许可证状态，以及后续可直接集成或适合参考重写的模块。后续开发以 `backend` 和 `frontend` 为主，`references` 中可用部分必须经过评估后再放入 `external` 或迁移进主项目。

## 总体集成原则

- 主项目底座：`backend` + `frontend`。
- `references` 是候选复用源码池，不在一开始混合运行或引入为主项目依赖。
- 直接迁移前必须确认：许可证、依赖体量、运行时边界、数据模型映射、权限模型、改造成本。
- 技术栈不兼容时，优先抽取业务规则、数据模型、页面流程、Prompt、JSON schema 或测试用例，在主项目中按 SpringBoot + Vue 方式重写。
- AI 相关能力必须支持 mock 模式和真实模型配置，API Key 只能来自配置文件或环境变量。
- AI 自动生成题目必须进入教师审核流程，审核通过后才能入库。

## 仓库清单

| 目录 | 仓库 | 用途 | 技术栈 | 许可证 | 集成结论 |
| --- | --- | --- | --- | --- | --- |
| `backend` | `Alanosy/online-exam-system-backend` | 主项目后端，提供在线考试基础能力 | Spring Boot 2.1.3、Java、MyBatis-Plus、MySQL、Redis、Spring Security、JWT、Swagger/Knife4j、Druid、OSS/MinIO、LangChain4j/Coze/Milvus 相关依赖 | MIT | 主开发目标，优先在此扩展课程、学情分析、AI 服务层和 SQL migration |
| `frontend` | `Alanosy/online-exam-system-frontend` | 主项目前端，提供管理端/教师端/学生端在线考试界面 | Vue 2.6、Vue CLI、Element UI、Vue Router、Vuex、Axios、ECharts、Quill | MIT | 主开发目标，新增页面要沿用现有路由、API 封装和 UI 风格 |
| `references/frappe-lms` | `frappe/lms` | 轻量 LMS 参考，课程、章节、课时、测验、作业、证书、批次管理 | Frappe Framework、Python、Vue、Frappe UI、Node/Yarn | AGPL-3.0-or-later（项目元数据） | 不直接混入 Frappe 运行时；适合参考课程结构、学习进度和测验/作业流程 |
| `references/moodle` | `moodle/moodle` | 成熟 LMS 参考，课程、活动、题库、测验、成绩册、完成度、能力模型、报表 | PHP 8.3+、Moodle 插件体系、Composer、JavaScript/React 构建工具 | GPL-3.0-or-later | 不直接迁移整套系统；适合参考题库、组卷、成绩、完成度和能力模型设计 |
| `references/learnhouse` | `learnhouse/learnhouse` | 新一代课程内容平台参考，课程编辑、作业、讨论、分析、AI、协作 | FastAPI、Python、SQLModel、Alembic、PostgreSQL、Redis、Next.js、React、TypeScript、Tailwind、Yjs/Hocuspocus | AGPL-3.0（根 LICENSE） | 可评估 AI/分析/课程编辑服务设计；技术栈差异大，主项目内优先重写 |
| `references/studyield` | `studyield/studyield` | AI 学习平台参考，RAG、知识库、考试克隆、AI 出题、学习路径、教回法、分析 | NestJS、TypeScript、PostgreSQL、Redis、Qdrant、ClickHouse、React、Vite、Tailwind、OpenAI SDK | 根 LICENSE 为 AGPL-3.0；README/package 元数据出现 Apache-2.0，需二次确认 | AI 模块价值高，但许可证状态存在冲突；直接复制前必须确认许可证，优先参考设计重写 |
| `references/mcq_generator` | `csv610/mcq_generator` | LLM 选择题/判断题生成工具参考 | Python 3.10+、LiteLLM、Tenacity、tqdm、CLI、unittest | 本地未发现 LICENSE，需确认 | 不直接复制源码；可参考 Prompt、参数、JSON 输出和测试场景后在 Java AI 服务层重写 |

## 主项目：backend

### 用途

`backend` 是后续开发主线，现有能力覆盖用户、角色、班级、题库、试题、试卷、考试、自动判分、成绩、错题本、证书、公告、讨论、日志、统计等模块。

### 技术栈

- Spring Boot 2.1.3
- Java（`pom.xml` 中编译目标为 1.8，README 中提到 JDK 17；后续实际构建前需统一确认）
- MyBatis-Plus、XML Mapper
- MySQL、Druid
- Redis、Spring Session
- Spring Security、JWT
- Swagger / Knife4j
- Aliyun OSS、MinIO
- LangChain4j、Coze API、Milvus、WebSocket 相关依赖

### 适合直接集成的模块

- 课程、章节、知识点、题目难度等新实体和 Mapper。
- 自动组卷服务，基于现有题库、试卷、考试模型扩展。
- 学情统计服务，基于现有考试记录、成绩、答题记录扩展。
- AI 服务层接口与 mock 实现，复用现有 Spring Boot 配置体系。
- AI 出题审核流，接入现有教师/管理员权限体系。

### 适合参考重写的模块

- 参考 Moodle/Frappe 的课程与学习进度模型，在 Java 实体和 SQL 中重写。
- 参考 Studyield/mcq_generator 的 Prompt 与 JSON schema，在 SpringBoot AI 服务层中重写。
- 参考 LearnHouse/Studyield 的分析指标，在现有统计模块中重写。

## 主项目：frontend

### 用途

`frontend` 是后续开发主线，现有能力覆盖登录、权限路由、仪表盘、班级、题库、试题、考试、练习、记录、成绩、错题本、证书、公告、讨论等页面。

### 技术栈

- Vue 2.6
- Vue CLI 4
- Element UI 2.13
- Vue Router、Vuex
- Axios
- ECharts
- Quill 富文本

### 适合直接集成的模块

- 课程管理、章节管理、知识点管理页面。
- 题目课程/章节/知识点/难度字段维护页面。
- 自动组卷配置页面。
- 学情分析图表页面，优先复用 ECharts。
- 网课视频、资料、观看进度页面。
- 学习任务发布和任务完成情况页面。
- AI 报告、AI 出题审核、AI 教学文案页面。

### 适合参考重写的模块

- 参考 Frappe LMS 的课程目录、课时播放器、测验/作业入口。
- 参考 Moodle 的成绩报表和完成度视图。
- 参考 Studyield 的 AI 学习仪表盘、知识库、报告、出题审核体验。
- 参考 LearnHouse 的内容编辑器、课程分析和协作体验，但按 Vue 2 + Element UI 重写。

## 参考仓库：frappe-lms

### 用途

Frappe LMS 是轻量 LMS，可作为课程、章节、课时、测验、作业、证书和学习进度的参考。

### 技术栈

- Frappe Framework
- Python 3.10+
- Frappe DocType 数据模型
- Vue / Frappe UI
- Node 22+、Yarn、Cypress

### 许可证

项目元数据标记为 `AGPL-3.0-or-later`。

### 适合直接集成的模块

- 暂不建议直接复制后端代码，因为 Frappe DocType、权限、ORM 与 SpringBoot/MyBatis 不兼容。
- 可评估少量静态业务规则、字段命名、课程层级概念，记录到 `external` 后再改写。

### 适合参考重写的模块

- 三层课程结构：课程、章节、课时。
- 测验与作业挂载到课时/课程的方式。
- 学习进度、完成状态、证书申请与发放流程。
- 教师端课程创建、课程目录管理、课时内容组织体验。

## 参考仓库：moodle

### 用途

Moodle 是成熟 LMS，可作为复杂题库、测验、成绩册、完成度、能力模型和报表的长期设计参考。

### 技术栈

- PHP 8.3+
- Moodle core 与插件体系
- Composer
- JavaScript/TypeScript 构建工具，部分 React 依赖
- 多数据库支持和庞大的权限/插件机制

### 许可证

`GPL-3.0-or-later`。

### 适合直接集成的模块

- 不建议直接复制 PHP 代码或插件系统。
- 可抽取非代码层面的字段设计、状态机、报表指标和测试用例思路。

### 适合参考重写的模块

- `question`、`qbank`、`mod/quiz` 的题库和测验模型。
- `grade` 的成绩册与成绩项设计。
- `completion` 的课程/活动完成度逻辑。
- `competency` 的能力/知识点掌握模型。
- `report` / `reportbuilder` 的报表组织方式。

## 参考仓库：learnhouse

### 用途

LearnHouse 是现代课程内容平台，可参考课程编辑、作业、讨论、学习分析、AI、协作和内容组织体验。

### 技术栈

- FastAPI、Python
- SQLModel、Alembic
- PostgreSQL、Redis
- Next.js、React、TypeScript、TailwindCSS、Radix UI、Tiptap
- Yjs / Hocuspocus 实时协作
- Gemini、LlamaIndex、AI 相关依赖

### 许可证

根 `LICENSE` 为 `AGPL-3.0`。

### 适合直接集成的模块

- 不建议直接复制 API 或前端页面，因为 FastAPI/Next.js 与主项目 SpringBoot/Vue 2 不兼容。
- 若未来需要独立 AI/内容服务，可先在 `external` 中隔离验证服务边界。

### 适合参考重写的模块

- 课程编辑、内容块、课时播放器和课程详情页体验。
- 作业、讨论、用户组和课程访问控制思路。
- 学习分析指标和课程表现统计。
- AI 辅助学习/教学入口、上下文组织和提示词链路。
- 协作编辑只作为远期参考，不作为当前优先项。

## 参考仓库：studyield

### 用途

Studyield 是 AI 学习平台，可参考知识库、RAG Chat、AI 出题、考试克隆、学习路径、教回法、学习分析和多语言体验。

### 技术栈

- NestJS、TypeScript
- PostgreSQL、Redis
- Qdrant、ClickHouse
- React、Vite、TailwindCSS、Radix UI、Recharts
- OpenAI SDK、文档解析、PDF/DOCX 处理、Socket.IO

### 许可证

本地根 `LICENSE` 为 `AGPL-3.0`，但 README、CHANGELOG、`backend/package.json` 等元数据出现 `Apache-2.0` 描述。迁移源码前必须二次确认许可证，以根 `LICENSE` 为最低合规基准处理。

### 适合直接集成的模块

- 在许可证确认前，不直接复制源码。
- 可优先评估并摘录到 `external` 的内容：Prompt、JSON schema、AI 出题审核流程、报告结构、测试样例。

### 适合参考重写的模块

- `exam-clone`：上传历史试卷、抽取风格、生成相似题、审核入库。
- `knowledge-base`：资料上传、文档解析、语义检索、RAG 问答。
- `quiz`：AI 生成题目、答题、解析和练习推荐。
- `analytics`：学习行为、掌握度、趋势和统计看板。
- `teach-back`：学生解释概念，AI 给出理解度反馈。
- 前端 AI 仪表盘、出题审核、学习路径和报告页面，需按 Vue 2 + Element UI 重写。

## 参考仓库：mcq_generator

### 用途

`mcq_generator` 是 LLM 生成选择题/判断题的 Python 工具，可作为 AI 自动出题的 Prompt、参数、输出结构和测试用例参考。

### 技术栈

- Python 3.10+
- LiteLLM
- Tenacity、tqdm
- CLI 脚本
- unittest

### 许可证

本地仓库未发现 `LICENSE` 文件，`pyproject.toml` 也未声明许可证。直接复制源码前必须确认许可证或获得授权；在确认前只能作为设计参考。

### 适合直接集成的模块

- 暂不直接复制源码。
- 可在确认许可证后，将 Prompt 模板、JSON 输出 schema、CLI 参数映射、测试样例整理到 `external/mcq_generator`。

### 适合参考重写的模块

- 题目生成参数：学科、子领域、难度、题数、选项数、正确答案数量。
- 题型：单选、多选、判断、是/否。
- 特殊答案规则：全选、以上都不是、多正确答案解析。
- 输出 JSON 结构、解析容错、保存/加载题目文件。
- 单元测试场景：选项数量、答案解析、输入校验、边界条件。

## 后续推荐阶段

1. 阶段一：主项目运行检查与数据库梳理，确认原考试流程可运行。
2. 阶段二：课程、章节、知识点、题目难度字段与基础管理页面。
3. 阶段三：按章节/知识点/题型/难度自动组卷。
4. 阶段四：考试后学生/班级学情统计与图表分析。
5. 阶段五：轻量网课、视频资料、观看进度、完成率。
6. 阶段六：学习任务发布与任务完成跟踪。
7. 阶段七：AI 服务层、mock 模式、Prompt 管理、AI 报告。
8. 阶段八：AI 自动出题、教师审核、推荐练习和学习行为评价。

每个阶段完成后必须输出：修改文件清单、数据库变化、接口清单、前端页面变化、运行测试方式、已知问题。
