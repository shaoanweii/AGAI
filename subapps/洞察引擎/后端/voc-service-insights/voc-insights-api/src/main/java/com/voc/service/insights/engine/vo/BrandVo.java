package com.voc.service.insights.engine.vo;

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
public class BrandVo {
    /**
     * id
     */
    private String id;
    /**
     * 项目id
     */
    private String projectId;

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
    @Schema(description = "渠道ID")
    private List<String> channel;

    /**
     * 渠道编码
     */
    @Schema(description = "渠道编码")
    private List<String> channelCode;
    /**
     * 渠道树
     */
    @Schema(description = "渠道树")
    private List<ChannelInfoVo> channelTree;
    /**
     * 区域
     */
    @Schema(description = "区域")
    private List<String> region;

    /**
     * 车系
     */
    private List<CarSeriesVo> carSeries;
    /**
     * 竞品
     */
    private List<CompetitiveProductVo> competitiveProduct;

    /**
     * 风险预警配置
     */
    @Schema(description = "风险预警配置")
    private List<InsRiskEarlyWarningVo> riskEarlyWarning;

    List<TagLibCategoryVo> BIZ;
    List<TagLibCategoryVo> QY;
    List<TagLibCategoryVo> SIC;
    List<RegionConfigVo> regionTree;
}
