package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/14 上午9:38
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandModel {
    private String id;
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
     * 应用标签
     */
    @Schema(description = "应用标签")
    private List<String> tags;
    /**
     * 数据源
     */
    @Schema(description = "数据源")
    private List<String> dataSource;

    /**
     * 渠道
     */
    @Schema(description = "渠道")
    private List<String> channel;
    /**
     * 区域
     */
    @Schema(description = "区域")
    private List<String> region;

    /**
     * 车系
     */
    private List<CarSeriesModel> carSeries;
    /**
     * 竞品
     */
    private List<CompetitiveProductModel> competitiveProduct;

    /**
     * 风险预警配置
     */
    @Schema(description = "风险预警配置")
    private List<InsRiskEarlyWarning> riskEarlyWarning;
}
