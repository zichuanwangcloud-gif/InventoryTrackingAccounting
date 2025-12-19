# 文档目录

## 📁 文档结构说明（已与实际文件对齐）

```text
docs/
├── README.md                                  # 文档总览（本文件）
├── PROGRESS.md                                # 功能进度追踪（快速查询功能完成状态）
├── business/                                  # 业务文档（PRD/用户故事/版本索引）
│   ├── PRD.md                                 # 当前主 PRD（总览）
│   ├── PRD-V1.0.md                            # V1.0 需求说明
│   ├── PRD-V1.1.md                            # V1.1 需求说明
│   ├── PRD-V1.2.md                            # V1.2 需求说明
│   ├── PRD-V2.0.md                            # V2.0 需求草案
│   ├── PRD_INDEX.md                           # 各版本 PRD 索引与状态
│   └── USER_STORIES.md                        # 用户故事与验收标准
├── technical/                                 # 技术文档（架构、技术方案、API）
│   ├── ARCHITECTURE.md                        # 系统架构设计（与当前实现对齐）
│   ├── TECH_SPEC_V1_0.md                      # V1.0 技术方案（当前主要基线）
│   ├── TECH_SPEC_V1.md                        # 早期 V1 技术方案
│   ├── TECH_SPEC_V1_1.md                      # V1.1 增量技术方案
│   ├── TECH_SPEC_V1_2.md                      # V1.2 增量技术方案
│   ├── TECH_SPEC_V2_0.md                      # V2.0 技术规划
│   ├── TECH_ROADMAP.md                        # 技术路线图与版本规划
│   ├── API_DESIGN.md                          # API 设计原则与通用规范
│   └── API_SPECIFICATION.md                   # 具体 API 规格与实现状态
├── operations/                                # 运维与部署文档
│   ├── INIT.md                                # 初始化与本地运行说明
│   └── DEPLOYMENT.md                          # 部署、监控、故障排查与备份
├── user/                                      # 面向终端用户的使用文档
│   └── USER_GUIDE.md                          # 用户使用手册（适配 V1.0）
└── project/
    └── inventory-tracking-accounting/
        └── v1.0/                              # 项目级版本化设计文档
            ├── prd.md                         # 从业务 PRD 抽取的 V1.0 摘要
            ├── architecture.md                # V1.0 架构设计（项目视角）
            ├── design.md                      # V1.0 技术设计（实现细节）
            └── tasks.md                       # V1.0 任务拆解与里程碑
```

> 说明：早期规划中的 `MONITORING.md` / `TROUBLESHOOTING.md` / `FAQ.md` 暂未拆分为独立文件，相关内容目前集中在：
> - 监控 & 故障排查：`operations/DEPLOYMENT.md` 的监控与故障排查章节  
> - 常见问题（用户视角）：`user/USER_GUIDE.md` 的“常见问题”小节  

---

## 📋 文档分类与推荐阅读顺序

### 业务文档 (`docs/business/`)
- **`PRD_INDEX.md`**：各版本 PRD 索引，说明 V1.0 / V1.1+ 的关系与状态。  
- **`PRD.md`**：主 PRD（整体需求与路线图）。  
- **`PRD-V1.0.md`**：当前实现重点对应的 V1.0 需求说明。  
- **`USER_STORIES.md`**：详细用户故事与验收标准。  

> 推荐：新成员可先阅读 `PRD_INDEX.md` → `PRD-V1.0.md` → `USER_STORIES.md`。  

### 技术文档 (`docs/technical/`)
- **`ARCHITECTURE.md`**：系统整体架构设计（后端/前端/DB/模块划分）。  
- **`TECH_SPEC_V1_0.md`**：V1.0 版本的详细技术方案（当前实现主基线）。  
- **`TECH_SPEC_V1_1.md` / `TECH_SPEC_V1_2.md` / `TECH_SPEC_V2_0.md`**：后续版本的增量/规划性技术方案。  
- **`TECH_ROADMAP.md`**：版本演进与重要技术里程碑。  
- **`API_DESIGN.md`**：REST/API 设计原则、统一响应格式、错误码规范等。  
- **`API_SPECIFICATION.md`**：具体 API 端点、字段、示例及“✅/⚠️ 实现状态统计”。  

> 推荐：后端/前端开发可按 `ARCHITECTURE.md` → `TECH_SPEC_V1_0.md` → `API_DESIGN.md` → `API_SPECIFICATION.md` 顺序阅读。  

### 运维文档 (`docs/operations/`)
- **`INIT.md`**：本地开发初始化说明（依赖、启动命令、环境变量）。  
- **`DEPLOYMENT.md`**：开发/测试/生产部署、Docker Compose、Nginx 配置、数据库迁移、监控、备份与故障排查。  

### 用户文档 (`docs/user/`)
- **`USER_GUIDE.md`**：面向终端用户的操作手册，目前主要适配 **V1.0 功能集**。  

### 项目版本化文档 (`docs/project/inventory-tracking-accounting/`)
- **`v1.0/`**：与当前实现对应的项目级文档：  
  - `prd.md`：从业务 PRD 抽取的 V1.0 需求视图  
  - `architecture.md`：项目视角的架构拆解  
  - `design.md`：技术设计与实现思路  
  - `tasks.md`：任务拆解、依赖关系与里程碑规划  

---

## 🚀 快速导航

- **功能进度追踪**（推荐首看）
  - [PROGRESS.md](./PROGRESS.md) — 快速查询"功能X做完了吗"
- 业务与需求
  - [产品需求文档（主 PRD）](./business/PRD.md)
  - [V1.0 需求说明](./business/PRD-V1.0.md)
  - [PRD 版本索引](./business/PRD_INDEX.md)  
- 架构与技术方案  
  - [系统架构设计](./technical/ARCHITECTURE.md)  
  - [V1.0 技术方案（当前基线）](./technical/TECH_SPEC_V1_0.md)  
  - [技术路线图](./technical/TECH_ROADMAP.md)  
- API 与实现进度  
  - [API 设计原则](./technical/API_DESIGN.md)  
  - [API 规格与实现状态](./technical/API_SPECIFICATION.md)  
- 开发与运维  
  - [初始化与本地运行指南](./operations/INIT.md)  
  - [部署与运维文档](./operations/DEPLOYMENT.md)  
- 项目级版本文档  
  - [V1.0 项目文档索引](./project/inventory-tracking-accounting/v1.0/prd.md)  
  - [V1.0 架构/设计/任务](./project/inventory-tracking-accounting/v1.0/)  
- 用户手册  
  - [终端用户使用手册](./user/USER_GUIDE.md)  

---

## 📝 文档维护规范

- 所有文档使用 Markdown 编写，标题层级不超过 4 级。  
- 业务/技术/运维/项目文档建议在文末标注：**文档版本、最后更新日期、维护者**。  
- 与代码/配置强相关的文档（例如 API 规格、部署说明）在功能变更或版本升级时必须同步更新。  
- 新版本（V1.1+）的架构与设计文档，优先放在 `docs/project/{project}/v{version}/` 目录下，以保持结构一致性。  

---

**最后更新**: 2025-12-01  
**维护者**: Documentation Team  
**当前实现基线版本**: 后端/前端主要对应 **V1.0**（详见 `business/PRD-V1.0.md` 与 `technical/TECH_SPEC_V1_0.md`）  
