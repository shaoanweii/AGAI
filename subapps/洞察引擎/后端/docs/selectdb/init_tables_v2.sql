-- SHOW DATA SKEW FROM ads_voc_model_tags_result_data_m_inc;

-- select date_trunc('2023-4-05 19:28:30', 'week');


-- max.request.size=10485880

-- drop table voc_anal_flow_model_tags_result_data_full
-- max.request.size=10485880
-- drop table voc_anal_flow_model_tags_result_data_full
CREATE TABLE IF NOT EXISTS voc_anal_flow_model_tags_result_data_full (
                                                                         `id` varchar(40) NOT NULL COMMENT "主键id",
                                                                         `publish_time` DATETIME NOT NULL COMMENT "发布时间（用于按天分区）",
                                                                         `data_id` varchar(40) NOT NULL COMMENT "业务主键id",
                                                                         `one_id` varchar(64) COMMENT "唯一Id",
                                                                         `work_id` varchar(40)   COMMENT "接收处理标识",
                                                                         `client_id` varchar(40)   COMMENT "客户标识",
                                                                         `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
                                                                         `content_type` varchar(40) NOT NULL  COMMENT "内容类型：文本：text、 工单：order",
                                                                         `sample_data_type` varchar(40) COMMENT "是否是示例数据",
                                                                         `original_id` varchar(40) COMMENT "原文id",
                                                                         `input_data_id` varchar(40) COMMENT "原文关联id",
                                                                         `original_text_scene` STRING COMMENT "原文片段",
                                                                         `brand_code` varchar(40) COMMENT "品牌名称",
                                                                         `car_series_code` varchar(40) COMMENT "车系名称",
                                                                         `label_type` varchar(10) COMMENT "标签类型：1服务 2产品 3品质",
                                                                         `scenario` varchar(128) COMMENT "用车场景",
                                                                         `sentiment` varchar(40) COMMENT "情感倾向",
                                                                         `intention_type` varchar(40) COMMENT "用户意图",
                                                                         `topic` varchar(40) COMMENT "聚合后的观点=>标签叶子结点",
                                                                         `opinion` STRING COMMENT "原始观点",
                                                                         `subject` STRING COMMENT "评价主体【如：雨刮器】",
                                                                         `fault_level` STRING COMMENT "故障问题严重性等级",
                                                                         `description` STRING COMMENT "描述/评价内容",
                                                                         `sentiment_score` varchar(40) COMMENT "情感严重程度",
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
                                                                         `abandon` int(11) NULL COMMENT "是否遗弃数据 是：1，否：0",
                                                                         `done` INT COMMENT "是否完成计算（整型）：是=1，否=0",
                                                                         `insert_dt` datetime not null default CURRENT_TIMESTAMP
)
UNIQUE   KEY(id,publish_time)
COMMENT 'VOC-ODS数据模型处理结果表'
AUTO PARTITION BY RANGE (date_trunc(publish_time, 'month'))()
DISTRIBUTED BY HASH (`id`) BUCKETS 32
PROPERTIES (
    "replication_num" = "3",
    "bloom_filter_columns" = "brand_code, car_series_code, channel_id,topic,insert_dt"
)
;


--  drop table voc_imp_hudi_dm_voc_cust_vehicle_rel
CREATE TABLE IF NOT EXISTS `voc_imp_hudi_dm_voc_cust_vehicle_rel` (
                                                                      `vin` VARCHAR(200) NOT NULL COMMENT '车架号',
    `idcard` STRING COMMENT '证件号',
    `w_insert_dt` STRING COMMENT '数据插入时间',
    `w_update_dt` STRING COMMENT '数据更新时间',
    `insert_dt` datetime not null default CURRENT_TIMESTAMP,
    `batch_dt` STRING COMMENT '任务名称',
    `job_name` STRING COMMENT '任务名称'
    )
DUPLICATE KEY(`vin`)
COMMENT 'dm_voc_cust_vehicle_rel'
AUTO PARTITION BY RANGE (date_trunc(`insert_dt`, 'day'))
DISTRIBUTED BY HASH(`vin`) BUCKETS 4
PROPERTIES (
           "replication_allocation" = "tag.location.default: 3",
           "min_load_replica_num" = "-1",
           "bloom_filter_columns" = "idcard, vin",
           "is_being_synced" = "false",
           "dynamic_partition.enable" = "true",
           "dynamic_partition.time_unit" = "DAY",
           "dynamic_partition.time_zone" = "Asia/Shanghai",
           "dynamic_partition.start" = "-7",
           "dynamic_partition.end" = "1",
           "dynamic_partition.prefix" = "p",
);

-- voc_ms_td.voc_imp_hudi_dm_voc_cust definition


-- voc_ms_td.voc_anal_flow_mate_data_full_ext definition

CREATE TABLE `voc_anal_flow_mate_data_full_ext` (
                                                    `id` varchar(40) NOT NULL COMMENT "主键ID",
                                                    `data_create_time` datetime NOT NULL COMMENT "数据产生时间",
                                                    `data_update_time` datetime NULL COMMENT "数据更新时间",
                                                    `create_time` datetime NOT NULL COMMENT "数据抓取时间",
                                                    `content_type` varchar(40) NULL COMMENT "内容类型：order(工单), post(帖子评论), opinion(意见反馈), questionnaire(问卷), consult(咨询)",
                                                    `data_id` varchar(40) NULL COMMENT "数据唯一标识",
                                                    `client_id` varchar(40) NULL COMMENT "客户标识",
                                                    `work_id` varchar(40) NULL COMMENT "客户标识",
                                                    `channel_code` varchar(40) NULL COMMENT "渠道编码",
                                                    `brand` text NULL COMMENT "品牌",
                                                    `series` text NULL COMMENT "车系",
                                                    `model` text NULL COMMENT "原始观点",
                                                    `is_outer` varchar(4) NULL COMMENT "是否往外数据",
                                                    `one_id` text NULL COMMENT "股份客户信息-one_id",
                                                    `id_car_no` text NULL COMMENT "客户证件号",
                                                    `mobile` text NULL COMMENT "客户手机号",
                                                    `email` text NULL COMMENT "客户邮箱",
                                                    `global_id` text NULL COMMENT "SSO全局ID",
                                                    `user_id` text NULL COMMENT "用户标识",
                                                    `user_name` text NULL COMMENT "用户名",
                                                    `vhl_id` text NULL COMMENT "车辆ID",
                                                    `vhl_vin` text NULL COMMENT "车辆车架号",
                                                    `dlr_id` text NULL COMMENT "股份售后经销商ID",
                                                    `dlr_code` text NULL COMMENT "股份售后经销商编码",
                                                    `dlr_type` text NULL COMMENT "股份售后经销商类型",
                                                    `market_id` text NULL COMMENT "股份产品物理编码",
                                                    `title` text NULL COMMENT "标题",
                                                    `content` text NULL COMMENT "内容正文",
                                                    `is_wsater_army` varchar(4) NULL COMMENT "是否水军",
                                                    `weight` int NULL COMMENT "权重值",
                                                    `attrs` json NULL COMMENT "业务系统其他字段",
                                                    `attrs2` json NULL COMMENT "业务系统其他字段",
                                                    `attrs3` json NULL COMMENT "业务系统其他字段",
                                                    `cust_ext_attrs` json NULL COMMENT "客户信息扩展字段",
                                                    `vhl_ext_attrs` json NULL COMMENT "车辆信息扩展字段",
                                                    `dealer_ext_attrs` json NULL COMMENT "经销商信息扩展字段",
                                                    `prd_ext_attrs` json NULL COMMENT "产品经销商信息扩展字段",
                                                    `done` int NULL COMMENT "是否完成计算：1-是，0-否",
                                                    `model_type` int NULL COMMENT "模型类型",
                                                    `data_status` int NULL COMMENT "数据状态",
                                                    `ds` varchar(20) NULL COMMENT "分区字段（备用）",
                                                    `abandon` int NULL COMMENT "分区字段（备用）",
                                                    `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=OLAP
DUPLICATE KEY(`id`)
COMMENT 'VOC-ODS原始数据记录表（优化版）'
AUTO PARTITION BY RANGE (date_trunc(`data_create_time`, 'month'))()
DISTRIBUTED BY HASH(`id`) BUCKETS 32
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "is_outer, insert_dt, data_id, channel_code, done",
"is_being_synced" = "false",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"compression" = "ZSTD",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728"
);


-- voc_ms_td.voc_anal_flow_model_tags_result_data_full_ext definition

CREATE TABLE `voc_anal_flow_model_tags_result_data_full_ext` (
                                                                 `id` varchar(40) NOT NULL COMMENT "主键ID",
                                                                 `publish_time` datetime NOT NULL COMMENT "内容发布时间（用于分区）",
                                                                 `data_id` varchar(40) NOT NULL COMMENT "业务主键ID",
                                                                 `channel_catagory` text NULL COMMENT "渠道类别",
                                                                 `channel_code` text NOT NULL COMMENT "渠道代码",
                                                                 `channel_name` text NULL COMMENT "渠道名称",
                                                                 `brand_code` text NULL COMMENT "品牌代码",
                                                                 `brand_name` text NULL COMMENT "品牌名称",
                                                                 `car_series_code` text NULL COMMENT "车系代码",
                                                                 `car_series_name` text NULL COMMENT "车系名称",
                                                                 `model_name` text NULL COMMENT "车型名称",
                                                                 `content_type` varchar(10) NOT NULL COMMENT "内容类型：text/order等",
                                                                 `title` text NULL COMMENT "内容标题",
                                                                 `content` text NULL COMMENT "内容",
                                                                 `sentiment` varchar(40) NULL COMMENT "情感倾向",
                                                                 `intention` varchar(40) NULL COMMENT "用户意图",
                                                                 `data_create_time` date NOT NULL COMMENT "数据创建时间",
                                                                 `create_time` datetime NOT NULL COMMENT "记录创建时间",
                                                                 `update_time` datetime NULL COMMENT "记录更新时间",
                                                                 `is_outer` varchar(2) NOT NULL COMMENT "是否外部数据：1是 0否",
                                                                 `hot_word` text NULL COMMENT "热点词汇",
                                                                 `keywords` text NULL COMMENT "关键词",
                                                                 `original_text_scene` text NULL COMMENT "原文片段",
                                                                 `market_id` text NULL COMMENT "市场ID",
                                                                 `competitive_type` text NULL COMMENT "竞品类型",
                                                                 `is_core` varchar(2) NULL COMMENT "是否核心内容：1是 0否",
                                                                 `series_factory` text NULL COMMENT "车系工厂",
                                                                 `automark` text NULL COMMENT "自动标记",
                                                                 `one_id` text NOT NULL COMMENT "唯一标识ID",
                                                                 `user_journey1` text NULL COMMENT "用户旅程层级1",
                                                                 `user_journey2` text NULL COMMENT "用户旅程层级2",
                                                                 `user_journey3` text NULL COMMENT "用户旅程层级3",
                                                                 `usage_scenario_first` text NULL COMMENT "使用场景一级",
                                                                 `usage_scenario_second` text NULL COMMENT "使用场景二级",
                                                                 `d2c_responsible_dept` text NULL COMMENT "D2C责任部门",
                                                                 `d2c_accountable_dept` text NULL COMMENT "D2C问责部门",
                                                                 `d2c_cc_dept` text NULL COMMENT "D2C抄送部门",
                                                                 `cust_global_id` text NULL COMMENT "客户全局ID",
                                                                 `cust_classify` text NULL COMMENT "客户分类",
                                                                 `cust_main_phone` text NULL COMMENT "客户主手机号",
                                                                 `is_car_owner` text NULL COMMENT "是否车主：1是 0否",
                                                                 `cust_age` text NULL COMMENT "客户年龄",
                                                                 `cust_age_group` text NULL COMMENT "客户年龄段",
                                                                 `cust_name` text NULL COMMENT "客户姓名",
                                                                 `cust_gender` text NULL COMMENT "客户性别",
                                                                 `cust_high_educaion` text NULL COMMENT "是否高学历：1是 0否",
                                                                 `marrige_statue` text NULL COMMENT "婚姻状况",
                                                                 `family_income` text NULL COMMENT "家庭收入",
                                                                 `is_exchange_flg` text NULL COMMENT "是否换购：1是 0否",
                                                                 `purchase_car_times` text NULL COMMENT "购车次数",
                                                                 `is_member_flg` text NULL COMMENT "是否会员：1是 0否",
                                                                 `cust_province_code` text NULL COMMENT "客户省份代码",
                                                                 `cust_province` text NULL COMMENT "客户省份",
                                                                 `cust_city_code` text NULL COMMENT "客户城市代码",
                                                                 `cust_city` text NULL COMMENT "客户城市",
                                                                 `cust_type` text NULL COMMENT "客户类型",
                                                                 `cust_lived_prov` text NULL COMMENT "常住省份",
                                                                 `cust_lived_city` text NULL COMMENT "常住城市",
                                                                 `cust_profession` text NULL COMMENT "客户职业",
                                                                 `vhl_vin` text NULL COMMENT "车辆VIN码",
                                                                 `vhl_color_name` text NULL COMMENT "车辆颜色",
                                                                 `vhl_product_date` text NULL COMMENT "车辆生产日期",
                                                                 `vhl_offline_date` text NULL COMMENT "车辆下线日期",
                                                                 `vhl_is_abroad` text NULL COMMENT "是否进口车：1是 0否",
                                                                 `vhl_dis_ch` text NULL COMMENT "车辆排量",
                                                                 `vhl_dis_mt` text NULL COMMENT "车辆变速箱",
                                                                 `vhl_eng_clsf` text NULL COMMENT "发动机分类",
                                                                 `vhl_eng_seris` text NULL COMMENT "发动机系列",
                                                                 `vhl_veh_type` text NULL COMMENT "车辆类型",
                                                                 `vhl_country` text NULL COMMENT "车辆国别",
                                                                 `vhl_bd_clsf` text NULL COMMENT "车身分类",
                                                                 `vhl_seg_mt` text NULL COMMENT "细分市场",
                                                                 `vhl_pow_clsf` text NULL COMMENT "动力分类",
                                                                 `vhl_fu_clsf` text NULL COMMENT "燃料分类",
                                                                 `vhl_modl_st` text NULL COMMENT "车型状态",
                                                                 `vhl_std_plnt_code` text NULL COMMENT "标准工厂代码",
                                                                 `dlr_oc_id` text NULL COMMENT "订单中心经销商ID",
                                                                 `dlr_oc_code` text NULL COMMENT "订单中心经销商代码",
                                                                 `dlr_oc_name` text NULL COMMENT "订单中心经销商名称",
                                                                 `dlr_oc_province_code` text NULL COMMENT "订单中心经销商省份代码",
                                                                 `dlr_oc_province` text NULL COMMENT "订单中心经销商省份",
                                                                 `dlr_oc_city_code` text NULL COMMENT "订单中心经销商城市代码",
                                                                 `dlr_oc_city` text NULL COMMENT "订单中心经销商城市",
                                                                 `dlr_dc_id` text NULL COMMENT "交付中心经销商ID",
                                                                 `dlr_dc_code` text NULL COMMENT "交付中心经销商代码",
                                                                 `dlr_dc_name` text NULL COMMENT "交付中心经销商名称",
                                                                 `dlr_dc_province_code` text NULL COMMENT "交付中心经销商省份代码",
                                                                 `dlr_dc_province` text NULL COMMENT "交付中心经销商省份",
                                                                 `dlr_dc_city_code` text NULL COMMENT "交付中心经销商城市代码",
                                                                 `dlr_dc_city` text NULL COMMENT "交付中心经销商城市",
                                                                 `dlr_mc_id` text NULL COMMENT "售后中心经销商ID",
                                                                 `dlr_mc_code` text NULL COMMENT "售后中心经销商代码",
                                                                 `dlr_mc_name` text NULL COMMENT "售后中心经销商名称",
                                                                 `dlr_mc_province_code` text NULL COMMENT "售后中心经销商省份代码",
                                                                 `dlr_mc_province` text NULL COMMENT "售后中心经销商省份",
                                                                 `dlr_mc_city_code` text NULL COMMENT "售后中心经销商城市代码",
                                                                 `dlr_mc_city` text NULL COMMENT "售后中心经销商城市",
                                                                 `is_wsater_army` text NULL COMMENT "是否水军：1是 0否",
                                                                 `is_manager_focused` text NULL COMMENT "是否管理层关注：1是 0否",
                                                                 `is_big_v` text NULL COMMENT "是否大V：1是 0否",
                                                                 `author_id` text NULL COMMENT "作者ID",
                                                                 `author_nick` text NULL COMMENT "作者昵称",
                                                                 `is_main_post` varchar(2) NULL COMMENT "是否主帖：1是 0否",
                                                                 `original_link` text NULL COMMENT "原文链接",
                                                                 `view_count` varchar(10) NULL COMMENT "浏览量",
                                                                 `comment_count` varchar(10) NULL COMMENT "评论量",
                                                                 `like_count` varchar(10) NULL COMMENT "点赞量",
                                                                 `share_count` varchar(10) NULL COMMENT "分享量",
                                                                 `favorite_count` varchar(10) NULL COMMENT "收藏量",
                                                                 `work_order_id` text NULL COMMENT "工单ID",
                                                                 `quest_id` text NULL COMMENT "问卷ID",
                                                                 `quest_type` text NULL COMMENT "问卷类型",
                                                                 `quest_answer_score` varchar(10) NULL COMMENT "问卷回答得分",
                                                                 `quest_business_type` text NULL COMMENT "问卷业务类型",
                                                                 `quest_business_scenario` text NULL COMMENT "问卷业务场景",
                                                                 `tag_accuracy` text NULL COMMENT "标签准确率",
                                                                 `tag_customer_issue_classification` text NULL COMMENT "客户问题分类标签",
                                                                 `tag_issue_severity` text NULL COMMENT "问题严重程度标签",
                                                                 `tag_code_status` text NULL COMMENT "标签代码状态",
                                                                 `tag_business_domain` text NULL COMMENT "业务域标签",
                                                                 `tag_event_clarity` text NULL COMMENT "事件清晰度标签",
                                                                 `tag_high_value_flag` text NULL COMMENT "高价值标识：1是 0否",
                                                                 `tag_complaint_flag_needing_reply` varchar(5) NULL COMMENT "需回复投诉标识：1是 0否",
                                                                 `tag_complaint_flag_needing_prtv_msg` varchar(2) NULL COMMENT "需私信投诉标识：1是 0否",
                                                                 `tag_high_quality_voc_flag` varchar(2) NULL COMMENT "高质量VOC标识：1是 0否",
                                                                 `tag_new_energy_or_fuel` text NULL COMMENT "新能源/燃油标识",
                                                                 `tag_need_forvclosed_loop` varchar(5) NULL COMMENT "需闭环标识：1是 0否",
                                                                 `tag_sort` varchar(2) NULL COMMENT "标签排序",
                                                                 `topic` text NOT NULL COMMENT "主题标签",
                                                                 `topic_text` text NULL COMMENT "主题文本",
                                                                 `opinion` text NULL COMMENT "观点内容",
                                                                 `cpt_tag_first_code` text NULL COMMENT "竞争标签一级代码",
                                                                 `cpt_tag_second_code` text NULL COMMENT "竞争标签二级代码",
                                                                 `cpt_tag_three_code` text NULL COMMENT "竞争标签三级代码",
                                                                 `cpt_tag_four_code` text NULL COMMENT "竞争标签四级代码",
                                                                 `cpt_tag_first` text NULL COMMENT "竞争标签一级",
                                                                 `cpt_tag_second` text NULL COMMENT "竞争标签二级",
                                                                 `cpt_tag_three` text NULL COMMENT "竞争标签三级",
                                                                 `cpt_tag_four` text NULL COMMENT "竞争标签四级",
                                                                 `ujy_tag_first_code` text NULL COMMENT "用户旅程标签一级代码",
                                                                 `ujy_tag_second_code` text NULL COMMENT "用户旅程标签二级代码",
                                                                 `ujy_tag_three_code` text NULL COMMENT "用户旅程标签三级代码",
                                                                 `ujy_tag_four_code` text NULL COMMENT "用户旅程标签四级代码",
                                                                 `ujy_tag_first` text NULL COMMENT "用户旅程标签一级",
                                                                 `ujy_tag_second` text NULL COMMENT "用户旅程标签二级",
                                                                 `ujy_tag_three` text NULL COMMENT "用户旅程标签三级",
                                                                 `ujy_tag_four` text NULL COMMENT "用户旅程标签四级",
                                                                 `cma_tag_first_code` text NULL COMMENT "客户媒介标签一级代码",
                                                                 `cma_tag_second_code` text NULL COMMENT "客户媒介标签二级代码",
                                                                 `cma_tag_three_code` text NULL COMMENT "客户媒介标签三级代码",
                                                                 `cma_tag_four_code` text NULL COMMENT "客户媒介标签四级代码",
                                                                 `cma_tag_first` text NULL COMMENT "客户媒介标签一级",
                                                                 `cma_tag_second` text NULL COMMENT "客户媒介标签二级",
                                                                 `cma_tag_three` text NULL COMMENT "客户媒介标签三级",
                                                                 `cma_tag_four` text NULL COMMENT "客户媒介标签四级",
                                                                 `dom_tag_first_code` text NULL COMMENT "领域标签一级代码",
                                                                 `dom_tag_second_code` text NULL COMMENT "领域标签二级代码",
                                                                 `dom_tag_three_code` text NULL COMMENT "领域标签三级代码",
                                                                 `dom_tag_four_code` text NULL COMMENT "领域标签四级代码",
                                                                 `dom_tag_first` text NULL COMMENT "领域标签一级",
                                                                 `dom_tag_second` text NULL COMMENT "领域标签二级",
                                                                 `dom_tag_three` text NULL COMMENT "领域标签三级",
                                                                 `dom_tag_four` text NULL COMMENT "领域标签四级",
                                                                 `nps_tag_first_code` text NULL COMMENT "NPS标签一级代码",
                                                                 `nps_tag_second_code` text NULL COMMENT "NPS标签二级代码",
                                                                 `nps_tag_three_code` text NULL COMMENT "NPS标签三级代码",
                                                                 `nps_tag_four_code` text NULL COMMENT "NPS标签四级代码",
                                                                 `nps_tag_first` text NULL COMMENT "NPS标签一级",
                                                                 `nps_tag_second` text NULL COMMENT "NPS标签二级",
                                                                 `nps_tag_three` text NULL COMMENT "NPS标签三级",
                                                                 `nps_tag_four` text NULL COMMENT "NPS标签四级",
                                                                 `vtr_tag_first_code` text NULL COMMENT "车辆技术标签一级代码",
                                                                 `vtr_tag_second_code` text NULL COMMENT "车辆技术标签二级代码",
                                                                 `vtr_tag_three_code` text NULL COMMENT "车辆技术标签三级代码",
                                                                 `vtr_tag_four_code` text NULL COMMENT "车辆技术标签四级代码",
                                                                 `vtr_tag_first` text NULL COMMENT "车辆技术标签一级",
                                                                 `vtr_tag_second` text NULL COMMENT "车辆技术标签二级",
                                                                 `vtr_tag_three` text NULL COMMENT "车辆技术标签三级",
                                                                 `vtr_tag_four` text NULL COMMENT "车辆技术标签四级",
                                                                 `abandon` int NOT NULL COMMENT "是否遗弃：1是 0否",
                                                                 `high_quality` varchar(2) NULL COMMENT "源数据ID",
                                                                 `retweeted_url` text NULL COMMENT "主贴url链接",
                                                                 `retweeted_user_id` text NULL COMMENT "发帖人id",
                                                                 `retweeted_user_name` text NULL COMMENT "发帖人姓名",
                                                                 `retweeted_content` text NULL COMMENT "发帖内容",
                                                                 `retweeted_title` text NULL COMMENT "发帖title",
                                                                 `retweeted_time` text NULL COMMENT "发帖时间",
                                                                 `source_data_id` text NULL COMMENT "评论url链接",
                                                                 `comment_user_name` text NULL COMMENT "评论用户名称",
                                                                 `comment_user_id` text NULL COMMENT "评论用户id",
                                                                 `one_id_risk` text NULL COMMENT " 发帖人id",
                                                                 `ad_type` text NULL COMMENT " 广告类型  中文 例如新闻类",
                                                                 `attribute_tag_code` text NULL COMMENT " 属性标签",
                                                                 `attribute_tag_name` text NULL COMMENT " 属性标签",
                                                                 `emotional_level` text NULL COMMENT " 情感程度 中文 一般 高 中",
                                                                 `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "数据插入时间",
                                                                 `comment_url` text NULL COMMENT "评论url链接",
                                                                 INDEX idx_inverted_content (`content`) USING INVERTED PROPERTIES("lower_case" = "true", "parser" = "chinese", "parser_mode" = "coarse_grained", "support_phrase" = "true"),
                                                                 INDEX idx_inverted_title (`title`) USING INVERTED PROPERTIES("lower_case" = "true", "parser" = "chinese", "parser_mode" = "coarse_grained", "support_phrase" = "true")
) ENGINE=OLAP
UNIQUE KEY(`id`, `publish_time`)
COMMENT 'VOC- 标签结果宽表（含客户/车辆/经销商/多维度标签）'
AUTO PARTITION BY RANGE (date_trunc(`publish_time`, 'month'))()
DISTRIBUTED BY HASH(`id`) BUCKETS 32
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "brand_code, data_id, car_series_code, data_create_time, channel_code",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "month",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-36",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p",
"dynamic_partition.replication_allocation" = "tag.location.default: 3",
"dynamic_partition.buckets" = "32",
"dynamic_partition.create_history_partition" = "false",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"dynamic_partition.start_day_of_month" = "1",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"compression" = "ZSTD",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);



-- voc_ms_td.voc_anal_flow_model_tags_result_data_ext definition

CREATE TABLE `voc_anal_flow_model_tags_result_data_ext` (
                                                            `id` varchar(40) NOT NULL COMMENT "主键ID",
                                                            `publish_time` datetime NOT NULL COMMENT "内容发布时间（用于分区）",
                                                            `data_id` varchar(40) NOT NULL COMMENT "业务主键ID",
                                                            `channel_catagory` text NULL COMMENT "渠道类别",
                                                            `channel_code` text NOT NULL COMMENT "渠道代码",
                                                            `channel_name` text NULL COMMENT "渠道名称",
                                                            `brand_code` text NULL COMMENT "品牌代码",
                                                            `brand_name` text NULL COMMENT "品牌名称",
                                                            `car_series_code` text NULL COMMENT "车系代码",
                                                            `car_series_name` text NULL COMMENT "车系名称",
                                                            `model_name` text NULL COMMENT "车型名称",
                                                            `content_type` varchar(10) NOT NULL COMMENT "内容类型：text/order等",
                                                            `title` text NULL COMMENT "内容标题",
                                                            `content` text NULL COMMENT "内容",
                                                            `sentiment` varchar(40) NULL COMMENT "情感倾向",
                                                            `intention` varchar(40) NULL COMMENT "用户意图",
                                                            `data_create_time` date NOT NULL COMMENT "数据创建时间",
                                                            `create_time` datetime NOT NULL COMMENT "记录创建时间",
                                                            `update_time` datetime NULL COMMENT "记录更新时间",
                                                            `is_outer` varchar(2) NOT NULL COMMENT "是否外部数据：1是 0否",
                                                            `hot_word` text NULL COMMENT "热点词汇",
                                                            `keywords` text NULL COMMENT "关键词",
                                                            `original_text_scene` text NULL COMMENT "原文片段",
                                                            `market_id` text NULL COMMENT "市场ID",
                                                            `competitive_type` text NULL COMMENT "竞品类型",
                                                            `is_core` varchar(2) NULL COMMENT "是否核心内容：1是 0否",
                                                            `series_factory` text NULL COMMENT "车系工厂",
                                                            `automark` text NULL COMMENT "自动标记",
                                                            `one_id` text NOT NULL COMMENT "唯一标识ID",
                                                            `user_journey1` text NULL COMMENT "用户旅程层级1",
                                                            `user_journey2` text NULL COMMENT "用户旅程层级2",
                                                            `user_journey3` text NULL COMMENT "用户旅程层级3",
                                                            `usage_scenario_first` text NULL COMMENT "使用场景一级",
                                                            `usage_scenario_second` text NULL COMMENT "使用场景二级",
                                                            `d2c_responsible_dept` text NULL COMMENT "D2C责任部门",
                                                            `d2c_accountable_dept` text NULL COMMENT "D2C问责部门",
                                                            `d2c_cc_dept` text NULL COMMENT "D2C抄送部门",
                                                            `cust_global_id` text NULL COMMENT "客户全局ID",
                                                            `cust_classify` text NULL COMMENT "客户分类",
                                                            `cust_main_phone` text NULL COMMENT "客户主手机号",
                                                            `is_car_owner` text NULL COMMENT "是否车主：1是 0否",
                                                            `cust_age` text NULL COMMENT "客户年龄",
                                                            `cust_age_group` text NULL COMMENT "客户年龄段",
                                                            `cust_name` text NULL COMMENT "客户姓名",
                                                            `cust_gender` text NULL COMMENT "客户性别",
                                                            `cust_high_educaion` text NULL COMMENT "是否高学历：1是 0否",
                                                            `marrige_statue` text NULL COMMENT "婚姻状况",
                                                            `family_income` text NULL COMMENT "家庭收入",
                                                            `is_exchange_flg` text NULL COMMENT "是否换购：1是 0否",
                                                            `purchase_car_times` text NULL COMMENT "购车次数",
                                                            `is_member_flg` text NULL COMMENT "是否会员：1是 0否",
                                                            `cust_province_code` text NULL COMMENT "客户省份代码",
                                                            `cust_province` text NULL COMMENT "客户省份",
                                                            `cust_city_code` text NULL COMMENT "客户城市代码",
                                                            `cust_city` text NULL COMMENT "客户城市",
                                                            `cust_type` text NULL COMMENT "客户类型",
                                                            `cust_lived_prov` text NULL COMMENT "常住省份",
                                                            `cust_lived_city` text NULL COMMENT "常住城市",
                                                            `cust_profession` text NULL COMMENT "客户职业",
                                                            `vhl_vin` text NULL COMMENT "车辆VIN码",
                                                            `vhl_color_name` text NULL COMMENT "车辆颜色",
                                                            `vhl_product_date` text NULL COMMENT "车辆生产日期",
                                                            `vhl_offline_date` text NULL COMMENT "车辆下线日期",
                                                            `vhl_is_abroad` text NULL COMMENT "是否进口车：1是 0否",
                                                            `vhl_dis_ch` text NULL COMMENT "车辆排量",
                                                            `vhl_dis_mt` text NULL COMMENT "车辆变速箱",
                                                            `vhl_eng_clsf` text NULL COMMENT "发动机分类",
                                                            `vhl_eng_seris` text NULL COMMENT "发动机系列",
                                                            `vhl_veh_type` text NULL COMMENT "车辆类型",
                                                            `vhl_country` text NULL COMMENT "车辆国别",
                                                            `vhl_bd_clsf` text NULL COMMENT "车身分类",
                                                            `vhl_seg_mt` text NULL COMMENT "细分市场",
                                                            `vhl_pow_clsf` text NULL COMMENT "动力分类",
                                                            `vhl_fu_clsf` text NULL COMMENT "燃料分类",
                                                            `vhl_modl_st` text NULL COMMENT "车型状态",
                                                            `vhl_std_plnt_code` text NULL COMMENT "标准工厂代码",
                                                            `dlr_oc_id` text NULL COMMENT "订单中心经销商ID",
                                                            `dlr_oc_code` text NULL COMMENT "订单中心经销商代码",
                                                            `dlr_oc_name` text NULL COMMENT "订单中心经销商名称",
                                                            `dlr_oc_province_code` text NULL COMMENT "订单中心经销商省份代码",
                                                            `dlr_oc_province` text NULL COMMENT "订单中心经销商省份",
                                                            `dlr_oc_city_code` text NULL COMMENT "订单中心经销商城市代码",
                                                            `dlr_oc_city` text NULL COMMENT "订单中心经销商城市",
                                                            `dlr_dc_id` text NULL COMMENT "交付中心经销商ID",
                                                            `dlr_dc_code` text NULL COMMENT "交付中心经销商代码",
                                                            `dlr_dc_name` text NULL COMMENT "交付中心经销商名称",
                                                            `dlr_dc_province_code` text NULL COMMENT "交付中心经销商省份代码",
                                                            `dlr_dc_province` text NULL COMMENT "交付中心经销商省份",
                                                            `dlr_dc_city_code` text NULL COMMENT "交付中心经销商城市代码",
                                                            `dlr_dc_city` text NULL COMMENT "交付中心经销商城市",
                                                            `dlr_mc_id` text NULL COMMENT "售后中心经销商ID",
                                                            `dlr_mc_code` text NULL COMMENT "售后中心经销商代码",
                                                            `dlr_mc_name` text NULL COMMENT "售后中心经销商名称",
                                                            `dlr_mc_province_code` text NULL COMMENT "售后中心经销商省份代码",
                                                            `dlr_mc_province` text NULL COMMENT "售后中心经销商省份",
                                                            `dlr_mc_city_code` text NULL COMMENT "售后中心经销商城市代码",
                                                            `dlr_mc_city` text NULL COMMENT "售后中心经销商城市",
                                                            `is_wsater_army` text NULL COMMENT "是否水军：1是 0否",
                                                            `is_manager_focused` text NULL COMMENT "是否管理层关注：1是 0否",
                                                            `is_big_v` text NULL COMMENT "是否大V：1是 0否",
                                                            `author_id` text NULL COMMENT "作者ID",
                                                            `author_nick` text NULL COMMENT "作者昵称",
                                                            `is_main_post` varchar(2) NULL COMMENT "是否主帖：1是 0否",
                                                            `original_link` text NULL COMMENT "原文链接",
                                                            `view_count` varchar(10) NULL COMMENT "浏览量",
                                                            `comment_count` varchar(10) NULL COMMENT "评论量",
                                                            `like_count` varchar(10) NULL COMMENT "点赞量",
                                                            `share_count` varchar(10) NULL COMMENT "分享量",
                                                            `favorite_count` varchar(10) NULL COMMENT "收藏量",
                                                            `work_order_id` text NULL COMMENT "工单ID",
                                                            `quest_id` text NULL COMMENT "问卷ID",
                                                            `quest_type` text NULL COMMENT "问卷类型",
                                                            `quest_answer_score` varchar(10) NULL COMMENT "问卷回答得分",
                                                            `quest_business_type` text NULL COMMENT "问卷业务类型",
                                                            `quest_business_scenario` text NULL COMMENT "问卷业务场景",
                                                            `tag_accuracy` text NULL COMMENT "标签准确率",
                                                            `tag_customer_issue_classification` text NULL COMMENT "客户问题分类标签",
                                                            `tag_issue_severity` text NULL COMMENT "问题严重程度标签",
                                                            `tag_code_status` text NULL COMMENT "标签代码状态",
                                                            `tag_business_domain` text NULL COMMENT "业务域标签",
                                                            `tag_event_clarity` text NULL COMMENT "事件清晰度标签",
                                                            `tag_high_value_flag` text NULL COMMENT "高价值标识：1是 0否",
                                                            `tag_complaint_flag_needing_reply` varchar(5) NULL COMMENT "需回复投诉标识：1是 0否",
                                                            `tag_complaint_flag_needing_prtv_msg` varchar(2) NULL COMMENT "需私信投诉标识：1是 0否",
                                                            `tag_high_quality_voc_flag` varchar(2) NULL COMMENT "高质量VOC标识：1是 0否",
                                                            `tag_new_energy_or_fuel` text NULL COMMENT "新能源/燃油标识",
                                                            `tag_need_forvclosed_loop` varchar(5) NULL COMMENT "需闭环标识：1是 0否",
                                                            `tag_sort` varchar(2) NULL COMMENT "标签排序",
                                                            `topic` text NOT NULL COMMENT "主题标签",
                                                            `topic_text` text NULL COMMENT "主题文本",
                                                            `opinion` text NULL COMMENT "观点内容",
                                                            `cpt_tag_first_code` text NULL COMMENT "竞争标签一级代码",
                                                            `cpt_tag_second_code` text NULL COMMENT "竞争标签二级代码",
                                                            `cpt_tag_three_code` text NULL COMMENT "竞争标签三级代码",
                                                            `cpt_tag_four_code` text NULL COMMENT "竞争标签四级代码",
                                                            `cpt_tag_first` text NULL COMMENT "竞争标签一级",
                                                            `cpt_tag_second` text NULL COMMENT "竞争标签二级",
                                                            `cpt_tag_three` text NULL COMMENT "竞争标签三级",
                                                            `cpt_tag_four` text NULL COMMENT "竞争标签四级",
                                                            `ujy_tag_first_code` text NULL COMMENT "用户旅程标签一级代码",
                                                            `ujy_tag_second_code` text NULL COMMENT "用户旅程标签二级代码",
                                                            `ujy_tag_three_code` text NULL COMMENT "用户旅程标签三级代码",
                                                            `ujy_tag_four_code` text NULL COMMENT "用户旅程标签四级代码",
                                                            `ujy_tag_first` text NULL COMMENT "用户旅程标签一级",
                                                            `ujy_tag_second` text NULL COMMENT "用户旅程标签二级",
                                                            `ujy_tag_three` text NULL COMMENT "用户旅程标签三级",
                                                            `ujy_tag_four` text NULL COMMENT "用户旅程标签四级",
                                                            `cma_tag_first_code` text NULL COMMENT "客户媒介标签一级代码",
                                                            `cma_tag_second_code` text NULL COMMENT "客户媒介标签二级代码",
                                                            `cma_tag_three_code` text NULL COMMENT "客户媒介标签三级代码",
                                                            `cma_tag_four_code` text NULL COMMENT "客户媒介标签四级代码",
                                                            `cma_tag_first` text NULL COMMENT "客户媒介标签一级",
                                                            `cma_tag_second` text NULL COMMENT "客户媒介标签二级",
                                                            `cma_tag_three` text NULL COMMENT "客户媒介标签三级",
                                                            `cma_tag_four` text NULL COMMENT "客户媒介标签四级",
                                                            `dom_tag_first_code` text NULL COMMENT "领域标签一级代码",
                                                            `dom_tag_second_code` text NULL COMMENT "领域标签二级代码",
                                                            `dom_tag_three_code` text NULL COMMENT "领域标签三级代码",
                                                            `dom_tag_four_code` text NULL COMMENT "领域标签四级代码",
                                                            `dom_tag_first` text NULL COMMENT "领域标签一级",
                                                            `dom_tag_second` text NULL COMMENT "领域标签二级",
                                                            `dom_tag_three` text NULL COMMENT "领域标签三级",
                                                            `dom_tag_four` text NULL COMMENT "领域标签四级",
                                                            `nps_tag_first_code` text NULL COMMENT "NPS标签一级代码",
                                                            `nps_tag_second_code` text NULL COMMENT "NPS标签二级代码",
                                                            `nps_tag_three_code` text NULL COMMENT "NPS标签三级代码",
                                                            `nps_tag_four_code` text NULL COMMENT "NPS标签四级代码",
                                                            `nps_tag_first` text NULL COMMENT "NPS标签一级",
                                                            `nps_tag_second` text NULL COMMENT "NPS标签二级",
                                                            `nps_tag_three` text NULL COMMENT "NPS标签三级",
                                                            `nps_tag_four` text NULL COMMENT "NPS标签四级",
                                                            `vtr_tag_first_code` text NULL COMMENT "车辆技术标签一级代码",
                                                            `vtr_tag_second_code` text NULL COMMENT "车辆技术标签二级代码",
                                                            `vtr_tag_three_code` text NULL COMMENT "车辆技术标签三级代码",
                                                            `vtr_tag_four_code` text NULL COMMENT "车辆技术标签四级代码",
                                                            `vtr_tag_first` text NULL COMMENT "车辆技术标签一级",
                                                            `vtr_tag_second` text NULL COMMENT "车辆技术标签二级",
                                                            `vtr_tag_three` text NULL COMMENT "车辆技术标签三级",
                                                            `vtr_tag_four` text NULL COMMENT "车辆技术标签四级",
                                                            `abandon` int NOT NULL COMMENT "是否遗弃：1是 0否",
                                                            `high_quality` varchar(2) NULL COMMENT "源数据ID",
                                                            `retweeted_url` text NULL COMMENT "主贴url链接",
                                                            `retweeted_user_id` text NULL COMMENT "发帖人id",
                                                            `retweeted_user_name` text NULL COMMENT "发帖人姓名",
                                                            `retweeted_content` text NULL COMMENT "发帖内容",
                                                            `retweeted_title` text NULL COMMENT "发帖title",
                                                            `retweeted_time` text NULL COMMENT "发帖时间",
                                                            `source_data_id` text NULL COMMENT "评论url链接",
                                                            `comment_user_name` text NULL COMMENT "评论用户名称",
                                                            `comment_user_id` text NULL COMMENT "评论用户id",
                                                            `one_id_risk` text NULL COMMENT " 发帖人id",
                                                            `ad_type` text NULL COMMENT " 广告类型  中文 例如新闻类",
                                                            `attribute_tag_code` text NULL COMMENT " 属性标签",
                                                            `attribute_tag_name` text NULL COMMENT " 属性标签",
                                                            `emotional_level` text NULL COMMENT " 情感程度 中文 一般 高 中",
                                                            `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "数据插入时间",
                                                            `comment_url` text NULL COMMENT "评论url链接",
                                                            `done` int NOT NULL DEFAULT "0" COMMENT "是否完成 是：1，否：0"
) ENGINE=OLAP
UNIQUE KEY(`id`, `publish_time`)
COMMENT 'VOC- enriched标签结果宽表（含客户/车辆/经销商/多维度标签）'
AUTO PARTITION BY RANGE (date_trunc(`publish_time`, 'month'))()
DISTRIBUTED BY HASH(`id`) BUCKETS 8
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "brand_code, data_id, car_series_code, data_create_time, channel_code",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "month",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-3",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p",
"dynamic_partition.replication_allocation" = "tag.location.default: 3",
"dynamic_partition.buckets" = "32",
"dynamic_partition.create_history_partition" = "false",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"dynamic_partition.start_day_of_month" = "1",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"compression" = "ZSTD",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);



-- voc_ms_td.voc_anal_flow_mate_data_status definition
-- voc_ms_td.voc_anal_flow_mate_data_status definition
drop  TABLE `voc_anal_flow_batch_update_record` ;

CREATE TABLE `voc_anal_flow_batch_update_record` (
                                                     `id` varchar(40) NOT NULL COMMENT "主键",
                                                     `create_time` datetime NOT NULL COMMENT "记录时间",
                                                     `update_time` datetime NULL COMMENT "更新时间",
                                                     `request_id` text NOT NULL COMMENT "请求标识主键",
                                                     `ids` text  NULL COMMENT "主键",
                                                     `attrs` json NOT NULL COMMENT "修改字段集合",
                                                     `filters` json NULL COMMENT "修改条件字段集合",
                                                     `type` int NOT NULL DEFAULT "0" COMMENT "1，修改：2：删除",
                                                     `status` int NOT NULL DEFAULT "0" COMMENT "是否完成 是：1，否：0"
) ENGINE=OLAP
UNIQUE KEY(`id`, `create_time`)
COMMENT '数据清洗-前置处理后数据记录表'
AUTO PARTITION BY RANGE (date_trunc(`create_time`, 'month'))()
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "request_id, status",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "month",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-24",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p",
"dynamic_partition.replication_allocation" = "tag.location.default: 1",
"dynamic_partition.buckets" = "4",
"dynamic_partition.create_history_partition" = "false",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"dynamic_partition.start_day_of_month" = "1",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"compression" = "ZSTD",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "30000",
"group_commit_data_bytes" = "264217728",
"enable_mow_light_delete" = "false"
);


-- voc_ms_td.voc_imp_hudi_dm_voc_cust2 definition

CREATE TABLE `voc_imp_hudi_dm_voc_cust` (
                                            `oneid` varchar(50) NOT NULL COMMENT "OneID",
                                            `global_id` varchar(35) NULL COMMENT "sso全局ID",
                                            `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            `cust_classify` varchar(20) NULL COMMENT "客户类型",
                                            `id_card_type` varchar(30) NULL COMMENT "证件类型",
                                            `id_card_no` varchar(64) NULL COMMENT "证件号码(加密)",
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
                                            `lived_addr` varchar(500) NULL COMMENT "居住地址",
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
                                            `dw_insert_time` datetime NOT NULL COMMENT "数仓插入时间",
                                            `ds` varchar(10) NULL COMMENT "分区字段，格式：yyyyMMdd"
) ENGINE=OLAP
UNIQUE KEY(`oneid`)
COMMENT 'dm_voc_cust'
DISTRIBUTED BY HASH(`oneid`) BUCKETS 8
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "id_card_no, insert_dt, mobile",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"store_row_column" = "true"
);



--  drop table voc_imp_hudi_dm_voc_cust_vehicle_rel




-- drop table voc_imp_hudi_dwd_maf_veh_d_full
CREATE TABLE IF NOT EXISTS `voc_imp_hudi_dwd_maf_veh_d_full2` (
                                                                  `vin` VARCHAR(200) NOT NULL COMMENT '车架号',
    `period_date` DATE COMMENT '日期维id YYYY-MM-DD',
    `period_time_sec` DATETIME COMMENT '时间去重精确到秒',
    `period_wid` STRING COMMENT '日期维id YYYYMMDD',
    `prod_code` STRING COMMENT '产品编码（三段式）',
    `prod_name` STRING COMMENT '产品名称',
    `mdl_code` STRING COMMENT '车型代码',
    `mdl_name` STRING COMMENT '车型名称',
    `series_code` STRING COMMENT '车系代码',
    `series_name` STRING COMMENT '车系名称',
    `opt_code` STRING COMMENT '配置代码',
    `opt_name` STRING COMMENT '配置名称',
    `col_code` STRING COMMENT '颜色代码',
    `col_name` STRING COMMENT '颜色名称',
    `eng_clsf` STRING COMMENT '动力系列大类',
    `eng_seris` STRING COMMENT '动力系列小类',
    `eng_mdl` STRING COMMENT '发动机型号',
    `dis_mt` STRING COMMENT '排量',
    `dis_ch` STRING COMMENT '排放',
    `trans_clsf` STRING COMMENT '变速器类型',
    `trans_form` STRING COMMENT '变速器型式',
    `custom_code` STRING COMMENT '定制编码',
    `veh_type` STRING COMMENT '车辆类型（出口车、领用车、加工车、商用车）',
    `vcl_num` STRING COMMENT '产量',
    `sbu_code` STRING COMMENT '经营单位编码',
    `sbu_name` STRING COMMENT '经营单位名称',
    `continent` STRING COMMENT '大洲',
    `home_abroad` STRING COMMENT '国内国外',
    `cntry_code3` STRING COMMENT '国家地区三位字母码',
    `cntry_name` STRING COMMENT '国家地区中文名称',
    `cntry_eng` STRING COMMENT '国家地区英文名称',
    `plnt_code` STRING COMMENT '标准工厂编码',
    `plnt_name` STRING COMMENT '标准工厂名称',
    `product_date` STRING COMMENT '生产日期',
    `offline_date` STRING COMMENT '总装下线时间',
    `rtn_veh_date` STRING COMMENT '退车时间',
    `src_sys` STRING COMMENT '来源系统名称',
    `src_sys_id` STRING COMMENT '来源系统id',
    `job_name` STRING COMMENT '作业名称',
    `insert_dt` datetime not null default CURRENT_TIMESTAMP,
    `ds` VARCHAR(10) NOT NULL COMMENT '分区字段，格式：yyyyMMdd'
    )
DUPLICATE KEY(`vin`)
COMMENT '车辆事实表'
AUTO PARTITION BY RANGE (date_trunc(insert_dt, 'day'))()
DISTRIBUTED BY HASH(`vin`) BUCKETS 8
PROPERTIES (
       "replication_num" = "3",
       "min_load_replica_num" = "-1",
       "bloom_filter_columns" = "insert_dt",
       "dynamic_partition.enable" = "true",
       "dynamic_partition.time_unit" = "day",
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
     `zip_start_dt` DATE  COMMENT '拉链开始日期',
     `zip_end_dt` DATE  COMMENT '拉链结束日期',
     `zip_enable_flg` STRING  COMMENT '拉链可用标识，Y/N',
     `job_nm` STRING COMMENT '任务名称',
     `ds` VARCHAR(10)COMMENT '分区字段，格式：yyyyMMdd',
    `insert_dt` datetime NULL DEFAULT CURRENT_TIMESTAMP
)
UNIQUE KEY(`sk_id`)  -- 拉链表主键：代理键 + 生效日期
COMMENT '维度_渠道_经销商_拉链表'
DISTRIBUTED BY HASH(`sk_id`) BUCKETS 2
PROPERTIES (
    "replication_num" = "2",
    "storage_format" = "V2",
    "compression" = "LZ4",
    "enable_unique_key_merge_on_write" = "true",
    "light_schema_change" = "true"
);



-- voc_ms_td.voc_anal_di_stg_mate_data_m_inc definition

CREATE TABLE `voc_anal_di_stg_mate_data_m_inc` (
                                                   `id` varchar(40) NOT NULL COMMENT "主键ID",
                                                   `create_time` datetime NOT NULL COMMENT "数据抓取时间",
                                                   `data_create_time` datetime NOT NULL COMMENT "数据产生时间",
                                                   `content_type` varchar(40) NULL COMMENT "内容类型：order(工单), post(帖子评论), opinion(意见反馈), questionnaire(问卷), consult(咨询)",
                                                   `data_update_time` datetime NULL COMMENT "数据更新时间",
                                                   `data_id` varchar(40) NULL COMMENT "数据唯一标识",
                                                   `channel_code` varchar(40) NULL COMMENT "渠道编码",
                                                   `brand` text NULL COMMENT "品牌",
                                                   `series` text NULL COMMENT "车系",
                                                   `model` text NULL COMMENT "原始观点",
                                                   `is_outer` varchar(4) NULL COMMENT "是否往外数据",
                                                   `one_id` text NULL COMMENT "股份客户信息-one_id",
                                                   `id_car_no` text NULL COMMENT "客户证件号",
                                                   `mobile` text NULL COMMENT "客户手机号",
                                                   `email` text NULL COMMENT "客户邮箱",
                                                   `global_id` text NULL COMMENT "SSO全局ID",
                                                   `user_id` text NULL COMMENT "用户标识",
                                                   `user_name` text NULL COMMENT "用户名",
                                                   `vhl_id` text NULL COMMENT "车辆ID",
                                                   `vhl_vin` text NULL COMMENT "车辆车架号",
                                                   `dlr_id` text NULL COMMENT "股份售后经销商ID",
                                                   `dlr_code` text NULL COMMENT "股份售后经销商编码",
                                                   `dlr_type` text NULL COMMENT "股份售后经销商类型",
                                                   `market_id` text NULL COMMENT "股份产品物理编码",
                                                   `title` text NULL COMMENT "标题",
                                                   `content` text NULL COMMENT "内容正文",
                                                   `is_wsater_army` varchar(4) NULL COMMENT "是否水军",
                                                   `weight` int NULL COMMENT "权重值",
                                                   `attrs` json NULL COMMENT "业务系统其他字段",
                                                   `attrs2` json NULL COMMENT "业务系统其他字段",
                                                   `attrs3` json NULL COMMENT "业务系统其他字段",
                                                   `work_id` varchar(40) NULL COMMENT "最终标识",
                                                   `done` int NULL COMMENT "是否完成计算：1-是，0-否",
                                                   `model_type` int NULL COMMENT "模型类型",
                                                   `ds` varchar(20) NULL COMMENT "分区字段（备用）",
                                                   `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=OLAP
UNIQUE KEY(`id`, `create_time`)
COMMENT 'VOC-ODS原始数据记录表（优化版）'
AUTO PARTITION BY RANGE (date_trunc(`create_time`, 'day'))
(PARTITION p20251208000000 VALUES [('2025-12-08 00:00:00'), ('2025-12-09 00:00:00')),
PARTITION p20251209000000 VALUES [('2025-12-09 00:00:00'), ('2025-12-10 00:00:00')),
PARTITION p20251210 VALUES [('2025-12-10 00:00:00'), ('2025-12-11 00:00:00')),
PARTITION p20251211 VALUES [('2025-12-11 00:00:00'), ('2025-12-12 00:00:00')))
DISTRIBUTED BY HASH(`id`) BUCKETS 16
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "insert_dt, data_id, data_create_time",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "day",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-30",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p",
"dynamic_partition.replication_allocation" = "tag.location.default: 3",
"dynamic_partition.buckets" = "16",
"dynamic_partition.create_history_partition" = "false",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "30000",
"group_commit_data_bytes" = "264217728",
"enable_mow_light_delete" = "false"
);



CREATE TABLE `voc_anal_di_stg_mate_data_m_inc2` (
                                                    `id` varchar(40) NOT NULL COMMENT "主键ID",
                                                    `create_time` datetime NOT NULL COMMENT "数据抓取时间",
                                                    `data_create_time` datetime NOT NULL COMMENT "数据产生时间",
                                                    `content_type` varchar(40) NULL COMMENT "内容类型：order(工单), post(帖子评论), opinion(意见反馈), questionnaire(问卷), consult(咨询)",
                                                    `data_update_time` datetime NULL COMMENT "数据更新时间",
                                                    `data_id` varchar(40) NULL COMMENT "数据唯一标识",
                                                    `channel_code` varchar(40) NULL COMMENT "渠道编码",
                                                    `brand` text NULL COMMENT "品牌",
                                                    `series` text NULL COMMENT "车系",
                                                    `model` text NULL COMMENT "原始观点",
                                                    `is_outer` varchar(4) NULL COMMENT "是否往外数据",
                                                    `one_id` text NULL COMMENT "股份客户信息-one_id",
                                                    `id_car_no` text NULL COMMENT "客户证件号",
                                                    `mobile` text NULL COMMENT "客户手机号",
                                                    `email` text NULL COMMENT "客户邮箱",
                                                    `global_id` text NULL COMMENT "SSO全局ID",
                                                    `user_id` text NULL COMMENT "用户标识",
                                                    `user_name` text NULL COMMENT "用户名",
                                                    `vhl_id` text NULL COMMENT "车辆ID",
                                                    `vhl_vin` text NULL COMMENT "车辆车架号",
                                                    `dlr_id` text NULL COMMENT "股份售后经销商ID",
                                                    `dlr_code` text NULL COMMENT "股份售后经销商编码",
                                                    `dlr_type` text NULL COMMENT "股份售后经销商类型",
                                                    `market_id` text NULL COMMENT "股份产品物理编码",
                                                    `title` text NULL COMMENT "标题",
                                                    `content` text NULL COMMENT "内容正文",
                                                    `is_wsater_army` varchar(4) NULL COMMENT "是否水军",
                                                    `weight` int NULL COMMENT "权重值",
                                                    `attrs` json NULL COMMENT "业务系统其他字段",
                                                    `attrs2` json NULL COMMENT "业务系统其他字段",
                                                    `attrs3` json NULL COMMENT "业务系统其他字段",
                                                    `work_id` varchar(40) NULL COMMENT "最终标识",
                                                    `done` int NULL COMMENT "是否完成计算：1-是，0-否",
                                                    `model_type` int NULL COMMENT "模型类型",
                                                    `ds` varchar(20) NULL COMMENT "分区字段（备用）",
                                                    `insert_dt` datetime not NULL DEFAULT CURRENT_TIMESTAMP
)
    DUPLICATE KEY(`id`)
COMMENT 'VOC-ODS原始数据记录表（优化版）'
AUTO PARTITION BY RANGE (date_trunc(insert_dt, 'day'))()
DISTRIBUTED BY HASH (`id`) BUCKETS 12
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "data_id,insert_dt,channel_code,data_create_time",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "day",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-5",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p"
);




CREATE TABLE `voc_anal_di_stg_mate_data_finished_record2` (
  `data_id` varchar(40) NOT NULL COMMENT "原数据标识",
  `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "异常数据创建时间",
  `id` varchar(40) NOT NULL COMMENT "主键",
  `work_id` varchar(40) NULL COMMENT "主键",
  `channel_type` varchar(60) NOT NULL COMMENT "渠道类型",
  `retry_count` int NOT NULL COMMENT "重试次数",
  `error_code` varchar(50) NULL COMMENT "异常编码",
  `error_msg` text NULL COMMENT "异常描述",
  `data` json NULL COMMENT "原数据",
  `last_exec_time` datetime NULL COMMENT "最后执行时间",
  `status` int NOT NULL COMMENT "状态",
  `tid` varchar(55) NULL COMMENT "链路标识"
) ENGINE=OLAP
UNIQUE KEY(`data_id`, `insert_dt`)
COMMENT '数据接收-接收数据处理记录表'
AUTO PARTITION BY RANGE (date_trunc(`insert_dt`, 'day'))()
DISTRIBUTED BY HASH(`data_id`) BUCKETS 8
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "DAY",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-7",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p",
"dynamic_partition.replication_allocation" = "tag.location.default: 3",
"dynamic_partition.buckets" = "8",
"dynamic_partition.create_history_partition" = "false",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"compression" = "LZ4",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);







CREATE TABLE `voc_anal_flow_mate_data_full` (
                `id` varchar(40) NOT NULL COMMENT "主键",
                `publish_time` datetime NOT NULL COMMENT "发布时间",
                `data_id` varchar(40) NOT NULL COMMENT "主键",
                `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
                `one_id` varchar(64) NOT NULL COMMENT "股份客户信息-one_id",
                `client_id` varchar(40) NOT NULL COMMENT "客户标识",
                `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
                `content_type` varchar(10) NULL COMMENT "内容类型：文本：text、 工单：order",
                `title` text NULL COMMENT "标题",
                `content` text NULL COMMENT "内容",
                `user_name` text NULL COMMENT "昵称",
                `data` json NULL,
                `done` int NULL COMMENT "是否完成计算 是：1，否：0",
                `data_status` int NULL COMMENT "数据状态 0全部 1去噪数据 2已打标数据 3未打标数据 ",
                `model_type` int NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
                `ext_fields` json NULL COMMENT "扩展字段",
                `biz_ext_attrs` json NULL COMMENT "扩展字段",
                `biz_ext_attrs2` json NULL COMMENT "扩展字段",
                `biz_ext_attrs3` json NULL COMMENT "扩展字段",
                `cust_ext_attrs` json NULL COMMENT "客户信息扩展字段",
                `vhl_ext_attrs` json NULL COMMENT "车辆信息扩展字段",
                `dealer_ext_attrs` json NULL COMMENT "经销商信息扩展字段",
                `prd_ext_attrs` json NULL COMMENT "产品经销商信息扩展字段",
                `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "接收时间",
                `insert_dt` datetime not null default CURRENT_TIMESTAMP
) ENGINE=OLAP
    UNIQUE KEY(`id`,`publish_time`)
COMMENT "数据清洗-模型入参数据记录表"
AUTO PARTITION BY RANGE (date_trunc(publish_time, 'week'))()
DISTRIBUTED BY HASH (`id`) BUCKETS 12
PROPERTIES (
	"replication_num" = "3",
	"bloom_filter_columns" = "publish_time,channel_id,done,data_id",
	"storage_format" = "default",
	"compression" = "ZLIB"
);



CREATE TABLE `voc_anal_flow_mate_data_full2` (
         `id` varchar(40) NOT NULL COMMENT "主键",
         `publish_time` datetime NOT NULL COMMENT "发布时间",
         `data_id` varchar(40) NOT NULL COMMENT "主键",
         `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
         `one_id` varchar(64) NOT NULL COMMENT "股份客户信息-one_id",
         `client_id` varchar(40) NOT NULL COMMENT "客户标识",
         `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
         `content_type` varchar(10) NULL COMMENT "内容类型：文本：text、 工单：order",
         `title` text NULL COMMENT "标题",
         `content` text NULL COMMENT "内容",
         `user_name` text NULL COMMENT "昵称",
         `data` json NULL,
         `done` int NULL COMMENT "是否完成计算 是：1，否：0",
         `data_status` int NULL COMMENT "数据状态 0全部 1去噪数据  2未打标数据 3已打标数据",
         `model_type` int NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
         `ext_fields` json NULL COMMENT "扩展字段",
         `biz_ext_attrs` json NULL COMMENT "扩展字段",
         `biz_ext_attrs2` json NULL COMMENT "扩展字段",
         `biz_ext_attrs3` json NULL COMMENT "扩展字段",
         `cust_ext_attrs` json NULL COMMENT "客户信息扩展字段",
         `vhl_ext_attrs` json NULL COMMENT "车辆信息扩展字段",
         `dealer_ext_attrs` json NULL COMMENT "经销商信息扩展字段",
         `prd_ext_attrs` json NULL COMMENT "产品经销商信息扩展字段",
         `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "接收时间",
         `insert_dt` datetime not null default CURRENT_TIMESTAMP
) ENGINE=OLAP
DUPLICATE KEY(`id`)
COMMENT "数据清洗-模型入参数据记录表"
AUTO PARTITION BY RANGE (date_trunc(publish_time, 'month'))()
DISTRIBUTED BY HASH (`id`) BUCKETS 16
PROPERTIES (
"replication_allocation" = "tag.location.default: 3",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "publish_time,channel_id,done,data_id,insert_dt"
);





CREATE TABLE `voc_anal_di_stg_mate_data_merge_m_inc2` (
                                                          `id` varchar(40) NOT NULL COMMENT "主键ID",
                                                          `create_time` datetime NOT NULL COMMENT "数据抓取时间",
                                                          `data_create_time` datetime NOT NULL COMMENT "数据产生时间",
                                                          `content_type` varchar(40) NULL COMMENT "内容类型：order(工单), post(帖子评论), opinion(意见反馈), questionnaire(问卷), consult(咨询)",
                                                          `data_update_time` datetime NULL COMMENT "数据更新时间",
                                                          `data_id` varchar(40) NULL COMMENT "数据唯一标识",
                                                          `channel_code` varchar(40) NULL COMMENT "渠道编码",
                                                          `brand` text NULL COMMENT "品牌",
                                                          `series` text NULL COMMENT "车系",
                                                          `model` text NULL COMMENT "原始观点",
                                                          `is_outer` varchar(4) NULL COMMENT "是否往外数据",
                                                          `one_id` text NULL COMMENT "股份客户信息-one_id",
                                                          `id_car_no` text NULL COMMENT "客户证件号",
                                                          `mobile` text NULL COMMENT "客户手机号",
                                                          `email` text NULL COMMENT "客户邮箱",
                                                          `global_id` text NULL COMMENT "SSO全局ID",
                                                          `user_id` text NULL COMMENT "用户标识",
                                                          `user_name` text NULL COMMENT "用户名",
                                                          `vhl_id` text NULL COMMENT "车辆ID",
                                                          `vhl_vin` text NULL COMMENT "车辆车架号",
                                                          `dlr_id` text NULL COMMENT "股份售后经销商ID",
                                                          `dlr_code` text NULL COMMENT "股份售后经销商编码",
                                                          `dlr_type` text NULL COMMENT "股份售后经销商类型",
                                                          `market_id` text NULL COMMENT "股份产品物理编码",
                                                          `title` text NULL COMMENT "标题",
                                                          `content` text NULL COMMENT "内容正文",
                                                          `is_wsater_army` varchar(4) NULL COMMENT "是否水军",
                                                          `weight` int NULL COMMENT "权重值",
                                                          `attrs` json NULL COMMENT "业务系统其他字段",
                                                          `attrs2` json NULL COMMENT "业务系统其他字段",
                                                          `attrs3` json NULL COMMENT "业务系统其他字段",
                                                          `work_id` varchar(40) NULL COMMENT "最终标识",
                                                          `done` int NULL COMMENT "是否完成计算：1-是，0-否",
                                                          `model_type` int NULL COMMENT "模型类型",
                                                          `cust_ext_attrs` json NULL COMMENT "客户信息扩展字段",
                                                          `vhl_ext_attrs` json NULL COMMENT "车辆信息扩展字段",
                                                          `dealer_ext_attrs` json NULL COMMENT "经销商信息扩展字段",
                                                          `prd_ext_attrs` json NULL COMMENT "产品经销商信息扩展字段",
                                                          `ds` varchar(20) NULL COMMENT "分区字段（备用）",
                                                          `insert_dt` datetime NULL DEFAULT CURRENT_TIMESTAMP
)  ENGINE=OLAP
DUPLICATE KEY(`id`)
COMMENT 'VOC-ODS原始数据记录表（优化版）'
--  时间分区：按天动态分区
AUTO PARTITION BY RANGE (date_trunc(data_create_time, 'month'))()
-- 分桶：32 buckets（适合千万~亿级数据）
DISTRIBUTED BY HASH(`id`) BUCKETS 12
-- 属性配置
PROPERTIES (
	"replication_allocation" = "tag.location.default: 3",
	"min_load_replica_num" = "-1",
    "bloom_filter_columns" = "data_id,insert_dt,data_create_time"
)
;



-- drop table voc_anal_flow_pre_rules_result_data_full
CREATE TABLE `voc_anal_flow_pre_rules_result_data_full` (
                                                            `id` varchar(40) NOT NULL COMMENT "主键",
                                                            `publish_time` datetime NOT NULL COMMENT "发布时间",
                                                            `data_id` varchar(40) NOT NULL COMMENT "主键",
                                                            `one_id` varchar(64) NULL COMMENT "股份客户信息-one_id",
                                                            `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
                                                            `client_id` varchar(40) NOT NULL COMMENT "客户标识",
                                                            `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
                                                            `content_type` varchar(10) NULL COMMENT "内容类型：文本：text、 工单：order",
                                                            `data` json NULL,
                                                            `data_md5` json NULL COMMENT "内容md5值",
                                                            `model_type` int NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
                                                            `ext_fields` json NULL COMMENT "扩展字段",
                                                            `biz_ext_attrs` json NULL COMMENT "扩展字段",
                                                            `biz_ext_attrs2` json NULL COMMENT "扩展字段",
                                                            `biz_ext_attrs3` json NULL COMMENT "扩展字段",
                                                            `cust_ext_attrs` json NULL COMMENT "客户信息扩展字段",
                                                            `vhl_ext_attrs` json NULL COMMENT "车辆信息扩展字段",
                                                            `dealer_ext_attrs` json NULL COMMENT "经销商信息扩展字段",
                                                            `prd_ext_attrs` json NULL COMMENT "产品经销商信息扩展字段",
                                                            `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "接收时间",
                                                            `abandon` int NULL COMMENT "是否遗弃数据 是：1，否：0",
                                                            `done` int NULL COMMENT "是否完成计算 是：1，否：0",
                                                            `hit_rules` json NULL COMMENT "规则id集合",
                                                            `insert_dt` datetime not null default CURRENT_TIMESTAMP
) ENGINE=OLAP
    UNIQUE KEY(`id`,`publish_time`)
COMMENT "数据清洗-前置处理后数据记录表"
AUTO PARTITION BY RANGE (date_trunc(publish_time, 'week'))()
DISTRIBUTED BY HASH (`id`) BUCKETS 12
PROPERTIES (
	"replication_num" = "3",
	"bloom_filter_columns" = "data_id,channel_id,done,abandon",
	"storage_format" = "default",
	"compression" = "ZLIB"
);


-- voc_ms_td.voc_anal_flow_pre_rules_result_data_full definition
-- voc_ms_td.voc_anal_flow_pre_rules_result_data_full definition
-- voc_ms_td.voc_anal_flow_pre_rules_result_data_full definition
-- drop table voc_anal_flow_pre_rules_result_data_full2
CREATE TABLE `voc_anal_flow_pre_rules_result_data_full2` (
                                                             `id` varchar(40) NOT NULL COMMENT "主键",
                                                             `publish_time` datetime NOT NULL COMMENT "发布时间",
                                                             `data_id` varchar(40) NOT NULL COMMENT "主键",
                                                             `abandon` int NULL COMMENT "是否遗弃数据 是：1，否：0",
                                                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "接收时间",
                                                             `one_id` varchar(64) NULL COMMENT "股份客户信息-one_id",
                                                             `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
                                                             `client_id` varchar(40) NOT NULL COMMENT "客户标识",
                                                             `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
                                                             `content_type` varchar(10) NULL COMMENT "内容类型：文本：text、 工单：order",
                                                             `data` json NULL,
                                                             `data_md5` json NULL COMMENT "内容md5值",
                                                             `model_type` int NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
                                                             `ext_fields` json NULL COMMENT "扩展字段",
                                                             `biz_ext_attrs` json NULL COMMENT "扩展字段",
                                                             `biz_ext_attrs2` json NULL COMMENT "扩展字段",
                                                             `biz_ext_attrs3` json NULL COMMENT "扩展字段",
                                                             `cust_ext_attrs` json NULL COMMENT "客户信息扩展字段",
                                                             `vhl_ext_attrs` json NULL COMMENT "车辆信息扩展字段",
                                                             `dealer_ext_attrs` json NULL COMMENT "经销商信息扩展字段",
                                                             `prd_ext_attrs` json NULL COMMENT "产品经销商信息扩展字段",
                                                             `done` int NULL COMMENT "是否完成计算 是：1，否：0",
                                                             `hit_rules` json NULL COMMENT "规则id集合",
                                                             `insert_dt` datetime NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=OLAP
DUPLICATE KEY(`id`,`publish_time`,`data_id`,`abandon`)
COMMENT '数据清洗-前置处理后数据记录表'
AUTO PARTITION BY RANGE (date_trunc(`create_time`, 'month'))()
DISTRIBUTED BY HASH(`id`) BUCKETS 32
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "insert_dt, abandon, data_id, channel_id, done",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "MONTH",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-12",
"dynamic_partition.end" = "3",
"dynamic_partition.prefix" = "p",
"dynamic_partition.create_history_partition" = "false",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"dynamic_partition.start_day_of_month" = "1",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728"
);




CREATE TABLE `voc_anal_flow_error_data_record` (
                   `id` varchar(40) NOT NULL COMMENT "主键",
                   `table` varchar(100) NOT NULL COMMENT "表名",
                   `action` varchar(40) NOT NULL COMMENT "操作类型",
                   `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
                   `client_id` varchar(40) NOT NULL COMMENT "消息来源 api,mq,file等",
                   `data` json NULL,
                   `create_time` datetime NULL COMMENT "接收时间",
                   `tid` varchar(55) NULL COMMENT "链路标识"
) ENGINE=OLAP
    UNIQUE KEY(`id`)
COMMENT "数据清洗-入库时异常数据记录表"
DISTRIBUTED BY HASH(`id`)  BUCKETS 4
PROPERTIES (
	"replication_num" = "3",
	"storage_format" = "default",
	"compression" = "ZLIB"
);









-- voc_ms_td.voc_anal_flow_model_tags_unlabeled_data_full definition

CREATE TABLE `voc_anal_flow_model_tags_unlabeled_data_full2` (
                                                                 `id` varchar(40) NOT NULL COMMENT "主键id",
                                                                 `publish_time` datetime NOT NULL COMMENT "发布时间",
                                                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间",
                                                                 `data_id` varchar(40) NOT NULL COMMENT "主键id",
                                                                 `one_id` varchar(64) NULL COMMENT "股份客户信息-one_id",
                                                                 `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
                                                                 `client_id` varchar(40) NOT NULL COMMENT "客户标识",
                                                                 `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
                                                                 `content_type` varchar(10) NULL COMMENT "内容类型：文本：text、 工单：order",
                                                                 `input_data_id` varchar(100) NULL COMMENT "原文id",
                                                                 `brand_code` varchar(1000) NULL COMMENT "品牌名称",
                                                                 `car_series_code` varchar(1000) NULL COMMENT "车系名称",
                                                                 `opinion` varchar(500) NULL COMMENT "观点",
                                                                 `opinion_sentiment` varchar(1000) NULL COMMENT "观点情感",
                                                                 `subject` varchar(1000) NULL COMMENT "主体",
                                                                 `description` varchar(1000) NULL COMMENT "描述",
                                                                 `car_body_label` varchar(1000) NULL COMMENT "整车体系",
                                                                 `view_label` varchar(1000) NULL COMMENT "评价维度",
                                                                 `model_type` int NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
                                                                 `ext_fields` json NULL COMMENT "扩展字段",
                                                                 `biz_ext_attrs` json NULL COMMENT "扩展字段",
                                                                 `biz_ext_attrs2` json NULL COMMENT "扩展字段",
                                                                 `biz_ext_attrs3` json NULL COMMENT "扩展字段",
                                                                 `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "更新时间",
                                                                 `done` int NULL COMMENT "是否完成计算 是：1，否：0",
                                                                 `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=OLAP
DUPLICATE KEY(`id`,`publish_time`)
COMMENT '模型计算数据分析结果表（未命中标签的)'
AUTO PARTITION BY RANGE (date_trunc(`create_time`, 'month'))()
DISTRIBUTED BY HASH(`id`) BUCKETS 32
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "insert_dt, publish_time, channel_id",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "MONTH",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-24",
"dynamic_partition.end" = "3",
"dynamic_partition.prefix" = "p",
"dynamic_partition.replication_allocation" = "tag.location.default: 2",
"dynamic_partition.buckets" = "32",
"dynamic_partition.create_history_partition" = "false",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"dynamic_partition.start_day_of_month" = "1",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "30000",
"group_commit_data_bytes" = "134217728"
);




-- drop table voc_imp_pub_consult
CREATE TABLE IF NOT EXISTS `voc_imp_pub_consult` (
                                                     `id` VARCHAR(40) NOT NULL COMMENT 'ID',
                                                     `data_create_time` DATETIME NOT NULL COMMENT '数据产生时间',
                                                     `data_update_time` DATETIME NULL COMMENT '数据更新时间',
                                                     `create_time` DATETIME NOT NULL COMMENT '数据抓取时间',
                                                     `data_id` VARCHAR(40) NOT NULL COMMENT '数据唯一标识',
                                                     `channel_code` VARCHAR(40) NOT NULL COMMENT '渠道编码',
                                                     `brand` VARCHAR(100) NULL COMMENT '品牌',
                                                     `series` VARCHAR(100) NULL COMMENT '车系',
                                                     `model` VARCHAR(100) NULL COMMENT '车型',
                                                     `is_outer` VARCHAR(4) NOT NULL COMMENT '是否往外数据',
                                                     `one_id` VARCHAR(50) NULL COMMENT '股份客户信息-one_id',
                                                     `id_car_no` VARCHAR(30) NULL COMMENT '客户证件好',
                                                     `mobile` VARCHAR(30) NULL COMMENT '客户手机号',
                                                     `email` VARCHAR(50) NULL COMMENT '客户邮箱',
                                                     `global_id` VARCHAR(50) NULL COMMENT 'SSO全局ID',
                                                     `user_id` VARCHAR(50) NULL COMMENT '用户标识',
                                                     `user_name` VARCHAR(100) NOT NULL COMMENT '用户名',
                                                     `vhl_id` VARCHAR(50) NULL COMMENT '车辆ID',
                                                     `vhl_vin` VARCHAR(50) NULL COMMENT '车辆车架号',
                                                     `dlr_id` VARCHAR(50) NULL COMMENT '股份售后经销商ID',
                                                     `dlr_code` VARCHAR(50) NULL COMMENT '股份售后经销商编码',
                                                     `dlr_type` VARCHAR(50) NOT NULL COMMENT '股份售后经销商类型',
                                                     `market_id` VARCHAR(50) NULL COMMENT '股份产品物理编码',
                                                     `title` TEXT NOT NULL COMMENT '标题',
                                                     `content` TEXT NOT NULL COMMENT '内容',
                                                     `is_wsater_army` VARCHAR(4) NULL COMMENT '是否水军',
                                                     `weight` INT NULL COMMENT '权重值',
                                                     `attrs` JSON NULL COMMENT '其他字段',
                                                     `attrs2` JSON NULL COMMENT '其他字段',
                                                     `attrs3` JSON NULL COMMENT '其他字段',
                                                     `session_id` VARCHAR(50) COMMENT '会话ID（群聊ID）',
)
    ENGINE = OLAP
    UNIQUE KEY(`id`,`data_create_time`)
COMMENT 'VOC-ODS数据归类表-咨询类(公域）'
AUTO PARTITION BY RANGE (date_trunc(`data_create_time`, 'MONTH')) ()
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
	"replication_num" = "3",
    -- 索引配置
    "bloom_filter_columns" = "data_id, create_time, channel_code",
    "storage_format" = "V2",
    "inverted_index_storage_format" = "V1",
    "compression" = "ZLIB"
);


-- drop table voc_imp_pub_post_comment
CREATE TABLE IF NOT EXISTS `voc_imp_pub_post_comment` (
                                                          `id` VARCHAR(40) NOT NULL COMMENT 'ID',
                                                          `data_create_time` DATETIME NOT NULL COMMENT '数据产生时间',
                                                          `data_update_time` DATETIME NULL COMMENT '数据更新时间',
                                                          `create_time` DATETIME NOT NULL COMMENT '数据抓取时间',
                                                          `data_id` VARCHAR(40) NOT NULL COMMENT '数据唯一标识',
                                                          `channel_code` VARCHAR(40) NOT NULL COMMENT '渠道编码',
                                                          `brand` VARCHAR(100) NULL COMMENT '品牌',
                                                          `series` VARCHAR(100) NULL COMMENT '车系',
                                                          `model` VARCHAR(100) NULL COMMENT '车型',
                                                          `is_outer` VARCHAR(4) NOT NULL COMMENT '是否往外数据',
                                                          `one_id` VARCHAR(50) NULL COMMENT '股份客户信息-one_id',
                                                          `id_car_no` VARCHAR(30) NULL COMMENT '客户证件好',
                                                          `mobile` VARCHAR(30) NULL COMMENT '客户手机号',
                                                          `email` VARCHAR(50) NULL COMMENT '客户邮箱',
                                                          `global_id` VARCHAR(50) NULL COMMENT 'SSO全局ID',
                                                          `user_id` VARCHAR(50) NULL COMMENT '用户标识',
                                                          `user_name` VARCHAR(100) NOT NULL COMMENT '用户名',
                                                          `vhl_id` VARCHAR(50) NULL COMMENT '车辆ID',
                                                          `vhl_vin` VARCHAR(50) NULL COMMENT '车辆车架号',
                                                          `dlr_id` VARCHAR(50) NULL COMMENT '股份售后经销商ID',
                                                          `dlr_code` VARCHAR(50) NULL COMMENT '股份售后经销商编码',
                                                          `dlr_type` VARCHAR(50) NOT NULL COMMENT '股份售后经销商类型',
                                                          `market_id` VARCHAR(50) NULL COMMENT '股份产品物理编码',
                                                          `title` TEXT NOT NULL COMMENT '标题',
                                                          `content` TEXT NOT NULL COMMENT '内容',
                                                          `is_wsater_army` VARCHAR(4) NULL COMMENT '是否水军',
                                                          `weight` INT NULL COMMENT '权重值',
                                                          `attrs` JSON NULL COMMENT '其他字段',
                                                          `attrs2` JSON NULL COMMENT '其他字段',
                                                          `attrs3` JSON NULL COMMENT '其他字段',
                                                          `is_main_post` VARCHAR(4) NOT NULL COMMENT '是否主贴',
                                                          `main_post_id` VARCHAR(40) NULL COMMENT '回帖时对应主贴ID',
                                                          `url` VARCHAR(512) NULL COMMENT '详情链接',
                                                          `view_count` VARCHAR(20) NULL COMMENT '浏览量or播放量',
                                                          `comment_count` VARCHAR(20) NULL COMMENT '评论量',
                                                          `like_count` VARCHAR(20) NULL COMMENT '点赞量',
                                                          `share_count` VARCHAR(20) NULL COMMENT '转发量',
                                                          `favorite_count` VARCHAR(20) NULL COMMENT '收藏量'
)
    ENGINE = OLAP
    UNIQUE KEY(`id`,`data_create_time`)
COMMENT '公域帖子评论增量表'
AUTO PARTITION BY RANGE (date_trunc(`data_create_time`, 'MONTH')) ()
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
    -- 索引配置
    "bloom_filter_columns" = "data_id, create_time, channel_code",
    "storage_format" = "V2",
    "inverted_index_storage_format" = "V1",
    "compression" = "ZLIB"
);




-- drop table voc_imp_pub_questionnaire
CREATE TABLE IF NOT EXISTS `voc_imp_pub_questionnaire` (
                                                           `id` VARCHAR(40) NOT NULL COMMENT 'ID',
                                                           `data_create_time` DATETIME NOT NULL COMMENT '数据产生时间',
                                                           `data_update_time` DATETIME NULL COMMENT '数据更新时间',
                                                           `create_time` DATETIME NOT NULL COMMENT '数据抓取时间',
                                                           `data_id` VARCHAR(40) NOT NULL COMMENT '数据唯一标识',
                                                           `channel_code` VARCHAR(40) NOT NULL COMMENT '渠道编码',
                                                           `brand` VARCHAR(100) NULL COMMENT '品牌',
                                                           `series` VARCHAR(100) NULL COMMENT '车系',
                                                           `model` VARCHAR(100) NULL COMMENT '车型',
                                                           `is_outer` VARCHAR(4) NOT NULL COMMENT '是否往外数据',
                                                           `one_id` VARCHAR(50) NULL COMMENT '股份客户信息-one_id',
                                                           `id_car_no` VARCHAR(30) NULL COMMENT '客户证件好',
                                                           `mobile` VARCHAR(30) NULL COMMENT '客户手机号',
                                                           `email` VARCHAR(50) NULL COMMENT '客户邮箱',
                                                           `global_id` VARCHAR(50) NULL COMMENT 'SSO全局ID',
                                                           `user_id` VARCHAR(50) NULL COMMENT '用户标识',
                                                           `user_name` VARCHAR(100) NOT NULL COMMENT '用户名',
                                                           `vhl_id` VARCHAR(50) NULL COMMENT '车辆ID',
                                                           `vhl_vin` VARCHAR(50) NULL COMMENT '车辆车架号',
                                                           `dlr_id` VARCHAR(50) NULL COMMENT '股份售后经销商ID',
                                                           `dlr_code` VARCHAR(50) NULL COMMENT '股份售后经销商编码',
                                                           `dlr_type` VARCHAR(50) NOT NULL COMMENT '股份售后经销商类型',
                                                           `market_id` VARCHAR(50) NULL COMMENT '股份产品物理编码',
                                                           `title` TEXT NOT NULL COMMENT '标题',
                                                           `content` TEXT NOT NULL COMMENT '内容',
                                                           `is_wsater_army` VARCHAR(4) NULL COMMENT '是否水军',
                                                           `weight` INT NULL COMMENT '权重值',
                                                           `attrs` JSON NULL COMMENT '其他字段',
                                                           `attrs2` JSON NULL COMMENT '其他字段',
                                                           `attrs3` JSON NULL COMMENT '其他字段',
                                                           `type` VARCHAR(50) NOT NULL COMMENT '题目类型',
                                                           `biz_type` VARCHAR(100) NULL COMMENT '业务类型',
                                                           `biz_scenario` VARCHAR(100) NULL COMMENT '业务场景',
                                                           `quest_name` VARCHAR(200) NULL COMMENT '问卷名称',
                                                           `quest_id` VARCHAR(50) NULL COMMENT '问卷ID',
                                                           `quest_url` VARCHAR(512) NULL COMMENT '问卷url',
                                                           `quest_answer_score` VARCHAR(20) NULL COMMENT '回答分数',
                                                           `url` VARCHAR(512) NULL COMMENT '详情链接'
)
    ENGINE = OLAP
    UNIQUE KEY(`id`,`data_create_time`)
COMMENT 'VOC问卷增量表'
AUTO PARTITION BY RANGE (date_trunc(`data_create_time`, 'MONTH')) ()
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
	"replication_num" = "3",
    -- 索引配置
    "bloom_filter_columns" = "data_id, create_time, channel_code",
    "storage_format" = "V2",
    "inverted_index_storage_format" = "V1",
    "compression" = "ZLIB"
);




-- drop table voc_imp_pub_questionnaire
CREATE TABLE IF NOT EXISTS `voc_imp_pub_sphere_upserts` (
                                                            `id` VARCHAR(40) NOT NULL COMMENT 'ID',
                                                            `create_time` DATETIME NOT NULL COMMENT '数据抓取时间（导入时写入当前时间）',
                                                            `metadata` TEXT COMMENT '原始元数据',
                                                            `raw_data` TEXT COMMENT '原始数据内容',
                                                            _kafka_metadata TEXT COMMENT 'kafka原始数据内容'
)
    ENGINE = OLAP
    UNIQUE KEY(`id`,`create_time`)
COMMENT 'VOC问卷增量表'
AUTO PARTITION BY RANGE (date_trunc(`create_time`, 'MONTH')) ()
DISTRIBUTED BY HASH(`id`) BUCKETS 4
PROPERTIES (
	"replication_num" = "3",
    -- 索引配置
    "bloom_filter_columns" = "create_time",
    "storage_format" = "V2",
    "inverted_index_storage_format" = "V1",
    "compression" = "ZLIB"
);

-- drop table   voc_imp_hudi_ca_es_bdu_bdu_netopinion_complaint_miaozhen
CREATE TABLE voc_imp_hudi_ca_es_bdu_bdu_netopinion_complaint_miaozhen (
                                                                          hash_code VARCHAR(100) COMMENT '唯一标识',
                                                                          app_name_final STRING COMMENT '网络来源',
                                                                          brand STRING COMMENT '车企',
                                                                          car_brand STRING COMMENT '品牌',
                                                                          content STRING COMMENT '正文内容',
                                                                          feel_tag STRING COMMENT '形式判断',
                                                                          news_author_final STRING COMMENT '发布者',
                                                                          news_posttime DATETIME COMMENT '发布时间',
                                                                          news_title STRING COMMENT '标题',
                                                                          news_url STRING COMMENT '链接',
                                                                          report_date DATE COMMENT '日报时间',
                                                                          series STRING COMMENT '车型',
                                                                          create_time DATETIME COMMENT '创建时间',
                                                                          job_nm STRING COMMENT '任务名称',
                                                                          batch_dt STRING COMMENT '批次时间',
                                                                          process_attention STRING COMMENT '过程关注度',
                                                                          ds STRING COMMENT '分区',
                                                                          `insert_dt` datetime not null default CURRENT_TIMESTAMP
)
    UNIQUE   KEY(hash_code)
COMMENT 'VOC-每天增量存储数据给voc'
DISTRIBUTED BY HASH (`hash_code`) BUCKETS 2
PROPERTIES (
    "replication_num" = "3"
)
;




CREATE TABLE `voc_anal_flow_pre_rules_abandon` (
                                                   `id` varchar(40) NOT NULL COMMENT "主键",
                                                   `insert_dt` datetime not null default CURRENT_TIMESTAMP
) ENGINE=OLAP
DUPLICATE KEY(`id`)
COMMENT "数据清洗-前置处理后数据记录表"
AUTO PARTITION BY RANGE (date_trunc(insert_dt, 'year'))()
DISTRIBUTED BY HASH (`id`) BUCKETS 2
PROPERTIES (
	"replication_num" = "3",
	"storage_format" = "default",
	"compression" = "ZLIB"
);






CREATE TABLE `voc_anal_flow_mate_data_status` (
                                                  `id` BIGINT NOT NULL AUTO_INCREMENT,
                                                  `data` json NOT NULL COMMENT "主键",
                                                  `insert_dt` datetime not null default CURRENT_TIMESTAMP
) ENGINE=OLAP
DUPLICATE KEY(`id`)
COMMENT "数据清洗-前置处理后数据记录表"
AUTO PARTITION BY RANGE (date_trunc(insert_dt, 'month'))()
DISTRIBUTED BY HASH (`id`) BUCKETS 4
PROPERTIES (
	"replication_num" = "3",
	"storage_format" = "default",
	"compression" = "ZLIB"
);





-- drop table  voc_ins_api_reqeust_record

CREATE TABLE IF NOT EXISTS voc_ins_api_reqeust_record (
                                                          id varchar(40) NOT NULL COMMENT '主键ID',
    create_time DATETIME not null COMMENT '创建时间',
    log_type int(11) COMMENT '日志类型（1登录日志，2操作日志）',
    log_content STRING COMMENT '操作详细日志',
    operate_type int(11) COMMENT '操作类型（1查询，2添加，3修改，4删除,5导入，6导出）',
    userid varchar(40) NOT NULL COMMENT '操作人用户账户',
    username STRING COMMENT '操作人用户名称',
    ip varchar(15) COMMENT 'IP',
    method STRING COMMENT '请求方法（Java类方法）',
    request_url STRING COMMENT '请求路径',
    request_param STRING COMMENT '请求参数（JSON字符串）',
    request_type STRING COMMENT '请求类型（如POST）',
    cost_time BIGINT COMMENT '耗时（毫秒）',
    create_by varchar(40) COMMENT '创建人',
    update_by varchar(40) COMMENT '更新人',
    update_time DATETIME COMMENT '更新时间',
    app_id varchar(20) COMMENT '应用ID',
    code varchar(10) COMMENT '返回码',
    message TEXT COMMENT '返回消息',
    tid STRING COMMENT '链路追踪ID',
    insert_dt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据插入时间'
    )ENGINE = OLAP
    DUPLICATE KEY(id)
    COMMENT "接口请求调用记录表"
    AUTO PARTITION BY RANGE (date_trunc(create_time, 'month'))()
    DISTRIBUTED BY HASH(id) BUCKETS 2
    PROPERTIES(
                  "replication_allocation" = "tag.location.default: 1",
                  "min_load_replica_num" = "-1",
                  "bloom_filter_columns" = "userid,create_time,code,message",
                  "dynamic_partition.enable" = "true",
                  "dynamic_partition.time_unit" = "month",
                  "dynamic_partition.time_zone" = "Asia/Shanghai",
                  "dynamic_partition.start" = "-12",
                  "dynamic_partition.end" = "1",
                  "dynamic_partition.prefix" = "p"
              );






drop TABLE voc_anal_flow_real_time;
CREATE TABLE voc_anal_flow_real_time
(
    `window_ts`    			datetime not null,
    `is_outer`        		varchar(1),
    `type`					int,
    `insert_dt`    			datetime replace,
    `abandon`    			int replace,
    `publish_time`    		datetime replace,
    `data_id`				varchar(40) replace,
    data_id_bitmap			bitmap bitmap_union
)
AGGREGATE KEY(`window_ts`, `is_outer`, `type`)
AUTO PARTITION BY RANGE (date_trunc(`window_ts`, 'day'))()
DISTRIBUTED BY HASH(window_ts) BUCKETS 4
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"bloom_filter_columns" = "window_ts,type",
"min_load_replica_num" = "-1",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "day",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-3",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p"
);
ALTER TABLE voc_anal_flow_real_time SET ("bloom_filter_columns" = "window_ts,type");






CREATE TABLE `voc_anal_flow_to_model_data` (
                                               `id` varchar(40) NOT NULL COMMENT "主键ID-topic_id",
                                               `create_time` datetime NOT NULL COMMENT "数据抓取时间",
                                               `data_id` varchar(40) NULL COMMENT "数据唯一标识",
                                               `channel_code` varchar(40) NULL COMMENT "渠道编码-dataSource",
                                               `publish_time_` bigint NOT NULL COMMENT "数据产生时间",
                                               `publish_time` datetime NOT NULL COMMENT "数据产生时间",
                                               `work_id` varchar(40) NULL COMMENT "最终标识",
                                               `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=OLAP
UNIQUE KEY(`id`, `create_time`)
COMMENT 'VOC-推送模型数据'
AUTO PARTITION BY RANGE (date_trunc(`create_time`, 'month'))()
DISTRIBUTED BY HASH(`id`) BUCKETS 16
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "data_id, publish_time,insert_dt",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "month",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-12",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p",
"dynamic_partition.buckets" = "16",
"dynamic_partition.create_history_partition" = "false",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"group_commit_interval_ms" = "30000",
"group_commit_data_bytes" = "264217728"
);




CREATE TABLE `voc_anal_di_raw_public_domain_data_full` (
  `id` bigint NOT NULL AUTO_INCREMENT(1) COMMENT "主键ID",
  `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `data_type` int NOT NULL COMMENT "数据源类型",
  `data` json NULL COMMENT "核心数据",
  `ext_attrs` text NULL COMMENT "通用扩展字段"
) ENGINE=OLAP
UNIQUE KEY(`id`, `insert_dt`)
COMMENT 'VOC-公域数据接收表（RAW）'
AUTO PARTITION BY RANGE (date_trunc(`insert_dt`, 'day'))()
DISTRIBUTED BY HASH(`id`) BUCKETS 8
PROPERTIES (
"replication_allocation" = "tag.location.default: 1",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "data_type",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "day",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-7",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p",
"dynamic_partition.buckets" = "8",
"dynamic_partition.create_history_partition" = "false",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "30000",
"group_commit_data_bytes" = "264217728",
"enable_mow_light_delete" = "false"
);



CREATE TABLE `voc_anal_di_pub_domain_data_finished_record` (
  `data_id` varchar(40) NOT NULL COMMENT "原数据标识",
  `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "异常数据创建时间",
  `id` varchar(40) NOT NULL COMMENT "主键",
  `work_id` varchar(40) NULL COMMENT "主键",
  `channel_type` varchar(60) NULL COMMENT "渠道类型",
  `retry_count` int NOT NULL COMMENT "重试次数",
  `error_code` varchar(50) NULL COMMENT "异常编码",
  `error_msg` text NULL COMMENT "异常描述",
  `data` json NULL COMMENT "原数据",
  `last_exec_time` datetime NULL COMMENT "最后执行时间",
  `status` int NOT NULL COMMENT "状态",
  `tid` varchar(55) NULL COMMENT "链路标识"
) ENGINE=OLAP
UNIQUE KEY(`data_id`, `insert_dt`)
COMMENT '数据接收-接收数据处理记录表'
AUTO PARTITION BY RANGE (date_trunc(`insert_dt`, 'day'))()
DISTRIBUTED BY HASH(`data_id`) BUCKETS 8
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "DAY",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-7",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p",
"dynamic_partition.buckets" = "8",
"dynamic_partition.create_history_partition" = "false",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"compression" = "LZ4",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "10000",
"group_commit_data_bytes" = "134217728",
"enable_mow_light_delete" = "false"
);




-- voc_ms_td.voc_anal_di_stg_mate_data_pub_m_inc definition

CREATE TABLE `voc_anal_di_stg_mate_data_pub_m_inc` (
                                                       `id` varchar(40) NOT NULL COMMENT "主键ID",
                                                       `insert_dt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                       `create_time` datetime NOT NULL COMMENT "数据抓取时间",
                                                       `data_create_time` datetime NOT NULL COMMENT "数据产生时间",
                                                       `content_type` varchar(40) NULL COMMENT "内容类型：order(工单), post(帖子评论), opinion(意见反馈), questionnaire(问卷), consult(咨询)",
                                                       `data_update_time` datetime NULL COMMENT "数据更新时间",
                                                       `data_id` varchar(40) NULL COMMENT "数据唯一标识",
                                                       `channel_code` varchar(40) NULL COMMENT "渠道编码",
                                                       `brand` text NULL COMMENT "品牌",
                                                       `series` text NULL COMMENT "车系",
                                                       `model` text NULL COMMENT "原始观点",
                                                       `is_outer` varchar(4) NULL COMMENT "是否往外数据",
                                                       `one_id` text NULL COMMENT "股份客户信息-one_id",
                                                       `id_car_no` text NULL COMMENT "客户证件号",
                                                       `mobile` text NULL COMMENT "客户手机号",
                                                       `email` text NULL COMMENT "客户邮箱",
                                                       `global_id` text NULL COMMENT "SSO全局ID",
                                                       `user_id` text NULL COMMENT "用户标识",
                                                       `user_name` text NULL COMMENT "用户名",
                                                       `vhl_id` text NULL COMMENT "车辆ID",
                                                       `vhl_vin` text NULL COMMENT "车辆车架号",
                                                       `dlr_id` text NULL COMMENT "股份售后经销商ID",
                                                       `dlr_code` text NULL COMMENT "股份售后经销商编码",
                                                       `dlr_type` text NULL COMMENT "股份售后经销商类型",
                                                       `market_id` text NULL COMMENT "股份产品物理编码",
                                                       `title` text NULL COMMENT "标题",
                                                       `content` text NULL COMMENT "内容正文",
                                                       `is_wsater_army` varchar(4) NULL COMMENT "是否水军",
                                                       `weight` int NULL COMMENT "权重值",
                                                       `attrs` json NULL COMMENT "业务系统其他字段",
                                                       `attrs2` json NULL COMMENT "业务系统其他字段",
                                                       `attrs3` json NULL COMMENT "业务系统其他字段",
                                                       `work_id` varchar(40) NULL COMMENT "最终标识",
                                                       `done` int NULL COMMENT "是否完成计算：1-是，0-否",
                                                       `model_type` int NULL COMMENT "模型类型",
                                                       `ds` varchar(20) NULL COMMENT "分区字段（备用）",
                                                       `is_deleted` tinyint NOT NULL DEFAULT "0"
) ENGINE=OLAP
UNIQUE KEY(`id`, `insert_dt`)
COMMENT 'VOC-ODS原始数据记录表（优化版）'
AUTO PARTITION BY RANGE (date_trunc(`insert_dt`, 'day'))()
DISTRIBUTED BY HASH(`id`) BUCKETS 16
PROPERTIES (
"replication_allocation" = "tag.location.default: 2",
"min_load_replica_num" = "-1",
"bloom_filter_columns" = "insert_dt, data_id, data_create_time",
"is_being_synced" = "false",
"dynamic_partition.enable" = "true",
"dynamic_partition.time_unit" = "day",
"dynamic_partition.time_zone" = "Asia/Shanghai",
"dynamic_partition.start" = "-7",
"dynamic_partition.end" = "1",
"dynamic_partition.prefix" = "p",
"dynamic_partition.replication_allocation" = "tag.location.default: 2",
"dynamic_partition.buckets" = "16",
"dynamic_partition.create_history_partition" = "false",
"dynamic_partition.history_partition_num" = "-1",
"dynamic_partition.hot_partition_num" = "0",
"dynamic_partition.reserved_history_periods" = "NULL",
"dynamic_partition.storage_policy" = "",
"storage_medium" = "hdd",
"storage_format" = "V2",
"inverted_index_storage_format" = "V1",
"enable_unique_key_merge_on_write" = "true",
"light_schema_change" = "true",
"disable_auto_compaction" = "false",
"enable_single_replica_compaction" = "false",
"group_commit_interval_ms" = "30000",
"group_commit_data_bytes" = "264217728",
"enable_mow_light_delete" = "false"
);