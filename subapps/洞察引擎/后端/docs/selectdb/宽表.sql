-- SHOW PROC '/dbs/10084'

CREATE MATERIALIZED VIEW voc_anal_flow_sentiment_annotations_results_mv2
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-24 15:00:00"
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
"enable_nondeterministic_function" = "true"
)
AS
with `base_data` as (
    select
        `id`
         ,`publish_time`
         ,`data_id`
         ,`one_id`
         ,`work_id`
         ,`client_id`
         ,`channel_id`
--          ,`content_type`
--          , sample_data_type
         ,nvl(nullif(`content_type`, ''), null) as `content_type`
         ,nvl(nullif(`sample_data_type`, ''), null) as `sample_data_type`
         ,`original_id`
         ,`input_data_id`
--          ,`original_text_scene`
         ,nvl(nullif(`original_text_scene`, ''), null) as `original_text_scene`
         ,nvl(nullif(`brand_code`, ''), null) as `brand_code`
         ,nvl(nullif(`car_series_code`, ''), null) as `car_series_code`
--          ,`brand_code`
--          ,`car_series_code`
--          ,`label_type`
         ,nvl(nullif(`label_type`, ''), null) as `label_type`
         ,nvl(nullif(`sentiment`, ''), null) as `sentiment`
         ,nvl(nullif(`intention_type`, ''), null) as `intention_type`
--          ,`sentiment`
--          ,`intention_type`
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
    from `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full_mv`
     WHERE `brand_code` IS NOT NULL AND `brand_code` != '' AND `brand_code` REGEXP '^[^\\x{4e00}-\\x{9fff}]+$'
	  AND `car_series_code` REGEXP '^[^\\x{4e00}-\\x{9fff}]+$'
      and `channel_id` IS NOT NULL AND `channel_id` != '' AND `channel_id` REGEXP '^[^\\x{4e00}-\\x{9fff}]+$'
      and `topic` IS NOT NULL AND `topic` != '' AND `topic` REGEXP '^[^\\x{4e00}-\\x{9fff}]+$'
      and `content_type` IS NOT NULL AND `content_type` != '' AND `content_type` REGEXP '^[^\\x{4e00}-\\x{9fff}]+$'
),
`brand_data` as (
    select `internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`.`code` as `brand_code`,`internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`.`name` as `brand_name`,`internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`.`competitive_type` as `competitive_type` ,
   `internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`.`automark` as `automark`,`internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`.`is_core` from `internal`.`voc_ms_td`.`voc_ext_ins_brand_info_mv`
),
`car_series_data` as (
    select  `internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`code` as `car_series_code`, `internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`name` as `car_series_name` ,`internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`alias`,`internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`exclusion_words`,`internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`factory`,`internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`competitive_type`,
    `internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`.`competitive_product` from `internal`.`voc_ms_td`.`voc_ext_ins_car_series_info_mv`
)
select
    `f`.`id`	-- 声音ID
     ,`f`.`data_id`	-- 数据唯一标识
     ,`internal`.`voc_ms_td`.`d4`.`channel_catagory_level1`  as `channel_catagory`	-- 类别（长视频、社交媒体、资讯类等）		优化-渠道数据
     ,`f`.`channel_id`	as `channel_code`-- 渠道编码
     ,`internal`.`voc_ms_td`.`d4`.`name`  as `channel_name`	-- 渠道名称		优化-品牌车系数据
     -- ,`f`.`brand_code`	-- 品牌编码
     ,nvl(nullif(`f`.`brand_code`, ''), null) as `brand_code`	-- 品牌编码
     -- ,`d3`.`brand_name`	-- 品牌名称		优化-品牌车系数据
     ,nvl(nullif(`d3`.`brand_name`, ''), null) as `brand_name`	-- 品牌名称		优化-品牌车系数据
     -- ,`f`.`car_series_code`	-- 车系代码	biz_ext_attrs
     ,nvl(nullif(`f`.`car_series_code`, ''), null) as `car_series_code`	-- 车系代码	biz_ext_attrs
     -- ,`d5`.`car_series_name` -- 车系名称
     ,nvl(nullif(`d5`.`car_series_name`, ''), null) as `car_series_name`	-- 车系名称
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.model") as `model_name`	-- 车型代码	优化（需要和鹏飞确认）
     ,`f`.`content_type`	-- 数据类型(1：咨询/2：意⻅反馈/3：帖⼦评论/4：问卷/5：⼯单)
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.title") as `title`
     ,`f`.`sentiment`	-- 情感
     ,`f`.`intention_type` as `intention`	-- 意图（表扬/建议/咨询/抱怨)）
     ,to_date(`f`.`publish_time`) as `data_create_time`	-- 数据产生时间
     ,`f`.`publish_time`
     ,`f`.`create_time`	-- 数据抓取时间
     ,`f`.update_time
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.is_outer") AS `is_outer`	-- 内外数据？
     ,null as `hot_word`	-- 热词   优化（模型无返回值）
     ,`f`.`keywords`	-- 关键词
     ,`f`.`original_text_scene`  -- 声音片段
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.market_id") as `market_id`	-- 细分市场ID
     -- ,JSON_EXTRACT_STRING(f.biz_ext_attrs2, "$.competitive_type") as competitive_type -- 竞争力类型（1：传统 Competitive、2：新兴 Competitive、3：其他 Competitive）
     -- ,JSON_EXTRACT_STRING(f.biz_ext_attrs2, "$.series_factory") as series_factory -- 车系所属企业
     ,`d3`.`competitive_type` as `competitive_type`-- 车系所属企业
     ,`d3`.`is_core` as `is_core`
     ,`d5`.`factory` as `series_factory`-- 车系所属企业
     ,`d3`.`automark` as `automark`-- 车系所属企业
     ,ifnull(`f`.`one_id`, concat('n_',md5(concat(`f`.`channel_id`
    ,ifnull(JSON_EXTRACT_STRING(`f`.`raw_data`, "$.user_name"), '')
    ,ifnull(JSON_EXTRACT_STRING(`f`.`raw_data`, "$.user_id"), '') )))) as `one_id`	-- oneId	代入
     ,`internal`.`voc_ms_td`.`d6`.`user_journey1` as `user_journey1`	-- 旅程维度1（看车、购车等）	优化-标签数据
     ,`internal`.`voc_ms_td`.`d6`.`user_journey2` as `user_journey2`	-- 旅程维度2（高速路、高原等）	优化-标签数据
     ,`internal`.`voc_ms_td`.`d6`.`user_journey3` as `user_journey3`
     -- ,f.scenario	-- 关注场景领域（舒适性/材质/异响）
     ,JSON_EXTRACT_STRING(`f`.`ext_fields`, "$.usage_scenario_first") as `usage_scenario_first`
     ,JSON_EXTRACT_STRING(`f`.`ext_fields`, "$.usage_scenario_second") as `usage_scenario_second`
     ,`internal`.`voc_ms_td`.`d6`.`d2c_responsible_dept` as `d2c_responsible_dept`	-- 主责部门	代入		优化-标签数据
     ,`internal`.`voc_ms_td`.`d6`.`d2c_accountable_dept` as `d2c_accountable_dept`	-- 责任部门	代入		优化-标签数据
     ,`internal`.`voc_ms_td`.`d6`.`d2c_cc_dept`  as `d2c_cc_dept`	-- 抄送部门	代入				优化-标签数据
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.global_id") as `cust_global_id`	--	sso全局ID
     ,JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.cust_classify") as `cust_classify`	--	客户类型
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.mobile") as `cust_main_phone`	--	主手机号
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
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.vhl_vin") as `vhl_vin`	-- 车辆车架号
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
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.view_count") as `view_count`	-- 浏览量or播放量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.comment_count") as `comment_count`	-- 评论量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.like_count") as `like_count`	-- 点赞量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.share_count") as `share_count`	-- 转发量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.favorite_count") as `favorite_count` 	-- 收藏量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.order_id") as `work_order_id`	-- 工单ID
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.quest_id") as `quest_id`	-- 问卷ID
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.quest_type") as `quest_type`	-- 问卷类型
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.quest_answer_score") as `quest_answer_score`	-- 问卷答案分数
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.quest_business_type") as `quest_business_type`	-- 问卷业务类型
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.quest_business_scenario") as `quest_business_scenario`	-- 问卷业务场景
     ,`internal`.`voc_ms_td`.`d6`.`tag_accuracy` as `tag_accuracy`	-- 代码的精准性（精准、有待提升等）	标签
     ,`internal`.`voc_ms_td`.`d6`.`tag_customer_issue_classification` as `tag_customer_issue_classification`	-- 客户问题分级（S、A、B、C等）	标签
     ,`internal`.`voc_ms_td`.`d6`.`tag_issue_severity` as `tag_issue_severity`	-- 问题程度（高、中、低）	标签
     ,`internal`.`voc_ms_td`.`d6`.`tag_code_status` as `tag_code_status`	-- 代码状态（有效、无效等）	标签
     ,`internal`.`voc_ms_td`.`d6`.`tag_business_domain` as `tag_business_domain`	-- 业务领域（产品质量、产品设计、服务体验）	标签
     ,`internal`.`voc_ms_td`.`d6`.`event_clarity` as `tag_event_clarity`	-- 事件清晰度（印象、事实）	标签
     ,JSON_EXTRACT_STRING(`f`.`ext_fields`, "$.tag_high_value_flag") as `tag_high_value_flag`	-- 需推送的高价值建议标识	手动
     ,`internal`.`voc_ms_td`.`d6`.`tag_complaint_flag_needing_reply` as `tag_complaint_flag_needing_reply`	-- 需回评的抱怨标识	标签
     ,JSON_EXTRACT_STRING(`f`.`ext_fields`, "$.tag_complaint_flag_needing_prtv_msg") as `tag_complaint_flag_needing_prtv_msg`	-- 需私信的抱怨标识	手动
     ,JSON_EXTRACT_STRING(`f`.`ext_fields`, "$.tag_high_quality_voc_flag") as `tag_high_quality_voc_flag`	-- 针对五级明细高质量VOC标识	手动
     ,`internal`.`voc_ms_td`.`d6`.`tag_new_energy_or_fuel` as `tag_new_energy_or_fuel`	-- 新能源特有/燃油特有	标签
     ,`internal`.`voc_ms_td`.`d6`.`tag_need_forvclosed_loop` as `tag_need_forvclosed_loop`	-- 批量问题是否需要闭环（短平快、通用等）	标签
     ,`internal`.`voc_ms_td`.`d6`.`sort` as `tag_sort`
     ,`f`.`topic`	 -- 	观点（根因标签ID）
     ,`internal`.`voc_ms_td`.`d6`.`topic_text` as `topic_text` -- 观点
     ,`f`.`opinion` -- 	原始观点
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
     , nvl(NULLIF(`f`.`abandon`,'') ,null) as `abandon`
     , `f`.`insert_dt` as   `insert_dt`
from base_data as `f`
         left join brand_data as `d3` on `f`.`brand_code` = `d3`.`brand_code`
         left join `internal`.`voc_ms_td`.`voc_ext_ins_channel_mv` as `d4` on `f`.`channel_id` = `internal`.`voc_ms_td`.`d4`.`code`
         left join car_series_data as `d5` on `f`.`car_series_code` = `d5`.`car_series_code`
         left join `internal`.`voc_ms_td`.`voc_ext_ins_tag_system_final_mv` as `d6` ON `f`.`topic` = `internal`.`voc_ms_td`.`d6`.`topic`





CREATE INDEX index_data_create_time ON voc_anal_flow_sentiment_annotations_results_mv_20251212 (data_create_time)
    USING INVERTED COMMENT '';
CREATE INDEX index_abandon ON voc_anal_flow_sentiment_annotations_results_mv_20251212 (abandon)
    USING INVERTED COMMENT '';

CREATE INDEX index_sentiment ON voc_anal_flow_sentiment_annotations_results_mv_20251212 (sentiment)
    USING INVERTED COMMENT '';
CREATE INDEX index_channel_code ON voc_anal_flow_sentiment_annotations_results_mv_20251212 (channel_code)
    USING INVERTED COMMENT '';






-- drop  MATERIALIZED VIEW voc_anal_flow_sentiment_annotations_results_ins_mv_20251212
CREATE MATERIALIZED VIEW voc_anal_flow_sentiment_annotations_results_ins_mv_20251212
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 15 MINUTE STARTS "2025-12-12 18:06:00"
DUPLICATE KEY(`id`)
PARTITION BY (date_trunc(`publish_time`, 'month'))
DISTRIBUTED BY HASH(`id`) BUCKETS 16
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "channel_code,brand_code,car_series_code,topic"
-- "enable_function_pushdown" = "true"
)
as
with result_data as (
	select *
	from voc_anal_flow_sentiment_annotations_results_mv_20251212
)
select
    f1.*,
    JSON_EXTRACT_STRING(`raw_data`, "$.content") as content
from result_data as f1
         left join voc_anal_flow_model_tags_result_data_full as f2
                   on f1.id = f2.id





-- SHOW INDEX FROM voc_anal_flow_sentiment_annotations_results_ins_mv_20251212;
CREATE INDEX idx_mv_results_ins
    ON voc_anal_flow_sentiment_annotations_results_ins_mv_20251212(content)
    USING NGRAM_BF PROPERTIES("gram_size"="3", "bf_size"="1024") COMMENT 'content ngram_bf index';






-- drop MATERIALIZED VIEW voc_anal_di_stg_mate_data_m_inc_mv
CREATE MATERIALIZED VIEW voc_anal_di_stg_mate_data_m_inc_mv
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 5 MINUTE STARTS "2025-11-14 15:30:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "data_id, channel_code",
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
with `mate_data` as (
--  私域渠道范围
    select
        `id`,
        `data_create_time`,
        `create_time`,
        `content_type`,
        `data_update_time`,
        `data_id`,
        `channel_code`,
        `brand`,
        `series`,
        `model`,
        `is_outer`,
        `one_id`,
        `id_car_no`,
        `mobile`,
        `email`,
        `global_id`,
        `user_id`,
        `user_name`,
        `vhl_id`,
        `vhl_vin`,
        `dlr_id`,
        `dlr_code`,
        `dlr_type`,
        `market_id`,
        `title`,
        `content`,
        `is_wsater_army`,
        `weight`,
        `attrs`,
        `attrs2`,
        `attrs3`,
        `work_id`,
        `model_type`
    from
        `voc_anal_di_stg_mate_data_m_inc`
    where
        `insert_dt` >= NOW() - INTERVAL 1 HOUR
      and `channel_code` is not null
),
     `encrypted_data` AS (
         SELECT
             `id`,
             `data_create_time`,
             `create_time`,
             `content_type`,
             `data_update_time`,
             `data_id`,
             `channel_code`,
             `brand`,
             `series`,
             `model`,
             `is_outer`,
             `one_id`,
             `id_car_no`,
             `mobile`,
             `email`,
             `global_id`,
--              `user_id`,
             concat('u_',md5(concat('_',COALESCE(`user_id`,`user_name`,uuid()), `channel_code` ))) as `user_id`,
             `user_name`,
             `vhl_id`,
             `vhl_vin`,
             `dlr_id`,
             `dlr_code`,
             `dlr_type`,
             `market_id`,
             `title`,
             `content`,
             `is_wsater_army`,
             `weight`,
             `attrs`,
             `attrs2`,
             `attrs3`,
             `work_id`,
             `model_type`,
             -- 加密手机号
             CASE
                 WHEN `mobile` REGEXP '^1[3-9]\\d{9}$' THEN
                     TO_BASE64(SM4_ENCRYPT(`mobile`, 'changanvoc2025xx'))
                 else `mobile`
                 END AS `mobile_enc`,
             CASE
                 WHEN `id_car_no` REGEXP '^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dX]$'
                     OR `id_car_no` REGEXP '^[1-9]\\d{5}\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}$'
                     THEN TO_BASE64(SM4_ENCRYPT(`id_car_no`, 'changanvoc2025xx'))
                 ELSE NULL
                 END AS `id_card_no_enc`
         FROM mate_data
     ),
     `marge_data` as (
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
             `e`.`one_id`,
             `e`.`id_card_no_enc` as `id_car_no`,
             `e`.`mobile_enc` as `mobile`,
             `e`.`email`,
             `e`.`global_id`,
             `e`.`user_id`,
             `e`.`user_name`,
             `e`.`vhl_id`,
             `e`.`vhl_vin`,
             `e`.`dlr_id`,
             `e`.`dlr_code`,
             `e`.`dlr_type`,
             `e`.`market_id`,
             `e`.`title`,
             `e`.`content`,
             `e`.`is_wsater_army`,
             `e`.`weight` ,
             `e`.`attrs`,
             `e`.`attrs2`,
             `e`.`attrs3`,
             `e`.`work_id`,
             `e`.`model_type`,
             COALESCE(`b1`.`cust_json`, `b2`.`cust_json`) as `cust_ext_attrs`,
             COALESCE(`b3`.`vehicle_json`, `b1`.`vehicle_json`, `b2`.`vehicle_json`) as `vhl_ext_attrs`,
             `d1`.`dealer_json` as `dealer_ext_attrs`,
             null as `prd_ext_attrs`
         FROM
             encrypted_data as `e`
                 LEFT JOIN `voc_imp_cust_vehicle_rel_json_info_mv` as `b1`
                           on
                               `e`.`is_outer` = 'N'
                                   and `b1`.`one_id` = `e`.`one_id`
                 LEFT JOIN `voc_imp_cust_vehicle_rel_json_info_mv` as `b2`
                           ON
                               `e`.`is_outer` = 'N'
                                   and `b2`.`mobile` = `e`.`mobile_enc`
                 LEFT JOIN `voc_imp_cust_vehicle_rel_json_info_mv` as `b3`
                           ON
                               `e`.`is_outer` = 'N'
                                   and `b3`.`vin` = `e`.`vhl_vin`
                 LEFT JOIN `voc_imp_dealer_json_info_mv` as `d1`
                           ON
                               `e`.`is_outer` = 'N'
                                   and `d1`.`dealer_code` = `e`.`dlr_code`
         order by
             `e`.`create_time` desc
     )
select
    `id`,
    `data_create_time`,
    `create_time`,
    `content_type`,
    `data_update_time`,
    `data_id`,
    `channel_code`,
    `brand`,
    `series`,
    `model`,
    `is_outer`,
    -- 	`one_id`,
    case
        when `one_id` is not null then `one_id`
        when JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id") is not null then JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id")
        when `is_outer` = 'Y' then concat('o_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        when `is_outer` = 'N' then concat('i_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        else concat('x_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        end as `one_id` ,
    `id_car_no`,
    `mobile`,
    `email`,
    `global_id`,
    `user_id`,
    `user_name`,
    `vhl_id`,
    `vhl_vin`,
    `dlr_id`,
    `dlr_code`,
    `dlr_type`,
    `market_id`,
    `title`,
    `content`,
    `is_wsater_army`,
    `weight` ,
    `attrs`,
    `attrs2`,
    `attrs3`,
    `work_id`,
    `model_type`,
    `cust_ext_attrs`,
    `vhl_ext_attrs`,
    `dealer_ext_attrs`,
    `prd_ext_attrs`,
    now() as `insert_dt`
from
    marge_data
;






-- create or replace
-- view `voc_anal_flow_mate_data_full_v` as

-- drop MATERIALIZED VIEW voc_anal_flow_mate_data_full_mv
CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_full_mv
REFRESH AUTO ON SCHEDULE EVERY 20 MINUTE STARTS "2025-12-16 18:23:00"
partition by (date_trunc(`create_time`,'month'))
DISTRIBUTED BY HASH (`id`) BUCKETS 32
PROPERTIES (
   "replication_num" = "3",
   "bloom_filter_columns" = "data_id,channel_code,brand,series,data_create_time",
   "enable_nondeterministic_function"  = "true"
)
as
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
    now() as `insert_dt`
from
    `voc_anal_flow_mate_data_full`

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
        when `f2`.`data_id` is null  then 3
        else 2
        end as `data_status`
from raw_data as `f1`
         left join voc_anal_flow_mate_data_labeled_mv as `f2` on
    `f1`.`data_id` = `f2`.`data_id`;








# create or replace view voc_anal_di_stg_mate_data_m_inc_v
# as
# select * from voc_anal_di_stg_mate_data_m_inc_mv
# order by create_time , data_create_time,id
# limit 1000;

-- drop MATERIALIZED VIEW voc_anal_flow_mate_data_labeled_mv
CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_labeled_mv
REFRESH AUTO ON SCHEDULE EVERY 20 MINUTE STARTS "2025-11-30 12:30:00"
partition by (date_trunc(`data_create_time`,'month'))
DISTRIBUTED BY HASH (`id`) BUCKETS 16
PROPERTIES (
   "replication_num" = "3",
   "bloom_filter_columns" = "data_id,channel_code,done",
   "enable_nondeterministic_function"  = "true"
)
as
with labeled_data as (
	-- 已打标数据范围
	select data_id from voc_anal_flow_mate_data_status_mv where status = 3
)
SELECT
    `id`,
    `data_id`,
    JSON_EXTRACT_STRING(`data`, "$.data_update_time") as data_update_time,
    create_time,
    JSON_EXTRACT_STRING(`data`, "$.content_type") as content_type,
    publish_time AS data_create_time,
    JSON_EXTRACT_STRING(`data`, "$.channel_code") AS `channel_code`,
    JSON_EXTRACT_STRING(`data`, "$.brand") AS `brand`,
    JSON_EXTRACT_STRING(`data`, "$.series") AS `series`,
    JSON_EXTRACT_STRING(`data`, "$.model") AS `model`,
    JSON_EXTRACT_STRING(`data`, "$.is_outer") AS `is_outer`,
    JSON_EXTRACT_STRING(`data`, "$.one_id") AS `one_id`,
    JSON_EXTRACT_STRING(`data`, "$.id_car_no") AS  `id_car_no`,
    JSON_EXTRACT_STRING(`data`, "$.mobile") AS `mobile`,
    JSON_EXTRACT_STRING(`data`, "$.email") AS `email`,
    JSON_EXTRACT_STRING(`data`, "$.global_id") AS `global_id`,
    JSON_EXTRACT_STRING(`data`, "$.user_id") AS `user_id`,
    JSON_EXTRACT_STRING(`data`, "$.user_name") AS `user_name`,
    JSON_EXTRACT_STRING(`data`, "$.vhl_id") AS `vhl_id`,
    JSON_EXTRACT_STRING(`data`, "$.vhl_vin") AS `vhl_vin`,
    JSON_EXTRACT_STRING(`data`, "$.dlr_id") AS `dlr_id`,
    JSON_EXTRACT_STRING(`data`, "$.dlr_code") AS `dlr_code`,
    JSON_EXTRACT_STRING(`data`, "$.dlr_type") AS `dlr_type`,
    JSON_EXTRACT_STRING(`data`, "$.market_id") AS `market_id`,
    JSON_EXTRACT_STRING(`data`, "$.title") AS `title`,
    JSON_EXTRACT_STRING(`data`, "$.content") AS `content`,
    JSON_EXTRACT_STRING(`data`, "$.is_wsater_army") as `is_wsater_army`,
    '0' AS `weight`,
    `biz_ext_attrs` AS `attrs`,
    `biz_ext_attrs2` AS `attrs2`,
    `biz_ext_attrs3` AS `attrs3`,
    `work_id`,
    `done`,
    JSON_EXTRACT_STRING(`data`, "$.model_type") as`model_type`,
    null as ds,
    now() as insert_dt
FROM voc_anal_flow_mate_data_full
where data_id in (
    select data_id from labeled_data
)
;






-- DROP JOB where jobName='job_voc_anal_di_stg_mate_data_merge_m_inc';
-- select * from jobs("type"="insert") where Name="job_voc_anal_di_stg_mate_data_merge_m_inc";
-- select * from tasks("type"="insert");
CREATE JOB job_voc_anal_di_stg_mate_data_merge_m_inc
ON SCHEDULE EVERY 5 MINUTE STARTS '2025-11-14 17:50:00'
DO
    INSERT INTO voc_anal_di_stg_mate_data_merge_m_inc(
	id,data_create_time,create_time,content_type,data_update_time,data_id,channel_code,brand,series,model,
	is_outer,one_id,id_car_no,mobile,email,global_id,user_id,user_name,vhl_id,vhl_vin,dlr_id,dlr_code,dlr_type,
	market_id,title,content,is_wsater_army,weight,attrs,attrs2,attrs3,work_id,model_type,
	cust_ext_attrs,vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs,insert_dt
)
with `mate_data` as (
--  私域渠道范围
    select
        `id`,
        `data_create_time`,
        `create_time`,
        `content_type`,
        `data_update_time`,
        `data_id`,
        `channel_code`,
        `brand`,
        `series`,
        `model`,
        `is_outer`,
        `one_id`,
        `id_car_no`,
        `mobile`,
        `email`,
        `global_id`,
        `user_id`,
        `user_name`,
        `vhl_id`,
        `vhl_vin`,
        `dlr_id`,
        `dlr_code`,
        `dlr_type`,
        `market_id`,
        `title`,
        `content`,
        `is_wsater_army`,
        `weight`,
        `attrs`,
        `attrs2`,
        `attrs3`,
        `work_id`,
        `model_type`
    from
        `voc_anal_di_stg_mate_data_m_inc`
    where
        `insert_dt` >= NOW() - INTERVAL 1 HOUR
      and `channel_code` is not null
),
filter_out_merged as (
	select f1.* from mate_data as f1
	left join (
		select id
		from  `voc_anal_di_stg_mate_data_merge_m_inc`
	    where  `insert_dt` >= NOW() - INTERVAL 24 HOUR
	    and `channel_code` is not null
	) as f2 on f1.id = f2.id
	where f2.id is null
	and length(attrs) < 900000 and length(content) < 900000
),
`encrypted_data` AS (
         SELECT
             `id`,
             `data_create_time`,
             `create_time`,
             `content_type`,
             `data_update_time`,
             `data_id`,
             `channel_code`,
             `brand`,
             `series`,
             `model`,
             `is_outer`,
             `one_id`,
             `id_car_no`,
             `mobile`,
             `email`,
             `global_id`,
--              `user_id`,
             concat('u_',md5(concat('_',COALESCE(`user_id`,`user_name`,uuid()), `channel_code` ))) as `user_id`,
             `user_name`,
             `vhl_id`,
             `vhl_vin`,
             `dlr_id`,
             `dlr_code`,
             `dlr_type`,
             `market_id`,
             `title`,
             `content`,
             `is_wsater_army`,
             `weight`,
             `attrs`,
             `attrs2`,
             `attrs3`,
             `work_id`,
             `model_type`,
             -- 加密手机号
             CASE
                 WHEN `mobile` REGEXP '^1[3-9]\\d{9}$' THEN
                     TO_BASE64(SM4_ENCRYPT(`mobile`, 'changanvoc2025xx'))
                 else `mobile`
                 END AS `mobile_enc`,
             CASE
                 WHEN `id_car_no` REGEXP '^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dX]$'
                     OR `id_car_no` REGEXP '^[1-9]\\d{5}\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}$'
                     THEN TO_BASE64(SM4_ENCRYPT(`id_car_no`, 'changanvoc2025xx'))
                 ELSE NULL
                 END AS `id_card_no_enc`
         FROM filter_out_merged
     ),
     `marge_data` as (
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
             `e`.`one_id`,
             `e`.`id_card_no_enc` as `id_car_no`,
             `e`.`mobile_enc` as `mobile`,
             `e`.`email`,
             `e`.`global_id`,
             `e`.`user_id`,
             `e`.`user_name`,
             `e`.`vhl_id`,
             `e`.`vhl_vin`,
             `e`.`dlr_id`,
             `e`.`dlr_code`,
             `e`.`dlr_type`,
             `e`.`market_id`,
             `e`.`title`,
             `e`.`content`,
             `e`.`is_wsater_army`,
             `e`.`weight` ,
             `e`.`attrs`,
             `e`.`attrs2`,
             `e`.`attrs3`,
             `e`.`work_id`,
             `e`.`model_type`,
             COALESCE(`b1`.`cust_json`, `b2`.`cust_json`) as `cust_ext_attrs`,
             COALESCE(`b3`.`vehicle_json`, `b1`.`vehicle_json`, `b2`.`vehicle_json`) as `vhl_ext_attrs`,
             `d1`.`dealer_json` as `dealer_ext_attrs`,
             null as `prd_ext_attrs`
         FROM
             encrypted_data as `e`
                 LEFT JOIN `voc_imp_cust_vehicle_rel_json_info_mv` as `b1`
                           on
                               `e`.`is_outer` = 'N'
                                   and `b1`.`one_id` = `e`.`one_id`
                 LEFT JOIN `voc_imp_cust_vehicle_rel_json_info_mv` as `b2`
                           ON
                               `e`.`is_outer` = 'N'
                                   and `b2`.`mobile` = `e`.`mobile_enc`
                 LEFT JOIN `voc_imp_cust_vehicle_rel_json_info_mv` as `b3`
                           ON
                               `e`.`is_outer` = 'N'
                                   and `b3`.`vin` = `e`.`vhl_vin`
                 LEFT JOIN `voc_imp_dealer_json_info_mv` as `d1`
                           ON
                               `e`.`is_outer` = 'N'
                                   and `d1`.`dealer_code` = `e`.`dlr_code`
         order by
             `e`.`create_time` desc
     )
select
    `id`,
    `data_create_time`,
    `create_time`,
    `content_type`,
    `data_update_time`,
    `data_id`,
    `channel_code`,
    `brand`,
    `series`,
    `model`,
    `is_outer`,
    -- 	`one_id`,
    case
        when `one_id` is not null then `one_id`
        when JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id") is not null then JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id")
        when `is_outer` = 'Y' then concat('o_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        when `is_outer` = 'N' then concat('i_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        else concat('x_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        end as `one_id` ,
    `id_car_no`,
    `mobile`,
    `email`,
    `global_id`,
    `user_id`,
    `user_name`,
    `vhl_id`,
    `vhl_vin`,
    `dlr_id`,
    `dlr_code`,
    `dlr_type`,
    `market_id`,
    `title`,
    `content`,
    `is_wsater_army`,
    `weight` ,
    `attrs`,
    `attrs2`,
    `attrs3`,
    `work_id`,
    `model_type`,
    `cust_ext_attrs`,
    `vhl_ext_attrs`,
    `dealer_ext_attrs`,
    `prd_ext_attrs`,
    now() as `insert_dt`
from
    marge_data
;




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
	select `f`.`rn`, `internal`.`voc_ms_td`.`f`.`sk_id`, `internal`.`voc_ms_td`.`f`.`dlr_cd`, `internal`.`voc_ms_td`.`f`.`erp_cd`, `internal`.`voc_ms_td`.`f`.`dlr_nm`, `internal`.`voc_ms_td`.`f`.`dlr_s_nm`, `internal`.`voc_ms_td`.`f`.`dept_cd`, `internal`.`voc_ms_td`.`f`.`dept_nm`, `internal`.`voc_ms_td`.`f`.`seq`, `internal`.`voc_ms_td`.`f`.`store_lvl`, `internal`.`voc_ms_td`.`f`.`invest_group_cd`, `internal`.`voc_ms_td`.`f`.`invest_group_nm`, `internal`.`voc_ms_td`.`f`.`invest_cd`, `internal`.`voc_ms_td`.`f`.`invest_nm`, `internal`.`voc_ms_td`.`f`.`s1_dlr_cd`, `internal`.`voc_ms_td`.`f`.`s1_dlr_nm`, `internal`.`voc_ms_td`.`f`.`s2_dlr_cd`, `internal`.`voc_ms_td`.`f`.`s2_dlr_nm`, `internal`.`voc_ms_td`.`f`.`main_invest_cd`, `internal`.`voc_ms_td`.`f`.`main_invest_nm`, `internal`.`voc_ms_td`.`f`.`main_dlr_cd`, `internal`.`voc_ms_td`.`f`.`main_dlr_nm`, `internal`.`voc_ms_td`.`f`.`ord_flg`, `internal`.`voc_ms_td`.`f`.`dlv_flg`, `internal`.`voc_ms_td`.`f`.`svs_flg`, `internal`.`voc_ms_td`.`f`.`paint_flg`, `internal`.`voc_ms_td`.`f`.`valid_flg`, `internal`.`voc_ms_td`.`f`.`quit_flg`, `internal`.`voc_ms_td`.`f`.`rez_flg`, `internal`.`voc_ms_td`.`f`.`dlr_type_group_cd`, `internal`.`voc_ms_td`.`f`.`dlr_type_group_nm`, `internal`.`voc_ms_td`.`f`.`dlr_type_cd`, `internal`.`voc_ms_td`.`f`.`dlr_type_nm`, `internal`.`voc_ms_td`.`f`.`chn_type_cd`, `internal`.`voc_ms_td`.`f`.`chn_type_nm`, `internal`.`voc_ms_td`.`f`.`chn_lvl_cd`, `internal`.`voc_ms_td`.`f`.`chn_lvl_nm`, `internal`.`voc_ms_td`.`f`.`image_cd`, `internal`.`voc_ms_td`.`f`.`image_nm`, `internal`.`voc_ms_td`.`f`.`image_lvl_cd`, `internal`.`voc_ms_td`.`f`.`image_lvl_nm`, `internal`.`voc_ms_td`.`f`.`store_front_function`, `internal`.`voc_ms_td`.`f`.`current_state`, `internal`.`voc_ms_td`.`f`.`operation_type`, `internal`.`voc_ms_td`.`f`.`area`, `internal`.`voc_ms_td`.`f`.`war_zone_cd`, `internal`.`voc_ms_td`.`f`.`war_zone_nm`, `internal`.`voc_ms_td`.`f`.`war_zone_part`, `internal`.`voc_ms_td`.`f`.`war_zone_part_user_cd`, `internal`.`voc_ms_td`.`f`.`war_zone_part_user_nm`, `internal`.`voc_ms_td`.`f`.`sdu`, `internal`.`voc_ms_td`.`f`.`sdu_link_man`, `internal`.`voc_ms_td`.`f`.`prov_cd`, `internal`.`voc_ms_td`.`f`.`city_cd`, `internal`.`voc_ms_td`.`f`.`cty_cd`, `internal`.`voc_ms_td`.`f`.`prov_nm`, `internal`.`voc_ms_td`.`f`.`city_nm`, `internal`.`voc_ms_td`.`f`.`cty_nm`, `internal`.`voc_ms_td`.`f`.`lng`, `internal`.`voc_ms_td`.`f`.`lat`, `internal`.`voc_ms_td`.`f`.`addr`, `internal`.`voc_ms_td`.`f`.`bank_nm`, `internal`.`voc_ms_td`.`f`.`bank_acct_no`, `internal`.`voc_ms_td`.`f`.`manager_nm`, `internal`.`voc_ms_td`.`f`.`manager_tel`, `internal`.`voc_ms_td`.`f`.`hotline`, `internal`.`voc_ms_td`.`f`.`emergency_tel`, `internal`.`voc_ms_td`.`f`.`in_net_dt`, `internal`.`voc_ms_td`.`f`.`out_net_dt`, `internal`.`voc_ms_td`.`f`.`network_type`, `internal`.`voc_ms_td`.`f`.`ord_dlr_cd`, `internal`.`voc_ms_td`.`f`.`dlv_dlr_cd`, `internal`.`voc_ms_td`.`f`.`svs_dlr_cd`, `internal`.`voc_ms_td`.`f`.`biz_cd`, `internal`.`voc_ms_td`.`f`.`biz_nm`, `internal`.`voc_ms_td`.`f`.`uni_dlr_cd_flg`, `internal`.`voc_ms_td`.`f`.`src_sys`, `internal`.`voc_ms_td`.`f`.`src_sys_id`, `internal`.`voc_ms_td`.`f`.`zip_start_dt`, `internal`.`voc_ms_td`.`f`.`zip_end_dt`, `internal`.`voc_ms_td`.`f`.`zip_enable_flg`, `internal`.`voc_ms_td`.`f`.`insert_dt`, `internal`.`voc_ms_td`.`f`.`job_nm`, `internal`.`voc_ms_td`.`f`.`ds` from (
		select
			ROW_NUMBER() OVER (PARTITION BY `dlr_cd` ORDER BY `zip_start_dt` DESC) AS `rn`
			,`sk_id`, `dlr_cd`, `erp_cd`, `dlr_nm`, `dlr_s_nm`, `dept_cd`, `dept_nm`, `seq`, `store_lvl`, `invest_group_cd`, `invest_group_nm`, `invest_cd`, `invest_nm`, `s1_dlr_cd`, `s1_dlr_nm`, `s2_dlr_cd`, `s2_dlr_nm`, `main_invest_cd`, `main_invest_nm`, `main_dlr_cd`, `main_dlr_nm`, `ord_flg`, `dlv_flg`, `svs_flg`, `paint_flg`, `valid_flg`, `quit_flg`, `rez_flg`, `dlr_type_group_cd`, `dlr_type_group_nm`, `dlr_type_cd`, `dlr_type_nm`, `chn_type_cd`, `chn_type_nm`, `chn_lvl_cd`, `chn_lvl_nm`, `image_cd`, `image_nm`, `image_lvl_cd`, `image_lvl_nm`, `store_front_function`, `current_state`, `operation_type`, `area`, `war_zone_cd`, `war_zone_nm`, `war_zone_part`, `war_zone_part_user_cd`, `war_zone_part_user_nm`, `sdu`, `sdu_link_man`, `prov_cd`, `city_cd`, `cty_cd`, `prov_nm`, `city_nm`, `cty_nm`, `lng`, `lat`, `addr`, `bank_nm`, `bank_acct_no`, `manager_nm`, `manager_tel`, `hotline`, `emergency_tel`, `in_net_dt`, `out_net_dt`, `network_type`, `ord_dlr_cd`, `dlv_dlr_cd`, `svs_dlr_cd`, `biz_cd`, `biz_nm`, `uni_dlr_cd_flg`, `src_sys`, `src_sys_id`, `zip_start_dt`, `zip_end_dt`, `zip_enable_flg`, `insert_dt`, `job_nm`, `ds`
		from `internal`.`voc_ms_td`.`voc_imp_hudi_dim_chn_dlr_zip_d_full`
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
from raw_data  as  `c`




-- voc_ms_td.voc_anal_di_stg_mate_data_m_inc_real_time_v source

create or replace
view `voc_anal_di_stg_mate_data_m_inc_real_time_v` as
with `time_wind` as (
select
    exploded_val.val_ as `n`
from
    (
    select
        sequence(0, 3 * 24) as `val_`
    ) `f1`
    lateral view EXPLODE(`f1`.`val_`) `exploded_val` as `val_`
),
`window_base` as (
select
        `time_wind`.`n`,
        FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(NOW()) / 1200) * 1200 - `time_wind`.`n` * 1200) as `window_start`,
        FLOOR(UNIX_TIMESTAMP(NOW()) / 1200) * 1200 - `time_wind`.`n` * 1200 as `window_ts`
from
    time_wind
),
-- di: voc_anal_di_stg_mate_data_m_inc
`data_aligned` as (
select
        `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_m_inc`.`data_id`,
        FLOOR(UNIX_TIMESTAMP(`internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_m_inc`.`data_create_time`) / 1200) * 1200 as `event_window_ts`,
        `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_m_inc`.`insert_dt`
from
    `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_m_inc`
where
        `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_m_inc`.`data_create_time` >= DATE_SUB(NOW(), interval 24 hour)
    and `internal`.`voc_ms_td`.`voc_anal_di_stg_mate_data_m_inc`.`data_create_time` is not null),
`stats` as (
select
        event_window_ts,
        COUNT(distinct `data_aligned`.`data_id`) as `unique_data_id_count`,
        MAX(`data_aligned`.`insert_dt`) as `max_insert_dt`
from
    data_aligned
group by
    `data_aligned`.`event_window_ts`),
`di_data` as (
select
        `tw`.`window_start` as `publish_time`,
        coalesce(`s`.`unique_data_id_count`, 0) as `id_count`,
        `s`.`max_insert_dt`
from
    window_base `tw`
left join stats `s` on
    `tw`.`window_ts` = `s`.`event_window_ts`),
-- mate: voc_anal_flow_mate_data_full
`mate_data_aligned` as (
select
        `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data_id`,
        FLOOR(UNIX_TIMESTAMP(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`publish_time`) / 1200) * 1200 as `event_window_ts`,
        `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`insert_dt`
from
    `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`
where
        `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`publish_time` >= DATE_SUB(NOW(), interval 24 hour)
        and `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`publish_time` is not null
),
`mate_stats` as (
select
        event_window_ts,
        COUNT(distinct `mate_data_aligned`.`data_id`) as `unique_data_id_count`,
        MAX(`mate_data_aligned`.`insert_dt`) as `max_insert_dt`
from
    mate_data_aligned
group by
    `mate_data_aligned`.`event_window_ts`
),
`mate_data` as (
select
        `tw`.`window_start` as `publish_time`,
        coalesce(`s`.`unique_data_id_count`, 0) as `id_count`,
        `s`.`max_insert_dt`
from
    window_base `tw`
left join mate_stats `s` on
    `tw`.`window_ts` = `s`.`event_window_ts`
),
-- pre: voc_anal_flow_pre_rules_result_data_full (abandon = 0)
`pre_data_aligned` as (
select
       distinct `internal`.`voc_ms_td`.`voc_anal_flow_pre_rules_result_data_full`.`data_id`,
       FLOOR(UNIX_TIMESTAMP(`internal`.`voc_ms_td`.`voc_anal_flow_pre_rules_result_data_full`.`publish_time`) / 1200) * 1200 as `event_window_ts`,
       JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_pre_rules_result_data_full`.`data`, "$.is_outer") as `is_outer`,
       `internal`.`voc_ms_td`.`voc_anal_flow_pre_rules_result_data_full`.`insert_dt`
from
    `internal`.`voc_ms_td`.`voc_anal_flow_pre_rules_result_data_full`
where
     `internal`.`voc_ms_td`.`voc_anal_flow_pre_rules_result_data_full`.`publish_time` >= DATE_SUB(NOW(), interval 24 hour)
        and `internal`.`voc_ms_td`.`voc_anal_flow_pre_rules_result_data_full`.`abandon` <> 1
),
`pre_stats` as (
select
    event_window_ts,
    count(`pre_data_aligned`.`data_id`) as `unique_data_id_count`,
    `is_outer`,
    MAX(`pre_data_aligned`.`insert_dt`) as `max_insert_dt`
from
    pre_data_aligned
group by
    `pre_data_aligned`.`event_window_ts`,
    `pre_data_aligned`.`is_outer`
),
`pre_data` as (
select
    `wb`.`window_start` as `publish_time`,
           coalesce(`s`.`unique_data_id_count`, 0) as `id_count`,
    `s`.`is_outer`,
           `s`.`max_insert_dt`
from
    window_base `wb`
left join pre_stats `s` on
    `wb`.`window_ts` = `s`.`event_window_ts`
),
-- pre: voc_anal_flow_pre_rules_result_data_full (abandon = 0)
`tomodel_aligned` as (
select
        `internal`.`voc_ms_td`.`voc_anal_flow_to_model_data`.`data_id`,
        FLOOR(UNIX_TIMESTAMP(`internal`.`voc_ms_td`.`voc_anal_flow_to_model_data`.`publish_time`) / 1200) * 1200 as `event_window_ts`,
        `internal`.`voc_ms_td`.`voc_anal_flow_to_model_data`.`insert_dt`
from
    `internal`.`voc_ms_td`.`voc_anal_flow_to_model_data`
where
    `internal`.`voc_ms_td`.`voc_anal_flow_to_model_data`.`publish_time` >= DATE_SUB(NOW(), interval 24 hour)
),
`tomodel_stats` as (
select
    event_window_ts,
    COUNT(distinct `tomodel_aligned`.`data_id`) as `unique_data_id_count`,
    MAX(`tomodel_aligned`.`insert_dt`) as `max_insert_dt`
from
    tomodel_aligned
group by
    `tomodel_aligned`.`event_window_ts`
),
`tomodel_data` as (
select
    `wb`.`window_start` as `publish_time`,
           coalesce(`s`.`unique_data_id_count`, 0) as `id_count`,
           `s`.`max_insert_dt`
from
    window_base `wb`
left join tomodel_stats `s` on
    `wb`.`window_ts` = `s`.`event_window_ts`
),
-- labeled: voc_anal_flow_model_tags_result_data_full
`labeled_data_aligned` as (
select
    distinct `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`data_id`,
    FLOOR(UNIX_TIMESTAMP(`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`publish_time`) / 1200) * 1200 as `event_window_ts`,
     JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`raw_data`, "$.is_outer") as `is_outer`,
    `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`insert_dt`
from
    `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`
where
     `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`publish_time` >= DATE_SUB(NOW(), interval 24 hour)
),
`labeled_stats` as (
select
    event_window_ts,
    COUNT(distinct `labeled_data_aligned`.`data_id`) as `unique_data_id_count`,
    ANY_VALUE(`labeled_data_aligned`.`is_outer`) as `is_outer`,
    MAX(`labeled_data_aligned`.`insert_dt`) as `max_insert_dt`
from
    labeled_data_aligned
group by
    `labeled_data_aligned`.`event_window_ts`,
    `labeled_data_aligned`.`is_outer`
 ),
`labeled_data` as (
select
    `wb`.`window_start` as `publish_time`,
           coalesce(`s`.`unique_data_id_count`, 0) as `id_count`,
    `s`.`is_outer`,
           `s`.`max_insert_dt`
from
    window_base `wb`
left join labeled_stats `s` on
    `wb`.`window_ts` = `s`.`event_window_ts`
),
-- unlabeled: voc_anal_flow_model_tags_unlabeled_data_full
`unlabeled_data_aligned` as (
select
        `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_unlabeled_data_full`.`data_id`,
        FLOOR(UNIX_TIMESTAMP(`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_unlabeled_data_full`.`publish_time`) / 1200) * 1200 as `event_window_ts`,
        `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_unlabeled_data_full`.`insert_dt`
from
    `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_unlabeled_data_full`
where
        `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_unlabeled_data_full`.`publish_time` >= DATE_SUB(NOW(), interval 24 hour)
        and `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_unlabeled_data_full`.`publish_time` is not null
),
`unlabeled_stats` as (
select
        event_window_ts,
        COUNT(distinct `unlabeled_data_aligned`.`data_id`) as `unique_data_id_count`,
        MAX(`unlabeled_data_aligned`.`insert_dt`) as `max_insert_dt`
from
    unlabeled_data_aligned
group by
    `unlabeled_data_aligned`.`event_window_ts`
),
`unlabeled_data` as (
select
        `tw`.`window_start` as `publish_time`,
        coalesce(`s`.`unique_data_id_count`, 0) as `id_count`,
        `s`.`max_insert_dt`
from
    window_base `tw`
left join unlabeled_stats `s` on
    `tw`.`window_ts` = `s`.`event_window_ts`
),
-- sentiment: voc_anal_flow_sentiment_annotations_results_mv_20251211
`sentiment_data_aligned` as (
select
        `internal`.`voc_ms_td`.`voc_anal_flow_sentiment_annotations_results_mv`.`data_id`,
        FLOOR(UNIX_TIMESTAMP(`internal`.`voc_ms_td`.`voc_anal_flow_sentiment_annotations_results_mv`.`publish_time`) / 1200) * 1200 as `event_window_ts`,
        `internal`.`voc_ms_td`.`voc_anal_flow_sentiment_annotations_results_mv`.`insert_dt`
from
    `internal`.`voc_ms_td`.`voc_anal_flow_sentiment_annotations_results_mv`
where

         `internal`.`voc_ms_td`.`voc_anal_flow_sentiment_annotations_results_mv`.`publish_time` >= DATE_SUB(NOW(), interval 24 hour)
),
`sentiment_stats` as (
select
    event_window_ts,
    COUNT(distinct `sentiment_data_aligned`.`data_id`) as `unique_data_id_count`,
    MAX(`sentiment_data_aligned`.`insert_dt`) as `max_insert_dt`
from
    sentiment_data_aligned
group by
    `sentiment_data_aligned`.`event_window_ts`
),
`sentiment_data` as (
select
    `wb`.`window_start` as `publish_time`,
           coalesce(`s`.`unique_data_id_count`, 0) as `id_count`,
           `d`.`max_insert_dt`
from
    window_base `wb`
left join sentiment_stats `s` on
    `wb`.`window_ts` = `s`.`event_window_ts`
left join (
    select
        `_table_valued_function_tasks`.`FinishTime` as `max_insert_dt`
    from
        tasks('type' = 'mv')
    where
        `_table_valued_function_tasks`.`mvname` = 'voc_anal_flow_sentiment_annotations_results_mv'
    order by
        `_table_valued_function_tasks`.`StartTime` desc
    limit 1
) as `d` on
    1 = 1
),
`body_1` as (
select
        max(`f1`.`publish_time`) as `publish_time`,
        max(`f1`.`id_count`) as `di`,
        max(`f2`.`id_count`) as `mate`,
    --         max(f3.id_count) AS pre,
    max(if(`f3`.`is_outer` = 'Y', `f3`.`id_count` , 0)) as `isouter_pre`,
        max(if(`f3`.`is_outer` = 'N', `f3`.`id_count` , 0 )) as `nonouter_pre`,
    max(`f7`.`id_count`) as `tomodel`,
    	max(if(`f4`.`is_outer` = 'Y', `f4`.`id_count` , 0)) as `isouter_labeled`,
        max(if(`f4`.`is_outer` = 'N', `f4`.`id_count` , 0 )) as `nonouter_labeled`,
        max(`f5`.`id_count`) as `unlabeled`,
        max(`f6`.`id_count`) as `app`,
    --         CONCAT(
    -- 	        CAST(ROUND(f4.id_count * 100.0 / NULLIF(f3.id_count, 0)) AS INT), '%'
    -- 	    ) AS labeled_p,
    -- 	    CONCAT(
    -- 	        CAST(ROUND((f2.id_count - f3.id_count) * 100.0 / NULLIF(f2.id_count, 0)) AS INT), '%'
    -- 	    ) AS filter_p,
        3 as `sort`,
        max(`f1`.`max_insert_dt`) as `di_dt`,
        max(`f2`.`max_insert_dt`) as `mate_dt`,
        max(`f3`.`max_insert_dt`) as `pre_dt`,
        max(`f7`.`max_insert_dt`) as `tomodel_dt`,
        max(`f4`.`max_insert_dt`) as `labeled_dt`,
        max(`f5`.`max_insert_dt`) as `unlabeled_dt`,
        max(`f6`.`max_insert_dt`) as `app_dt`
from
    di_data `f1`
left join mate_data `f2` on
    `f1`.`publish_time` = `f2`.`publish_time`
left join pre_data `f3` on
    `f1`.`publish_time` = `f3`.`publish_time`
left join labeled_data `f4` on
    `f1`.`publish_time` = `f4`.`publish_time`
left join unlabeled_data `f5` on
    `f1`.`publish_time` = `f5`.`publish_time`
left join sentiment_data `f6` on
    `f1`.`publish_time` = `f6`.`publish_time`
left join tomodel_data `f7` on
    `f1`.`publish_time` = `f7`.`publish_time`
group by
    `f1`.`publish_time`
),
`final_data` as(
select
		'(今日范围)-Total' as `publish_time`,
		sum(`body_1`.`di`) as `di`,
		sum(`body_1`.`mate`) as `mate`,
		sum(`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`) as `pre`,
		sum(`body_1`.`tomodel`) as `tomodel`,
		concat(LPAD(sum(`body_1`.`isouter_labeled` + `body_1`.`nonouter_labeled` + `body_1`.`unlabeled`), 5, 0),
			' (标: ',
			LPAD(sum(`isouter_labeled` + `nonouter_labeled`), 5, 0),
			' , 无: ',
			LPAD(sum(unlabeled), 5, 0),
			'）'
		) as `model_result`,
-- 		sum(`unlabeled`) as `unlabeled`,
--     	sum(`body_1`.`app`) as `app` ,
    	concat(LPAD(sum(`body_1`.`app` ), 5, 0),
			' (标: ',
			LPAD(sum(`isouter_labeled` + `nonouter_labeled`), 5, 0),
			' , 用: ',
			CONCAT(
		        LPAD(cast(ROUND(sum(app) * 100.0 / nullif(sum(`isouter_labeled` + `nonouter_labeled`), 0)) as INT), 2, 0), '%'
		    ),
			'）'
		) as app,
	    concat('',
	    	CONCAT(
		        LPAD(cast(ROUND(sum(isouter_labeled + nonouter_labeled) * 100.0 / nullif(sum(isouter_pre + nonouter_pre), 0)) as INT), 2, 0), '%'
		    ),
		    '（公: ',
		    CONCAT(
		        LPAD(cast(ROUND(sum(isouter_labeled) * 100.0 / nullif(sum(isouter_pre), 0)) as INT), 2, 0), '%'
		    ),
		    ' , 私: ',
		    CONCAT(
		        LPAD(cast(ROUND(sum(nonouter_labeled) * 100.0 / nullif(sum(nonouter_pre), 0)) as INT), 2, 0), '%'
		    ),
		    '）'
	    ) as `labeled_p`,
 	    CONCAT(
	        LPAD(cast(ROUND((sum(mate) - sum(isouter_pre + nonouter_pre)) * 100.0 / nullif(sum(mate), 0)) as INT), 2, 0), '%'
	    ) as `filter_p`,
		0 as `sort`
	from body_1
	where `body_1`.`publish_time` >= date(now())
	limit 1
	union all
	select
		'latest_times' as `publish_time`,
		DATE_FORMAT(max(`body_1`.`di_dt`), '%H:%i:%s') as `di`,
		DATE_FORMAT(max(`body_1`.`mate_dt`), '%H:%i:%s') as `mate`,
		DATE_FORMAT(max(`body_1`.`pre_dt`), '%H:%i:%s') as `pre`,
		DATE_FORMAT(max(`body_1`.`tomodel_dt`), '%H:%i:%s') as `tomodel`,
		DATE_FORMAT(max(`body_1`.`labeled_dt`), '%H:%i:%s') as `model_result`,
-- 		DATE_FORMAT(max(`unlabeled_dt`), '%H:%i:%s') as `unlabeled`,
DATE_FORMAT(max(`body_1`.`app_dt`), '%H:%i:%s') as `app`,
		null as `labeled_p`,
		null as `filter_p`,
		2 as `sort`
	from body_1 limit 1
	union all
	select
		`body_1`.`publish_time`, `body_1`.`di`, `body_1`.`mate`,
	    (`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`) as `pre`,
	    `body_1`.`tomodel`,
-- 	    (`isouter_labeled` + `nonouter_labeled` + unlabeled) as `model_result`,
concat('', LPAD((`body_1`.`isouter_labeled` + `body_1`.`nonouter_labeled` + `body_1`.`unlabeled`), 5, 0),
			' (标: ',
			LPAD((`body_1`.`isouter_labeled` + `body_1`.`nonouter_labeled` ), 5, 0),
			' , 无: ',
			LPAD((`body_1`.`unlabeled`), 5, 0),
			'）'
		) as `model_result`,
	    `body_1`.`app`,
		concat('',
		    case
		    	when (`body_1`.`isouter_labeled` + `body_1`.`nonouter_labeled`) = 0 or (`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`) = 0 then '00%'
		    	else
		    	CONCAT(LPAD(cast(ROUND((`body_1`.`isouter_labeled` + `body_1`.`nonouter_labeled`) * 100.0 / nullif((`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`), 0)) as INT), 2, 0), '%')
		    end,
		    '（公: ',
		    case
		    	when `body_1`.`isouter_labeled` = 0 or `body_1`.`isouter_pre` = 0 then '00%'
		    	else
		    	CONCAT(LPAD(cast(ROUND((`body_1`.`isouter_labeled`) * 100.0 / nullif((`body_1`.`isouter_pre`), 0)) as INT), 2, 0), '%')
		    end,
		    ' , 私: ',
		    case
		    	when `body_1`.`nonouter_labeled` = 0 or `body_1`.`nonouter_pre` = 0 then '00%'
		    	else
		    	CONCAT(LPAD(cast(ROUND((`body_1`.`nonouter_labeled`) * 100.0 / nullif(`body_1`.`nonouter_pre`, 0)) as INT), 2, 0), '%')
		    end,
		    '）'
	    ) as `labeled_p`,
	    case
	    	when ((`body_1`.`mate`) - (`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`)) = 0 or `body_1`.`mate` = 0 then '00%'
	    	else
	    	CONCAT(LPAD(cast(ROUND(((`body_1`.`mate`) - (`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`)) * 100.0 / nullif((`body_1`.`mate`), 0)) as INT), 2, 0), '%')
	    end as `filter_p`,
		3 as `sort`
	from body_1
)
select
    `final_data`.`publish_time`,
    `final_data`.`di`,
    `final_data`.`mate`,
    `final_data`.`pre`,
    `final_data`.`tomodel` as toAI,
    `final_data`.`model_result`,
    --     `labeled`,
    --     `unlabeled`,
    `final_data`.`app`,
    ifnull(`final_data`.`labeled_p`, '') as `labeled_p`,
    ifnull(`final_data`.`filter_p`, '') as `filter_p`
from
    final_data
order by
    `final_data`.`sort`,
    `final_data`.`publish_time` desc;






-- drop  MATERIALIZED VIEW voc_imp_cust_vehicle_rel_json_info_mv2

CREATE MATERIALIZED VIEW voc_imp_hudi_dm_voc_cust_mv
REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-09 01:10:00"
DISTRIBUTED BY HASH (`oneid`) BUCKETS 8
PROPERTIES (
   "replication_num" = "2",
   "bloom_filter_columns" = "oneid,mobile,id_card_no"
)
as

SELECT oneid, cust_classify, id_card_no, mobile, cust_nm,
       is_car_owner_flg, gender, age, age_group,
       high_educaion, marriage_statue, family_income,
       is_exchange_flg, purchase_car_times,
       is_member_flg,
       hukou_prov_cd, hukou_prov_nm, hukou_city_cd, hukou_city_nm,
       cust_type,
       lived_prov_nm, lived_city_nm, profession, insert_dt
FROM voc_imp_hudi_dm_voc_cust
WHERE oneid IS NOT null
;

CREATE MATERIALIZED VIEW voc_imp_hudi_dm_voc_cust_vehicle_rel_mv
REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-09 01:15:00"
DISTRIBUTED BY HASH (`vin`) BUCKETS 8
PROPERTIES (
   "replication_num" = "2",
   "bloom_filter_columns" = "vin,idcard"
)
as
SELECT vin, idcard
FROM voc_imp_hudi_dm_voc_cust_vehicle_rel
WHERE idcard IS NOT NULL AND vin IS NOT null
;


-- drop  MATERIALIZED VIEW voc_imp_cust_vehicle_rel_json_info_mv2

CREATE MATERIALIZED VIEW voc_imp_hudi_dwd_maf_veh_d_full_mv
REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-09 01:15:00"
DISTRIBUTED BY HASH (`vin`) BUCKETS 8
PROPERTIES (
   "replication_num" = "2",
   "bloom_filter_columns" = "vin"
)
as
SELECT
    vin, col_name, product_date, offline_date,
    home_abroad, dis_ch, dis_mt, eng_clsf, eng_seris,
    veh_type, plnt_code
FROM voc_imp_hudi_dwd_maf_veh_d_full
WHERE vin IS NOT NULL
;



CREATE MATERIALIZED VIEW voc_anal_di_stg_mate_data_merge_m_inc_mv
REFRESH AUTO ON SCHEDULE EVERY 10 MINUTE STARTS "2025-11-20 21:00:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 8
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "data_id, channel_code",
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
AS with `mate_data` as (
--  私域渠道范围
    select
        `f`.`id`,
        `f`.`data_create_time`,
        `f`.`create_time`,
        `f`.`content_type`,
        `f`.`data_update_time`,
        `f`.`data_id`,
        `f`.`channel_code`,
        `f`.`brand`,
        `f`.`series`,
        `f`.`model`,
        `f`.`is_outer`,
        `f`.`one_id`,
        `f`.`id_car_no`,
        `f`.`mobile`,
        `f`.`email`,
        `f`.`global_id`,
        `f`.`user_id`,
        `f`.`user_name`,
        `f`.`vhl_id`,
        `f`.`vhl_vin`,
        `f`.`dlr_id`,
        `f`.`dlr_code`,
        `f`.`dlr_type`,
        `f`.`market_id`,
        `f`.`title`,
        `f`.`content`,
        `f`.`is_wsater_army`,
        `f`.`weight`,
        `f`.`attrs`,
        `f`.`attrs2`,
        `f`.`attrs3`,
        `f`.`work_id`,
        `f`.`model_type`
    from
        (
        	SELECT
		        `id`, `create_time`, `data_create_time`, `content_type`, `data_update_time`, `data_id`, `channel_code`, `brand`, `series`, `model`, `is_outer`, `one_id`, `id_car_no`, `mobile`, `email`, `global_id`, `user_id`, `user_name`, `vhl_id`, `vhl_vin`, `dlr_id`, `dlr_code`, `dlr_type`, `market_id`, `title`, `content`, `is_wsater_army`, `weight`, `attrs`, `attrs2`, `attrs3`, `work_id`, `done`, `model_type`, `ds`, `insert_dt`,
		        ROW_NUMBER() OVER (PARTITION BY `data_id` ORDER BY `create_time` ASC) AS `rn`
		    FROM
		        `voc_anal_di_stg_mate_data_m_inc`
		    WHERE
		        `insert_dt` >= NOW() - INTERVAL 1 HOUR
		        AND `channel_code` IS NOT NULL
        ) `f`
    where  `f`.`rn` = 1
),
-- filter_out_merged as (
-- 	select f1.* from mate_data as f1
-- 	left join (
-- 		select id
-- 		from  `voc_anal_di_stg_mate_data_finished_record`
-- 	    where  `insert_dt` >= NOW() - INTERVAL 24 HOUR
-- 	) as f2 on f1.id = f2.id
-- 	where f2.id is null
-- 	and length(attrs) < 900000 and length(content) < 900000
-- ),
`encrypted_data` AS (
         SELECT
             `id`,
             `data_create_time`,
             `create_time`,
             `content_type`,
             `data_update_time`,
             `data_id`,
             `channel_code`,
             `brand`,
             `series`,
             `model`,
             `is_outer`,
             `one_id`,
             `id_car_no`,
             `mobile`,
             `email`,
             `global_id`,
--              `user_id`,
             concat('u_',md5(concat('_',COALESCE(`user_id`,`user_name`,uuid()), `channel_code` ))) as `user_id`,
             `user_name`,
             `vhl_id`,
             `vhl_vin`,
             `dlr_id`,
             `dlr_code`,
             `dlr_type`,
             `market_id`,
             `title`,
             `content`,
             `is_wsater_army`,
             `weight`,
             `attrs`,
             `attrs2`,
             `attrs3`,
             `work_id`,
             `model_type`,
             `mobile_enc`,
             -- 加密手机号
             CASE
                 WHEN `mobile` REGEXP '^1[3-9]\\d{9}$' THEN
                     TO_BASE64(SM4_ENCRYPT(`mobile`, 'changanvoc2025xx'))
                 else `mobile`
                 END AS `mobile_enc`,
             CASE
                 WHEN `id_car_no` REGEXP '^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dX]$'
                     OR `id_car_no` REGEXP '^[1-9]\\d{5}\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}$'
                     THEN TO_BASE64(SM4_ENCRYPT(`id_car_no`, 'changanvoc2025xx'))
                 ELSE NULL
                 END AS `id_card_no_enc`
         FROM mate_data
     ),
     `marge_data` as (
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
             `e`.`one_id`,
             `e`.`id_card_no_enc` as `id_car_no`,
             `e`.`mobile_enc` as `mobile`,
             `e`.`email`,
             `e`.`global_id`,
             `e`.`user_id`,
             `e`.`user_name`,
             `e`.`vhl_id`,
             `e`.`vhl_vin`,
             `e`.`dlr_id`,
             `e`.`dlr_code`,
             `e`.`dlr_type`,
             `e`.`market_id`,
             `e`.`title`,
             `e`.`content`,
             `e`.`is_wsater_army`,
             `e`.`weight` ,
             `e`.`attrs`,
             `e`.`attrs2`,
             `e`.`attrs3`,
             `e`.`work_id`,
             `e`.`model_type`,
             COALESCE(`b1`.`cust_json`, `b2`.`cust_json`) as `cust_ext_attrs`,
             COALESCE(`b3`.`vehicle_json`, `b1`.`vehicle_json`, `b2`.`vehicle_json`) as `vhl_ext_attrs`,
             `d1`.`dealer_json` as `dealer_ext_attrs`,
             null as `prd_ext_attrs`
         FROM
             encrypted_data as `e`
                 LEFT JOIN `voc_imp_cust_vehicle_rel_json_info_mv` as `b1`
                           on
                               `e`.`is_outer` = 'N'
                                   and `b1`.`one_id` = `e`.`one_id`
                 LEFT JOIN `voc_imp_cust_vehicle_rel_json_info_mv` as `b2`
                           ON
                               `e`.`is_outer` = 'N'
                                   and `b2`.`mobile` = `e`.`mobile_enc`
                 LEFT JOIN `voc_imp_cust_vehicle_rel_json_info_mv` as `b3`
                           ON
                               `e`.`is_outer` = 'N'
                                   and `b3`.`vin` = `e`.`vhl_vin`
                 LEFT JOIN `voc_imp_dealer_json_info_mv` as `d1`
                           ON
                               `e`.`is_outer` = 'N'
                                   and `d1`.`dealer_code` = `e`.`dlr_code`
         order by
             `e`.`create_time` desc
     )
select
    `id`,
    `data_create_time`,
    `create_time`,
    `content_type`,
    `data_update_time`,
    `data_id`,
    `channel_code`,
    `brand`,
    `series`,
    `model`,
    `is_outer`,
    -- 	`one_id`,
    case
        when `one_id` is not null then `one_id`
        when JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id") is not null then JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id")
        when `is_outer` = 'Y' then concat('o_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        when `is_outer` = 'N' then concat('i_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        else concat('x_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        end as `one_id` ,
    `id_car_no`,
    `mobile`,
    `email`,
    `global_id`,
    `user_id`,
    `user_name`,
    `vhl_id`,
    `vhl_vin`,
    `dlr_id`,
    `dlr_code`,
    `dlr_type`,
    `market_id`,
    `title`,
    `content`,
    `is_wsater_army`,
    `weight` ,
    `attrs`,
    `attrs2`,
    `attrs3`,
    `work_id`,
    `model_type`,
    `cust_ext_attrs`,
    `vhl_ext_attrs`,
    `dealer_ext_attrs`,
    `prd_ext_attrs`,
    now() as `insert_dt`
from
    marge_data
;




CREATE MATERIALIZED VIEW voc_imp_cust_vehicle_rel_json_info_mv
(one_id,vin,mobile,id_card_no,cust_json,vehicle_json,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-09 05:10:00"
DUPLICATE KEY(`one_id`)
DISTRIBUTED BY HASH(`one_id`) BUCKETS 12
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
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
        FROM `internal`.`voc_ms_td`.`voc_imp_hudi_dm_voc_cust_mv`
        WHERE `oneid` IS NOT null
),
`rel_filtered` AS (
	    SELECT `internal`.`voc_ms_td`.`voc_imp_hudi_dm_voc_cust_vehicle_rel_mv`.`vin`, `internal`.`voc_ms_td`.`voc_imp_hudi_dm_voc_cust_vehicle_rel_mv`.`idcard`
	    FROM `internal`.`voc_ms_td`.`voc_imp_hudi_dm_voc_cust_vehicle_rel_mv`
	    WHERE `internal`.`voc_ms_td`.`voc_imp_hudi_dm_voc_cust_vehicle_rel_mv`.`idcard` IS NOT NULL AND `internal`.`voc_ms_td`.`voc_imp_hudi_dm_voc_cust_vehicle_rel_mv`.`vin` IS NOT null

),
`veh_data` AS (

	    SELECT
	        `vin`, `col_name`, `product_date`, `offline_date`,
	        `home_abroad`, `dis_ch`, `dis_mt`, `eng_clsf`, `eng_seris`,
	        `veh_type`, `plnt_code`
	    FROM `internal`.`voc_ms_td`.`voc_imp_hudi_dwd_maf_veh_d_full_mv`
	    WHERE `vin` IS NOT NULL

)
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
;


CREATE MATERIALIZED VIEW voc_imp_cust_json_by_one_id_mv
REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-23 06:10:00"
DUPLICATE KEY(`one_id`)
DISTRIBUTED BY HASH(`one_id`) BUCKETS 12
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"bloom_filter_columns" = "insert_dt",
"enable_nondeterministic_function"  = "true"
)
as
select distinct one_id as one_id ,cust_json, vehicle_json, now() as insert_dt
from voc_imp_cust_vehicle_rel_json_info_mv
where one_id is not null and one_id <> ''
;

CREATE MATERIALIZED VIEW voc_imp_cust_json_b_by_mobile_mv
REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-23 06:11:00"
DUPLICATE KEY(`mobile`)
DISTRIBUTED BY HASH(`mobile`) BUCKETS 12
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"bloom_filter_columns" = "insert_dt",
"enable_nondeterministic_function"  = "true"
)
as
select distinct mobile ,cust_json, vehicle_json ,now() as insert_dt
from voc_imp_cust_vehicle_rel_json_info_mv
where mobile is not null and mobile <> ''
;


CREATE MATERIALIZED VIEW voc_imp_cust_json_b_by_vin_mv
REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-23 06:11:00"
DUPLICATE KEY(`vin`)
DISTRIBUTED BY HASH(`vin`) BUCKETS 12
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"bloom_filter_columns" = "insert_dt",
"enable_nondeterministic_function"  = "true"
)
as
select distinct vin ,cust_json, vehicle_json,now() as insert_dt
from voc_imp_cust_vehicle_rel_json_info_mv
where vin is not null and vin <> ''
;


CREATE MATERIALIZED VIEW voc_imp_cust_json_b_by_idcard_mv
REFRESH AUTO ON SCHEDULE EVERY 24 HOUR STARTS "2025-12-23 06:11:00"
DUPLICATE KEY(`id_card_no`)
DISTRIBUTED BY HASH(`id_card_no`) BUCKETS 12
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"bloom_filter_columns" = "insert_dt",
"enable_nondeterministic_function"  = "true"
)
as
select DISTINCT id_card_no ,cust_json, vehicle_json ,now() as insert_dt
from voc_imp_cust_vehicle_rel_json_info_mv
where id_card_no is not null and id_card_no <> ''
;








create or replace view voc_app_system_pushed_data_statistics_v
as
SELECT
    COALESCE(avcmfm.name, t.channel_code) AS channel_name,
    SUM(CASE WHEN date(publish_time) = date_sub(date(now()), 1) THEN 1 ELSE 0 END) AS yesterday_num,
    SUM(CASE WHEN date(publish_time) = date(now()) THEN 1 ELSE 0 END) AS today_num
FROM voc_sentiment_annotations_results_v t
         LEFT JOIN voc_ext_ins_channel_mv avcmfm
                   ON t.channel_code = avcmfm.code
WHERE date(publish_time) IN (date_sub(date(now()), 1), date(now()))
GROUP BY t.channel_code, avcmfm.name
ORDER BY today_num desc;


CREATE MATERIALIZED VIEW voc_ext_ins_account_lexicon_mv
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 10 MINUTE STARTS "2025-12-15 15:30:40"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 2
PROPERTIES (
	"replication_allocation" = "tag.location.default: 3",
	"bloom_filter_columns" = "account_id, status"
)
AS
select
    id,resource_id,account_name,account_id,channel,status,create_time,update_time,create_user,update_user,final_channel
from voc_mysql_jdbc.voc_ms_be.ins_account_lexicon
where resource_id = '4daa754477839cb8295033bc3f7ef7a2' and status = 'Enabled'
;



-- 领导人账号
CREATE MATERIALIZED VIEW voc_ext_ins_executives_accounts_mv
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-15 16:30:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 2
PROPERTIES (
	"replication_allocation" = "tag.location.default: 3",
	"bloom_filter_columns" = "account_id, status",
"enable_nondeterministic_function" = "true"
)
AS
select
    id,account_name,account_id,channel,status,create_time, now() as insert_dt
from voc_mysql_jdbc.voc_ms_be.ins_account_lexicon
where status = 'Enabled'
-- resource_id = '4daa754477839cb8295033bc3f7ef7a2' and
;




-- 大V账号
CREATE MATERIALIZED VIEW voc_ext_ins_kols_accounts_mv
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-12-15 16:30:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 2
PROPERTIES (
	"replication_allocation" = "tag.location.default: 3",
	"bloom_filter_columns" = "account_id, status",
"enable_nondeterministic_function" = "true"
)
AS
select
    id,account_name,account_id,channel,status,create_time, now() as insert_dt
from voc_mysql_jdbc.voc_ms_be.ins_account_lexicon
where status = 'Enabled'
-- resource_id = '4daa754477839cb8295033bc3f7ef7a2' and
;



CREATE or replace VIEW `voc_anal_di_stg_mate_data_batch_range_v` AS
with di_data as (
	select data_id from voc_anal_di_stg_mate_data_finished_record_range_mv
)
select
    `f1`.`id`,
    `f1`.`data_create_time`,
    `f1`.`create_time`,
    `f1`.`content_type`,
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
    `f1`.`model_type`,
    `f1`.`cust_ext_attrs`,
    `f1`.`vhl_ext_attrs`,
    `f1`.`dealer_ext_attrs`,
    `f1`.`prd_ext_attrs`,
    `f1`.`insert_dt`
from
    `voc_anal_di_stg_mate_data_m_inc_v` as `f1`
        left join `di_data` as `f2` on
        `f1`.`data_id` = `f2`.`data_id`
where
    `f2`.`data_id` is null;








-- drop MATERIALIZED VIEW voc_anal_di_stg_mate_data_m_batch_range_merge_mv
CREATE MATERIALIZED VIEW voc_anal_di_stg_mate_data_m_batch_range_merge_mv
(id,data_create_time,create_time,content_type,data_update_time,data_id,channel_code,brand,series,model,is_outer,one_id,id_car_no,mobile,email,global_id,user_id,user_name,vhl_id,vhl_vin,dlr_id,dlr_code,dlr_type,market_id,title,content,is_wsater_army,weight,attrs,attrs2,attrs3,work_id,model_type,cust_ext_attrs,vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 20 MINUTE STARTS "2025-12-22 17:02:00"
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
AS
WITH `raw_data` AS (
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
        `e`.`attrs`,
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
        AND `e`.`work_id` IS NOT NULL
),
-- 第一步：去重并只保留 rn=1
`main_data` AS (
select `f1`.`id`, `f1`.`data_create_time`, `f1`.`create_time`, `f1`.`content_type`, `f1`.`data_update_time`, `f1`.`data_id`, `f1`.`channel_code`, `f1`.`brand`, `f1`.`series`, `f1`.`model`, `f1`.`is_outer`, `f1`.`mobile`, `f1`.`id_car_no`, `f1`.`email`, `f1`.`global_id`, `f1`.`user_id`, `f1`.`user_name`, `f1`.`vhl_id`, `f1`.`vhl_vin`, `f1`.`dlr_id`, `f1`.`dlr_code`, `f1`.`dlr_type`, `f1`.`market_id`, `f1`.`title`, `f1`.`content`, `f1`.`is_wsater_army`, `f1`.`weight`, `f1`.`attrs`, `f1`.`attrs2`, `f1`.`attrs3`, `f1`.`work_id`, `f1`.`model_type`, `f1`.`one_id`, `f1`.`client_id` from raw_data as `f1`
left join `voc_anal_di_stg_mate_data_finished_record` as `f2` on `f1`.`data_id` = `f2`.`data_id`
where `f2`.`data_id` is null
limit 0, 50000
),
-- 第二步：仅对 is_outer = 'N' 的数据做关联
`inner_data` AS (
    SELECT `main_data`.`id`, `main_data`.`data_create_time`, `main_data`.`create_time`, `main_data`.`content_type`, `main_data`.`data_update_time`, `main_data`.`data_id`, `main_data`.`channel_code`, `main_data`.`brand`, `main_data`.`series`, `main_data`.`model`, `main_data`.`is_outer`, `main_data`.`mobile`, `main_data`.`id_car_no`, `main_data`.`email`, `main_data`.`global_id`, `main_data`.`user_id`, `main_data`.`user_name`, `main_data`.`vhl_id`, `main_data`.`vhl_vin`, `main_data`.`dlr_id`, `main_data`.`dlr_code`, `main_data`.`dlr_type`, `main_data`.`market_id`, `main_data`.`title`, `main_data`.`content`, `main_data`.`is_wsater_army`, `main_data`.`weight`, `main_data`.`attrs`, `main_data`.`attrs2`, `main_data`.`attrs3`, `main_data`.`work_id`, `main_data`.`model_type`, `main_data`.`one_id`, `main_data`.`client_id`
    FROM main_data WHERE `main_data`.`is_outer` = 'N'
),
`outer_data` AS (
    SELECT `main_data`.`id`, `main_data`.`data_create_time`, `main_data`.`create_time`, `main_data`.`content_type`, `main_data`.`data_update_time`, `main_data`.`data_id`, `main_data`.`channel_code`, `main_data`.`brand`, `main_data`.`series`, `main_data`.`model`, `main_data`.`is_outer`, `main_data`.`mobile`, `main_data`.`id_car_no`, `main_data`.`email`, `main_data`.`global_id`, `main_data`.`user_id`, `main_data`.`user_name`, `main_data`.`vhl_id`, `main_data`.`vhl_vin`, `main_data`.`dlr_id`, `main_data`.`dlr_code`, `main_data`.`dlr_type`, `main_data`.`market_id`, `main_data`.`title`, `main_data`.`content`, `main_data`.`is_wsater_army`, `main_data`.`weight`, `main_data`.`attrs`, `main_data`.`attrs2`, `main_data`.`attrs3`, `main_data`.`work_id`, `main_data`.`model_type`, `main_data`.`one_id`, `main_data`.`client_id` FROM main_data WHERE `main_data`.`is_outer` != 'N'  -- 包含 'Y' 和其他
),
-- 第三步：安全 JOIN（避免 OR）
`joined_inner` AS (
    SELECT
        `i`.`id`, `i`.`data_create_time`, `i`.`create_time`, `i`.`content_type`, `i`.`data_update_time`, `i`.`data_id`, `i`.`channel_code`, `i`.`brand`, `i`.`series`, `i`.`model`, `i`.`is_outer`, `i`.`mobile`, `i`.`id_car_no`, `i`.`email`, `i`.`global_id`, `i`.`user_id`, `i`.`user_name`, `i`.`vhl_id`, `i`.`vhl_vin`, `i`.`dlr_id`, `i`.`dlr_code`, `i`.`dlr_type`, `i`.`market_id`, `i`.`title`, `i`.`content`, `i`.`is_wsater_army`, `i`.`weight`, `i`.`attrs`, `i`.`attrs2`, `i`.`attrs3`, `i`.`work_id`, `i`.`model_type`, `i`.`one_id`, `i`.`client_id`,
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
    `joined_inner`.`id`,   `joined_inner`.`data_create_time`, `joined_inner`.`create_time`, `joined_inner`.`content_type`, `joined_inner`.`data_update_time`,
    `joined_inner`.`data_id`, `joined_inner`.`channel_code`, `joined_inner`.`brand`, `joined_inner`.`series`, `joined_inner`.`model`, `joined_inner`.`is_outer`, `joined_inner`.`one_id`,
    `joined_inner`.`id_car_no`, `joined_inner`.`mobile`, `joined_inner`.`email`, `joined_inner`.`global_id`, `joined_inner`.`user_id`, `joined_inner`.`user_name`,
    `joined_inner`.`vhl_id`, `joined_inner`.`vhl_vin`, `joined_inner`.`dlr_id`, `joined_inner`.`dlr_code`, `joined_inner`.`dlr_type`, `joined_inner`.`market_id`,
    `joined_inner`.`title`, `joined_inner`.`content`, `joined_inner`.`is_wsater_army`, `joined_inner`.`weight`, `joined_inner`.`attrs`, `joined_inner`.`attrs2`, `joined_inner`.`attrs3`,
    `joined_inner`.`work_id`, `joined_inner`.`model_type`,
    `joined_inner`.`cust_ext_attrs`,
    `joined_inner`.`vhl_ext_attrs`,
    `joined_inner`.`dealer_ext_attrs`,
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
FROM outer_data
;









-- 过滤掉了已执行的数据范围
-- drop MATERIALIZED VIEW voc_anal_di_stg_mate_data_m_batch_range_mv
CREATE MATERIALIZED VIEW voc_anal_di_stg_mate_data_m_batch_range_mv
REFRESH AUTO ON SCHEDULE EVERY 20 MINUTE STARTS "2025-11-27 23:02:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"bloom_filter_columns" = "one_id, mobile,vhl_vin,dlr_code,id_car_no",
"enable_nondeterministic_function" = "true"
)
AS
with raw_data as (
	select
    `id`,
    `data_create_time`,
    `create_time`,
    `content_type`,
    `data_update_time`,
    `data_id`,
    `channel_code`,
    `brand`,
    `series`,
    `model`,
    `is_outer`,
--     `one_id`,
    case
        when  nullif(`one_id`, '') is not null then `one_id`
--         when JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id") is not null then JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id")
        when `is_outer` = 'Y' then concat('o_', md5(concat_ws(`channel_code`, concat(`user_id`, `user_name`, uuid()) , `channel_code`)))
--         when `is_outer` = 'N' then concat('i_', md5(concat_ws(`channel_code`, concat(`user_id`, `user_name`, uuid()) , `channel_code`)))
         when `is_outer` = 'N' then concat('i_', md5(concat_ws(`channel_code`, null)))
        else concat('x_', md5(concat_ws(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
    end as `one_id` ,
--          `id_car_no`,
--          `mobile`,
    `email`,
    `global_id`,
--              `user_id`,
    concat('u_',md5(concat('_',COALESCE(`user_id`,`user_name`,uuid()), `channel_code` ))) as `user_id`,
    `user_name`,
    `vhl_id`,
    `vhl_vin`,
    `dlr_id`,
    `dlr_code`,
    `dlr_type`,
    `market_id`,
    `title`,
    `content`,
    `is_wsater_army`,
    `weight`,
    `attrs`,
    `attrs2`,
    `attrs3`,
    `work_id`,
    `model_type`,
    -- 加密手机号
    CASE
        WHEN `mobile` REGEXP '^1[3-9]\\d{9}$' THEN
            TO_BASE64(SM4_ENCRYPT(`mobile`, 'changanvoc2025xx'))
        else `mobile`
        END AS `mobile`,
    CASE
        WHEN `id_car_no` REGEXP '^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dX]$'
        OR `id_car_no` REGEXP '^[1-9]\\d{5}\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}$'
    THEN TO_BASE64(SM4_ENCRYPT(`id_car_no`, 'changanvoc2025xx'))
    ELSE null
    END AS `id_car_no`,
    '764547797eb2e192763f5334028d49c9' as client_id,
	now() as insert_dt
from
    `voc_anal_di_stg_mate_data_m_inc`
where
    (`insert_dt` >= NOW() - INTERVAL 1 hour and `insert_dt` < now())
    and `channel_code` is not null
)
select f1.* from raw_data as f1
                     left join voc_anal_di_stg_mate_data_finished_record_range_mv as f2
                               on f1.data_id = f2.data_id
where
    f2.data_id is not null
  and   `channel_code` is not null
  and data_create_time  is not null
  and work_id is not null
  and one_id is not null
;



-- drop MATERIALIZED VIEW voc_anal_di_stg_mate_data_finished_record_range_mv
CREATE MATERIALIZED VIEW voc_anal_di_stg_mate_data_finished_record_range_mv
REFRESH AUTO ON SCHEDULE EVERY 20 MINUTE STARTS "2025-11-27 17:01:00"
DUPLICATE KEY(`data_id`)
DISTRIBUTED BY HASH(`data_id`) BUCKETS 2
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"enable_nondeterministic_function" = "true"
)
AS
select
    data_id ,
    now() as insert_dt
from (
         select
             `data_id`
         from
             `voc_anal_di_stg_mate_data_finished_record`
         where
             `insert_dt` >= NOW() - interval 24 * 7 hour
         group by data_id
     ) f;





-- drop MATERIALIZED VIEW voc_anal_flow_mate_data_status_mv

CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_status_mv
REFRESH AUTO ON SCHEDULE EVERY 10 MINUTE STARTS "2025-11-30 12:11:00"
DUPLICATE KEY(`data_id`)
DISTRIBUTED BY HASH(`data_id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"enable_nondeterministic_function" = "true"
)
AS
SELECT
    data_id,
    status,
    insert_dt
FROM (
         SELECT
             json_keys(data)[1] AS data_id,
             JSON_EXTRACT_STRING(data, CONCAT('$.', json_keys(data)[1])) AS status,
             insert_dt,
             ROW_NUMBER() OVER (PARTITION BY json_keys(data)[1] ORDER BY insert_dt desc) AS rn
         FROM voc_anal_flow_mate_data_status
         WHERE
             data IS NOT NULL
           AND json_length(data) > 0  -- 确保非空对象 {}
     ) t
WHERE rn = 1
  AND data_id IS NOT NULL;




-- voc_ms_td.ads_voc_all_mate_data_m_inc source

create or replace
view `voc_anal_flow_mate_data_labeled_v` as
select
    `id`,
    `data_id`,
    `data_update_time`,
    `create_time`,
    `content_type`,
    `data_create_time`,
    `channel_code`,
    `brand`,
    `series`,
    `model`,
    `is_outer`,
    `one_id`,
    `id_car_no`,
    `mobile`,
    `email`,
    `global_id`,
    `user_id`,
    `user_name`,
    `vhl_id`,
    `vhl_vin`,
    `dlr_id`,
    `dlr_code`,
    `dlr_type`,
    `market_id`,
    `title`,
    `content`,
    `is_wsater_army`,
    `weight`,
    `attrs`,
    `attrs2`,
    `attrs3`,
    `work_id`,
    `done`,
    `model_type`,
    `ds`,
    `insert_dt`
from
    `voc_anal_flow_mate_data_labeled_mv`;





create or replace
view `voc_anal_flow_mate_data_full_v` as
with data_status as (
	-- 已打标数据范围
	select data_id,status from voc_anal_flow_mate_data_status_mv
),
final_data as (
	SELECT
    `id`,
    `data_id`,
    JSON_EXTRACT_STRING(`data`, "$.data_update_time") as data_update_time,
    create_time,
    JSON_EXTRACT_STRING(`data`, "$.content_type") as content_type,
    publish_time AS data_create_time,
    JSON_EXTRACT_STRING(`data`, "$.channel_code") AS `channel_code`,
    JSON_EXTRACT_STRING(`data`, "$.brand") AS `brand`,
    JSON_EXTRACT_STRING(`data`, "$.series") AS `series`,
    JSON_EXTRACT_STRING(`data`, "$.model") AS `model`,
    JSON_EXTRACT_STRING(`data`, "$.is_outer") AS `is_outer`,
    JSON_EXTRACT_STRING(`data`, "$.one_id") AS `one_id`,
    JSON_EXTRACT_STRING(`data`, "$.id_car_no") AS  `id_car_no`,
    JSON_EXTRACT_STRING(`data`, "$.mobile") AS `mobile`,
    JSON_EXTRACT_STRING(`data`, "$.email") AS `email`,
    JSON_EXTRACT_STRING(`data`, "$.global_id") AS `global_id`,
    JSON_EXTRACT_STRING(`data`, "$.user_id") AS `user_id`,
    JSON_EXTRACT_STRING(`data`, "$.user_name") AS `user_name`,
    JSON_EXTRACT_STRING(`data`, "$.vhl_id") AS `vhl_id`,
    JSON_EXTRACT_STRING(`data`, "$.vhl_vin") AS `vhl_vin`,
    JSON_EXTRACT_STRING(`data`, "$.dlr_id") AS `dlr_id`,
    JSON_EXTRACT_STRING(`data`, "$.dlr_code") AS `dlr_code`,
    JSON_EXTRACT_STRING(`data`, "$.dlr_type") AS `dlr_type`,
    JSON_EXTRACT_STRING(`data`, "$.market_id") AS `market_id`,
    JSON_EXTRACT_STRING(`data`, "$.title") AS `title`,
    JSON_EXTRACT_STRING(`data`, "$.content") AS `content`,
    JSON_EXTRACT_STRING(`data`, "$.is_wsater_army") as `is_wsater_army`,
    '0' AS `weight`,
    `biz_ext_attrs` AS `attrs`,
    `biz_ext_attrs2` AS `attrs2`,
    `biz_ext_attrs3` AS `attrs3`,
    `work_id`,
    `done`,
    JSON_EXTRACT_STRING(`data`, "$.model_type") as`model_type`,
    null as ds,
    now() as insert_dt
FROM voc_anal_flow_mate_data_full
)
select
    `id`,
    `create_time`,
    `content_type`,
    `data_create_time`,
    `data_update_time`,
    f1.`data_id`,
    `channel_code`,
    `brand`,
    `series`,
    `model`,
    `is_outer`,
    `one_id`,
    `id_car_no`,
    `mobile`,
    `email`,
    `global_id`,
    `user_id`,
    `user_name`,
    `vhl_id`,
    `vhl_vin`,
    `dlr_id`,
    `dlr_code`,
    `dlr_type`,
    `market_id`,
    `title`,
    `content`,
    `is_wsater_army`,
    `weight`,
    `attrs`,
    `attrs2`,
    `attrs3`,
    `work_id`,
    `done`,
    `model_type`,
    `ds`,
    f2.status as `data_status`
from final_data as f1
         left join data_status as f2 on f1.data_id = f2.data_id
;





-- drop MATERIALIZED VIEW voc_ext_ins_province_area_mv
CREATE MATERIALIZED VIEW voc_ext_ins_province_area_mv
REFRESH COMPLETE ON SCHEDULE EVERY 10 MINUTE STARTS "2025-12-02 15:11:00"
DISTRIBUTED BY HASH (`id`) BUCKETS 1
PROPERTIES (
   "replication_num" = "1",
   "bloom_filter_columns" = "province_code,province_name",
   "enable_nondeterministic_function" = "true"
)
as
select id,area_code,area_name,province_code,province_name
from `voc_mysql_jdbc`.`voc_ms_be`.`ins_province_area`;





CREATE OR REPLACE
VIEW `voc_province_dict_data_v` AS
select
    distinct `province_code`,`province_name`
from voc_ext_ins_province_area_mv;



-- voc_ms_td.voc_anal_di_stg_mate_data_m_inc_real_time_v source

create or replace
view `voc_anal_di_stg_mate_data_m_inc_real_time_v` as
with `time_wind` as (
select
    exploded_val.val_ as `n`
from
    (
    select
        sequence(0, 3 * 24) as `val_`
    ) `f1`
    lateral view EXPLODE(`f1`.`val_`) `exploded_val` as `val_`
),
`window_base` as (
select
        `time_wind`.`n`,
        FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(NOW()) / 1200) * 1200 - `time_wind`.`n` * 1200) as `window_start`,
        FLOOR(UNIX_TIMESTAMP(NOW()) / 1200) * 1200 - `time_wind`.`n` * 1200 as `window_ts`
from
    time_wind
),
-- di: voc_anal_di_stg_mate_data_m_inc
`data_aligned` as (
    SELECT
        data_id,
        FLOOR(UNIX_TIMESTAMP(data_create_time) / 1200) * 1200 AS event_window_ts,
        insert_dt
    FROM internal.voc_ms_td.voc_anal_di_stg_mate_data_m_inc
    WHERE
        data_create_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
        AND data_create_time IS NOT NULL),
`stats` as (
    SELECT
        event_window_ts,
        COUNT(DISTINCT data_id) AS unique_data_id_count,
        MAX(insert_dt) AS max_insert_dt
    FROM data_aligned
    GROUP BY event_window_ts),
`di_data` as (
    SELECT
        tw.window_start AS publish_time,
        COALESCE(s.unique_data_id_count, 0) AS id_count,
        s.max_insert_dt
    FROM window_base tw
    LEFT JOIN stats s ON tw.window_ts = s.event_window_ts),
-- mate: voc_anal_flow_mate_data_full
`mate_data_aligned` as (

    SELECT
        data_id,
        FLOOR(UNIX_TIMESTAMP(publish_time) / 1200) * 1200 AS event_window_ts,
        insert_dt
    FROM internal.voc_ms_td.voc_anal_flow_mate_data_full
    WHERE
        publish_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
        AND publish_time IS NOT NULL
),
`mate_stats` as (

    SELECT
        event_window_ts,
        COUNT(DISTINCT data_id) AS unique_data_id_count,
        MAX(insert_dt) AS max_insert_dt
    FROM mate_data_aligned
    GROUP BY event_window_ts
),
`mate_data` as (
SELECT
        tw.window_start AS publish_time,
        COALESCE(s.unique_data_id_count, 0) AS id_count,
        s.max_insert_dt
    FROM window_base tw
    LEFT JOIN mate_stats s ON tw.window_ts = s.event_window_ts
),
-- pre: voc_anal_flow_pre_rules_result_data_full (abandon = 0)
`pre_data_aligned` as (
select
       distinct `data_id`,
       FLOOR(UNIX_TIMESTAMP(`publish_time`) / 1200) * 1200 as `event_window_ts`,
       JSON_EXTRACT_STRING(`data`, "$.is_outer") as `is_outer`,
       `insert_dt`
from
    `voc_anal_flow_pre_rules_result_data_full`
where
     `publish_time` >= DATE_SUB(NOW(), interval 24 hour)
        and `abandon` <> 1
),
`pre_stats` as (
select
    event_window_ts,
    count(data_id) as `unique_data_id_count`,
    `is_outer`,
    MAX(`pre_data_aligned`.`insert_dt`) as `max_insert_dt`
from
    pre_data_aligned
group by
    `pre_data_aligned`.`event_window_ts`,is_outer
),
`pre_data` as (
select
    `wb`.`window_start` as `publish_time`,
           coalesce(`s`.`unique_data_id_count`, 0) as `id_count`,
    `s`.`is_outer`,
           `s`.`max_insert_dt`
from
    window_base `wb`
left join pre_stats `s` on
    `wb`.`window_ts` = `s`.`event_window_ts`
),
-- pre: voc_anal_flow_pre_rules_result_data_full (abandon = 0)
`tomodel_aligned` as (
select
        `internal`.`voc_ms_td`.`voc_anal_flow_to_model_data`.`data_id`,
        FLOOR(UNIX_TIMESTAMP(`internal`.`voc_ms_td`.`voc_anal_flow_to_model_data`.`publish_time`) / 1200) * 1200 as `event_window_ts`,
        `internal`.`voc_ms_td`.`voc_anal_flow_to_model_data`.`insert_dt`
from
    `internal`.`voc_ms_td`.`voc_anal_flow_to_model_data`
where
    `internal`.`voc_ms_td`.`voc_anal_flow_to_model_data`.`publish_time` >= DATE_SUB(NOW(), interval 24 hour)
),
`tomodel_stats` as (
select
    event_window_ts,
    COUNT(distinct `tomodel_aligned`.`data_id`) as `unique_data_id_count`,
    MAX(`tomodel_aligned`.`insert_dt`) as `max_insert_dt`
from
    tomodel_aligned
group by
    `tomodel_aligned`.`event_window_ts`
),
`tomodel_data` as (
select
    `wb`.`window_start` as `publish_time`,
           coalesce(`s`.`unique_data_id_count`, 0) as `id_count`,
           `s`.`max_insert_dt`
from
    window_base `wb`
left join tomodel_stats `s` on
    `wb`.`window_ts` = `s`.`event_window_ts`
),
-- labeled: voc_anal_flow_model_tags_result_data_full
`labeled_data_aligned` as (
select
    distinct `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`data_id`,
    FLOOR(UNIX_TIMESTAMP(`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`publish_time`) / 1200) * 1200 as `event_window_ts`,
     JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`raw_data`, "$.is_outer") as `is_outer`,
    `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`insert_dt`
from
    `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`
where
     `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`publish_time` >= DATE_SUB(NOW(), interval 24 hour)
),
`labeled_stats` as (
select
    event_window_ts,
    COUNT(distinct `labeled_data_aligned`.`data_id`) as `unique_data_id_count`,
    ANY_VALUE(`labeled_data_aligned`.`is_outer`) as `is_outer`,
    MAX(`labeled_data_aligned`.`insert_dt`) as `max_insert_dt`
from
    labeled_data_aligned
group by
    `labeled_data_aligned`.`event_window_ts`,
    `labeled_data_aligned`.`is_outer`
 ),
`labeled_data` as (
select
    `wb`.`window_start` as `publish_time`,
           coalesce(`s`.`unique_data_id_count`, 0) as `id_count`,
    `s`.`is_outer`,
           `s`.`max_insert_dt`
from
    window_base `wb`
left join labeled_stats `s` on
    `wb`.`window_ts` = `s`.`event_window_ts`
),
-- unlabeled: voc_anal_flow_model_tags_unlabeled_data_full
`unlabeled_data_aligned` as (

    SELECT
        data_id,
        FLOOR(UNIX_TIMESTAMP(publish_time) / 1200) * 1200 AS event_window_ts,
        insert_dt
    FROM internal.voc_ms_td.voc_anal_flow_model_tags_unlabeled_data_full
    WHERE
        publish_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
        AND publish_time IS NOT NULL
),
`unlabeled_stats` as (
    SELECT
        event_window_ts,
        COUNT(DISTINCT data_id) AS unique_data_id_count,
        MAX(insert_dt) AS max_insert_dt
    FROM unlabeled_data_aligned
    GROUP BY event_window_ts
),
unlabeled_data AS (
    SELECT
        tw.window_start AS publish_time,
        COALESCE(s.unique_data_id_count, 0) AS id_count,
        s.max_insert_dt
    FROM window_base tw
    LEFT JOIN unlabeled_stats s ON tw.window_ts = s.event_window_ts
),
-- sentiment: voc_anal_flow_sentiment_annotations_results_mv_20251211
`sentiment_data_aligned` as (
select
        `internal`.`voc_ms_td`.`voc_anal_flow_sentiment_annotations_results_mv`.`data_id`,
        FLOOR(UNIX_TIMESTAMP(`internal`.`voc_ms_td`.`voc_anal_flow_sentiment_annotations_results_mv`.`publish_time`) / 1200) * 1200 as `event_window_ts`,
        `internal`.`voc_ms_td`.`voc_anal_flow_sentiment_annotations_results_mv`.`insert_dt`
from
    `internal`.`voc_ms_td`.`voc_anal_flow_sentiment_annotations_results_mv`
where

         `internal`.`voc_ms_td`.`voc_anal_flow_sentiment_annotations_results_mv`.`publish_time` >= DATE_SUB(NOW(), interval 24 hour)
),
`sentiment_stats` as (
select
    event_window_ts,
    COUNT(distinct `sentiment_data_aligned`.`data_id`) as `unique_data_id_count`,
    MAX(`sentiment_data_aligned`.`insert_dt`) as `max_insert_dt`
from
    sentiment_data_aligned
group by
    `sentiment_data_aligned`.`event_window_ts`
),
`sentiment_data` as (
select
    `wb`.`window_start` as `publish_time`,
           coalesce(`s`.`unique_data_id_count`, 0) as `id_count`,
           `s`.`max_insert_dt`
from
    window_base `wb`
left join sentiment_stats `s` on
    `wb`.`window_ts` = `s`.`event_window_ts`
),
`body_1` as (
select
        max(`f1`.`publish_time`) as `publish_time`,
        max(`f1`.`id_count`) as `di`,
        max(`f2`.`id_count`) as `mate`,
    --         max(f3.id_count) AS pre,
    max(if(`f3`.`is_outer` = 'Y', `f3`.`id_count` , 0)) as `isouter_pre`,
        max(if(`f3`.`is_outer` = 'N', `f3`.`id_count` , 0 )) as `nonouter_pre`,
    max(`f7`.`id_count`) as `tomodel`,
    	max(if(`f4`.`is_outer` = 'Y', `f4`.`id_count` , 0)) as `isouter_labeled`,
        max(if(`f4`.`is_outer` = 'N', `f4`.`id_count` , 0 )) as `nonouter_labeled`,
        max(`f5`.`id_count`) as `unlabeled`,
        max(`f6`.`id_count`) as `app`,
    --         CONCAT(
    -- 	        CAST(ROUND(f4.id_count * 100.0 / NULLIF(f3.id_count, 0)) AS INT), '%'
    -- 	    ) AS labeled_p,
    -- 	    CONCAT(
    -- 	        CAST(ROUND((f2.id_count - f3.id_count) * 100.0 / NULLIF(f2.id_count, 0)) AS INT), '%'
    -- 	    ) AS filter_p,
        3 as `sort`,
        max(`f1`.`max_insert_dt`) as `di_dt`,
        max(`f2`.`max_insert_dt`) as `mate_dt`,
        max(`f3`.`max_insert_dt`) as `pre_dt`,
        max(`f7`.`max_insert_dt`) as `tomodel_dt`,
        max(`f4`.`max_insert_dt`) as `labeled_dt`,
        max(`f5`.`max_insert_dt`) as `unlabeled_dt`,
        max(`f6`.`max_insert_dt`) as `app_dt`
from
    di_data `f1`
left join mate_data `f2` on
    `f1`.`publish_time` = `f2`.`publish_time`
left join pre_data `f3` on
    `f1`.`publish_time` = `f3`.`publish_time`
left join labeled_data `f4` on
    `f1`.`publish_time` = `f4`.`publish_time`
left join unlabeled_data `f5` on
    `f1`.`publish_time` = `f5`.`publish_time`
left join sentiment_data `f6` on
    `f1`.`publish_time` = `f6`.`publish_time`
left join tomodel_data `f7` on
    `f1`.`publish_time` = `f7`.`publish_time`
group by
    `f1`.`publish_time`
),
`final_data` as(
select
		'(今日范围)-Total' as `publish_time`,
		sum(`body_1`.`di`) as `di`,
		sum(`body_1`.`mate`) as `mate`,
		sum(`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`) as `pre`,
		sum(`body_1`.`tomodel`) as `tomodel`,
		concat(LPAD(sum(`body_1`.`isouter_labeled` + `body_1`.`nonouter_labeled` + `body_1`.`unlabeled`), 5, 0),
			' (打: ',
			LPAD(sum(`isouter_labeled` + `nonouter_labeled`),5, 0),
			' , 未: ',
			LPAD(sum(unlabeled), 5, 0),
			'）'
		) as `model_result`,
-- 		sum(`unlabeled`) as `unlabeled`,
    sum(`body_1`.`app`) as `app` ,
	    concat('',
	    	CONCAT(
		        LPAD(cast(ROUND(sum(isouter_labeled + nonouter_labeled) * 100.0 / nullif(sum(isouter_pre + nonouter_pre), 0)) as INT), 2, 0), '%'
		    ),
		    '（公: ',
		    CONCAT(
		        LPAD(cast(ROUND(sum(isouter_labeled) * 100.0 / nullif(sum(isouter_pre), 0)) as INT), 2, 0), '%'
		    ),
		    ' , 私: ',
		    CONCAT(
		        LPAD(cast(ROUND(sum(nonouter_labeled) * 100.0 / nullif(sum(nonouter_pre), 0)) as INT), 2, 0), '%'
		    ),
		    '）'
	    ) as `labeled_p`,
 	    CONCAT(
	        LPAD(cast(ROUND((sum(mate) - sum(isouter_pre + nonouter_pre)) * 100.0 / nullif(sum(mate), 0)) as INT), 2, 0), '%'
	    ) as `filter_p`,
		0 as `sort`
	from body_1
	where `body_1`.`publish_time` >= date(now())
	limit 1
	union all
	select
		'latest_times' as `publish_time`,
		DATE_FORMAT(max(`body_1`.`di_dt`), '%H:%i:%s') as `di`,
		DATE_FORMAT(max(`body_1`.`mate_dt`), '%H:%i:%s') as `mate`,
		DATE_FORMAT(max(`body_1`.`pre_dt`), '%H:%i:%s') as `pre`,
		DATE_FORMAT(max(`body_1`.`tomodel_dt`), '%H:%i:%s') as `tomodel`,
		DATE_FORMAT(max(`body_1`.`labeled_dt`), '%H:%i:%s') as `model_result`,
-- 		DATE_FORMAT(max(`unlabeled_dt`), '%H:%i:%s') as `unlabeled`,
DATE_FORMAT(max(`body_1`.`app_dt`), '%H:%i:%s') as `app`,
		null as `labeled_p`,
		null as `filter_p`,
		2 as `sort`
	from body_1 limit 1
	union all
	select
		`body_1`.`publish_time`, `body_1`.`di`, `body_1`.`mate`,
	    (`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`) as `pre`,
	    `body_1`.`tomodel`,
-- 	    (`isouter_labeled` + `nonouter_labeled` + unlabeled) as `model_result`,
concat('', LPAD((`body_1`.`isouter_labeled` + `body_1`.`nonouter_labeled` + `body_1`.`unlabeled`), 5, 0),
			' (打: ',
			LPAD((`body_1`.`isouter_labeled` + `body_1`.`nonouter_labeled` ), 5, 0),
			' , 未: ',
			LPAD((`body_1`.`unlabeled`), 5, 0),
			'）'
		) as `model_result`,
	     `body_1`.`app`,
		concat('',
		    case
		    	when (`body_1`.`isouter_labeled` + `body_1`.`nonouter_labeled`) = 0 or (`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`) = 0 then '00%'
		    	else
		    	CONCAT(LPAD(cast(ROUND((`body_1`.`isouter_labeled` + `body_1`.`nonouter_labeled`) * 100.0 / nullif((`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`), 0)) as INT), 2, 0), '%')
		    end,
		    '（公: ',
		    case
		    	when `body_1`.`isouter_labeled` = 0 or `body_1`.`isouter_pre` = 0 then '00%'
		    	else
		    	CONCAT(LPAD(cast(ROUND((`body_1`.`isouter_labeled`) * 100.0 / nullif((`body_1`.`isouter_pre`), 0)) as INT), 2, 0), '%')
		    end,
		    ' , 私: ',
		    case
		    	when `body_1`.`nonouter_labeled` = 0 or `body_1`.`nonouter_pre` = 0 then '00%'
		    	else
		    	CONCAT(LPAD(cast(ROUND((`body_1`.`nonouter_labeled`) * 100.0 / nullif(`body_1`.`nonouter_pre`, 0)) as INT), 2, 0), '%')
		    end,
		    '）'
	    ) as `labeled_p`,
	    case
	    	when ((`body_1`.`mate`) - (`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`)) = 0 or `body_1`.`mate` = 0 then '00%'
	    	else
	    	CONCAT(LPAD(cast(ROUND(((`body_1`.`mate`) - (`body_1`.`isouter_pre` + `body_1`.`nonouter_pre`)) * 100.0 / nullif((`body_1`.`mate`), 0)) as INT), 2, 0), '%')
	    end as `filter_p`,
		3 as `sort`
	from body_1
)
select
    `final_data`.`publish_time`,
    `final_data`.`di`,
    `final_data`.`mate`,
    `final_data`.`pre`,
    `final_data`.`tomodel`,
    `final_data`.`model_result`,
    --     `labeled`,
    --     `unlabeled`,
    `final_data`.`app`,
    ifnull(`final_data`.`labeled_p`, '') as `labeled_p`,
    ifnull(`final_data`.`filter_p`, '') as `filter_p`
from
    final_data
order by
    `final_data`.`sort`,
    `final_data`.`publish_time` desc;








-- voc_ms_td.voc_sentiment_annotations_results_to_avatr_v source

create or replace
view `voc_sentiment_annotations_results_to_avatr_v` as
select
     *
from
    `voc_sentiment_annotations_results_to_avatr_mv`
where
    (create_time >=  NOW() - INTERVAL 7 day or update_time >=  NOW() - INTERVAL 7 day )
  and (`channel_code` in (
    'pdt_order_awtrxfw',
    'pdt_consult_awtllzxzxdh',
    'pdt_opinion_awtlyb',
    'pdt_quest_awtzp',
    'pdt_order_awtdmsgd',
    'pdt_post_awtaxcx_k_sq',
    'pdt_post_awtaxcx_k_zx',
    'pdt_opinion_awtaxcx_bzyfk'
  ) or brand_code = 'A04' )
order by id
    limit 0;




-- drop MATERIALIZED VIEW voc_sentiment_annotations_results_to_avatr_mv
CREATE MATERIALIZED VIEW voc_sentiment_annotations_results_to_avatr_mv
(id,data_id,channel_catagory,channel_code,channel_name,brand_code,brand_name,car_series_code,car_series_name,model_name,content_type,title,sentiment,intention,data_create_time,publish_time,create_time,update_time,is_outer,hot_word,keywords,original_text_scene,market_id,competitive_type,is_core,series_factory,automark,one_id,user_journey1,user_journey2,user_journey3,usage_scenario_first,usage_scenario_second,d2c_responsible_dept,d2c_accountable_dept,d2c_cc_dept,cust_global_id,cust_classify,cust_main_phone,is_car_owner,vhl_color_name,vhl_product_date,vhl_offline_date,vhl_is_abroad,vhl_dis_ch,vhl_dis_mt,vhl_eng_clsf,vhl_eng_seris,vhl_veh_type,vhl_country,vhl_bd_clsf,vhl_seg_mt,vhl_pow_clsf,vhl_fu_clsf,vhl_modl_st,vhl_std_plnt_code,dlr_oc_id,dlr_oc_code,dlr_oc_name,dlr_oc_province_code,dlr_oc_province,dlr_oc_city_code,dlr_oc_city,dlr_dc_id,dlr_dc_code,dlr_dc_name,dlr_dc_province_code,dlr_dc_province,dlr_dc_city_code,dlr_dc_city,dlr_mc_id,dlr_mc_code,dlr_mc_name,dlr_mc_province_code,dlr_mc_province,dlr_mc_city_code,dlr_mc_city,is_wsater_army,is_manager_focused,is_big_v,author_id,author_nick,is_main_post,original_link,view_count,comment_count,like_count,share_count,favorite_count,work_order_id,quest_id,quest_type,quest_answer_score,quest_business_type,quest_business_scenario,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_event_clarity,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_complaint_flag_needing_prtv_msg,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,tag_sort,topic,topic_text,opinion,cpt_tag_first_code,cpt_tag_second_code,cpt_tag_three_code,cpt_tag_four_code,cpt_tag_first,cpt_tag_second,cpt_tag_three,cpt_tag_four,ujy_tag_first_code,ujy_tag_second_code,ujy_tag_three_code,ujy_tag_four_code,ujy_tag_first,ujy_tag_second,ujy_tag_three,ujy_tag_four,cma_tag_first_code,cma_tag_second_code,cma_tag_three_code,cma_tag_four_code,cma_tag_first,cma_tag_second,cma_tag_three,cma_tag_four,dom_tag_first_code,dom_tag_second_code,dom_tag_three_code,dom_tag_four_code,dom_tag_first,dom_tag_second,dom_tag_three,dom_tag_four,nps_tag_first_code,nps_tag_second_code,nps_tag_three_code,nps_tag_four_code,nps_tag_first,nps_tag_second,nps_tag_three,nps_tag_four,vtr_tag_first_code,vtr_tag_second_code,vtr_tag_three_code,vtr_tag_four_code,vtr_tag_first,vtr_tag_second,vtr_tag_three,vtr_tag_four,abandon,insert_dt)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 120 MINUTE STARTS "2026-01-28 14:20:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "update_time, insert_dt, create_time, data_id",
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
              `id`,
              `data_id`,
              `channel_catagory`,
              `channel_code`,
              `channel_name`,
              `brand_code`,
              `brand_name`,
              `car_series_code`,
              `car_series_name`,
              `model_name`,
              `content_type`,
              `title`,
              `sentiment`,
              `intention`,
              `data_create_time`,
              `publish_time`,
              `create_time`,
              `update_time`,
              `is_outer`,
              `hot_word`,
              `keywords`,
              `original_text_scene`,
              `market_id`,
              `competitive_type`,
              `is_core`,
              `series_factory`,
              `automark`,
              `one_id`,
              `user_journey1`,
              `user_journey2`,
              `user_journey3`,
              `usage_scenario_first`,
              `usage_scenario_second`,
              `d2c_responsible_dept`,
              `d2c_accountable_dept`,
              `d2c_cc_dept`,
              `cust_global_id`,
              `cust_classify`,
              `cust_main_phone`,
              `is_car_owner`,
              `vhl_color_name`,
              `vhl_product_date`,
              `vhl_offline_date`,
              `vhl_is_abroad`,
              `vhl_dis_ch`,
              `vhl_dis_mt`,
              `vhl_eng_clsf`,
              `vhl_eng_seris`,
              `vhl_veh_type`,
              `vhl_country`,
              `vhl_bd_clsf`,
              `vhl_seg_mt`,
              `vhl_pow_clsf`,
              `vhl_fu_clsf`,
              `vhl_modl_st`,
              `vhl_std_plnt_code`,
              `dlr_oc_id`,
              `dlr_oc_code`,
              `dlr_oc_name`,
              `dlr_oc_province_code`,
              `dlr_oc_province`,
              `dlr_oc_city_code`,
              `dlr_oc_city`,
              `dlr_dc_id`,
              `dlr_dc_code`,
              `dlr_dc_name`,
              `dlr_dc_province_code`,
              `dlr_dc_province`,
              `dlr_dc_city_code`,
              `dlr_dc_city`,
              `dlr_mc_id`,
              `dlr_mc_code`,
              `dlr_mc_name`,
              `dlr_mc_province_code`,
              `dlr_mc_province`,
              `dlr_mc_city_code`,
              `dlr_mc_city`,
              `is_wsater_army`,
              `is_manager_focused`,
              `is_big_v`,
              `author_id`,
              `author_nick`,
              `is_main_post`,
              `original_link`,
              `view_count`,
              `comment_count`,
              `like_count`,
              `share_count`,
              `favorite_count`,
              `work_order_id`,
              `quest_id`,
              `quest_type`,
              `quest_answer_score`,
              `quest_business_type`,
              `quest_business_scenario`,
              `tag_accuracy`,
              `tag_customer_issue_classification`,
              `tag_issue_severity`,
              `tag_code_status`,
              `tag_business_domain`,
              `tag_event_clarity`,
              `tag_high_value_flag`,
              `tag_complaint_flag_needing_reply`,
              `tag_complaint_flag_needing_prtv_msg`,
              `tag_high_quality_voc_flag`,
              `tag_new_energy_or_fuel`,
              `tag_need_forvclosed_loop`,
              `tag_sort`,
              `topic`,
              `topic_text`,
              `opinion`,
              `cpt_tag_first_code`,
              `cpt_tag_second_code`,
              `cpt_tag_three_code`,
              `cpt_tag_four_code`,
              `cpt_tag_first`,
              `cpt_tag_second`,
              `cpt_tag_three`,
              `cpt_tag_four`,
              `ujy_tag_first_code`,
              `ujy_tag_second_code`,
              `ujy_tag_three_code`,
              `ujy_tag_four_code`,
              `ujy_tag_first`,
              `ujy_tag_second`,
              `ujy_tag_three`,
              `ujy_tag_four`,
              `cma_tag_first_code`,
              `cma_tag_second_code`,
              `cma_tag_three_code`,
              `cma_tag_four_code`,
              `cma_tag_first`,
              `cma_tag_second`,
              `cma_tag_three`,
              `cma_tag_four`,
              `dom_tag_first_code`,
              `dom_tag_second_code`,
              `dom_tag_three_code`,
              `dom_tag_four_code`,
              `dom_tag_first`,
              `dom_tag_second`,
              `dom_tag_three`,
              `dom_tag_four`,
              `nps_tag_first_code`,
              `nps_tag_second_code`,
              `nps_tag_three_code`,
              `nps_tag_four_code`,
              `nps_tag_first`,
              `nps_tag_second`,
              `nps_tag_three`,
              `nps_tag_four`,
              `vtr_tag_first_code`,
              `vtr_tag_second_code`,
              `vtr_tag_three_code`,
              `vtr_tag_four_code`,
              `vtr_tag_first`,
              `vtr_tag_second`,
              `vtr_tag_three`,
              `vtr_tag_four`,
              `abandon`,
              `insert_dt`
   from
              `voc_anal_flow_sentiment_annotations_results_mv`
   where
-- 	    (`create_time` >=  NOW() - INTERVAL 7 day or `update_time` >=  NOW() - INTERVAL 7 day )
-- 		  and
  ( is_outer = 'N' and ( `brand_code` = 'A04' or `channel_code` in (
                                                                    'pdt_order_awtrxfw',
                                                                    'pdt_consult_awtllzxzxdh',
                                                                    'pdt_opinion_awtlyb',
                                                                    'pdt_quest_awtzp',
                                                                    'pdt_order_awtdmsgd',
                                                                    'pdt_post_awtaxcx_k_sq',
                                                                    'pdt_post_awtaxcx_k_zx',
                                                                    'pdt_opinion_awtaxcx_bzyfk'
      ) ) )
or
  (
      is_outer = 'Y' and `brand_code` = 'A04'
      );

create or replace view voc_sentiment_annotations_results_to_avatr_v as
select * from voc_sentiment_annotations_results_to_avatr_mv


