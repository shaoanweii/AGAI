
-- =============================================
-- 表: sta_report_user_system_access_duration 索引优化方案
-- 说明: 根据 UserSystemAccessMapper.xml 和 actualCTE 中所有查询场景分析设计
-- 数据库: voc_ms_td
-- =============================================

-- 已有索引（建表时定义，此处仅注释说明，无需重复创建）:
--   PRIMARY KEY (id)
--   UNIQUE KEY uk_session_id (session_id)
--   KEY idx_user_id (user_id)
--   KEY idx_create_time (create_time)
--   KEY idx_status_create_time (status, create_time)

-- 1. 心跳 / 正常结束 / 精确查询复合索引
--    覆盖:
--      updateHeartbeat:           WHERE user_id = ? AND session_id = ?
--      updateNormalEnd:           WHERE user_id = ? AND session_id = ? AND status = 1
--      selectByUserIdAndSessionId: WHERE user_id = ? AND session_id = ?
--    说明: session_id 已有唯一索引，若业务只用 session_id 定位行则无需此索引；
--          此处补充复合索引以覆盖同时携带 user_id + session_id + status 的 UPDATE，
--          避免在唯一索引扫出行后再做回表过滤
CREATE INDEX idx_user_id_session_id_status
    ON sta_report_user_system_access_duration (user_id, session_id, status);

-- 2. 超时兜底扫描复合索引
--    覆盖:
--      updateTimeoutEnd / selectTimeoutSession:
--        WHERE status = 1
--          AND heartbeat_last_time < DATE_SUB(NOW(), INTERVAL ? MINUTE)   -- 有心跳分支
--          AND heartbeat_last_time IS NOT NULL
--    说明: status 作前缀可快速过滤未结束会话（status=1 在数据体量大时选择性高），
--          heartbeat_last_time 作第二列支持范围扫描，覆盖有心跳分支
CREATE INDEX idx_status_heartbeat
    ON sta_report_user_system_access_duration (status, heartbeat_last_time);

-- 3. 超时兜底扫描复合索引（无心跳分支）
--    覆盖:
--      updateTimeoutEnd / selectTimeoutSession:
--        WHERE status = 1
--          AND heartbeat_last_time IS NULL
--          AND access_start_time < DATE_SUB(NOW(), INTERVAL ? MINUTE)
--    说明: 与索引2联合覆盖 OR 的两个分支；
--          MySQL 对 OR 两侧分支分别用索引再 UNION（index_merge），两个索引各自命中一个分支
CREATE INDEX idx_status_access_start_time
    ON sta_report_user_system_access_duration (status, access_start_time);

-- 4. 报表聚合覆盖索引（actualCTE）
--    覆盖:
--      actualCTE: SELECT user_id, SUM(actual_duration)
--                 FROM sta_report_user_system_access_duration
--                 WHERE create_time >= ? AND create_time < DATE_ADD(?, INTERVAL 1 DAY)
--                 GROUP BY user_id
--    说明: 查询条件已改写为范围条件（消除 date() 函数包装），create_time 作前缀命中范围扫描，
--          user_id 覆盖 GROUP BY，actual_duration 覆盖 SUM 聚合，整体无需回表
CREATE INDEX idx_create_time_user_actual
    ON sta_report_user_system_access_duration (create_time, user_id, actual_duration);


-- =============================================
-- 表: ins_car_series_info 索引优化方案
-- 说明: 根据 InsCarSeriesInfoServiceImpl 中所有查询场景分析设计
-- =============================================
-- mysql voc_ms_be 数据库
-- 1. 核心过滤索引
--    覆盖: findAll() -> createQueryWrapper(del_flag=false, status='1')
--    场景: 全量数据加载，高频调用，selectivity 依赖 status 提升区分度
CREATE INDEX idx_status
    ON ins_car_series_info (status);

-- 2. 品牌编码 + 车系编码复合索引
--    覆盖:
--      a) codeGenerationRules: brand_code = ? ORDER BY code DESC
--      b) selectMultiInsCarSeriesInfoEntity: brand_code = ? / brand_code IN (...)
--    说明: brand_code 作为前缀，单独的 brand_code = ? 查询同样命中此索引
CREATE INDEX idx_brand_code_code
    ON ins_car_series_info (brand_code, code);

-- 3. 车系名称索引
--    覆盖: checkParameter 唯一性校验 name = ?
--    注意: del_flag 软删除可能导致同名多条记录，业务层控制唯一性，不用 UNIQUE INDEX
CREATE INDEX idx_name
    ON ins_car_series_info (name);

-- 4. 车系编码单列索引
--    覆盖: analyzeExcelData 中单独的 code = ? 更新查询
--    说明: idx_brand_code_code 中 code 为非前缀列，单独 code = ? 不命中，需独立索引
CREATE INDEX idx_code
    ON ins_car_series_info (code);

-- 5. 管理列表分页查询复合索引
--    覆盖: findInsCarSeriesInfoList 默认排序 competitive_type ASC, is_core DESC, code ASC
--
--    ★ MySQL 8.0+（推荐，支持混合方向索引）:
CREATE INDEX idx_status_list_sort
    ON ins_car_series_info (status, competitive_type ASC, is_core DESC, code ASC);
--
--    ★ MySQL 5.7（不支持混合方向，覆盖过滤，排序仍需 filesort，按需二选一）:
-- CREATE INDEX idx_status_list_sort
--     ON ins_car_series_info (status, competitive_type, is_core, code);


-- voc_ms_td.report_user_browse_record definition（MySQL 适配版）
CREATE TABLE `sta_report_user_browse_record` (
                                                 `id` varchar(64) NOT NULL COMMENT "主键",
                                                 `sound_id` varchar(64) DEFAULT NULL COMMENT "声音id",
                                                 `original_id` varchar(64) NOT NULL COMMENT "原文id",
                                                 `browse_user_id` varchar(64) NOT NULL COMMENT "浏览人id",
                                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT "创建时间",
                                                 `browse_duration` int(11) DEFAULT NULL COMMENT "浏览时长(秒)",
                                                 `sound_intention` varchar(64) DEFAULT NULL COMMENT "声音意图"
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户浏览记录表';

-- 补充 MySQL 核心索引（适配业务查询/写入场景）
ALTER TABLE `sta_report_user_browse_record`
    ADD PRIMARY KEY (`id`),
    ADD KEY `idx_browse_user_id` (`browse_user_id`) COMMENT '浏览人ID索引（按用户查浏览记录）',
    ADD KEY `idx_original_id` (`original_id`) COMMENT '原文ID索引（按原文查浏览量）',
    ADD KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引（按时间筛选浏览记录）',
    ADD KEY `idx_create_time_user_original_intention` (`create_time`, `browse_user_id`, `original_id`, `sound_intention`) COMMENT 'browseCTE 覆盖索引：create_time 范围扫描，覆盖 GROUP BY browse_user_id 及 COUNT(original_id)/sound_intention 聚合，无需回表',
    ADD KEY `idx_sound_id` (`sound_id`) COMMENT '声音ID索引（按声音查浏览记录）';



-- voc_ms_td.voc_report_user_system_access_duration definition（MySQL 适配版）
drop table sta_report_user_system_access_duration;
CREATE TABLE `sta_report_user_system_access_duration` (
                                                          `id` varchar(64) NOT NULL COMMENT "主键（建议用UUID生成）",
                                                          `user_id` varchar(64) DEFAULT NULL COMMENT "用户ID（关联用户表主键）",
                                                          `session_id` varchar(70) DEFAULT NULL COMMENT "前端会话唯一标识（防重复上报）",
                                                          `access_start_time` datetime DEFAULT NULL COMMENT "访问开始时间（格式：yyyy-MM-dd HH:mm:ss）",
                                                          `access_end_time` datetime DEFAULT NULL COMMENT "访问结束时间（前端上报/后端兜底判定）",
                                                          `actual_duration` int(11) DEFAULT 0 COMMENT "实际访问时长（秒），后端计算",
                                                          `heartbeat_last_time` datetime DEFAULT NULL COMMENT "最后心跳时间（兜底用）",
                                                          `device` varchar(100) DEFAULT NULL COMMENT "访问设备（pc/android/ios/h5）",
                                                          `browser` varchar(100) DEFAULT NULL COMMENT "浏览器类型（Chrome/Firefox/微信内置浏览器）",
                                                          `ip` varchar(50) DEFAULT NULL COMMENT "访问IP地址",
                                                          `status` tinyint(4) DEFAULT 1 COMMENT "会话状态：1-未结束 2-已结束 3-异常兜底",
                                                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT "记录创建时间",
                                                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT "记录更新时间"
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户系统访问时长表';

-- 补充 MySQL 最优索引（适配心跳更新、业务查询场景）
ALTER TABLE `sta_report_user_system_access_duration`
    ADD PRIMARY KEY (`id`),
    ADD UNIQUE KEY `uk_session_id` (`session_id`) COMMENT '会话ID唯一索引（加速心跳更新）',
    ADD KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引（按用户查询时长）',
    ADD KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引（按时间筛选）',
    ADD KEY `idx_status_create_time` (`status`, `create_time`) COMMENT '状态+创建时间索引（筛选未结束会话）';




INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871978329829377', '2013869162549399553', '系统配置', '系统配置', null, 23, 1, 'cqca_chengsheng', '2026-01-21 15:11:10', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871930112110594', '2013869162549399553', '日志查询', '日志查询', null, 22, 1, 'cqca_chengsheng', '2026-01-21 15:10:59', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871872931164161', '2013869162549399553', '报告管理', '报告管理', null, 21, 1, 'cqca_chengsheng', '2026-01-21 15:10:45', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871827561377793', '2013869162549399553', '场景管理', '场景管理', null, 20, 1, 'cqca_chengsheng', '2026-01-21 15:10:34', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871769839366145', '2013869162549399553', '下载管理', '下载管理', null, 19, 1, 'cqca_chengsheng', '2026-01-21 15:10:21', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871710599016449', '2013869162549399553', '角色管理', '角色管理', null, 18, 1, 'cqca_chengsheng', '2026-01-21 15:10:06', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871513479311362', '2013869162549399553', '账户管理', '账户管理', null, 17, 1, 'cqca_chengsheng', '2026-01-21 15:09:19', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871445829382146', '2013869162549399553', '原声查询', '原声查询', null, 16, 1, 'cqca_chengsheng', '2026-01-21 15:09:03', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871400287629314', '2013869162549399553', '根因分析', '根因分析', null, 15, 1, 'cqca_chengsheng', '2026-01-21 15:08:52', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871356033527810', '2013869162549399553', '智能问数', '智能问数', null, 14, 1, 'cqca_chengsheng', '2026-01-21 15:08:42', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871250857160705', '2013869162549399553', '单点事件', '单点事件', null, 13, 1, 'cqca_chengsheng', '2026-01-21 15:08:17', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871145435975682', '2013869162549399553', 'VOC移动端-事件任务', 'VOC移动端-事件任务', null, 12, 1, 'cqca_chengsheng', '2026-01-21 15:07:52', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013871052343398401', '2013869162549399553', 'VOC移动端-任务完成率', 'VOC移动端-任务完成率', null, 11, 1, 'cqca_chengsheng', '2026-01-21 15:07:29', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013870894415269890', '2013869162549399553', 'VOC移动端-首页', 'VOC移动端-首页', null, 10, 1, 'cqca_chengsheng', '2026-01-21 15:06:52', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013870815700766721', '2013869162549399553', '服务分析', '服务分析', null, 9, 1, 'cqca_chengsheng', '2026-01-21 15:06:33', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013870764010164226', '2013869162549399553', '产品分析', '产品分析', null, 8, 1, 'cqca_chengsheng', '2026-01-21 15:06:21', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013870709433880578', '2013869162549399553', '旅程分析', '旅程分析', null, 7, 1, 'cqca_chengsheng', '2026-01-21 15:06:08', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013870666589065218', '2013869162549399553', '本品分析', '本品分析', null, 6, 1, 'cqca_chengsheng', '2026-01-21 15:05:57', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013870241022398465', '2013869162549399553', '竞品对比', '竞品对比', null, 5, 1, 'cqca_chengsheng', '2026-01-21 15:04:16', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013869592599777282', '2013869162549399553', '集团分析', '集团分析', null, 4, 1, 'cqca_chengsheng', '2026-01-21 15:01:41', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013869557493452802', '2013869162549399553', '场景分析', '场景分析', null, 3, 1, 'cqca_chengsheng', '2026-01-21 15:01:33', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013869501306556417', '2013869162549399553', '领导总览', '领导总览', null, 2, 1, 'cqca_chengsheng', '2026-01-21 15:01:20', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013869409900089346', '2013869162549399553', 'VOC总览', 'VOC总览', null, 1, 1, 'cqca_chengsheng', '2026-01-21 15:00:58', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013863266453585921', '2013863185029562370', 'PC', 'PC', null, 1, 1, 'cqca_chengsheng', '2026-01-21 14:36:33', null, null);
INSERT INTO voc_ms_td.ins_dict_item (id, dict_id, item_text, item_value, description, sort_order, status, operator, create_time, item_text_en, item_key) VALUES ('2013863233909981186', '2013863185029562370', 'APP', 'APP', null, 1, 1, 'cqca_chengsheng', '2026-01-21 14:36:25', null, null);



INSERT INTO voc_ms_td.ins_dict (id, dict_name, dict_code, description, del_flag, operator, create_time, update_time, type) VALUES ('2013869162549399553', '日志菜单', 'voc_report_log_menu', null, 0, 'cqca_chengsheng', '2026-01-21 14:59:59', '2026-01-21 14:59:59', 0);
INSERT INTO voc_ms_td.ins_dict (id, dict_name, dict_code, description, del_flag, operator, create_time, update_time, type) VALUES ('2013863185029562370', '日志来源类型', 'voc_report_log_access_app', null, 0, 'cqca_chengsheng', '2026-01-21 14:36:14', '2026-01-21 14:36:14', 0);




alter table voc_ms_be.ins_record_logs
    add access_source_type varchar(200) null comment '访问来源（PC，APP）';





UPDATE voc_ms_td.ins_tag_client t SET t.sort = 3 WHERE t.tag_name = '购买' and t.tag_parent_id='0' and t.tag_type='JOUR';
UPDATE voc_ms_td.ins_tag_client t SET t.sort = 4 WHERE t.tag_name = '使用' and t.tag_parent_id='0' and t.tag_type='JOUR';
UPDATE voc_ms_td.ins_tag_client t SET t.sort = 1 WHERE t.tag_name = '认知' and t.tag_parent_id='0' and t.tag_type='JOUR';
UPDATE voc_ms_td.ins_tag_client t SET t.sort = 2 WHERE t.tag_name = '选择' and t.tag_parent_id='0' and t.tag_type='JOUR';
UPDATE voc_ms_td.ins_tag_client t SET t.sort = 5 WHERE t.tag_name = '推荐' and t.tag_parent_id='0' and t.tag_type='JOUR';




-- 更新 voc_ms_td 数据库中的图像路径
UPDATE voc_ms_td.sta_sys_menu_permission
SET
    big_image = REPLACE(big_image, '/resource/static/场景分析_slices/', ''),
    small_image = REPLACE(small_image, '/resource/static/主控台_slices/', '')
WHERE
    big_image LIKE '/resource/static/场景分析_slices/%'
   OR small_image LIKE '/resource/static/主控台_slices/%';

-- 更新 vdp_ms_td 数据库中的图像路径
UPDATE vdp_ms_td.sta_sys_menu_permission
SET
    big_image = REPLACE(big_image, '/resource/static/场景分析_slices/', ''),
    small_image = REPLACE(small_image, '/resource/static/场景分析_slices/', '')
WHERE
    big_image LIKE '/resource/static/场景分析_slices/%'
   OR small_image LIKE '/resource/static/场景分析_slices/%';




-- 为sta_sys_menu_permission表添加大图和说明字段
alter table sta_sys_menu_permission
    add big_image varchar(255) DEFAULT NULL COMMENT '大图';

alter table sta_sys_menu_permission
    add description varchar(500) DEFAULT NULL COMMENT '说明';

alter table sta_sys_menu_permission
    add small_image varchar(255) DEFAULT NULL COMMENT '小图';
http://172.16.80.16:32215/files/resource/static/品牌/长安集团@2x.png



UPDATE ins_brand_info
SET img = CONCAT('http://172.16.80.16:32215/files/resource/static/品牌/', name, '.png')
WHERE 1=1;




