package com.voc.service.analysis.model;

import lombok.*;

import java.io.Serializable;

/**
 * @author liuhb
 * @version 1.0.0
 * @ClassName msg_event_data
 * @createTime 2024年04月15日 12:00
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AysProcessValidDataModel implements Serializable {

    private String newOriginalTextScene;

    private String newBrandCodeName;

    private String newCarSeriesName;

    private String channelId;

    private String newLabelTypeLevelFirst;
    private String newLabelTypeLevelSecond;
    private String newLabelTypeLevelThree;
    private String newLabelTypeLevelFour;
    private String newScenario;
    private String newSentiment;
    private String newIntentionType;
    private String newOpinion;
    private String newTopicProportion;
    private String newSubject;
    private String newFaultLevel;
    private String newDescription;
    private String newSentimentScore;
    private String newKeywords;
    private String newHitRules;

    private String oldOriginalTextScene;

    private String oldBrandCodeName;

    private String oldCarSeriesName;
    private String oldLabelTypeLevelFirst;
    private String oldLabelTypeLevelSecond;
    private String oldLabelTypeLevelThree;
    private String oldLabelTypeLevelFour;
    private String oldScenario;
    private String oldSentiment;
    private String oldIntentionType;
    private String oldOpinion;
    private String oldTopicProportion;
    private String oldSubject;
    private String oldFaultLevel;
    private String oldDescription;
    private String oldSentimentScore;
    private String oldKeywords;

    private Integer abandon;


    private String oldHitRules;

    private String dataStr;

}
