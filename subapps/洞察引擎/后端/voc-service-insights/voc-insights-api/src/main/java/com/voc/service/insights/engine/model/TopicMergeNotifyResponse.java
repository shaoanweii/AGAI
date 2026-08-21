package com.voc.service.insights.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TopicMergeNotifyResponse implements Serializable {
    private String code;
    private String message;
    private Boolean success;
    private ResultData result;

    @Data
    public static class ResultData implements Serializable {
        @JsonProperty("transferred_count")
        private Integer transferredCount;
        @JsonProperty("failed_count")
        private Integer failedCount;
        @JsonProperty("failed_items")
        private List<Object> failedItems;
    }
}
