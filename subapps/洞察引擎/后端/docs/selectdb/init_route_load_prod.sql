-- STOP ROUTINE LOAD FOR voc_anal_flow_pre_rules_result_data_kafka
-- stop ROUTINE LOAD FOR voc_anal_flow_pre_rules_result_data_kafka
CREATE ROUTINE LOAD voc_anal_flow_pre_rules_result_data_kafka ON voc_anal_flow_pre_rules_result_data_full
COLUMNS(id, data_id, one_id, work_id, client_id, channel_id, content_type, `data`, data_md5,
	publish_time, create_time, abandon, done, hit_rules,model_type
    ,ext_fields, biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,cust_ext_attrs,vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs)
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "7",
     "max_batch_interval" = "3",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.oneId\",\"$.workId\",\"$.clientId\",\"$.channelId\",\"$.contentType\"
		,\"$.data\",\"$.dataMd5\",\"$.publishTime\",\"$.createTime\",\"$.abandon\",\"$.done\",\"$.hitRules\",\"$.modelType\"
        ,\"$.extFields\",\"$.bizExtAttrs\",\"$.bizExtAttrs2\",\"$.bizExtAttrs3\"
		,\"$.custExtAttrs\",\"$.vhlExtAttrs\",\"$.dealerExtAttrs\",\"$.prdExtAttrs\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2,3,4,5,6",
    "kafka_topic" = "voc_anal_flow_pre_rules_result_data",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);





-- stop ROUTINE LOAD for voc_anal_flow_model_tags_unlabeled_data_kafka
CREATE ROUTINE LOAD voc_anal_flow_model_tags_unlabeled_data_kafka ON voc_anal_flow_model_tags_unlabeled_data_full
COLUMNS(id, data_id, work_id, one_id, client_id, channel_id, content_type, input_data_id, brand_code,
		car_series_code, opinion, opinion_sentiment, subject, description,
		car_body_label, view_label, create_time, update_time, done,model_type,ext_fields, biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,publish_time
	)
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "7",
     "max_batch_interval" = "30",
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.workId\",\"$.oneId\",\"$.clientId\",\"$.channelId\",\"$.contentType\"
		,\"$.inputDataId\",\"$.brandCode\",\"$.carSeriesCode\",\"$.opinion\",\"$.opinionSentiment\",\"$.subject\"
		,\"$.description\",\"$.carBodyLabel\",\"$.viewLabel\"
		,\"$.createTime\",\"$.updateTime\",\"$.done\",\"$.modelType\",\"$.extFields\",\"$.bizExtAttrs\",\"$.bizExtAttrs2\",\"$.bizExtAttrs3\",\"$.publishTime\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2,3,4,5,6",
    "kafka_topic" = "voc_anal_flow_model_tags_unlabeled_data",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_END"
);






 -- show ROUTINE LOAD
 -- stop  ROUTINE LOAD for voc_anal_flow_model_tags_result_data_kafka
CREATE ROUTINE LOAD voc_anal_flow_model_tags_result_data_kafka ON voc_anal_flow_model_tags_result_data_full
COLUMNS(id,data_id,work_id,client_id,channel_id,original_id,content_type,input_data_id,
sample_data_type,original_text_scene,brand_code,car_series_code,label_type,scenario,sentiment,
intention_type,topic,opinion,subject,fault_level,description,sentiment_score,keywords,publish_time,
create_time,update_time,done,model_type,
raw_data,ext_fields,biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,
cust_ext_attrs,vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs,tags_ext_attrs,one_id
)
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "7",
     "max_batch_interval" = "30",
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.workId\",\"$.clientId\",\"$.channelId\",\"$.originalId\"
,\"$.contentType\",\"$.inputDataId\",\"$.sampleDataType\",\"$.originalTextScene\",\"$.brandCode\",\"$.carSeriesCode\"
,\"$.labelType\",\"$.scenario\",\"$.sentiment\",\"$.intentionType\"
,\"$.topic\",\"$.opinion\",\"$.subject\",\"$.faultLevel\",\"$.description\",\"$.sentimentScore\",\"$.keywords\"
,\"$.publishTime\",\"$.createTime\",\"$.updateTime\",\"$.done\",\"$.modelType\"
,\"$.rawData\",\"$.extFields\",\"$.bizExtAttrs\",\"$.bizExtAttrs2\",\"$.bizExtAttrs3\"
,\"$.custExtAttrs\",\"$.vhlExtAttrs\",\"$.dealerExtAttrs\",\"$.prdExtAttrs\",\"$.tagsExtAttrs\",\"$.oneId\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2,3,4,5,6",
    "kafka_topic" = "voc_anal_flow_model_tags_result_data",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_END"

);



--  stop ROUTINE LOAD FOR voc_anal_di_stg_mate_data_finished_record_kafka
 CREATE ROUTINE LOAD voc_anal_di_stg_mate_data_finished_record_kafka ON voc_anal_di_stg_mate_data_finished_record
 COLUMNS(id, data_id, work_id, channel_type, retry_count, error_code, error_msg, `data`, create_time, last_exec_time, status, tid)
 PROPERTIES
 (
     "enclose"="\"",
     "escape"="\\",
     "desired_concurrent_number" = "3",
     "max_batch_interval" = "30",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
     "format" = "json",
     "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.workId\",\"$.channelType\",\"$.retryCount\",\"$.errorCode\",\"$.errorMsg\",
             \"$.data\",\"$.createTime\",\"$.lastExecTime\",\"$.status\",\"$.tid\"]"
  )
  FROM KAFKA
  (
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2",
    "kafka_topic" = "voc_anal_di_stg_mate_data_finished_record",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_END"
  );






-- stop ROUTINE LOAD FOR voc_anal_flow_error_data_record_kafka
-- show  ROUTINE LOAD

CREATE ROUTINE LOAD voc_anal_flow_error_data_record_kafka ON voc_anal_flow_error_data_record
COLUMNS(id, `table`, `action`, work_id, client_id, `data`, create_time, tid)
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "5",
     "max_batch_interval" = "30",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.table\",\"$.action\",\"$.workId\",\"$.clientId\"
		,\"$.data\",\"$.create_time\",\"$.tid\"]"
 )
 FROM KAFKA
 (

    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2",
    "kafka_topic" = "voc_anal_flow_error_data_record",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
 );




-- stop  ROUTINE LOAD for voc_anal_flow_mate_data_full_kafka
CREATE ROUTINE LOAD voc_anal_flow_mate_data_full_kafka ON voc_anal_flow_mate_data_full
COLUMNS(id, data_id, one_id, work_id, client_id, channel_id, content_type, title, content, user_name, `data`, done
                    , data_status,model_type,ext_fields, biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3
                    , cust_ext_attrs,vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs
                    ,publish_time, create_time)
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "7",
     "max_batch_interval" = "3",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.oneId\",\"$.workId\",\"$.clientId\",\"$.channelId\",\"$.contentType\",\"$.title\",\"$.content\"
        ,\"$.userName\",\"$.data\",\"$.done\",\"$.dataStatus\",\"$.modelType\",\"$.extFields\",\"$.bizExtAttrs\",\"$.bizExtAttrs2\",\"$.bizExtAttrs3\"
		,\"$.custExtAttrs\",\"$.vhlExtAttrs\",\"$.dealerExtAttrs\",\"$.prdExtAttrs\"
        ,\"$.publishTime\",\"$.createTime\"]"
 )
FROM KAFKA
(
	"kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2,3,4,5,6",
    "kafka_topic" = "voc_anal_flow_mate_data",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);

--


-- show ROUTINE LOAD
-- stop ROUTINE LOAD for voc_imp_hudi_dwd_maf_veh_d_full_kafka
CREATE ROUTINE LOAD voc_imp_hudi_dwd_maf_veh_d_full_kafka ON voc_imp_hudi_dwd_maf_veh_d_full
COLUMNS(
    vin,year,month,quarter_name,hour,period_date,period_time_sec,period_wid,prod_code,prod_name,enterp_nm,
    enterp_bd,bus_bd_sec_cls,prod_bd_seq,prod_bd,vcl_cd,vcl_clr_cd,proj_cd,bd_clsf,pow_clsf,fu_clsf,pub_cd,
    driv_motor_mdl,mdl_code,mdl_name,series_code,series_name,opt_code,opt_name,col_code,
    eng_clsf,eng_seris,eng_mdl,dis_mt,dis_ch,trans_clsf,trans_form,custom_code,veh_type,vcl_num,sbu_code,sbu_name,
    continent,home_abroad,cntry_code3,cntry_name,cntry_eng,plnt_code,plnt_name,product_date,offline_date,
    rtn_veh_date,src_sys,src_sys_id,job_name,batch_dt,manu_bs,manu_bs_cd,sbu_name_full,std_plnt_code,
    std_plnt_name,grp_nm,bus_bd_cd,prod_bd_cd,prod_nm_cd,cfgtn_nm,annual_mdl,model_ver_nm,model_ver_cd,
    time_mkt,seg_mt,modl_st,modl_st_nm,country,ds
)
PROPERTIES (
    "desired_concurrent_number" = "3",           -- 并发数别太高
    "max_batch_interval" = "60",                 -- 秒
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json"
)
FROM KAFKA (
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2",
    "kafka_topic" = "voc_imp_hudi_dwd_maf_veh_d_full",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);




-- show ROUTINE LOAD
-- stop ROUTINE LOAD for voc_imp_hudi_dwd_maf_veh_d_full_kafka
CREATE ROUTINE LOAD voc_anal_flow_pre_rules_abandon_kafka ON voc_anal_flow_pre_rules_abandon
COLUMNS(id)
PROPERTIES (
    "desired_concurrent_number" = "3",           -- 并发数别太高
    "max_batch_interval" = "60",                 -- 秒
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json"
)
FROM KAFKA (
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0",
    "kafka_topic" = "voc_anal_flow_pre_rules_abandon",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);





-- show ROUTINE LOAD
-- stop ROUTINE LOAD for voc_imp_hudi_dwd_maf_veh_d_full_kafka
CREATE ROUTINE LOAD voc_anal_flow_mate_data_status_kafka ON voc_anal_flow_mate_data_status
COLUMNS(data)
PROPERTIES (
    "desired_concurrent_number" = "3",           -- 并发数别太高
    "max_batch_interval" = "120",                 -- 秒
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "jsonpaths" = "[\"$.data\"]"
)
FROM KAFKA (
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0",
    "kafka_topic" = "voc_anal_flow_mate_data_status",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);




-- show ROUTINE LOAD
-- stop ROUTINE LOAD for voc_ins_api_reqeust_record_kafka
CREATE ROUTINE LOAD voc_ins_api_reqeust_record_kafka ON voc_ins_api_reqeust_record
COLUMNS(id,create_time,log_type,log_content,operate_type,userid,username,ip,method,request_url,request_param,request_type,
cost_time,create_by,update_by,update_time,app_id,code,message,tid,insert_dt)
PROPERTIES (
    "desired_concurrent_number" = "3",           -- 并发数别太高
    "max_batch_interval" = "3",                 -- 秒
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.create_time\",\"$.log_type\",\"$.log_content\",\"$.operate_type\",\"$.userid\",
\"$.username\",\"$.ip\",\"$.method\",\"$.request_url\",\"$.request_param\",\"$.request_type\",\"$.cost_time\",
\"$.create_by\",\"$.update_by\",\"$.update_time\",\"$.app_id\",\"$.code\",\"$.message\",\"$.tid\",\"$.insert_dt\"]"
)
FROM KAFKA (
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2",
    "kafka_topic" = "voc_ins_api_reqeust_record",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);






-- show ROUTINE LOAD
-- stop ROUTINE LOAD for voc_imp_hudi_dm_voc_cust_kafka
CREATE ROUTINE LOAD voc_imp_hudi_dm_voc_cust_kafka ON voc_imp_hudi_dm_voc_cust
WITH APPEND
COLUMNS(
    oneid, cust_classify, id_card_type, id_card_no, global_id, email, mobile, cust_nm, gender, age, age_group,
    birthday_dt, birthday, born_years, life_stage, constellation, zodiac, high_educaion, marriage_statue, hukou_prov_cd,
    hukie_prov_nm, hukou_city_cd, hukou_city_nm, hukou_cty_cd, hukou_cty_nm, lived_prov_cd, lived_prov_nm, lived_city_cd,
    lived_city_nm, lived_cty_cd, lived_cty_nm, lived_addr, profession, family_income, cust_type, is_exchange_flg,
    is_re_purchase_flg, is_recommend_flg, is_car_owner_flg, is_deal_flg, is_uni_owner_flg, is_jc_owner_flg,
    is_wc_owner_flg, is_ev_owner_flg, is_qxc_owner_flg, purchase_car_qty, purchase_car_times, lately_purchase_time,
    his_consume_amt, is_member_flg, member_register_mth, mem_activity, is_birthday_1day_flg, is_birthday_30day_flg,
    is_birthday_60day_flg
)
PROPERTIES (
    "desired_concurrent_number" = "3",           -- 并发数别太高
    "max_batch_interval" = "30",                 -- 秒
    "format" = "json",
    "jsonpaths" = "[\"$.oneid\",\"$.cust_classify\",\"$.id_card_type\",\"$.id_card_no\",\"$.global_id\",\"$.email\",\"$.mobile\",
       \"$.cust_nm\",\"$.gender\",\"$.age\",\"$.age_group\",\"$.birthday_dt\",\"$.birthday\",\"$.born_years\",\"$.life_stage\",
       \"$.constellation\",\"$.zodiac\",\"$.high_educaion\",\"$.marriage_statue\",\"$.hukou_prov_cd\",\"$.hukie_prov_nm\",
       \"$.hukou_city_cd\",\"$.hukou_city_nm\",\"$.hukou_cty_cd\",\"$.hukou_cty_nm\",\"$.lived_prov_cd\",\"$.lived_prov_nm\",
       \"$.lived_city_cd\",\"$.lived_city_nm\",\"$.lived_cty_cd\",\"$.lived_cty_nm\",\"$.lived_addr\",\"$.profession\",
       \"$.family_income\",\"$.cust_type\",\"$.is_exchange_flg\",\"$.is_re_purchase_flg\",\"$.is_recommend_flg\",
       \"$.is_car_owner_flg\",\"$.is_deal_flg\",\"$.is_uni_owner_flg\",\"$.is_jc_owner_flg\",\"$.is_wc_owner_flg\",
       \"$.is_ev_owner_flg\",\"$.is_qxc_owner_flg\",\"$.purchase_car_qty\",\"$.purchase_car_times\",\"$.lately_purchase_time\",
       \"$.his_consume_amt\",\"$.is_member_flg\",\"$.member_register_mth\",\"$.mem_activity\",\"$.is_birthday_1day_flg\",
       \"$.is_birthday_30day_flg\",\"$.is_birthday_60day_flg\",\"$.ds\"]"
)
FROM KAFKA (
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_topic" = "voc_imp_hudi_dm_voc_cust",
    "kafka_partitions" = "0,1,2",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);









CREATE ROUTINE LOAD voc_anal_flow_real_time_mate_kafka ON voc_anal_flow_real_time
columns(publish_time,is_outer,abandon = 0, data_id,
	window_ts=FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(publish_time) / 1200) * 1200),
	type = 2, insert_dt = now(),
	 data_id_bitmap = bitmap_hash64(data_id) )
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "7",
     "max_batch_interval" = "10",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "jsonpaths" = "[\"$.publishTime\",\"$.data.is_outer\",\"$.dataId\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2,3,4,5,6",
    "kafka_topic" = "voc_anal_flow_mate_data",
    "property.group.id" = "voc-analysis-sdb-time-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);






CREATE ROUTINE LOAD voc_anal_flow_real_time_pre_kafka ON voc_anal_flow_real_time
columns(
	publish_time,is_outer, abandon, data_id,
	window_ts=FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(publish_time) / 1200) * 1200),
	type = 3, insert_dt = now(),
	data_id_bitmap = case when abandon <> '1'  then bitmap_hash64(data_id) else  bitmap_empty() end     )
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "7",
     "max_batch_interval" = "10",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "jsonpaths" = "[\"$.publishTime\",\"$.data.is_outer\",\"$.abandon\",\"$.dataId\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2,3,4,5,6",
    "kafka_topic" = "voc_anal_flow_pre_rules_result_data",
    "property.group.id" = "voc-analysis-sdb-prod-test",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);







CREATE ROUTINE LOAD voc_anal_flow_real_time_result_kafka ON voc_anal_flow_real_time
columns(publish_time,is_outer,abandon = 0, data_id,
	window_ts=FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(publish_time) / 1200) * 1200),
	type = 4, insert_dt = now(),
	 data_id_bitmap = bitmap_hash64(data_id) )
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "7",
     "max_batch_interval" = "10",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "jsonpaths" = "[\"$.publishTime\",\"$.rawData.is_outer\",\"$.dataId\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2,3,4,5,6",
    "kafka_topic" = "voc_anal_flow_model_tags_result_data",
    "property.group.id" = "voc-analysis-sdb-time-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);




CREATE ROUTINE LOAD voc_anal_flow_real_time_unlabeled_kafka ON voc_anal_flow_real_time
columns(publish_time,data_id, abandon = 0,  is_outer = null,
	window_ts=FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(publish_time) / 1200) * 1200),
	type = 5, insert_dt = now(),
	 data_id_bitmap = bitmap_hash64(data_id) )
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "7",
     "max_batch_interval" = "10",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "jsonpaths" = "[\"$.publishTime\",\"$.dataId\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2,3,4,5,6",
    "kafka_topic" = "voc_anal_flow_model_tags_unlabeled_data",
    "property.group.id" = "voc-analysis-sdb-time-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);




CREATE ROUTINE LOAD voc_anal_flow_real_time_to_model_kafka ON voc_anal_flow_real_time
columns(publish_time ,data_id, abandon = 0,  is_outer = null,
	window_ts=FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(FROM_UNIXTIME(publish_time / 1000)) / 1200) * 1200),
	type = 6, insert_dt = now(),
	 data_id_bitmap = bitmap_hash64(data_id) )

PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "7",
     "max_batch_interval" = "10",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "jsonpaths" = "[\"$.source_data.ext.publishTime\",\"$.source_data.ext.dataId\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2,3,4,5",
    "kafka_topic" = "voc_toModel_topic",
    "property.group.id" = "voc-analysis-sdb-time-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);


       

-- stop ROUTINE load for voc_anal_flow_to_model_data_kafka
CREATE ROUTINE LOAD voc_anal_flow_to_model_data_kafka ON voc_anal_flow_to_model_data
columns(id,create_time,data_id,channel_code,publish_time_, publish_time=FROM_UNIXTIME(publish_time_ / 1000) ,work_id)
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "7",
     "max_batch_interval" = "10",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "jsonpaths" = "[\"$.topic_id\",\"$.source_data.create_time\",\"$.source_data.ext.dataId\",\"$.source_data.dataSource\"
	,\"$.source_data.ext.publishTime\",\"$.workId\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2,3,4,5",
    "kafka_topic" = "voc_toModel_topic",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);





CREATE ROUTINE LOAD voc_anal_di_raw_public_domain_data_full_complaint1_kafka ON voc_anal_di_raw_public_domain_data_full
columns(data, data_type=1)
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "12",
     "max_batch_interval" = "30",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "json_root" = "$.raw_data",
    "jsonpaths" = "[\"$.data\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.64.22.151:39094,10.64.22.151:39095,10.64.22.151:39096",
    "kafka_partitions" = "0,1,2,3,4,5,6,7,8,9,10,11",
    "kafka_topic" = "complaint2_original_V1",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_END"
);



CREATE ROUTINE LOAD voc_anal_di_raw_public_domain_data_full_complaint2_kafka ON voc_anal_di_raw_public_domain_data_full
columns(data, data_type=2)
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "12",
     "max_batch_interval" = "30",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "json_root" = "$.raw_data",
    "jsonpaths" = "[\"$.data\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.64.22.151:39094,10.64.22.151:39095,10.64.22.151:39096",
    "kafka_partitions" = "0,1,2,3,4,5,6,7,8,9,10,11",
    "kafka_topic" = "complaint2_original_V2",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_END"
);



CREATE ROUTINE LOAD voc_anal_di_raw_public_domain_data_full_opinions1_kafka ON voc_anal_di_raw_public_domain_data_full
columns(data, data_type=3)
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "12",
     "max_batch_interval" = "30",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "json_root" = "$.raw_data",
    "jsonpaths" = "[\"$.data\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.64.22.151:39094,10.64.22.151:39095,10.64.22.151:39096",
    "kafka_partitions" = "0,1,2,3,4,5,6,7,8,9,10,11",
    "kafka_topic" = "complaint2_original_V1",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_END"
);


CREATE ROUTINE LOAD voc_anal_di_raw_public_domain_data_full_opinions2_kafka ON voc_anal_di_raw_public_domain_data_full
columns(data, data_type=4)
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "12",
     "max_batch_interval" = "30",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "json_root" = "$.raw_data",
    "jsonpaths" = "[\"$.data\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.64.22.151:39094,10.64.22.151:39095,10.64.22.151:39096",
    "kafka_partitions" = "0,1,2,3,4,5,6,7,8,9,10,11",
    "kafka_topic" = "opinions2_original_V2",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_END"
);



 CREATE ROUTINE LOAD voc_anal_di_pub_domain_data_finished_record_kafka ON voc_anal_di_pub_domain_data_finished_record
 COLUMNS(id, data_id, work_id, channel_type, retry_count, error_code, error_msg, `data`, create_time, last_exec_time, status, tid)
 PROPERTIES
 (
     "enclose"="\"",
     "escape"="\\",
    "desired_concurrent_number" = "3",           -- 并发数别太高
    "max_batch_interval" = "5",                 -- 秒
    "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "1073741824",             -- 1GB/批
    "exec_mem_limit" = "2147483648",              -- 2GB/任务
     "format" = "json",
     "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.workId\",\"$.channelType\",\"$.retryCount\",\"$.errorCode\",\"$.errorMsg\",
             \"$.data\",\"$.createTime\",\"$.lastExecTime\",\"$.status\",\"$.tid\"]"
  )
  FROM KAFKA
  (
       "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
        "kafka_partitions" = "0,1,2",
      "kafka_topic" = "voc_anal_di_pub_domain_data_finished_record",
      "property.group.id" = "voc-analysis-sdb-prod",
      "property.kafka_default_offsets" = "OFFSET_BEGINNING"
  );




CREATE ROUTINE LOAD voc_anal_di_stg_mate_data_pub_m_inc_kafka ON voc_anal_di_stg_mate_data_pub_m_inc
 COLUMNS(id, create_time, data_create_time, content_type, data_update_time, data_id,
 channel_code, brand, series, model, is_outer, one_id, id_car_no, mobile, email, global_id,
 user_id, user_name, vhl_id, vhl_vin, dlr_id, dlr_code, dlr_type, market_id, title, content,
 is_wsater_army, weight, attrs, attrs2, attrs3, work_id, done, model_type,is_deleted)
 PROPERTIES
 (
     "enclose"="\"",
     "escape"="\\",
    "desired_concurrent_number" = "3",           -- 并发数别太高
    "max_batch_interval" = "5",                 -- 秒
    "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "1073741824",             -- 1GB/批
    "exec_mem_limit" = "2147483648",              -- 2GB/任务
     "format" = "json",
     "jsonpaths" = "[\"$.id\",\"$.createTime\", \"$.dataCreateTime\",\"$.contentType\",\"$.dataUpdateTime\",\"$.dataId\",
                    \"$.channelCode\",\"$.brand\", \"$.series\",\"$.model\",\"$.isOuter\",\"$.oneId\",\"$.idCarNo\", \"$.mobile\",\"$.email\",\"$.globalId\",
                    \"$.userId\",\"$.userName\",\"$.vhlId\", \"$.vhlVin\",\"$.dlrId\",\"$.dlrCode\",\"$.dlrType\",\"$.marketId\", \"$.title\",\"$.content\",
                    \"$.isWsaterArmy\",\"$.weight\",\"$.attrs\",\"$.attrs2\", \"$.attrs3\",\"$.workId\",\"$.done\",\"$.modelType\",\"$.isDeleted\"]"
  )
  FROM KAFKA
  (
      "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
        "kafka_partitions" = "0,1,2",
      "kafka_topic" = "voc_anal_di_stg_mate_data_pub_m_inc",
      "property.group.id" = "voc-analysis-sdb-prod",
      "property.kafka_default_offsets" = "OFFSET_END"
  );







CREATE ROUTINE LOAD voc_anal_flow_model_tags_result_data_ext_kafka ON voc_anal_flow_model_tags_result_data_ext
COLUMNS(id,publish_time,data_id,channel_catagory,channel_code,channel_name,brand_code,brand_name,car_series_code,car_series_name,model_name,content_type,title,
	content,sentiment,intention,data_create_time,create_time,update_time,is_outer,hot_word,keywords,original_text_scene,market_id,competitive_type,is_core,series_factory,
	automark,one_id,user_journey1,user_journey2,user_journey3,usage_scenario_first,usage_scenario_second,d2c_responsible_dept,d2c_accountable_dept,d2c_cc_dept,
	cust_global_id,cust_classify,cust_main_phone,is_car_owner,cust_age,cust_age_group,cust_name,cust_gender,cust_high_educaion,marrige_statue,family_income,
	is_exchange_flg,purchase_car_times,is_member_flg,cust_province_code,cust_province,cust_city_code,cust_city,cust_type,cust_lived_prov,cust_lived_city,
	cust_profession,vhl_vin,vhl_color_name,vhl_product_date,vhl_offline_date,vhl_is_abroad,vhl_dis_ch,vhl_dis_mt,vhl_eng_clsf,vhl_eng_seris,vhl_veh_type,
	vhl_country,vhl_bd_clsf,vhl_seg_mt,vhl_pow_clsf,vhl_fu_clsf,vhl_modl_st,vhl_std_plnt_code,dlr_oc_id,dlr_oc_code,dlr_oc_name,dlr_oc_province_code,
	dlr_oc_province,dlr_oc_city_code,dlr_oc_city,dlr_dc_id,dlr_dc_code,dlr_dc_name,dlr_dc_province_code,dlr_dc_province,dlr_dc_city_code,dlr_dc_city,
	dlr_mc_id,dlr_mc_code,dlr_mc_name,dlr_mc_province_code,dlr_mc_province,dlr_mc_city_code,dlr_mc_city,is_wsater_army,is_manager_focused,is_big_v,author_id,
	author_nick,is_main_post,original_link,view_count,comment_count,like_count,share_count,favorite_count,work_order_id,quest_id,quest_type,quest_answer_score,
	quest_business_type,quest_business_scenario,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,
	tag_event_clarity,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_complaint_flag_needing_prtv_msg,tag_high_quality_voc_flag,tag_new_energy_or_fuel,
	tag_need_forvclosed_loop,tag_sort,topic,topic_text,opinion,cpt_tag_first_code,cpt_tag_second_code,cpt_tag_three_code,cpt_tag_four_code,cpt_tag_first,
	cpt_tag_second,cpt_tag_three,cpt_tag_four,ujy_tag_first_code,ujy_tag_second_code,ujy_tag_three_code,ujy_tag_four_code,ujy_tag_first,ujy_tag_second,ujy_tag_three,
	ujy_tag_four,cma_tag_first_code,cma_tag_second_code,cma_tag_three_code,cma_tag_four_code,cma_tag_first,cma_tag_second,cma_tag_three,cma_tag_four,dom_tag_first_code,
	dom_tag_second_code,dom_tag_three_code,dom_tag_four_code,dom_tag_first,dom_tag_second,dom_tag_three,dom_tag_four,nps_tag_first_code,nps_tag_second_code,
	nps_tag_three_code,nps_tag_four_code,nps_tag_first,nps_tag_second,nps_tag_three,nps_tag_four,vtr_tag_first_code,vtr_tag_second_code,vtr_tag_three_code,
	vtr_tag_four_code,vtr_tag_first,vtr_tag_second,vtr_tag_three,vtr_tag_four,abandon,source_data_id,high_quality,
	retweeted_url,retweeted_user_id,retweeted_user_name,retweeted_content,retweeted_title,retweeted_time,comment_user_name,comment_user_id,comment_url,one_id_risk,
	ad_type,attribute_tag_code,attribute_tag_name,emotional_level,
	insert_dt)
PROPERTIES
(
    "enclose"="\"",
    "escape"="\\",
     "desired_concurrent_number" = "7",
     "max_batch_interval" = "5",
     "max_batch_rows" = "200000",                 -- 行数
    "max_batch_size" = "104857600",   -- 100 MB
    "format" = "json",
    "jsonpaths"="[\"$.id\",\"$.publishTime\", \"$.dataId\",\"$.channelCatagory\", \"$.channelCode\",\"$.channelName\", \"$.brandCode\",\"$.brandName\", \"$.carSeriesCode\",
		\"$.carSeriesName\", \"$.modelName\",\"$.contentType\", \"$.title\",\"$.content\", \"$.sentiment\",\"$.intention\", \"$.dataCreateTime\",\"$.createTime\",
		\"$.updateTime\",\"$.isOuter\", \"$.hotWord\",\"$.keywords\", \"$.originalTextScene\",\"$.marketId\", \"$.competitiveType\",\"$.isCore\", \"$.seriesFactory\",
		\"$.automark\", \"$.oneId\",\"$.userJourney1\", \"$.userJourney2\",\"$.userJourney3\", \"$.usageScenarioFirst\",\"$.usageScenarioSecond\", \"$.d2cResponsibleDept\",
		\"$.d2cAccountableDept\", \"$.d2cCcDept\",\"$.custGlobalId\", \"$.custClassify\",\"$.custMainPhone\", \"$.isCarOwner\",\"$.custAge\", \"$.custAgeGroup\",\"$.custName\",
		\"$.custGender\",\"$.custHighEducaion\", \"$.marrigeStatue\",\"$.familyIncome\", \"$.isExchangeFlg\",\"$.purchaseCarTimes\", \"$.isMemberFlg\",\"$.custProvinceCode\",
		\"$.custProvince\",\"$.custCityCode\", \"$.custCity\",\"$.custType\", \"$.custLivedProv\",\"$.custLivedCity\", \"$.custProfession\",\"$.vhlVin\", \"$.vhlColorName\",
		\"$.vhlProductDate\", \"$.vhlOfflineDate\",\"$.vhlIsAbroad\", \"$.vhlDisCh\",\"$.vhlDisMt\", \"$.vhlEngClsf\",\"$.vhlEngSeris\", \"$.vhlVehType\",\"$.vhlCountry\",
		\"$.vhlBdClsf\",\"$.vhlSegMt\", \"$.vhlPowClsf\",\"$.vhlFuClsf\", \"$.vhlModlSt\",\"$.vhlStdPlntCode\", \"$.dlrOcId\",\"$.dlrOcCode\", \"$.dlrOcName\",
		\"$.dlrOcProvinceCode\", \"$.dlrOcProvince\",\"$.dlrOcCityCode\", \"$.dlrOcCity\",\"$.dlrDcId\", \"$.dlrDcCode\",\"$.dlrDcName\", \"$.dlrDcProvinceCode\",
		\"$.dlrDcProvince\", \"$.dlrDcCityCode\",\"$.dlrDcCity\", \"$.dlrMcId\",\"$.dlrMcCode\", \"$.dlrMcName\",\"$.dlrMcProvinceCode\", \"$.dlrMcProvince\",
		\"$.dlrMcCityCode\", \"$.dlrMcCity\",\"$.isWsaterArmy\", \"$.isManagerFocused\",\"$.isBigV\", \"$.authorId\",\"$.authorNick\", \"$.isMainPost\",\"$.originalLink\",
		\"$.viewCount\",\"$.commentCount\", \"$.likeCount\",\"$.shareCount\", \"$.favoriteCount\",\"$.workOrderId\", \"$.questId\",\"$.questType\", \"$.questAnswerScore\",
		\"$.questBusinessType\", \"$.questBusinessScenario\",\"$.tagAccuracy\", \"$.tagCustomerIssueClassification\",\"$.tagIssueSeverity\", \"$.tagCodeStatus\",
		\"$.tagBusinessDomain\", \"$.tagEventClarity\",\"$.tagHighValueFlag\", \"$.tagComplaintFlagNeedingReply\",\"$.tagComplaintFlagNeedingPrtvMsg\",
		\"$.tagHighQualityVocFlag\",\"$.tagNewEnergyOrFuel\", \"$.tagNeedForvclosedLoop\",\"$.tagSort\", \"$.topic\",\"$.topicText\", \"$.opinion\",\"$.cptTagFirstCode\",
		\"$.cptTagSecondCode\",\"$.cptTagThreeCode\", \"$.cptTagFourCode\",\"$.cptTagFirst\", \"$.cptTagSecond\",\"$.cptTagThree\", \"$.cptTagFour\",\"$.ujyTagFirstCode\",
		\"$.ujyTagSecondCode\",\"$.ujyTagThreeCode\", \"$.ujyTagFourCode\",\"$.ujyTagFirst\", \"$.ujyTagSecond\",\"$.ujyTagThree\", \"$.ujyTagFour\",\"$.cmaTagFirstCode\",
		\"$.cmaTagSecondCode\",\"$.cmaTagThreeCode\", \"$.cmaTagFourCode\",\"$.cmaTagFirst\", \"$.cmaTagSecond\",\"$.cmaTagThree\", \"$.cmaTagFour\",\"$.domTagFirstCode\",
		\"$.domTagSecondCode\",\"$.domTagThreeCode\", \"$.domTagFourCode\",\"$.domTagFirst\", \"$.domTagSecond\",\"$.domTagThree\", \"$.domTagFour\",\"$.npsTagFirstCode\",
		\"$.npsTagSecondCode\",\"$.npsTagThreeCode\", \"$.npsTagFourCode\",\"$.npsTagFirst\", \"$.npsTagSecond\",\"$.npsTagThree\", \"$.npsTagFour\",\"$.vtrTagFirstCode\",
		\"$.vtrTagSecondCode\",\"$.vtrTagThreeCode\", \"$.vtrTagFourCode\",\"$.vtrTagFirst\", \"$.vtrTagSecond\",\"$.vtrTagThree\", \"$.vtrTagFour\",\"$.abandon\",
		\"$.sourceDataId\",\"$.highQuality\",
		\"$.retweetedUrl\",\"$.retweetedUserId\",\"$.retweetedUserName\",\"$.retweetedContent\",\"$.retweetedTitle\",\"$.retweetedTime\",
		\"$.commentUserName\",\"$.commentUserId\",\"$.commentUrl\",\"$.oneIdRisk\",
		\"$.adType\",\"$.attributeTagCode\",\"$.attributeTagName\",\"$.emotionalLevel\",
		\"$.insertDt\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "10.63.8.125:29094,10.63.8.125:29095,10.63.8.125:29096",
    "kafka_partitions" = "0,1,2,3,4,5,6",
    "kafka_topic" = "voc_anal_flow_model_tags_result_data_ext",
    "property.group.id" = "voc-analysis-sdb-prod",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);
