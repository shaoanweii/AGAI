package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/28 上午9:24
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsCommonDataBaseModel extends Page  implements Serializable {
    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private LocalDate startTime;
    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private LocalDate endTime;
    /**
     * 客户id
     */
    @Schema(description = "客户id")
    private String clientId;
    /**
     * 品牌
     */
    @Schema(description = "品牌")
    private List<String> brandCode;
    /**
     * 车系
     */
    @Schema(description = "车系")
    private List<String> carSeries;
    /**
     * 渠道
     */
    @Schema(description = "渠道")
    private List<String> channelIdList;
    /**
     * 数据类型
     */
    @Schema(description = "数据类型")
    private List<String> metaDataType;

    @Schema(description = "业务末级标签")
    private List<String> businessEndTag;

    @Schema(description = "质量末级标签")
    private List<String> qualityEndTag;
    /**
     * 情感
     */
    @Schema(description = "情感")
    private List<String> sentiment;
    /**
     * 意图
     */
    @Schema(description = "意图")
    private List<String> intention;

    @Schema(description = "模型类型 1 智谱AI离线 2智谱AI实时 3聚类大模型")
    @Builder.Default
    private String modelType = "1";

    @Schema(description = "关键词")
    private String keywords;
}
