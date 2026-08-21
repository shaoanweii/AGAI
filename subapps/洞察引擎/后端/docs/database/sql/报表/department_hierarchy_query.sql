-- 查询一到n级部门层级结构
-- 数据源: qcca_dev_mysql
-- 架构: voc_ms_td
-- 表: sta_sys_depart

WITH RECURSIVE dept_tree AS (
    -- 锚点：查找所有一级部门（parent_id = '-1'）
    SELECT 
        depart_id,
        name,
        code,
        parent_id,
        parent_code,
        1 AS level,
        CAST(name AS CHAR(1000)) AS path_name,
        CAST(code AS CHAR(1000)) AS path_code,
        name AS level1_name,
        code AS level1_code,
        CAST(NULL AS CHAR(100)) AS level2_name,
        CAST(NULL AS CHAR(100)) AS level2_code,
        CAST(NULL AS CHAR(100)) AS level3_name,
        CAST(NULL AS CHAR(100)) AS level3_code,
        CAST(NULL AS CHAR(100)) AS level4_name,
        CAST(NULL AS CHAR(100)) AS level4_code,
        CAST(NULL AS CHAR(100)) AS level5_name,
        CAST(NULL AS CHAR(100)) AS level5_code
    FROM sta_sys_depart
    WHERE parent_id = '-1'
    
    UNION ALL
    
    -- 递归：查找子部门
    SELECT 
        d.depart_id,
        d.name,
        d.code,
        d.parent_id,
        d.parent_code,
        dt.level + 1,
        CONCAT(dt.path_name, ' > ', d.name),
        CONCAT(dt.path_code, ' > ', d.code),
        dt.level1_name,
        dt.level1_code,
        CASE WHEN dt.level = 1 THEN d.name ELSE dt.level2_name END,
        CASE WHEN dt.level = 1 THEN d.code ELSE dt.level2_code END,
        CASE WHEN dt.level = 2 THEN d.name ELSE dt.level3_name END,
        CASE WHEN dt.level = 2 THEN d.code ELSE dt.level3_code END,
        CASE WHEN dt.level = 3 THEN d.name ELSE dt.level4_name END,
        CASE WHEN dt.level = 3 THEN d.code ELSE dt.level4_code END,
        CASE WHEN dt.level = 4 THEN d.name ELSE dt.level5_name END,
        CASE WHEN dt.level = 4 THEN d.code ELSE dt.level5_code END
    FROM sta_sys_depart d
    INNER JOIN dept_tree dt ON d.parent_id = dt.depart_id
    WHERE d.parent_id != '-1'
)
SELECT 
    depart_id AS 部门ID,
    name AS 当前部门名称,
    code AS 当前部门编码,
    level AS 层级,
    path_name AS 完整路径,
    level1_name AS 一级部门,
    level1_code AS 一级部门code,
    level2_name AS 二级部门,
    level2_code AS 二级部门code,
    level3_name AS 三级部门,
    level3_code AS 三级部门code,
    level4_name AS 四级部门,
    level4_code AS 四级部门code,
    level5_name AS 五级部门,
    level5_code AS 五级部门code
FROM dept_tree
ORDER BY level, level1_code, level2_code, level3_code, level4_code, level5_code;


-- ============================================
-- 如果需要更多层级（6-10级），使用以下查询
-- ============================================

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
    depart_id AS 部门ID, name AS 当前部门, code AS 当前编码, level AS 层级,
    l1_name AS 一级部门, l1_code AS 一级部门code,
    l2_name AS 二级部门, l2_code AS 二级部门code,
    l3_name AS 三级部门, l3_code AS 三级部门code,
    l4_name AS 四级部门, l4_code AS 四级部门code,
    l5_name AS 五级部门, l5_code AS 五级部门code,
    l6_name AS 六级部门, l6_code AS 六级部门code,
    l7_name AS 七级部门, l7_code AS 七级部门code,
    l8_name AS 八级部门, l8_code AS 八级部门code,
    l9_name AS 九级部门, l9_code AS 九级部门code,
    l10_name AS 十级部门, l10_code AS 十级部门code
FROM dept_tree
ORDER BY level, l1_code, l2_code, l3_code, l4_code, l5_code;
