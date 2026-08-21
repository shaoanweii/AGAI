package com.voc.service.trhird.model;


import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DownloadAIFileResultModel implements Serializable {

    private ResponseAiModel response;

    @SerializedName("custom_id")
    private String customId;

    private String id;

}
