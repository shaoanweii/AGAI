package com.voc.service.analysis.model;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "rp_quality_risk_data")
public class QualityRiskDataModel {
    private String id; // 主键
    private String channelId; // 渠道id
    private String brandName; // 品牌code
    private String carSeriesName; // 车系
    private String labelType;
    private String labelTypeLevelFirst;
    private String labelTypeLevelSecond;
    private String labelTypeLevelThree;
    private String labelTypeLevelFour;
    private String labelTypeLevelFive;
    private Long totalNum; // 总提及量
    private Long negativeNum; // 负面量
    private Long channelNum;
    private Long userNum;
    private Long riskKeywordsNum; // 风险关键词量
    private String riskIndex; // 与riskType有关，为1时是风险程度值G，为0时是综合指数
    private String riskLevel;
    private String projectId;
    private String statisticType; // 周期类型
    private LocalDateTime publishDate; // 发生时间
    private String dateYear; // 年
    private String dateDay; // 天
    private String dateMonth; // 月
    private String dateWeek; // 周
    private LocalDateTime createTime; // 创建时间
    private String sNum; // 情感指数
    private String pNum; // 风险词指数
    private String dateQuarter; // 季
    private String keywords;
    private String newIdArray;
    private String cityCode;

}
