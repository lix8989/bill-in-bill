# Windows 分发方案（帐里说帐 / bill-in-bill）：ZIP 绿色便携版优先

本文档梳理当前应用的**技术栈与功能**，并给出在现有结构下将应用打成 **ZIP 压缩包**：用户**解压到本机磁盘任意位置**后，通过 **启动脚本** 即可运行的 **Windows 绿色版（免安装安装器）**。MSI/exe 等安装包可作为后续扩充，下文仅作备选说明。

---

## 1. 当前项目结构概览

| 目录 | 说明 |
|------|------|
| `backend/` | Spring Boot 后端，可打成可执行 Fat JAR |
| `frontend/` | Vue 3 + Vite 前端，产出静态资源目录 `dist/` |
| `data/`（运行时） | SQLite 数据库文件目录；配置为相对路径 `./data/wechat-bill.db` |

---

## 2. 技术栈与运行环境

### 2.1 后端（`backend/`）

- **框架**：Spring Boot **3.3.12**
- **语言 / 运行时**：**Java 21**（`pom.xml` 中 `java.version=21`）
- **持久化**：SQLite（`sqlite-jdbc`）+ **MyBatis-Plus**
- **其他**：Apache POI（Excel 相关导入导出能力）
- **入口**：`com.lex.wechatbill.WechatBillApplication`
- **HTTP 端口**：`8080`（`application.properties`）
- **数据库 URL**：`jdbc:sqlite:./data/wechat-bill.db`（**相对当前进程工作目录**）

### 2.2 前端（`frontend/`）

- **框架**：Vue **3**，构建工具 **Vite**
- **UI**：Element Plus
- **图表**：ECharts
- **路由**：Vue Router（`history` 模式）
- **HTTP**：Axios；本地开发连接 `http://localhost:8080/api`，生产构建后使用同源 **`/api`**（见 `frontend/src/api/http.js`）

### 2.3 开发与部署时的典型命令

```text
后端：backend 目录下 mvn spring-boot:run 或 java -jar xxx.jar（需 JDK/JRE 21）
前端开发：frontend 目录下 npm install && npm run dev
前端生产构建：frontend 目录下 npm run build → 产出 frontend/dist/
```

---

## 3. 功能梳理（与用户可见模块对应）

应用品牌与导航在 `frontend/src/App.vue` 与各 `views` 中体现，后端按 `/api/**` 提供 REST。主要模块如下：

| 模块（界面） | 路由 | 后端领域（示意） |
|--------------|------|------------------|
| 说帐总览 | `/dashboard` | `/api/dashboard` |
| 逐笔说帐 | `/bills` | `/api/bills` |
| 导入说帐 | `/imports` | `/api/imports` |
| 年度账本 | `/annual-report` | `/api/reports` |
| 自动说帐 | `/classifier-tasks` | `/api/classifier/tasks` |
| 说帐复核 | `/review-bills` | （与账单/分类流水线相关接口） |
| 说帐疑问 | `/failed-bills` | |
| 说帐分类 | `/categories` | `/api/categories` |
| 关键字规则 | `/keyword-rules` | `/api/classifier/keyword-rules` |
| 同步日志 / 同步调试 | `/classifier-sync-logs`、`/classifier-sync-test` | `/api/classifier/sync`、`/api/classifier/llm-settings` |
| 使用指南 | `/guide` | 主要为前端静态内容 |
| 健康检查 | — | `/api/health` |

**业务简述**：以个人账单（含微信等来源）为核心的导入、浏览、仪表盘统计、关键字与自动分类任务、复核与异常账单处理，以及年度报告类分析展示；数据存储在本地 SQLite。

---

## 4. Windows 便携版（解压即用）的前提设计问题

绿色版不写注册表、不强制「安装向导」，但发布后仍是一台 Windows 上的一个长期运行目录，需注意与「开发环境」的不同点：

### 4.1 前后端同源部署（推荐）

- 解压并启动后，用户通过浏览器访问**单一地址**（如 `http://127.0.0.1:8080`）即可。
- **建议**：将 `frontend/dist` 作为 Spring Boot 的静态资源（`classpath:/static/` 或通过 `spring.web.resources` 指定），`/api/**` 仍为接口；前端 Axios 改用**相对路径** `baseURL: '/api'`，避免写死 `localhost`（也方便以后换端口或反向代理）。
- **CORS**：当前 `WebConfig` 主要放开 `localhost`；同源部署后可不再依赖浏览器跨域，逻辑更简单。

### 4.2 路由 History 与 SPA 刷新

- 使用 `createWebHistory()` 时，直接访问 `/bills` 等非根路径需要服务端**回退到 `index.html`**（典型 SPA fallback）。
- **建议**：在 Spring Boot 中增加：对非 `/api/**`、非静态文件路径的请求转发到 `index.html`（可用 `RouterFunction`、自定义 `WebMvcConfigurer` 或等价方式）。

### 4.3 持久化：仅使用 SQLite（数据目录与用户权限）

本应用**仅以 SQLite 为唯一数据库**（单机、本机浏览器访问），不引入 MySQL / PostgreSQL 等服务端数据库。

**为何坚持 SQLite**：零独立数据库服务运维；与本机安装包、`wechat-bill.db` 单文件备份契合；现有栈已为 `sqlite-jdbc` + MyBatis-Plus，无需为多库做额外抽象。

**连接与存放路径**

- **开发**：可继续使用内置相对路径 `jdbc:sqlite:./data/wechat-bill.db`（随进程**工作目录**变化）。
- **ZIP 绿色版**：**推荐解压到用户目录下可读写的路径**（如 `桌面\帐里说帐`、`D:\Apps\BillInBill`），此时启动脚本可将**工作目录**固定为解压根目录，`./data/wechat-bill.db` 即可正常使用，简单又符合「解压即用」。
- **若解压到 `Program Files` 等受保护目录**：仍可能无法创建 `./data/wechat-bill.db`，则需改为**指向用户可写路径的绝对 URL**（如 `%LOCALAPPDATA%\BillInBill\data\wechat-bill.db`），方式同下。

**落地的配置方式（择一即可）**

- 启动脚本：`cd /d %~dp0`（始终以压缩包解压根目录为工作目录），再 `java -jar ...`，使相对路径 `./data/` 指向压缩包内的 `data\`；
- 与 Fat JAR 同目录放置**外置** `application.properties` / `application.yml`，仅重写 `spring.datasource.url`；
- 启动参数：`--spring.datasource.url=jdbc:sqlite:<绝对路径>`（适合必须写 `%LOCALAPPDATA%` 的策略）；
- 若未来仍做 MSI：**安装程序**可把数据目录建在用户路径并写入外置配置；绿色版则用「解压位置 + start.bat」替代安装器逻辑。

**结构与演进**

- 建表继续使用 `classpath:db/schema.sql` 与现有的 `spring.sql.init.*`。
- 表结构升级仅针对 **SQLite 语法**保留（手写 SQL、`SqliteSchemaUpgradeConfig` 等）；**不包含**迁到其他数据库的双轨方案。

**运行期可选优化（按需）**

- JDBC URL 可带 `busy_timeout` 等参数，减轻短时写写冲突下的 `database is locked`；
- 应用启动后对连接执行 `PRAGMA journal_mode=WAL;`（常见桌面单机场景下有收益）；
- 向终端用户说明：**备份即复制 `wechat-bill.db`**；若需更强保障，再在应用层做导出/快照（仍指向同一 SQLite 文件语义）。

**能力边界（与说明书一致即可）**

- **不支持**：多机或多进程同时对**同一个**通过网络/SMB 打开的 `.db` 文件写入（易产生锁与文件损坏）。
- **适合**：同一台机器上**一个后端实例** + 前端访问；若将来要多端共享账本，宜单独立项，再评估是否仍为「服务端单机 + SQLite」或更换数据库，与当前产品定位区分开。

以上内容核心是：**解压目录具备写权限**（或由外置配置把 DB 指到 `%LOCALAPPDATA%`），并让启动脚本的**工作目录可控**。

---

## 5. ZIP 绿色版打包方案（主推）与备选

### 主方案：**Fat JAR + 便携运行时 + ZIP**

- **目标产物**：单个 `BillInBill-windows-amd64-{版本}.zip`（名称可自定），解压后双击 `start.bat` 启动。
- **包内典型组成**：
  - `README.txt`：访问地址、`8080` 端口说明、备份 `data\wechat-bill.db`、不要复制到多台机同时写一个库等；
  - `start.bat`：`cd /d %~dp0`，调用自带的 `jdk-21\bin\java.exe`（或 `%JAVA_HOME%`），执行 `java -jar lib\bill-in-bill-backend-*.jar`（参数可按需追加 `--spring.config.additional-location=...\config\`）；
  - `lib\*.jar`：Spring Boot Fat JAR（建议已内嵌前端 `dist`，见 §4）；
  - `jdk-21\`（或 `jre\`）：Windows x64 **Java 21** 解压版（遵守所选发行版的**再分发条款**）；若压缩包体积敏感，可在 README 要求用户本机安装 JDK 21 并仅分发 `jar + bat`，但「解压即用」体验会变差；
  - `data\`：空目录或带 `.gitkeep`；首次启动由 SQLite 写库；
  - 可选 `config\application.properties`：覆盖端口、`spring.datasource.url` 等，无需改 jar。
- **构建流水线**：CI/本地脚本顺序为：`npm ci && npm run build` → 将 `frontend/dist` 打入后端静态资源并完成 `mvn package` → 组 staging 目录并按上表拷贝 → `Compress-Archive` 或 `7z`/zip 打成最终 zip。
- **优点**：不打安装器、不改注册表；升级可「整包替换」或保留 `data\` + 替换 `lib\`/`jdk\`。
- **注意**：杀毒软件可能对解压型 `jdk` 误判；可考虑代码签名或对 zip 说明「首次解压需时间较长」。

### 备选 A：**jpackage 生成 MSI/exe**

- 适合需要写入开始菜单、控制面板卸载项、或与商业签名流程绑定的交付；仍依赖 §4 中同源 SPA、SQLite 路径等前置条件。
- 体积与构建复杂度高于 ZIP，不作为当前文档的主路径。

### 备选 B：**Inno Setup / NSIS 仅做「安装器外壳」**

- 实质内容与 ZIP 相近：解压到所选目录、`start.bat`、可选创建快捷方式；适合希望用户仍走「向导」但实际仍是绿色目录结构的场景。

**综合建议**：正式对外快速迭代优先 **ZIP 绿色版**；需要「像传统软件一样的安装向导」再上备选 B或 A。

### 5.5 已实现：ZIP 流水线与脚本

**产出位置**：仓库根目录下 **`dist-portable/`**（已写入根 `.gitignore`）。成功后生成形如 **`bill-in-bill-{pom-version}-windows-amd64.zip`** 的文件；解压后顶层为 **`BillInBill`** 文件夹，内含：

- `README.txt`（内容由 `packaging/templates/README-PORTABLE.txt` 复制）
- `start.bat`
- `lib/bill-in-bill-backend.jar`（Fat JAR，已内嵌构建时的前端静态资源）
- `data/`、`config/application.properties.example`（可选拷贝为 `config/application.properties` 覆盖配置）
- 可选 `jdk-21/`（仅在打包时传入 `-JdkPath` 时附带）

**流程步骤**

1. `frontend/`：`npm ci`（可加 `-SkipNpmCi` 跳过）→ `npm run build`
2. `backend/`：`mvn -DskipTests package`。`pom.xml` 中 **`maven-resources-plugin`** 在 `prepare-package` 将 **`../frontend/dist`** 拷贝到 **`target/classes/static`**，Spring Boot Repackage 打入 Fat JAR。
3. 脚本选取 `backend/target/` 下的 Spring Boot Jar，复制并重命名为 **`lib/bill-in-bill-backend.jar`**
4. 组装上文目录，`Compress-Archive` 写入 `dist-portable/`，默认删除中间目录 `_staging`（可用 `-KeepStaging` 保留排查）

**在 Windows 上一键打包**

```powershell
cd <仓库根目录>
powershell -ExecutionPolicy Bypass -File .\packaging\build-portable.ps1

# PowerShell Core（若已安装 pwsh）：pwsh .\packaging\build-portable.ps1

# 若在 zip 中捆绑便携 JDK（目录须包含 bin/java.exe）：
powershell -ExecutionPolicy Bypass -File .\packaging\build-portable.ps1 -JdkPath "D:\sdk\temurin-21-windows-x64\jdk-21.0.6+7"

powershell -ExecutionPolicy Bypass -File .\packaging\build-portable.ps1 -SkipNpmCi   # CI 已 npm ci 过可加速

powershell -ExecutionPolicy Bypass -File .\packaging\build-portable.ps1 -KeepStaging  # 保留 dist-portable\_staging
```

**打包机前置条件**：JDK 21、Maven、`node`/`npm` 均在 PATH。**未传 `-JdkPath`** 时 zip **不包含**运行时，解压方须在 PATH 中存在 **Java 21** 或自备 `jdk-21`（将便携 JDK 手动放进解压目录）。

**脚本与模板文件**：`packaging/build-portable.ps1`（入口）、`packaging/start.bat`、`packaging/templates/README-PORTABLE.txt`、`packaging/templates/data/.keep`、`packaging/config/application.properties.example`。

---

## 6. 建议的实施步骤清单（ZIP 为主线；与 §5.5 脚本一致）

1. **整合构建**：已实现由 **`frontend` → `vite build`、`backend` → `mvn package`** 串联，`maven-resources-plugin` 在打包前拷贝 `frontend/dist`，无需手工复制。
2. **前端 `baseURL`**：已实现生产构建走后端同源 **`/api`**（仍可设 `VITE_API_BASE_URL` 覆盖）。
3. **后端**：已实现静态资源 **`classpath:/static`** 与 **`SpaForwardingController`** 对应用路由页的 `forward:/index.html`。
4. **SQLite 与目录**：ZIP 内带 `data/`；`start.bat` 使用 **`cd /d %~dp0`**。
5. **组装与压缩**：执行 **`powershell -ExecutionPolicy Bypass -File .\packaging\build-portable.ps1`**（或使用 `pwsh`）；版本号取自 `backend/pom.xml` 的文件名。
6. **验证**：在未预装 JDK 的机器上解压带 `jdk-21` 的 zip，或未带 jdk 的机器上装好 Java 21，双击 **`start.bat`**，访问 `http://127.0.0.1:8080`，并做一次 `data/` 备份/删除验证。
7. **附带说明**：`README.txt` 已含端口与外置 **`config/application.properties`** 说明。

---

## 7. 验收标准（ZIP 交付物）

- **单个 zip** 解压到本机任意**可写路径**（按 README 建议）后，**无需运行安装向导**即可使用；
- 双击 **`start.bat`**（或等价启动器）后，控制台无致命错误且可在浏览器打开约定地址；
- **数据**落在解压目录下的 `data\`（或文档约定的绝对路径），用户可只靠复制该目录完成备份；
- **无需单独安装 JDK**（若压缩包内含 Java 21 运行时）；若采用精简包策略，须在 README 中明确依赖本机 JDK 21；
- `8080` 被占用时有可见提示或文档说明如何通过 `config\application.properties` 改端口。

---

## 8. ZIP 内推荐目录示例（占位）

解压后示意（名称可按项目习惯微调）：

```text
BillInBill/
  README.txt
  start.bat
  lib/
    bill-in-bill-backend.jar          # Fat JAR 固定命名，便于脚本与说明
  jdk-21/                    # Windows x64 便携 JDK，或改名为 jre
  data/                      # 可为空；SQLite 写入此目录（与相对 JDBC 对齐时）
  config/                    # 可选 application.properties 覆盖端口/数据库路径
```

`start.bat` 要点：**先切换工作目录至脚本所在目录**，再调用 Java；否则 `./data/wechat-bill.db` 会落到「当前双击时的目录」，导致找不到库或多份库。

---

## 9. 参考：当前关键配置摘录（开发态）

**后端端口与数据源**（`backend/src/main/resources/application.properties`）：

```properties
server.port=8080
spring.datasource.url=jdbc:sqlite:./data/wechat-bill.db
```

**前端 API**（`frontend/src/api/http.js`）要点：

```javascript
// 本地开发：默认 http://localhost:8080/api；生产构建：同源 /api。
// 可设环境变量 VITE_API_BASE_URL 覆盖。
```

Spring Boot **`SpaForwardingController`** 对 `/dashboard`、`/bills` 等路由 **`forward:/index.html`**，支持 History 模式下直接刷新。**打包产物已由 §5.5 脚本自动生成。**

---

*文档版本：ZIP 主方案已落地 Maven 拷贝前端 + SPA 转发 + PowerShell 打包脚本；产物见 `dist-portable/`。*
