package com.voc.service.analysis.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @创建者: liuhb
 * @创建时间: 2024/4/15 09:27
 * @描述:
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultDataParamModel extends Page implements Serializable {

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "客户ID")
    @NotBlank(message = "客户ID不能为空")
    private String clientId;

    @Schema(description = "workIdList")
    private List<String> workIdList;

    @Schema(description = "渠道ID")
    private List<String> channelIdList;

    @Schema(description = "数据状态")
    private List<String> dataStatus;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "情感")
    private List<String> sentiment;

    @Schema(description = "意图")
    private List<String> intention;

    @Schema(description = "品牌")
    private List<String> brandCode;

    @Schema(description = "车系")
    private List<String> carSeries;

    @Schema(description = "业务末级标签")
    private List<String> businessEndTag;


    @Schema(description = "质量末级标签")
    private List<String> qualityEndTag;
    @Schema(description = "模型类型 1 智谱AI离线 2智谱AI实时 3聚类大模型")
    private String modelType;
    @Schema(description = "数据类型")
    private List<String> metaDataType;

    @Schema(description = "本品车系")
    private List<String> ownCarSeries;

    @Schema(description = "竞品车系")
    private List<String> competitorsCarSeries;

    private List<String> mentionCarSeriesList;

    private String mentionCarSeriesString;

    @Schema(description = "车系合并集合")
    private Set<String> allCarSeriesList;

    @Schema(description = "时间")
    private String date;

    @Schema(description = "1本地上传 2系统集成")
    @Builder.Default
    private Integer showType =2;

}
