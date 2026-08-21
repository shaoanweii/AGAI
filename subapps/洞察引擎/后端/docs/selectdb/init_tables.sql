-- max.request.size=10485880
-- drop table ads_voc_model_tags_result_data_m_inc
CREATE TABLE IF NOT EXISTS ads_voc_model_tags_result_data_m_inc (
            `id` varchar(40) NOT NULL COMMENT "主键id",
            `publish_time` DATETIME NOT NULL COMMENT "发布时间（用于按天分区）",
            `data_id` STRING NOT NULL COMMENT "业务主键id",
            `one_id` STRING COMMENT "唯一Id",
            `work_id` STRING   COMMENT "接收处理标识",
            `client_id` STRING   COMMENT "客户标识",
            `channel_id` STRING NOT NULL COMMENT "渠道标识",
            `content_type` STRING COMMENT "内容类型：文本：text、 工单：order",
            `sample_data_type` STRING COMMENT "是否是示例数据",
            `original_id` STRING COMMENT "原文id",
            `input_data_id` STRING COMMENT "原文关联id",
            `original_text_scene` STRING COMMENT "原文片段",
            `brand_code` STRING COMMENT "品牌名称",
            `car_series_code` STRING COMMENT "车系名称",
            `label_type` STRING COMMENT "标签类型：1服务 2产品 3品质",
            `scenario` STRING COMMENT "用车场景",
            `sentiment` STRING COMMENT "情感倾向",
            `intention_type` STRING COMMENT "用户意图",
            `topic` STRING COMMENT "聚合后的观点=>标签叶子结点",
            `opinion` STRING COMMENT "原始观点",
            `subject` STRING COMMENT "评价主体【如：雨刮器】",
            `fault_level` STRING COMMENT "故障问题严重性等级",
            `description` STRING COMMENT "描述/评价内容",
            `sentiment_score` STRING COMMENT "情感严重程度",
            `keywords` STRING COMMENT "提取的热词",
            `model_type` INT COMMENT "模型类型：1 智谱AI离线 2智谱AI实时 3聚类大模型",
            `raw_data` STRING COMMENT "原始数据（未处理前，）",
            `ext_fields` STRING COMMENT "通用扩展字段",
            `biz_ext_attrs` STRING COMMENT "业务扩展字段1",
            `biz_ext_attrs2` STRING COMMENT "业务扩展字段2",
            `biz_ext_attrs3` STRING COMMENT "业务扩展字段3",
            `cust_ext_attrs` STRING COMMENT "客户信息扩展字段",
            `vhl_ext_attrs` STRING COMMENT "车辆信息扩展字段",
            `dealer_ext_attrs` STRING COMMENT "经销商信息扩展字段",
            `prd_ext_attrs` STRING COMMENT "产品经销商信息扩展字段",
            `tags_ext_attrs` STRING COMMENT "标签产品经销商信息扩展字段",
            `create_time` DATETIME NOT NULL COMMENT "记录创建时间",
            `update_time` DATETIME NOT NULL COMMENT "记录更新时间",
            `abandon` STRING COMMENT "是否完成计算：是=1，否=0",
            `done` INT COMMENT "是否完成计算（整型）：是=1，否=0"
)
UNIQUE   KEY(id,publish_time)
COMMENT 'VOC-ODS数据模型处理结果表'
PARTITION BY RANGE (publish_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 12
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "publish_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "true",
    "enable_unique_key_merge_on_write" = "true"
);
ALTER TABLE ads_voc_model_tags_result_data_m_inc
SET (
    "bloom_filter_columns" = "brand_code, car_series_code, channel_id, topic"
);




-- voc_ms_td.temp_2 definition
-- voc_ms_td.temp_2 definition



-- CREATE MATERIALIZED VIEW voc_imp_customer_info_mv
-- BUILD DEFERRED REFRESH AUTO ON MANUAL
-- PARTITION BY (create_time)
-- DISTRIBUTED BY HASH (`one_id`) BUCKETS 32
-- PROPERTIES (
--    "replication_num" = "2",
--    "bloom_filter_columns" = "one_id,mobile,id_card_no"
-- )
-- AS
-- SELECT one_id, mobile, id_card_no, cust_ext_attrs, vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs, create_time
-- FROM  voc_imp_customer_info;



-- drop table voc_imp_customer_info
CREATE TABLE `voc_imp_customer_info` (
                                         `one_id` varchar(60) NULL COMMENT "数据唯一标识",
                                         `create_time` datetime NOT NULL COMMENT "数据抓取时间",
                                         `mobile` string NULL COMMENT "数据唯一标识",
                                         `id_card_no` string NULL COMMENT "数据唯一标识",
                                         `cust_ext_attrs` text NULL COMMENT "客户信息扩展字段",
                                         `vhl_ext_attrs` text NULL COMMENT "车辆信息扩展字段",
                                         `dealer_ext_attrs` text NULL COMMENT "经销商信息扩展字段",
                                         `prd_ext_attrs` text NULL COMMENT "产品经销商信息扩展字段"
) ENGINE=OLAP
    UNIQUE KEY(`one_id`,`create_time`)
COMMENT 'VOC-客户维数据'
PARTITION BY RANGE (create_time)()
DISTRIBUTED BY HASH(`one_id`) BUCKETS 16
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"compression" = "ZLIB",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);



-- ------------------------------
-- Table: ads_voc_all_mate_data_m_full
-- Description: VOC-ODS 原始数据记录表（优化版）
-- Author: Your Team
-- Date: 2025-10-26
-- ------------------------------
CREATE TABLE `ads_voc_all_mate_data_m_full` (
                                                         `id` varchar(40) NOT NULL COMMENT '主键ID',
                                                         `create_time` datetime NOT NULL COMMENT '数据抓取时间',
                                                         `content_type` varchar(40) NULL COMMENT '内容类型：order(工单), post(帖子评论), opinion(意见反馈), questionnaire(问卷), consult(咨询)',
                                                         `data_create_time` datetime NULL COMMENT '数据产生时间',
                                                         `data_update_time` datetime NULL COMMENT '数据更新时间',
                                                         `data_id` varchar(40) NULL COMMENT '数据唯一标识',
                                                         `channel_code` varchar(40) NULL COMMENT '渠道编码' ,
                                                         `brand` text NULL COMMENT '品牌',
                                                         `series` text NULL COMMENT '车系',
                                                         `model` text NULL COMMENT '原始观点',
                                                         `is_outer` varchar(4) NULL COMMENT '是否往外数据',
                                                         `one_id` text NULL COMMENT '股份客户信息-one_id',
                                                         `id_car_no` text NULL COMMENT '客户证件号',
                                                         `mobile` text NULL COMMENT '客户手机号',
                                                         `email` text NULL COMMENT '客户邮箱',
                                                         `global_id` text NULL COMMENT 'SSO全局ID',
                                                         `user_id` text NULL COMMENT '用户标识',
                                                         `user_name` text NULL COMMENT '用户名',
                                                         `vhl_id` text NULL COMMENT '车辆ID',
                                                         `vhl_vin` text NULL COMMENT '车辆车架号',
                                                         `dlr_id` text NULL COMMENT '股份售后经销商ID',
                                                         `dlr_code` text NULL COMMENT '股份售后经销商编码',
                                                         `dlr_type` text NULL COMMENT '股份售后经销商类型',
                                                         `market_id` text NULL COMMENT '股份产品物理编码',
                                                         `title` text NULL COMMENT '标题',
                                                         `content` text NULL COMMENT '内容正文' ,
                                                         `is_wsater_army` varchar(4) NULL COMMENT '是否水军',
                                                         `weight` int NULL COMMENT '权重值',
                                                         `attrs` json NULL COMMENT '业务系统其他字段',
                                                         `attrs2` json NULL COMMENT '业务系统其他字段',
                                                         `attrs3` json NULL COMMENT '业务系统其他字段',
                                                         `work_id` varchar(40) NULL COMMENT '最终标识' ,
                                                         `done` int NULL COMMENT '是否完成计算：1-是，0-否',
                                                         `model_type` int NULL COMMENT '模型类型',
                                                         `ds` varchar(20) NULL COMMENT '分区字段（备用）'
)
    ENGINE=OLAP
    UNIQUE KEY(`id`,`create_time`)
COMMENT 'VOC-ODS原始数据记录表（优化版）'
--  时间分区：按天动态分区
PARTITION BY RANGE(`create_time`) ()
-- 分桶：32 buckets（适合千万~亿级数据）
DISTRIBUTED BY HASH(`id`) BUCKETS 16
-- 属性配置
PROPERTIES (
    -- 索引配置
    "bloom_filter_columns" = "data_id, content_type, work_id, channel_code",
    "replication_allocation" = "tag.location.default: 2",
    "min_load_replica_num" = "-1",
    "is_being_synced" = "false",

    -- 动态分区：按天，保留24天历史，预创建1天
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH",
    "dynamic_partition.time_zone" = "Asia/Shanghai",
    "dynamic_partition.start" = "-24",
    "dynamic_partition.end" = "1",
    "dynamic_partition.prefix" = "p",
    "dynamic_partition.replication_allocation" = "tag.location.default: 2",
    "dynamic_partition.buckets" = "32",
    "dynamic_partition.create_history_partition" = "true",
    "dynamic_partition.history_partition_num" = "-1",
    "dynamic_partition.hot_partition_num" = "0",
    "dynamic_partition.reserved_history_periods" = "NULL",
    "dynamic_partition.storage_policy" = "",
    "dynamic_partition.start_day_of_month" = "1",

    -- 存储配置
    "storage_medium" = "hdd",
    "storage_format" = "V2",
    "inverted_index_storage_format" = "V1",
    "compression" = "ZLIB",

    -- Merge on Write 模式（UNIQUE KEY 表）
    "enable_unique_key_merge_on_write" = "true",

    -- 轻量级 Schema 变更
    "light_schema_change" = "true",

    -- 写入与 Compaction 配置
    "disable_auto_compaction" = "false",
    "enable_single_replica_compaction" = "false",
    "group_commit_interval_ms" = "10000",
    "group_commit_data_bytes" = "134217728",
    "enable_mow_light_delete" = "false"
)

CREATE TABLE IF NOT EXISTS ads_voc_model_tags_result_data_m_inc_his (
                                                                        `data_id` varchar(50) NOT NULL COMMENT "业务主键id",
                                                                        `channel_id` varchar(50) NOT NULL COMMENT "渠道标识",
                                                                        `topic` varchar(100) COMMENT "聚合后的观点=>标签叶子结点",
                                                                        `intention_type` varchar(50) COMMENT "用户意图",
                                                                        `sentiment` varchar(50) COMMENT "情感倾向",
                                                                        `publish_time` DATETIME NOT NULL COMMENT "发布时间（用于按天分区）",
                                                                        `id` varchar(50) NOT NULL COMMENT "主键id",
                                                                        `one_id` STRING COMMENT "唯一Id",
                                                                        `work_id` STRING   COMMENT "接收处理标识",
                                                                        `scenario` STRING COMMENT "用车场景",
                                                                        `client_id` STRING   COMMENT "客户标识",
                                                                        `content_type` STRING COMMENT "内容类型：文本：text、 工单：order",
                                                                        `sample_data_type` STRING COMMENT "是否是示例数据",
                                                                        `original_id` STRING COMMENT "原文id",
                                                                        `input_data_id` STRING COMMENT "原文关联id",
                                                                        `original_text_scene` STRING COMMENT "原文片段",
                                                                        `brand_code` STRING COMMENT "品牌名称",
                                                                        `car_series_code` STRING COMMENT "车系名称",
                                                                        `label_type` STRING COMMENT "标签类型：1服务 2产品 3品质",
                                                                        `opinion` STRING COMMENT "原始观点",
                                                                        `subject` STRING COMMENT "评价主体【如：雨刮器】",
                                                                        `fault_level` STRING COMMENT "故障问题严重性等级",
                                                                        `description` STRING COMMENT "描述/评价内容",
                                                                        `sentiment_score` STRING COMMENT "情感严重程度",
                                                                        `keywords` STRING COMMENT "提取的热词",
                                                                        `model_type` INT COMMENT "模型类型：1 智谱AI离线 2智谱AI实时 3聚类大模型",
                                                                        `raw_data` STRING COMMENT "原始数据（未处理前，）",
                                                                        `ext_fields` STRING COMMENT "通用扩展字段",
                                                                        `biz_ext_attrs` STRING COMMENT "业务扩展字段1",
                                                                        `biz_ext_attrs2` STRING COMMENT "业务扩展字段2",
                                                                        `biz_ext_attrs3` STRING COMMENT "业务扩展字段3",
                                                                        `cust_ext_attrs` STRING COMMENT "客户信息扩展字段",
                                                                        `vhl_ext_attrs` STRING COMMENT "车辆信息扩展字段",
                                                                        `dealer_ext_attrs` STRING COMMENT "经销商信息扩展字段",
                                                                        `prd_ext_attrs` STRING COMMENT "产品经销商信息扩展字段",
                                                                        `tags_ext_attrs` STRING COMMENT "标签产品经销商信息扩展字段",
                                                                        `create_time` DATETIME NOT NULL COMMENT "记录创建时间",
                                                                        `update_time` DATETIME NOT NULL COMMENT "记录更新时间",
                                                                        `abandon` STRING COMMENT "是否完成计算：是=1，否=0",
                                                                        `done` INT COMMENT "是否完成计算（整型）：是=1，否=0"
)
UNIQUE KEY(data_id, channel_id, topic, intention_type, sentiment, publish_time)
COMMENT 'VOC-ODS数据模型处理结果表'
PARTITION BY RANGE (publish_time)()
DISTRIBUTED BY HASH (`data_id`) BUCKETS 4
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "publish_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "true",
    "enable_unique_key_merge_on_write" = "true"
);


-- 请确认表名、字段类型、精度等符合您需求后再点击“新建”按钮。
-- 表名默认来源表的表名，表comment默认来源表的comment，请您按需修改。
-- 建表语句参考来源表的字段类型、长度进行了初步的转换，请您按需修改。
create table `ins_brand_info` (
                                  `id` varchar(64) not null comment 'id',
                                  `create_time` DATETIME not null ,
                                  `code` string,
                                  `name` string,
                                  `name_en` string,
                                  `alias` string,
                                  `exclusion_words` string comment 'AI',
                                  `order_by` int,
                                  `operator` string,
                                  `update_time` DATETIME,
                                  `del_flag` int comment ' 0 1',
                                  `img` string,
                                  `app_id` string,
                                  `competitive_type` int comment ' 123',
                                  `country` string,
                                  `nature` string comment '()',
                                  `insert_dt` DATETIME,
                                  `ds` string
) ENGINE=OLAP
    UNIQUE   KEY(id,create_time)
COMMENT 'VOC-ODS数据模型处理结果表'
PARTITION BY RANGE (create_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 2
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "create_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "true",
    "enable_unique_key_merge_on_write" = "true"
)

-- drop table ads_voc_data_flow_status
create table `ads_voc_data_flow_status` (
                                            `id` varchar(40) NOT NULL COMMENT "VOC中数据ID",
                                            `insert_dt` DATETIME not null default CURRENT_TIMESTAMP COMMENT "数据写入时间",
                                            `data_id` varchar(40) NOT NULL COMMENT "源数据ID",
                                            `mate_data_c_time` DATETIME NOT NULL  COMMENT "原始数据写入时间",
                                            `pre_rule_data_c_time` DATETIME NOT NULL  COMMENT "前置规则处理完成时间",
                                            `model_input_c_time` DATETIME NOT NULL  COMMENT "推送模型时间",
                                            `model_result_c_time` DATETIME NOT NULL  COMMENT "模型返回结果时间",
                                            `push_ads_c_time` DATETIME NOT NULL  COMMENT "推送APP端时间",
                                            `status` string COMMENT "数据状态"
)
UNIQUE   KEY(id,insert_dt)
COMMENT 'VOC-数据流转各核心节点数据状态记录表'
PARTITION BY RANGE (insert_dt)()
DISTRIBUTED BY HASH (`id`) BUCKETS 4
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "insert_dt",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "false",
    "enable_unique_key_merge_on_write" = "true"
);


-- 请确认表名、字段类型、精度等符合您需求后再点击“新建”按钮。
-- 表名默认来源表的表名，表comment默认来源表的comment，请您按需修改。
-- 建表语句参考来源表的字段类型、长度进行了初步的转换，请您按需修改。

create table `ins_car_series_info` (
                                       `id` varchar(40) comment '主键id',
                                       `create_time` DATETIME comment '创建时间',
                                       `name` string comment '名称',
                                       `name_en` string comment '英文名称',
                                       `brand_id` string comment '汽车品牌id',
                                       `brand_code` string comment '品牌编码',
                                       `alias` string comment '别名，多个别称以逗号隔开',
                                       `exclusion_words` string comment '排除词；多个别称以逗号隔开，用于AI调用',
                                       `code` string comment '编码',
                                       `order_by` bigint comment '排序',
                                       `car_name` string comment '车型名称',
                                       `car_code` string comment '车型编码',
                                       `factory` string comment '工厂 本品车系会分析工厂字段',
                                       `car_level1` string comment '车辆级别1',
                                       `car_level2` string comment '车辆级别2',
                                       `energy_type1` string comment '能用类型1',
                                       `energy_type2` string comment '能用类型2',
                                       `level_name` string comment '级别名称 级别2+级别1拼接',
                                       `competitive_type` bigint comment '本竞品类型 1本品，2竞品，3非关注范围',
                                       `competitive_product` string comment '关联本竞品 选择本品时绑定竞品车系，选择竞品时绑定本品车系（多对多）',
                                       `start_time` DATETIME comment '新车开始时间',
                                       `end_time` DATETIME comment '新车结束时间',
                                       `is_core` bigint comment '是否核心 0 非核心 1 核心 默认为0',
                                       `operator` string comment '创建人',
                                       `update_time` DATETIME comment '更新时间',
                                       `del_flag` bigint comment '删除状态 0正常 1已删除',
                                       `img` string,
                                       `app_id` string comment '系统标识',
                                       `country` string comment '国家',
                                       `insert_dt` DATETIME,
                                       `ds` string
) ENGINE=OLAP
    UNIQUE   KEY(id,create_time)
COMMENT 'VOC-ODS数据模型处理结果表'
PARTITION BY RANGE (create_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 2
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "create_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "true",
    "enable_unique_key_merge_on_write" = "true"
)



-- 请确认表名、字段类型、精度等符合您需求后再点击“新建”按钮。
-- 表名默认来源表的表名，表comment默认来源表的comment，请您按需修改。
-- 建表语句参考来源表的字段类型、长度进行了初步的转换，请您按需修改。

create table `ins_tag_client` (
                                  `id` varchar(40) comment '主键',
                                  `create_time` DATETIME comment '创建时间',
                                  `tag_parent_id` string comment '父级id',
                                  `tag_name` string comment '标签名称',
                                  `tag_name_en` string comment '标签英文名称',
                                  `tag_code` string comment '标签编码',
                                  `tag_type` string comment '标签类型',
                                  `tag_attribute` string comment '标签属性',
                                  `energy_type` string comment '关联能源',
                                  `car_type` string comment '车辆类型',
                                  `tag_status` string comment '状态(禁用:0,启用:1)',
                                  `tag_description` string comment '描述',
                                  `seriousness` string comment '严重性',
                                  `user_journey1` string comment '用户旅途1(看车、购车等)',
                                  `user_journey2` string comment '用户旅途1(高速路、高原等)',
                                  `scenario_attr` string comment '场景属性(舒适性/材质/异响)',
                                  `event_clarity` string comment '事件清晰度(印象、事实)',
                                  `d2c_responsible_dept` string comment '主责部门',
                                  `d2c_cc_dept` string comment '抄送部门',
                                  `update_time` DATETIME comment '更新时间',
                                  `create_user` string comment '创建人',
                                  `update_user` string comment '更新人',
                                  `app_client` string comment '应用客户',
                                  `sort` int,
                                  `level` int comment '层级',
                                  `emotion` string comment '情感',
                                  `intention` string comment '意图',
                                  `d2c_accountable_dept` string comment '责任部门',
                                  `tag_accuracy` string comment '代码的精准性(精准、有待提升等)',
                                  `tag_customer_issue_classification` string comment '客户问题分级(S、A、B、C等)',
                                  `tag_issue_severity` string comment '问题程度(高、中、低)',
                                  `tag_code_status` string comment '代码状态(有效、无效等)',
                                  `tag_business_domain` string comment '业务领域(产品质量、产品设计、服务体验)',
                                  `tag_high_value_flag` string comment '需推送的高价值建议标识',
                                  `tag_complaint_flag_needing_reply` string comment '需回复的抱怨标识',
                                  `tag_high_quality_voc_flag` string comment '针对五级明细高质量VOC标识',
                                  `tag_new_energy_or_fuel` string comment '新能源特有/燃油特有',
                                  `tag_need_forvclosed_loop` string comment '是否需要闭环的(短平快、通用等)',
                                  `user_journey3` string,
                                  `insert_dt` DATETIME,
                                  `ds` string
) ENGINE=OLAP
    UNIQUE   KEY(id,create_time)
COMMENT 'VOC-ODS数据模型处理结果表'
PARTITION BY RANGE (create_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 2
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "create_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "true",
    "enable_unique_key_merge_on_write" = "true"
)

-- 请确认表名、字段类型、精度等符合您需求后再点击“新建”按钮。
-- 表名默认来源表的表名，表comment默认来源表的comment，请您按需修改。
-- 建表语句参考来源表的字段类型、长度进行了初步的转换，请您按需修改。

create table `ins_channel` (
                               `id` varchar(40) ,
                               `create_time` DATETIME,
                               `parent_id` string comment 'id',
                               `name` string,
                               `type` string comment ' :Category :Channel',
                               `status` string,
                               `name_en` string,
                               `update_time` DATETIME,
                               `code` string comment 'code',
                               `description` string,
                               `is_core_channel` string,
                               `data_source_type` string,
                               `level` int,
                               `top_id` string comment 'id',
                               `insert_dt` DATETIME,
                               `ds` string
) ENGINE=OLAP
    UNIQUE   KEY(id,create_time)
COMMENT 'VOC-ODS数据模型处理结果表'
PARTITION BY RANGE (create_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 2
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "create_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "true",
    "enable_unique_key_merge_on_write" = "true"
)

-- drop MATERIALIZED VIEW ads_voc_model_tags_result_data_mv
CREATE MATERIALIZED VIEW ads_voc_model_tags_result_data_mv
REFRESH AUTO ON SCHEDULE EVERY 1 hour
PARTITION BY (publish_time)
DISTRIBUTED BY HASH (`id`) BUCKETS 4
-- 5. 物化视图查询逻辑（保留原需求，新增按月分区字段）
AS
SELECT
    *,
    to_date(`publish_time`) AS biz_date
FROM ads_voc_model_tags_result_data_m_inc
;




create table `ads_voc_all_mate_data_m_inc` (
               id varchar(40) not null COMMENT 'ID',
               create_time DATETIME not null  COMMENT '数据抓取时间',
               content_type STRING COMMENT '类型：order:工单，post:帖子评论， opinion:意见反馈,questionnaire:问卷，consult：咨询',
               data_create_time DATETIME COMMENT '数据产生时间',
               data_update_time DATETIME COMMENT '数据更新时间',
               data_id STRING COMMENT '数据唯一标识',
               channel_code STRING COMMENT '渠道编码',
               brand STRING COMMENT '品牌',
               series STRING COMMENT '车系',
               model STRING COMMENT '车型',
               is_outer STRING COMMENT '是否往外数据',
               one_id STRING COMMENT '股份客户信息-one_id',
               id_car_no STRING COMMENT '客户证件好',
               mobile STRING COMMENT '客户手机号',
               email STRING COMMENT '客户邮箱',
               global_id STRING COMMENT 'SSO全局ID',
               user_id STRING COMMENT '用户标识',
               user_name STRING COMMENT '用户名',
               vhl_id STRING COMMENT '车辆ID',
               vhl_vin STRING COMMENT '车辆车架号',
               dlr_id STRING COMMENT '股份售后经销商ID',
               dlr_code STRING COMMENT '股份售后经销商编码',
               dlr_type STRING COMMENT '股份售后经销商类型',
               market_id STRING COMMENT '股份产品物理编码',
               title STRING COMMENT '标题',
               content STRING COMMENT '内容',
               is_wsater_army STRING COMMENT '是否水军',
               weight INT COMMENT '权重值',
               attrs STRING COMMENT '业务系统其他字段',
               attrs2 STRING COMMENT '业务系统其他字段',
               attrs3 STRING COMMENT '业务系统其他字段',
               work_id STRING COMMENT '最终标识',
               done INT COMMENT '是否完成计算 是：1，否：0',
               model_type INT COMMENT '模型类型',
               ds STRING
) ENGINE=OLAP
    UNIQUE KEY(id,create_time)
COMMENT 'VOC-ODS原始数据记录表'
PARTITION BY RANGE (create_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 4
PROPERTIES (
    "replication_num" = "2",
	"storage_format" = "default",
	"compression" = "ZLIB",

	-- 动态分区配置（按月分区核心调整）
	"dynamic_partition.enable" = "true",
	"dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
	"dynamic_partition.column" = "create_time",
	"dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
	-- "dynamic_partition.buckets" = "4",
	"dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
	"dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
	"dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
	"dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
	"dynamic_partition.create_history_partition" = "true",
	"enable_unique_key_merge_on_write" = "true"
);









-- drop table ads_voc_flow_pre_data_part_m_inc
create table `ads_voc_flow_pre_result_data_part_m_inc` (
                                                    `id` varchar(50) NOT NULL COMMENT "VOC中数据ID",
                                                    `create_time` DATETIME not null default CURRENT_TIMESTAMP COMMENT "数据写入时间",
                                                    `data_id` varchar(50) NOT NULL COMMENT "源数据ID",
                                                    `channel_code` varchar(50) NOT NULL  COMMENT "原始数据写入时间",
                                                    `content_type`  varchar(50) NOT NULL  COMMENT "前置规则处理完成时间",
                                                    `data_create_time` DATETIME NOT NULL  COMMENT "推送模型时间",
                                                    `is_outer` varchar(2) NOT NULL  COMMENT "模型返回结果时间",
                                                    `ext_fields` String NOT NULL  COMMENT "模型返回结果时间",
                                                    `ds` varchar(20) NOT NULL  COMMENT "模型返回结果时间"
)
    UNIQUE   KEY(id,create_time)
COMMENT 'VOC-数据流转各核心节点数据状态记录表'
PARTITION BY RANGE (create_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 2
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "create_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "false",
    "enable_unique_key_merge_on_write" = "true"
);





-- drop table ads_voc_flow_pre_data_part_m_inc
create table `ads_voc_flow_mate_data_part_m_inc` (
                                                    `id` varchar(50) NOT NULL COMMENT "VOC中数据ID",
                                                    `create_time` DATETIME not null default CURRENT_TIMESTAMP COMMENT "数据写入时间",
                                                    `data_id` varchar(50) NOT NULL COMMENT "源数据ID",
                                                    `channel_code` varchar(50) NOT NULL  COMMENT "原始数据写入时间",
                                                    `content_type`  varchar(50) NOT NULL  COMMENT "前置规则处理完成时间",
                                                    `data_create_time` DATETIME NOT NULL  COMMENT "推送模型时间",
                                                    `is_outer` varchar(2) NOT NULL  COMMENT "模型返回结果时间",
                                                    `ds` varchar(20) NOT NULL  COMMENT "模型返回结果时间"
)
    UNIQUE   KEY(id,create_time)
COMMENT 'VOC-数据流转各核心节点数据状态记录表'
PARTITION BY RANGE (create_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 2
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "create_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "false",
    "enable_unique_key_merge_on_write" = "true"
);





-- drop table `ads_voc_model_input_data_m_inc`
create table `ads_voc_flow_model_input_data_m_inc` (
                                                       `id` varchar(50) NOT NULL COMMENT "VOC中数据ID",
                                                       `create_time` DATETIME not null default CURRENT_TIMESTAMP COMMENT "数据写入时间",
                                                       `data_id` varchar(50) NOT NULL COMMENT "源数据ID",
                                                       `channel_code` varchar(50) NOT NULL  COMMENT "原始数据写入时间",
                                                       `content_type`  varchar(50) NOT NULL  COMMENT "前置规则处理完成时间",
                                                       `data_create_time` DATETIME NOT NULL  COMMENT "推送模型时间",
                                                       `is_outer` varchar(2) NOT NULL  COMMENT "模型返回结果时间",
                                                       `ds` varchar(20) NOT NULL  COMMENT "模型返回结果时间"
)
    UNIQUE   KEY(id,create_time)
COMMENT 'VOC-数据流转各核心节点数据状态记录表'
PARTITION BY RANGE (create_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 2
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "create_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "false",
    "enable_unique_key_merge_on_write" = "true"
);


-- drop table ads_voc_flow_model_tags_result_part_m_inc
create table `ads_voc_flow_model_tags_result_part_m_inc` (
                                                             `id` varchar(50) NOT NULL COMMENT "VOC中数据ID",
                                                             `create_time` DATETIME not null default CURRENT_TIMESTAMP COMMENT "数据写入时间",
                                                             `data_id` varchar(50) COMMENT "源数据ID",
                                                             `data_create_time` DATETIME COMMENT "推送模型时间",
                                                             `channel_code` varchar(50)  COMMENT "原始数据写入时间",
                                                             `content_type`  varchar(50) COMMENT "前置规则处理完成时间",
                                                             `is_outer` varchar(10)  COMMENT "模型返回结果时间",
                                                             `ds` varchar(20)  COMMENT "模型返回结果时间"
)
UNIQUE   KEY(id ,create_time)
COMMENT 'VOC-数据流转各核心节点数据状态记录表'
PARTITION BY RANGE (create_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 2
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "create_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "false",
    "enable_unique_key_merge_on_write" = "true"
);




-- drop table ads_voc_model_tags_result_part_m_inc
create table `ads_voc_model_input_data_m_inc` (
                                                        `id` varchar(50) NOT NULL COMMENT "VOC中数据ID",
                                                        `create_time` DATETIME not null default CURRENT_TIMESTAMP COMMENT "数据写入时间",
                                                        `data_id` varchar(50) NOT NULL COMMENT "源数据ID",
                                                        `channel_code` varchar(50) NOT NULL  COMMENT "原始数据写入时间",
                                                        `content_type`  varchar(50) NOT NULL  COMMENT "前置规则处理完成时间",
                                                        `data_create_time` DATETIME NOT NULL  COMMENT "推送模型时间",
                                                        `is_outer` varchar(2) NOT NULL  COMMENT "模型返回结果时间",
                                                        `ds` varchar(20) NOT NULL  COMMENT "模型返回结果时间"
)
    UNIQUE   KEY(id,create_time)
COMMENT 'VOC-数据流转各核心节点数据状态记录表'
PARTITION BY RANGE (create_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 2
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "create_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "false",
    "enable_unique_key_merge_on_write" = "true"
);





CREATE TABLE IF NOT EXISTS voc_ms_td.stg_voc_pre_rules_result_data_m_inc (
                                                                             `id` VARCHAR(64) NOT NULL COMMENT '主键（Hive原string类型，指定长度64适配常见主键场景）',
                                                                             `publish_time` DATETIME NOT NULL COMMENT '发布时间（Hive原TIMESTAMP类型）',
                                                                             `data_id` VARCHAR(64) NOT NULL COMMENT '主键（Hive原string类型，指定长度64）',
                                                                             `one_id` VARCHAR(64) COMMENT '唯一Id（Hive原string类型）',
                                                                             `work_id` VARCHAR(64) NOT NULL COMMENT '接收处理标识（Hive原string类型）',
                                                                             `client_id` VARCHAR(64) NOT NULL COMMENT '客户标识（Hive原string类型）',
                                                                             `channel_id` VARCHAR(64) NOT NULL COMMENT '渠道标识（Hive原string类型）',
                                                                             `content_type` VARCHAR(32) COMMENT '内容类型：文本：text、 工单：order（Hive原string类型）',
                                                                             `data` TEXT COMMENT '数据内容（Hive原string类型，用TEXT适配长文本）',
                                                                             `data_md5` VARCHAR(64) COMMENT '内容md5值（固定32位，预留64位兼容扩展）',
                                                                             `model_type` VARCHAR(16) COMMENT '1 智谱AI离线 2智谱AI实时 3聚类大模型（Hive原string类型）',
                                                                             `ext_fields` TEXT COMMENT '扩展字段（Hive原string类型，用TEXT适配JSON等长文本）',
                                                                             `biz_ext_attrs` TEXT COMMENT '扩展字段（Hive原string类型）',
                                                                             `biz_ext_attrs2` TEXT COMMENT '扩展字段（Hive原string类型）',
                                                                             `biz_ext_attrs3` TEXT COMMENT '扩展字段（Hive原string类型）',
                                                                             `create_time` DATETIME COMMENT '接收时间（Hive原TIMESTAMP类型）',
                                                                             `update_time` DATETIME COMMENT '接收时间（Hive原TIMESTAMP类型）',
                                                                             `abandon` VARCHAR(2) COMMENT '是否遗弃数据 是：1，否：0（Hive原string类型）',
                                                                             `done` VARCHAR(2) COMMENT '是否完成计算 是：1，否：0（Hive原string类型）',
                                                                             `hit_rules` TEXT COMMENT '规则id集合（Hive原string类型，指定长度255）'
)
    UNIQUE   KEY(id,publish_time)
COMMENT 'VOC-ODS数据模型处理结果表'
PARTITION BY RANGE (publish_time)()
DISTRIBUTED BY HASH (`id`) BUCKETS 4
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "default",
    "compression" = "ZLIB",

    -- 动态分区配置（按月分区核心调整）
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH", -- 时间粒度改为月
    "dynamic_partition.column" = "publish_time",
    "dynamic_partition.prefix" = "p", -- 分区名前缀（如p202509）
    -- "dynamic_partition.buckets" = "4",
    "dynamic_partition.history_partitions" = "24", -- 历史保留66个月（约5.5年）
    "dynamic_partition.future_partitions" = "1", -- 提前创建1个未来月份分区
    "dynamic_partition.start" = "-24", -- 起始偏移量（与历史保留月数一致）
    "dynamic_partition.end" = "1", -- 结束偏移量（与未来创建月数一致）
    "dynamic_partition.create_history_partition" = "true",
    "enable_unique_key_merge_on_write" = "true"
);




CREATE TABLE `voc_imp_hudi_dm_voc_cust3` (
                                             `oneid` varchar(50) NOT NULL COMMENT "OneID",
                                             `dw_insert_time` datetime NOT NULL  COMMENT "数仓插入时间",
                                             `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             `cust_classify` varchar(20) NULL COMMENT "客户类型",
                                             `id_card_type` varchar(30) NULL COMMENT "证件类型",
                                             `id_card_no` varchar(64) NULL COMMENT "证件号码(加密)",
                                             `global_id`  varchar(35) NULL COMMENT "sso全局ID",
                                             `email` varchar(40) NULL COMMENT "邮箱",
                                             `mobile` varchar(110) NULL COMMENT "手机号",
                                             `cust_nm` varchar(200) NULL COMMENT "客户姓名(加密)",
                                             `gender` varchar(25) NULL COMMENT "性别",
                                             `age` varchar(5) NULL COMMENT "年龄",
                                             `age_group` varchar(15) NULL COMMENT "年龄段",
                                             `birthday_dt` varchar(45) NULL COMMENT "出生日期",
                                             `birthday` varchar(25) NULL COMMENT "生日",
                                             `born_years` varchar(10) NULL COMMENT "出生年代",
                                             `life_stage` varchar(10) NULL COMMENT "人生阶段",
                                             `constellation` varchar(10) NULL COMMENT "星座",
                                             `zodiac` varchar(10) NULL COMMENT "生肖",
                                             `high_educaion` varchar(20) NULL COMMENT "最高学历",
                                             `marriage_statue` varchar(10) NULL COMMENT "婚姻状况",
                                             `hukou_prov_cd` varchar(10) NULL COMMENT "户籍地_省份_编码",
                                             `hukou_prov_nm` varchar(50) NULL COMMENT "户籍地_省份",
                                             `hukou_city_cd` varchar(10) NULL COMMENT "户籍地_城市_编码",
                                             `hukou_city_nm` varchar(50) NULL COMMENT "户籍地_城市",
                                             `hukou_cty_cd` varchar(10) NULL COMMENT "户籍地_区县_编码",
                                             `hukou_cty_nm` varchar(50) NULL COMMENT "户籍地_区县",
                                             `lived_prov_cd` varchar(10) NULL COMMENT "居住地_省份_编码",
                                             `lived_prov_nm` varchar(50) NULL COMMENT "居住地_省份",
                                             `lived_city_cd` varchar(10) NULL COMMENT "居住地_城市_编码",
                                             `lived_city_nm` varchar(50) NULL COMMENT "居住地_城市",
                                             `lived_cty_cd` varchar(10) NULL COMMENT "居住地_区县_编码",
                                             `lived_cty_nm` varchar(50) NULL COMMENT "居住地_区县",
                                             `lived_addr` varchar(500)  NULL COMMENT "居住地址",
                                             `profession` varchar(45) NULL COMMENT "职业",
                                             `family_income` varchar(45) NULL COMMENT "家庭月收入",
                                             `cust_type` varchar(100) NULL COMMENT "客户分类",
                                             `is_exchange_flg` varchar(1) NULL COMMENT "是否换购",
                                             `is_re_purchase_flg` varchar(1) NULL COMMENT "是否增换购",
                                             `is_recommend_flg` varchar(1) NULL COMMENT "是否推荐购",
                                             `is_car_owner_flg` varchar(1) NULL COMMENT "是否车主",
                                             `is_deal_flg` varchar(1) NULL COMMENT "是否成交",
                                             `is_uni_owner_flg` varchar(1) NULL COMMENT "是否UNI车主",
                                             `is_jc_owner_flg` varchar(1) NULL COMMENT "是否乘用车车主",
                                             `is_wc_owner_flg` varchar(1) NULL COMMENT "是否欧尚车主",
                                             `is_ev_owner_flg` varchar(1) NULL COMMENT "是否新能源车主",
                                             `is_qxc_owner_flg` varchar(1) NULL COMMENT "是否凯程车主",
                                             `purchase_car_qty` varchar(5) NULL COMMENT "购车数量",
                                             `purchase_car_times` varchar(5) NULL COMMENT "购车次数",
                                             `lately_purchase_time` varchar(25) NULL COMMENT "最近购车时间",
                                             `his_consume_amt` varchar(20) NULL COMMENT "历史消费金额",
                                             `is_member_flg` varchar(1) NULL COMMENT "是否会员",
                                             `member_register_mth` varchar(10) NULL COMMENT "会员注册时间",
                                             `mem_activity` varchar(20) NULL COMMENT "会员活跃度",
                                             `is_birthday_1day_flg` varchar(1) NULL COMMENT "明日是否生日",
                                             `is_birthday_30day_flg` varchar(1) NULL COMMENT "30日内是否生日",
                                             `is_birthday_60day_flg` varchar(1) NULL COMMENT "60日内是否生日",
                                             `dw_update_time` varchar(20) NULL COMMENT "数仓更新时间",
                                             `ds` varchar(10) NULL COMMENT "分区字段，格式：yyyyMMdd"
) ENGINE=OLAP
UNIQUE KEY(`oneid`,`dw_insert_time`)
COMMENT 'dm_voc_cust'
AUTO PARTITION BY RANGE (date_trunc(dw_insert_time, 'day'))()
DISTRIBUTED BY HASH(`oneid`) BUCKETS 16
PROPERTIES (
    "replication_allocation" = "tag.location.default: 3",
    "min_load_replica_num" = "-1",
    "bloom_filter_columns" = "id_card_no, mobile,insert_dt",
    "enable_unique_key_merge_on_write" = "false",
    "is_being_synced" = "false",
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "DAY",
    "dynamic_partition.time_zone" = "Asia/Shanghai",
    "dynamic_partition.start" = "-7",
    "dynamic_partition.end" = "1",
    "dynamic_partition.prefix" = "p"
);


CREATE TABLE `voc_imp_hudi_dwd_maf_veh_d_full` (
                                                   `vin` varchar(50) NOT NULL COMMENT "车架号",
                                                   `period_date` date NOT NULL COMMENT "日期维id YYYY-MM-DD",
                                                   `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                   `period_time_sec` datetime NULL COMMENT "时间去重精确到秒",
                                                   `period_wid` varchar(10) NULL COMMENT "日期维id YYYYMMDD",
                                                   `prod_code` varchar(50) NULL COMMENT "产品编码（三段式）",
                                                   `prod_name` varchar(50) NULL COMMENT "产品名称",
                                                   `mdl_code` varchar(20) NULL COMMENT "车型代码",
                                                   `mdl_name` varchar(200) NULL COMMENT "车型名称",
                                                   `series_code` varchar(20) NULL COMMENT "车系代码",
                                                   `series_name` varchar(30) NULL COMMENT "车系名称",
                                                   `opt_code` varchar(30) NULL COMMENT "配置代码",
                                                   `opt_name` varchar(100) NULL COMMENT "配置名称",
                                                   `col_code` varchar(150) NULL COMMENT "颜色代码",
                                                   `col_name` varchar(150) NULL COMMENT "颜色名称",
                                                   `eng_clsf` varchar(20) NULL COMMENT "动力系列大类",
                                                   `eng_seris` varchar(30) NULL COMMENT "动力系列小类",
                                                   `eng_mdl` varchar(30) NULL COMMENT "发动机型号",
                                                   `dis_mt` varchar(20) NULL COMMENT "排量",
                                                   `dis_ch` varchar(20) NULL COMMENT "排放",
                                                   `trans_clsf` varchar(20) NULL COMMENT "变速器类型",
                                                   `trans_form` varchar(30) NULL COMMENT "变速器型式",
                                                   `custom_code` varchar(20) NULL COMMENT "定制编码",
                                                   `veh_type` varchar(20) NULL COMMENT "车辆类型（出口车、领用车、加工车、商用车）",
                                                   `vcl_num` varchar(1) NULL COMMENT "产量",
                                                   `sbu_code` varchar(10) NULL COMMENT "经营单位编码",
                                                   `sbu_name` varchar(30) NULL COMMENT "经营单位名称",
                                                   `continent` varchar(30) NULL COMMENT "大洲",
                                                   `home_abroad` varchar(5) NULL COMMENT "国内国外",
                                                   `cntry_code3` varchar(30) NULL COMMENT "国家地区三位字母码",
                                                   `cntry_name` varchar(50) NULL COMMENT "国家地区中文名称",
                                                   `cntry_eng` varchar(5) NULL COMMENT "国家地区英文名称",
                                                   `plnt_code` varchar(50) NULL COMMENT "标准工厂编码",
                                                   `plnt_name` varchar(50) NULL COMMENT "标准工厂名称",
                                                   `product_date` varchar(30) NULL COMMENT "生产日期",
                                                   `offline_date` varchar(30) NULL COMMENT "总装下线时间",
                                                   `rtn_veh_date` varchar(30) NULL COMMENT "退车时间",
                                                   `src_sys` varchar(30) NULL COMMENT "来源系统名称",
                                                   `src_sys_id` varchar(20) NULL COMMENT "来源系统id",
                                                   `ds` varchar(10) NOT NULL COMMENT "分区字段，格式：yyyyMMdd"
) ENGINE=OLAP
UNIQUE KEY(`vin`,`period_date`)
COMMENT '车辆事实表'
AUTO PARTITION BY RANGE (date_trunc(`period_date`, 'year'))()
DISTRIBUTED BY HASH(`vin`) BUCKETS 8
PROPERTIES (
           "replication_allocation" = "tag.location.default: 3",
           "min_load_replica_num" = "-1",
           "bloom_filter_columns" = "period_date",
    	   "enable_unique_key_merge_on_write" = "false",
           "is_being_synced" = "false",
           "dynamic_partition.enable" = "true",
           "dynamic_partition.time_unit" = "year",
           "dynamic_partition.time_zone" = "Asia/Shanghai",
           "dynamic_partition.start" = "-50",
           "dynamic_partition.end" = "1",
           "dynamic_partition.prefix" = "p"
);








--  drop table voc_imp_hudi_dm_voc_cust_vehicle_rel
CREATE TABLE IF NOT EXISTS `voc_imp_hudi_dm_voc_cust_vehicle_rel` (
                                                                      `vin` VARCHAR(200) NOT NULL COMMENT '车架号',
    `w_insert_dt` datetime NOT NULL  COMMENT "数仓插入时间",
    `insert_dt` datetime not null default CURRENT_TIMESTAMP,
    `idcard` varchar(200) COMMENT '证件号',
    `w_update_dt` varchar(30) COMMENT '数据更新时间',
    `batch_dt` varchar(30) COMMENT '任务名称'
    )
    UNIQUE KEY(`vin`,`w_insert_dt`)
    COMMENT 'dm_voc_cust_vehicle_rel'
    AUTO PARTITION BY RANGE (date_trunc(w_insert_dt, 'day'))()
    DISTRIBUTED BY HASH(`vin`) BUCKETS 4
    PROPERTIES (
                   "replication_allocation" = "tag.location.default: 3",
                   "min_load_replica_num" = "-1",
                   "bloom_filter_columns" = "idcard, vin,insert_dt",
                   "enable_unique_key_merge_on_write" = "false",
                   "is_being_synced" = "false",
                   "dynamic_partition.enable" = "true",
                   "dynamic_partition.time_unit" = "DAY",
                   "dynamic_partition.time_zone" = "Asia/Shanghai",
                   "dynamic_partition.start" = "-7",
                   "dynamic_partition.end" = "1",
                   "dynamic_partition.prefix" = "p"
               );



-- drop table dim_chn_dlr_zip_d_full
CREATE TABLE IF NOT EXISTS `voc_imp_hudi_dim_chn_dlr_zip_d_full` (
    -- 业务主键 & 维度字段
                                                                     `sk_id` VARCHAR(100) NOT NULL COMMENT '经销商代理键id',
                                                                     `dlr_cd` STRING COMMENT '经销商代码',
                                                                     `erp_cd` STRING COMMENT 'ERP编码',
                                                                     `dlr_nm` STRING COMMENT '经销商名称',
                                                                     `dlr_s_nm` STRING COMMENT '经销商名称简称',
                                                                     `dept_cd` STRING COMMENT '部门编码',
                                                                     `dept_nm` STRING COMMENT '部门名称',
                                                                     `seq` STRING COMMENT '序列',
                                                                     `store_lvl` STRING COMMENT '经销商店级别(1:一级店,2:二级店)',
                                                                     `invest_group_cd` STRING COMMENT '同投商代码（隶属关系）投资商集团/凯程投资主体(l1_dealer)',
                                                                     `invest_group_nm` STRING COMMENT '同投商名称（隶属关系）投资商集团/凯程投资主体(l1_dealer)',
                                                                     `invest_cd` STRING COMMENT '投资商代码/凯程虚拟一级经销商(l2_dealer)',
                                                                     `invest_nm` STRING COMMENT '投资商名称/凯程虚拟一级经销商(l2_dealer)',
                                                                     `s1_dlr_cd` STRING COMMENT '一级渠道代码/凯程一级店(中心店)(l3_dealer)',
                                                                     `s1_dlr_nm` STRING COMMENT '一级渠道名称/凯程一级店(中心店)(l3_dealer)',
                                                                     `s2_dlr_cd` STRING COMMENT '二级渠道代码/凯程二级店or触点(l4_dealer)',
                                                                     `s2_dlr_nm` STRING COMMENT '二级渠道名称/凯程二级店or触点(l4_dealer)',
                                                                     `main_invest_cd` STRING COMMENT '同投商代码（占股关系）投资商主体/凯程虚拟一级商',
                                                                     `main_invest_nm` STRING COMMENT '同投商名称（占股关系）投资商主体/凯程虚拟一级商',
                                                                     `main_dlr_cd` STRING COMMENT '同投店代码（占股关系）主要在品牌管理该关系',
                                                                     `main_dlr_nm` STRING COMMENT '同投店名称（占股关系）主要在品牌管理该关系',
                                                                     `ord_flg` STRING COMMENT '是否订单门店',
                                                                     `dlv_flg` STRING COMMENT '是否交付门店',
                                                                     `svs_flg` STRING COMMENT '是否服务门店',
                                                                     `paint_flg` STRING COMMENT '是否有钣喷能力',
                                                                     `valid_flg` STRING COMMENT '是否正常经营',
                                                                     `quit_flg` STRING COMMENT '是否退网',
                                                                     `rez_flg` STRING COMMENT '是否接受服务线索预约',
                                                                     `dlr_type_group_cd` STRING COMMENT '经销商类型分组代码',
                                                                     `dlr_type_group_nm` STRING COMMENT '经销商类型分组名称',
                                                                     `dlr_type_cd` STRING COMMENT '经销商类型代码',
                                                                     `dlr_type_nm` STRING COMMENT '经销商类型名称',
                                                                     `chn_type_cd` STRING COMMENT '渠道类型代码',
                                                                     `chn_type_nm` STRING COMMENT '渠道类型名称',
                                                                     `chn_lvl_cd` STRING COMMENT '渠道等级代码',
                                                                     `chn_lvl_nm` STRING COMMENT '渠道等级名称',
                                                                     `image_cd` STRING COMMENT '门店形象代码',
                                                                     `image_nm` STRING COMMENT '门店形象名称',
                                                                     `image_lvl_cd` STRING COMMENT '门店形象级别代码',
                                                                     `image_lvl_nm` STRING COMMENT '门店形象级别名称',
                                                                     `store_front_function` STRING COMMENT '门店能力',
                                                                     `current_state` STRING COMMENT '当前状态',
                                                                     `operation_type` STRING COMMENT '运营类型',
                                                                     `area` STRING COMMENT '大区',
                                                                     `war_zone_cd` STRING COMMENT '战区代码',
                                                                     `war_zone_nm` STRING COMMENT '战区名称',
                                                                     `war_zone_part` STRING COMMENT '战区分区',
                                                                     `war_zone_part_user_cd` STRING COMMENT '战区分区人员编码',
                                                                     `war_zone_part_user_nm` STRING COMMENT '战区分区人员名称',
                                                                     `sdu` STRING COMMENT 'sdu名称',
                                                                     `sdu_link_man` STRING COMMENT 'sdu联系人',
                                                                     `prov_cd` STRING COMMENT '省份代码',
                                                                     `city_cd` STRING COMMENT '城市代码',
                                                                     `cty_cd` STRING COMMENT '区县代码',
                                                                     `prov_nm` STRING COMMENT '省份名称',
                                                                     `city_nm` STRING COMMENT '城市名称',
                                                                     `cty_nm` STRING COMMENT '区县名称',
                                                                     `lng` STRING COMMENT '经度',
                                                                     `lat` STRING COMMENT '纬度',
                                                                     `addr` STRING COMMENT '详细地址',
                                                                     `bank_nm` STRING COMMENT '开户行名称',
                                                                     `bank_acct_no` STRING COMMENT '开户行账号',
                                                                     `manager_nm` STRING COMMENT '店长名称',
                                                                     `manager_tel` STRING COMMENT '店长联系电话',
                                                                     `hotline` STRING COMMENT '服务热线',
                                                                     `emergency_tel` STRING COMMENT '24小时救援电话',
                                                                     `in_net_dt` STRING COMMENT '入网日期',
                                                                     `out_net_dt` STRING COMMENT '退网日期',
                                                                     `network_type` STRING COMMENT '在网类型',
                                                                     `ord_dlr_cd` STRING COMMENT '对应的订单店代码',
                                                                     `dlv_dlr_cd` STRING COMMENT '对应的交付店代码',
                                                                     `svs_dlr_cd` STRING COMMENT '对应的服务店代码',
                                                                     `biz_cd` STRING COMMENT '事业部代码',
                                                                     `biz_nm` STRING COMMENT '事业部名称',
                                                                     `uni_dlr_cd_flg` STRING COMMENT '首选经销商代码标识',
                                                                     `src_sys` STRING COMMENT '来源系统',
                                                                     `src_sys_id` STRING COMMENT '来源系统id',

    -- 拉链字段
                                                                     `zip_start_dt` DATE NOT NULL COMMENT '拉链开始日期',
                                                                     `zip_end_dt` DATE NOT NULL COMMENT '拉链结束日期',
                                                                     `zip_enable_flg` STRING NOT NULL COMMENT '拉链可用标识，Y/N',

    -- 元数据字段
                                                                     `insert_dt` datetime not null COMMENT '批次日期',
                                                                     `job_nm` STRING COMMENT '任务名称',
                                                                     `ds` VARCHAR(10) NOT NULL COMMENT '分区字段，格式：yyyyMMdd'
)
    UNIQUE KEY(`sk_id`)  -- 拉链表主键：代理键 + 生效日期
COMMENT '维度_渠道_经销商_拉链表'
DISTRIBUTED BY HASH(`sk_id`) BUCKETS 32
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "V2",
    "compression" = "LZ4",
    "enable_unique_key_merge_on_write" = "true",
    "light_schema_change" = "true"
);




SHOW PARTITIONS FROM ads_voc_model_tags_result_data_m_inc

INSERT INTO voc_ms_td.ads_voc_model_tags_result_data_m_inc
(id, publish_time, data_id, one_id, work_id, client_id,
 channel_id, content_type, sample_data_type, original_id, input_data_id, original_text_scene,
 brand_code, car_series_code, label_type, scenario, sentiment, intention_type, topic, opinion, subject, fault_level, description,
 sentiment_score, keywords, model_type,
 raw_data,
 ext_fields,
 biz_ext_attrs,
 biz_ext_attrs2, biz_ext_attrs3,
 cust_ext_attrs,
 veh_ext_attrs,
 dealer_ext_attrs,
 prd_ext_attrs,tags_ext_attrs,
 create_time, update_time, abandon, done)
VALUES('b124', '2025-08-13 14:57:59', 'f170c75abb04a8084030477f2cd49ac81', '1541692005309219148', '1', null,
       'pdt', 'order', '1', 'f170c75abb04a8084030477f2cd49ac8', 'f170c75abb04a8084030477f2cd49ac8', '还是吉利星越内饰好看',
       'A31A03', 'C0001','', '操作情景-制动”', '正面', '表扬', '内饰好看', '大灯-好看', '内饰', '', '好看',
       '', '', 0,
       'ewogICJpZCI6ICJmYzViYjliM2M4YTMyYjk1ZTBkZjc0ZDlhMzg0NGQ2MyIsCiAgIm9uZV9pZCI6ICIxNTQxNjkyMDY1Mjc5MzY5MjkwIiwKICAiaWRfY2FyX25vIjogIjUwMDIzMzE5ODcwNjE3MDExWCIsCiAgImNvbnRlbnRfdHlwZSI6ICJvcmRlciIsCiAgImRhdGFfY3JlYXRlX3RpbWUiOiAiMjAyNS0wOS0xM1QxMzoyNDowOC4wMDArMDg6MDAiLAogICJkYXRhX3VwZGF0ZV90aW1lIjogIjIwMjUtMDktMTNUMTM6MjQ6MDguMDAwKzA4OjAwIiwKICAiY3JlYXRlX3RpbWUiOiAiMjAyNS0wOS0xM1QxODozMzoyMi41MjcrMDg6MDAiLAogICJkYXRhX2lkIjogImYxNzBjNzVhYmIwNGE4MDg0MDMwNDc3ZjJjZDQ5YWM4IiwKICAiY2hhbm5lbF9jb2RlIjogInpoamh6eF8wMSIsCiAgImJyYW5kIjogIumVv+WuieW8leWKmyIsCiAgInNlcmllcyI6ICJDUzc1IiwKICAiaXNfb3V0ZXIiOiAiTiIsCiAgIm1vYmlsZSI6ICIxNzMwNDcwMzE4MSIsCiAgInVzZXJfaWQiOiAiMkNNWmoiLAogICJkbHJfdHlwZSI6ICIzIiwKICAiZGxyX2NvZGUiOiAiSFFDMDg3MzYiLAogICJ0aXRsZSI6ICLmkq3lt6Xlj7fml7bmjILmnLoiLAogICJjb250ZW50IjogIjxwPuaSreW3peWPt+aXtuaMguacujwvcD4iLAogICJpc193c2F0ZXJfYXJteSI6ICJOIiwKICAid2VpZ2h0IjogMCwKICAiZG9uZSI6IDAsCiAgIm1vZGVsX3R5cGUiOiAwLAogICJyZXF1aXJlZF9hdHRycyI6IHsKICAgICJvcmRlcl9pZCI6ICJDQTIwMjUwOTEyMTAwNzIyMSIsCiAgICAib3JkZXJfdHlwZSI6ICIwIiwKICAgICJ3b3JrX3NvdXJjZSI6ICJbXCJndWFuZmFuZ3JleGlhbixuZXdnZnJ4XzAxNFwiXSIKICB9LAogICJkcyI6ICIyMDk5Igp9',
       null,
       'eyJpZCI6IjY4YzRmZmY4ZWIzNzM2MjhjNDZlYzU1YyIsIm9yZGVyX25vIjoiQ0EyMDI1MDkxMjEwMDcyMjEiLCJjdXN0b21lcl9pZCI6IjJDTVpqIiwib3JkZXJfdGVtcGxhdGUiOiIyIiwib3JkZXJfcG9vbF9ncm91cCI6IjgyIiwiYnVzaW5lc3Nfc2NlbmFyaW9zIjoiIiwiZmlyc3RfcmVzcG9uc2VfdGltZSI6MTc1Nzc0MTA0ODAwMCwidXBkYXRlX3RpbWUiOjE3NTc3NDEwNDgwMDAsImNyZWF0ZV90aW1lIjoxNzU3NzQxMDQ4MDAwLCJ0aW1lb3V0X3RpbWUiOjE3NTc3NjI2NDgwMDAsImNsb3NlX3RpbWUiOjE3NTc3NDEwNDgwMDAsImxpYWJsZV93b3JrZ3JvdXAiOiJjbWd6Yndmbm5tNXdlYSNxbXRsbHp4MjQzX3lpbmxpMSIsImxpYWJsZV9hZ2VudCI6ImNtZ3pid2Zubm01d2VhI21jc183NDdjODFmZDY0YWE0NTI2NzNkMmU0MmNhN2EzZWVjZiIsImN1cnJlbnRfd29ya2dyb3VwIjoiY21nemJ3Zm5ubTV3ZWEjcW10bGx6eDI0M195aW5saTEiLCJjdXJyZW50X2FnZW50IjoiY21nemJ3Zm5ubTV3ZWEjbWNzXzc0N2M4MWZkNjRhYTQ1MjY3M2QyZTQyY2E3YTNlZWNmIiwiY3JlYXRlX2FnZW50IjoiY21nemJ3Zm5ubTV3ZWEjbWNzXzc0N2M4MWZkNjRhYTQ1MjY3M2QyZTQyY2E3YTNlZWNmIiwiY3JlYXRlX3dvcmtncm91cCI6ImNtZ3pid2Zubm01d2VhI3FtdGxsengyNDNfeWlubGkxIiwiYmVsb25nX2RlcGFydG1lbnQiOiJtZHZzb3huMWV3Zmx6ZyIsInVwZGF0ZV9hZ2VudCI6ImNtZ3pid2Zubm01d2VhI21jc183NDdjODFmZDY0YWE0NTI2NzNkMmU0MmNhN2EzZWVjZiIsInVwZGF0ZV93b3JrZ3JvdXAiOiJjbWd6Yndmbm5tNXdlYSNxbXRsbHp4MjQzX3lpbmxpMSIsImlzX2Rpc3RyaWJ1dGVfc3RvcmUiOjAsInJvYm90X2Fzc2lzdGVkX2NvbnRlbnQiOiIiLCJpc19wcm9ibGVtX3NvbHZlZCI6IjEiLCJlbnRlcnByaXNlX2lkIjoiY21nemJ3Zm5ubTV3ZWEiLCJzb3VyY2UiOiJbXCJndWFuZmFuZ3JleGlhbixuZXdnZnJ4XzAxNFwiXSIsImNhbGxfaWQiOiIxMTc4NzczMTBfMjIxNDM4MTE4XzE3NTc2NDI4ODgiLCJvcmRlcl9zdGF0dXMiOjQsImJyYW5kIjoieWwiLCJzZXJpZXMiOiJDUzc1IiwiY3VzdG9tZXJfcmVxdWlyZW1lbnRfY29kZSI6IjIwMjQxMTI3LVU3NlhNS1FGLTY2NywyMDI0MTEyNy1VNzZYTUtRRi02NjgsMjAyNTA0MjctUzhheW9KakQtMSwyMDI1MDQyNy1TOGF5b0pqRC00LDIwMjUwNDI3LVM4YXlvSmpELTUiLCJvcmRlcl9wcmlvcml0eSI6IiIsInByb2Nlc3NfdHlwZSI6IjAiLCJyZXF1ZXN0X2xldmVsIjoiMSIsImNhbGxpbmdfbnVtYmVyIjoiMTczMDQ3MDMxODEiLCJoYW5kbGVfZGVhbGVyX25hbWUiOiIiLCJoYW5kbGVfZGVhbGVyX2NvZGUiOiIiLCJoYW5kbGVfZGVhbGVyX2FyZWEiOiIiLCJoYW5kbGVfZGVhbGVyX3Byb3ZpbmNlIjoiIiwiaGFuZGxlX2RlYWxlcl9jaXR5IjoiIiwiaGFuZGxlX2RlYWxlcl9icmFuZCI6IiIsImhhbmRsZV9kZWFsZXJfc3RhdHVzIjoiIiwiYmVfY29tcGxhaW5lZF9kZWFsZXJfbmFtZSI6IiIsImJlX2NvbXBsYWluZWRfZGVhbGVyX2NvZGUiOiIiLCJiZV9jb21wbGFpbmVkX2RlYWxlcl9hcmVhIjoiIiwiYmVfY29tcGxhaW5lZF9kZWFsZXJfcHJvdmluY2UiOiIiLCJiZV9jb21wbGFpbmVkX2RlYWxlcl9jaXR5IjoiIiwiYmVfY29tcGxhaW5lZF9kZWFsZXJfYnJhbmQiOiIiLCJiZV9jb21wbGFpbmVkX2RlYWxlcl9zdGF0dXMiOiIiLCJvd25lcl9uYW1lIjoiIiwiY2FyX3NlcmllcyI6IiIsIm1vZGVsIjoiIiwicGhvbmVfbm8iOiIiLCJ2aW4iOiIiLCJjYXJfYnJhbmQiOiIiLCJsaWNlbnNlIjoiIiwiYnV5X3RpbWUiOiIiLCJjb2xvciI6IiIsImNvbmZpZ19uYW1lIjoiIiwiY3VycmVudF9wcm9jZXNzX25vZGVfaWQiOiJEOUYzODE1MkU2MzcwMzI4NTg0NiIsImVudGVyX3Bvb2xfdGltZSI6MTc1Nzc0MTA0ODc4NX0=',
       null, null,
       'eyJvbmVpZCI6IjE2MDA2MjM4MzY4MTE4MDg3NjkiLCJjdXN0X2NsYXNzaWZ5Ijoi5Liq5Lq66L2m5Li7IiwiaWRfY2FyZF90eXBlIjoiMTA5MzEwMDEiLCJnZW5kZXIiOiIzMDMwMTAwMSIsImJpcnRoZGF5IjoiMTk3My0wNy0wOSAwMDowMDowMCIsImh1a291X3Byb3ZfY2QiOiI2MzAwMDAiLCJodWtvdV9wcm92X25tIjoi6Z2S5rW355yBIiwiaHVrb3VfY2l0eV9jZCI6IjYzMjUwMCIsImh1a291X2NpdHlfbm0iOiLmtbfljZfol4/ml4/oh6rmsrvlt54iLCJodWtvdV9jdHlfY2QiOiI2MzI1MjQiLCJodWtvdV9jdHlfbm0iOiLlhbTmtbfljr8ifQ==',
       'eyJwZXJpb2RfZGF0ZSI6IjIwMjEtMTItMTIiLCJwZXJpb2RfdGltZV9zZWMiOiIyMDIxLTEyLTEyVDAwOjAwOjAwLjAwMCswODowMCIsInBlcmlvZF93aWQiOiIyMDIxMTIxMiIsInZpbiI6IkxTQ0JCWjJQNk1HNzAwNzcyIiwicHJvZF9jb2RlIjoiU0MxMDM1U1BDTTYuQjJEMS0xMzAzLlcyNiIsInByb2RfbmFtZSI6IuWHr+eoi0Y3MCIsIm1kbF9jb2RlIjoiU0MxMDM1U1BDTTYiLCJtZGxfbmFtZSI6IlAyMDEtNEsyMlTvvIw0SzIyVO+8jOWbvTbvvIw2QVTvvIzplb/lrqLvvIzlm5vpqbHvvIwzMTEwa2ciLCJzZXJpZXNfbmFtZSI6IkY3MCIsIm9wdF9jb2RlIjoiQjJEMS0xMzAzIiwib3B0X25hbWUiOiLosarljY7lnosiLCJjb2xfY29kZSI6IlcyNiIsImNvbF9uYW1lIjoi576955m9IiwiZW5nX2Nsc2YiOiI0S+ezu+WIlyIsImVuZ19zZXJpcyI6IjRLMjJUIiwiZW5nX21kbCI6IjRLMjJUIiwiZGlzX210IjoiMi40VCIsImRpc19jaCI6IuWbveWFrWIiLCJ0cmFuc19jbHNmIjoi6Ieq5YqoIiwidHJhbnNfZm9ybSI6IjZBVCIsImN1c3RvbV9jb2RlIjoiPyIsInZlaF90eXBlIjoi5ZWG5ZOB6L2mIiwidmNsX251bSI6IjEiLCJwbG50X2NvZGUiOiJCSzUiLCJwbG50X25hbWUiOiLkv53lrprplb/lrokiLCJwcm9kdWN0X2RhdGUiOiIyMDIxMTIxMiIsInNyY19zeXMiOiLkv53lrprplb/lrokiLCJzcmNfc3lzX2lkIjoiRVJQX0RHX1FYQyJ9',
       'eyJza19pZCI6ImMxZDY4NGYxMzdjZDA3OGRhOTk5NTRlZTY1ZDY2ZDA5IiwiZGxyX2NkIjoiSFFDMjAzMzMiLCJkbHJfbm0iOiLokKXlj6Por5rmmLHmsb3ovabplIDllK7mnInpmZDlhazlj7jokKXlj6PlupciLCJkbHJfc19ubSI6IuiQpeWPo+axvemFjeWfjuW6lyIsImRlcHRfY2QiOiJERVBfS0MiLCJkZXB0X25tIjoi5Yev56iLIiwic3RvcmVfbHZsIjoiMSIsInMxX2Rscl9jZCI6IkhRQzIwMzMzIiwiczFfZGxyX25tIjoi6JCl5Y+j6K+a5pix5rG96L2m6ZSA5ZSu5pyJ6ZmQ5YWs5Y+46JCl5Y+j5bqXIiwib3JkX2ZsZyI6IjEiLCJkbHZfZmxnIjoiMSIsInN2c19mbGciOiIwIiwicGFpbnRfZmxnIjoiMCIsInZhbGlkX2ZsZyI6IjEiLCJxdWl0X2ZsZyI6IjAiLCJyZXpfZmxnIjoiMCIsImNobl90eXBlX2NkIjoiS0NfQ0hBTk5FTF9UWVBFX0NPREVfMyIsImNobl90eXBlX25tIjoi5Lit5b+DIiwiY2huX2x2bF9jZCI6IktDX0NIQU5ORUxfTEVWRUxfQ09ERV8yIiwiY2huX2x2bF9ubSI6IuS4reW/gyIsImltYWdlX2NkIjoiS0NfSU1BR0VfQ09ERV82OSIsImltYWdlX25tIjoi5L2T6aqM5bqXIiwiY3VycmVudF9zdGF0ZSI6Iuato+W8j+i/kOiQpSIsIm9wZXJhdGlvbl90eXBlIjoi5q2j5bi46L+Q6JClIiwid2FyX3pvbmVfY2QiOiIxNTU3MTgwMDg1OTk4NTg3OTA1Iiwid2FyX3pvbmVfbm0iOiLljJfpg6jmiJjljLoiLCJ3YXJfem9uZV9wYXJ0Ijoi6L695a6B5YiG5Yy6Iiwid2FyX3pvbmVfcGFydF91c2VyX2NkIjoiMDEwMTYzMiIsIndhcl96b25lX3BhcnRfdXNlcl9ubSI6Iue/n+azoiIsInByb3ZfY2QiOiIyMTAwMDAiLCJjaXR5X2NkIjoiMjEwODAwIiwiY3R5X2NkIjoiMjEwODExIiwicHJvdl9ubSI6Iui+veWugeecgSIsImNpdHlfbm0iOiLokKXlj6PluIIiLCJjdHlfbm0iOiLogIHovrnljLoiLCJsbmciOiIxMjIuMjg5MjM5IiwibGF0IjoiNDAuNjYyNzY4IiwiYWRkciI6IuiQpeWPo+W4guiAgei+ueWMuuaWsOiQpei3r+WNlzIx5Y+3IiwibWFuYWdlcl9ubSI6IuW8oOWFtCIsIm1hbmFnZXJfdGVsIjoiMTg1MjQ2Mjg4MTYiLCJlbWVyZ2VuY3lfdGVsIjoi6JCl5Y+j5biCIiwiaW5fbmV0X2R0IjoiMjAyNC0wMi0yNSAwMDowMDowMCIsIm5ldHdvcmtfdHlwZSI6IuWVhueUqOi9piIsIm9yZF9kbHJfY2QiOiJIUUMyMDMzMyIsImJpel9jZCI6IlFYQyIsImJpel9ubSI6IumHjeW6humVv+WuieWHr+eoi+axvei9puenkeaKgOaciemZkOWFrOWPuCIsInVuaV9kbHJfY2RfZmxnIjoiMSIsInNyY19zeXMiOiJjX2RlYWxlcl9kYXRhIiwic3JjX3N5c19pZCI6IlFEMjAyNDAzMDQwMDAwMSIsInppcF9zdGFydF9kdCI6IjE5NzAwMTAxIiwiemlwX2VuZF9kdCI6Ijk5OTkxMjMxIiwiemlwX2VuYWJsZV9mbGciOiIxIiwiZHMiOiI5OTk5MTIzMSJ9',
       'eyJwcmRfd2lkIjoiYWM5NzYyMDRjOTcwMzE1ZmNiZGQ0MGRkMDhkNDI3ZWYiLCJ2Y2xfY2QiOiJTQzcwMDZBQUZCRVYuQ05IMzAyNC5TSzIiLCJncnBfbm0iOiLkuK3lm73plb/lrokiLCJlbnRlcnBfbm0iOiLmt7Hok53msb3ovaYiLCJlbnRlcnBfYmQiOiLplb/lrokiLCJidXNfYmRfc2VjX2NscyI6Iua3seiTneaxvei9piIsImJ1c19iZF9jZCI6IjAwNTAwNTAiLCJwcm9kX2JkX3NlcSI6Iua3seiTneaxvei9piIsInByb2RfYmQiOiJTTDAzIiwicHJvZF9ubV9jZCI6IkMzODUiLCJwcm9kX25tIjoi5rex6JOdU0wwMy3nuq/nlLXniYgiLCJjZmd0bl9ubSI6IjUxNee6r+eUteeJiCIsImFubnVhbF9tZGwiOiIyMDIy5qy+IiwidGltZV9ta3QiOiIyMDIy5bm0MDfmnIgiLCJ2Y2xfY2xyX2NkIjoiU0syIiwidmNsX2Nscl9ubSI6IuaciOWyqeeBsCIsImNvdW50cnkiOiLoh6rkuLsiLCJzYWxfZGVwcnRtdCI6Iua3seiTneaxvei9piIsInByb2pfY2QiOiJDMzg1Iiwic2J1X25hbWVfZnVsbCI6Iua3seiTneaxvei9puenkeaKgOaciemZkOWFrOWPuCIsInNidV9uYW1lIjoi5rex6JOd5rG96L2mIiwic2J1X2NvZGUiOiJTTCIsImJkX2Nsc2YiOiJDQVIiLCJzZWdfbXQiOiLntKflh5Hlnovovb/ovaYiLCJwb3dfY2xzZiI6IuaWsOiDvea6kCIsImZ1X2Nsc2YiOiJCRVbvvIjnuq/nlLXliqjvvIkiLCJwdWJfY2QiOiJTQzcwMDZBQUZCRVYiLCJwdWJfbm0iOiJDMzg177yMWFRETTI377yMNTE1a23vvIzph43luoYt5Lik5rGf5LiA5bel5Y6CIiwibWF4X21zIjoyMTAwLCJjbHRjX21pbCI6MCwib3ZyX2xlbiI6NDgyMCwiYmRfZm9ybSI6IuS4ieWOoiIsImRyaXZfYmlnX2Nsc2YiOiLkuKTpqbEiLCJkcml2X2Nsc2YiOiLlkI7nva7lkI7pqbEiLCJtb2RsX3N0IjoiU0M3MDA2QUFGQkVWLkNOSDMwMjQiLCJtb2RsX3N0X25tIjoiQzM4Ne+8jFhURE0yN++8jDUxNWtt77yM6YeN5bqGLeS4pOaxn+S4gOW3peWOgu+8jOS4reiIqueUteaxoO+8jGxldmVsM++8jOeZveiJsu+8jOaLv+ajru+8jOemj+eRnuazsOWFiyIsInN0YXR1cyI6IkNOSDMwMjQiLCJzZWF0X251bSI6NSwibWFudV9ic19jZCI6IjAwNTAyODIiLCJtYW51X2JzIjoi6YeN5bqGLeS4pOaxn+S4gOW3peWOgiIsInN0ZF9wbG50X2NvZGUiOiJDM0QiLCJzdGRfcGxudF9uYW1lIjoi5Lik5rGf5bel5Y6C5LiA5Y6C5Yy6IiwiaW5fcHJkdG4iOiLlnKjkuqciLCJpbl9zb2xkIjoi5Zyo5ZSuIiwiZW5hYmxlX2ZsYWciOiJGIiwic3RhcnRfZGF0ZV9hY3RpdmUiOiIyMDIyLTEyLTE0IiwiZW5kX2RhdGVfYWN0aXZlIjoiMjAyNS0wNi0xOSIsInNyY19zeXMiOiLkuqflk4HkuLvmlbDmja7ns7vnu58sYm9t57O757ufLOWPr+mFjee9rmJvbeezu+e7n+OAgeihpeW9leezu+e7nyIsInNyY19zeXNfaWQiOiJwbWRfY2Fib21fbWRwLHBtZF9jYWJvbV9ib20saWJvbV9jcWNhYm9tZGIsYmxwdCIsImJhdGNoX2R0IjoiMjAyNS0wOS0yMCAwMTozMDo0MCIsImpvYl9ubSI6ImpvYl9taWRfcHJkX3Byb2R1Y3RfMiIsImRzIjoiOTk5OTEyMzEifQ==',
       null,
       now(), now(), '0', 0);