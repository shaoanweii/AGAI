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
public class AysModelResltDataAnalysisValidModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String newId;
    private String id;

    private String workId;
    private String oldWorkId;
    private String clientId;
    private String channelId;
    private String contentType;
    private String inputDataId;
    private String originalId;

    private String sampleDataType;

    private String originalTextScene;

    private String brandCodeName;

    private String carSeriesName;

    private String labelType;

    private String labelTypeLevelFirst;

    private String labelTypeLevelSecond;

    private String labelTypeLevelThree;

    private String labelTypeLevelFour;

    private String labelTypeLevelFive;

    private String scenario;

    private String sentiment;

    private String intentionType;

    private String topic;

    private String opinion;

    private String subject;

    private String faultLevel;

    private String description;

    @Builder.Default
    private String sentimentScore = "";

    @Builder.Default
    private String keywords = "";

    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    Integer modelType;

    Object extFields;

    @Builder.Default
    String done = "0";

    String hitRules;

    String hitValidRules;


}
