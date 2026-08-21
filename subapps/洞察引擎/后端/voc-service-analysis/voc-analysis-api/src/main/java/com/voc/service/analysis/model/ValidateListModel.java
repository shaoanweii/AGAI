package com.voc.service.analysis.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author liuhb
 * @version 1.0.0
 * @ClassName msg_event_data
 * @createTime 2024年04月08日 12:00
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateListModel implements Serializable {

    @Schema(description = "原文")
    private String originalText;

    @Schema(description = "原文片段")
    private String originalTextScene;

    @Schema(description = "车系名称")
    private String carSeriesName;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "是否命中 0没命中 1命中")
    private String hitState;

    @Schema(description = "是否命中 0没命中 1命中")
    private String hitStateStr;

    @Schema(description = "数据对比 0一致 1不同")
    private String dataCompare;

    @Schema(description = "数据对比 0一致 1不同")
    private String dataCompareStr;

    @Schema(description = "原处理结果")
    private String originalProcessingResult;

    @Schema(description = "新处理结果")
    private String newProcessingResult;

}
