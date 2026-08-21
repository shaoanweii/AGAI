package com.voc.service.trhird.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class AIRequestModel  implements Serializable {

    @JsonProperty("text")
    private String text;
    @JsonProperty("model_name")
    private String model_name;
    @JsonProperty("unique_id")
    private String unique_id;


}
