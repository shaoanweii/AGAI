-- =====================================================================
-- voc_report_user_menu_visit_record 索引优化
-- 说明：当前 DUPLICATE KEY(id) 对业务查询无益，通过添加倒排/bitmap索引
--       提升 user_id / user_code / menu_name / app_type 的过滤性能
-- =====================================================================

-- 1. menu_name 倒排索引（IS NOT NULL + IN 高频过滤）
ALTER TABLE voc_report_user_menu_visit_record
    ADD INDEX idx_inv_menu_name (menu_name) USING INVERTED;

-- 2. app_type bitmap 索引（低基数列：app / pc，效果显著）
ALTER TABLE voc_report_user_menu_visit_record
    ADD INDEX idx_bitmap_app_type (app_type) USING BITMAP;

-- 3. user_id 倒排索引（IN 查询，配合已有 bloom_filter 双重加速）
ALTER TABLE voc_report_user_menu_visit_record
    ADD INDEX idx_inv_user_id (user_id) USING INVERTED;

-- 4. user_code 倒排索引（IN 查询）
ALTER TABLE voc_report_user_menu_visit_record
    ADD INDEX idx_inv_user_code (user_code) USING INVERTED;

-- 5. create_time 倒排索引（范围查询兜底，分区裁剪失效时生效）
ALTER TABLE voc_report_user_menu_visit_record
    ADD INDEX idx_inv_create_time (create_time) USING INVERTED;



ALTER TABLE report_user_browse_record RENAME report_user_browse_record_old;
ALTER TABLE voc_report_user_system_access_duration RENAME voc_report_user_system_access_duration_old;


REFRESH MATERIALIZED VIEW voc_report_user_browse_record_mv COMPLETE;
show create MATERIALIZED VIEW voc_ins_sys_login_histroy_mv;

CREATE MATERIALIZED VIEW voc_report_user_browse_record_mv
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 5 MINUTE STARTS "2026-02-25 18:33:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 2
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "id",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"compression" = "ZSTD",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS select
*
from voc_mysql_jdbc.voc_ms_td.sta_report_user_browse_record


CREATE MATERIALIZED VIEW voc_report_user_system_access_duration_mv
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 5 MINUTE STARTS "2026-02-25 09:55:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 2
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "id",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"compression" = "ZSTD",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_nondeterministic_function" = "true"
)
AS select
              *
   from voc_mysql_jdbc.voc_ms_td.sta_report_user_system_access_duration




-- 添加是否置顶字段
ALTER TABLE report_custom_report ADD COLUMN `pin_to_top` tinyint(4) DEFAULT '0' COMMENT '是否置顶:1为置顶';


-- 添加置顶时间字段
ALTER TABLE report_custom_report ADD COLUMN `pin_to_top_time` datetime  COMMENT '置顶时间';


SHOW ROUTINE LOAD;

SHOW CREATE ROUTINE LOAD FOR voc_ins_api_reqeust_record_kafka;

stop ROUTINE LOAD FOR voc_ins_api_reqeust_record_kafka_new;

CREATE ROUTINE LOAD voc_ins_api_reqeust_record_kafka ON voc_ins_api_reqeust_record
WITH APPEND
COLUMNS(id,create_time,log_type,log_content,operate_type,userid,username,ip,method,request_url,request_param,request_type,cost_time,create_by,update_by,update_time,app_id,code,message,tid,insert_dt,access_source_type)
PROPERTIES
(
"desired_concurrent_number" = "3",
"max_error_number" = "0",
"max_filter_ratio" = "1.0",
"max_batch_interval" = "3",
"max_batch_rows" = "20000000",
"max_batch_size" = "1073741824",
"format" = "json",
-- 关键修复：1. 外层用双引号，内层用单引号；2. 合并为单行避免换行解析问题
"jsonpaths" = '["$.id","$.create_time","$.log_type","$.log_content","$.operate_type","$.userid","$.username","$.ip","$.method","$.request_url","$.request_param","$.request_type","$.cost_time","$.create_by","$.update_by","$.update_time","$.app_id","$.code","$.message","$.tid","$.insert_dt","$.access_source_type"]',
"strip_outer_array" = "false",
"num_as_string" = "false",
"fuzzy_parse" = "false",
"strict_mode" = "false",
"timezone" = "Asia/Shanghai",
"exec_mem_limit" = "2147483648"
)
FROM KAFKA
(
"kafka_broker_list" = "172.16.80.16:30092,172.16.80.16:31673,172.16.80.16:32705",
"kafka_topic" = "voc_ins_api_reqeust_record",
"property.kafka_default_offsets" = "OFFSET_BEGINNING",
"property.group.id" = "voc-analysis-sdb-prod",
"kafka_partitions" = "0, 1, 2",
"kafka_offsets" = "8820, 8746, 8506"
);



alter table voc_ins_api_reqeust_record
    add COLUMN  access_source_type varchar(1000) null comment '访问来源类型 （如：PC、APP）';





CREATE TABLE `voc_report_user_system_access_duration` (
                                                          `id` varchar(64) NOT NULL COMMENT "主键（建议用UUID生成）",
                                                          `user_id` varchar(64) NULL COMMENT "用户ID（关联用户表主键）",
                                                          `session_id` varchar(64) NULL COMMENT "前端会话唯一标识（防重复上报）",
                                                          `access_start_time` datetime NULL COMMENT "访问开始时间（格式：yyyy-MM-dd HH:mm:ss）",
                                                          `access_end_time` datetime NULL COMMENT "访问结束时间（前端上报/后端兜底判定）",
                                                          `actual_duration` int(11) NULL DEFAULT 0 COMMENT "实际访问时长（秒），后端计算",
                                                          `heartbeat_last_time` datetime NULL COMMENT "最后心跳时间（兜底用）",
                                                          `device` varchar(50) NULL COMMENT "访问设备（pc/android/ios/h5）",
                                                          `browser` varchar(50) NULL COMMENT "浏览器类型（Chrome/Firefox/微信内置浏览器）",
                                                          `ip` varchar(50) NULL COMMENT "访问IP地址",
                                                          `status` tinyint(4) NULL DEFAULT 1 COMMENT "会话状态：1-未结束 2-已结束 3-异常兜底",
                                                          `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "记录创建时间",
                                                          `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT "记录更新时间"
) ENGINE=OLAP
    UNIQUE KEY(`id`)  -- 基于主键id保证唯一性，避免重复插入
COMMENT '用户系统访问时长表'
DISTRIBUTED BY HASH(`id`) BUCKETS 1  -- 按id哈希分桶，单桶适配中小数据量
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V2",
"enable_unique_key_merge_on_write" = "true",  -- 开启唯一键合并，支持UPSERT
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);

DROP  table voc_report_user_menu_visit_record

CREATE TABLE `voc_report_user_menu_visit_record` (
                                                     `id` varchar(164) NOT NULL COMMENT "主键",
                                                     `user_id` varchar(164) NULL COMMENT "用户id",
                                                     `user_code` varchar(164) NULL COMMENT "工号",
                                                     `user_name` varchar(164) NULL COMMENT "用户姓名",
                                                     `visit_url` varchar(1064) NULL COMMENT "访问url",
                                                     `menu_id` varchar(164) NULL COMMENT "菜单id",
                                                     `visit_date` varchar(164) NULL COMMENT "开始日期",
                                                     `visit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "开始访问时间",
                                                     `end_time` datetime NULL COMMENT "结束访问时间",
                                                     `ip_addr` varchar(164) NULL COMMENT "IP地址",

                                                     `front_routing` varchar(1064) NULL COMMENT "前端访问路由",
                                                     `session_id` varchar(300) NULL COMMENT "会话id",
                                                     `app_type` varchar(164) NULL COMMENT "APP类型(app,pc)",
                                                     `model_code` varchar(164) NULL COMMENT "模块编码(voc-app-report)",
                                                     `menu_name` varchar(164) NULL COMMENT "菜单名称",
                                                     `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间"
) ENGINE=OLAP
    UNIQUE KEY(`id`)  -- 细化唯一键，避免重复
COMMENT '用户菜单访问记录表'
DISTRIBUTED BY HASH(`id`) BUCKETS 1
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V2",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);


-- voc_ms_td.report_user_menu_visit_record （用户菜单访问记录表）
CREATE TABLE `report_user_menu_visit_record` (
                                                 `user_code` varchar(64) NULL COMMENT "用户账号",
                                                 `user_name` varchar(64) NULL COMMENT "姓名",
                                                 `org_name` varchar(64) NULL COMMENT "部门",
                                                 `app_type` varchar(64) NULL COMMENT "APP类型（如移动端/PC端/小程序等）",  -- 补充类型说明
                                                 `model_code` varchar(64) NULL COMMENT "模块编码",
                                                 `model_name` varchar(64) NULL COMMENT "模块名称",
                                                 `c_menu_name` varchar(64) NULL COMMENT "联合菜单",
                                                 `menu_id` varchar(64) NULL COMMENT "菜单ID",
                                                 `menu_name` varchar(64) NULL COMMENT "菜单名称",
                                                 `ip_address` varchar(64) NULL COMMENT "IP地址",
                                                 `visit_date` varchar(64) NULL COMMENT "开始访问日期",
                                                 `visit_time` varchar(64) NULL COMMENT "开始访问时间",
                                                 `end_time` varchar(64) NULL COMMENT "结束访问时间",
                                                 `ca_holiday_flag` varchar(64) NULL COMMENT "节假日标识（Y=是，N=否）",
                                                 `shift_flag` bigint NULL COMMENT "工作时间标识（1=8-18点，0=非工作时间）",
                                                 `source_code` varchar(64) NULL COMMENT "系统来源编码",
                                                 `w_insert_time` varchar(64) NULL COMMENT "数据插入时间",
                                                 `part_dt` varchar(64) NULL COMMENT "分区日期"
) ENGINE=OLAP
    UNIQUE KEY(`user_code`, `menu_id`, `visit_date`, `visit_time`)
COMMENT '用户菜单访问记录表'
DISTRIBUTED BY HASH(`user_code`) BUCKETS 1
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V2",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);







SELECT
    TABLE_NAME,
    CONCAT('REFRESH MATERIALIZED VIEW ', TABLE_NAME, ' COMPLETE;') AS refresh_command,
    CONCAT('show create MATERIALIZED VIEW ', TABLE_NAME, ' ;') AS show_create
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'voc_ms_td'
  and ENGINE is null



delete from report_custom_report;
delete from report_view_log;
delete from report_special_analysis_role;
delete from report_special_analysis_type;
INSERT INTO voc_ms_td.ins_dict (id, dict_name, dict_code, description, del_flag, operator, create_time, update_time, type) VALUES ('2001831620481585154', '发布报告状态', 'report_release_staus', null, 0, 'cqca_chengsheng', '2025-12-19 09:47:05', '2025-12-19 09:47:05', 0);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2001832051215634434', '2001831620481585154', '待审核', '0', null, 1, 1, 'cqca_chengsheng', '2025-12-19 09:48:48', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2001832106047770625', '2001831620481585154', '已发布', '1', null, 1, 1, 'cqca_chengsheng', '2025-12-19 09:49:01', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2001832143821672450', '2001831620481585154', '已下架', '2', null, 1, 1, 'cqca_chengsheng', '2025-12-19 09:49:10', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2001832186230280193', '2001831620481585154', '未通过', '3', null, 1, 1, 'cqca_chengsheng', '2025-12-19 09:49:20', null, null);

-- 更新 status 字段的注释
ALTER TABLE report_custom_report MODIFY COLUMN `status` tinyint(4) DEFAULT '0' COMMENT '状态：0-待审核、1-已发布、2-已下架、3-未通过';

-- 添加审核人字段
ALTER TABLE report_custom_report ADD COLUMN `audit_by` VARCHAR(100) null COMMENT '审核人';

-- 添加审核时间字段
ALTER TABLE report_custom_report ADD COLUMN `audit_time` DATETIME COMMENT '审核时间';



alter table report_user_browse_record
    add COLUMN  original_id varchar(50000) null comment '原文id';






delete from voc_anal_flow_mate_data_full
       where data_id not in (select data_id from voc_sentiment_annotations_results_v);




DELETE f FROM voc_anal_flow_mate_data_full f
                  LEFT JOIN voc_sentiment_annotations_results_v v ON f.data_id = v.data_id
WHERE v.data_id IS NULL
    LIMIT 1000;

DELETE FROM voc_anal_flow_mate_data_full
WHERE 1=1
  AND data_id NOT IN (SELECT data_id FROM voc_sentiment_annotations_results_v);


DELETE FROM voc_anal_flow_mate_data_full
WHERE data_id NOT IN (SELECT data_id FROM voc_sentiment_annotations_results_v)
LIMIT 999999999;

DELETE FROM voc_anal_flow_mate_data_full
WHERE id > 0
  AND data_id NOT IN (SELECT data_id FROM voc_sentiment_annotations_results_v);



DELETE FROM voc_anal_flow_mate_data_full where id='0003d3a29a7dca34b0d83b931360b5de';


INSERT INTO voc_ms_td.sta_sys_filter_type (id, attr, name, filter_type, options_json, value_type, sort, create_time, page_display_type) VALUES ('40', 'topic_text', '观点', '2', '{"name": "观点", "term": "like", "field": "topic_text", "valueType": "string", "filterType": "2"}', null, null, null, 'leaderOverview,competitorAnalysis,H5Home,groupAnalysis,thisProductAnalysis,journeyAnalysis,productAnalysis,serviceAnalysis');



select
    count(1)
from voc_sentiment_annotations_results_v
WHERE (date(data_create_time) BETWEEN '2024-10-13' AND '2025-10-13')
      AND sentiment IS NOT NULL
					AND sentiment <> ''
					AND competitive_type IS NOT NULL
					AND competitive_type <> ''
--					AND competitive_type = 0
-- 					AND topic NOT IN ('cqca1001013001001060', 'cqca1002002014004004', 'cqca1002004001003005', 'cqca1003001001001', 'cqca1001001007002007', 'cqca1118004003043', 'cqca1001005003119002', 'cqca1001005001011001', 'cqca1002003001001001', 'cqca1001009002003007', 'cqca1002002014010003', 'cqca1001010001009001', 'cqca1118003007325', 'cqca1009002002', 'cqca1118003001196', 'cqca1002002008001014', 'cqca1002004001015019', 'cqca1001013001001018', 'cqca1001005002104001', 'cqca1001010005001003', 'cqca1002002001001', 'cqca1002005005004011', 'cqca1002002015001021', 'cqca1001004003001002')/*渠道、车系、一级，二级，区域，数据类型，数据源 */
					AND brand_code IN ('A01', 'A03', 'A04', 'A05')
-- 					AND competitive_type = 0
--					AND channel_code IN ('pdt_opinion_cjd_hw_yjfk', 'pdt_quest_caylzp', 'pd_post_ks', 'pd_post_xhs', 'pdt_order_awtrxfw', 'pd_post_tpyh', 'pdt_opinion_cayllyb', 'pdt_order_slqclrxfw', 'pdt_opinion_yly_yjfk', 'pdt_order_caqysbgd', 'pd_post_zg315w', 'pdt_opinion_cjd_yjfk_sda', 'pd_post_tpyqc_lt', 'pdt_order_caylrxfw', 'pdt_opinion_cakclyb', 'pdt_opinion_awtlyb', 'pdt_quest_chmzp', 'pdt_opinion_cakcaxcx_yjfk', 'pd_post_bjh', 'pdt_opinion_cjd_yjfk_wt', 'pdt_order_caylsbgd', 'pd_post_qczj_cjh', 'pd_post_yc_sq', 'pdt_opinion_slqcaxcx_yjfk', 'pd_post_yc_sp', 'pd_post_czzj', 'pd_post_qctsw', 'pdt_order_slqcsbgd', 'pd_post_tpyqc_wz', 'pdt_order_cakcrxfw', 'pdt_quest_jtzhyxzp', 'pd_post_bilibili', 'pd_post_zgqcw', 'pdt_opinion_caqylyb', 'pdt_order_caqcrxfw', 'pdt_quest_awtzp', 'pd_post_bdtb', 'pdt_opinion_catxcx_yjfk', 'pdt_post_awtaxcx_k_zx', 'pd_post_jrtt', 'pd_post_dy', 'pdt_quest_slqczp', 'pd_post_wyh', 'pd_post_yc_wz', 'pd_post_qcmw_ts', 'pd_post_qcmw_xw', 'pdt_quest_caqyzp', 'pd_post_akqc', 'pd_post_qczj_lt', 'pd_post_akqh', 'pd_post_qczhw', 'pd_post_wb', 'pd_post_czw_lt', 'pd_post_qczj_wz', 'pdt_order_cakcsbgd', 'pd_post_czw_ts', 'pd_post_czw_xw', 'pd_post_dcd_pl', 'pd_post_zgqczlw_ts', 'pd_post_zgqczlw_xw', 'pd_post_qczj_sp', 'pdt_quest_kfgszp', 'pdt_post_awtaxcx_k_sq', 'pdt_opinion_slqclyb', 'pd_post_hmts_ts', 'pdt_order_awtdmsgd', 'pdt_opinion_caqyaxcx_lxwm', 'pd_post_zh', 'pdt_post_slqcaxcx_ts_sq', 'pdt_order_caqyrxfw', 'pdt_quest_kcqczp', 'pub_post_wx_spx')




-- ============================================================================
-- ROUTINE LOAD: 从 Kafka 持续导入数据到 SelectDB/Doris 表
-- 任务名: voc_anal_flow_model_tags_result_data_kafka
-- 目标表: voc_anal_flow_model_tags_result_data_full
-- ============================================================================

CREATE ROUTINE LOAD voc_anal_flow_model_tags_result_data_kafka  -- 【必需】创建例行导入任务，任务名需全局唯一
    ON voc_anal_flow_model_tags_result_data_full  -- 【必需】指定目标表名
WITH APPEND  -- 【必需】追加模式：数据追加到表中（与MERGE模式相对）
COLUMNS(  -- 【必需】定义列映射：指定从Kafka消息映射到表的列顺序
    id,data_id,work_id,client_id,channel_id,original_id,content_type,input_data_id,sample_data_type,original_text_scene,
    brand_code,car_series_code,label_type,scenario,sentiment,intention_type,topic,opinion,subject,fault_level,
    description,sentiment_score,keywords,publish_time,create_time,update_time,done,model_type,raw_data,ext_fields,
    biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,cust_ext_attrs,vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs,tags_ext_attrs,one_id
)
PROPERTIES
(
    -- ========== 并发与性能参数 ==========
    "desired_concurrent_number" = "3",  -- 【核心】并发消费任务数：3个并发线程消费Kafka
                                         -- 调优：建议设置为 Kafka分区数 或 BE节点数，过大会增加调度开销
                                         -- 性能：3分区对应3并发最优，可提升至6-10（需监控CPU）
    
    -- ========== 错误容忍参数 ==========
    "max_error_number" = "0",  -- 【严格】最大错误行数：0表示不容忍任何错误行
                                -- 调优：生产环境建议设置为100-1000，避免因少量脏数据导致任务失败
                                -- 风险：当前配置下任何格式错误都会导致任务暂停
    
    "max_filter_ratio" = "1.0",  -- 【宽松】最大过滤比例：1.0表示允许100%数据被过滤
                                   -- 调优：建议设置为0.1-0.3（10%-30%），防止大量数据丢失不被察觉
                                   -- 配合：与max_error_number配合使用，两者任一触发则任务失败
    
    -- ========== 批次控制参数（核心性能参数）==========
    "max_batch_interval" = "5",  -- 【高频】最大批次间隔：5秒提交一次批次
                                   -- 调优：实时性要求高设置5-10秒，吞吐量优先设置30-60秒
                                   -- 影响：间隔越短实时性越好但事务开销越大，建议10-30秒平衡
    
    "max_batch_rows" = "20000000",  -- 【大批次】最大批次行数：2000万行触发提交
                                     -- 调优：根据单行大小调整，小行(1KB)可设置100万，大行(10KB)建议10-50万
                                     -- 性能：当前值过大，建议降至50万-200万行，减少单批次内存压力
    
    "max_batch_size" = "1073741824",  -- 【1GB】最大批次大小：1GB数据触发提交
                                        -- 调优：建议设置为100MB-500MB，避免OOM和长事务
                                        -- 计算：单行约1KB时，配合max_batch_rows=500000，批次约500MB
    
    -- ========== JSON解析参数 ==========
    "format" = "json",  -- 【必需】数据格式：JSON格式
    
    "jsonpaths" = "[  -- 【必需】JSON字段映射：定义JSON字段到表列的映射路径
        \"$.id\",\"$.dataId\",\"$.workId\",\"$.clientId\",\"$.channelId\",\"$.originalId\",
        \"$.contentType\",\"$.inputDataId\",\"$.sampleDataType\",\"$.originalTextScene\",\"$.brandCode\",\"$.carSeriesCode\",
        \"$.labelType\",\"$.scenario\",\"$.sentiment\",\"$.intentionType\",
        \"$.topic\",\"$.opinion\",\"$.subject\",\"$.faultLevel\",\"$.description\",\"$.sentimentScore\",\"$.keywords\",
        \"$.publishTime\",\"$.createTime\",\"$.updateTime\",\"$.done\",\"$.modelType\",
        \"$.rawData\",\"$.extFields\",\"$.bizExtAttrs\",\"$.bizExtAttrs2\",\"$.bizExtAttrs3\",
        \"$.custExtAttrs\",\"$.vhlExtAttrs\",\"$.dealerExtAttrs\",\"$.prdExtAttrs\",\"$.tagsExtAttrs\",\"$.oneId\"
    ]",  -- 注意：字段顺序必须与COLUMNS定义一致
    
    "strip_outer_array" = "false",  -- 【单对象】是否剥离外层数组：false表示每条消息是单个JSON对象
                                     -- 场景：true用于消息体为JSON数组 [{},{}]，false用于单对象 {}
    
    "num_as_string" = "false",  -- 【类型转换】数字是否作为字符串：false表示保持数字类型
                                  -- 调优：大整数(>2^53)建议设置true，避免精度丢失
    
    "fuzzy_parse" = "false",  -- 【严格解析】是否模糊解析：false表示严格按jsonpaths解析
                                -- 调优：字段顺序不固定时设置true，但会降低性能10%-20%
    
    -- ========== 数据质量参数 ==========
    "strict_mode" = "false",  -- 【宽松模式】严格模式：false允许NULL值和类型自动转换
                                -- 调优：数据质量高时设置true，可提前发现数据问题
                                -- 影响：true时NULL值、溢出、类型不匹配都会被过滤
    
    -- ========== 系统参数 ==========
    "timezone" = "Asia/Shanghai",  -- 【时区】时区设置：影响时间类型字段的解析
    
    "exec_mem_limit" = "2147483648"  -- 【2GB】单个导入任务内存限制：2GB
                                       -- 调优：根据批次大小调整，建议为max_batch_size的2-3倍
                                       -- 计算：当前max_batch_size=1GB，建议设置为2-3GB
                                       -- 风险：内存不足会导致任务失败，需监控BE内存使用率
)
FROM KAFKA
(
    -- ========== Kafka连接参数 ==========
    "kafka_broker_list" = "10.62.133.17:29095",  -- 【必需】Kafka集群地址：多个broker用逗号分隔
                                                   -- 高可用：建议配置多个broker "host1:port1,host2:port2"
    
    "kafka_topic" = "voc_anal_flow_model_tags_result_data",  -- 【必需】Kafka主题名
    
    -- ========== 消费位置参数 ==========
    "property.kafka_default_offsets" = "OFFSET_BEGINNING",  -- 【初始位置】默认消费位置：从最早消息开始
                                                              -- 选项：OFFSET_BEGINNING(最早) / OFFSET_END(最新) / 时间戳
                                                              -- 注意：仅在首次消费或无checkpoint时生效
    
    "property.group.id" = "voc-analysis-sdb-test",  -- 【必需】消费者组ID：用于Kafka offset管理
                                                      -- 重要：修改group.id会重新消费数据
    
    -- ========== 分区与偏移量参数（可选）==========
    "kafka_partitions" = "0, 1, 2",  -- 【可选】指定消费的分区：仅消费0、1、2三个分区
                                       -- 场景：用于数据修复或部分分区重新消费
                                       -- 注意：不指定则消费所有分区
    
    "kafka_offsets" = "426740, 426375, 426719"  -- 【可选】指定每个分区的起始offset
                                                  -- 对应：分区0从426740开始，分区1从426375开始，分区2从426719开始
                                                  -- 场景：用于断点续传或数据回溯
                                                  -- 注意：与kafka_partitions一一对应，数量必须匹配
);

-- ============================================================================
-- 性能调优建议总结
-- ============================================================================
-- 1. 【高优先级】降低批次大小：max_batch_rows改为500000-1000000，max_batch_size改为524288000(500MB)
-- 2. 【高优先级】增加错误容忍：max_error_number改为1000，max_filter_ratio改为0.1
-- 3. 【中优先级】调整批次间隔：max_batch_interval改为10-30秒，平衡实时性和吞吐量
-- 4. 【中优先级】增加并发数：desired_concurrent_number可提升至6-10（需监控CPU和内存）
-- 5. 【低优先级】启用严格模式：数据质量稳定后设置strict_mode=true
-- 6. 【监控指标】关注：导入速度(rows/s)、错误率、内存使用、Kafka消费延迟
-- ============================================================================



# 清理不规范的表
drop table if exists  ins_brand_info_1124;
drop table if exists  ins_car_series_info_1124;
drop table if exists  voc_ins_user_journey_level1_v;
drop table if exists  province_dict_data_v;
drop table if exists  ins_channel;
drop table if exists  ins_brand_info_mv;
drop table if exists  brand_self_brand_v;
drop view if exists  brand_self_brand_v;
drop MATERIALIZED view if exists  ins_car_series_info;
drop MATERIALIZED view if exists  ins_brand_info;
drop table if exists ins_tag_client;
drop table if exists report_display_rule_old;
drop view if exists dws_voc2_sounds_data;
drop table if exists brand_self_brand_v;
drop table if exists  brand_all_v;
drop table if exists  brand_series_all_v;
drop table if exists  ins_car_series_info;





show CREATE MATERIALIZED VIEW voc_ext_ins_tag_client_mv;
show CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_labeled_mv;



-- 查询所有物化视图并生成刷新命令
SELECT
    TABLE_NAME,
    CONCAT('REFRESH MATERIALIZED VIEW ', TABLE_NAME, ' COMPLETE;') AS refresh_command
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'voc_ms_td'
and ENGINE is null

drop materialized view if exists voc_anal_flow_mate_data_labeled_mv;

     CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_labeled_mv
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 20 MINUTE STARTS "2025-12-01 12:30:00"
DUPLICATE KEY(`id`)
PARTITION BY (date_trunc(`data_create_time`, 'month'))
DISTRIBUTED BY HASH(`id`) BUCKETS 16
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
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


CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_labeled_mv
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 20 MINUTE STARTS "2025-12-10 11:30:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 16
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
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
AS SELECT
                                           `internal`.`voc_ms_td`.`t`.`id`,
                                           `internal`.`voc_ms_td`.`t`.`data_id`,
                                           -- 优化：JSON解析函数添加默认值，避免空值导致刷新失败
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.data_update_time"), '') as `data_update_time`,
                                           `internal`.`voc_ms_td`.`t`.`create_time`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.content_type"), '') as `content_type`,
                                           `internal`.`voc_ms_td`.`t`.`publish_time` AS `data_create_time`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.channel_code"), '') AS `channel_code`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.brand"), '') AS `brand`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.series"), '') AS `series`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.model"), '') AS `model`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.is_outer"), '') AS `is_outer`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.one_id"), '') AS `one_id`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.id_car_no"), '') AS  `id_car_no`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.mobile"), '') AS `mobile`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.email"), '') AS `email`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.global_id"), '') AS `global_id`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.user_id"), '') AS `user_id`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.user_name"), '') AS `user_name`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.vhl_id"), '') AS `vhl_id`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.vhl_vin"), '') AS `vhl_vin`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.dlr_id"), '') AS `dlr_id`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.dlr_code"), '') AS `dlr_code`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.dlr_type"), '') AS `dlr_type`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.market_id"), '') AS `market_id`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.title"), '') AS `title`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.content"), '') AS `content`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.is_wsater_army"), '') as `is_wsater_army`,
                                           '0' AS `weight`,
                                           `internal`.`voc_ms_td`.`t`.`biz_ext_attrs` AS `attrs`,
                                           `internal`.`voc_ms_td`.`t`.`biz_ext_attrs2` AS `attrs2`,
                                           `internal`.`voc_ms_td`.`t`.`biz_ext_attrs3` AS `attrs3`,
                                           `internal`.`voc_ms_td`.`t`.`work_id`,
                                           `internal`.`voc_ms_td`.`t`.`done`,
                                           COALESCE(JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`t`.`raw_data`, "$.model_type"), '') as`model_type`,
                                           NULL as `ds`,
                                           NOW() as `insert_dt`
   FROM `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full` `t`
-- 替代原自连接：按 data_id 分组取最大 id，消除冗余数据
   WHERE `internal`.`voc_ms_td`.`t`.`id` IN (
       SELECT MAX(id)
       FROM `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`
       GROUP BY data_id
   )




CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_labeled_mv
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 20 MINUTE STARTS "2025-12-01 12:30:00"
DUPLICATE KEY(`id`)
PARTITION BY (date_trunc(`data_create_time`, 'month'))
DISTRIBUTED BY HASH(`id`) BUCKETS 16
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
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
AS with `labeled_data` as (
	-- 已打标数据范围
	select data_id from `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`  group by `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`data_id`
)
SELECT
    `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`id`,
    `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data_id`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.data_update_time") as `data_update_time`,
    `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`create_time`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.content_type") as `content_type`,
    `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`publish_time` AS `data_create_time`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.channel_code") AS `channel_code`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.brand") AS `brand`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.series") AS `series`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.model") AS `model`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.is_outer") AS `is_outer`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.one_id") AS `one_id`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.id_car_no") AS  `id_car_no`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.mobile") AS `mobile`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.email") AS `email`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.global_id") AS `global_id`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.user_id") AS `user_id`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.user_name") AS `user_name`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.vhl_id") AS `vhl_id`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.vhl_vin") AS `vhl_vin`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.dlr_id") AS `dlr_id`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.dlr_code") AS `dlr_code`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.dlr_type") AS `dlr_type`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.market_id") AS `market_id`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.title") AS `title`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.content") AS `content`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.is_wsater_army") as `is_wsater_army`,
    '0' AS `weight`,
    `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`biz_ext_attrs` AS `attrs`,
    `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`biz_ext_attrs2` AS `attrs2`,
    `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`biz_ext_attrs3` AS `attrs3`,
    `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`work_id`,
    `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`done`,
    JSON_EXTRACT_STRING(`internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`.`data`, "$.model_type") as`model_type`,
    null as `ds`,
    now() as `insert_dt`
FROM `internal`.`voc_ms_td`.`voc_anal_flow_mate_data_full`
where data_id in (
    select `labeled_data`.`data_id` from labeled_data
)







DROP MATERIALIZED VIEW IF EXISTS voc_anal_flow_sentiment_annotations_results_mv;

CREATE MATERIALIZED VIEW voc_anal_flow_sentiment_annotations_results_mv
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 10 MINUTE STARTS "2025-11-26 23:30:00"
DUPLICATE KEY(`id`)
COMMENT '物化-topic/标签/品牌/车系/渠道关联表'
DISTRIBUTED BY HASH(`id`) BUCKETS 12
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",  -- 【必需】副本分配策略：默认位置1个副本
"min_load_replica_num" = "-1",  -- 【可选】最小加载副本数：-1表示使用默认值
"bloom_filter_columns" = "brand_code, car_series_code, topic, channel_code",  -- 【可选】布隆过滤器列：加速等值查询
"is_being_synced" = "false",  -- 【可选】是否正在同步：否
"storage_medium" = "hdd",  -- 【可选】存储介质：机械硬盘（默认hdd，可选ssd）
"storage_format" = "V2",  -- 【可选】存储格式：V2版本（默认V2）
"inverted_index_storage_format" = "V1",  -- 【可选】倒排索引存储格式：V1版本
"light_schema_change" = "true",  -- 【可选】轻量级schema变更：启用（支持快速加减列）
"disable_auto_compaction" = "false",  -- 【可选】禁用自动压缩：否（允许自动合并小文件）
"enable_single_replica_compaction" = "false",  -- 【可选】启用单副本压缩：否
"group_commit_interval_ms" = "10000",  -- 【可选】组提交间隔：10秒（批量提交优化）
"group_commit_data_bytes" = "134217728",  -- 【可选】组提交数据大小：128MB
"enable_nondeterministic_function" = "true"  -- 【可选】启用非确定性函数：是（允许使用now()等函数）
)
AS with `base_data` as (
    select
        `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`id`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`publish_time`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`data_id`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`one_id`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`work_id`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`client_id`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`channel_id`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`content_type`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`sample_data_type`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`original_id`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`input_data_id`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`original_text_scene`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`brand_code`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`car_series_code`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`label_type`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`sentiment`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`intention_type`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`topic`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`opinion`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`subject`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`fault_level`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`description`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`sentiment_score`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`keywords`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`model_type`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`raw_data`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`ext_fields`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`biz_ext_attrs`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`biz_ext_attrs2`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`biz_ext_attrs3`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`cust_ext_attrs`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`vhl_ext_attrs`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`dealer_ext_attrs`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`prd_ext_attrs`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`create_time`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`update_time`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`abandon`
         ,`internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`done`
    from `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`
     WHERE `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`brand_code` is not null
      and `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`channel_id` is not null
      and `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`topic` is not null
      and `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`content_type` is not null
      and `internal`.`voc_ms_td`.`voc_anal_flow_model_tags_result_data_full`.`topic` not in ("cqca1001004003001002",
"cqca1003001001001",
"cqca1002004001015019",
"cqca1118003007562",
"cqca1001005002252002",
"cqca1002005016002005",
"cqca1003001003014",
"cqca1001001007002007",
"cqca1002005005004011",
"cqca1002002001006001",
"cqca1002002015001012",
"cqca1002002014009005",
"cqca1002002001001",
"cqca1001013001001060",
"cqca1002007010001004",
"cqca1001005002126002",
"cqca1001013001001018",
"cqca1002002013002017",
"cqca1002002014004004",
"cqca1002002014010003",
"cqca1002002015002007",
"cqca1002004004009005",
"cqca1002008004001001",
"cqca1009002002",
"cqca1118003007325",
"cqca1001005002104001",
"cqca1001005002120003",
"cqca1001005002122002",
"cqca1001005002180001",
"cqca1002002002008005",
"cqca1002006001001004",
"cqca1001005002128007",
"cqca1001005003106001",
"cqca1001005003119002",
"cqca1001009005001018",
"cqca1001012001002006",
"cqca1001014002001002",
"cqca1002002001005001",
"cqca1002002015001011",
"cqca1002002015001027",
"cqca1002004001003005",
"cqca1002004001015021",
"cqca1002005001006005",
"cqca1002006001003013",
"cqca1002009001001034",
"cqca1118003001196",
"cqca1001003005023001",
"cqca1001010005001003",
"cqca1002003001001001",
"cqca1118004003043",
 "cqca1001005002179001",
"cqca1001009001001064",
"cqca1001013001001018",
"cqca1001010001009001",
"cqca1002002003001027",
"cqca1002002008001014",
"cqca1002005007001008",
"cqca1001001006003008",
"cqca1001005002104001",
"cqca1002002003003003",
"cqca1002002015001021",
"cqca1002009001002099",
"cqca1001010010032005",
"cqca1001009002003007",
"cqca1001013001001058",
"cqca1001014002001002",
"cqca1001012001002006",
"cqca1002007010003002",
"cqca1001005001011001",
"cqca1001009002003010"
)
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
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.view_count") as `view_count`	-- 浏览量or播放量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.comment_count") as `comment_count`	-- 评论量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.like_count") as `like_count`	-- 点赞量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.share_count") as `share_count`	-- 转发量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.favorite_count") as `favorite_count` 	-- 收藏量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.work_order_id") as `work_order_id`	-- 工单ID
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
     , CURRENT_TIMESTAMP()  `insert_dt`
from base_data as `f`
         left join brand_data as `d3` on `f`.`brand_code` = `d3`.`brand_code`
         left join `internal`.`voc_ms_td`.`voc_ext_ins_channel_mv` as `d4` on `f`.`channel_id` = `internal`.`voc_ms_td`.`d4`.`code`
         left join car_series_data as `d5` on `f`.`car_series_code` = `d5`.`car_series_code`
         left join `internal`.`voc_ms_td`.`voc_ext_ins_tag_system_final_mv` as `d6` ON `f`.`topic` = `internal`.`voc_ms_td`.`d6`.`topic`


DROP MATERIALIZED VIEW IF EXISTS voc_ext_ins_tag_system_final_mv;

CREATE MATERIALIZED VIEW voc_ext_ins_tag_system_final_mv
(topic,id,topic_text,cpt_tag_first_code,cpt_tag_first,cpt_tag_second_code,cpt_tag_second,cpt_tag_three_code,cpt_tag_three,cpt_tag_four_code,cpt_tag_four,ujy_tag_first_code,ujy_tag_first,ujy_tag_second_code,ujy_tag_second,ujy_tag_three_code,ujy_tag_three,ujy_tag_four_code,ujy_tag_four,cma_tag_first_code,cma_tag_first,cma_tag_second_code,cma_tag_second,cma_tag_three_code,cma_tag_three,cma_tag_four_code,cma_tag_four,dom_tag_first_code,dom_tag_first,dom_tag_second_code,dom_tag_second,dom_tag_three_code,dom_tag_three,dom_tag_four_code,dom_tag_four,vtr_tag_first_code,vtr_tag_first,vtr_tag_second_code,vtr_tag_second,vtr_tag_three_code,vtr_tag_three,vtr_tag_four_code,vtr_tag_four,nps_tag_first_code,nps_tag_first,nps_tag_second_code,nps_tag_second,nps_tag_three_code,nps_tag_three,nps_tag_four_code,nps_tag_four,tag_parent_id,tag_type,tag_attribute,energy_type,car_type,tag_status,tag_description,seriousness,user_journey1,user_journey2,user_journey3,scenario_attr,event_clarity,d2c_responsible_dept,d2c_cc_dept,d2c_accountable_dept,create_time,update_time,create_user,update_user,app_client,sort,level,emotion,intention,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,insert_dt)
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-11-25 22:30:00"
DUPLICATE KEY(`topic`)
COMMENT '物化-最终与模型结果匹配标签表'
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
AS SELECT
              `internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`topic`,
              -- 处理id：两行id不同，随机取一个（用MAX/MIN均可，因无业务优先级）
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`id`) AS `id`,
              -- 处理topic_text：两行值一致（均为“轮毂异常磨损”），直接取
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`topic_text`) AS `topic_text`,
              -- CPT标签：两行均为NULL，合并后仍为NULL
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cpt_tag_first_code`) AS `cpt_tag_first_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cpt_tag_first`) AS `cpt_tag_first`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cpt_tag_second_code`) AS `cpt_tag_second_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cpt_tag_second`) AS `cpt_tag_second`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cpt_tag_three_code`) AS `cpt_tag_three_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cpt_tag_three`) AS `cpt_tag_three`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cpt_tag_four_code`) AS `cpt_tag_four_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cpt_tag_four`) AS `cpt_tag_four`,
              -- UJY标签：第一行有值（cqca1118/日常用车等）、第二行NULL，取有值行
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`ujy_tag_first_code`) AS `ujy_tag_first_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`ujy_tag_first`) AS `ujy_tag_first`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`ujy_tag_second_code`) AS `ujy_tag_second_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`ujy_tag_second`) AS `ujy_tag_second`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`ujy_tag_three_code`) AS `ujy_tag_three_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`ujy_tag_three`) AS `ujy_tag_three`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`ujy_tag_four_code`) AS `ujy_tag_four_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`ujy_tag_four`) AS `ujy_tag_four`,
              -- CMA标签：第一行NULL、第二行有值（cqca1001/底盘等），取有值行
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cma_tag_first_code`) AS `cma_tag_first_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cma_tag_first`) AS `cma_tag_first`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cma_tag_second_code`) AS `cma_tag_second_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cma_tag_second`) AS `cma_tag_second`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cma_tag_three_code`) AS `cma_tag_three_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cma_tag_three`) AS `cma_tag_three`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cma_tag_four_code`) AS `cma_tag_four_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`cma_tag_four`) AS `cma_tag_four`,
              -- DOM/VTR/NPS标签：两行均为NULL，合并后仍为NULL
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`dom_tag_first_code`) AS `dom_tag_first_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`dom_tag_first`) AS `dom_tag_first`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`dom_tag_second_code`) AS `dom_tag_second_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`dom_tag_second`) AS `dom_tag_second`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`dom_tag_three_code`) AS `dom_tag_three_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`dom_tag_three`) AS `dom_tag_three`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`dom_tag_four_code`) AS `dom_tag_four_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`dom_tag_four`) AS `dom_tag_four`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`vtr_tag_first_code`) AS `vtr_tag_first_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`vtr_tag_first`) AS `vtr_tag_first`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`vtr_tag_second_code`) AS `vtr_tag_second_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`vtr_tag_second`) AS `vtr_tag_second`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`vtr_tag_three_code`) AS `vtr_tag_three_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`vtr_tag_three`) AS `vtr_tag_three`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`vtr_tag_four_code`) AS `vtr_tag_four_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`vtr_tag_four`) AS `vtr_tag_four`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`nps_tag_first_code`) AS `nps_tag_first_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`nps_tag_first`) AS `nps_tag_first`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`nps_tag_second_code`) AS `nps_tag_second_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`nps_tag_second`) AS `nps_tag_second`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`nps_tag_three_code`) AS `nps_tag_three_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`nps_tag_three`) AS `nps_tag_three`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`nps_tag_four_code`) AS `nps_tag_four_code`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`nps_tag_four`) AS `nps_tag_four`,
              -- 基础字段：两行值一致（如tag_type=FinalLabel、emotion=负向等），直接取
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_parent_id`) AS `tag_parent_id`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_type`) AS `tag_type`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_attribute`) AS `tag_attribute`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`energy_type`) AS `energy_type`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`car_type`) AS `car_type`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_status`) AS `tag_status`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_description`) AS `tag_description`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`seriousness`) AS `seriousness`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`user_journey1`) AS `user_journey1`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`user_journey2`) AS `user_journey2`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`user_journey3`) AS `user_journey3`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`scenario_attr`) AS `scenario_attr`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`event_clarity`) AS `event_clarity`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`d2c_responsible_dept`) AS `d2c_responsible_dept`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`d2c_cc_dept`) AS `d2c_cc_dept`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`d2c_accountable_dept`) AS `d2c_accountable_dept`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`create_time`) AS `create_time`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`update_time`) AS `update_time`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`create_user`) AS `create_user`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`update_user`) AS `update_user`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`app_client`) AS `app_client`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`sort`) AS `sort`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`level`) AS `level`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`emotion`) AS `emotion`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`intention`) AS `intention`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_accuracy`) AS `tag_accuracy`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_customer_issue_classification`) AS `tag_customer_issue_classification`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_issue_severity`) AS `tag_issue_severity`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_code_status`) AS `tag_code_status`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_business_domain`) AS `tag_business_domain`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_high_value_flag`) AS `tag_high_value_flag`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_complaint_flag_needing_reply`) AS `tag_complaint_flag_needing_reply`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_high_quality_voc_flag`) AS `tag_high_quality_voc_flag`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_new_energy_or_fuel`) AS `tag_new_energy_or_fuel`,
              MAX(`internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`tag_need_forvclosed_loop`) AS `tag_need_forvclosed_loop`,
              current_timestamp() as `insert_dt`
   FROM `internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`
-- 按topic分组：将相同topic的两行数据合并为一行
   GROUP BY `internal`.`voc_ms_td`.`voc_ext_ins_tag_by_system_mv`.`topic`



DROP MATERIALIZED VIEW IF EXISTS voc_ext_ins_tag_by_system_mv;

CREATE MATERIALIZED VIEW voc_ext_ins_tag_by_system_mv
(id,topic,topic_text,cpt_tag_first_code,cpt_tag_first,cpt_tag_second_code,cpt_tag_second,cpt_tag_three_code,cpt_tag_three,cpt_tag_four_code,cpt_tag_four,ujy_tag_first_code,ujy_tag_first,ujy_tag_second_code,ujy_tag_second,ujy_tag_three_code,ujy_tag_three,ujy_tag_four_code,ujy_tag_four,cma_tag_first_code,cma_tag_first,cma_tag_second_code,cma_tag_second,cma_tag_three_code,cma_tag_three,cma_tag_four_code,cma_tag_four,dom_tag_first_code,dom_tag_first,dom_tag_second_code,dom_tag_second,dom_tag_three_code,dom_tag_three,dom_tag_four_code,dom_tag_four,vtr_tag_first_code,vtr_tag_first,vtr_tag_second_code,vtr_tag_second,vtr_tag_three_code,vtr_tag_three,vtr_tag_four_code,vtr_tag_four,nps_tag_first_code,nps_tag_first,nps_tag_second_code,nps_tag_second,nps_tag_three_code,nps_tag_three,nps_tag_four_code,nps_tag_four,tag_parent_id,tag_type,tag_attribute,energy_type,car_type,tag_status,tag_description,seriousness,user_journey1,user_journey2,user_journey3,scenario_attr,event_clarity,d2c_responsible_dept,d2c_cc_dept,d2c_accountable_dept,create_time,update_time,create_user,update_user,app_client,sort,level,emotion,intention,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,insert_dt)
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-11-25 22:30:00"
DUPLICATE KEY(`id`)
COMMENT '物化-所有类型标签多列表'
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
AS SELECT
              -- 主键：复用源表id（确保符合目标表Hudi主键规则`primaryKey='id'`，避免冲突）
              `internal`.`voc_ms_td`.`src`.`id`,
              -- topic/topic_text：源表无对应数据，暂填NULL（可根据后续业务逻辑补充）
              `internal`.`voc_ms_td`.`src`.`leaf_code` AS `topic`,
              `internal`.`voc_ms_td`.`src`.`leaf_label` AS `topic_text`,
              -- -------------------------- CPT标签映射（tag_type='CPT'） --------------------------
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CPT' THEN `internal`.`voc_ms_td`.`src`.`frist_code` ELSE NULL END AS `cpt_tag_first_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CPT' THEN `internal`.`voc_ms_td`.`src`.`frist_label` ELSE NULL END AS `cpt_tag_first`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CPT' THEN `internal`.`voc_ms_td`.`src`.`second_code` ELSE NULL END AS `cpt_tag_second_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CPT' THEN `internal`.`voc_ms_td`.`src`.`second_label` ELSE NULL END AS `cpt_tag_second`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CPT' THEN `internal`.`voc_ms_td`.`src`.`three_code` ELSE NULL END AS `cpt_tag_three_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CPT' THEN `internal`.`voc_ms_td`.`src`.`three_label` ELSE NULL END AS `cpt_tag_three`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CPT' THEN `internal`.`voc_ms_td`.`src`.`four_code` ELSE NULL END AS `cpt_tag_four_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CPT' THEN `internal`.`voc_ms_td`.`src`.`four_label` ELSE NULL END AS `cpt_tag_four`,
              -- -------------------------- userJourney标签映射（tag_type='userJourney'，用户全旅程） --------------------------
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'JOUR' THEN `internal`.`voc_ms_td`.`src`.`frist_code` ELSE NULL END AS `ujy_tag_first_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'JOUR' THEN `internal`.`voc_ms_td`.`src`.`frist_label` ELSE NULL END AS `ujy_tag_first`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'JOUR' THEN `internal`.`voc_ms_td`.`src`.`second_code` ELSE NULL END AS `ujy_tag_second_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'JOUR' THEN `internal`.`voc_ms_td`.`src`.`second_label` ELSE NULL END AS `ujy_tag_second`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'JOUR' THEN `internal`.`voc_ms_td`.`src`.`three_code` ELSE NULL END AS `ujy_tag_three_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'JOUR' THEN `internal`.`voc_ms_td`.`src`.`three_label` ELSE NULL END AS `ujy_tag_three`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'JOUR' THEN `internal`.`voc_ms_td`.`src`.`four_code` ELSE NULL END AS `ujy_tag_four_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'JOUR' THEN `internal`.`voc_ms_td`.`src`.`four_label` ELSE NULL END AS `ujy_tag_four`,
              -- -------------------------- Domain标签映射（tag_type='Domain'，商品化属性） --------------------------
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'PR0' THEN `internal`.`voc_ms_td`.`src`.`frist_code` ELSE NULL END AS `cma_tag_first_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'PR0' THEN `internal`.`voc_ms_td`.`src`.`frist_label` ELSE NULL END AS `cma_tag_first`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'PR0' THEN `internal`.`voc_ms_td`.`src`.`second_code` ELSE NULL END AS `cma_tag_second_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'PR0' THEN `internal`.`voc_ms_td`.`src`.`second_label` ELSE NULL END AS `cma_tag_second`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'PR0' THEN `internal`.`voc_ms_td`.`src`.`three_code` ELSE NULL END AS `cma_tag_three_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'PR0' THEN `internal`.`voc_ms_td`.`src`.`three_label` ELSE NULL END AS `cma_tag_three`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'PR0' THEN `internal`.`voc_ms_td`.`src`.`four_code` ELSE NULL END AS `cma_tag_four_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'PR0' THEN `internal`.`voc_ms_td`.`src`.`four_label` ELSE NULL END AS `cma_tag_four`,
              -- -------------------------- CommodityAttr标签映射（tag_type='CommodityAttr'，全领域业务） --------------------------
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CA' THEN `internal`.`voc_ms_td`.`src`.`frist_code` ELSE NULL END AS `dom_tag_first_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CA' THEN `internal`.`voc_ms_td`.`src`.`frist_label` ELSE NULL END AS `dom_tag_first`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CA' THEN `internal`.`voc_ms_td`.`src`.`second_code` ELSE NULL END AS `dom_tag_second_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CA' THEN `internal`.`voc_ms_td`.`src`.`second_label` ELSE NULL END AS `dom_tag_second`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CA' THEN `internal`.`voc_ms_td`.`src`.`three_code` ELSE NULL END AS `dom_tag_three_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CA' THEN `internal`.`voc_ms_td`.`src`.`three_label` ELSE NULL END AS `dom_tag_three`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CA' THEN `internal`.`voc_ms_td`.`src`.`four_code` ELSE NULL END AS `dom_tag_four_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'CA' THEN `internal`.`voc_ms_td`.`src`.`four_label` ELSE NULL END AS `dom_tag_four`,
              -- -------------------------- VRT标签映射（tag_type='VRT'） --------------------------
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'VRT' THEN `internal`.`voc_ms_td`.`src`.`frist_code` ELSE NULL END AS `vtr_tag_first_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'VRT' THEN `internal`.`voc_ms_td`.`src`.`frist_label` ELSE NULL END AS `vtr_tag_first`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'VRT' THEN `internal`.`voc_ms_td`.`src`.`second_code` ELSE NULL END AS `vtr_tag_second_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'VRT' THEN `internal`.`voc_ms_td`.`src`.`second_label` ELSE NULL END AS `vtr_tag_second`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'VRT' THEN `internal`.`voc_ms_td`.`src`.`three_code` ELSE NULL END AS `vtr_tag_three_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'VRT' THEN `internal`.`voc_ms_td`.`src`.`three_label` ELSE NULL END AS `vtr_tag_three`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'VRT' THEN `internal`.`voc_ms_td`.`src`.`four_code` ELSE NULL END AS `vtr_tag_four_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'VRT' THEN `internal`.`voc_ms_td`.`src`.`four_label` ELSE NULL END AS `vtr_tag_four`,
              -- -------------------------- NPS标签映射（tag_type='NPS'） --------------------------
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'NPS' THEN `internal`.`voc_ms_td`.`src`.`frist_code` ELSE NULL END AS `nps_tag_first_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'NPS' THEN `internal`.`voc_ms_td`.`src`.`frist_label` ELSE NULL END AS `nps_tag_first`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'NPS' THEN `internal`.`voc_ms_td`.`src`.`second_code` ELSE NULL END AS `nps_tag_second_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'NPS' THEN `internal`.`voc_ms_td`.`src`.`second_label` ELSE NULL END AS `nps_tag_second`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'NPS' THEN `internal`.`voc_ms_td`.`src`.`three_code` ELSE NULL END AS `nps_tag_three_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'NPS' THEN `internal`.`voc_ms_td`.`src`.`three_label` ELSE NULL END AS `nps_tag_three`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'NPS' THEN `internal`.`voc_ms_td`.`src`.`four_code` ELSE NULL END AS `nps_tag_four_code`,
              CASE WHEN `internal`.`voc_ms_td`.`src`.`tag_type` = 'NPS' THEN `internal`.`voc_ms_td`.`src`.`four_label` ELSE NULL END AS `nps_tag_four`,
              -- -------------------------- 公共基础字段（直接复用源表，确保类型与目标表一致） --------------------------
              `internal`.`voc_ms_td`.`src`.`tag_parent_id`,
              `internal`.`voc_ms_td`.`src`.`tag_type`,
              `internal`.`voc_ms_td`.`src`.`tag_attribute`,
              `internal`.`voc_ms_td`.`src`.`energy_type`,
              `internal`.`voc_ms_td`.`src`.`car_type`,
              `internal`.`voc_ms_td`.`src`.`tag_status`,
              `internal`.`voc_ms_td`.`src`.`tag_description`,
              `internal`.`voc_ms_td`.`src`.`seriousness`,
              `internal`.`voc_ms_td`.`src`.`user_journey1`,
              `internal`.`voc_ms_td`.`src`.`user_journey2`,
              `internal`.`voc_ms_td`.`src`.`user_journey3`,
              `internal`.`voc_ms_td`.`src`.`scenario_attr`,
              `internal`.`voc_ms_td`.`src`.`event_clarity`,
              `internal`.`voc_ms_td`.`src`.`d2c_responsible_dept`,
              `internal`.`voc_ms_td`.`src`.`d2c_cc_dept`,
              `internal`.`voc_ms_td`.`src`.`d2c_accountable_dept`,
              `internal`.`voc_ms_td`.`src`.`create_time`,
              `internal`.`voc_ms_td`.`src`.`update_time`,
              `internal`.`voc_ms_td`.`src`.`create_user`,
              `internal`.`voc_ms_td`.`src`.`update_user`,
              `internal`.`voc_ms_td`.`src`.`app_client`,
              `internal`.`voc_ms_td`.`src`.`sort`,
              `internal`.`voc_ms_td`.`src`.`level`,
              `internal`.`voc_ms_td`.`src`.`emotion`,
              `internal`.`voc_ms_td`.`src`.`intention`,
              `internal`.`voc_ms_td`.`src`.`tag_accuracy`,
              `internal`.`voc_ms_td`.`src`.`tag_customer_issue_classification`,
              `internal`.`voc_ms_td`.`src`.`tag_issue_severity`,
              `internal`.`voc_ms_td`.`src`.`tag_code_status`,
              `internal`.`voc_ms_td`.`src`.`tag_business_domain`,
              `internal`.`voc_ms_td`.`src`.`tag_high_value_flag`,
              `internal`.`voc_ms_td`.`src`.`tag_complaint_flag_needing_reply`,
              `internal`.`voc_ms_td`.`src`.`tag_high_quality_voc_flag`,
              `internal`.`voc_ms_td`.`src`.`tag_new_energy_or_fuel`,
              `internal`.`voc_ms_td`.`src`.`tag_need_forvclosed_loop`,
              current_timestamp() as `insert_dt`
   FROM `internal`.`voc_ms_td`.`voc_ext_ins_tag_by_level_mv` `src`

DROP MATERIALIZED VIEW IF EXISTS voc_ext_ins_tag_by_level_mv;


CREATE MATERIALIZED VIEW voc_ext_ins_tag_by_level_mv
(id,leaf_code,leaf_label,tag_parent_id,level,frist_code,frist_label,second_code,second_label,three_code,three_label,four_code,four_label,tag_type,tag_attribute,energy_type,car_type,tag_status,tag_description,seriousness,user_journey1,user_journey2,user_journey3,create_time,update_time,create_user,update_user,app_client,sort,emotion,intention,scenario_attr,event_clarity,d2c_responsible_dept,d2c_cc_dept,d2c_accountable_dept,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,insert_dt)
BUILD IMMEDIATE REFRESH COMPLETE ON SCHEDULE EVERY 30 MINUTE STARTS "2025-11-25 22:20:00"
DUPLICATE KEY(`id`)
COMMENT '物化-1到5级标签映射表'
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
        `internal`.`voc_ms_td`.`ins_tag_client`.`id`,              -- 标签主键（用于后续补充目标表字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_code`,        -- 标签编码
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_name`,        -- 标签名称
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_parent_id`,   -- 父级ID（目标表必填字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_attribute`,   -- 标签属性（目标表字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_type`,        -- 标签类型（目标表字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`energy_type`,     -- 关联能源（目标表字段，原表为JSON，转STRING适配）
        `internal`.`voc_ms_td`.`ins_tag_client`.`car_type`,        -- 车辆类型（目标表字段，原表为JSON，转STRING适配）
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_status`,      -- 标签状态（目标表字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_description`, -- 标签描述（目标表字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`seriousness`,     -- 严重性（目标表字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`user_journey1`,    -- 用户旅途（原表为JSON，拆分适配目标表user_journey1-3）
        `internal`.`voc_ms_td`.`ins_tag_client`.`user_journey2`,    -- 用户旅途（原表为JSON，拆分适配目标表user_journey1-3）
        `internal`.`voc_ms_td`.`ins_tag_client`.`user_journey3`,    -- 用户旅途（原表为JSON，拆分适配目标表user_journey1-3）
        `internal`.`voc_ms_td`.`ins_tag_client`.`create_time`,     -- 创建时间（目标表Hudi预合并字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`update_time`,     -- 更新时间（目标表字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`create_user`,     -- 创建人（目标表字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`update_user`,     -- 更新人（目标表字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`app_client`,      -- 应用客户（目标表字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`sort`,            -- 排序字段（目标表字段）
        `internal`.`voc_ms_td`.`ins_tag_client`.`emotion`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`intention`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`scenario_attr`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`event_clarity`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`d2c_responsible_dept`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`d2c_cc_dept`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`d2c_accountable_dept`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_accuracy`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_customer_issue_classification`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_issue_severity`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_code_status`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_business_domain`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_high_value_flag`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_complaint_flag_needing_reply`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_high_quality_voc_flag`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_new_energy_or_fuel`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_need_forvclosed_loop`,
        `internal`.`voc_ms_td`.`ins_tag_client`.`ds`               -- 分区字段（目标表Hudi分区，原表筛选条件已确保非空）
    FROM `internal`.`voc_ms_td`.`ins_tag_client`
    WHERE
        -- tag_type = 'userJourney'  -- 规则7：仅保留userJourney类型
        -- ds =  '99999'              -- 规则7：过滤空ds（适配目标表ds分区）
        `internal`.`voc_ms_td`.`ins_tag_client`.`tag_status` = '1'     -- 仅启用状态（目标表tag_status字段）
),
-- 提取5级标签（基准层，补充原表基础属性）
`level5` AS (
    SELECT
        `base_tags`.`id` AS `l5_id`,
        `base_tags`.`tag_code` AS `l5_tag_code`,
        `base_tags`.`tag_name` AS `l5_tag_name`,
        `base_tags`.`tag_parent_id` AS `l5_parent_id`,
        5 AS `l5_level`,  -- 层级标识（目标表level字段）
        -- 补充目标表所需基础属性（从base_tags继承）
        `base_tags`.`tag_type`,
        `base_tags`.`tag_attribute`,
        CAST(`base_tags`.`energy_type` AS STRING) AS `energy_type`,  -- JSON转STRING适配目标表
        CAST(`base_tags`.`car_type` AS STRING) AS `car_type`,        -- JSON转STRING适配目标表
        `base_tags`.`tag_status`,
        `base_tags`.`tag_description`,
        `base_tags`.`seriousness`,
        -- 拆分user_journey JSON为目标表user_journey1-3（示例：取前3个元素，可按实际JSON结构调整）
        `base_tags`.`user_journey1`,
        `base_tags`.`user_journey2`,
        `base_tags`.`user_journey3`,
        `base_tags`.`create_time`,
        `base_tags`.`update_time`,
        `base_tags`.`create_user`,
        `base_tags`.`update_user`,
        `base_tags`.`app_client`,
        `base_tags`.`sort`,
        `base_tags`.`emotion`,
        `base_tags`.`intention`,
        `base_tags`.`scenario_attr`,
        `base_tags`.`event_clarity`,
        `base_tags`.`d2c_responsible_dept`,
        `base_tags`.`d2c_cc_dept`,
        `base_tags`.`d2c_accountable_dept`,
        `base_tags`.`tag_accuracy`,
        `base_tags`.`tag_customer_issue_classification`,
        `base_tags`.`tag_issue_severity`,
        `base_tags`.`tag_code_status`,
        `base_tags`.`tag_business_domain`,
        `base_tags`.`tag_high_value_flag`,
        `base_tags`.`tag_complaint_flag_needing_reply`,
        `base_tags`.`tag_high_quality_voc_flag`,
        `base_tags`.`tag_new_energy_or_fuel`,
        `base_tags`.`tag_need_forvclosed_loop`,
        `base_tags`.`ds`
    FROM base_tags
    WHERE `base_tags`.`tag_attribute` = 'FinalLabel'  -- 5级节点标识
),
-- 提取1级标签（父级为0级，用于关联）
`level1` AS (
    SELECT
        `base_tags`.`id` AS `level1_id`,
        `base_tags`.`tag_code` AS `level1_code`,
        `base_tags`.`tag_name` AS `level1_name`
    FROM base_tags
    WHERE `base_tags`.`tag_parent_id` = '0'
),
-- 提取2级标签（关联1级，用于关联）
`level2` AS (
    SELECT
        `l2`.`id` AS `level2_id`,
        `l2`.`tag_code` AS `level2_code`,
        `l2`.`tag_name` AS `level2_name`,
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
    `l5`.`sort`,
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






SELECT ibi.code AS code, ibi.name AS name, mention_stats.self_mentions, mention_stats.competitor_mentions
FROM voc_ext_ins_brand_info_mv ibi
         LEFT JOIN (
    SELECT brand_code
         , SUM(CASE
                   WHEN competitive_type = '1' THEN 1
                   ELSE 0
        END) AS self_mentions
         , SUM(CASE
                   WHEN competitive_type = '2' THEN 1
                   ELSE 0
        END) AS competitor_mentions
         , COUNT(1) AS total_mentions
    FROM voc_sentiment_annotations_results_v
    WHERE brand_name IS NOT NULL
      AND data_create_time >= '2025-01-01'
      AND data_create_time <= '2025-11-27'
      AND sentiment IS NOT NULL
      AND sentiment <> ''
      AND channel_catagory = '公域'
      AND topic NOT IN ('cqca1001013001001060', 'cqca1002002014004004', 'cqca1002004001003005', 'cqca1003001001001', 'cqca1001001007002007', 'cqca1118004003043', 'cqca1001005003119002', 'cqca1001005001011001', 'cqca1002003001001001', 'cqca1001009002003007', 'cqca1002002014010003', 'cqca1001010001009001', 'cqca1118003007325', 'cqca1009002002', 'cqca1118003001196', 'cqca1002002008001014', 'cqca1002004001015019', 'cqca1001013001001018', 'cqca1001005002104001', 'cqca1001010005001003', 'cqca1002002001001', 'cqca1002005005004011', 'cqca1002002015001021', 'cqca1001004003001002')/*渠道、车系、一级，二级，区域，数据类型，数据源 */
      AND (competitive_type = 2
        OR (brand_code IN ('A01', 'A03', 'A04', 'A05')
            AND competitive_type = 1)) /*区域code*//*区域专营店*//*客户类型*//*客户性别*//*车主年龄*//*车辆年龄*/
      AND channel_code IN ('pdt_opinion_cjd_hw_yjfk', 'pdt_quest_caylzp', 'pd_post_ks', 'pd_post_xhs', 'pdt_order_awtrxfw', 'pd_post_tpyh', 'pdt_opinion_cayllyb', 'pdt_order_slqclrxfw', 'pdt_opinion_yly_yjfk', 'pdt_order_caqysbgd', 'pd_post_zg315w', 'pdt_opinion_cjd_yjfk_sda', 'pd_post_tpyqc_lt', 'pdt_order_caylrxfw', 'pdt_opinion_cakclyb', 'pdt_opinion_awtlyb', 'pdt_quest_chmzp', 'pdt_opinion_cakcaxcx_yjfk', 'pd_post_bjh', 'pdt_opinion_cjd_yjfk_wt', 'pdt_order_caylsbgd', 'pd_post_qczj_cjh', 'pd_post_yc_sq', 'pdt_opinion_slqcaxcx_yjfk', 'pd_post_yc_sp', 'pd_post_czzj', 'pd_post_qctsw', 'pdt_order_slqcsbgd', 'pd_post_tpyqc_wz', 'pdt_order_cakcrxfw', 'pdt_quest_jtzhyxzp', 'pd_post_bilibili', 'pd_post_zgqcw', 'pdt_opinion_caqylyb', 'pdt_order_caqcrxfw', 'pdt_quest_awtzp', 'pd_post_bdtb', 'pdt_opinion_catxcx_yjfk', 'pdt_post_awtaxcx_k_zx', 'pd_post_jrtt', 'pd_post_dy', 'pdt_quest_slqczp', 'pd_post_wyh', 'pd_post_yc_wz', 'pd_post_qcmw_ts', 'pd_post_qcmw_xw', 'pdt_quest_caqyzp', 'pd_post_akqc', 'pd_post_qczj_lt', 'pd_post_akqh', 'pd_post_qczhw', 'pd_post_wb', 'pd_post_czw_lt', 'pd_post_qczj_wz', 'pdt_order_cakcsbgd', 'pd_post_czw_ts', 'pd_post_czw_xw', 'pd_post_dcd_pl', 'pd_post_zgqczlw_ts', 'pd_post_zgqczlw_xw', 'pd_post_qczj_sp', 'pdt_quest_kfgszp', 'pdt_post_awtaxcx_k_sq', 'pdt_opinion_slqclyb', 'pd_post_hmts_ts', 'pdt_order_awtdmsgd', 'pdt_opinion_caqyaxcx_lxwm', 'pd_post_zh', 'pdt_post_slqcaxcx_ts_sq', 'pdt_order_caqyrxfw', 'pdt_quest_kcqczp', 'pub_post_wx_spx')
    GROUP BY brand_code
) mention_stats
                   ON ibi.code = mention_stats.brand_code
WHERE 1 = 1
  AND (ibi.competitive_type != 1
    OR (ibi.code IN ('A01', 'A03', 'A04', 'A05')
        AND ibi.competitive_type = 1))
ORDER BY COALESCE(mention_stats.self_mentions, 0) DESC,
         COALESCE(mention_stats.competitor_mentions, 0) DESC,
         CASE ibi.competitive_type
                WHEN 1 THEN 1
                WHEN 2 THEN 2
                WHEN 0 THEN 3
                WHEN 3 THEN 4
                ELSE 5
    END ASC






DROP MATERIALIZED VIEW IF EXISTS ins_brand_info_mv;

drop table voc_ins_user_journey_level1_v;
drop table province_dict_data_v;
drop table ins_channel;
drop table ins_car_series_info;
drop table ins_brand_info;
drop table ins_brand_info_mv;

drop view brand_self_brand_v;

REFRESH MATERIALIZED VIEW voc_ext_ins_car_series_info_mv COMPLETE;
CREATE OR REPLACE VIEW  voc_user_journey_level1_v as
select tag_name AS `name`,
       tag_code AS `code`,
       sort    AS `sort`
from voc_ext_ins_tag_client_mv
where tag_type = 'JOUR' and tag_parent_id = '0'
order by sort








CREATE MATERIALIZED VIEW voc_ext_ins_tag_client_mv
BUILD IMMEDIATE
REFRESH AUTO ON SCHEDULE EVERY 6 HOUR STARTS "2025-11-25 02:59:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 1
PROPERTIES (
    "replication_allocation" = "tag.location.default: 1",
    "storage_medium" = "hdd",
    "storage_format" = "V2"
)
AS select *
   from `voc_mysql_jdbc`.`voc_ms_td`.`ins_tag_client`






CREATE OR REPLACE VIEW  voc_province_dict_data_v as
select distinct province_code,province_name
from voc_mysql_jdbc.voc_ms_be.ins_province_area



CREATE OR REPLACE VIEW  voc_brand_self_brand_v as
select
    *
from (
         select  '长安汽车集团' as brand_name,"groupCode" as brand_code,-1 as sort, "长安汽车集团.png" as imgUrl
         union all
         select
             *
         from (
                  select
                      name as brand_name,
                      code as brand_code,
                      order_by as sort,
                      img as imgUrl
                  from  voc_ext_ins_brand_info_mv
                  where 1=1
                    and competitive_type = 1
                  order by order_by asc
              ) t
     ) seb
order by sort asc;

-- 查看所有例行导入任务状态
SHOW ROUTINE LOAD;

-- 查看指定例行导入任务详情
SHOW ROUTINE LOAD FOR dwd_voc_all_meta_data_kafka;

-- 查看数据库版本
SELECT VERSION();

-- 查看Doris版本变量
SHOW VARIABLES LIKE 'doris_version';

-- 查看物化视图创建语句
SHOW CREATE MATERIALIZED VIEW voc_ext_ins_brand_info_mv;
SHOW CREATE MATERIALIZED VIEW voc_anal_flow_sentiment_annotations_results_mv;
SHOW CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_labeled_mv;
SHOW CREATE MATERIALIZED VIEW report_voc_model_tags_result_data_risk_mv;

-- 查看例行导入任务的创建语句
SHOW CREATE ROUTINE LOAD FOR voc_anal_flow_model_tags_result_data_kafka;

-- 查看所有例行导入任务状态
SHOW ROUTINE LOAD;







select
    *
from (
         SHOW ROUTINE LOAD;
)

-- 查看数据库版本
SELECT VERSION();

-- 查看前端节点信息
SHOW PROC '/frontends';

-- 查看Doris版本变量
SHOW VARIABLES LIKE 'doris_version';

-- 查看指定例行导入任务详情
SHOW ROUTINE LOAD FOR voc_anal_flow_model_tags_result_data_kafka;

-- 查看例行导入任务的创建语句
SHOW CREATE ROUTINE LOAD FOR voc_anal_flow_model_tags_result_data_kafka;

-- 查看前端节点信息
SHOW PROC '/frontends';

CREATE ROUTINE LOAD voc_anal_flow_model_tags_result_data_kafka ON voc_anal_flow_model_tags_result_data_full
WITH APPEND
COLUMNS(id,data_id,work_id,client_id,channel_id,original_id,content_type,input_data_id,sample_data_type,original_text_scene,brand_code,car_series_code,label_type,scenario,sentiment,intention_type,topic,opinion,subject,fault_level,description,sentiment_score,keywords,publish_time,create_time,update_time,done,model_type,raw_data,ext_fields,biz_ext_attrs,biz_ext_attrs2,biz_ext_attrs3,cust_ext_attrs,vhl_ext_attrs,dealer_ext_attrs,prd_ext_attrs,tags_ext_attrs,one_id)
PROPERTIES
(
"desired_concurrent_number" = "3",
"max_error_number" = "0",
"max_filter_ratio" = "1.0",
"max_batch_interval" = "3",
"max_batch_rows" = "20000000",
"max_batch_size" = "1073741824",
"format" = "json",
"jsonpaths" = "["$.id","$.dataId","$.workId","$.clientId","$.channelId","$.originalId"
,"$.contentType","$.inputDataId","$.sampleDataType","$.originalTextScene","$.brandCode","$.carSeriesCode"
,"$.labelType","$.scenario","$.sentiment","$.intentionType"
,"$.topic","$.opinion","$.subject","$.faultLevel","$.description","$.sentimentScore","$.keywords"
,"$.publishTime","$.createTime","$.updateTime","$.done","$.modelType"
,"$.rawData","$.extFields","$.bizExtAttrs","$.bizExtAttrs2","$.bizExtAttrs3"
,"$.custExtAttrs","$.vhlExtAttrs","$.dealerExtAttrs","$.prdExtAttrs","$.tagsExtAttrs","$.oneId"]",
"strip_outer_array" = "false",
"num_as_string" = "false",
"fuzzy_parse" = "false",
"strict_mode" = "false",
"timezone" = "Asia/Shanghai",
"exec_mem_limit" = "2147483648"
)
FROM KAFKA
(
"kafka_broker_list" = "10.62.133.17:29095",
"kafka_topic" = "voc_anal_flow_model_tags_result_data",
"property.kafka_default_offsets" = "OFFSET_BEGINNING",
"property.group.id" = "voc-analysis-sdb-prod",
"kafka_partitions" = "0, 1, 2",
"kafka_offsets" = "193930, 193976, 193896"
);




CREATE MATERIALIZED VIEW ins_car_series_info
BUILD IMMEDIATE
REFRESH AUTO ON SCHEDULE EVERY 6 HOUR STARTS "2025-11-25 02:59:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 1
PROPERTIES (
    "replication_allocation" = "tag.location.default: 1",
    "storage_medium" = "hdd",
    "storage_format" = "V2"
)
AS SELECT * FROM voc_mysql_jdbc.voc_ms_be.ins_car_series_info;


CREATE MATERIALIZED VIEW ins_brand_info
BUILD IMMEDIATE
REFRESH AUTO ON SCHEDULE EVERY 6 HOUR STARTS "2025-11-25 01:59:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 1
PROPERTIES (
    "replication_allocation" = "tag.location.default: 1",
    "storage_medium" = "hdd",
    "storage_format" = "V2"
)
AS SELECT * FROM voc_mysql_jdbc.voc_ms_be.ins_brand_info;

REFRESH MATERIALIZED VIEW ins_brand_info_mv COMPLETE;

DROP MATERIALIZED VIEW IF EXISTS ins_brand_info_mv;



select
    competitive_product
    from
voc_mysql_jdbc.voc_ms_be.ins_brand_info
    where competitive_product is not null



REFRESH MATERIALIZED VIEW ads_voc_channel_m_full_mv COMPLETE;
REFRESH MATERIALIZED VIEW ads_voc_model_tags_result_data_mv COMPLETE;
REFRESH MATERIALIZED VIEW ads_voc_tags_h_full_mv COMPLETE;
REFRESH MATERIALIZED VIEW ads_voc_tags_system_h_full_mv COMPLETE;
REFRESH MATERIALIZED VIEW ads_voc_tags_system_info_h_full_mv COMPLETE;
REFRESH MATERIALIZED VIEW report_voc_model_tags_result_data_risk_mv COMPLETE;

SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema = 'voc_ms_td' ORDER BY table_rows DESC;


CREATE TABLE ads_voc_model_tags_result_data_m_inc LIKE ads_voc_model_tags_result_data_m_inc_1112;
INSERT INTO ads_voc_model_tags_result_data_m_inc_1112 SELECT * FROM ads_voc_model_tags_result_data_m_inc;
truncate table ads_voc_model_tags_result_data_m_inc;
drop table ads_voc_model_tags_result_data_m_inc_new;

-- 备份高质量标记
CREATE TABLE report_high_quality_record_1104old AS SELECT * FROM report_high_quality_record;
-- 更新高质量标记为原文id  @时光旅人的沉思
-- 步骤1：创建临时表
CREATE TABLE tmp_update_original_id AS
SELECT rhqr.sound_id, dvsd.data_id
FROM report_high_quality_record rhqr
         LEFT JOIN voc_sentiment_annotations_results_v dvsd ON rhqr.sound_id = dvsd.id
WHERE dvsd.id IS NOT NULL;

-- 步骤2：使用 UPDATE...FROM 语法
UPDATE report_high_quality_record
SET original_id = tmp.data_id
    FROM tmp_update_original_id tmp
WHERE report_high_quality_record.sound_id = tmp.sound_id;

-- 步骤3：删除临时表
DROP TABLE tmp_update_original_id;

-- 步骤4：删除重复数据
DELETE FROM report_high_quality_record
WHERE id NOT IN (
    SELECT MIN(id)
    FROM report_high_quality_record
    GROUP BY original_id
);

select
    count(1)
from ads_voc_model_tags_result_data_m_inc;


select
    *
from ins_brand_info ibi
where ibi.competitive_type=1 and ibi.competitive_product is not null


TRUNCATE TABLE voc_anal_flow_mate_data_full;

CREATE TABLE voc_anal_flow_model_tags_result_data_full LIKE voc_anal_flow_model_tags_result_data_full_1204;

-- 一条语句复制表结构和数据
CREATE TABLE voc_sentiment_annotations_results_v_old AS SELECT * FROM voc_sentiment_annotations_results_v;

CREATE TABLE voc_anal_flow_mate_data_labeled_v AS SELECT * FROM voc_anal_flow_mate_data_labeled_v_old;

CREATE TABLE ads_voc_model_tags_result_data_m_inc LIKE ads_voc_model_tags_result_data_m_inc_old;
ALTER TABLE ads_voc_model_tags_result_data_m_inc_1112 RENAME ads_voc_model_tags_result_data_m_inc;
ALTER TABLE voc_anal_flow_model_tags_result_data_full RENAME voc_anal_flow_model_tags_result_data_full_1204;
ALTER TABLE voc_anal_flow_model_tags_result_data_full_1204_null RENAME voc_anal_flow_model_tags_result_data_full;
ALTER TABLE voc_anal_flow_model_tags_result_data_full RENAME voc_anal_flow_model_tags_result_data_full_1205;
ALTER TABLE voc_anal_flow_model_tags_result_data_full RENAME voc_anal_flow_model_tags_result_data_full_1205;



ALTER TABLE ads_voc_model_tags_result_data_m_inc RENAME ads_voc_model_tags_result_data_m_inc_1124;
ALTER TABLE voc_anal_flow_mate_data_full RENAME voc_anal_flow_mate_data_full_1205_in;
drop table voc_anal_flow_mate_data_labeled_v;
CREATE TABLE voc_anal_flow_mate_data_full LIKE voc_anal_flow_mate_data_full_1205;


INSERT INTO voc_ms_td.sta_sys_filter_type (id, attr, name, filter_type, options_json, value_type, sort, create_time, page_display_type) VALUES ('40', 'topic_text', '观点', '2', '{"name": "观点", "term": "like", "field": "topic_text", "valueType": "string", "filterType": "2"}', null, null, null, 'H5Home,groupAnalysis,thisProductAnalysis,journeyAnalysis,productAnalysis,serviceAnalysis');

SHOW CREATE MATERIALIZED VIEW ads_voc_model_tags_result_data_mv;

drop MATERIALIZED view ads_voc_model_tags_result_data_mv;

CREATE MATERIALIZED VIEW ads_voc_model_tags_result_data_mv
(id,data_id,channel_catagory,channel_code,channel_name,brand_code,brand_name,car_series_code,car_series_name,model_name,content_type,title,sentiment,intention,data_create_time,publish_time,create_time,is_outer,hot_word,keywords,original_text_scene,market_id,competitive_type,series_factory,automark,one_id,user_journey1,user_journey2,user_journey3,usage_scenario_first,usage_scenario_second,d2c_responsible_dept,d2c_accountable_dept,d2c_cc_dept,cust_global_id,cust_classify,cust_main_phone,is_car_owner,cust_age,cust_age_group,cust_name,cust_gender,cust_high_educaion,marrige_statue,family_income,is_exchange_flg,purchase_car_times,is_member_flg,cust_province_code,cust_province,cust_city_code,cust_city,cust_type,cust_lived_prov,cust_lived_city,cust_profession,vhl_vin,vhl_color_name,vhl_product_date,vhl_offline_date,vhl_is_abroad,vhl_dis_ch,vhl_dis_mt,vhl_eng_clsf,vhl_eng_seris,vhl_veh_type,vhl_country,vhl_bd_clsf,vhl_seg_mt,vhl_pow_clsf,vhl_fu_clsf,vhl_modl_st,vhl_std_plnt_code,dlr_oc_id,dlr_oc_code,dlr_oc_name,dlr_oc_province_code,dlr_oc_province,dlr_oc_city_code,dlr_oc_city,dlr_dc_id,dlr_dc_code,dlr_dc_name,dlr_dc_province_code,dlr_dc_province,dlr_dc_city_code,dlr_dc_city,dlr_mc_id,dlr_mc_code,dlr_mc_name,dlr_mc_province_code,dlr_mc_province,dlr_mc_city_code,dlr_mc_city,is_wsater_army,is_manager_focused,is_big_v,author_id,author_nick,is_main_post,original_link,view_count,comment_count,like_count,share_count,favorite_count,work_order_id,quest_id,quest_type,quest_answer_score,quest_business_type,quest_business_scenario,tag_accuracy,tag_customer_issue_classification,tag_issue_severity,tag_code_status,tag_business_domain,tag_event_clarity,tag_high_value_flag,tag_complaint_flag_needing_reply,tag_complaint_flag_needing_prtv_msg,tag_high_quality_voc_flag,tag_new_energy_or_fuel,tag_need_forvclosed_loop,topic,topic_text,opinion,cpt_tag_first_code,cpt_tag_second_code,cpt_tag_three_code,cpt_tag_four_code,cpt_tag_first,cpt_tag_second,cpt_tag_three,cpt_tag_four,ujy_tag_first_code,ujy_tag_second_code,ujy_tag_three_code,ujy_tag_four_code,ujy_tag_first,ujy_tag_second,ujy_tag_three,ujy_tag_four,cma_tag_first_code,cma_tag_second_code,cma_tag_three_code,cma_tag_four_code,cma_tag_first,cma_tag_second,cma_tag_three,cma_tag_four,dom_tag_first_code,dom_tag_second_code,dom_tag_three_code,dom_tag_four_code,dom_tag_first,dom_tag_second,dom_tag_three,dom_tag_four,nps_tag_first_code,nps_tag_second_code,nps_tag_three_code,nps_tag_four_code,nps_tag_first,nps_tag_second,nps_tag_three,nps_tag_four,vtr_tag_first_code,vtr_tag_second_code,vtr_tag_three_code,vtr_tag_four_code,vtr_tag_first,vtr_tag_second,vtr_tag_three,vtr_tag_four,abandon)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 1 HOUR STARTS "2025-10-29 15:59:00" -- ！！！注意时间要大于当前时间
DUPLICATE KEY(`id`)
PARTITION BY (`publish_time`)
DISTRIBUTED BY HASH(`id`) BUCKETS 1
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
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
AS with `base_data` as (
    select
        `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`id`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`publish_time`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`data_id`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`one_id`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`work_id`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`client_id`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`channel_id`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`content_type`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`sample_data_type`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`original_id`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`input_data_id`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`original_text_scene`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`brand_code`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`car_series_code`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`label_type`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`sentiment`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`intention_type`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`topic`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`opinion`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`subject`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`fault_level`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`description`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`sentiment_score`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`keywords`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`model_type`
         ,from_base64(`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`raw_data`) as `raw_data`
         ,from_base64(`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`ext_fields`) as `ext_fields`
         ,from_base64(`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`biz_ext_attrs`) as `biz_ext_attrs`
         ,from_base64(`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`biz_ext_attrs2`) as `biz_ext_attrs2`
         ,from_base64(`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`biz_ext_attrs3`) as `biz_ext_attrs3`
         ,from_base64(`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`cust_ext_attrs`) as `cust_ext_attrs`
         ,from_base64(`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`vhl_ext_attrs`) as `vhl_ext_attrs`
         ,from_base64(`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`dealer_ext_attrs`) as `dealer_ext_attrs`
         ,from_base64(`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`prd_ext_attrs`) as `prd_ext_attrs`
         -- ,from_base64(tags_ext_attrs) as tags_ext_attrs
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`create_time`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`update_time`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`abandon`
         ,`internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`done`
    from `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`
    where  `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`topic` is not null and `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`channel_id` is not null
       and `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`content_type` is not null
),
`brand_data` as (
       select `internal`.`voc_ms_td`.`ins_brand_info`.`code` as `brand_code`,`internal`.`voc_ms_td`.`ins_brand_info`.`name` as `brand_name`,`internal`.`voc_ms_td`.`ins_brand_info`.`competitive_type` as `competitive_type` ,`internal`.`voc_ms_td`.`ins_brand_info`.`automark` as `automark` from `internal`.`voc_ms_td`.`ins_brand_info`
),
`car_series_data` as (
       select  `internal`.`voc_ms_td`.`ins_car_series_info`.`code` as `car_series_code`, `internal`.`voc_ms_td`.`ins_car_series_info`.`name` as `car_series_name` ,`internal`.`voc_ms_td`.`ins_car_series_info`.`alias`,`internal`.`voc_ms_td`.`ins_car_series_info`.`exclusion_words`,`internal`.`voc_ms_td`.`ins_car_series_info`.`factory`,`internal`.`voc_ms_td`.`ins_car_series_info`.`competitive_type`,`internal`.`voc_ms_td`.`ins_car_series_info`.`competitive_product` from `internal`.`voc_ms_td`.`ins_car_series_info`
)
select
    `f`.`id`	-- 声音ID
     ,`f`.`data_id`	-- 数据唯一标识
     ,`internal`.`voc_ms_td`.`d4`.`channel_catagory_level1`  as `channel_catagory`	-- 类别（长视频、社交媒体、资讯类等）		优化-渠道数据
     ,`f`.`channel_id`	as `channel_code`-- 渠道编码
     ,`internal`.`voc_ms_td`.`d4`.`name`  as `channel_name`	-- 渠道名称		优化-品牌车系数据
     ,`f`.`brand_code`	-- 品牌编码
     ,`d3`.`brand_name`	-- 品牌名称		优化-品牌车系数据
     ,`d5`.`car_series_code`	-- 车系代码	biz_ext_attrs
     ,`d5`.`car_series_name` as `car_series_name` -- 车系名称
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.model") as `model_name`	-- 车型代码	优化（需要和鹏飞确认）
     ,`f`.`content_type`	-- 数据类型(1：咨询/2：意⻅反馈/3：帖⼦评论/4：问卷/5：⼯单)
     ,  CASE
            WHEN `f`.`channel_id` = "pdt_order_awtrxfw" THEN "-"  -- 符合条件时显示 "-"
            ELSE JSON_EXTRACT_STRING(`f`.`raw_data`, "$.title")  -- 其他情况提取 JSON 中的 title
    END AS `title`
     ,`f`.`sentiment`	-- 情感
     ,`f`.`intention_type` as `intention`	-- 意图（表扬/建议/咨询/抱怨)）
     ,to_date(`f`.`publish_time`) as `data_create_time`	-- 数据产生时间
     ,`f`.`publish_time`
     ,`f`.`create_time`	-- 数据抓取时间
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.is_outer") AS `is_outer`	-- 内外数据？
     ,null as `hot_word`	-- 热词   优化（模型无返回值）
     ,`f`.`keywords`	-- 关键词
     ,`f`.`original_text_scene`  -- 声音片段
     ,JSON_EXTRACT_STRING(`f`.`raw_data`, "$.market_id") as `market_id`	-- 细分市场ID
     -- ,JSON_EXTRACT_STRING(f.biz_ext_attrs2, "$.competitive_type") as competitive_type -- 竞争力类型（1：传统 Competitive、2：新兴 Competitive、3：其他 Competitive）
     -- ,JSON_EXTRACT_STRING(f.biz_ext_attrs2, "$.series_factory") as series_factory -- 车系所属企业
     ,`d3`.`competitive_type` as `competitive_type`-- 车系所属企业
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
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.view_count") as `view_count`	-- 浏览量or播放量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.comment_count") as `comment_count`	-- 评论量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.like_count") as `like_count`	-- 点赞量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.share_count") as `share_count`	-- 转发量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.favorite_count") as `favorite_count` 	-- 收藏量
     ,JSON_EXTRACT_STRING(`f`.`biz_ext_attrs2`, "$.work_order_id") as `work_order_id`	-- 工单ID
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
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_first` as `vtr_tag_first` -- VRT标签1级
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_second` as `vtr_tag_second` -- VRT标签2级
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_three` as `vtr_tag_three` -- VRT标签3级
     ,`internal`.`voc_ms_td`.`d6`.`vtr_tag_four` as `vtr_tag_four` -- VRT标签4级
     , `f`.`abandon` as `abandon`
from base_data as `f`
         left join brand_data as `d3` on `f`.`brand_code` = `d3`.`brand_code`
         left join `internal`.`voc_ms_td`.`ads_voc_channel_m_full_mv` as `d4` on `f`.`channel_id` = `internal`.`voc_ms_td`.`d4`.`code`
         left join car_series_data as `d5` on `f`.`car_series_code` = `d5`.`car_series_code`
         left join `internal`.`voc_ms_td`.`ads_voc_tags_system_info_h_full_mv` as `d6` on `f`.`topic` = `internal`.`voc_ms_td`.`d6`.`topic`;

CREATE MATERIALIZED VIEW voc_anal_flow_mate_data_labeled_v
(id,create_time,content_type,data_create_time,data_update_time,data_id,channel_code,brand,series,model,is_outer,one_id,id_car_no,mobile,email,global_id,user_id,user_name,vhl_id,vhl_vin,dlr_id,dlr_code,dlr_type,market_id,title,content,is_wsater_army,weight,attrs,attrs2,attrs3,work_id,done,model_type,ds)
BUILD IMMEDIATE REFRESH AUTO ON SCHEDULE EVERY 5 MINUTE STARTS "2025-11-04 19:50:00"
DUPLICATE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 3
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
"group_commit_data_bytes" = "134217728"
)
AS SELECT
              `internal`.`voc_ms_td`.`f`.`id`,
              `internal`.`voc_ms_td`.`f`.`create_time`,
              `internal`.`voc_ms_td`.`f`.`content_type`,
              `internal`.`voc_ms_td`.`f`.`publish_time` AS `data_create_time`,
              NULL AS `data_update_time`,
              `internal`.`voc_ms_td`.`f`.`data_id`,
              `internal`.`voc_ms_td`.`f`.`channel_id` AS `channel_code`,
              `internal`.`voc_ms_td`.`f`.`brand_code` AS `brand`,
              `internal`.`voc_ms_td`.`f`.`car_series_code` AS `series`,
              JSON_EXTRACT_STRING(FROM_BASE64(`internal`.`voc_ms_td`.`f`.`raw_data`), "$.model") AS `model`,
              JSON_EXTRACT_STRING(FROM_BASE64(`internal`.`voc_ms_td`.`f`.`raw_data`), "$.is_outer") AS `is_outer`,
              `internal`.`voc_ms_td`.`f`.`one_id`,
              NULL AS `id_car_no`,
              JSON_EXTRACT_STRING(FROM_BASE64(`internal`.`voc_ms_td`.`f`.`cust_ext_attrs`), "$.mobile") AS `mobile`,
              NULL AS `email`,
              NULL AS `global_id`,
              NULL AS `user_id`,
              JSON_EXTRACT_STRING(FROM_BASE64(`internal`.`voc_ms_td`.`f`.`raw_data`), "$.user_name") AS `user_name`,
              NULL AS `vhl_id`,
              JSON_EXTRACT_STRING(FROM_BASE64(`internal`.`voc_ms_td`.`f`.`vhl_ext_attrs`), "$.vin") AS `vhl_vin`,
              NULL AS `dlr_id`,
              JSON_EXTRACT_STRING(FROM_BASE64(`internal`.`voc_ms_td`.`f`.`dealer_ext_attrs`), "$.dlr_cd") AS `dlr_code`,
              NULL AS `dlr_type`,
              NULL AS `market_id`,
              CASE
                  WHEN `internal`.`voc_ms_td`.`f`.`channel_id` = "pdt_order_awtrxfw" THEN "-"  -- 符合条件时显示 "-"
                  ELSE JSON_EXTRACT_STRING(FROM_BASE64(`internal`.`voc_ms_td`.`f`.`raw_data`), "$.title")  -- 其他情况提取 JSON 中的 title
                  END AS `title`,
              JSON_EXTRACT_STRING(FROM_BASE64(`internal`.`voc_ms_td`.`f`.`raw_data`), "$.content") AS `content`,
              JSON_EXTRACT_STRING(FROM_BASE64(`internal`.`voc_ms_td`.`f`.`raw_data`), "$.is_wsater_army") as `is_wsater_army`,
              '0' AS `weight`,
              FROM_BASE64(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs`) AS `attrs`,
              FROM_BASE64(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs2`) AS `attrs2`,
              FROM_BASE64(`internal`.`voc_ms_td`.`f`.`biz_ext_attrs3`) AS `attrs3`,
              `internal`.`voc_ms_td`.`f`.`work_id`,
              `internal`.`voc_ms_td`.`f`.`done`,
              `internal`.`voc_ms_td`.`f`.`model_type`,
              '99999' AS `ds`
   FROM (
            SELECT `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`id`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`publish_time`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`data_id`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`one_id`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`work_id`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`client_id`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`channel_id`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`content_type`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`sample_data_type`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`original_id`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`input_data_id`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`original_text_scene`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`brand_code`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`car_series_code`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`label_type`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`scenario`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`sentiment`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`intention_type`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`topic`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`opinion`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`subject`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`fault_level`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`description`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`sentiment_score`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`keywords`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`model_type`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`raw_data`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`ext_fields`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`biz_ext_attrs`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`biz_ext_attrs2`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`biz_ext_attrs3`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`cust_ext_attrs`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`vhl_ext_attrs`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`dealer_ext_attrs`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`prd_ext_attrs`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`tags_ext_attrs`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`create_time`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`update_time`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`abandon`, `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`done`,
                   ROW_NUMBER() OVER (PARTITION BY `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`data_id` ORDER BY `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`.`create_time` DESC) AS `rn`
            FROM `internal`.`voc_ms_td`.`ads_voc_model_tags_result_data_m_inc`
        ) `f`
   WHERE `f`.`rn` = 1
   order by `internal`.`voc_ms_td`.`f`.`create_time` desc;






drop catalog if exists voc_jdbc;


show  catalogs;
# 生产
CREATE CATALOG mysql_voc properties(
'type' = 'jdbc',
'user' = 'root',
'password' = 'xxxx',
'jdbc_url' = 'jdbc:mysql://10.63.8.125:33306',
'driver_url' = 'mysql-connector-java-8.0.25.jar',
'driver_class' = 'com.mysql.cj.jdbc.Driver'
);


# 测试
CREATE CATALOG mysql_voc properties(
'type' = 'jdbc',
'user' = 'root',
'password' = 'xxxxxxx',
'jdbc_url' = 'jdbc:mysql://10.62.133.17:33306',
'driver_url' = 'mysql-connector-java-8.0.25.jar',
'driver_class' = 'com.mysql.cj.jdbc.Driver'
);



# 开发
CREATE CATALOG mysql_voc properties(
'type' = 'jdbc',
'user' = 'root',
'password' = 'L7bzd1gmm+db',
'jdbc_url' = 'jdbc:mysql://172.16.80.17:30799',
'driver_url' = 'https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.25/mysql-connector-java-8.0.25.jar',
'driver_class' = 'com.mysql.cj.jdbc.Driver'
);


# 开发
CREATE CATALOG voc_mysql_jdbc properties(
'type' = 'jdbc',
'user' = 'root',
'password' = 'L7bzd1gmm+db',
'jdbc_url' = 'jdbc:mysql://172.16.80.17:30799',
'driver_url' = 'https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.25/mysql-connector-java-8.0.25.jar',
'driver_class' = 'com.mysql.cj.jdbc.Driver'
);


WITH RECURSIVE dept_hierarchy AS (
    -- 从三级部门开始，同时记录根部门ID
    SELECT id, name, parent_id, id as root_dept_id,code
    FROM sta_sys_depart
    WHERE id IN (
        SELECT d3.id
        FROM sta_sys_depart d1
                 JOIN sta_sys_depart d2 ON d1.id = d2.parent_id
                 JOIN sta_sys_depart d3 ON d2.id = d3.parent_id
        WHERE d1.parent_id = '-1'
    )

    UNION ALL

    -- 递归查询所有子部门，保持根部门ID
    SELECT d.id, d.name, d.parent_id, dh.root_dept_id,d.code
    FROM sta_sys_depart d
             JOIN dept_hierarchy dh ON d.parent_id = dh.id
)
-- 按三级部门分组显示用户
SELECT
    dh.root_dept_id as depId,
    (SELECT name FROM sta_sys_depart WHERE id = dh.root_dept_id) as deptName,
    du.user_id as userId
FROM sta_sys_user_depart du
         JOIN dept_hierarchy dh ON du.dep_id = dh.code
ORDER BY dh.root_dept_id, du.user_id




SELECT
    su.firstname as '名字',
    su.username as '工号',
    dp.l1_name as '部门1',
    dp.l2_name as '部门2',
    dp.l3_name as '部门3',
    dp.l4_name as '部门4',
    dp.l5_name as '部门5',
    dp.l6_name as '部门6',
    dp.l7_name as '部门7',
    rubr.browse_user_id,
    COUNT(rubr.sound_id) AS countf,
    CASE
        WHEN FLOOR(IFNULL(SUM(browse_duration), 0)/60) = 0 THEN CONCAT(IFNULL(SUM(browse_duration), 0), '秒')
        ELSE CONCAT(FLOOR(IFNULL(SUM(browse_duration), 0)/60), '分', IFNULL(SUM(browse_duration), 0)%60, '秒')
        END AS browseDuration
FROM
    mysql_voc.voc_ms_be.sys_users su
   left join  mysql_voc.voc_ms_td.sta_sys_user_depart sd ON sd.user_id = su.username
    left join mysql_voc.voc_ms_td.sta_all_level_depart_10_v dp ON sd.dep_id = dp.code
        LEFT JOIN report_user_browse_record rubr ON su.id = rubr.browse_user_id AND rubr.sound_id IS NOT NULL
WHERE
    su.username IS NOT NULL
--     AND su.id='47f49f8c7659efe14439a6790fa764fa'
GROUP BY su.firstname, su.username,dp.l1_name,dp.l2_name,dp.l3_name, dp.l4_name,  dp.l5_name, dp.l6_name,dp.l7_name, rubr.browse_user_id
ORDER BY countf DESC





-- voc_ms_td.ads_voc_model_tags_result_data_m_inc definition
TRUNCATE TABLE ads_voc_model_tags_result_data_m_inc;

alter table report_high_quality_record
    add COLUMN  original_id varchar(50000) null comment '原文id';


alter table report_user_browse_record
    add COLUMN  original_id varchar(50000) null comment '原文id';

alter table report_user_browse_record
    add COLUMN  sound_id varchar(64) null comment '声音id';


ALTER TABLE report_user_browse_record
    MODIFY COLUMN sound_id varchar(64) NULL COMMENT '声音id';

ALTER TABLE report_high_quality_record
    MODIFY COLUMN sound_id varchar(64) NULL COMMENT '声音id';






CREATE TABLE `ads_voc_model_tags_result_data_m_inc` (
                                                        `data_id` varchar(50) NOT NULL COMMENT "业务主键id",
                                                        `channel_id` varchar(50) NOT NULL COMMENT "渠道标识",
                                                        `topic` varchar(100) NULL COMMENT "聚合后的观点=>标签叶子结点",
                                                        `intention_type` varchar(50) NULL COMMENT "用户意图",
                                                        `sentiment` varchar(50) NULL COMMENT "情感倾向",
                                                        `publish_time` datetime NOT NULL COMMENT "发布时间（用于按天分区）",
                                                        `id` varchar(50) NOT NULL COMMENT "主键id",
                                                        `one_id` text NULL COMMENT "唯一Id",
                                                        `work_id` text NULL COMMENT "接收处理标识",
                                                        `scenario` text NULL COMMENT "用车场景",
                                                        `client_id` text NULL COMMENT "客户标识",
                                                        `content_type` text NULL COMMENT "内容类型：文本：text、 工单：order",
                                                        `sample_data_type` text NULL COMMENT "是否是示例数据",
                                                        `original_id` text NULL COMMENT "原文id",
                                                        `input_data_id` text NULL COMMENT "原文关联id",
                                                        `original_text_scene` text NULL COMMENT "原文片段",
                                                        `brand_code` text NULL COMMENT "品牌名称",
                                                        `car_series_code` text NULL COMMENT "车系名称",
                                                        `label_type` text NULL COMMENT "标签类型：1服务 2产品 3品质",
                                                        `opinion` text NULL COMMENT "原始观点",
                                                        `subject` text NULL COMMENT "评价主体【如：雨刮器】",
                                                        `fault_level` text NULL COMMENT "故障问题严重性等级",
                                                        `description` text NULL COMMENT "描述/评价内容",
                                                        `sentiment_score` text NULL COMMENT "情感严重程度",
                                                        `keywords` text NULL COMMENT "提取的热词",
                                                        `model_type` int NULL COMMENT "模型类型：1 智谱AI离线 2智谱AI实时 3聚类大模型",
                                                        `raw_data` text NULL COMMENT "原始数据（未处理前，）",
                                                        `ext_fields` text NULL COMMENT "通用扩展字段",
                                                        `biz_ext_attrs` text NULL COMMENT "业务扩展字段1",
                                                        `biz_ext_attrs2` text NULL COMMENT "业务扩展字段2",
                                                        `biz_ext_attrs3` text NULL COMMENT "业务扩展字段3",
                                                        `cust_ext_attrs` text NULL COMMENT "客户信息扩展字段",
                                                        `vhl_ext_attrs` text NULL COMMENT "车辆信息扩展字段",
                                                        `dealer_ext_attrs` text NULL COMMENT "经销商信息扩展字段",
                                                        `prd_ext_attrs` text NULL COMMENT "产品经销商信息扩展字段",
                                                        `tags_ext_attrs` text NULL COMMENT "标签产品经销商信息扩展字段",
                                                        `create_time` datetime NOT NULL COMMENT "记录创建时间",
                                                        `update_time` datetime NOT NULL COMMENT "记录更新时间",
                                                        `abandon` text NULL COMMENT "是否完成计算：是=1，否=0",
                                                        `done` int NULL COMMENT "是否完成计算（整型）：是=1，否=0"
) ENGINE=OLAP
    UNIQUE KEY(`data_id`, `channel_id`, `topic`, `intention_type`, `sentiment`, `publish_time`)
COMMENT 'VOC-ODS数据模型处理结果表'
PARTITION BY RANGE(`publish_time`)
(PARTITION p202310 VALUES [('2023-10-01 00:00:00'), ('2023-11-01 00:00:00')),
PARTITION p202311 VALUES [('2023-11-01 00:00:00'), ('2023-12-01 00:00:00')),
PARTITION p202312 VALUES [('2023-12-01 00:00:00'), ('2024-01-01 00:00:00')),
PARTITION p202401 VALUES [('2024-01-01 00:00:00'), ('2024-02-01 00:00:00')),
PARTITION p202402 VALUES [('2024-02-01 00:00:00'), ('2024-03-01 00:00:00')),
PARTITION p202403 VALUES [('2024-03-01 00:00:00'), ('2024-04-01 00:00:00')),
PARTITION p202404 VALUES [('2024-04-01 00:00:00'), ('2024-05-01 00:00:00')),
PARTITION p202405 VALUES [('2024-05-01 00:00:00'), ('2024-06-01 00:00:00')),
PARTITION p202406 VALUES [('2024-06-01 00:00:00'), ('2024-07-01 00:00:00')),
PARTITION p202407 VALUES [('2024-07-01 00:00:00'), ('2024-08-01 00:00:00')),
PARTITION p202408 VALUES [('2024-08-01 00:00:00'), ('2024-09-01 00:00:00')),
PARTITION p202409 VALUES [('2024-09-01 00:00:00'), ('2024-10-01 00:00:00')),
PARTITION p202410 VALUES [('2024-10-01 00:00:00'), ('2024-11-01 00:00:00')),
PARTITION p202411 VALUES [('2024-11-01 00:00:00'), ('2024-12-01 00:00:00')),
PARTITION p202412 VALUES [('2024-12-01 00:00:00'), ('2025-01-01 00:00:00')),
PARTITION p202501 VALUES [('2025-01-01 00:00:00'), ('2025-02-01 00:00:00')),
PARTITION p202502 VALUES [('2025-02-01 00:00:00'), ('2025-03-01 00:00:00')),
PARTITION p202503 VALUES [('2025-03-01 00:00:00'), ('2025-04-01 00:00:00')),
PARTITION p202504 VALUES [('2025-04-01 00:00:00'), ('2025-05-01 00:00:00')),
PARTITION p202505 VALUES [('2025-05-01 00:00:00'), ('2025-06-01 00:00:00')),
PARTITION p202506 VALUES [('2025-06-01 00:00:00'), ('2025-07-01 00:00:00')),
PARTITION p202507 VALUES [('2025-07-01 00:00:00'), ('2025-08-01 00:00:00')),
PARTITION p202508 VALUES [('2025-08-01 00:00:00'), ('2025-09-01 00:00:00')),
PARTITION p202509 VALUES [('2025-09-01 00:00:00'), ('2025-10-01 00:00:00')),
PARTITION p202510 VALUES [('2025-10-01 00:00:00'), ('2025-11-01 00:00:00')),
PARTITION p202511 VALUES [('2025-11-01 00:00:00'), ('2025-12-01 00:00:00')))
DISTRIBUTED BY HASH(`data_id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "MONTH",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-24",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p",
"dynamic_partition.replication_allocation" = "tag.location.default: 1",
"dynamic_partition.buckets" = "4",
"dynamic_partition.create_history_partition" = "true",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"dynamic_partition.start_day_of_month" = "1",
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




delete  from report_user_browse_record;
drop table ads_voc_model_tags_result_data_m_inc_old;

ALTER TABLE ads_voc_model_tags_result_data_m_inc RENAME ads_voc_model_tags_result_data_m_inc_old;


select
    count(1)
from voc_sentiment_annotations_results_v;

-- voc_ms_td.report_display_rule definition
ALTER TABLE report_display_rule RENAME report_display_rule_old;
ALTER TABLE voc_sentiment_annotations_results_v_old RENAME voc_sentiment_annotations_results_v;
ALTER TABLE voc_sentiment_annotations_results_v RENAME voc_sentiment_annotations_results_v_old;

drop table voc_anal_flow_mate_data_labeled_v;
delete  from voc_sentiment_annotations_results_v where id is not null;


TRUNCATE TABLE voc_sentiment_annotations_results_v;

CREATE TABLE voc_anal_flow_mate_data_labeled_v LIKE voc_anal_flow_mate_data_labeled_v_tmp;
ALTER TABLE voc_anal_flow_mate_data_labeled_v_tmp RENAME voc_anal_flow_mate_data_labeled_v;
ALTER TABLE voc_anal_flow_mate_data_labeled_v_tmp RENAME voc_anal_flow_mate_data_labeled_v;

-- 复制表结构和数据
CREATE TABLE voc_sentiment_annotations_results_v LIKE voc_sentiment_annotations_results_v_old;
INSERT INTO voc_sentiment_annotations_results_v_old SELECT * FROM voc_sentiment_annotations_results_v;

-- 一条语句复制表结构和数据
CREATE TABLE voc_sentiment_annotations_results_v_old AS SELECT * FROM voc_sentiment_annotations_results_v;

CREATE TABLE voc_anal_flow_mate_data_labeled_v_old AS SELECT * FROM voc_anal_flow_mate_data_labeled_v;


TRUNCATE TABLE voc_anal_flow_mate_data_labeled_v;

-- 复制表结构和数据
CREATE TABLE voc_anal_flow_mate_data_labeled_v_old LIKE voc_anal_flow_mate_data_labeled_v;
INSERT INTO voc_anal_flow_mate_data_labeled_v_old SELECT * FROM voc_anal_flow_mate_data_labeled_v;



-- voc_ms_td.report_user_browse_record definition

CREATE TABLE `report_user_browse_record` (
                                             `id` varchar(64) NOT NULL COMMENT "主键",
                                             `sound_id` varchar(64) NOT NULL COMMENT "声音id",
                                             `original_id` varchar(64) NOT NULL COMMENT "原文id",
                                             `browse_user_id` varchar(64) NOT NULL COMMENT "浏览人id",
                                             `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间",
                                             `browse_duration` int NULL COMMENT "浏览时长(秒)",
                                             `sound_intention` varchar(64) NULL COMMENT "声音意图"
)
UNIQUE KEY(`id`)
COMMENT '用户浏览记录表'
DISTRIBUTED BY HASH(`id`) BUCKETS 1
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"enable_unique_key_merge_on_write" = "true"
);



INSERT INTO report_display_rule (id, metric_code, metric_name, range_min, range_max, color_hex, emoji_key, sort_no, status, create_time, update_time) VALUES('c92c001e043e4d58152a040074009f03', 'negativeRate', '负面率', 20.00, 40.00, '#d9f0fb', '4', 4, 1, '2025-09-05 09:54:24', '2025-09-22 16:58:55.0');
INSERT INTO report_display_rule (id, metric_code, metric_name, range_min, range_max, color_hex, emoji_key, sort_no, status, create_time, update_time) VALUES('4461f91d043e4d58388b010073009f03', 'negativeRate', '负面率', 40.00, 60.00, '#f8f3d7', '3', 3, 1, '2025-09-05 09:54:23', '2025-09-19 17:37:33.0');
INSERT INTO report_display_rule (id, metric_code, metric_name, range_min, range_max, color_hex, emoji_key, sort_no, status, create_time, update_time) VALUES('c349f01d043e4d58db45070072009f03', 'negativeRate', '负面率', 60.00, 80.00, '#eddac7', '2', 2, 1, '2025-09-05 09:54:23', '2025-09-22 18:08:16.0');
INSERT INTO report_display_rule (id, metric_code, metric_name, range_min, range_max, color_hex, emoji_key, sort_no, status, create_time, update_time) VALUES('12c2de1d043e4d58e3b60e0071009f03', 'negativeRate', '负面率', 80.00, 100.00, '#ff5959', '1', 1, 1, '2025-09-05 09:54:22', '2025-09-22 15:11:18.0');
INSERT INTO report_display_rule (id, metric_code, metric_name, range_min, range_max, color_hex, emoji_key, sort_no, status, create_time, update_time) VALUES('277ea72fe80ca6d7a11b5dcecf7c6fdb', 'negativeRate', '负面率', 0.00, 20.00, '#d7f1f1', '5', 5, 1, '2025-09-05 22:21:26.0', '2025-09-22 16:59:04.0');


CREATE TABLE `report_display_rule` (
                                       `id` varchar(64) NOT NULL COMMENT "主键",
                                       `metric_code` varchar(64) NOT NULL COMMENT "指标编码，如 NEG_RATE",
                                       `metric_name` varchar(128) NOT NULL COMMENT "指标名称",
                                       `range_min` decimal(10,2) NOT NULL COMMENT "区间下限(含)",
                                       `range_max` decimal(10,2) NOT NULL COMMENT "区间上限(含)",
                                       `color_hex` varchar(16) NOT NULL COMMENT "颜色HEX，如#FF4D4F",
                                       `emoji_key` varchar(64) NOT NULL COMMENT "表情/图标编码",
                                       `sort_no` int NULL DEFAULT "0" COMMENT "排序号",
                                       `status` tinyint NULL DEFAULT "1" COMMENT "1启用 0禁用",
                                       `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                       `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP
) UNIQUE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 1
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"enable_unique_key_merge_on_write" = "true"
);


INSERT INTO voc_ins_user_journey_level1_v (code, name, sort) VALUES('cqca1001', '推荐', 4);
INSERT INTO voc_ins_user_journey_level1_v (code, name, sort) VALUES('cqca1004', '选择', 2);
INSERT INTO voc_ins_user_journey_level1_v (code, name, sort) VALUES('cqca1002', '认知', 1);
INSERT INTO voc_ins_user_journey_level1_v (code, name, sort) VALUES('cqca1003', '购买', 3);




CREATE TABLE report_special_analysis_type
(
    id          VARCHAR(64)  COMMENT 'ID', -- UNIQUE KEY 列，不带 NOT NULL
    name        VARCHAR(150) NOT NULL COMMENT '名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    create_by   VARCHAR(100) NULL COMMENT '创建人',
    update_by   VARCHAR(100) NULL COMMENT '修改人',
    del_flag    TINYINT  DEFAULT 0 COMMENT '0正常,1已删除',
    description VARCHAR(255) NULL COMMENT '描述',
    sort_no     INT      DEFAULT 0 COMMENT '排序(越小越靠前)',
    icon        VARCHAR(255) NULL COMMENT '图标(名称或URL)',
    pid         VARCHAR(64)  NULL COMMENT '父id',
    type        INT          NULL COMMENT '类型：1一级，2二级',
    enabled     INT      DEFAULT 1 COMMENT '是否启用'
) UNIQUE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 1
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"enable_unique_key_merge_on_write" = "true"
);


create table report_special_analysis_role
(
    id          varchar(64) not null,
    category_id varchar(64) not null,
    role_id     varchar(64) not null
) UNIQUE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 1
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"enable_unique_key_merge_on_write" = "true"
);


create table report_custom_report
(
    id                  varchar(64)                        not null comment 'ID',
    report_name         varchar(250)                       not null comment '报告名称',
    view_count          bigint   default 0                 comment '浏览数',
    collection_count    bigint   default 0                 comment '收藏量',
    type                tinyint                            not null comment '类型 1:voc, 2:智能问数',
    default_condition   varchar(65533)                     null comment '默认条件(JSON字符串)',
    brand_code          varchar(100)                       null comment '品牌编码',
    special_type_id     varchar(64)                        null comment '专项类型ID',
    status              tinyint  default 0                 comment '状态 0未发布,1已发布',
    create_time         datetime default CURRENT_TIMESTAMP  comment '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    create_by           varchar(100)                       null comment '创建人',
    update_by           varchar(100)                       null comment '修改人',
    del_flag            tinyint  default 0                 comment '0正常,1已删除',
    description         varchar(255)                       null comment '描述',
    first_level_zone_id varchar(100)                       null comment '专区分类1',
    date_condition      varchar(65533)                     null comment '时间json',
    report_url          varchar(100)                       null comment '报告地址'
) UNIQUE KEY(`id`)
DISTRIBUTED BY HASH(`id`) BUCKETS 1
PROPERTIES (
    "replication_allocation" = "tag.location.default: 1",
    "enable_unique_key_merge_on_write" = "true"
);








ALTER TABLE voc_sentiment_annotations_results_v RENAME voc_sentiment_annotations_results_v_old;
alter table voc_sentiment_annotations_results_v
    add COLUMN  title varchar(50000) null comment '标题';



# 根据提供的视图DDL，我将为您生成对应的建表DDL，包含所有字段和中文注释：

CREATE TABLE voc_sentiment_annotations_results_v (
                                      id varchar(40000) NULL COMMENT "主键ID",
                                      data_id varchar(40000) NULL COMMENT "数据ID",
                                      channel_catagory varchar(40000) NULL COMMENT "渠道分类",
                                      channel_code varchar(40000) NULL COMMENT "渠道编码",
                                      channel_name varchar(40000) NULL COMMENT "渠道名称",
                                      brand_code varchar(40000) NULL COMMENT "品牌编码",
                                      brand_name varchar(40000) NULL COMMENT "品牌名称",
                                      car_series_code varchar(40000) NULL COMMENT "车系编码",
                                      car_series_name varchar(40000) NULL COMMENT "车系名称",
                                      model_name varchar(40000) NULL COMMENT "车型名称",
                                      content_type varchar(40000) NULL COMMENT "内容类型",
                                      sentiment varchar(40000) NULL COMMENT "情感倾向",
                                      intention varchar(40000) NULL COMMENT "意图",
                                      data_create_time datetime NULL COMMENT "数据创建时间",
                                      publish_time datetime NULL COMMENT "发布时间",
                                      create_time datetime NULL COMMENT "创建时间",
                                      is_outer varchar(10000) NULL COMMENT "是否外部数据",
                                      hot_word varchar(50000) NULL COMMENT "热词",
                                      keywords varchar(50000) NULL COMMENT "关键词",
                                      original_text_scene varchar(50000) NULL COMMENT "原始文本场景",
                                      market_id varchar(40000) NULL COMMENT "市场ID",
                                      competitive_type varchar(40000) NULL COMMENT "竞争类型",
                                      series_factory varchar(40000) NULL COMMENT "系列工厂",
                                      user_journey1 varchar(40000) NULL COMMENT "用户旅程1",
                                      user_journey2 varchar(40000) NULL COMMENT "用户旅程2",
                                      user_journey3 varchar(40000) NULL COMMENT "用户旅程3",
                                      scenario varchar(40000) NULL COMMENT "场景",
                                      d2c_responsible_dept varchar(40000) NULL COMMENT "D2C责任部门",
                                      d2c_accountable_dept varchar(40000) NULL COMMENT "D2C负责部门",
                                      d2c_cc_dept varchar(40000) NULL COMMENT "D2C抄送部门",
                                      one_id varchar(40000) NULL COMMENT "唯一ID",
                                      cust_global_id varchar(40000) NULL COMMENT "客户全局ID",
                                      cust_classify varchar(40000) NULL COMMENT "客户分类",
                                      cust_main_phone varchar(40000) NULL COMMENT "客户主电话",
                                      is_car_owner varchar(10000) NULL COMMENT "是否车主",
                                      cust_age int NULL COMMENT "客户年龄",
                                      cust_age_group varchar(40000) NULL COMMENT "客户年龄段",
                                      cust_name varchar(40000) NULL COMMENT "客户姓名",
                                      cust_gender varchar(10000) NULL COMMENT "客户性别",
                                      cust_high_educaion varchar(40000) NULL COMMENT "客户最高学历",
                                      marrige_statue varchar(40000) NULL COMMENT "婚姻状况",
                                      family_income varchar(40000) NULL COMMENT "家庭收入",
                                      is_exchange_flg varchar(10000) NULL COMMENT "是否置换标志",
                                      purchase_car_times int NULL COMMENT "购车次数",
                                      is_member_flg varchar(10000) NULL COMMENT "是否会员标志",
                                      cust_province_code varchar(40000) NULL COMMENT "客户省份编码",
                                      cust_province varchar(40000) NULL COMMENT "客户省份",
                                      cust_city_code varchar(40000) NULL COMMENT "客户城市编码",
                                      cust_city varchar(40000) NULL COMMENT "客户城市",
                                      cust_type varchar(40000) NULL COMMENT "客户类型",
                                      cust_lived_prov varchar(40000) NULL COMMENT "客户居住省份",
                                      cust_lived_city varchar(40000) NULL COMMENT "客户居住城市",
                                      cust_profession varchar(40000) NULL COMMENT "客户职业",
                                      vhl_vin varchar(40000) NULL COMMENT "车辆VIN码",
                                      vhl_color_name varchar(40000) NULL COMMENT "车辆颜色名称",
                                      vhl_product_date date NULL COMMENT "车辆生产日期",
                                      vhl_offline_date date NULL COMMENT "车辆下线日期",
                                      vhl_is_abroad varchar(10000) NULL COMMENT "是否进口车辆",
                                      vhl_dis_ch varchar(40000) NULL COMMENT "车辆排放标准",
                                      vhl_dis_mt varchar(40000) NULL COMMENT "车辆排放类型",
                                      vhl_eng_clsf varchar(40000) NULL COMMENT "发动机分类",
                                      vhl_eng_seris varchar(40000) NULL COMMENT "发动机系列",
                                      vhl_veh_type varchar(40000) NULL COMMENT "车辆类型",
                                      vhl_country varchar(40000) NULL COMMENT "车辆国家",
                                      vhl_bd_clsf varchar(40000) NULL COMMENT "车身分类",
                                      vhl_seg_mt varchar(40000) NULL COMMENT "车辆细分市场",
                                      vhl_pow_clsf varchar(40000) NULL COMMENT "动力分类",
                                      vhl_fu_clsf varchar(40000) NULL COMMENT "燃料分类",
                                      vhl_modl_st varchar(40000) NULL COMMENT "车型状态",
                                      vhl_std_plnt_code varchar(40000) NULL COMMENT "标准工厂编码",
                                      dlr_oc_id varchar(40000) NULL COMMENT "经销商OC ID",
                                      dlr_oc_code varchar(40000) NULL COMMENT "经销商OC编码",
                                      dlr_oc_name varchar(40000) NULL COMMENT "经销商OC名称",
                                      dlr_oc_province_code varchar(40000) NULL COMMENT "经销商OC省份编码",
                                      dlr_oc_province varchar(40000) NULL COMMENT "经销商OC省份",
                                      dlr_oc_city_code varchar(40000) NULL COMMENT "经销商OC城市编码",
                                      dlr_oc_city varchar(40000) NULL COMMENT "经销商OC城市",
                                      dlr_dc_id varchar(40000) NULL COMMENT "经销商DC ID",
                                      dlr_dc_code varchar(40000) NULL COMMENT "经销商DC编码",
                                      dlr_dc_name varchar(40000) NULL COMMENT "经销商DC名称",
                                      dlr_dc_province_code varchar(40000) NULL COMMENT "经销商DC省份编码",
                                      dlr_dc_province varchar(40000) NULL COMMENT "经销商DC省份",
                                      dlr_dc_city_code varchar(40000) NULL COMMENT "经销商DC城市编码",
                                      dlr_dc_city varchar(40000) NULL COMMENT "经销商DC城市",
                                      dlr_mc_id varchar(40000) NULL COMMENT "经销商MC ID",
                                      dlr_mc_code varchar(40000) NULL COMMENT "经销商MC编码",
                                      dlr_mc_name varchar(40000) NULL COMMENT "经销商MC名称",
                                      dlr_mc_province_code varchar(40000) NULL COMMENT "经销商MC省份编码",
                                      dlr_mc_province varchar(40000) NULL COMMENT "经销商MC省份",
                                      dlr_mc_city_code varchar(40000) NULL COMMENT "经销商MC城市编码",
                                      dlr_mc_city varchar(40000) NULL COMMENT "经销商MC城市",
                                      is_wsater_army varchar(10000) NULL COMMENT "是否水军",
                                      is_manager_focused varchar(10000) NULL COMMENT "是否管理层关注",
                                      is_big_v varchar(10000) NULL COMMENT "是否大V",
                                      author_id varchar(40000) NULL COMMENT "作者ID",
                                      author_nick varchar(40000) NULL COMMENT "作者昵称",
                                      is_main_post varchar(10000) NULL COMMENT "是否主贴",
                                      original_link varchar(50000) NULL COMMENT "原始链接",
                                      view_count int NULL COMMENT "浏览数",
                                      comment_count int NULL COMMENT "评论数",
                                      like_count int NULL COMMENT "点赞数",
                                      share_count int NULL COMMENT "分享数",
                                      favorite_count int NULL COMMENT "收藏数",
                                      work_order_id varchar(40000) NULL COMMENT "工单ID",
                                      quest_id varchar(40000) NULL COMMENT "问卷ID",
                                      quest_type varchar(40000) NULL COMMENT "问卷类型",
                                      quest_answer_score int NULL COMMENT "问卷回答分数",
                                      quest_business_type varchar(40000) NULL COMMENT "问卷业务类型",
                                      quest_business_scenario varchar(40000) NULL COMMENT "问卷业务场景",
                                      tag_accuracy varchar(40000) NULL COMMENT "标签准确度",
                                      tag_customer_issue_classification varchar(40000) NULL COMMENT "标签客户问题分类",
                                      tag_issue_severity varchar(40000) NULL COMMENT "标签问题严重程度",
                                      tag_code_status varchar(40000) NULL COMMENT "标签代码状态",
                                      tag_business_domain varchar(40000) NULL COMMENT "标签业务领域",
                                      tag_event_clarity varchar(40000) NULL COMMENT "标签事件清晰度",
                                      tag_high_value_flag varchar(10000) NULL COMMENT "标签高价值标志",
                                      tag_complaint_flag_needing_reply varchar(10000) NULL COMMENT "标签需要回复的投诉标志",
                                      tag_complaint_flag_needing_prtv_msg varchar(10000) NULL COMMENT "标签需要公关消息的投诉标志",
                                      tag_high_quality_voc_flag varchar(10000) NULL COMMENT "标签高质量VOC标志",
                                      tag_new_energy_or_fuel varchar(10000) NULL COMMENT "标签新能源或燃油",
                                      tag_need_forvclosed_loop varchar(10000) NULL COMMENT "标签需要闭环处理",
                                      topic varchar(40000) NULL COMMENT "主题",
                                      opinion varchar(50000) NULL COMMENT "观点",
                                      topic_text varchar(50000) NULL COMMENT "主题文本",
                                      cpt_tag_first_code varchar(40000) NULL COMMENT "CPT标签1级编码",
                                      cpt_tag_second_code varchar(40000) NULL COMMENT "CPT标签2级编码",
                                      cpt_tag_three_code varchar(40000) NULL COMMENT "CPT标签3级编码",
                                      cpt_tag_four_code varchar(40000) NULL COMMENT "CPT标签4级编码",
                                      cpt_tag_first varchar(40000) NULL COMMENT "CPT标签1级",
                                      cpt_tag_second varchar(40000) NULL COMMENT "CPT标签2级",
                                      cpt_tag_three varchar(40000) NULL COMMENT "CPT标签3级",
                                      cpt_tag_four varchar(40000) NULL COMMENT "CPT标签4级",
                                      ujy_tag_first_code varchar(40000) NULL COMMENT "UJY标签1级编码",
                                      ujy_tag_second_code varchar(40000) NULL COMMENT "UJY标签2级编码",
                                      ujy_tag_three_code varchar(40000) NULL COMMENT "UJY标签3级编码",
                                      ujy_tag_four_code varchar(40000) NULL COMMENT "UJY标签4级编码",
                                      ujy_tag_first varchar(40000) NULL COMMENT "UJY标签1级",
                                      ujy_tag_second varchar(40000) NULL COMMENT "UJY标签2级",
                                      ujy_tag_three varchar(40000) NULL COMMENT "UJY标签3级",
                                      ujy_tag_four varchar(40000) NULL COMMENT "UJY标签4级",
                                      cma_tag_first_code varchar(40000) NULL COMMENT "CMA标签1级编码",
                                      cma_tag_second_code varchar(40000) NULL COMMENT "CMA标签2级编码",
                                      cma_tag_three_code varchar(40000) NULL COMMENT "CMA标签3级编码",
                                      cma_tag_four_code varchar(40000) NULL COMMENT "CMA标签4级编码",
                                      cma_tag_first varchar(40000) NULL COMMENT "CMA标签1级",
                                      cma_tag_second varchar(40000) NULL COMMENT "CMA标签2级",
                                      cma_tag_three varchar(40000) NULL COMMENT "CMA标签3级",
                                      cma_tag_four varchar(40000) NULL COMMENT "CMA标签4级",
                                      dom_tag_first_code varchar(40000) NULL COMMENT "DOM标签1级编码",
                                      dom_tag_second_code varchar(40000) NULL COMMENT "DOM标签2级编码",
                                      dom_tag_three_code varchar(40000) NULL COMMENT "DOM标签3级编码",
                                      dom_tag_four_code varchar(40000) NULL COMMENT "DOM标签4级编码",
                                      dom_tag_first varchar(40000) NULL COMMENT "DOM标签1级",
                                      dom_tag_second varchar(40000) NULL COMMENT "DOM标签2级",
                                      dom_tag_three varchar(40000) NULL COMMENT "DOM标签3级",
                                      dom_tag_four varchar(40000) NULL COMMENT "DOM标签4级",
                                      nps_tag_first_code varchar(40000) NULL COMMENT "NPS标签1级编码",
                                      nps_tag_second_code varchar(40000) NULL COMMENT "NPS标签2级编码",
                                      nps_tag_three_code varchar(40000) NULL COMMENT "NPS标签3级编码",
                                      nps_tag_four_code varchar(40000) NULL COMMENT "NPS标签4级编码",
                                      nps_tag_first varchar(40000) NULL COMMENT "NPS标签1级",
                                      nps_tag_second varchar(40000) NULL COMMENT "NPS标签2级",
                                      nps_tag_three varchar(40000) NULL COMMENT "NPS标签3级",
                                      nps_tag_four varchar(40000) NULL COMMENT "NPS标签4级",
                                      vtr_tag_first_code varchar(40000) NULL COMMENT "VTR标签1级编码",
                                      vtr_tag_second_code varchar(40000) NULL COMMENT "VTR标签2级编码",
                                      vtr_tag_three_code varchar(40000) NULL COMMENT "VTR标签3级编码",
                                      vtr_tag_four_code varchar(40000) NULL COMMENT "VTR标签4级编码",
                                      vtr_tag_first varchar(40000) NULL COMMENT "VTR标签1级",
                                      vtr_tag_second varchar(40000) NULL COMMENT "VTR标签2级",
                                      vtr_tag_three varchar(40000) NULL COMMENT "VTR标签3级",
                                      vtr_tag_four varchar(40000) NULL COMMENT "VTR标签4级"
) ENGINE=OLAP
    DUPLICATE KEY(`id`)
COMMENT 'voc所有声音数据宽表（可更新）'
PARTITION BY RANGE(`data_create_time`)
(PARTITION p2023_Q1 VALUES [('0000-01-01'), ('2023-04-01')),
PARTITION p2023_Q2 VALUES [('2023-04-01'), ('2023-07-01')),
PARTITION p2023_Q3 VALUES [('2023-07-01'), ('2023-10-01')),
PARTITION p2023_Q4 VALUES [('2023-10-01'), ('2024-01-01')),
PARTITION p2024_Q1 VALUES [('2024-01-01'), ('2024-04-01')),
PARTITION p2024_Q2 VALUES [('2024-04-01'), ('2024-07-01')),
PARTITION p2024_Q3 VALUES [('2024-07-01'), ('2024-10-01')),
PARTITION p2024_Q4 VALUES [('2024-10-01'), ('2025-01-01')),
PARTITION p2025_Q1 VALUES [('2025-01-01'), ('2025-04-01')),
PARTITION p2025_Q2 VALUES [('2025-04-01'), ('2025-07-01')),
PARTITION p2025_Q3 VALUES [('2025-07-01'), ('2025-10-01')),
PARTITION p2025_Q4 VALUES [('2025-10-01'), ('2026-01-01')),
PARTITION p2026_Q1 VALUES [('2026-01-01'), ('2026-04-01')),
PARTITION p2026_Q2 VALUES [('2026-04-01'), ('2026-07-01')),
PARTITION p2026_Q3 VALUES [('2026-07-01'), ('2026-10-01')),
PARTITION p2026_Q4 VALUES [('2026-10-01'), ('2027-01-01')))
DISTRIBUTED BY HASH(`id`, `data_create_time`) BUCKETS 10
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V2",
"compression" = "LZ4",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728"
);


