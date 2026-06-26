# 旅行社旅游管理系统 / Travel Agency Management System

> 题目七：基于 Servlet + SPA 的旅行社旅游管理系统的设计与实现  
> Topic 7: Design and Implementation of a Travel Agency Management System Based on Servlet + SPA

---

## 架构 / Architecture

| 层 Layer | 技术 Technology | 说明 Description |
|----------|---------------|-----------------|
| 前端 Frontend | HTML5 + CSS3 + JS SPA | 单页应用（~150KB），20 页面模块，零第三方框架 / 20 page modules, zero third-party frameworks |
| 控制层 Controller | Java Servlet 3.0 (9) | LoginServlet、**ApiServlet**(统一 REST JSON API / unified REST JSON API) 等 |
| 数据层 Data | JDBC (DBUtil) + MySQL 8.0 | 连接管理 + JSON 序列化 + 异常翻译 / connection mgmt + JSON serialization + error translation |
| 安全 Security | AuthFilter (web.xml) + SHA2-256 | 登录校验 + Cache-Control + RBAC / login check + Cache-Control + RBAC |
| 服务器 Server | Embedded Tomcat 9.0.85 | Main.java 一键启动 / one-click startup, `-Dport` 切换端口 |

## 前端特性 / Frontend Features

- **登录 / Login** — SVG 动画背景 + 毛玻璃卡片 + Canvas 验证码 / SVG animated background + frosted glass card + Canvas CAPTCHA
- **仪表盘 / Dashboard** — 统计卡片(CountUp) + 骨架屏 + 行入场动画 / stat cards + skeleton loading + stagger animation
- **CRUD 管理** (线路/班次/导游/宾馆/游客/用户 / routes/batches/guides/hotels/tourists/users) — 搜索 + 筛选 + 分页
- **游客报名 / Signup** — 三步向导(pick-card) / 3-step wizard
- **入团/退团 / Join/Quit** — 拖拽穿梭面板 / drag-and-drop transfer panel
- **成团校验 / Group Check** — 一键校验 + 闪光动画 / flash-green/flash-red
- **团体投保 / Insurance** — 投保表单 + 回执 / form + receipt
- **团费结算 / Settlement** — 统计卡片 + 费用明细
- **查询统计 / Statistics** — SVG 图表(柱状/折线/饼图) + 月历排班 + 进度条 + 排行榜 + CSV 导出
- **全局 / Global** — Ripple 水波纹、模态框、Toast、表单校验 shake+✓、侧边栏折叠

## 文件结构 / File Structure

```
E:\mysql2\
├── README.md
├── .gitignore
├── sql/                         # 数据库脚本 (7) / database scripts
├── ui/                          # UI 设计原型 / design prototype
├── webapp/                      # Java Web 应用
│   ├── pom.xml
│   ├── compile.bat / run.bat
│   ├── lib/                     # 依赖 JAR / dependencies
│   └── src/main/
│       ├── java/com/travel/
│       │   ├── Main.java               # 内嵌 Tomcat 入口 / embedded Tomcat entry
│       │   ├── util/
│       │   │   ├── DBUtil.java         # JDBC + JSON序列化/异常翻译
│       │   │   └── Html.java
│       │   └── web/
│       │       ├── AuthFilter.java     # 登录校验 / login filter
│       │       ├── ApiServlet.java     # 统一 REST API (24 端点/endpoints)
│       │       ├── LoginServlet.java
│       │       ├── RouteServlet.java   # 线路 CRUD
│       │       ├── BatchServlet.java   # 班次 / batches
│       │       ├── TouristServlet.java # 游客(事务/transactional)
│       │       ├── GroupServlet.java   # 旅游团(存储过程/stored procedures)
│       │       ├── InsuranceServlet.java
│       │       ├── StatsServlet.java
│       │       └── UserServlet.java
│       ├── resources/
│       │   └── db.properties.example   # 配置模板 / config template
│       └── webapp/
│           ├── index.html              # SPA 主页面 (~150KB)
│           ├── css/
│           └── WEB-INF/web.xml
```

## API 端点 / API Endpoints

ApiServlet (`@WebServlet("/api/*")`):

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/routes` | GET/POST | 线路列表+增改 / routes CRUD |
| `/api/routes/delete` | POST | 删除线路 / delete route |
| `/api/batches` | GET/POST | 班次列表+增改 / batches CRUD |
| `/api/batches/delete` | POST | 删除班次 / delete batch |
| `/api/guides` | GET/POST | 导游列表+增改 / guides CRUD |
| `/api/guides/delete` | POST | 删除导游 / delete guide |
| `/api/hotels` | GET/POST | 宾馆列表+增改 / hotels CRUD |
| `/api/hotels/delete` | POST | 删除宾馆 / delete hotel |
| `/api/tourists` | GET/POST | 游客列表+增改 / tourists CRUD |
| `/api/tourists/delete` | POST | 删除游客 / delete tourist |
| `/api/registrations` | GET | 报名记录 / registrations |
| `/api/groups` | GET | 旅游团列表 / groups |
| `/api/group_members` | GET | 团员名单 (?group_no=) / members |
| `/api/insurances` | GET | 保险记录 / insurances |
| `/api/stats/income` | GET | 班次收入 / batch income |
| `/api/stats/guide_schedule` | GET | 导游排班 / guide schedule |
| `/api/stats/hotel_reception` | GET | 宾馆接待 / hotel reception |
| `/api/dashboard/stats` | GET | 仪表盘统计 / dashboard stats |
| `/api/users` | GET/POST | 用户管理(仅管理员/admin only) |
| `/api/users/delete` | POST | 删除用户(仅管理员/admin only) |

统一响应格式 / Unified response: `{"ok":true,"data":[...]}` or `{"ok":false,"err":"..."}`

## 数据库 / Database

**`travel_agency`** (MySQL 8.0+, InnoDB, utf8mb4)

| Table | PK | Description |
|-------|-----|-------------|
| `route` | route_no | 旅游线路 / routes |
| `batch` | batch_no | 班次 / batches |
| `guide` | guide_no | 导游 / guides |
| `hotel` | hotel_no | 宾馆 / hotels |
| `tourist` | tourist_no | 游客 / tourists |
| `registration` | reg_no | 报名记录 / registrations |
| `tour_group` | group_no | 旅游团 (20~50人) / groups |
| `group_member` | (group_no,tourist_no) | 团员 / members |
| `batch_guide` | (batch_no,guide_no) | 导游随团 / guide assignments |
| `batch_hotel` | (batch_no,hotel_no) | 宾馆住宿 / hotel assignments |
| `insurance` | policy_no | 保险(一团一保/1:1) |
| `sys_user` | username | 用户(SHA2-256) / users |

**5 存储过程 / Stored Procedures** — `sp_tourist_register`, `sp_join_group`, `sp_confirm_group`, `sp_buy_insurance`, `sp_calc_group_fee`

**9 触发器 / Triggers** (含 4 校验触发器 / incl. 4 validation triggers)

**5 视图 / Views** — `v_batch_full`, `v_group_overview`, `v_group_roster`(身份证脱敏/ID masked), `v_guide_schedule`, `v_hotel_reception`

## 核心业务规则 / Core Business Rules

| Rule | Implementation |
|------|---------------|
| 团上限50人+自动计数 / Max 50 + auto-count | Triggers trg_member_bi/ai/ad + CHECK |
| 成团下限20人 / Min 20 to form group | sp_confirm_group / sp_buy_insurance |
| 一团一保 / One insurance per group | UNIQUE(group_no) + trg_insurance_bi |
| 回程日期自动推算 / Auto return date | trg_batch_bi |
| 报名原子性 / Atomic registration | JDBC transaction in TouristServlet |
| 游客编号防并发 / Concurrency-safe ID | GET_LOCK/RELEASE_LOCK + 1062 retry |
| 异常友好提示 / Friendly errors | DBUtil.friendly(): error codes → text |
| 字段名映射 / Field mapping | apiFieldMap + ApiServlet |

## 初始化 / Setup

```bash
mysql -u root -p < sql/01_create_schema.sql
mysql -u root -p < sql/02_indexes_views.sql
mysql -u root -p < sql/03_procedures_triggers.sql
mysql -u root -p < sql/04_sample_data.sql
mysql -u root -p < sql/06_app_user.sql
# 可选/Optional:
mysql -u root -p < sql/07_add_validation.sql
```

## 运行 / Run

**1.** 复制 `db.properties.example` → `db.properties`，填入 MySQL 密码 / fill in your MySQL password

**2.** 编译 / Compile: `compile.bat`

**3.** 启动 / Start: `run.bat` (默认端口 / default port 8090)

**4.** 访问 / Open: `http://localhost:8090/index.html`

| Account | Password | Role |
|---------|----------|------|
| admin | admin123 | 管理员 / Admin |
| business | bus123 | 业务员 / Agent |
| dispatch | dis123 | 调度员 / Dispatcher |

## 数据流 / Data Flow

```
Browser SPA (index.html)
    │
    ├── Login → POST /login?fmt=json
    │
    ├── Load → GET /api/* (ApiServlet → MySQL)
    │     ├── routes → MOCK.routes (via apiFieldMap)
    │     ├── batches / guides / hotels / tourists / groups
    │     ├── registrations → REG_DATA
    │     └── dashboard/stats → DASH_STATS
    │
    └── CRUD → POST /api/* → INSERT/UPDATE/DELETE
```

## 已验证 / Verified

- Java 全部编译通过 / All compiled: 9 Servlet + 2 Util + Main
- API 全部 `{"ok":true}` / All API endpoints OK
- SPA 20 页面正常渲染 / All 20 pages render correctly
- 全部交互特性通过 / All features verified (wizard, drag-drop, SVG charts, calendar, skeleton, etc.)
- 数据库全部可执行 / DB objects: 11 tables + 5 views + 5 procedures + 9 triggers
