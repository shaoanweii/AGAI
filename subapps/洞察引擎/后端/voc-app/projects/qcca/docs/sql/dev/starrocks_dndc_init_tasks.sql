

-- SHOW ROUTINE LOAD FOR example_tbl2_ordertest9
-- SHOW ROUTINE LOAD TASK WHERE JobName = "example_tbl2_ordertest9"
-- PAUSE  ROUTINE LOAD FOR  ays_meta_data_analysis_kafka_test1
--   RESUME  ROUTINE LOAD FOR  ays_meta_data_analysis_kafka_20240712_c_1
-- STOP ROUTINE LOAD FOR ays_meta_data_analysis_kafka_20240712_c_1


-- ays_meta_data_analysis
CREATE ROUTINE LOAD dwd_voc2_all_meta_data_kafka ON dwd_voc2_all_meta_data
COLUMNS(id, data_id, one_id, work_id, client_id, channel_id, content_type, title, content, user_name, `data`, done
                    , data_status,model_type,ext_fields, biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,publish_time, create_time)
PROPERTIES
(
	"trim_space"="true",
    "enclose"="\"",
    "escape"="\\",
    "desired_concurrent_number" = "5",
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.oneId\",\"$.workId\",\"$.clientId\",\"$.channelId\",\"$.contentType\",\"$.title\",\"$.content\"
        ,\"$.userName\",\"$.data\",\"$.done\",\"$.dataStatus\",\"$.modelType\",\"$.extFields\",\"$.bizExtAttrs\",\"$.bizExtAttrs2\",\"$.bizExtAttrs3\"
        ,\"$.publishTime\",\"$.createTime\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "172.16.80.16:30092",
    -- "kafka_topic" = "VDP_metaDataAnalysisData_764547797eb2e192763f5334028d49c9",
    "kafka_topic" = "VDP_dwd_voc2_all_meta_data",
    "property.group.id" = "VDP-voc2-analysis",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);



-- ays_pre_process_data
CREATE ROUTINE LOAD dwd_voc2_pre_rules_result_data_kafka ON dwd_voc2_pre_rules_result_data
COLUMNS(id, data_id, one_id, work_id, client_id, channel_id, content_type, `data`, data_md5,
	publish_time, create_time, abandon, done, hit_rules,model_type
    ,ext_fields, biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3)
PROPERTIES
(
	"trim_space"="true",
    "enclose"="\"",
    "escape"="\\",
    "desired_concurrent_number" = "5",
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.oneId\",\"$.workId\",\"$.clientId\",\"$.channelId\",\"$.contentType\"
		,\"$.data\",\"$.dataMd5\",\"$.publishTime\",\"$.createTime\",\"$.abandon\",\"$.done\",\"$.hitRules\",\"$.modelType\"
        ,\"$.extFields\",\"$.bizExtAttrs\",\"$.bizExtAttrs2\",\"$.bizExtAttrs3\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "172.16.80.16:30092",
    -- "kafka_topic" = "VDP_processPreRulesData_764547797eb2e192763f5334028d49c9",
    "kafka_topic" = "VDP_voc2_pre_rules_result_data",
    "property.group.id" = "VDP-voc2-analysis",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);








-- ays_api_reslt_data_analysis
CREATE ROUTINE LOAD dwd_voc2_model_tags_result_data_kafka ON dwd_voc2_model_tags_result_data
COLUMNS(id,data_id,work_id,client_id,channel_id,original_id,content_type,input_data_id,
sample_data_type,original_text_scene,brand_code,car_series_code,label_type,label_type_level_first,
label_type_level_second,label_type_level_three,label_type_level_four,label_type_level_five,scenario,sentiment,
intention_type,topic,opinion,subject,fault_level,description,sentiment_score,keywords,publish_time,create_time,update_time,
hit_valid_rules,hit_rules,done,model_type,ext_fields,biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,one_id
	)
PROPERTIES
(
	"trim_space"="true",
    "enclose"="\"",
    "escape"="\\",
    "desired_concurrent_number" = "5",
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.workId\",\"$.clientId\",\"$.channelId\",\"$.originalId\",
\"$.contentType\",\"$.inputDataId\",\"$.sampleDataType\",\"$.originalTextScene\",\"$.brandCode\",\"$.carSeriesCode\",
\"$.labelType\",\"$.labelTypeLevelFirst\",\"$.labelTypeLevelSecond\",\"$.labelTypeLevelThree\"
,\"$.labelTypeLevelFour\",\"$.labelTypeLevelFive\",\"$.scenario\",\"$.sentiment\",\"$.intentionType\"
,\"$.topic\",\"$.opinion\",\"$.subject\",\"$.faultLevel\",\"$.description\",\"$.sentimentScore\",\"$.keywords\"
,\"$.publishTime\",\"$.createTime\",\"$.updateTime\",\"$.hitValidRules\",\"$.hitRules\",\"$.done\",
\"$.modelType\",\"$.extFields\",\"$.bizExtAttrs\",\"$.bizExtAttrs2\",\"$.bizExtAttrs3\",\"$.oneId\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "172.16.80.16:30092",
    -- "kafka_topic" = "VDP_modelResltAnalysistData_764547797eb2e192763f5334028d49c9",
    "kafka_topic" = "VDP_voc2_model_tags_result_data",
    "property.group.id" = "VDP-voc2-analysis",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);




-- ays_api_reslt_data_analysis_miss
CREATE ROUTINE LOAD dwd_voc2_model_tags_unlabeled_data_kafka ON dwd_voc2_model_tags_unlabeled_data
COLUMNS(id, data_id, work_id, one_id, client_id, channel_id, content_type, input_data_id, brand_code,
		car_series_code, opinion, opinion_sentiment, subject, description,
		car_body_label, view_label, create_time, update_time, done,model_type,ext_fields, biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,publish_time
	)
PROPERTIES
(
	"trim_space"="true",
    "enclose"="\"",
    "escape"="\\",
    "desired_concurrent_number" = "5",
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.workId\",\"$.oneId\",\"$.clientId\",\"$.channelId\",\"$.contentType\"
		,\"$.inputDataId\",\"$.brandCode\",\"$.carSeriesCode\",\"$.opinion\",\"$.opinionSentiment\",\"$.subject\"
		,\"$.description\",\"$.carBodyLabel\",\"$.viewLabel\"
		,\"$.createTime\",\"$.updateTime\",\"$.done\",\"$.modelType\",\"$.extFields\",\"$.bizExtAttrs\",\"$.bizExtAttrs2\",\"$.bizExtAttrs3\",\"$.publishTime\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "172.16.80.16:30092",
    -- "kafka_topic" = "VDP_processPostRulesDataMisss_764547797eb2e192763f5334028d49c9",
    "kafka_topic" = "VDP_voc2_model_tags_unlabeled_data",
    "property.group.id" = "VDP-voc2-analysis",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);






-- ays_post_process_data
CREATE ROUTINE LOAD dwd_voc2_post_rules_result_data_kafka ON dwd_voc2_post_rules_result_data
COLUMNS(id,data_id,work_id,client_id,channel_id,original_id,content_type,input_data_id,
sample_data_type,original_text_scene,brand_code,car_series_code,label_type,label_type_level_first,
label_type_level_second,label_type_level_three,label_type_level_four,label_type_level_five,scenario,sentiment,
intention_type,topic,opinion,subject,fault_level,description,sentiment_score,keywords,publish_time,create_time,update_time,
hit_valid_rules,hit_rules,done,model_type,ext_fields,biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,one_id
	)
PROPERTIES
(
	"trim_space"="true",
    "enclose"="\"",
    "escape"="\\",
    "desired_concurrent_number" = "5",
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.workId\",\"$.clientId\",\"$.channelId\",\"$.originalId\",
\"$.contentType\",\"$.inputDataId\",\"$.sampleDataType\",\"$.originalTextScene\",\"$.brandCode\",\"$.carSeriescode\",
\"$.labelType\",\"$.labelTypeLevelFirst\",\"$.labelTypeLevelSecond\",\"$.labelTypeLevelThree\"
,\"$.labelTypeLevelFour\",\"$.labelTypeLevelFive\",\"$.scenario\",\"$.sentiment\",\"$.intentionType\"
,\"$.topic\",\"$.opinion\",\"$.subject\",\"$.faultLevel\",\"$.description\",\"$.sentimentScore\",\"$.keywords\"
,\"$.publishTime\",\"$.createTime\",\"$.updateTime\",\"$.hitValidRules\",\"$.hitRules\",\"$.done\",
\"$.modelType\",\"$.extFields\",\"$.bizExtAttrs\",\"$.bizExtAttrs2\",\"$.bizExtAttrs3\",\"$.oneId\"]"
 )
FROM KAFKA
(
    "kafka_broker_list" = "172.16.80.16:30092",
    -- "kafka_topic" = "VDP_modelResltAnalysistData_764547797eb2e192763f5334028d49c9",
    "kafka_topic" = "VDP_voc2_post_rules_result_data",
    "property.group.id" = "VDP-voc2-analysis",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);






-- ays_error_push_data
CREATE ROUTINE LOAD dws_voc2_error_push_data_kafka ON dws_voc2_error_push_data
COLUMNS(id, `table`, `action`, work_id, client_id, `data`, create_time, tid)
PROPERTIES
(
	"trim_space"="true",
    "enclose"="\"",
    "escape"="\\",
    "desired_concurrent_number" = "5",
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.table\",\"$.action\",\"$.workId\",\"$.clientId\"
		,\"$.data\",\"$.create_time\",\"$.tid\"]"
 )
 FROM KAFKA
 (
     "kafka_broker_list" = "172.16.80.16:30092",
     -- "kafka_topic" = "VDP_errorPushData_764547797eb2e192763f5334028d49c9",
     "kafka_topic" = "VDP_voc2_error_push_data",
     "property.group.id" = "VDP-voc2-analysis",
     "property.kafka_default_offsets" = "OFFSET_BEGINNING"
 );




CREATE ROUTINE LOAD dws_voc2_batch_push_record_kafka ON dws_voc2_batch_push_record
COLUMNS(id, reqeut_id, work_id, `status`, `source`, create_time, update_time,model_type,ext_fields, tid)
PROPERTIES
(
	"trim_space"="true",
    "enclose"="\"",
    "escape"="\\",
    "desired_concurrent_number" = "5",
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.reqeutId\",\"$.workId\",\"$.status\"
		,\"$.source\",\"$.createTime\",\"$.updateTime\",\"$.modelType\",\"$.extFields\",\"$.tid\"]"
 )
 FROM KAFKA
 (
     "kafka_broker_list" = "172.16.80.16:30092",
     -- "kafka_topic" = "VDP_aysBatchPushRecord_764547797eb2e192763f5334028d49c9",
     "kafka_topic" = "VDP_voc2_batch_push_record",
     "property.group.id" = "VDP-voc2-analysis",
     "property.kafka_default_offsets" = "OFFSET_BEGINNING"
 );



-- ods_channel_execution_result
CREATE ROUTINE LOAD dwd_voc2_processing_data_record_kafka ON dwd_voc2_processing_data_record
COLUMNS(id, data_id, work_id, channel_type, retry_count, error_code, error_msg, `data`, create_time, last_exec_time, status, tid)
PROPERTIES
(
	"trim_space"="true",
    "enclose"="\"",
    "escape"="\\",
    "desired_concurrent_number" = "5",
    "format" = "json",
    "jsonpaths" = "[\"$.id\",\"$.dataId\",\"$.workId\",\"$.channelType\",\"$.retryCount\",\"$.errorCode\",\"$.errorMsg\",
            \"$.data\",\"$.createTime\",\"$.lastExecTime\",\"$.status\",\"$.tid\"]"
 )
 FROM KAFKA
 (
     "kafka_broker_list" = "172.16.80.16:30092",
     -- "kafka_topic" = "VDP_channelExecutionResult_764547797eb2e192763f5334028d49c9",
     "kafka_topic" = "VDP_voc2_processing_data_record",
     "property.group.id" = "VDP-voc2-analysis",
     "property.kafka_default_offsets" = "OFFSET_BEGINNING"
 );























--
-- -- draop task ins_brand_info_cache
-- submit task ins_brand_info_cache
-- schedule every(interval 10 minute)
-- as
-- cache
-- SELECT * from voc_jdbc.vdp_ms_be.ins_brand_info
--
-- select * from default_catalog.information_schema.tasks;


