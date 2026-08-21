# VOC数据流转图

## 概述
本文档描述VOC系统中物化视图的数据流转关系和依赖层次。

## 数据流转层次

### 第一层：基础数据源（MySQL JDBC）

#### 1.1 客户数据源
- **voc_imp_hudi_dm_voc_cust** → `voc_imp_hudi_dm_voc_cust_mv`
  - 客户基础信息（oneid, 手机号, 身份证等）
  - 刷新频率：每24小时

- **voc_imp_hudi_dm_voc_cust_vehicle_rel** → `voc_imp_hudi_dm_voc_cust_vehicle_rel_mv`
  - 客户车辆关系
  - 刷新频率：每24小时

#### 1.2 车辆数据源
- **voc_imp_hudi_dwd_maf_veh_d_full** → `voc_imp_hudi_dwd_maf_veh_d_full_mv`
  - 车辆详细信息（VIN, 颜色, 生产日期等）
  - 刷新频率：每24小时

#### 1.3 经销商数据源
- **voc_imp_hudi_dim_chn_dlr_zip_d_full** → `voc_imp_dealer_json_info_mv`
  - 经销商信息JSON化
  - 刷新频率：每30分钟

#### 1.4 配置数据源（MySQL）
- **ins_brand_info** → `voc_ext_ins_brand_info_mv`
  - 品牌信息
  - 刷新频率：每30分钟

- **ins_channel** → `voc_ext_ins_channel_mv`
  - 渠道信息
  - 刷新频率：每30分钟

- **ins_tag_client** → `voc_ext_ins_tag_client_mv`
  - 标签客户端配置
  - 刷新频率：每30分钟

- **ins_car_series_info** → `voc_ext_ins_car_series_info_mv`
  - 车系信息
  - 刷新频率：每30分钟

- **ins_province_area** → `voc_ext_ins_province_area_mv`
  - 省份区域信息
  - 刷新频率：每30分钟

---

### 第二层：客户车辆关联JSON视图

#### 2.1 客户车辆关系整合
```
voc_imp_hudi_dm_voc_cust_mv
    + voc_imp_hudi_dm_voc_cust_vehicle_rel_mv
    + voc_imp_hudi_dwd_maf_veh_d_full_mv
    ↓
voc_imp_cust_vehicle_rel_json_info_mv
```
- 功能：将客户、车辆关系、车辆详情整合为JSON
- 刷新频率：每24小时

#### 2.2 多维度客户查询视图
```
voc_imp_cust_vehicle_rel_json_info_mv
    ↓
    ├─ voc_imp_cust_json_by_one_id_mv (按one_id)
    ├─ voc_imp_cust_json_b_by_mobile_mv (按手机号)
    ├─ voc_imp_cust_json_b_by_idcard_mv (按身份证)
    └─ voc_imp_cust_json_b_by_vin_mv (按VIN)
```
- 功能：为不同查询场景提供快速索引
- 刷新频率：每24小时

---

### 第三层：标签体系构建

#### 3.1 标签层级展开
```
ins_tag_client (MySQL)
    ↓
voc_ext_ins_tag_by_level_mv
```
- 功能：将5级标签展开为1-4级标签层级
- 刷新频率：每30分钟（完全刷新）

#### 3.2 标签系统化
```
voc_ext_ins_tag_by_level_mv
    ↓
voc_ext_ins_tag_by_system_mv
```
- 功能：按标签类型（CPT/JOUR/PR0/CA/VRT/NPS）分类
- 刷新频率：每30分钟（完全刷新）

#### 3.3 标签最终视图
```
voc_ext_ins_tag_by_system_mv
    ↓
voc_ext_ins_tag_system_final_mv
```
- 功能：合并相同topic的多行标签数据
- 刷新频率：每30分钟（完全刷新）

---

### 第四层：原始数据处理

#### 4.1 增量数据合并
```
voc_anal_di_stg_mate_data_m_inc
    + voc_imp_cust_json_by_one_id_mv
    + voc_imp_cust_json_b_by_mobile_mv
    + voc_imp_cust_json_b_by_idcard_mv
    + voc_imp_cust_json_b_by_vin_mv
    + voc_imp_dealer_json_info_mv
    ↓
voc_anal_di_stg_mate_data_m_batch_range_merge_mv
```
- 功能：增量数据与客户、经销商信息关联
- 刷新频率：每30分钟
- 处理范围：最近2小时数据
- 批量限制：20万条/次

#### 4.2 全量数据视图
```
voc_anal_flow_mate_data_full
    ↓
voc_anal_flow_mate_data_full_mv
```
- 功能：全量原始数据视图
- 刷新频率：每30分钟
- 分区：按月分区

#### 4.3 已标注数据视图
```
voc_anal_flow_model_tags_result_data_full_mv
    ↓
voc_anal_flow_mate_data_labeled_mv
```
- 功能：提取已完成标注的数据
- 刷新频率：每30分钟

---

### 第五层：模型标注结果

#### 5.1 模型标注全量数据
```
voc_anal_flow_model_tags_result_data_full
    ↓
voc_anal_flow_model_tags_result_data_full_mv
```
- 功能：过滤有效标注数据（品牌、渠道、标签、内容类型非空）
- 刷新频率：每30分钟
- 数据范围：最近24个月
- 分区：按月分区

#### 5.2 无效数据视图
```
voc_anal_flow_model_tags_result_data_full
    ↓
voc_anal_flow_model_tags_result_invalid_data_full_mv
```
- 功能：识别无效标注数据（关键字段为空或包含中文）
- 刷新频率：手动刷新

---

### 第六层：业务结果视图

#### 6.1 情感标注结果（通用）
```
voc_anal_flow_model_tags_result_data_full_mv
    + voc_ext_ins_brand_info_mv
    + voc_ext_ins_channel_mv
    + voc_ext_ins_car_series_info_mv
    + voc_ext_ins_tag_system_final_mv
    ↓
voc_anal_flow_sentiment_annotations_results_mv
```
- 功能：整合标注结果与配置信息
- 刷新频率：每30分钟
- 分区：按月分区
- 字段：180+业务字段

#### 6.2 情感标注结果（风险监控）
```
voc_anal_flow_model_tags_result_data_full_mv (最近1天)
    + voc_ext_ins_brand_info_mv
    + voc_ext_ins_channel_mv
    + voc_ext_ins_car_series_info_mv
    + voc_ext_ins_tag_system_final_mv
    ↓
voc_anal_flow_sentiment_annotations_results_risk_mv
```
- 功能：实时风险监控（仅处理最近1天数据）
- 刷新频率：每30分钟
- 特点：包含转发、评论等社交媒体字段

#### 6.3 情感标注结果（洞察引擎）
```
voc_anal_flow_sentiment_annotations_results_mv
    + voc_anal_flow_model_tags_result_data_full_mv (原文)
    ↓
voc_anal_flow_sentiment_annotations_results_ins_mv
```
- 功能：为洞察引擎提供完整数据（包含原文）
- 刷新频率：每30分钟
- 分区：按月分区

---

### 第七层：辅助视图

#### 7.1 新词管理
```
voc_model.new_words_management (MySQL)
    ↓
voc_ins_model_new_words_mv
```
- 功能：展开新词data_id_list数组
- 刷新频率：每24小时

#### 7.2 数据状态追踪
```
voc_anal_flow_mate_data_status
    ↓
voc_anal_flow_mate_data_status_mv
```
- 功能：追踪数据处理状态
- 刷新频率：每30分钟
- 特点：取每个data_id的最新状态

---

## 核心数据流

### 主流程：原始数据 → 标注结果 → 业务视图
```
原始数据采集
    ↓
voc_anal_di_stg_mate_data_m_inc
    ↓
voc_anal_di_stg_mate_data_m_batch_range_merge_mv (关联客户/经销商)
    ↓
voc_anal_flow_mate_data_full
    ↓
模型标注处理
    ↓
voc_anal_flow_model_tags_result_data_full
    ↓
voc_anal_flow_model_tags_result_data_full_mv (过滤有效数据)
    ↓
voc_anal_flow_sentiment_annotations_results_mv (整合配置)
    ↓
    ├─ voc_anal_flow_sentiment_annotations_results_risk_mv (风险监控)
    └─ voc_anal_flow_sentiment_annotations_results_ins_mv (洞察引擎)
```

### 辅助流程：配置数据同步
```
MySQL配置表
    ↓
    ├─ voc_ext_ins_brand_info_mv (品牌)
    ├─ voc_ext_ins_channel_mv (渠道)
    ├─ voc_ext_ins_car_series_info_mv (车系)
    └─ voc_ext_ins_tag_system_final_mv (标签体系)
    ↓
关联到业务结果视图
```

### 客户数据流程
```
Hudi客户表
    ↓
    ├─ voc_imp_hudi_dm_voc_cust_mv
    ├─ voc_imp_hudi_dm_voc_cust_vehicle_rel_mv
    └─ voc_imp_hudi_dwd_maf_veh_d_full_mv
    ↓
voc_imp_cust_vehicle_rel_json_info_mv (JSON整合)
    ↓
    ├─ voc_imp_cust_json_by_one_id_mv
    ├─ voc_imp_cust_json_b_by_mobile_mv
    ├─ voc_imp_cust_json_b_by_idcard_mv
    └─ voc_imp_cust_json_b_by_vin_mv
    ↓
关联到原始数据处理
```

---

## 刷新策略

### 高频刷新（每30分钟）
- 配置类视图：品牌、渠道、车系、标签
- 业务结果视图：情感标注结果系列
- 数据处理视图：增量合并、全量视图

### 低频刷新（每24小时）
- 客户数据视图：客户信息、车辆信息、客户车辆关系
- 新词管理视图

### 手动刷新
- 无效数据视图：用于数据质量分析

---

## 数据分区策略

### 按月分区
- `voc_anal_flow_model_tags_result_data_full_mv`
- `voc_anal_flow_sentiment_annotations_results_mv`
- `voc_anal_flow_sentiment_annotations_results_ins_mv`
- `voc_anal_flow_mate_data_full_mv`

### 无分区
- 配置类视图（数据量小）
- 客户数据视图（按主键分布）

---

## 关键依赖关系

### 标签体系依赖链
```
ins_tag_client 
  → voc_ext_ins_tag_by_level_mv 
  → voc_ext_ins_tag_by_system_mv 
  → voc_ext_ins_tag_system_final_mv
```

### 客户数据依赖链
```
voc_imp_hudi_dm_voc_cust 
  → voc_imp_hudi_dm_voc_cust_mv 
  → voc_imp_cust_vehicle_rel_json_info_mv 
  → voc_imp_cust_json_by_*_mv
```

### 业务结果依赖链
```
voc_anal_flow_model_tags_result_data_full 
  → voc_anal_flow_model_tags_result_data_full_mv 
  → voc_anal_flow_sentiment_annotations_results_mv 
  → voc_anal_flow_sentiment_annotations_results_ins_mv
```

---

## 数据质量控制

### 有效性过滤
- 品牌编码非空且不含中文
- 渠道编码非空且不含中文
- 标签topic非空且不含中文
- 内容类型非空且不含中文

### 时间范围控制
- 模型标注结果：最近24个月
- 风险监控：最近1天
- 增量处理：最近2小时

### 数据去重
- 按data_id分组取最大id
- 按topic合并多行标签数据

---

## 性能优化

### 布隆过滤器
- 高频查询字段：brand_code, car_series_code, topic, channel_code
- 客户查询字段：oneid, mobile, id_card_no, vin

### 分桶策略
- 大表：32-64桶（结果视图）
- 中表：8-16桶（客户数据）
- 小表：2-4桶（配置数据）

### 增量处理
- 批量限制：20万条/次
- 时间窗口：2小时
- 去重机制：finished_record表

---

## 注意事项

1. **数据加密**：内部数据（is_outer='N'）的手机号、身份证、VIN等敏感字段使用SM4加密
2. **JSON处理**：大量使用JSON_EXTRACT_STRING提取嵌套字段
3. **NULL处理**：使用nvl/nullif处理空值，避免刷新失败
4. **字符集过滤**：使用REGEXP过滤中文字符，确保编码一致性
5. **分区管理**：按月分区需定期维护历史分区

---

## 更新日志

- 2026-01-06：初始版本，基于export_202601061620.csv创建
