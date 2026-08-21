package com.voc.service.analysis.risk.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "rp_user_risk_data")
public class UserRiskDataEntity {

    private String id;


    private String userId;


    private String userName;

    private String labelType;
    private String labelTypeLevelFirst;
    private String labelTypeLevelSecond;
    private String labelTypeLevelThree;
    private String labelTypeLevelFour;
    private String labelTypeLevelFive;

    private String focusProblem;

    private Long negativeNum;


    private Long complainNum;


    private Long channelNum;

    private Long voiceNum;


    private BigDecimal emotionNum;


    private String riskLevel;


    private String riskIndex;


    private String statisticType;


    private LocalDateTime publishDate;


    private String dateYear;


    private String dateMonth;


    private String dateWeek;


    private String dateQuarter;


    private LocalDateTime createTime;

    private String brandName;

    private String newIdArray;

    private String channelId;

    private String keywords;

    private String carSeriesName;
    private String cityCode;
}
