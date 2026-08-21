# common_date_tools 使用指南

## 概述

`common_date_tools` 是一个强大的MyBatis SQL片段，用于生成时间维度的数据分析工具表。它支持多种时间粒度（日、周、月、季度、年）的数据统计和环比、同比计算。

## 核心功能

### 1. 时间维度支持
- **日维度** (date_unit=1): 按天统计
- **周维度** (date_unit=2): 按周统计  
- **月维度** (date_unit=3): 按月统计
- **季度维度** (date_unit=4): 按季度统计
- **年维度** (date_unit=5): 按年统计

### 2. 时间范围计算
- **当前期间** (ic=0): 用户指定的查询时间范围
- **环比期间** (ic=1): 与当前期间长度相同的上一个时间段
- **同比期间** (ic=2): 去年同期时间段

### 3. 关键字段说明

| 字段名 | 说明 | 示例 |
|--------|------|------|
| ic | 时间类型标识 | 0=当前期，1=环比期，2=同比期 |
| date_ | 格式化后的日期字符串 | 2024-01-15, 2024-01, 2024-W03 |
| date_unit | 时间粒度 | 1=日，2=周，3=月，4=季度，5=年 |
| metaStartDate | 实际查询开始日期 | 2024-01-01 |
| metaEndDate | 实际查询结束日期 | 2024-01-31 |
| startDateC/endDateC | 环比期间的开始/结束日期 | |
| startDateY/endDateY | 同比期间的开始/结束日期 | |

| 字段名              | 含义                                                                                                                              |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| `ic`             | **标识码（Identifier Code）**，用于区分不同时间段类型：<br>`0`：同比（Year-over-Year）<br>`1`：环比（Chain/Period-over-Period）<br>`2`：当前周期（Current Period） |
| `date_`          | **展开后的具体日期/周期字符串**，根据 `date_unit` 的不同，可能是：<br>- 日：`2025-08-04`<br>- 周：`2025-35`<br>- 月：`2025-08`                                |
| `date_unit`      | **时间单位类型**：<br>`1`：日<br>`2`：周<br>`3`：月                                                                                          |
| `scaleMagnitude` | **周期跨度倍数**，如 `1` 表示 1 天、1 周、1 个月，可用于扩展周期范围                                                                                      |
| `metaStartDate`  | **原始输入的起始日期**（标准化后）                                                                                                             |
| `metaEndDate`    | **原始输入的结束日期**（标准化后）                                                                                                             |
| `startDateP`     | **当前周期的起始日期**（用于生成当前周期范围）                                                                                                       |
| `endDateP`       | **当前周期的结束日期**（用于生成当前周期范围）                                                                                                       |
| `startDateC`     | **环比周期的起始日期**（用于生成环比范围）                                                                                                         |
| `endDateC`       | **环比周期的结束日期**（用于生成环比范围）                                                                                                         |
| `startDateY`     | **同比周期的起始日期**（用于生成同比范围）                                                                                                         |
| `endDateY`       | **同比周期的结束日期**（用于生成同比范围）                                                                                                         |

## 使用方式

### 1. 基本引用
```xml
<select id="yourMethod" resultType="YourResultType">
    <include refid="commonFilter.common_date_tools" />
    SELECT 
        -- 你的查询字段
    FROM (
        SELECT * FROM common_date_tools
    ) ct
    LEFT JOIN your_data_table t ON ct.date_ = t.formatted_date
    WHERE ic = 0  -- 只取当前期数据
</select>
```

### 2. 参数传递
调用时需要传递以下参数：
```java
Map<String, Object> params = new HashMap<>();
params.put("startDate", "2024-01-01");     // 查询开始日期
params.put("endDate", "2024-01-31");       // 查询结束日期  
params.put("dateUnit", 1);                 // 时间粒度：1=日
params.put("scaleMagnitude", 30);          // 时间跨度
params.put("typeR", 1);                    // 环比类型
params.put("typeY", 1);                    // 同比类型
```

## 实际应用案例

### 案例1: 产品分析数据简报
```xml
<select id="getProductBrief" resultType="ProductBriefVo">
    <include refid="commonFilter.common_date_tools" />
    SELECT
        ifnull((negative_c/sum_c) * 100, 0) as negativeRate,
        sum_c as mentions,
        user_c as users,
        -- 环比计算逻辑
        CASE 
            WHEN sum_r = 0 AND sum_c != 0 THEN '999999'
            WHEN sum_c != 0 AND sum_r != 0 THEN 
                ifnull((sum_c - sum_r) / abs(sum_r) * 100, 0)
            ELSE 0
        END as mentionsMoM
    FROM (
        SELECT
            ic,
            ifnull(sum(sum_c), 0) as sum_c,
            ifnull(sum(user_c), 0) as user_c,
            -- 使用窗口函数获取环比数据
            ifnull(LEAD(sum_c, 1) OVER(ORDER BY ic ASC), 0) as sum_r
        FROM (
            SELECT * FROM common_date_tools
        ) ct
        LEFT JOIN (
            SELECT
                CASE 
                    WHEN date_unit = 1 THEN data_create_time
                    WHEN date_unit = 2 THEN concat(year(data_create_time), '-', LPAD(weekofyear(data_create_time), 2, 0))
                    WHEN date_unit = 3 THEN concat(year(data_create_time), '-', LPAD(month(data_create_time), 2, 0))
                END as date_str,
                count(*) as sum_c,
                count(DISTINCT one_id) as user_c
            FROM voc_sentiment_annotations_results_v
            WHERE date(data_create_time) BETWEEN startDateC AND metaEndDate
               OR date(data_create_time) BETWEEN startDateY AND endDateY
            GROUP BY date_str
        ) c ON ct.date_ = c.date_str
        GROUP BY ic
    ) f
    WHERE ic = 0  -- 只返回当前期数据
</select>
```

### 案例2: 数据趋势变化
```xml
<select id="getDataTrendChange" resultType="ProductTrendPointVo">
    <include refid="commonFilter.common_date_tools" />
    SELECT
        date_str as date,
        ifnull((negative_c/sum_c) * 100, 0) as negativeRate,
        negative_c as negativeMentions,
        sum_c as totalMentions
    FROM (
        SELECT * FROM common_date_tools
    ) ct
    LEFT JOIN (
        SELECT
            CASE 
                WHEN date_unit = 1 THEN data_create_time
                WHEN date_unit = 2 THEN concat(year(data_create_time), '-', LPAD(weekofyear(data_create_time), 2, 0))
                WHEN date_unit = 3 THEN concat(year(data_create_time), '-', LPAD(month(data_create_time), 2, 0))
            END as date_str,
            count(*) as sum_c,
            sum(sentiment = '负面') as negative_c
        FROM voc_sentiment_annotations_results_v
        WHERE date(data_create_time) BETWEEN startDateC AND metaEndDate
           OR date(data_create_time) BETWEEN startDateY AND endDateY
        GROUP BY date_str
    ) c ON ct.date_ = c.date_str
    WHERE ic = 0
    ORDER BY date_str ASC
</select>
```

## 关键设计模式

### 1. 时间格式化策略
根据不同的时间粒度，将原始日期转换为统一格式：
```sql
CASE 
    WHEN date_unit = 1 THEN data_create_time           -- 2024-01-15
    WHEN date_unit = 2 THEN concat(year(data_create_time), '-', LPAD(weekofyear(data_create_time), 2, 0))  -- 2024-03
    WHEN date_unit = 3 THEN concat(year(data_create_time), '-', LPAD(month(data_create_time), 2, 0))       -- 2024-01
END as date_str
```

### 2. 环比计算模式
使用窗口函数LEAD获取下一行数据进行环比计算：
```sql
SELECT
    ic,
    current_value,
    ifnull(LEAD(current_value, 1) OVER(ORDER BY ic ASC), 0) as previous_value
FROM aggregated_data
```

### 3. 数据关联策略
通过LEFT JOIN将时间工具表与业务数据关联：
```sql
FROM (
    SELECT * FROM common_date_tools
) ct
LEFT JOIN business_data bd ON ct.date_ = bd.formatted_date
```

## 最佳实践

### 1. 性能优化
- 在业务表的时间字段上建立索引
- 合理使用WHERE条件过滤数据
- 避免在大数据量时使用复杂的窗口函数

### 2. 数据准确性
- 确保时间字段格式一致
- 处理NULL值和边界情况
- 验证环比、同比计算逻辑

### 3. 可维护性
- 统一使用common_date_tools进行时间处理
- 将复杂的业务逻辑封装在子查询中
- 添加适当的注释说明

## 常见问题

### Q1: 为什么环比数据为空？
A: 检查scaleMagnitude参数是否正确设置，确保有足够的历史数据用于环比计算。

### Q2: 周维度统计不准确？
A: 注意weekofyear函数在跨年时的处理，建议使用标准的ISO周计算方式。

### Q3: 如何处理数据缺失的日期？
A: common_date_tools会生成完整的日期序列，通过LEFT JOIN可以保留所有日期，缺失数据显示为NULL。

## 扩展应用

### 1. 多维度分析
可以结合其他维度进行分析：
```sql
SELECT
    channel_name,
    date_str,
    sum_c as mentions
FROM common_date_tools ct
LEFT JOIN channel_data cd ON ct.date_ = cd.date_str
WHERE ic = 0
```

### 2. 自定义时间范围
通过调整参数支持不同的时间范围需求：
```java
// 最近7天
params.put("scaleMagnitude", 7);
params.put("dateUnit", 1);

// 最近12个月  
params.put("scaleMagnitude", 12);
params.put("dateUnit", 3);
```