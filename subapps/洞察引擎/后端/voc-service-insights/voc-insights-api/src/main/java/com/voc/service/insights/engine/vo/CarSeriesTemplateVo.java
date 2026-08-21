package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建者: fanrong
 * @创建时间: 2024/12/24 上午11:50
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarSeriesTemplateVo {
    private String code;
    /**
     * 车系名称
     */
//    private String carSeries;
    /**
     * 品牌名称
     */
//    private String brandName;
    /**
     * 竞品名称
     */
//    private String competitive;
    /**
     * 车系排除词
     */
//    private String carSeriesExclusionWords;
    /**
     * 车辆类型
     */
//    private String carType;
    /**
     * 车系级别1
     */
//    private String carTypeLevel;
    /**
     * 能源类型1
     */
//    private String energyType1;
    /**
     * 品牌别名
     */
//    private String brandAlias;
    /**
     * 厂商
     */
    private String factory;

    private String carLevel1;
    /**
     * 国家
     */
//    private String country;
    /**
     * 别名
     */
//    private String alias1;
    /**
     * 别名2
     */
//    private String alias2;
    /**
     * 别名3
     */
//    private String alias3;
    /**
     * 车辆级别2
     */
//    private String carTypeLevel2;
    /**
     * 车系图片
     */
//    private String carSeriesImage;
    /**
     * 品牌图片
     */
//    private String brandImage;

    /**
     * 是否本竞品
     */
//    private Integer competitiveType;
}
