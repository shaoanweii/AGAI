package com.voc.service.insights.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/25 上午10:37
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitiveCarSeriesEntity {
    /**
     * 竞品品牌编码
     */
    private String competitiveBrandCode;
    /**
     * 竞品品牌名称
     */
    private String competitiveBrandName;
    /**
     * 竞品车系编码
     */
    private String competitiveCarSeriesCode;
    /**
     * 竞品车系名称
     */
    private String competitiveCarSeriesName;
    /**
     * 是否核心竞品
     */
    private String core;
}
