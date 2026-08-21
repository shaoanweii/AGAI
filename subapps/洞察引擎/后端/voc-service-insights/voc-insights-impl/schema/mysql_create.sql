-- voc.ins_brand_info definition

CREATE TABLE `ins_brand_info`
(
    `id`              varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '主键id',
    `code`            varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '编码',
    `name`            varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '名称',
    `name_en`         varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci          DEFAULT NULL COMMENT '英文名称',
    `alias`           text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '别名，多个别称以逗号隔开',
    `exclusion_words` text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '排除词；多个别称以逗号隔开，用于AI调用',
    `order_by`        int                                                              DEFAULT NULL COMMENT '排序',
    `operator`        varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci           DEFAULT NULL COMMENT '创建人',
    `create_time`     datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`        tinyint(1)                                                       DEFAULT NULL COMMENT '删除状态 0正常 1已删除',
    `img`             text COMMENT '展示图片',
    `app_id`          varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci           DEFAULT NULL COMMENT '系统标识',
    PRIMARY KEY (`id`, `code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3 COMMENT ='汽车品牌信息表';


-- voc.ins_car_series_info definition

CREATE TABLE `ins_car_series_info`
(
    `id`              varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '主键id',
    `code`            varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '编码',
    `brand_id`        varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '汽车品牌id',
    `name`            varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '名称',
    `name_en`         varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci          DEFAULT NULL COMMENT '英文名称',
    `alias`           text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '别名，多个别称以逗号隔开',
    `exclusion_words` text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '排除词；多个别称以逗号隔开，用于AI调用',
    `car_level1`      varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci           DEFAULT NULL COMMENT '车辆级别1',
    `car_level2`      varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci           DEFAULT NULL COMMENT '车辆级别2',
    `energy_type1`    varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci           DEFAULT NULL COMMENT '能用类型1',
    `energy_type2`    varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci           DEFAULT NULL COMMENT '能用类型2',
    `img`             text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '展示图片',
    `order_by`        int                                                              DEFAULT NULL COMMENT '排序',
    `operator`        varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci           DEFAULT NULL COMMENT '创建人',
    `create_time`     datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`        tinyint(1)                                                       DEFAULT NULL COMMENT '删除状态 0正常 1已删除',
    `app_id`          varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci           DEFAULT NULL COMMENT '系统标识',
    PRIMARY KEY (`id`, `code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3 COMMENT ='汽车车系信息表';


-- voc.ins_channel_distribution definition

CREATE TABLE `ins_channel_distribution`
(
    `id`        varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `parent_id` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '父级id',
    `name`      varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道名称',
    `level`     int                                    DEFAULT NULL COMMENT '渠道层级',
    `name_en`   varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道英文名称',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='渠道分布';


-- voc.ins_customer_info definition

CREATE TABLE `ins_customer_info`
(
    `id`           varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `code`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '编码',
    `full_name`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '全称',
    `abbreviation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '简称',
    `province`     varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省',
    `city`         varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '市',
    `contacts`     varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '联系人',
    `phone`        varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '联系电话',
    `email`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '联系邮箱',
    `address`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '联系地址',
    `remark`       text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
    `status`       int                                                          NOT NULL DEFAULT '1' COMMENT '停用/启用状态 停用:0 启用:1 默认为启用',
    `del_flag`     int                                                          NOT NULL DEFAULT '0' COMMENT '是否删除 是:1 否:0 默认为否',
    `create_time`  datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime                                                              DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '创建者',
    `update_user`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '更新者',
    `sort`         int                                                                   DEFAULT NULL COMMENT '排序字段 用于下拉列表中的排序',
    PRIMARY KEY (`id`),
    KEY `ins_customer_info_province_city_index` (`province`, `city`) COMMENT '省市联合索引',
    KEY `ins_customer_info_full_name_abbreviation_index` (`full_name`, `abbreviation`) COMMENT '全称与简称联合索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='客户信息';


-- voc.ins_data_database definition

CREATE TABLE `ins_data_database`
(
    `id`       int NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `name`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据源名称',
    `url`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '连接地址',
    `port`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '连接端口',
    `type`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据库类型',
    `ip`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '连接ip',
    `user`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
    `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- voc.ins_data_expect definition

CREATE TABLE `ins_data_expect`
(
    `id`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
    `name`        varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据集语料库名称',
    `format`      varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据格式',
    `count`       int                                                          DEFAULT '0' COMMENT '数据总数',
    `client_id`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '⽤户客户ID',
    `project_id`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci       DEFAULT NULL COMMENT '项目Id',
    `update_time` datetime                                                     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_time` datetime                                                     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`   varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci       DEFAULT NULL COMMENT '修改用户',
    `create_by`   varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci       DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='语料库数据集';


-- voc.ins_data_expect_desc definition

CREATE TABLE `ins_data_expect_desc`
(
    `id`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
    `expect_id`   varchar(60) COLLATE utf8mb4_unicode_ci                        DEFAULT NULL COMMENT '语料库数据集id',
    `content`     longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '内容',
    `business`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务标签',
    `quality`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '质量标签',
    `scene`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '场景标签',
    `emotion`     varchar(100) COLLATE utf8mb4_unicode_ci                       DEFAULT NULL COMMENT '情感',
    `intention`   varchar(100) COLLATE utf8mb4_unicode_ci                       DEFAULT NULL COMMENT '意图',
    `viewpoint`   varchar(100) COLLATE utf8mb4_unicode_ci                       DEFAULT NULL COMMENT '观点',
    `update_time` datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_time` datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`   varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '修改用户',
    `create_by`   varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='语料库数据详情';


-- voc.ins_data_resource definition

CREATE TABLE `ins_data_resource`
(
    `id`          varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
    `name`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源名称',
    `update_time` datetime                                               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_time` datetime                                               DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`   varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '修改用户',
    `create_by`   varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='资源库';


-- voc.ins_data_resource_desc definition

CREATE TABLE `ins_data_resource_desc`
(
    `id`          varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
    `resource_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源id',
    `name`        text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci        NOT NULL COMMENT '资源详情',
    `status`      varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'NotEnabled' COMMENT '状态：全部、已启用、未启用、已禁用',
    `update_time` datetime                                                     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_time` datetime                                                     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`   varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci       DEFAULT NULL COMMENT '修改用户',
    `create_by`   varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci       DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`),
    KEY `ins_data_resource_desc_resource_id_IDX` (`resource_id`, `status`, `update_by`, `id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='资源详情';


-- voc.ins_data_source definition

CREATE TABLE `ins_data_source`
(
    `id`          varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据集名称',
    `format`      varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '数据集格式',
    `type`        varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '接入数据类型，1.本地上传，2.数据库链接，3.api接入',
    `client_id`   varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '⽤户客户ID',
    `project_id`  varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '项目Id',
    `update_time` datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_time` datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`   varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '修改用户',
    `create_by`   varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='数据源集';


-- voc.ins_data_source_desc definition

CREATE TABLE `ins_data_source_desc`
(
    `id`            varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '主键id',
    `data_id`       varchar(50) COLLATE utf8mb4_unicode_ci                                DEFAULT NULL COMMENT '数据源集id',
    `content`       longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '内容',
    `channel_id`    varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '渠道ID',
    `province`      varchar(255) COLLATE utf8mb4_unicode_ci                               DEFAULT NULL COMMENT '区域',
    `user_name`     varchar(255) COLLATE utf8mb4_unicode_ci                               DEFAULT NULL COMMENT '用户名称',
    `customer_name` varchar(255) COLLATE utf8mb4_unicode_ci                               DEFAULT NULL COMMENT '客户名称',
    `model_name`    varchar(255) COLLATE utf8mb4_unicode_ci                               DEFAULT NULL COMMENT '模型名称',
    `publish_time`  datetime                                                              DEFAULT NULL COMMENT '发布时间',
    `update_time`   datetime                                                              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_time`   datetime                                                              DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`     varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci                DEFAULT NULL COMMENT '修改用户',
    `create_by`     varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci                DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='数据集详情';


-- voc.ins_data_source_desc_copy1 definition

CREATE TABLE `ins_data_source_desc_copy1`
(
    `id`            varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
    `data_id`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '数据源集id',
    `content`       longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '内容',
    `channel_id`    varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '渠道ID',
    `province`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域',
    `user_name`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名称',
    `customer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户名称',
    `model_name`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模型名称',
    `publish_time`  datetime                                                      DEFAULT NULL COMMENT '发布时间',
    `update_time`   datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_time`   datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`     varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '修改用户',
    `create_by`     varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='数据集详情';


-- voc.ins_dict definition

CREATE TABLE `ins_dict`
(
    `id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL,
    `dict_name`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典名称',
    `dict_code`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典编码',
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
    `del_flag`    tinyint                                                       NOT NULL COMMENT '删除状态 0:正常 1:已删除',
    `operator`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '创建人',
    `create_time` datetime                                                      NOT NULL COMMENT '创建时间',
    `update_time` datetime                                                      NOT NULL COMMENT '更新时间',
    `type`        tinyint                                                       NOT NULL COMMENT '字典类型  0:string 1:number',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='字典表';


-- voc.ins_dict_item definition

CREATE TABLE `ins_dict_item`
(
    `id`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `dict_id`      varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典id',
    `item_text`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '字典明细值',
    `item_value`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '字典明细key',
    `description`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '字典明细描述',
    `sort_order`   tinyint                                                       DEFAULT NULL COMMENT '排序',
    `status`       tinyint                                                      NOT NULL COMMENT '状态',
    `operator`     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建人',
    `create_time`  datetime                                                     NOT NULL COMMENT '创建时间',
    `item_text_en` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '字典明细英文',
    `item_key`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '字典明细key',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='字典明细表';


-- voc.ins_menu definition

CREATE TABLE `ins_menu`
(
    `id`          varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键id',
    `parent_id`   varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci          DEFAULT NULL COMMENT '父id',
    `name`        varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci         DEFAULT NULL COMMENT '菜单标题',
    `html_uri`    varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci         DEFAULT NULL COMMENT '路径',
    `api_uri`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci         DEFAULT NULL COMMENT '接口路径',
    `sort_no`     int                                                             DEFAULT NULL COMMENT '菜单排序',
    `icon`        varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci         DEFAULT NULL COMMENT '菜单图标',
    `is_route`    tinyint(1)                                                      DEFAULT NULL COMMENT '是否路由菜单: 0:不是  1:是（默认值1）',
    `is_leaf`     tinyint(1)                                                      DEFAULT NULL COMMENT '是否叶子节点:      1:是   0:不是',
    `hidden`      tinyint(1)                                                      DEFAULT NULL COMMENT '是否隐藏路由: 0否,1是',
    `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci         DEFAULT NULL COMMENT '描述',
    `operator`    varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci          DEFAULT NULL COMMENT '创建人',
    `create_time` datetime                                                        DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime                                                        DEFAULT NULL COMMENT '更新时间',
    `del_flag`    tinyint(1)                                                      DEFAULT NULL COMMENT '删除状态 0正常 1已删除',
    `app_id`      varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci          DEFAULT NULL COMMENT '系统标识',
    `enabled`     int                                                    NOT NULL DEFAULT '1' COMMENT '是否启用',
    KEY `ins_menu_del_flag_IDX` (`del_flag`, `enabled`, `hidden`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3 COMMENT ='菜单信息表';


-- voc.ins_menu_perms definition

CREATE TABLE `ins_menu_perms`
(
    `id`          varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键id',
    `user_id`     varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户标识',
    `menu_id`     varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '菜单标识',
    `user_perms`  varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci          DEFAULT NULL COMMENT '访问权限: r:读取 w:写入',
    `operator`    varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci          DEFAULT NULL COMMENT '创建人',
    `create_time` datetime                                                        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                        DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`    tinyint(1)                                                      DEFAULT '0' COMMENT '删除状态 0正常 1已删除',
    `app_id`      varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci          DEFAULT NULL COMMENT '系统标识',
    `enabled`     int                                                    NOT NULL DEFAULT '1' COMMENT '是否启用',
    KEY `ins_menu_perms_user_id_IDX` (`user_id`, `del_flag`, `enabled`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3 COMMENT ='菜单权限信息表';


-- voc.ins_model_desc definition

CREATE TABLE `ins_model_desc`
(
    `id`           varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
    `model_id`     varchar(60) COLLATE utf8mb4_unicode_ci                        DEFAULT NULL COMMENT 'ins_model_info的主键id',
    `model_label`  varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '应用标签',
    `model_path`   varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci       DEFAULT NULL COMMENT '模型路径',
    `model_desc`   varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci       DEFAULT NULL COMMENT '模型描述',
    `status`       varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '模型状态：1=训练中,,2=已停止,3=部署完成',
    `test_acc`     varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'F1值',
    `version`      varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '版本号',
    `version_desc` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci       DEFAULT NULL COMMENT '版本说明',
    `update_time`  datetime                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_time`  datetime                                                      DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`    varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '修改用户',
    `create_by`    varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci        DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='模型训练详情';


-- voc.ins_model_info definition

CREATE TABLE `ins_model_info`
(
    `id`           varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键id',
    `model_name`   varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '模型名称',
    `model_type`   varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '模型类型：1=标签模型,2=观点模型,3=情感模型,4=意图模型',
    `format`       varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '数据格式：1=文本,2=工单,3=对话,4=其他',
    `client_id`    varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '⽤户客户ID',
    `project_id`   varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '项目Id',
    `project_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '项目名称',
    `update_time`  datetime                                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_time`  datetime                                                DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`    varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '修改用户',
    `create_by`    varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  ROW_FORMAT = DYNAMIC COMMENT ='模型配置数据';


-- voc.ins_project_info definition

CREATE TABLE `ins_project_info`
(
    `id`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
    `name`        varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '项目名称',
    `client_id`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '⽤户客户ID',
    `label`       varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci               DEFAULT NULL COMMENT '应用标签',
    `channel`     varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci               DEFAULT NULL COMMENT '数据渠道',
    `enable`      int                                                          NOT NULL DEFAULT '0' COMMENT '状态(停用:0,启用:1)',
    `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '描述',
    `update_time` datetime                                                              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_time` datetime                                                              DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci                DEFAULT NULL COMMENT '修改用户',
    `create_user` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci                DEFAULT NULL COMMENT '创建用户',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='项目应用信息';


-- voc.ins_province_area definition

CREATE TABLE `ins_province_area`
(
    `id`            varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
    `area_code`     varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '区域编码',
    `area_name`     varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域名称',
    `province_code` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市编码',
    `province_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市名称',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='区域城市信息';


-- voc.ins_record_logs definition

CREATE TABLE `ins_record_logs`
(
    `id`            varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL,
    `log_type`      decimal(11, 0)                                          DEFAULT NULL COMMENT '日志类型（1登录日志，2操作日志）',
    `log_content`   text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '日志内容',
    `operate_type`  decimal(11, 0)                                          DEFAULT NULL COMMENT '操作类型',
    `userid`        varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '操作用户账号',
    `username`      varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '操作用户名称',
    `ip`            varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'IP',
    `method`        text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '请求java方法',
    `request_url`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '请求路径',
    `request_param` longtext CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '请求参数',
    `request_type`  varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '请求类型',
    `cost_time`     decimal(20, 0)                                          DEFAULT NULL COMMENT '耗时',
    `create_by`     varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '创建人',
    `create_time`   datetime                                                DEFAULT NULL COMMENT '创建时间',
    `update_by`     varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '更新人',
    `update_time`   datetime                                                DEFAULT NULL COMMENT '更新时间',
    `app_id`        varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '系统标识',
    `code`          varchar(10)                                             DEFAULT NULL COMMENT '返回状态码',
    `message`       text COMMENT '返回的消息内容',
    `tid`           varchar(55) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '追踪标识',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_userid_type` (`userid`, `log_type`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  ROW_FORMAT = DYNAMIC COMMENT ='系统日志表';


-- voc.ins_regulation_detail definition

CREATE TABLE `ins_regulation_detail`
(
    `id`               varchar(60) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `regulation_id`    varchar(60) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '规则id',
    `field_name`       varchar(50) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '字段名称',
    `variable_value`   varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '变量值',
    `logical_operator` varchar(20) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '逻辑运算符',
    `condition_type`   varchar(50) COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '条件类型',
    `condition_detail` varchar(100) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '条件类型详情',
    `detail_type`      varchar(2) COLLATE utf8mb4_unicode_ci   NOT NULL COMMENT '规则详情类型 规则条件:0 规则执行动作:1',
    `status`           varchar(20) COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT 'NotEnabled' COMMENT '停用/启用状态 已禁用:Disabled 已启用:Enabled 未启用:NotEnabled 默认未启用',
    `del_flag`         varchar(2) COLLATE utf8mb4_unicode_ci   NOT NULL DEFAULT '0' COMMENT '删除状态 未删除: 0 ,已删除:1',
    `create_time`      datetime                                NOT NULL COMMENT '创建时间',
    `create_user`      varchar(20) COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '创建者',
    `serial_number`    varchar(50) COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '序号',
    PRIMARY KEY (`id`),
    KEY `ins_regulation_detail_serial_number_IDX` (`serial_number`, `del_flag`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='规则详情';


-- voc.ins_regulation_info definition

CREATE TABLE `ins_regulation_info`
(
    `id`                  varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `name`                varchar(25) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
    `client_id`           varchar(50) COLLATE utf8mb4_unicode_ci                      DEFAULT NULL COMMENT '应用客户',
    `description`         text COLLATE utf8mb4_unicode_ci COMMENT '描述',
    `process_phase`       varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '1' COMMENT '处理阶段 前置处理:0 后置处理:1',
    `regulation_type`     varchar(30) COLLATE utf8mb4_unicode_ci                      DEFAULT NULL COMMENT '规则类型',
    `content_type`        varchar(60) COLLATE utf8mb4_unicode_ci                      DEFAULT NULL COMMENT '内容类型',
    `channel`             json                                                        DEFAULT NULL COMMENT '数据渠道',
    `matching_rule`       varchar(10) COLLATE utf8mb4_unicode_ci                      DEFAULT NULL COMMENT '匹配规则',
    `regulation_weight`   bigint                                                      DEFAULT NULL COMMENT '规则权重',
    `relevancy_table`     varchar(100) COLLATE utf8mb4_unicode_ci                     DEFAULT NULL COMMENT '关联表',
    `virtualization`      varchar(2) COLLATE utf8mb4_unicode_ci                       DEFAULT '0' COMMENT '是否为虚拟数据 虚拟数据:1 非虚拟数据:0 默认为非虚拟数据',
    `regulation_classify` varchar(30) COLLATE utf8mb4_unicode_ci                      DEFAULT 'custom' COMMENT '规则分类',
    `status`              varchar(20) COLLATE utf8mb4_unicode_ci                      DEFAULT 'NotEnabled' COMMENT '停用/启用状态 已禁用:Disabled 已启用:Enabled 未启用:NotEnabled 默认未启用',
    `del_flag`            varchar(2) COLLATE utf8mb4_unicode_ci                       DEFAULT '0' COMMENT '删除标识 未删除:0 已删除:1',
    `create_time`         datetime                                                    DEFAULT NULL COMMENT '创建时间',
    `update_time`         datetime                                                    DEFAULT NULL COMMENT '更新时间',
    `create_user`         varchar(20) COLLATE utf8mb4_unicode_ci                      DEFAULT NULL COMMENT '创建者',
    `update_user`         varchar(20) COLLATE utf8mb4_unicode_ci                      DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`),
    KEY `ins_regulation_info_virtualization_IDX` (`virtualization`, `regulation_classify`, `client_id`, `id`,
                                                  `create_time`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='数据规则';


-- voc.ins_rule_info definition

CREATE TABLE `ins_rule_info`
(
    `id`              varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `rule_code`       varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则编码',
    `rule_name`       varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
    `client_code`     varchar(50) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '关联客户编码',
    `project_code`    varchar(50) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '关联项目编码',
    `enable`          varchar(2) COLLATE utf8mb4_unicode_ci           DEFAULT '1' COMMENT '停用/启用状态 停用:0 启用:1 默认启用',
    `del_flag`        varchar(2) COLLATE utf8mb4_unicode_ci           DEFAULT '0' COMMENT '删除标识 未删除:0 已删除：1',
    `description`     varchar(255) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '规则描述',
    `relevancy_table` varchar(100) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '关联表',
    `virtualization`  varchar(2) COLLATE utf8mb4_unicode_ci           DEFAULT '0' COMMENT '是否为虚拟数据 虚拟数据:1 非虚拟数据:0 默认为非虚拟数据',
    `create_time`     datetime                                        DEFAULT NULL COMMENT '创建时间',
    `update_time`     datetime                                        DEFAULT NULL COMMENT '更新时间',
    `create_user`     varchar(50) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '创建者',
    `update_user`     varchar(50) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '修改者',
    `content_type`    varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容类型 例如:文本、工单',
    `process_phase`   varchar(2) COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT '1' COMMENT '处理阶段 前置处理:0,后置处理:1  默认后置处理',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='规则信息';


-- voc.ins_tag_client definition

CREATE TABLE `ins_tag_client`
(
    `id`                 varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `client_id`          varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '应用客户id',
    `parent_id`          varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '父级标签id',
    `name`               varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
    `name_en`            varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '标签英文名称',
    `code`               varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签编码',
    `type`               varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签类型(业务标签:BIZ,质量标签:QY)',
    `label_type`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '新增类型：1末级标签，2分类',
    `energy`             varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '关联能源',
    `stage`              varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '关联阶段',
    `association_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '关联状态',
    `apply_number`       int                                                                   DEFAULT '0' COMMENT '应用客户数',
    `tagged_corpus`      int                                                                   DEFAULT '0' COMMENT '标注语料',
    `enable`             int                                                          NOT NULL DEFAULT '0' COMMENT '状态(停用:0,启用:1)',
    `source`             varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '来源',
    `seriousness`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '严重性',
    `description`        text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '描述',
    `tag_id`             varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '关联标签id',
    `create_time`        datetime                                                              DEFAULT NULL COMMENT '创建时间',
    `update_time`        datetime                                                              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='标签-应用客户详情';


-- voc.ins_tag_info definition

CREATE TABLE `ins_tag_info`
(
    `id`            varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `parent_id`     varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '父级id',
    `name`          varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
    `name_en`       varchar(60) COLLATE utf8mb4_unicode_ci                       DEFAULT NULL COMMENT '标签英文名称',
    `code`          varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签编码',
    `type`          varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签类型(业务标签:BIZ,质量标签:QY)',
    `label_type`    varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '新增类型：1末级标签，2分类',
    `energy`        varchar(500) COLLATE utf8mb4_unicode_ci                      DEFAULT NULL COMMENT '关联能源',
    `stage`         varchar(500) COLLATE utf8mb4_unicode_ci                      DEFAULT NULL COMMENT '关联阶段',
    `apply_number`  int                                                          DEFAULT '0' COMMENT '应用客户数',
    `tagged_corpus` int                                                          DEFAULT '0' COMMENT '标注语料',
    `enable`        int                                    NOT NULL              DEFAULT '0' COMMENT '状态(停用:0,启用:1)',
    `source`        varchar(50) COLLATE utf8mb4_unicode_ci                       DEFAULT NULL COMMENT '来源',
    `seriousness`   varchar(50) COLLATE utf8mb4_unicode_ci                       DEFAULT NULL COMMENT '严重性',
    `description`   text COLLATE utf8mb4_unicode_ci COMMENT '描述',
    `create_time`   datetime                                                     DEFAULT NULL COMMENT '创建时间',
    `update_time`   datetime                                                     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='标签详情';


-- voc.ins_validate_rule definition

CREATE TABLE `ins_validate_rule`
(
    `id`                             varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `regulation_id`                  varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则id',
    `word_id`                        varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据处理链路标识',
    `single_or_full_type`            varchar(2) COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '数据处理类型 单规则类型：0 测试类型：1',
    `single_or_full_validate_status` varchar(2) COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '数据处理校验状态    当 数据处理类型singleOrFullType 为 0 时:未校验:-1 校验中:0  校验成功:1 校验失败:2  当 数据处理类型singleOrFullType 为 1 时:未测试:-1 测试中:0  测试成功:1 测试失败:2',
    `create_time`                    datetime                               DEFAULT NULL COMMENT '检验开始时间',
    `update_time`                    datetime                               DEFAULT NULL COMMENT '校验结束时间',
    `operator`                       varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人',
    PRIMARY KEY (`id`),
    KEY `ins_validate_rule_single_or_full_validate_status_IDX` (`single_or_full_validate_status`, `update_time`, `word_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- voc.ins_alt_task_config_data definition

CREATE TABLE `ins_alt_task_config_data`
(
    `id`             varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `name`           varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
    `channel_id`     varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道标识',
    `client_id`      varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户标识',
    `data_type`      varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点类型',
    `scheduled_time` datetime                               NOT NULL COMMENT '调度时间',
    `period`         varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务周期',
    `period_number`  varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '周期数：对比的历史周期数量，如任务周期为每日，周期数填写7，则对比历史7日的落库数量均值。（每天:day，每周:week，每月: month）',
    `alarm_id`       varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '告警标识',
    `alarm_level`    varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '告警等级',
    `alarm_rulel`    varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '告警比对规则',
    `alarm_ompare`   varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '升高: i、降低: d',
    `ompare_rise`    varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '升高值',
    `ompare_reduce`  varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '降低值',
    `create_time`    datetime                               DEFAULT NULL COMMENT '接收时间',
    `update_time`    datetime                               DEFAULT NULL COMMENT '更新时间',
    `enable_time`    datetime                               DEFAULT NULL COMMENT '启用日期',
    `disable_time`   datetime                               DEFAULT NULL COMMENT '停用日期',
    `timeliness`     varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理时效',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='数据监控-任务配置表';

CREATE TABLE `sys_credentials_change_record` (
                                                 `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                                 `user_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'sys_user_id',
                                                 `credential` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
                                                 `app_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '系统标识',
                                                 `identifier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '例如：手机号 ,unionid',
                                                 `identity_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '认证类型：phone、weixin、base',
                                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                                 `operator` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人',
                                                 `non_locked` int NOT NULL DEFAULT '1' COMMENT '是否锁定',
                                                 `enabled` int NOT NULL DEFAULT '1' COMMENT '是否启用',
                                                 `expire_date` datetime(3) DEFAULT '2099-12-30 00:00:00.000' COMMENT '过期时间',
                                                 `start_expire_date` datetime DEFAULT CURRENT_TIMESTAMP,
                                                 `non_expired` int NOT NULL DEFAULT '1' COMMENT '是否过期',
                                                 `admin` int NOT NULL DEFAULT '0' COMMENT '是否为管理员，是：1 ， 否：0',
                                                 `change_time` datetime DEFAULT NULL COMMENT '变更时间',
                                                 PRIMARY KEY (`id`),
                                                 KEY `sys_credentials_identity_type_IDX` (`identity_type`,`app_id`,`identifier`) USING BTREE,
                                                 KEY `sys_credentials_user_id_IDX` (`user_id`,`app_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号信息变更记录';

CREATE TABLE `sys_users_change_record` (
                                           `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                           `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
                                           `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
                                           `firstname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
                                           `lastname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
                                           `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
                                           `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人',
                                           `labelstud_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'labelstud系统token',
                                           `non_locked` int NOT NULL DEFAULT '1' COMMENT '是否锁定',
                                           `enabled` int NOT NULL DEFAULT '1' COMMENT '是否启用',
                                           `expire_date` datetime(3) NOT NULL DEFAULT '2099-12-30 00:00:00.000' COMMENT '过期时间',
                                           `start_expire_date` datetime DEFAULT CURRENT_TIMESTAMP,
                                           `client_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户标识',
                                           `employee_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '员工编号',
                                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                           `change_time` datetime DEFAULT NULL COMMENT '变更时间',
                                           PRIMARY KEY (`id`),
                                           KEY `sys_users_username_IDX` (`username`,`phone`,`email`) USING BTREE,
                                           KEY `sys_users_client_id_IDX` (`client_id`,`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户变更记录表';




INSERT INTO `ins_channel` (`id`, `parent_id`, `name`, `type`, `status`, `name_en`)
VALUES ('-1', '0', '默认分类', 'Category', NULL, NULL);

INSERT INTO `ins_customer_info` (`id`, `code`, `full_name`, `abbreviation`, `province`, `city`, `contacts`, `phone`,
                                 `email`, `address`, `remark`, `status`, `del_flag`, `create_time`, `update_time`,
                                 `create_user`, `update_user`, `sort`)
VALUES ('0', 'system', '系统', '系统', '110000', '110100000000', 'system', '132111111', 'system@gmail.com', 'system',
        '系统默认', 1, 0, '2024-02-21 13:01:53', '2024-03-21 15:51:40', 'system', 'admin', 2);
INSERT INTO `ins_customer_info` (`id`, `code`, `full_name`, `abbreviation`, `province`, `city`, `contacts`, `phone`,
                                 `email`, `address`, `remark`, `status`, `del_flag`, `create_time`, `update_time`,
                                 `create_user`, `update_user`, `sort`)
VALUES ('1', 'futong', '北京富通东方科技有限公司', '富通东方', '110000', '110100000000', 'system', '12345678911',
        'futong@gmail.com', 'system', '系统默认', 1, 0, '2024-02-23 10:47:23', '2024-02-23 10:47:23', 'system',
        'system', 1);

