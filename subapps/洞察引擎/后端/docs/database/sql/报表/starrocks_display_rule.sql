-- StarRocks 显示规则表结构（可更新）
-- 建议在 starrocks_client_db 数据源中执行
CREATE TABLE IF NOT EXISTS ins_display_rule (
    id VARCHAR(64) NOT NULL COMMENT '主键',
    metric_code VARCHAR(64) NOT NULL COMMENT '指标编码，如 NEG_RATE',
    metric_name VARCHAR(128) NOT NULL COMMENT '指标名称',
    range_min DECIMAL(10,2) NOT NULL COMMENT '区间下限(含)',
    range_max DECIMAL(10,2) NOT NULL COMMENT '区间上限(含)',
    color_hex VARCHAR(16) NOT NULL COMMENT '颜色HEX，如#FF4D4F',
    emoji_key VARCHAR(64) NOT NULL COMMENT '表情/图标编码',
    sort_no INT DEFAULT '0' COMMENT '排序号',
    status TINYINT DEFAULT '1' COMMENT '1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=OLAP
PRIMARY KEY(`id`)
COMMENT "显示规则配置（可更新）"
DISTRIBUTED BY HASH(`id`) BUCKETS 8
PROPERTIES (
  "replication_num" = "1"
);

ALTER TABLE ins_display_rule RENAME report_display_rule;


-- 初始化数据：负面率的显示规则（5个区间）
-- emojiKey：1=愤怒 2=失望 3=一般/中立 4=满意 5=惊喜
INSERT INTO report_display_rule
(id, metric_code, metric_name, range_min, range_max, color_hex, emoji_key, sort_no, status, create_time, update_time)
VALUES (REPLACE(UUID(),'-',''), 'negativeRate', '负面率', 80, 100, '#FF4D4F', '1', 1, 1, NOW(), NOW());

INSERT INTO report_display_rule
(id, metric_code, metric_name, range_min, range_max, color_hex, emoji_key, sort_no, status, create_time, update_time)
VALUES (REPLACE(UUID(),'-',''), 'negativeRate', '负面率', 60, 80,  '#FA8C16', '2', 2, 1, NOW(), NOW());

INSERT INTO report_display_rule
(id, metric_code, metric_name, range_min, range_max, color_hex, emoji_key, sort_no, status, create_time, update_time)
VALUES (REPLACE(UUID(),'-',''), 'negativeRate', '负面率', 40, 60,  '#FAAD14', '3', 3, 1, NOW(), NOW());

INSERT INTO report_display_rule
(id, metric_code, metric_name, range_min, range_max, color_hex, emoji_key, sort_no, status, create_time, update_time)
VALUES (REPLACE(UUID(),'-',''), 'negativeRate', '负面率', 20, 40,  '#1890FF', '4', 4, 1, NOW(), NOW());

INSERT INTO report_display_rule
(id, metric_code, metric_name, range_min, range_max, color_hex, emoji_key, sort_no, status, create_time, update_time)
VALUES (REPLACE(UUID(),'-',''), 'negativeRate', '负面率', 0,  20,  '#13C2C2', '5', 5, 1, NOW(), NOW());
