# VOC_MS_TD Kafka消费配置文档

## 一、Kafka消费任务概览

### 1.1 任务基本信息

| 配置项 | 值 | 说明 |
|--------|-----|------|
| **任务ID** | 1761480772739 | Routine Load任务唯一标识 |
| **任务名称** | dwd_voc_all_meta_data_kafka | Kafka消费任务名称 |
| **创建时间** | 2025-10-28 17:24:42 | 任务创建时间 |
| **数据库** | voc_ms_td | 目标数据库 |
| **目标表** | dwd_voc_all_meta_data | 数据写入的目标表 |
| **任务状态** | RUNNING | 当前运行状态 |
| **数据源类型** | KAFKA | 数据源为Kafka |
| **当前任务数** | 1 | 当前并发消费任务数 |
| **创建用户** | root | 任务创建者 |

---

## 二、Kafka连接配置

### 2.1 Kafka集群信息

```properties
# Kafka Broker地址
brokerList: 172.16.80.16:30092

# Kafka Topic
topic: VDP_dwd_voc2_all_meta_data

# 当前消费的Kafka分区
currentKafkaPartitions: 0

# Consumer Group ID
group.id: VDP-voc2-analysis

# 默认消费位置
kafka_default_offsets: OFFSET_BEGINNING
```

### 2.2 消费进度

```json
{
  "Progress": {
    "0": "36"  // 分区0当前消费到offset 36
  },
  "Lag": {
    "0": 0     // 分区0消费延迟为0，表示已消费到最新
  }
}
```

---

## 三、数据加载配置

### 3.1 批次配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| **max_batch_rows** | 20000000 | 单批次最大行数（2000万行） |
| **max_batch_size** | 1073741824 | 单批次最大数据量（1GB） |
| **max_batch_interval** | 60 | 批次最大间隔时间（60秒） |

**说明**: 满足以上任一条件即触发数据导入

### 3.2 并发配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| **desired_concurrent_number** | 5 | 期望并发数 |
| **current_concurrent_number** | 1 | 当前实际并发数 |
| **send_batch_parallelism** | 1 | 发送批次并行度 |

### 3.3 内存配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| **exec_mem_limit** | 2147483648 | 执行内存限制（2GB） |

---

## 四、数据格式配置

### 4.1 格式类型

```properties
format: json
json_root: ""
strip_outer_array: false
num_as_string: false
fuzzy_parse: false
```

### 4.2 JSON字段映射

**JSONPath配置**:
```json
[
  "$.id",
  "$.dataId",
  "$.oneId",
  "$.workId",
  "$.clientId",
  "$.channelId",
  "$.contentType",
  "$.title",
  "$.content",
  "$.userName",
  "$.data",
  "$.done",
  "$.dataStatus",
  "$.modelType",
  "$.extFields",
  "$.bizExtAttrs",
  "$.bizExtAttrs2",
  "$.bizExtAttrs3",
  "$.publishTime",
  "$.createTime"
]
```

**字段映射关系**:
```
Kafka JSON字段 → SelectDB表字段

$.id            → id
$.dataId        → data_id
$.oneId         → one_id
$.workId        → work_id
$.clientId      → client_id
$.channelId     → channel_id
$.contentType   → content_type
$.title         → title
$.content       → content
$.userName      → user_name
$.data          → data
$.done          → done
$.dataStatus    → data_status
$.modelType     → model_type
$.extFields     → ext_fields
$.bizExtAttrs   → biz_ext_attrs
$.bizExtAttrs2  → biz_ext_attrs2
$.bizExtAttrs3  → biz_ext_attrs3
$.publishTime   → publish_time
$.createTime    → create_time
```

---

## 五、数据质量配置

### 5.1 错误处理

| 配置项 | 值 | 说明 |
|--------|-----|------|
| **strict_mode** | false | 非严格模式，允许部分数据错误 |
| **max_error_number** | 0 | 最大错误行数（0表示不限制） |
| **max_filter_ratio** | 1.0 | 最大过滤比例（100%） |

### 5.2 数据处理

| 配置项 | 值 | 说明 |
|--------|-----|------|
| **merge_type** | APPEND | 数据合并类型（追加模式） |
| **partial_columns** | false | 不支持部分列更新 |
| **load_to_single_tablet** | false | 不强制加载到单个tablet |

### 5.3 过滤条件

```properties
whereExpr: *           # WHERE条件（*表示无过滤）
precedingFilter: *     # 前置过滤（*表示无过滤）
partitions: *          # 目标分区（*表示所有分区）
```

---

## 六、统计信息

### 6.1 实时统计

```json
{
  "receivedBytes": 0,           // 接收字节数
  "receivedBytesRate": 0,       // 接收字节速率
  "loadedRows": 0,              // 已加载行数
  "loadRowsRate": 0,            // 加载行速率
  "totalRows": 0,               // 总行数
  "errorRows": 0,               // 错误行数
  "errorRowsAfterResumed": 0,   // 恢复后错误行数
  "unselectedRows": 0,          // 未选中行数
  "committedTaskNum": 0,        // 已提交任务数
  "abortedTaskNum": 0,          // 已中止任务数
  "runningTxns": [],            // 运行中的事务
  "taskExecuteTimeMs": 1        // 任务执行时间（毫秒）
}
```

---

## 七、Kafka消息格式示例

### 7.1 标准JSON消息格式

```json
{
  "id": "uuid-string",
  "dataId": "data-id-string",
  "oneId": "customer-one-id",
  "workId": "work-order-id",
  "clientId": "client-id",
  "channelId": "channel-id",
  "contentType": "text",
  "title": "标题内容",
  "content": "正文内容",
  "userName": "用户名",
  "data": "原始数据",
  "done": 1,
  "dataStatus": 1,
  "modelType": 1,
  "extFields": "{\"field1\":\"value1\"}",
  "bizExtAttrs": "{\"attr1\":\"value1\"}",
  "bizExtAttrs2": "{\"attr2\":\"value2\"}",
  "bizExtAttrs3": "{\"attr3\":\"value3\"}",
  "publishTime": "2025-01-01 12:00:00",
  "createTime": "2025-01-01 12:00:00"
}
```

---

## 八、数据流向

### 8.1 完整数据流

```
Kafka Topic: VDP_dwd_voc2_all_meta_data
    ↓
Kafka Broker: 172.16.80.16:30092
    ↓
Consumer Group: VDP-voc2-analysis
    ↓
Routine Load Task: dwd_voc_all_meta_data_kafka
    ↓
SelectDB Database: voc_ms_td
    ↓
Target Table: dwd_voc_all_meta_data
```

### 8.2 数据处理流程

```
1. Kafka消息接收
   ↓
2. JSON解析（按JSONPath提取字段）
   ↓
3. 字段映射（驼峰转下划线）
   ↓
4. 数据验证（非严格模式）
   ↓
5. 批次聚合（满足批次条件）
   ↓
6. 数据写入（APPEND模式）
   ↓
7. 提交Offset
```

---

## 九、创建Routine Load任务SQL

### 9.1 完整创建语句

```sql
CREATE ROUTINE LOAD voc_ms_td.dwd_voc_all_meta_data_kafka ON dwd_voc_all_meta_data
COLUMNS(
    id,
    data_id,
    one_id,
    work_id,
    client_id,
    channel_id,
    content_type,
    title,
    content,
    user_name,
    data,
    done,
    data_status,
    model_type,
    ext_fields,
    biz_ext_attrs,
    biz_ext_attrs2,
    biz_ext_attrs3,
    publish_time,
    create_time
)
PROPERTIES
(
    "desired_concurrent_number" = "5",
    "max_batch_interval" = "60",
    "max_batch_rows" = "20000000",
    "max_batch_size" = "1073741824",
    "strict_mode" = "false",
    "format" = "json",
    "jsonpaths" = "[
        \"$.id\",
        \"$.dataId\",
        \"$.oneId\",
        \"$.workId\",
        \"$.clientId\",
        \"$.channelId\",
        \"$.contentType\",
        \"$.title\",
        \"$.content\",
        \"$.userName\",
        \"$.data\",
        \"$.done\",
        \"$.dataStatus\",
        \"$.modelType\",
        \"$.extFields\",
        \"$.bizExtAttrs\",
        \"$.bizExtAttrs2\",
        \"$.bizExtAttrs3\",
        \"$.publishTime\",
        \"$.createTime\"
    ]",
    "strip_outer_array" = "false",
    "timezone" = "Asia/Shanghai"
)
FROM KAFKA
(
    "kafka_broker_list" = "172.16.80.16:30092",
    "kafka_topic" = "VDP_dwd_voc2_all_meta_data",
    "kafka_partitions" = "0",
    "property.group.id" = "VDP-voc2-analysis",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);
```

---

## 十、运维管理命令

### 10.1 查看任务状态

```sql
-- 查看所有Routine Load任务
SHOW ROUTINE LOAD;

-- 查看指定任务详情
SHOW ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka;

-- 查看任务详细信息（包括错误日志）
SHOW ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka\G
```

### 10.2 任务控制

```sql
-- 暂停任务
PAUSE ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka;

-- 恢复任务
RESUME ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka;

-- 停止任务
STOP ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka;
```

### 10.3 修改任务配置

```sql
-- 修改并发数
ALTER ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka
PROPERTIES
(
    "desired_concurrent_number" = "10"
);

-- 修改批次大小
ALTER ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka
PROPERTIES
(
    "max_batch_rows" = "30000000",
    "max_batch_size" = "2147483648"
);
```

---

## 十一、监控指标

### 11.1 关键监控指标

| 指标 | 说明 | 告警阈值建议 |
|------|------|-------------|
| **State** | 任务状态 | 非RUNNING状态告警 |
| **Lag** | 消费延迟 | >1000告警 |
| **errorRows** | 错误行数 | >100告警 |
| **loadRowsRate** | 加载速率 | <预期值50%告警 |
| **taskExecuteTimeMs** | 任务执行时间 | >60000ms告警 |

### 11.2 监控查询

```sql
-- 查看消费延迟
SELECT 
    Name,
    State,
    JSON_EXTRACT(Lag, '$.0') as partition_0_lag,
    JSON_EXTRACT(Progress, '$.0') as partition_0_offset
FROM information_schema.routine_load_jobs
WHERE Name = 'dwd_voc_all_meta_data_kafka';

-- 查看加载统计
SELECT 
    Name,
    JSON_EXTRACT(Statistic, '$.loadedRows') as loaded_rows,
    JSON_EXTRACT(Statistic, '$.errorRows') as error_rows,
    JSON_EXTRACT(Statistic, '$.loadRowsRate') as load_rate
FROM information_schema.routine_load_jobs
WHERE Name = 'dwd_voc_all_meta_data_kafka';
```

---

## 十二、故障排查

### 12.1 常见问题

#### 问题1: 任务状态为PAUSED
**原因**: 
- 连续错误次数过多
- Kafka连接失败
- 目标表不存在或权限不足

**解决方案**:
```sql
-- 查看错误日志
SHOW ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka;

-- 修复问题后恢复任务
RESUME ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka;
```

#### 问题2: 消费延迟过高
**原因**:
- 并发数不足
- 批次配置不合理
- 下游写入性能瓶颈

**解决方案**:
```sql
-- 增加并发数
ALTER ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka
PROPERTIES ("desired_concurrent_number" = "10");

-- 调整批次大小
ALTER ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka
PROPERTIES (
    "max_batch_rows" = "30000000",
    "max_batch_interval" = "30"
);
```

#### 问题3: 数据解析错误
**原因**:
- JSON格式不匹配
- JSONPath配置错误
- 字段类型不兼容

**解决方案**:
1. 检查Kafka消息格式
2. 验证JSONPath配置
3. 查看错误日志定位具体字段

### 12.2 日志查看

```sql
-- 查看错误日志URL
SELECT ErrorLogUrls 
FROM information_schema.routine_load_jobs
WHERE Name = 'dwd_voc_all_meta_data_kafka';
```

---

## 十三、性能优化建议

### 13.1 并发优化

```sql
-- 根据Kafka分区数调整并发
-- 建议: desired_concurrent_number = Kafka分区数
ALTER ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka
PROPERTIES ("desired_concurrent_number" = "分区数");
```

### 13.2 批次优化

```properties
# 高吞吐场景
max_batch_rows = 50000000
max_batch_size = 2147483648
max_batch_interval = 30

# 低延迟场景
max_batch_rows = 5000000
max_batch_size = 536870912
max_batch_interval = 10
```

### 13.3 内存优化

```sql
-- 增加执行内存限制
ALTER ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka
PROPERTIES ("exec_mem_limit" = "4294967296");  -- 4GB
```

---

## 十四、最佳实践

### 14.1 配置建议

1. **并发数**: 设置为Kafka分区数的1-2倍
2. **批次大小**: 根据消息大小和频率调整，建议1000万-5000万行
3. **批次间隔**: 建议30-60秒
4. **内存限制**: 建议2GB-4GB

### 14.2 监控建议

1. 定期检查任务状态
2. 监控消费延迟（Lag）
3. 关注错误行数
4. 监控加载速率

### 14.3 运维建议

1. 定期备份Routine Load配置
2. 建立告警机制
3. 保留错误日志用于问题排查
4. 定期评估性能并优化配置

---

## 十五、总结

### 15.1 配置特点

1. **数据源**: Kafka Topic `VDP_dwd_voc2_all_meta_data`
2. **目标表**: `dwd_voc_all_meta_data`
3. **数据格式**: JSON格式，20个字段映射
4. **消费模式**: 从头开始消费（OFFSET_BEGINNING）
5. **加载模式**: 追加模式（APPEND）
6. **容错策略**: 非严格模式，允许部分错误

### 15.2 关键参数

- **批次大小**: 2000万行 或 1GB 或 60秒
- **并发数**: 期望5个，当前1个
- **内存限制**: 2GB
- **时区**: Asia/Shanghai

---

**文档版本**: v1.0  
**生成时间**: 2025  
**维护团队**: VOC报表服务团队  
**联系方式**: [团队联系方式]
