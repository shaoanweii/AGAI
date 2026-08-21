-- voc.ins_record_logs definition

CREATE TABLE `ins_record_logs` (
                                   `id` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                                   `log_type` decimal(11,0) DEFAULT NULL COMMENT '日志类型（1登录日志，2操作日志）',
                                   `log_content` text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '日志内容',
                                   `operate_type` decimal(11,0) DEFAULT NULL COMMENT '操作类型',
                                   `userid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '操作用户账号',
                                   `username` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '操作用户名称',
                                   `ip` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'IP',
                                   `method` text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '请求java方法',
                                   `request_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '请求路径',
                                   `request_param` longtext CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '请求参数',
                                   `request_type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '请求类型',
                                   `cost_time` decimal(20,0) DEFAULT NULL COMMENT '耗时',
                                   `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '创建人',
                                   `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                   `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '更新人',
                                   `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                   `app_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '系统标识',
                                   `tid` varchar(55) DEFAULT NULL COMMENT '追踪标识',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   KEY `idx_userid_type` (`userid`,`log_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='系统日志表';