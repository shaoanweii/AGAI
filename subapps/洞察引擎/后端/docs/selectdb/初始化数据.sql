


--  初始化数据
--
insert into voc_anal_flow_mate_data_full(
    id,publish_time,data_id,work_id,one_id,client_id,channel_id,content_type,
    title,	content,
    user_name,data,done,data_status,model_type,ext_fields,biz_ext_attrs,
    biz_ext_attrs2,biz_ext_attrs3,cust_ext_attrs,vhl_ext_attrs,dealer_ext_attrs,
    prd_ext_attrs,
    create_time
)
select
    id,  data_create_time  as publish_time,data_id,work_id,
    case
        when `one_id` is not null then `one_id`
--         when JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id") is not null then JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id")
        when `is_outer` = 'Y' then concat('o_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        when `is_outer` = 'N' then concat('i_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        else concat('x_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
        end as `one_id` ,
--         one_id,
    '1' as client_id,
    channel_code as channel_id,content_type,
    title,	content,
    user_name,
    JSON_OBJECT(
            'id',id,
            'create_time',create_time,
            'content_type',content_type,
            'data_create_time',data_create_time,
            'data_update_time',data_update_time,
            'data_id',data_id,
            'channel_code',channel_code,
            'brand',brand,
            'series',series,
            'model',model,
            'is_outer',is_outer,
            'one_id',
            case
                when `one_id` is not null then `one_id`
                when `is_outer` = 'Y' then concat('o_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
                when `is_outer` = 'N' then concat('i_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
                else concat('x_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
                end,
            'id_car_no',id_car_no,
            'mobile',mobile,
            'email',email,
            'global_id',global_id,
            'user_id',user_id,
            'user_name',user_name,
            'vhl_id',vhl_id,
            'vhl_vin',vhl_vin,
            'dlr_id',dlr_id,
            'dlr_code',dlr_code,
            'dlr_type',dlr_type,
            'market_id',market_id,
            'title',title,
            'content',content,
            'is_wsater_army',is_wsater_army,
            'weight',weight,
            'work_id',work_id,
            'done',done,
            'model_type',model_type,
            'ds',ds
    ) as `data`,
    CAST(done AS INT) as done,
    0 as  data_status,
    CAST(model_type AS INT) as model_type,
    null as ext_fields,
    attrs as biz_ext_attrs,
    attrs2 as biz_ext_attrs2,
    attrs3 as biz_ext_attrs3,
    null as cust_ext_attrs,null as vhl_ext_attrs,null as dealer_ext_attrs,
    null as prd_ext_attrs,
    create_time
from ads_voc_all_mate_data_m_inc
where date(create_time) between date('2025-11-13') and date('2025-11-13')
  and channel_code is not null
-- and (content_type  is  null or work_id is  null)
order by create_time desc
;

update voc_anal_flow_mate_data_full set data_status = 3
  where data_id in (
      select data_id from ads_voc_flow_model_tags_result_part_m_inc
      group by data_id
);

select count(*) from voc_anal_flow_mate_data_full
where data_status = 3;

REFRESH MATERIALIZED VIEW voc_anal_flow_mate_data_labeled_mv COMPLETE;

select count(*) from voc_anal_flow_mate_data_labeled_mv;







insert into voc_anal_flow_model_tags_result_data_full
(
  abandon
,biz_ext_attrs
,biz_ext_attrs2
,biz_ext_attrs3
,brand_code
,car_series_code
,channel_id
,client_id
,content_type
,create_time
,cust_ext_attrs
,data_id
,dealer_ext_attrs
,description
,done
,ext_fields
,fault_level
,id
,input_data_id
,intention_type
,keywords
,label_type
,model_type
,one_id
,opinion
,original_id
,original_text_scene
,prd_ext_attrs
,publish_time
,raw_data
,sample_data_type
,scenario
,sentiment
,sentiment_score
,subject
,tags_ext_attrs
,topic
,update_time
,vhl_ext_attrs
,work_id

)
select
    abandon
     ,biz_ext_attrs
     ,biz_ext_attrs2
     ,biz_ext_attrs3
     ,brand_code
     ,car_series_code
     ,channel_id
     ,client_id
     ,content_type
     ,create_time
     ,cust_ext_attrs
     ,data_id
     ,dealer_ext_attrs
     ,description
     ,done
     ,ext_fields
     ,fault_level
     ,id
     ,input_data_id
     ,intention_type
     ,keywords
     ,label_type
     ,model_type
     ,one_id
     ,opinion
     ,original_id
     ,original_text_scene
     ,prd_ext_attrs
     ,publish_time
     ,raw_data
     ,sample_data_type
     ,scenario
     ,sentiment
     ,sentiment_score
     ,subject
     ,tags_ext_attrs
     ,topic
     ,update_time
     ,vhl_ext_attrs
     ,work_id

from ads_voc_model_tags_result_data_m_inc
where date(create_time) between date('2025-11-01') and date('2025-11-11')







-- truncate table test1;
-- insert into test1(id)
-- select id from test2 limit 200000;
--
-- delete from voc_anal_flow_mate_data_full
-- where data_id in (
-- 	select id as data_id from test1
-- );
-- -- select count(*) from test2
-- delete from test2 where id in (select id as id from test1);

-- select
-- 	id,publish_time,data_id,work_id,one_id,client_id,channel_id,content_type,title,content,user_name,`data`,
-- 	done,data_status,model_type,ext_fields,biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,cust_ext_attrs,
-- 	vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs,create_time
-- from voc_anal_flow_mate_data_full
-- select count(*) from  voc_anal_flow_mate_data_full
insert into voc_anal_flow_mate_data_full(
    id,publish_time,data_id,work_id,one_id,client_id,channel_id,content_type,title,content,user_name,`data`,
    done,data_status,model_type,ext_fields,biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,cust_ext_attrs,
    vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs,create_time
)
with `raw_data` as (
    select
    `f`.`id`,
    `f`.`publish_time`,
    `f`.`data_id`,
    `f`.`one_id`,
    `f`.`work_id`,
    `f`.`client_id`,
    `f`.`channel_id`,
    `f`.`content_type`,
    `f`.`sample_data_type`,
    `f`.`original_id`,
    `f`.`input_data_id`,
    `f`.`original_text_scene`,
    `f`.`brand_code`,
    `f`.`car_series_code`,
    `f`.`label_type`,
    `f`.`scenario`,
    `f`.`sentiment`,
    `f`.`intention_type`,
    `f`.`topic`,
    `f`.`opinion`,
    `f`.`subject`,
    `f`.`fault_level`,
    `f`.`description`,
    `f`.`sentiment_score`,
    `f`.`keywords`,
    `f`.`model_type`,
    `f`.`raw_data`,
    `f`.`ext_fields`,
    `f`.`biz_ext_attrs`,
    `f`.`biz_ext_attrs2`,
    `f`.`biz_ext_attrs3`,
    `f`.`cust_ext_attrs`,
    `f`.`vhl_ext_attrs`,
    `f`.`dealer_ext_attrs`,
    `f`.`prd_ext_attrs`,
    `f`.`tags_ext_attrs`,
    `f`.`create_time`,
    `f`.`update_time`,
    `f`.`abandon`,
    `f`.`done`,
    `f`.`rn`,
    f.is_outer,
    f.user_id,
    f.user_name
    from
    (
    select
    `id`,
    `publish_time`,
    `data_id`,
    `one_id`,
    `work_id`,
    `client_id`,
    `channel_id`,
    `content_type`,
    `sample_data_type`,
    `original_id`,
    `input_data_id`,
    `original_text_scene`,
    `brand_code`,
    `car_series_code`,
    `label_type`,
    `scenario`,
    `sentiment`,
    `intention_type`,
    `topic`,
    `opinion`,
    `subject`,
    `fault_level`,
    `description`,
    `sentiment_score`,
    `keywords`,
    `model_type`,
    `raw_data`,
    `ext_fields`,
    `biz_ext_attrs`,
    `biz_ext_attrs2`,
    `biz_ext_attrs3`,
    `cust_ext_attrs`,
    `vhl_ext_attrs`,
    `dealer_ext_attrs`,
    `prd_ext_attrs`,
    `tags_ext_attrs`,
    `create_time`,
    `update_time`,
    `abandon`,
    `done`,
    JSON_EXTRACT_STRING(`raw_data`, "$.is_outer") as `is_outer`,
    JSON_EXTRACT_STRING(`raw_data`, "$.user_id") as `user_id`,

    JSON_EXTRACT_STRING(`f`.`raw_data`, "$.user_name") as `user_name`,
    row_number() over (partition by `data_id`
    order by
    `create_time` desc) as `rn`
    from
    `voc_anal_flow_model_tags_result_data_full`
    where
    date(`publish_time`) >= date('2025-10-25') and  date(`publish_time`) <= date('2025-10-31')
    ) `f`
    where
    `f`.`rn` = 1
    )
select
    id,
    publish_time,
    data_id,work_id,
    case
        when  nullif(`one_id`, '') is not null then `one_id`
        --         when JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id") is not null then JSON_EXTRACT_STRING(`cust_ext_attrs`, "$.one_id")
        when `is_outer` = 'Y' then concat('o_', md5(concat_ws(`channel_id`, concat(`user_id`, `user_name`, uuid()) , `channel_id`)))
        --         when `is_outer` = 'N' then concat('i_', md5(concat_ws(`channel_code`, concat(`user_id`, `user_name`, uuid()) , `channel_code`)))
        when `is_outer` = 'N' then concat('i_', md5(concat_ws(`channel_id`, null)))
        else concat('x_', md5(concat_ws(`channel_id`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_id`)))
        end as `one_id` ,
    client_id,
    channel_id,
    content_type,
    JSON_EXTRACT_STRING(`f`.`raw_data`, "$.title") as `title`,
    JSON_EXTRACT_STRING(`f`.`raw_data`, "$.content") as `content`,
    user_name,
    raw_data as data
        ,1 as done,3 as data_status,1 as  model_type,ext_fields
        ,biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,null as cust_ext_attrs,vhl_ext_attrs
        ,dealer_ext_attrs,prd_ext_attrs,create_time
from
    raw_data `f`;








insert into voc_anal_flow_mate_data_full (
    id,
    publish_time,
    data_id,
    work_id,
    one_id,
    client_id,
    channel_id,
    content_type,
    title,
    content,
    user_name,
    `data`,
    ext_fields,
    biz_ext_attrs,
    biz_ext_attrs2,
    biz_ext_attrs3,
    cust_ext_attrs,
    vhl_ext_attrs,
    dealer_ext_attrs,
    prd_ext_attrs,
    create_time,
    done,
    insert_dt,
    data_status,
    model_type
)
with raw_data as (
    select
        *,
        channel_id as channel_code,
        COALESCE(JSON_EXTRACT_STRING(`raw_data`, "$.user_id"), '') AS `user_id`,
        COALESCE(JSON_EXTRACT_STRING(`raw_data`, "$.title"), '') AS `title`,
        COALESCE(JSON_EXTRACT_STRING(`raw_data`, "$.content"), '') AS `content`,
        COALESCE(JSON_EXTRACT_STRING(`raw_data`, "$.user_name"), '') AS `user_name`,
        COALESCE(JSON_EXTRACT_STRING(`raw_data`, "$.is_outer"), '') AS `is_outer`
    from test_labeled_mv
)
select
    id
     ,publish_time
     ,data_id
     ,work_id
     ,case
          when `one_id` is not null then `one_id`
          when `is_outer` = 'Y' then concat('o_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
          when `is_outer` = 'N' then concat('i_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
          else concat('x_', md5(concat(`channel_code`, COALESCE(`user_id`, `user_name`, uuid()) , `channel_code`)))
    end as one_id
     ,client_id
     ,channel_id
     ,content_type
     ,title
     ,content
     ,user_name
     ,raw_data as data
     ,ext_fields
     ,biz_ext_attrs
     ,biz_ext_attrs2
     ,biz_ext_attrs3
     ,cust_ext_attrs
     ,vhl_ext_attrs
     ,dealer_ext_attrs
     ,prd_ext_attrs
     ,create_time
     ,done
     ,insert_dt
     ,3 as data_status
     ,1 as model_type
from raw_data as f






-- 人车数据关联-结果表
-- 844478
-- select count(*) from voc_anal_flow_model_tags_result_data_full2
    insert into voc_anal_flow_model_tags_result_data_full3
(
    id, publish_time, data_id, one_id, work_id, client_id, channel_id, content_type, sample_data_type,
    original_id, input_data_id, original_text_scene, brand_code, car_series_code, label_type, scenario,
    sentiment, intention_type, topic, opinion, subject, fault_level, description, sentiment_score,
    keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3,
    cust_ext_attrs, vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs,
    tags_ext_attrs, create_time,
    update_time, abandon, done, insert_dt
)
with
-- 第二步：仅对 is_outer = 'N' 的数据做关联
    `inner_data` AS (
    SELECT
    JSON_EXTRACT_STRING(`f`.`raw_data`, "$.mobile") as mobile,
    JSON_EXTRACT_STRING(`f`.`raw_data`, "$.vhl_vin") as vhl_vin,
    JSON_EXTRACT_STRING(`f`.`raw_data`, "$.id_car_no") as id_card_no,
    JSON_EXTRACT_STRING(`f`.`raw_data`, "$.dlr_code") as dlr_code,
    id, publish_time, data_id, one_id, work_id, client_id, channel_id, content_type, sample_data_type,
    original_id, input_data_id, original_text_scene, brand_code, car_series_code, label_type, scenario,
    sentiment, intention_type, topic, opinion, subject, fault_level, description, sentiment_score,
    keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3,
-- cust_ext_attrs, vhl_ext_attrs, dealer_ext_attrs,
    prd_ext_attrs,
    tags_ext_attrs, create_time,
    update_time, abandon, done, insert_dt
    FROM voc_anal_flow_model_tags_result_data_full2 as f
    WHERE JSON_EXTRACT_STRING(`raw_data`, "$.is_outer") = 'N'
    order by id
    limit 120000, 150000
    ),
    `joined_inner` AS (
    select
    i.*,
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
    ON `b_by_idcard`.`id_card_no` = `i`.`id_card_no`
    LEFT JOIN `voc_imp_cust_json_b_by_vin_mv` `b_by_vin`
    ON `b_by_vin`.`vin` = `i`.`vhl_vin`
-- 经销商关联
    LEFT JOIN `voc_imp_dealer_json_info_mv` `d`
    ON `d`.`dealer_code` = `i`.`dlr_code`
    )
select
    id, publish_time, data_id, one_id, work_id, client_id, channel_id, content_type, sample_data_type,
    original_id, input_data_id, original_text_scene, brand_code, car_series_code, label_type, scenario,
    sentiment, intention_type, topic, opinion, subject, fault_level, description, sentiment_score,
    keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3,
    cust_ext_attrs, vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs,
    tags_ext_attrs, create_time,
    update_time, abandon, done, insert_dt
from joined_inner









insert into voc_anal_flow_model_tags_result_data_full
(id, publish_time, data_id, one_id, work_id, client_id, channel_id, content_type, sample_data_type, original_id, input_data_id, original_text_scene, brand_code, car_series_code, label_type, scenario, sentiment, intention_type, topic, opinion, subject, fault_level, description, sentiment_score, keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3,
	vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs, tags_ext_attrs, create_time, update_time, abandon, done, insert_dt,
	cust_ext_attrs
)
with gender_data as (
	select
	id, publish_time, data_id, one_id, work_id, client_id, channel_id, content_type, sample_data_type, original_id, input_data_id, original_text_scene, brand_code, car_series_code, label_type, scenario, sentiment, intention_type, topic, opinion, subject, fault_level, description, sentiment_score, keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3,
	vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs, tags_ext_attrs, create_time, update_time, abandon, done, insert_dt,
-- 	JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.gender"),
	JSON_SET(cust_ext_attrs, '$.gender'
					,  TO_BASE64(SM4_ENCRYPT(JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.gender"),'changanvoc2025xx')) )
					as cust_ext_attrs
	from voc_anal_flow_model_tags_result_data_full2 as f
),
cust_nm_data as (
	select
		id, publish_time, data_id, one_id, work_id, client_id, channel_id, content_type, sample_data_type, original_id, input_data_id, original_text_scene, brand_code, car_series_code, label_type, scenario, sentiment, intention_type, topic, opinion, subject, fault_level, description, sentiment_score, keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3,
		vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs, tags_ext_attrs, create_time, update_time, abandon, done, insert_dt,
	-- 	JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.gender"),
		JSON_SET(cust_ext_attrs, '$.cust_nm'
						,  TO_BASE64(SM4_ENCRYPT(JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.cust_nm"),'changanvoc2025xx')) )
						as cust_ext_attrs
	from gender_data as f
),
mobile_data as (
	select
		id, publish_time, data_id, one_id, work_id, client_id, channel_id, content_type, sample_data_type, original_id, input_data_id, original_text_scene, brand_code, car_series_code, label_type, scenario, sentiment, intention_type, topic, opinion, subject, fault_level, description, sentiment_score, keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3,
		vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs, tags_ext_attrs, create_time, update_time, abandon, done, insert_dt,
	-- 	JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.gender"),
		JSON_SET(cust_ext_attrs, '$.mobile'
						,  TO_BASE64(SM4_ENCRYPT(JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.mobile"),'changanvoc2025xx')) )
						as cust_ext_attrs
	from cust_nm_data as f
),
age_data as (
	select
		id, publish_time, data_id, one_id, work_id, client_id, channel_id, content_type, sample_data_type, original_id, input_data_id, original_text_scene, brand_code, car_series_code, label_type, scenario, sentiment, intention_type, topic, opinion, subject, fault_level, description, sentiment_score, keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3,
		vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs, tags_ext_attrs, create_time, update_time, abandon, done, insert_dt,
	-- 	JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.gender"),
		JSON_SET(cust_ext_attrs, '$.age'
						,  TO_BASE64(SM4_ENCRYPT(JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.age"),'changanvoc2025xx')) )
						as cust_ext_attrs
	from mobile_data as f
),
lived_addr_data as (
	select
		id, publish_time, data_id, one_id, work_id, client_id, channel_id, content_type, sample_data_type, original_id, input_data_id, original_text_scene, brand_code, car_series_code, label_type, scenario, sentiment, intention_type, topic, opinion, subject, fault_level, description, sentiment_score, keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3,
		vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs, tags_ext_attrs, create_time, update_time, abandon, done, insert_dt,
	-- 	JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.gender"),
		JSON_SET(cust_ext_attrs, '$.lived_addr'
						,  TO_BASE64(SM4_ENCRYPT(JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.lived_addr"),'changanvoc2025xx')) )
						as cust_ext_attrs
	from age_data as f
),
birthday_dt_data as (
	select
		id, publish_time, data_id, one_id, work_id, client_id, channel_id, content_type, sample_data_type, original_id, input_data_id, original_text_scene, brand_code, car_series_code, label_type, scenario, sentiment, intention_type, topic, opinion, subject, fault_level, description, sentiment_score, keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3,
		vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs, tags_ext_attrs, create_time, update_time, abandon, done, insert_dt,
	-- 	JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.gender"),
		JSON_SET(cust_ext_attrs, '$.birthday_dt'
						,  TO_BASE64(SM4_ENCRYPT(JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.birthday_dt"),'changanvoc2025xx')) )
						as cust_ext_attrs
	from lived_addr_data as f
),
age_group_data as (
	select
		id, publish_time, data_id, one_id, work_id, client_id, channel_id, content_type, sample_data_type, original_id, input_data_id, original_text_scene, brand_code, car_series_code, label_type, scenario, sentiment, intention_type, topic, opinion, subject, fault_level, description, sentiment_score, keywords, model_type, raw_data, ext_fields, biz_ext_attrs, biz_ext_attrs2, biz_ext_attrs3,
		vhl_ext_attrs, dealer_ext_attrs, prd_ext_attrs, tags_ext_attrs, create_time, update_time, abandon, done, insert_dt,
	-- 	JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.gender"),
		JSON_SET(cust_ext_attrs, '$.age_group'
						,  TO_BASE64(SM4_ENCRYPT(JSON_EXTRACT_STRING(`f`.`cust_ext_attrs`, "$.age_group"),'changanvoc2025xx')) )
						as cust_ext_attrs
	from birthday_dt_data as f
)
select * from age_group_data






SELECT `id`, `data_create_time`, `create_time`, `content_type`, `data_update_time`, `data_id`, `channel_code`, `brand`, `series`, `model`, `is_outer`, `mobile`, `id_car_no`, `email`, `global_id`, `user_id`, `user_name`, `vhl_id`, `vhl_vin`, `dlr_id`, `dlr_code`, `dlr_type`, `market_id`, `title`, `content`, `is_wsater_army`, `weight`,  `attrs2`, `attrs3`, `work_id`, `model_type`, `one_id`
	,'764547797eb2e192763f5334028d49c9' AS `client_id`
	,case
		when json_valid(`attrs`) = 0
			then null
		else
			json_set(`attrs`
			, "$.title",null, "$.raw_content",null, "$.ocr_dic",null
			, "$.content",null, "$.ocr",null, "$.istar_asr",null
			, "$.label",null, "$.pic_urls",null, "$.content_xml",null
			, "$.analysis.summary",null)
	end as `attrs`
FROM voc_anal_di_stg_mate_data_pub_m_inc as e
WHERE  `e`.`insert_dt` >= NOW() - INTERVAL 6 hour
        and `e`.`insert_dt` <= NOW()
        AND `e`.`channel_code` IS NOT NULL
        AND `e`.`data_create_time` IS NOT NULL