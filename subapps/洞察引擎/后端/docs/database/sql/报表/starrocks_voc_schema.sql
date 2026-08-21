update voc_sentiment_annotations_results_v
set intention='抱怨' where intention not in ('抱怨', '建议', '咨询','表扬');
# @Schema(description = "最高学历")
#     private String  education="无";
# @Schema(description = "职业")
#     private String  occupation="无";
# @Schema(description = "家庭月收入")
#     private String  householdIncome="无";
# @Schema(description = "是否换购")
#     private String exchangeBuy="无";

update voc_sentiment_annotations_results_v set brand_code='A01' where brand_name='长安引力';
update voc_sentiment_annotations_results_v set brand_code='A05' where brand_name='长安启源';
update voc_sentiment_annotations_results_v set brand_code='A02' where brand_name='长安凯程';
update voc_sentiment_annotations_results_v set brand_code='A03' where brand_name='深蓝汽车';
update voc_sentiment_annotations_results_v set brand_code='A04' where brand_name='阿维塔';

alter table voc_sentiment_annotations_results_v
    add COLUMN  cust_name varchar(100) null comment '用户名';


alter table report_custom_report
    add COLUMN date_condition varchar(65533) null comment '时间json';

alter table report_custom_report
    add COLUMN report_url varchar(100) null comment '报告地址';

ALTER TABLE `VDP_RS_TD`.`voc_sentiment_annotations_results_v`
    ADD COLUMN `exchangeBuy` VARCHAR(20) NULL COMMENT "是否换购";

ALTER TABLE `VDP_RS_TD`.`voc_sentiment_annotations_results_v`
    ADD COLUMN `exchangeBuy` VARCHAR(20) NULL COMMENT "是否换购";
ALTER TABLE `VDP_RS_TD`.`voc_sentiment_annotations_results_v`
    ADD COLUMN `householdIncome` VARCHAR(100) NULL COMMENT "家庭月收入";
ALTER TABLE `VDP_RS_TD`.`voc_sentiment_annotations_results_v`
    ADD COLUMN `education` VARCHAR(100) NULL COMMENT "最高学历";
ALTER TABLE `VDP_RS_TD`.`voc_sentiment_annotations_results_v`
    ADD COLUMN `occupation` VARCHAR(100) NULL COMMENT "职业";

update voc_sentiment_annotations_results_v set exchangeBuy='否' where exchangeBuy is null;
update voc_sentiment_annotations_results_v set occupation='工程师' where occupation is null;
update voc_sentiment_annotations_results_v set education='本科' where education is null;
update voc_sentiment_annotations_results_v set householdIncome='20k' where householdIncome is null;
# insert  into (householdIncome, education, occupation, exchangeBuy) voc_sentiment_annotations_results_v values ('DATA_000c85e5597d4122bff92f95b9d70e4f', 'TEST_090c9aa82a4f4bcdb1dfec63ef0e9f4f', 'TEST_090c9aa82a4f4bcdb1dfec63ef0e9f4f', 'TEST_090c9aa82a4f4bcdb1dfec63ef0e9f4f', 'TEST_090c9aa82a4f4bcdb1dfec63ef0e9)


select
    *
from voc_sentiment_annotations_results_v where  competitive_type is null;
update report_user_browse_record set sound_intention = '抱怨' where sound_intention not in ('抱怨', '建议', '咨询','投诉');

update report_user_browse_record set original_id = 'DATA_02e1840065f4416a90b22908cc620642' where original_id  in ('originalId1', '1111', '咨询','投诉');
update report_user_browse_record set sound_id = 'TEST_02e1840065f4416a90b22908cc620642' where sound_id  in ('newId1', '1111', '咨询','投诉');

UPDATE voc_sentiment_annotations_results_v
SET competitive_type =2 where competitive_type is null
and  brand_name IN ('长安引力', '阿维塔', '深蓝汽车', '长安凯程', '长安启源', '长安汽车集团');


UPDATE voc_sentiment_annotations_results_v
SET originalTexTScene ='长安UNI-K整体看上去非常大气，配置也丰富，驾驶辅助功能基本都有，但跑了几个月后油耗有点高，市区大概在12个油左右，和宣传的有差距。' where originalTexTScene is null;



-- StarRocks 用户浏览记录表结构
-- 建议在 starrocks_client_db 数据源中执行
CREATE TABLE IF NOT EXISTS report_user_browse_record (
                                                  id VARCHAR(64) NOT NULL COMMENT '主键',
                                                  sound_id VARCHAR(64) NOT NULL COMMENT '声音id',
                                                  original_id VARCHAR(64) NOT NULL COMMENT '原文id',
                                                  browse_user_id VARCHAR(64) NOT NULL COMMENT '浏览人id',
                                                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                  browse_duration INT COMMENT '浏览时长(秒)',
                                                  sound_intention VARCHAR(64) COMMENT '声音意图'
) ENGINE=OLAP
    PRIMARY KEY(`id`)
COMMENT "用户浏览记录表"
DISTRIBUTED BY HASH(`id`) BUCKETS 16
PROPERTIES (
  "replication_num" = "1"
);







-- 根据品牌名称更新品牌代码
UPDATE voc_sentiment_annotations_results_v
SET brand_code = CASE
                     WHEN brand_name = '长安引力' THEN 'A75'
                     WHEN brand_name = '阿维塔' THEN 'E73'
                     WHEN brand_name = '深蓝汽车' THEN 'F03'
                     WHEN brand_name = '长安凯程' THEN 'A76'
                     WHEN brand_name = '长安启源' THEN 'F49'
                     WHEN brand_name = '长安汽车集团' THEN 'A00'
                     ELSE brand_code
    END
WHERE brand_name IN ('长安引力', '阿维塔', '深蓝汽车', '长安凯程', '长安启源', '长安汽车集团');






UPDATE voc_sentiment_annotations_results_v
SET
    cust_province = CASE cust_province
                        WHEN '北京' THEN '北京市'
                        WHEN '天津' THEN '天津市'
                        WHEN '河北' THEN '河北省'
                        WHEN '山西' THEN '山西省'
                        WHEN '内蒙古' THEN '内蒙古自治区'
                        WHEN '辽宁' THEN '辽宁省'
                        WHEN '吉林' THEN '吉林省'
                        WHEN '黑龙江' THEN '黑龙江省'
                        WHEN '上海' THEN '上海市'
                        WHEN '江苏' THEN '江苏省'
                        WHEN '浙江' THEN '浙江省'
                        WHEN '安徽' THEN '安徽省'
                        WHEN '福建' THEN '福建省'
                        WHEN '江西' THEN '江西省'
                        WHEN '山东' THEN '山东省'
                        WHEN '河南' THEN '河南省'
                        WHEN '湖北' THEN '湖北省'
                        WHEN '湖南' THEN '湖南省'
                        WHEN '广东' THEN '广东省'
                        WHEN '广西' THEN '广西壮族自治区'
                        WHEN '海南' THEN '海南省'
                        WHEN '重庆' THEN '重庆市'
                        WHEN '四川' THEN '四川省'
                        WHEN '贵州' THEN '贵州省'
                        WHEN '云南' THEN '云南省'
                        WHEN '西藏' THEN '西藏自治区'
                        WHEN '陕西' THEN '陕西省'
                        WHEN '甘肃' THEN '甘肃省'
                        WHEN '青海' THEN '青海省'
                        WHEN '宁夏' THEN '宁夏回族自治区'
                        WHEN '新疆' THEN '新疆维吾尔自治区'
                        WHEN '台湾' THEN '台湾省'
                        WHEN '香港' THEN '香港特别行政区'
                        WHEN '澳门' THEN '澳门特别行政区'
                        ELSE cust_province END,
    cust_province_code = CASE cust_province
                             WHEN '北京' THEN '110000'
                             WHEN '天津' THEN '120000'
                             WHEN '河北' THEN '130000'
                             WHEN '山西' THEN '140000'
                             WHEN '内蒙古' THEN '150000'
                             WHEN '辽宁' THEN '210000'
                             WHEN '吉林' THEN '220000'
                             WHEN '黑龙江' THEN '230000'
                             WHEN '上海' THEN '310000'
                             WHEN '江苏' THEN '320000'
                             WHEN '浙江' THEN '330000'
                             WHEN '安徽' THEN '340000'
                             WHEN '福建' THEN '350000'
                             WHEN '江西' THEN '360000'
                             WHEN '山东' THEN '370000'
                             WHEN '河南' THEN '410000'
                             WHEN '湖北' THEN '420000'
                             WHEN '湖南' THEN '430000'
                             WHEN '广东' THEN '440000'
                             WHEN '广西' THEN '450000'
                             WHEN '海南' THEN '460000'
                             WHEN '重庆' THEN '500000'
                             WHEN '四川' THEN '510000'
                             WHEN '贵州' THEN '520000'
                             WHEN '云南' THEN '530000'
                             WHEN '西藏' THEN '540000'
                             WHEN '陕西' THEN '610000'
                             WHEN '甘肃' THEN '620000'
                             WHEN '青海' THEN '630000'
                             WHEN '宁夏' THEN '640000'
                             WHEN '新疆' THEN '650000'
                             WHEN '台湾' THEN '710000'
                             WHEN '香港' THEN '810000'
                             WHEN '澳门' THEN '820000'
                             ELSE cust_province_code END
WHERE cust_province IN ('北京','天津','河北','山西','内蒙古','辽宁','吉林','黑龙江','上海','江苏','浙江','安徽','福建','江西','山东','河南','湖北','湖南','广东','广西','海南','重庆','四川','贵州','云南','西藏','陕西','甘肃','青海','宁夏','新疆','台湾','香港','澳门');

UPDATE voc_sentiment_annotations_results_v d
    JOIN (
        SELECT '北京' short_name,'北京市' full_name,'110000' code UNION ALL
        SELECT '天津','天津市','120000' UNION ALL
        SELECT '河北','河北省','130000' UNION ALL
        SELECT '山西','山西省','140000' UNION ALL
        SELECT '内蒙古','内蒙古自治区','150000' UNION ALL
        SELECT '辽宁','辽宁省','210000' UNION ALL
        SELECT '吉林','吉林省','220000' UNION ALL
        SELECT '黑龙江','黑龙江省','230000' UNION ALL
        SELECT '上海','上海市','310000' UNION ALL
        SELECT '江苏','江苏省','320000' UNION ALL
        SELECT '浙江','浙江省','330000' UNION ALL
        SELECT '安徽','安徽省','340000' UNION ALL
        SELECT '福建','福建省','350000' UNION ALL
        SELECT '江西','江西省','360000' UNION ALL
        SELECT '山东','山东省','370000' UNION ALL
        SELECT '河南','河南省','410000' UNION ALL
        SELECT '湖北','湖北省','420000' UNION ALL
        SELECT '湖南','湖南省','430000' UNION ALL
        SELECT '广东','广东省','440000' UNION ALL
        SELECT '广西','广西壮族自治区','450000' UNION ALL
        SELECT '海南','海南省','460000' UNION ALL
        SELECT '重庆','重庆市','500000' UNION ALL
        SELECT '四川','四川省','510000' UNION ALL
        SELECT '贵州','贵州省','520000' UNION ALL
        SELECT '云南','云南省','530000' UNION ALL
        SELECT '西藏','西藏自治区','540000' UNION ALL
        SELECT '陕西','陕西省','610000' UNION ALL
        SELECT '甘肃','甘肃省','620000' UNION ALL
        SELECT '青海','青海省','630000' UNION ALL
        SELECT '宁夏','宁夏回族自治区','640000' UNION ALL
        SELECT '新疆','新疆维吾尔自治区','650000' UNION ALL
        SELECT '台湾','台湾省','710000' UNION ALL
        SELECT '香港','香港特别行政区','810000' UNION ALL
        SELECT '澳门','澳门特别行政区','820000'
    ) m ON d.cust_province = m.short_name
SET d.cust_province= m.full_name,d.cust_province_code= m.code where d.cust_province is null;

MERGE INTO voc_sentiment_annotations_results_v d
USING (  SELECT '北京' short_name,'北京市' full_name,'110000' code UNION ALL
        SELECT '天津','天津市','120000' UNION ALL
        SELECT '河北','河北省','130000' UNION ALL
        SELECT '山西','山西省','140000' UNION ALL
        SELECT '内蒙古','内蒙古自治区','150000' UNION ALL
        SELECT '辽宁','辽宁省','210000' UNION ALL
        SELECT '吉林','吉林省','220000' UNION ALL
        SELECT '黑龙江','黑龙江省','230000' UNION ALL
        SELECT '上海','上海市','310000' UNION ALL
        SELECT '江苏','江苏省','320000' UNION ALL
        SELECT '浙江','浙江省','330000' UNION ALL
        SELECT '安徽','安徽省','340000' UNION ALL
        SELECT '福建','福建省','350000' UNION ALL
        SELECT '江西','江西省','360000' UNION ALL
        SELECT '山东','山东省','370000' UNION ALL
        SELECT '河南','河南省','410000' UNION ALL
        SELECT '湖北','湖北省','420000' UNION ALL
        SELECT '湖南','湖南省','430000' UNION ALL
        SELECT '广东','广东省','440000' UNION ALL
        SELECT '广西','广西壮族自治区','450000' UNION ALL
        SELECT '海南','海南省','460000' UNION ALL
        SELECT '重庆','重庆市','500000' UNION ALL
        SELECT '四川','四川省','510000' UNION ALL
        SELECT '贵州','贵州省','520000' UNION ALL
        SELECT '云南','云南省','530000' UNION ALL
        SELECT '西藏','西藏自治区','540000' UNION ALL
        SELECT '陕西','陕西省','610000' UNION ALL
        SELECT '甘肃','甘肃省','620000' UNION ALL
        SELECT '青海','青海省','630000' UNION ALL
        SELECT '宁夏','宁夏回族自治区','640000' UNION ALL
        SELECT '新疆','新疆维吾尔自治区','650000' UNION ALL
        SELECT '台湾','台湾省','710000' UNION ALL
        SELECT '香港','香港特别行政区','810000' UNION ALL
        SELECT '澳门','澳门特别行政区','820000') m
ON d.cust_province = m.short_name
WHEN MATCHED THEN UPDATE SET
                      d.cust_province = m.full_name,
                      d.cust_province_code = m.code where d.cust_province is null;


SELECT d.cust_province AS before_name, m.full_name AS after_name,
       d.cust_province_code AS before_code, m.code AS after_code, COUNT(*) cnt
FROM voc_sentiment_annotations_results_v d
         JOIN ( SELECT '北京' short_name,'北京市' full_name,'110000' code UNION ALL
                SELECT '天津','天津市','120000' UNION ALL
                SELECT '河北','河北省','130000' UNION ALL
                SELECT '山西','山西省','140000' UNION ALL
                SELECT '内蒙古','内蒙古自治区','150000' UNION ALL
                SELECT '辽宁','辽宁省','210000' UNION ALL
                SELECT '吉林','吉林省','220000' UNION ALL
                SELECT '黑龙江','黑龙江省','230000' UNION ALL
                SELECT '上海','上海市','310000' UNION ALL
                SELECT '江苏','江苏省','320000' UNION ALL
                SELECT '浙江','浙江省','330000' UNION ALL
                SELECT '安徽','安徽省','340000' UNION ALL
                SELECT '福建','福建省','350000' UNION ALL
                SELECT '江西','江西省','360000' UNION ALL
                SELECT '山东','山东省','370000' UNION ALL
                SELECT '河南','河南省','410000' UNION ALL
                SELECT '湖北','湖北省','420000' UNION ALL
                SELECT '湖南','湖南省','430000' UNION ALL
                SELECT '广东','广东省','440000' UNION ALL
                SELECT '广西','广西壮族自治区','450000' UNION ALL
                SELECT '海南','海南省','460000' UNION ALL
                SELECT '重庆','重庆市','500000' UNION ALL
                SELECT '四川','四川省','510000' UNION ALL
                SELECT '贵州','贵州省','520000' UNION ALL
                SELECT '云南','云南省','530000' UNION ALL
                SELECT '西藏','西藏自治区','540000' UNION ALL
                SELECT '陕西','陕西省','610000' UNION ALL
                SELECT '甘肃','甘肃省','620000' UNION ALL
                SELECT '青海','青海省','630000' UNION ALL
                SELECT '宁夏','宁夏回族自治区','640000' UNION ALL
                SELECT '新疆','新疆维吾尔自治区','650000' UNION ALL
                SELECT '台湾','台湾省','710000' UNION ALL
                SELECT '香港','香港特别行政区','810000' UNION ALL
                SELECT '澳门','澳门特别行政区','820000') m
              ON d.cust_province = m.short_name
GROUP BY 1,2,3,4 ORDER BY cnt DESC;



-- VDP_RS_TD.voc_sentiment_annotations_results_v definition
select
    cust_type
from
    voc_sentiment_annotations_results_v group by cust_type;

# 老客户
# 新客户
# 潜在客户

UPDATE voc_sentiment_annotations_results_v
SET cust_type = '会员客户'
WHERE cust_type = '潜在客户';

UPDATE voc_sentiment_annotations_results_v
SET cust_type = '车主客户'
WHERE cust_type = '新客户';

UPDATE voc_sentiment_annotations_results_v
SET cust_type = '普通用户'
WHERE cust_type = '老客户';


UPDATE voc_sentiment_annotations_results_v
SET adb_tag_first = '口碑'
WHERE adb_tag_first = '营销';

CREATE TABLE `voc_sentiment_annotations_results_v` (
  `id` VARCHAR(40) NOT NULL COMMENT "声音ID",
  `data_id` VARCHAR(40) NOT NULL COMMENT "原始数据ID",
  `data_create_time` DATE NOT NULL COMMENT "数据产生日期",
  `brand_code` VARCHAR(1000) NULL COMMENT "品牌编码",
  `car_series_code` VARCHAR(1000) NULL COMMENT "车系编码",
  `channel_catagory` VARCHAR(50) NULL COMMENT "渠道类别",
  `channel_code` VARCHAR(40) NOT NULL COMMENT "渠道编码",
  `channel_name` VARCHAR(50) NULL COMMENT "渠道名称",
  `brand_name` VARCHAR(100) NULL COMMENT "品牌名称",
  `car_series_name` VARCHAR(100) NULL COMMENT "车系名称",
  `content_type` VARCHAR(10) NULL COMMENT "内容类型",
  `sentiment` VARCHAR(100) NULL COMMENT "情感",
  `intention` VARCHAR(100) NULL COMMENT "意图",
  `create_date` DATE NULL COMMENT "创建日期（源）",
  `keywords` VARCHAR(65533) NULL COMMENT "关键词",
  `is_outer` VARCHAR(1048576) NULL COMMENT "是否外部数据标识",
  `hot_word` VARCHAR(1048576) NULL COMMENT "热词",
  `user_journey1` VARCHAR(1048576) NULL COMMENT "全旅程一级",
  `user_journey2` VARCHAR(1048576) NULL COMMENT "全旅程二级",
  `scenario` VARCHAR(1048576) NULL COMMENT "场景",
  `d2c_responsible_dept` VARCHAR(1048576) NULL COMMENT "D2C负责部门",
  `d2c_accountable_dept` VARCHAR(1048576) NULL COMMENT "D2C牵头部门",
  `d2c_cc_dept` VARCHAR(1048576) NULL COMMENT "D2C抄送部门",
  `one_id` VARCHAR(64) NULL COMMENT "oneId",
  `cust_id_card_type` VARCHAR(65533) NULL COMMENT "客户证件类型",
  `cust_id_card_no` VARCHAR(65533) NULL COMMENT "客户证件号码",
  `cust_mobile` VARCHAR(65533) NULL COMMENT "客户手机号",
  `cust_email` VARCHAR(65533) NULL COMMENT "客户邮箱",
  `cust_global_id` VARCHAR(65533) NULL COMMENT "客户全局ID",
  `cust_age` VARCHAR(65533) NULL COMMENT "客户年龄",
  `cust_gender` VARCHAR(65533) NULL COMMENT "客户性别",
  `cust_province_code` VARCHAR(60) NULL COMMENT "客户省份编码",
  `cust_province` VARCHAR(60) NULL COMMENT "客户省份",
  `cust_city_code` VARCHAR(60) NULL COMMENT "客户城市编码",
  `cust_city` VARCHAR(60) NULL COMMENT "客户城市",
  `cust_nick` VARCHAR(1048576) NULL COMMENT "客户昵称",
  `cust_type` VARCHAR(65533) NULL COMMENT "客户分类",
  `is_car_owner_flg` BIGINT NULL COMMENT "是否车主标识",
  `total_mnt_cnt` VARCHAR(1048576) NULL COMMENT "总维保次数",
  `veh_purch_price` VARCHAR(1048576) NULL COMMENT "购车价格",
  `veh_displ` VARCHAR(1048576) NULL COMMENT "排量",
  `reg_mnt_consum_lvl` VARCHAR(1048576) NULL COMMENT "常规保养消费水平",
  `spare_pt_mnt_consum_lvl` VARCHAR(1048576) NULL COMMENT "备件维保消费水平",
  `acc_mnt_consum_lvl` VARCHAR(1048576) NULL COMMENT "精品维保消费水平",
  `mnt_pkg_purch_cnt` VARCHAR(1048576) NULL COMMENT "保养套餐购买次数",
  `cust_accum_consum_amt` VARCHAR(1048576) NULL COMMENT "客户累计消费金额",
  `mnt_rpr_accum_cons_amt` VARCHAR(1048576) NULL COMMENT "维修保养累计消费金额",
  `mnt_accum_consum_amt` VARCHAR(1048576) NULL COMMENT "保养累计消费金额",
  `repair_accum_cons_amt` VARCHAR(1048576) NULL COMMENT "维修累计消费金额",
  `dustry` VARCHAR(1048576) NULL COMMENT "行业",
  `lead_level` VARCHAR(1048576) NULL COMMENT "线索等级",
  `lost_cat` VARCHAR(1048576) NULL COMMENT "流失类别",
  `is_store_visit_lead` VARCHAR(1048576) NULL COMMENT "是否到店线索",
  `mem_lvl` VARCHAR(1048576) NULL COMMENT "会员等级",
  `cert_mem_cat` VARCHAR(1048576) NULL COMMENT "认证会员类别",
  `is_active_mem` VARCHAR(1048576) NULL COMMENT "是否活跃会员",
  `accum_ca_purch_cnt` VARCHAR(1048576) NULL COMMENT "累计购车次数",
  `cust_source` VARCHAR(1048576) NULL COMMENT "客户来源",
  `consum_points_total` VARCHAR(1048576) NULL COMMENT "消费积分总数",
  `cust_value` VARCHAR(1048576) NULL COMMENT "客户价值",
  `main_chnl_pref` VARCHAR(1048576) NULL COMMENT "主要渠道偏好",
  `car_owner_type` VARCHAR(1048576) NULL COMMENT "车主类型",
  `vin` VARCHAR(1048576) NULL COMMENT "VIN",
  `vhl_col_name` VARCHAR(65533) NULL COMMENT "车型颜色名称",
  `vhl_product_date` VARCHAR(65533) NULL COMMENT "车辆生产日期",
  `vhl_offline_date` VARCHAR(65533) NULL COMMENT "车辆下线日期",
  `vhl_is_abroad` VARCHAR(65533) NULL COMMENT "是否海外",
  `vhl_dis_ch` VARCHAR(65533) NULL COMMENT "驱动形式-变速箱",
  `vhl_dis_mt` VARCHAR(65533) NULL COMMENT "驱动形式-手动/自动",
  `vhl_eng_clsf` VARCHAR(65533) NULL COMMENT "发动机分类",
  `vhl_eng_seris` VARCHAR(65533) NULL COMMENT "发动机系列",
  `vhl_veh_type` VARCHAR(65533) NULL COMMENT "车型类型",
  `vhl_country` VARCHAR(65533) NULL COMMENT "国家",
  `vhl_bd_clsf` VARCHAR(65533) NULL COMMENT "车身分类",
  `vhl_seg_mt` VARCHAR(65533) NULL COMMENT "细分市场",
  `vhl_pow_clsf` VARCHAR(65533) NULL COMMENT "动力分类",
  `vhl_fu_clsf` VARCHAR(65533) NULL COMMENT "燃料分类",
  `vhl_modl_st` VARCHAR(65533) NULL COMMENT "车型状态",
  `vhl_std_plnt_code` VARCHAR(65533) NULL COMMENT "标准工厂代码",
  `dlr_oc_id` VARCHAR(65533) NULL COMMENT "销售组织ID",
  `dlr_oc_code` VARCHAR(65533) NULL COMMENT "销售组织编码",
  `dlr_oc_name` VARCHAR(65533) NULL COMMENT "销售组织名称",
  `dlr_oc_province_code` VARCHAR(60) NULL COMMENT "销售组织省份编码",
  `dlr_oc_province` VARCHAR(1048576) NULL COMMENT "销售组织省份",
  `dlr_oc_city_code` VARCHAR(60) NULL COMMENT "销售组织城市编码",
  `dlr_oc_city` VARCHAR(1048576) NULL COMMENT "销售组织城市",
  `dlr_dc_id` VARCHAR(65533) NULL COMMENT "服务组织ID",
  `dlr_dc_code` VARCHAR(65533) NULL COMMENT "服务组织编码",
  `dlr_dc_name` VARCHAR(65533) NULL COMMENT "服务组织名称",
  `dlr_dc_province_code` VARCHAR(60) NULL COMMENT "服务组织省份编码",
  `dlr_dc_province` VARCHAR(1048576) NULL COMMENT "服务组织省份",
  `dlr_dc_city_code` VARCHAR(60) NULL COMMENT "服务组织城市编码",
  `dlr_dc_city` VARCHAR(1048576) NULL COMMENT "服务组织城市",
  `dlr_mc_id` VARCHAR(65533) NULL COMMENT "经销商管理公司ID",
  `dlr_mc_code` VARCHAR(65533) NULL COMMENT "经销商管理公司编码",
  `dlr_mc_name` VARCHAR(65533) NULL COMMENT "经销商管理公司名称",
  `dlr_mc_province_code` VARCHAR(60) NULL COMMENT "经销商管理公司省份编码",
  `dlr_mc_province` VARCHAR(1048576) NULL COMMENT "经销商管理公司省份",
  `dlr_mc_city_code` VARCHAR(60) NULL COMMENT "经销商管理公司城市编码",
  `dlr_mc_city` VARCHAR(1048576) NULL COMMENT "经销商管理公司城市",
  `is_wsater_army` VARCHAR(1048576) NULL COMMENT "是否水军",
  `is_manager_focused` VARCHAR(1048576) NULL COMMENT "是否管理层关注",
  `is_big_v` VARCHAR(1048576) NULL COMMENT "是否大V",
  `author_id` VARCHAR(1048576) NULL COMMENT "作者ID",
  `user_nick` VARCHAR(1048576) NULL COMMENT "用户昵称",
  `is_main_post` VARCHAR(1048576) NULL COMMENT "是否主贴",
  `original_link` VARCHAR(1048576) NULL COMMENT "原文链接",
  `view_count` VARCHAR(1048576) NULL COMMENT "浏览数",
  `comment_count` VARCHAR(1048576) NULL COMMENT "评论数",
  `like_count` VARCHAR(1048576) NULL COMMENT "点赞数",
  `share_count` VARCHAR(1048576) NULL COMMENT "分享数",
  `favorite_count` VARCHAR(1048576) NULL COMMENT "收藏数",
  `work_order_id` VARCHAR(1048576) NULL COMMENT "工单ID",
  `work_order_parent_id` VARCHAR(1048576) NULL COMMENT "父工单ID",
  `quest_type` VARCHAR(1048576) NULL COMMENT "问卷类型",
  `quest_answer_score` VARCHAR(1048576) NULL COMMENT "问卷答案分数",
  `quest_business_type` VARCHAR(1048576) NULL COMMENT "问卷业务类型",
  `quest_business_scenario` VARCHAR(1048576) NULL COMMENT "问卷业务场景",
  `model_code` VARCHAR(1048576) NULL COMMENT "车型编码",
  `model_name` VARCHAR(1048576) NULL COMMENT "车型名称",
  `opinion` VARCHAR(65533) NULL COMMENT "观点",
  `topic` VARCHAR(65533) NULL COMMENT "主题",
  `topic_text` VARCHAR(1048576) NULL COMMENT "主题文本",
  `vtr_tag_first_code` VARCHAR(1048576) NULL COMMENT "VRT标签编码1级",
  `vtr_tag_first` VARCHAR(1048576) NULL COMMENT "VRT标签1级",
  `vtr_tag_second_code` VARCHAR(1048576) NULL COMMENT "VRT标签编码2级",
  `vtr_tag_second` VARCHAR(1048576) NULL COMMENT "VRT标签2级",
  `vtr_tag_three_code` VARCHAR(1048576) NULL COMMENT "VRT标签编码3级",
  `vtr_tag_three` VARCHAR(1048576) NULL COMMENT "VRT标签3级",
  `vtr_tag_four_code` VARCHAR(1048576) NULL COMMENT "VRT标签编码4级",
  `vtr_tag_four` VARCHAR(1048576) NULL COMMENT "VRT标签4级",
  `com_tag_first_code` VARCHAR(1048576) NULL COMMENT "商品化属性标签编码1级",
  `com_tag_first` VARCHAR(1048576) NULL COMMENT "商品化属性标签1级",
  `com_tag_second_code` VARCHAR(1048576) NULL COMMENT "商品化属性标签编码2级",
  `com_tag_second` VARCHAR(1048576) NULL COMMENT "商品化属性标签2级",
  `com_tag_three_code` VARCHAR(1048576) NULL COMMENT "商品化属性标签编码3级",
  `com_tag_three` VARCHAR(1048576) NULL COMMENT "商品化属性标签3级",
  `com_tag_four_code` VARCHAR(1048576) NULL COMMENT "商品化属性标签编码4级",
  `com_tag_four` VARCHAR(1048576) NULL COMMENT "商品化属性标签4级",
  `adb_tag_first_code` VARCHAR(1048576) NULL COMMENT "全领域业务标签编码1级",
  `adb_tag_first` VARCHAR(1048576) NULL COMMENT "全领域业务标签1级",
  `adb_tag_second_code` VARCHAR(1048576) NULL COMMENT "全领域业务标签编码2级",
  `adb_tag_second` VARCHAR(1048576) NULL COMMENT "全领域业务标签2级",
  `adb_tag_three_code` VARCHAR(1048576) NULL COMMENT "全领域业务标签编码3级",
  `adb_tag_three` VARCHAR(1048576) NULL COMMENT "全领域业务标签3级",
  `adb_tag_four_code` VARCHAR(1048576) NULL COMMENT "全领域业务标签编码4级",
  `adb_tag_four` VARCHAR(1048576) NULL COMMENT "全领域业务标签4级",
  `cj_tag_first_code` VARCHAR(1048576) NULL COMMENT "全旅程客户标签编码1级",
  `cj_tag_first` VARCHAR(1048576) NULL COMMENT "全旅程客户标签1级",
  `cj_tag_second_code` VARCHAR(1048576) NULL COMMENT "全旅程客户标签编码2级",
  `cj_tag_second` VARCHAR(1048576) NULL COMMENT "全旅程客户标签2级",
  `cj_tag_three_code` VARCHAR(1048576) NULL COMMENT "全旅程客户标签编码3级",
  `cj_tag_three` VARCHAR(1048576) NULL COMMENT "全旅程客户标签3级",
  `cj_tag_four_code` VARCHAR(1048576) NULL COMMENT "全旅程客户标签编码4级",
  `cj_tag_four` VARCHAR(1048576) NULL COMMENT "全旅程客户标签4级",
  `nps_tag_first_code` VARCHAR(1048576) NULL COMMENT "NPS标签编码1级",
  `nps_tag_first` VARCHAR(1048576) NULL COMMENT "NPS标签1级",
  `nps_tag_second_code` VARCHAR(1048576) NULL COMMENT "NPS标签编码2级",
  `nps_tag_second` VARCHAR(1048576) NULL COMMENT "NPS标签2级",
  `nps_tag_three_code` VARCHAR(1048576) NULL COMMENT "NPS标签编码3级",
  `nps_tag_three` VARCHAR(1048576) NULL COMMENT "NPS标签3级",
  `nps_tag_four_code` VARCHAR(1048576) NULL COMMENT "NPS标签编码4级",
  `nps_tag_four` VARCHAR(1048576) NULL COMMENT "NPS标签4级",
  `tag_accuracy` VARCHAR(1048576) NULL COMMENT "标签-准确性",
  `tag_customer_issue_classification` VARCHAR(1048576) NULL COMMENT "标签-客户问题分类",
  `tag_issue_severity` VARCHAR(1048576) NULL COMMENT "标签-问题严重程度",
  `tag_code_status` VARCHAR(1048576) NULL COMMENT "标签-编码状态",
  `tag_business_domain` VARCHAR(1048576) NULL COMMENT "标签-业务域",
  `tag_event_clarity` VARCHAR(1048576) NULL COMMENT "标签-事件清晰度",
  `tag_high_value_flag` VARCHAR(1048576) NULL COMMENT "标签-高价值标识",
  `tag_complaint_flag_needing_reply` VARCHAR(1048576) NULL COMMENT "标签-需回复投诉标识",
  `tag_high_quality_voc_flag` VARCHAR(1048576) NULL COMMENT "标签-高质量VOC标识",
  `tag_new_energy_or_fuel` VARCHAR(1048576) NULL COMMENT "标签-新能源/燃油",
  `tag_need_forvclosed_loop` VARCHAR(1048576) NULL COMMENT "标签-需要闭环",
  `data_create_year` VARCHAR(1048576) NULL COMMENT "数据年",
  `data_create_quarter` VARCHAR(1048576) NULL COMMENT "数据季",
  `data_create_month` VARCHAR(1048576) NULL COMMENT "数据月",
  `data_create_week` VARCHAR(1048576) NULL COMMENT "数据周"
) ENGINE=OLAP
    DUPLICATE KEY(`id`)
COMMENT "voc所有声音数据宽表"
PARTITION BY RANGE(`data_create_time`) (
  PARTITION p2023_Q1 VALUES LESS THAN ("2023-04-01"),
  PARTITION p2023_Q2 VALUES LESS THAN ("2023-07-01"),
  PARTITION p2023_Q3 VALUES LESS THAN ("2023-10-01"),
  PARTITION p2023_Q4 VALUES LESS THAN ("2024-01-01"),
  PARTITION p2024_Q1 VALUES LESS THAN ("2024-04-01"),
  PARTITION p2024_Q2 VALUES LESS THAN ("2024-07-01"),
  PARTITION p2024_Q3 VALUES LESS THAN ("2024-10-01"),
  PARTITION p2024_Q4 VALUES LESS THAN ("2025-01-01"),
  PARTITION p2025_Q1 VALUES LESS THAN ("2025-04-01"),
  PARTITION p2025_Q2 VALUES LESS THAN ("2025-07-01"),
  PARTITION p2025_Q3 VALUES LESS THAN ("2025-10-01"),
  PARTITION p2025_Q4 VALUES LESS THAN ("2026-01-01"),
  PARTITION p2026_Q1 VALUES LESS THAN ("2026-04-01"),
  PARTITION p2026_Q2 VALUES LESS THAN ("2026-07-01"),
  PARTITION p2026_Q3 VALUES LESS THAN ("2026-10-01"),
  PARTITION p2026_Q4 VALUES LESS THAN ("2027-01-01")
)
DISTRIBUTED BY HASH(`id`) BUCKETS 10
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);

ALTER TABLE voc_overview RENAME voc_sentiment_annotations_results_v;


ALTER TABLE voc_sentiment_annotations_results_v RENAME voc_sentiment_annotations_results_v_old;

-- 修改表结构并添加备注说明
ALTER TABLE voc_sentiment_annotations_results_v
    COMMENT='voc声音数据表';

alter table voc_sentiment_annotations_results_v
    modify dealer_province_code varchar(20) null comment '经销商所属省份编码';

alter table voc_sentiment_annotations_results_v
    modify dealer_province varchar(100) null comment '经销商所属省份';

ALTER TABLE VDP_RS_TD.voc_sentiment_annotations_results_v MODIFY COLUMN dealer_province_code varchar(20) NULL COMMENT '经销商所属省份编码';
ALTER TABLE VDP_RS_TD.voc_sentiment_annotations_results_v MODIFY COLUMN dealer_province varchar(20) NULL COMMENT '经销商所属省份';


ALTER TABLE VDP_RS_TD.voc_sentiment_annotations_results_v ADD COLUMN dealer_regional_code VARCHAR(50) COMMENT '经销商所在大区编码';
ALTER TABLE VDP_RS_TD.voc_sentiment_annotations_results_v ADD COLUMN dealer_regional VARCHAR(100) COMMENT '经销商所在大区' ;



-- =============================
-- 新增：报告相关三张表（VDP_RS_TD）
-- =============================

-- 专项分析类型表
CREATE TABLE IF NOT EXISTS `VDP_RS_TD`.`report_special_analysis_type` (
  id VARCHAR(64) NOT NULL COMMENT 'ID',
  name VARCHAR(150) NOT NULL COMMENT '名称',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间(应用更新)',
  create_by VARCHAR(100) NULL COMMENT '创建人',
  update_by VARCHAR(100) NULL COMMENT '修改人',
  del_flag TINYINT NOT NULL DEFAULT "0" COMMENT '0正常,1已删除',
  description VARCHAR(255) NULL COMMENT '描述'
)
ENGINE=OLAP
PRIMARY KEY(`id`)
COMMENT "专项分析类型表"
DISTRIBUTED BY HASH(`id`) BUCKETS 8
ORDER BY(`id`)
PROPERTIES (
"compression" = "LZ4",
"enable_persistent_index" = "false",
"fast_schema_evolution" = "true",
"replicated_storage" = "true",
"replication_num" = "1"
);


INSERT INTO report_special_analysis_type
(id, name, create_time, update_time, create_by, update_by, del_flag, description)
VALUES
    ('sat_brand',      '品牌分析', NOW(), NOW(), 'admin', 'admin', 0, '品牌维度专项分析'),
    ('sat_series',     '车系分析', NOW(), NOW(), 'admin', 'admin', 0, '车系维度专项分析'),
    ('sat_competitor', '竞品分析', NOW(), NOW(), 'admin', 'admin', 0, '竞品对比专项分析'),
    ('sat_journey',    '用户旅程', NOW(), NOW(), 'admin', 'admin', 0, '用户旅程专项分析'),
    ('sat_design',     '产品设计', NOW(), NOW(), 'admin', 'admin', 0, '产品设计专项分析'),
    ('sat_marketing',  '营销服务', NOW(), NOW(), 'admin', 'admin', 0, '营销服务专项分析');


-- 自定义报告表
CREATE TABLE IF NOT EXISTS `VDP_RS_TD`.`report_custom_report` (
  id VARCHAR(64) NOT NULL COMMENT 'ID',
  report_name VARCHAR(250) NOT NULL COMMENT '报告名称',
  view_count BIGINT NOT NULL DEFAULT "0" COMMENT '浏览数',
  collection_count BIGINT NOT NULL DEFAULT "0" COMMENT '收藏量',
  type TINYINT NOT NULL COMMENT '类型 1:voc, 2:智能问数',
  default_condition STRING NULL COMMENT '默认条件(JSON字符串)',
  brand_code VARCHAR(100) NULL COMMENT '品牌编码',
  special_type_id VARCHAR(64) NULL COMMENT '专项类型ID',
  status TINYINT NOT NULL DEFAULT "0" COMMENT '状态 0未发布,1已发布',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间(应用更新)',
  create_by VARCHAR(100) NULL COMMENT '创建人',
  update_by VARCHAR(100) NULL COMMENT '修改人',
  del_flag TINYINT NOT NULL DEFAULT "0" COMMENT '0正常,1已删除',
  description VARCHAR(255) NULL COMMENT '描述'
)
ENGINE=OLAP
PRIMARY KEY(`id`)
COMMENT "自定义报告表"
DISTRIBUTED BY HASH(`id`) BUCKETS 8
ORDER BY(`id`, `status`, `special_type_id`)
PROPERTIES (
"compression" = "LZ4",
"enable_persistent_index" = "false",
"fast_schema_evolution" = "true",
"replicated_storage" = "true",
"replication_num" = "1"
);


INSERT INTO report_custom_report
(id, report_name, view_count, collection_count, type, default_condition, brand_code, special_type_id, status, create_time, update_time, create_by, update_by, del_flag, description)
VALUES
-- 品牌分析
('cr_brand_0001','长安汽车集团品牌整体分析',            2819,245,1,'{\"period\":\"2025-07\",\"brandCode\":\"A127\"}','A127','sat_brand',1,'2025-07-30 10:00:00','2025-07-30 10:00:00','admin','admin',0,'品牌整体洞察'),
('cr_brand_0002','深蓝汽车品牌舆情分析',                1650,110,1,'{\"period\":\"2025-07\",\"brand\":\"深蓝\"}',     NULL,'sat_brand',1,'2025-07-30 10:05:00','2025-07-30 10:05:00','admin','admin',0,'品牌舆情走势'),

-- 车系分析
('cr_series_0001','2025年Q1 CS75 关注度趋势分析',        1320,95, 1,'{\"period\":\"2025-Q1\",\"seriesCode\":\"CS75\"}','A127','sat_series',1,'2025-07-30 10:10:00','2025-07-30 10:10:00','admin','admin',0,'车系关注度'),
('cr_series_0002','2025年Q2 CS55 热点议题TOP10',         1180,82, 1,'{\"period\":\"2025-Q2\",\"seriesCode\":\"CS55\",\"top\":10}','A127','sat_series',1,'2025-07-30 10:12:00','2025-07-30 10:12:00','admin','admin',0,'车系热点'),

-- 竞品分析
('cr_comp_0001','长安汽车集团与奥迪A5外观设计对比',     2320,176,1,'{\"period\":\"2025-07\",\"compare\":[\"长安\",\"奥迪A5\"]}','A127','sat_competitor',1,'2025-07-30 10:15:00','2025-07-30 10:15:00','admin','admin',0,'竞品外观对比'),
('cr_comp_0002','2025年主要竞品口碑对比分析',            2010,150,1,'{\"period\":\"2025-07\",\"brands\":[\"长安\",\"丰田\",\"本田\"]}','A127','sat_competitor',1,'2025-07-30 10:18:00','2025-07-30 10:18:00','admin','admin',0,'竞品口碑'),

-- 用户旅程
('cr_journey_0001','售前阶段用户关注点分析',              980, 70, 1,'{\"period\":\"2025-07\",\"journey\":\"售前\"}',      'A127','sat_journey',1,'2025-07-30 10:20:00','2025-07-30 10:20:00','admin','admin',0,'售前关注点'),
('cr_journey_0002','交付阶段用户反馈分析',                890, 66, 1,'{\"period\":\"2025-07\",\"journey\":\"交付\"}',      'A127','sat_journey',1,'2025-07-30 10:22:00','2025-07-30 10:22:00','admin','admin',0,'交付反馈'),

-- 产品设计
('cr_design_0001','内饰设计偏好分析',                    1205,88, 1,'{\"period\":\"2025-07\",\"topic\":\"内饰\"}',        'A127','sat_design',1,'2025-07-30 10:25:00','2025-07-30 10:25:00','admin','admin',0,'内饰偏好'),
('cr_design_0002','外观设计痛点分析',                    1410,99, 1,'{\"period\":\"2025-07\",\"topic\":\"外观\"}',        'A127','sat_design',1,'2025-07-30 10:27:00','2025-07-30 10:27:00','admin','admin',0,'外观痛点'),

-- 营销服务
('cr_marketing_0001','营销活动触达效果分析',              1050,77, 1,'{\"period\":\"2025-07\",\"activity\":\"暑期促销\"}','A127','sat_marketing',1,'2025-07-30 10:30:00','2025-07-30 10:30:00','admin','admin',0,'活动触达'),
('cr_marketing_0002','售后服务满意度分析',                 990, 73, 1,'{\"period\":\"2025-07\",\"service\":\"售后\"}',       'A127','sat_marketing',1,'2025-07-30 10:32:00','2025-07-30 10:32:00','admin','admin',0,'售后满意度');


-- 报告查看记录表
CREATE TABLE IF NOT EXISTS `VDP_RS_TD`.`report_view_log` (
  id VARCHAR(64) NOT NULL COMMENT 'ID',
  report_id VARCHAR(64) NOT NULL COMMENT '报告ID',
  user_id VARCHAR(64) NULL COMMENT '查看人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间(应用更新)',
  create_by VARCHAR(100) NULL COMMENT '创建人'
)
ENGINE=OLAP
PRIMARY KEY(`id`)
COMMENT "报告查看记录表"
DISTRIBUTED BY HASH(`id`) BUCKETS 8
ORDER BY(`id`, `report_id`, `user_id`)
PROPERTIES (
"compression" = "LZ4",
"enable_persistent_index" = "false",
"fast_schema_evolution" = "true",
"replicated_storage" = "true",
"replication_num" = "1"
);


-- 按类型统计自定义报告数量
SELECT t.name AS special_type, COUNT(*) AS report_cnt
FROM report_custom_report r
         JOIN report_special_analysis_type t ON r.special_type_id = t.id
GROUP BY t.name
ORDER BY report_cnt DESC;


USE VDP_RS_TD;


ALTER TABLE `VDP_RS_TD`.`report_special_analysis_type`
    ADD COLUMN `sort_no` INT DEFAULT "0" COMMENT "排序(越小越靠前)";

UPDATE VDP_RS_TD.report_special_analysis_type SET sort_no = 1 WHERE name = '品牌分析';
UPDATE VDP_RS_TD.report_special_analysis_type SET sort_no = 2 WHERE name = '车系分析';
UPDATE VDP_RS_TD.report_special_analysis_type SET sort_no = 3 WHERE name = '竞品分析';
UPDATE VDP_RS_TD.report_special_analysis_type SET sort_no = 4 WHERE name = '用户旅程';
UPDATE VDP_RS_TD.report_special_analysis_type SET sort_no = 5 WHERE name = '产品设计';
UPDATE VDP_RS_TD.report_special_analysis_type SET sort_no = 6 WHERE name = '营销服务';



-- 增加字段：icon（专项分析类型图标）
ALTER TABLE `VDP_RS_TD`.`report_special_analysis_type`
    ADD COLUMN `icon` VARCHAR(255) NULL COMMENT "图标(名称或URL)";



UPDATE VDP_RS_TD.report_special_analysis_type SET icon = 'brand_analy' WHERE name = '品牌分析';
UPDATE VDP_RS_TD.report_special_analysis_type SET icon = 'series_analy' WHERE name = '车系分析';
UPDATE VDP_RS_TD.report_special_analysis_type SET icon = 'competitor_analy' WHERE name = '竞品分析';
UPDATE VDP_RS_TD.report_special_analysis_type SET icon = 'journey_analy' WHERE name = '用户旅程';
UPDATE VDP_RS_TD.report_special_analysis_type SET icon = 'design_analy' WHERE name = '产品设计';
UPDATE VDP_RS_TD.report_special_analysis_type SET icon = 'market_analy' WHERE name = '营销服务';


-- ============================================
-- 新建支持 UPDATE 的表：voc_sentiment_annotations_results_v_new
-- 说明：采用 PRIMARY KEY(id, data_create_time) 模型，支持按主键更新其它字段（分区列必须为Key列）。
-- ============================================
USE VDP_RS_TD;

DROP TABLE IF EXISTS voc_sentiment_annotations_results_v_new;
CREATE TABLE IF NOT EXISTS `voc_sentiment_annotations_results_v_new` (
  `id` VARCHAR(40) NOT NULL COMMENT "声音ID",
  `data_create_time` DATE NOT NULL COMMENT "数据产生日期",
  `data_id` VARCHAR(40) NOT NULL COMMENT "原始数据ID",
  `brand_code` VARCHAR(1000) NULL COMMENT "品牌编码",
  `car_series_code` VARCHAR(1000) NULL COMMENT "车系编码",
  `channel_catagory` VARCHAR(50) NULL COMMENT "渠道类别",
  `channel_code` VARCHAR(40) NOT NULL COMMENT "渠道编码",
  `channel_name` VARCHAR(50) NULL COMMENT "渠道名称",
  `brand_name` VARCHAR(100) NULL COMMENT "品牌名称",
  `car_series_name` VARCHAR(100) NULL COMMENT "车系名称",
  `content_type` VARCHAR(10) NULL COMMENT "内容类型",
  `sentiment` VARCHAR(100) NULL COMMENT "情感",
  `intention` VARCHAR(100) NULL COMMENT "意图",
  `create_date` DATE NULL COMMENT "创建日期（源）",
  `keywords` VARCHAR(65533) NULL COMMENT "关键词",
  `is_outer` VARCHAR(1048576) NULL COMMENT "是否外部数据标识",
  `hot_word` VARCHAR(1048576) NULL COMMENT "热词",
  `user_journey1` VARCHAR(1048576) NULL COMMENT "全旅程一级",
  `user_journey2` VARCHAR(1048576) NULL COMMENT "全旅程二级",
  `scenario` VARCHAR(1048576) NULL COMMENT "场景",
  `d2c_responsible_dept` VARCHAR(1048576) NULL COMMENT "D2C负责部门",
  `d2c_accountable_dept` VARCHAR(1048576) NULL COMMENT "D2C牵头部门",
  `d2c_cc_dept` VARCHAR(1048576) NULL COMMENT "D2C抄送部门",
  `one_id` VARCHAR(64) NULL COMMENT "oneId",
  `cust_id_card_type` VARCHAR(65533) NULL COMMENT "客户证件类型",
  `cust_id_card_no` VARCHAR(65533) NULL COMMENT "客户证件号码",
  `cust_mobile` VARCHAR(65533) NULL COMMENT "客户手机号",
  `cust_email` VARCHAR(65533) NULL COMMENT "客户邮箱",
  `cust_global_id` VARCHAR(65533) NULL COMMENT "客户全局ID",
  `cust_age` VARCHAR(65533) NULL COMMENT "客户年龄",
  `cust_gender` VARCHAR(65533) NULL COMMENT "客户性别",
  `cust_province_code` VARCHAR(60) NULL COMMENT "客户省份编码",
  `cust_province` VARCHAR(60) NULL COMMENT "客户省份",
  `cust_city_code` VARCHAR(60) NULL COMMENT "客户城市编码",
  `cust_city` VARCHAR(60) NULL COMMENT "客户城市",
  `cust_nick` VARCHAR(1048576) NULL COMMENT "客户昵称",
  `cust_type` VARCHAR(65533) NULL COMMENT "客户分类",
  `is_car_owner_flg` BIGINT NULL COMMENT "是否车主标识",
  `total_mnt_cnt` VARCHAR(1048576) NULL COMMENT "总维保次数",
  `veh_purch_price` VARCHAR(1048576) NULL COMMENT "购车价格",
  `veh_displ` VARCHAR(1048576) NULL COMMENT "排量",
  `reg_mnt_consum_lvl` VARCHAR(1048576) NULL COMMENT "常规保养消费水平",
  `spare_pt_mnt_consum_lvl` VARCHAR(1048576) NULL COMMENT "备件维保消费水平",
  `acc_mnt_consum_lvl` VARCHAR(1048576) NULL COMMENT "精品维保消费水平",
  `mnt_pkg_purch_cnt` VARCHAR(1048576) NULL COMMENT "保养套餐购买次数",
  `cust_accum_consum_amt` VARCHAR(1048576) NULL COMMENT "客户累计消费金额",
  `mnt_rpr_accum_cons_amt` VARCHAR(1048576) NULL COMMENT "维修保养累计消费金额",
  `mnt_accum_consum_amt` VARCHAR(1048576) NULL COMMENT "保养累计消费金额",
  `repair_accum_cons_amt` VARCHAR(1048576) NULL COMMENT "维修累计消费金额",
  `dustry` VARCHAR(1048576) NULL COMMENT "行业",
  `lead_level` VARCHAR(1048576) NULL COMMENT "线索等级",
  `lost_cat` VARCHAR(1048576) NULL COMMENT "流失类别",
  `is_store_visit_lead` VARCHAR(1048576) NULL COMMENT "是否到店线索",
  `mem_lvl` VARCHAR(1048576) NULL COMMENT "会员等级",
  `cert_mem_cat` VARCHAR(1048576) NULL COMMENT "认证会员类别",
  `is_active_mem` VARCHAR(1048576) NULL COMMENT "是否活跃会员",
  `accum_ca_purch_cnt` VARCHAR(1048576) NULL COMMENT "累计购车次数",
  `cust_source` VARCHAR(1048576) NULL COMMENT "客户来源",
  `consum_points_total` VARCHAR(1048576) NULL COMMENT "消费积分总数",
  `cust_value` VARCHAR(1048576) NULL COMMENT "客户价值",
  `main_chnl_pref` VARCHAR(1048576) NULL COMMENT "主要渠道偏好",
  `car_owner_type` VARCHAR(1048576) NULL COMMENT "车主类型",
  `vin` VARCHAR(1048576) NULL COMMENT "VIN",
  `vhl_col_name` VARCHAR(65533) NULL COMMENT "车型颜色名称",
  `vhl_product_date` VARCHAR(65533) NULL COMMENT "车辆生产日期",
  `vhl_offline_date` VARCHAR(65533) NULL COMMENT "车辆下线日期",
  `vhl_is_abroad` VARCHAR(65533) NULL COMMENT "是否海外",
  `vhl_dis_ch` VARCHAR(65533) NULL COMMENT "驱动形式-变速箱",
  `vhl_dis_mt` VARCHAR(65533) NULL COMMENT "驱动形式-手动/自动",
  `vhl_eng_clsf` VARCHAR(65533) NULL COMMENT "发动机分类",
  `vhl_eng_seris` VARCHAR(65533) NULL COMMENT "发动机系列",
  `vhl_veh_type` VARCHAR(65533) NULL COMMENT "车型类型",
  `vhl_country` VARCHAR(65533) NULL COMMENT "国家",
  `vhl_bd_clsf` VARCHAR(65533) NULL COMMENT "车身分类",
  `vhl_seg_mt` VARCHAR(65533) NULL COMMENT "细分市场",
  `vhl_pow_clsf` VARCHAR(65533) NULL COMMENT "动力分类",
  `vhl_fu_clsf` VARCHAR(65533) NULL COMMENT "燃料分类",
  `vhl_modl_st` VARCHAR(65533) NULL COMMENT "车型状态",
  `vhl_std_plnt_code` VARCHAR(65533) NULL COMMENT "标准工厂代码",
  `dlr_oc_id` VARCHAR(65533) NULL COMMENT "销售组织ID",
  `dlr_oc_code` VARCHAR(65533) NULL COMMENT "销售组织编码",
  `dlr_oc_name` VARCHAR(65533) NULL COMMENT "销售组织名称",
  `dlr_oc_province_code` VARCHAR(60) NULL COMMENT "销售组织省份编码",
  `dlr_oc_province` VARCHAR(1048576) NULL COMMENT "销售组织省份",
  `dlr_oc_city_code` VARCHAR(60) NULL COMMENT "销售组织城市编码",
  `dlr_oc_city` VARCHAR(1048576) NULL COMMENT "销售组织城市",
  `dlr_dc_id` VARCHAR(65533) NULL COMMENT "服务组织ID",
  `dlr_dc_code` VARCHAR(65533) NULL COMMENT "服务组织编码",
  `dlr_dc_name` VARCHAR(65533) NULL COMMENT "服务组织名称",
  `dlr_dc_province_code` VARCHAR(60) NULL COMMENT "服务组织省份编码",
  `dlr_dc_province` VARCHAR(1048576) NULL COMMENT "服务组织省份",
  `dlr_dc_city_code` VARCHAR(60) NULL COMMENT "服务组织城市编码",
  `dlr_dc_city` VARCHAR(1048576) NULL COMMENT "服务组织城市",
  `dlr_mc_id` VARCHAR(65533) NULL COMMENT "经销商管理公司ID",
  `dlr_mc_code` VARCHAR(65533) NULL COMMENT "经销商管理公司编码",
  `dlr_mc_name` VARCHAR(65533) NULL COMMENT "经销商管理公司名称",
  `dlr_mc_province_code` VARCHAR(60) NULL COMMENT "经销商管理公司省份编码",
  `dlr_mc_province` VARCHAR(1048576) NULL COMMENT "经销商管理公司省份",
  `dlr_mc_city_code` VARCHAR(60) NULL COMMENT "经销商管理公司城市编码",
  `dlr_mc_city` VARCHAR(1048576) NULL COMMENT "经销商管理公司城市",
  `is_wsater_army` VARCHAR(1048576) NULL COMMENT "是否水军",
  `is_manager_focused` VARCHAR(1048576) NULL COMMENT "是否管理层关注",
  `is_big_v` VARCHAR(1048576) NULL COMMENT "是否大V",
  `author_id` VARCHAR(1048576) NULL COMMENT "作者ID",
  `user_nick` VARCHAR(1048576) NULL COMMENT "用户昵称",
  `is_main_post` VARCHAR(1048576) NULL COMMENT "是否主贴",
  `original_link` VARCHAR(1048576) NULL COMMENT "原文链接",
  `view_count` VARCHAR(1048576) NULL COMMENT "浏览数",
  `comment_count` VARCHAR(1048576) NULL COMMENT "评论数",
  `like_count` VARCHAR(1048576) NULL COMMENT "点赞数",
  `share_count` VARCHAR(1048576) NULL COMMENT "分享数",
  `favorite_count` VARCHAR(1048576) NULL COMMENT "收藏数",
  `work_order_id` VARCHAR(1048576) NULL COMMENT "工单ID",
  `work_order_parent_id` VARCHAR(1048576) NULL COMMENT "父工单ID",
  `quest_type` VARCHAR(1048576) NULL COMMENT "问卷类型",
  `quest_answer_score` VARCHAR(1048576) NULL COMMENT "问卷答案分数",
  `quest_business_type` VARCHAR(1048576) NULL COMMENT "问卷业务类型",
  `quest_business_scenario` VARCHAR(1048576) NULL COMMENT "问卷业务场景",
  `model_code` VARCHAR(1048576) NULL COMMENT "车型编码",
  `model_name` VARCHAR(1048576) NULL COMMENT "车型名称",
  `opinion` VARCHAR(65533) NULL COMMENT "观点",
  `topic` VARCHAR(65533) NULL COMMENT "主题",
  `topic_text` VARCHAR(1048576) NULL COMMENT "主题文本",
  `vtr_tag_first_code` VARCHAR(1048576) NULL COMMENT "VRT标签编码1级",
  `vtr_tag_first` VARCHAR(1048576) NULL COMMENT "VRT标签1级",
  `vtr_tag_second_code` VARCHAR(1048576) NULL COMMENT "VRT标签编码2级",
  `vtr_tag_second` VARCHAR(1048576) NULL COMMENT "VRT标签2级",
  `vtr_tag_three_code` VARCHAR(1048576) NULL COMMENT "VRT标签编码3级",
  `vtr_tag_three` VARCHAR(1048576) NULL COMMENT "VRT标签3级",
  `vtr_tag_four_code` VARCHAR(1048576) NULL COMMENT "VRT标签编码4级",
  `vtr_tag_four` VARCHAR(1048576) NULL COMMENT "VRT标签4级",
  `com_tag_first_code` VARCHAR(1048576) NULL COMMENT "商品化属性标签编码1级",
  `com_tag_first` VARCHAR(1048576) NULL COMMENT "商品化属性标签1级",
  `com_tag_second_code` VARCHAR(1048576) NULL COMMENT "商品化属性标签编码2级",
  `com_tag_second` VARCHAR(1048576) NULL COMMENT "商品化属性标签2级",
  `com_tag_three_code` VARCHAR(1048576) NULL COMMENT "商品化属性标签编码3级",
  `com_tag_three` VARCHAR(1048576) NULL COMMENT "商品化属性标签3级",
  `com_tag_four_code` VARCHAR(1048576) NULL COMMENT "商品化属性标签编码4级",
  `com_tag_four` VARCHAR(1048576) NULL COMMENT "商品化属性标签4级",
  `adb_tag_first_code` VARCHAR(1048576) NULL COMMENT "全领域业务标签编码1级",
  `adb_tag_first` VARCHAR(1048576) NULL COMMENT "全领域业务标签1级",
  `adb_tag_second_code` VARCHAR(1048576) NULL COMMENT "全领域业务标签编码2级",
  `adb_tag_second` VARCHAR(1048576) NULL COMMENT "全领域业务标签2级",
  `adb_tag_three_code` VARCHAR(1048576) NULL COMMENT "全领域业务标签编码3级",
  `adb_tag_three` VARCHAR(1048576) NULL COMMENT "全领域业务标签3级",
  `adb_tag_four_code` VARCHAR(1048576) NULL COMMENT "全领域业务标签编码4级",
  `adb_tag_four` VARCHAR(1048576) NULL COMMENT "全领域业务标签4级",
  `cj_tag_first_code` VARCHAR(1048576) NULL COMMENT "全旅程客户标签编码1级",
  `cj_tag_first` VARCHAR(1048576) NULL COMMENT "全旅程客户标签1级",
  `cj_tag_second_code` VARCHAR(1048576) NULL COMMENT "全旅程客户标签编码2级",
  `cj_tag_second` VARCHAR(1048576) NULL COMMENT "全旅程客户标签2级",
  `cj_tag_three_code` VARCHAR(1048576) NULL COMMENT "全旅程客户标签编码3级",
  `cj_tag_three` VARCHAR(1048576) NULL COMMENT "全旅程客户标签3级",
  `cj_tag_four_code` VARCHAR(1048576) NULL COMMENT "全旅程客户标签编码4级",
  `cj_tag_four` VARCHAR(1048576) NULL COMMENT "全旅程客户标签4级",
  `nps_tag_first_code` VARCHAR(1048576) NULL COMMENT "NPS标签编码1级",
  `nps_tag_first` VARCHAR(1048576) NULL COMMENT "NPS标签1级",
  `nps_tag_second_code` VARCHAR(1048576) NULL COMMENT "NPS标签编码2级",
  `nps_tag_second` VARCHAR(1048576) NULL COMMENT "NPS标签2级",
  `nps_tag_three_code` VARCHAR(1048576) NULL COMMENT "NPS标签编码3级",
  `nps_tag_three` VARCHAR(1048576) NULL COMMENT "NPS标签3级",
  `nps_tag_four_code` VARCHAR(1048576) NULL COMMENT "NPS标签编码4级",
  `nps_tag_four` VARCHAR(1048576) NULL COMMENT "NPS标签4级",
  `tag_accuracy` VARCHAR(1048576) NULL COMMENT "标签-准确性",
  `tag_customer_issue_classification` VARCHAR(1048576) NULL COMMENT "标签-客户问题分类",
  `tag_issue_severity` VARCHAR(1048576) NULL COMMENT "标签-问题严重程度",
  `tag_code_status` VARCHAR(1048576) NULL COMMENT "标签-编码状态",
  `tag_business_domain` VARCHAR(1048576) NULL COMMENT "标签-业务域",
  `tag_event_clarity` VARCHAR(1048576) NULL COMMENT "标签-事件清晰度",
  `tag_high_value_flag` VARCHAR(1048576) NULL COMMENT "标签-高价值标识",
  `tag_complaint_flag_needing_reply` VARCHAR(1048576) NULL COMMENT "标签-需回复投诉标识",
  `tag_high_quality_voc_flag` VARCHAR(1048576) NULL COMMENT "标签-高质量VOC标识",
  `tag_new_energy_or_fuel` VARCHAR(1048576) NULL COMMENT "标签-新能源/燃油",
  `tag_need_forvclosed_loop` VARCHAR(1048576) NULL COMMENT "标签-需要闭环",
  `data_create_year` VARCHAR(1048576) NULL COMMENT "数据年",
  `data_create_quarter` VARCHAR(1048576) NULL COMMENT "数据季",
  `data_create_month` VARCHAR(1048576) NULL COMMENT "数据月",
  `data_create_week` VARCHAR(1048576) NULL COMMENT "数据周"
) ENGINE=OLAP
PRIMARY KEY(`id`, `data_create_time`)
COMMENT "voc所有声音数据宽表（可更新）"
PARTITION BY RANGE(`data_create_time`) (
  PARTITION p2023_Q1 VALUES LESS THAN ("2023-04-01"),
  PARTITION p2023_Q2 VALUES LESS THAN ("2023-07-01"),
  PARTITION p2023_Q3 VALUES LESS THAN ("2023-10-01"),
  PARTITION p2023_Q4 VALUES LESS THAN ("2024-01-01"),
  PARTITION p2024_Q1 VALUES LESS THAN ("2024-04-01"),
  PARTITION p2024_Q2 VALUES LESS THAN ("2024-07-01"),
  PARTITION p2024_Q3 VALUES LESS THAN ("2024-10-01"),
  PARTITION p2024_Q4 VALUES LESS THAN ("2025-01-01"),
  PARTITION p2025_Q1 VALUES LESS THAN ("2025-04-01"),
  PARTITION p2025_Q2 VALUES LESS THAN ("2025-07-01"),
  PARTITION p2025_Q3 VALUES LESS THAN ("2025-10-01"),
  PARTITION p2025_Q4 VALUES LESS THAN ("2026-01-01"),
  PARTITION p2026_Q1 VALUES LESS THAN ("2026-04-01"),
  PARTITION p2026_Q2 VALUES LESS THAN ("2026-07-01"),
  PARTITION p2026_Q3 VALUES LESS THAN ("2026-10-01"),
  PARTITION p2026_Q4 VALUES LESS THAN ("2027-01-01")
)
DISTRIBUTED BY HASH(`id`, `data_create_time`) BUCKETS 10
PROPERTIES (
  "replication_num" = "1",
  "in_memory" = "false",
  "enable_persistent_index" = "false",
  "replicated_storage" = "true",
  "compression" = "LZ4"
);

-- 将旧表数据一次性插入到新表（显式列映射，避免列顺序差异）
INSERT INTO voc_sentiment_annotations_results_v_new (
  id, data_create_time, data_id,
  brand_code, car_series_code, channel_catagory, channel_code, channel_name,
  brand_name, car_series_name, content_type, sentiment, intention, create_date,
  keywords, is_outer, hot_word, user_journey1, user_journey2, scenario,
  d2c_responsible_dept, d2c_accountable_dept, d2c_cc_dept,
  one_id, cust_id_card_type, cust_id_card_no, cust_mobile, cust_email,
  cust_global_id, cust_age, cust_gender,
  cust_province_code, cust_province, cust_city_code, cust_city,
  cust_nick, cust_type, is_car_owner_flg,
  total_mnt_cnt, veh_purch_price, veh_displ,
  reg_mnt_consum_lvl, spare_pt_mnt_consum_lvl, acc_mnt_consum_lvl,
  mnt_pkg_purch_cnt, cust_accum_consum_amt, mnt_rpr_accum_cons_amt,
  mnt_accum_consum_amt, repair_accum_cons_amt,
  dustry, lead_level, lost_cat, is_store_visit_lead,
  mem_lvl, cert_mem_cat, is_active_mem, accum_ca_purch_cnt,
  cust_source, consum_points_total, cust_value, main_chnl_pref,
  car_owner_type, vin,
  vhl_col_name, vhl_product_date, vhl_offline_date, vhl_is_abroad,
  vhl_dis_ch, vhl_dis_mt, vhl_eng_clsf, vhl_eng_seris, vhl_veh_type,
  vhl_country, vhl_bd_clsf, vhl_seg_mt, vhl_pow_clsf, vhl_fu_clsf,
  vhl_modl_st, vhl_std_plnt_code,
  dlr_oc_id, dlr_oc_code, dlr_oc_name, dlr_oc_province_code, dlr_oc_province,
  dlr_oc_city_code, dlr_oc_city,
  dlr_dc_id, dlr_dc_code, dlr_dc_name, dlr_dc_province_code, dlr_dc_province,
  dlr_dc_city_code, dlr_dc_city,
  dlr_mc_id, dlr_mc_code, dlr_mc_name, dlr_mc_province_code, dlr_mc_province,
  dlr_mc_city_code, dlr_mc_city,
  is_wsater_army, is_manager_focused, is_big_v,

-- ============================================================
-- 用户旅程标签（CJ）变更后的批量更新示例
-- 说明：以下更新基于截图中阶段变更：认知/选择/购买/使用/维护/再购
--       如数据存在更细致的映射规则，请据此扩展 CASE 条件。
-- ============================================================

-- A) 一级标签与用户旅程一级映射：售前→认知，售中/交付→购买，售后/维保→维护，增换购/二手车→再购
UPDATE voc_sentiment_annotations_results_v
SET
  user_journey1 = CASE user_journey1
    WHEN '售前' THEN '认知'
    WHEN '售中' THEN '购买'
    WHEN '交付' THEN '购买'
    WHEN '售后' THEN '维护'
    WHEN '维保' THEN '维护'
    WHEN '增换购' THEN '再购'
    WHEN '二手车' THEN '再购'
    ELSE user_journey1 END,
  cj_tag_first = CASE cj_tag_first
    WHEN '售前' THEN '认知'
    WHEN '售中' THEN '购买'
    WHEN '交付' THEN '购买'
    WHEN '售后' THEN '维护'
    WHEN '维保' THEN '维护'
    WHEN '增换购' THEN '再购'
    WHEN '二手车' THEN '再购'
    ELSE cj_tag_first END,
  cj_tag_first_code = CASE cj_tag_first
    WHEN '售前' THEN 'CJ001'
    WHEN '售中' THEN 'CJ003'
    WHEN '交付' THEN 'CJ003'
    WHEN '使用' THEN 'CJ004'
    WHEN '售后' THEN 'CJ005'
    WHEN '维保' THEN 'CJ005'
    WHEN '增换购' THEN 'CJ006'
    WHEN '二手车' THEN 'CJ006'
    ELSE cj_tag_first_code END
WHERE cj_tag_first IN ('售前','售中','交付','售后','维保','增换购','二手车')
   OR user_journey1 IN ('售前','售中','交付','售后','维保','增换购','二手车');

-- B) 二级标签与用户旅程二级映射
--    咨询对比/试乘试驾/金融方案 → 选择/对比评估（三级建议统一到“方案选择”）
--    车辆交付 → 购买/下订成交（三级统一到“签约提车”）
--    驾驶体验/充电补能 → 使用/日常用车（三级“驾驶体验”）
--    维保服务 → 维护/保养维修（三级“维保服务”）
UPDATE voc_sentiment_annotations_results_v
SET
  user_journey2 = CASE user_journey2
    WHEN '咨询对比' THEN '对比评估'
    WHEN '试乘试驾' THEN '方案选择'
    WHEN '下订成交' THEN '下订成交'
    WHEN '金融方案' THEN '方案选择'
    WHEN '车辆交付' THEN '签约提车'
    WHEN '驾驶体验' THEN '日常用车'
    WHEN '充电补能' THEN '日常用车'
    WHEN '维保服务' THEN '保养维修'
    ELSE user_journey2 END,
  cj_tag_second = CASE cj_tag_second
    WHEN '咨询对比' THEN '对比评估'
    WHEN '试乘试驾' THEN '对比评估'
    WHEN '下订成交' THEN '下订成交'
    WHEN '金融方案' THEN '对比评估'
    WHEN '车辆交付' THEN '下订成交'
    WHEN '驾驶体验' THEN '日常用车'
    WHEN '充电补能' THEN '日常用车'
    WHEN '维保服务' THEN '保养维修'
    ELSE cj_tag_second END,
  cj_tag_second_code = CASE cj_tag_second
    WHEN '咨询对比' THEN 'CJ002001'
    WHEN '试乘试驾' THEN 'CJ002001'
    WHEN '下订成交' THEN 'CJ003001'
    WHEN '金融方案' THEN 'CJ002001'
    WHEN '车辆交付' THEN 'CJ003001'
    WHEN '驾驶体验' THEN 'CJ004001'
    WHEN '充电补能' THEN 'CJ004001'
    WHEN '维保服务' THEN 'CJ005001'
    ELSE cj_tag_second_code END
WHERE cj_tag_second IN ('咨询对比','试乘试驾','下订成交','金融方案','车辆交付','驾驶体验','充电补能','维保服务')
   OR user_journey2 IN ('咨询对比','试乘试驾','下订成交','金融方案','车辆交付','驾驶体验','充电补能','维保服务');

-- C) 三级标签映射（示例）
UPDATE voc_sentiment_annotations_results_v
SET
  cj_tag_three = CASE cj_tag_three
    WHEN '配置咨询' THEN '方案选择'
    WHEN '到店试驾' THEN '方案选择'
    WHEN '签约' THEN '签约提车'
    WHEN '交车仪式' THEN '签约提车'
    WHEN '续航体验' THEN '驾驶体验'
    WHEN '充电效率' THEN '驾驶体验'
    WHEN '工时费用' THEN '维保服务'
    ELSE cj_tag_three END,
  cj_tag_three_code = CASE cj_tag_three
    WHEN '配置咨询' THEN 'CJ002001001'
    WHEN '到店试驾' THEN 'CJ002001001'
    WHEN '签约' THEN 'CJ003001001'
    WHEN '交车仪式' THEN 'CJ003001001'
    WHEN '续航体验' THEN 'CJ004001001'
    WHEN '充电效率' THEN 'CJ004001001'
    WHEN '工时费用' THEN 'CJ005001001'
    ELSE cj_tag_three_code END
WHERE cj_tag_three IN ('配置咨询','到店试驾','签约','交车仪式','续航体验','充电效率','工时费用');

-- 可选：按新的三级编码衍生四级（若有四级结构需求）
-- UPDATE voc_sentiment_annotations_results_v_new
-- SET cj_tag_four_code = CONCAT(cj_tag_three_code, '001'),
--     cj_tag_four = CONCAT(cj_tag_three, '-细分')
-- WHERE cj_tag_three_code REGEXP 'CJ00.*';

SELECT
  id, data_create_time, data_id,
  brand_code, car_series_code, channel_catagory, channel_code, channel_name,
  brand_name, car_series_name, content_type, sentiment, intention, create_date,
  keywords, is_outer, hot_word, user_journey1, user_journey2, scenario,
  d2c_responsible_dept, d2c_accountable_dept, d2c_cc_dept,
  one_id, cust_id_card_type, cust_id_card_no, cust_mobile, cust_email,
  cust_global_id, cust_age, cust_gender,
  cust_province_code, cust_province, cust_city_code, cust_city,
  cust_nick, cust_type, is_car_owner_flg,
  total_mnt_cnt, veh_purch_price, veh_displ,
  reg_mnt_consum_lvl, spare_pt_mnt_consum_lvl, acc_mnt_consum_lvl,
  mnt_pkg_purch_cnt, cust_accum_consum_amt, mnt_rpr_accum_cons_amt,
  mnt_accum_consum_amt, repair_accum_cons_amt,
  dustry, lead_level, lost_cat, is_store_visit_lead,
  mem_lvl, cert_mem_cat, is_active_mem, accum_ca_purch_cnt,
  cust_source, consum_points_total, cust_value, main_chnl_pref,
  car_owner_type, vin,
  vhl_col_name, vhl_product_date, vhl_offline_date, vhl_is_abroad,
  vhl_dis_ch, vhl_dis_mt, vhl_eng_clsf, vhl_eng_seris, vhl_veh_type,
  vhl_country, vhl_bd_clsf, vhl_seg_mt, vhl_pow_clsf, vhl_fu_clsf,
  vhl_modl_st, vhl_std_plnt_code,
  dlr_oc_id, dlr_oc_code, dlr_oc_name, dlr_oc_province_code, dlr_oc_province,
  dlr_oc_city_code, dlr_oc_city,
  dlr_dc_id, dlr_dc_code, dlr_dc_name, dlr_dc_province_code, dlr_dc_province,
  dlr_dc_city_code, dlr_dc_city,
  dlr_mc_id, dlr_mc_code, dlr_mc_name, dlr_mc_province_code, dlr_mc_province,
  dlr_mc_city_code, dlr_mc_city,
  is_wsater_army, is_manager_focused, is_big_v,
  author_id, user_nick, is_main_post, original_link,
  view_count, comment_count, like_count, share_count, favorite_count,
  work_order_id, work_order_parent_id,
  quest_type, quest_answer_score, quest_business_type, quest_business_scenario,
  model_code, model_name, opinion, topic, topic_text,
  vtr_tag_first_code, vtr_tag_first, vtr_tag_second_code, vtr_tag_second,
  vtr_tag_three_code, vtr_tag_three, vtr_tag_four_code, vtr_tag_four,
  com_tag_first_code, com_tag_first, com_tag_second_code, com_tag_second,
  com_tag_three_code, com_tag_three, com_tag_four_code, com_tag_four,
  adb_tag_first_code, adb_tag_first, adb_tag_second_code, adb_tag_second,
  adb_tag_three_code, adb_tag_three, adb_tag_four_code, adb_tag_four,
  cj_tag_first_code, cj_tag_first, cj_tag_second_code, cj_tag_second,
  cj_tag_three_code, cj_tag_three, cj_tag_four_code, cj_tag_four,
  nps_tag_first_code, nps_tag_first, nps_tag_second_code, nps_tag_second,
  nps_tag_three_code, nps_tag_three, nps_tag_four_code, nps_tag_four,
  tag_accuracy, tag_customer_issue_classification, tag_issue_severity, tag_code_status,
  tag_business_domain, tag_event_clarity, tag_high_value_flag,
  tag_complaint_flag_needing_reply, tag_high_quality_voc_flag,
  tag_new_energy_or_fuel, tag_need_forvclosed_loop,
  data_create_year, data_create_quarter, data_create_month, data_create_week
FROM voc_sentiment_annotations_results_v;


ALTER TABLE voc_sentiment_annotations_results_v_new RENAME voc_sentiment_annotations_results_v;


-- ============================================================
-- 两张表的区别说明（文档）
-- 1) 表模型差异
--    - voc_sentiment_annotations_results_v：DUPLICATE KEY(id) 模型，允许相同 id 多行共存，不做去重。
--    - voc_sentiment_annotations_results_v_new：PRIMARY KEY(id) 模型，id 全局唯一，支持 UPDATE/DELETE/UPSERT。
-- 2) 更新能力
--    - 旧表（DUPLICATE KEY）：追加写入为新行，不支持原地 UPDATE；若需改值通常依赖删除+插入或查询侧去重。
--    - 新表（PRIMARY KEY）：可以直接对某行字段进行 UPDATE，也支持基于主键的幂等写入。
-- 3) 读写语义
--    - 旧表：Append-only，写入快，但查询需处理重复。
--    - 新表：写入维护主键索引，写入略慢，但查询简单、语义清晰。
-- 4) 适用场景
--    - 旧表：适合保留多版本快照或需要累加的明细。
--    - 新表：适合需要“根据任务字段/规则回填另一个字段”的场景（直接 UPDATE）。
-- 5) 其它
--    - 两表分区/分桶与字段保持一致，便于迁移：INSERT INTO new SELECT * FROM old。
-- ============================================================

-- 示例：在新表上根据一个业务字段更新另一个字段（示例规则）
-- 说明：以下为演示示例，按需要放开注释执行；请先根据正式规则调整条件。
-- UPDATE voc_sentiment_annotations_results_v_new
-- SET sentiment = CASE
--   WHEN intention = '表扬' THEN '正面'
--   WHEN intention = '投诉' THEN '负面'
--   ELSE sentiment
-- END
-- WHERE data_create_time >= '2024-01-01';

-- ============================================================
-- 品牌名称更新（与前端展示保持一致）
-- 将品牌名称统一更新为：长安引力、长安启源、长安凯程、深蓝汽车、阿维塔
-- 说明：此处仅更新 brand_name 字段的显示名称，不改动 brand_code
-- 可按需限制时间或数据范围，例如 WHERE data_create_time >= '2024-01-01'
-- ============================================================

-- 长安 -> 长安引力（如已存在“长安引力”则不重复）
UPDATE voc_sentiment_annotations_results_v
SET brand_name = '长安引力'
WHERE brand_name = '长安';

-- 欧尚 -> 长安启源
UPDATE voc_sentiment_annotations_results_v
SET brand_name = '长安启源'
WHERE brand_name = '欧尚';

-- 凯程 -> 长安凯程
UPDATE voc_sentiment_annotations_results_v
SET brand_name = '长安凯程'
WHERE brand_name = '凯程';

-- 深蓝 -> 深蓝汽车
UPDATE voc_sentiment_annotations_results_v
SET brand_name = '深蓝汽车'
WHERE brand_name = '深蓝';

-- 阿维塔 -> 阿维塔（保持不变，这里给出幂等更新示例）
UPDATE voc_sentiment_annotations_results_v
SET brand_name = '阿维塔'
WHERE brand_name IN ('阿维塔','AVATR');

