package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/12/11 下午7:08
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandTreeVo {
    /**
     * 品牌编码
     */
    @Schema(description = "品牌编码")
    private String brandCode;
    /**
     * 品牌名称
     */
    @Schema(description = "品牌名称")
    private String brandName;

    /**
     * 本品车系
     */
    private List<CarSeriesTreeVo> carSeries;
    /**
     * 竞品车系
     */
    private List<CarSeriesTreeVo> competitiveCarSeries;
    /**
     * 同时提及
     */
    private List<CarSeriesTreeVo> mentionCarSeriesList;
    /**
     * 融合树(本品车系+竞品车系+同时提及)
     */
    private List<CarSeriesTreeVo> integrationList;


}
