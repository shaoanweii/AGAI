package com.voc.service.insights.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class TopicMergeNotifyPayload implements Serializable {
    @JsonProperty("source_standard_opinion_id")
    private String sourceStandardOpinionId;
    @JsonProperty("source_standard_opinion")
    private String sourceStandardOpinion;
    @JsonProperty("target_standard_opinion_id")
    private String targetStandardOpinionId;
    @JsonProperty("target_standard_opinion")
    private String targetStandardOpinion;
    @JsonProperty("operator")
    private String operator;
}
