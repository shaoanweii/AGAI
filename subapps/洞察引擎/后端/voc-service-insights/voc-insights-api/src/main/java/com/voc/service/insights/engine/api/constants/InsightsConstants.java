package com.voc.service.insights.engine.api.constants;

public interface InsightsConstants {
   String PERMS_BIZ_USER_INFO_KEY = "PERMS_BIZ_USER_INFO_KEY";
   String PERMS_SYS_USER_INFO_KEY = "PERMS_SYS_USER_INFO_KEY";
   String PERMS_ACC_USER_INFO_KEY = "PERMS_ACC_USER_INFO_KEY";
   String PERMS_MENUS_KEY = "PERMS_MENUS_KEY";
   String PERMS_MENUS_TREE_KEY = "PERMS_MENUS_TREE_KEY";
   String PERMS_BUTTON_KEY = "PERMS_BUTTON_KEY";
   String PERMS_DRILL_DOWN_KEY = "PERMS_DRILL_DOWN_KEY";
   String PERMS_CUSTOMER_ID = "PERMS_CUSTOMER_ID";
   //角色ID
   String PERMS_BIZ_ROLE_ID_KEY = "PERMS_BIZ_ROLE_ID_KEY";
   //渠道
   String PERMS_BIZ_CHANEL_DATA_KEY = "PERMS_BIZ_CHANEL_DATA_KEY";
   //数据源
   String PERMS_DATA_SOURCE_KEY = "PERMS_BIZ_DATA_SOURCE_KEY";
   String PERMS_CAR_SERIES_KEY = "PERMS_CAR_SERIES_KEY";
   String PERMS_CAR_SERIES_KEY_NAME = "PERMS_CAR_SERIES_KEY_NAME";
   //资源组
   String PERMS_DATA_RESOURCE_KEY = "PERMS_DATA_RESOURCE_KEY";
   //产品标签
   String PERMS_TAG_ALL_LIB_KEY = "PERMS_TAG_ALL_LIB_KEY";
   String PERMS_BIZ_TAG_LEVE_FIRST_LIB_MAP = "PERMS_BIZ_TAG_LEVE_FIRST_LIB_MAP";
   String PERMS_BIZ_TAG_LEVE_FIRST_LIB_KEY = "PERMS_BIZ_TAG_LEVE_FIRST_LIB_KEY";
   String PERMS_BIZ_TAG_LEVE_SECOND_LIB_KEY = "PERMS_BIZ_TAG_LEVE_SECOND_LIB_KEY";
   //品质标签
   String PERMS_QY_TAG_ALL_LIB_KEY = "PERMS_QY_TAG_ALL_LIB_KEY";

   String PERMS_QY_TAG_LEVE_FIRST_LIB_MAP = "PERMS_QY_TAG_LEVE_FIRST_LIB_MAP";
   String PERMS_QY_TAG_LEVE_FIRST_LIB_KEY = "PERMS_QY_TAG_LEVE_FIRST_LIB_KEY";
   String PERMS_QY_TAG_LEVE_SECOND_LIB_KEY = "PERMS_QY_TAG_LEVE_SECOND_LIB_KEY";
   //服务
   String PERMS_SERVICE_TAG_ALL_LIB_KEY = "PERMS_SERVICE_TAG_ALL_LIB_KEY";
   String PERMS_SERVICE_TAG_LEVE_FIRST_LIB_MAP = "PERMS_SERVICE_TAG_LEVE_FIRST_LIB_MAP";
   String PERMS_SERVICE_TAG_LEVE_FIRST_LIB_KEY = "PERMS_SERVICE_TAG_LEVE_FIRST_LIB_KEY";
   String PERMS_SERVICE_TAG_LEVE_SECOND_LIB_KEY = "PERMS_SERVICE_TAG_LEVE_SECOND_LIB_KEY";
   //获取全部禁用的标签 getFourTagLib  AllDisableTagLibClient
   String PERMS_ALL_DISABLE_FOUR_TAG_LIB_LIB_MAP = "PERMS_ALL_DISABLE_FOUR_TAG_LIB_LIB_MAP";
   //标签
   String PERMS_APP_TAG_LIB_KEY = "PERMS_APP_TAG_LIB_KEY";
   //区域
   String PERMS_REGION_KEY = "PERMS_REGION_KEY";
   //品牌
   String PERMS_BRAND_KEY = "PERMS_BRAND_KEY";
   String PERMS_BRAND_LIST = "PERMS_BRAND_LIST";

   String PERMS_PROD_TAG_LIB_KEY = "PERMS_PROD_TAG_LIB_KEY";
   String PERMS_SERV_TAG_LIB_KEY = "PERMS_SERV_TAG_LIB_KEY";

   String PERMS_APP_DEFAULT_RANGE_LIST = "PERMS_APP_DEFAULT_RANGE_LIST";

   String PERMS_CHANNEL_KEY = "PERMS_CHANNEL_KEY";
   //项目
   String PERMS_PROJECT_KEY = "PERMS_PROJECT_KEY";
   //部门
   String PERMS_DEPART_KEY = "PERMS_DEPART_KEY";
   //单点事件权限
   String PERMS_SINGLE_EVENT_PERMISSION_KEY = "PERMS_SINGLE_EVENT_PERMISSION_KEY";

   String SINGLE_EVENT_PERMISSION = "single_event_permission";
   String SINGLE_EVENT_SCOPE = "single_event_scope";
   String SINGLE_EVENT_OPERATION = "single_event_operation";

   /**
    * 字典类型：停用/启用
    */
   String ENABLE_CODE = "enable_type";
   /**
    * 字典类型：用车阶段
    */
   String VEHICLE_STAGE = "vehicle_stage";
   /**
    * 字典类型：能源类型
    */
   String ENERGY_TYPE = "energy_type";
   /**
    * 标签类型: 业务标签类型
    */
   String BUSINESS_TAG_TYPE = "BIZ";
   /**
    * 标签类型: 质量标签
    */
   String QUALITY_TAG_TYPE = "QY";
   /**
    * 字典类型：数据格式
    */
   String DATA_TYPE = "data_type";
   /**
    * 字典类型：模型类型
    */
   String MODEL_TYPE = "model_type";
   /**
    * 字典类型：模型状态
    */
   String MODEL_STATUS = "model_status";
   /**
    * 字典类型：来源
    */
   String SOURCE = "source";
   /**
    * 字典类型：严重性
    */
   String SERIOUSNESS = "seriousness";
   /**
    * 字典类型:车辆类型
    */
   String CAR_TYPE = "car_type";
   /**
    * 字典类型:规则类型
    */
   String RULE_TYPE= "rule_type";
   String CLIENT = "client";

   String SYSTEM_ACCOUNT = "system";
   /**
    * 运营账号
    */
   String OPERATION_ACCOUNT = "operation";
   /**
    * 字典类型:账号类型
    */
   String ACCOUNT_TYPE = "account_type";
   /**
    * 应用标签
    */
   String TAG_APP = "tag_app";

   /**
    * 标签分类
    */
   String TAG_TYPE = "tag_type";

   /**
    * 业务标签新增类型
    */
   String BUSINESS_ADD_TYPE = "business_add_type";

   /**
    * 质量标签新增类型
    */
   String QUALITY_ADD_TYPE = "quality_add_type";

   /**
    * 标签新增类型
    */
   String TAG_ADD_TYPE ="tag_add_type";
   String REPOSITORY_STATYS ="repositoryStatus";
   String FULLY_VALIDATE ="fully_validate";
   String SINGLE_VALIDATE ="single_validate";
   String POST_FIELDS ="post_fields";
   String LABEL_TYPE = "label_type";
   String USER_JOURNEY = "user_journey";
   String ALLOCATION_STATUS = "allocation_status";
   String RESOURCE_GROUP_TYPE = "resource_group_type";
   String PROCESSING_STATUS = "processing_status";


   String PERMS_PROJECT_ID_KEY = "PERMS_PROJECT_ID_KEY";
   String PERMS_THRESHOLD_KEY = "PERMS_THRESHOLD_KEY";
}
