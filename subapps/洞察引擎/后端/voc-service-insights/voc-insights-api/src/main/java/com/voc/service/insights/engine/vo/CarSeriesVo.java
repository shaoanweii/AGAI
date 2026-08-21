package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/25 上午9:03
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarSeriesVo {
    /**
     * 车系编码
     */
    private String carSeriesCode;
    /**
     * 车系名称
     */
    private String carSeriesName;
    /**
     * 是否核心
     */
    private String core;
    /**
     * 是否停售
     */
    private String haltSales;
    /**
     * 竞品车系
     */
    private List<CompetitiveCarSeriesVo> competitiveCarSeries;
}
