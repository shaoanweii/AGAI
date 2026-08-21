package com.voc.service.analysis.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequestDataModel implements Serializable {
    @Builder.Default
    private String enableModel = "yes";

    @Schema(description = "下标必传")
    private String id;

    @Schema(description = "必传工单/文本/对话")
    private String source;

    @Schema(description = "原文必传")
    private String content;

    private String modelType;

    private String title;

    private String carSeries;

    private String brand;

    private String taskId;

    private String workId;

    private String channelId;

    private String clientId;

    @Schema(description = "东风日产二级分类")
    private String categoryTwo;

    private String channelSubclass;
}
