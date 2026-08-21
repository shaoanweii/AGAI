# 本地最小化启动说明

目标：在不连接公司内网的情况下，启动最小本地链路。

最小链路包括：

- MySQL 8
- Redis 7 单机
- `voc-app-auth`
- `voc-app-insights`
- 前端 Vite

## 1. 启动基础设施

先检查本机依赖：

```bash
java -version
docker version
node -v
npm -v
```

要求：

- JDK 17
- Docker Desktop，或 Colima + Docker CLI
- Node.js 20.x，当前项目 README 建议 `20.11.0`

```bash
docker-compose -p voc-local -f docker-compose.local.yml up -d
docker ps
```

MySQL root 密码：`root`

Redis 密码：`root`

## 2. 初始化数据库

```bash
bash scripts/init-local-db.sh
```

脚本会创建：

- `vdp_ms_be`
- `vdp_ms_td`

并导入 security / insights 的最小 SQL。

## 3. 启动 auth

```bash
cd 后端
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH \
./gradlew :voc-app:voc-app-auth:bootRun --args='--spring.profiles.active=standalone'
```

端口：`8081`

## 4. 启动 insights

另开一个终端：

```bash
cd 后端
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH \
./gradlew :voc-app:voc-app-insights:bootRun --args='--spring.profiles.active=standalone'
```

端口：`8086`

## 5. 启动前端

另开一个终端：

```bash
cd 前端
npm install
npm run dev:standalone -- --host 127.0.0.1 --port 5173
```

端口：`5173`

`standalone` 模式会把代理指向：

- `/api/auth` -> `http://localhost:8081`
- `/api` -> `http://localhost:8086`
- `/api/ai` -> `http://localhost:8086`
- `/api/new-words` -> `http://localhost:8086`

## 6. 最小验收

- `http://localhost:5175` 能打开。
- `http://127.0.0.1:5173` 能打开。
- `http://localhost:8081/actuator` 返回 200。
- `http://localhost:8086/actuator` 返回 200。
- `POST http://127.0.0.1:5173/api/insights/base/login` 使用 `admin / Passw0rd@!` 能返回 token。
- `POST http://127.0.0.1:5173/api/insights/userPermissions` 携带 token 能返回 200。
- 前端请求不再转发到 `172.16.*`。

默认本地账号：

```text
用户名：admin
密码：Passw0rd@!
验证码：2587
```

## 7. 已知边界

本地 `standalone` 配置只服务于最小启动，不保证以下模块可用：

- 报表
- ChatBI
- Kafka 异步链路
- StarRocks 查询
- MinIO 文件上传
- Milvus
- XXL-Job
- 第三方 AI / 飞书 / CAnswer / KTM

当前本地权限种子已补齐最小菜单链路；`userPermissions` 可返回 13 个菜单节点和 8 个查看按钮。seed 也会补最小部门/账号树数据，`/accountInfo/findDepartAccountTree` 可返回本地默认部门和 admin。若修改 seed 后仍看到旧菜单或乱码，删除 Redis db1 中 `VDP_:voc-app-insights:users:1:*:perms:*` 权限缓存后重新请求。
