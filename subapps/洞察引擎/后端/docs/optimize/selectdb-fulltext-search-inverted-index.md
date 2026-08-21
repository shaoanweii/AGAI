# SelectDB 大文本字段模糊查询优化方案

## 一、问题背景

### 当前查询方式

声音列表接口在关键词搜索时，对多个字段执行 `%keyword%` 全文模糊匹配：

```sql
AND (title                 LIKE CONCAT('%',#{searchKeywords},'%')
  OR original_text_scene   LIKE CONCAT('%',#{searchKeywords},'%')
  OR one_id                LIKE CONCAT('%',#{searchKeywords},'%')
  OR cust_name             LIKE CONCAT('%',#{searchKeywords},'%')
  OR original_text         LIKE CONCAT('%',#{searchKeywords},'%'))
```

### 问题根因

| 问题 | 说明 |
|------|------|
| `%keyword%` 前缀通配符 | 索引完全失效，强制全表扫描 |
| 5 个字段 OR 叠加 | 每个字段独立扫描，IO 成倍放大 |
| `original_text` 为大文本字段 | 单行数据量大，扫描代价极高 |
| 无分区裁剪 | 关键词搜索不带分区条件时跨全量分区扫描 |

---

## 二、数据链路说明

```
Java Mapper SQL 查询
  └── 普通视图: voc_sentiment_annotations_results_ins_v
        └── 异步物化视图: voc_anal_flow_sentiment_annotations_results_ins_mv  ← 优化目标
              ├── f1: voc_anal_flow_sentiment_annotations_results_mv
              │     (title / original_text_scene / one_id / cust_name 等字段来源)
              └── f2: voc_anal_flow_model_tags_result_data_full_mv
                    (content AS original_text 来源)
```

**关键结论**：所有搜索字段已物化存储在 `voc_anal_flow_sentiment_annotations_results_ins_mv`，可直接在此 MV 上建倒排索引。

---

## 三、优化方案：倒排索引 + MATCH 全文检索

### 3.1 方案原理

SelectDB（Doris 2.0+）原生支持倒排索引和全文检索函数，原理与 Elasticsearch 一致：

- 写入时对文本进行分词，建立 `词 → 行号` 的倒排索引文件
- 查询时通过词典直接定位匹配行，**跳过未命中的数据块**
- 中文支持 `chinese` 分词器（基于 analyzer），效果等同 ES 的 `ik_max_word`

### 3.2 目标 MV 存储特征确认

`voc_anal_flow_sentiment_annotations_results_ins_mv` DDL 已具备支持倒排索引的条件：

```sql
"inverted_index_storage_format" = "V1"  -- 已开启倒排索引存储格式
"storage_format" = "V2"                 -- 列式存储 V2
DUPLICATE KEY(`id`)                      -- 明细模型，无聚合限制
```

> 经开发环境验证：对异步物化视图执行 `ALTER TABLE ... ADD INDEX USING INVERTED` **不报错，执行成功**。

---

## 四、执行步骤

### Step 1：添加倒排索引（开发/测试环境先验证）

```sql
-- 大文本字段：中文分词，支持短语查询
ALTER TABLE voc_anal_flow_sentiment_annotations_results_ins_mv
ADD INDEX idx_inv_original_text (original_text)
USING INVERTED PROPERTIES("parser"="chinese", "support_phrase"="true");

ALTER TABLE voc_anal_flow_sentiment_annotations_results_ins_mv
ADD INDEX idx_inv_title (title)
USING INVERTED PROPERTIES("parser"="chinese", "support_phrase"="true");

ALTER TABLE voc_anal_flow_sentiment_annotations_results_ins_mv
ADD INDEX idx_inv_scene (original_text_scene)
USING INVERTED PROPERTIES("parser"="chinese", "support_phrase"="true");

-- 名称字段：unicode 分词（按标点/空格切分，不做语义分词）
ALTER TABLE voc_anal_flow_sentiment_annotations_results_ins_mv
ADD INDEX idx_inv_cust_name (cust_name)
USING INVERTED PROPERTIES("parser"="unicode");

-- ID 字段：不分词，精确匹配
ALTER TABLE voc_anal_flow_sentiment_annotations_results_ins_mv
ADD INDEX idx_inv_one_id (one_id)
USING INVERTED;
```

> `ALTER TABLE` 加索引**不锁表**，业务不受影响，但 build 存量数据会占用 IO，建议低峰执行。

### Step 2：监控存量数据 Build 进度

```sql
-- 轮询直到所有字段 State = FINISHED
SHOW BUILD INDEX FROM voc_anal_flow_sentiment_annotations_results_ins_mv;
```

| 字段 | 预期 Build 耗时 | 说明 |
|------|--------------|------|
| `original_text` | 最长 | 大文本，数据量最大 |
| `title` / `original_text_scene` | 中等 | - |
| `cust_name` / `one_id` | 较短 | 短字符串 |

> Build 完成前：**新增数据立即有索引**，存量数据仍走全扫；Build 完成后全量生效。

### Step 3：改写 Mapper SQL

**改写前（慢）：**
```xml
<if test="searchKeywords != null and searchKeywords !=''">
    AND (title LIKE CONCAT('%',#{searchKeywords},'%')
      OR original_text_scene LIKE CONCAT('%',#{searchKeywords},'%')
      OR one_id LIKE CONCAT('%',#{searchKeywords},'%')
      OR cust_name LIKE CONCAT('%',#{searchKeywords},'%')
      OR original_text LIKE CONCAT('%',#{searchKeywords},'%'))
</if>
```

**改写后（快）：**
```xml
<if test="searchKeywords != null and searchKeywords !=''">
    AND (
        title                  MATCH_ANY #{searchKeywords}
        OR original_text_scene MATCH_ANY #{searchKeywords}
        OR one_id              MATCH_ANY #{searchKeywords}
        OR cust_name           MATCH_ANY #{searchKeywords}
        OR original_text       MATCH_ANY #{searchKeywords}
    )
</if>
```

### Step 4：验证索引命中

```sql
-- 用 EXPLAIN 确认走了倒排索引
EXPLAIN SELECT id, title, original_text
FROM voc_sentiment_annotations_results_ins_v
WHERE data_create_time >= '2026-01-01'
  AND original_text MATCH_ANY '空调噪音'
LIMIT 10;

-- EXPLAIN 输出中应包含：inverted index / MATCH 关键字
-- 若仍显示 TABLE SCAN 全扫，说明索引未命中，需排查
```

---

## 五、MATCH 函数说明

| 函数 | 含义 | 对应 ES |
|------|------|--------|
| `MATCH_ANY` | 分词后任意词命中即返回 | `match` (operator=or) |
| `MATCH_ALL` | 分词后所有词都命中才返回 | `match` (operator=and) |
| `MATCH_PHRASE` | 词序一致的短语精确匹配 | `match_phrase` |

当前场景建议使用 `MATCH_ANY`，召回率与原 `LIKE %keyword%` 一致。

---

## 六、MV 定时刷新对索引的影响

该 MV 每 60 分钟自动刷新（`REFRESH AUTO ON SCHEDULE EVERY 60 MINUTE`）。

| 刷新类型 | 索引 Schema 是否保留 | 数据索引是否重建 |
|---------|-------------------|--------------|
| 增量刷新（Incremental） | 保留 | 仅新增数据建索引 |
| 全量刷新（Full Refresh） | 保留（Schema 级） | 数据重写后自动重建 |

> 全量刷新期间索引重建完成前，查询会短暂退化为全扫，建议监控刷新日志。

---

## 七、效果预期

| 指标 | 优化前 | 优化后 |
|------|-------|-------|
| 查询方式 | 全表扫描（LIKE %xx%） | 倒排索引（MATCH_ANY） |
| 百万级数据搜索耗时 | 秒级～分钟级 | 毫秒级～百毫秒级 |
| `original_text` 大文本扫描 | 全量 IO | 仅读命中 Segment |
| CPU/IO 压力 | 高（全扫） | 低（索引命中后精准读取） |

---

## 八、上线 Checklist

- [ ] 开发环境执行 ALTER，确认无报错
- [ ] `SHOW BUILD INDEX` 确认所有字段 State = FINISHED
- [ ] 开发环境用 EXPLAIN 验证索引命中
- [ ] 回归测试：搜索结果与 LIKE 方式结果一致（注意分词差异）
- [ ] 生产环境选业务低峰执行 ALTER
- [ ] 生产环境 Build 完成后再上线 Mapper 代码变更
- [ ] 上线后监控接口响应时间及 SelectDB 查询耗时
