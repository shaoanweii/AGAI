package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaultsModel implements Serializable {

    private String sentence;

    private String journey;

    private String scenario;

    private String topic;

    private String opinion;

    private String subject;

    private String desc;

    private String levelOne;

    private String levelTwo;

    private String levelThree;

    private String levelFour;

    private String levelFive;

    private String similarTopic;

    private String levelOneCode;

    private String  levelTwoCode;

    private String levelThreeCode;

    private String levelFourCode;

    private String levelFiveCode;

    private String sentiment;

    private String intention;

    private String level;

    private String sentimentScore;

    private Object extFields;
}
