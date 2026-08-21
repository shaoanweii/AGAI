package com.voc.service.analysis.largeModel.vo;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class Dimension implements Serializable {

    private String segment;
    private String brand;
    private String series;
    private String sentiment;
    private String sub_sentiment;
    private String usage_scenario;
    private String intent;
    private String aspect;
    private String description;
    private String car_level1;
    private String car_level2;
    private NormalizedOpinion normalized_opinion;
    private StandardKeyword standard_keyword;
//    private String aDType;
//    private String emotionalLevel;
}
