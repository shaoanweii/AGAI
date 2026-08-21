package com.voc.service.analysis.model;

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
@NoArgsConstructor
@AllArgsConstructor
public class JsonListModel implements Serializable {

    private String originalTextScene;

    private String brandCodeName = "";

    private String carSeriesName = "";

    private String businessLabelTypeLevelFirst = "";

    private String businessLabelTypeLevelSecond = "";

    private String businessLabelTypeLevelThree = "";

    private String businessLabelTypeLevelFour = "";

    private String qualityLabelTypeLevelFirst = "";

    private String qualityLabelTypeLevelSecond = "";

    private String qualityLabelTypeLevelThree = "";

    private String qualityLabelTypeLevelFour = "";

    private String scenario = "";

    private String sentiment = "";

    private String intentionType = "";

    private String topic = "";

    private String opinion = "";

    private String subject = "";

    private String faultLevel = "";

    private String description = "";

    private String sentimentScore = "";

    private String keywords = "";

}
