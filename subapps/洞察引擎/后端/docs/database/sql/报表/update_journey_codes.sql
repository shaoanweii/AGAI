-- 更新用户旅程标签编码脚本
-- 根据 user_journey_level1_v 表的标准编码更新相关数据表

-- 1. 更新 voc_sentiment_annotations_results_v 表中的旅程编码
UPDATE voc_sentiment_annotations_results_v
SET cj_tag_first_code = CASE 
    WHEN cj_tag_first = '认知' THEN 'cqca1004'
    WHEN cj_tag_first = '选择' THEN 'cqca1005' 
    WHEN cj_tag_first = '购买' THEN 'cqca1002'
    WHEN cj_tag_first = '使用' THEN 'cqca1001'
    WHEN cj_tag_first = '推荐' THEN 'cqca1003'
    ELSE cj_tag_first_code
END
WHERE cj_tag_first IN ('认知', '选择', '购买', '使用', '推荐');

-- 2. 更新旧编码到新编码的映射
UPDATE voc_sentiment_annotations_results_v
SET cj_tag_first_code = CASE 
    WHEN cj_tag_first_code = 'CJ001' THEN 'cqca1004'
    WHEN cj_tag_first_code = 'CJ002' THEN 'cqca1005'
    WHEN cj_tag_first_code = 'CJ003' THEN 'cqca1002'
    WHEN cj_tag_first_code = 'CJ004' THEN 'cqca1001'
    WHEN cj_tag_first_code = 'CJ005' THEN 'CJ005'  -- 维护保持不变
    WHEN cj_tag_first_code = 'CJ006' THEN 'cqca1003'
    ELSE cj_tag_first_code
END
WHERE cj_tag_first_code IN ('CJ001', 'CJ002', 'CJ003', 'CJ004', 'CJ006');

-- 3. 统一旅程名称（如果有不一致的情况）
UPDATE voc_sentiment_annotations_results_v
SET cj_tag_first = CASE 
    WHEN cj_tag_first_code = 'cqca1004' THEN '认知'
    WHEN cj_tag_first_code = 'cqca1005' THEN '选择'
    WHEN cj_tag_first_code = 'cqca1002' THEN '购买'
    WHEN cj_tag_first_code = 'cqca1001' THEN '使用'
    WHEN cj_tag_first_code = 'cqca1003' THEN '推荐'
    ELSE cj_tag_first
END
WHERE cj_tag_first_code IN ('cqca1004', 'cqca1005', 'cqca1002', 'cqca1001', 'cqca1003');

-- 4. 验证更新结果
SELECT 
    cj_tag_first,
    cj_tag_first_code,
    COUNT(*) as count
FROM voc_sentiment_annotations_results_v
WHERE cj_tag_first_code IN ('cqca1004', 'cqca1005', 'cqca1002', 'cqca1001', 'cqca1003')
GROUP BY cj_tag_first, cj_tag_first_code
ORDER BY cj_tag_first_code;