-- security_data.sys_user definition

CREATE TABLE `sys_user`
(
    `id`          varchar(60) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `username`    varchar(100) COLLATE utf8mb4_unicode_ci                       DEFAULT NULL,
    `app_id`      varchar(20) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `phone`       varchar(20) COLLATE utf8mb4_unicode_ci                        DEFAULT NULL,
    `firstname`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `lastname`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `email`       varchar(50) COLLATE utf8mb4_unicode_ci                        DEFAULT NULL,
    `create_time` datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime                                                      DEFAULT CURRENT_TIMESTAMP,
    `operator`    varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `non_expired` int                                     NOT NULL              DEFAULT '0',
    `non_locked`  int                                     NOT NULL              DEFAULT '0',
    `enabled`     int                                     NOT NULL              DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- security_data.sys_credentials definition

CREATE TABLE `sys_credentials`
(
    `id`            varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL,
    `user_id`       varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT 'sys_user_id',
    `credential`    varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '密码',
    `app_id`        varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL,
    `identifier`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '例如：手机号 ,unionid',
    `identity_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT 'phone、weixin、base',
    `create_time`   datetime                                                               DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime                                                               DEFAULT CURRENT_TIMESTAMP,
    `operator`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `non_expired`   int                                                           NOT NULL DEFAULT '0',
    `non_locked`    int                                                           NOT NULL DEFAULT '0',
    `enabled`       int                                                           NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- security_data.sys_apps definition

CREATE TABLE `sys_apps`
(
    `id`     varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
    `app_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
    `note`   varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;