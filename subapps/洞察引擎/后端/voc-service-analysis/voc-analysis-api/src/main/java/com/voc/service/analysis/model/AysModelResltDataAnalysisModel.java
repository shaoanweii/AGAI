package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AysModelResltDataAnalysisModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dataId;

    private String id;

    private String workId;
    private String clientId;
    private String channelId;
    private String contentType;
    private String inputDataId;
    private String originalId;

    private String sampleDataType;

    private String originalTextScene;

    private String brandCode;

    private String carSeriesCode;

    private String labelType;

    private String scenario;

    private String sentiment;

    private String intentionType;

    private String topic;

    private String opinion;

    private String subject;

    private String adType;

    private String faultLevel;

    private String description;

    private String sentimentScore;

    private String keywords;

    private LocalDateTime publishTime;
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();

    private Integer modelType;

    private Object extFields;

    private Object bizExtAttrs;

    private Object bizExtAttrs2;

    private Object bizExtAttrs3;

    private Object rawData;
    private Object custExtAttrs;
    private Object vhlExtAttrs;
    private Object dealerExtAttrs;
    private Object prdExtAttrs;

    private String oneId;

    @Builder.Default
    String done = "0";
    @Builder.Default
    String abandon = "0";
}
