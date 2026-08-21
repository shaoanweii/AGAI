-- voc.alt_core_data definition

CREATE TABLE `alt_core_data` (
                                 `id` varchar(40) NOT NULL COMMENT "主键id",
                                 `data_type` varchar(40) NULL COMMENT "节点类型",
                                 `alt_id` varchar(40) NULL COMMENT "任务配置id",
                                 `channel_id` varchar(40) NULL COMMENT "渠道id",
                                 `client_id` varchar(40) NULL COMMENT "客户id",
                                 `level` varchar(255) NULL COMMENT "告警等级",
                                 `push_status` int(11) NULL COMMENT "推送状态 已推送完成：1 ，未推送：0，未处理：-1",
                                 `push_list` int(11) NULL COMMENT "推送信息",
                                 `push_msg` varchar(10000) NULL COMMENT "推送信息",
                                 `create_time` datetime NULL COMMENT "创建时间",
                                 `update_time` datetime NULL COMMENT "修改时间",
                                 `update_by` varchar(40) NULL COMMENT "修改人"
) ENGINE=OLAP
    PRIMARY KEY(`id`)
COMMENT "数据监控-监控数据表"
DISTRIBUTED BY HASH(`id`)
ORDER BY(`create_time`, `channel_id`, `client_id`, `data_type`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);


-- voc.alt_monitoring_data definition

CREATE TABLE `alt_monitoring_data` (
                                       `id` varchar(40) NOT NULL COMMENT "主键",
                                       `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
                                       `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
                                       `client_id` varchar(40) NOT NULL COMMENT "客户标识",
                                       `create_time` datetime NULL COMMENT "接收时间",
                                       `meta_date_create_time` datetime NULL COMMENT "原数据创建时间",
                                       `data_size` bigint(20) NULL COMMENT "数据集大小",
                                       `data_type` varchar(10) NOT NULL COMMENT "数据来源- metaData,nlpData,pushData",
                                       `tid` varchar(55) NULL COMMENT "链路标识"
) ENGINE=OLAP
    PRIMARY KEY(`id`)
COMMENT "数据监控-监控数据表"
DISTRIBUTED BY HASH(`id`)
ORDER BY(`create_time`, `channel_id`, `client_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);


-- voc.alt_monitoring_data_history definition

CREATE TABLE `alt_monitoring_data_history` (
                                               `id` varchar(40) NOT NULL COMMENT "主键",
                                               `work_id` varchar(40) NOT NULL COMMENT "接收处理标识",
                                               `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
                                               `client_id` varchar(40) NOT NULL COMMENT "客户标识",
                                               `create_time` datetime NULL COMMENT "接收时间",
                                               `meta_date_create_time` datetime NULL COMMENT "原数据创建时间",
                                               `data_size` bigint(20) NULL COMMENT "数据集大小",
                                               `data_type` varchar(10) NOT NULL COMMENT "数据来源- metaData,nlpData,pushData",
                                               `tid` varchar(55) NULL COMMENT "链路标识"
) ENGINE=OLAP
    PRIMARY KEY(`id`)
COMMENT "数据监控-监控数据表"
DISTRIBUTED BY HASH(`id`)
ORDER BY(`create_time`, `channel_id`, `client_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);


-- voc.alt_push_data_record definition

CREATE TABLE `alt_push_data_record` (
                                        `id` varchar(40) NOT NULL COMMENT "主键id",
                                        `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
                                        `client_id` varchar(40) NOT NULL COMMENT "客户标识",
                                        `create_time` datetime NULL COMMENT "接收时间",
                                        `data_size` bigint(20) NULL COMMENT "数据集大小",
                                        `data_type` varchar(10) NOT NULL DEFAULT "pushData" COMMENT "数据来源- metaData,nlpData,pushData"
) ENGINE=OLAP
    PRIMARY KEY(`id`)
COMMENT "数据监控-推送数据记录表"
DISTRIBUTED BY HASH(`id`)
ORDER BY(`create_time`, `channel_id`, `client_id`, `data_type`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);



-- voc.alt_task_execution_record definition

CREATE TABLE `alt_task_execution_record` (
                                             `id` varchar(40) NOT NULL COMMENT "主键",
                                             `task_id` varchar(40) NOT NULL COMMENT "任务名称",
                                             `channel_id` varchar(40) NOT NULL COMMENT "渠道标识",
                                             `client_id` varchar(40) NOT NULL COMMENT "客户标识",
                                             `data_type` varchar(20) NULL COMMENT "数据源类型",
                                             `status` varchar(1) NULL COMMENT "已完成：1， 未完成：0",
                                             `start_time` datetime NULL COMMENT "启用日期",
                                             `stop_time` datetime NULL COMMENT "停用日期",
                                             `tid` varchar(45) NOT NULL COMMENT "链路标识"
) ENGINE=OLAP
    PRIMARY KEY(`id`)
COMMENT "数据监控-任务执行状态记录表"
DISTRIBUTED BY HASH(`id`)
ORDER BY(`start_time`, `channel_id`, `client_id`)
PROPERTIES (
"replication_num" = "1",
"in_memory" = "false",
"enable_persistent_index" = "false",
"replicated_storage" = "true",
"compression" = "LZ4"
);



CREATE EXTERNAL TABLE alt_task_config_data
(
  `id` varchar(40) ,
  `name` varchar(40) ,
  `channel_id` varchar(40) ,
  `client_id` varchar(40) ,
  `data_type` varchar(40) ,
  `scheduled_time` datetime ,
  `period` varchar(40) ,
  `period_number` varchar(40) ,
  `alarm_id` varchar(40) ,
  `alarm_level` varchar(40) ,
  `alarm_rulel` varchar(40) ,
  `alarm_ompare` varchar(40) ,
  `ompare_rise` varchar(40) ,
  `ompare_reduce` varchar(40) ,
  `create_time` datetime ,
  `update_time` datetime ,
  `enable_time` datetime ,
  `disable_time` datetime ,
  `timeliness` varchar(40)
)
ENGINE=mysql
PROPERTIES
(
    "host" = "mysql-products-service.middleware-dev.svc.cluster.local",
    "port" = "3306",
    "user" = "voc",
    "password" = "voc2024.",
    "database" = "voc",
    "table" = "ins_alt_task_config_data"
);
