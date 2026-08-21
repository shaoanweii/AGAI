# VOC数据库设计文档

## 1. 概述

### 1.1 项目背景
VOC（Voice of Customer）平台是一个专门用于收集、分析和处理客户声音的数据平台。该平台通过多源数据整合，为汽车企业提供全面的客户洞察，支持产品优化、服务改进和营销决策。

### 1.2 数据库架构
- **数据库名称**: VDP_RS_TD (VOC Data Platform Report Service)
- **数据库类型**: StarRocks OLAP数据库
- **数据规模**: 约22万条声音数据记录
- **数据来源**: 8个主要渠道（小红书、知乎、微博、网站、汽车之家、贴吧、微信、抖音）

### 1.3 设计原则
- **分层架构**: 采用DWD-DWS-DIM分层设计
- **数据质量**: 确保数据完整性、准确性和一致性
- **性能优化**: 通过分区、索引等策略优化查询性能
- **扩展性**: 支持未来数据增长和业务扩展

## 2. 数据架构

### 2.1 分层架构图
```
┌─────────────────────────────────────────────────────────────┐
│                        应用层                                │
├─────────────────────────────────────────────────────────────┤
│                        视图层                                │
│  业务视图:                                                  │
│  voc2_computed_result_all_data_m_v (计算结果汇总)           │
│  voc2_cust_info_m_v (客户信息)                              │
│  voc2_dealer_info_m_v (经销商信息)                          │
│  voc2_vehicle_info_m_v (车辆信息)                           │
│  voc2_dealership_data_info_m_v (经销商数据)                 │
│  voc2_voc2_computed_result_all_data_v (计算结果数据)        │
│                                                             │
│  权限视图:                                                  │
│  voc2_user_label_scope_perms_v (用户标签权限)               │
│  voc2_ins_car_series_perms_v (车系权限)                     │
│                                                             │
│  配置视图:                                                  │
│  voc2_ins_car_series_info_m_v (车系配置)                    │
│  voc2_ins_channel_info_m_v (渠道配置)                       │
│  voc2_ins_project_car_series_info_v (项目车系)              │
│  voc2_ins_province_area_m_v (省份区域)                      │
│  voc2_ins_tags_info_m_v (标签配置)                          │
│                                                             │
│  元数据视图:                                                │
│  voc2_raw_meta_data_range_m_v (原始数据元数据)              │
│  voc2_sta_dict_v (统计字典)                                 │
│  voc2_sta_tag_level_values_v (标签层级统计)                 │
├─────────────────────────────────────────────────────────────┤
│                        汇总层 (DWS)                          │
│  voc_sentiment_annotations_results_v (核心事实表)                           │
│  dws_voc2_batch_push_record                                 │
│  dws_voc2_error_push_data                                   │
├─────────────────────────────────────────────────────────────┤
│                        维度层 (DIM)                          │
│  dim_voc2_cust_info (客户维度)                              │
│  dim_voc2_dealer_info (经销商维度)                          │
│  dim_voc2_vehicle_info (车辆维度)                           │
│  dim_voc2_product_info (产品维度)                           │
│  dim_voc2_big_v_user_info (大V用户维度)                     │
│  dim_voc2_manager_user_info (管理员维度)                    │
├─────────────────────────────────────────────────────────────┤
│                        明细层 (DWD)                          │
│  dwd_voc2_raw_public_opinion (公开意见)                     │
│  dwd_voc2_raw_private_opinion (私密意见)                    │
│  dwd_voc2_raw_public_consult (公开咨询)                     │
│  dwd_voc2_raw_private_consult (私密咨询)                    │
│  dwd_voc2_raw_public_posts_comment (公开帖子评论)           │
│  dwd_voc2_raw_private_posts_comment (私密帖子评论)          │
│  dwd_voc2_raw_public_questionnaire (公开问卷)               │
│  dwd_voc2_raw_private_questionnaire (私密问卷)              │
│  dwd_voc2_raw_private_work_order (私密工单)                 │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 数据流向图
```
数据源 → 数据采集 → 数据清洗 → 数据标注 → 数据聚合 → 数据服务
  ↓         ↓         ↓         ↓         ↓         ↓
原始数据 → 明细数据 → 汇总数据 → 维度数据 → 视图数据 → 应用接口
```

## 3. 核心表结构

### 3.1 事实表 (voc_sentiment_annotations_results_v)
核心业务事实表，包含所有声音数据的汇总信息。

**主要特征**:
- 数据量: 220,500条记录
- 分区策略: 按时间分区（季度）
- 主键: id
- 分布策略: HASH(id) BUCKETS 10

**关键字段分类**:
1. **基础信息**: id, data_id, channel_code, brand_code等
2. **情感分析**: sentiment, intention, label_type
3. **内容分析**: hot_word, keywords, user_journey, topic
4. **多维度标签**: vtr_tag_*, com_tag_*, adb_tag_*等8类标签
5. **时间维度**: data_create_time及周/月/季/年字段
6. **关联维度**: 客户、经销商、车辆信息

### 3.2 维度表
- **客户维度**: 客户基本信息、人口统计学特征、购买历史
- **经销商维度**: 经销商信息、层级结构、地理位置
- **车辆维度**: 车辆信息、配置信息、生产信息
- **产品维度**: 产品分类、规格参数
- **用户维度**: 大V用户、管理员用户信息

## 4. 标签体系

### 4.1 标签分类
系统采用8大类、4层级的标签体系：

1. **VTR标签** (Vehicle Technology Related): 车辆技术相关
2. **COM标签** (Commercialization): 商品化属性
3. **ADB标签** (All Domain Business): 全领域业务
4. **WOM标签** (Word of Mouth): 口碑评价指标
5. **CX标签** (Customer Experience): 客户体验指标
6. **CJ标签** (Customer Journey): 全旅程客户
7. **SL标签** (Sales Lead): 销售线索
8. **OM标签** (Omni Media): 全媒体指标

### 4.2 标签层级
每类标签都包含4个层级：
- 一级标签: 主要分类
- 二级标签: 子分类
- 三级标签: 具体项目
- 四级标签: 详细描述

## 5. 数据质量

### 5.1 数据完整性
- 主键完整性: 100%
- 必填字段完整性: >95%
- 外键关联完整性: >90%

### 5.2 数据准确性
- 情感标注准确率: >85%
- 标签标注准确率: >80%
- 地理位置准确率: >90%

### 5.3 数据一致性
- 编码标准统一
- 命名规范一致
- 数据格式标准化

## 6. 性能优化

### 6.1 分区策略
- 按时间分区（季度）
- 支持历史数据归档
- 优化时间范围查询

### 6.2 索引策略
- 主键索引: id
- 复合索引: (channel_code, data_create_time)
- 标签索引: 各标签字段

### 6.3 存储优化
- 压缩算法: LZ4
- 副本数: 1
- 存储引擎: OLAP

## 7. 数据安全

### 7.1 访问控制
- 基于角色的访问控制
- 数据脱敏处理
- 审计日志记录

### 7.2 数据保护
- 敏感信息加密
- 数据备份策略
- 灾难恢复方案

## 8. 监控与维护

### 8.1 数据监控
- 数据量监控
- 数据质量监控
- 性能监控

### 8.2 维护策略
- 定期数据清理
- 索引重建
- 统计信息更新

## 9. 扩展规划

### 9.1 短期规划
- 增加更多数据源
- 优化查询性能
- 完善标签体系

### 9.2 长期规划
- 支持实时数据处理
- 集成机器学习模型
- 扩展多行业支持 