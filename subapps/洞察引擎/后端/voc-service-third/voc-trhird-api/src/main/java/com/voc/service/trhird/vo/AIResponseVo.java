package com.voc.service.trhird.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class AIResponseVo implements Serializable {

    @JsonProperty("result")
    private String result;
    @JsonProperty("model_name")
    private String modelName;
    @JsonProperty("unique_id")
    private String uniqueId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("error_message")
    private String errorMessage;
    @JsonProperty("cost")
    private Double cost;
    @JsonProperty("prompt_tokens")
    private Long promptTokens;
    @JsonProperty("completion_tokens")
    private Long completionTokens;
    @JsonProperty("total_tokens")
    private Long totalTokens;
}
