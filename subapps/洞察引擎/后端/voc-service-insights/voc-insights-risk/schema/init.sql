CREATE TABLE `rp_all_types_risk_data` (
          `id` varchar(32) NOT NULL COMMENT "主键",
          `channel_id` varchar(60000) NULL COMMENT "渠道id",
          `risk_id` varchar(32) NULL COMMENT "风险点Id",
          `project_id` varchar(255) NULL COMMENT "项目ID",
          `label_type` varchar(10000) NULL COMMENT "srv:服务 prod:产品 qy:品质",
          `label_type_level_first` varchar(60000) NULL COMMENT "一级标签",
          `label_type_level_second` varchar(60000) NULL COMMENT "二级标签",
          `label_type_level_three` varchar(60000) NULL COMMENT "三级标签",
          `label_type_level_four` varchar(60000) NULL COMMENT "四级标签/话题",
          `label_type_level_five` varchar(60000) NULL COMMENT "五级",
          `new_id_array` json NULL COMMENT "聚合ID数组",
          `city_code` json NULL COMMENT "城市数组",
          `risk` varchar(100) NULL COMMENT "风险code或用户Id",
          `risk_index` varchar(32) NULL COMMENT "风险程度值G",
          `brand_code_name` varchar(255) NULL COMMENT "品牌名称",
          `car_series_name` varchar(255) NULL COMMENT "车系名称",
          `risk_type` varchar(32) NULL COMMENT "风险类型1为业务,2为质量,3为用户",
          `risk_name` varchar(1000) NULL COMMENT "风险问题",
          `focus_name` varchar(60000) NULL COMMENT "聚焦问题",
          `opinion_words` json NULL COMMENT "观点热词",
          `opinion_words_json` json NULL COMMENT "观点热词",
          `negative_num` bigint(20) NULL COMMENT "负面提及量",
          `complain_num` bigint(20) NULL COMMENT "投诉提及量",
          `risk_words_num` bigint(20) NULL COMMENT "风险词提及量",
          `user_num` bigint(20) NULL COMMENT "发声用户(累加)",
          `channel_num` bigint(20) NULL COMMENT "发声渠道(累加)",
          `emotion_num` decimal(38, 2) NULL COMMENT "净情感值",
          `risk_level` varchar(50) NULL COMMENT "当前风险等级",
          `statistic_type` char(1) NULL COMMENT "洞察周期",
          `create_time` datetime NULL COMMENT "创建时间",
          `update_time` datetime NULL COMMENT "更新时间",
          `del_flag` decimal(1, 0) NULL COMMENT "删除标记"
) ENGINE=OLAP
PRIMARY KEY(`id`)
COMMENT "风险预警-所有类型风险表"
DISTRIBUTED BY HASH(`id`)
ORDER BY(`id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);


CREATE TABLE `rp_emotion_risk_data` (
            `id` varchar(64) NOT NULL COMMENT "",
            `channel_id` varchar(60000) NULL COMMENT "渠道id",
            `brand_name` varchar(255) NULL COMMENT "品牌code",
            `car_series_name` varchar(255) NULL COMMENT "车系",
            `label_type_level_first` varchar(32) NULL COMMENT "一级code",
            `label_type_level_second` varchar(32) NULL COMMENT "二级code",
            `label_type_level_three` varchar(32) NULL COMMENT "三级code",
            `label_type_level_four` varchar(100) NULL COMMENT "四级code",
            `label_type_level_five` varchar(100) NULL COMMENT "五级",
            `label_type` varchar(200) NULL COMMENT "产品 PROD，服务 SERVICE，品质 QY",
            `new_id_array` json NULL COMMENT "聚合ID数组",
            `city_code` json NULL COMMENT "城市数组",
            `total_num` bigint(20) NULL COMMENT "总提及量",
            `negative_num` bigint(20) NULL COMMENT "负面量",
            `complain_num` bigint(20) NULL COMMENT "投诉量",
            `user_num` bigint(20) NULL COMMENT "发声用户(累加)",
            `channel_num` bigint(20) NULL COMMENT "发声渠道(累加)",
            `risk_keywords_num` bigint(20) NULL COMMENT "风险关键词量",
            `keywords` json NULL COMMENT "观点热词",
            `risk_index` varchar(32) NULL COMMENT "与riskType有关 为1时为风险程度值G,为0时是综合指数",
            `statistic_type` char(1) NULL COMMENT "周期类型",
            `publish_date` datetime NULL COMMENT "发生时间",
            `date_year` varchar(4) NULL COMMENT "年",
            `date_day` varchar(4) NULL COMMENT "天",
            `date_month` varchar(2) NULL COMMENT "月",
            `date_week` varchar(2) NULL COMMENT "周",
            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间",
            `s_num` varchar(32) NULL COMMENT "情感指数",
            `r_num` varchar(32) NULL COMMENT "风险词指数",
            `date_quarter` varchar(1) NULL COMMENT "季"
) ENGINE=OLAP
PRIMARY KEY(`id`)
COMMENT "风险-业务风险表"
DISTRIBUTED BY HASH(`id`)
ORDER BY(`id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);


CREATE TABLE `rp_quality_risk_data` (
        `id` varchar(64) NOT NULL COMMENT "",
        `channel_id` varchar(60000) NULL COMMENT "渠道id",
        `brand_name` varchar(255) NULL COMMENT "品牌code",
        `car_series_name` varchar(255) NULL COMMENT "车系",
        `label_type_level_first` varchar(32) NULL COMMENT "一级code",
        `label_type_level_second` varchar(32) NULL COMMENT "二级code",
        `label_type_level_three` varchar(32) NULL COMMENT "三级code",
        `label_type_level_four` varchar(100) NULL COMMENT "四级code",
        `label_type_level_five` varchar(100) NULL COMMENT "五级",
        `new_id_array` json NULL COMMENT "聚合ID数组",
        `city_code` json NULL COMMENT "城市数组",
        `total_num` bigint(20) NULL COMMENT "总提及量",
        `negative_num` bigint(20) NULL COMMENT "负面量",
        `risk_keywords_num` bigint(20) NULL COMMENT "风险关键词量",
        `user_num` bigint(20) NULL COMMENT "发声用户(累加)",
        `channel_num` bigint(20) NULL COMMENT "发声渠道(累加)",
        `keywords` json NULL COMMENT "观点热词",
        `statistic_type` char(1) NULL COMMENT "周期类型",
        `publish_date` datetime NULL COMMENT "发生时间",
        `date_year` varchar(4) NULL COMMENT "年",
        `date_month` varchar(2) NULL COMMENT "月",
        `date_week` varchar(2) NULL COMMENT "周",
        `date_quarter` varchar(1) NULL COMMENT "季",
        `date_day` varchar(4) NULL COMMENT "天",
        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间",
        `p_num` varchar(64) NULL COMMENT "质量指数",
        `s_num` varchar(64) NULL COMMENT "严重性分数",
        `risk_index` varchar(64) NULL COMMENT "与riskType有关 为1时为风险程度值G,为0时是综合指数"
) ENGINE=OLAP
PRIMARY KEY(`id`)
COMMENT "风险-质量风险表"
DISTRIBUTED BY HASH(`id`)
ORDER BY(`id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);


CREATE TABLE `rp_user_risk_data` (
                                     `id` varchar(64) NOT NULL COMMENT "",
                                     `channel_id` varchar(128) NULL COMMENT "渠道id",
                                     `user_id` varchar(128) NULL COMMENT "用户id",
                                     `user_name` varchar(65533) NULL COMMENT "用户名",
                                     `negative_num` bigint(20) NULL COMMENT "负面量",
                                     `complain_num` bigint(20) NULL COMMENT "投诉量",
                                     `channel_num` bigint(20) NULL COMMENT "渠道量",
                                     `emotion_num` decimal(38, 2) NULL COMMENT "净情感值",
                                     `voice_num` bigint(20) NULL COMMENT "发声数",
                                     `risk_level` char(1) NULL COMMENT "等级",
                                     `focus_problem` json NULL COMMENT "聚焦问题",
                                     `label_type_level_first` varchar(65533) NULL COMMENT "一级code",
                                     `label_type_level_second` varchar(65533) NULL COMMENT "二级code",
                                     `label_type_level_three` varchar(65533) NULL COMMENT "三级code",
                                     `label_type_level_four` varchar(65533) NULL COMMENT "四级code",
                                     `label_type_level_five` varchar(65533) NULL COMMENT "五级",
                                     `label_type` varchar(65533) NULL COMMENT "产品 PROD，服务 SERVICE，品质 QY",
                                     `keywords` json NULL COMMENT "观点热词",
                                     `new_id_array` varchar(65533) NULL COMMENT "聚合ID数组",
                                     `city_code` json NULL COMMENT "城市数组",
                                     `risk_index` varchar(32) NULL COMMENT "风险程度值G",
                                     `statistic_type` char(1) NULL COMMENT "周期类型",
                                     `publish_date` datetime NULL COMMENT "发生时间",
                                     `date_year` varchar(4) NULL COMMENT "年",
                                     `date_month` varchar(2) NULL COMMENT "月",
                                     `date_week` varchar(2) NULL COMMENT "周",
                                     `date_quarter` varchar(1) NULL COMMENT "季",
                                     `brand_name` varchar(255) NULL COMMENT "品牌code",
                                     `car_series_name` varchar(255) NULL COMMENT "车系",
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间"
) ENGINE=OLAP
PRIMARY KEY(`id`)
COMMENT "风险-用户风险表"
DISTRIBUTED BY HASH(`id`)
ORDER BY(`id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);


CREATE MATERIALIZED VIEW sta_rp_all_types_risk_data_view
DISTRIBUTED BY HASH(`id`)
REFRESH ASYNC START('2024-01-01 10:00:00') EVERY (interval 5 MINUTE )
AS
SELECT
    `VDP_RS_TD`.`rp_all_types_risk_data`.`id`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`risk_id`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`channel_id`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`project_id`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`label_type`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`label_type_level_first`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`label_type_level_second`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`label_type_level_three`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`label_type_level_four`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`label_type_level_five`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`new_id_array`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`city_code`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`risk`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`risk_index`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`brand_code_name`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`car_series_name`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`risk_type`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`risk_name`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`focus_name`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`opinion_words`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`opinion_words_json`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`negative_num`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`complain_num`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`risk_words_num`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`user_num`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`channel_num`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`emotion_num`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`risk_level`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`statistic_type`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`create_time`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`update_time`,
    `VDP_RS_TD`.`rp_all_types_risk_data`.`del_flag`
FROM
    `VDP_RS_TD`.`rp_all_types_risk_data`