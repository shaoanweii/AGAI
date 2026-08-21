-- ========================================
-- VOC_MS_TD 数据库表结构DDL文档
-- 数据库: voc_ms_td
-- 生成时间: 2025
-- 说明: 本文档包含所有表的字段定义和说明
-- ========================================

-- ========================================
-- 一、DWD层 - 数据明细层
-- ========================================

-- 1. dwd_voc_all_meta_data (VOC元数据明细表)
-- 说明: 存储VOC系统所有原始元数据
-- 主键: id, create_time
-- 索引: BLOOM_FILTER on create_time, data_create_time, data_id
/*
字段列表:
- id (varchar(40), NOT NULL): 主键ID
- create_time (datetime, NOT NULL): 创建时间 [BLOOM_FILTER]
- content_type (varchar(40)): 内容类型
- data_create_time (datetime): 数据创建时间 [BLOOM_FILTER]
- data_update_time (datetime): 数据更新时间
- data_id (varchar(40)): 数据ID [BLOOM_FILTER]
- channel_code (varchar(40)): 渠道编码
- brand (text): 品牌
- series (text): 车系
- model (text): 车型
- is_outer (varchar(4)): 是否外部
- one_id (text): 统一客户ID
- id_car_no (text): 身份证/车牌号
- mobile (text): 手机号
- email (text): 邮箱
- global_id (text): 全局ID
- user_id (text): 用户ID
- user_name (text): 用户名称
- vhl_id (text): 车辆ID
- vhl_vin (text): 车辆VIN码
- dlr_id (text): 经销商ID
- dlr_code (text): 经销商编码
- dlr_type (text): 经销商类型
- market_id (text): 市场ID
- title (text): 标题
- content (text): 内容
- is_wsater_army (varchar(4)): 水军标识
- weight (int): 权重
- attrs (json): 扩展属性1
- attrs2 (json): 扩展属性2
- attrs3 (json): 扩展属性3
- work_id (varchar(40)): 工单ID
- done (int): 完成标识
- model_type (int): 模型类型
- ds (varchar(20)): 数据分区标识
- data_status (int): 数据状态
*/


-- ========================================
-- 二、ADS层 - 应用数据层
-- ========================================

-- 2.1 核心事实表

-- 2.1.1 ads_voc_model_tags_result_data_m_inc (模型标签结果增量表)
-- 说明: 存储VOC模型打标后的结果数据
-- 主键: id, publish_time, data_id
/*
字段列表:
- id (varchar(50), NOT NULL): 主键ID
- publish_time (datetime, NOT NULL): 发布时间
- data_id (varchar(50), NOT NULL): 数据ID
- channel_id (varchar(50), NOT NULL): 渠道ID
- topic (varchar(100)): 观点主题
- intention_type (varchar(50)): 意图类型
- sentiment (varchar(50)): 情感倾向
- one_id (text): 统一客户ID
- work_id (text): 工单ID
- scenario (text): 场景
- client_id (text): 客户端ID
- content_type (text): 内容类型
- sample_data_type (text): 样本数据类型
- original_id (text): 原始ID
- input_data_id (text): 输入数据ID
- original_text_scene (text): 原始文本场景
- brand_code (text): 品牌编码
- car_series_code (text): 车系编码
- label_type (text): 标签类型
- opinion (text): 原始观点
- subject (text): 主题
- fault_level (text): 故障等级
- description (text): 描述
- sentiment_score (text): 情感分数
- keywords (text): 关键词
- model_type (int): 模型类型
- raw_data (text): 原始数据
- ext_fields (text): 扩展字段
- biz_ext_attrs (text): 业务扩展属性
- biz_ext_attrs2 (text): 业务扩展属性2
- biz_ext_attrs3 (text): 业务扩展属性3
- cust_ext_attrs (text): 客户扩展属性
- vhl_ext_attrs (text): 车辆扩展属性
- dealer_ext_attrs (text): 经销商扩展属性
- prd_ext_attrs (text): 产品扩展属性
- tags_ext_attrs (text): 标签扩展属性
- create_time (datetime, NOT NULL): 创建时间
- update_time (datetime, NOT NULL): 更新时间
- abandon (text): 废弃标识
- done (int): 完成标识
*/


-- 2.1.2 ads_voc_all_mate_data_m_full (VOC元数据月度全量表)
-- 说明: 月度全量元数据表
-- 主键: id, create_time
-- 字段结构同 dwd_voc_all_meta_data，data_status类型为varchar(100)


-- 2.2 物化视图

-- 2.2.1 ads_voc_model_tags_result_data_mv (模型标签结果物化视图)
-- 说明: 整合模型标签、品牌、车系、渠道、标签系统的宽表物化视图
-- 刷新策略: 每1小时自动刷新
-- 数据源: ads_voc_model_tags_result_data_m_inc + 多表LEFT JOIN


-- 2.2.2 ads_voc_channel_m_full_mv (渠道月度全量物化视图)
-- 说明: 渠道维度物化视图
-- 数据源: ins_channel


-- 2.2.3 ads_voc_tags_system_info_h_full_mv (标签系统信息小时全量物化视图)
-- 说明: 标签体系完整信息物化视图


-- 2.2.4 ads_voc_tags_h_full_mv (标签小时全量物化视图)
-- 说明: 标签数据小时级汇总


-- 2.2.5 ads_voc_tags_system_h_full_mv (系统标签小时全量物化视图)
-- 说明: 系统级标签小时汇总


-- ========================================
-- 三、维度表
-- ========================================

-- 3.1 品牌维度

-- 3.1.1 ins_brand_info (品牌信息表)
-- 说明: 品牌维度数据
-- 主键: id, create_time
/*
字段列表:
- id (varchar(64), NOT NULL): 主键ID
- create_time (datetime, NOT NULL): 创建时间
- code (text): 品牌编码
- name (text): 品牌名称
- name_en (text): 品牌英文名称
- alias (text): 别名
- exclusion_words (text): 排除词
- order_by (int): 排序
- operator (text): 操作人
- update_time (datetime): 更新时间
- del_flag (int): 删除标识
- img (text): 图片
- app_id (text): 应用ID
- competitive_type (int): 竞品类型
- country (text): 国家
- nature (text): 性质
- insert_dt (datetime): 插入时间
- ds (text): 数据分区标识
- is_core (int): 核心品牌标识
- competitive_product (text): 竞品
- automark (text): 自动标记
*/


-- 3.2 车系维度

-- 3.2.1 ins_car_series_info (车系信息表)
-- 说明: 车系维度数据
-- 主键: id, create_time
/*
字段列表:
- id (varchar(40)): 主键ID
- create_time (datetime): 创建时间
- name (text): 车系名称
- name_en (text): 车系英文名称
- brand_id (text): 品牌ID
- brand_code (text): 品牌编码
- alias (text): 别名
- exclusion_words (text): 排除词
- code (text): 车系编码
- order_by (bigint): 排序
- car_name (text): 车型名称
- car_code (text): 车型编码
- factory (text): 工厂
- car_level1 (text): 车型级别1
- car_level2 (text): 车型级别2
- energy_type1 (text): 能源类型1
- energy_type2 (text): 能源类型2
- level_name (text): 级别名称
- competitive_type (bigint): 竞品类型
- competitive_product (text): 竞品
- start_time (datetime): 开始时间
- end_time (datetime): 结束时间
- is_core (bigint): 核心车系标识
- operator (text): 操作人
- update_time (datetime): 更新时间
- del_flag (bigint): 删除标识
- img (text): 图片
- app_id (text): 应用ID
- country (text): 国家
- insert_dt (datetime): 插入时间
- ds (text): 数据分区标识
*/


-- 3.3 渠道维度

-- 3.3.1 ins_channel (渠道信息表)
-- 说明: 渠道维度数据，支持多级渠道
-- 主键: id, create_time
/*
字段列表:
- id (varchar(40)): 主键ID
- create_time (datetime): 创建时间
- parent_id (text): 父级ID
- name (text): 渠道名称
- type (text): 渠道类型
- status (text): 状态
- name_en (text): 渠道英文名称
- update_time (datetime): 更新时间
- code (text): 渠道编码
- description (text): 描述
- is_core_channel (text): 核心渠道标识
- data_source_type (text): 数据源类型
- level (int): 层级
- top_id (text): 顶级ID
- insert_dt (datetime): 插入时间
- ds (text): 数据分区标识
*/


-- 3.4 标签维度

-- 3.4.1 ins_tag_client (标签客户表)
-- 说明: 标签体系维度数据
-- 主键: id, create_time
/*
字段列表:
- id (varchar(40)): 主键ID
- create_time (datetime): 创建时间
- tag_parent_id (text): 标签父级ID
- tag_name (text): 标签名称
- tag_name_en (text): 标签英文名称
- tag_code (text): 标签编码
- tag_type (text): 标签类型
- tag_attribute (text): 标签属性
- energy_type (text): 能源类型
- car_type (text): 车型
- tag_status (text): 标签状态
- tag_description (text): 标签描述
- seriousness (text): 严重程度
- user_journey1 (text): 用户旅程1
- user_journey2 (text): 用户旅程2
- user_journey3 (text): 用户旅程3
- scenario_attr (text): 场景属性
- event_clarity (text): 事件清晰度
- d2c_responsible_dept (text): D2C责任部门
- d2c_cc_dept (text): D2C抄送部门
- d2c_accountable_dept (text): D2C问责部门
- update_time (datetime): 更新时间
- create_user (text): 创建用户
- update_user (text): 更新用户
- app_client (text): 应用客户端
- sort (int): 排序
- level (int): 层级
- emotion (text): 情感
- intention (text): 意图
- tag_accuracy (text): 标签准确性
- tag_customer_issue_classification (text): 客户问题分类
- tag_issue_severity (text): 问题严重程度
- tag_code_status (text): 标签编码状态
- tag_business_domain (text): 业务领域
- tag_high_value_flag (text): 高价值标识
- tag_complaint_flag_needing_reply (text): 需回复投诉标识
- tag_high_quality_voc_flag (text): 高质量VOC标识
- tag_new_energy_or_fuel (text): 新能源或燃油
- tag_need_forvclosed_loop (text): 需闭环标识
- insert_dt (datetime): 插入时间
- ds (text): 数据分区标识
*/


-- ========================================
-- 四、报表业务表
-- ========================================

-- 4.1 用户行为表

-- 4.1.1 report_user_browse_record (用户浏览记录表)
-- 说明: 记录用户浏览行为
-- 主键: id
/*
字段列表:
- id (varchar(64), NOT NULL): 主键ID
- sound_id (varchar(64)): 声音ID
- original_id (varchar(64), NOT NULL): 原始ID
- browse_user_id (varchar(64), NOT NULL): 浏览用户ID
- create_time (datetime): 创建时间，默认CURRENT_TIMESTAMP
- browse_duration (int): 浏览时长
- sound_intention (varchar(64)): 声音意图
*/


-- 4.2 数据质量表

-- 4.2.1 report_high_quality_record (高质量记录表)
-- 说明: 标记高质量VOC数据
-- 主键: id
/*
字段列表:
- id (varchar(64), NOT NULL): 主键ID
- sound_id (varchar(64)): 声音ID
- data_create_time (date, NOT NULL): 数据创建时间
- status (tinyint): 状态，默认1
- create_time (datetime): 创建时间，默认CURRENT_TIMESTAMP
- update_time (datetime): 更新时间，默认CURRENT_TIMESTAMP
- original_id (varchar(50000)): 原始ID
*/


-- 4.2.2 report_label_correction_info (标签纠正信息表)
-- 说明: 记录标签纠正信息
-- 主键: id
/*
字段列表:
- id (varchar(50), NOT NULL): 主键ID
- data_id (varchar(50), NOT NULL): 数据ID
- correction_record_id (varchar(50), NOT NULL): 纠正记录ID
- channel_id (varchar(50), NOT NULL): 渠道ID
- topic (varchar(100)): 观点主题
- intention_type (varchar(50)): 意图类型
- sentiment (varchar(50)): 情感倾向
- publish_time (datetime, NOT NULL): 发布时间
- one_id (text): 统一客户ID
- work_id (text): 工单ID
- scenario (text): 场景
- client_id (text): 客户端ID
- content_type (text): 内容类型
- sample_data_type (text): 样本数据类型
- original_id (text): 原始ID
- input_data_id (text): 输入数据ID
- original_text_scene (text): 原始文本场景
- brand_code (text): 品牌编码
- car_series_code (text): 车系编码
- label_type (text): 标签类型
- opinion (text): 原始观点
- subject (text): 主题
- fault_level (text): 故障等级
- description (text): 描述
- sentiment_score (text): 情感分数
- keywords (text): 关键词
- model_type (int): 模型类型
- raw_data (text): 原始数据
- ext_fields (text): 扩展字段
- biz_ext_attrs (text): 业务扩展属性
- biz_ext_attrs2 (text): 业务扩展属性2
- biz_ext_attrs3 (text): 业务扩展属性3
- cust_ext_attrs (text): 客户扩展属性
- vhl_ext_attrs (text): 车辆扩展属性
- dealer_ext_attrs (text): 经销商扩展属性
- prd_ext_attrs (text): 产品扩展属性
- tags_ext_attrs (text): 标签扩展属性
- create_time (datetime, NOT NULL): 创建时间
- update_time (datetime, NOT NULL): 更新时间
- abandon (text): 废弃标识
- done (int): 完成标识
*/


-- 4.3 报表配置表

-- 4.3.1 report_custom_report (自定义报表表)
-- 说明: 管理自定义报表配置
-- 主键: id
/*
字段列表:
- id (varchar(64), NOT NULL): 主键ID
- report_name (varchar(250), NOT NULL): 报表名称
- view_count (bigint): 浏览次数，默认0
- collection_count (bigint): 收藏次数，默认0
- type (tinyint, NOT NULL): 报表类型
- default_condition (varchar(65533)): 默认条件
- brand_code (varchar(100)): 品牌编码
- special_type_id (varchar(64)): 特殊类型ID
- status (tinyint): 状态，默认0
- create_time (datetime): 创建时间，默认CURRENT_TIMESTAMP
- update_time (datetime): 更新时间，默认CURRENT_TIMESTAMP
- create_by (varchar(100)): 创建人
- update_by (varchar(100)): 更新人
- del_flag (tinyint): 删除标识，默认0
- description (varchar(255)): 描述
- first_level_zone_id (varchar(100)): 一级区域ID
- date_condition (varchar(65533)): 日期条件
- report_url (varchar(100)): 报表URL
*/


-- 4.3.2 report_display_rule (报表展示规则表)
-- 说明: 配置报表展示规则
-- 主键: id
/*
字段列表:
- id (varchar(64), NOT NULL): 主键ID
- metric_code (varchar(64), NOT NULL): 指标编码
- metric_name (varchar(128), NOT NULL): 指标名称
- range_min (decimal(10,2), NOT NULL): 范围最小值
- range_max (decimal(10,2), NOT NULL): 范围最大值
- color_hex (varchar(30), NOT NULL): 颜色十六进制
- background_color_hex (varchar(30)): 背景颜色十六进制
- emoji_key (varchar(64), NOT NULL): 表情符号键
- sort_no (int): 排序号，默认0
- status (tinyint): 状态，默认1
- create_time (datetime): 创建时间，默认CURRENT_TIMESTAMP
- update_time (datetime): 更新时间，默认CURRENT_TIMESTAMP
*/


-- ========================================
-- 五、应用层视图
-- ========================================

-- 5.1 voc_sentiment_annotations_results_v (VOC声音数据视图)
-- 说明: 为应用层提供声音数据的统一查询接口
CREATE VIEW voc_sentiment_annotations_results_v AS
SELECT * FROM ads_voc_model_tags_result_data_mv;


-- 5.2 voc_anal_flow_mate_data_full_v (VOC元数据全量增量视图)
-- 说明: 提供元数据的查询接口
CREATE VIEW voc_anal_flow_mate_data_full_v AS
SELECT 
    id, create_time, content_type, data_create_time, data_update_time,
    data_id, channel_code, brand, series, model, is_outer, one_id,
    id_car_no, mobile, email, global_id, user_id, user_name,
    vhl_id, vhl_vin, dlr_id, dlr_code, dlr_type, market_id,
    title, content, is_wsater_army, weight, attrs, attrs2, attrs3,
    work_id, done, model_type, ds, data_status
FROM ads_voc_all_mate_data_m_full;


-- ========================================
-- 六、辅助维度表
-- ========================================

-- 6.1 品牌车系视图
-- brand_all_v (所有品牌视图)
-- brand_self_brand_v (自主品牌视图)
-- brand_series_all_v (所有品牌车系视图)

-- 6.2 其他维度
-- province_dict_data_v (省份字典数据视图)
-- voc_ins_user_journey_level1_v (用户旅程一级视图)
-- dwd_voc_ext_attrs_mapping_values (扩展属性映射值表)


-- ========================================
-- 七、索引说明
-- ========================================

/*
布隆过滤器索引 (BLOOM_FILTER):
1. dwd_voc_all_meta_data:
   - create_time
   - data_create_time
   - data_id

2. ads_voc_all_mate_data_m_full:
   - create_time
   - data_create_time
   - data_id

说明: 布隆过滤器索引用于加速高频查询字段的过滤性能
*/


-- ========================================
-- 八、分区说明
-- ========================================

/*
分区策略:
1. 按时间分区:
   - publish_time: 发布时间分区
   - create_time: 创建时间分区
   - data_create_time: 数据创建时间分区

2. 分区管理:
   - 建议按月或按天分区
   - 定期归档历史分区数据
   - 保留最近N个月的热数据
*/


-- ========================================
-- 九、数据类型说明
-- ========================================

/*
1. varchar: 变长字符串，适用于固定长度范围的字段
2. text: 文本类型，适用于长文本字段
3. datetime: 日期时间类型
4. date: 日期类型
5. int/bigint: 整数类型
6. tinyint: 小整数类型，通常用于状态标识
7. decimal: 精确数值类型，用于金额或精确计算
8. json: JSON类型，用于存储结构化扩展属性
*/


-- ========================================
-- 十、命名规范
-- ========================================

/*
1. 表命名规范:
   - dwd_: 数据明细层
   - ads_: 应用数据层
   - dws_: 数据服务层
   - ins_: 洞察引擎维度表
   - report_: 报表业务表
   - _mv: 物化视图后缀
   - _v: 视图后缀

2. 字段命名规范:
   - _id: ID类字段
   - _code: 编码类字段
   - _name: 名称类字段
   - _time: 时间类字段
   - _type: 类型类字段
   - _flag: 标识类字段
   - _attrs: 扩展属性类字段
   - is_: 布尔类字段前缀
*/


-- ========================================
-- 文档结束
-- ========================================
