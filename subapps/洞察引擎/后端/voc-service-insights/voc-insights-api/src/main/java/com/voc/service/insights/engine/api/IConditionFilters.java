package com.voc.service.insights.engine.api;

import com.voc.service.insights.engine.vo.ConditionVo;

public interface IConditionFilters {
    /**
     * 省市条件编码
     */
    String PROVINCE = "province";
    /**
     * 品牌条件编码
     */
    String BRAND = "brand";
    /**
     * 品牌车系
     */
    String BRAND_CAR = "brand_car";
    /**
     * 用车阶段条件编码
     */
    String VEHICLE_STAGE = "vehicleStag";
    /**
     * 能源条件编码
     */
    String ENERGY = "energy";
    /**
     * 车辆类型条件编码
     */
    String CAR_TYPE = "carType";
    /**
     * 停用/启用状态条件编码
     */
    String STATUS = "enable_type";
    /**
     * 资源状态条件编码  未启用，启用，停用
     */
    String REPOSITORY_STATUS = "repositoryStatus";
    /**
     * 渠道条件编码
     */
    String CHANNEL = "channel";
    /**
     * 模型类型条件编码
     */
    String MODEL_TYPE = "model_type";
    /**
     * 处理模型编码
     */
    String PROCESSING_MODEL = "processing_model";
    /**
     * 数据格式条件编码
     */
    String DATA_TYPE = "data_type";
    /**
     * 元数据类型
     */
    String META_DATA_TYPE = "meta_data_type";
    /**
     * 来源条件编码
     */
    String SOURCE = "source";
    /**
     * 严重性条件编码
     */
    String SERIOUSNESS = "seriousness";
    String ACCOUNT_TYPE = "accountType";
    String APP_CLIENT = "appClient";
    String REGULATION_POST_TYPE = "regulationPostType";
    String REGULATION_PRE_TYPE = "regulationPreType";
    String REGULATION_CONTENT_TYPE = "regulationContentType";
    String REGULATION_STATUS_TYPE = "regulationStatusType";
    String REGULATION_STAGE = "regulationStage";
    String REGULATION_RELATIONS = "regulationRelations";
    String REGULATION_CLASSIFY = "regulationClassify";
    String RULE_WEIGHT = "ruleWeight";
    String RULE_CONDITION_TYPE = "ruleConditionType";
    String RULE_LOGICAL_OPERATOR = "ruleLogicalOperator";
    String VOC_TEXT_TYPE = "voc_text_type";
    String VOC_ORDER_TYPE = "voc_order_type";
    String VARIABLE_VALUE = "variableValue";

    String POST_FIELDS = "post_fields";

    String HIT_STATE = "hit_state";
    String DATA_COMPARISON = "data_comparison";

    String ALARM_NODE = "alarm_node";
    String ALARM_LEVEL = "alarm_level";
    String RESOURCE_GROUP_TYPE = "resource_group_type";
    String LABEL_TYPE = "tag_libe_type";
    String LABEL_AND_MODEL = "LABEL_AND_MODEL";
    String USER_JOURNEY = "user_journey";

    String ALLOCATION_STATUS = "allocation_status";

    String CATEGORY_TYPE = "category_type";

    String INCREASE_TYPE = "increase_type";

    String ENABLE_STATUS = "enable_status";

    String AUDIT_STATUS = "audit_status";

    String ACCOUNT_STATUS = "account_status";
    String RULE_TEST = "rule_test";
    String RULE_TYPE = "rule_type";

    String TAG_LIB_ATTRIBUTE = "tag_lib_attribute";
    String DATA_SOURCE_ACCESS_WAY = "data_source_access_way";

    String ORIGINAL_DATA_STATUS = "original_data_status";
    String CONTENT_TYPE = "content_type";
    String DROPDOWN_FILTER = "dropdown_filter";
    String IS_HIGH_QUALITY = "high_quality_tag";
    String RESULT_DATA_STATUS = "result_data_status";
    String EMOTION = "voc_sentiment";
    String INTENTION = "voc_intention";
    String STOP_OR_ENABLE = "stop_or_enable";
    String EARLY_WARNING_TYPE = "early_warning_type";
    String RISK_LEVEL = "risk_level";
    String INSIGHT_CYCLE = "insight_cycle";
    String IS_APPLY = "isApply";
    String COLOR = "color";
    String COMPLETION_RATE = "completion_rate";

    // 闭环规则数据字典
    String CLOSED_RULE_CONFIRM_METHOD = "closed_rule_confirm_method";
    String CLOSED_RULE_CONDITION_OPERATOR = "closed_rule_condition_operator";
    String CLOSED_RULE_AUDIT_METHOD = "closed_rule_audit_method";
    String CLOSED_RULE_ALERT_CHANNEL = "closed_rule_alert_channel";
    String CLOSED_RULE_LEVEL = "closed_rule_level";
    String CLOSED_RULE_CONDITION_OPTION = "closed_rule_condition_option";
    String CLOSED_RULE_ENABLED_STATUS = "closed_rule_enabled_status";
    String CLOSED_RULE_PRIORITY = "closed_rule_priority";
    String CLOSED_RULE_TYPE = "closed_rule_type";
    String CLOSED_RULE_CONDITION_VALUE_TYPE = "closed_rule_condition_value_type";
    //问题程度
    String ISSUE_SEVERIT = "issue_severit";
    //事件清晰度
    String EVENT_CLARITY = "event_clarity";
    //敏感类型
    String SUSCEPTIVE_TYPE = "susceptive_type";
    //代码的精准性
    String ACCURACY = "accuracy";
    //业务领域
    String BUSINESS_DOMAIN = "business_domain";
    //是否需回复
    String COMPLAINT_FLAG_NEEDING_REPLY = "complaint_flag_needing_reply";
    //是否需闭环
    String NEED_FORVCLOSED_LOOP = "need_forvclosed_loop";

    String CLOSED_REGULATION_CONTENT_TYPE = "regulation_content_type";
    String RULE_STATUS = "rule_status";

    /**
     * 本品品牌条件编码
     */
    String SELF_BRAND = "self_brand";
    /**
     * 本品品牌车系
     */
    String SELF_BRAND_CAR = "self_brand_car";
    /**
     * 是否核心
     */
    String IS_CORE = "is_core";
    /**
     * 本竞品类型
     */
    String COMPETITIVE_TYPE = "competitive_type";

    String AUTOMARK = "automark";

    String IS_NEW_CAR = "is_new_car";
    
    // 批量规则新增条件
    String CUSTOMER_GENDER = "customer_gender";
    String WATER_MAN = "water_man";
    String V_MAN = "V_man";
    String CAR_OWNER = "car_owner";
    //-- 如下来自字典表
    // 情感程度
    String BATCH_EMOTIONAL_LEVEL = "batch_Emotional_level";
    // 广告类型
    String BATCH_AD_TYPE = "batch_ad_type";
    // 客户类型
    String CUSTOMER_TYPE= "batch_kh_type";

    // 指标生效关系
    String INDICATOR_EFFECT_RELATION = "indicator_effect_relation";
    ConditionVo get(String key);
    // 批量规则条件类型
    String BATCH_RULE_TYPE = "batch_rule_type";
}
