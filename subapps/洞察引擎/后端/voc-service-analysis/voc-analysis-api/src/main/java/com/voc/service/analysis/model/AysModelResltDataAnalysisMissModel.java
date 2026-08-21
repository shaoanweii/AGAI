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
public class AysModelResltDataAnalysisMissModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dataId;
    private String id;

    private String workId;
    private String clientId;
    private String channelId;
    private String contentType;
    private String inputDataId;

    private Object rawData;

    private String brandCode;

    private LocalDateTime publishTime;

    private String carSeriesCode;

    private String opinion;

    private String opinionSentiment;

    private String subject;

    private String description;

    private String carBodyLabel;

    private String viewLabel;

    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();

    Integer modelType;

    Object extFields;

    Object bizExtAttrs;

    Object bizExtAttrs2;

    Object bizExtAttrs3;

    String oneId;

    @Builder.Default
    String done = "1";

}
