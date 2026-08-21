


-- SHOW MATERIALIZED VIEWS
-- CANCEL REFRESH MATERIALIZED VIEW sta_model_processed_data;
-- ALTER MATERIALIZED VIEW mv2 SET ( 'session.query_timeout' = '4000' );
-- SHOW CREATE MATERIALIZED VIEW sta_model_processed_data
-- SHOW PARTITIONS FROM sta_model_processed_data;
-- SHOW INDEX FROM sta_model_processed_data
-- select * from voc_computed_result_all_data_m_v
-- REFRESH  MATERIALIZED VIEW sta_model_processed_data
-- REFRESH MATERIALIZED VIEW lo_mv1 PARTITION START ("2024-01-01") END ("2024-03-01");
-- drop MATERIALIZED view voc_computed_result_all_data_m_v
CREATE MATERIALIZED VIEW voc_computed_result_all_data_m_v
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 10 MINUTE )   -- 刷新策略
DISTRIBUTED BY HASH(`id`)  BUCKETS 12  -- 分桶策略
PARTITION BY date_trunc("DAY", data_create_time) -- 分区策略
ORDER BY( data_create_time, brand_code, car_series_code)
PROPERTIES (
"replication_num" = "1"
-- , "colocate_with" = "voc_processing_group"	-- SHOW PROC '/colocation_group';
)
AS
SELECT
    a.id as id                                                                             -- 声音ID新生成
     ,a.publish_time as data_create_time                                                    -- 数据产生时间
     ,a.data_id                                                                             -- 数据唯一标识
     ,channel.channel_catagory_level1 as channel_catagory                                   -- 类别（长视频、社交媒体、资讯类等）
     ,a.channel_id as channel_code                                                          -- 渠道编码
     ,channel.name as channel_name                                                          -- 渠道
     ,a.brand_code as brand_code                                                            -- 品牌编码
     ,carseries.brand_name as brand_name                                                    -- 品牌名称
     ,a.car_series_code                                                                     -- 车系代码
     ,carseries.name as car_series_name                                                     -- 车系
-- ,a.model_code as model_code                                                          -- 车型代码
-- ,carseries.name as model_name                                                          -- 车型
     ,a.content_type                                                                        -- 数据类型(1：咨询/2：意⻅反馈/3：帖⼦评论/4：问卷/5：⼯单)
     ,a.sentiment                                                                           -- 情感
     ,a.intention_type as intention                                                         -- 意图（表扬/建议/咨询/抱怨)）
     ,date(a.create_time) create_date                                                       -- 数据抓取时间
     ,a.keywords                                                                            -- 关键词
     ,get_json_string(a.ext_fields, "$.is_outer") AS `is_outer`                             -- 是否往外数据
     ,get_json_string(a.ext_fields, "$.hot_word") AS `hot_word`                             -- 热词
     ,get_json_string(a.ext_fields, "$.user_journey1") AS `user_journey1`                   -- 旅程维度1（看车、购车等）
     ,get_json_string(a.ext_fields, "$.user_journey2") AS `user_journey2`                   -- 旅程维度2（高速路、高原等）
     ,get_json_string(a.ext_fields, "$.scenario") AS `scenario`                             -- 场景属性（舒适性/材质/异响）
     ,get_json_string(a.ext_fields, "$.d2c_responsible_dept") as d2c_responsible_dept         -- 主责部门
     ,get_json_string(a.ext_fields, "$.d2c_accountable_dept") as d2c_accountable_dept         -- 责任部门
     ,get_json_string(a.ext_fields, "$.d2c_cc_dept") as d2c_cc_dept                           -- 抄送部门
     ,a.one_id                                                                              -- oneId
     ,cust.id_card_type     as cust_id_card_type                                         -- 证件类型
     ,cust.id_card_no     as cust_id_card_no                                             -- 证件号
     ,cust.mobile as cust_mobile                                                            -- 客户关联表
     ,cust.email as cust_email                                                              -- 客户关联表
     ,cust.global_id as cust_global_id                                                      -- 客户关联表
     ,cust.age as cust_age                                                                  -- 客户关联表
     ,cust.gender as cust_gender                                                            -- 客户关联表
     ,cust.lived_prov_cd as  cust_province_code                                             -- 客户关联表
     ,cust.lived_prov_nm as  cust_province                                                  -- 客户关联表
     ,cust.lived_city_cd as cust_city_code                                                  -- 客户关联表
     ,cust.lived_city_nm as cust_city                                                       -- 客户关联表
     ,get_json_string(a.biz_ext_attrs, "$.user_name") AS `cust_nick`                        -- 原始表
     ,cust.cust_type                                                                        -- 客户关联表
     ,cust.is_car_owner_flg                                                                 -- 客户关联表
     ,cust.total_mnt_cnt																	--	总保养次数（值）
     ,cust.veh_purch_price																	--	购车价格
     ,cust.veh_displ																		--	购买车辆排量
     ,cust.reg_mnt_consum_lvl																--	普通维修消费水平
     ,cust.spare_pt_mnt_consum_lvl															--	备件维修消费水平
     ,cust.acc_mnt_consum_lvl																--	事故维修消费水平
     ,cust.mnt_pkg_purch_cnt																--	保养套餐购买次数
     ,cust.cust_accum_consum_amt															--	客户累计消费金额（值）
     ,cust.mnt_rpr_accum_cons_amt															--	维保累计消费金额（值）
     ,cust.mnt_accum_consum_amt																--	保养累计消费金额（值）
     ,cust.repair_accum_cons_amt															--	维修累计消费金额（值）
     ,cust.dustry																			--	行业
     ,cust.lead_level																		--	线索级别
     ,cust.lost_cat																			--	战败类别
     ,cust.is_store_visit_lead																--	是否到店线索
     ,cust.mem_lvl																			--	会员等级
     ,cust.cert_mem_cat																		--	认证会员类别
     ,cust.is_active_mem																	--	是否活跃会员
     ,cust.accum_ca_purch_cnt																--	累计购长安次数
     ,cust.cust_source																		--	客户来源
     ,cust.consum_points_total																--	消费积分总额（值）
     ,cust.cust_value																		--	客户价值
     ,cust.main_chnl_pref																	--	主要渠道偏好
     ,cust.car_owner_type																	--	车主类型
-- ,cust.purchased_date as vhl_purchase_date                                               -- 购买日期
-- ,cust.purchased_date as vhl_purchase_date                                               -- 里程公里数
-- ,cust.purchased_date as vhl_purchase_date                                               -- 表盘公里数
     ,get_json_string(ext_fields, "$.vin") as vin                                           -- 车辆车架号
     ,vhl.col_name as vhl_col_name                                                          -- 颜色名称
     ,vhl.product_date as vhl_product_date                                             		-- 生产日期
     ,vhl.offline_date as vhl_offline_date                                                  -- 出厂日期
     ,vhl.is_abroad as vhl_is_abroad                                                        -- 国内国外
     ,vhl.dis_ch as vhl_dis_ch                                                              -- 排放
     ,vhl.dis_mt as vhl_dis_mt                                                              -- 排量
     ,vhl.eng_clsf as vhl_eng_clsf                                                          -- 动力系列大类
     ,vhl.eng_seris as vhl_eng_seris                                                        -- 动力系列小类
     ,vhl.veh_type as vhl_veh_type                                                          -- 车辆类型（出口车、领用车、代工车、商用车）
     ,vhl.country as vhl_country                                                            -- 国别
     ,vhl.bd_clsf as vhl_bd_clsf                                                            -- 车身类型
     ,vhl.seg_mt as vhl_seg_mt                                                              -- 细分市场
     ,vhl.pow_clsf as vhl_pow_clsf                                                          -- 动力类型
     ,vhl.fu_clsf as vhl_fu_clsf                                                            -- 燃料类型
     ,vhl.modl_st as vhl_modl_st                                                            -- 车型状态号
     ,vhl.std_plnt_code as vhl_std_plnt_code                                                -- 标准工厂编码
     ,dlr_oc.dealership_id        	   	as dlr_oc_id                                        -- 订单中心-经销商ID
     ,dlr_oc.dealership_code       		as dlr_oc_code                                      -- 订单中心-经销商编码
     ,dlr_oc.dealership_name           	as dlr_oc_name                                      -- 订单中心-经销商全称
     ,dlr_oc.province_code  		as dlr_oc_province_code                                 -- 订单中心-经销商所在省编码
     ,dlr_oc.province_name       		as dlr_oc_province 	                                -- 订单中心-经销商所在省
     ,dlr_oc.city_code      			as dlr_oc_city_code                                 -- 订单中心-经销商所在市编码
     ,dlr_oc.city_name           		as dlr_oc_city                                      -- 订单中心-经销商所在市
     ,dlr_dc.dealership_id        	   	as dlr_dc_id                                        -- 交付中心-经销商ID
     ,dlr_dc.dealership_code       		as dlr_dc_code                                      -- 交付中心-经销商编码
     ,dlr_dc.dealership_name           	as dlr_dc_name                                      -- 交付中心-经销商全称
     ,dlr_dc.province_code  		as dlr_dc_province_code                                 -- 交付中心-经销商所在省编码
     ,dlr_dc.province_name       		as dlr_dc_province 	                                -- 交付中心-经销商所在省
     ,dlr_dc.city_code      			as dlr_dc_city_code                                 -- 交付中心-经销商所在市编码
     ,dlr_dc.city_name           		as dlr_dc_city                                      -- 交付中心-经销商所在市
     ,dlr_mc.dealership_id        	   	as dlr_mc_id                                        -- 维保中心-经销商ID
     ,dlr_mc.dealership_code       		as dlr_mc_code                                      -- 维保中心-经销商编码
     ,dlr_mc.dealership_name           	as dlr_mc_name                                      -- 维保中心-经销商全称
     ,dlr_mc.province_code  		as dlr_mc_province_code                                 -- 维保中心-经销商所在省编码
     ,dlr_mc.province_name       		as dlr_mc_province 	                                -- 维保中心-经销商所在省
     ,dlr_mc.city_code      			as dlr_mc_city_code                                 -- 维保中心-经销商所在市编码
     ,dlr_mc.city_name           		as dlr_mc_city                                      -- 维保中心-经销商所在市
     ,get_json_string(biz_ext_attrs, "$.is_wsater_army") as is_wsater_army                    -- 是否水军
     ,get_json_string(biz_ext_attrs, "$.is_manager_focused") as is_manager_focused            -- 是否领导重点关注
     ,get_json_string(biz_ext_attrs, "$.is_big_v") as is_big_v                                -- 是否KOC账号
     ,get_json_string(biz_ext_attrs, "$.author_id") as author_id                              -- 作者账号
     ,get_json_string(biz_ext_attrs, "$.author_nick") as user_nick                            -- 作者昵称
     ,get_json_string(biz_ext_attrs, "$.is_main_post") as is_main_post                        -- 是否主贴(Y/N)
     ,get_json_string(biz_ext_attrs, "$.original_link") as original_link                      -- 帖子原文链接
     ,ifnull(get_json_string(biz_ext_attrs, "$.view_count") ,0  ) as view_count           -- 浏览量or播放量
     ,ifnull(get_json_string(biz_ext_attrs, "$.comment_count") ,0 ) as comment_count     -- 评论量
     ,ifnull(get_json_string(biz_ext_attrs, "$.like_count") ,0 ) as like_count           -- 点赞量
     ,ifnull(get_json_string(biz_ext_attrs, "$.share_count") ,0 ) as share_count         -- 转发量
     ,ifnull(get_json_string(biz_ext_attrs, "$.favorite_count ") ,0 ) as favorite_count  -- 收藏量
     ,get_json_string(biz_ext_attrs, "$.work_order_id") as work_order_id                      -- 工单ID
     ,get_json_string(biz_ext_attrs, "$.work_order_parent_id") as work_order_parent_id        -- 工单关联ID（上级)
     ,get_json_string(biz_ext_attrs, "$.quest_type") as quest_type                            -- 问卷类型
     ,get_json_string(biz_ext_attrs, "$.quest_answer_score") as quest_answer_score            -- 问卷答案分数
     ,get_json_string(biz_ext_attrs, "$.quest_business_type") as quest_business_type          -- 问卷业务类型
     ,get_json_string(biz_ext_attrs, "$.quest_business_scenario") as quest_business_scenario  -- 问卷业务场景
     ,get_json_string(biz_ext_attrs, "$.quest_business_scenario") as model_code               -- 模型返回
     ,get_json_string(biz_ext_attrs, "$.quest_business_scenario") as model_name               -- 模型返回
     ,a.opinion                                                                             -- 原始观点
     ,a.topic                                                                               -- 观点（根因标签ID）
     ,tag_system.topic_text                                                    				-- 观点（内容）
     ,tag_system.vtr_tag_first_code                                                			-- 标签关联表
     ,tag_system.vtr_tag_first                                                     			-- 标签关联表
     ,tag_system.vtr_tag_second_code                                               			-- 标签关联表
     ,tag_system.vtr_tag_second                                                    			-- 标签关联表
     ,tag_system.vtr_tag_three_code                                                			-- 标签关联表
     ,tag_system.vtr_tag_three                                                     			-- 标签关联表
     ,tag_system.vtr_tag_four_code                                                 			-- 标签关联表
     ,tag_system.vtr_tag_four                                                      			-- 标签关联表
     ,tag_system.com_tag_first_code                                                			-- 标签关联表
     ,tag_system.com_tag_first                                                     			-- 标签关联表
     ,tag_system.com_tag_second_code                                               			-- 标签关联表
     ,tag_system.com_tag_second                                                    			-- 标签关联表
     ,tag_system.com_tag_three_code                                                			-- 标签关联表
     ,tag_system.com_tag_three                                                     			-- 标签关联表
     ,tag_system.com_tag_four_code                                                 			-- 标签关联表
     ,tag_system.com_tag_four                                                      			-- 标签关联表
     ,tag_system.adb_tag_first_code                                                			-- 标签关联表
     ,tag_system.adb_tag_first                                                     			-- 标签关联表
     ,tag_system.adb_tag_second_code                                               			-- 标签关联表
     ,tag_system.adb_tag_second                                                    			-- 标签关联表
     ,tag_system.adb_tag_three_code                                                			-- 标签关联表
     ,tag_system.adb_tag_three                                                     			-- 标签关联表
     ,tag_system.adb_tag_four_code                                                 			-- 标签关联表
     ,tag_system.adb_tag_four                                                      			-- 标签关联表
     ,tag_system.cj_tag_first_code                                                 			-- 标签关联表
     ,tag_system.cj_tag_first                                                      			-- 标签关联表
     ,tag_system.cj_tag_second_code                                                			-- 标签关联表
     ,tag_system.cj_tag_second                                                     			-- 标签关联表
     ,tag_system.cj_tag_three_code                                                 			-- 标签关联表
     ,tag_system.cj_tag_three                                                      			-- 标签关联表
     ,tag_system.cj_tag_four_code                                                  			-- 标签关联表
     ,tag_system.cj_tag_four                                                       			-- 标签关联表
     ,tag_system.nps_tag_first_code                                                			-- 标签关联表
     ,tag_system.nps_tag_first                                                     			-- 标签关联表
     ,tag_system.nps_tag_second_code                                               			-- 标签关联表
     ,tag_system.nps_tag_second                                                    			-- 标签关联表
     ,tag_system.nps_tag_three_code                                                  		-- 标签关联表
     ,tag_system.nps_tag_three                                                      		-- 标签关联表
     ,tag_system.nps_tag_four_code                                                    		-- 标签关联表
     ,tag_system.nps_tag_four                                                        		-- 标签关联表
     ,tag_system.tag_accuracy                                                               -- 标签属性关联表
     ,tag_system.tag_customer_issue_classification                                          -- 标签属性关联表
     ,tag_system.tag_issue_severity                                                         -- 标签属性关联表
     ,tag_system.tag_code_status                                                            -- 标签属性关联表
     ,tag_system.tag_business_domain                                                        -- 标签属性关联表
     ,tag_system.tag_event_clarity                                                          -- 标签属性关联表
     ,tag_system.tag_high_value_flag                                                        -- 标签属性关联表
     ,tag_system.tag_complaint_flag_needing_reply                                           -- 标签属性关联表
     ,tag_system.tag_high_quality_voc_flag                                                  -- 标签属性关联表
     ,tag_system.tag_new_energy_or_fuel                                                     -- 标签属性关联表
     ,tag_system.tag_need_forvclosed_loop                                                   -- 标签属性关联表
     ,concat(year(a.publish_time)) as data_create_year                                      -- 原始表
     ,concat(year(a.publish_time), '-', LPAD(quarter (a.publish_time), 2, 0)) as data_create_quarter        -- 原始表
     ,concat(year(a.publish_time), '-', LPAD(month (a.publish_time), 2, 0)) as data_create_month            -- 原始表
     ,concat(year(a.publish_time), '-', LPAD(weekofyear(a.publish_time), 2, 0)) as data_create_week         -- 原始表
from dwd_voc_post_rules_result_data as a
    left join voc_cust_info_m_v as cust on a.one_id = cust.oneid
    left join (select code,name,brand_name from voc_ins_car_series_info_m_v ) as carseries on a.car_series_code = carseries.code
    left join voc_vehicle_info_m_v as vhl on vhl.vin = get_json_string(ext_fields, "$.vin")
    left join voc_dealer_info_m_v as dlr_oc on dlr_oc.dealership_code = get_json_string(ext_fields, "$.dealer_code")
    left join voc_dealer_info_m_v as dlr_dc on dlr_dc.dealership_code = get_json_string(ext_fields, "$.dealer_code")
    left join voc_dealer_info_m_v as dlr_mc on dlr_mc.dealership_code = get_json_string(ext_fields, "$.dealer_code")
    left join (select code,name,channel_catagory_level1 from voc_ins_channel_info_m_v) as channel on channel.code = a.channel_id
    left join voc_ins_tags_system_info_m_v as tag_system on tag_system.topic = a.topic
--  where done = 1  and abandon = 0
-- length(trim(id))
-- 			and (a.brand_code is not null and a.brand_code <> '')
-- 			and (car_series_code is not null and car_series_code <> '')
-- 			and (topic is not null and topic <> '')
-- 			and (sentiment is not null and sentiment <> '')
-- 			and (intention_type is not null and intention_type <> '')





-- select * from mv_ins_province_area
-- REFRESH  MATERIALIZED VIEW mv_ins_province_area
-- drop MATERIALIZED VIEW mv_ins_province_area
-- CREATE MATERIALIZED VIEW rmt_ins_province_area
CREATE MATERIALIZED VIEW mv_ins_province_area
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 1 day )   -- 刷新策略
DISTRIBUTED BY HASH(`area_code`)   BUCKETS 2-- 分桶策略
ORDER BY(area_code,province_code)
PROPERTIES (
"replication_num" = "1"
)
AS
select
area_code,area_name,province_code,province_name
from voc_jdbc.vdp_ms_be.ins_province_area









-- REFRESH  MATERIALIZED VIEW mv_dim_vehicle_info
-- REFRESH MATERIALIZED VIEW lo_mv1 PARTITION START ("2024-01-01") END ("2024-03-01");
-- drop MATERIALIZED view voc_vehicle_info_m_v
CREATE MATERIALIZED VIEW voc_vehicle_info_m_v
REFRESH ASYNC START('2025-01-01 00:01:00') EVERY (interval 60 MINUTE )
DISTRIBUTED BY HASH(`vin`)  BUCKETS 16 -- 分桶策略(分桶数 = BE节点数 × 每个节点的分片数)
ORDER BY(vin)
PROPERTIES (
"replication_num" = "1"
)
AS
select
    vhl.vin
     ,vhl.prod_code
     ,vhl.prod_name
     ,vhl.mdl_code
     ,vhl.mdl_name
     ,vhl.series_code
     ,vhl.series_name
     ,vhl.opt_code
     ,vhl.opt_name
     ,vhl.col_code
     ,vhl.col_name
     ,vhl.eng_clsf
     ,vhl.eng_seris
     ,vhl.eng_mdl
     ,vhl.dis_mt
     ,vhl.dis_ch
     ,vhl.trans_clsf
     ,vhl.trans_form
     ,vhl.custom_code
     ,vhl.veh_type
     ,vhl.vcl_num
     ,vhl.sbu_code
     ,vhl.sbu_name
     ,vhl.continent
     ,vhl.is_abroad
     ,vhl.cntry_code3
     ,vhl.cntry_name
     ,vhl.cntry_eng
     ,vhl.plnt_code
     ,vhl.plnt_name
     ,vhl.product_date
     ,vhl.offline_date
     ,vhl.rtn_veh_date
     ,vhl.src_sys
     ,vhl.src_sys_id
     ,vhl_prd.country	-- 	Y	国别
     ,vhl_prd.seg_mt	-- 	Y	细分市场
     ,vhl_prd.pow_clsf	-- 	Y	动力类型
     ,vhl_prd.fu_clsf	-- 	Y	燃料类型
     ,vhl_prd.bd_clsf	-- 	Y	车身类型
     ,vhl_prd.range_condition	-- 		续使里程工况
     ,vhl_prd.cltc_mil	--		续驶里程
     ,vhl_prd.modl_st	--		车型状态号
     ,vhl_prd.driv_big_clsf	-- 		驱动方式大类
     ,vhl_prd.driv_clsf	-- 		驱动方式小类
     ,vhl_prd.std_plnt_code	-- 	Y	标准工厂编码
     ,vhl_prd.std_plnt_name	-- 	Y	标准工厂名称
from dim_voc_vehicle_info as vhl
         left join dim_voc_product_info as vhl_prd on
    vhl.prod_code = vhl_prd.vcl_cd ;




-- 优化， 客户表字段中的省市需要于voc系统对齐
-- REFRESH  MATERIALIZED VIEW mv_dim_cust_info
-- REFRESH MATERIALIZED VIEW lo_mv1 PARTITION START ("2024-01-01") END ("2024-03-01");
-- drop MATERIALIZED view voc_cust_info_m_v
CREATE MATERIALIZED VIEW voc_cust_info_m_v
REFRESH ASYNC START('2025-01-01 00:02:00') EVERY (interval 60 MINUTE )
DISTRIBUTED BY HASH(`oneid`)  BUCKETS 16 -- 分桶策略(分桶数 = BE节点数 × 每个节点的分片数)
ORDER BY(oneid, id_card_no, mobile)
PROPERTIES (
"replication_num" = "1"
)
AS
with voc_ins_province_area_m_v_ as (
	select
		province_name,province_code,
		area_name,area_code
	from voc_ins_province_area_m_v
)
select
    oneid, cust_classify, id_card_type, id_card_no, global_id, email, mobile, cust_nm, gender,
    age, age_group, birthday_dt, birthday, born_years, life_stage, constellation, zodiac, high_educaion,
    marriage_statue, hukou_prov_cd, hukou_prov_nm, hukou_city_cd, hukou_city_nm, hukou_cty_cd, hukou_cty_nm,
    pap.province_code as lived_prov_cd, pap.province_name as lived_prov_nm,
    pac.city_code as lived_city_cd, pac.city_name as lived_city_nm,
    lived_cty_cd, lived_cty_nm, lived_addr,
    profession, family_income, cust_type, is_exchange_flg, is_re_purchase_flg, is_recommend_flg, is_car_owner_flg,
    is_deal_flg, is_uni_owner_flg, is_jc_owner_flg, is_wc_owner_flg, is_ev_owner_flg, is_qxc_owner_flg, purchase_car_qty,
    purchase_car_times, lately_purchase_time, his_consume_amt, is_member_flg, member_register_mth, mem_activity, is_birthday_1day_flg,
    is_birthday_30day_flg, is_birthday_60day_flg,
    --  tags
    tags.dustry,tags.lead_level,tags.lost_cat,tags.is_store_visit_lead,tags.mem_lvl,tags.cert_mem_cat,tags.is_active_mem,
    tags.accum_ca_purch_cnt,tags.cust_source,tags.consum_points_total,tags.cust_value,tags.main_chnl_pref,tags.car_owner_type,
    tags.total_mnt_cnt,tags.veh_purch_price,tags.veh_displ,tags.reg_mnt_consum_lvl,tags.spare_pt_mnt_consum_lvl,tags.acc_mnt_consum_lvl,
    tags.mnt_pkg_purch_cnt,tags.cust_accum_consum_amt,tags.mnt_rpr_accum_cons_amt,tags.mnt_accum_consum_amt,tags.repair_accum_cons_amt
from dim_voc_cust_info as cust
         left join (
    select
        province_name,province_code
    from voc_ins_province_area_m_v_
    group by province_name,province_code
) as pap on cust.lived_prov_nm = pap.province_name
         left join (
    select
        area_name as city_name, area_code as city_code
    from voc_ins_province_area_m_v_
    group by city_name,city_code
) as pac on cust.lived_city_nm = pac.city_name
         left join voc_data_service_tag_m_v as tags on tags.one_id = cust.oneid ;






-- drop MATERIALIZED VIEW mv_voc_ins_province_area
CREATE MATERIALIZED VIEW voc_ins_province_area_m_v
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 1 DAY )   -- 刷新策略
DISTRIBUTED BY HASH(`id`)   BUCKETS 2-- 分桶策略
ORDER BY(area_name,province_name)
PROPERTIES (
"replication_num" = "1"
)
AS
SELECT * from voc_jdbc.vdp_ms_be.ins_province_area



-- show  MATERIALIZED VIEWs
-- REFRESH  MATERIALIZED VIEW mv_dim_dealer_info
-- REFRESH MATERIALIZED VIEW lo_mv1 PARTITION START ("2024-01-01") END ("2024-03-01");
-- drop MATERIALIZED view voc_dealer_info_m_v
CREATE MATERIALIZED VIEW voc_dealer_info_m_v
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 180 MINUTE )
DISTRIBUTED BY HASH(`dealership_id`)  BUCKETS 16 -- 分桶策略(分桶数 = BE节点数 × 每个节点的分片数)
ORDER BY(dealership_id, dealership_code)
PROPERTIES (
"replication_num" = "1"
)
AS
select
    c1.dealership_id,
    c1.dealership_code,
    c2.dealership_name,
    c2.dealership_short_name,
    c2.city_code,c2.city_name,
    c1.province_code,c1.province_name,
    c1.big_area_code,c1.big_area_sale
-- c1.small_area_code,c1.samll_area_sale,c2.status
from(
        select
            f2.dealership_id,
            f2.dealership_code,
            province_code,
            f1.province_name,
            f2.big_area_code,
            f2.big_area_sale
        -- f2.small_area_code,
        -- f2.samll_area_sale
        from (
                 select
                     province_code,
                     rtrim(province_name,'省市壮族回族维吾尔自治区特别行政区')  as province_name
                 from voc_ins_province_area_m_v
                 group by province_code,province_name
                 union all
                 select '-' as province_code , '-' as province_name
             )f1
                 left join
             (
                 select
                     dealer_id  as  dealership_id ,
                     dealer_code  as  dealership_code ,
                     dealer_name  as  dealership_name , dealer_shortname  as  dealership_short_name ,
                     city_name,  rtrim(province_name,'省市壮族回族维吾尔自治区特别行政区')  as province_name,
                     md5(area) as big_area_code, area as big_area_sale
                 -- , region_code_new as small_area_code,region_name_new as samll_area_sale,status
                 from voc_dealership_data_info_m_v
                 group by dealership_id,dealership_code,dealership_name,dealership_short_name,
                          city_name,province_name
                         ,big_area_code,big_area_sale
                 -- ,small_area_code,samll_area_sale,status
             )f2 on f1.province_name = f2.province_name
        where dealership_code is not null) as c1
        left join
    (
        select
            dealership_code,
            dealership_short_name,
            dealership_name,
            f2.city_code,
            f2.city_name,
            f2.province_code
        -- , big_area_code,big_area_sale,small_area_code,samll_area_sale, status
        from (
                 select
                     dealer_code  as  dealership_code ,
                     dealer_name  as  dealership_name , dealer_shortname  as  dealership_short_name ,
                     rtrim(city_name,'市') as city_name,  province_name,
                     md5(area) as big_area_code, area as big_area_sale
                 -- ,region_code_new as small_area_code,region_name_new as samll_area_sale, status
                 from voc_dealership_data_info_m_v
                 group by dealership_code,dealership_name,dealership_short_name,
                          city_name,province_name
                         ,big_area_code,big_area_sale
                 -- ,small_area_code,samll_area_sale,status
             )f1
                 left join
             (
                 select
                     area_code as  city_code ,
                     rtrim(area_name,'市') as city_name,
                     province_code
                 from voc_ins_province_area_m_v
             )f2 on f1.city_name = f2.city_name
    )as c2 on c1.dealership_code = c2.dealership_code
group by c1.dealership_id,c1.dealership_code,dealership_name,dealership_short_name,
         c2.city_code,c2.city_name,
         c1.province_code,c1.province_name,
         c1.big_area_code,c1.big_area_sale







-- show MATERIALIZED VIEWs
-- REFRESH  MATERIALIZED VIEW mv_dim_dealer_info
-- REFRESH MATERIALIZED VIEW lo_mv1 PARTITION START ("2024-01-01") END ("2024-03-01");
-- drop MATERIALIZED view voc_dealership_data_info_m_v
CREATE MATERIALIZED VIEW voc_dealership_data_info_m_v
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 180 MINUTE )
DISTRIBUTED BY HASH(`dealer_code`)  BUCKETS 16 -- 分桶策略(分桶数 = BE节点数 × 每个节点的分片数)
ORDER BY(dealer_id, dealer_code)
PROPERTIES (
"replication_num" = "1"
)
AS
select
    dealer_code
     ,dealer_id
     ,erp_cd
     ,dealer_name
     ,dealer_shortname
     ,dept_cd
     ,dept_nm
     ,seq
     ,store_lvl
     ,invest_group_cd
     ,invest_group_nm
     ,invest_cd
     ,invest_nm
     ,s1_dlr_cd
     ,s1_dlr_nm
     ,s2_dlr_cd
     ,s2_dlr_nm
     ,main_invest_cd
     ,main_invest_nm
     ,main_dlr_cd
     ,main_dlr_nm
     ,ord_flg
     ,dlv_flg
     ,svs_flg
     ,paint_flg
     ,valid_flg
     ,quit_flg
     ,rez_flg
     ,dlr_type_group_cd
     ,dlr_type_group_nm
     ,dlr_type_cd
     ,dlr_type_nm
     ,chn_type_cd
     ,chn_type_nm
     ,chn_lvl_cd
     ,chn_lvl_nm
     ,image_cd
     ,image_nm
     ,image_lvl_cd
     ,image_lvl_nm
     ,store_front_function
     ,current_state
     ,operation_type
     ,area
     ,war_zone_cd
     ,war_zone_nm
     ,war_zone_part
     ,war_zone_part_user_cd
     ,war_zone_part_user_nm
     ,sdu
     ,sdu_link_man
     ,pap.province_code, dlr.province_name, pac.city_code, dlr.city_name
     ,cty_cd
     ,cty_nm
     ,lng
     ,lat
     ,addr
     ,bank_nm
     ,bank_acct_no
     ,manager_nm
     ,manager_tel
     ,hotline
     ,emergency_tel
     ,in_net_dt
     ,out_net_dt
     ,network_type
     ,ord_dlr_cd
     ,dlv_dlr_cd
     ,svs_dlr_cd
     ,biz_cd
     ,biz_nm
     ,uni_dlr_cd_flg
from dim_voc_dealer_info as dlr
         left join (
    select
        province_name,province_code
    from voc_ins_province_area_m_v
    group by province_name,province_code
) as pap on dlr.province_name = pap.province_name
         left join (
    select
        area_name as city_name, area_code as city_code
    from voc_ins_province_area_m_v
    group by city_name,city_code
) as pac on dlr.city_name = pac.city_name












create  or REPLACE  view vw_dealership_data
-- create  or REPLACE  view sta_dealership_data_view
as
select
    c1.brand,if(c1.big_area_code='-',"-",dealership_code) as dealership_code,
    c2.city_code,c2.city_name,
    c1.province_code,c1.province_name,
    c1.big_area_code,c1.big_area_sale,
    c1.small_area_code,c1.samll_area_sale
from(

        select
            brand,
            province_code,
            f1.province_name,
            f2.big_area_code,
            f2.big_area_sale,
            f2.small_area_code,
            f2.samll_area_sale
        from (
                 select
                     province_code,
                     rtrim(province_name,'省市壮族回族维吾尔自治区特别行政区')  as province_name
                 from rmt_ins_province_area
                 group by province_code,province_name
                 union all
                 select '-' as province_code , '-' as province_name
             )f1
                 left join
             (
                 select city, province ,big_area_code,big_area_sale,small_area_code,samll_area_sale from
                                      (
                                          select  city ,province,big_area_code,big_area_sale,small_area_code,samll_area_sale
                                          from mv_voc_platform_dlr_info
                                      )f
                                  group by city, province , big_area_code,big_area_sale,small_area_code,samll_area_sale
                                  order by city
             )f2 on f1.province_name = f2.province
    )c1
        left join
    (
        select
            brand,
            dealership_code,
            f2.city_code,
            f2.city_name,
            f2.province_code,
            big_area_code,big_area_sale,small_area_code,samll_area_sale
        from (
                 select
                      dlr_code_  as  dealership_code , city as city_name,province as province_name,
                      big_area_code,big_area_sale,small_area_code,samll_area_sale
                  from mv_voc_platform_dlr_info
                  group by dlr_code_ ,city,province,big_area_code,big_area_sale,small_area_code,samll_area_sale
             )f1
                 left join
             (
                 select
                     area_code as  city_code ,
                     rtrim(area_name,'市') as city_name,
                     province_code
                 from rmt_ins_province_area
             )f2 on f1.city_name = f2.city_name
    )c2 on c1.province_code = c2.province_code and c1.brand = c2.brand and c1.big_area_code = c2.big_area_code and c1.small_area_code = c2.small_area_code
group by c1.brand,dealership_code,c2.city_code,c2.city_name,
         c1.province_code,c1.province_name,
         c1.big_area_code,c1.big_area_sale,
         c1.small_area_code,c1.samll_area_sale;










-- 优化
-- select * from mv_ins_car_series_info
-- REFRESH  MATERIALIZED VIEW voc_ins_car_series_info_m_v
-- drop MATERIALIZED VIEW voc_ins_car_series_info_m_v
CREATE MATERIALIZED VIEW voc_ins_car_series_info_m_v
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 5 MINUTE )   -- 刷新策略
DISTRIBUTED BY HASH(`code`)   BUCKETS 2-- 分桶策略
ORDER BY(id,code,brand_code)
PROPERTIES (
"replication_num" = "1"
)
AS
select
	carseries.id
	,carseries.code
	,carseries.brand_id as brand_code
	,brand.name as brand_name
	,carseries.name
	,carseries.name_en
	,carseries.alias
	,carseries.exclusion_words
	,carseries.car_level1
	,carseries.car_level2
	,carseries.energy_type1
	,carseries.energy_type2
	,carseries.img
	,carseries.order_by
	,carseries.operator
	,carseries.create_time
	,carseries.update_time
	,carseries.del_flag
	,carseries.app_id
from voc_jdbc.vdp_ms_be.ins_car_series_info as carseries
left join voc_jdbc.vdp_ms_be.ins_brand_info as brand on brand.code = carseries.brand_id




     -- select * from mv_ins_province_area
-- REFRESH  MATERIALIZED VIEW mv_ins_province_area
-- drop MATERIALIZED VIEW mv_ins_province_area
-- CREATE MATERIALIZED VIEW rmt_ins_province_area
CREATE MATERIALIZED VIEW mv_ins_province_area
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 1 day )   -- 刷新策略
DISTRIBUTED BY HASH(`area_code`)   BUCKETS 2-- 分桶策略
ORDER BY(area_code,province_code)
PROPERTIES (
"replication_num" = "1"
)
AS
select
area_code,area_name,province_code,province_name
from voc_jdbc.vdp_ms_be.ins_province_area








-- REFRESH  MATERIALIZED VIEW mv_dim_vehicle_info
-- REFRESH MATERIALIZED VIEW lo_mv1 PARTITION START ("2024-01-01") END ("2024-03-01");
-- drop MATERIALIZED view mv_dim_voc2_vehicle_info
CREATE MATERIALIZED VIEW mv_dim_voc2_vehicle_info
REFRESH ASYNC START('2025-01-01 00:01:00') EVERY (interval 60 MINUTE )
DISTRIBUTED BY HASH(`vehicle_id`)  BUCKETS 16 -- 分桶策略(分桶数 = BE节点数 × 每个节点的分片数)
ORDER BY(vehicle_id, vin)
PROPERTIES (
"replication_num" = "1"
)
AS
select
dim_vehicle.vehicle_id, dim_vehicle.material_code, dim_vehicle.material_name, dim_vehicle.erp_id, dim_vehicle.erp_name, dim_vehicle.source_code, dim_vehicle.brand_code, dim_vehicle.brand_name, dim_vehicle.series_code, dim_vehicle.series_name,
dim_vehicle.model_code, dim_vehicle.model_name, dim_vehicle.opt_code, dim_vehicle.opt_name, dim_vehicle.color_code, dim_vehicle.color_name, dim_vehicle.org_type, dim_vehicle.org_type_name, dim_vehicle.dealer_id, dim_vehicle.vin, dim_vehicle.life_cycle,
dim_vehicle.life_cycle_name, dim_vehicle.lock_status, dim_vehicle.lock_status_name, dim_vehicle.license_no, dim_vehicle.engine_no, dim_vehicle.package_id, dim_vehicle.purchased_date, dim_vehicle.product_date, dim_vehicle.factory_date,
dim_vehicle.mileage, dim_vehicle.storage_date, dim_vehicle.warehouse_id, dim_vehicle.transfer_no, dim_vehicle.yieldly, dim_vehicle.org_storage_date, dim_vehicle.special_batch_no, dim_vehicle.vehicle_area, dim_vehicle.oem_company_id,
dim_vehicle.batch_no, dim_vehicle.claim_tactics_id, dim_vehicle.n_warehouse_id, dim_vehicle.erp_storage_date, dim_vehicle.org_id, dim_vehicle.vn, dim_vehicle.node_code, dim_vehicle.node_date, dim_vehicle.location, dim_vehicle.vehicle_type, dim_vehicle.remark,
dim_vehicle.gearbox_no, dim_vehicle.rearaxle_no, dim_vehicle.model_year, dim_vehicle.license_date, dim_vehicle.start_mileage, dim_vehicle.meter_mile, dim_vehicle.history_mile, dim_vehicle.ver, dim_vehicle.free_times, dim_vehicle.create_by, dim_vehicle.create_date,
dim_vehicle.update_by, dim_vehicle.update_date, dim_vehicle.w_insert_dt, dim_vehicle.start_date_active, dim_vehicle.end_date_active, dim_vehicle.enable_flag,
dim_product.driv_big_clsf,dim_product.driv_clsf	,dim_product.factory_code, dim_product.factory_name,
dim_product.fu_clsf,  dim_product.seg_mt,   dim_product.cld_seg_mt
from dim_voc2_vehicle_info as dim_vehicle
left join dim_voc2_product_info as dim_product on
dim_vehicle.material_code = dim_product.product_code







-- drop MATERIALIZED VIEW voc_ins_channel_info_m_v
CREATE MATERIALIZED VIEW voc_ins_channel_info_m_v
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 5 MINUTE )   -- 刷新策略
DISTRIBUTED BY HASH(`id`)   BUCKETS 2-- 分桶策略
ORDER BY(code,name,type)
PROPERTIES (
"replication_num" = "1"
)
AS
select
    f.*,
    c.name as channel_catagory_level1
from (
         SELECT
             id,parent_id,name,`type`, status,name_en,create_time,update_time,code,`level`,top_id,description,is_core_channel,data_source_type
         from voc_jdbc.vdp_ms_td.ins_channel
         where status = 1 and type = 'Channel' and top_id is not null
     )f
         left join
     (
         select id,name
         from voc_jdbc.vdp_ms_td.ins_channel
         where parent_id = '0'
     )c on f.top_id = c.id;





     -- 优化
-- select * from mv_ins_car_series_info
-- REFRESH  MATERIALIZED VIEW mv_voc_ins_car_series_info
-- drop MATERIALIZED VIEW voc_ins_tags_info_m_v
     CREATE MATERIALIZED VIEW voc_ins_tags_info_m_v
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 30 MINUTE )   -- 刷新策略
DISTRIBUTED BY HASH(`leaf_code`)   BUCKETS 4-- 分桶策略
ORDER BY(frist_code,leaf_code,tag_type)
PROPERTIES (
"replication_num" = "1"
)
AS
with tags as (
    SELECT
    	`id`
		,`tag_name`
		,`tag_code`
		,`tag_type`
		,`tag_parent_id`
		,`tag_attribute`
        ,create_time
        ,'' as tag_scenario
        ,'' as tag_accuracy
        ,'' as tag_customer_issue_classification
        ,'' as tag_issue_severity
        ,'' as tag_code_status
        ,'' as tag_business_domain
        ,'' as tag_high_value_flag
        ,'' as tag_complaint_flag_needing_reply
        ,'' as tag_high_quality_voc_flag
        ,'' as tag_new_energy_or_fuel
        ,'' as tag_need_forvclosed_loop
        ,'' as tag_event_clarity
        ,'' as d2c_responsible_dept
        ,'' as d2c_accountable_dept
        ,'' as d2c_cc_dept
    FROM voc_jdbc.vdp_ms_td.ins_tag_client
)

     SELECT
         `f1`.`frist_code`
          ,`f1`.`frist_label`
          ,`f2`.`second_code`
          ,`f2`.`second_label`
          ,`f3`.`three_code`
          ,`f3`.`three_label`
          ,`f4`.`four_code`
          ,`f4`.`four_label`
          ,if(`f5`.`leaf_code` IS NULL, `f4`.`four_code`, `f5`.`leaf_code`) AS `leaf_code`
          ,if(`f5`.`leaf_code` IS NULL, `f4`.`four_label`, `f4`.`four_label`) AS `leaf_label`
          ,`f1`.`tag_type`
          ,create_time
          ,tag_accuracy
          ,tag_scenario
          ,tag_customer_issue_classification
          ,tag_issue_severity
          ,tag_code_status
          ,tag_business_domain
          ,tag_event_clarity
          ,tag_high_value_flag
          ,tag_complaint_flag_needing_reply
          ,tag_high_quality_voc_flag
          ,tag_new_energy_or_fuel
          ,tag_need_forvclosed_loop
          ,d2c_responsible_dept
          ,d2c_accountable_dept
          ,d2c_cc_dept
     FROM
         (
             SELECT
                 `tag_name` AS `frist_label`
                  ,`tag_code` AS `frist_code`
                  ,`tag_parent_id`
                  ,`id`
                  ,`tag_type`
                  ,create_time
                  ,tag_accuracy
                  ,tag_scenario
                  ,tag_customer_issue_classification
                  ,tag_issue_severity
                  ,tag_code_status
                  ,tag_business_domain
                  ,tag_event_clarity
                  ,tag_high_value_flag
                  ,tag_complaint_flag_needing_reply
                  ,tag_high_quality_voc_flag
                  ,tag_new_energy_or_fuel
                  ,tag_need_forvclosed_loop
                  ,d2c_responsible_dept
                  ,d2c_accountable_dept
                  ,d2c_cc_dept
             FROM
                 tags
             WHERE
                 `tag_parent_id` = '0') `f1`
             LEFT OUTER JOIN (
             SELECT
                 `tag_name` AS `second_label`,
                 `tag_code` AS `second_code`,
                 `tag_parent_id`,
                 `id`
             FROM
                 tags
         ) `f2` ON `f2`.`tag_parent_id` = `f1`.`id`
             LEFT OUTER JOIN (
             SELECT
                 `tag_name` AS `three_label`,
                 `tag_code` AS `three_code`,
                 `tag_parent_id`,
                 `id`
             FROM
                 tags
         ) `f3` ON `f3`.`tag_parent_id` = `f2`.`id`
             LEFT OUTER JOIN (
             SELECT
                 `tag_name` AS `four_label`,
                 `tag_code` AS `four_code`,
                 `tag_parent_id`,
                 `id`
             FROM
                 tags
         ) `f4` ON  `f4`.`tag_parent_id` = `f3`.`id`
             LEFT OUTER JOIN (
             SELECT
                 `tag_name` AS `leaf_label`,
                 `tag_code` AS `leaf_code`,
                 `tag_parent_id`,
                 `id`
             FROM
                 tags
             WHERE
                 `tag_attribute` = 'FinalLabel'
         ) `f5` ON `f5`.`tag_parent_id` = `f4`.`id`  ;







CREATE MATERIALIZED VIEW voc_ins_tags_system_info_m_v
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 10 MINUTE )   -- 刷新策略
DISTRIBUTED BY HASH(`topic`)   BUCKETS 12-- 分桶策略
ORDER BY(topic)
PROPERTIES (
"replication_num" = "1"
)
AS
select
    leaf_code as topic,
    max(leaf_label) as topic_text,  -- 同一leaf_code的leaf_label应唯一，用max/min均可
    -- 提取VTR类型标签
    max(case when tag_type = 'VTR' then frist_code end) as vtr_tag_first_code,
    max(case when tag_type = 'VTR' then frist_label end) as vtr_tag_first,
    max(case when tag_type = 'VTR' then second_code end) as vtr_tag_second_code,
    max(case when tag_type = 'VTR' then second_label end) as vtr_tag_second,
    max(case when tag_type = 'VTR' then three_code end) as vtr_tag_three_code,
    max(case when tag_type = 'VTR' then three_label end) as vtr_tag_three,
    max(case when tag_type = 'VTR' then four_code end) as vtr_tag_four_code,
    max(case when tag_type = 'VTR' then four_label end) as vtr_tag_four,
    -- 提取COM类型标签
    max(case when tag_type = 'COM' then frist_code end) as com_tag_first_code,
    max(case when tag_type = 'COM' then frist_label end) as com_tag_first,
    max(case when tag_type = 'COM' then second_code end) as com_tag_second_code,
    max(case when tag_type = 'COM' then second_label end) as com_tag_second,
    max(case when tag_type = 'COM' then three_code end) as com_tag_three_code,
    max(case when tag_type = 'COM' then three_label end) as com_tag_three,
    max(case when tag_type = 'COM' then four_code end) as com_tag_four_code,
    max(case when tag_type = 'COM' then four_label end) as com_tag_four,
    -- 提取ADB类型标签
    max(case when tag_type = 'ADB' then frist_code end) as adb_tag_first_code,
    max(case when tag_type = 'ADB' then frist_label end) as adb_tag_first,
    max(case when tag_type = 'ADB' then second_code end) as adb_tag_second_code,
    max(case when tag_type = 'ADB' then second_label end) as adb_tag_second,
    max(case when tag_type = 'ADB' then three_code end) as adb_tag_three_code,
    max(case when tag_type = 'ADB' then three_label end) as adb_tag_three,
    max(case when tag_type = 'ADB' then four_code end) as adb_tag_four_code,
    max(case when tag_type = 'ADB' then four_label end) as adb_tag_four,
    -- 提取CJ类型标签
    max(case when tag_type = 'CJ' then frist_code end) as cj_tag_first_code,
    max(case when tag_type = 'CJ' then frist_label end) as cj_tag_first,
    max(case when tag_type = 'CJ' then second_code end) as cj_tag_second_code,
    max(case when tag_type = 'CJ' then second_label end) as cj_tag_second,
    max(case when tag_type = 'CJ' then three_code end) as cj_tag_three_code,
    max(case when tag_type = 'CJ' then three_label end) as cj_tag_three,
    max(case when tag_type = 'CJ' then four_code end) as cj_tag_four_code,
    max(case when tag_type = 'CJ' then four_label end) as cj_tag_four,
    -- 提取NPS类型标签
    max(case when tag_type = 'NPS' then frist_code end) as nps_tag_first_code,
    max(case when tag_type = 'NPS' then frist_label end) as nps_tag_first,
    max(case when tag_type = 'NPS' then second_code end) as nps_tag_second_code,
    max(case when tag_type = 'NPS' then second_label end) as nps_tag_second,
    max(case when tag_type = 'NPS' then three_code end) as nps_tag_three_code,
    max(case when tag_type = 'NPS' then three_label end) as nps_tag_three,
    max(case when tag_type = 'NPS' then four_code end) as nps_tag_four_code,
    max(case when tag_type = 'NPS' then four_label end) as nps_tag_four,
    -- 标签属性字段（取任意非空值，假设同一leaf_code属性唯一）
    max(d2c_responsible_dept) as d2c_responsible_dept,
    max(d2c_accountable_dept) as d2c_accountable_dept,
    max(d2c_cc_dept) as d2c_cc_dept,
    max(tag_accuracy) as tag_accuracy,
    max(tag_customer_issue_classification) as tag_customer_issue_classification,
    max(tag_issue_severity) as tag_issue_severity,
    max(tag_code_status) as tag_code_status,
    max(tag_business_domain) as tag_business_domain,
    max(tag_event_clarity) as tag_event_clarity,
    max(tag_high_value_flag) as tag_high_value_flag,
    max(tag_complaint_flag_needing_reply) as tag_complaint_flag_needing_reply,
    max(tag_high_quality_voc_flag) as tag_high_quality_voc_flag,
    max(tag_new_energy_or_fuel) as tag_new_energy_or_fuel,
    max(tag_need_forvclosed_loop) as tag_need_forvclosed_loop
from voc_ins_tags_info_m_v as  t
group by leaf_code ;




-- 需优化
-- select * from mv_dealership_data_view
-- REFRESH  MATERIALIZED VIEW mv_dealership_data_view
-- drop MATERIALIZED VIEW mv_voc2_dealership_data_info
-- create  or REPLACE  view sta_dealership_data_view
CREATE MATERIALIZED VIEW mv_voc2_dealership_data_info
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 60 MINUTE )   -- 刷新策略
DISTRIBUTED BY HASH(`dealership_code`)   BUCKETS 4-- 分桶策略
ORDER BY(dealership_code,city_code)
AS
select
    dealership_code as dealership_code,
    c2.dealership_name,
    c2.dealership_short_name,
    c2.city_code,c2.city_name,
    c1.province_code,c1.province_name,
    c1.big_area_code,c1.big_area_sale,
    c1.small_area_code,c1.samll_area_sale,
    c2.status
from(
        select
            province_code,
            f1.province_name,
            f2.big_area_code,
            f2.big_area_sale,
            f2.small_area_code,
            f2.samll_area_sale
        from (
                 select
                     province_code,
                     rtrim(province_name,'省市壮族回族维吾尔自治区特别行政区')  as province_name
                 from mv_voc2_ins_province_area
                 group by province_code,province_name
                 union all
                 select '-' as province_code , '-' as province_name
             )f1
                 left join
             (
                 select
                      dealer_id as dealership_id,  dealer_code  as  dealership_code ,
                      dealer_name  as  dealership_name , dealer_shortname  as  dealership_short_name ,
                      city_name,  province_name,
                      region_code_new as big_area_code, region_name_new as big_area_sale,
                      region_code_new as small_area_code,region_name_new as samll_area_sale,
                      status
                  from mv_dim_voc2_dealer_info
                  group by dealership_id ,dealership_code,dealership_name,dealership_short_name,
                  	city_name,province_name
                  	,big_area_code,big_area_sale,small_area_code,samll_area_sale,status
             )f2 on f1.province_name = f2.province_name
    )c1
        left join
    (
        select
            dealership_code,
            dealership_short_name,
            dealership_name,
            f2.city_code,
            f2.city_name,
            f2.province_code,
            big_area_code,big_area_sale,small_area_code,samll_area_sale,
            status
        from (
				select
                      dealer_id as dealership_id,  dealer_code  as  dealership_code ,
                      dealer_name  as  dealership_name , dealer_shortname  as  dealership_short_name ,
                      city_name,  province_name,
                      region_code_new as big_area_code, region_name_new as big_area_sale,
                      region_code_new as small_area_code,region_name_new as samll_area_sale,
                      status
                  from mv_dim_voc2_dealer_info
                  group by dealership_id ,dealership_code,dealership_name,dealership_short_name,
                  	city_name,province_name
                  	,big_area_code,big_area_sale,small_area_code,samll_area_sale,status
             )f1
                 left join
             (
                 select
                     area_code as  city_code ,
                     rtrim(area_name,'市') as city_name,
                     province_code
                 from mv_voc2_ins_province_area
             )f2 on f1.city_name = f2.city_name
    )c2 on c1.province_code = c2.province_code  and c1.big_area_code = c2.big_area_code and c1.small_area_code = c2.small_area_code
group by dealership_code,dealership_name,dealership_short_name,
		 c2.city_code,c2.city_name,
         c1.province_code,c1.province_name,
         c1.big_area_code,c1.big_area_sale,
         c1.small_area_code,c1.samll_area_sale,
         c2.status










create  or REPLACE  view mv_dealership_data
-- create  or REPLACE  view sta_dealership_data_view
as
select
    c1.brand,if(c1.big_area_code='-',"-",dealership_code) as dealership_code,
    c2.city_code,c2.city_name,
    c1.province_code,c1.province_name,
    c1.big_area_code,c1.big_area_sale,
    c1.small_area_code,c1.samll_area_sale
from(

        select
            brand,
            province_code,
            f1.province_name,
            f2.big_area_code,
            f2.big_area_sale,
            f2.small_area_code,
            f2.samll_area_sale
        from (
                 select
                     province_code,
                     rtrim(province_name,'省市壮族回族维吾尔自治区特别行政区')  as province_name
                 from rmt_ins_province_area
                 group by province_code,province_name
                 union all
                 select '-' as province_code , '-' as province_name
             )f1
                 left join
             (
                 select city, province ,big_area_code,big_area_sale,small_area_code,samll_area_sale from
                                      (
                                          select  city ,province,big_area_code,big_area_sale,small_area_code,samll_area_sale
                                          from mv_voc_platform_dlr_info
                                      )f
                                  group by city, province , big_area_code,big_area_sale,small_area_code,samll_area_sale
                                  order by city
             )f2 on f1.province_name = f2.province
    )c1
        left join
    (
        select
            brand,
            dealership_code,
            f2.city_code,
            f2.city_name,
            f2.province_code,
            big_area_code,big_area_sale,small_area_code,samll_area_sale
        from (
                 select
                      dlr_code_  as  dealership_code , city as city_name,province as province_name,
                      big_area_code,big_area_sale,small_area_code,samll_area_sale
                  from mv_voc_platform_dlr_info
                  group by dlr_code_ ,city,province,big_area_code,big_area_sale,small_area_code,samll_area_sale
             )f1
                 left join
             (
                 select
                     area_code as  city_code ,
                     rtrim(area_name,'市') as city_name,
                     province_code
                 from rmt_ins_province_area
             )f2 on f1.city_name = f2.city_name
    )c2 on c1.province_code = c2.province_code and c1.brand = c2.brand and c1.big_area_code = c2.big_area_code and c1.small_area_code = c2.small_area_code
group by c1.brand,dealership_code,c2.city_code,c2.city_name,
         c1.province_code,c1.province_name,
         c1.big_area_code,c1.big_area_sale,
         c1.small_area_code,c1.samll_area_sale;


CREATE MATERIALIZED VIEW voc_raw_meta_data_range_m_v
DISTRIBUTED BY HASH(`data_id`)
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 5 MINUTE )
AS

     SELECT
         md5(concat(channel_code,id)) as id,
         md5(`id`) as data_id,
         `one_id` as one_id,
         `channel_code` as channel_biz,
         `channel_code` as channel_dc,
         `content` as content,
         `create_date` as create_time,
         `data_create_time` as biz_create_time,
         `dlr_id` as dealership_code_purchase,
         map_from_arrays([
    	'id',
                 'create_date',
                 'data_id',
                 'channel_code',
                 'brand_code',
                 'series_code',
                 'data_create_time',
                 'is_outer',
                 'one_id',
                 'user_id',
                 'user_name',
                 'vhl_id',
                 'vhl_vin',
                 'dlr_id',
                 'content',
                 'title',
                 'mobile',
                 'id_car_no',
                 'session_id',
                 'is_wsater_army']
             ,[id,
                 create_date,
                 data_id,
                 channel_code,
                 brand_code,
                 series_code,
                 data_create_time,
                 is_outer,
                 one_id,
                 user_id,
                 user_name,
                 vhl_id,
                 vhl_vin,
                 dlr_id,
                 content,
                 title,
                 mobile,
                 id_car_no,
                 session_id,
                 is_wsater_army
                     ]) as ext_attrs,
         map_from_arrays(['title',  'content'],[title,  content]) as ext_attrs2,
         attrs as ext_attrs3
     from
         dwd_voc_raw_private_consult
     where date(create_date) between date_sub(now(), 1) and now()  -- 修改成前4个小时范围
       and length(trim(id)) > 0
       and length(trim(one_id)) > 0
       and length(trim(channel_code)) > 0
       and length(trim(data_id)) > 0
       and length(trim(data_create_time)) > 0
       and length(trim(is_outer)) > 0
       and length(trim(title)) > 0
       and length(trim(content)) > 0

  union all

     SELECT
         md5(concat(channel_code,id)) as id,
         md5(`id`) as data_id,
         `one_id` as one_id,
         `channel_code` as channel_biz,
         `channel_code` as channel_dc,
         `content` as content,
         `create_date` as create_time,
         `data_create_time` as biz_create_time,
         `dlr_id` as dealership_code_purchase,
         map_from_arrays([
    	'id',
                 'create_date',
                 'data_id',
                 'channel_code',
                 'brand_code',
                 'series_code',
                 'data_create_time',
                 'is_outer',
                 'one_id',
                 'user_id',
                 'user_name',
                 'vhl_id',
                 'vhl_vin',
                 'dlr_id',
                 'content',
                 'title',
                 'url',
                 'is_wsater_army']
             ,[id,
                 create_date,
                 data_id,
                 channel_code,
                 brand_code,
                 series_code,
                 data_create_time,
                 is_outer,
                 one_id,
                 user_id,
                 user_name,
                 vhl_id,
                 vhl_vin,
                 dlr_id,
                 content,
                 title,
                 url,
                 is_wsater_army
                     ]) as ext_attrs,
         map_from_arrays(['title',  'content'],[title,  content]) as ext_attrs2,
         attrs as ext_attrs3
     from
         dwd_voc_raw_private_opinion
     where date(create_date) between date_sub(now(), 1) and now()  --修改成前4个小时范围
       and length(trim(id)) > 0
       and length(trim(one_id)) > 0
       and length(trim(channel_code)) > 0
       and length(trim(data_id)) > 0
       and length(trim(data_create_time)) > 0
       and length(trim(is_outer)) > 0
       and length(trim(title)) > 0
       and length(trim(content)) > 0

     union all

     SELECT
         md5(concat(channel_code,id)) as id,
         md5(`id`) as data_id,
         `one_id` as one_id,
         `channel_code` as channel_biz,
         `channel_code` as channel_dc,
         `content` as content,
         `create_date` as create_time,
         `data_create_time` as biz_create_time,
         `dlr_id` as dealership_code_purchase,
         map_from_arrays([
    	'id',
                 'create_date',
                 'data_id',
                 'channel_code',
                 'brand_code',
                 'series_code',
                 'data_create_time',
                 'is_outer',
                 'one_id',
                 'user_id',
                 'user_name',
                 'mobile',
                 'id_car_no',
                 'vhl_id',
                 'vhl_vin',
                 'dlr_id',
                 'content',
                 'title',
                 'url',
                 'comment',
                 'is_main_post',
                 'main_post_id',
                 'view_count',
                 'comment_count',
                 'like_count',
                 'share_count',
                 'favorite_count',
                 'is_wsater_army']
             ,[id,
                 create_date,
                 data_id,
                 channel_code,
                 brand_code,
                 series_code,
                 data_create_time,
                 is_outer,
                 one_id,
                 user_id,
                 user_name,
                 mobile,
                 id_car_no,
                 vhl_id,
                 vhl_vin,
                 dlr_id,
                 content,
                 title,
                 url,
                 comment,
                 is_main_post,
                 main_post_id,
                 view_count,
                 comment_count,
                 like_count,
                 share_count,
                 favorite_count,
                 is_wsater_army
                     ]) as ext_attrs,
         map_from_arrays(['title',  'content'],[title,  content]) as ext_attrs2,
         attrs as ext_attrs3
     from
         dwd_voc_raw_private_posts_comment
     where date(create_date) between date_sub(now(), 1) and now()  --修改成前4个小时范围
       and length(trim(id)) > 0
       and length(trim(one_id)) > 0
       and length(trim(channel_code)) > 0
       and length(trim(data_id)) > 0
       and length(trim(data_create_time)) > 0
       and length(trim(is_outer)) > 0
       and length(trim(title)) > 0
       and length(trim(content)) > 0

     union all

     SELECT
         md5(concat(channel_code,id)) as id,
         md5(`id`) as data_id,
         `one_id` as one_id,
         `channel_code` as channel_biz,
         `channel_code` as channel_dc,
         `content` as content,
         `create_date` as create_time,
         `data_create_time` as biz_create_time,
         `dlr_id` as dealership_code_purchase,
         map_from_arrays([
    	'id',
                 'create_date',
                 'data_id',
                 'channel_code',
                 'brand_code',
                 'series_code',
                 'data_create_time',
                 'is_outer',
                 'one_id',
                 'user_id',
                 'user_name',
                 'vhl_id',
                 'vhl_vin',
                 'dlr_id',
                 'content',
                 'title',
                 'market_id',
                 'type',
                 'analysis_type',
                 'quest_type',
                 'quest_answer_score',
                 'mobile',
                 'id_car_no',
                 'url',
                 'is_wsater_army']
             ,[id,
                 create_date,
                 data_id,
                 channel_code,
                 brand_code,
                 series_code,
                 data_create_time,
                 is_outer,
                 one_id,
                 user_id,
                 user_name,
                 vhl_id,
                 vhl_vin,
                 dlr_id,
                 content,
                 title,
                 market_id,
                 type,
                 analysis_type,
                 quest_type,
                 quest_answer_score,
                 mobile,
                 id_car_no,
                 url,
                 is_wsater_army
                     ]) as ext_attrs,
         map_from_arrays(['title',  'content'],[title,  content]) as ext_attrs2,
         attrs as ext_attrs3
     from
         dwd_voc_raw_private_questionnaire
     where date(create_date) between date_sub(now(), 1) and now()  --修改成前4个小时范围
       and length(trim(id)) > 0
       and length(trim(one_id)) > 0
       and length(trim(channel_code)) > 0
       and length(trim(data_id)) > 0
       and length(trim(data_create_time)) > 0
       and length(trim(is_outer)) > 0
       and length(trim(title)) > 0
       and length(trim(content)) > 0

     union all

     SELECT
         md5(concat(channel_code,id)) as id,
         md5(`id`) as data_id,
         `one_id` as one_id,
         `channel_code` as channel_biz,
         `channel_code` as channel_dc,
         `content` as content,
         `create_date` as create_time,
         `data_create_time` as biz_create_time,
         `dlr_id` as dealership_code_purchase,
         map_from_arrays([
    	'id',
                 'create_date',
                 'data_id',
                 'channel_code',
                 'brand_code',
                 'series_code',
                 'data_create_time',
                 'is_outer',
                 'one_id',
                 'user_id',
                 'user_name',
                 'vhl_id',
                 'vhl_vin',
                 'dlr_id',
                 'content',
                 'title',
                 'order_id',
                 'order_type',
                 'parent_order_id',
                 'car_owner_name',
                 'is_car_owner',
                 'market_id',
                 'is_wsater_army']
             ,[id,
                 create_date,
                 data_id,
                 channel_code,
                 brand_code,
                 series_code,
                 data_create_time,
                 is_outer,
                 one_id,
                 user_id,
                 user_name,
                 vhl_id,
                 vhl_vin,
                 dlr_id,
                 content,
                 title,
                 order_id,
                 order_type,
                 parent_order_id,
                 parent_order_id,
                 is_car_owner,
                 market_id,
                 is_wsater_army
                     ]) as ext_attrs,
         map_from_arrays(['title',  'content'],[title,  content]) as ext_attrs2,
         attrs as ext_attrs3
     from
         dwd_voc_raw_private_work_order
     where date(create_date) between date_sub(now(), 1) and now()  --修改成前4个小时范围
       and length(trim(id)) > 0
       and length(trim(one_id)) > 0
       and length(trim(channel_code)) > 0
       and length(trim(data_id)) > 0
       and length(trim(data_create_time)) > 0
       and length(trim(is_outer)) > 0
       and length(trim(title)) > 0
       and length(trim(content)) > 0

     union all

     SELECT
         md5(concat(channel_code,id)) as id,
         md5(`id`) as data_id,
         `one_id` as one_id,
         `channel_code` as channel_biz,
         `channel_code` as channel_dc,
         `content` as content,
         `create_date` as create_time,
         `data_create_time` as biz_create_time,
         `dlr_id` as dealership_code_purchase,
         map_from_arrays([
    	'id',
                 'create_date',
                 'data_id',
                 'channel_code',
                 'brand_code',
                 'series_code',
                 'data_create_time',
                 'is_outer',
                 'one_id',
                 'user_id',
                 'user_name',
                 'vhl_id',
                 'vhl_vin',
                 'dlr_id',
                 'content',
                 'title',
                 'mobile',
                 'id_car_no',
                 'session_id',
                 'is_wsater_army']
             ,[id,
                 create_date,
                 data_id,
                 channel_code,
                 brand_code,
                 series_code,
                 data_create_time,
                 is_outer,
                 one_id,
                 user_id,
                 user_name,
                 vhl_id,
                 vhl_vin,
                 dlr_id,
                 content,
                 title,
                 mobile,
                 id_car_no,
                 session_id,
                 is_wsater_army
                     ]) as ext_attrs,
         map_from_arrays(['title',  'content'],[title,  content]) as ext_attrs2,
         attrs as ext_attrs3
     from
         dwd_voc_raw_public_consult
     where date(create_date) between date_sub(now(), 1) and now()  --修改成前4个小时范围
       and length(trim(id)) > 0
       and length(trim(one_id)) > 0
       and length(trim(channel_code)) > 0
       and length(trim(data_id)) > 0
       and length(trim(data_create_time)) > 0
       and length(trim(is_outer)) > 0
       and length(trim(title)) > 0
       and length(trim(content)) > 0


     union all

     SELECT
         md5(concat(channel_code,id)) as id,
         md5(`id`) as data_id,
         `one_id` as one_id,
         `channel_code` as channel_biz,
         `channel_code` as channel_dc,
         `content` as content,
         `create_date` as create_time,
         `data_create_time` as biz_create_time,
         `dlr_id` as dealership_code_purchase,
         map_from_arrays([
    	'id',
                 'create_date',
                 'data_id',
                 'channel_code',
                 'brand_code',
                 'series_code',
                 'data_create_time',
                 'is_outer',
                 'one_id',
                 'user_id',
                 'user_name',
                 'vhl_id',
                 'vhl_vin',
                 'dlr_id',
                 'content',
                 'title',
                 'url',
                 'is_wsater_army']
             ,[id,
                 create_date,
                 data_id,
                 channel_code,
                 brand_code,
                 series_code,
                 data_create_time,
                 is_outer,
                 one_id,
                 user_id,
                 user_name,
                 vhl_id,
                 vhl_vin,
                 dlr_id,
                 content,
                 title,
                 url,
                 is_wsater_army
                     ]) as ext_attrs,
         map_from_arrays(['title',  'content'],[title,  content]) as ext_attrs2,
         attrs as ext_attrs3
     from
         dwd_voc_raw_public_opinion
     where date(create_date) between date_sub(now(), 1) and now()  --修改成前4个小时范围
       and length(trim(id)) > 0
       and length(trim(one_id)) > 0
       and length(trim(channel_code)) > 0
       and length(trim(data_id)) > 0
       and length(trim(data_create_time)) > 0
       and length(trim(is_outer)) > 0
       and length(trim(title)) > 0
       and length(trim(content)) > 0


     union all

     SELECT
         md5(concat(channel_code,id)) as id,
         md5(`id`) as data_id,
         `one_id` as one_id,
         `channel_code` as channel_biz,
         `channel_code` as channel_dc,
         `content` as content,
         `create_date` as create_time,
         `data_create_time` as biz_create_time,
         `dlr_id` as dealership_code_purchase,
         map_from_arrays([
    	'id',
                 'create_date',
                 'data_id',
                 'channel_code',
                 'brand_code',
                 'series_code',
                 'data_create_time',
                 'is_outer',
                 'one_id',
                 'user_id',
                 'user_name',
                 'mobile',
                 'id_car_no',
                 'vhl_id',
                 'vhl_vin',
                 'dlr_id',
                 'content',
                 'title',
                 'url',
                 'comment',
                 'is_main_post',
                 'main_post_id',
                 'view_count',
                 'comment_count',
                 'like_count',
                 'share_count',
                 'favorite_count',
                 'is_wsater_army']
             ,[id,
                 create_date,
                 data_id,
                 channel_code,
                 brand_code,
                 series_code,
                 data_create_time,
                 is_outer,
                 one_id,
                 user_id,
                 user_name,
                 mobile,
                 id_car_no,
                 vhl_id,
                 vhl_vin,
                 dlr_id,
                 content,
                 title,
                 url,
                 comment,
                 is_main_post,
                 main_post_id,
                 view_count,
                 comment_count,
                 like_count,
                 share_count,
                 favorite_count,
                 is_wsater_army
                     ]) as ext_attrs,
         map_from_arrays(['title',  'content'],[title,  content]) as ext_attrs2,
         attrs as ext_attrs3
     from
         dwd_voc_raw_public_posts_comment
     where date(create_date) between date_sub(now(), 1) and now()  --修改成前4个小时范围
       and length(trim(id)) > 0
       and length(trim(one_id)) > 0
       and length(trim(channel_code)) > 0
       and length(trim(data_id)) > 0
       and length(trim(data_create_time)) > 0
       and length(trim(is_outer)) > 0
       and length(trim(title)) > 0
       and length(trim(content)) > 0

     union all

     SELECT
         md5(concat(channel_code,id)) as id,
         md5(`id`) as data_id,
         `one_id` as one_id,
         `channel_code` as channel_biz,
         `channel_code` as channel_dc,
         `content` as content,
         `create_date` as create_time,
         `data_create_time` as biz_create_time,
         `dlr_id` as dealership_code_purchase,
         map_from_arrays([
    	'id',
                 'create_date',
                 'data_id',
                 'channel_code',
                 'brand_code',
                 'series_code',
                 'data_create_time',
                 'is_outer',
                 'one_id',
                 'user_id',
                 'user_name',
                 'mobile',
                 'id_car_no',
                 'vhl_id',
                 'vhl_vin',
                 'dlr_id',
                 'content',
                 'title',
                 'market_id',
                 'type',
                 'analysis_type',
                 'quest_type',
                 'quest_answer_score',
                 'url',
                 'is_wsater_army']
             ,[id,
                 create_date,
                 data_id,
                 channel_code,
                 brand_code,
                 series_code,
                 data_create_time,
                 is_outer,
                 one_id,
                 user_id,
                 user_name,
                 mobile,
                 id_car_no,
                 vhl_id,
                 vhl_vin,
                 dlr_id,
                 content,
                 title,
                 market_id,
                 type,
                 analysis_type,
                 quest_type,
                 quest_answer_score,
                 url,
                 is_wsater_army
                     ]) as ext_attrs,
         map_from_arrays(['title',  'content'],[title,  content]) as ext_attrs2,
         attrs as ext_attrs3
     from
         dwd_voc_raw_public_questionnaire
     where date(create_date) between date_sub(now(), 1) and now()  --修改成前4个小时范围
       and length(trim(id)) > 0
       and length(trim(one_id)) > 0
       and length(trim(channel_code)) > 0
       and length(trim(data_id)) > 0
       and length(trim(data_create_time)) > 0
       and length(trim(is_outer)) > 0
       and length(trim(title)) > 0
       and length(trim(content)) > 0
     ;







-- drop MATERIALIZED view voc_data_service_tag_m_v
CREATE MATERIALIZED VIEW voc_data_service_tag_m_v
REFRESH ASYNC START('2025-01-01 00:00:00') EVERY (interval 10 MINUTE )   -- 刷新策略
DISTRIBUTED BY HASH(`one_id`)  BUCKETS 12  -- 分桶策略
ORDER BY( one_id)
PROPERTIES (
"replication_num" = "1"
)
AS
SELECT
    one_id
     ,MAX(CASE WHEN tag_id = '159' THEN tag_value ELSE NULL END) AS 	total_mnt_cnt	--	总保养次数（值）
     ,MAX(CASE WHEN tag_id = '193' THEN tag_value ELSE NULL END) AS 	veh_purch_price	--	购车价格
     ,MAX(CASE WHEN tag_id = '140' THEN tag_value ELSE NULL END) AS 	veh_displ	--	购买车辆排量
     ,MAX(CASE WHEN tag_id = '164' THEN tag_value ELSE NULL END) AS 	reg_mnt_consum_lvl	--	普通维修消费水平
     ,MAX(CASE WHEN tag_id = '165' THEN tag_value ELSE NULL END) AS 	spare_pt_mnt_consum_lvl	--	备件维修消费水平
     ,MAX(CASE WHEN tag_id = '168' THEN tag_value ELSE NULL END) AS 	acc_mnt_consum_lvl	--	事故维修消费水平
     ,MAX(CASE WHEN tag_id = '192' THEN tag_value ELSE NULL END) AS 	mnt_pkg_purch_cnt	--	保养套餐购买次数
     ,MAX(CASE WHEN tag_id = '169' THEN tag_value ELSE NULL END) AS 	cust_accum_consum_amt	--	客户累计消费金额（值）
     ,MAX(CASE WHEN tag_id = '170' THEN tag_value ELSE NULL END) AS 	mnt_rpr_accum_cons_amt	--	维保累计消费金额（值）
     ,MAX(CASE WHEN tag_id = '172' THEN tag_value ELSE NULL END) AS 	mnt_accum_consum_amt	--	保养累计消费金额（值）
     ,MAX(CASE WHEN tag_id = '173' THEN tag_value ELSE NULL END) AS 	repair_accum_cons_amt	--	维修累计消费金额（值）
     ,MAX(CASE WHEN tag_id = '125' THEN tag_value ELSE NULL END) AS 	dustry	--	行业
     ,MAX(CASE WHEN tag_id = '120' THEN tag_value ELSE NULL END) AS 	lead_level	--	线索级别
     ,MAX(CASE WHEN tag_id = '121' THEN tag_value ELSE NULL END) AS 	lost_cat	--	战败类别
     ,MAX(CASE WHEN tag_id = '122' THEN tag_value ELSE NULL END) AS 	is_store_visit_lead	--	是否到店线索
     ,MAX(CASE WHEN tag_id = '141' THEN tag_value ELSE NULL END) AS 	mem_lvl	--	会员等级
     ,MAX(CASE WHEN tag_id = '142' THEN tag_value ELSE NULL END) AS 	cert_mem_cat	--	认证会员类别
     ,MAX(CASE WHEN tag_id = '148' THEN tag_value ELSE NULL END) AS 	is_active_mem	--	是否活跃会员
     ,MAX(CASE WHEN tag_id = '126' THEN tag_value ELSE NULL END) AS 	accum_ca_purch_cnt	--	累计购长安次数
     ,MAX(CASE WHEN tag_id = '196' THEN tag_value ELSE NULL END) AS 	cust_source	--	客户来源
     ,MAX(CASE WHEN tag_id = '150' THEN tag_value ELSE NULL END) AS 	consum_points_total	--	消费积分总额（值）
     ,MAX(CASE WHEN tag_id = '184' THEN tag_value ELSE NULL END) AS 	cust_value	--	客户价值
     ,MAX(CASE WHEN tag_id = '151' THEN tag_value ELSE NULL END) AS 	main_chnl_pref	--	主要渠道偏好
     ,MAX(CASE WHEN tag_id = '96' THEN tag_value ELSE NULL END) AS 	car_owner_type	--	车主类型
FROM dim_data_service_tag
GROUP BY one_id -- 按one_id分组，将同一one_id的行聚合为一行



CREATE OR REPLACE VIEW `voc_sta_d2c_order_info_m_v` AS
SELECT
    '' AS `post_data_id`,
    `VDP_RS_TD`.`dwd_voc_sta_d2c_order_info`.`id`,
    `VDP_RS_TD`.`dwd_voc_sta_d2c_order_info`.`create_time`,
    `VDP_RS_TD`.`dwd_voc_sta_d2c_order_info`.`responsible_dept`,
    `VDP_RS_TD`.`dwd_voc_sta_d2c_order_info`.`accountable_dept`,
    `VDP_RS_TD`.`dwd_voc_sta_d2c_order_info`.`cc_dept`,
    `VDP_RS_TD`.`dwd_voc_sta_d2c_order_info`.`status`
FROM
    `VDP_RS_TD`.`dwd_voc_sta_d2c_order_info`

