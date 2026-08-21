package com.voc.service.analysis.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AnalysisUserRiskModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String userId;

    private Long userType;

    private String focusProblem;

    private Long userLevel;

    private String userName;

    private Long negativeNum;

    private Long complainNum;

    private Long channelNum;

    private Long originalNum;

    private Double emotionNum;

    private String riskLevel;

    private Double riskIndex;

    private String statisticType;

    private LocalDateTime publishDate;

    private String dateYear;

    private String dateMonth;

    private String dateWeek;

    private String dateQuarter;

    private LocalDateTime createTime;

    private String brandName;

    private String channelId;

    private String labelType;
    private String labelTypeLevelFirst;
    private String labelTypeLevelSecond;
    private String labelTypeLevelThree;
    private String labelTypeLevelFour;
    private String labelTypeLevelFive;

    private String carSeriesName;

    private String keywords;

    private String newIdArray;

    private String cityCode;
}
