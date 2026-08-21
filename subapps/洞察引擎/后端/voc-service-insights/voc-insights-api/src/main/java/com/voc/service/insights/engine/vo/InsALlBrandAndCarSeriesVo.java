package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/27 下午4:57
 * @描述:
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsALlBrandAndCarSeriesVo {
    //品牌id
    private String brandId;
    //品牌编码
    private String brandCode;
    //品牌名称
    private String brandName;
    //品牌别名
    private String brandAlias;
    //品牌排除词
    private String brandExclusionWords;
    //车系编码
    private String carSeriesCode;
    //车系名称
    private String carSeriesName;
    //车系别名
    private String carSeriesAlias;
    //车系排除词
    private String carSeriesExclusionWords;
    //车辆级别1
    private String carLevel1;
    //车辆级别2
    private String carLevel2;
    //能源类型1
    private String energyType1;
    //能源类型2
    private String energyType2;
}
