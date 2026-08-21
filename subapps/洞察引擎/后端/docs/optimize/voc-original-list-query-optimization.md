# VOC 原文列表分页查询优化方案

> **接口**：`getOriginalList`
> **服务**：`VocSoundsServiceImpl`
> **数据库**：StarRocks（SelectDB）
> **主表**：`voc_sentiment_annotations_results_ins_v`

---

## 一、优化背景与问题定位

### 原始方案（优化前）

原始查询为单次复杂 SQL，结构如下：

```sql
-- 旧方案核心结构（已废弃，见 git commit a623d5a91 删除的 getVocListOriginalContentCount）
WITH high_quality_data AS (
    SELECT original_id AS data_id
    FROM report_high_quality_record
    WHERE original_id IS NOT NULL
    GROUP BY data_id
)
SELECT f1.*, f3.content, f3.user_name, ...
    CASE WHEN f2.data_id IS NOT NULL THEN 1 ELSE 0 END AS highQuality
FROM (
    SELECT ANY_VALUE(id) AS id, data_id, ANY_VALUE(content_type), ...
           array_join(array_sort(ARRAY_AGG(DISTINCT topic_text)), ',') AS topic_text
    FROM voc_sentiment_annotations_results_v
    WHERE data_create_time >= ... AND data_create_time <= ...
    GROUP BY data_id
) AS f1
LEFT JOIN high_quality_data AS f2 ON f1.data_id = f2.data_id
LEFT JOIN (
    SELECT data_id, content, user_name, ...
    FROM voc_anal_flow_mate_data_labeled_v
    WHERE ...
) AS f3 ON f1.data_id = f3.data_id
WHERE ...
<if test='highQuality == "1"'>
    AND EXISTS (SELECT 1 FROM report_high_quality_record ...)
</if>
ORDER BY highQuality DESC, f1.publish_time DESC
```

**问题根因**：

| 问题 | 说明 |
|------|------|
| 三表 JOIN（含子查询） | `voc_sentiment_annotations_results_v` + `report_high_quality_record` + `voc_anal_flow_mate_data_labeled_v`，数据量大时 JOIN 代价极高 |
| COUNT 与详情同一 SQL | 分页计数与完整字段读取耦合，每次翻页都全量聚合 |
| `ARRAY_AGG + array_join` 在 GROUP BY 中执行 | 对所有数据执行字符串聚合，内存消耗大 |
| `high_quality` 来自外部表关联 | `report_high_quality_record` 非列存，跨引擎 JOIN 性能差 |
| `searchKeywords` 在外层 WHERE 过滤 | 全量聚合后再过滤，过滤下推失效 |
| 全量宽列读取 | 分页前读取所有字段，IO 浪费严重 |

---

## 二、第一阶段优化（2026-03-06，commit `2b5196fa0`）

### 优化思路

将单次复杂查询拆分为**三阶段**：

1. **Count 查询**（`getVocListOriginalContentInsCount`）：仅统计 `data_id` 数量，不读取宽列。
2. **分页 ID 查询**（`getVocListOriginalContentIns`）：内层子查询仅取 `data_id`、`publish_time`、`high_quality`，GROUP BY + ORDER BY 列最小化，LIMIT 精确分页。
3. **详情并行查询**：按分页后的 `originalId` 列表（通常 10~20 条），并行执行两个独立查询：
   - `getOriginalContentDetailByIds`：从 `voc_sentiment_annotations_results_ins_v` 按 ID IN 查原文详情。
   - `getVocListSoundsNew`：从 `voc_sentiment_annotations_results_v` 查话题/意图/情感信息。
   - 使用 `CompletableFuture` 异步并行，两路 IO 同步执行。

### 优化后 SQL 结构

**第一阶段 - 分页 ID 查询**（`getVocListOriginalContentIns`）：

```sql
-- 仅查 data_id + high_quality，IO 最小化
SELECT t.data_id AS originalId, t.highQuality
FROM (
    SELECT data_id, ANY_VALUE(high_quality) AS highQuality
    FROM voc_sentiment_annotations_results_ins_v
    WHERE data_create_time >= date(#{startDate})
      AND data_create_time <= date(#{endDate})
    -- 通用过滤条件下推至主表
    <include refid="commonFilter.whereFilterCommon"/>
    -- searchKeywords 在主表子查询内过滤，避免全量聚合后再过滤
    <if test="searchKeywords != null and searchKeywords !=''">
        AND (title LIKE CONCAT('%',#{searchKeywords},'%')
          OR original_text_scene LIKE CONCAT('%',#{searchKeywords},'%')
          OR one_id LIKE CONCAT('%',#{searchKeywords},'%')
          OR cust_name LIKE CONCAT('%',#{searchKeywords},'%')
          OR original_text LIKE CONCAT('%',#{searchKeywords},'%'))
    </if>
    <if test="highQuality != null and highQuality != ''">
        AND high_quality = #{highQuality}
    </if>
    GROUP BY data_id
    ORDER BY ANY_VALUE(high_quality) DESC, max(publish_time) DESC
    LIMIT #{pageNum}, #{pageSize}
) t
```

**说明**：
- `high_quality` 字段已物化到 `voc_sentiment_annotations_results_ins_v` 中，消除了与 `report_high_quality_record` 的跨表 JOIN。
- 内层仅需 `data_id + high_quality + publish_time`，排序、分页所需列最小，IO 极低。
- `searchKeywords` 在子查询内部过滤，过滤优先于 GROUP BY，减少聚合数据量。

**第二阶段 - 按 ID 查详情**（`getOriginalContentDetailByIds`）：

```sql
-- 仅对分页后的 10~20 条 ID 查完整字段，IN 过滤极快
SELECT
    data_id         AS originalId,
    title,
    original_text   AS originalTexTScene,
    channel_code    AS channelCode,
    channel_name    AS channel,
    MAX(publish_time) AS dataCreateTime,
    cust_name       AS custName,
    content_type    AS contentType
FROM voc_sentiment_annotations_results_ins_v
WHERE data_id IN (#{id1}, #{id2}, ...)
  AND data_create_time >= #{startDate}
  AND data_create_time <= #{endDate}
GROUP BY data_id, title, original_text, channel_code, channel_name, cust_name, content_type
```

### 第一阶段收益

| 对比项 | 优化前 | 优化后 |
|--------|--------|--------|
| JOIN 表数 | 3 表（含 CTE） | 单表（ins_v 物化表） |
| 分页前读取列数 | 全量宽列 | 仅 3 列（data_id + high_quality + publish_time） |
| 详情查询范围 | 全量 GROUP BY 后截取 | IN (10~20 条 ID) 精确定位 |
| 话题/情感并行 | 串行聚合 | CompletableFuture 并行 |
| high_quality 来源 | 跨表 LEFT JOIN | 物化列直读 |

---

## 三、第二阶段优化（2026-03-10~11，commits `b2daecc49` ~ `371e6f870`）

### 背景

第一阶段中 `getOriginalContentDetailByIds` 第二阶段查询与 `getVocListSoundsNew` 分属不同视图，且 `getOriginalContentDetailByIds` 查询时缺少时间范围条件，在数据量极大时仍有 IN 列表跨全分区扫描的风险。

### 优化内容

1. **`getOriginalContentDetailByIds` 补充时间范围条件**：确保分区裁剪生效，避免全分区扫描。
2. **Mapper 接口参数统一为 `ExtendComQueryModel`**：将 `originalIds` 和日期范围统一放入模型，避免多参数传递歧义。
3. **`getVocListOriginalContentInsCount` 精简子查询**：移除冗余外层 WHERE 过滤，仅在子查询内部完成所有过滤后 COUNT，减少一层嵌套。

### 第二阶段收益

| 对比项 | 优化前 | 优化后 |
|--------|--------|--------|
| 详情查询分区裁剪 | 缺失时间条件，全分区扫描 | 添加时间范围，分区裁剪生效 |
| 参数传递 | List<String> 单独传入 | 统一封装 ExtendComQueryModel |
| COUNT 嵌套层数 | 含冗余外层 WHERE 过滤 | 内层一次过滤，精简嵌套 |

---

## 四、第三阶段优化（2026-03-12，commit `a623d5a91`）

### 背景

清理历史遗留废弃方法，消除无效代码和 SQL：

- 删除旧的 `getVocListOriginalContentCount`（三表 JOIN 的旧计数 SQL）。
- 删除 `getUserSoundsDetails`（已被 `getUserDynamicEvaluationInfo` 替代）。
- 删除 `getBackstopUserDynamicInfo`（查 `voc_anal_flow_mate_data_full_v` 的废弃接口）。
- 对应清理 Service 层旧方法及 Mapper 接口声明。

### 收益

- 消除死代码带来的维护成本与理解负担。
- 防止旧接口被误调用（旧接口走三表 JOIN，性能极差）。

---

## 五、当前方案总结

### 完整调用链

```
getOriginalList(model)
│
├─① drillDownDefaFilter(model)           // 权限过滤
│
├─② getVocListOriginalContentInsCount    // COUNT 单表子查询
│      FROM voc_sentiment_annotations_results_ins_v
│      WHERE 时间 + 通用过滤 + searchKeywords + highQuality
│      GROUP BY data_id → COUNT(1)
│
├─③ getVocListOriginalContentIns         // 分页 ID 查询（仅3列）
│      FROM voc_sentiment_annotations_results_ins_v
│      GROUP BY data_id
│      ORDER BY high_quality DESC, publish_time DESC
│      LIMIT offset, pageSize
│
└─④ 并行执行（CompletableFuture）
       ├─ getOriginalContentDetailByIds  // IN (pageSize 条 ID) 查详情
       │    FROM voc_sentiment_annotations_results_ins_v
       │    + 时间分区裁剪
       └─ getVocListSoundsNew           // 话题/意图/情感
            FROM voc_sentiment_annotations_results_v
```

### 关键设计要点

| 要点 | 说明 |
|------|------|
| **物化单表** | `original_text`（原文）和 `high_quality`（高质量标记）均已物化到 `voc_sentiment_annotations_results_ins_v`，无需关联外部表 |
| **两级分页** | COUNT 与详情完全分离，COUNT 仅聚合极少列，不读宽列 |
| **ID 精确回查** | 详情查询范围固定为 `pageSize` 条（通常 10~20），IN 过滤快 |
| **分区裁剪** | 所有查询均携带时间范围，StarRocks 分区裁剪生效 |
| **并行 IO** | 详情 + 话题两路独立查询并行，总耗时取两者最大值而非之和 |
| **数据源上下文** | 异步线程手动 push/poll `DynamicDataSourceContextHolder`，确保 StarRocks 数据源不丢失 |

---

## 六、遗留优化建议

### 6.1 全文搜索性能问题（高优先级）

**现状**：`searchKeywords` 仍使用 `LIKE '%keyword%'` 对 5 个字段做前缀通配符模糊匹配，索引完全失效，`original_text` 为大文本字段，扫描代价极高。

**建议**：参考 [`selectdb-fulltext-search-inverted-index.md`](./selectdb-fulltext-search-inverted-index.md) 方案，在 `voc_sentiment_annotations_results_ins_v` 上为 `title`、`original_text_scene`、`original_text` 建立倒排索引，将 `LIKE` 替换为 `MATCH_ALL` 全文检索函数。

### 6.2 COUNT 查询仍含 GROUP BY

**现状**：

```sql
SELECT count(1) FROM (
    SELECT data_id FROM ... GROUP BY data_id
) t
```

`GROUP BY data_id` 后再 COUNT 相比直接 `COUNT(DISTINCT data_id)` 多一层派生表，StarRocks 优化器通常能等价处理，但可进一步验证执行计划是否等价：

```sql
-- 可尝试直接 COUNT DISTINCT，减少派生表层级
SELECT COUNT(DISTINCT data_id)
FROM voc_sentiment_annotations_results_ins_v
WHERE data_create_time >= date(#{startDate})
  AND data_create_time <= date(#{endDate})
  <include refid="commonFilter.whereFilterCommon"/>
  ...
```

### 6.3 `getVocListSoundsNew` 并行查询的必要性评估

**现状**：话题/意图/情感数据（`topic`、`intention`、`sentiment`）通过并行查询 `voc_sentiment_annotations_results_v` 获取，但 `voc_sentiment_annotations_results_ins_v` 中已包含 `topic_text`、`intention`、`sentiment` 字段（见 `getVocListOriginalContentIns` 中 `ANY_VALUE` 聚合逻辑）。

**建议**：确认 `voc_sentiment_annotations_results_ins_v` 中话题相关字段的覆盖完整性；若完整，可将话题查询合并至第二阶段的 `getOriginalContentDetailByIds`，消除 `getVocListSoundsNew` 的独立并行查询，减少一次全量扫描。

### 6.4 `original_text` 字段双存问题

**现状**：`voc_sentiment_annotations_results_ins_v` 同时存有 `original_text_scene`（摘要/场景描述）和 `original_text`（原始全文）两个字段，均参与 `LIKE` 搜索。

**建议**：
1. 明确两字段的业务语义差异，避免在 `searchKeywords` 中重复扫描含义相近的字段。
2. 若 `original_text` 为超长大文本，可将全文搜索改为仅搜索 `original_text_scene`（摘要）+ 倒排索引，进一步降低 IO。

---

## 七、优化演进时间线

| 时间 | Commit | 关键变更 |
|------|--------|----------|
| 2026-03-06 | `2b5196fa0` | 拆分两阶段查询 + CompletableFuture 并行 + high_quality 物化到 ins_v 单表 |
| 2026-03-10 | `b2daecc49` | 精简 COUNT 子查询，移除冗余过滤层 |
| 2026-03-11 | `371e6f870` | 详情查询补充时间范围分区裁剪；Mapper 参数统一为 ExtendComQueryModel |
| 2026-03-12 | `a623d5a91` | 删除三表 JOIN 旧方法，清理废弃 SQL 和 Service 层冗余代码 |
