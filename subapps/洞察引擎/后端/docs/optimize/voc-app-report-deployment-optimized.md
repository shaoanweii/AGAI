# voc-app-report Deployment 压测重启问题分析与优化方案

## 一、压测重启根本原因分析

### 问题 1：存活探针（livenessProbe）配置过于激进 ⚠️ 主因

| 参数 | 当前值 | 风险 |
|------|--------|------|
| `timeoutSeconds` | 1s | 压测时 JVM GC 暂停（Stop-The-World）或 CPU 打满，健康接口 1 秒内无响应即超时 |
| `failureThreshold` | 3 | 仅连续失败 3 次（约 18 秒）就触发 Pod 重启，容忍度极低 |
| `periodSeconds` | 6 | 探测过于频繁，在高负载下本身也消耗资源 |

**结论**：压测时 JVM 忙于处理请求/GC，健康检查响应变慢 → 探针超时 → 累计3次 → K8s 杀掉 Pod 重启。

---

### 问题 2：JVM 内存配置与容器限制比例不合理 ⚠️ 次因（OOMKilled）

```
容器内存上限：6Gi ≈ 6144 MB

JVM 实际占用估算：
  -Xmx4096M           = 4096 MB  (Java 堆上限)
  -XX:MaxDirectMemorySize=1G = 1024 MB  (堆外直接内存)
  Metaspace           ≈  512 MB  (类元数据，默认无上限)
  线程栈 -Xss512k × N ≈  256 MB+ (线程数越多越大)
  JVM 内部开销         ≈  256 MB+
  ─────────────────────────────────
  合计预估             ≈ 6144 MB+ ← 与限制齐平，压测时极易 OOMKilled
```

**结论**：压测时线程数/堆外内存使用量上涨，总内存超过 6Gi 限制，操作系统直接 Kill 进程 → Pod 以 OOMKilled 状态重启。

---

### 问题 3：JVM GC 参数未适配 Java 17 ⚠️ 性能因素

- `-XX:NewRatio=4` / `-XX:SurvivorRatio=8` / `-XX:MaxTenuringThreshold=15` 是 CMS/ParallelGC 时代的老式参数
- Java 17 默认使用 **G1GC**，上述参数与 G1GC 存在冲突，会导致 GC 暂停时间变长，加重探针超时风险
- 缺少显式 GC 目标配置（如 `MaxGCPauseMillis`）

---

### 问题 4：就绪探针（readinessProbe）与存活探针配置相同

- readinessProbe 应该比 livenessProbe 更敏感（快速从负载均衡摘除），但不应触发重启
- 两者使用完全相同配置，失去分层保护的意义

---

### 问题 5：优雅关机时间不足

- `terminationGracePeriodSeconds: 30` 在压测场景中，30 秒内可能无法处理完已接入的请求，导致请求丢失

---

## 二、优化后的 Deployment YAML

```yaml
kind: Deployment
apiVersion: apps/v1
metadata:
  name: voc-app-report
  namespace: voc-test
spec:
  replicas: 4
  selector:
    matchLabels:
      app: voc-app-report

  strategy:
    type: RollingUpdate
    rollingUpdate:
      # 压测期间滚动更新时最多允许 1 个 Pod 不可用，降低流量冲击
      maxUnavailable: 1
      # 最多允许超出 1 个副本，控制发布期间的资源用量
      maxSurge: 1

  template:
    metadata:
      labels:
        app: voc-app-report
    spec:
      # ----------------------------------------------------------------
      # 【优化5】优雅关机：给 JVM 足够时间处理完已进入的请求再关闭
      # 原值 30s → 改为 60s，压测场景下请求处理链路更长
      # ----------------------------------------------------------------
      terminationGracePeriodSeconds: 60

      volumes:
        - name: localtime
          hostPath:
            path: /etc/localtime
            type: ''
        - name: volume-voc-app
          emptyDir: {}
        - name: volume-voc-data
          hostPath:
            path: /data/volume-voc-data
            type: ''

      initContainers:
        - name: copy-jar
          image: >-
            gaia.changan.com.cn/changan/yinxiao-docker/snapshot/voc-app-report:2026.03.10-094449-238
          command:
            - cp
            - /tmp/voc-app-report.jar
            - /voc-app/voc-app-report.jar
          resources: {}
          volumeMounts:
            - name: localtime
              readOnly: true
              mountPath: /etc/localtime
            - name: volume-voc-app
              mountPath: /voc-app
          imagePullPolicy: IfNotPresent

      containers:
        - name: voc-app-report
          image: >-
            gaia.changan.com.cn/changan/yinxiao-docker/snapshot/openjdk:jdk17-sw-ag-9.1.0-chromium
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
              protocol: TCP

          env:
            - name: APP_NAME
              value: voc-app-report
            - name: JAVA_OPTS
              value: >-
                -Dspring.profiles.active=test
                -Dserver.port=8080
                -Dfile.encoding=UTF-8
                -server

                -----------------------------------------------------------------
                【优化2】JVM 堆内存 — 与容器限制留出至少 1.5~2G 安全余量
                  容器限制: 6Gi ≈ 6144MB
                  Xmx:       3584MB  (3.5G)
                  DirectMem: 1024MB  (1G)
                  Metaspace:  256MB  (显式封顶，防止无限增长)
                  其余开销:  ~1280MB  安全余量
                  合计:       6144MB  ≤ 容器上限，OOMKilled 风险大幅降低
                -----------------------------------------------------------------
                -Xmx3584M
                -Xms2048M
                -Xmn768m

                -----------------------------------------------------------------
                【优化3】JVM GC — 适配 Java 17 的 G1GC 显式配置
                  移除不兼容的 CMS 时代参数：NewRatio/SurvivorRatio/MaxTenuringThreshold
                  改用 G1GC 目标导向参数，减少 STW 暂停时间，降低探针超时概率
                -----------------------------------------------------------------
                -XX:+UseG1GC
                -XX:MaxGCPauseMillis=200
                -XX:G1HeapRegionSize=16m
                -XX:InitiatingHeapOccupancyPercent=45

                -----------------------------------------------------------------
                【优化2续】Metaspace 上限 — 防止类加载无限占用内存
                -----------------------------------------------------------------
                -XX:MetaspaceSize=256m
                -XX:MaxMetaspaceSize=512m

                -Xss512k
                -XX:MaxDirectMemorySize=1G
                -XX:+DisableExplicitGC

                -----------------------------------------------------------------
                【诊断】OOMKilled 时自动转储堆快照，便于排查内存问题
                -----------------------------------------------------------------
                -XX:+HeapDumpOnOutOfMemoryError
                -XX:HeapDumpPath=/voc-data/heapdump.hprof

                -Dmoresec.agent.name=voc-app-report
                -javaagent:/voc-data/iast_agent.jar
                --add-opens=java.base/java.util=ALL-UNNAMED
                --add-opens=java.base/java.lang=ALL-UNNAMED
                --add-opens=java.base/java.lang.invoke=ALL-UNNAMED
                --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED
                --add-opens=java.base/java.nio.charset=ALL-UNNAMED
                --add-opens=java.base/java.net=ALL-UNNAMED
                --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED

          # ----------------------------------------------------------------
          # 【优化2】资源配额 — limits.memory 与 JVM 内存参数保持一致
          # ----------------------------------------------------------------
          resources:
            requests:
              cpu: 500m       # 适当提高 request，保证调度到资源充足的节点
              memory: 2Gi
            limits:
              cpu: '4'        # 压测场景放开 CPU 上限，避免 CPU Throttle 拖慢响应
              memory: 6Gi

          volumeMounts:
            - name: localtime
              readOnly: true
              mountPath: /etc/localtime
            - name: volume-voc-app
              mountPath: /voc-app
            - name: volume-voc-data
              mountPath: /voc-data

          # ----------------------------------------------------------------
          # 【优化1】启动探针 — 给 JVM 足够的预热时间，避免启动阶段误重启
          # 最长等待 = initialDelaySeconds + periodSeconds × failureThreshold
          #           = 90 + 15 × 20 = 390 秒（约 6.5 分钟）
          # ----------------------------------------------------------------
          startupProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
              scheme: HTTP
            initialDelaySeconds: 90   # 容器启动后等待 90s 再开始探测
            timeoutSeconds: 10        # 单次探测超时：原 6s → 10s，适应启动阶段慢响应
            periodSeconds: 15         # 探测间隔：原 10s → 15s
            successThreshold: 1
            failureThreshold: 20      # 允许失败次数：原 10 → 20，给 JVM 充足预热时间

          # ----------------------------------------------------------------
          # 【优化1】存活探针 — 核心优化，解决压测重启的主因
          #
          # 原配置问题：timeoutSeconds=1 + failureThreshold=3 + periodSeconds=6
          #   → 18秒内若 JVM 因 GC/高负载响应慢，即触发重启
          #
          # 优化目标：宽容短暂的高负载抖动，只在真正长时间不可用时才重启
          #   → 容忍窗口 = periodSeconds × failureThreshold = 20 × 6 = 120 秒
          #   → 必须连续 120 秒内 6 次探测全部超过 10s 不响应，才触发重启
          # ----------------------------------------------------------------
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
              scheme: HTTP
            initialDelaySeconds: 90   # 与 startupProbe 结束时间对齐，避免双重探测干扰
            timeoutSeconds: 10        # 原 1s → 10s：容忍 GC 暂停/高负载下的响应延迟
            periodSeconds: 20         # 原 6s → 20s：降低探测频率，减少对服务的额外压力
            successThreshold: 1
            failureThreshold: 6       # 原 3 → 6：需连续失败 6 次（共 120s）才触发重启

          # ----------------------------------------------------------------
          # 【优化4】就绪探针 — 与存活探针分离，快速感知负载过高时从 LB 摘除
          #
          # 就绪探针失败 → Pod 从 Service 端点摘除（不重启，只是不接新流量）
          # 就绪探针应比存活探针更敏感，能快速保护下游，但不触发 Pod 重启
          # ----------------------------------------------------------------
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness   # 建议使用独立的 readiness 端点
              port: 8080
              scheme: HTTP
            initialDelaySeconds: 90
            timeoutSeconds: 5         # 比 livenessProbe 更严格，快速感知服务是否就绪
            periodSeconds: 10         # 比 livenessProbe 检查更频繁
            successThreshold: 1
            failureThreshold: 3       # 失败 3 次（30s）即从 LB 摘除，但不重启

      restartPolicy: Always
      dnsPolicy: ClusterFirst
      imagePullSecrets:
        - name: changan-registry-secret
      schedulerName: default-scheduler

  revisionHistoryLimit: 10
  progressDeadlineSeconds: 600
```

---

## 三、关键参数变更对比表

| 配置项 | 原值 | 优化值 | 变更原因 |
|--------|------|--------|----------|
| **livenessProbe.timeoutSeconds** | 1s | **10s** | 原值太短，JVM GC 暂停即超时，是重启主因 |
| **livenessProbe.periodSeconds** | 6s | **20s** | 降低探测频率，减少高负载下的额外开销 |
| **livenessProbe.failureThreshold** | 3 | **6** | 容忍窗口从 18s 延长到 120s，避免短暂抖动触发重启 |
| **readinessProbe** | 同 liveness | **独立配置** | 分离就绪/存活语义，就绪摘除流量而不重启 |
| **startupProbe.failureThreshold** | 10 | **20** | 给 JVM 更充足的预热时间 |
| **-Xmx** | 4096M | **3584M** | 与 DirectMemory+Metaspace 合计不超过容器限制 |
| **-Xms** | 1024M | **2048M** | 提高初始堆，减少 GC 扩堆次数 |
| **GC 参数** | 旧式 NewRatio/SurvivorRatio | **G1GC 显式配置** | 适配 Java 17，减少 GC STW 暂停时间 |
| **MetaspaceSize** | 未配置 | **256m/512m** | 防止 Metaspace 无限增长导致 OOM |
| **resources.cpu limit** | 2 | **4** | 压测时放开 CPU 上限，避免 Throttle 拖慢响应 |
| **resources.cpu request** | 250m | **500m** | 保证调度到资源充足节点 |
| **terminationGracePeriodSeconds** | 30s | **60s** | 给压测中的长链路请求足够的处理时间 |
| **HeapDumpOnOutOfMemoryError** | 未配置 | **已添加** | OOM 时自动转储，便于事后分析 |

---

## 四、Spring Boot 配套建议

确保 `application.yml` 中已正确暴露 `readiness` 端点（适配优化4中的独立就绪端点）：

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true        # 启用 liveness/readiness 子路径
      show-details: never
  endpoints:
    web:
      exposure:
        include: health
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

如果不想修改 Spring Boot 配置，可将 readinessProbe 的路径也改回 `/actuator/health/liveness`，但仍需保持与 livenessProbe 的探测参数分离。

---

## 五、验证方法

压测前检查 Pod 状态：
```bash
# 观察探针状态和重启次数
kubectl get pods -n voc-test -l app=voc-app-report -w

# 查看 Pod 事件，确认是否还有 Liveness probe failed
kubectl describe pod <pod-name> -n voc-test | grep -A5 Events

# 查看 JVM 实际内存占用
kubectl exec -n voc-test <pod-name> -- java -XX:+PrintFlagsFinal -version 2>&1 | grep -E "MaxHeapSize|MetaspaceSize"
```
