# 文档治理审计报告 - Inventory Tracking & Accounting

**审计时间**: 2025-12-01  
**审计范围**: `docs/` 目录下业务/技术/运维/用户/项目文档  

---

## 1. 总体概览与评分（高层结论）

> 粗粒度评分采用“结构完整性 / 内容质量 / 准确性”三维主观评估，满分 100。

| 文档类别     | 代表文件                               | 结构完整性 | 内容质量 | 准确性/时效性 | 总体评价     |
|--------------|----------------------------------------|------------|----------|---------------|--------------|
| 业务文档     | `business/PRD.md`, `PRD-V1.0.md` 等   | 高         | 高       | 中-高         | ✅ 基本优秀   |
| 技术架构文档 | `technical/ARCHITECTURE.md`           | 高         | 高       | 中             | ✅ 良好       |
| 技术方案文档 | `technical/TECH_SPEC_V1_0.md` 等      | 高         | 高       | 中             | ✅ 良好       |
| API 文档     | `technical/API_DESIGN.md`, `API_SPECIFICATION.md` | 高 | 中-高    | 中             | ✅ 合格-良好  |
| 运维文档     | `operations/INIT.md`, `DEPLOYMENT.md` | 高         | 高       | 中             | ✅ 良好       |
| 用户文档     | `user/USER_GUIDE.md`                  | 中-高      | 高       | 中-低          | ⚠️ 合格       |
| 文档索引     | `docs/README.md`                      | 中         | 中       | 中-低          | ⚠️ 需改进     |
| 项目版本文档 | `project/inventory-tracking-accounting/v1.0/*` | 高 | 高   | 高（已更新）   | ✅ 良好       |

整体结论：
- **优势**：业务/技术/运维/API 文档体系较完整，结构清晰，关键设计与接口说明已经成型，且有实现状态标记（✅/⚠️），便于追踪进度。
- **主要问题**：  
  - 文档索引 (`docs/README.md`) 与实际目录存在明显“过时/缺失项”（如 MONITORING.md/FAQ.md 未创建）。  
  - 部分用户文档（USER_GUIDE）描述了“还未完全实现或规划中”的功能，**未明确标注版本/实现状态**。  
  - 业务/技术文档中旧版本与新版本版本号较多（V1 / V1.0 / V1.1 / V1.2 / V2.0），**缺少统一索引与“当前生效版本”的显式标记**。  

---

## 2. 分类审计详情（按目录）

### 2.1 `docs/business/` 业务文档

- 文件列表：`PRD.md`, `PRD-V1.0.md`, `PRD-V1.1.md`, `PRD-V1.2.md`, `PRD-V2.0.md`, `PRD_INDEX.md`, `USER_STORIES.md`

**优点：**
- PRD 按版本细分，主线需求在 `PRD.md` 与 `PRD-V1.0.md` 中表达清晰，覆盖功能模块、非功能需求、术语等。
- `USER_STORIES.md` 提供了行为/验收视角，有利于测试与任务拆解。

**问题与建议：**
1. **版本关系说明不足（中优先级）**  
   - 问题：`PRD.md` 与 `PRD-V1.x/V2.0` 之间的关系主要依赖 `PRD_INDEX.md` 理解，对“当前后端/前端实现对应哪一版 PRD”缺少一行明确结论。  
   - 建议：在 `PRD_INDEX.md` 顶部增加一小节：「当前实现基线：后端/前端主干对应 PRD-V1.0，V1.1+ 为规划中版本」，并在各 PRD 文件头部增加“状态：已实现/进行中/规划”。  

2. **PRD 与实际技术栈差异（低优先级）**  
   - 情况：旧文档中部分提法偏向“技术栈未最终定稿”的阶段，但当前已经确定为 “Java 17 + Spring Boot 3 + PostgreSQL + Vue3”。  
   - 建议：在业务 PRD 中保留技术无关描述即可，技术细节转交 TECH_SPEC/ARCHITECTURE，避免重复与漂移。  

### 2.2 `docs/technical/` 技术文档

- 文件列表：`ARCHITECTURE.md`, `TECH_SPEC_V1.md`, `TECH_SPEC_V1_0.md`, `TECH_SPEC_V1_1.md`, `TECH_SPEC_V1_2.md`, `TECH_SPEC_V2_0.md`, `TECH_ROADMAP.md`, `API_DESIGN.md`, `API_SPECIFICATION.md`

**优点：**
- `ARCHITECTURE.md`：整体架构、领域模型、模块划分与用例流完整清晰，与当前代码结构高度一致（Java + Spring Boot + PostgreSQL + Vue3）。  
- `TECH_SPEC_V1_0.md`：详细记录了 V1.0 的数据库 schema、索引、后端结构、前端结构、多租户策略、测试与部署方案，并标记“已实现/待实现”。  
- `API_SPECIFICATION.md`：对 API 进行了逐接口的规范化描述，包含字段、响应示例与实现状态百分比，是当前“需求→实现进度”的主参照。  

**问题与建议：**
1. **技术规格版本众多，缺少“当前主线指针”（中优先级）**  
   - 问题：TECH_SPEC 有 `V1`, `V1_0`, `V1_1`, `V1_2`, `V2_0` 多个文件，新加入成员较难一眼判断“应该先看哪一份、哪一份对应当前代码”。  
   - 建议：  
     - 在 `TECH_ROADMAP.md` 或 `ARCHITECTURE.md` 顶部增加「当前实现版本：V1.0，V1.1+ 正在设计/规划」的显式说明。  
     - 在每个 TECH_SPEC 文件头部增加统一的小节：`状态: {已实现/进行中/规划}` + `适用范围: {后端/前端/DB}`。  

2. **API 文档轻微重复/交叉（低优先级）**  
   - 情况：`API_DESIGN.md` 与 `API_SPECIFICATION.md` 部分内容重叠；后者已经是更完整的规格与实现状态表。  
   - 建议：  
     - 将 `API_DESIGN.md` 定位为“设计原则 + 通用规范”（REST 风格、错误码、分页规则），  
     - `API_SPECIFICATION.md` 专注“具体端点 + 字段 + 状态”，并在两份文档中互相链接。  

3. **少量技术栈描述未完全同步（低优先级）**  
   - 个别旧版 TECH_SPEC 中曾提到“PostgreSQL 14 / 某些版本号”，而部署与实际环境已升级到 PostgreSQL 16 + 对应依赖版本。  
   - 建议：以 `TECH_SPEC_V1_0.md`、`DEPLOYMENT.md` 中的版本为准，后续统一校正并在旧文档中显式标注“历史记录，仅供参考”。  

### 2.3 `docs/operations/` 运维文档

- 文件：`INIT.md`, `DEPLOYMENT.md`

**优点：**
- INIT 明确了本地开发的前提条件、后端/前端初始化步骤、Docker 启动 PG 的命令，**已与当前 Postgres + Vue3 + Gradle 实际工程匹配**。  
- DEPLOYMENT 覆盖了开发/本地 Docker/生产部署（systemd + Nginx + DB 配置 + 备份 + 安全 + 日志/监控）一整套流程，命令具体且可执行。  

**问题与建议：**
1. **与 `docs/README.md` 中列出的 MONITORING.md/TROUBLESHOOTING.md 不一致（中优先级）**  
   - 问题：`docs/README.md` 目录示意中有 `MONITORING.md`、`TROUBLESHOOTING.md`，但实际 `operations/` 下尚未创建对应文件，监控/排障内容混合写在 DEPLOYMENT 中。  
   - 建议（两种方案二选一）：  
     - A) 按 README 约定新增 `MONITORING.md` 与 `TROUBLESHOOTING.md`，从 DEPLOYMENT 中拆出监控与故障排查章节；  
     - B) 若暂不拆分，则更新 `docs/README.md` 的目录结构说明，移除尚未使用的文件名，避免误导。  

2. **环境变量说明与代码/compose 文件需要定期对齐（低优先级）**  
   - 当前后端环境变量列表与 docker-compose 配置基本一致，但未来调整（例如新增 Redis/缓存策略）时，应及时同步两侧。  

### 2.4 `docs/user/` 用户文档

- 文件：`USER_GUIDE.md`

**优点：**
- 按“用户视角”拆分了注册登录、物品管理、库存交易、账户管理、报表、搜索/筛选、移动端使用、导入导出、FAQ、支持联系等，多数章节结构完整、语言友好。  

**问题与建议：**
1. **实现状态与版本标记缺失（高优先级，用户预期管理）**  
   - 问题：文档中描述了一些功能（如“批量添加物品”、“导出 CSV/Excel”、“移动端拍照上传 + 触摸手势”等），**部分尚未在当前代码中完全实现或仍处于规划阶段**。  
   - 建议：  
     - 在文件头部加一段「适用版本：前端 V1.0 / 后端 V1.0；标记图例：✅ 已实现、⚠️ 开发中、📝 规划中」；  
     - 对仍在规划中的能力（例如批量导入导出、部分移动端交互）在对应小节加上“⚠️ 该功能将在后续版本中提供”的提示。  

2. **缺少与技术文档/PRD 的跳转链接（中优先级）**  
   - 建议在 USER_GUIDE 结尾增加“相关文档”一节，指向：业务 PRD、API 文档（供高级用户/开发者）、TECH_SPEC 等。  

### 2.5 `docs/README.md` 与项目文档索引

**优点：**
- 给出了较清晰的“文档分类”与快速导航入口（业务 PRD / 架构 / 技术方案 / 初始化指南）。  

**问题与建议：**
1. **目录结构与真实文件不符（高优先级）**  
   - 问题：README 中列出了 `operations/MONITORING.md`, `operations/TROUBLESHOOTING.md`, `user/FAQ.md` 等文件，但目前并不存在；而实际存在的 V1.x/V2.0 技术文档、API_SPECIFICATION 等未在 README 中完全体现。  
   - 建议：  
     - 立即按真实情况更新 `docs/README.md` 的目录结构示意，至少保证罗列的文件“真实存在”；  
     - 为 `technical/TECH_SPEC_V1_0.md`、`technical/API_SPECIFICATION.md`、`project/inventory-tracking-accounting/v1.0/*` 增加导航链接。  

2. **缺少“当前推荐阅读顺序”的指引（中优先级）**  
   - 建议在 README 中增加“推荐阅读路径”：PRD_INDEX → PRD-V1.0 → ARCHITECTURE → TECH_SPEC_V1_0 → API_SPECIFICATION → USER_GUIDE / DEPLOYMENT。  

### 2.6 `docs/project/inventory-tracking-accounting/v1.0/`

- 文件：`prd.md`, `architecture.md`, `design.md`, `tasks.md`

**优点：**
- 已具备完整的项目级版本化设计：业务 PRD 抽取、架构设计、技术设计与任务拆解（含 Mermaid 依赖图、里程碑、人日预估等），整体风格与内容较为一致。  
- `tasks.md` 已更新为以 PostgreSQL + Vue 为基础的 V1.0 闭环任务列表，并区分 P0/P1，便于后续执行与追踪。  

**问题与建议：**
- 目前看与代码和上层文档的一致性较好，后续需要注意：当 TECH_SPEC/API_SPEC / 实际实现状态发生变化时，**同步更新对应任务状态**（如打钩/附“已完成/进行中”小标签）。  

---

## 3. 重点问题清单与优先级

### 3.1 高优先级（建议近期修复）

1. **`docs/README.md` 目录结构与实际文件不一致**  
   - 影响：新成员或 AI 工具根据 README 导航会跳转到不存在的 MONITORING/TROUBLESHOOTING/FAQ，降低信任度。  
   - 建议：  
     - 先统一“列出的文件必须存在”；  
     - 若暂不打算拆分监控/排障，直接在 README 中用一两句说明“监控与排障内容目前在 DEPLOYMENT.md 中”。  

2. **`USER_GUIDE.md` 未标注版本与实现状态，包含未来功能描述**  
   - 影响：真实用户会认为所有文档中提到的功能已经存在，导致预期与体验不匹配。  
   - 建议：  
     - 顶部增加“适用版本：V1.0；标记说明”；  
     - 为尚未实现功能在标题或段落前加上 “📝 规划中” 或 “⚠️ 后续版本提供”。  

### 3.2 中优先级（建议一个迭代内完成）

1. **业务/技术文档的版本关系需要一处权威说明**  
   - 在 `PRD_INDEX.md` 与 `TECH_ROADMAP.md` 中各自补充「当前后端/前端实现对应版本」小节，避免多处猜测。  

2. **API 文档拆分职责**  
   - 明确 `API_DESIGN.md` = 原则与规范；`API_SPECIFICATION.md` = 端点规格与实现状态，并互相加上“相关文档链接”。  

3. **运维文档的监控/排障章节拆分与链接调整**  
   - 如果计划长期维护 MONITORING/TROUBLESHOOTING 独立文档，可从 DEPLOYMENT 中裁剪出来；否则更新 README 描述。  

### 3.3 低优先级（可视时间安排）

1. 技术文档中旧版依赖/版本号轻微漂移（例如 PG 14 vs 16）统一修订或标注为“历史记录”。  
2. 在所有主要文档尾部统一增加「最后更新」「维护者」「适用版本」信息，提升可维护性。  

---

## 4. 建议的后续治理步骤

1. **第一步（半天内可完成）**  
   - 修正 `docs/README.md` 的目录示意与导航链接。  
   - 在 `USER_GUIDE.md` 顶部与关键“规划中功能”处补充版本与状态说明。  

2. **第二步（作为一次小型文档迭代）**  
   - 更新 `PRD_INDEX.md` 与 `TECH_ROADMAP.md`，显式声明“当前实现基线版本”；  
   - 梳理 `API_DESIGN.md` 与 `API_SPECIFICATION.md` 的角色，并添加交叉链接。  

3. **第三步（长期治理）**  
   - 为后续版本（V1.1+）继续在 `docs/project/{project}/v{version}/` 下保持“prd + architecture + design + tasks”的结构，避免技术文档散落；  
   - 在 CI 中加入简单的文档检查（例如：`docs/README.md` 引用的文件都存在）。  

---

**备注**：本审计报告仅对 `docs/` 目录内的 Markdown 文档进行静态分析与结构/内容评估，未自动修改任何已有文档内容。如需，我可以按上述优先级逐项提交具体修改补丁。  


