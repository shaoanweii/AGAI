package com.voc.service.analysis.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AnalysisEmotionRiskModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String labelType;
    private String labelTypeLevelFirst;
    private String labelTypeLevelSecond;
    private String labelTypeLevelThree;
    private String labelTypeLevelFour;
    private String labelTypeLevelFive;

    private Long totalNum;

    private Long negativeNum;

    private Long complainNum;

    private Long channelNum;

    private Long voiceNum;

    private Long riskKeywordsNum; // 风险关键词量

    private Double riskIndex;

    private String statisticType;

    private LocalDateTime publishDate;

    private String dateYear;

    private String dateQuarter;

    private String dateMonth;

    private String dateWeek;

    private LocalDateTime createTime;

    private String id;

    private String channelId;

    private Double sNum;
    private Double rNum;

    private Long maxNegativeNum;

    private Long maxComplainNum;

    private String dateWeekYear;

    private Long positiveNum;

    private String isRisk;

    private Integer riskStatus;

    private Integer riskType;

    private String auditMessage;

    private String brandName;

    private String carSeriesName;

    private String keywords;

    private String faultLevel;

    private String startTime;

    private String endTime;

    private String newIdArray;

    private String cityCode;

}
