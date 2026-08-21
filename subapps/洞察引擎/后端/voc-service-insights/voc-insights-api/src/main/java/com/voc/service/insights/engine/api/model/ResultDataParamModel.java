package com.voc.service.insights.engine.api.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
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
    @Builder.Default
    private Set<String> workIdList = new HashSet<>();

    @Schema(description = "渠道ID")
    @Builder.Default
    private List<String> channelIdList = new ArrayList<>();

    @Schema(description = "数据状态")
    @Builder.Default
    private List<String> dataStatus = new ArrayList<>();

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "情感")
    @Builder.Default
    private List<String> sentiment = new ArrayList<>();

    @Schema(description = "意图")
    @Builder.Default
    private List<String> intention = new ArrayList<>();

    @Schema(description = "品牌")
    @Builder.Default
    private List<String> brandCode = new ArrayList<>();

    @Schema(description = "车系")
    @Builder.Default
    private List<String> carSeries= new ArrayList<>();

    @Schema(description = "业务末级标签")
    @Builder.Default
    private List<String> businessEndTag= new ArrayList<>();


    @Schema(description = "质量末级标签")
    @Builder.Default
    private List<String> qualityEndTag= new ArrayList<>();
    @Schema(description = "模型类型 1 智谱AI离线 2智谱AI实时 3聚类大模型")
    private String modelType;
    @Schema(description = "数据类型")
    @Builder.Default
    private List<String> metaDataType= new ArrayList<>();

    @Schema(description = "本品车系")
    @Builder.Default
    private List<String> ownCarSeries= new ArrayList<>();

    @Schema(description = "竞品车系")
    @Builder.Default
    private List<String> competitorsCarSeries= new ArrayList<>();

    @Builder.Default
    private List<String> mentionCarSeriesList= new ArrayList<>();

    private String mentionCarSeriesString;

    @Schema(description = "车系合并集合")
    @Builder.Default
    private Set<String> allCarSeriesList= new HashSet<>();

    @Schema(description = "时间")
    private String date;

    @Schema(description = "1本地上传 2系统集成")
    @Builder.Default
    private String showType="2";

    private String taskId;

    private String fileName;

}
