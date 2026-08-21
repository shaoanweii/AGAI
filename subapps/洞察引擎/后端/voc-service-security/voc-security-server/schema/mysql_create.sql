-- voc.sys_apps definition

CREATE TABLE `sys_apps` (
                            `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                            `app_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '系统标识',
                            `urls` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '系统访问URL',
                            `note` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- voc.sys_credentials definition

CREATE TABLE `sys_credentials` (
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
                                   PRIMARY KEY (`id`),
                                   KEY `sys_credentials_identity_type_IDX` (`identity_type`,`app_id`,`identifier`) USING BTREE,
                                   KEY `sys_credentials_user_id_IDX` (`user_id`,`app_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号信息';


-- voc.sys_login_histroy definition

CREATE TABLE `sys_login_histroy` (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `credential_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号标识',
                                     `user_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户标识',
                                     `login_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登陆方式',
                                     `app_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登陆系统',
                                     `login_time` datetime(3) NOT NULL COMMENT '登陆时间',
                                     `tid` varchar(55) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '追踪标识',
                                     PRIMARY KEY (`id`),
                                     KEY `sys_login_histroy_id_IDX` (`id`,`user_id`,`login_time`,`app_id`) USING BTREE,
                                     KEY `sys_login_histroy_user_id_IDX` (`user_id`,`app_id`,`credential_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1764917116087922691 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号登陆记录';

-- voc.sys_users definition

CREATE TABLE `sys_users` (
                             `id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                             `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
                             `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
                             `firstname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
                             `lastname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
                             `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
                             `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人',
                             `labelstud_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'labelstud系统token',
                             `non_locked` int NOT NULL DEFAULT '1' COMMENT '是否锁定',
                             `enabled` int NOT NULL DEFAULT '1' COMMENT '是否启用',
                             `expire_date` datetime(3) NOT NULL DEFAULT '2099-12-30 00:00:00.000' COMMENT '过期时间',
                             `start_expire_date` datetime DEFAULT CURRENT_TIMESTAMP,
                             `client_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户标识',
                             PRIMARY KEY (`id`),
                             KEY `sys_users_username_IDX` (`username`,`phone`,`email`) USING BTREE,
                             KEY `sys_users_client_id_IDX` (`client_id`,`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;