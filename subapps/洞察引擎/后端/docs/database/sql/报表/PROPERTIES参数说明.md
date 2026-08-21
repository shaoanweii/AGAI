# SelectDB/Doris PROPERTIES 参数说明

## 参数列表及性能优化建议

### 1. replication_allocation
- **类型**: 【必需】
- **说明**: 副本分配策略，默认位置1个副本
- **默认值**: 无默认值，必须指定
- **示例**: `"replication_allocation" = "tag.location.default: 1"`

### 2. min_load_replica_num
- **类型**: 【可选】
- **说明**: 最小加载副本数，-1表示使用默认值
- **默认值**: -1
- **示例**: `"min_load_replica_num" = "-1"`

### 3. bloom_filter_columns ⭐性能优化
- **类型**: 【可选】
- **说明**: 布隆过滤器列，为高频WHERE条件字段建立，加速等值查询和IN查询
- **默认值**: 空（不创建布隆过滤器）
- **优化建议**: 为经常出现在WHERE条件中的字段添加布隆过滤器
- **示例**: `"bloom_filter_columns" = "brand_code, car_series_code, topic, channel_code"`

### 4. is_being_synced
- **类型**: 【可选】
- **说明**: 是否正在同步，否
- **默认值**: false
- **示例**: `"is_being_synced" = "false"`

### 5. storage_medium ⭐性能优化
- **类型**: 【可选】
- **说明**: 存储介质，hdd(机械硬盘)或ssd(固态硬盘)
- **默认值**: hdd
- **优化建议**: 改为ssd可大幅提升查询速度（3-10倍）
- **示例**: `"storage_medium" = "hdd"` 或 `"storage_medium" = "ssd"`

### 6. storage_format
- **类型**: 【可选】
- **说明**: 存储格式，V2版本（默认V2）
- **默认值**: V2
- **示例**: `"storage_format" = "V2"`

### 7. inverted_index_storage_format
- **类型**: 【可选】
- **说明**: 倒排索引存储格式，V1版本
- **默认值**: V1
- **示例**: `"inverted_index_storage_format" = "V1"`

### 8. light_schema_change
- **类型**: 【可选】
- **说明**: 轻量级schema变更，启用（支持快速加减列）
- **默认值**: true
- **示例**: `"light_schema_change" = "true"`

### 9. disable_auto_compaction ⭐性能优化
- **类型**: 【可选】
- **说明**: 禁用自动压缩，false表示启用自动压缩，定期合并小文件提升查询性能
- **默认值**: false
- **优化建议**: 保持false，让系统自动合并小文件，减少文件数量，提升查询效率
- **示例**: `"disable_auto_compaction" = "false"`

### 10. enable_single_replica_compaction
- **类型**: 【可选】
- **说明**: 启用单副本压缩，否
- **默认值**: false
- **示例**: `"enable_single_replica_compaction" = "false"`

### 11. group_commit_interval_ms ⭐性能优化
- **类型**: 【可选】
- **说明**: 组提交间隔，单位毫秒
- **默认值**: 10000 (10秒)
- **优化建议**: 
  - 减小值(如5000)可加快数据可见性，适合实时性要求高的场景
  - 增大值(如30000)可提升写入吞吐，适合批量导入场景
- **示例**: `"group_commit_interval_ms" = "10000"` (10秒)

### 12. group_commit_data_bytes ⭐性能优化
- **类型**: 【可选】
- **说明**: 组提交数据大小，单位字节
- **默认值**: 134217728 (128MB)
- **优化建议**: 增大值(如268435456=256MB)可提升批量写入性能
- **示例**: `"group_commit_data_bytes" = "134217728"` (128MB)

### 13. enable_nondeterministic_function
- **类型**: 【可选】
- **说明**: 启用非确定性函数，是（允许使用now()等函数）
- **默认值**: false
- **示例**: `"enable_nondeterministic_function" = "true"`

## 性能优化总结

### 查询性能优化（⭐标记的参数）
1. **bloom_filter_columns**: 为高频查询字段添加布隆过滤器
2. **storage_medium**: 使用SSD存储介质
3. **disable_auto_compaction**: 保持false，启用自动压缩

### 写入性能优化（⭐标记的参数）
1. **group_commit_interval_ms**: 根据实时性需求调整
2. **group_commit_data_bytes**: 增大批量提交大小

### 推荐配置示例

```sql
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "brand_code, car_series_code, topic, channel_code",  -- 性能优化
"is_being_synced" = "false",
"storage_medium" = "ssd",  -- 性能优化：使用SSD
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",  -- 性能优化：启用自动压缩
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",  -- 性能优化：可根据需求调整
"group_commit_data_bytes" = "268435456",  -- 性能优化：256MB
"enable_nondeterministic_function" = "true"
)
```
