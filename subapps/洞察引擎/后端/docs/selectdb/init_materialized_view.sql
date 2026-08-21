CREATE MATERIALIZED VIEW voc_ext_ins_tag_by_level_mv
(id,leaf_code,leaf_label,tag_parent_id,level,frist_code,frist_label,second_code,second_label,three_code,three_label,four_code,four_label,tag_type,tag_attribute,energy_type,car_type,tag_status,tag_description,seriousness,user_journey1,user_journey2,user_journey3,create_time,update_time,create_user,update_user,app_client,sort,emotion,intention,scenario_attr,event_clarity,d2c_responsible_dept,d2c_cc_dept,d2c_accountable_dept,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,insert_dt)
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-16 19:30:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 2
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS WITH `base_tags` AS (
    SELECT
        `id`,              -- 标签主键（用于后续补充目标表字段）
        `tag_code`,        -- 标签编码
        `tag_name`,        -- 标签名称
        `tag_parent_id`,   -- 父级ID（目标表必填字段）
        `tag_attribute`,   -- 标签属性（目标表字段）
        `tag_type`,        -- 标签类型（目标表字段）
        `energy_type`,     -- 关联能源（目标表字段，原表为JSON，转STRING适配）
        `car_type`,        -- 车辆类型（目标表字段，原表为JSON，转STRING适配）
        `tag_status`,      -- 标签状态（目标表字段）
        `tag_description`, -- 标签描述（目标表字段）
        `seriousness`,     -- 严重性（目标表字段）
        `user_journey1`,    -- 用户旅途（原表为JSON，拆分适配目标表user_journey1-3）
        `user_journey2`,    -- 用户旅途（原表为JSON，拆分适配目标表user_journey1-3）
        `user_journey3`,    -- 用户旅途（原表为JSON，拆分适配目标表user_journey1-3）
        `create_time`,     -- 创建时间（目标表Hudi预合并字段）
        `update_time`,     -- 更新时间（目标表字段）
        `create_user`,     -- 创建人（目标表字段）
        `update_user`,     -- 更新人（目标表字段）
        `app_client`,      -- 应用客户（目标表字段）
        `sort`,            -- 排序字段（目标表字段）
        `emotion`,
        `intention`,
        `scenario_attr`,
        `event_clarity`,
        `d2c_responsible_dept`,
        `d2c_cc_dept`,
        `d2c_accountable_dept`,
        `tag_accuracy`,
        `tag_customer_issue_classification`,
        `tag_issue_severity`,
        `tag_code_status`,
        `tag_business_domain`,
        `tag_high_value_flag`,
        `tag_complaint_flag_needing_reply`,
        `tag_high_quality_voc_flag`,
        `tag_new_energy_or_fuel`,
        `tag_need_forvclosed_loop`
    FROM `ins_tag_client`
    WHERE
        -- tag_type = 'userJourney'  -- 规则7：仅保留userJourney类型
        `tag_status` = '1'     -- 仅启用状态（目标表tag_status字段）
),
-- 提取5级标签（基准层，补充原表基础属性）
`level5` AS (
    SELECT
        `id` AS `l5_id`,
        `tag_code` AS `l5_tag_code`,
        `tag_name` AS `l5_tag_name`,
        `tag_parent_id` AS `l5_parent_id`,
        5 AS `l5_level`,  -- 层级标识（目标表level字段）
        -- 补充目标表所需基础属性（从base_tags继承）
        `tag_type`,
        `tag_attribute`,
        CAST(`energy_type` AS STRING) AS `energy_type`,  -- JSON转STRING适配目标表
        CAST(`car_type` AS STRING) AS `car_type`,        -- JSON转STRING适配目标表
        `tag_status`,
        `tag_description`,
        `seriousness`,
        -- 拆分user_journey JSON为目标表user_journey1-3（示例：取前3个元素，可按实际JSON结构调整）
        `user_journey1`,
        `user_journey2`,
        `user_journey3`,
        `create_time`,
        `update_time`,
        `create_user`,
        `update_user`,
        `app_client`,
        `sort`,
        `emotion`,
        `intention`,
        `scenario_attr`,
        `event_clarity`,
        `d2c_responsible_dept`,
        `d2c_cc_dept`,
        `d2c_accountable_dept`,
        `tag_accuracy`,
        `tag_customer_issue_classification`,
        `tag_issue_severity`,
        `tag_code_status`,
        `tag_business_domain`,
        `tag_high_value_flag`,
        `tag_complaint_flag_needing_reply`,
        `tag_high_quality_voc_flag`,
        `tag_new_energy_or_fuel`,
        `tag_need_forvclosed_loop`
    FROM base_tags
    WHERE `tag_attribute` = 'FinalLabel'  -- 5级节点标识
),
-- 提取1级标签（父级为0级，用于关联）
`level1` AS (
    SELECT
        `id` AS `level1_id`,
        `tag_code` AS `level1_code`,
        `tag_name` AS `level1_name`
    FROM base_tags
    WHERE `tag_parent_id` = '0'
),
-- 提取2级标签（关联1级，用于关联）
`level2` AS (
    SELECT
        `l2`.`id` AS `level2_id`,
        `l2`.`tag_code` AS `level2_code`,
        `l2`.`tag_name` AS `level2_name`,
        `l2`.`sort` AS `sort`,
        `l1`.`level1_id` AS `level2_p_level1_id`  -- 关联1级ID，用于后续追溯
    FROM base_tags `l2`
    INNER JOIN level1 `l1` ON `l2`.`tag_parent_id` = `l1`.`level1_id`
),
-- 提取3级标签（关联2级，用于关联）
`level3` AS (
    SELECT
        `l3`.`id` AS `level3_id`,
        `l3`.`tag_code` AS `level3_code`,
        `l3`.`tag_name` AS `level3_name`,
        `l2`.`level2_id` AS `level3_p_level2_id`,  -- 关联2级ID
        `l2`.`level2_p_level1_id` AS `level3_p_level1_id`  -- 关联1级ID
    FROM base_tags `l3`
    INNER JOIN level2 `l2` ON `l3`.`tag_parent_id` = `l2`.`level2_id`
),
-- 提取4级标签（关联3级，用于关联）
`level4` AS (
    SELECT
        `l4`.`id` AS `level4_id`,
        `l4`.`tag_code` AS `level4_code`,
        `l4`.`tag_name` AS `level4_name`,
        `l3`.`level3_id` AS `level4_p_level3_id`,  -- 关联3级ID
        `l3`.`level3_p_level2_id` AS `level4_p_level2_id`,  -- 关联2级ID
        `l3`.`level3_p_level1_id` AS `level4_p_level1_id`  -- 关联1级ID
    FROM base_tags `l4`
    INNER JOIN level3 `l3` ON `l4`.`tag_parent_id` = `l3`.`level3_id`
)
-- 5级标签关联1-4级标签（补充完整层级与基础属性）
SELECT
    -- 5级标签核心字段（基准层）
    `l5`.`l5_id` as `id`,
    `l5`.`l5_tag_code` AS `leaf_code`,
    `l5`.`l5_tag_name` AS `leaf_label`,
    `l5`.`l5_parent_id` AS `tag_parent_id`,
    CAST(`l5`.`l5_level` AS STRING) AS `level`,
    -- 1级标签字段（跨级时为NULL）
    `l1`.`level1_code` AS `frist_code`,
    `l1`.`level1_name` AS `frist_label`,
    -- 2级标签字段（跨级时为NULL）
    `l2`.`level2_code` AS `second_code`,
    `l2`.`level2_name` AS `second_label`,
    -- 3级标签字段（跨级时为NULL）
    `l3`.`level3_code` AS `three_code`,
    `l3`.`level3_name` AS `three_label`,
    -- 4级标签字段（跨级时为NULL）
    `l4`.`level4_code` AS `four_code`,
    `l4`.`level4_name` AS `four_label`,
    -- 目标表所需基础属性（从l5继承，确保与5级标签属性一致）
    `l5`.`tag_type`,
    `l5`.`tag_attribute`,
    `l5`.`energy_type`,
    `l5`.`car_type`,
    `l5`.`tag_status`,
    `l5`.`tag_description`,
    `l5`.`seriousness`,
    `l5`.`user_journey1`,
    `l5`.`user_journey2`,
    `l5`.`user_journey3`,
    `l5`.`create_time`,
    `l5`.`update_time`,
    `l5`.`create_user`,
    `l5`.`update_user`,
    `l5`.`app_client`,
    `l2`.`sort`,
    `l5`.`emotion`,
    `l5`.`intention`,
    `l5`.`scenario_attr`,
    `l5`.`event_clarity`,
    `l5`.`d2c_responsible_dept`,
    `l5`.`d2c_cc_dept`,
    `l5`.`d2c_accountable_dept`,
    `l5`.`tag_accuracy`,
    `l5`.`tag_customer_issue_classification`,
    `l5`.`tag_issue_severity`,
    `l5`.`tag_code_status`,
    `l5`.`tag_business_domain`,
    `l5`.`tag_high_value_flag`,
    `l5`.`tag_complaint_flag_needing_reply`,
    `l5`.`tag_high_quality_voc_flag`,
    `l5`.`tag_new_energy_or_fuel`,
    `l5`.`tag_need_forvclosed_loop`,
    current_timestamp() as `insert_dt`
FROM level5 `l5`
-- 左连接4级：5级父级=4级ID则匹配，否则为NULL
         LEFT JOIN level4 `l4` ON `l5`.`l5_parent_id` = `l4`.`level4_id`
-- 左连接3级：优先用4级关联的3级ID，无则用5级父级直接关联
         LEFT JOIN level3 `l3` ON COALESCE(`l4`.`level4_p_level3_id`, `l5`.`l5_parent_id`) = `l3`.`level3_id`
-- 左连接2级：优先用3级关联的2级ID，无则用5级父级直接关联
         LEFT JOIN level2 `l2` ON COALESCE(`l3`.`level3_p_level2_id`, `l5`.`l5_parent_id`) = `l2`.`level2_id`
-- 左连接1级：优先用2级关联的1级ID，无则用5级父级直接关联（1级必关联，无NULL）
         LEFT JOIN level1 `l1` ON COALESCE(`l2`.`level2_p_level1_id`, `l5`.`l5_parent_id`) = `l1`.`level1_id`
;

CREATE MATERIALIZED VIEW voc_imp_hudi_dm_voc_cust_mv
(oneid,cust_classify,id_card_no,mobile,cust_nm,is_car_owner_flg,gender,age,age_group,high_educaion,marriage_statue,family_income,is_exchange_flg,purchase_car_times,is_member_flg,hukou_prov_cd,hukou_prov_nm,hukou_city_cd,hukou_city_nm,cust_type,lived_prov_nm,lived_city_nm,profession,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-24 05:05:00"
DUPLICATE KEY(`oneid`)
DISTRIBUTED BY HASH(`oneid`) BUCKETS 8
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "id_card_no, mobile, oneid",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728"
)
AS SELECT `oneid`, `cust_classify`, `id_card_no`, `mobile`, `cust_nm`,
          `is_car_owner_flg`, `gender`, `age`, `age_group`,
          `high_educaion`, `marriage_statue`, `family_income`,
          `is_exchange_flg`, `purchase_car_times`,
          `is_member_flg`,
          `hukou_prov_cd`, `hukou_prov_nm`, `hukou_city_cd`, `hukou_city_nm`,
          `cust_type`,
          `lived_prov_nm`, `lived_city_nm`, `profession`, `insert_dt`
   FROM `voc_imp_hudi_dm_voc_cust`
   WHERE `oneid` IS NOT null;




CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_status_mv
(data_id,status,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-23 10:22:00"
DUPLICATE KEY(`data_id`)
DISTRIBUTED BY HASH(`data_id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS SELECT
              `t`.`data_id`,
              `t`.`status`,
              `t`.`insert_dt`
   FROM (
            SELECT
                json_keys(`voc_anal_flow_mate_data_status`.`data`)[1] AS `data_id`,
                JSON_EXTRACT_STRING(`voc_anal_flow_mate_data_status`.`data`, CONCAT('$.', json_keys(`voc_anal_flow_mate_data_status`.`data`)[1])) AS `status`,
                `voc_anal_flow_mate_data_status`.`insert_dt`,
                ROW_NUMBER() OVER (PARTITION BY json_keys(`voc_anal_flow_mate_data_status`.`data`)[1] ORDER BY `voc_anal_flow_mate_data_status`.`insert_dt` desc) AS `rn`
            FROM `voc_anal_flow_mate_data_status`
            WHERE
                `voc_anal_flow_mate_data_status`.`data` IS NOT NULL
              AND json_length(`voc_anal_flow_mate_data_status`.`data`) > 0  -- 确保非空对象 {}
        ) `t`
   WHERE `t`.`rn` = 1
     AND `t`.`data_id` IS NOT NULL;




CREATE MATERIALIZED VIEW voc_ext_ins_tag_by_system_mv
(id,topic,topic_text,cpt_tag_first_code,cpt_tag_first,cpt_tag_second_code,cpt_tag_second,cpt_tag_three_code,cpt_tag_three,cpt_tag_four_code,cpt_tag_four,ujy_tag_first_code,ujy_tag_first,ujy_tag_second_code,ujy_tag_second,ujy_tag_three_code,ujy_tag_three,ujy_tag_four_code,ujy_tag_four,cma_tag_first_code,cma_tag_first,cma_tag_second_code,cma_tag_second,cma_tag_three_code,cma_tag_three,cma_tag_four_code,cma_tag_four,dom_tag_first_code,dom_tag_first,dom_tag_second_code,dom_tag_second,dom_tag_three_code,dom_tag_three,dom_tag_four_code,dom_tag_four,vtr_tag_first_code,vtr_tag_first,vtr_tag_second_code,vtr_tag_second,vtr_tag_three_code,vtr_tag_three,vtr_tag_four_code,vtr_tag_four,nps_tag_first_code,nps_tag_first,nps_tag_second_code,nps_tag_second,nps_tag_three_code,nps_tag_three,nps_tag_four_code,nps_tag_four,tag_parent_id,tag_type,tag_attribute,energy_type,car_type,tag_status,tag_description,seriousness,user_journey1,user_journey2,user_journey3,scenario_attr,event_clarity,d2c_responsible_dept,d2c_cc_dept,d2c_accountable_dept,create_time,update_time,create_user,update_user,app_client,sort,level,emotion,intention,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,insert_dt)
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-23 11:26:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS SELECT
              -- 主键：复用源表id（确保符合目标表Hudi主键规则`primaryKey='id'`，避免冲突）
              `id`,
              -- topic/topic_text：源表无对应数据，暂填NULL（可根据后续业务逻辑补充）
              `leaf_code` AS `topic`,
              `leaf_label` AS `topic_text`,
              -- -------------------------- CPT标签映射（tag_type='CPT'） --------------------------
              CASE WHEN `tag_type` = 'CPT' THEN `frist_code` ELSE NULL END AS `cpt_tag_first_code`,
              CASE WHEN `tag_type` = 'CPT' THEN `frist_label` ELSE NULL END AS `cpt_tag_first`,
              CASE WHEN `tag_type` = 'CPT' THEN `second_code` ELSE NULL END AS `cpt_tag_second_code`,
              CASE WHEN `tag_type` = 'CPT' THEN `second_label` ELSE NULL END AS `cpt_tag_second`,
              CASE WHEN `tag_type` = 'CPT' THEN `three_code` ELSE NULL END AS `cpt_tag_three_code`,
              CASE WHEN `tag_type` = 'CPT' THEN `three_label` ELSE NULL END AS `cpt_tag_three`,
              CASE WHEN `tag_type` = 'CPT' THEN `four_code` ELSE NULL END AS `cpt_tag_four_code`,
              CASE WHEN `tag_type` = 'CPT' THEN `four_label` ELSE NULL END AS `cpt_tag_four`,
              -- -------------------------- userJourney标签映射（tag_type='userJourney'，用户全旅程） --------------------------
              CASE WHEN `tag_type` = 'JOUR' THEN `frist_code` ELSE NULL END AS `ujy_tag_first_code`,
              CASE WHEN `tag_type` = 'JOUR' THEN `frist_label` ELSE NULL END AS `ujy_tag_first`,
              CASE WHEN `tag_type` = 'JOUR' THEN `second_code` ELSE NULL END AS `ujy_tag_second_code`,
              CASE WHEN `tag_type` = 'JOUR' THEN `second_label` ELSE NULL END AS `ujy_tag_second`,
              CASE WHEN `tag_type` = 'JOUR' THEN `three_code` ELSE NULL END AS `ujy_tag_three_code`,
              CASE WHEN `tag_type` = 'JOUR' THEN `three_label` ELSE NULL END AS `ujy_tag_three`,
              CASE WHEN `tag_type` = 'JOUR' THEN `four_code` ELSE NULL END AS `ujy_tag_four_code`,
              CASE WHEN `tag_type` = 'JOUR' THEN `four_label` ELSE NULL END AS `ujy_tag_four`,
              -- -------------------------- Domain标签映射（tag_type='Domain'，商品化属性） --------------------------
              CASE WHEN `tag_type` = 'PR0' THEN `frist_code` ELSE NULL END AS `cma_tag_first_code`,
              CASE WHEN `tag_type` = 'PR0' THEN `frist_label` ELSE NULL END AS `cma_tag_first`,
              CASE WHEN `tag_type` = 'PR0' THEN `second_code` ELSE NULL END AS `cma_tag_second_code`,
              CASE WHEN `tag_type` = 'PR0' THEN `second_label` ELSE NULL END AS `cma_tag_second`,
              CASE WHEN `tag_type` = 'PR0' THEN `three_code` ELSE NULL END AS `cma_tag_three_code`,
              CASE WHEN `tag_type` = 'PR0' THEN `three_label` ELSE NULL END AS `cma_tag_three`,
              CASE WHEN `tag_type` = 'PR0' THEN `four_code` ELSE NULL END AS `cma_tag_four_code`,
              CASE WHEN `tag_type` = 'PR0' THEN `four_label` ELSE NULL END AS `cma_tag_four`,
              -- -------------------------- CommodityAttr标签映射（tag_type='CommodityAttr'，全领域业务） --------------------------
              CASE WHEN `tag_type` = 'CA' THEN `frist_code` ELSE NULL END AS `dom_tag_first_code`,
              CASE WHEN `tag_type` = 'CA' THEN `frist_label` ELSE NULL END AS `dom_tag_first`,
              CASE WHEN `tag_type` = 'CA' THEN `second_code` ELSE NULL END AS `dom_tag_second_code`,
              CASE WHEN `tag_type` = 'CA' THEN `second_label` ELSE NULL END AS `dom_tag_second`,
              CASE WHEN `tag_type` = 'CA' THEN `three_code` ELSE NULL END AS `dom_tag_three_code`,
              CASE WHEN `tag_type` = 'CA' THEN `three_label` ELSE NULL END AS `dom_tag_three`,
              CASE WHEN `tag_type` = 'CA' THEN `four_code` ELSE NULL END AS `dom_tag_four_code`,
              CASE WHEN `tag_type` = 'CA' THEN `four_label` ELSE NULL END AS `dom_tag_four`,
              -- -------------------------- VRT标签映射（tag_type='VRT'） --------------------------
              CASE WHEN `tag_type` = 'VRT' THEN `frist_code` ELSE NULL END AS `vtr_tag_first_code`,
              CASE WHEN `tag_type` = 'VRT' THEN `frist_label` ELSE NULL END AS `vtr_tag_first`,
              CASE WHEN `tag_type` = 'VRT' THEN `second_code` ELSE NULL END AS `vtr_tag_second_code`,
              CASE WHEN `tag_type` = 'VRT' THEN `second_label` ELSE NULL END AS `vtr_tag_second`,
              CASE WHEN `tag_type` = 'VRT' THEN `three_code` ELSE NULL END AS `vtr_tag_three_code`,
              CASE WHEN `tag_type` = 'VRT' THEN `three_label` ELSE NULL END AS `vtr_tag_three`,
              CASE WHEN `tag_type` = 'VRT' THEN `four_code` ELSE NULL END AS `vtr_tag_four_code`,
              CASE WHEN `tag_type` = 'VRT' THEN `four_label` ELSE NULL END AS `vtr_tag_four`,
              -- -------------------------- NPS标签映射（tag_type='NPS'） --------------------------
              CASE WHEN `tag_type` = 'NPS' THEN `frist_code` ELSE NULL END AS `nps_tag_first_code`,
              CASE WHEN `tag_type` = 'NPS' THEN `frist_label` ELSE NULL END AS `nps_tag_first`,
              CASE WHEN `tag_type` = 'NPS' THEN `second_code` ELSE NULL END AS `nps_tag_second_code`,
              CASE WHEN `tag_type` = 'NPS' THEN `second_label` ELSE NULL END AS `nps_tag_second`,
              CASE WHEN `tag_type` = 'NPS' THEN `three_code` ELSE NULL END AS `nps_tag_three_code`,
              CASE WHEN `tag_type` = 'NPS' THEN `three_label` ELSE NULL END AS `nps_tag_three`,
              CASE WHEN `tag_type` = 'NPS' THEN `four_code` ELSE NULL END AS `nps_tag_four_code`,
              CASE WHEN `tag_type` = 'NPS' THEN `four_label` ELSE NULL END AS `nps_tag_four`,
              -- -------------------------- 公共基础字段（直接复用源表，确保类型与目标表一致） --------------------------
              `tag_parent_id`,
              `tag_type`,
              `tag_attribute`,
              `energy_type`,
              `car_type`,
              `tag_status`,
              `tag_description`,
              `seriousness`,
              `user_journey1`,
              `user_journey2`,
              `user_journey3`,
              `scenario_attr`,
              `event_clarity`,
              `d2c_responsible_dept`,
              `d2c_cc_dept`,
              `d2c_accountable_dept`,
              `create_time`,
              `update_time`,
              `create_user`,
              `update_user`,
              `app_client`,
              `sort`,
              `level`,
              `emotion`,
              `intention`,
              `tag_accuracy`,
              `tag_customer_issue_classification`,
              `tag_issue_severity`,
              `tag_code_status`,
              `tag_business_domain`,
              `tag_high_value_flag`,
              `tag_complaint_flag_needing_reply`,
              `tag_high_quality_voc_flag`,
              `tag_new_energy_or_fuel`,
              `tag_need_forvclosed_loop`,
              current_timestamp() as `insert_dt`
   FROM `voc_ext_ins_tag_by_level_mv` `src`;







CREATE MATERIALIZED VIEW voc_anal_di_stg_mate_data_m_batch_range_merge_mv
(id,data_create_time,create_time,content_type,data_update_time,data_id,channel_code,brand,series,model,is_outer,one_id,id_car_no,mobile,email,global_id,user_id,user_name,vhl_id,vhl_vin,dlr_id,dlr_code,dlr_type,market_id,title,content,is_wsater_army,weight,attrs,attrs2,attrs3,work_id,model_type,cust_ext_attrs,vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-30 11:29:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "insert_dt, data_id, id",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS WITH `raw_data` AS (
    SELECT
        `e`.`id`,
        `e`.`data_create_time`,
        `e`.`create_time`,
        `e`.`content_type`,
        `e`.`data_update_time`,
        `e`.`data_id`,
        `e`.`channel_code`,
        `e`.`brand`,
        `e`.`series`,
        `e`.`model`,
        `e`.`is_outer`,
        case
        	when `e`.`is_outer` <> 'Y' and `e`.`mobile`  is not null
        		and `e`.`mobile` not REGEXP '^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$'
        		then TO_BASE64(SM4_ENCRYPT(`e`.`mobile`, 'changanvoc2025xx'))
        	else `e`.`mobile`
        end as `mobile`,
        case
        	when `e`.`is_outer` <> 'Y' and `e`.`id_car_no`  is not null
        		and `e`.`id_car_no` not REGEXP '^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$'
        		then TO_BASE64(SM4_ENCRYPT(`e`.`id_car_no`, 'changanvoc2025xx'))
        	else `e`.`id_car_no`
        end as `id_car_no`,
        `e`.`email`,
        `e`.`global_id`,
        CONCAT('u_', MD5(CONCAT('_', COALESCE(`e`.`user_id`, `e`.`user_name`, 'unknown'), `e`.`channel_code`))) AS `user_id`,
--         e.user_name,
        case
        	when `e`.`is_outer` <> 'Y' and `e`.`user_name`  is not null
        		and `e`.`user_name` not REGEXP '^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$'
        		then TO_BASE64(SM4_ENCRYPT(`e`.`user_name`, 'changanvoc2025xx'))
        	else `e`.`user_name`
        end as `user_name`,
        `e`.`vhl_id`,
--         e.vhl_vin,
        case
        	when `e`.`is_outer` <> 'Y' and `e`.`vhl_vin`  is not null
        		and `e`.`vhl_vin` not REGEXP '^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$'
        		then TO_BASE64(SM4_ENCRYPT(`e`.`vhl_vin`, 'changanvoc2025xx'))
        	else `e`.`vhl_vin`
        end as `vhl_vin`,
        `e`.`dlr_id`,
        `e`.`dlr_code`,
        `e`.`dlr_type`,
        `e`.`market_id`,
        `e`.`title`,
        `e`.`content`,
        `e`.`is_wsater_army`,
        `e`.`weight`,
        case
			when nvl(nullif(`e`.`attrs`, ''), null) is  null or cast(`e`.`attrs` as string) = '{}'
				then JSON_OBJECT('default_', null)
			else `e`.`attrs`
		end as `attrs`,
--         `e`.`attrs`,
        `e`.`attrs2`,
        `e`.`attrs3`,
        `e`.`work_id`,
        `e`.`model_type`,
        CASE
            WHEN NULLIF(`e`.`one_id`, '') IS NOT NULL THEN `e`.`one_id`
            WHEN `e`.`is_outer` = 'Y' THEN CONCAT('o_', MD5(CONCAT(`e`.`channel_code`, COALESCE(`e`.`user_id`, `e`.`user_name`, 'outer'), `e`.`channel_code`)))
            WHEN `e`.`is_outer` = 'N' THEN CONCAT('i_', MD5(CONCAT(`e`.`channel_code`, COALESCE(`e`.`user_id`, `e`.`user_name`, 'inner'), `e`.`channel_code`)))
            ELSE CONCAT('x_', MD5(CONCAT(`e`.`channel_code`, COALESCE(`e`.`user_id`, `e`.`user_name`, 'unknown'), `e`.`channel_code`)))
        END AS `one_id`,
        '764547797eb2e192763f5334028d49c9' AS `client_id`
    FROM `voc_anal_di_stg_mate_data_m_inc` `e`
    WHERE
        `e`.`insert_dt` >= NOW() - INTERVAL 2 hour
        and `e`.`insert_dt` <= NOW()
        AND `e`.`channel_code` IS NOT NULL
        AND `e`.`data_create_time` IS NOT NULL
        AND `e`.`work_id` IS NOT null
),
`finished_data` as (
	select `voc_anal_di_stg_mate_data_finished_record`.`data_id` from `voc_anal_di_stg_mate_data_finished_record`
),
`workid_data` as(
	select md5(uuid()) as `work_id`
),
`main_data` AS (
	select `f1`.`id`, `f1`.`data_create_time`, `f1`.`create_time`, `f1`.`content_type`, `f1`.`data_update_time`, `f1`.`data_id`, `f1`.`channel_code`, `f1`.`brand`, `f1`.`series`, `f1`.`model`, `f1`.`is_outer`, `f1`.`mobile`, `f1`.`id_car_no`, `f1`.`email`, `f1`.`global_id`, `f1`.`user_id`, `f1`.`user_name`, `f1`.`vhl_id`, `f1`.`vhl_vin`, `f1`.`dlr_id`, `f1`.`dlr_code`, `f1`.`dlr_type`, `f1`.`market_id`, `f1`.`title`, `f1`.`content`, `f1`.`is_wsater_army`, `f1`.`weight`, `f1`.`attrs`, `f1`.`attrs2`, `f1`.`attrs3`,
	`f3`.`work_id`, `f1`.`model_type`, `f1`.`one_id`, `f1`.`client_id` from raw_data as `f1`
	left join `finished_data` as `f2` on `f1`.`data_id` = `f2`.`data_id`
	left join workid_data as `f3` on 1=1
	where `f2`.`data_id` is null
	limit 0, 200000
),
-- 第二步：仅对 is_outer = 'N' 的数据做关联
`inner_data` AS (
    SELECT `main_data`.`id`, `main_data`.`data_create_time`, `main_data`.`create_time`, `main_data`.`content_type`, `main_data`.`data_update_time`, `main_data`.`data_id`, `main_data`.`channel_code`, `main_data`.`brand`, `main_data`.`series`, `main_data`.`model`, `main_data`.`is_outer`, `main_data`.`mobile`, `main_data`.`id_car_no`, `main_data`.`email`, `main_data`.`global_id`, `main_data`.`user_id`, `main_data`.`user_name`, `main_data`.`vhl_id`, `main_data`.`vhl_vin`, `main_data`.`dlr_id`, `main_data`.`dlr_code`, `main_data`.`dlr_type`, `main_data`.`market_id`, `main_data`.`title`, `main_data`.`content`, `main_data`.`is_wsater_army`, `main_data`.`weight`, `main_data`.`attrs`, `main_data`.`attrs2`, `main_data`.`attrs3`, `main_data`.`work_id`, `main_data`.`model_type`, `main_data`.`one_id`, `main_data`.`client_id`
    FROM main_data WHERE `main_data`.`is_outer` <> 'Y'
),

`outer_data` AS (
    SELECT `main_data`.`id`, `main_data`.`data_create_time`, `main_data`.`create_time`, `main_data`.`content_type`, `main_data`.`data_update_time`, `main_data`.`data_id`, `main_data`.`channel_code`, `main_data`.`brand`, `main_data`.`series`, `main_data`.`model`, `main_data`.`is_outer`, `main_data`.`mobile`, `main_data`.`id_car_no`, `main_data`.`email`, `main_data`.`global_id`, `main_data`.`user_id`, `main_data`.`user_name`, `main_data`.`vhl_id`, `main_data`.`vhl_vin`, `main_data`.`dlr_id`, `main_data`.`dlr_code`, `main_data`.`dlr_type`, `main_data`.`market_id`, `main_data`.`title`, `main_data`.`content`, `main_data`.`is_wsater_army`, `main_data`.`weight`,  `main_data`.`attrs2`, `main_data`.`attrs3`, `main_data`.`work_id`, `main_data`.`model_type`, `main_data`.`one_id`, `main_data`.`client_id`
    ,case
			when json_valid(`main_data`.`attrs`) = 0
				then null
			else
				json_set(`main_data`.`attrs`
						, "$.data.retweeted.content", null,"$.data.retweeted.title",null
						, "$.data.title",null, "$.data.raw_content",null, "$.data.ocr_dic",null
						, "$.data.content",null, "$.data.ocr",null, "$.data.istar_asr",null
						, "$.data.label",null, "$.data.pic_urls",null, "$.data.content_xml",null
						, "$.data.analysis.summary",null)
		end as `attrs`
    FROM main_data
    WHERE `main_data`.`is_outer` = 'Y'  -- 包含 'Y' 和其他

),
-- 第三步：安全 JOIN（避免 OR）
`joined_inner` AS (
    SELECT
        `i`.`id`, `i`.`data_create_time`, `i`.`create_time`, `i`.`content_type`, `i`.`data_update_time`,
        `i`.`data_id`, `i`.`channel_code`, `i`.`brand`, `i`.`series`, `i`.`model`, `i`.`is_outer`,
        `i`.`mobile`, `i`.`id_car_no`, `i`.`email`, `i`.`global_id`, `i`.`user_id`, `i`.`user_name`,
        `i`.`vhl_id`, `i`.`vhl_vin`, `i`.`dlr_id`, `i`.`dlr_code`, `i`.`dlr_type`, `i`.`market_id`,
        `i`.`title`, `i`.`content`, `i`.`is_wsater_army`, `i`.`weight`, `i`.`attrs`, `i`.`attrs2`,
        `i`.`attrs3`, `i`.`work_id`, `i`.`model_type`,
--         `i`.`one_id`,
        CASE
            WHEN `b_by_one`.`cust_json` IS NOT NULL THEN md5(concat_ws("|",JSON_EXTRACT_STRING(`b_by_one`.`cust_json`, "$.oneid")))
            WHEN `b_by_mobile`.`cust_json` IS NOT NULL THEN md5(concat_ws("|",JSON_EXTRACT_STRING(`b_by_mobile`.`cust_json`, "$.oneid")))
            WHEN `b_by_idcard`.`cust_json` IS NOT NULL THEN md5(concat_ws("|",JSON_EXTRACT_STRING(`b_by_idcard`.`cust_json`, "$.oneid")))
            ELSE `i`.`one_id`
        END AS `one_id`,
        `i`.`client_id`,
        CASE
            WHEN `b_by_one`.`cust_json` IS NOT NULL THEN `b_by_one`.`cust_json`
            WHEN `b_by_mobile`.`cust_json` IS NOT NULL THEN `b_by_mobile`.`cust_json`
            WHEN `b_by_idcard`.`cust_json` IS NOT NULL THEN `b_by_idcard`.`cust_json`
            ELSE NULL
        END AS `cust_ext_attrs`,
        CASE
            WHEN `b_by_one`.`vehicle_json` IS NOT NULL THEN `b_by_one`.`vehicle_json`
            WHEN `b_by_mobile`.`vehicle_json` IS NOT NULL THEN `b_by_mobile`.`vehicle_json`
            WHEN `b_by_idcard`.`vehicle_json` IS NOT NULL THEN `b_by_idcard`.`vehicle_json`
            WHEN `b_by_vin`.`vehicle_json` IS NOT NULL THEN `b_by_vin`.`vehicle_json`
            ELSE NULL
        END AS `vhl_ext_attrs`,
        `d`.`dealer_json` AS `dealer_ext_attrs`
    FROM inner_data `i`
    -- 按 one_id 关联（优先）
    LEFT JOIN `voc_imp_cust_json_by_one_id_mv` `b_by_one`
        ON `b_by_one`.`one_id` = `i`.`one_id`
    -- 按 mobile 关联（次之）
    LEFT JOIN `voc_imp_cust_json_b_by_mobile_mv` `b_by_mobile`
        ON `b_by_mobile`.`mobile` = `i`.`mobile` AND `b_by_one`.`one_id` IS NULL
    -- 按 id_card_no 关联（最后）
    LEFT JOIN `voc_imp_cust_json_b_by_idcard_mv` `b_by_idcard`
        ON `b_by_idcard`.`id_card_no` = `i`.`id_car_no`
    LEFT JOIN `voc_imp_cust_json_b_by_vin_mv` `b_by_vin`
        ON `b_by_vin`.`vin` = `i`.`vhl_vin`
    -- 经销商关联
    LEFT JOIN `voc_imp_dealer_json_info_mv` `d`
        ON `d`.`dealer_code` = `i`.`dlr_code`
)
-- 最终合并：inner + outer
SELECT
    `id`,   `data_create_time`, `create_time`, `content_type`, `data_update_time`,
    `data_id`, `channel_code`, `brand`, `series`, `model`, `is_outer`, `one_id`,
    `id_car_no`, `mobile`, `email`, `global_id`, `user_id`, `user_name`,
    `vhl_id`, `vhl_vin`, `dlr_id`, `dlr_code`, `dlr_type`, `market_id`,
    `title`, `content`, `is_wsater_army`, `weight`, `attrs`, `attrs2`, `attrs3`,
    `work_id`, `model_type`,
    `cust_ext_attrs`,
    `vhl_ext_attrs`,
    `dealer_ext_attrs`,
    NULL AS `prd_ext_attrs`,
    NOW() AS `insert_dt`
FROM joined_inner
UNION ALL
SELECT
    `outer_data`.`id`, `outer_data`.`data_create_time`, `outer_data`.`create_time`, `outer_data`.`content_type`, `outer_data`.`data_update_time`,
    `outer_data`.`data_id`, `outer_data`.`channel_code`, `outer_data`.`brand`, `outer_data`.`series`, `outer_data`.`model`, `outer_data`.`is_outer`, `outer_data`.`one_id`,
    `outer_data`.`id_car_no`, `outer_data`.`mobile`, `outer_data`.`email`, `outer_data`.`global_id`, `outer_data`.`user_id`, `outer_data`.`user_name`,
    `outer_data`.`vhl_id`, `outer_data`.`vhl_vin`, `outer_data`.`dlr_id`, `outer_data`.`dlr_code`, `outer_data`.`dlr_type`, `outer_data`.`market_id`,
    `outer_data`.`title`, `outer_data`.`content`, `outer_data`.`is_wsater_army`, `outer_data`.`weight`, `outer_data`.`attrs`, `outer_data`.`attrs2`, `outer_data`.`attrs3`,
    `outer_data`.`work_id`, `outer_data`.`model_type`,
    NULL AS `cust_ext_attrs`,
    NULL AS `vhl_ext_attrs`,
    NULL AS `dealer_ext_attrs`,
    NULL AS `prd_ext_attrs`,
    NOW() AS `insert_dt`
FROM outer_data;




-- drop  MATERIALIZED VIEW voc_anal_di_stg_mate_data_m_batch_range_merge_mv_v2;
CREATE MATERIALIZED VIEW voc_anal_di_stg_mate_data_m_batch_range_merge_mv_v2
(id,data_create_time,create_time,content_type,data_update_time,data_id,channel_code,brand,series,model,is_outer,id_car_no,mobile,email,global_id,user_id,user_name,vhl_id,vhl_vin,dlr_id,dlr_code,dlr_type,market_id,title,content,is_wsater_army,weight,attrs,attrs2,attrs3,model_type,cust_ext_attrs,vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs,insert_dt,client_id,work_id,one_id)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 30 MINUTE STARTS "2026-04-16 16:33:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "insert_dt, data_id, id",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS WITH  `raw_data` AS (
    SELECT
        `internal`.`voc_ms_td`.`e`.`id`,
        `internal`.`voc_ms_td`.`e`.`data_create_time`,
        `internal`.`voc_ms_td`.`e`.`create_time`,
        `internal`.`voc_ms_td`.`e`.`content_type`,
        `internal`.`voc_ms_td`.`e`.`data_update_time`,
        `internal`.`voc_ms_td`.`e`.`data_id`,
        `internal`.`voc_ms_td`.`e`.`channel_code`,
        `internal`.`voc_ms_td`.`e`.`brand`,
        `internal`.`voc_ms_td`.`e`.`series`,
        `internal`.`voc_ms_td`.`e`.`model`,
        `internal`.`voc_ms_td`.`e`.`is_outer`,
        case
        	when `internal`.`voc_ms_td`.`e`.`is_outer` <> 'Y' and `internal`.`voc_ms_td`.`e`.`mobile`  is not null
        		and `internal`.`voc_ms_td`.`e`.`mobile` not REGEXP '^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$'
        		then TO_BASE64(SM4_ENCRYPT(`internal`.`voc_ms_td`.`e`.`mobile`, 'changanvoc2025xx'))
        	else `internal`.`voc_ms_td`.`e`.`mobile`
        end as `mobile`,
        case
        	when `internal`.`voc_ms_td`.`e`.`is_outer` <> 'Y' and `internal`.`voc_ms_td`.`e`.`id_car_no`  is not null
        		and `internal`.`voc_ms_td`.`e`.`id_car_no` not REGEXP '^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$'
        		then TO_BASE64(SM4_ENCRYPT(`internal`.`voc_ms_td`.`e`.`id_car_no`, 'changanvoc2025xx'))
        	else `internal`.`voc_ms_td`.`e`.`id_car_no`
        end as `id_car_no`,
        `internal`.`voc_ms_td`.`e`.`email`,
        `internal`.`voc_ms_td`.`e`.`global_id`,
        CONCAT('u_', MD5(CONCAT('_', COALESCE(
        	`internal`.`voc_ms_td`.`e`.`user_id`,
        	`internal`.`voc_ms_td`.`e`.`user_name`,
        	`internal`.`voc_ms_td`.`e`.`mobile`,
        	`internal`.`voc_ms_td`.`e`.`id_car_no`,
        	'unknown'), `internal`.`voc_ms_td`.`e`.`channel_code`)))
        AS `user_id`,
--         e.user_name,
        case
        	when `internal`.`voc_ms_td`.`e`.`is_outer` <> 'Y' and `internal`.`voc_ms_td`.`e`.`user_name`  is not null
        		and `internal`.`voc_ms_td`.`e`.`user_name` not REGEXP '^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$'
        		then TO_BASE64(SM4_ENCRYPT(`internal`.`voc_ms_td`.`e`.`user_name`, 'changanvoc2025xx'))
        	else `internal`.`voc_ms_td`.`e`.`user_name`
        end as `user_name`,
        `internal`.`voc_ms_td`.`e`.`vhl_id`,
--         e.vhl_vin,
        case
        	when `internal`.`voc_ms_td`.`e`.`is_outer` <> 'Y' and `internal`.`voc_ms_td`.`e`.`vhl_vin`  is not null
        		and `internal`.`voc_ms_td`.`e`.`vhl_vin` not REGEXP '^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$'
        		then TO_BASE64(SM4_ENCRYPT(`internal`.`voc_ms_td`.`e`.`vhl_vin`, 'changanvoc2025xx'))
        	else `internal`.`voc_ms_td`.`e`.`vhl_vin`
        end as `vhl_vin`,
        `internal`.`voc_ms_td`.`e`.`dlr_id`,
        `internal`.`voc_ms_td`.`e`.`dlr_code`,
        `internal`.`voc_ms_td`.`e`.`dlr_type`,
        `internal`.`voc_ms_td`.`e`.`market_id`,
        `internal`.`voc_ms_td`.`e`.`title`,
        `internal`.`voc_ms_td`.`e`.`content`,
        `internal`.`voc_ms_td`.`e`.`is_wsater_army`,
        `internal`.`voc_ms_td`.`e`.`weight`,
        case
			when nvl(nullif(`internal`.`voc_ms_td`.`e`.`attrs`, ''), null) is  null or cast(`internal`.`voc_ms_td`.`e`.`attrs` as string) = '{}'
				then JSON_OBJECT('default_', null)
			else `internal`.`voc_ms_td`.`e`.`attrs`
		end as `attrs`,
		null as `one_id`,
--         `e`.`attrs`,
        `internal`.`voc_ms_td`.`e`.`attrs2`,
        `internal`.`voc_ms_td`.`e`.`attrs3`,
        `internal`.`voc_ms_td`.`e`.`model_type`
    FROM `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_m_inc` `e`
    where  `internal`.`voc_ms_td`.`e`.`data_create_time` >= NOW() - INTERVAL 1 month
    	and `internal`.`voc_ms_td`.`e`.`is_outer` <> 'Y'
        and `internal`.`voc_ms_td`.`e`.`insert_dt` >= NOW() - INTERVAL 24 hour
        and `internal`.`voc_ms_td`.`e`.`insert_dt` <= NOW()
        AND `internal`.`voc_ms_td`.`e`.`channel_code` IS NOT NULL
        AND `internal`.`voc_ms_td`.`e`.`data_create_time` IS NOT NULL
        AND `internal`.`voc_ms_td`.`e`.`work_id` IS NOT null
),
`duplicate_data` as (
	select JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_finished_record`.`data`, "$.md5") as `md5`  from `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_finished_record`
-- 	where
),
`finished_data` as (
	select `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_finished_record`.`data_id` from `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_finished_record`
),
`inner_data` AS (
	select
		`f2`.`id`,`f2`.`data_create_time`,`f2`.`create_time`,`f2`.`content_type`,`f2`.`data_update_time`,`f2`.`data_id`,`f2`.`channel_code`,`f2`.`brand`,`f2`.`series`,`f2`.`model`,`f2`.`is_outer`,`f2`.`mobile`,`f2`.`id_car_no`,`f2`.`email`,`f2`.`global_id`,`f2`.`user_id`,`f2`.`user_name`,`f2`.`vhl_id`,`f2`.`vhl_vin`,`f2`.`dlr_id`,`f2`.`dlr_code`,`f2`.`dlr_type`,`f2`.`market_id`,`f2`.`title`,`f2`.`content`,`f2`.`is_wsater_army`,`f2`.`weight`,`f2`.`attrs`,`f2`.`attrs2`,`f2`.`attrs3`,`f2`.`model_type`,`f2`.`one_id`
		,`f2`.`md5`
	from (
		select
			`f`.`id`, `f`.`data_create_time`, `f`.`create_time`, `f`.`content_type`, `f`.`data_update_time`, `f`.`data_id`, `f`.`channel_code`, `f`.`brand`, `f`.`series`, `f`.`model`, `f`.`is_outer`, `f`.`mobile`, `f`.`id_car_no`, `f`.`email`, `f`.`global_id`, `f`.`user_id`, `f`.`user_name`, `f`.`vhl_id`, `f`.`vhl_vin`, `f`.`dlr_id`, `f`.`dlr_code`, `f`.`dlr_type`, `f`.`market_id`, `f`.`title`, `f`.`content`, `f`.`is_wsater_army`, `f`.`weight`, `f`.`attrs`, `f`.`attrs2`, `f`.`attrs3`, `f`.`model_type`, `f`.`one_id`,  -- 关键：将 NULL 的 data_id 按唯一 id 分区，避免所有 NULL 被合并
			`f`.`md5`,
		   ROW_NUMBER() OVER (
		       PARTITION BY
		           CASE WHEN `f`.`data_id` IS NULL THEN CAST(`f`.`id` AS CHAR) ELSE `f`.`data_id` END
		       ORDER BY `f`.`create_time` DESC, `f`.`id` DESC  -- 防止时间重复
		   ) AS `rn`
		from (
			select `f1`.`id`, `f1`.`data_create_time`, `f1`.`create_time`, `f1`.`content_type`, `f1`.`data_update_time`, `f1`.`data_id`, `f1`.`channel_code`, `f1`.`brand`, `f1`.`series`, `f1`.`model`, `f1`.`is_outer`, `f1`.`mobile`, `f1`.`id_car_no`, `f1`.`email`, `f1`.`global_id`, `f1`.`user_id`, `f1`.`user_name`, `f1`.`vhl_id`, `f1`.`vhl_vin`, `f1`.`dlr_id`, `f1`.`dlr_code`, `f1`.`dlr_type`, `f1`.`market_id`, `f1`.`title`, `f1`.`content`, `f1`.`is_wsater_army`, `f1`.`weight`, `f1`.`attrs`, `f1`.`attrs2`, `f1`.`attrs3`,
				`f1`.`model_type`, `f1`.`one_id`,`f1`.`md5` from
				(
					select
						`raw_data`.`id`, `raw_data`.`data_create_time`, `raw_data`.`create_time`, `raw_data`.`content_type`, `raw_data`.`data_update_time`, `raw_data`.`data_id`, `raw_data`.`channel_code`, `raw_data`.`brand`, `raw_data`.`series`, `raw_data`.`model`, `raw_data`.`is_outer`, `raw_data`.`mobile`, `raw_data`.`id_car_no`, `raw_data`.`email`, `raw_data`.`global_id`, `raw_data`.`user_id`, `raw_data`.`user_name`, `raw_data`.`vhl_id`, `raw_data`.`vhl_vin`, `raw_data`.`dlr_id`, `raw_data`.`dlr_code`, `raw_data`.`dlr_type`, `raw_data`.`market_id`, `raw_data`.`title`, `raw_data`.`content`, `raw_data`.`is_wsater_army`, `raw_data`.`weight`, `raw_data`.`attrs`, `raw_data`.`one_id`, `raw_data`.`attrs2`, `raw_data`.`attrs3`, `raw_data`.`model_type`,
						md5(concat_ws("", `raw_data`.`channel_code`, `raw_data`.`user_id`, `raw_data`.`title`, `raw_data`.`content`)) as `md5`
					from raw_data
				)
				as `f1`
				left join `finished_data` as `f2` on `f1`.`data_id` = `f2`.`data_id`
				left join duplicate_data as `f3` on `f3`.`md5` = `f1`.`md5`
				where `f2`.`data_id` is null
				limit 0, 50000
		)`f`
	)`f2` WHERE `f2`.`rn` = 1
),
`outer_data` AS (
select
		`internal`.`voc_ms_td`.`f`.`id`, `internal`.`voc_ms_td`.`f`.`data_create_time`, `internal`.`voc_ms_td`.`f`.`create_time`,
		if(`internal`.`voc_ms_td`.`f`.`content_type` = "complaint" and `internal`.`voc_ms_td`.`f`.`is_outer` = "Y", "opinion", `internal`.`voc_ms_td`.`f`.`content_type`) as `content_type`,
		`internal`.`voc_ms_td`.`f`.`data_update_time`, `internal`.`voc_ms_td`.`f`.`data_id`, `internal`.`voc_ms_td`.`f`.`channel_code`, `internal`.`voc_ms_td`.`f`.`brand`, `internal`.`voc_ms_td`.`f`.`series`, `internal`.`voc_ms_td`.`f`.`model`, `internal`.`voc_ms_td`.`f`.`is_outer`, `internal`.`voc_ms_td`.`f`.`one_id`, `internal`.`voc_ms_td`.`f`.`id_car_no`, `internal`.`voc_ms_td`.`f`.`mobile`, `internal`.`voc_ms_td`.`f`.`email`, `internal`.`voc_ms_td`.`f`.`global_id`, `internal`.`voc_ms_td`.`f`.`user_id`, `internal`.`voc_ms_td`.`f`.`user_name`, `internal`.`voc_ms_td`.`f`.`vhl_id`, `internal`.`voc_ms_td`.`f`.`vhl_vin`, `internal`.`voc_ms_td`.`f`.`dlr_id`, `internal`.`voc_ms_td`.`f`.`dlr_code`, `internal`.`voc_ms_td`.`f`.`dlr_type`, `internal`.`voc_ms_td`.`f`.`market_id`, `internal`.`voc_ms_td`.`f`.`title`, `internal`.`voc_ms_td`.`f`.`content`, `internal`.`voc_ms_td`.`f`.`is_wsater_army`, `internal`.`voc_ms_td`.`f`.`weight`, `internal`.`voc_ms_td`.`f`.`attrs`, `internal`.`voc_ms_td`.`f`.`attrs2`, `internal`.`voc_ms_td`.`f`.`attrs3`, `internal`.`voc_ms_td`.`f`.`model_type`, `f`.`rn`
		,`f`.`md5`
		from (
		select
			`internal`.`voc_ms_td`.`f`.`id`,`internal`.`voc_ms_td`.`f`.`data_create_time`,`internal`.`voc_ms_td`.`f`.`create_time`,`internal`.`voc_ms_td`.`f`.`content_type`,`internal`.`voc_ms_td`.`f`.`data_update_time`,`internal`.`voc_ms_td`.`f`.`data_id`,`internal`.`voc_ms_td`.`f`.`channel_code`,`internal`.`voc_ms_td`.`f`.`brand`,`internal`.`voc_ms_td`.`f`.`series`,`internal`.`voc_ms_td`.`f`.`model`,`internal`.`voc_ms_td`.`f`.`is_outer`,`internal`.`voc_ms_td`.`f`.`one_id`,`internal`.`voc_ms_td`.`f`.`id_car_no`,`internal`.`voc_ms_td`.`f`.`mobile`,`internal`.`voc_ms_td`.`f`.`email`,`internal`.`voc_ms_td`.`f`.`global_id`,`internal`.`voc_ms_td`.`f`.`user_id`,`internal`.`voc_ms_td`.`f`.`user_name`,`internal`.`voc_ms_td`.`f`.`vhl_id`,`internal`.`voc_ms_td`.`f`.`vhl_vin`,`internal`.`voc_ms_td`.`f`.`dlr_id`,`internal`.`voc_ms_td`.`f`.`dlr_code`,`internal`.`voc_ms_td`.`f`.`dlr_type`,`internal`.`voc_ms_td`.`f`.`market_id`,`internal`.`voc_ms_td`.`f`.`title`,`internal`.`voc_ms_td`.`f`.`content`,`internal`.`voc_ms_td`.`f`.`is_wsater_army`,`internal`.`voc_ms_td`.`f`.`weight`,`internal`.`voc_ms_td`.`f`.`attrs`,`internal`.`voc_ms_td`.`f`.`attrs2`,`internal`.`voc_ms_td`.`f`.`attrs3`,`internal`.`voc_ms_td`.`f`.`model_type`
			,`f`.`md5`
	    	,ROW_NUMBER() OVER (
			       PARTITION BY
			           CASE WHEN `internal`.`voc_ms_td`.`f`.`data_id` IS NULL THEN CAST(`internal`.`voc_ms_td`.`f`.`id` AS CHAR) ELSE `internal`.`voc_ms_td`.`f`.`data_id` END
			       ORDER BY `internal`.`voc_ms_td`.`f`.`create_time` DESC, `internal`.`voc_ms_td`.`f`.`id` DESC  -- 防止时间重复
			   ) AS `rn`
		from (
		    select
				`internal`.`voc_ms_td`.`f1`.`id`,`internal`.`voc_ms_td`.`f1`.`data_create_time`,`internal`.`voc_ms_td`.`f1`.`create_time`,`internal`.`voc_ms_td`.`f1`.`content_type`,`internal`.`voc_ms_td`.`f1`.`data_update_time`,`internal`.`voc_ms_td`.`f1`.`data_id`,`internal`.`voc_ms_td`.`f1`.`channel_code`,`internal`.`voc_ms_td`.`f1`.`brand`,`internal`.`voc_ms_td`.`f1`.`series`,`internal`.`voc_ms_td`.`f1`.`model`,`internal`.`voc_ms_td`.`f1`.`is_outer`,`internal`.`voc_ms_td`.`f1`.`one_id`,`internal`.`voc_ms_td`.`f1`.`id_car_no`,`internal`.`voc_ms_td`.`f1`.`mobile`,`internal`.`voc_ms_td`.`f1`.`email`,`internal`.`voc_ms_td`.`f1`.`global_id`,`internal`.`voc_ms_td`.`f1`.`user_id`,`internal`.`voc_ms_td`.`f1`.`user_name`,`internal`.`voc_ms_td`.`f1`.`vhl_id`,`internal`.`voc_ms_td`.`f1`.`vhl_vin`,`internal`.`voc_ms_td`.`f1`.`dlr_id`,`internal`.`voc_ms_td`.`f1`.`dlr_code`,`internal`.`voc_ms_td`.`f1`.`dlr_type`,`internal`.`voc_ms_td`.`f1`.`market_id`,`internal`.`voc_ms_td`.`f1`.`title`,`internal`.`voc_ms_td`.`f1`.`content`,`internal`.`voc_ms_td`.`f1`.`is_wsater_army`,`internal`.`voc_ms_td`.`f1`.`weight`,`internal`.`voc_ms_td`.`f1`.`attrs`,`internal`.`voc_ms_td`.`f1`.`attrs2`,`internal`.`voc_ms_td`.`f1`.`attrs3`,`internal`.`voc_ms_td`.`f1`.`model_type`
				,`f1`.`md5`
		    from
			(
				select
					`internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`id`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`insert_dt`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`create_time`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`data_create_time`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`content_type`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`data_update_time`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`data_id`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`channel_code`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`brand`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`series`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`model`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`is_outer`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`one_id`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`id_car_no`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`mobile`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`email`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`global_id`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`user_id`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`user_name`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`vhl_id`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`vhl_vin`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`dlr_id`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`dlr_code`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`dlr_type`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`market_id`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`title`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`content`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`is_wsater_army`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`weight`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`attrs`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`attrs2`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`attrs3`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`work_id`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`done`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`model_type`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`ds`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`is_deleted`,
					md5(concat_ws("", `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`channel_code`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`user_id`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`title`, `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`.`content`)) as `md5`
				from `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_pub_m_inc`
		    ) as `f1`
		    left join finished_data as `f2` on `internal`.`voc_ms_td`.`f1`.`data_id` = `f2`.`data_id`
			left join duplicate_data as `f3` on `f3`.`md5` = `f1`.`md5`
		    where `internal`.`voc_ms_td`.`f1`.`data_create_time` >= NOW() - INTERVAL 1 month
		    	and `internal`.`voc_ms_td`.`f1`.`insert_dt` >= NOW() - INTERVAL 2 hour
		        and `internal`.`voc_ms_td`.`f1`.`insert_dt` <= NOW()
		       and `f2`.`data_id` is null
		      and `f3`.`md5` is null
		      and `internal`.`voc_ms_td`.`f1`.`is_deleted`  = 0
		)`f`
	)`f`
	 WHERE `f`.`rn` = 1
	limit 0, 50000),
-- 第三步：安全 JOIN（避免 OR）
`joined_inner` AS (
    SELECT
        `i`.`id`, `i`.`data_create_time`, `i`.`create_time`, `i`.`content_type`, `i`.`data_update_time`,
        `i`.`data_id`, `i`.`channel_code`, `i`.`brand`, `i`.`series`, `i`.`model`, `i`.`is_outer`,
        `i`.`mobile`, `i`.`id_car_no`, `i`.`email`, `i`.`global_id`, `i`.`user_id`, `i`.`user_name`,
        `i`.`vhl_id`, `i`.`vhl_vin`, `i`.`dlr_id`, `i`.`dlr_code`, `i`.`dlr_type`, `i`.`market_id`,
        `i`.`title`, `i`.`content`, `i`.`is_wsater_army`, `i`.`weight`, `i`.`attrs`, `i`.`attrs2`,
        `i`.`attrs3`,  `i`.`model_type`,
        `i`.`md5`,
        CASE
            WHEN `internal`.`voc_ms_td`.`b_by_one`.`cust_json` IS NOT NULL THEN md5(concat_ws("|",JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`b_by_one`.`cust_json`, "$.oneid")))
            WHEN `internal`.`voc_ms_td`.`b_by_mobile`.`cust_json` IS NOT NULL THEN md5(concat_ws("|",JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`b_by_mobile`.`cust_json`, "$.oneid")))
            WHEN `internal`.`voc_ms_td`.`b_by_idcard`.`cust_json` IS NOT NULL THEN md5(concat_ws("|",JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`b_by_idcard`.`cust_json`, "$.oneid")))
            ELSE `i`.`one_id`
        END AS `one_id`,
        CASE
            WHEN `internal`.`voc_ms_td`.`b_by_one`.`cust_json` IS NOT NULL THEN `internal`.`voc_ms_td`.`b_by_one`.`cust_json`
            WHEN `internal`.`voc_ms_td`.`b_by_mobile`.`cust_json` IS NOT NULL THEN `internal`.`voc_ms_td`.`b_by_mobile`.`cust_json`
            WHEN `internal`.`voc_ms_td`.`b_by_idcard`.`cust_json` IS NOT NULL THEN `internal`.`voc_ms_td`.`b_by_idcard`.`cust_json`
            ELSE NULL
        END AS `cust_ext_attrs`,
        CASE
            WHEN `internal`.`voc_ms_td`.`b_by_one`.`vehicle_json` IS NOT NULL THEN `internal`.`voc_ms_td`.`b_by_one`.`vehicle_json`
            WHEN `internal`.`voc_ms_td`.`b_by_mobile`.`vehicle_json` IS NOT NULL THEN `internal`.`voc_ms_td`.`b_by_mobile`.`vehicle_json`
            WHEN `internal`.`voc_ms_td`.`b_by_idcard`.`vehicle_json` IS NOT NULL THEN `internal`.`voc_ms_td`.`b_by_idcard`.`vehicle_json`
            WHEN `internal`.`voc_ms_td`.`b_by_vin`.`vehicle_json` IS NOT NULL THEN `internal`.`voc_ms_td`.`b_by_vin`.`vehicle_json`
            ELSE NULL
        END AS `vhl_ext_attrs`,
        `internal`.`voc_ms_td`.`d`.`dealer_json` AS `dealer_ext_attrs`
    FROM inner_data `i`
    -- 按 one_id 关联（优先）
    LEFT JOIN `internal`.`voc_ms_td`.`voc_imp_cust_json_by_one_id_mv` `b_by_one`
        ON `internal`.`voc_ms_td`.`b_by_one`.`one_id` = `i`.`one_id`
    -- 按 mobile 关联（次之）
    LEFT JOIN `internal`.`voc_ms_td`.`voc_imp_cust_json_b_by_mobile_mv` `b_by_mobile`
        ON `internal`.`voc_ms_td`.`b_by_mobile`.`mobile` = `i`.`mobile` AND `internal`.`voc_ms_td`.`b_by_one`.`one_id` IS NULL
    -- 按 id_card_no 关联（最后）
    LEFT JOIN `internal`.`voc_ms_td`.`voc_imp_cust_json_b_by_idcard_mv` `b_by_idcard`
        ON `internal`.`voc_ms_td`.`b_by_idcard`.`id_card_no` = `i`.`id_car_no`
    LEFT JOIN `internal`.`voc_ms_td`.`voc_imp_cust_json_b_by_vin_mv` `b_by_vin`
        ON `internal`.`voc_ms_td`.`b_by_vin`.`vin` = `i`.`vhl_vin`
    -- 经销商关联
    LEFT JOIN `internal`.`voc_ms_td`.`voc_imp_dealer_json_info_mv` `d`
        ON `internal`.`voc_ms_td`.`d`.`dealer_code` = `i`.`dlr_code`
)
select
    `f2`.`id`,`f2`.`data_create_time`,`f2`.`create_time`,`f2`.`content_type`,`f2`.`data_update_time`,`f2`.`data_id`,`f2`.`channel_code`,`f2`.`brand`,`f2`.`series`,`f2`.`model`,`f2`.`is_outer`,`f2`.`id_car_no`,`f2`.`mobile`,`f2`.`email`,
    `f2`.`global_id`,`f2`.`user_id`,`f2`.`user_name`,`f2`.`vhl_id`,`f2`.`vhl_vin`,`f2`.`dlr_id`,`f2`.`dlr_code`,`f2`.`dlr_type`,`f2`.`market_id`,`f2`.`title`,`f2`.`content`,`f2`.`is_wsater_army`,`f2`.`weight`,`f2`.`attrs`,`f2`.`attrs2`,
    `f2`.`attrs3`,`f2`.`model_type`,`f2`.`cust_ext_attrs`,`f2`.`vhl_ext_attrs`,`f2`.`dealer_ext_attrs`,`f2`.`prd_ext_attrs`,`f2`.`insert_dt`,`f2`.`client_id`,`f2`.`work_id`,`f2`.`one_id`
from (

         select
             -- 	f.md5,
             `f`.`id`, `f`.`data_create_time`, `f`.`create_time`, `f`.`content_type`, `f`.`data_update_time`, `f`.`data_id`, `f`.`channel_code`, `f`.`brand`, `f`.`series`, `f`.`model`, `f`.`is_outer`,  `f`.`id_car_no`, `f`.`mobile`, `f`.`email`, `f`.`global_id`, `f`.`user_id`, `f`.`user_name`, `f`.`vhl_id`, `f`.`vhl_vin`, `f`.`dlr_id`, `f`.`dlr_code`, `f`.`dlr_type`, `f`.`market_id`, `f`.`title`, `f`.`content`, `f`.`is_wsater_army`, `f`.`weight`, `f`.`attrs`, `f`.`attrs2`, `f`.`attrs3`, `f`.`model_type`, `f`.`cust_ext_attrs`, `f`.`vhl_ext_attrs`, `f`.`dealer_ext_attrs`, `f`.`prd_ext_attrs`, `f`.`insert_dt`,
             '764547797eb2e192763f5334028d49c9' AS `client_id`,md5(uuid()) as `work_id`,
             CASE
                 WHEN `f`.`is_outer` = 'Y' THEN CONCAT('o_', MD5(CONCAT(`f`.`channel_code`, COALESCE(`f`.`user_id`, `f`.`user_name`, 'outer'), `f`.`channel_code`)))
                 WHEN `f`.`is_outer` = 'N' THEN CONCAT('i_', MD5(CONCAT(`f`.`channel_code`, COALESCE(`f`.`user_id`, `f`.`user_name`, 'inner'), `f`.`channel_code`)))
                 ELSE CONCAT('x_', MD5(CONCAT(`f`.`channel_code`, COALESCE(`f`.`user_id`, `f`.`user_name`, 'unknown'), `f`.`channel_code`)))
                 END AS `one_id`,
             ROW_NUMBER() OVER (
				       PARTITION BY
				           `f`.`md5`
				       ORDER BY `f`.`create_time` DESC, `f`.`id` DESC  -- 防止时间重复
				   ) AS `rn`
         from (
                  -- 最终合并：inner + outer
                  select
                      `joined_inner`.`md5`,
                      `joined_inner`.`id`,   `joined_inner`.`data_create_time`, `joined_inner`.`create_time`, `joined_inner`.`content_type`, `joined_inner`.`data_update_time`,
                      `joined_inner`.`data_id`, `joined_inner`.`channel_code`, `joined_inner`.`brand`, `joined_inner`.`series`, `joined_inner`.`model`, `joined_inner`.`is_outer`,
                      `joined_inner`.`id_car_no`, `joined_inner`.`mobile`, `joined_inner`.`email`, `joined_inner`.`global_id`, `joined_inner`.`user_id`, `joined_inner`.`user_name`,
                      `joined_inner`.`vhl_id`, `joined_inner`.`vhl_vin`, `joined_inner`.`dlr_id`, `joined_inner`.`dlr_code`, `joined_inner`.`dlr_type`, `joined_inner`.`market_id`,
                      `joined_inner`.`title`, `joined_inner`.`content`, `joined_inner`.`is_wsater_army`, `joined_inner`.`weight`, `joined_inner`.`attrs`, `joined_inner`.`attrs2`, `joined_inner`.`attrs3`,
                      `joined_inner`.`model_type`,
                      `joined_inner`.`cust_ext_attrs`,
                      `joined_inner`.`vhl_ext_attrs`,
                      `joined_inner`.`dealer_ext_attrs`,
                      NULL AS `prd_ext_attrs`,
                      NOW() AS `insert_dt`
                  FROM joined_inner
                  UNION ALL
                  select
                      `outer_data`.`md5`,
                      `outer_data`.`id`, `outer_data`.`data_create_time`, `outer_data`.`create_time`, `outer_data`.`content_type`, `outer_data`.`data_update_time`,
                      `outer_data`.`data_id`, `outer_data`.`channel_code`, `outer_data`.`brand`, `outer_data`.`series`, `outer_data`.`model`, `outer_data`.`is_outer`,
                      `outer_data`.`id_car_no`, `outer_data`.`mobile`, `outer_data`.`email`, `outer_data`.`global_id`, `outer_data`.`user_id`, `outer_data`.`user_name`,
                      `outer_data`.`vhl_id`, `outer_data`.`vhl_vin`, `outer_data`.`dlr_id`, `outer_data`.`dlr_code`, `outer_data`.`dlr_type`, `outer_data`.`market_id`,
                      `outer_data`.`title`, `outer_data`.`content`, `outer_data`.`is_wsater_army`, `outer_data`.`weight`, `outer_data`.`attrs`, `outer_data`.`attrs2`, `outer_data`.`attrs3`,
                      `outer_data`.`model_type`,
                      NULL AS `cust_ext_attrs`,
                      NULL AS `vhl_ext_attrs`,
                      NULL AS `dealer_ext_attrs`,
                      NULL AS `prd_ext_attrs`,
                      NOW() AS `insert_dt`
                  FROM outer_data
              )`f`
     )  `f2`
where `f2`.`rn` = 1




CREATE MATERIALIZED VIEW voc_imp_cust_vehicle_rel_json_info_mv
(one_id,vin,mobile,id_card_no,cust_json,vehicle_json,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-24 05:10:00"
DUPLICATE KEY(`one_id`)
DISTRIBUTED BY HASH(`one_id`) BUCKETS 16
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "id_card_no, one_id, mobile",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728"
)
AS WITH `cust_latest` AS (
        SELECT `oneid`, `cust_classify`, `id_card_no`, `mobile`, `cust_nm`,
	        `is_car_owner_flg`, `gender`, `age`, `age_group`,
	        `high_educaion`, `marriage_statue`, `family_income`,
	        `is_exchange_flg`, `purchase_car_times`,
	        `is_member_flg`,
	        `hukou_prov_cd`, `hukou_prov_nm`, `hukou_city_cd`, `hukou_city_nm`,
	        `cust_type`,
	        `lived_prov_nm`, `lived_city_nm`, `profession`, `insert_dt`
        FROM `voc_imp_hudi_dm_voc_cust_mv`
        WHERE `oneid` IS NOT null
),
`rel_filtered` AS (
	    SELECT `vin`, `idcard`
	    FROM `voc_imp_hudi_dm_voc_cust_vehicle_rel_mv`
	    WHERE `idcard` IS NOT NULL AND `vin` IS NOT null

),
`veh_data` AS (

	    SELECT
	        `vin`, `col_name`, `product_date`, `offline_date`,
	        `home_abroad`, `dis_ch`, `dis_mt`, `eng_clsf`, `eng_seris`,
	        `veh_type`, `plnt_code`
	    FROM `voc_imp_hudi_dwd_maf_veh_d_full_mv`
	    WHERE `vin` IS NOT NULL

),
`final_data` as (
SELECT
    `cust`.`oneid` AS `one_id`,
    `vhl`.`vin`,
    `cust`.`mobile`,
    `cust`.`id_card_no`,
    -- ✅ 仅使用 cust_latest 中存在的字段
    JSON_OBJECT(
            'oneid', `cust`.`oneid`,
            'cust_classify', `cust`.`cust_classify`,
            'id_card_no', `cust`.`id_card_no`,          -- 注意：原SQL写的是 id_card_type，但CTE中只有 id_card_no
            'mobile', `cust`.`mobile`,
            'cust_nm', `cust`.`cust_nm`,
            'gender', `cust`.`gender`,
            'age', `cust`.`age`,
            'age_group', `cust`.`age_group`,
            'high_educaion', `cust`.`high_educaion`,
            'marriage_statue', `cust`.`marriage_statue`,
            'family_income', `cust`.`family_income`,
            'cust_type', `cust`.`cust_type`,
            'is_exchange_flg', `cust`.`is_exchange_flg`,
            'purchase_car_times', `cust`.`purchase_car_times`,
            'is_member_flg', `cust`.`is_member_flg`,
            'is_car_owner_flg', `cust`.`is_car_owner_flg`,
            'hukou_prov_cd', `cust`.`hukou_prov_cd`,
            'hukou_prov_nm', `cust`.`hukou_prov_nm`,
            'hukou_city_cd', `cust`.`hukou_city_cd`,
            'hukou_city_nm', `cust`.`hukou_city_nm`,
            'lived_prov_nm', `cust`.`lived_prov_nm`,
            'lived_city_nm', `cust`.`lived_city_nm`,
            'profession', `cust`.`profession`
    ) AS `cust_json`,

    JSON_OBJECT(
            'vin', `vhl`.`vin`,
            'col_name', `vhl`.`col_name`,
            'product_date', `vhl`.`product_date`,
            'offline_date', `vhl`.`offline_date`,
            'home_abroad', `vhl`.`home_abroad`,
            'dis_ch', `vhl`.`dis_ch`,
            'dis_mt', `vhl`.`dis_mt`,
            'eng_clsf', `vhl`.`eng_clsf`,
            'eng_seris', `vhl`.`eng_seris`,
            'veh_type', `vhl`.`veh_type`,
            'plnt_code', `vhl`.`plnt_code`
        -- ⚠️ 以下字段不在 veh_data 中，已移除：
        -- period_date, prod_code, series_name, cntry_name, trans_form...
    ) AS `vehicle_json`,
    `cust`.`insert_dt`
FROM cust_latest AS `cust`
         LEFT JOIN rel_filtered AS `rel` ON `cust`.`id_card_no` = `rel`.`idcard`
         LEFT JOIN veh_data AS `vhl` ON `rel`.`vin` = `vhl`.`vin`
 )
select
    distinct `final_data`.`one_id`,
             any_value(`final_data`.`vin`),
             any_value(`final_data`.`mobile`),
             any_value(`final_data`.`id_card_no`),
             any_value(`final_data`.`cust_json`),
             any_value(`final_data`.`vehicle_json`),
             any_value(`final_data`.`insert_dt`)
from final_data
group by `final_data`.`one_id`;





CREATE MATERIALIZED VIEW voc_ext_ins_province_area_mv
(id,area_code,area_name,province_code,province_name)
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-23 11:26:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "province_code, province_name",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS select `voc_mysql_jdbc`.`voc_ms_be`.`ins_province_area`.`id`,`voc_mysql_jdbc`.`voc_ms_be`.`ins_province_area`.`area_code`,`voc_mysql_jdbc`.`voc_ms_be`.`ins_province_area`.`area_name`,`voc_mysql_jdbc`.`voc_ms_be`.`ins_province_area`.`province_code`,`voc_mysql_jdbc`.`voc_ms_be`.`ins_province_area`.`province_name`
   from `voc_mysql_jdbc`.`voc_ms_be`.`ins_province_area`;


-- drop MATERIALIZED VIEW voc_anal_flow_sentiment_annotations_results_ins_mv;


CREATE MATERIALIZED VIEW voc_anal_flow_sentiment_annotations_results_ins_mv
(id,data_id,channel_catagory,channel_code,channel_name,brand_code,brand_name,car_series_code,car_series_name,model_name,content_type,title,sentiment,intention,data_create_time,publish_time,create_time,is_outer,hot_word,keywords,original_text_scene,market_id,competitive_type,is_core,series_factory,automark,one_id,user_journey1,user_journey2,user_journey3,usage_scenario_first,usage_scenario_second,d2c_responsible_dept,d2c_accountable_dept,d2c_cc_dept,cust_global_id,cust_classify,cust_main_phone,is_car_owner,cust_age,cust_age_group,cust_name,cust_gender,cust_high_educaion,marrige_statue,family_income,is_exchange_flg,purchase_car_times,is_member_flg,cust_province_code,cust_province,cust_city_code,cust_city,cust_type,cust_lived_prov,cust_lived_city,cust_profession,vhl_vin,vhl_color_name,vhl_product_date,vhl_offline_date,vhl_is_abroad,vhl_dis_ch,vhl_dis_mt,vhl_eng_clsf,vhl_eng_seris,vhl_veh_type,vhl_country,vhl_bd_clsf,vhl_seg_mt,vhl_pow_clsf,vhl_fu_clsf,vhl_modl_st,vhl_std_plnt_code,dlr_oc_id,dlr_oc_code,dlr_oc_name,dlr_oc_province_code,dlr_oc_province,dlr_oc_city_code,dlr_oc_city,dlr_dc_id,dlr_dc_code,dlr_dc_name,dlr_dc_province_code,dlr_dc_province,dlr_dc_city_code,dlr_dc_city,dlr_mc_id,dlr_mc_code,dlr_mc_name,dlr_mc_province_code,dlr_mc_province,dlr_mc_city_code,dlr_mc_city,is_wsater_army,is_manager_focused,is_big_v,author_id,author_nick,is_main_post,original_link,view_count,comment_count,like_count,share_count,favorite_count,work_order_id,quest_id,quest_type,quest_answer_score,quest_business_type,quest_business_scenario,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_event_clarity,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_complaint_flag_needing_prtv_msg,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,topic,topic_text,opinion,cpt_tag_first_code,cpt_tag_second_code,cpt_tag_three_code,cpt_tag_four_code,cpt_tag_first,cpt_tag_second,cpt_tag_three,cpt_tag_four,ujy_tag_first_code,ujy_tag_second_code,ujy_tag_three_code,ujy_tag_four_code,ujy_tag_first,ujy_tag_second,ujy_tag_three,ujy_tag_four,cma_tag_first_code,cma_tag_second_code,cma_tag_three_code,cma_tag_four_code,cma_tag_first,cma_tag_second,cma_tag_three,cma_tag_four,dom_tag_first_code,dom_tag_second_code,dom_tag_three_code,dom_tag_four_code,dom_tag_first,dom_tag_second,dom_tag_three,dom_tag_four,nps_tag_first_code,nps_tag_second_code,nps_tag_three_code,nps_tag_four_code,nps_tag_first,nps_tag_second,nps_tag_three,nps_tag_four,vtr_tag_first_code,vtr_tag_second_code,vtr_tag_three_code,vtr_tag_four_code,vtr_tag_first,vtr_tag_second,vtr_tag_three,vtr_tag_four,abandon,insert_dt,original_text)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 30 MINUTE STARTS "2026-02-10 20:15:00"
DUPLICATE KEY(`id`)
PARTITION BY (date_trunc(`publish_time`, 'month'))
DISTRIBUTED BY HASH(`id`) BUCKETS 16
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "brand_code, car_series_code, topic, channel_code",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728"
)
AS
WITH result_data AS (
  	select
  		`id`, `data_id`, `channel_catagory`, `channel_code`, `channel_name`, `brand_code`, `brand_name`, `car_series_code`, `car_series_name`, `model_name`, `content_type`, `title`, `sentiment`, `intention`, `data_create_time`, `publish_time`, `create_time`, `is_outer`, `hot_word`, `keywords`, `original_text_scene`, `market_id`, `competitive_type`, `is_core`, `series_factory`, `automark`, `one_id`, `user_journey1`, `user_journey2`, `user_journey3`, `usage_scenario_first`, `usage_scenario_second`, `d2c_responsible_dept`, `d2c_accountable_dept`, `d2c_cc_dept`, `cust_global_id`, `cust_classify`, `cust_main_phone`, `is_car_owner`, `cust_age`, `cust_age_group`, `cust_name`, `cust_gender`, `cust_high_educaion`, `marrige_statue`, `family_income`, `is_exchange_flg`, `purchase_car_times`, `is_member_flg`, `cust_province_code`, `cust_province`, `cust_city_code`, `cust_city`, `cust_type`, `cust_lived_prov`, `cust_lived_city`, `cust_profession`, `vhl_vin`, `vhl_color_name`, `vhl_product_date`, `vhl_offline_date`, `vhl_is_abroad`, `vhl_dis_ch`, `vhl_dis_mt`, `vhl_eng_clsf`, `vhl_eng_seris`, `vhl_veh_type`, `vhl_country`, `vhl_bd_clsf`, `vhl_seg_mt`, `vhl_pow_clsf`, `vhl_fu_clsf`, `vhl_modl_st`, `vhl_std_plnt_code`, `dlr_oc_id`, `dlr_oc_code`, `dlr_oc_name`, `dlr_oc_province_code`, `dlr_oc_province`, `dlr_oc_city_code`, `dlr_oc_city`, `dlr_dc_id`, `dlr_dc_code`, `dlr_dc_name`, `dlr_dc_province_code`, `dlr_dc_province`, `dlr_dc_city_code`, `dlr_dc_city`, `dlr_mc_id`, `dlr_mc_code`, `dlr_mc_name`, `dlr_mc_province_code`, `dlr_mc_province`, `dlr_mc_city_code`, `dlr_mc_city`, `is_wsater_army`, `is_manager_focused`, `is_big_v`, `author_id`, `author_nick`, `is_main_post`, `original_link`, `view_count`, `comment_count`, `like_count`, `share_count`, `favorite_count`, `work_order_id`, `quest_id`, `quest_type`, `quest_answer_score`, `quest_business_type`, `quest_business_scenario`, `tag_accuracy`, `tag_customer_issue_classification`, `tag_issue_severity`, `tag_code_status`, `tag_business_domain`, `tag_event_clarity`, `tag_high_value_flag`, `tag_complaint_flag_needing_reply`, `tag_complaint_flag_needing_prtv_msg`, `tag_high_quality_voc_flag`, `tag_new_energy_or_fuel`, `tag_need_forvclosed_loop`, `topic`, `topic_text`, `opinion`, `cpt_tag_first_code`, `cpt_tag_second_code`, `cpt_tag_three_code`, `cpt_tag_four_code`, `cpt_tag_first`, `cpt_tag_second`, `cpt_tag_three`, `cpt_tag_four`, `ujy_tag_first_code`, `ujy_tag_second_code`, `ujy_tag_three_code`, `ujy_tag_four_code`, `ujy_tag_first`, `ujy_tag_second`, `ujy_tag_three`, `ujy_tag_four`, `cma_tag_first_code`, `cma_tag_second_code`, `cma_tag_three_code`, `cma_tag_four_code`, `cma_tag_first`, `cma_tag_second`, `cma_tag_three`, `cma_tag_four`, `dom_tag_first_code`, `dom_tag_second_code`, `dom_tag_three_code`, `dom_tag_four_code`, `dom_tag_first`, `dom_tag_second`, `dom_tag_three`, `dom_tag_four`, `nps_tag_first_code`, `nps_tag_second_code`, `nps_tag_three_code`, `nps_tag_four_code`, `nps_tag_first`, `nps_tag_second`, `nps_tag_three`, `nps_tag_four`, `vtr_tag_first_code`, `vtr_tag_second_code`, `vtr_tag_three_code`, `vtr_tag_four_code`, `vtr_tag_first`, `vtr_tag_second`, `vtr_tag_three`, `vtr_tag_four`, `abandon`, `insert_dt`
	from `voc_anal_flow_sentiment_annotations_results_mv`
)
SELECT
    f1.*,
    f2.content AS original_text  -- 避免重复写150+字段
FROM result_data f1
         LEFT JOIN (
    -- 仅提取必要字段 + 提前解析JSON
    SELECT
        id,
        JSON_EXTRACT_STRING(raw_data, '$.content') AS content
    FROM `voc_anal_flow_model_tags_result_data_full_mv`
) f2 ON f1.id = f2.id;
;




CREATE MATERIALIZED VIEW voc_anal_flow_sentiment_annotations_results_mv
(id,data_id,channel_catagory,channel_code,channel_name,brand_code,brand_name,car_series_code,car_series_name,model_name,content_type,title,sentiment,intention,data_create_time,publish_time,create_time,update_time,is_outer,hot_word,keywords,original_text_scene,market_id,competitive_type,is_core,series_factory,automark,one_id,user_journey1,user_journey2,user_journey3,usage_scenario_first,usage_scenario_second,d2c_responsible_dept,d2c_accountable_dept,d2c_cc_dept,cust_global_id,cust_classify,cust_main_phone,is_car_owner,cust_age,cust_age_group,cust_name,cust_gender,cust_high_educaion,marrige_statue,family_income,is_exchange_flg,purchase_car_times,is_member_flg,cust_province_code,cust_province,cust_city_code,cust_city,cust_type,cust_lived_prov,cust_lived_city,cust_profession,vhl_vin,vhl_color_name,vhl_product_date,vhl_offline_date,vhl_is_abroad,vhl_dis_ch,vhl_dis_mt,vhl_eng_clsf,vhl_eng_seris,vhl_veh_type,vhl_country,vhl_bd_clsf,vhl_seg_mt,vhl_pow_clsf,vhl_fu_clsf,vhl_modl_st,vhl_std_plnt_code,dlr_oc_id,dlr_oc_code,dlr_oc_name,dlr_oc_province_code,dlr_oc_province,dlr_oc_city_code,dlr_oc_city,dlr_dc_id,dlr_dc_code,dlr_dc_name,dlr_dc_province_code,dlr_dc_province,dlr_dc_city_code,dlr_dc_city,dlr_mc_id,dlr_mc_code,dlr_mc_name,dlr_mc_province_code,dlr_mc_province,dlr_mc_city_code,dlr_mc_city,is_wsater_army,is_manager_focused,is_big_v,author_id,author_nick,is_main_post,original_link,view_count,comment_count,like_count,share_count,favorite_count,work_order_id,quest_id,quest_type,quest_answer_score,quest_business_type,quest_business_scenario,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_event_clarity,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_complaint_flag_needing_prtv_msg,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,tag_sort,topic,topic_text,opinion,cpt_tag_first_code,cpt_tag_second_code,cpt_tag_three_code,cpt_tag_four_code,cpt_tag_first,cpt_tag_second,cpt_tag_three,cpt_tag_four,ujy_tag_first_code,ujy_tag_second_code,ujy_tag_three_code,ujy_tag_four_code,ujy_tag_first,ujy_tag_second,ujy_tag_three,ujy_tag_four,cma_tag_first_code,cma_tag_second_code,cma_tag_three_code,cma_tag_four_code,cma_tag_first,cma_tag_second,cma_tag_three,cma_tag_four,dom_tag_first_code,dom_tag_second_code,dom_tag_three_code,dom_tag_four_code,dom_tag_first,dom_tag_second,dom_tag_three,dom_tag_four,nps_tag_first_code,nps_tag_second_code,nps_tag_three_code,nps_tag_four_code,nps_tag_first,nps_tag_second,nps_tag_three,nps_tag_four,vtr_tag_first_code,vtr_tag_second_code,vtr_tag_three_code,vtr_tag_four_code,vtr_tag_first,vtr_tag_second,vtr_tag_three,vtr_tag_four,abandon,source_data_id,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 60 MINUTE STARTS "2026-04-08 11:55:00"
DUPLICATE KEY(`id`)
PARTITION BY (date_trunc(`publish_time`, 'month'))
DISTRIBUTED BY HASH(`id`) BUCKETS 32
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "brand_code, car_series_code, topic, channel_code",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true",
"excluded_trigger_tables" = "voc_ext_ins_channel_mv,voc_ext_ins_tag_system_final_mv,voc_ext_ins_brand_info_mv,voc_ext_ins_car_series_info_mv"
)
AS with `brand_data` as (
    select `internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`.`code` as `brand_code`,`internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`.`name` as `brand_name`,`internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`.`competitive_type` as `competitive_type` ,
   `internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`.`automark` as `automark`,`internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`.`is_core` from `internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`
),
`car_series_data` as (
    select  `internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`code` as `car_series_code`, `internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`name` as `car_series_name` ,`internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`alias`,`internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`exclusion_words`,`internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`factory`,`internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`competitive_type`,
    `internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`competitive_product` from `internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`
)
select
    `internal`.`voc_ms_td`.`f`.`id`	-- 声音ID
     ,`internal`.`voc_ms_td`.`f`.`data_id`	-- 数据唯一标识
     ,`internal`.`voc_ms_td`.`d4`.`channel_catagory_level1`  as `channel_catagory`	-- 类别（长视频、社交媒体、资讯类等）		优化-渠道数据
     ,`internal`.`voc_ms_td`.`f`.`channel_id`	as `channel_code`-- 渠道编码
     ,`internal`.`voc_ms_td`.`d4`.`name`  as `channel_name`	-- 渠道名称		优化-品牌车系数据
     -- ,`f`.`brand_code`	-- 品牌编码
     ,nvl(nullif(`internal`.`voc_ms_td`.`f`.`brand_code`, ''), null) as `brand_code`	-- 品牌编码
     -- ,`d3`.`brand_name`	-- 品牌名称		优化-品牌车系数据
     ,nvl(nullif(`d3`.`brand_name`, ''), null) as `brand_name`	-- 品牌名称		优化-品牌车系数据
     -- ,`f`.`car_series_code`	-- 车系代码	biz_ext_attrs
     ,nvl(nullif(`internal`.`voc_ms_td`.`f`.`car_series_code`, ''), null) as `car_series_code`	-- 车系代码	biz_ext_attrs
     -- ,`d5`.`car_series_name` -- 车系名称
     ,nvl(nullif(`d5`.`car_series_name`, ''), null) as `car_series_name`	-- 车系名称
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`raw_data`, "$.model") as `model_name`	-- 车型代码	优化（需要和鹏飞确认）
     ,`internal`.`voc_ms_td`.`f`.`content_type`	-- 数据类型(1：咨询/2：意⻅反馈/3：帖⼦评论/4：问卷/5：⼯单)
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`raw_data`, "$.title") as `title`
--      ,`f`.`sentiment`	-- 情感
--      ,`f`.`intention_type` as `intention`	-- 意图（表扬/建议/咨询/抱怨)）
     ,ifnull(`internal`.`voc_ms_td`.`f`.`sentiment`,`internal`.`voc_ms_td`.`d6`.`emotion`) as `sentiment`        -- 情感
     ,ifnull(`internal`.`voc_ms_td`.`f`.`intention_type`,`internal`.`voc_ms_td`.`d6`.`intention`) as `intention`        -- 意图（表扬/建议/咨询/抱怨)）
     ,to_date(`internal`.`voc_ms_td`.`f`.`publish_time`) as `data_create_time`	-- 数据产生时间
     ,`internal`.`voc_ms_td`.`f`.`publish_time`
     ,`internal`.`voc_ms_td`.`f`.`create_time`	-- 数据抓取时间
     ,`internal`.`voc_ms_td`.`f`.`update_time`
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`raw_data`, "$.is_outer") AS `is_outer`	-- 内外数据？
     ,null as `hot_word`	-- 热词   优化（模型无返回值）
     ,`internal`.`voc_ms_td`.`f`.`keywords`	-- 关键词
     ,`internal`.`voc_ms_td`.`f`.`original_text_scene`  -- 声音片段
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`raw_data`, "$.market_id") as `market_id`	-- 细分市场ID
     -- ,JSON_EXTRACT_STRING(f.biz_ext_attrs2, "$.competitive_type") as competitive_type -- 竞争力类型（1：传统 Competitive、2：新兴 Competitive、3：其他 Competitive）
     -- ,JSON_EXTRACT_STRING(f.biz_ext_attrs2, "$.series_factory") as series_factory -- 车系所属企业
     ,`d3`.`competitive_type` as `competitive_type`-- 车系所属企业
     ,`d3`.`is_core` as `is_core`
     ,`d5`.`factory` as `series_factory`-- 车系所属企业
     ,`d3`.`automark` as `automark`-- 车系所属企业
     ,ifnull(`internal`.`voc_ms_td`.`f`.`one_id`, concat('n_',md5(concat(`internal`.`voc_ms_td`.`f`.`channel_id`
    ,ifnull(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`raw_data`, "$.user_name"), '')
    ,ifnull(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`raw_data`, "$.user_id"), '') )))) as `one_id`	-- oneId	代入
     ,`internal`.`voc_ms_td`.`d6`.`user_journey1` as `user_journey1`	-- 旅程维度1（看车、购车等）	优化-标签数据
     ,`internal`.`voc_ms_td`.`d6`.`user_journey2` as `user_journey2`	-- 旅程维度2（高速路、高原等）	优化-标签数据
     ,`internal`.`voc_ms_td`.`d6`.`user_journey3` as `user_journey3`
     -- ,f.scenario	-- 关注场景领域（舒适性/材质/异响）
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`ext_fields`, "$.usage_scenario_first") as `usage_scenario_first`
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`ext_fields`, "$.usage_scenario_second") as `usage_scenario_second`
     ,`internal`.`voc_ms_td`.`d6`.`d2c_responsible_dept` as `d2c_responsible_dept`	-- 主责部门	代入		优化-标签数据
     ,`internal`.`voc_ms_td`.`d6`.`d2c_accountable_dept` as `d2c_accountable_dept`	-- 责任部门	代入		优化-标签数据
     ,`internal`.`voc_ms_td`.`d6`.`d2c_cc_dept`  as `d2c_cc_dept`	-- 抄送部门	代入				优化-标签数据
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.global_id") as `cust_global_id`	--	sso全局ID
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.cust_classify") as `cust_classify`	--	客户类型
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.mobile") as `cust_main_phone`	--	主手机号
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.is_car_owner_flg") as `is_car_owner`	--	是否车主：Y、N
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.age") as `cust_age`	--	客户年龄
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.age_group") as `cust_age_group`	--	年龄段
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`raw_data`, "$.user_name") as `cust_name`	--	客户姓名
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.gender") as `cust_gender`	--	客户性别(男/女）
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.high_educaion") as `cust_high_educaion`	--	最高学历
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.marriage_statue") as `marrige_statue`	--	婚姻状况
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.family_income") as `family_income`	--	家庭月收入
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.is_exchange_flg") as `is_exchange_flg`	--	是否换购
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.purchase_car_times") as `purchase_car_times`	--	购车次数
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.is_member_flg") as `is_member_flg`	--	是否会员
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.hukou_prov_cd") as `cust_province_code`	--	客户常驻省份编码/户籍地
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.hukou_prov_nm") as `cust_province`	--	客户常驻省份/户籍地
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.hukou_city_cd") as `cust_city_code`	--	客户常驻市编码/户籍地
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.hukou_city_nm") as `cust_city`	--	客户常驻市/户籍地
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.cust_type") as `cust_type`	--	客户分类
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.lived_prov_nm") as `cust_lived_prov`	--	居住地-省份    -- 优化  区域数据
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.lived_city_nm") as `cust_lived_city`	--	居住地-城市    -- 优化  区域数据
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`, "$.profession") as `cust_profession`	--	职业
     ,ifnull(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`, "$.vin"),
             JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`raw_data`, "$.vhl_vin")) as `vhl_vin`	-- 车辆车架号
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`, "$.col_name") as `vhl_color_name`		-- 颜色名称
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`, "$.product_date") as `vhl_product_date`		-- 生产日期
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`, "$.offline_date") as `vhl_offline_date`		-- 出厂日期
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`, "$.home_abroad") as `vhl_is_abroad`		-- 国内国外
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`, "$.dis_ch") as `vhl_dis_ch`		-- 排放
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`, "$.dis_mt") as `vhl_dis_mt`		-- 排量
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`, "$.eng_clsf") as `vhl_eng_clsf`		-- 动力系列大类
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`, "$.eng_seris") as `vhl_eng_seris`		-- 动力系列小类
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`, "$.veh_type") as `vhl_veh_type`		-- 车辆类型（出口车、领用车、代工车、商用车）
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`prd_ext_attrs`, "$.country") as `vhl_country`		-- 国别
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`prd_ext_attrs`, "$.bd_clsf") as `vhl_bd_clsf`		-- 车身类型
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`prd_ext_attrs`, "$.seg_mt") as `vhl_seg_mt`		-- 细分市场
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`prd_ext_attrs`, "$.pow_clsf") as `vhl_pow_clsf`		-- 动力类型
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`prd_ext_attrs`, "$.fu_clsf") as `vhl_fu_clsf`		-- 燃料类型
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`prd_ext_attrs`, "$.modl_st") as `vhl_modl_st`		-- 车型状态号
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`, "$.plnt_code") as `vhl_std_plnt_code`		-- 标准工厂编码
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.sk_id"		) as `dlr_oc_id`               	-- 订单中心-经销商ID
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.dlr_cd") as `dlr_oc_code`             	-- 订单中心-经销商编码
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.dlr_nm") as `dlr_oc_name`             	-- 订单中心-经销商全称
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.prov_cd") as `dlr_oc_province_code`    -- 	订单中心-经销商所在省编码
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.prov_nm") as `dlr_oc_province`         -- 	订单中心-经销商所在省
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.city_cd") as `dlr_oc_city_code`        -- 	订单中心-经销商所在市编码
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.city_nm") as `dlr_oc_city`             -- 	订单中心-经销商所在市
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.sk_id") as `dlr_dc_id`               	-- 	交付中心-经销商ID
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.dlr_cd") as `dlr_dc_code`             -- 	交付中心-经销商编码
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.dlr_nm") as `dlr_dc_name`             -- 	交付中心-经销商全称
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.prov_cd") as `dlr_dc_province_code`    -- 	交付中心-经销商所在省编码
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.prov_nm") as `dlr_dc_province`         -- 	交付中心-经销商所在省
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.city_cd") as `dlr_dc_city_code`        -- 	交付中心-经销商所在市编码
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.city_nm") as `dlr_dc_city`             -- 	交付中心-经销商所在市
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.sk_id") as `dlr_mc_id`               --    	维保中心-经销商ID
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.dlr_cd") as `dlr_mc_code`             -- 	维保中心-经销商编码
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.dlr_nm") as `dlr_mc_name`             -- 	维保中心-经销商全称
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.prov_cd") as `dlr_mc_province_code`    -- 	维保中心-经销商所在省编码
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.prov_nm") as `dlr_mc_province`         -- 	维保中心-经销商所在省
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.city_cd") as `dlr_mc_city_code`        -- 	维保中心-经销商所在市编码
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`, "$.city_nm") as `dlr_mc_city`             -- 	维保中心-经销商所在市
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`raw_data`, "$.is_wsater_army") as `is_wsater_army`	-- 是否水军
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.is_manager_focused") as `is_manager_focused`	-- 是否领导重点关注
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.is_big_v") as `is_big_v`	-- 是否KOC账号
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.author_id") as `author_id`	-- 作者账号
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.author_nick") as `author_nick`	-- 作者昵称
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.is_main_post") as `is_main_post`	-- 是否主贴(Y/N)
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.url") as `original_link`	-- 帖子原文链接
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.view_count") as `view_count`	-- 浏览量or播放量
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.comment_count") as `comment_count`	-- 评论量
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.like_count") as `like_count`	-- 点赞量
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.share_count") as `share_count`	-- 转发量
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.favorite_count") as `favorite_count` 	-- 收藏量
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.order_id") as `work_order_id`	-- 工单ID
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.quest_id") as `quest_id`	-- 问卷ID
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.quest_type") as `quest_type`	-- 问卷类型
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.quest_answer_score") as `quest_answer_score`	-- 问卷答案分数
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.quest_business_type") as `quest_business_type`	-- 问卷业务类型
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.quest_business_scenario") as `quest_business_scenario`	-- 问卷业务场景
     ,`internal`.`voc_ms_td`.`d6`.`tag_accuracy` as `tag_accuracy`	-- 代码的精准性（精准、有待提升等）	标签
     ,`internal`.`voc_ms_td`.`d6`.`tag_customer_issue_classification` as `tag_customer_issue_classification`	-- 客户问题分级（S、A、B、C等）	标签
     ,`internal`.`voc_ms_td`.`d6`.`tag_issue_severity` as `tag_issue_severity`	-- 问题程度（高、中、低）	标签
     ,`internal`.`voc_ms_td`.`d6`.`tag_code_status` as `tag_code_status`	-- 代码状态（有效、无效等）	标签
     ,`internal`.`voc_ms_td`.`d6`.`tag_business_domain` as `tag_business_domain`	-- 业务领域（产品质量、产品设计、服务体验）	标签
     ,`internal`.`voc_ms_td`.`d6`.`event_clarity` as `tag_event_clarity`	-- 事件清晰度（印象、事实）	标签
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`ext_fields`, "$.tag_high_value_flag") as `tag_high_value_flag`	-- 需推送的高价值建议标识	手动
     ,`internal`.`voc_ms_td`.`d6`.`tag_complaint_flag_needing_reply` as `tag_complaint_flag_needing_reply`	-- 需回评的抱怨标识	标签
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`ext_fields`, "$.tag_complaint_flag_needing_prtv_msg") as `tag_complaint_flag_needing_prtv_msg`	-- 需私信的抱怨标识	手动
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`ext_fields`, "$.tag_high_quality_voc_flag") as `tag_high_quality_voc_flag`	-- 针对五级明细高质量VOC标识	手动
     ,`internal`.`voc_ms_td`.`d6`.`tag_new_energy_or_fuel` as `tag_new_energy_or_fuel`	-- 新能源特有/燃油特有	标签
     ,`internal`.`voc_ms_td`.`d6`.`tag_need_forvclosed_loop` as `tag_need_forvclosed_loop`	-- 批量问题是否需要闭环（短平快、通用等）	标签
     ,`internal`.`voc_ms_td`.`d6`.`sort` as `tag_sort`
     ,`internal`.`voc_ms_td`.`f`.`topic`	 -- 	观点（根因标签ID）
     ,`internal`.`voc_ms_td`.`d6`.`topic_text` as `topic_text` -- 观点
     ,`internal`.`voc_ms_td`.`f`.`opinion` -- 	原始观点
     ,`internal`.`voc_ms_td`.`d6`.`cpt_tag_first_code` as `cpt_tag_first_code` -- CPT签1级编码
     ,`internal`.`voc_ms_td`.`d6`.`cpt_tag_second_code` as `cpt_tag_second_code` -- CPT签2级编码
     ,`internal`.`voc_ms_td`.`d6`.`cpt_tag_three_code` as `cpt_tag_three_code` -- CPT签3级编码
     ,`internal`.`voc_ms_td`.`d6`.`cpt_tag_four_code` as `cpt_tag_four_code` -- CPT签4级编码
     ,`internal`.`voc_ms_td`.`d6`.`cpt_tag_first` as `cpt_tag_first` -- CPT签1级
     ,`internal`.`voc_ms_td`.`d6`.`cpt_tag_second` as `cpt_tag_second` -- CPT签2级
     ,`internal`.`voc_ms_td`.`d6`.`cpt_tag_three` as `cpt_tag_three` -- CPT签3级
     ,`internal`.`voc_ms_td`.`d6`.`cpt_tag_four` as `cpt_tag_four` -- CPT签4级
     ,`internal`.`voc_ms_td`.`d6`.`ujy_tag_first_code` as `ujy_tag_first_code` -- 全旅程客户签1级编码
     ,`internal`.`voc_ms_td`.`d6`.`ujy_tag_second_code` as `ujy_tag_second_code` -- 全旅程客户签2级编码
     ,`internal`.`voc_ms_td`.`d6`.`ujy_tag_three_code` as `ujy_tag_three_code` -- 全旅程客户签3级编码
     ,`internal`.`voc_ms_td`.`d6`.`ujy_tag_four_code` as `ujy_tag_four_code` -- 全旅程客户签4级编码
     ,`internal`.`voc_ms_td`.`d6`.`ujy_tag_first` as `ujy_tag_first` -- 全旅程客户签1级
     ,`internal`.`voc_ms_td`.`d6`.`ujy_tag_second` as `ujy_tag_second` -- 全旅程客户签2级
     ,`internal`.`voc_ms_td`.`d6`.`ujy_tag_three` as `ujy_tag_three` -- 全旅程客户签3级
     ,`internal`.`voc_ms_td`.`d6`.`ujy_tag_four` as `ujy_tag_four` -- 全旅程客户签4级
     ,`internal`.`voc_ms_td`.`d6`.`cma_tag_first_code` as `cma_tag_first_code` -- 全领域业务标签1级编码
     ,`internal`.`voc_ms_td`.`d6`.`cma_tag_second_code` as `cma_tag_second_code` -- 全领域业务标签2级编码
     ,`internal`.`voc_ms_td`.`d6`.`cma_tag_three_code` as `cma_tag_three_code` -- 全领域业务标签3级编码
     ,`internal`.`voc_ms_td`.`d6`.`cma_tag_four_code` as `cma_tag_four_code` -- 全领域业务标签4级编码
     ,`internal`.`voc_ms_td`.`d6`.`cma_tag_first` as `cma_tag_first` -- 全领域业务标签1级
     ,`internal`.`voc_ms_td`.`d6`.`cma_tag_second` as `cma_tag_second` -- 全领域业务标签2级
     ,`internal`.`voc_ms_td`.`d6`.`cma_tag_three` as `cma_tag_three` -- 全领域业务标签3级
     ,`internal`.`voc_ms_td`.`d6`.`cma_tag_four` as `cma_tag_four` -- 全领域业务标签4级
     ,`internal`.`voc_ms_td`.`d6`.`dom_tag_first_code` as `dom_tag_first_code` -- 商品化属性标签1级编码
     ,`internal`.`voc_ms_td`.`d6`.`dom_tag_second_code` as `dom_tag_second_code` -- 商品化属性标签2级编码
     ,`internal`.`voc_ms_td`.`d6`.`dom_tag_three_code` as `dom_tag_three_code` -- 商品化属性标签3级编码
     ,`internal`.`voc_ms_td`.`d6`.`dom_tag_four_code` as `dom_tag_four_code` -- 商品化属性标签4级编码
     ,`internal`.`voc_ms_td`.`d6`.`dom_tag_first` as `dom_tag_first` -- 商品化属性标签1级
     ,`internal`.`voc_ms_td`.`d6`.`dom_tag_second` as `dom_tag_second` -- 商品化属性标签2级
     ,`internal`.`voc_ms_td`.`d6`.`dom_tag_three` as `dom_tag_three` -- 商品化属性标签3级
     ,`internal`.`voc_ms_td`.`d6`.`dom_tag_four` as `dom_tag_four` -- 商品化属性标签4级
     ,`internal`.`voc_ms_td`.`d6`.`nps_tag_first_code` as `nps_tag_first_code` -- NPS标签1级编码
     ,`internal`.`voc_ms_td`.`d6`.`nps_tag_second_code` as `nps_tag_second_code` -- NPS标签2级编码
     ,`internal`.`voc_ms_td`.`d6`.`nps_tag_three_code` as `nps_tag_three_code` -- NPS标签3级编码
     ,`internal`.`voc_ms_td`.`d6`.`nps_tag_four_code` as `nps_tag_four_code` -- NPS标签4级编码
     ,`internal`.`voc_ms_td`.`d6`.`nps_tag_first` as `nps_tag_first` -- NPS标签1级
     ,`internal`.`voc_ms_td`.`d6`.`nps_tag_second` as `nps_tag_second` -- NPS标签2级
     ,`internal`.`voc_ms_td`.`d6`.`nps_tag_three` as `nps_tag_three` -- NPS标签3级
     ,`internal`.`voc_ms_td`.`d6`.`nps_tag_four` as `nps_tag_four` -- NPS标签4级
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_first_code` as `vtr_tag_first_code` -- VRT标签1级编码
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_second_code` as `vtr_tag_second_code` -- VRT标签2级编码
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_three_code` as `vtr_tag_three_code` -- VRT标签3级编码
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_four_code` as `vtr_tag_four_code` -- VRT标签4级编码
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_first` as `vtr_tag_first` -- VRT标.签1级
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_second` as `vtr_tag_second` -- VRT标签2级
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_three` as `vtr_tag_three` -- VRT标签3级
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_four` as `vtr_tag_four` -- VRT标签4级
     , ifnull(`internal`.`voc_ms_td`.`f`.`abandon`,'0') as `abandon`
     ,JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`, "$.source_data_id") as `source_data_id`
     , `internal`.`voc_ms_td`.`f`.`insert_dt` as   `insert_dt`
from `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full_mv` as `f`
         left join brand_data as `d3` on `internal`.`voc_ms_td`.`f`.`brand_code` = `d3`.`brand_code`
         left join `internal`.`voc_ms_td`.`voc_ext_ins_channel_mv` as `d4` on `internal`.`voc_ms_td`.`f`.`channel_id` = `internal`.`voc_ms_td`.`d4`.`code`
         left join car_series_data as `d5` on `internal`.`voc_ms_td`.`f`.`car_series_code` = `d5`.`car_series_code`
         left join `internal`.`voc_ms_td`.`voc_ext_ins_tag_system_final_mv` as `d6` ON `internal`.`voc_ms_td`.`f`.`topic` = `internal`.`voc_ms_td`.`d6`.`topic`








CREATE MATERIALIZED VIEW voc_imp_dealer_json_info_mv
(dealer_code,dealer_json,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 30 MINUTE STARTS "2025-11-14 11:00:00"
DUPLICATE KEY(`dealer_code`)
DISTRIBUTED BY HASH(`dealer_code`) BUCKETS 8
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "dealer_code",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728"
)
AS with `raw_data` as (
	select `f`.`rn`, `f`.`sk_id`, `f`.`dlr_cd`, `f`.`erp_cd`, `f`.`dlr_nm`, `f`.`dlr_s_nm`, `f`.`dept_cd`, `f`.`dept_nm`, `f`.`seq`, `f`.`store_lvl`, `f`.`invest_group_cd`, `f`.`invest_group_nm`, `f`.`invest_cd`, `f`.`invest_nm`, `f`.`s1_dlr_cd`, `f`.`s1_dlr_nm`, `f`.`s2_dlr_cd`, `f`.`s2_dlr_nm`, `f`.`main_invest_cd`, `f`.`main_invest_nm`, `f`.`main_dlr_cd`, `f`.`main_dlr_nm`, `f`.`ord_flg`, `f`.`dlv_flg`, `f`.`svs_flg`, `f`.`paint_flg`, `f`.`valid_flg`, `f`.`quit_flg`, `f`.`rez_flg`, `f`.`dlr_type_group_cd`, `f`.`dlr_type_group_nm`, `f`.`dlr_type_cd`, `f`.`dlr_type_nm`, `f`.`chn_type_cd`, `f`.`chn_type_nm`, `f`.`chn_lvl_cd`, `f`.`chn_lvl_nm`, `f`.`image_cd`, `f`.`image_nm`, `f`.`image_lvl_cd`, `f`.`image_lvl_nm`, `f`.`store_front_function`, `f`.`current_state`, `f`.`operation_type`, `f`.`area`, `f`.`war_zone_cd`, `f`.`war_zone_nm`, `f`.`war_zone_part`, `f`.`war_zone_part_user_cd`, `f`.`war_zone_part_user_nm`, `f`.`sdu`, `f`.`sdu_link_man`, `f`.`prov_cd`, `f`.`city_cd`, `f`.`cty_cd`, `f`.`prov_nm`, `f`.`city_nm`, `f`.`cty_nm`, `f`.`lng`, `f`.`lat`, `f`.`addr`, `f`.`bank_nm`, `f`.`bank_acct_no`, `f`.`manager_nm`, `f`.`manager_tel`, `f`.`hotline`, `f`.`emergency_tel`, `f`.`in_net_dt`, `f`.`out_net_dt`, `f`.`network_type`, `f`.`ord_dlr_cd`, `f`.`dlv_dlr_cd`, `f`.`svs_dlr_cd`, `f`.`biz_cd`, `f`.`biz_nm`, `f`.`uni_dlr_cd_flg`, `f`.`src_sys`, `f`.`src_sys_id`, `f`.`zip_start_dt`, `f`.`zip_end_dt`, `f`.`zip_enable_flg`, `f`.`insert_dt`, `f`.`job_nm`, `f`.`ds` from (
		select
			ROW_NUMBER() OVER (PARTITION BY `dlr_cd` ORDER BY `zip_start_dt` DESC) AS `rn`
			,`sk_id`, `dlr_cd`, `erp_cd`, `dlr_nm`, `dlr_s_nm`, `dept_cd`, `dept_nm`, `seq`, `store_lvl`, `invest_group_cd`, `invest_group_nm`, `invest_cd`, `invest_nm`, `s1_dlr_cd`, `s1_dlr_nm`, `s2_dlr_cd`, `s2_dlr_nm`, `main_invest_cd`, `main_invest_nm`, `main_dlr_cd`, `main_dlr_nm`, `ord_flg`, `dlv_flg`, `svs_flg`, `paint_flg`, `valid_flg`, `quit_flg`, `rez_flg`, `dlr_type_group_cd`, `dlr_type_group_nm`, `dlr_type_cd`, `dlr_type_nm`, `chn_type_cd`, `chn_type_nm`, `chn_lvl_cd`, `chn_lvl_nm`, `image_cd`, `image_nm`, `image_lvl_cd`, `image_lvl_nm`, `store_front_function`, `current_state`, `operation_type`, `area`, `war_zone_cd`, `war_zone_nm`, `war_zone_part`, `war_zone_part_user_cd`, `war_zone_part_user_nm`, `sdu`, `sdu_link_man`, `prov_cd`, `city_cd`, `cty_cd`, `prov_nm`, `city_nm`, `cty_nm`, `lng`, `lat`, `addr`, `bank_nm`, `bank_acct_no`, `manager_nm`, `manager_tel`, `hotline`, `emergency_tel`, `in_net_dt`, `out_net_dt`, `network_type`, `ord_dlr_cd`, `dlv_dlr_cd`, `svs_dlr_cd`, `biz_cd`, `biz_nm`, `uni_dlr_cd_flg`, `src_sys`, `src_sys_id`, `zip_start_dt`, `zip_end_dt`, `zip_enable_flg`, `insert_dt`, `job_nm`, `ds`
		from `voc_imp_hudi_dim_chn_dlr_zip_d_full`
	) `f`
	WHERE `f`.`rn` = 1
)
select
    `c`.`dlr_cd` as `dealer_code`
     ,JSON_OBJECT(
        'sk_id',`c`.`sk_id`,
        'dlr_cd',`c`.`dlr_cd`,
        'erp_cd',`c`.`erp_cd`,
        'dlr_nm',`c`.`dlr_nm`,
        'dlr_s_nm',`c`.`dlr_s_nm`,
        'dept_cd',`c`.`dept_cd`,
        'dept_nm',`c`.`dept_nm`,
        'seq',`c`.`seq`,
        'store_lvl',`c`.`store_lvl`,
        'invest_group_cd',`c`.`invest_group_cd`,
        'invest_group_nm',`c`.`invest_group_nm`,
        'invest_cd',`c`.`invest_cd`,
        'invest_nm',`c`.`invest_nm`,
        's1_dlr_cd',`c`.`s1_dlr_cd`,
        's1_dlr_nm',`c`.`s1_dlr_nm`,
        's2_dlr_cd',`c`.`s2_dlr_cd`,
        's2_dlr_nm',`c`.`s2_dlr_nm`,
        'main_invest_cd',`c`.`main_invest_cd`,
        'main_invest_nm',`c`.`main_invest_nm`,
        'main_dlr_cd',`c`.`main_dlr_cd`,
        'main_dlr_nm',`c`.`main_dlr_nm`,
        'ord_flg',`c`.`ord_flg`,
        'dlv_flg',`c`.`dlv_flg`,
        'svs_flg',`c`.`svs_flg`,
        'paint_flg',`c`.`paint_flg`,
        'valid_flg',`c`.`valid_flg`,
        'quit_flg',`c`.`quit_flg`,
        'rez_flg',`c`.`rez_flg`,
        'dlr_type_group_cd',`c`.`dlr_type_group_cd`,
        'dlr_type_group_nm',`c`.`dlr_type_group_nm`,
        'dlr_type_cd',`c`.`dlr_type_cd`,
        'dlr_type_nm',`c`.`dlr_type_nm`,
        'chn_type_cd',`c`.`chn_type_cd`,
        'chn_type_nm',`c`.`chn_type_nm`,
        'chn_lvl_cd',`c`.`chn_lvl_cd`,
        'chn_lvl_nm',`c`.`chn_lvl_nm`,
        'image_cd',`c`.`image_cd`,
        'image_nm',`c`.`image_nm`,
        'image_lvl_cd',`c`.`image_lvl_cd`,
        'image_lvl_nm',`c`.`image_lvl_nm`,
        'store_front_function',`c`.`store_front_function`,
        'current_state',`c`.`current_state`,
        'operation_type',`c`.`operation_type`,
        'area',`c`.`area`,
        'war_zone_cd',`c`.`war_zone_cd`,
        'war_zone_nm',`c`.`war_zone_nm`,
        'war_zone_part',`c`.`war_zone_part`,
        'war_zone_part_user_cd',`c`.`war_zone_part_user_cd`,
        'war_zone_part_user_nm',`c`.`war_zone_part_user_nm`,
        'sdu',`c`.`sdu`,
        'sdu_link_man',`c`.`sdu_link_man`,
        'prov_cd',`c`.`prov_cd`,
        'city_cd',`c`.`city_cd`,
        'cty_cd',`c`.`cty_cd`,
        'prov_nm',`c`.`prov_nm`,
        'city_nm',`c`.`city_nm`,
        'cty_nm',`c`.`cty_nm`,
        'lng',`c`.`lng`,
        'lat',`c`.`lat`,
        'addr',`c`.`addr`,
        'bank_nm',`c`.`bank_nm`,
        'bank_acct_no',`c`.`bank_acct_no`,
        'manager_nm',`c`.`manager_nm`,
        'manager_tel',`c`.`manager_tel`,
        'hotline',`c`.`hotline`,
        'emergency_tel',`c`.`emergency_tel`,
        'in_net_dt',`c`.`in_net_dt`,
        'out_net_dt',`c`.`out_net_dt`,
        'network_type',`c`.`network_type`,
        'ord_dlr_cd',`c`.`ord_dlr_cd`,
        'dlv_dlr_cd',`c`.`dlv_dlr_cd`,
        'svs_dlr_cd',`c`.`svs_dlr_cd`,
        'biz_cd',`c`.`biz_cd`,
        'biz_nm',`c`.`biz_nm`,
        'uni_dlr_cd_flg',`c`.`uni_dlr_cd_flg`,
        'src_sys',`c`.`src_sys`,
        'src_sys_id',`c`.`src_sys_id`,
        'zip_start_dt',`c`.`zip_start_dt`,
        'zip_end_dt',`c`.`zip_end_dt`,
        'zip_enable_flg',`c`.`zip_enable_flg`,
        'ds',`c`.`ds`
      ) as `dealer_json`
     ,`c`.`insert_dt`
from raw_data  as  `c`;



CREATE MATERIALIZED VIEW voc_imp_cust_json_b_by_vin_mv
(vin,cust_json,vehicle_json,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-23 06:11:00"
DUPLICATE KEY(`vin`)
DISTRIBUTED BY HASH(`vin`) BUCKETS 12
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "insert_dt",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS select distinct `vin` ,`cust_json`, `vehicle_json`,now() as `insert_dt`
   from `voc_imp_cust_vehicle_rel_json_info_mv`
   where `vin` is not null and `vin` <> '';






drop  MATERIALIZED VIEW voc_anal_flow_mate_data_full_mv_v2;
CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_full_mv_v2
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 30 MINUTE STARTS "2026-02-25 20:25:00"
DUPLICATE KEY(`id`)
PARTITION BY (date_trunc(`create_time`, 'month'))
DISTRIBUTED BY HASH(`id`) BUCKETS 64
PROPERTIES (
	"replication_allocation" = "tag.location.default: 3",
	"min_load_replica_num" = "-1",
	"bloom_filter_columns" = "data_id, data_create_time, channel_code, brand",
	"is_being_synced" = "false",
	"storage_medium" = "hdd",
	"storage_format" = "V2",
	"inverted_index_storage_format" = "V1",
	"light_schema_change" = "true",
	"disable_auto_compaction" = "false",
	"enable_single_replica_compaction" = "false",
	"group_commit_interval_ms" = "10000",
	"group_commit_data_bytes" = "134217728",
	"enable_nondeterministic_function" = "true"
)
AS
with `result_data` as (
   select distinct `data_id`
   FROM `voc_anal_flow_mate_data_labeled_mv`
)
SELECT
    t1.`id`,
    t1.`data_id`,
    -- 一次性解析 JSON 字段，避免重复调用函数
    t1.`create_time`,
    content_type as `content_type`,
    date(t1.`publish_time`) as `data_create_time`,
    JSON_EXTRACT_STRING(t1.`data`, "$.data_update_time") as `data_update_time`,
    channel_id as `channel_code`,
    JSON_EXTRACT_STRING(t1.`data`, "$.brand") as `brand`,
    JSON_EXTRACT_STRING(t1.`data`, "$.series") as `series`,
    JSON_EXTRACT_STRING(t1.`data`, "$.model") as `model`,
    JSON_EXTRACT_STRING(t1.`data`, "$.is_outer") as `is_outer`,
    JSON_EXTRACT_STRING(t1.`data`, "$.one_id") as `one_id`,
    JSON_EXTRACT_STRING(t1.`data`, "$.id_car_no") as `id_car_no`,
    JSON_EXTRACT_STRING(t1.`data`, "$.mobile") as `mobile`,
    JSON_EXTRACT_STRING(t1.`data`, "$.email") as `email`,
    JSON_EXTRACT_STRING(t1.`data`, "$.global_id") as `global_id`,
    JSON_EXTRACT_STRING(t1.`data`, "$.user_id") as `user_id`,
    JSON_EXTRACT_STRING(t1.`data`, "$.user_name") as `user_name`,
    JSON_EXTRACT_STRING(t1.`data`, "$.vhl_id") as `vhl_id`,
    JSON_EXTRACT_STRING(t1.`data`, "$.vhl_vin") as `vhl_vin`,
    JSON_EXTRACT_STRING(t1.`data`, "$.dlr_id") as `dlr_id`,
    JSON_EXTRACT_STRING(t1.`data`, "$.dlr_code") as `dlr_code`,
    JSON_EXTRACT_STRING(t1.`data`, "$.dlr_type") as `dlr_type`,
    JSON_EXTRACT_STRING(t1.`data`, "$.market_id") as `market_id`,
    JSON_EXTRACT_STRING(t1.`data`, "$.title") as `title`,
    JSON_EXTRACT_STRING(t1.`data`, "$.content") as `content`,
    JSON_EXTRACT_STRING(t1.`data`, "$.model_type") as `model_type`,
    JSON_EXTRACT_STRING(t1.`data`, "$.is_wsater_army") as `is_wsater_army`,
    JSON_EXTRACT_STRING(t1.`cust_ext_attrs`, "$.gender") as `cust_gender`,
    JSON_EXTRACT_STRING(t1.`cust_ext_attrs`, "$.is_car_owner_flg") as `is_car_owner`,
    JSON_EXTRACT_STRING(t1.`cust_ext_attrs`, "$.hukou_prov_cd") as `cust_province_code`,
    JSON_EXTRACT_STRING(t1.`biz_ext_attrs2`, "$.is_manager_focused") as `is_manager_focused`,
    JSON_EXTRACT_STRING(t1.`biz_ext_attrs2`, "$.is_big_v") as `is_big_v`,
    JSON_EXTRACT_STRING(t1.`biz_ext_attrs2`, "$.is_main_post") as `is_main_post`,
    JSON_EXTRACT_STRING(t1.`biz_ext_attrs2`, "$.order_id") as `order_id`,
    JSON_EXTRACT_STRING(t1.`biz_ext_attrs2`, "$.quest_id") as `quest_id`,
    JSON_EXTRACT_STRING(t1.`biz_ext_attrs2`, "$.quest_type") as `quest_type`,
    '0' as `weight`,
    t1.`work_id`,
    t1.`done`,
    null as `ds`,
    case
    when `f2`.`data_id` is not null  then 3
    else 2
end as `data_status`
FROM `voc_anal_flow_mate_data_full` t1
INNER JOIN (
    SELECT DISTINCT `data_id`
    FROM `voc_anal_flow_mate_data_labeled_mv`
) t2 ON t1.`data_id` = t2.`data_id`
left join result_data as `f2` on `t1`.`data_id` = `f2`.`data_id`
    ;



-- drop MATERIALIZED VIEW voc_anal_flow_mate_data_full_mv
CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_full_mv
(id,create_time,content_type,data_create_time,data_update_time,data_id,channel_code,brand,series,model,is_outer,one_id,id_car_no,mobile,email,global_id,user_id,user_name,vhl_id,vhl_vin,dlr_id,dlr_code,dlr_type,market_id,title,content,is_wsater_army,weight,attrs,attrs2,attrs3,work_id,done,model_type,ds,data_status)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-30 11:25:00"
DUPLICATE KEY(`id`)
PARTITION BY (date_trunc(`create_time`, 'month'))
DISTRIBUTED BY HASH(`id`) BUCKETS 64
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "data_id, data_create_time, channel_code, brand",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS
with `raw_data` as (

	select
    `id`,
    `data_id`,
    JSON_EXTRACT_STRING(`data`, "$.data_update_time") as `data_update_time`,
    `create_time`,
    JSON_EXTRACT_STRING(`data`, "$.content_type") as `content_type`,
    `publish_time` as `data_create_time`,
    JSON_EXTRACT_STRING(`data`, "$.channel_code") as `channel_code`,
    JSON_EXTRACT_STRING(`data`, "$.brand") as `brand`,
    JSON_EXTRACT_STRING(`data`, "$.series") as `series`,
    JSON_EXTRACT_STRING(`data`, "$.model") as `model`,
    JSON_EXTRACT_STRING(`data`, "$.is_outer") as `is_outer`,
    JSON_EXTRACT_STRING(`data`, "$.one_id") as `one_id`,
    JSON_EXTRACT_STRING(`data`, "$.id_car_no") as `id_car_no`,
    JSON_EXTRACT_STRING(`data`, "$.mobile") as `mobile`,
    JSON_EXTRACT_STRING(`data`, "$.email") as `email`,
    JSON_EXTRACT_STRING(`data`, "$.global_id") as `global_id`,
    JSON_EXTRACT_STRING(`data`, "$.user_id") as `user_id`,
    JSON_EXTRACT_STRING(`data`, "$.user_name") as `user_name`,
    JSON_EXTRACT_STRING(`data`, "$.vhl_id") as `vhl_id`,
    JSON_EXTRACT_STRING(`data`, "$.vhl_vin") as `vhl_vin`,
    JSON_EXTRACT_STRING(`data`, "$.dlr_id") as `dlr_id`,
    JSON_EXTRACT_STRING(`data`, "$.dlr_code") as `dlr_code`,
    JSON_EXTRACT_STRING(`data`, "$.dlr_type") as `dlr_type`,
    JSON_EXTRACT_STRING(`data`, "$.market_id") as `market_id`,
    JSON_EXTRACT_STRING(`data`, "$.title") as `title`,
    JSON_EXTRACT_STRING(`data`, "$.content") as `content`,
    JSON_EXTRACT_STRING(`data`, "$.is_wsater_army") as `is_wsater_army`,
    '0' as `weight`,
    `biz_ext_attrs` as `attrs`,
    `biz_ext_attrs2` as `attrs2`,
    `biz_ext_attrs3` as `attrs3`,
    `work_id`,
    `done`,
    JSON_EXTRACT_STRING(`data`, "$.model_type") as `model_type`,
    null as `ds`,
    `insert_dt`
from
    `voc_anal_flow_mate_data_full`

),
`result_data` as (
   select distinct `voc_anal_flow_mate_data_labeled_mv`.`data_id`
   FROM `voc_anal_flow_mate_data_labeled_mv`
)

select
    `f1`.`id`,
    `f1`.`create_time`,
    `f1`.`content_type`,
    `f1`.`data_create_time`,
    `f1`.`data_update_time`,
    `f1`.`data_id`,
    `f1`.`channel_code`,
    `f1`.`brand`,
    `f1`.`series`,
    `f1`.`model`,
    `f1`.`is_outer`,
    `f1`.`one_id`,
    `f1`.`id_car_no`,
    `f1`.`mobile`,
    `f1`.`email`,
    `f1`.`global_id`,
    `f1`.`user_id`,
    `f1`.`user_name`,
    `f1`.`vhl_id`,
    `f1`.`vhl_vin`,
    `f1`.`dlr_id`,
    `f1`.`dlr_code`,
    `f1`.`dlr_type`,
    `f1`.`market_id`,
    `f1`.`title`,
    `f1`.`content`,
    `f1`.`is_wsater_army`,
    `f1`.`weight`,
    `f1`.`attrs`,
    `f1`.`attrs2`,
    `f1`.`attrs3`,
    `f1`.`work_id`,
    `f1`.`done`,
    `f1`.`model_type`,
    `f1`.`ds`,
    case
        when `f2`.`data_id` is not null  then 3
        else 2
        end as `data_status`
from raw_data as `f1`
         left join result_data as `f2` on `f1`.`data_id` = `f2`.`data_id`;








CREATE MATERIALIZED VIEW voc_imp_hudi_dm_voc_cust_vehicle_rel_mv
(vin,idcard)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-24 05:02:00"
DUPLICATE KEY(`vin`)
DISTRIBUTED BY HASH(`vin`) BUCKETS 8
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "idcard, vin",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728"
)
AS SELECT `vin`, `idcard`
   FROM `voc_imp_hudi_dm_voc_cust_vehicle_rel`
   WHERE `idcard` IS NOT NULL AND `vin` IS NOT null;









-- drop MATERIALIZED VIEW voc_anal_flow_mate_data_labeled_mv2;
CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_labeled_mv
(id,data_id,data_update_time,create_time,content_type,data_create_time,channel_code,brand,series,model,is_outer,one_id,id_car_no,mobile,email,global_id,user_id,user_name,vhl_id,vhl_vin,dlr_id,dlr_code,dlr_type,market_id,title,content,is_wsater_army,weight,attrs,attrs2,attrs3,work_id,done,model_type,ds,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-30 11:40:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 32
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "data_id, channel_code, done",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS
SELECT
    `t`.`id`,
    `t`.`data_id`,
    -- 优化：JSON解析函数添加默认值，避免空值导致刷新失败
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.data_update_time"), '') as `data_update_time`,
    `t`.`create_time`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.content_type"), '') as `content_type`,
    `t`.`publish_time` AS `data_create_time`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.channel_code"), '') AS `channel_code`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.brand"), '') AS `brand`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.series"), '') AS `series`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.model"), '') AS `model`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.is_outer"), '') AS `is_outer`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.one_id"), '') AS `one_id`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.id_car_no"), '') AS  `id_car_no`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.mobile"), '') AS `mobile`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.email"), '') AS `email`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.global_id"), '') AS `global_id`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.user_id"), '') AS `user_id`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.user_name"), '') AS `user_name`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.vhl_id"), '') AS `vhl_id`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.vhl_vin"), '') AS `vhl_vin`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.dlr_id"), '') AS `dlr_id`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.dlr_code"), '') AS `dlr_code`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.dlr_type"), '') AS `dlr_type`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.market_id"), '') AS `market_id`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.title"), '') AS `title`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.content"), '') AS `content`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.is_wsater_army"), '') as `is_wsater_army`,
    '0' AS `weight`,
    `t`.`biz_ext_attrs` AS `attrs`,
    `t`.`biz_ext_attrs2` AS `attrs2`,
    `t`.`biz_ext_attrs3` AS `attrs3`,
    `t`.`work_id`,
    `t`.`done`,
    COALESCE(JSON_EXTRACT_STRING(`t`.`raw_data`, "$.model_type"), '') as`model_type`,
    NULL as `ds`,
    NOW() as `insert_dt`
FROM `voc_anal_flow_model_tags_result_data_full_mv` `t`
-- 替代原自连接：按 data_id 分组取最大 id，消除冗余数据
WHERE `t`.`id` IN (
    SELECT MAX(id)
    FROM `voc_anal_flow_model_tags_result_data_full_mv`
    GROUP BY data_id
)







CREATE MATERIALIZED VIEW voc_imp_hudi_dwd_maf_veh_d_full_mv
(vin,col_name,product_date,offline_date,home_abroad,dis_ch,dis_mt,eng_clsf,eng_seris,veh_type,plnt_code)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-24 05:01:00"
DUPLICATE KEY(`vin`)
DISTRIBUTED BY HASH(`vin`) BUCKETS 8
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "vin",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728"
)
AS SELECT
              `vin`, `col_name`, `product_date`, `offline_date`,
              `home_abroad`, `dis_ch`, `dis_mt`, `eng_clsf`, `eng_seris`,
              `veh_type`, `plnt_code`
   FROM `voc_imp_hudi_dwd_maf_veh_d_full`
   WHERE `vin` IS NOT NULL;






-- drop MATERIALIZED VIEW voc_anal_flow_sentiment_annotations_results_risk_mv;
CREATE MATERIALIZED VIEW voc_anal_flow_sentiment_annotations_results_risk_mv
(id,data_id,channel_catagory,channel_code,channel_name,brand_code,brand_name,car_series_code,car_series_name,model_name,content_type,title,sentiment,intention,data_create_time,publish_time,create_time,is_outer,hot_word,keywords,original_text_scene,content,market_id,competitive_type,series_factory,automark,one_id,user_journey1,user_journey2,user_journey3,usage_scenario_first,usage_scenario_second,d2c_responsible_dept,d2c_accountable_dept,d2c_cc_dept,cust_global_id,cust_classify,cust_main_phone,is_car_owner,cust_age,cust_age_group,cust_name,cust_gender,cust_high_educaion,marrige_statue,family_income,is_exchange_flg,purchase_car_times,is_member_flg,cust_province_code,cust_province,cust_city_code,cust_city,cust_type,cust_lived_prov,cust_lived_city,cust_profession,vhl_vin,vhl_color_name,vhl_product_date,vhl_offline_date,vhl_is_abroad,vhl_dis_ch,vhl_dis_mt,vhl_eng_clsf,vhl_eng_seris,vhl_veh_type,vhl_country,vhl_bd_clsf,vhl_seg_mt,vhl_pow_clsf,vhl_fu_clsf,vhl_modl_st,vhl_std_plnt_code,dlr_oc_id,dlr_oc_code,dlr_oc_name,dlr_oc_province_code,dlr_oc_province,dlr_oc_city_code,dlr_oc_city,dlr_dc_id,dlr_dc_code,dlr_dc_name,dlr_dc_province_code,dlr_dc_province,dlr_dc_city_code,dlr_dc_city,dlr_mc_id,dlr_mc_code,dlr_mc_name,dlr_mc_province_code,dlr_mc_province,dlr_mc_city_code,dlr_mc_city,is_wsater_army,is_manager_focused,is_big_v,author_id,author_nick,is_main_post,original_link,retweeted_url,retweeted_user_id,retweeted_user_name,retweeted_content,retweeted_title,retweeted_time,view_count,comment_count,comment_url,comment_user_name,comment_user_id,like_count,share_count,favorite_count,work_order_id,quest_id,quest_type,quest_answer_score,quest_business_type,quest_business_scenario,tag_accuracy,sensitive_type,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_event_clarity,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_complaint_flag_needing_prtv_msg,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,topic,topic_text,opinion,cpt_tag_first_code,cpt_tag_second_code,cpt_tag_three_code,cpt_tag_four_code,cpt_tag_first,cpt_tag_second,cpt_tag_three,cpt_tag_four,ujy_tag_first_code,ujy_tag_second_code,ujy_tag_three_code,ujy_tag_four_code,ujy_tag_first,ujy_tag_second,ujy_tag_three,ujy_tag_four,cma_tag_first_code,cma_tag_second_code,cma_tag_three_code,cma_tag_four_code,cma_tag_first,cma_tag_second,cma_tag_three,cma_tag_four,dom_tag_first_code,dom_tag_second_code,dom_tag_three_code,dom_tag_four_code,dom_tag_first,dom_tag_second,dom_tag_three,dom_tag_four,nps_tag_first_code,nps_tag_second_code,nps_tag_three_code,nps_tag_four_code,nps_tag_first,nps_tag_second,nps_tag_three,nps_tag_four,vtr_tag_first_code,vtr_tag_second_code,vtr_tag_three_code,vtr_tag_four_code,vtr_tag_first,vtr_tag_second,vtr_tag_three,vtr_tag_four,abandon,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-26 17:58:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 6
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "brand_code, car_series_code, topic, channel_code",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS with `base_data` as (
    select
        `id`
         ,`publish_time`
         ,`data_id`
         ,`one_id`
         ,`work_id`
         ,`client_id`
         ,`channel_id`
         ,`content_type`
         ,`sample_data_type`
         ,`original_id`
         ,`input_data_id`
         ,`original_text_scene`
         ,`brand_code`
         ,`car_series_code`
         ,`label_type`
         ,`sentiment`
         ,`intention_type`
         ,`topic`
         ,`opinion`
         ,`subject`
         ,`fault_level`
         ,`description`
         ,`sentiment_score`
         ,`keywords`
         ,`model_type`
         ,`raw_data`
         ,`ext_fields`
         ,`biz_ext_attrs`
         ,`biz_ext_attrs2`
         ,`biz_ext_attrs3`
         ,`cust_ext_attrs`
         ,`vhl_ext_attrs`
         ,`dealer_ext_attrs`
         ,`prd_ext_attrs`
         ,`create_time`
         ,`update_time`
         ,`abandon`
         ,`done`
         ,`insert_dt`
    from `voc_anal_flow_model_tags_result_data_full_mv`
     WHERE `brand_code` is not null
      and `channel_id` is not null
      and `topic` is not null
      and `content_type` is not null
      and `publish_time` >= DATE_SUB(CURDATE(), INTERVAL 1 DAY)
      and `publish_time` < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
),
`brand_data` as (
    select `code` as `brand_code`,`name` as `brand_name`,`competitive_type` as `competitive_type` ,
   `automark` as `automark` from `voc_ext_ins_brand_info_mv`
),
`car_series_data` as (
    select  `code` as `car_series_code`, `name` as `car_series_name` ,`alias`,`exclusion_words`,`factory`,`competitive_type`,
    `competitive_product` from `voc_ext_ins_car_series_info_mv`
)
select
    `f`.`id`	-- 声音ID
     ,`f`.`data_id`	-- 数据唯一标识
     ,`d4`.`channel_catagory_level1`  as `channel_catagory`	-- 类别（长视频、社交媒体、资讯类等）		优化-渠道数据
     ,`f`.`channel_id`	as `channel_code`-- 渠道编码
     ,`d4`.`name`  as `channel_name`	-- 渠道名称		优化-品牌车系数据
     ,`f`.`brand_code`	-- 品牌编码
     ,`d3`.`brand_name`	-- 品牌名称		优化-品牌车系数据
     ,`f`.`car_series_code`	-- 车系代码	biz_ext_attrs
     ,`d5`.`car_series_name` -- 车系名称
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.model") as `model_name`	-- 车型代码	优化（需要和鹏飞确认）
     ,`f`.`content_type`	-- 数据类型(1：咨询/2：意⻅反馈/3：帖⼦评论/4：问卷/5：⼯单)
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.title") as `title`
     ,`f`.`sentiment`	-- 情感
     ,`f`.`intention_type` as `intention`	-- 意图（表扬/建议/咨询/抱怨)）
     ,to_date(`f`.`publish_time`) as `data_create_time`	-- 数据产生时间
     ,`f`.`publish_time`
     ,`f`.`create_time`	-- 数据抓取时间
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.is_outer") AS `is_outer`	-- 内外数据？
     ,null as `hot_word`	-- 热词   优化（模型无返回值）
     ,`f`.`keywords`	-- 关键词
     ,`f`.`original_text_scene`  -- 声音片段
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.content") as `content`
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.market_id") as `market_id`	-- 细分市场ID
     -- ,JSON_EXTRACT_STRING(f.biz_ext_attrs2, "$.competitive_type") as competitive_type -- 竞争力类型（1：传统 Competitive、2：新兴 Competitive、3：其他 Competitive）
     -- ,JSON_EXTRACT_STRING(f.biz_ext_attrs2, "$.series_factory") as series_factory -- 车系所属企业
     ,`d5`.`competitive_type` as `competitive_type`-- 车系所属企业
     ,`d5`.`factory` as `series_factory`-- 车系所属企业
     ,`d3`.`automark` as `automark`-- 车系所属企业
     ,if(JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.is_main_post")="Y",ifnull(JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.user.uid_org'),JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.user.uid')),ifnull(JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.retweeted.user.uid_org'),JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.retweeted.user.uid'))) AS  `one_id`	-- oneId	代入
     ,`d6`.`user_journey1` as `user_journey1`	-- 旅程维度1（看车、购车等）	优化-标签数据
     ,`d6`.`user_journey2` as `user_journey2`	-- 旅程维度2（高速路、高原等）	优化-标签数据
     ,`d6`.`user_journey3` as `user_journey3`
     -- ,f.scenario	-- 关注场景领域（舒适性/材质/异响）
     ,JSON_EXTRACT_STRING(`f`.`ext_fields`, "$.usage_scenario_first") as `usage_scenario_first`
     ,JSON_EXTRACT_STRING(`f`.`ext_fields`, "$.usage_scenario_second") as `usage_scenario_second`
     ,`d6`.`d2c_responsible_dept` as `d2c_responsible_dept`	-- 主责部门	代入		优化-标签数据
     ,`d6`.`d2c_accountable_dept` as `d2c_accountable_dept`	-- 责任部门	代入		优化-标签数据
     ,`d6`.`d2c_cc_dept`  as `d2c_cc_dept`	-- 抄送部门	代入				优化-标签数据
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.global_id") as `cust_global_id`	--	sso全局ID
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.cust_classify") as `cust_classify`	--	客户类型
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.mobile") as `cust_main_phone`	--	主手机号
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.is_car_owner_flg") as `is_car_owner`	--	是否车主：Y、N
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.age") as `cust_age`	--	客户年龄
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.age_group") as `cust_age_group`	--	年龄段
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.user_name") as `cust_name`	--	客户姓名
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.gender") as `cust_gender`	--	客户性别(男/女）
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.high_educaion") as `cust_high_educaion`	--	最高学历
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.marriage_statue") as `marrige_statue`	--	婚姻状况
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.family_income") as `family_income`	--	家庭月收入
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.is_exchange_flg") as `is_exchange_flg`	--	是否换购
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.purchase_car_times") as `purchase_car_times`	--	购车次数
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.is_member_flg") as `is_member_flg`	--	是否会员
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.hukou_prov_cd") as `cust_province_code`	--	客户常驻省份编码/户籍地
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.hukou_prov_nm") as `cust_province`	--	客户常驻省份/户籍地
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.hukou_city_cd") as `cust_city_code`	--	客户常驻市编码/户籍地
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.hukou_city_nm") as `cust_city`	--	客户常驻市/户籍地
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.cust_type") as `cust_type`	--	客户分类
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.lived_prov_nm") as `cust_lived_prov`	--	居住地-省份    -- 优化  区域数据
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.lived_city_nm") as `cust_lived_city`	--	居住地-城市    -- 优化  区域数据
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.profession") as `cust_profession`	--	职业
     ,JSON_EXTRACT_STRING(`f`.`vhl_ext_attrs`, "$.vin") as `vhl_vin`	-- 车辆车架号
     ,JSON_EXTRACT_STRING(`f`.`vhl_ext_attrs`, "$.col_name") as `vhl_color_name`		-- 颜色名称
     ,JSON_EXTRACT_STRING(`f`.`vhl_ext_attrs`, "$.product_date") as `vhl_product_date`		-- 生产日期
     ,JSON_EXTRACT_STRING(`f`.`vhl_ext_attrs`, "$.offline_date") as `vhl_offline_date`		-- 出厂日期
     ,JSON_EXTRACT_STRING(`f`.`vhl_ext_attrs`, "$.home_abroad") as `vhl_is_abroad`		-- 国内国外
     ,JSON_EXTRACT_STRING(`f`.`vhl_ext_attrs`, "$.dis_ch") as `vhl_dis_ch`		-- 排放
     ,JSON_EXTRACT_STRING(`f`.`vhl_ext_attrs`, "$.dis_mt") as `vhl_dis_mt`		-- 排量
     ,JSON_EXTRACT_STRING(`f`.`vhl_ext_attrs`, "$.eng_clsf") as `vhl_eng_clsf`		-- 动力系列大类
     ,JSON_EXTRACT_STRING(`f`.`vhl_ext_attrs`, "$.eng_seris") as `vhl_eng_seris`		-- 动力系列小类
     ,JSON_EXTRACT_STRING(`f`.`vhl_ext_attrs`, "$.veh_type") as `vhl_veh_type`		-- 车辆类型（出口车、领用车、代工车、商用车）
     ,JSON_EXTRACT_STRING(`f`.`prd_ext_attrs`, "$.country") as `vhl_country`		-- 国别
     ,JSON_EXTRACT_STRING(`f`.`prd_ext_attrs`, "$.bd_clsf") as `vhl_bd_clsf`		-- 车身类型
     ,JSON_EXTRACT_STRING(`f`.`prd_ext_attrs`, "$.seg_mt") as `vhl_seg_mt`		-- 细分市场
     ,JSON_EXTRACT_STRING(`f`.`prd_ext_attrs`, "$.pow_clsf") as `vhl_pow_clsf`		-- 动力类型
     ,JSON_EXTRACT_STRING(`f`.`prd_ext_attrs`, "$.fu_clsf") as `vhl_fu_clsf`		-- 燃料类型
     ,JSON_EXTRACT_STRING(`f`.`prd_ext_attrs`, "$.modl_st") as `vhl_modl_st`		-- 车型状态号
     ,JSON_EXTRACT_STRING(`f`.`vhl_ext_attrs`, "$.plnt_code") as `vhl_std_plnt_code`		-- 标准工厂编码
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.sk_id"		) as `dlr_oc_id`               	-- 订单中心-经销商ID
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.dlr_cd") as `dlr_oc_code`             	-- 订单中心-经销商编码
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.dlr_nm") as `dlr_oc_name`             	-- 订单中心-经销商全称
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.prov_cd") as `dlr_oc_province_code`    -- 	订单中心-经销商所在省编码
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.prov_nm") as `dlr_oc_province`         -- 	订单中心-经销商所在省
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.city_cd") as `dlr_oc_city_code`        -- 	订单中心-经销商所在市编码
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.city_nm") as `dlr_oc_city`             -- 	订单中心-经销商所在市
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.sk_id") as `dlr_dc_id`               	-- 	交付中心-经销商ID
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.dlr_cd") as `dlr_dc_code`             -- 	交付中心-经销商编码
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.dlr_nm") as `dlr_dc_name`             -- 	交付中心-经销商全称
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.prov_cd") as `dlr_dc_province_code`    -- 	交付中心-经销商所在省编码
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.prov_nm") as `dlr_dc_province`         -- 	交付中心-经销商所在省
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.city_cd") as `dlr_dc_city_code`        -- 	交付中心-经销商所在市编码
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.city_nm") as `dlr_dc_city`             -- 	交付中心-经销商所在市
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.sk_id") as `dlr_mc_id`               --    	维保中心-经销商ID
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.dlr_cd") as `dlr_mc_code`             -- 	维保中心-经销商编码
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.dlr_nm") as `dlr_mc_name`             -- 	维保中心-经销商全称
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.prov_cd") as `dlr_mc_province_code`    -- 	维保中心-经销商所在省编码
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.prov_nm") as `dlr_mc_province`         -- 	维保中心-经销商所在省
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.city_cd") as `dlr_mc_city_code`        -- 	维保中心-经销商所在市编码
     ,JSON_EXTRACT_STRING(`f`.`dealer_ext_attrs`, "$.city_nm") as `dlr_mc_city`             -- 	维保中心-经销商所在市
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.is_wsater_army") as `is_wsater_army`	-- 是否水军
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.is_manager_focused") as `is_manager_focused`	-- 是否领导重点关注
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.is_big_v") as `is_big_v`	-- 是否KOC账号
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.author_id") as `author_id`	-- 作者账号
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.author_nick") as `author_nick`	-- 作者昵称
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.is_main_post") as `is_main_post`	-- 是否主贴(Y/N)
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.url") as `original_link`	-- 帖子原文链接
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.retweeted.url') AS `retweeted_url`
     ,ifnull(JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.retweeted.user.uid_org'),JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.retweeted.user.uid')) AS `retweeted_user_id`
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.retweeted.user.name') AS `retweeted_user_name`
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.retweeted.content') AS `retweeted_content`
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.retweeted.title') AS `retweeted_title`
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.retweeted.ctime') AS `retweeted_time`
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.view_count") as `view_count`	-- 浏览量or播放量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.comment_count") as `comment_count`	-- 评论量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.user.url') AS `comment_url`
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.user.name') AS `comment_user_name`
     ,ifnull(JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.user.uid_org'),JSON_EXTRACT_STRING(`f`.`biz_ext_attrs`, '$.data.user.uid')) AS `comment_user_id`
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.like_count") as `like_count`	-- 点赞量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.share_count") as `share_count`	-- 转发量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.favorite_count") as `favorite_count` 	-- 收藏量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.order_id") as `work_order_id`	-- 工单ID
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.quest_id") as `quest_id`	-- 问卷ID
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.quest_type") as `quest_type`	-- 问卷类型
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.quest_answer_score") as `quest_answer_score`	-- 问卷答案分数
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.quest_business_type") as `quest_business_type`	-- 问卷业务类型
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.quest_business_scenario") as `quest_business_scenario`	-- 问卷业务场景
     ,`d6`.`tag_accuracy` as `tag_accuracy`
     , "" as `sensitive_type`-- 代码的精准性（精准、有待提升等）	标签
     ,`d6`.`tag_customer_issue_classification` as `tag_customer_issue_classification`	-- 客户问题分级（S、A、B、C等）	标签
     ,`d6`.`tag_issue_severity` as `tag_issue_severity`	-- 问题程度（高、中、低）	标签
     ,`d6`.`tag_code_status` as `tag_code_status`	-- 代码状态（有效、无效等）	标签
     ,`d6`.`tag_business_domain` as `tag_business_domain`	-- 业务领域（产品质量、产品设计、服务体验）	标签
     ,`d6`.`event_clarity` as `tag_event_clarity`	-- 事件清晰度（印象、事实）	标签
     ,JSON_EXTRACT_STRING(`f`.`ext_fields`, "$.tag_high_value_flag") as `tag_high_value_flag`	-- 需推送的高价值建议标识	手动
     ,`d6`.`tag_complaint_flag_needing_reply` as `tag_complaint_flag_needing_reply`	-- 需回评的抱怨标识	标签
     ,JSON_EXTRACT_STRING(`f`.`ext_fields`, "$.tag_complaint_flag_needing_prtv_msg") as `tag_complaint_flag_needing_prtv_msg`	-- 需私信的抱怨标识	手动
     ,JSON_EXTRACT_STRING(`f`.`ext_fields`, "$.tag_high_quality_voc_flag") as `tag_high_quality_voc_flag`	-- 针对五级明细高质量VOC标识	手动
     ,`d6`.`tag_new_energy_or_fuel` as `tag_new_energy_or_fuel`	-- 新能源特有/燃油特有	标签
     ,`d6`.`tag_need_forvclosed_loop` as `tag_need_forvclosed_loop`	-- 批量问题是否需要闭环（短平快、通用等）	标签
     ,`f`.`topic`	 -- 	观点（根因标签ID）
     ,`d6`.`topic_text` as `topic_text` -- 观点
     ,`f`.`opinion` -- 	原始观点
     ,`d6`.`cpt_tag_first_code` as `cpt_tag_first_code` -- CPT签1级编码
     ,`d6`.`cpt_tag_second_code` as `cpt_tag_second_code` -- CPT签2级编码
     ,`d6`.`cpt_tag_three_code` as `cpt_tag_three_code` -- CPT签3级编码
     ,`d6`.`cpt_tag_four_code` as `cpt_tag_four_code` -- CPT签4级编码
     ,`d6`.`cpt_tag_first` as `cpt_tag_first` -- CPT签1级
     ,`d6`.`cpt_tag_second` as `cpt_tag_second` -- CPT签2级
     ,`d6`.`cpt_tag_three` as `cpt_tag_three` -- CPT签3级
     ,`d6`.`cpt_tag_four` as `cpt_tag_four` -- CPT签4级
     ,`d6`.`ujy_tag_first_code` as `ujy_tag_first_code` -- 全旅程客户签1级编码
     ,`d6`.`ujy_tag_second_code` as `ujy_tag_second_code` -- 全旅程客户签2级编码
     ,`d6`.`ujy_tag_three_code` as `ujy_tag_three_code` -- 全旅程客户签3级编码
     ,`d6`.`ujy_tag_four_code` as `ujy_tag_four_code` -- 全旅程客户签4级编码
     ,`d6`.`ujy_tag_first` as `ujy_tag_first` -- 全旅程客户签1级
     ,`d6`.`ujy_tag_second` as `ujy_tag_second` -- 全旅程客户签2级
     ,`d6`.`ujy_tag_three` as `ujy_tag_three` -- 全旅程客户签3级
     ,`d6`.`ujy_tag_four` as `ujy_tag_four` -- 全旅程客户签4级
     ,`d6`.`cma_tag_first_code` as `cma_tag_first_code` -- 全领域业务标签1级编码
     ,`d6`.`cma_tag_second_code` as `cma_tag_second_code` -- 全领域业务标签2级编码
     ,`d6`.`cma_tag_three_code` as `cma_tag_three_code` -- 全领域业务标签3级编码
     ,`d6`.`cma_tag_four_code` as `cma_tag_four_code` -- 全领域业务标签4级编码
     ,`d6`.`cma_tag_first` as `cma_tag_first` -- 全领域业务标签1级
     ,`d6`.`cma_tag_second` as `cma_tag_second` -- 全领域业务标签2级
     ,`d6`.`cma_tag_three` as `cma_tag_three` -- 全领域业务标签3级
     ,`d6`.`cma_tag_four` as `cma_tag_four` -- 全领域业务标签4级
     ,`d6`.`dom_tag_first_code` as `dom_tag_first_code` -- 商品化属性标签1级编码
     ,`d6`.`dom_tag_second_code` as `dom_tag_second_code` -- 商品化属性标签2级编码
     ,`d6`.`dom_tag_three_code` as `dom_tag_three_code` -- 商品化属性标签3级编码
     ,`d6`.`dom_tag_four_code` as `dom_tag_four_code` -- 商品化属性标签4级编码
     ,`d6`.`dom_tag_first` as `dom_tag_first` -- 商品化属性标签1级
     ,`d6`.`dom_tag_second` as `dom_tag_second` -- 商品化属性标签2级
     ,`d6`.`dom_tag_three` as `dom_tag_three` -- 商品化属性标签3级
     ,`d6`.`dom_tag_four` as `dom_tag_four` -- 商品化属性标签4级
     ,`d6`.`nps_tag_first_code` as `nps_tag_first_code` -- NPS标签1级编码
     ,`d6`.`nps_tag_second_code` as `nps_tag_second_code` -- NPS标签2级编码
     ,`d6`.`nps_tag_three_code` as `nps_tag_three_code` -- NPS标签3级编码
     ,`d6`.`nps_tag_four_code` as `nps_tag_four_code` -- NPS标签4级编码
     ,`d6`.`nps_tag_first` as `nps_tag_first` -- NPS标签1级
     ,`d6`.`nps_tag_second` as `nps_tag_second` -- NPS标签2级
     ,`d6`.`nps_tag_three` as `nps_tag_three` -- NPS标签3级
     ,`d6`.`nps_tag_four` as `nps_tag_four` -- NPS标签4级
     ,`d6`.`vtr_tag_first_code` as `vtr_tag_first_code` -- VRT标签1级编码
     ,`d6`.`vtr_tag_second_code` as `vtr_tag_second_code` -- VRT标签2级编码
     ,`d6`.`vtr_tag_three_code` as `vtr_tag_three_code` -- VRT标签3级编码
     ,`d6`.`vtr_tag_four_code` as `vtr_tag_four_code` -- VRT标签4级编码
     ,`d6`.`vtr_tag_first` as `vtr_tag_first` -- VRT标.签1级
     ,`d6`.`vtr_tag_second` as `vtr_tag_second` -- VRT标签2级
     ,`d6`.`vtr_tag_three` as `vtr_tag_three` -- VRT标签3级
     ,`d6`.`vtr_tag_four` as `vtr_tag_four` -- VRT标签4级
     , nvl(NULLIF(`f`.`abandon`,'') ,null) as `abandon`
     , `f`.`insert_dt` as  `insert_dt`
from base_data as `f`
         left join brand_data as `d3` on `f`.`brand_code` = `d3`.`brand_code`
         left join `voc_ext_ins_channel_mv` as `d4` on `f`.`channel_id` = `d4`.`code`
         left join car_series_data as `d5` on `f`.`car_series_code` = `d5`.`car_series_code`
         left join `voc_ext_ins_tag_system_final_mv` as `d6` ON `f`.`topic` = `d6`.`topic`






CREATE MATERIALIZED VIEW voc_imp_cust_json_by_one_id_mv
(one_id,cust_json,vehicle_json,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-23 06:10:00"
DUPLICATE KEY(`one_id`)
DISTRIBUTED BY HASH(`one_id`) BUCKETS 12
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "insert_dt",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS select distinct `one_id` as `one_id` ,`cust_json`, `vehicle_json`, now() as `insert_dt`
   from `voc_imp_cust_vehicle_rel_json_info_mv`
   where `one_id` is not null and `one_id` <> ''
   ;





CREATE MATERIALIZED VIEW voc_ext_ins_channel_mv
(id,parent_id,name,type,status,name_en,create_time,update_time,code,level,top_id,description,is_core_channel,data_source_type,channel_catagory_level1,insert_dt)
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-23 11:25:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "code, name",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS with `raw_data` as (
	select
    `f`.`id`, `f`.`parent_id`, `f`.`name`, `f`.`type`, `f`.`status`, `f`.`name_en`, `f`.`create_time`, `f`.`update_time`, `f`.`code`, `f`.`level`, `f`.`top_id`, `f`.`description`, `f`.`is_core_channel`, `f`.`data_source_type`,
    `c`.`name` as `channel_catagory_level1`
	from (
	    SELECT
	        `id`,`parent_id`,`name`,`type`, `status`,`name_en`,`create_time`,`update_time`,`code`,`level`,
	        `top_id`,`description`,`is_core_channel`,`data_source_type`
	    from `ins_channel`
	    where
	    	-- status = 1 and
	    	`type` = 'Channel' and `top_id` is not null
	)`f`
left join
(
    select `id`,`name`
    from `ins_channel`
    where `parent_id` = '0'
)`c` on `f`.`top_id` = `c`.`id`
)
select
    `raw_data`.`id`, `raw_data`.`parent_id`, `raw_data`.`name`, `raw_data`.`type`, `raw_data`.`status`, `raw_data`.`name_en`, `raw_data`.`create_time`, `raw_data`.`update_time`, `raw_data`.`code`, `raw_data`.`level`, `raw_data`.`top_id`, `raw_data`.`description`, `raw_data`.`is_core_channel`, `raw_data`.`data_source_type`, `raw_data`.`channel_catagory_level1`,
    current_timestamp() as `insert_dt`
from raw_data;




CREATE MATERIALIZED VIEW voc_imp_cust_json_b_by_mobile_mv
(mobile,cust_json,vehicle_json,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-23 06:11:00"
DUPLICATE KEY(`mobile`)
DISTRIBUTED BY HASH(`mobile`) BUCKETS 12
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "insert_dt",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS select distinct `mobile` ,`cust_json`, `vehicle_json` ,now() as `insert_dt`
   from `voc_imp_cust_vehicle_rel_json_info_mv`
   where `mobile` is not null and `mobile` <> '';




CREATE MATERIALIZED VIEW voc_ext_ins_car_series_info_mv
(id,name,name_en,brand_id,brand_code,alias,exclusion_words,code,order_by,car_name,car_code,factory,car_level1,car_level2,energy_type1,energy_type2,level_name,competitive_type,competitive_product,start_time,end_time,is_core,operator,create_time,update_time,del_flag,img,app_id,country)
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-23 11:25:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "code, name",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS select
              `id`, `name`, `name_en`, `brand_id`, `brand_code`, `alias`,
              `exclusion_words`, `code`, `order_by`, `car_name`, `car_code`, `factory`, `car_level1`,
              `car_level2`, `energy_type1`, `energy_type2`, `level_name`, `competitive_type`,
              `competitive_product`, `start_time`, `end_time`, `is_core`, `operator`, `create_time`,
              `update_time`, `del_flag`, `img`, `app_id`, `country`
   from `voc_mysql_jdbc`.`voc_ms_be`.`ins_car_series_info`;




CREATE MATERIALIZED VIEW voc_imp_cust_json_b_by_idcard_mv
(id_card_no,cust_json,vehicle_json,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-23 06:11:00"
DUPLICATE KEY(`id_card_no`)
DISTRIBUTED BY HASH(`id_card_no`) BUCKETS 12
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "insert_dt",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS select DISTINCT `id_card_no` ,`cust_json`, `vehicle_json` ,now() as `insert_dt`
   from `voc_imp_cust_vehicle_rel_json_info_mv`
   where `id_card_no` is not null and `id_card_no` <> '';




CREATE MATERIALIZED VIEW voc_ext_ins_brand_info_mv
(id,name,name_en,alias,exclusion_words,code,order_by,operator,create_time,update_time,del_flag,img,app_id,nature,country,competitive_type,competitive_product,automark,is_core,insert_dt)
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-23 11:27:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "code, name",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS select
              `id`, `name`, `name_en`, `alias`, `exclusion_words`, `code`, `order_by`, `operator`,
              `create_time`, `update_time`, `del_flag`, `img`, `app_id`, `nature`, `country`,
              `competitive_type`, `competitive_product`, `automark`, `is_core`,
              current_timestamp() as `insert_dt`
   from `voc_mysql_jdbc`.`voc_ms_be`.`ins_brand_info`;



CREATE MATERIALIZED VIEW voc_ext_ins_tag_client_mv
(id,tag_parent_id,tag_name,tag_name_en,tag_code,tag_type,tag_attribute,energy_type,car_type,tag_status,tag_description,seriousness,user_journey1,user_journey2,user_journey3,scenario_attr,event_clarity,d2c_responsible_dept,d2c_cc_dept,create_time,update_time,create_user,update_user,app_client,sort,level,emotion,intention,d2c_accountable_dept,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,topic_id,susceptive_type)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-23 11:27:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "tag_parent_id, tag_code, tag_name",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728"
)
AS select
              `id`, `tag_parent_id`, `tag_name`, `tag_name_en`, `tag_code`, `tag_type`, `tag_attribute`,
              `energy_type`, `car_type`, `tag_status`, `tag_description`, `seriousness`, `user_journey1`,
              `user_journey2`, `user_journey3`, `scenario_attr`, `event_clarity`, `d2c_responsible_dept`,
              `d2c_cc_dept`, `create_time`, `update_time`, `create_user`, `update_user`, `app_client`, `sort`,
              `level`, `emotion`, `intention`, `d2c_accountable_dept`, `tag_accuracy`, `tag_customer_issue_classification`,
              `tag_issue_severity`, `tag_code_status`, `tag_business_domain`, `tag_high_value_flag`,
              `tag_complaint_flag_needing_reply`, `tag_high_quality_voc_flag`, `tag_new_energy_or_fuel`,
              `tag_need_forvclosed_loop`,
              `topic_id`,
              `susceptive_type`
   from `ins_tag_client`;




CREATE MATERIALIZED VIEW voc_ext_ins_tag_system_final_mv
(topic,id,topic_text,cpt_tag_first_code,cpt_tag_first,cpt_tag_second_code,cpt_tag_second,cpt_tag_three_code,cpt_tag_three,cpt_tag_four_code,cpt_tag_four,ujy_tag_first_code,ujy_tag_first,ujy_tag_second_code,ujy_tag_second,ujy_tag_three_code,ujy_tag_three,ujy_tag_four_code,ujy_tag_four,cma_tag_first_code,cma_tag_first,cma_tag_second_code,cma_tag_second,cma_tag_three_code,cma_tag_three,cma_tag_four_code,cma_tag_four,dom_tag_first_code,dom_tag_first,dom_tag_second_code,dom_tag_second,dom_tag_three_code,dom_tag_three,dom_tag_four_code,dom_tag_four,vtr_tag_first_code,vtr_tag_first,vtr_tag_second_code,vtr_tag_second,vtr_tag_three_code,vtr_tag_three,vtr_tag_four_code,vtr_tag_four,nps_tag_first_code,nps_tag_first,nps_tag_second_code,nps_tag_second,nps_tag_three_code,nps_tag_three,nps_tag_four_code,nps_tag_four,tag_parent_id,tag_type,tag_attribute,energy_type,car_type,tag_status,tag_description,seriousness,user_journey1,user_journey2,user_journey3,scenario_attr,event_clarity,d2c_responsible_dept,d2c_cc_dept,d2c_accountable_dept,create_time,update_time,create_user,update_user,app_client,sort,level,emotion,intention,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,insert_dt)
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-23 11:28:00"
DUPLICATE KEY(`topic`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS SELECT
              `topic`,
              -- 处理id：两行id不同，随机取一个（用MAX/MIN均可，因无业务优先级）
              MAX(`id`) AS `id`,
              -- 处理topic_text：两行值一致（均为“轮毂异常磨损”），直接取
              MAX(`topic_text`) AS `topic_text`,
              -- CPT标签：两行均为NULL，合并后仍为NULL
              MAX(`cpt_tag_first_code`) AS `cpt_tag_first_code`,
              MAX(`cpt_tag_first`) AS `cpt_tag_first`,
              MAX(`cpt_tag_second_code`) AS `cpt_tag_second_code`,
              MAX(`cpt_tag_second`) AS `cpt_tag_second`,
              MAX(`cpt_tag_three_code`) AS `cpt_tag_three_code`,
              MAX(`cpt_tag_three`) AS `cpt_tag_three`,
              MAX(`cpt_tag_four_code`) AS `cpt_tag_four_code`,
              MAX(`cpt_tag_four`) AS `cpt_tag_four`,
              -- UJY标签：第一行有值（cqca1118/日常用车等）、第二行NULL，取有值行
              MAX(`ujy_tag_first_code`) AS `ujy_tag_first_code`,
              MAX(`ujy_tag_first`) AS `ujy_tag_first`,
              MAX(`ujy_tag_second_code`) AS `ujy_tag_second_code`,
              MAX(`ujy_tag_second`) AS `ujy_tag_second`,
              MAX(`ujy_tag_three_code`) AS `ujy_tag_three_code`,
              MAX(`ujy_tag_three`) AS `ujy_tag_three`,
              MAX(`ujy_tag_four_code`) AS `ujy_tag_four_code`,
              MAX(`ujy_tag_four`) AS `ujy_tag_four`,
              -- CMA标签：第一行NULL、第二行有值（cqca1001/底盘等），取有值行
              MAX(`cma_tag_first_code`) AS `cma_tag_first_code`,
              MAX(`cma_tag_first`) AS `cma_tag_first`,
              MAX(`cma_tag_second_code`) AS `cma_tag_second_code`,
              MAX(`cma_tag_second`) AS `cma_tag_second`,
              MAX(`cma_tag_three_code`) AS `cma_tag_three_code`,
              MAX(`cma_tag_three`) AS `cma_tag_three`,
              MAX(`cma_tag_four_code`) AS `cma_tag_four_code`,
              MAX(`cma_tag_four`) AS `cma_tag_four`,
              -- DOM/VTR/NPS标签：两行均为NULL，合并后仍为NULL
              MAX(`dom_tag_first_code`) AS `dom_tag_first_code`,
              MAX(`dom_tag_first`) AS `dom_tag_first`,
              MAX(`dom_tag_second_code`) AS `dom_tag_second_code`,
              MAX(`dom_tag_second`) AS `dom_tag_second`,
              MAX(`dom_tag_three_code`) AS `dom_tag_three_code`,
              MAX(`dom_tag_three`) AS `dom_tag_three`,
              MAX(`dom_tag_four_code`) AS `dom_tag_four_code`,
              MAX(`dom_tag_four`) AS `dom_tag_four`,
              MAX(`vtr_tag_first_code`) AS `vtr_tag_first_code`,
              MAX(`vtr_tag_first`) AS `vtr_tag_first`,
              MAX(`vtr_tag_second_code`) AS `vtr_tag_second_code`,
              MAX(`vtr_tag_second`) AS `vtr_tag_second`,
              MAX(`vtr_tag_three_code`) AS `vtr_tag_three_code`,
              MAX(`vtr_tag_three`) AS `vtr_tag_three`,
              MAX(`vtr_tag_four_code`) AS `vtr_tag_four_code`,
              MAX(`vtr_tag_four`) AS `vtr_tag_four`,
              MAX(`nps_tag_first_code`) AS `nps_tag_first_code`,
              MAX(`nps_tag_first`) AS `nps_tag_first`,
              MAX(`nps_tag_second_code`) AS `nps_tag_second_code`,
              MAX(`nps_tag_second`) AS `nps_tag_second`,
              MAX(`nps_tag_three_code`) AS `nps_tag_three_code`,
              MAX(`nps_tag_three`) AS `nps_tag_three`,
              MAX(`nps_tag_four_code`) AS `nps_tag_four_code`,
              MAX(`nps_tag_four`) AS `nps_tag_four`,
              -- 基础字段：两行值一致（如tag_type=FinalLabel、emotion=负向等），直接取
              MAX(`tag_parent_id`) AS `tag_parent_id`,
              MAX(`tag_type`) AS `tag_type`,
              MAX(`tag_attribute`) AS `tag_attribute`,
              MAX(`energy_type`) AS `energy_type`,
              MAX(`car_type`) AS `car_type`,
              MAX(`tag_status`) AS `tag_status`,
              MAX(`tag_description`) AS `tag_description`,
              MAX(`seriousness`) AS `seriousness`,
              MAX(`user_journey1`) AS `user_journey1`,
              MAX(`user_journey2`) AS `user_journey2`,
              MAX(`user_journey3`) AS `user_journey3`,
              MAX(`scenario_attr`) AS `scenario_attr`,
              MAX(`event_clarity`) AS `event_clarity`,
              MAX(`d2c_responsible_dept`) AS `d2c_responsible_dept`,
              MAX(`d2c_cc_dept`) AS `d2c_cc_dept`,
              MAX(`d2c_accountable_dept`) AS `d2c_accountable_dept`,
              MAX(`create_time`) AS `create_time`,
              MAX(`update_time`) AS `update_time`,
              MAX(`create_user`) AS `create_user`,
              MAX(`update_user`) AS `update_user`,
              MAX(`app_client`) AS `app_client`,
              MAX(`sort`) AS `sort`,
              MAX(`level`) AS `level`,
              MAX(`emotion`) AS `emotion`,
              MAX(`intention`) AS `intention`,
              MAX(`tag_accuracy`) AS `tag_accuracy`,
              MAX(`tag_customer_issue_classification`) AS `tag_customer_issue_classification`,
              MAX(`tag_issue_severity`) AS `tag_issue_severity`,
              MAX(`tag_code_status`) AS `tag_code_status`,
              MAX(`tag_business_domain`) AS `tag_business_domain`,
              MAX(`tag_high_value_flag`) AS `tag_high_value_flag`,
              MAX(`tag_complaint_flag_needing_reply`) AS `tag_complaint_flag_needing_reply`,
              MAX(`tag_high_quality_voc_flag`) AS `tag_high_quality_voc_flag`,
              MAX(`tag_new_energy_or_fuel`) AS `tag_new_energy_or_fuel`,
              MAX(`tag_need_forvclosed_loop`) AS `tag_need_forvclosed_loop`,
              current_timestamp() as `insert_dt`
   FROM `voc_ext_ins_tag_by_system_mv`
-- 按topic分组：将相同topic的两行数据合并为一行
   GROUP BY `topic`;




-- drop MATERIALIZED VIEW voc_anal_flow_model_tags_result_data_full_mv;
CREATE MATERIALIZED VIEW voc_anal_flow_model_tags_result_data_full_mv
(id,publish_time,data_id,one_id,work_id,client_id,channel_id,content_type,sample_data_type,original_id,input_data_id,original_text_scene,brand_code,car_series_code,label_type,scenario,sentiment,intention_type,topic,opinion,subject,fault_level,description,sentiment_score,keywords,model_type,raw_data,ext_fields,biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,cust_ext_attrs,vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs,tags_ext_attrs,create_time,update_time,abandon,done,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 60 MINUTE STARTS "2026-02-28 18:50:00"
DUPLICATE KEY(`id`)
PARTITION BY (date_trunc(`publish_time`, 'month'))
DISTRIBUTED BY HASH(`id`) BUCKETS 32
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "brand_code, data_id, topic, channel_id",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS select
              id,
              nullif(`publish_time`, '') as `publish_time`,
              nullif(`data_id`, '') as `data_id`,
              nullif(`one_id`, '') as `one_id`,
              nullif(`work_id`, '') as `work_id`,
              nullif(`client_id`, '') as `client_id`,
              nullif(`channel_id`, '') as `channel_id`,
              nullif(`content_type`, '') as `content_type`,
              nullif(`sample_data_type`, '') as `sample_data_type`,
              nullif(`original_id`, '') as `original_id`,
              nullif(`input_data_id`, '') as `input_data_id`,
              nullif(`original_text_scene`, '') as `original_text_scene`,
              nullif(`brand_code`, '') as `brand_code`,
              nullif(`car_series_code`, '') as `car_series_code`,
              nullif(`label_type`, '') as `label_type`,
              nullif(`scenario`, '') as `scenario`,
              nullif(`sentiment`, '') as `sentiment`,
              nullif(`intention_type`, '') as `intention_type`,
              nullif(`topic`, '') as `topic`,
              nullif(`opinion`, '') as `opinion`,
              nullif(`subject`, '') as `subject`,
              nullif(`fault_level`, '') as `fault_level`,
              nullif(`description`, '') as `description`,
              nullif(`sentiment_score`, '') as `sentiment_score`,
              nullif(`keywords`, '') as `keywords`,
              nullif(`model_type`, '') as `model_type`,
              nullif(`raw_data`, '') as `raw_data`,
              nullif(`ext_fields`, '') as `ext_fields`,
              nullif(`biz_ext_attrs`, '') as `biz_ext_attrs`,
              nullif(`biz_ext_attrs2`, '') as `biz_ext_attrs2`,
              nullif(`biz_ext_attrs3`, '') as `biz_ext_attrs3`,
              nullif(`cust_ext_attrs`, '') as `cust_ext_attrs`,
              nullif(`vhl_ext_attrs`, '') as `vhl_ext_attrs`,
              nullif(`dealer_ext_attrs`, '') as `dealer_ext_attrs`,
              nullif(`prd_ext_attrs`, '') as `prd_ext_attrs`,
              nullif(`tags_ext_attrs`, '') as `tags_ext_attrs`,
              nullif(`create_time`, '') as `create_time`,
              nullif(`update_time`, '') as `update_time`,
              nullif(`abandon`, '') as `abandon`,
              nullif(`done`, '') as `done`,
              nullif(`insert_dt`, '') as `insert_dt`
   from `voc_anal_flow_model_tags_result_data_full`
   WHERE `brand_code` IS NOT NULL AND `brand_code` != '' AND `brand_code` REGEXP '^[^\\x{4e00}-\\x{9fff}]+$'
-- 	  AND `car_series_code` REGEXP '^[^\\x{4e00}-\\x{9fff}]+$'
      and `channel_id` IS NOT NULL AND `channel_id` != '' AND `channel_id` REGEXP '^[^\\x{4e00}-\\x{9fff}]+$'
      and `topic` IS NOT NULL AND `topic` != '' AND `topic` REGEXP '^[^\\x{4e00}-\\x{9fff}]+$'
      and `content_type` IS NOT NULL AND `content_type` != '' AND `content_type` REGEXP '^[^\\x{4e00}-\\x{9fff}]+$'
and   `publish_time` >= DATE_SUB(CURDATE(), INTERVAL 24 MONTH)
AND `publish_time` < CURDATE() + INTERVAL 1 day




--
CREATE MATERIALIZED VIEW voc_anal_flow_model_tags_result_invalid_data_full_mv
BUILD IMMEDIATE REFRESH AUTO ON MANUAL
DUPLICATE KEY(`data_id`)
DISTRIBUTED BY HASH(`data_id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "data_id",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728"
)
AS
with raw_data as (
	select * from voc_anal_flow_model_tags_result_data_full
	where  brand_code is null or brand_code = '' or  CHAR_LENGTH(`brand_code`) != LENGTH(`brand_code`)
	or channel_id is null or channel_id = '' or  CHAR_LENGTH(`channel_id`) != LENGTH(`channel_id`)
	or topic is null or topic = '' or  CHAR_LENGTH(`topic`) != LENGTH(`topic`)
	or content_type is null or content_type = '' or  CHAR_LENGTH(`content_type`) != LENGTH(`content_type`)
),
`brand_data` as (
    select `code` as `brand_code`,`name` as `brand_name`,`competitive_type` as `competitive_type` ,
   `automark` as `automark`,`is_core` from `voc_ext_ins_brand_info_mv`
)
select
    f.data_id,f.id, f.create_time, publish_time,  one_id, work_id, client_id, channel_id, content_type, sample_data_type, original_id, input_data_id, original_text_scene, f.brand_code, car_series_code, label_type, scenario, sentiment, intention_type, f.topic, opinion, subject, fault_level, f.description, sentiment_score, keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3, cust_ext_attrs, vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs, tags_ext_attrs, f.update_time, abandon, f.done, f.insert_dt
from raw_data as f
         left join brand_data as `d3` on `f`.`brand_code` = `d3`.`brand_code`
         left join `voc_ext_ins_channel_mv` as `d4` on `f`.`channel_id` = `d4`.`code`
         left join `voc_ext_ins_tag_system_final_mv` as `d6` ON `f`.`topic` = `d6`.`topic`
where d3.brand_code is  null
   or `d4`.`code` is null
   or `d6`.`topic` is null
;




-- drop MATERIALIZED VIEW voc_ins_model_new_words_mv
-- select * from voc_ins_model_new_words_mv
CREATE MATERIALIZED VIEW voc_ins_model_new_words_mv
BUILD IMMEDIATE REFRESH complete ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-30 03:30:00"
DUPLICATE KEY(`data_id`)
DISTRIBUTED BY HASH(`data_id`) BUCKETS 2
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"bloom_filter_columns" = "id,data_id",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS select
              `exploded_val`.`val_` as `data_id`,
              `voc_mysql_jdbc`.`voc_model`.`new_words_management`.`id`,
              now() as insert_dt
   from
              `voc_mysql_jdbc`.`voc_model`.`new_words_management`
                  LATERAL VIEW EXPLODE(
  cast (`voc_mysql_jdbc`.`voc_model`.`new_words_management`.`data_id_list` as Array<String>)
) `exploded_val` AS `val_`




-- drop MATERIALIZED VIEW voc_anal_di_pub_domain_data_batch_range_mv
CREATE MATERIALIZED VIEW voc_anal_di_pub_domain_data_batch_range_mv
(id,insert_dt,data,data_type)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 20 MINUTE STARTS "2026-01-30 11:39:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 6
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "insert_dt, data_type",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS
with raw_data as (
	select md5(id) as md5_id, id,insert_dt,data_type,data,ext_attrs
  	from voc_anal_di_raw_public_domain_data_full
	where `insert_dt`  >= NOW() - INTERVAL 2 hour
-- 	and id = '7882772'
	order by insert_dt desc
)
select `f1`.`id`,`f1`.`insert_dt`, `f1`.`data`, `f1`.`data_type`
from `raw_data` as `f1`
         left join `voc_anal_di_pub_domain_data_finished_record` as `f2`
                   on md5_id = `f2`.id
where  `f2`.`id` is null
    limit 50000;








-- drop MATERIALIZED VIEW voc_sentiment_annotations_no_results_to_avatr_mv;
CREATE MATERIALIZED VIEW voc_sentiment_annotations_no_results_to_avatr_mv
(id,data_id,publish_time,create_time,update_time,work_id,client_id,channel_code,content_type,type)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 120 MINUTE STARTS "2026-02-02 11:55:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 6
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "create_time, data_id",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS (
	select
	    `id`,
	    `data_id`,
	    `publish_time`,
	    `create_time`,
-- 	    `update_time`,
	   case
		when update_time is null then create_time
		else update_time
	end as update_time,
	    `work_id`,
	    `client_id`,
	    `channel_id` as `channel_code`,
	    `content_type`,
	    2 as `type`
	from
	    `voc_anal_flow_model_tags_unlabeled_data_full`
	where
-- 	    (`create_time` >=  NOW() - INTERVAL 7 day or `update_time` >=  NOW() - INTERVAL 7 day )
-- 	  and
	  (`channel_id` in (
	    'pdt_order_awtrxfw',
	    'pdt_consult_awtllzxzxdh',
	    'pdt_opinion_awtlyb',
	    'pdt_quest_awtzp',
	    'pdt_order_awtdmsgd',
	    'pdt_post_awtaxcx_k_sq',
	    'pdt_post_awtaxcx_k_zx',
	    'pdt_opinion_awtaxcx_bzyfk'
	  ))
-- 	  order by `id` limit 10
)
union all
(
	select
	    `id`,
	    `data_id`,
	    `publish_time`,
	    `create_time`,
	    create_time as `update_time`,
	    `work_id`,
	    `client_id`,
	    `channel_id` as `channel_code`,
	    `content_type`,
	    1 as `type`
	from
	    `voc_anal_flow_pre_rules_result_data_full`
	where
-- 	    (`create_time` >=  NOW() - INTERVAL 7 day )
-- 	  and
	  `abandon` = 1
	  and (`channel_id` in (
	    'pdt_order_awtrxfw',
	    'pdt_consult_awtllzxzxdh',
	    'pdt_opinion_awtlyb',
	    'pdt_quest_awtzp',
	    'pdt_order_awtdmsgd',
	    'pdt_post_awtaxcx_k_sq',
	    'pdt_post_awtaxcx_k_zx',
	    'pdt_opinion_awtaxcx_bzyfk'
	  ))

)



