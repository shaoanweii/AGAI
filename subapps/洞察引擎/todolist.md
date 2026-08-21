# 本地最小化启动 TODO

目标：在无法连接公司内网的情况下，先用本地最小环境跑起项目基础链路。

最小可用范围：

- 前端能打开。
- `voc-app-auth` 能启动。
- `voc-app-insights` 能启动。
- 可以本地登录。
- 登录后能获取菜单权限并进入主框架。

暂不追求：

- 报表全链路。
- ChatBI。
- 数据集成。
- Kafka 消费链路。
- StarRocks 宽表和物化视图。
- MinIO 文件上传。
- Milvus 向量检索。
- XXL-Job 定时任务。
- 第三方 AI / 飞书 / KTM / CAnswer。

## 0. 环境准备

- [x] 安装 JDK 17。当前已通过 Homebrew 安装 `openjdk@17`，路径为 `/opt/homebrew/opt/openjdk@17`。
- [x] 安装 Node.js。当前环境可用，但版本不是建议版本；检测到 Node 可用，项目建议 Node 20.x。
- [x] 安装 npm。当前环境可用。
- [x] 安装 Docker/Compose。Docker Desktop 因 sudo 交互失败未安装，已改用 Colima + Docker CLI + docker-compose。
- [ ] 安装 Git。
- [ ] 可选：安装 MySQL 客户端。
- [ ] 可选：安装 Redis 客户端。
- [ ] 可选：安装 Postman / Apifox。
- [ ] 可选：安装 IDEA。
- [ ] 可选：安装 VS Code。

检查命令：

```bash
java -version
node -v
npm -v
docker version
git --version
```

## 1. 端口规划

- [ ] 确认前端端口 `5175` 未占用。
- [ ] 确认 auth 服务端口 `8081` 未占用。
- [ ] 确认 insights 服务端口 `8086` 未占用。
- [ ] 确认 MySQL 端口 `3306` 未占用。
- [ ] 确认 Redis 端口 `6379` 未占用。

建议端口：

```text
前端 Vite:        5175
auth 服务:        8081
insights 服务:    8086
MySQL:           3306
Redis:           6379
```

## 2. Docker 最小基础设施

- [x] 在项目根目录创建 `docker-compose.local.yml`。
- [x] 配置 MySQL 8。
- [x] 配置 Redis 7 单机。
- [x] 启动 Docker 服务。当前使用 Colima。
- [x] 确认 MySQL 容器运行正常：`voc-mysql`。
- [x] 确认 Redis 容器运行正常：`voc-redis`。

建议 `docker-compose.local.yml`：

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: voc-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    command:
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-time-zone=+08:00
    volumes:
      - voc_mysql_data:/var/lib/mysql

  redis:
    image: redis:7
    container_name: voc-redis
    ports:
      - "6379:6379"

volumes:
  voc_mysql_data:
```

启动命令：

```bash
docker compose -f docker-compose.local.yml up -d
docker ps
```

## 3. MySQL 初始化

- [x] 创建 `vdp_ms_be` 数据库。
- [x] 创建 `vdp_ms_td` 数据库。
- [x] 导入 security 表结构。
- [x] 导入 security 初始化数据。
- [x] 导入 insights 基础表结构。
- [x] 导入 insights 权限表结构/数据。
- [x] 导入 insights 客户相关表结构/数据。
- [ ] 如果启动时报业务日志缺表，导入 bizlogs 表结构。
- [ ] 如果启动时报 alert 缺表，导入 alert 表结构。

建库 SQL：

```sql
CREATE DATABASE vdp_ms_be DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE vdp_ms_td DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

优先导入：

```text
后端/voc-service-security/voc-security-server/schema/mysql_create.sql
后端/voc-service-security/voc-security-server/schema/mysql_init.sql
后端/voc-service-insights/voc-insights-impl/schema/mysql_create.sql
后端/voc-service-insights/voc-insights-impl/schema/mysql_permission_create_step1.sql
后端/voc-service-insights/voc-insights-impl/schema/mysql_customer_create_step2.sql
```

按报错后补：

```text
后端/voc-service-bizlogs/voc-bizlogs-impl/schema/mysql_create.sql
后端/voc-service-insights/voc-insights-alert-impl/schema/mysql_create.sql
```

## 4. 后端 standalone 配置

原则：不要破坏已有 `local/dev/test/prod` 配置。新增 `standalone` profile，专门用于纯本地启动。

- [x] 新建 auth 的 `bootstrap-standalone.yml`。
- [x] 新建 insights 的 `bootstrap-standalone.yml`。
- [x] 新建或复制一套 `standalone` 配置资源。
- [x] 将 MySQL 地址改为 `localhost:3306`。
- [x] 将 MySQL 用户名改为 `root`。
- [x] 将 MySQL 密码改为本地密码，例如 `root`。
- [x] 将 Redis Cluster 改为 Redis 单机。
- [x] 将 Redis 地址改为 `localhost:6379`。
- [x] 将 Redis 密码改为本地密码 `root`，避免 Redisson 单机配置对空密码不兼容。
- [x] 确认 Nacos config/discovery 已关闭。
- [x] 暂时禁用或绕过 Kafka listener 自动启动。
- [x] 暂时将 StarRocks 数据源映射到本地 MySQL 占位。
- [x] 暂时将 MinIO 指向本地占位。
- [x] 暂时将 Milvus 指向本地占位。
- [x] 暂时关闭 XXL-Job admin 地址。
- [x] 将第三方服务配置为 mock/disabled/本地假地址。

重点参考文件：

```text
后端/voc-app/voc-app-auth/src/main/resources/bootstrap-local.yml
后端/voc-app/voc-app-insights/src/main/resources/bootstrap-local.yml
后端/voc-service-config/src/main/resources/local/common-mysql.yml
后端/voc-service-config/src/main/resources/local/common-security-mysql.yml
后端/voc-service-config/src/main/resources/local/voc-insights-redis.yml
后端/voc-service-config/src/main/resources/local/voc-security-redis.yml
后端/voc-service-config/src/main/resources/local/voc-insights-service.yml
后端/voc-service-config/src/main/resources/local/voc-security-service.yml
```

## 5. 启动 auth 服务

- [x] 执行 Gradle 依赖下载。
- [x] 启动 `voc-app-auth`。
- [ ] 解决首次编译错误。
- [x] 解决数据库连接错误。
- [x] 解决 Redis 连接错误。
- [ ] 解决 Jasypt/PBE 配置错误。
- [ ] 验证验证码接口。
- [x] 验证登录接口：`POST /auth/login` 可用，前端经 `/api/insights/base/login` 可登录。

启动命令：

```bash
cd /Users/Qiu./Desktop/洞察引擎/后端
./gradlew :voc-app:voc-app-auth:bootRun --args='--spring.profiles.active=standalone'
```

验收接口：

```text
GET  http://localhost:8081/randomImage/123
POST http://localhost:8081/login
POST http://localhost:8081/token/checkToken
```

## 6. 启动 insights 服务

- [x] 启动 `voc-app-insights`。
- [ ] 解决首次编译错误。
- [x] 解决数据库连接错误。
- [x] 解决 Redis 连接错误。
- [x] 解决缺表/缺列错误。已补 `sys_users.employee_id`、`sys_credentials.admin`、`ins_customer_permission`。
- [x] 解决 Kafka/StarRocks/MinIO/Milvus/XXL-Job 启动阻断。standalone 配置使用本地占位/禁用外部依赖。
- [ ] 验证用户信息接口。
- [x] 验证用户权限接口。携带 token 调用 `/api/insights/userPermissions` 返回 200。
- [x] 确认权限种子数据。已补本地角色、菜单、按钮、客户权限、用户角色关系；数据库递归查询可返回 13 个菜单节点和 8 个查看按钮。
- [x] 补本地部门/账号树最小数据。`/accountInfo/findDepartAccountTree` 返回 200，首页不再显示系统内部错误。

启动命令：

```bash
cd /Users/Qiu./Desktop/洞察引擎/后端
./gradlew :voc-app:voc-app-insights:bootRun --args='--spring.profiles.active=standalone'
```

验收接口：

```text
POST http://localhost:8086/insights/userInfo
POST http://localhost:8086/insights/userPermissions
```

## 7. 本地账号、角色、菜单种子数据

- [x] 确认本地用户存在。
- [x] 确认本地用户凭证存在。
- [x] 确认本地角色存在。
- [x] 确认用户和角色已关联。
- [x] 确认菜单权限存在。
- [x] 确认角色和菜单权限已关联。
- [x] 确认按钮权限不会阻断主流程。
- [x] 确认客户/client 数据存在。
- [x] 确认用户有默认 `clientId`。
- [x] 如现有 SQL 不完整，新增 `scripts/dev-seed-permissions.sql`。

最小菜单 `permissionKey`：

```text
dataCenter
dataCenter-dataQuery
knowledgeCenter
knowledgeCenter-corpusMapping
review
review-errorCorrection
rules
rules-closedLoopRules
rules-rulesTest
settings
settings-accountManagement
settings-role
settings-download
```

前端动态路由依赖后端返回的 `permissionKey` 与前端 route `name` 一致。

## 8. 前端本地启动

- [x] 安装前端依赖。已通过 `npm install --cache ../.npm-cache` 安装成功。
- [ ] 启动 Vite。
- [x] 修改前端代理到本地服务。
- [ ] 打开页面确认能加载。
- [ ] 确认登录页可访问。
- [ ] 确认请求不再转发到 `172.16.*`。

启动命令：

```bash
cd /Users/Qiu./Desktop/洞察引擎/前端
npm install
npm run dev
```

建议代理：

```text
/api/auth     -> http://localhost:8081
/api/insights -> http://localhost:8086
/api          -> http://localhost:8086
```

已新增：

```text
前端/.env.standalone
package script: npm run dev:standalone
```

重点参考文件：

```text
前端/vite.config.mts
前端/.env.development
前端/src/api/index.ts
前端/src/api/main.ts
```

## 9. 前后端登录联调

- [ ] 确认前端登录接口实际路径。
- [ ] 如果登录走 `/insights/base/login`，确认 insights 是否能处理。
- [ ] 如果 insights 登录依赖 auth 失败，临时将前端登录改为 auth `/login`。
- [ ] 登录成功后保存 token。
- [ ] 请求 `userPermissions` 自动携带 `Authorization: Bearer <token>`。
- [ ] 前端能收到 `menus`。
- [ ] 前端能动态添加路由。
- [ ] 页面进入主框架。
- [ ] 侧边栏显示菜单。

前端当前登录相关接口：

```text
POST /insights/base/login
POST /insights/logout
POST /insights/userPermissions
POST /insights/userInfo
```

## 10. 最小验收标准

- [x] `docker ps` 能看到 `voc-mysql`。
- [x] `docker ps` 能看到 `voc-redis`。
- [x] MySQL 可连接。
- [x] Redis 可连接。
- [x] `voc-app-auth` 在 `8081` 启动成功。
- [x] `voc-app-insights` 在 `8086` 启动成功。
- [x] 前端在 `5175` 启动成功。
- [x] 浏览器能打开前端。
- [x] 登录接口返回 token。
- [x] `userPermissions` 返回菜单。
- [ ] 主框架和侧边菜单可见。
- [ ] 刷新页面后仍能保持登录状态或正常回登录页。

## 11. 后置模块恢复顺序

最小链路跑通后，再按下面顺序恢复业务模块：

- [ ] 系统设置：账号管理。
- [ ] 系统设置：角色管理。
- [ ] 系统设置：下载管理。
- [ ] 知识中心：语料映射。
- [ ] 知识中心：关键词库。
- [ ] 知识中心：标准观点。
- [ ] 知识中心：体验代码。
- [ ] 知识中心：属性标签。
- [ ] 知识中心：用车场景。
- [ ] 知识中心：品牌车系。
- [ ] 规则引擎：闭环规则。
- [ ] 规则引擎：规则测试。
- [ ] 审核管理：纠错审核。
- [ ] 数据治理：数据查询。
- [ ] 数据治理：新词发现。
- [ ] 报表服务。
- [ ] ChatBI。
- [ ] 数据集成。
- [ ] 分析任务。

每恢复一个模块时，按以下检查：

- [ ] 定位前端 API 文件。
- [ ] 定位后端 Controller。
- [ ] 定位后端 Service/Impl。
- [ ] 定位 Mapper/XML。
- [ ] 确认依赖表。
- [ ] 补充初始化数据。
- [ ] 确认是否依赖 Redis。
- [ ] 确认是否依赖 Kafka。
- [ ] 确认是否依赖 StarRocks。
- [ ] 确认是否依赖 MinIO。
- [ ] 确认是否依赖第三方服务。
- [ ] 添加本地 mock 或禁用策略。

## 12. 暂时不处理清单

以下内容不进入最小化启动范围：

- [ ] `voc-app-report`
- [ ] `voc-app-chat-bi`
- [ ] `voc-app-analysis`
- [ ] `voc-app-data-integration`
- [ ] `voc-app-risk`
- [ ] StarRocks / SelectDB 建库和物化视图。
- [ ] Kafka topic 和消费任务。
- [ ] MinIO 文件上传下载。
- [ ] Milvus 向量数据库。
- [ ] XXL-Job 调度中心。
- [ ] Nacos 注册发现和配置中心。
- [ ] SkyWalking 链路追踪。
- [ ] Spring Boot Admin。
- [ ] Sentinel 控流。
- [ ] Feishu 消息通知。
- [ ] 智谱 AI / 火山 / CAnswer / KTM。

## 13. 问题记录

启动过程中按下面格式记录问题：

```text
时间：
模块：
命令：
错误摘要：
完整日志位置：
判断原因：
处理方案：
处理结果：
是否阻断：
```

### 2026-06-05 当前阻断点

```text
时间：2026-06-05
模块：本地基础设施
命令：docker compose -f docker-compose.local.yml up -d
错误摘要：docker: command not found
判断原因：当前机器未安装 Docker CLI，或 Codex 环境无法访问 Docker。
处理方案：安装 Docker Desktop，确认 `docker version` 可用。
处理结果：待处理。
是否阻断：阻断 MySQL/Redis 本地启动。
```

```text
时间：2026-06-05
模块：后端
命令：java -version / ./gradlew --version
错误摘要：Unable to locate a Java Runtime
判断原因：当前机器未安装 JDK 17。
处理方案：安装 JDK 17，并设置 JAVA_HOME。
处理结果：待处理。
是否阻断：阻断后端编译和启动。
```

```text
时间：2026-06-05
模块：前端
命令：npm install --cache ../.npm-cache
错误摘要：第一次网络解析失败；清理损坏 node_modules 后重新安装成功。
判断原因：首次安装受网络解析影响，并留下半安装目录。
处理方案：删除生成物 node_modules 后，使用项目内 npm cache 重装。
处理结果：已安装 716 packages。
是否阻断：否。
```

```text
时间：2026-06-05
模块：前端
命令：npm run type-check
错误摘要：无。
判断原因：前端 TypeScript/Vite 配置可通过类型检查。
处理方案：无。
处理结果：通过。
是否阻断：否。
```

```text
时间：2026-06-05
模块：前端
命令：npm run build-only -- --mode standalone
错误摘要：无构建错误；存在既有 ESLint 未使用变量 warning。
判断原因：standalone Vite 配置可被完整构建链路接受。
处理方案：warning 不影响最小本地启动，后续可单独清理。
处理结果：通过。
是否阻断：否。
```

```text
时间：2026-06-05
模块：前端
命令：npm run dev:standalone
错误摘要：listen EPERM: operation not permitted 127.0.0.1:5175
判断原因：当前 Codex 沙箱不允许监听本地端口。
处理方案：在用户本机普通终端执行 npm run dev:standalone。
处理结果：待在本机终端验证。
是否阻断：仅阻断 Codex 沙箱内启动，不阻断配置交付。
```

## 14. 推荐里程碑

- [ ] M1：Docker MySQL + Redis 启动成功。
- [ ] M2：数据库基础表导入成功。
- [ ] M3：auth 服务启动成功。
- [ ] M4：auth 登录接口返回 token。
- [ ] M5：insights 服务启动成功。
- [ ] M6：`userPermissions` 返回菜单。
- [ ] M7：前端启动成功。依赖安装和类型检查已通过；Codex 沙箱内启动 Vite 被端口监听权限阻断。
- [ ] M8：前端登录成功。
- [ ] M9：主框架和侧边菜单显示成功。
- [ ] M10：第一个业务页面接口跑通。
