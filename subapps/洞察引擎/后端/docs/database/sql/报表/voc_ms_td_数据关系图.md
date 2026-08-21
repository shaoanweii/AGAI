# VOC_MS_TD 数据库关系图

## 一、整体架构关系图（含Kafka实时消费）

```
┌─────────────────────────────────────────────────────────────────────┐
│                          数据采集层                                    │
│                    (外部数据源/业务系统)                                │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│                      Kafka消息队列层                                   │
├─────────────────────────────────────────────────────────────────────┤
│  Kafka Topic: VDP_dwd_voc2_all_meta_data                            │
│  Kafka Broker: 172.16.80.16:30092                                   │
│  Consumer Group: VDP-voc2-analysis                                  │
│                                                                     │
│  Routine Load Task: dwd_voc_all_meta_data_kafka                    │
│  - 数据格式: JSON (20字段映射)                                        │
│  - 批次配置: 2000万行 或 1GB 或 60秒                                  │
│  - 并发数: 期望5 / 当前1                                              │
│  - 加载模式: APPEND追加                                               │
│  - 状态: RUNNING                                                    │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│                       DWD层 - 数据明细层                               │
├─────────────────────────────────────────────────────────────────────┤
│  dwd_voc_all_meta_data                                              │
│  - 元数据明细表                                                        │
│  - 包含客户、车辆、经销商等完整信息                                      │
│  - 索引: BLOOM_FILTER(create_time, data_create_time, data_id)       │
│  - 数据来源: Kafka实时消费                                            │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│                       ADS层 - 应用数据层                               │
├─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ ads_voc_all_mate_data_m_full (月度全量)                       │   │
│  │ - 元数据月度快照                                               │   │
│  └────────────────────────┬────────────────────────────────────┘   │
│                           │                                         │
│                           ↓                                         │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ ads_voc_model_tags_result_data_m_inc (标签结果增量)            │   │
│  │ - 模型打标结果                                                 │   │
│  │ - 情感分析、意图识别、标签体系                                   │   │
│  └────────────────────────┬────────────────────────────────────┘   │
└───────────────────────────┼─────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────────────┐
│                    物化视图层 (每小时刷新)                              │
├─────────────────────────────────────────────────────────────────────┤
│  ads_voc_model_tags_result_data_mv                                  │
│  = ads_voc_model_tags_result_data_m_inc                             │
│    + ins_brand_info (品牌维度)                                       │
│    + ins_car_series_info (车系维度)                                  │
│    + ads_voc_channel_m_full_mv (渠道维度)                            │
│    + ads_voc_tags_system_info_h_full_mv (标签维度)                   │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│                       应用视图层                                       │
├─────────────────────────────────────────────────────────────────────┤
│  voc_sentiment_annotations_results_v                                               │
│  voc_anal_flow_mate_data_full_v                                            │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│                       应用层/报表层                                    │
│                    (BI工具/报表服务)                                   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、核心事实表与维度表关系

```
                    ┌──────────────────────┐
                    │  ins_brand_info      │
                    │  (品牌维度)           │
                    │  - code              │
                    │  - name              │
                    │  - competitive_type  │
                    └──────────┬───────────┘
                               │
                               │ brand_code
                               │
    ┌──────────────────────────┼──────────────────────────┐
    │                          │                          │
    │                          ↓                          │
    │         ┌────────────────────────────────┐          │
    │         │ ads_voc_model_tags_result_     │          │
    │         │ data_m_inc                     │          │
    │         │ (核心事实表)                    │          │
    │         │ - id (PK)                      │          │
    │         │ - publish_time (PK)            │          │
    │         │ - data_id (PK)                 │          │
    │         │ - brand_code (FK)              │          │
    │         │ - car_series_code (FK)         │          │
    │         │ - channel_id (FK)              │          │
    │         │ - topic                        │          │
    │         │ - sentiment                    │          │
    │         │ - intention_type               │          │
    │         └────────────┬───────────────────┘          │
    │                      │                              │
    │                      │ car_series_code              │
    │                      │                              │
    │                      ↓                              │
    │         ┌──────────────────────┐                    │
    │         │ ins_car_series_info  │                    │
    │         │ (车系维度)            │                    │
    │         │ - code               │                    │
    │         │ - name               │                    │
    │         │ - brand_code         │                    │
    │         │ - car_level1/2       │                    │
    │         │ - energy_type1/2     │                    │
    │         └──────────────────────┘                    │
    │                                                     │
    │ channel_id                                          │
    │                                                     │
    ↓                                                     │
┌──────────────────────┐                                 │
│  ins_channel         │                                 │
│  (渠道维度)           │                                 │
│  - id                │                                 │
│  - name              │                                 │
│  - parent_id         │                                 │
│  - level             │                                 │
│  - is_core_channel   │                                 │
└──────────────────────┘                                 │
                                                         │
                                                         │
                                    ┌────────────────────┘
                                    │
                                    ↓
                       ┌──────────────────────┐
                       │  ins_tag_client      │
                       │  (标签维度)           │
                       │  - tag_code          │
                       │  - tag_name          │
                       │  - tag_type          │
                       │  - user_journey1/2/3 │
                       │  - emotion           │
                       │  - intention         │
                       └──────────────────────┘
```

---

## 三、数据流向详细关系

### 3.1 元数据流向（含Kafka实时消费）

```
外部数据源
    ↓
Kafka Topic: VDP_dwd_voc2_all_meta_data
    ↓
Kafka Broker: 172.16.80.16:30092
    ↓
Routine Load Task: dwd_voc_all_meta_data_kafka
    ├─ Consumer Group: VDP-voc2-analysis
    ├─ JSON字段映射 (20个字段)
    │  $.id → id
    │  $.dataId → data_id
    │  $.oneId → one_id
    │  $.workId → work_id
    │  $.channelId → channel_id
    │  ... (共20个字段)
    ├─ 批次触发条件:
    │  - 2000万行 OR
    │  - 1GB数据 OR
    │  - 60秒间隔
    └─ 加载模式: APPEND追加
    ↓
dwd_voc_all_meta_data (DWD层)
    │
    ├─→ ads_voc_all_mate_data_m_full (ADS层-月度全量)
    │       ↓
    │   voc_anal_flow_mate_data_full_v (视图)
    │       ↓
    │   应用层查询
    │
    └─→ 数据清洗/模型计算
            ↓
        ads_voc_model_tags_result_data_m_inc
```

### 3.2 模型标签数据流向（含物化视图详细处理）

```
ads_voc_model_tags_result_data_m_inc (基础表)
    │
    ├─ JSON字段解码 (from_base64)
    │   ├─ raw_data
    │   ├─ ext_fields
    │   ├─ biz_ext_attrs
    │   ├─ cust_ext_attrs
    │   ├─ vhl_ext_attrs
    │   └─ dealer_ext_attrs
    │
    ├─ LEFT JOIN ins_brand_info ON brand_code
    │   └─ 获取: brand_name, competitive_type, country, nature
    │
    ├─ LEFT JOIN ins_car_series_info ON car_series_code
    │   └─ 获取: car_series_name, factory, car_level, energy_type, competitive_type
    │
    ├─ LEFT JOIN ads_voc_channel_m_full_mv ON channel_id = code
    │   └─ 获取: channel_catagory_level1, channel_name
    │
    └─ LEFT JOIN ads_voc_tags_system_info_h_full_mv ON topic
        └─ 获取完整标签体系:
            ├─ CPT标签 (cpt_tag_first/second/three/four + code)
            ├─ UJY标签 (ujy_tag_first/second/three/four + code)
            ├─ CMA标签 (cma_tag_first/second/three/four + code)
            ├─ DOM标签 (dom_tag_first/second/three/four + code)
            ├─ NPS标签 (nps_tag_first/second/three/four + code)
            ├─ VTR标签 (vtr_tag_first/second/three/four + code)
            ├─ user_journey1/2/3
            ├─ d2c_responsible_dept
            └─ tag_accuracy
            ↓
        ads_voc_model_tags_result_data_mv (物化视图)
        - 自动刷新: 每1小时
        - 分区策略: 按publish_time分区
        - 分布策略: HASH(id) 8个桶
        - 索引优化: bloom_filter(brand_code, car_series_code, topic, channel_code)
            ↓
        voc_sentiment_annotations_results_v (应用视图)
            ↓
        报表服务/BI工具
```

### 3.3 渠道数据流向

```
ins_channel (基础表)
    ↓
ads_voc_channel_m_full_mv (物化视图)
    ↓
关联到主数据流
```

### 3.4 标签数据流向

```
标签系统基础表
    │
    ├─→ ads_voc_tags_h_full_mv (小时物化视图)
    │
    ├─→ ads_voc_tags_system_h_full_mv (系统标签物化视图)
    │
    └─→ ads_voc_tags_system_info_h_full_mv (标签系统信息物化视图)
            ↓
        关联到主数据流
```

---

## 四、报表业务表关系

### 4.1 用户行为分析

```
voc_sentiment_annotations_results_v (声音数据)
    │
    ├─→ report_user_browse_record (用户浏览记录)
    │   - sound_id (FK)
    │   - browse_user_id
    │   - browse_duration
    │   - sound_intention
    │
    └─→ report_view_log (报表查看日志)
        - 审计日志
        - 统计分析
```

### 4.2 数据质量管理

```
ads_voc_model_tags_result_data_m_inc
    │
    ├─→ report_high_quality_record (高质量记录)
    │   - sound_id (FK)
    │   - status
    │   - data_create_time
    │
    └─→ report_label_correction_info (标签纠正信息)
        - data_id (FK)
        - correction_record_id
        - 纠正前后的标签对比
            ↓
        report_label_correction_record (纠正记录)
        - 历史记录
        - 模型优化依据
```

### 4.3 报表配置管理

```
report_custom_report (自定义报表)
    │
    ├─ report_name
    ├─ type
    ├─ default_condition
    ├─ brand_code (FK → ins_brand_info)
    ├─ special_type_id (FK → report_special_analysis_type)
    └─ first_level_zone_id
        │
        ├─→ report_special_analysis_type (专项分析类型)
        │
        ├─→ report_special_analysis_role (专项分析角色)
        │
        └─→ report_display_rule (展示规则)
            - metric_code
            - range_min/max
            - color_hex
            - emoji_key
```

---

## 五、维度表层级关系

### 5.1 品牌-车系层级

```
ins_brand_info (品牌)
    │
    └─→ ins_car_series_info (车系)
            │
            ├─ brand_id (FK)
            ├─ brand_code (FK)
            │
            └─→ 车型信息
                - car_name
                - car_code
                - car_level1/2
                - energy_type1/2
```

### 5.2 渠道层级

```
ins_channel (渠道)
    │
    ├─ level = 1 (一级渠道)
    │   └─ top_id = NULL
    │
    ├─ level = 2 (二级渠道)
    │   ├─ parent_id → level 1
    │   └─ top_id → level 1
    │
    └─ level = 3 (三级渠道)
        ├─ parent_id → level 2
        └─ top_id → level 1
```

### 5.3 标签层级

```
ins_tag_client (标签)
    │
    ├─ level = 1 (一级标签)
    │   └─ tag_parent_id = NULL
    │
    ├─ level = 2 (二级标签)
    │   └─ tag_parent_id → level 1
    │
    ├─ level = 3 (三级标签)
    │   └─ tag_parent_id → level 2
    │
    └─ level = 4 (四级标签)
        └─ tag_parent_id → level 3
```

---

## 六、扩展属性关系

### 6.1 JSON扩展属性结构

```
ads_voc_model_tags_result_data_m_inc
    │
    ├─ biz_ext_attrs (业务扩展属性)
    │   └─ JSON格式存储业务相关扩展字段
    │
    ├─ biz_ext_attrs2 (业务扩展属性2)
    │   └─ JSON格式存储业务相关扩展字段
    │
    ├─ biz_ext_attrs3 (业务扩展属性3)
    │   └─ JSON格式存储业务相关扩展字段
    │
    ├─ cust_ext_attrs (客户扩展属性)
    │   └─ JSON格式存储客户相关信息
    │       - 年龄、性别、学历、收入等
    │
    ├─ vhl_ext_attrs (车辆扩展属性)
    │   └─ JSON格式存储车辆相关信息
    │       - VIN、颜色、生产日期等
    │
    ├─ dealer_ext_attrs (经销商扩展属性)
    │   └─ JSON格式存储经销商相关信息
    │       - dlr_oc_* (订单中心)
    │       - dlr_dc_* (交付中心)
    │       - dlr_mc_* (维保中心)
    │
    ├─ prd_ext_attrs (产品扩展属性)
    │   └─ JSON格式存储产品相关信息
    │
    └─ tags_ext_attrs (标签扩展属性)
        └─ JSON格式存储标签相关信息
            - CPT标签 (产品标签)
            - UJY标签 (用户旅程标签)
            - CMA标签 (全领域业务标签)
            - DOM标签 (商品化属性标签)
            - NPS标签
            - VTR标签
```

### 6.2 扩展属性映射

```
dwd_voc_ext_attrs_mapping_values (扩展属性映射表)
    │
    └─ 提供扩展属性的映射关系
        - 属性名称 → 属性值
        - 属性编码 → 属性含义
```

---

## 七、时间维度关系

### 7.1 时间字段关系

```
数据时间线:

data_create_time (数据创建时间)
    ↓
publish_time (发布时间)
    ↓
create_time (入库时间)
    ↓
update_time (更新时间)
```

### 7.2 分区策略

```
按时间分区:

├─ 按月分区
│   └─ ds = 'YYYYMM'
│
├─ 按天分区
│   └─ ds = 'YYYYMMDD'
│
└─ 按小时分区
    └─ ds = 'YYYYMMDDHH'
```

---

## 八、物化视图刷新关系

```
基础表更新
    ↓
触发物化视图刷新 (每小时)
    │
    ├─→ ads_voc_channel_m_full_mv
    │   - 渠道维度刷新
    │
    ├─→ ads_voc_tags_h_full_mv
    │   - 标签小时刷新
    │
    ├─→ ads_voc_tags_system_h_full_mv
    │   - 系统标签刷新
    │
    ├─→ ads_voc_tags_system_info_h_full_mv
    │   - 标签系统信息刷新
    │
    └─→ ads_voc_model_tags_result_data_mv
        - 主数据宽表刷新
            ↓
        应用视图自动更新
            ↓
        报表数据更新
```

---

## 九、查询路径优化

### 9.1 推荐查询路径

```
应用层查询
    ↓
voc_sentiment_annotations_results_v (视图)
    ↓
ads_voc_model_tags_result_data_mv (物化视图)
    ↓
返回结果 (已包含所有维度信息)
```

### 9.2 不推荐查询路径

```
应用层查询
    ↓
ads_voc_model_tags_result_data_m_inc (基础表)
    ↓
手动JOIN多个维度表
    ↓
性能较差
```

---

## 十、数据血缘关系

```
数据血缘追溯:

original_id (原始ID)
    ↓
data_id (数据ID)
    ↓
input_data_id (输入数据ID)
    ↓
id (主键ID)
    ↓
sound_id (声音ID)
```

---

## 十一、关系总结

### 11.1 一对多关系

1. **品牌 → 车系**: 一个品牌对应多个车系
2. **渠道 → 子渠道**: 一个渠道对应多个子渠道
3. **标签 → 子标签**: 一个标签对应多个子标签
4. **数据 → 浏览记录**: 一条数据对应多条浏览记录
5. **数据 → 纠正记录**: 一条数据对应多条纠正记录

### 11.2 多对一关系

1. **车系 → 品牌**: 多个车系属于一个品牌
2. **数据 → 渠道**: 多条数据来自一个渠道
3. **数据 → 品牌**: 多条数据属于一个品牌
4. **数据 → 车系**: 多条数据属于一个车系

### 11.3 多对多关系

1. **数据 ↔ 标签**: 一条数据可以有多个标签，一个标签可以对应多条数据
2. **报表 ↔ 角色**: 一个报表可以分配给多个角色，一个角色可以访问多个报表

---

## 十二、标签体系结构

```
标签系统 (ads_voc_tags_system_info_h_full_mv)
│
├─ CPT标签 (产品标签)
│   ├─ cpt_tag_first_code / cpt_tag_first
│   ├─ cpt_tag_second_code / cpt_tag_second
│   ├─ cpt_tag_three_code / cpt_tag_three
│   └─ cpt_tag_four_code / cpt_tag_four
│
├─ UJY标签 (用户旅程标签)
│   ├─ ujy_tag_first_code / ujy_tag_first
│   ├─ ujy_tag_second_code / ujy_tag_second
│   ├─ ujy_tag_three_code / ujy_tag_three
│   └─ ujy_tag_four_code / ujy_tag_four
│
├─ CMA标签 (全领域业务标签)
│   ├─ cma_tag_first_code / cma_tag_first
│   ├─ cma_tag_second_code / cma_tag_second
│   ├─ cma_tag_three_code / cma_tag_three
│   └─ cma_tag_four_code / cma_tag_four
│
├─ DOM标签 (商品化属性标签)
│   ├─ dom_tag_first_code / dom_tag_first
│   ├─ dom_tag_second_code / dom_tag_second
│   ├─ dom_tag_three_code / dom_tag_three
│   └─ dom_tag_four_code / dom_tag_four
│
├─ NPS标签
│   ├─ nps_tag_first_code / nps_tag_first
│   ├─ nps_tag_second_code / nps_tag_second
│   ├─ nps_tag_three_code / nps_tag_three
│   └─ nps_tag_four_code / nps_tag_four
│
└─ VTR标签
    ├─ vtr_tag_first_code / vtr_tag_first
    ├─ vtr_tag_second_code / vtr_tag_second
    ├─ vtr_tag_three_code / vtr_tag_three
    └─ vtr_tag_four_code / vtr_tag_four
```

---

## 十三、物化视图特性与优化

### 13.1 物化视图关键特性

| 特性 | 配置 | 说明 |
|------|------|------|
| **自动刷新** | 每1小时 | 定时自动刷新，保持数据准实时 |
| **分区策略** | publish_time | 按发布时间分区，便于历史数据管理 |
| **分布策略** | HASH(id) 8桶 | 数据均匀分布，提升查询性能 |
| **索引优化** | bloom_filter | brand_code, car_series_code, topic, channel_code |

### 13.2 数据关联关系表

| 物化视图 | 关联表 | 关联字段 | 获取信息 |
|---------|--------|---------|----------|
| ads_voc_model_tags_result_data_mv | ins_brand_info | brand_code | 品牌名称、竞争类型、国家、性质 |
| ads_voc_model_tags_result_data_mv | ins_car_series_info | car_series_code | 车系名称、工厂、车型级别、能源类型 |
| ads_voc_model_tags_result_data_mv | ads_voc_channel_m_full_mv | channel_id = code | 渠道分类、渠道名称 |
| ads_voc_model_tags_result_data_mv | ads_voc_tags_system_info_h_full_mv | topic | 完整标签体系(CPT/UJY/CMA/DOM/NPS/VTR) |

### 13.3 视图依赖关系

```
voc_sentiment_annotations_results_v (应用视图)
    └─ 依赖: ads_voc_model_tags_result_data_mv (物化视图)

voc_anal_flow_mate_data_full_v (应用视图)
    └─ 依赖: ads_voc_all_mate_data_m_full (基础表)
```

---

## 十四、查询优化建议

### 14.1 推荐查询策略

1. **优先使用物化视图**: 直接查询 `ads_voc_model_tags_result_data_mv` 而非基础表
2. **使用应用视图**: 通过 `voc_sentiment_annotations_results_v` 查询，保持接口稳定
3. **利用分区裁剪**: 查询时指定 `publish_time` 范围
4. **利用索引过滤**: WHERE条件使用 brand_code, car_series_code, topic, channel_code
5. **避免全表扫描**: 始终添加时间范围或其他过滤条件

### 14.2 性能优化要点

```
查询优化路径:

1. 时间范围过滤
   WHERE publish_time >= '2025-01-01' AND publish_time < '2025-02-01'
   └─ 利用分区裁剪

2. 维度字段过滤
   AND brand_code = 'BRAND001'
   AND car_series_code = 'SERIES001'
   └─ 利用bloom_filter索引

3. 标签字段过滤
   AND topic = 'TOPIC001'
   └─ 利用bloom_filter索引

4. 渠道字段过滤
   AND channel_code = 'CHANNEL001'
   └─ 利用bloom_filter索引
```

---

## 十五、维护建议

### 15.1 日常维护

1. **监控刷新状态**: 定期检查物化视图刷新是否正常
2. **分区管理**: 定期归档或删除历史分区数据
3. **统计信息更新**: 定期更新表统计信息优化查询计划
4. **存储监控**: 监控物化视图存储空间使用情况

### 15.2 性能监控

```sql
-- 查看物化视图刷新状态
SHOW ALTER TABLE MATERIALIZED VIEW;

-- 查看表统计信息
SHOW TABLE STATS ads_voc_model_tags_result_data_mv;

-- 查看分区信息
SHOW PARTITIONS FROM ads_voc_model_tags_result_data_mv;
```

---

**文档版本**: v2.0  
**生成时间**: 2025  
**维护团队**: VOC报表服务团队  
**更新说明**: 整合SelectDB物化视图详细信息和优化建议
