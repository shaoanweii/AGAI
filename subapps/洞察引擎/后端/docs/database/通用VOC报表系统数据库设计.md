# 通用VOC报表系统数据库设计文档

## 1. 概述

### 1.1 数据库简介
通用VOC（Voice of Customer）报表系统数据库是为支持多行业客户之声分析而设计的数据存储方案。该数据库基于原有的汽车行业专用VOC系统数据库结构（VDP_RS_TD），经过重构和扩展，能够支持汽车、星巴克、信访、手机、美妆等多个行业的客户反馈数据分析。

### 1.2 设计目标
- 支持多行业客户数据的统一存储和分析
- 提供高效的数据查询和报表生成能力
- 实现数据的实时处理和分析
- 支持大规模数据存储和高并发访问
- 保证数据的一致性和完整性
- 通过配置实现不同行业的快速适配

### 1.3 命名规范
- 维度表：以 `dim_` 前缀开头
- 数据仓库明细层表：以 `dwd_` 前缀开头
- 数据仓库汇总层表：以 `dws_` 前缀开头
- 物化视图：以 `m_v` 后缀结尾
- 普通视图：以 `_v` 后缀结尾但不以 `m_v` 结尾

## 2. 数据库结构

### 2.1 数据库概览
通用VOC报表系统数据库主要包含以下几类对象：
1. 维度表（Dimension Tables）
2. 数据仓库明细层表（DWD - Data Warehouse Detail）
3. 数据仓库汇总层表（DWS - Data Warehouse Summary）
4. 物化视图（Materialized Views）
5. 普通视图（Views）

### 2.2 核心表结构

#### 2.2.1 维度表
维度表存储描述性信息，用于对事实数据进行分类和描述。

| 表名 | 中文名称 | 描述 |
|------|---------|------|
| dim_voc_customers | 客户信息维度表 | 包含客户的详细资料信息，适用于所有行业 |
| dim_voc_users | 用户信息维度表 | 存储用户的基本标识信息 |
| dim_voc_dealers | 经销商/门店信息维度表 | 存储经销商或门店的详细信息 |
| dim_voc_products | 产品信息维度表 | 存储产品相关信息 |
| dim_voc_channels | 渠道信息维度表 | 存储反馈渠道信息 |
| dim_voc_industries | 行业信息维度表 | 存储支持的行业信息 |
| dim_voc_stores | 门店信息维度表 | 存储门店相关信息（零售业专用） |

#### 2.2.2 数据仓库明细层表（DWD）
DWD层表存储经过清洗、标准化后的明细数据，是数据仓库的基础。

| 表名 | 中文名称 | 描述 |
|------|---------|------|
| dwd_voc_raw_feedback | 原始客户反馈数据表 | 存储从各种渠道收集的原始客户反馈 |
| dwd_voc_model_tags_result | 模型标签结果数据表 | 存储通过AI模型分析后的标签数据 |
| dwd_voc_all_meta_data | 所有元数据表 | 存储所有元数据信息 |
| dwd_voc_model_tags_unlabeled | 模型标签未标注数据表 | 存储未标注的模型标签数据 |
| dwd_voc_post_rules_result | 帖子规则结果数据表 | 存储帖子规则分析结果 |
| dwd_voc_pre_rules_result | 预规则结果数据表 | 存储预规则分析结果 |
| dwd_voc_processing_record | 处理数据记录表 | 记录数据处理过程 |
| dwd_voc_raw_private_consult | 原始私人咨询表 | 存储私人咨询数据 |
| dwd_voc_raw_private_feedback | 原始私人反馈表 | 存储私人反馈数据 |
| dwd_voc_raw_private_posts_comment | 原始私人帖子评论表 | 存储私人帖子评论数据 |
| dwd_voc_raw_private_questionnaire | 原始私人问卷表 | 存储私人问卷数据 |
| dwd_voc_raw_private_work_order | 原始私人工单表 | 存储私人工单数据 |
| dwd_voc_raw_public_consult | 原始公共咨询表 | 存储公共咨询数据 |
| dwd_voc_raw_public_posts_comment | 原始公共帖子评论表 | 存储公共帖子评论数据 |
| dwd_voc_raw_public_questionnaire | 原始公共问卷表 | 存储公共问卷数据 |

#### 2.2.3 数据仓库汇总层表（DWS）
DWS层表存储经过聚合计算后的汇总数据，用于提高查询性能。

| 表名 | 中文名称 | 描述 |
|------|---------|------|
| dws_voc_batch_push_record | 批量推送记录表 | 记录批量数据推送信息 |
| dws_voc_error_push_data | 错误推送数据表 | 存储推送错误的数据 |
| dws_voc_feedback_summary | 客户反馈汇总表 | 存储客户反馈汇总数据 |

### 2.3 物化视图设计

#### 2.3.1 所有计算结果数据物化视图 (voc_computed_result_all_data_m_v)
该物化视图是系统中最核心的数据对象之一，整合了所有计算结果数据，为上层应用提供统一的数据视图。

**主要功能**：
- 整合所有维度和事实数据
- 提供高性能的数据查询能力
- 支持多维度分析和报表生成
- 实现数据的预计算和缓存

**各行业通用字段**：

##### 基础信息字段
| 字段名 | 类型 | 描述 |
|--------|------|------|
| data_create_time | DATE | 数据创建时间（主键） |
| industry_code | VARCHAR(50) | 行业代码（主键） |
| id | VARCHAR(40) | 数据唯一标识 |
| data_id | VARCHAR(40) | 原始数据ID |
| create_date | DATE | 创建日期 |

##### 渠道信息字段
| 字段名 | 类型 | 描述 |
|--------|------|------|
| channel_catagory | VARCHAR(50) | 渠道分类 |
| channel_code | VARCHAR(40) | 渠道代码 |
| channel_name | VARCHAR(50) | 渠道名称 |

##### 品牌和产品信息字段
| 字段名 | 类型 | 描述 |
|--------|------|------|
| brand_code | VARCHAR(1000) | 品牌代码（主键） |
| brand_name | VARCHAR(100) | 品牌名称 |
| product_code | VARCHAR(1000) | 产品代码 |
| product_name | VARCHAR(100) | 产品名称 |

##### 内容分析字段
| 字段名 | 类型 | 描述 |
|--------|------|------|
| content_type | VARCHAR(10) | 内容类型 |
| sentiment | VARCHAR(100) | 情感分析结果 |
| sentiment_score | DECIMAL(5,4) | 情感分析置信度分数 |
| sentiment_positive_score | DECIMAL(5,4) | 正面情感分数 |
| sentiment_negative_score | DECIMAL(5,4) | 负面情感分数 |
| sentiment_neutral_score | DECIMAL(5,4) | 中性情感分数 |
| intention | VARCHAR(100) | 意图识别结果 |
| intention_score | DECIMAL(5,4) | 意图识别置信度分数 |
| topic | VARCHAR(65533) | 主题分类 |
| topic_score | DECIMAL(5,4) | 主题分类置信度分数 |
| topic_text | VARCHAR(1048576) | 主题文本 |
| opinion | VARCHAR(65533) | 观点 |
| keywords | VARCHAR(65533) | 关键词 |
| hot_word | VARCHAR(1048576) | 热词 |
| scenario | VARCHAR(1048576) | 场景 |
| urgency_level | VARCHAR(50) | 紧急程度 |
| urgency_score | DECIMAL(5,4) | 紧急程度置信度分数 |
| emotion_type | VARCHAR(100) | 情绪类型（愤怒、失望、满意等） |
| emotion_score | DECIMAL(5,4) | 情绪识别置信度分数 |
| satisfaction_score | DECIMAL(5,4) | 满意度评分 |
| nps_score | INTEGER | NPS净推荐值 |
| aspect_term | VARCHAR(1048576) | 属性词 |
| opinion_term | VARCHAR(1048576) | 观点词 |
| aspect_category | VARCHAR(1048576) | 属性分类 |
| quality_issue_flag | BOOLEAN | 质量问题标记 |
| service_issue_flag | BOOLEAN | 服务问题标记 |
| price_issue_flag | BOOLEAN | 价格问题标记 |
| delivery_issue_flag | BOOLEAN | 交付问题标记 |
| promotion_issue_flag | BOOLEAN | 推广问题标记 |
| content_summary | VARCHAR(1048576) | 内容摘要 |
| content_length | INTEGER | 内容长度 |
| language_type | VARCHAR(50) | 语言类型 |
| translation_flag | BOOLEAN | 是否需要翻译 |
| spam_flag | BOOLEAN | 垃圾信息标记 |
| duplicate_flag | BOOLEAN | 重复内容标记 |
| ai_analysis_time | DATETIME | AI分析时间 |
| ai_model_version | VARCHAR(50) | AI模型版本 |

##### 客户信息字段
| 字段名 | 类型 | 描述 |
|--------|------|------|
| one_id | VARCHAR(64) | 统一客户ID |
| cust_age | VARCHAR(65533) | 客户年龄 |
| cust_gender | VARCHAR(65533) | 客户性别 |
| cust_province_code | VARCHAR(60) | 客户省份代码 |
| cust_province | VARCHAR(60) | 客户省份 |
| cust_city_code | VARCHAR(60) | 客户城市代码 |
| cust_city | VARCHAR(60) | 客户城市 |
| cust_name | VARCHAR(65533) | 客户姓名 |
| cust_nick | VARCHAR(1048576) | 客户昵称 |
| cust_mobile | VARCHAR(65533) | 客户手机号 |
| cust_email | VARCHAR(65533) | 客户邮箱 |
| cust_global_id | VARCHAR(65533) | 全局客户ID |
| cust_type | VARCHAR(65533) | 客户类型 |
| customer_journey_stage | VARCHAR(1048576) | 客户生命旅程阶段 |
| customer_segment | VARCHAR(1048576) | 客户细分 |
| customer_value_level | VARCHAR(1048576) | 客户价值等级 |
| is_vip | BOOLEAN | 是否VIP客户 |
| is_new_customer | BOOLEAN | 是否新客户 |
| first_purchase_date | DATE | 首次购买日期 |
| last_purchase_date | DATE | 最近购买日期 |
| purchase_frequency | INTEGER | 购买频率 |
| total_spent | DECIMAL(15,2) | 累计消费金额 |
| avg_order_value | DECIMAL(15,2) | 平均订单价值 |

##### 经销商/门店信息字段
| 字段名 | 类型 | 描述 |
|--------|------|------|
| dealer_id | VARCHAR(1048576) | 经销商/门店ID |
| dealer_code | VARCHAR(65533) | 经销商/门店代码 |
| dealer_name | VARCHAR(65533) | 经销商/门店名称 |
| dealer_shortname | VARCHAR(65533) | 经销商/门店简称 |
| dealer_province_code | VARCHAR(60) | 省份代码 |
| dealer_province | VARCHAR(1048576) | 省份名称 |
| dealer_city_code | VARCHAR(60) | 城市代码 |
| dealer_city | VARCHAR(1048576) | 城市名称 |
| dealer_status | VARCHAR(65533) | 经销商/门店状态 |
| dealer_type | VARCHAR(1048576) | 经销商/门店类型 |
| dealer_level | VARCHAR(1048576) | 经销商/门店等级 |

##### 社交媒体字段
| 字段名 | 类型 | 描述 |
|--------|------|------|
| is_wsater_army | VARCHAR(1048576) | 是否水军 |
| is_manager_focused | VARCHAR(1048576) | 是否经理关注 |
| is_big_v | VARCHAR(1048576) | 是否大V |
| author_id | VARCHAR(1048576) | 作者ID |
| author_nick | VARCHAR(1048576) | 作者昵称 |
| is_main_post | VARCHAR(1048576) | 是否主贴 |
| original_link | VARCHAR(1048576) | 原始链接 |
| view_count | VARCHAR(1048576) | 浏览数 |
| comment_count | VARCHAR(1048576) | 评论数 |
| like_count | VARCHAR(1048576) | 点赞数 |
| share_count | VARCHAR(1048576) | 分享数 |
| favorite_count | VARCHAR(1048576) | 收藏数 |
| social_platform | VARCHAR(1048576) | 社交平台 |

##### 工单信息字段
| 字段名 | 类型 | 描述 |
|--------|------|------|
| work_order_id | VARCHAR(1048576) | 工单ID |
| quest_type | VARCHAR(1048576) | 问题类型 |
| quest_answer_score | VARCHAR(1048576) | 问题回答评分 |
| quest_business_type | VARCHAR(1048576) | 业务类型 |
| quest_business_scenario | VARCHAR(1048576) | 业务场景 |
| work_order_status | VARCHAR(1048576) | 工单状态 |
| work_order_priority | VARCHAR(1048576) | 工单优先级 |
| work_order_create_time | DATETIME | 工单创建时间 |
| work_order_resolve_time | DATETIME | 工单解决时间 |
| work_order_duration | INTEGER | 工单处理时长（分钟） |

##### 标签信息字段（通用标签结构）
| 字段名 | 类型 | 描述 |
|--------|------|------|
| tag_first_code | VARCHAR(50) | 一级标签代码 |
| tag_first | VARCHAR(255) | 一级标签 |
| tag_second_code | VARCHAR(50) | 二级标签代码 |
| tag_second | VARCHAR(255) | 二级标签 |
| tag_three_code | VARCHAR(50) | 三级标签代码 |
| tag_three | VARCHAR(255) | 三级标签 |
| tag_four_code | VARCHAR(50) | 四级标签代码 |
| tag_four | VARCHAR(255) | 四级标签 |

##### 责任部门字段
| 字段名 | 类型 | 描述 |
|--------|------|------|
| responsible_dept | VARCHAR(1048576) | 责任部门 |
| accountable_dept | VARCHAR(1048576) | 问责部门 |
| cc_dept | VARCHAR(1048576) | 抄送部门 |

##### 标签属性字段
| 字段名 | 类型 | 描述 |
|--------|------|------|
| tag_accuracy | VARCHAR(1048576) | 标签准确性 |
| tag_customer_issue_classification | VARCHAR(1048576) | 客户问题分类 |
| tag_issue_severity | VARCHAR(1048576) | 问题严重程度 |
| tag_code_status | VARCHAR(1048576) | 编码状态 |
| tag_business_domain | VARCHAR(1048576) | 业务领域 |
| tag_event_clarity | VARCHAR(1048576) | 事件清晰度 |
| tag_high_value_flag | VARCHAR(1048576) | 高价值标记 |
| tag_complaint_flag_needing_reply | VARCHAR(1048576) | 需要回复的投诉标记 |
| tag_high_quality_voc_flag | VARCHAR(1048576) | 高质量VOC标记 |
| tag_need_forvclosed_loop | VARCHAR(1048576) | 需要闭环处理 |
| customer_journey_step1 | VARCHAR(1048576) | 客户旅程第一步 |
| customer_journey_step2 | VARCHAR(1048576) | 客户旅程第二步 |
| customer_journey_step3 | VARCHAR(1048576) | 客户旅程第三步 |
| customer_journey_step4 | VARCHAR(1048576) | 客户旅程第四步 |
| customer_journey_step5 | VARCHAR(1048576) | 客户旅程第五步 |

##### 时间维度字段
| 字段名 | 类型 | 描述 |
|--------|------|------|
| data_create_year | VARCHAR(1048576) | 数据创建年份 |
| data_create_quarter | VARCHAR(1048576) | 数据创建季度 |
| data_create_month | VARCHAR(1048576) | 数据创建月份 |
| data_create_week | VARCHAR(1048576) | 数据创建周 |

**设计特点**：
- 预计算并存储查询结果，提高查询性能
- 定期刷新以保证数据时效性
- 支持复杂的多表关联查询
- 为报表和分析提供统一数据源
- 通过行业代码字段支持多行业数据隔离和查询
- 专注于各行业通用字段，避免行业特定字段干扰

#### 2.3.2 其他物化视图
| 视图名 | 中文名称 | 描述 |
|-------|---------|------|
| voc_customer_info_m_v | 客户信息物化视图 | 客户信息的预计算视图 |
| voc_dealer_info_m_v | 经销商信息物化视图 | 经销商信息的预计算视图 |
| voc_raw_feedback_m_v | 原始反馈物化视图 | 原始反馈数据的预计算视图 |
| voc_product_info_m_v | 产品信息物化视图 | 产品信息的预计算视图 |

## 3. 行业适配设计

### 3.1 行业标识字段
为了支持多行业适配，所有核心表都包含行业标识字段：
- `industry_code`：行业代码（如 automotive、starbucks、petition、mobile、beauty）
- `industry_name`：行业名称

### 3.2 行业特定字段
根据不同行业的特点，表结构中包含行业特定字段，这些字段将在具体行业实施时添加：
- 汽车行业：车辆信息、经销商信息、售后服务等
- 星巴克：产品信息、门店信息、咖啡师信息等
- 信访：部门分类、问题类型、紧急程度等
- 手机：型号信息、功能特性、售后服务等
- 美妆：产品效果、使用体验、品牌形象等

### 3.3 配置驱动的表结构
通过配置管理，系统可以动态适配不同行业的数据结构：
- 使用JSON或MAP类型字段存储行业特定信息
- 通过视图层实现行业特定的数据展示
- 支持Schema Evolution，允许动态添加字段

## 4. 数据流设计

### 4.1 数据处理流程
1. **数据采集**：从各个数据源（API、文件、数据库等）采集原始数据
2. **数据清洗**：清洗和标准化数据，去除无效和重复数据
3. **行业识别**：根据数据特征识别所属行业
4. **数据存储**：将清洗后的数据存储到DWD层表中
5. **数据分析**：通过AI模型和规则引擎进行数据分析
6. **数据聚合**：将分析结果聚合到DWS层表中
7. **物化视图更新**：定期更新物化视图以提高查询性能

### 4.2 数据更新策略
- **实时更新**：对于需要实时展示的数据，采用流式处理方式实时更新
- **定时更新**：对于汇总数据和物化视图，采用定时任务定期更新
- **增量更新**：只更新发生变化的数据，提高更新效率

## 5. 性能优化

### 5.1 索引设计
- 为经常用于查询条件的字段创建索引
- 为外键字段创建索引以提高关联查询性能
- 为时间字段创建分区索引以提高时间范围查询性能
- 为行业标识字段创建索引以提高行业筛选性能

### 5.2 分区策略
- 按时间对大表进行分区，提高查询性能
- 根据行业对数据进行分区，提高行业特定查询性能
- 根据数据热度进行冷热数据分离

### 5.3 物化视图优化
- 对复杂查询结果创建物化视图
- 定期刷新物化视图以保证数据新鲜度
- 根据查询频率调整物化视图的刷新策略

## 6. 安全设计

### 6.1 数据访问控制
- 基于角色的访问控制（RBAC）
- 数据行级和列级访问控制
- 敏感数据脱敏处理
- 行业数据隔离，确保不同行业数据互不干扰

### 6.2 数据备份与恢复
- 定期进行数据备份
- 制定数据恢复策略
- 异地备份保证数据安全
- 支持按行业进行数据恢复

## 7. 维护管理

### 7.1 数据监控
- 监控数据库性能指标
- 监控数据质量和完整性
- 设置告警机制及时发现异常
- 监控各行业数据量和处理情况

### 7.2 定期维护
- 定期优化数据库性能
- 清理过期和无效数据
- 更新统计信息以优化查询计划
- 检查和更新物化视图

## 8. 扩展性设计

### 8.1 行业扩展
- 通过配置增加新行业支持
- 动态创建行业特定视图
- 支持行业特定字段扩展

### 8.2 数据结构扩展
- 支持Schema Evolution
- 通过JSON字段支持灵活数据结构
- 物化视图动态更新机制

## 9. 总结

通用VOC报表系统数据库是基于原有汽车行业专用系统重构的多行业支持数据库。通过合理的分层设计、物化视图机制和行业适配策略，为系统提供了高性能的数据存储和查询能力。该设计充分考虑了多行业数据的统一处理需求，专注于各行业的通用字段设计，同时为特定行业扩展预留了空间，支持系统的扩展性和维护性，能够满足汽车、星巴克、信访、手机、美妆等多个行业的客户之声分析需求。

新增的客户生命旅程相关字段能够更好地支持客户旅程分析，包括客户旅程阶段、客户细分、客户价值等级等，以及具体的旅程步骤信息。这些字段可以帮助企业更好地理解客户在不同阶段的需求和行为，从而制定更精准的营销和服务策略。
