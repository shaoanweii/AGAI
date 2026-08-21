ALTER TABLE `ins_car_series_info`
    ADD COLUMN `preheat_start_time` datetime NULL COMMENT '预热开始时间' AFTER `competitive_product`,
    ADD COLUMN `preheat_end_time` datetime NULL COMMENT '预热结束时间' AFTER `preheat_start_time`,
    ADD COLUMN `launch_start_time` datetime NULL COMMENT '上市开始时间' AFTER `preheat_end_time`,
    ADD COLUMN `launch_end_time` datetime NULL COMMENT '上市结束时间' AFTER `launch_start_time`,
    ADD COLUMN `stable_start_time` datetime NULL COMMENT '稳定开始时间' AFTER `launch_end_time`,
    ADD COLUMN `stable_end_time` datetime NULL COMMENT '稳定结束时间' AFTER `stable_start_time`;

-- 如需清理旧字段，请在业务数据完成迁移后再执行：
-- ALTER TABLE `ins_car_series_info`
--     DROP COLUMN `start_time`,
--     DROP COLUMN `end_time`;
