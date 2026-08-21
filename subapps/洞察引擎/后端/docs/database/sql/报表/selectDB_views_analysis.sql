-- ========================================
-- SelectDB 视图与物化视图 DDL 文档
-- 数据库: voc_ms_td
-- 生成时间: 2025
-- ========================================

-- ========================================
-- 一、物化视图列表 (Materialized Views)
-- ========================================

-- 1. ads_voc_model_tags_result_data_mv
--    说明: VOC模型标签结果数据物化视图，每小时自动刷新
--    数据源: ads_voc_model_tags_result_data_m_inc (基础表)
--    关联表: ins_brand_info, ins_car_series_info, ads_voc_channel_m_full_mv, ads_voc_tags_system_info_h_full_mv
--    刷新策略: 每1小时自动刷新
--    分区字段: publish_time
--    用途: 整合模型标签、品牌、车系、渠道、标签系统信息的宽表

-- 2. ads_voc_channel_m_full_mv
--    说明: 渠道月度全量物化视图
--    数据源: ins_channel
--    用途: 提供渠道维度数据

-- 3. ads_voc_tags_system_info_h_full_mv
--    说明: 标签系统信息小时全量物化视图
--    数据源: 标签系统表
--    用途: 提供标签体系的完整信息，包括CPT、UJY、CMA、DOM、NPS、VTR等标签

-- 4. ads_voc_tags_h_full_mv
--    说明: 标签小时全量物化视图
--    用途: 标签数据的小时级汇总

-- 5. ads_voc_tags_system_h_full_mv
--    说明: 系统标签小时全量物化视图
--    用途: 系统级标签的小时汇总


-- ========================================
-- 二、普通视图列表 (Views)
-- ========================================

-- 1. voc_sentiment_annotations_results_v
--    说明: VOC声音数据视图，直接查询物化视图
--    数据源: ads_voc_model_tags_result_data_mv
--    用途: 为应用层提供声音数据的统一查询接口

CREATE VIEW voc_sentiment_annotations_results_v AS
SELECT * FROM ads_voc_model_tags_result_data_mv;


-- 2. voc_anal_flow_mate_data_full_v
--    说明: VOC元数据全量增量视图
--    数据源: ads_voc_all_mate_data_m_full
--    用途: 提供元数据的查询接口

CREATE VIEW voc_anal_flow_mate_data_full_v AS
SELECT 
    id, create_time, content_type, data_create_time, data_update_time,
    data_id, channel_code, brand, series, model, is_outer, one_id,
    id_car_no, mobile, email, global_id, user_id, user_name,
    vhl_id, vhl_vin, dlr_id, dlr_code, dlr_type, market_id,
    title, content, is_wsater_army, weight, attrs, attrs2, attrs3,
    work_id, done, model_type, ds, data_status
FROM ads_voc_all_mate_data_m_full;


-- ========================================
-- 三、数据流向关系图
-- ========================================

/*
数据流向:

1. 核心数据流
   ads_voc_model_tags_result_data_m_inc (基础表)
   ├─> ads_voc_model_tags_result_data_mv (物化视图)
   │   ├─ LEFT JOIN ins_brand_info (品牌信息)
   │   ├─ LEFT JOIN ins_car_series_info (车系信息)
   │   ├─ LEFT JOIN ads_voc_channel_m_full_mv (渠道物化视图)
   │   └─ LEFT JOIN ads_voc_tags_system_info_h_full_mv (标签系统物化视图)
   └─> voc_sentiment_annotations_results_v (视图)

2. 渠道数据流
   ins_channel (渠道基础表)
   └─> ads_voc_channel_m_full_mv (物化视图)

3. 标签数据流
   标签系统表
   ├─> ads_voc_tags_h_full_mv (物化视图)
   ├─> ads_voc_tags_system_h_full_mv (物化视图)
   └─> ads_voc_tags_system_info_h_full_mv (物化视图)

4. 元数据流
   ads_voc_all_mate_data_m_full (基础表)
   └─> voc_anal_flow_mate_data_full_v (视图)
*/


-- ========================================
-- 四、关键字段说明
-- ========================================

/*
1. 标签体系字段:
   - CPT标签: cpt_tag_first/second/three/four (产品标签)
   - UJY标签: ujy_tag_first/second/three/four (用户旅程标签)
   - CMA标签: cma_tag_first/second/three/four (全领域业务标签)
   - DOM标签: dom_tag_first/second/three/four (商品化属性标签)
   - NPS标签: nps_tag_first/second/three/four (NPS标签)
   - VTR标签: vtr_tag_first/second/three/four (VTR标签)

2. 客户信息字段:
   - cust_*: 客户相关信息 (年龄、性别、学历、收入等)
   
3. 车辆信息字段:
   - vhl_*: 车辆相关信息 (VIN、颜色、生产日期等)
   
4. 经销商信息字段:
   - dlr_oc_*: 订单中心经销商信息
   - dlr_dc_*: 交付中心经销商信息
   - dlr_mc_*: 维保中心经销商信息

5. 业务标识字段:
   - sentiment: 情感倾向
   - intention: 用户意图
   - topic: 观点主题
   - opinion: 原始观点
*/


-- ========================================
-- 五、性能优化建议
-- ========================================

/*
1. 物化视图刷新策略:
   - ads_voc_model_tags_result_data_mv: 每小时刷新，适合准实时分析
   - 建议在业务低峰期刷新以减少系统负载

2. 分区策略:
   - 按 publish_time 分区，便于历史数据管理和查询优化
   - 建议定期归档历史分区数据

3. 索引优化:
   - bloom_filter_columns: brand_code, car_series_code, topic, channel_code
   - 这些字段是高频查询字段，已建立布隆过滤器索引

4. 查询优化:
   - 优先使用物化视图而非基础表
   - 使用视图 voc_sentiment_annotations_results_v 进行应用层查询
   - 避免直接查询 ads_voc_model_tags_result_data_m_inc
*/

