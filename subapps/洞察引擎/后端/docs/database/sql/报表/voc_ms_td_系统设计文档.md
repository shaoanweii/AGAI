# VOC_MS_TD 数据库系统设计文档

## 一、系统概述

### 1.1 数据库简介
- **数据库名称**: voc_ms_td
- **数据库类型**: SelectDB (基于Apache Doris)
- **存储格式**: V2
- **倒排索引格式**: V1/V2
- **业务领域**: VOC(Voice of Customer)客户之声分析系统 - 报表服务

### 1.2 系统架构
本数据库采用分层架构设计，遵循数据仓库建模规范：
- **ODS层**: 原始数据层（未在此库体现）
- **DWD层**: 数据明细层 - 清洗后的明细数据
- **ADS层**: 应用数据层 - 面向业务应用的汇总宽表
- **应用层**: 视图层 - 为应用提供统一查询接口

---

## 二、数据分层设计

### 2.1 DWD层 - 数据明细层

#### 2.1.1 dwd_voc_all_meta_data (VOC元数据明细表)
**表说明**: 存储VOC系统所有原始元数据，包含客户、车辆、经销商等完整信息

**主键**: id, create_time

**核心字段**:
- **基础信息**: id, create_time, content_type, data_create_time, data_update_time
- **数据标识**: data_id, channel_code, brand, series, model
- **客户信息**: one_id, user_id, user_name, mobile, email, global_id
- **车辆信息**: vhl_id, vhl_vin, id_car_no
- **经销商信息**: dlr_id, dlr_code, dlr_type, market_id
- **内容信息**: title, content, is_wsater_army(水军标识), weight(权重)
- **扩展属性**: attrs, attrs2, attrs3 (JSON格式)
- **业务标识**: work_id, done, model_type, ds, data_status

**索引策略**:
- BLOOM_FILTER: create_time, data_create_time, data_id

**用途**: 作为数据清洗后的明细数据存储，为上层ADS层提供数据源

---

### 2.2 ADS层 - 应用数据层

#### 2.2.1 核心事实表

##### ads_voc_model_tags_result_data_m_inc (模型标签结果增量表)
**表说明**: 存储VOC模型打标后的结果数据，包含情感分析、意图识别、标签体系等核心业务数据

**主键**: id, publish_time, data_id

**核心字段分类**:

1. **基础标识**
   - id: 主键ID
   - data_id: 数据ID
   - publish_time: 发布时间
   - channel_id: 渠道ID
   - create_time/update_time: 创建/更新时间

2. **业务分析字段**
   - topic: 观点主题
   - intention_type: 意图类型
   - sentiment: 情感倾向
   - sentiment_score: 情感分数
   - opinion: 原始观点
   - subject: 主题
   - fault_level: 故障等级
   - description: 描述
   - keywords: 关键词

3. **客户与场景**
   - one_id: 统一客户ID
   - work_id: 工单ID
   - scenario: 场景
   - client_id: 客户端ID
   - content_type: 内容类型
   - sample_data_type: 样本数据类型

4. **品牌车系**
   - brand_code: 品牌编码
   - car_series_code: 车系编码
   - label_type: 标签类型

5. **扩展属性** (JSON格式)
   - biz_ext_attrs: 业务扩展属性
   - biz_ext_attrs2: 业务扩展属性2
   - biz_ext_attrs3: 业务扩展属性3
   - cust_ext_attrs: 客户扩展属性
   - vhl_ext_attrs: 车辆扩展属性
   - dealer_ext_attrs: 经销商扩展属性
   - prd_ext_attrs: 产品扩展属性
   - tags_ext_attrs: 标签扩展属性

6. **数据溯源**
   - original_id: 原始ID
   - input_data_id: 输入数据ID
   - original_text_scene: 原始文本场景
   - raw_data: 原始数据
   - ext_fields: 扩展字段

7. **状态标识**
   - model_type: 模型类型
   - abandon: 废弃标识
   - done: 完成标识

**用途**: 核心业务表，存储模型计算后的标签结果，为报表分析提供数据基础

---

##### ads_voc_all_mate_data_m_full (VOC元数据月度全量表)
**表说明**: 月度全量元数据表，与dwd_voc_all_meta_data结构相同

**字段结构**: 同dwd_voc_all_meta_data

**用途**: 提供月度全量数据快照，支持历史数据分析

---

#### 2.2.2 物化视图

##### ads_voc_model_tags_result_data_mv (模型标签结果物化视图)
**视图说明**: 整合模型标签、品牌、车系、渠道、标签系统的宽表物化视图

**数据源**:
- 主表: ads_voc_model_tags_result_data_m_inc
- LEFT JOIN ins_brand_info (品牌信息)
- LEFT JOIN ins_car_series_info (车系信息)
- LEFT JOIN ads_voc_channel_m_full_mv (渠道物化视图)
- LEFT JOIN ads_voc_tags_system_info_h_full_mv (标签系统物化视图)

**刷新策略**: 每1小时自动刷新

**用途**: 为应用层提供高性能的宽表查询，避免多表JOIN

---

##### ads_voc_channel_m_full_mv (渠道月度全量物化视图)
**数据源**: ins_channel

**用途**: 提供渠道维度数据的快速查询

---

##### ads_voc_tags_system_info_h_full_mv (标签系统信息小时全量物化视图)
**用途**: 提供标签体系的完整信息，包括CPT、UJY、CMA、DOM、NPS、VTR等标签

---

##### ads_voc_tags_h_full_mv (标签小时全量物化视图)
**用途**: 标签数据的小时级汇总

---

##### ads_voc_tags_system_h_full_mv (系统标签小时全量物化视图)
**用途**: 系统级标签的小时汇总

---

### 2.3 应用层 - 视图层

#### voc_sentiment_annotations_results_v (VOC声音数据视图)
**视图定义**:
```sql
CREATE VIEW voc_sentiment_annotations_results_v AS
SELECT * FROM ads_voc_model_tags_result_data_mv;
```

**用途**: 为应用层提供声音数据的统一查询接口，屏蔽底层物化视图细节

---

#### voc_anal_flow_mate_data_full_v (VOC元数据全量增量视图)
**视图定义**:
```sql
CREATE VIEW voc_anal_flow_mate_data_full_v AS
SELECT 
    id, create_time, content_type, data_create_time, data_update_time,
    data_id, channel_code, brand, series, model, is_outer, one_id,
    id_car_no, mobile, email, global_id, user_id, user_name,
    vhl_id, vhl_vin, dlr_id, dlr_code, dlr_type, market_id,
    title, content, is_wsater_army, weight, attrs, attrs2, attrs3,
    work_id, done, model_type, ds, data_status
FROM ads_voc_all_mate_data_m_full;
```

**用途**: 提供元数据的查询接口

---

## 三、维度表设计

### 3.1 品牌维度

#### ins_brand_info (品牌信息表)
**主键**: id, create_time

**核心字段**:
- **基础信息**: id, code, name, name_en, alias
- **品牌属性**: competitive_type(竞品类型), country(国家), nature(性质)
- **业务标识**: is_core(核心品牌), competitive_product(竞品), automark
- **管理字段**: order_by, operator, update_time, del_flag, app_id
- **其他**: exclusion_words(排除词), img(图片)

**用途**: 品牌维度数据，支持品牌分析

---

### 3.2 车系维度

#### ins_car_series_info (车系信息表)
**主键**: id, create_time

**核心字段**:
- **基础信息**: id, name, name_en, code, alias
- **品牌关联**: brand_id, brand_code
- **车型信息**: car_name, car_code, factory
- **分类属性**: 
  - car_level1/car_level2: 车型级别
  - energy_type1/energy_type2: 能源类型
  - level_name: 级别名称
- **竞品信息**: competitive_type, competitive_product
- **业务标识**: is_core, start_time, end_time
- **管理字段**: order_by, operator, update_time, del_flag

**用途**: 车系维度数据，支持车系分析

---

### 3.3 渠道维度

#### ins_channel (渠道信息表)
**主键**: id, create_time

**核心字段**:
- **基础信息**: id, name, name_en, code, description
- **层级关系**: parent_id, level, top_id
- **渠道属性**: 
  - type: 渠道类型
  - is_core_channel: 核心渠道标识
  - data_source_type: 数据源类型
- **管理字段**: status, update_time

**用途**: 渠道维度数据，支持多级渠道分析

---

### 3.4 标签维度

#### ins_tag_client (标签客户表)
**主键**: id, create_time

**核心字段**:
- **标签基础**: tag_name, tag_name_en, tag_code, tag_type
- **标签层级**: tag_parent_id, level, sort
- **标签属性**: 
  - tag_attribute: 标签属性
  - tag_status: 标签状态
  - tag_description: 标签描述
  - seriousness: 严重程度
- **用户旅程**: user_journey1, user_journey2, user_journey3
- **场景属性**: scenario_attr, event_clarity
- **责任部门**: d2c_responsible_dept, d2c_cc_dept, d2c_accountable_dept
- **业务标识**:
  - emotion: 情感
  - intention: 意图
  - energy_type: 能源类型
  - car_type: 车型
- **扩展标识**:
  - tag_accuracy: 标签准确性
  - tag_customer_issue_classification: 客户问题分类
  - tag_issue_severity: 问题严重程度
  - tag_code_status: 标签编码状态
  - tag_business_domain: 业务领域
  - tag_high_value_flag: 高价值标识
  - tag_complaint_flag_needing_reply: 需回复投诉标识
  - tag_high_quality_voc_flag: 高质量VOC标识
  - tag_new_energy_or_fuel: 新能源或燃油
  - tag_need_forvclosed_loop: 需闭环标识
- **管理字段**: create_user, update_user, app_client

**用途**: 标签体系维度数据，支持多维度标签分析

---

## 四、报表业务表设计

### 4.1 用户行为表

#### report_user_browse_record (用户浏览记录表)
**主键**: id

**核心字段**:
- sound_id: 声音ID
- original_id: 原始ID
- browse_user_id: 浏览用户ID
- create_time: 创建时间
- browse_duration: 浏览时长
- sound_intention: 声音意图

**用途**: 记录用户浏览行为，支持用户行为分析

---

### 4.2 数据质量表

#### report_high_quality_record (高质量记录表)
**主键**: id

**核心字段**:
- sound_id: 声音ID
- original_id: 原始ID
- data_create_time: 数据创建时间
- status: 状态
- create_time/update_time: 创建/更新时间

**用途**: 标记高质量VOC数据，支持数据质量管理

---

#### report_label_correction_info (标签纠正信息表)
**主键**: id

**核心字段**:
- data_id: 数据ID
- correction_record_id: 纠正记录ID
- channel_id: 渠道ID
- topic, intention_type, sentiment: 业务字段
- publish_time: 发布时间
- 其他字段同ads_voc_model_tags_result_data_m_inc

**用途**: 记录标签纠正信息，支持模型优化和数据质量提升

---

#### report_label_correction_record (标签纠正记录表)
**用途**: 记录标签纠正的历史记录

---

### 4.3 报表配置表

#### report_custom_report (自定义报表表)
**主键**: id

**核心字段**:
- report_name: 报表名称
- view_count: 浏览次数
- collection_count: 收藏次数
- type: 报表类型
- default_condition: 默认条件
- date_condition: 日期条件
- brand_code: 品牌编码
- special_type_id: 特殊类型ID
- first_level_zone_id: 一级区域ID
- report_url: 报表URL
- description: 描述
- status: 状态
- create_by/update_by: 创建人/更新人
- del_flag: 删除标识

**用途**: 管理自定义报表配置，支持灵活的报表定制

---

#### report_display_rule (报表展示规则表)
**主键**: id

**核心字段**:
- metric_code: 指标编码
- metric_name: 指标名称
- range_min/range_max: 范围最小值/最大值
- color_hex: 颜色十六进制
- background_color_hex: 背景颜色十六进制
- emoji_key: 表情符号键
- sort_no: 排序号
- status: 状态

**用途**: 配置报表展示规则，支持可视化展示

---

### 4.4 专项分析表

#### report_special_analysis_type (专项分析类型表)
**用途**: 定义专项分析的类型

---

#### report_special_analysis_role (专项分析角色表)
**用途**: 管理专项分析的角色权限

---

### 4.5 日志表

#### report_view_log (报表查看日志表)
**用途**: 记录报表查看日志，支持审计和统计

---

## 五、辅助维度表

### 5.1 品牌车系视图

#### brand_all_v (所有品牌视图)
**用途**: 提供所有品牌的查询视图

---

#### brand_self_brand_v (自主品牌视图)
**用途**: 提供自主品牌的查询视图

---

#### brand_series_all_v (所有品牌车系视图)
**用途**: 提供品牌车系关联的查询视图

---

### 5.2 其他维度

#### province_dict_data_v (省份字典数据视图)
**用途**: 提供省份维度数据

---

#### voc_ins_user_journey_level1_v (用户旅程一级视图)
**用途**: 提供用户旅程一级分类数据

---

#### dwd_voc_ext_attrs_mapping_values (扩展属性映射值表)
**用途**: 存储扩展属性的映射关系

---

## 六、数据流向关系

### 6.1 核心数据流（含Kafka实时消费）

```
Kafka Topic: VDP_dwd_voc2_all_meta_data
    ↓
Kafka Broker: 172.16.80.16:30092
    ↓
Routine Load Task: dwd_voc_all_meta_data_kafka
    ├─ Consumer Group: VDP-voc2-analysis
    ├─ 数据格式: JSON (20个字段映射)
    ├─ 批次配置: 2000万行 或 1GB 或 60秒
    └─ 加载模式: APPEND追加
    ↓
dwd_voc_all_meta_data (DWD层-明细数据)
    ↓
ads_voc_all_mate_data_m_full (ADS层-月度全量)
    ↓
voc_anal_flow_mate_data_full_v (视图层)
    ↓
应用层查询
```

### 6.2 模型标签数据流

```
模型计算
    ↓
ads_voc_model_tags_result_data_m_inc (ADS层-标签结果)
    ├─ LEFT JOIN ins_brand_info (品牌维度)
    ├─ LEFT JOIN ins_car_series_info (车系维度)
    ├─ LEFT JOIN ads_voc_channel_m_full_mv (渠道维度)
    └─ LEFT JOIN ads_voc_tags_system_info_h_full_mv (标签维度)
    ↓
ads_voc_model_tags_result_data_mv (物化视图-每小时刷新)
    ↓
voc_sentiment_annotations_results_v (视图层)
    ↓
应用层查询/报表展示
```

### 6.3 渠道数据流

```
ins_channel (渠道基础表)
    ↓
ads_voc_channel_m_full_mv (物化视图)
    ↓
关联到主数据流
```

### 6.4 标签数据流

```
标签系统表
    ├─ ads_voc_tags_h_full_mv (小时物化视图)
    ├─ ads_voc_tags_system_h_full_mv (系统标签物化视图)
    └─ ads_voc_tags_system_info_h_full_mv (标签系统信息物化视图)
    ↓
关联到主数据流
```

---

## 七、关键字段说明

### 7.1 标签体系字段

#### CPT标签 (产品标签)
- cpt_tag_first: CPT一级标签
- cpt_tag_second: CPT二级标签
- cpt_tag_three: CPT三级标签
- cpt_tag_four: CPT四级标签

#### UJY标签 (用户旅程标签)
- ujy_tag_first: UJY一级标签
- ujy_tag_second: UJY二级标签
- ujy_tag_three: UJY三级标签
- ujy_tag_four: UJY四级标签

#### CMA标签 (全领域业务标签)
- cma_tag_first: CMA一级标签
- cma_tag_second: CMA二级标签
- cma_tag_three: CMA三级标签
- cma_tag_four: CMA四级标签

#### DOM标签 (商品化属性标签)
- dom_tag_first: DOM一级标签
- dom_tag_second: DOM二级标签
- dom_tag_three: DOM三级标签
- dom_tag_four: DOM四级标签

#### NPS标签
- nps_tag_first: NPS一级标签
- nps_tag_second: NPS二级标签
- nps_tag_three: NPS三级标签
- nps_tag_four: NPS四级标签

#### VTR标签
- vtr_tag_first: VTR一级标签
- vtr_tag_second: VTR二级标签
- vtr_tag_three: VTR三级标签
- vtr_tag_four: VTR四级标签

### 7.2 客户信息字段
- cust_*: 客户相关信息 (年龄、性别、学历、收入等)
- one_id: 统一客户ID
- user_id: 用户ID
- user_name: 用户名称
- mobile: 手机号
- email: 邮箱

### 7.3 车辆信息字段
- vhl_*: 车辆相关信息
- vhl_id: 车辆ID
- vhl_vin: 车辆VIN码
- vhl_color: 车辆颜色
- vhl_production_date: 生产日期

### 7.4 经销商信息字段
- dlr_oc_*: 订单中心经销商信息
- dlr_dc_*: 交付中心经销商信息
- dlr_mc_*: 维保中心经销商信息
- dlr_id: 经销商ID
- dlr_code: 经销商编码
- dlr_type: 经销商类型

### 7.5 业务标识字段
- sentiment: 情感倾向 (正面/负面/中性)
- intention: 用户意图
- topic: 观点主题
- opinion: 原始观点
- subject: 主题
- fault_level: 故障等级
- keywords: 关键词

---

## 八、性能优化策略

### 8.1 物化视图刷新策略
- **ads_voc_model_tags_result_data_mv**: 每小时刷新，适合准实时分析
- **建议**: 在业务低峰期刷新以减少系统负载

### 8.2 分区策略
- **分区字段**: publish_time, create_time
- **优势**: 便于历史数据管理和查询优化
- **建议**: 定期归档历史分区数据

### 8.3 索引优化
- **BLOOM_FILTER索引**: 
  - brand_code
  - car_series_code
  - topic
  - channel_code
  - data_id
  - create_time
  - data_create_time
- **说明**: 这些字段是高频查询字段，已建立布隆过滤器索引

### 8.4 查询优化建议
1. 优先使用物化视图而非基础表
2. 使用视图 voc_sentiment_annotations_results_v 进行应用层查询
3. 避免直接查询 ads_voc_model_tags_result_data_m_inc
4. 合理使用分区裁剪，指定时间范围查询
5. 对于大批量数据导出，使用异步任务

---

## 九、数据质量保障

### 9.1 数据完整性
- 主键约束确保数据唯一性
- 外键关联保证维度数据一致性

### 9.2 数据准确性
- report_label_correction_info 记录标签纠正
- report_high_quality_record 标记高质量数据

### 9.3 数据时效性
- 物化视图每小时刷新
- 增量表支持实时数据写入

---

## 十、系统扩展性

### 10.1 扩展属性设计
采用JSON格式存储扩展属性，支持灵活扩展：
- biz_ext_attrs: 业务扩展属性
- cust_ext_attrs: 客户扩展属性
- vhl_ext_attrs: 车辆扩展属性
- dealer_ext_attrs: 经销商扩展属性
- prd_ext_attrs: 产品扩展属性
- tags_ext_attrs: 标签扩展属性

### 10.2 多租户支持
- app_id: 应用ID
- app_client: 应用客户端
- brand_code: 品牌编码

### 10.3 历史数据管理
- ds: 数据分区标识
- data_status: 数据状态
- del_flag: 删除标识

---

## 十一、安全与权限

### 11.1 数据脱敏
敏感字段需要脱敏处理：
- mobile: 手机号
- email: 邮箱
- id_car_no: 身份证/车牌号

### 11.2 审计日志
- report_view_log: 报表查看日志
- create_by/update_by: 操作人记录
- create_time/update_time: 操作时间记录

---

## 十二、总结

### 12.1 系统特点
1. **分层清晰**: DWD-ADS-应用层三层架构
2. **性能优化**: 物化视图+布隆过滤器索引
3. **灵活扩展**: JSON扩展属性+多维度标签体系
4. **数据质量**: 标签纠正+高质量标记机制
5. **业务完整**: 覆盖品牌、车系、渠道、标签等全维度

### 12.2 核心业务流程
1. 数据采集 → DWD层清洗
2. DWD层 → ADS层汇总
3. ADS层 → 物化视图优化
4. 物化视图 → 应用视图
5. 应用视图 → 报表展示

### 12.3 技术栈
- 数据库: SelectDB (Apache Doris)
- 存储: 列式存储V2
- 索引: 布隆过滤器+倒排索引
- 优化: 物化视图+分区表

---

**文档版本**: v1.0  
**生成时间**: 2025  
**维护团队**: VOC报表服务团队
