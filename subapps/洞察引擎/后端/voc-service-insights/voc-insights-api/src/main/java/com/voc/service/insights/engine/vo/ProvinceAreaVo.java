package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 11:09
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceAreaVo  implements Serializable {

    private String bigAreaSale;
    /**
     * 一级大区编码
     */
    private String bigAreaSaleCode;

    /**
     * 二级大区名称
     */
    private String smallAreaSale;
    /**
     * 二级大区编码
     */
    private String smallAreaSaleCode;
    /**
     * 省份名称
     */
    private String provinceName;
    /**
     * 省份编码
     */
    private String provinceCode;
    /**
     * 城市名称
     */
    private String areaName;
    /**
     * 城市编码
     */
    private String areaCode;
}
