package com.voc.service.analysis.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AnalysisQualityRiskModel implements Serializable {

    private String id;

    private String channelId;

    private String labelType;
    private String labelTypeLevelFirst;
    private String labelTypeLevelSecond;
    private String labelTypeLevelThree;
    private String labelTypeLevelFour;
    private String labelTypeLevelFive;


    private Long totalNum;

    private Double riskIndex;


    private String statisticType;

    private LocalDateTime publishDate;

    private String dateYear;

    private String dateQuarter;

    private String dateMonth;

    private String dateWeek;

    private LocalDateTime createTime;

    private Double pNum;

    private Double sNum;

    private Long channelNum;

    private Long voiceNum;

    private Long riskKeywordsNum;
    private Long maxTotalNum;

    private Long maxChannelNum;

    private Integer riskType;
    private Integer riskStatus;

    private String brandName;

    private String carSeriesName;

    private String keywords;

    private String faultLevel;

    private String startTime;

    private String endTime;

    private String newIdArray;

    private String cityCode;
}
