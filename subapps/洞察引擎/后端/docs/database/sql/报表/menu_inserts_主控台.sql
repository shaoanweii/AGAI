-- 主控台菜单插入（含 html_uri、small_image、big_image、description）
-- 说明：html_uri 依据中文名生成为 /scene/<pinyin-kebab>；big_image 指向 resource/static/场景分析_slices；description 采用你提供的文案，缺失项由我补充

INSERT INTO vdp_ms_td.sta_sys_menu_permission (id, parent_id, name, html_uri, sort_no, last_level, app_id, create_time, del_flag, small_image, big_image, description)
VALUES (REPLACE(UUID(),'-',''),'22a85c723de691f442bb8464d3691a62','综合分析','/scene/zonghe-fenxi',1,1,'report',NOW(),'0','resource/static/主控台_slices/综合分析.png','resource/static/场景分析_slices/综合分析@2x.png','整合销售、舆情等多维数据，通过AI建模挖掘市场趋势与业务增长机会');

INSERT INTO vdp_ms_td.sta_sys_menu_permission (id, parent_id, name, html_uri, sort_no, last_level, app_id, create_time, del_flag, small_image, big_image, description)
VALUES (REPLACE(UUID(),'-',''),'22a85c723de691f442bb8464d3691a62','竞品挖掘','/scene/jingpin-wajue',2,1,'report',NOW(),'0','resource/static/主控台_slices/竞品挖掘.png','resource/static/场景分析_slices/竞品分析@2x.png','从全网声量、产品配置与用户评价中识别潜在竞品与机会点，定位差距与突破方向');

INSERT INTO vdp_ms_td.sta_sys_menu_permission (id, parent_id, name, html_uri, sort_no, last_level, app_id, create_time, del_flag, small_image, big_image, description)
VALUES (REPLACE(UUID(),'-',''),'22a85c723de691f442bb8464d3691a62','竞品对比','/scene/jingpin-duibi',3,1,'report',NOW(),'0','resource/static/主控台_slices/竞品对比.png','resource/static/场景分析_slices/竞品分析@2x.png','对比核心配置、价格策略与口碑指标，量化差异并输出选型建议');

INSERT INTO vdp_ms_td.sta_sys_menu_permission (id, parent_id, name, html_uri, sort_no, last_level, app_id, create_time, del_flag, small_image, big_image, description)
VALUES (REPLACE(UUID(),'-',''),'22a85c723de691f442bb8464d3691a62','用户旅程','/scene/yonghu-lvcheng',4,1,'report',NOW(),'0','resource/static/主控台_slices/用户旅程.png','resource/static/场景分析_slices/用户旅程@2x.png','基于用户行为路径与触点反馈，优化购车-用车-售后全流程体验设计');

INSERT INTO vdp_ms_td.sta_sys_menu_permission (id, parent_id, name, html_uri, sort_no, last_level, app_id, create_time, del_flag, small_image, big_image, description)
VALUES (REPLACE(UUID(),'-',''),'22a85c723de691f442bb8464d3691a62','新车上市','/scene/xinche-shangshi',5,1,'report',NOW(),'0','resource/static/主控台_slices/新车上市.png','resource/static/场景分析_slices/新车上市@2x.png','从定价、渠道到传播策略的全周期管理，结合竞品分析快速抢占市场份额');

INSERT INTO vdp_ms_td.sta_sys_menu_permission (id, parent_id, name, html_uri, sort_no, last_level, app_id, create_time, del_flag, small_image, big_image, description)
VALUES (REPLACE(UUID(),'-',''),'22a85c723de691f442bb8464d3691a62','头部账号','/scene/toubu-zhanghao',6,1,'report',NOW(),'0','resource/static/主控台_slices/头部账号.png','resource/static/场景分析_slices/头部账号@2x.png','监测头部账号内容表现与用户互动，提炼选题趋势与投放策略指引');

INSERT INTO vdp_ms_td.sta_sys_menu_permission (id, parent_id, name, html_uri, sort_no, last_level, app_id, create_time, del_flag, small_image, big_image, description)
VALUES (REPLACE(UUID(),'-',''),'22a85c723de691f442bb8464d3691a62','产品质量','/scene/chanpin-zhiliang',7,1,'report',NOW(),'0','resource/static/主控台_slices/产品质量.png','resource/static/场景分析_slices/产品质量@2x.png','通过用户投诉、故障数据构建预警体系，驱动研发与生产环节的持续改进');

INSERT INTO vdp_ms_td.sta_sys_menu_permission (id, parent_id, name, html_uri, sort_no, last_level, app_id, create_time, del_flag, small_image, big_image, description)
VALUES (REPLACE(UUID(),'-',''),'22a85c723de691f442bb8464d3691a62','产品设计','/scene/chanpin-sheji',8,1,'report',NOW(),'0','resource/static/主控台_slices/产品设计.png',NULL,'结合用户反馈与竞品动向，洞察需求痛点，支持功能规划与体验优化');


# update sta_sys_menu_permission set small_image = '/'+small_image ,big_image = '/'+big_image where small_image is not null;

UPDATE sta_sys_menu_permission
SET
    small_image = CASE
                      WHEN small_image IS NOT NULL AND small_image NOT LIKE '/%' THEN CONCAT('/', small_image)
                      ELSE small_image
        END,
    big_image = CASE
                    WHEN big_image IS NOT NULL AND big_image <> '' AND big_image NOT LIKE '/%' THEN CONCAT('/', big_image)
                    ELSE big_image
        END
WHERE (small_image IS NOT NULL AND small_image NOT LIKE '/%')
   OR (big_image IS NOT NULL AND big_image <> '' AND big_image NOT LIKE '/%');