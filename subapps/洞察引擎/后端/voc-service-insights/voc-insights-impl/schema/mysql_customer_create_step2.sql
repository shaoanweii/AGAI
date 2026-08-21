CREATE TABLE `cli_allocation_opinions_record` (
                                                  `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                                  `tag_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签表ID',
                                                  `opinions_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签表ID',
                                                  `tag_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签类型',
                                                  `tag_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属分类',
                                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='高频观点信息表';

CREATE TABLE `cli_allocation_words_record` (
                                               `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                               `tag_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签表ID',
                                               `words_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签表ID',
                                               `tag_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签类型',
                                               `tag_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属分类',
                                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='高频观点信息表';

CREATE TABLE `cli_high_frequency_opinions` (
                                               `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                               `client_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户标识',
                                               `tag_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签表ID',
                                               `tag_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签类型',
                                               `tag_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属分类',
                                               `normalized_opinions` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '归一观点',
                                               `corresponding_opinions` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '对应观点',
                                               `current_frequency` bigint NOT NULL DEFAULT '0' COMMENT '当前频次',
                                               `system_suggested_business` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '系统建议_业务标签',
                                               `system_suggested_quality` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '系统建议_质量标签',
                                               `channel_source` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源渠道',
                                               `allocation_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否分配: 0否,1是',
                                               `create_time` date NOT NULL COMMENT '创建时间',
                                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                               `operate_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人',
                                               `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
                                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='高频观点信息表';

CREATE TABLE `cli_high_frequency_words` (
                                            `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                            `client_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户标识',
                                            `tag_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签表ID',
                                            `tag_type` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签类型',
                                            `tag_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属分类',
                                            `word_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '新词名称',
                                            `current_frequency` bigint NOT NULL DEFAULT '0' COMMENT '当前频次',
                                            `system_suggested_business` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '系统建议_业务标签',
                                            `system_suggested_quality` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '系统建议_质量标签',
                                            `channel_source` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源渠道',
                                            `allocation_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否分配: 0否,1是',
                                            `create_time` date NOT NULL COMMENT '创建时间',
                                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                            `operate_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人',
                                            `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
                                            PRIMARY KEY (`id`),
                                            KEY `cli_tag_id_index` (`tag_id`) COMMENT '标签表ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='高频词汇信息表';

CREATE TABLE `cli_risk_keywords` (
                                     `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                     `client_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户标识',
                                     `risk_keywords` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '风险关键词',
                                     `extended_word` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '扩展词',
                                     `serious_level` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '严重等级',
                                     `tag_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属分类',
                                     `current_frequency` bigint NOT NULL DEFAULT '0' COMMENT '当前频次',
                                     `increase_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '增加类型: 1模型识别,2手动添加',
                                     `enable_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '启用状态: 0 待审核,1已启用,2已禁用',
                                     `create_time` date NOT NULL COMMENT '创建时间',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `operate_user` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人',
                                     `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险关键词信息表';


CREATE TABLE `ins_channel` (
                               `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                               `parent_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '父级id',
                               `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道名称',
                               `type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道类型 分类:Category 渠道:Channel',
                               `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道状态',
                               `name_en` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道英文名称',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道分布';

CREATE TABLE `ins_data_resource` (
                                     `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
                                     `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源名称',
                                     `update_time` datetime DEFAULT NULL COMMENT '创建时间',
                                     `create_time` datetime DEFAULT NULL COMMENT '更新时间',
                                     `update_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '修改用户',
                                     `create_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '创建用户',
                                     `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'custom' COMMENT '资源组类型',
                                     `customer` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属客户',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源库';

CREATE TABLE `ins_data_resource_desc` (
                                          `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键id',
                                          `resource_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源id',
                                          `name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源详情',
                                          `customer` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属客户',
                                          `status` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'NotEnabled' COMMENT '状态：全部、已启用、未启用、已禁用',
                                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                          `update_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '修改用户',
                                          `create_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '创建用户',
                                          PRIMARY KEY (`id`),
                                          KEY `ins_data_resource_desc_resource_id_IDX` (`resource_id`,`status`,`update_by`,`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源详情';

create table if not exists ins_data_source
(
    id                     varchar(60)  not null comment '主键'
    primary key,
    data_source_name       varchar(100) null comment '数据源名称',
    data_source_type       varchar(5)   null comment '数据源类型 文本:text 工单：order',
    data_source_access_way varchar(20)  null comment '数据源接入方式 本地导入：upload API接入:api 数仓推送:push',
    create_user            varchar(100) null comment '创建人',
    create_time            datetime     null comment '创建时间'
    )
    comment '数据源';

create table if not exists ins_data_source_desc
(
    new_id            varchar(60)             not null comment '主键'
    primary key,
    id                varchar(100)            null,
    channel_id        varchar(60)             null comment '渠道',
    title             text                    null comment '标题',
    content           text                    null comment '内容',
    publish_time      varchar(255)            null comment '发布时间',
    user_id           varchar(255)            null comment '用户id',
    user_name         varchar(255)            null comment '昵称',
    URL               varchar(255)            null comment '链接',
    reading_count     varchar(255)            null comment '阅读数',
    focus_count       varchar(255)            null comment '关注数',
    comments_count    varchar(255)            null comment '评论数',
    favor_count       varchar(255)            null comment '收藏数',
    redirection_count varchar(255)            null comment '转发数',
    total_num         bigint                  null comment '总数',
    success_num       bigint                  null comment '成功数',
    fail_num          bigint                  null comment '失败数',
    data_name         varchar(255)            null comment '数据名称',
    batch_Id          varchar(60)             null comment '批次id',
    data_source_id    varchar(60)             null comment '数据源id',
    status            varchar(10) default '0' null comment '状态 未处理：0 处理中：1 已处理：2 处理失败：-1',
    create_time       datetime                null comment '文件上传并解析入库时间',
    work_id           varchar(60)             null comment '数据链路id',
    data_validity     varchar(5)  default '1' null comment '数据有效性(0:无效 1：有效)'
    )
    comment '数据源详情';

create table if not exists ins_date_source_template
(
    new_id            varchar(60)            not null comment '主键'
    primary key,
    id                varchar(60)            null,
    channel_id        varchar(60)            null comment '渠道',
    title             text                   null comment '标题',
    content           text                   null comment '内容',
    publish_time      varchar(60)            null comment '发布时间',
    user_Id           varchar(60)            null comment '用户id',
    user_name         varchar(60)            null comment '昵称',
    URL               varchar(500)           null comment '链接',
    reading_count     varchar(50)            null comment '阅读数',
    focus_count       varchar(50)            null comment '关注数',
    comments_count    varchar(50)            null comment '评论数',
    favor_count       varchar(50)            null comment '点赞数',
    collections_count varchar(50)            null comment '收藏数',
    redirection_count varchar(50)            null comment '转发数',
    batch_id          varchar(60)            null comment '批次id',
    total_num         bigint                 null comment '总数',
    success_num       bigint                 null comment '成功数',
    fail_num          bigint                 null comment '失败数',
    create_time       datetime               null comment '数据上传并解析后的时间',
    create_user       varchar(50)            null comment '创建者',
    data_validity     varchar(5) default '1' null comment '数据有效性(0:无效 1：有效)'
    )
    comment '数据源临时表';




CREATE TABLE `ins_regulation_detail` (
                                         `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                         `regulation_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则id',
                                         `field_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字段名称',
                                         `variable_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '变量值',
                                         `logical_operator` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '逻辑运算符',
                                         `condition_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '条件类型',
                                         `condition_detail` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '条件类型详情',
                                         `detail_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则详情类型 规则条件:0 规则执行动作:1',
                                         `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NotEnabled' COMMENT '停用/启用状态 已禁用:Disabled 已启用:Enabled 未启用:NotEnabled 默认未启用',
                                         `del_flag` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '删除状态 未删除: 0 ,已删除:1',
                                         `create_time` datetime NOT NULL COMMENT '创建时间',
                                         `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建者',
                                         `serial_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '序号',
                                         PRIMARY KEY (`id`),
                                         KEY `ins_regulation_detail_serial_number_IDX` (`serial_number`,`del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则详情';

CREATE TABLE `ins_regulation_info` (
                                       `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                       `name` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
                                       `client_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '应用客户',
                                       `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '描述',
                                       `process_phase` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '1' COMMENT '处理阶段 前置处理:0 后置处理:1',
                                       `regulation_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '规则类型',
                                       `content_type` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容类型',
                                       `channel` json DEFAULT NULL COMMENT '数据渠道',
                                       `matching_rule` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '匹配规则',
                                       `regulation_weight` bigint DEFAULT NULL COMMENT '规则权重',
                                       `relevancy_table` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联表',
                                       `virtualization` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '是否为虚拟数据 虚拟数据:1 非虚拟数据:0 默认为非虚拟数据',
                                       `regulation_classify` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'custom' COMMENT '规则分类',
                                       `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'NotEnabled' COMMENT '停用/启用状态 已禁用:Disabled 已启用:Enabled 未启用:NotEnabled 默认未启用',
                                       `del_flag` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标识 未删除:0 已删除:1',
                                       `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                       `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                       `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建者',
                                       `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新者',
                                       PRIMARY KEY (`id`),
                                       KEY `ins_regulation_info_virtualization_IDX` (`virtualization`,`regulation_classify`,`client_id`,`id`,`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据规则';


CREATE TABLE `ins_tag_client` (
                                  `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                  `tag_parent_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '父级id',
                                  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
                                  `tag_name_en` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签英文名称',
                                  `tag_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签编码',
                                  `tag_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签类型',
                                  `tag_attribute` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签属性',
                                  `energy_type` json DEFAULT NULL COMMENT '关联能源',
                                  `car_type` json DEFAULT NULL COMMENT '车辆类型',
                                  `tag_status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '状态(禁用:0,启用:1)',
                                  `tag_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '描述',
                                  `seriousness` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '严重性',
                                  `user_journey` json DEFAULT NULL COMMENT '用户旅途',
                                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                  `create_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
                                  `update_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
                                  `app_client` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '应用客户',
                                  `sort` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签应用';


CREATE TABLE `ins_validate_rule` (
                                     `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                     `regulation_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则id',
                                     `work_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据处理链路标识',
                                     `single_or_full_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据处理类型 单规则类型：0 测试类型：1',
                                     `single_validate_status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据处理校验状态 未校验:-1 校验中:0  校验成功:1 校验失败:2 ',
                                     `create_time` datetime DEFAULT NULL COMMENT '检验开始时间',
                                     `update_time` datetime DEFAULT NULL COMMENT '校验结束时间',
                                     `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人',
                                     `full_validate_status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '数据处理校验状态 未测试:-1 测试中:0  测试成功:1 测试失败:2',
                                     `content_type` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                     `channel` json DEFAULT NULL,
                                     PRIMARY KEY (`id`),
                                     KEY `ins_validate_rule_single_or_full_validate_status_IDX` (`single_validate_status`,`update_time`,`work_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `ins_region` (
                              `id` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                              `parent_id` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '父级id',
                              `name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类名称',
                              `name_en` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类英文名称',
                              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                              `create_user` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
                              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                              `update_user` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '修改人',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域分类表';

CREATE TABLE `ins_region_detail` (
                                     `id` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                     `parent_id` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '父级id',
                                     `name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域名称',
                                     `name_en` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域英文名称',
                                     `status` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域状态',
                                     `region` json DEFAULT NULL COMMENT '区域(省份+城市)',
                                     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                     `create_user` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
                                     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                                     `update_user` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域详情表';

CREATE TABLE `ins_project_details` (
                                       `id` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                       `project_id` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目id',
                                       `brand_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌code',
                                       `brand_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌名称',
                                       `tags` json DEFAULT NULL COMMENT '应用标签',
                                       `data_source` json DEFAULT NULL COMMENT '数据源',
                                       `channel` json DEFAULT NULL COMMENT '渠道',
                                       `region` json DEFAULT NULL COMMENT '区域',
                                       `car_series` json DEFAULT NULL COMMENT '品牌',
                                       `competitive_product` json DEFAULT NULL COMMENT '竞品',
                                       `risk_early_warning` json DEFAULT NULL COMMENT '风险预警配置',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目详情表';

CREATE TABLE `ins_project_info` (
                                    `id` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
                                    `project_Name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目名称',
                                    `project_desc` text COLLATE utf8mb4_unicode_ci COMMENT '项目描述',
                                    `status` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '状态(禁用：0 启用：1)',
                                    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                    `create_user` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建者',
                                    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                    `update_user` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新者',
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目管理表';







INSERT INTO `ins_channel` (`id`, `parent_id`, `name`, `type`, `status`, `name_en`)
VALUES ('-1', '0', '默认分类', 'Category', NULL, NULL);

alter table ins_data_source
    add label_type json null comment '标签类型';

alter table ins_data_source
    add model_type varchar(10) null comment '模型类型(1：GLM离线大模型，2：GLM实时大模型，3：NLP复合模型)';


INSERT INTO `ins_region` (`id`, `parent_id`, `name`, `name_en`, `create_time`, `create_user`, `update_time`, `update_user`)
VALUES ('-1', '0', '默认分类', NULL, '2024-09-12 10:08:56', 'admin', NULL, NULL);


alter table ins_data_source_desc
    add province varchar(200) null comment '省份';

alter table ins_data_source_desc
    add city varchar(200) null comment '城市';