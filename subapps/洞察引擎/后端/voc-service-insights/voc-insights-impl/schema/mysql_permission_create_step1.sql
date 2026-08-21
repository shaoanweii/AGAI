-- 新建客户需要初始化的权限表
CREATE TABLE `ins_role`
(
    `id`          varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键id',
    `role_name`   varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci         DEFAULT NULL COMMENT '角色名称',
    `role_type`   int                                                    NOT NULL DEFAULT '1' COMMENT '角色类型',
    `enabled`     int                                                    NOT NULL DEFAULT '1' COMMENT '是否启用',
    `create_user` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci          DEFAULT NULL COMMENT '创建人',
    `create_time` datetime                                                        DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime                                                        DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  ROW_FORMAT = DYNAMIC COMMENT ='角色表';



CREATE TABLE `ins_user_role`
(
    `id`          varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键id',
    `user_id`     varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户id',
    `role_id`     varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色id',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_uid_rid` (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  ROW_FORMAT = DYNAMIC COMMENT ='用户角色表';


CREATE TABLE `ins_role_relation_permission`
(
    `id`                varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键id',
    `role_id`           varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '菜单标题',
    `permission_id`     varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '菜单权限Id',
    `button_permission` int                                                    DEFAULT NULL COMMENT '按钮权限数字',
    `permission_type`   tinyint(1)                                             DEFAULT NULL COMMENT '1:菜单 2:按钮',
    `create_time`       datetime                                               DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  ROW_FORMAT = DYNAMIC COMMENT ='角色权限关联表';


CREATE TABLE `ins_button_permission`
(
    `id`             varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键id',
    `menu_id`        varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '菜单id',
    `parent_id`      varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '父id',
    `name`           varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '按钮名称',
    `button_code`    int                                                     DEFAULT NULL COMMENT '按钮数值',
    `sort_no`        int                                                     DEFAULT NULL COMMENT '按钮排序',
    `icon`           varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '按钮图标',
    `last_level`     tinyint(1)                                              DEFAULT '0' COMMENT '是否是末级 0否 1是',
    `app_id`         varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '系统标识',
    `create_time`    datetime                                                DEFAULT NULL COMMENT '创建时间',
    `api_url`        varchar(100)                                            DEFAULT NULL,
    `permission_key` varchar(100)                                            DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  ROW_FORMAT = DYNAMIC COMMENT ='按钮权限表';



CREATE TABLE `ins_menu_permission`
(
    `id`             varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键id',
    `parent_id`      varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '父id',
    `name`           varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '菜单标题',
    `html_uri`       varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '路径',
    `api_url`        varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '接口路径',
    `sort_no`        int                                                     DEFAULT NULL COMMENT '菜单排序',
    `icon`           varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '菜单图标',
    `last_level`     tinyint(1)                                              DEFAULT '0' COMMENT '是否是末级 0否 1是',
    `app_id`         varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  DEFAULT NULL COMMENT '系统标识',
    `create_time`    datetime                                                DEFAULT NULL COMMENT '创建时间',
    `permission_key` varchar(100)                                            DEFAULT NULL COMMENT '权限key',
    `del_flag`       varchar(10)                                             DEFAULT '0' COMMENT '0未删除 1删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3
  ROW_FORMAT = DYNAMIC COMMENT ='菜单权限表';


-- 插入超级管理员角色 替换库名
INSERT INTO ins_role(id, role_name, role_type, enabled, create_time)
VALUES (SUBSTRING(REPLACE(UUID(), '-', ''), 1, 32), '超级管理员', '1', '1', now());

-- 插入超级管理员管理权限ID 替换库名
INSERT INTO ins_role_relation_permission (id, role_id, permission_id, button_permission, permission_type, create_time)
select SUBSTRING(REPLACE(UUID(), '-', ''), 1, 32)                                         as id,
       (select id from ins_role ir where ir.role_name = '超级管理员' and role_type = '1') as role_id,
       permission_id,
       button_permission,
       permission_type,
       now()
from voc.ins_customer_permission
where client_id = 'e11ab369ea4d56a7a64ab0a3c491a2cc';


-- 插入按钮权限基础信息 替换库名
INSERT INTO ins_button_permission (id, parent_id, name, button_code, sort_no, icon, last_level, app_id, api_url,
                                   create_time, permission_key)
WITH RECURSIVE tree (id, parent_id, name, html_uri, button_code, sort_no, icon, last_level, app_id, api_url,
                     create_time, permissionType, permissionKey) AS
                   (select d.*
                    from (SELECT id,
                                 parent_id,
                                 name,
                                 null           as html_uri,
                                 button_code,
                                 sort_no,
                                 icon,
                                 last_level,
                                 app_id,
                                 api_url,
                                 create_time,
                                 1              as permissionType,
                                 permission_key as permissionKey
                          FROM voc.ins_button_permission
                          union all
                          SELECT id,
                                 parent_id,
                                 name,
                                 html_uri,
                                 null           as button_code,
                                 sort_no,
                                 icon,
                                 last_level,
                                 app_id,
                                 api_url,
                                 create_time,
                                 2              as permissionType,
                                 permission_key as permissionKey
                          FROM voc.ins_menu_permission
                          where del_flag = 0) d
                    where d.id in (select permission_id
                                   from ins_role_relation_permission
                                   where role_id = (select id
                                                    from ins_role ir
                                                    where ir.role_name = '超级管理员' and role_type = '1')
                                     and permission_type = 1)
                    union all
                    select f.*
                    from (SELECT id,
                                 parent_id,
                                 name,
                                 null           as html_uri,
                                 button_code,
                                 sort_no,
                                 icon,
                                 last_level,
                                 app_id,
                                 api_url,
                                 create_time,
                                 1              as permissionType,
                                 permission_key as permissionKey
                          FROM voc.ins_button_permission
                          union all
                          SELECT id,
                                 parent_id,
                                 name,
                                 html_uri,
                                 null           as button_code,
                                 sort_no,
                                 icon,
                                 last_level,
                                 app_id,
                                 api_url,
                                 create_time,
                                 2              as permissionType,
                                 permission_key as permissionKey
                          FROM voc.ins_menu_permission
                          where del_flag = 0) f
                             INNER JOIN tree t ON t.parent_id = f.id)
SELECT id,
       parent_id,
       name,
       button_code,
       sort_no,
       icon,
       last_level,
       app_id,
       api_url,
       now(),
       permissionKey
FROM tree
where permissionType = 1
group by id;


-- 插入菜单权限基础信息 替换库名
INSERT INTO ins_menu_permission (id, parent_id, name, html_uri, api_url, sort_no, icon, last_level, app_id, create_time,
                                 permission_key, del_flag)
WITH RECURSIVE tree (id, parent_id, name, html_uri, button_code, sort_no, icon, last_level, app_id, api_url,
                     create_time, permissionType, permissionKey) AS
                   (select d.*
                    from (SELECT id,
                                 parent_id,
                                 name,
                                 null           as html_uri,
                                 button_code,
                                 sort_no,
                                 icon,
                                 last_level,
                                 app_id,
                                 api_url,
                                 create_time,
                                 1              as permissionType,
                                 permission_key as permissionKey
                          FROM voc.ins_button_permission
                          union all
                          SELECT id,
                                 parent_id,
                                 name,
                                 html_uri,
                                 null           as button_code,
                                 sort_no,
                                 icon,
                                 last_level,
                                 app_id,
                                 api_url,
                                 create_time,
                                 2              as permissionType,
                                 permission_key as permissionKey
                          FROM voc.ins_menu_permission
                          where del_flag = 0) d
                    where d.id in (select permission_id
                                   from ins_role_relation_permission
                                   where role_id = (select id
                                                    from ins_role ir
                                                    where ir.role_name = '超级管理员' and role_type = '1')
                                     and permission_type = 1)
                    union all
                    select f.*
                    from (SELECT id,
                                 parent_id,
                                 name,
                                 null           as html_uri,
                                 button_code,
                                 sort_no,
                                 icon,
                                 last_level,
                                 app_id,
                                 api_url,
                                 create_time,
                                 1              as permissionType,
                                 permission_key as permissionKey
                          FROM voc.ins_button_permission
                          union all
                          SELECT id,
                                 parent_id,
                                 name,
                                 html_uri,
                                 null           as button_code,
                                 sort_no,
                                 icon,
                                 last_level,
                                 app_id,
                                 api_url,
                                 create_time,
                                 2              as permissionType,
                                 permission_key as permissionKey
                          FROM voc.ins_menu_permission
                          where del_flag = 0) f
                             INNER JOIN tree t ON t.parent_id = f.id)
SELECT id,
       parent_id,
       name,
       html_uri,
       api_url,
       sort_no,
       icon,
       last_level,
       app_id,
       now(),
       permissionKey,
       0 as del_flag
FROM tree
where permissionType = 2
group by id;


-- 替换用户密码信息
public static void main(String[] args) {
        String encrypt = PBEStringEncryptor.getInstance().encrypt("hst_admin123");
System.out.println(encrypt);
}

-- 客户库创建超级管理员关联角色信息
INSERT INTO ins_user_role (id, user_id, role_id, create_time)
VALUES (SUBSTRING(REPLACE(UUID(), '-', ''), 1, 32),
        (select id
         from voc.sys_users
         where username =
               CONCAT((select code from voc.ins_customer_info ici where id = 'e11ab369ea4d56a7a64ab0a3c491a2cc'), '_',
                      'admin')
           and client_id = 'e11ab369ea4d56a7a64ab0a3c491a2cc'),
        (select id from ins_role ir where ir.role_name = '超级管理员' and role_type = '1'),
        now());

