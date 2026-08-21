package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsLabelCorrectionRecordQueryModel extends Page implements Serializable {

    @Schema(description = "开始时间")
    @Builder.Default
    private String startTime = "";

    @Schema(description = "结束时间")
    @Builder.Default
    private String endTime = "";

    @Schema(description = "客户ID")
    @NotBlank(message = "客户ID不能为空")
    private String clientId;

    @Schema(description = "渠道ID")
    @Builder.Default
    private List<String> channelIdList = new ArrayList<>();

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

    @Schema(description = "标签集合")
    @Builder.Default
    private List<String> tagList = new ArrayList<>();

    @Schema(description = "审核状态")
    @Builder.Default
    private List<String> auditStatus = new ArrayList<>();

    private String newId;

}
