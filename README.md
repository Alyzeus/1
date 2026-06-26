# 题目七：旅行社旅游管理系统 —— 项目说明

## 架构概述

| 层 | 技术 | 说明 |
|----|------|------|
| 前端 | HTML5 + CSS3 + JavaScript SPA | 单页应用（index.html ~150KB），20 页面模块，纯前端路由，零第三方框架 |
| 控制层 | Java Servlet 3.0（9 个） | LoginServlet、**ApiServlet**（统一 REST JSON API）、RouteServlet、BatchServlet、TouristServlet、GroupServlet、InsuranceServlet、StatsServlet、UserServlet |
| 数据层 | JDBC（DBUtil） + MySQL 8.0 | 连接池管理、查询/更新封装、**JSON 序列化**、异常友好翻译 |
| 安全 | AuthFilter（web.xml 注册） + SHA2-256 | 登录校验、UTF-8 编码、**Cache-Control 头**、RBAC 角色权限 |
| 服务器 | 内嵌 Tomcat 9.0.85 | Main.java 一键启动，`-Dport` 切换端口 |

## 前端特性

SPA 单页应用实现了 UI 原型中的全部交互特性：

- 登录：SVG 风景动画背景 + 毛玻璃卡片 + Canvas 4 位验证码
- 仪表盘：统计卡片（CountUp 动画）+ 骨架屏加载 + 行交错入场
- 线路管理 / 班次管理 / 导游档案 / 宾馆档案 / 游客档案 / 用户管理：CRUD + 搜索 + 筛选 + 分页
- 游客报名：三步分步向导（pick-card 选线/选班→填信息→回执单）
- 创建旅游团：表单 + 勾选首批团员
- 入团 / 退团：拖拽穿梭面板 + 按钮操作
- 成团校验：一键校验 + 闪光动画（flash-green / flash-red）
- 导游随团安排：表格 + 导游选择
- 宾馆住宿安排：宾馆选择卡片 + 订房回执单
- 团体投保：投保表单 + 团员名单 + 投保回执
- 团费结算：统计卡片 + 费用明细表
- 查询统计：SVG 柱状图 / 折线图 / 饼图 + 月历排班视图 + 进度条 + 排行榜 + CSV 导出
- 全局：按钮 Ripple 水波纹、模态框淡入动画、Toast 通知、表单校验 shake + ✓ 反馈、侧边栏折叠

## 文件结构

```
E:\mysql2\
├── README.md                    # 本文件
├── .idea/                       # IntelliJ IDEA 项目文件
├── sql/                         # 数据库脚本（7 个）
│   ├── 01_create_schema.sql     # 建库 + 11 表
│   ├── 02_indexes_views.sql     # 6 二级索引 + 5 视图
│   ├── 03_procedures_triggers.sql # 5 存储过程 + 5 触发器
│   ├── 04_sample_data.sql       # 示例数据
│   ├── 05_test.sql              # 数据库层测试用例
│   ├── 06_app_user.sql          # 系统用户表 + 初始账号
│   └── 07_add_validation.sql    # 数据校验触发器（扩充）
├── ui/                          # UI 设计原型
│   ├── index.html               # 原型 SPA（完整 CSS + 20 页面模板 + Mock 数据）
│   ├── dashboard.png            # 仪表盘设计图
│   └── login-page.png           # 登录页设计图
├── reports/                     # 报告与截图
│   ├── 实训报告-终稿.docx        # 最终实训报告
│   ├── 实训报告-初稿.docx        # JSP 版本初稿（备份）
│   ├── 阶段1-选题报告.docx       # 阶段 1 交付物
│   ├── 技能调度规范.md           # AI Agent 技能调度规范
│   └── screenshots/             # 报告用截图（12 张）
└── webapp/                      # Java Web 应用
    ├── pom.xml                  # Maven POM
    ├── compile.bat              # 编译脚本
    ├── run.bat                  # 启动脚本
    ├── lib/                     # 依赖 JAR
    │   ├── tomcat-embed-core-9.0.85.jar
    │   ├── tomcat-embed-jasper-9.0.85.jar
    │   ├── tomcat-embed-el-9.0.85.jar
    │   ├── tomcat-annotations-api-9.0.85.jar
    │   ├── mysql-connector-j-8.0.33.jar
    │   └── ecj-3.26.0.jar
    └── src/main/
        ├── java/com/travel/
        │   ├── Main.java               # 内嵌 Tomcat 启动入口
        │   ├── util/
        │   │   ├── DBUtil.java         # JDBC 连接/查询/JSON序列化/异常翻译
        │   │   └── Html.java           # HTML 表格渲染（旧 JSP 兼容，保留）
        │   └── web/
        │       ├── AuthFilter.java     # 登录校验 + 编码 + Cache-Control
        │       ├── ApiServlet.java     # 统一 JSON REST API（10+GET / 6+POST）
        │       ├── LoginServlet.java   # 登录 / 注销（表单 + JSON 双模式）
        │       ├── RouteServlet.java   # 线路管理 CRUD
        │       ├── BatchServlet.java   # 班次管理
        │       ├── TouristServlet.java # 游客报名（JDBC 事务）
        │       ├── GroupServlet.java   # 旅游团管理（存储过程调用）
        │       ├── InsuranceServlet.java # 团体投保
        │       ├── StatsServlet.java   # 查询统计
        │       └── UserServlet.java    # 用户管理
        ├── resources/
        │   └── db.properties           # 数据库连接配置
        └── webapp/
            ├── index.html              # SPA 主页面（~150KB）
            ├── css/
            │   ├── modern.css          # 现代化样式（旧版保留）
            │   └── style.css           # 基础样式（旧版保留）
            └── WEB-INF/
                └── web.xml             # Servlet/Filter 部署描述符
```

## API 端点

ApiServlet（`@WebServlet("/api/*")`）提供的 REST JSON API：

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/routes` | GET | 线路列表 |
| `/api/routes` | POST | 新增/更新线路 |
| `/api/routes/delete` | POST | 删除线路 |
| `/api/batches` | GET | 班次列表 |
| `/api/batches` | POST | 新增/更新班次 |
| `/api/batches/delete` | POST | 删除班次 |
| `/api/guides` | GET | 导游列表 |
| `/api/guides` | POST | 新增/更新导游 |
| `/api/guides/delete` | POST | 删除导游 |
| `/api/hotels` | GET | 宾馆列表 |
| `/api/hotels` | POST | 新增/更新宾馆 |
| `/api/hotels/delete` | POST | 删除宾馆 |
| `/api/tourists` | GET | 游客列表 |
| `/api/tourists` | POST | 新增/更新游客 |
| `/api/tourists/delete` | POST | 删除游客 |
| `/api/registrations` | GET | 报名记录 |
| `/api/groups` | GET | 旅游团列表 |
| `/api/group_members` | GET | 团员名单（支持 `?group_no=` 筛选） |
| `/api/insurances` | GET | 保险记录 |
| `/api/stats/income` | GET | 班次收入统计 |
| `/api/stats/guide_schedule` | GET | 导游排班 |
| `/api/stats/hotel_reception` | GET | 宾馆接待 |
| `/api/dashboard/stats` | GET | 仪表盘统计（报名数/在团人数/团次数/收入） |
| `/api/users` | GET/POST | 用户管理（仅管理员） |
| `/api/users/delete` | POST | 删除用户（仅管理员） |

所有 API 返回统一 JSON 格式：`{"ok": true, "data": [...]}` 或 `{"ok": false, "err": "..."}`。

## 数据库

### 数据库：`travel_agency`（MySQL 8.0+，InnoDB，utf8mb4）

**11 张表**（`01_create_schema.sql`）：

| 表 | 主键 | 说明 |
|----|------|------|
| `route` | route_no | 旅游线路 |
| `batch` | batch_no | 班次（关联线路） |
| `guide` | guide_no | 导游（含身份证/语种/等级） |
| `hotel` | hotel_no | 宾馆（含星级/房价） |
| `tourist` | tourist_no | 游客（含身份证/手机号） |
| `registration` | reg_no（自增） | 报名记录 |
| `tour_group` | group_no | 旅游团（关联班次，20~50 人） |
| `group_member` | (group_no, tourist_no) | 团员名单 |
| `batch_guide` | (batch_no, guide_no) | 导游随团 |
| `batch_hotel` | (batch_no, hotel_no) | 宾馆住宿 |
| `insurance` | policy_no | 保险单（UNIQUE group_no，一团一保） |
| `sys_user` | username | 系统用户（SHA2-256 密码） |

**5 个存储过程**（`03_procedures_triggers.sql`）：
- `sp_tourist_register` — 游客报名建档
- `sp_join_group` — 游客入团
- `sp_confirm_group` — 成团校验（≥20 人）
- `sp_buy_insurance` — 团体投保
- `sp_calc_group_fee` — 团费结算

**9 个触发器**（含 `07_add_validation.sql` 扩充的 4 个校验触发器）

**5 个视图**（`02_indexes_views.sql`）：v_batch_full、v_group_overview、v_group_roster（身份证脱敏）、v_guide_schedule、v_hotel_reception

**6 个二级索引**：覆盖高频查询（按线路/日期/姓名/城市等）

## 核心业务规则

| 业务规则 | 实现位置 |
|---------|---------|
| 团人数上限 50 / 自动计数 | 触发器 trg_member_bi / trg_member_ai / trg_member_ad + CHECK |
| 成团下限 20 人 | 存储过程 sp_confirm_group / sp_buy_insurance |
| 一团一保（1:1） | UNIQUE(group_no) + 触发器 trg_insurance_bi |
| 回程日期自动推算 | 触发器 trg_batch_bi |
| 报名建档原子性 | TouristServlet 中 JDBC 事务（setAutoCommit/commit/rollback） |
| 游客编号防并发 | GET_LOCK / RELEASE_LOCK 分布式锁 + 1062 重试 |
| 异常友好提示 | DBUtil.friendly()：1062/1451/1452/3819/45000 → 中文 |
| SPA 字段→DB 列名映射 | 前端 apiFieldMap + ApiServlet 统一参数名 |

## 数据库初始化

```bash
mysql -u root -p < sql/01_create_schema.sql
mysql -u root -p < sql/02_indexes_views.sql
mysql -u root -p < sql/03_procedures_triggers.sql
mysql -u root -p < sql/04_sample_data.sql
mysql -u root -p < sql/06_app_user.sql

# 可选：追加数据校验触发器
mysql -u root -p < sql/07_add_validation.sql
```

## 运行

### 1. 配置数据库密码

编辑 `webapp/src/main/resources/db.properties`：

```properties
jdbc.url=jdbc:mysql://127.0.0.1:3306/travel_agency?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
jdbc.user=root
jdbc.password=你的密码
```

### 2. 编译

```bash
cd webapp
compile.bat
```

### 3. 启动

```bash
run.bat                   # 默认端口 8090
set PORT=8080 && run.bat  # 切换端口
```

### 4. 访问

浏览器打开 `http://localhost:8090/index.html`

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 管理员（全部功能） |
| business | bus123 | 业务员（线路/报名/组团） |
| dispatch | dis123 | 调度员（班次/导游/宾馆安排） |

## 前端数据流

```
浏览器 SPA (index.html)
    │
    ├─ 登录 → POST /login?fmt=json (LoginServlet)
    │
    ├─ 数据加载 → GET /api/* (ApiServlet → MySQL)
    │     ├── /api/routes       → MOCK.routes（经 apiFieldMap 转换）
    │     ├── /api/batches      → MOCK.batches
    │     ├── /api/guides       → MOCK.guides
    │     ├── /api/hotels       → MOCK.hotels
    │     ├── /api/tourists     → MOCK.tourists
    │     ├── /api/registrations → REG_DATA
    │     ├── /api/groups       → MOCK.groups
    │     └── /api/dashboard/stats → DASH_STATS
    │
    └─ CRUD 操作 → POST /api/* (ApiServlet → MySQL)
          ├── apiPost(path, apiVals) → INSERT/UPDATE
          └── apiDel(path, params)  → DELETE
```

## 已验证

- Java 全部编译通过（9 Servlet + 2 Util + Main）
- 7 个 API 端点全部 `{"ok": true}`
- SPA 20 页面模块全部正常渲染
- SPA 全部交互特性（步骤向导/拖拽/SVG 图表/日历/骨架屏等）验证通过
- 数据库 11 表 + 5 视图 + 5 存储过程 + 9 触发器全部可执行
- 报告截图已更新为 SPA 版本
