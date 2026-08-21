-- Local development seed for Insights user menu permissions.
-- Run inside each local tenant database after the Insights schema is created.

SET NAMES utf8mb4;

SET @local_client_id := '764547797eb2e192763f5334028d49c9';
SET @local_role_id := 'local_super_admin_role';
SET @local_user_id := '1';

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_menu_permission ADD COLUMN filter_status int DEFAULT 0 COMMENT ''0 visible, 1 hidden''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_menu_permission'
    AND COLUMN_NAME = 'filter_status'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_button_permission ADD COLUMN filter_status int DEFAULT 0 COMMENT ''0 visible, 1 hidden''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_button_permission'
    AND COLUMN_NAME = 'filter_status'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_role ADD COLUMN remark varchar(255) DEFAULT NULL COMMENT ''备注''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_role'
    AND COLUMN_NAME = 'remark'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_brand_info ADD COLUMN automark varchar(100) DEFAULT NULL COMMENT ''车企''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_brand_info'
    AND COLUMN_NAME = 'automark'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_brand_info ADD COLUMN automark_id varchar(60) DEFAULT NULL COMMENT ''车企id''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_brand_info'
    AND COLUMN_NAME = 'automark_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_brand_info ADD COLUMN is_core int DEFAULT 0 COMMENT ''是否核心''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_brand_info'
    AND COLUMN_NAME = 'is_core'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_brand_info ADD COLUMN competitive_type int DEFAULT 3 COMMENT ''本竞品类型''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_brand_info'
    AND COLUMN_NAME = 'competitive_type'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_brand_info ADD COLUMN competitive_product json DEFAULT NULL COMMENT ''本竞品关系''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_brand_info'
    AND COLUMN_NAME = 'competitive_product'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_brand_info ADD COLUMN update_user varchar(100) DEFAULT NULL COMMENT ''更新人''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_brand_info'
    AND COLUMN_NAME = 'update_user'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_brand_info ADD COLUMN nature varchar(50) DEFAULT NULL COMMENT ''性质''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_brand_info'
    AND COLUMN_NAME = 'nature'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_brand_info ADD COLUMN country varchar(50) DEFAULT NULL COMMENT ''国家''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_brand_info'
    AND COLUMN_NAME = 'country'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_brand_info ADD COLUMN status varchar(20) DEFAULT ''1'' COMMENT ''状态''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_brand_info'
    AND COLUMN_NAME = 'status'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN brand_code varchar(60) DEFAULT NULL COMMENT ''品牌编码''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'brand_code'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN car_name varchar(100) DEFAULT NULL COMMENT ''车型名称''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'car_name'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN car_code varchar(60) DEFAULT NULL COMMENT ''车型编码''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'car_code'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN factory varchar(100) DEFAULT NULL COMMENT ''工厂''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'factory'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN level_name varchar(100) DEFAULT NULL COMMENT ''级别名称''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'level_name'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN competitive_type int DEFAULT 3 COMMENT ''本竞品类型''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'competitive_type'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN competitive_product json DEFAULT NULL COMMENT ''关联本竞品''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'competitive_product'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN preheat_start_time date DEFAULT NULL COMMENT ''预热开始时间''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'preheat_start_time'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN preheat_end_time date DEFAULT NULL COMMENT ''预热结束时间''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'preheat_end_time'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN launch_start_time date DEFAULT NULL COMMENT ''上市开始时间''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'launch_start_time'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN launch_end_time date DEFAULT NULL COMMENT ''上市结束时间''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'launch_end_time'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN stable_start_time date DEFAULT NULL COMMENT ''稳定开始时间''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'stable_start_time'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN stable_end_time date DEFAULT NULL COMMENT ''稳定结束时间''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'stable_end_time'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN is_core int DEFAULT 0 COMMENT ''是否核心车系''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'is_core'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN update_user varchar(100) DEFAULT NULL COMMENT ''更新人''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'update_user'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN country varchar(50) DEFAULT NULL COMMENT ''国家''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'country'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN is_new_car int DEFAULT 0 COMMENT ''是否新车''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'is_new_car'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_car_series_info ADD COLUMN status varchar(20) DEFAULT ''1'' COMMENT ''状态''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_car_series_info'
    AND COLUMN_NAME = 'status'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_channel ADD COLUMN code varchar(60) DEFAULT NULL COMMENT ''渠道编码''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_channel'
    AND COLUMN_NAME = 'code'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_channel ADD COLUMN level int DEFAULT 1 COMMENT ''渠道层级''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_channel'
    AND COLUMN_NAME = 'level'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_channel ADD COLUMN create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_channel'
    AND COLUMN_NAME = 'create_time'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_channel ADD COLUMN update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_channel'
    AND COLUMN_NAME = 'update_time'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO ins_customer_info
  (id, code, full_name, abbreviation, province, city, contacts, phone, email, address, remark, status, del_flag, create_time, update_time, create_user, update_user, sort)
VALUES
  (@local_client_id, 'local', '本地开发客户', '本地开发', '110000', '110100000000', 'local', '13211111111', 'local@example.com', 'local', '本地开发默认客户', 1, 0, NOW(), NOW(), 'local', 'local', 0)
ON DUPLICATE KEY UPDATE
  code = VALUES(code),
  full_name = VALUES(full_name),
  abbreviation = VALUES(abbreviation),
  status = VALUES(status),
  del_flag = VALUES(del_flag),
  update_time = NOW();

INSERT INTO ins_role (id, role_name, role_type, enabled, create_user, create_time, remark)
VALUES (@local_role_id, '本地超级管理员', 1, 1, 'local', NOW(), '本地开发权限种子角色')
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  role_type = VALUES(role_type),
  enabled = VALUES(enabled),
  remark = VALUES(remark),
  update_time = NOW();

INSERT INTO ins_menu_permission
  (id, parent_id, name, html_uri, api_url, sort_no, icon, last_level, app_id, create_time, permission_key, del_flag, filter_status)
VALUES
  ('m_dc', '0', '数据治理', '/dataCenter', NULL, 100, 'menus-database-2-fill', 0, 'insights', NOW(), 'dataCenter', '0', 0),
  ('m_dc_dq', 'm_dc', '数据查询', '/dataCenter/dataQuery', NULL, 110, 'menus-database-2-fill', 1, 'insights', NOW(), 'dataCenter-dataQuery', '0', 0),
  ('m_dc_task', 'm_dc', '任务管理', '/dataCenter/taskManagement', NULL, 120, 'menus-database-2-fill', 1, 'insights', NOW(), 'dataCenter-taskManagement', '0', 0),
  ('m_dc_discovery', 'm_dc', '新词发现', '/dataCenter/discovery', NULL, 130, 'menus-database-2-fill', 1, 'insights', NOW(), 'dataCenter-discovery', '0', 0),
  ('m_kc', '0', '知识中心', '/knowledgeCenter', NULL, 200, 'menus-instance-fill', 0, 'insights', NOW(), 'knowledgeCenter', '0', 0),
  ('m_kc_cm', 'm_kc', '语料映射', '/knowledgeCenter/corpusMapping', NULL, 210, 'menus-instance-fill', 1, 'insights', NOW(), 'knowledgeCenter-corpusMapping', '0', 0),
  ('m_kc_kw', 'm_kc', '关键词库', '/knowledgeCenter/keywordLibrary', NULL, 220, 'menus-instance-fill', 1, 'insights', NOW(), 'knowledgeCenter-keywordLibrary', '0', 0),
  ('m_kc_sp', 'm_kc', '标准观点', '/knowledgeCenter/standardPoint', NULL, 230, 'menus-instance-fill', 1, 'insights', NOW(), 'knowledgeCenter-standardPoint', '0', 0),
  ('m_kc_bs', 'm_kc', '品牌车系', '/knowledgeCenter/brandSeries', NULL, 240, 'menus-instance-fill', 1, 'insights', NOW(), 'knowledgeCenter-brandSeries', '0', 0),
  ('m_kc_scene', 'm_kc', '用车场景', '/knowledgeCenter/carUsageScenarios', NULL, 250, 'menus-instance-fill', 1, 'insights', NOW(), 'knowledgeCenter-carUsageScenarios', '0', 0),
  ('m_kc_exp', 'm_kc', '体验代码', '/knowledgeCenter/experienceCode', NULL, 260, 'menus-instance-fill', 1, 'insights', NOW(), 'knowledgeCenter-experienceCode', '0', 0),
  ('m_rules', '0', '规则引擎', '/rules', NULL, 400, 'menus-ruler-fill', 0, 'insights', NOW(), 'rules', '0', 0),
  ('m_rules_rt', 'm_rules', '规则测试', '/rules/rulesTest', NULL, 410, 'menus-ruler-fill', 1, 'insights', NOW(), 'rules-rulesTest', '0', 0),
  ('m_rules_clean', 'm_rules', '清洗规则', '/rules/cleaningRules', NULL, 420, 'menus-ruler-fill', 1, 'insights', NOW(), 'rules-cleaningRules', '0', 0),
  ('m_set', '0', '系统设置', '/settings', NULL, 500, 'menus-settings-6-fill', 0, 'insights', NOW(), 'settings', '0', 0),
  ('m_set_acct', 'm_set', '账号管理', '/settings/accountManagement', NULL, 510, 'menus-settings-6-fill', 1, 'insights', NOW(), 'settings-accountManagement', '0', 0),
  ('m_set_role', 'm_set', '角色管理', '/settings/role', NULL, 520, 'menus-settings-6-fill', 1, 'insights', NOW(), 'settings-role', '0', 0),
  ('m_set_dl', 'm_set', '下载管理', '/settings/download', NULL, 530, 'menus-settings-6-fill', 1, 'insights', NOW(), 'settings-download', '0', 0),
  ('m_set_log', 'm_set', '操作日志', '/settings/operationLog', NULL, 540, 'menus-settings-6-fill', 1, 'insights', NOW(), 'settings-operationLog', '0', 0)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  html_uri = VALUES(html_uri),
  api_url = VALUES(api_url),
  sort_no = VALUES(sort_no),
  icon = VALUES(icon),
  last_level = VALUES(last_level),
  app_id = VALUES(app_id),
  permission_key = VALUES(permission_key),
  del_flag = VALUES(del_flag),
  filter_status = VALUES(filter_status);

UPDATE ins_menu_permission
SET del_flag = '1',
    filter_status = 1
WHERE permission_key IN (
  'review',
  'review-errorCorrection',
  'rules-closedLoopRules',
  'knowledgeCenter-attributeLabel'
);

INSERT INTO ins_button_permission
  (id, menu_id, parent_id, name, button_code, sort_no, icon, last_level, app_id, create_time, api_url, permission_key, filter_status)
VALUES
  ('b_dc_dq_sel', 'm_dc_dq', 'm_dc_dq', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'dataCenter-dataQuery-select', 0),
  ('b_dc_task_sel', 'm_dc_task', 'm_dc_task', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'dataCenter-taskManagement-select', 0),
  ('b_dc_discovery_sel', 'm_dc_discovery', 'm_dc_discovery', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'dataCenter-discovery-select', 0),
  ('b_kc_cm_sel', 'm_kc_cm', 'm_kc_cm', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'knowledgeCenter-corpusMapping-select', 0),
  ('b_kc_kw_sel', 'm_kc_kw', 'm_kc_kw', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'knowledgeCenter-keywordLibrary-select', 0),
  ('b_kc_sp_sel', 'm_kc_sp', 'm_kc_sp', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'knowledgeCenter-standardPoint-select', 0),
  ('b_kc_bs_sel', 'm_kc_bs', 'm_kc_bs', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'knowledgeCenter-brandSeries-select', 0),
  ('b_kc_scene_sel', 'm_kc_scene', 'm_kc_scene', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'knowledgeCenter-carUsageScenarios-select', 0),
  ('b_kc_exp_sel', 'm_kc_exp', 'm_kc_exp', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'knowledgeCenter-experienceCode-select', 0),
  ('b_rules_rt_sel', 'm_rules_rt', 'm_rules_rt', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'rules-rulesTest-select', 0),
  ('b_rules_clean_sel', 'm_rules_clean', 'm_rules_clean', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'rules-cleaningRules-select', 0),
  ('b_set_acct_sel', 'm_set_acct', 'm_set_acct', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'settings-accountManagement-select', 0),
  ('b_set_role_sel', 'm_set_role', 'm_set_role', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'settings-role-select', 0),
  ('b_set_dl_sel', 'm_set_dl', 'm_set_dl', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'settings-download-select', 0),
  ('b_set_log_sel', 'm_set_log', 'm_set_log', '查看', 1, 1, NULL, 1, 'insights', NOW(), NULL, 'settings-operationLog-select', 0)
ON DUPLICATE KEY UPDATE
  menu_id = VALUES(menu_id),
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  button_code = VALUES(button_code),
  sort_no = VALUES(sort_no),
  icon = VALUES(icon),
  last_level = VALUES(last_level),
  app_id = VALUES(app_id),
  api_url = VALUES(api_url),
  permission_key = VALUES(permission_key),
  filter_status = VALUES(filter_status);

UPDATE ins_button_permission
SET filter_status = 1
WHERE permission_key IN (
  'review-errorCorrection-select',
  'rules-closedLoopRules-select',
  'knowledgeCenter-attributeLabel-select'
);

INSERT INTO ins_customer_permission
  (id, client_id, permission_id, button_permission, permission_type, create_user, create_time)
SELECT CONCAT('cp_', id), @local_client_id, id, NULL, 2, 'local', NOW()
FROM ins_menu_permission
WHERE permission_key IN (
  'dataCenter',
  'dataCenter-dataQuery',
  'dataCenter-taskManagement',
  'dataCenter-discovery',
  'knowledgeCenter',
  'knowledgeCenter-corpusMapping',
  'knowledgeCenter-keywordLibrary',
  'knowledgeCenter-standardPoint',
  'knowledgeCenter-brandSeries',
  'knowledgeCenter-carUsageScenarios',
  'knowledgeCenter-experienceCode',
  'rules',
  'rules-rulesTest',
  'rules-cleaningRules',
  'settings',
  'settings-accountManagement',
  'settings-role',
  'settings-download',
  'settings-operationLog'
)
ON DUPLICATE KEY UPDATE
  client_id = VALUES(client_id),
  permission_id = VALUES(permission_id),
  button_permission = VALUES(button_permission),
  permission_type = VALUES(permission_type);

INSERT INTO ins_customer_permission
  (id, client_id, permission_id, button_permission, permission_type, create_user, create_time)
SELECT CONCAT('cp_', id), @local_client_id, id, button_code, 1, 'local', NOW()
FROM ins_button_permission
WHERE permission_key IN (
  'dataCenter-dataQuery-select',
  'dataCenter-taskManagement-select',
  'dataCenter-discovery-select',
  'knowledgeCenter-corpusMapping-select',
  'knowledgeCenter-keywordLibrary-select',
  'knowledgeCenter-standardPoint-select',
  'knowledgeCenter-brandSeries-select',
  'knowledgeCenter-carUsageScenarios-select',
  'knowledgeCenter-experienceCode-select',
  'rules-rulesTest-select',
  'rules-cleaningRules-select',
  'settings-accountManagement-select',
  'settings-role-select',
  'settings-download-select',
  'settings-operationLog-select'
)
ON DUPLICATE KEY UPDATE
  client_id = VALUES(client_id),
  permission_id = VALUES(permission_id),
  button_permission = VALUES(button_permission),
  permission_type = VALUES(permission_type);

INSERT INTO ins_role_relation_permission
  (id, role_id, permission_id, button_permission, permission_type, create_time)
SELECT CONCAT('rrp_', id), @local_role_id, id, button_code, 1, NOW()
FROM ins_button_permission
WHERE permission_key IN (
  'dataCenter-dataQuery-select',
  'dataCenter-taskManagement-select',
  'dataCenter-discovery-select',
  'knowledgeCenter-corpusMapping-select',
  'knowledgeCenter-keywordLibrary-select',
  'knowledgeCenter-standardPoint-select',
  'knowledgeCenter-brandSeries-select',
  'knowledgeCenter-carUsageScenarios-select',
  'knowledgeCenter-experienceCode-select',
  'rules-rulesTest-select',
  'rules-cleaningRules-select',
  'settings-accountManagement-select',
  'settings-role-select',
  'settings-download-select',
  'settings-operationLog-select'
)
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id),
  permission_id = VALUES(permission_id),
  button_permission = VALUES(button_permission),
  permission_type = VALUES(permission_type);

INSERT INTO ins_user_role (id, user_id, role_id, create_time)
VALUES ('local_admin_user_role', @local_user_id, @local_role_id, NOW())
ON DUPLICATE KEY UPDATE
  role_id = VALUES(role_id),
  create_time = NOW();

CREATE TABLE IF NOT EXISTS sys_users (
  id varchar(60) NOT NULL,
  username varchar(100) DEFAULT NULL,
  firstname varchar(100) DEFAULT NULL,
  employee_id varchar(50) DEFAULT NULL,
  enabled int DEFAULT 1,
  client_id varchar(50) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_credentials (
  id varchar(60) NOT NULL,
  user_id varchar(60) DEFAULT NULL,
  app_id varchar(50) DEFAULT NULL,
  enabled int DEFAULT 1,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP PROCEDURE IF EXISTS local_add_column;
DELIMITER //
CREATE PROCEDURE local_add_column(IN p_table varchar(64), IN p_column varchar(64), IN p_definition text)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table
      AND column_name = p_column
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_column, ' ', p_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL local_add_column('sys_users', 'email', 'varchar(255) DEFAULT NULL');
CALL local_add_column('sys_users', 'lastname', 'varchar(100) DEFAULT NULL');
CALL local_add_column('sys_users', 'phone', 'varchar(50) DEFAULT NULL');
CALL local_add_column('sys_users', 'operator', 'varchar(100) DEFAULT NULL');
CALL local_add_column('sys_users', 'create_time', 'datetime DEFAULT CURRENT_TIMESTAMP');
CALL local_add_column('sys_users', 'update_time', 'datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL local_add_column('sys_users', 'labelstud_token', 'varchar(255) DEFAULT NULL');
CALL local_add_column('sys_users', 'expire_date', 'datetime DEFAULT ''2099-12-30 00:00:00''');
CALL local_add_column('sys_users', 'start_expire_date', 'datetime DEFAULT ''2000-01-01 00:00:00''');
CALL local_add_column('sys_users', 'non_locked', 'int DEFAULT 1');
CALL local_add_column('sys_users', 'remark', 'varchar(255) DEFAULT NULL');
CALL local_add_column('sys_users', 'position', 'varchar(100) DEFAULT NULL');
CALL local_add_column('sys_users', 'office_phone', 'varchar(50) DEFAULT NULL');
CALL local_add_column('sys_users', 'home_phone', 'varchar(50) DEFAULT NULL');

DROP PROCEDURE IF EXISTS local_add_column;

SET @ddl := IF(
  DATABASE() = 'vdp_ms_be',
  'SELECT 1',
  CONCAT(
    'INSERT INTO sys_users (id, username, firstname, lastname, employee_id, enabled, client_id, expire_date, start_expire_date, non_locked) VALUES (''',
    @local_user_id,
    ''', ''admin'', ''admin'', ''admin'', ''admin'', 1, ''',
    @local_client_id,
    ''', ''2099-12-30 00:00:00'', ''2000-01-01 00:00:00'', 1) ON DUPLICATE KEY UPDATE username = VALUES(username), firstname = VALUES(firstname), lastname = VALUES(lastname), employee_id = VALUES(employee_id), enabled = VALUES(enabled), client_id = VALUES(client_id), expire_date = VALUES(expire_date), start_expire_date = VALUES(start_expire_date), non_locked = VALUES(non_locked)'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := IF(
  DATABASE() = 'vdp_ms_be',
  'SELECT 1',
  'INSERT INTO sys_credentials (id, user_id, app_id, enabled) VALUES (''1'', ''1'', ''insights'', 1) ON DUPLICATE KEY UPDATE user_id = VALUES(user_id), app_id = VALUES(app_id), enabled = VALUES(enabled)'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS sta_sys_depart (
  id varchar(60) NOT NULL,
  depart_id varchar(60) DEFAULT NULL,
  name varchar(100) DEFAULT NULL,
  status varchar(10) DEFAULT '1',
  code varchar(60) DEFAULT NULL,
  parent_id varchar(60) DEFAULT NULL,
  parent_code varchar(60) DEFAULT NULL,
  remark varchar(255) DEFAULT NULL,
  org_order varchar(20) DEFAULT '0',
  org_level varchar(20) DEFAULT '1',
  org_admin varchar(60) DEFAULT NULL,
  org_type varchar(20) DEFAULT '3',
  company_id varchar(60) DEFAULT NULL,
  company_code varchar(60) DEFAULT NULL,
  org_template_id varchar(60) DEFAULT NULL,
  org_id_path varchar(255) DEFAULT NULL,
  tenant_code varchar(60) DEFAULT NULL,
  tenant_id varchar(60) DEFAULT NULL,
  del_flag varchar(10) DEFAULT '0',
  create_by varchar(60) DEFAULT 'local',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_by varchar(60) DEFAULT 'local',
  PRIMARY KEY (id),
  KEY idx_sta_sys_depart_status (status),
  KEY idx_sta_sys_depart_code (code),
  KEY idx_sta_sys_depart_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sta_sys_user_depart (
  id varchar(60) NOT NULL,
  user_id varchar(100) DEFAULT NULL,
  dep_id varchar(60) DEFAULT NULL,
  org_type varchar(20) DEFAULT '3',
  PRIMARY KEY (id),
  KEY idx_sta_sys_user_depart_user (user_id),
  KEY idx_sta_sys_user_depart_dep (dep_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO sta_sys_depart
  (id, depart_id, name, status, code, parent_id, parent_code, remark, org_order, org_level, org_type, tenant_id, del_flag, create_by, create_time, update_by)
VALUES
  ('local_dept', 'local_dept', '本地默认部门', '1', 'local_dept', '-1', '-1', '本地开发默认部门', '0', '1', '3', @local_client_id, '0', 'local', NOW(), 'local')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  status = VALUES(status),
  code = VALUES(code),
  parent_id = VALUES(parent_id),
  parent_code = VALUES(parent_code),
  tenant_id = VALUES(tenant_id),
  del_flag = VALUES(del_flag),
  update_by = VALUES(update_by);

INSERT INTO sta_sys_user_depart (id, user_id, dep_id, org_type)
VALUES ('local_admin_user_depart', 'admin', 'local_dept', '3')
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  dep_id = VALUES(dep_id),
  org_type = VALUES(org_type);

INSERT INTO ins_channel
  (id, code, parent_id, name, type, status, name_en, level, create_time, update_time)
VALUES
  ('local_channel_root', 'local_channel_root', '0', '本地渠道', 'Category', '1', 'local_channel', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  code = VALUES(code),
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  type = VALUES(type),
  status = VALUES(status),
  name_en = VALUES(name_en),
  level = VALUES(level),
  update_time = NOW();

CREATE DATABASE IF NOT EXISTS voc_ms_td DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS voc_ms_td.sta_sys_user_depart (
  id varchar(60) NOT NULL,
  user_id varchar(100) DEFAULT NULL,
  dep_id varchar(60) DEFAULT NULL,
  org_type varchar(20) DEFAULT '3',
  PRIMARY KEY (id),
  KEY idx_sta_sys_user_depart_user (user_id),
  KEY idx_sta_sys_user_depart_dep (dep_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO voc_ms_td.sta_sys_user_depart (id, user_id, dep_id, org_type)
VALUES ('local_admin_user_depart', 'admin', 'local_dept', '3')
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  dep_id = VALUES(dep_id),
  org_type = VALUES(org_type);

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_data_resource ADD COLUMN type varchar(20) NOT NULL DEFAULT ''custom'' COMMENT ''资源组类型''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_data_resource'
    AND COLUMN_NAME = 'type'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_data_resource ADD COLUMN customer varchar(60) DEFAULT NULL COMMENT ''所属客户''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_data_resource'
    AND COLUMN_NAME = 'customer'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_data_resource ADD COLUMN rule_type varchar(50) DEFAULT NULL COMMENT ''规则类型''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_data_resource'
    AND COLUMN_NAME = 'rule_type'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE ins_data_resource ADD COLUMN icon varchar(100) DEFAULT NULL COMMENT ''图标''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ins_data_resource'
    AND COLUMN_NAME = 'icon'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS report_label_correction_record (
  id varchar(60) NOT NULL,
  error_type varchar(50) DEFAULT NULL,
  correction_count varchar(50) DEFAULT NULL,
  correction_time varchar(50) DEFAULT NULL,
  correction_info text DEFAULT NULL,
  correction_data longtext DEFAULT NULL,
  audit_status varchar(20) DEFAULT '0',
  audit_user varchar(100) DEFAULT NULL,
  audit_user_id varchar(60) DEFAULT NULL,
  audit_time datetime DEFAULT NULL,
  operate_time datetime DEFAULT CURRENT_TIMESTAMP,
  operate_user varchar(100) DEFAULT NULL,
  operate_user_id varchar(60) DEFAULT NULL,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_label_correction_operate_time (operate_time),
  KEY idx_label_correction_audit_status (audit_status),
  KEY idx_label_correction_operate_user (operate_user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ins_tag_client (
  id varchar(60) NOT NULL,
  app_client varchar(60) DEFAULT NULL,
  tag_parent_id varchar(60) DEFAULT NULL,
  tag_name varchar(255) DEFAULT NULL,
  tag_name_en varchar(255) DEFAULT NULL,
  tag_code varchar(100) DEFAULT NULL,
  tag_type varchar(50) DEFAULT NULL,
  tag_attribute varchar(50) DEFAULT NULL,
  energy_type json DEFAULT NULL,
  car_type json DEFAULT NULL,
  tag_status varchar(20) DEFAULT '1',
  tag_description text DEFAULT NULL,
  synonyms text DEFAULT NULL,
  seriousness varchar(50) DEFAULT NULL,
  user_journey1 varchar(100) DEFAULT NULL,
  user_journey2 varchar(100) DEFAULT NULL,
  scenario_attr varchar(100) DEFAULT NULL,
  attribute_label_ids json DEFAULT NULL,
  event_clarity varchar(50) DEFAULT NULL,
  d2c_responsible_dept varchar(60) DEFAULT NULL,
  d2c_accountable_dept json DEFAULT NULL,
  d2c_cc_dept json DEFAULT NULL,
  tag_accuracy varchar(50) DEFAULT NULL,
  tag_customer_issue_classification varchar(50) DEFAULT NULL,
  tag_issue_severity varchar(50) DEFAULT NULL,
  tag_code_status varchar(50) DEFAULT NULL,
  tag_business_domain varchar(100) DEFAULT NULL,
  tag_high_value_flag varchar(50) DEFAULT NULL,
  tag_complaint_flag_needing_reply varchar(50) DEFAULT NULL,
  tag_high_quality_voc_flag varchar(50) DEFAULT NULL,
  tag_new_energy_or_fuel varchar(50) DEFAULT NULL,
  tag_need_forvclosed_loop varchar(50) DEFAULT NULL,
  mapping_code varchar(100) DEFAULT NULL,
  susceptive_type varchar(50) DEFAULT NULL,
  identifier varchar(100) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_user varchar(60) DEFAULT 'local',
  update_user varchar(60) DEFAULT 'local',
  sort int DEFAULT 0,
  `level` int DEFAULT 1,
  emotion varchar(50) DEFAULT NULL,
  intention varchar(50) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_tag_client_app_type (app_client, tag_type),
  KEY idx_tag_client_parent (tag_parent_id),
  KEY idx_tag_client_code (tag_code),
  KEY idx_tag_client_attr (tag_attribute)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP PROCEDURE IF EXISTS local_add_column;
DELIMITER //
CREATE PROCEDURE local_add_column(IN p_table varchar(64), IN p_column varchar(64), IN p_definition text)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND COLUMN_NAME = p_column
  ) THEN
    SET @ddl := CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_column, ' ', p_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL local_add_column('ins_tag_client', 'app_client', 'varchar(60) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_parent_id', 'varchar(60) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_name', 'varchar(255) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_name_en', 'varchar(255) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_code', 'varchar(100) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_type', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_attribute', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'energy_type', 'json DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'car_type', 'json DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_status', 'varchar(20) DEFAULT ''1''');
CALL local_add_column('ins_tag_client', 'tag_description', 'text DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'synonyms', 'text DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'seriousness', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'user_journey1', 'varchar(100) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'user_journey2', 'varchar(100) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'scenario_attr', 'varchar(100) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'attribute_label_ids', 'json DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'event_clarity', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'd2c_responsible_dept', 'varchar(60) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'd2c_accountable_dept', 'json DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'd2c_cc_dept', 'json DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_accuracy', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_customer_issue_classification', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_issue_severity', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_code_status', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_business_domain', 'varchar(100) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_high_value_flag', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_complaint_flag_needing_reply', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_high_quality_voc_flag', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_new_energy_or_fuel', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'tag_need_forvclosed_loop', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'mapping_code', 'varchar(100) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'susceptive_type', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'identifier', 'varchar(100) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'create_time', 'datetime DEFAULT CURRENT_TIMESTAMP');
CALL local_add_column('ins_tag_client', 'update_time', 'datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL local_add_column('ins_tag_client', 'create_user', 'varchar(60) DEFAULT ''local''');
CALL local_add_column('ins_tag_client', 'update_user', 'varchar(60) DEFAULT ''local''');
CALL local_add_column('ins_tag_client', 'sort', 'int DEFAULT 0');
CALL local_add_column('ins_tag_client', 'level', 'int DEFAULT 1');
CALL local_add_column('ins_tag_client', 'emotion', 'varchar(50) DEFAULT NULL');
CALL local_add_column('ins_tag_client', 'intention', 'varchar(50) DEFAULT NULL');
DROP PROCEDURE IF EXISTS local_add_column;

INSERT INTO ins_tag_client
  (id, client_id, parent_id, name, name_en, code, type, label_type, enable, description,
   app_client, tag_parent_id, tag_name, tag_name_en, tag_code, tag_type, tag_attribute, tag_status, tag_description, create_user, update_user, sort, `level`, emotion, intention)
VALUES
  ('local_tag_ca_root', @local_client_id, '0', '本地体验代码', 'local_ca', 'LOCAL_CA', 'CA', 'Category', 1, '本地开发默认体验代码根节点',
   @local_client_id, '0', '本地体验代码', 'local_ca', 'LOCAL_CA', 'CA', 'Category', '1', '本地开发默认体验代码根节点', 'local', 'local', 0, 1, NULL, NULL),
  ('local_tag_ca_topic', @local_client_id, 'local_tag_ca_root', '本地标准观点', 'local_topic', 'LOCAL_TOPIC', 'CA', 'Topic', 1, '本地开发默认标准观点',
   @local_client_id, 'local_tag_ca_root', '本地标准观点', 'local_topic', 'LOCAL_TOPIC', 'CA', 'Topic', '1', '本地开发默认标准观点', 'local', 'local', 1, 2, 'neutral', 'other')
ON DUPLICATE KEY UPDATE
  client_id = VALUES(client_id),
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  code = VALUES(code),
  type = VALUES(type),
  enable = VALUES(enable),
  app_client = VALUES(app_client),
  tag_parent_id = VALUES(tag_parent_id),
  tag_name = VALUES(tag_name),
  tag_code = VALUES(tag_code),
  tag_type = VALUES(tag_type),
  tag_attribute = VALUES(tag_attribute),
  tag_status = VALUES(tag_status),
  update_time = NOW();

CREATE TABLE IF NOT EXISTS report_label_correction_info (
  id varchar(60) NOT NULL,
  correction_record_id varchar(60) DEFAULT NULL,
  original_data_id varchar(60) DEFAULT NULL,
  correction_data longtext DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_label_correction_info_record (correction_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ins_closed_rule (
  rule_id varchar(60) NOT NULL,
  rule_name varchar(255) DEFAULT NULL,
  data_source json DEFAULT NULL,
  rule_type varchar(50) DEFAULT NULL,
  category_type varchar(60) DEFAULT NULL,
  brand_code varchar(60) DEFAULT NULL,
  event_level varchar(50) DEFAULT NULL,
  process_priority varchar(50) DEFAULT NULL,
  audit_method varchar(50) DEFAULT NULL,
  audit_department json DEFAULT NULL,
  auditor json DEFAULT NULL,
  main_department json DEFAULT NULL,
  main_responder json DEFAULT NULL,
  cc_personnel json DEFAULT NULL,
  confirm_method varchar(50) DEFAULT NULL,
  confirm_department json DEFAULT NULL,
  confirmer json DEFAULT NULL,
  is_enabled varchar(50) DEFAULT 'disabled',
  version int DEFAULT 1,
  creator json DEFAULT NULL,
  updater json DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (rule_id),
  KEY idx_closed_rule_category (category_type),
  KEY idx_closed_rule_type (rule_type),
  KEY idx_closed_rule_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ins_closed_rule_condition (
  id varchar(60) NOT NULL,
  rule_id varchar(60) DEFAULT NULL,
  condition_type varchar(100) DEFAULT NULL,
  operator varchar(50) DEFAULT NULL,
  `option` varchar(255) DEFAULT NULL,
  value_type varchar(50) DEFAULT NULL,
  `value` longtext DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_closed_rule_condition_rule (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ins_closed_rule_condition_his LIKE ins_closed_rule_condition;

CREATE TABLE IF NOT EXISTS ins_closed_rule_alert (
  id varchar(60) NOT NULL,
  rule_id varchar(60) DEFAULT NULL,
  alert_cycle varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_closed_rule_alert_rule (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS report_rule_test_data (
  id varchar(60) NOT NULL,
  rule_id longtext DEFAULT NULL,
  batch_id varchar(60) DEFAULT NULL,
  rule_test_info varchar(255) DEFAULT NULL,
  rule_type varchar(50) DEFAULT NULL,
  create_user varchar(100) DEFAULT NULL,
  rule_count varchar(50) DEFAULT NULL,
  sample_count varchar(50) DEFAULT NULL,
  finish_time datetime DEFAULT NULL,
  test_status varchar(20) DEFAULT '0',
  file_name varchar(255) DEFAULT NULL,
  file_base_name varchar(255) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_rule_test_create_time (create_time),
  KEY idx_rule_test_status (test_status),
  KEY idx_rule_test_create_user (create_user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS report_rule_test_data_risk_info (
  id varchar(60) NOT NULL,
  batch_id varchar(60) DEFAULT NULL,
  rule_id varchar(60) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_rule_test_info_batch (batch_id),
  KEY idx_rule_test_info_rule (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS report_rule_test_data_risk_result (
  id varchar(60) NOT NULL,
  batch_id varchar(60) DEFAULT NULL,
  rule_id varchar(60) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_rule_test_result_batch (batch_id),
  KEY idx_rule_test_result_rule (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ins_car_scene_category (
  id varchar(60) NOT NULL,
  patent_id varchar(60) DEFAULT '0',
  category_name varchar(255) DEFAULT NULL,
  category_description text DEFAULT NULL,
  level int DEFAULT 1,
  synonyms text DEFAULT NULL,
  status varchar(20) DEFAULT '1',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by varchar(60) DEFAULT 'local',
  update_by varchar(60) DEFAULT 'local',
  PRIMARY KEY (id),
  KEY idx_car_scene_category_parent (patent_id),
  KEY idx_car_scene_category_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO ins_car_scene_category
  (id, patent_id, category_name, category_description, level, synonyms, status, create_by, create_time, update_by, update_time)
VALUES
  ('local_car_scene_root', '0', '本地场景', '本地开发默认用车场景分类', 1, NULL, '1', 'local', NOW(), 'local', NOW())
ON DUPLICATE KEY UPDATE
  patent_id = VALUES(patent_id),
  category_name = VALUES(category_name),
  category_description = VALUES(category_description),
  level = VALUES(level),
  status = VALUES(status),
  update_by = VALUES(update_by),
  update_time = NOW();

CREATE TABLE IF NOT EXISTS ins_car_scene (
  id varchar(60) NOT NULL,
  scene_name varchar(255) DEFAULT NULL,
  scene_description text DEFAULT NULL,
  category_id varchar(60) DEFAULT NULL,
  synonyms text DEFAULT NULL,
  status varchar(20) DEFAULT '1',
  create_by varchar(60) DEFAULT 'local',
  update_by varchar(60) DEFAULT 'local',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_car_scene_category (category_id),
  KEY idx_car_scene_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sta_attachment_download_record (
  id varchar(60) NOT NULL,
  task_id varchar(100) DEFAULT NULL,
  user_id varchar(60) DEFAULT NULL,
  user_name varchar(100) DEFAULT NULL,
  task_name varchar(255) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  status varchar(20) DEFAULT NULL,
  file_key varchar(255) DEFAULT NULL,
  file_url varchar(500) DEFAULT NULL,
  md5 varchar(64) DEFAULT NULL,
  parameters longtext DEFAULT NULL,
  app_id varchar(50) DEFAULT 'insights',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_download_app_user (app_id, user_id),
  KEY idx_download_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE OR REPLACE VIEW voc_ins_user_role_depart_v AS
SELECT
  u.id AS user_id,
  u.username,
  u.firstname,
  u.employee_id,
  d.dep_id,
  d.org_type
FROM sys_users u
LEFT JOIN sta_sys_user_depart d ON d.user_id = u.username
WHERE u.enabled = 1;

CREATE DATABASE IF NOT EXISTS voc_ms_be DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS voc_ms_be.ins_closed_rule LIKE ins_closed_rule;
CREATE TABLE IF NOT EXISTS voc_ms_be.ins_data_resource LIKE ins_data_resource;
CREATE TABLE IF NOT EXISTS voc_ms_td.report_rule_test_data_risk_info LIKE report_rule_test_data_risk_info;
CREATE TABLE IF NOT EXISTS voc_ms_td.report_rule_test_data_risk_result LIKE report_rule_test_data_risk_result;
CREATE TABLE IF NOT EXISTS voc_ms_td.sta_attachment_download_record LIKE sta_attachment_download_record;

CREATE TABLE IF NOT EXISTS voc_ms_td.ins_channel LIKE ins_channel;

CREATE TABLE IF NOT EXISTS voc_anal_flow_mate_data_v (
  id varchar(100) NOT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  content_type varchar(100) DEFAULT NULL,
  data_create_time datetime DEFAULT NULL,
  data_id varchar(100) DEFAULT NULL,
  channel_code varchar(100) DEFAULT NULL,
  brand varchar(100) DEFAULT NULL,
  series varchar(100) DEFAULT NULL,
  model varchar(100) DEFAULT NULL,
  is_outer varchar(20) DEFAULT NULL,
  one_id varchar(100) DEFAULT NULL,
  id_car_no varchar(100) DEFAULT NULL,
  mobile varchar(50) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  global_id varchar(100) DEFAULT NULL,
  user_id varchar(100) DEFAULT NULL,
  user_name varchar(255) DEFAULT NULL,
  vhl_id varchar(100) DEFAULT NULL,
  vhl_vin varchar(100) DEFAULT NULL,
  dlr_id varchar(100) DEFAULT NULL,
  dlr_code varchar(100) DEFAULT NULL,
  dlr_type varchar(100) DEFAULT NULL,
  market_id varchar(100) DEFAULT NULL,
  title text DEFAULT NULL,
  content text DEFAULT NULL,
  is_wsater_army varchar(20) DEFAULT NULL,
  weight int DEFAULT NULL,
  is_manager_focused varchar(20) DEFAULT NULL,
  is_big_v varchar(20) DEFAULT NULL,
  author_id varchar(100) DEFAULT NULL,
  author_nick varchar(255) DEFAULT NULL,
  is_main_post varchar(20) DEFAULT NULL,
  url varchar(500) DEFAULT NULL,
  view_count varchar(50) DEFAULT NULL,
  comment_count varchar(50) DEFAULT NULL,
  like_count varchar(50) DEFAULT NULL,
  share_count varchar(50) DEFAULT NULL,
  favorite_count varchar(50) DEFAULT NULL,
  order_id varchar(100) DEFAULT NULL,
  work_order_id varchar(100) DEFAULT NULL,
  quest_id varchar(100) DEFAULT NULL,
  quest_type varchar(100) DEFAULT NULL,
  quest_answer_score varchar(100) DEFAULT NULL,
  quest_business_type varchar(100) DEFAULT NULL,
  quest_business_scenario varchar(100) DEFAULT NULL,
  work_id varchar(100) DEFAULT NULL,
  done tinyint(1) DEFAULT NULL,
  model_type varchar(100) DEFAULT NULL,
  ds varchar(100) DEFAULT NULL,
  data_status varchar(100) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_mate_data_create_time (data_create_time),
  KEY idx_mate_data_data_id (data_id),
  KEY idx_mate_data_channel (channel_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS voc_ms_td.voc_anal_flow_mate_data_v LIKE voc_anal_flow_mate_data_v;

CREATE TABLE IF NOT EXISTS voc_ms_td.voc_sentiment_annotations_results_v (
  id varchar(100) NOT NULL,
  data_id varchar(100) DEFAULT NULL,
  title text DEFAULT NULL,
  original_text text DEFAULT NULL,
  original_text_scene text DEFAULT NULL,
  brand_code varchar(100) DEFAULT NULL,
  brand_name varchar(255) DEFAULT NULL,
  car_series_code varchar(100) DEFAULT NULL,
  car_series varchar(255) DEFAULT NULL,
  opinion text DEFAULT NULL,
  topic varchar(255) DEFAULT NULL,
  sentiment varchar(50) DEFAULT NULL,
  intention varchar(50) DEFAULT NULL,
  content_type varchar(100) DEFAULT NULL,
  usage_scenario_first varchar(100) DEFAULT NULL,
  usage_scenario_second varchar(100) DEFAULT NULL,
  channel_catagory varchar(100) DEFAULT NULL,
  second_channel varchar(100) DEFAULT NULL,
  channel_code varchar(100) DEFAULT NULL,
  channel_name varchar(255) DEFAULT NULL,
  publish_time datetime DEFAULT NULL,
  hot_words text DEFAULT NULL,
  keyword text DEFAULT NULL,
  user_journey1 varchar(100) DEFAULT NULL,
  user_journey2 varchar(100) DEFAULT NULL,
  user_journey3 varchar(100) DEFAULT NULL,
  standard_opinion_code varchar(100) DEFAULT NULL,
  dom_tag_first_code varchar(100) DEFAULT NULL,
  dom_tag_second_code varchar(100) DEFAULT NULL,
  dom_tag_three_code varchar(100) DEFAULT NULL,
  dom_tag_four_code varchar(100) DEFAULT NULL,
  tag_issue_severity varchar(50) DEFAULT NULL,
  tag_high_quality_voc_flag varchar(50) DEFAULT NULL,
  tag_need_forvclosed_loop varchar(50) DEFAULT NULL,
  is_wsater_army varchar(10) DEFAULT NULL,
  is_manager_focused varchar(10) DEFAULT NULL,
  is_big_v varchar(10) DEFAULT NULL,
  is_main_post varchar(10) DEFAULT NULL,
  one_id varchar(100) DEFAULT NULL,
  cust_name varchar(255) DEFAULT NULL,
  url varchar(500) DEFAULT NULL,
  work_order_id varchar(100) DEFAULT NULL,
  quest_id varchar(100) DEFAULT NULL,
  quest_type varchar(100) DEFAULT NULL,
  d2c_responsible_dept varchar(100) DEFAULT NULL,
  user_name varchar(255) DEFAULT NULL,
  abandon varchar(10) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_voc_results_publish_time (publish_time),
  KEY idx_voc_results_data_id (data_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
