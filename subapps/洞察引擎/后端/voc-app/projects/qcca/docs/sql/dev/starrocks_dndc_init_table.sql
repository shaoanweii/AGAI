
-- select * from voc_jdbc.vdp_ms_be.ins_car_series_info
-- drop CATALOG voc_jdbc
CREATE EXTERNAL CATALOG voc_jdbc
PROPERTIES
(
    "type" = "jdbc",
    "user" = "root",
    "password" = "L7bzd1gmm+db",
    "jdbc_uri" = "jdbc:mysql://172.16.80.17:30849",
    "driver_url" = "https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.28/mysql-connector-java-8.0.28.jar",
    "driver_class" = "com.mysql.cj.jdbc.Driver"
);





-- drop table dim_voc_manager_user_info
CREATE TABLE `dim_voc_big_v_user_info`(
	`user_id`  string NOT NULL  COMMENT '用户标识 [channel_code_user_id]',
	int_ BIGINT AUTO_INCREMENT
) ENGINE=OLAP
PRIMARY KEY(`user_id`)
COMMENT "大V关注集合"
ORDER BY(`user_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);



-- drop table dim_voc_manager_user_info
CREATE TABLE `dim_voc_manager_user_info`(
	`user_id`  string NOT NULL  COMMENT '用户标识 [channel_code_user_id]',
	int_ BIGINT AUTO_INCREMENT
) ENGINE=OLAP
PRIMARY KEY(`user_id`)
COMMENT "领导关注集合"
ORDER BY(`user_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);



CREATE TABLE `dim_voc_cust_info`(
`oneid` string COMMENT 'OneID',
`cust_classify` string COMMENT '客户类型',
`id_card_type` string COMMENT '证件类型',
`id_card_no` string COMMENT '证件号码(加密)',
`global_id` string COMMENT 'sso全局ID',
`email` string COMMENT '邮箱',
`mobile` string COMMENT '手机号',
`cust_nm` string COMMENT '客户姓名(加密)',
`gender` string COMMENT '性别',
`age` string COMMENT '年龄',
`age_group` string COMMENT '年龄段',
`birthday_dt` string COMMENT '出生日期',
`birthday` string COMMENT '生日',
`born_years` string COMMENT '出生年代',
`life_stage` string COMMENT '人生阶段',
`constellation` string COMMENT '星座',
`zodiac` string COMMENT '生肖',
`high_educaion` string COMMENT '最高学历',
`marriage_statue` string COMMENT '婚姻状况',
`hukou_prov_cd` string COMMENT '户籍地_省份_编码',
`hukou_prov_nm` string COMMENT '户籍地_省份',
`hukou_city_cd` string COMMENT '户籍地_城市_编码',
`hukou_city_nm` string COMMENT '户籍地_城市',
`hukou_cty_cd` string COMMENT '户籍地_区县_编码',
`hukou_cty_nm` string COMMENT '户籍地_区县',
`lived_prov_cd` string COMMENT '居住地_省份_编码',
`lived_prov_nm` string COMMENT '居住地_省份',
`lived_city_cd` string COMMENT '居住地_城市_编码',
`lived_city_nm` string COMMENT '居住地_城市',
`lived_cty_cd` string COMMENT '居住地_区县_编码',
`lived_cty_nm` string COMMENT '居住地_区县',
`lived_addr` string COMMENT '居住地址',
`profession` string COMMENT '职业',
`family_income` string COMMENT '家庭月收入',
`cust_type` string COMMENT '客户分类',
`is_exchange_flg` bigint COMMENT '是否换购',
`is_re_purchase_flg` bigint COMMENT '是否增换购',
`is_recommend_flg` bigint COMMENT '是否推荐购',
`is_car_owner_flg` bigint COMMENT '是否车主',
`is_deal_flg` bigint COMMENT '是否成交',
`is_uni_owner_flg` bigint COMMENT '是否UNI车主',
`is_jc_owner_flg` bigint COMMENT '是否乘用车车主',
`is_wc_owner_flg` bigint COMMENT '是否欧尚车主',
`is_ev_owner_flg` bigint COMMENT '是否新能源车主',
`is_qxc_owner_flg` bigint COMMENT '是否凯程车主',
`purchase_car_qty` bigint COMMENT '购车数量',
`purchase_car_times` bigint COMMENT '购车次数',
`lately_purchase_time` string COMMENT '最近购车时间',
`his_consume_amt` decimal(32,8) COMMENT '历史消费金额',
`is_member_flg` bigint COMMENT '是否会员',
`member_register_mth` string COMMENT '会员注册时间',
`mem_activity` string COMMENT '会员活跃度',
`is_birthday_1day_flg` bigint COMMENT '明日是否生日',
`is_birthday_30day_flg` bigint COMMENT '30日内是否生日',
`is_birthday_60day_flg` bigint COMMENT '60日内是否生日',
`dw_insert_time` string COMMENT '数仓插入时间',
`dw_update_time` string COMMENT '数仓更新时间',
`batch_dt` string COMMENT '批次时间',
`job_nm` string COMMENT '作业名称'
) ENGINE=OLAP
PRIMARY KEY(`oneid`)
COMMENT "客户数据维表"
DISTRIBUTED BY HASH(`oneid`)  BUCKETS 8
ORDER BY(`oneid`,`mobile`,`id_card_no`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);


-- drop  TABLE `dim_voc_dealer_info`
CREATE TABLE `dim_voc_dealer_info`(
                                       `dealer_code` string not null COMMENT '经销商代码',
                                       `dealer_id` string COMMENT '经销商代理键id',
                                       `erp_cd` string COMMENT 'ERP编码',
                                       `dealer_name` string COMMENT '经销商名称',
                                       `dealer_shortname` string COMMENT '经销商名称简称',
                                       `dept_cd` string COMMENT '部门编码',
                                       `dept_nm` string COMMENT '部门名称',
                                       `seq` string COMMENT '序列',
                                       `store_lvl` string COMMENT '经销商店级别(1:一级店,2:二级店)',
                                       `invest_group_cd` string COMMENT '同投商代码（隶属关系）投资商集团/凯程投资主体(l1_dealer)',
                                       `invest_group_nm` string COMMENT '同投商名称（隶属关系）投资商集团/凯程投资主体(l1_dealer)',
                                       `invest_cd` string COMMENT '投资商代码/凯程虚拟一级经销商(l2_dealer)',
                                       `invest_nm` string COMMENT '投资商名称/凯程虚拟一级经销商(l2_dealer)',
                                       `s1_dlr_cd` string COMMENT '一级渠道代码/凯程一级店(中心店)(l3_dealer)',
                                       `s1_dlr_nm` string COMMENT '一级渠道名称/凯程一级店(中心店)(l3_dealer)',
                                       `s2_dlr_cd` string COMMENT '二级渠道代码/凯程二级店or触点(l4_dealer)',
                                       `s2_dlr_nm` string COMMENT '二级渠道名称/凯程二级店or触点(l4_dealer)',
                                       `main_invest_cd` string COMMENT '同投商代码（占股关系）投资商主体/凯程虚拟一级商',
                                       `main_invest_nm` string COMMENT '同投商名称（占股关系）投资商主体/凯程虚拟一级商',
                                       `main_dlr_cd` string COMMENT '同投店代码（占股关系）主要在品牌管理该关系',
                                       `main_dlr_nm` string COMMENT '同投店名称（占股关系）主要在品牌管理该关系',
                                       `ord_flg` string COMMENT '是否订单门店',
                                       `dlv_flg` string COMMENT '是否交付门店',
                                       `svs_flg` string COMMENT '是否服务门店',
                                       `paint_flg` string COMMENT '是否有钣喷能力',
                                       `valid_flg` string COMMENT '是否正常经营',
                                       `quit_flg` string COMMENT '是否退网',
                                       `rez_flg` string COMMENT '是否接受服务线索预约',
                                       `dlr_type_group_cd` string COMMENT '经销商类型分组代码',
                                       `dlr_type_group_nm` string COMMENT '经销商类型分组名称',
                                       `dlr_type_cd` string COMMENT '经销商类型代码',
                                       `dlr_type_nm` string COMMENT '经销商类型名称',
                                       `chn_type_cd` string COMMENT '渠道类型代码',
                                       `chn_type_nm` string COMMENT '渠道类型名称',
                                       `chn_lvl_cd` string COMMENT '渠道等级代码',
                                       `chn_lvl_nm` string COMMENT '渠道等级名称',
                                       `image_cd` string COMMENT '门店形象代码',
                                       `image_nm` string COMMENT '门店形象名称',
                                       `image_lvl_cd` string COMMENT '门店形象级别代码',
                                       `image_lvl_nm` string COMMENT '门店形象级别名称',
                                       `store_front_function` string COMMENT '门店能力',
                                       `current_state` string COMMENT '当前状态',
                                       `operation_type` string COMMENT '运营类型',
                                       `area` string COMMENT '大区',
                                       `war_zone_cd` string COMMENT '战区代码',
                                       `war_zone_nm` string COMMENT '战区名称',
                                       `war_zone_part` string COMMENT '战区分区',
                                       `war_zone_part_user_cd` string COMMENT '战区分区人员编码',
                                       `war_zone_part_user_nm` string COMMENT '战区分区人员名称',
                                       `sdu` string COMMENT 'sdu名称',
                                       `sdu_link_man` string COMMENT 'sdu联系人',
                                       `province_code` string COMMENT '省份代码',
                                       `province_name` string COMMENT '省份名称',
                                       `city_code` string COMMENT '城市代码',
                                       `city_name` string COMMENT '城市名称',
                                       `cty_cd` string COMMENT '区县代码',
                                       `cty_nm` string COMMENT '区县名称',
                                       `lng` string COMMENT '经度',
                                       `lat` string COMMENT '纬度',
                                       `addr` string COMMENT '详细地址',
                                       `bank_nm` string COMMENT '开户行名称',
                                       `bank_acct_no` string COMMENT '开户行账号',
                                       `manager_nm` string COMMENT '店长名称',
                                       `manager_tel` string COMMENT '店长联系电话',
                                       `hotline` string COMMENT '服务热线',
                                       `emergency_tel` string COMMENT '24小时救援电话',
                                       `in_net_dt` string COMMENT '入网日期',
                                       `out_net_dt` string COMMENT '退网日期',
                                       `network_type` string COMMENT '在网类型',
                                       `ord_dlr_cd` string COMMENT '对应的订单店代码',
                                       `dlv_dlr_cd` string COMMENT '对应的交付店代码',
                                       `svs_dlr_cd` string COMMENT '对应的服务店代码',
                                       `biz_cd` string COMMENT '事业部代码',
                                       `biz_nm` string COMMENT '事业部名称',
                                       `uni_dlr_cd_flg` string COMMENT '首选经销商代码标识'


) ENGINE=OLAP
    PRIMARY KEY(`dealer_code`)
COMMENT "经销商维（dim_chn_dlr_zip_d_full）"
DISTRIBUTED BY HASH(`dealer_code`) BUCKETS 8
ORDER BY(`dealer_code`, `province_code`,  `city_code`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);




-- drop  TABLE `dim_voc_vehicle_info`
CREATE TABLE `dim_voc_vehicle_info`(
            `vin`  string  NOT NULL COMMENT '车架号',
            `prod_code`  string   COMMENT '产品编码（三段式）',
            `prod_name`  string   COMMENT '产品名称',
            `mdl_code`  string  NOT NULL COMMENT '车型代码',
            `mdl_name`  string  NOT NULL COMMENT '车型名称',
            `series_code`  string  NOT NULL COMMENT '车系代码',
            `series_name`  string  NOT NULL COMMENT '车系名称',
            `opt_code`  string  NOT NULL COMMENT '配置代码',
            `opt_name`  string  NOT NULL COMMENT '配置名称',
            `col_code`  string   COMMENT '颜色代码',
            `col_name`  string  NOT NULL COMMENT '颜色名称',
            `eng_clsf`  string  NOT NULL COMMENT '动力系列大类',
            `eng_seris`  string   COMMENT '动力系列小类',
            `eng_mdl`  string  NOT NULL COMMENT '发动机型号',
            `dis_mt`  string  NOT NULL COMMENT '排量',
            `dis_ch`  string   COMMENT '排放',
            `trans_clsf`  string   COMMENT '变速器类型',
            `trans_form`  string   COMMENT '变速器型式',
            `custom_code`  string   COMMENT '定制编码',
            `veh_type`  string   COMMENT '车辆类型（出口车、领用车、代工车、商用车）',
            `vcl_num`  string  NOT NULL COMMENT '产量',
            `sbu_code`  string  NOT NULL COMMENT '经营单位编码',
            `sbu_name`  string  NOT NULL COMMENT '经营单位名称',
            `continent`  string  NOT NULL COMMENT '大洲',
            `is_abroad`  string  NOT NULL COMMENT '国内国外',
            `cntry_code3`  string  NOT NULL COMMENT '国家地区三位字母码',
            `cntry_name`  string  NOT NULL COMMENT '国家地区中文名称',
            `cntry_eng`  string  NOT NULL COMMENT '国家地区英文名称',
            `plnt_code`  string  NOT NULL COMMENT '标准工厂编码',
            `plnt_name`  string  NOT NULL COMMENT '标准工厂名称',
            `product_date`  string   COMMENT '生产日期',
            `offline_date`  string   COMMENT '总装下线时间',
            `rtn_veh_date`  string   COMMENT '退车时间',
            `src_sys`  string   COMMENT '来源系统名称',
            `src_sys_id`  string   COMMENT '来源系统id'
) ENGINE=OLAP
PRIMARY KEY(`vin`)
COMMENT "车辆维表（dim_maf_veh_d_full）"
DISTRIBUTED BY HASH(`vin`) BUCKETS 8
ORDER BY(`vin`, `prod_code`,  `series_code`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);


-- drop  TABLE `dim_voc_car_series_info`
CREATE TABLE `dim_voc_car_series_info`(
                                           `prd_wid`  string  NOT NULL COMMENT '车型配置代理键',
                                           `grp_nm`  string   COMMENT '集团简称',
                                           `enterp_nm`  string   COMMENT '企业简称',
                                           `enterp_bd`  string   COMMENT '企业品牌',
                                           `bus_bd_sec_cls`  string   COMMENT '业务品牌',
                                           `bus_bd_cd`  string   COMMENT '业务品牌代码',
                                           `prod_bd_seq`  string   COMMENT '产品序列',
                                           `prod_bd_cd`  string   COMMENT '车系代码',
                                           `prod_bd`  string   COMMENT '车系',
                                           `prod_bd_en`  string   COMMENT '车系英文',
                                           `prod_nm_cd`  string   COMMENT '产品名称代码',
                                           `prod_nm`  string   COMMENT '产品名称',
                                           `prod_nm_en`  string   COMMENT '产品名称英文',
                                           `cfgtn_nm`  string   COMMENT '配置名称',
                                           `annual_mdl`  string   COMMENT '年度款型',
                                           `model_ver_nm`  string   COMMENT '版本名称',
                                           `model_ver_cd`  string   COMMENT '版本代码',
                                           `time_mkt`  string   COMMENT '上市时间',
                                           `inner_clr_nm`  string   COMMENT '内饰颜色名称',
                                           `inner_clr_cd`  string   COMMENT '内饰颜色编码',
                                           `country`  string   COMMENT '国别',
                                           `sal_deprtmt`  string   COMMENT '销售部门',
                                           `proj_cd`  string   COMMENT '研发代号',
                                           `sbu_name_full`  string   COMMENT '经营单位',
                                           `sbu_name`  string   COMMENT '经营单位简称',
                                           `sbu_code`  string   COMMENT '经营单位代码',
                                           `bd_clsf`  string   COMMENT '车身类型',
                                           `seg_mt`  string   COMMENT '细分市场',
                                           `pow_clsf`  string   COMMENT '动力类型',
                                           `fu_clsf`  string   COMMENT '燃料类型',
                                           `pub_cd`  string   COMMENT '车型号',
                                           `pub_nm`  string   COMMENT '车型名称',
                                           `max_ms`  string   COMMENT '最大允许总质量',
                                           `range_condition`  string   COMMENT '续使里程工况',
                                           `cltc_mil`  string   COMMENT '续驶里程',
                                           `ovr_len`  string   COMMENT '产品长度',
                                           `bd_form`  string   COMMENT '车身型式',
                                           `driv_big_clsf`  string   COMMENT '驱动方式大类',
                                           `driv_clsf`  string   COMMENT '驱动方式小类',
                                           `driv_motor_mdl`  string   COMMENT '驱动电机型号',
                                           `trans_clsf`  string   COMMENT '变速器类型',
                                           `trans_form`  string   COMMENT '变速器型式',
                                           `eng_clsf`  string   COMMENT '动力系列大类',
                                           `eng_seris`  string   COMMENT '动力系列小类',
                                           `eng_mdl`  string   COMMENT '发动机型号',
                                           `dis_mt`  string   COMMENT '排量',
                                           `dis_ch`  string   COMMENT '排放',
                                           `modl_st`  string   COMMENT '车型状态号',
                                           `modl_st_nm`  string   COMMENT '车型状态名称',
                                           `status`  string   COMMENT '整车状态号',
                                           `cfg_level`  string   COMMENT '配置等级',
                                           `seat_num`  string   COMMENT '座位数',
                                           `manu_bs_cd`  string   COMMENT '制造基地代码',
                                           `manu_bs`  string   COMMENT '制造基地',
                                           `std_plnt_code`  string   COMMENT '标准工厂编码',
                                           `std_plnt_name`  string   COMMENT '标准工厂名称',
                                           `in_prdtn`  string   COMMENT '是否在产',
                                           `in_sold`  string   COMMENT '是否在售',
                                           `enable_flag`  string   COMMENT '是否最新版本',
                                           `start_date_active`  string   COMMENT '开始有效时间',
                                           `end_date_active`  string   COMMENT '结束有效时间'
) ENGINE=OLAP
    PRIMARY KEY(`prd_wid`)
COMMENT "车系维（dim_prd_car_st_full）"
DISTRIBUTED BY HASH(`prd_wid`) BUCKETS 8
ORDER BY(`prd_wid`, `prod_bd_cd`,  `bus_bd_cd`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);









CREATE TABLE `dwd_voc_processing_data_record` (
  `id` varchar(40) NOT NULL DEFAULT (uuid()) COMMENT "主键",
  `data_id` varchar(40) NOT NULL COMMENT "原数据标识",
  `work_id` varchar(40) NULL COMMENT "主键",
  `channel_type` varchar(30) NOT NULL COMMENT "渠道类型",
  `retry_count` int(11) NOT NULL COMMENT "重试次数",
  `error_code` varchar(10) NULL COMMENT "异常编码",
  `error_msg` string NULL COMMENT "异常描述",
  `data` json NULL COMMENT "原数据",
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT "异常数据创建时间",
  `last_exec_time` datetime NULL COMMENT "最后执行时间",
  `status` int(11) NOT NULL COMMENT "数据状态",
  `tid` string NULL COMMENT "链路标识"


) ENGINE=OLAP
PRIMARY KEY(`id`,`data_id`)
COMMENT "车辆信息维表"
DISTRIBUTED BY HASH(`id`)  BUCKETS 8
ORDER BY(`data_id`，`create_time`,`channel_type`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);





-- drop TABLE `dwd_voc_post_rules_result_data`
CREATE TABLE `dwd_voc_post_rules_result_data` (
  `id` varchar(40) NOT NULL COMMENT "主键id",
  `publish_time` date NOT NULL COMMENT "发布时间",
  `data_id` varchar(40) NOT NULL COMMENT "主键id",
  `one_id` varchar(64) NULL COMMENT "股份客户信息-one_id",
  `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
  `client_id` varchar(40) NOT NULL COMMENT "客户标识",
  `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
  `content_type` varchar(10) NULL COMMENT "内容类型：文本：text、 工单：order",
  `sample_data_type` varchar(100) NULL COMMENT "是否是示例数据",
  `original_id` varchar(40) NOT NULL COMMENT "原文id",
  `input_data_id` varchar(40) NULL COMMENT "原文id",
  `original_text_scene` string NULL COMMENT "原文片段",
  `brand_code` varchar(1000) NULL COMMENT "品牌名称",
  `car_series_code` varchar(1000) NULL COMMENT "车系名称",
  `label_type` varchar(50) NULL COMMENT "1服务 2产品 3品质",
  `label_type_level_first` string NULL COMMENT "一级标签",
  `label_type_level_second` string NULL COMMENT "二级标签",
  `label_type_level_three` string NULL COMMENT "三级标签",
  `label_type_level_four` string NULL COMMENT "四级标签/话题",
  `label_type_level_five` string NULL COMMENT "五级",
  `scenario` string NULL COMMENT "用车场景",
  `sentiment` varchar(100) NULL COMMENT "情感",
  `intention_type` varchar(100) NULL COMMENT "意图",
  `topic` string NULL COMMENT "聚合后的观点=>标签叶子结点",
  `opinion` string NULL COMMENT "原始观点",
  `subject` string NULL COMMENT "主体【雨刮器】",
  `fault_level` string NULL COMMENT "故障问题严重性等级",
  `description` string NULL COMMENT "描述/评价",
  `sentiment_score` string NULL COMMENT "情感严重程度",
  `keywords` string NULL COMMENT "热词",
  `model_type` int(11) NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
  `ext_fields` json NULL COMMENT "扩展字段",
  `biz_ext_attrs` json NULL COMMENT "扩展字段",
  `biz_ext_attrs2` json NULL COMMENT "扩展字段",
  `biz_ext_attrs3` json NULL COMMENT "扩展字段",
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间",
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "更新时间",
  `abandon` int(11) NULL COMMENT "是否完成计算 是：1，否：0",
  `hit_rules` json NULL COMMENT "规则id集合",
  `done` int(11) NULL COMMENT "是否完成计算 是：1，否：0"
) ENGINE=OLAP
PRIMARY KEY(`id`,`publish_time`)
COMMENT "模型计算数据分析结果表（包含规则命中)"
PARTITION BY date_trunc("MONTH", publish_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)  BUCKETS 16
ORDER BY(`brand_code`,`channel_id`, `publish_time`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"colocate_with" = "voc_processing_group",
"compression" = "LZ4"
);



-- ays_meta_data_analysis
-- drop table dwd_voc_all_meta_data
CREATE TABLE `dwd_voc_all_meta_data` (
  `id` varchar(40) NOT NULL COMMENT "主键",
  `publish_time` datetime NOT NULL COMMENT "发布时间",
  `data_id` varchar(40) NOT NULL COMMENT "主键",
  `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
  `one_id` varchar(64) NOT NULL COMMENT "股份客户信息-one_id",
  `client_id` varchar(40) NOT NULL COMMENT "客户标识",
  `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
  `content_type` varchar(10) NULL COMMENT "内容类型：文本：text、 工单：order",
  `title` string NULL COMMENT "标题",
  `content` string NULL COMMENT "内容",
  `user_name` string NULL COMMENT "昵称",
  `data` json NULL COMMENT "",
  `done` int(11) NULL COMMENT "是否完成计算 是：1，否：0",
  `data_status` int(11) NULL COMMENT "数据状态 0全部 1去噪数据 2已打标数据 3未打标数据",
  `model_type` int(11) NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
  `ext_fields` json NULL COMMENT "扩展字段",
  `biz_ext_attrs` json NULL COMMENT "扩展字段",
  `biz_ext_attrs2` json NULL COMMENT "扩展字段",
  `biz_ext_attrs3` json NULL COMMENT "扩展字段",
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "接收时间"
) ENGINE=OLAP
    PRIMARY KEY(`id`,`publish_time`)
COMMENT "数据清洗-模型入参数据记录表"
PARTITION BY date_trunc("MONTH", publish_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)
ORDER BY(`publish_time`,  `done`, `channel_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);





-- ays_pre_process_data
CREATE TABLE `dwd_voc_pre_rules_result_data` (
	`id` varchar(40) NOT NULL COMMENT "主键",
	`publish_time` datetime NOT NULL COMMENT "发布时间",
    `data_id` varchar(40) NOT NULL COMMENT "主键",
    `one_id` varchar(64) NULL COMMENT "股份客户信息-one_id",
    `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
    `client_id` varchar(40) NOT NULL COMMENT "客户标识",
    `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
    `content_type` varchar(10) NULL COMMENT "内容类型：文本：text、 工单：order",
    `data` json NULL COMMENT "",
    `data_md5` json NULL COMMENT "内容md5值",
    `model_type` int(11) NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
    `ext_fields` json NULL COMMENT "扩展字段",
    `biz_ext_attrs` json NULL COMMENT "扩展字段",
    `biz_ext_attrs2` json NULL COMMENT "扩展字段",
    `biz_ext_attrs3` json NULL COMMENT "扩展字段",
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "接收时间",
    `abandon` int(11) NULL COMMENT "是否遗弃数据 是：1，否：0",
    `done` int(11) NULL COMMENT "是否完成计算 是：1，否：0",
    `hit_rules` json NULL COMMENT "规则id集合"
) ENGINE=OLAP
PRIMARY KEY(`id`,`publish_time`)
COMMENT "数据清洗-前置处理后数据记录表"
PARTITION BY date_trunc("MONTH", publish_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)
ORDER BY(`publish_time`,  `done`, `channel_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);










CREATE TABLE `dwd_voc_model_tags_unlabeled_data` (
	`id` varchar(40) NOT NULL COMMENT "主键id",
	`publish_time` datetime NOT NULL COMMENT "发布时间",
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
	`model_type` int(11) NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
	`ext_fields` json NULL COMMENT "扩展字段",
	`biz_ext_attrs` json NULL COMMENT "扩展字段",
	`biz_ext_attrs2` json NULL COMMENT "扩展字段",
	`biz_ext_attrs3` json NULL COMMENT "扩展字段",
	`create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间",
	`update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "更新时间",
	`done` int(11) NULL COMMENT "是否完成计算 是：1，否：0"
) ENGINE=OLAP
PRIMARY KEY(`id`,`publish_time`)
COMMENT "模型计算数据分析结果表（未命中标签的)"
PARTITION BY date_trunc("MONTH", publish_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)
ORDER BY(`publish_time`,  `done`, `channel_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);





CREATE TABLE `dwd_voc_model_tags_result_data` (
	`id` varchar(40) NOT NULL COMMENT "主键id",
	`publish_time` datetime NOT NULL COMMENT "发布时间",
	`data_id` varchar(40) NOT NULL COMMENT "主键id",
	`one_id` varchar(64) NULL COMMENT "股份客户信息-one_id",
   `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
   `client_id` varchar(40) NOT NULL COMMENT "客户标识",
   `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
   `original_id` varchar(40) NOT NULL COMMENT "原文id",
   `content_type` varchar(10) NULL COMMENT "内容类型：文本：text、 工单：order",
   `input_data_id` varchar(100) NULL COMMENT "原文id",
   `sample_data_type` varchar(100) NULL COMMENT "是否是示例数据",
   `original_text_scene` varchar(10000) NULL COMMENT "原文片段",
   `brand_code` varchar(1000) NULL COMMENT "品牌名称",
   `car_series_code` varchar(1000) NULL COMMENT "车系名称",
   `label_type` varchar(200) NULL COMMENT "1服务 2产品 3品质",
   `label_type_level_first` varchar(200) NULL COMMENT "一级标签",
   `label_type_level_second` varchar(200) NULL COMMENT "二级标签",
   `label_type_level_three` varchar(200) NULL COMMENT "三级标签",
   `label_type_level_four` varchar(200) NULL COMMENT "四级标签/话题",
   `label_type_level_five` varchar(200) NULL COMMENT "五级",
   `scenario` varchar(500) NULL COMMENT "用车场景",
   `sentiment` varchar(1000) NULL COMMENT "情感",
   `intention_type` varchar(1000) NULL COMMENT "意图",
   `topic` varchar(300) NULL COMMENT "聚合后的观点=>标签叶子结点",
   `opinion` varchar(300) NULL COMMENT "原始观点",
   `subject` varchar(300) NULL COMMENT "主体【雨刮器】",
   `fault_level` varchar(50) NULL COMMENT "故障问题严重性等级",
   `description` varchar(200) NULL COMMENT "描述/评价【时灵时不灵】",
   `sentiment_score` varchar(10) NULL COMMENT "情感严重程度",
   `keywords` varchar(1000) NULL COMMENT "热词",
   `model_type` int(11) NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
   `ext_fields` json NULL COMMENT "扩展字段",
   `biz_ext_attrs` json NULL COMMENT "扩展字段",
   `biz_ext_attrs2` json NULL COMMENT "扩展字段",
   `biz_ext_attrs3` json NULL COMMENT "扩展字段",
   `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间",
   `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "更新时间",
   `hit_valid_rules` json NULL COMMENT "验证规则id集合",
   `hit_rules` json NULL COMMENT "规则id集合",
   `done` int(11) NULL COMMENT "是否完成计算 是：1，否：0"
) ENGINE=OLAP
PRIMARY KEY(`id`,`publish_time`)
COMMENT "模型计算数据分析结果表（包含规则命中)"
PARTITION BY date_trunc("MONTH", publish_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)
ORDER BY(`publish_time`,  `done`, `channel_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);




-- ays_api_reslt_data
CREATE TABLE `dwd_voc_model_tags_request_data` (
	 `id` varchar(40) NOT NULL COMMENT "主键id",
	`publish_time` datetime NOT NULL COMMENT "发布时间",
	`data_id` varchar(40) NOT NULL COMMENT "主键id",
	`one_id` varchar(64) NULL COMMENT "股份客户信息-one_id",
	`work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
	`client_id` varchar(40) NOT NULL COMMENT "客户标识",
	`channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
	`original_id` varchar(40) NOT NULL COMMENT "原文id",
	`content_type` varchar(10) NULL COMMENT "内容类型：文本：text、 工单：order",
	`data` json NULL COMMENT "",
	`data_md5` json NULL COMMENT "内容md5值",
	`model_type` int(11) NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
	`ext_fields` json NULL COMMENT "扩展字段",
	`biz_ext_attrs` json NULL COMMENT "扩展字段",
	`biz_ext_attrs2` json NULL COMMENT "扩展字段",
	`biz_ext_attrs3` json NULL COMMENT "扩展字段",
	`create_time` datetime NULL COMMENT "接收时间",
	`done` int(11) NULL COMMENT "是否完成计算 是：1，否：0"
) ENGINE=OLAP
PRIMARY KEY(`id`,`publish_time`)
COMMENT "数据清洗-前置处理后数据记录表"
PARTITION BY date_trunc("MONTH", publish_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)
ORDER BY(`publish_time`,  `done`, `channel_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);




-- drop  TABLE `dim_voc_product_info`
CREATE TABLE `dim_voc_product_info`(
                                        `vcl_cd` string  not null  COMMENT '整车编码',
                                        `vcl_cd_nm` string   COMMENT '整车编码名称',
                                        `grp_nm` string   COMMENT '集团简称',
                                        `enterp_nm` string   COMMENT '企业简称',
                                        `enterp_bd` string   COMMENT '企业品牌',
                                        `bus_bd_sec_cls` string   COMMENT '业务品牌',
                                        `bus_bd_cd` string   COMMENT '业务品牌代码',
                                        `prod_bd_seq` string   COMMENT '产品序列',
                                        `prod_bd_cd` string   COMMENT '车系代码',
                                        `prod_bd` string   COMMENT '车系',
                                        `prod_bd_en` string   COMMENT '车系英文',
                                        `prod_nm_cd` string   COMMENT '产品名称代码',
                                        `prod_nm` string   COMMENT '产品名称',
                                        `prod_nm_en` string   COMMENT '产品名称英文',
                                        `cfgtn_nm` string   COMMENT '配置名称',
                                        `annual_mdl` string   COMMENT '年度款型',
                                        `model_ver_nm` string   COMMENT '版本名称',
                                        `model_ver_cd` string   COMMENT '版本代码',
                                        `time_mkt` string   COMMENT '上市时间',
                                        `opt_pkg_des` string   COMMENT '选装包组合描述',
                                        `opt_pkg_cd` string   COMMENT '选装包组合编码',
                                        `inner_clr_nm` string   COMMENT '内饰颜色名称',
                                        `inner_clr_cd` string   COMMENT '内饰颜色编码',
                                        `vcl_clr_cd` string   COMMENT '车身颜色编码',
                                        `vcl_clr_nm` string   COMMENT '车身颜色名称',
                                        `country` string   COMMENT '国别',
                                        `sal_deprtmt` string   COMMENT '销售部门',
                                        `proj_cd` string   COMMENT '研发代号',
                                        `sbu_name_full` string   COMMENT '经营单位',
                                        `sbu_name` string   COMMENT '经营单位简称',
                                        `sbu_code` string   COMMENT '经营单位代码',
                                        `bd_clsf` string   COMMENT '车身类型',
                                        `seg_mt` string   COMMENT '细分市场',
                                        `pow_clsf` string   COMMENT '动力类型',
                                        `fu_clsf` string   COMMENT '燃料类型',
                                        `pub_cd` string   COMMENT '车型号',
                                        `pub_nm` string   COMMENT '车型名称',
                                        `max_ms` string   COMMENT '最大允许总质量',
                                        `range_condition` string   COMMENT '续使里程工况',
                                        `cltc_mil` string   COMMENT '续驶里程',
                                        `ovr_len` string   COMMENT '产品长度',
                                        `bd_form` string   COMMENT '车身型式',
                                        `driv_big_clsf` string   COMMENT '驱动方式大类',
                                        `driv_clsf` string   COMMENT '驱动方式小类',
                                        `driv_motor_mdl` string   COMMENT '驱动电机型号',
                                        `trans_clsf` string   COMMENT '变速器类型',
                                        `trans_form` string   COMMENT '变速器型式',
                                        `eng_clsf` string   COMMENT '动力系列大类',
                                        `eng_seris` string   COMMENT '动力系列小类',
                                        `eng_mdl` string   COMMENT '发动机型号',
                                        `dis_mt` string   COMMENT '排量',
                                        `dis_ch` string   COMMENT '排放',
                                        `modl_st` string   COMMENT '车型状态号',
                                        `modl_st_nm` string   COMMENT '车型状态名称',
                                        `status` string   COMMENT '整车状态号',
                                        `cfg_level` string   COMMENT '配置等级',
                                        `seat_num` string   COMMENT '座位数',
                                        `manu_bs_cd` string   COMMENT '制造基地代码',
                                        `manu_bs` string   COMMENT '制造基地',
                                        `std_plnt_code` string   COMMENT '标准工厂编码',
                                        `std_plnt_name` string   COMMENT '标准工厂名称',
                                        `in_prdtn` string   COMMENT '是否在产',
                                        `in_sold` string   COMMENT '是否在售',
                                        `mkt_vcl_seris_cd` string   COMMENT '营销车系代码',
                                        `enable_flag` string   COMMENT '是否最新版本',
                                        `start_date_active` string   COMMENT '开始有效时间',
                                        `end_date_active` string   COMMENT '结束有效时间'
) ENGINE=OLAP
    PRIMARY KEY(`vcl_cd`)
COMMENT "产品维（dim_prd_product_full）"
DISTRIBUTED BY HASH(`vcl_cd`) BUCKETS 8
ORDER BY(`vcl_cd`, `prod_bd_cd`,  `bus_bd_cd`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);





-- ays_batch_push_record
CREATE TABLE `dws_voc_batch_push_record` (
  `id` varchar(40) NOT NULL COMMENT "主键",
  `reqeut_id` varchar(40) NOT NULL COMMENT "请求处理标识",
  `work_id` varchar(40) NOT NULL COMMENT "处理标识",
  `status` varchar(1) NULL DEFAULT "0" COMMENT "0:未处理，1：已处理，-1：异常",
  `source` varchar(20) NULL COMMENT "A:前置过滤， B：后置过滤， C：模型已达标，D：模型未达标，E：异常,F:解析数据校验",
  `model_type` int(11) NULL COMMENT "1 智谱AI离线 2智谱AI实时 3聚类大模型",
  `ext_fields` json NULL COMMENT "扩展字段",
  `create_time` datetime NULL COMMENT "接收时间",
  `update_time` datetime NULL COMMENT "更新时间",
  `tid` varchar(60) NULL COMMENT "链路标识"
) ENGINE=OLAP
PRIMARY KEY(`id`)
COMMENT "数据清洗-分批次接收数据记录表"
DISTRIBUTED BY HASH(`id`)
ORDER BY(`work_id`, `reqeut_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);









-- ays_error_push_data

CREATE TABLE `dim_data_service_tag` (
        `one_id` string NOT NULL COMMENT "股份客户信息-one_id",
        `tag_id` string NOT NULL COMMENT "标签标识",
        `tag_value` string NOT NULL COMMENT "标签值"
) ENGINE=OLAP
    PRIMARY KEY(`one_id`)
COMMENT "基表-客户标签"
DISTRIBUTED BY HASH(`one_id`) BUCKETS 8
ORDER BY(`one_id`,`tag_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);


-- drop  TABLE `dwd_voc_raw_private_consult`
CREATE TABLE `dws_voc_error_push_data` (
  `id` varchar(40) NOT NULL COMMENT "主键",
  `table` varchar(100) NOT NULL COMMENT "表名",
  `action` varchar(40) NOT NULL COMMENT "操作类型",
  `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
  `client_id` varchar(40) NOT NULL COMMENT "消息来源 api,mq,file等",
  `data` json NULL COMMENT "",
  `create_time` datetime NULL COMMENT "接收时间",
  `tid` varchar(55) NULL COMMENT "链路标识"
) ENGINE=OLAP
PRIMARY KEY(`id`)
COMMENT "数据清洗-入库时异常数据记录表"
DISTRIBUTED BY HASH(`id`)
ORDER BY(`work_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);






CREATE TABLE `dwd_voc_raw_private_work_order`(
                                              `id`  varchar(40) NOT NULL  COMMENT 'ID',
                                              `data_create_time` datetime NOT NULL COMMENT '数据产生时间',
                                              `create_date` datetime NOT NULL COMMENT '数据抓取时间',
                                              `data_id`  varchar(40) NOT NULL  COMMENT '数据唯一标识',
                                              `channel_code`  varchar(30) NOT NULL  COMMENT '渠道编码',
                                              `brand_code` string COMMENT '品牌编码',
                                              `series_code` string COMMENT '车系编码',
                                              `is_outer` varchar(1) NOT NULL DEFAULT 'N' COMMENT '是否往外数据',
                                              `one_id` string NULL COMMENT '股份客户信息-one_id',
                                              `id_car_no` string NULL COMMENT '客户证件好',
                                              `mobile` string NULL COMMENT '客户手机号',
                                              `global_id` string NULL COMMENT 'SSO全局ID',
                                              `user_id` string COMMENT '用户标识',
                                              `user_name` string NOT NULL COMMENT '用户名',
                                              `vhl_id` string COMMENT '车辆ID',
                                              `vhl_vin` string COMMENT '车辆车架号',
                                              `dlr_id` string COMMENT '股份售后经销商ID',
                                              `dlr_code` string COMMENT '股份售后经销商编码',
                                              `dlr_type` varchar(1)  NOT NULL DEFAULT '0' COMMENT '股份售后经销商类型 (1：订单中心 2、交付中心 3、维保中心',
                                              `market_id` string COMMENT '股份产品物理编码',
                                              `title` string NOT NULL  COMMENT '标题',
                                              `content` string NOT NULL  COMMENT '内容',
                                              `is_wsater_army`  varchar(1) COMMENT '是否水军',
                                              `weight` int COMMENT '权重值',
                                              `attrs` json COMMENT '其他字段',
                                              `order_id` string NOT NULL COMMENT '工单ID',
                                              `order_type` string NOT NULL COMMENT '工单类型',
                                              `parent_order_id` string COMMENT '上级工单ID',
                                              `car_owner_name` string COMMENT '车主（个人、企业）',
                                              `is_car_owner` varchar(1) COMMENT '是否车主'
) ENGINE=OLAP
    PRIMARY KEY(`id`,`data_create_time`)
COMMENT "工单"
PARTITION BY date_trunc('MONTH', data_create_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)  BUCKETS 2
ORDER BY(`id`,`user_id`,`create_date`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);



-- ays_batch_push_record
CREATE TABLE `dwd_voc_raw_private_consult`(
                                               `id`  varchar(40) NOT NULL  COMMENT 'ID',
                                               `data_create_time` datetime NOT NULL COMMENT '数据产生时间',
                                               `create_date` datetime NOT NULL COMMENT '数据抓取时间',
                                               `data_id`  varchar(40) NOT NULL  COMMENT '数据唯一标识',
                                               `channel_code`  varchar(30) NOT NULL  COMMENT '渠道编码',
                                               `brand_code` string COMMENT '品牌编码',
                                               `series_code` string COMMENT '车系编码',
                                               `is_outer` varchar(1) NOT NULL DEFAULT 'N' COMMENT '是否往外数据',
                                               `one_id` string NULL COMMENT '股份客户信息-one_id',
                                               `id_car_no` string NULL COMMENT '客户证件好',
                                               `mobile` string NULL COMMENT '客户手机号',
                                               `global_id` string NULL COMMENT 'SSO全局ID',
                                               `user_id` string COMMENT '用户标识',
                                               `user_name` string NOT NULL COMMENT '用户名',
                                               `vhl_id` string COMMENT '车辆ID',
                                               `vhl_vin` string COMMENT '车辆车架号',
                                               `dlr_id` string COMMENT '股份售后经销商ID',
                                               `dlr_code` string COMMENT '股份售后经销商编码',
                                               `dlr_type` varchar(1)  NOT NULL DEFAULT '0' COMMENT '股份售后经销商类型 (1：订单中心 2、交付中心 3、维保中心',
                                               `market_id` string COMMENT '股份产品物理编码',
                                               `title` string NOT NULL  COMMENT '标题',
                                               `content` string NOT NULL  COMMENT '内容',
                                               `is_wsater_army`  varchar(1) COMMENT '是否水军',
                                               `weight` int COMMENT '权重值',
                                               `attrs` json COMMENT '其他字段',
                                               `session_id` string NOT NULL COMMENT '会话ID'
) ENGINE=OLAP
    PRIMARY KEY(`id`,`data_create_time`)
COMMENT "咨询"
PARTITION BY date_trunc('MONTH', data_create_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)  BUCKETS 2
ORDER BY(`id`,`user_id`,`create_date`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);



-- drop  TABLE `dwd_voc_raw_private_opinion`
CREATE TABLE `dwd_voc_raw_private_opinion`(
       `id`  varchar(40) NOT NULL  COMMENT 'ID',
       `data_create_time` datetime NOT NULL COMMENT '数据产生时间',
       `create_date` datetime NOT NULL COMMENT '数据抓取时间',
       `data_id`  varchar(40) NOT NULL  COMMENT '数据唯一标识',
       `channel_code`  varchar(30) NOT NULL  COMMENT '渠道编码',
       `brand_code` string COMMENT '品牌编码',
       `series_code` string COMMENT '车系编码',
       `is_outer` varchar(1) NOT NULL DEFAULT 'N' COMMENT '是否往外数据',
       `one_id` string NULL COMMENT '股份客户信息-one_id',
       `id_car_no` string NULL COMMENT '客户证件好',
       `mobile` string NULL COMMENT '客户手机号',
       `global_id` string NULL COMMENT 'SSO全局ID',
       `user_id` string COMMENT '用户标识',
       `user_name` string NOT NULL COMMENT '用户名',
       `vhl_id` string COMMENT '车辆ID',
       `vhl_vin` string COMMENT '车辆车架号',
       `dlr_id` string COMMENT '股份售后经销商ID',
       `dlr_code` string COMMENT '股份售后经销商编码',
       `dlr_type` varchar(1)  NOT NULL DEFAULT '0' COMMENT '股份售后经销商类型 (1：订单中心 2、交付中心 3、维保中心',
       `market_id` string COMMENT '股份产品物理编码',
       `title` string NOT NULL  COMMENT '标题',
       `content` string NOT NULL  COMMENT '内容',
       `is_wsater_army`  varchar(1) COMMENT '是否水军',
       `weight` int COMMENT '权重值',
       `attrs` json COMMENT '其他字段',
       `url` string COMMENT '详情链接'
) ENGINE=OLAP
PRIMARY KEY(`id`,`data_create_time`)
COMMENT "意见反馈"
PARTITION BY date_trunc('MONTH', data_create_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)  BUCKETS 2
ORDER BY(`id`,`user_id`,`create_date`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);






-- drop  TABLE `dwd_voc_raw_private_questionnaire`
CREATE TABLE `dwd_voc_raw_private_opinion`(
                                               `id`  varchar(40) NOT NULL  COMMENT 'ID',
                                               `data_create_time` datetime NOT NULL COMMENT '数据产生时间',
                                               `create_date` datetime NOT NULL COMMENT '数据抓取时间',
                                               `data_id`  varchar(40) NOT NULL  COMMENT '数据唯一标识',
                                               `channel_code`  varchar(30) NOT NULL  COMMENT '渠道编码',
                                               `brand_code` string COMMENT '品牌编码',
                                               `series_code` string COMMENT '车系编码',
                                               `is_outer` varchar(1) NOT NULL DEFAULT 'N' COMMENT '是否往外数据',
                                               `one_id` string NULL COMMENT '股份客户信息-one_id',
                                               `id_car_no` string NULL COMMENT '客户证件好',
                                               `mobile` string NULL COMMENT '客户手机号',
                                               `global_id` string NULL COMMENT 'SSO全局ID',
                                               `user_id` string COMMENT '用户标识',
                                               `user_name` string NOT NULL COMMENT '用户名',
                                               `vhl_id` string COMMENT '车辆ID',
                                               `vhl_vin` string COMMENT '车辆车架号',
                                               `dlr_id` string COMMENT '股份售后经销商ID',
                                               `dlr_code` string COMMENT '股份售后经销商编码',
                                               `dlr_type` varchar(1)  NOT NULL DEFAULT '0' COMMENT '股份售后经销商类型 (1：订单中心 2、交付中心 3、维保中心',
                                               `market_id` string COMMENT '股份产品物理编码',
                                               `title` string NOT NULL  COMMENT '标题',
                                               `content` string NOT NULL  COMMENT '内容',
                                               `is_wsater_army`  varchar(1) COMMENT '是否水军',
                                               `weight` int COMMENT '权重值',
                                               `attrs` json COMMENT '其他字段',
                                               `url` string COMMENT '详情链接'
) ENGINE=OLAP
    PRIMARY KEY(`id`,`data_create_time`)
COMMENT "意见反馈"
PARTITION BY date_trunc('MONTH', data_create_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)  BUCKETS 2
ORDER BY(`id`,`user_id`,`create_date`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);








CREATE TABLE `dwd_voc_raw_private_questionnaire`(
                                                     `id`  varchar(40) NOT NULL  COMMENT 'ID',
                                                     `data_create_time` datetime NOT NULL COMMENT '数据产生时间',
                                                     `create_date` datetime NOT NULL COMMENT '数据抓取时间',
                                                     `data_id`  varchar(40) NOT NULL  COMMENT '数据唯一标识',
                                                     `channel_code`  varchar(30) NOT NULL  COMMENT '渠道编码',
                                                     `brand_code` string COMMENT '品牌编码',
                                                     `series_code` string COMMENT '车系编码',
                                                     `is_outer` varchar(1) NOT NULL DEFAULT 'N' COMMENT '是否往外数据',
                                                     `one_id` string NULL COMMENT '股份客户信息-one_id',
                                                     `id_car_no` string NULL COMMENT '客户证件好',
                                                     `mobile` string NULL COMMENT '客户手机号',
                                                     `global_id` string NULL COMMENT 'SSO全局ID',
                                                     `user_id` string COMMENT '用户标识',
                                                     `user_name` string NOT NULL COMMENT '用户名',
                                                     `vhl_id` string COMMENT '车辆ID',
                                                     `vhl_vin` string COMMENT '车辆车架号',
                                                     `dlr_id` string COMMENT '股份售后经销商ID',
                                                     `dlr_code` string COMMENT '股份售后经销商编码',
                                                     `dlr_type` varchar(1)  NOT NULL DEFAULT '0' COMMENT '股份售后经销商类型 (1：订单中心 2、交付中心 3、维保中心',
                                                     `market_id` string COMMENT '股份产品物理编码',
                                                     `title` string NOT NULL  COMMENT '标题',
                                                     `content` string NOT NULL  COMMENT '内容',
                                                     `is_wsater_army`  varchar(1) COMMENT '是否水军',
                                                     `weight` int COMMENT '权重值',
                                                     `attrs` json COMMENT '其他字段',
                                                     `type`  varchar(1) NOT NULL   COMMENT '问卷类型',
                                                     `analysis_type`  varchar(1)  NOT NULL  COMMENT '分析类型-0：长文本 1：评分 2：短文本',
                                                     `quest_type`  varchar(1) NOT NULL   COMMENT '问题类型-正负选项标识  0正向选项 1负向选项',
                                                     `quest_answer_score`  string COMMENT '回答分数',
                                                     `url` string COMMENT '详情链接'
) ENGINE=OLAP
    PRIMARY KEY(`id`,`data_create_time`)
COMMENT "问卷"
PARTITION BY date_trunc('MONTH', data_create_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)  BUCKETS 2
ORDER BY(`id`,`user_id`,`create_date`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);



-- drop TABLE `dwd_voc_raw_publicprivate_consult`
CREATE TABLE `dwd_voc_raw_public_consult`(
              `id`  varchar(40) NOT NULL  COMMENT 'ID',
              `data_create_time` datetime NOT NULL COMMENT '数据产生时间',
              `create_date` datetime NOT NULL COMMENT '数据抓取时间',
              `data_id`  varchar(40) NOT NULL  COMMENT '数据唯一标识',
              `channel_code`  varchar(30) NOT NULL  COMMENT '渠道编码',
              `brand_code` string COMMENT '品牌编码',
              `series_code` string COMMENT '车系编码',
              `is_outer` varchar(1) NOT NULL COMMENT '是否往外数据',
              `one_id` string NULL COMMENT '股份客户信息-one_id',
              `user_id` string NOT NULL COMMENT '用户标识',
              `mobile` string NULL COMMENT '客户手机号',
              `id_car_no` string NULL COMMENT '客户证件好',
              `user_name` string NOT NULL COMMENT '用户名',
              `vhl_id` string COMMENT '车辆ID',
              `vhl_vin` string COMMENT '车辆车架号',
              `dlr_id` string COMMENT '经销商ID',
              `title` string NOT NULL  COMMENT '标题',
              `content` string NOT NULL  COMMENT '内容',
              `is_wsater_army`  varchar(1) COMMENT '是否水军',
              `attrs` json COMMENT '其他字段',
              `session_id` NOT NULL  string COMMENT '会话ID'
) ENGINE=OLAP
    PRIMARY KEY(`id`,`data_create_time`)
COMMENT "咨询"
PARTITION BY date_trunc('MONTH', data_create_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)  BUCKETS 2
ORDER BY(`id`,`user_id`,`create_date`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);




CREATE TABLE `dwd_voc_raw_public_opinion`(
                                               `id`  varchar(40) NOT NULL  COMMENT 'ID',
                                               `data_create_time` datetime NOT NULL COMMENT '数据产生时间',
                                               `create_date` datetime NOT NULL COMMENT '数据抓取时间',
                                               `data_id`  varchar(40) NOT NULL  COMMENT '数据唯一标识',
                                               `channel_code`  varchar(30) NOT NULL  COMMENT '渠道编码',
                                               `brand_code` string COMMENT '品牌编码',
                                               `series_code` string COMMENT '车系编码',
                                               `is_outer` varchar(1) NOT NULL COMMENT '是否往外数据',
                                               `one_id` string NULL COMMENT '股份客户信息-one_id',
                                               `user_id` string NOT NULL COMMENT '用户标识',
                                               `mobile` string NULL COMMENT '客户手机号',
                                               `id_car_no` string NULL COMMENT '客户证件好',
                                               `user_name` string NOT NULL COMMENT '用户名',
                                               `vhl_id` string COMMENT '车辆ID',
                                               `vhl_vin` string COMMENT '车辆车架号',
                                               `dlr_id` string COMMENT '经销商ID',
                                               `title` string NOT NULL  COMMENT '标题',
                                               `content` string NOT NULL  COMMENT '内容',
                                               `is_wsater_army`  varchar(1) COMMENT '是否水军',
                                               `attrs` json COMMENT '其他字段',
                                               `url` string COMMENT '详情链接'
) ENGINE=OLAP
    PRIMARY KEY(`id`,`data_create_time`)
COMMENT "意见反馈"
PARTITION BY date_trunc('MONTH', data_create_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)  BUCKETS 2
ORDER BY(`id`,`user_id`,`create_date`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);





CREATE TABLE `dwd_voc_raw_public_posts_comment`(
                                                     `id`  varchar(40) NOT NULL  COMMENT 'ID',
                                                     `data_create_time` datetime NOT NULL COMMENT '数据产生时间',
                                                     `create_date` datetime NOT NULL COMMENT '数据抓取时间',
                                                     `data_id`  varchar(40) NOT NULL  COMMENT '数据唯一标识',
                                                     `channel_code`  varchar(30) NOT NULL  COMMENT '渠道编码',
                                                     `brand_code` string COMMENT '品牌编码',
                                                     `series_code` string COMMENT '车系编码',
                                                     `is_outer` varchar(1) NOT NULL COMMENT '是否往外数据',
                                                     `one_id` string NULL COMMENT '股份客户信息-one_id',
                                                     `user_id` string NOT NULL COMMENT '用户标识',
                                                     `mobile` string NULL COMMENT '客户手机号',
                                                     `id_car_no` string NULL COMMENT '客户证件好',
                                                     `user_name` string NOT NULL  COMMENT '用户名',
                                                     `vhl_id` string COMMENT '车辆ID',
                                                     `vhl_vin` string COMMENT '车辆车架号',
                                                     `dlr_id` string COMMENT '经销商ID',
                                                     `title` string NOT NULL  COMMENT '标题',
                                                     `content` string NOT NULL  COMMENT '内容',
                                                     `is_wsater_army`  varchar(1) COMMENT '是否水军',
                                                     `attrs` json COMMENT '其他字段',
                                                     `comment` string COMMENT '评价内容',
                                                     `is_main_post`  varchar(1) NOT NULL  COMMENT '是否主贴',
                                                     `main_post_id` string NOT NULL   COMMENT '回帖时对应主贴ID',
                                                     `url` string COMMENT '详情链接',
                                                     `view_count` bigint COMMENT '浏览量or播放量',
                                                     `comment_count` bigint COMMENT '评论量',
                                                     `like_count` bigint COMMENT '点赞量',
                                                     `share_count` bigint COMMENT '转发量',
                                                     `favorite_count` bigint COMMENT '收藏量'
) ENGINE=OLAP
    PRIMARY KEY(`id`,`data_create_time`)
COMMENT "帖子评论"
PARTITION BY date_trunc('MONTH', data_create_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)  BUCKETS 2
ORDER BY(`id`,`user_id`,`create_date`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);





CREATE TABLE `dwd_voc_raw_public_questionnaire`(
                                                    `id`  varchar(40) NOT NULL  COMMENT 'ID',
                                                    `data_create_time` datetime NOT NULL COMMENT '数据产生时间',
                                                    `create_date` datetime NOT NULL COMMENT '数据抓取时间',
                                                    `data_id`  varchar(40) NOT NULL  COMMENT '数据唯一标识',
                                                    `channel_code`  varchar(30) NOT NULL  COMMENT '渠道编码',
                                                    `brand_code` string COMMENT '品牌编码',
                                                    `series_code` string COMMENT '车系编码',
                                                    `is_outer` varchar(1) NOT NULL COMMENT '是否往外数据',
                                                    `one_id` string NULL COMMENT '股份客户信息-one_id',
                                                    `user_id` string NOT NULL COMMENT '用户标识',
                                                    `mobile` string NULL COMMENT '客户手机号',
                                                    `id_car_no` string NULL COMMENT '客户证件好',
                                                    `user_name` string NOT NULL  COMMENT '用户名',
                                                    `vhl_id` string COMMENT '车辆ID',
                                                    `vhl_vin` string COMMENT '车辆车架号',
                                                    `dlr_id` string COMMENT '经销商ID',
                                                    `title` string NOT NULL  COMMENT '标题',
                                                    `content` string NOT NULL  COMMENT '内容',
                                                    `is_wsater_army`  varchar(1) COMMENT '是否水军',
                                                    `attrs` json COMMENT '其他字段',
                                                    `market_id` string COMMENT '细分市场ID',
                                                    `type`  varchar(1) NOT NULL   COMMENT '问卷类型',
                                                    `analysis_type`  varchar(1)  NOT NULL  COMMENT '分析类型-0：长文本 1：评分 2：短文本',
                                                    `quest_type`  varchar(1) NOT NULL   COMMENT '问题类型-正负选项标识  0正向选项 1负向选项',
                                                    `quest_answer_score`  string COMMENT '回答分数',
                                                    `url` string COMMENT '详情链接'
) ENGINE=OLAP
    PRIMARY KEY(`id`,`data_create_time`)
COMMENT "问卷"
PARTITION BY date_trunc('MONTH', data_create_time) -- 分区策略
DISTRIBUTED BY HASH(`id`)  BUCKETS 2
ORDER BY(`id`,`user_id`,`create_date`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"storage_format" = "DEFAULT",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);

