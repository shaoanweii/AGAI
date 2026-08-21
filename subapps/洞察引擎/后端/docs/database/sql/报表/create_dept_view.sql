-- 创建部门层级视图（5级）
drop view if exists sta_all_level_depart_10_v;
CREATE OR REPLACE VIEW sta_all_level_depart_10_v AS
WITH RECURSIVE dept_tree AS (
    SELECT
        depart_id, name, code, parent_id, 1 AS level,
        name AS l1_name, code AS l1_code,
        CAST(NULL AS CHAR(100)) AS l2_name, CAST(NULL AS CHAR(100)) AS l2_code,
        CAST(NULL AS CHAR(100)) AS l3_name, CAST(NULL AS CHAR(100)) AS l3_code,
        CAST(NULL AS CHAR(100)) AS l4_name, CAST(NULL AS CHAR(100)) AS l4_code,
        CAST(NULL AS CHAR(100)) AS l5_name, CAST(NULL AS CHAR(100)) AS l5_code,
        CAST(NULL AS CHAR(100)) AS l6_name, CAST(NULL AS CHAR(100)) AS l6_code,
        CAST(NULL AS CHAR(100)) AS l7_name, CAST(NULL AS CHAR(100)) AS l7_code,
        CAST(NULL AS CHAR(100)) AS l8_name, CAST(NULL AS CHAR(100)) AS l8_code,
        CAST(NULL AS CHAR(100)) AS l9_name, CAST(NULL AS CHAR(100)) AS l9_code,
        CAST(NULL AS CHAR(100)) AS l10_name, CAST(NULL AS CHAR(100)) AS l10_code
    FROM sta_sys_depart
    WHERE parent_id = '-1'

    UNION ALL

    SELECT
        d.depart_id, d.name, d.code, d.parent_id, dt.level + 1,
        dt.l1_name, dt.l1_code,
        CASE WHEN dt.level = 1 THEN d.name ELSE dt.l2_name END,
        CASE WHEN dt.level = 1 THEN d.code ELSE dt.l2_code END,
        CASE WHEN dt.level = 2 THEN d.name ELSE dt.l3_name END,
        CASE WHEN dt.level = 2 THEN d.code ELSE dt.l3_code END,
        CASE WHEN dt.level = 3 THEN d.name ELSE dt.l4_name END,
        CASE WHEN dt.level = 3 THEN d.code ELSE dt.l4_code END,
        CASE WHEN dt.level = 4 THEN d.name ELSE dt.l5_name END,
        CASE WHEN dt.level = 4 THEN d.code ELSE dt.l5_code END,
        CASE WHEN dt.level = 5 THEN d.name ELSE dt.l6_name END,
        CASE WHEN dt.level = 5 THEN d.code ELSE dt.l6_code END,
        CASE WHEN dt.level = 6 THEN d.name ELSE dt.l7_name END,
        CASE WHEN dt.level = 6 THEN d.code ELSE dt.l7_code END,
        CASE WHEN dt.level = 7 THEN d.name ELSE dt.l8_name END,
        CASE WHEN dt.level = 7 THEN d.code ELSE dt.l8_code END,
        CASE WHEN dt.level = 8 THEN d.name ELSE dt.l9_name END,
        CASE WHEN dt.level = 8 THEN d.code ELSE dt.l9_code END,
        CASE WHEN dt.level = 9 THEN d.name ELSE dt.l10_name END,
        CASE WHEN dt.level = 9 THEN d.code ELSE dt.l10_code END
    FROM sta_sys_depart d
             INNER JOIN dept_tree dt ON d.parent_id = dt.depart_id
    WHERE d.parent_id != '-1'
)
SELECT
    depart_id, name, code, level,
    l1_name, l1_code,
    l2_name, l2_code,
    l3_name, l3_code,
    l4_name, l4_code,
    l5_name, l5_code,
    l6_name, l6_code,
    l7_name, l7_code,
    l8_name, l8_code,
    l9_name, l9_code,
    l10_name, l10_code
FROM dept_tree