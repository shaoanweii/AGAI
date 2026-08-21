CREATE OR REPLACE  VIEW voc_voc2_computed_result_all_data_v
AS
select
    *
from  voc_computed_result_all_data_m_v




-- sta_dict_view
CREATE OR REPLACE
VIEW `voc_sta_dict_v` (`dict`) AS
SELECT
    parse_json('{"project_id": "4cb464bb8f604284dd83c92356fd62a4"
        ,"A": "二级", "B": "三级", "S": "一级"
        ,"biz": "BIZ","qy": "QY","prod": "PROD","srv": "SERVICE"
        ,"sentiment_types": ["中性","正面","负面"]
        ,"frist_label_sort": {
                                    "产品体验": 1,
                                    "智能化体验": 2,
                                    "品牌体验": 3,
                                    "销售服务": 4,
                                    "售后服务": 5,
                                    "权益服务": 6,
                                    "线上互动": 7,
                                     "整车质量": 9,
                                     "车联质量": 8
                                }
        ,"fault_level_types": ["高","较高","中","较低","低","无法评估"]}') AS `dict`;






-- rmt_ins_project_car_series_info_view
CREATE OR REPLACE VIEW `voc_ins_project_car_series_info_v` (`car_series`,
`priority_level`,
`haltSales`) SECURITY NONE AS
SELECT
    `f`.`car_series`,
    `f`.`priority_level`,
    `f`.`haltSales`
FROM
    (
        SELECT
            `f`.`car_series`,
            `f`.`priority_level`,
            `f`.`haltSales`,
            row_number() OVER (PARTITION BY `f`.`car_series`
    ORDER BY
        `f`.`haltSales` DESC ) AS `rn`
        FROM
            (
                SELECT
                    CAST((`j`.`value`->'carSeriesName') AS VARCHAR(65533)) AS `car_series`,
                    ifnull(CAST((`j`.`value`->'core') AS VARCHAR(65533)), 0) AS `priority_level`,
                    ifnull(CAST((`j`.`value`->'haltSales') AS VARCHAR(65533)), 0) AS `haltSales`
                FROM
                    voc_jdbc.vdp_ms_td.ins_project_details ,
                    LATERAL json_each(voc_jdbc.vdp_ms_td.ins_project_details.`car_series`) j(`key`, `value`)
                WHERE
                    `voc_jdbc`.`vdp_ms_td`.`ins_project_details`.`project_id` IN (((((((
                    SELECT
                    CAST((`VDP_RS_TD`.`voc_sta_dict_v`.`dict`->'project_id') AS VARCHAR(65533)) AS `CAST((dict->'project_id') AS string)`
                    FROM
                    `VDP_RS_TD`.`voc_sta_dict_v`
                    LIMIT 1)))))))) `f`
        GROUP BY
            `f`.`car_series`,
            `f`.`priority_level`,
            `f`.`haltSales`) `f`
WHERE
    `f`.`rn` = 1;





-- sta_user_label_scope_view
CREATE OR REPLACE
VIEW `voc_user_label_scope_perms_v` (`user_id`,
`brand_code`,
`brand`,
`label_type`,
`frist_code`,
`frist_label`,
`frist_label_sort`,
`second_code`,
`second_label`,
`three_code`,
`three_label`,
`four_code`,
`four_label`,
`leaf_code`,
`leaf_label`) AS
SELECT
    `v1`.`user_id`,
    `v1`.`brand_code`,
    `v3`.`name` AS `brand`,
    `v2`.`tag_type` AS `label_type`,
    `v2`.`frist_code`,
    `v2`.`frist_label`,
    ifnull(`v4`.`frist_label_sort`, 100) AS `frist_label_sort`,
    `v2`.`second_code`,
    `v2`.`second_label`,
    `v2`.`three_code`,
    `v2`.`three_label`,
    `v2`.`four_code`,
    `v2`.`four_label`,
    `v2`.`leaf_code`,
    `v2`.`leaf_label`
FROM
    (
        SELECT
            `f1`.`user_id`,
            `f2`.`tag_code`,
            `f2`.`brand_code`
        FROM
            voc_jdbc.vdp_ms_td.sta_sys_user_role AS `f1`
                LEFT OUTER JOIN (
                SELECT
                    `role_id`,
                    `tag_code`,
                    `brand_code`
                FROM
                    voc_jdbc.vdp_ms_td.sta_sys_role_business_tag
                UNION ALL
                SELECT
                    `role_id`,
                    `tag_code`,
                    `brand_code`
                FROM
                    voc_jdbc.vdp_ms_td.sta_sys_role_business_tag) `f2` ON
                `f1`.`role_id` = `f2`.`role_id`
        GROUP BY
            `f1`.`user_id`,
            `f2`.`tag_code`,
            `f2`.`brand_code`) `v1`
        LEFT OUTER JOIN (
        SELECT
            voc_sta_tag_level_values_v.`frist_code`,
            voc_sta_tag_level_values_v.`frist_label`,
            voc_sta_tag_level_values_v.`second_code`,
            voc_sta_tag_level_values_v.`second_label`,
            voc_sta_tag_level_values_v.`three_code`,
            voc_sta_tag_level_values_v.`three_label`,
            voc_sta_tag_level_values_v.`four_code`,
            voc_sta_tag_level_values_v.`four_label`,
            voc_sta_tag_level_values_v.`leaf_code`,
            voc_sta_tag_level_values_v.`leaf_label`,
            voc_sta_tag_level_values_v.`tag_type`
        FROM
            voc_sta_tag_level_values_v) `v2` ON
        `v1`.`tag_code` = `v2`.`second_code`
        LEFT OUTER JOIN (
        SELECT
            `code`,
            `name`
        FROM
            voc_jdbc.vdp_ms_be.ins_brand_info) `v3` ON
        `v1`.`brand_code` = `v3`.`code`
        LEFT OUTER JOIN (
        SELECT
            `t`.`key` AS `key_`,
            `t`.`value` AS `frist_label_sort`
        FROM
            ( SELECT  CAST((`dict`->'frist_label_sort') AS string)  as frist_label_sort
              FROM voc_sta_dict_v
            ) as `a` ,
            LATERAL json_each(`a`.`frist_label_sort`) t(`key`, `value`) ) `v4` ON
        `v2`.`frist_label` = `v4`.`key_`
WHERE
    `v2`.`frist_code` IS NOT NULL;







-- sta_tag_level_values_view
CREATE OR REPLACE
VIEW `voc_sta_tag_level_values_v` (`frist_code`,
`frist_label`,
`second_code`,
`second_label`,
`three_code`,
`three_label`,
`four_code`,
`four_label`,
`leaf_code`,
`leaf_label`,
`tag_type`) AS
SELECT
    `f1`.`frist_code`,
    `f1`.`frist_label`,
    `f2`.`second_code`,
    `f2`.`second_label`,
    `f3`.`three_code`,
    `f3`.`three_label`,
    `f4`.`four_code`,
    `f4`.`four_label`,
    if(`f5`.`leaf_code` IS NULL, `f4`.`four_code`, `f5`.`leaf_code`) AS `leaf_code`,
    if(`f5`.`leaf_code` IS NULL, `f4`.`four_label`, `f4`.`four_label`) AS `leaf_label`,
    `f1`.`tag_type`
FROM
    (
    SELECT
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_name` AS `frist_label`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_code` AS `frist_code`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_parent_id`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`id`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_type`
    FROM
        voc_jdbc.vdp_ms_td.ins_tag_client
    WHERE
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_parent_id` = '0') `f1`
LEFT OUTER JOIN (
    SELECT
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_name` AS `second_label`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_code` AS `second_code`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_parent_id`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`id`
    FROM
        voc_jdbc.vdp_ms_td.ins_tag_client) `f2` ON
    `f2`.`tag_parent_id` = `f1`.`id`
LEFT OUTER JOIN (
    SELECT
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_name` AS `three_label`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_code` AS `three_code`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_parent_id`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`id`
    FROM
        voc_jdbc.vdp_ms_td.ins_tag_client) `f3` ON
    `f3`.`tag_parent_id` = `f2`.`id`
LEFT OUTER JOIN (
    SELECT
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_name` AS `four_label`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_code` AS `four_code`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_parent_id`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`id`
    FROM
        voc_jdbc.vdp_ms_td.ins_tag_client) `f4` ON
    `f4`.`tag_parent_id` = `f3`.`id`
LEFT OUTER JOIN (
    SELECT
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_name` AS `leaf_label`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_code` AS `leaf_code`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_parent_id`,
        voc_jdbc.vdp_ms_td.ins_tag_client.`id`
    FROM
        voc_jdbc.vdp_ms_td.ins_tag_client
    WHERE
        voc_jdbc.vdp_ms_td.ins_tag_client.`tag_attribute` = 'FinalLabel') `f5` ON
    `f5`.`tag_parent_id` = `f4`.`id`;






-- rmt_ins_car_series_info_view
-- <sql id="common_find_car_series_name">
CREATE OR REPLACE
VIEW `voc_ins_car_series_perms_v` (`user_id`,
`brand`,
`car_series_code`,
`car_series`,
`priority_level`,
`haltSales`,
`car_level1`,
`car_level2`,
`energy_type1`,
`energy_type2`,
`img_key`) AS
SELECT
    `f1`.`user_id`,
    `f1`.`brand`,
    `f1`.`car_series_code`,
    `f1`.`car_series_name` AS `car_series`,
    ifnull(`f2`.`priority_level`, 0) AS `priority_level`,
    ifnull(`f2`.`haltSales`, 0) AS `haltSales`,
    `f1`.`car_level1`,
    `f1`.`car_level2`,
    `f1`.`energy_type1`,
    `f1`.`energy_type2`,
    `f1`.`img_key`
FROM
    (
        SELECT
            `f1`.`user_id`,
            `f4`.`name` AS `brand`,
            `f2`.`car_code` AS `car_series_code`,
            `f3`.`name` AS `car_series_name`,
            `f3`.`car_level1`,
            `f3`.`car_level2`,
            `f3`.`energy_type1`,
            `f3`.`energy_type2`,
            `f3`.`img_key`
        FROM
            (
                SELECT
                    `user_id`,
                    `role_id`
                FROM
                    voc_jdbc.vdp_ms_td.sta_sys_user_role) `f1`
                LEFT OUTER JOIN (
                SELECT
                    `role_id`,
                    `car_code`
                FROM
                    voc_jdbc.vdp_ms_td.sta_sys_role_series) `f2` ON
                `f1`.`role_id` = `f2`.`role_id`
                LEFT OUTER JOIN (
                SELECT
                    `brand_id`,
                    `code`,
                    `name`,
                    `car_level1`,
                    `car_level2`,
                    `energy_type1`,
                    `energy_type2`,
                    `img` AS `img_key`
                FROM
                    voc_jdbc.vdp_ms_be.ins_car_series_info
                WHERE
                    ((`name` IS NOT NULL)
                        AND (`code` IS NOT NULL))
                  AND (`del_flag` = 0)) `f3` ON
                `f2`.`car_code` = `f3`.`code`
                LEFT OUTER JOIN (
                SELECT
                    `id`,
                    `code`,
                    `name`
                FROM
                    voc_jdbc.vdp_ms_be.ins_brand_info) `f4` ON
                `f3`.`brand_id` = `f4`.`id`
        WHERE
            (`f2`.`car_code` IS NOT NULL)
          AND (`f3`.`name` IS NOT NULL)) `f1`
        LEFT OUTER JOIN (
        SELECT
            `car_series`,
            `priority_level`,
            `haltSales`
        FROM
            voc_ins_project_car_series_info_v) `f2` ON
        `f2`.`car_series` = `f1`.`car_series_name`;


-- sta_user_big_area_mapping_view

CREATE OR REPLACE
VIEW `view_user_big_area_mapping` (`user_id`,
`brand`,
`code`,
`city_name`,
`big_area_code`,
`big_area_sale`,
`province_name`,
`province_code`,
`small_area_code`,
`samll_area_sale`) AS

SELECT
    `f2`.`user_id`,
    `f1`.`code`,
    `f3`.`city_name`,
    `f3`.`big_area_code`,
    `f3`.`big_area_sale`,
    `f3`.`province_name`,
    `f3`.`province_code`,
    `f3`.`small_area_code`,
    `f3`.`samll_area_sale`
FROM
    (
    SELECT
        `role_id`,
        `code`,
        `brand_code`
    FROM
        voc_jdbc.vdp_ms_td.sta_sys_role_area) `f1`
LEFT OUTER JOIN (
    SELECT
        `user_id`,
        `role_id`
    FROM
        voc_jdbc.vdp_ms_td.sta_sys_user_role) `f2` ON
    `f1`.`role_id` = `f2`.`role_id`
LEFT OUTER JOIN (
    SELECT
        `city_code`,
        `city_name`,
        `big_area_code`,
        `big_area_sale`,
        `province_name`,
        `province_code`,
        `small_area_code`,
        `samll_area_sale`
    FROM
        mv_voc2_dealership_data_info) `f3` ON
    `f1`.`code` = `f3`.`small_area_code`
WHERE
    (`f2`.`user_id` IS NOT NULL)
    AND (`f3`.`big_area_code` IS NOT NULL)
GROUP BY
    `f2`.`user_id`,
    `f1`.`code`,
    `f3`.`city_name`,
    `f3`.`big_area_code`,
    `f3`.`big_area_sale`,
    `f3`.`province_name`,
    `f3`.`province_code`,
    `f3`.`small_area_code`,
    `f3`.`samll_area_sale`;



CREATE OR REPLACE VIEW `user_journey_level1_v` (`name`,
                                                `code`) SECURITY NONE AS
SELECT
    `voc_jdbc`.`voc_ms_be`.`ins_tag_info`.`tag_name` AS `name`,
    `voc_jdbc`.`voc_ms_be`.`ins_tag_info`.`tag_code` AS `code`
FROM
    `voc_jdbc`.`voc_ms_be`.`ins_tag_info`
WHERE
    (`voc_jdbc`.`voc_ms_be`.`ins_tag_info`.`tag_type` = 'userJourney')
  AND (`voc_jdbc`.`voc_ms_be`.`ins_tag_info`.`tag_parent_id` = '0')
ORDER BY
    `voc_jdbc`.`voc_ms_be`.`ins_tag_info`.`sort` ASC ;


CREATE OR REPLACE VIEW  brand_self_brand_v as
select  '长安汽车集团' as brand_name,"groupCode" as brand_code,1 as sort, "http://172.16.80.16:32215/files/resource/static/品牌/长安集团@2x.png" as imgUrl
union all
select
    *
from (
         select
             name as brand_name,
             code as brand_code,
             order_by as sort,
             img as imgUrl
         from  `voc_jdbc`.`voc_ms_be`.ins_brand_info
         where name in ('长安启源','长安凯程','长安引力','深蓝汽车','阿维塔')
           and competitive_type = 1
         order by order_by asc
     ) t


