package com.voc.service.analysis.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllTypesRiskDataModel {
    private String id; // 主键
    private String riskId; // 风险点Id
    private String risk; // 风险code或用户Id
    private String brandCodeName; // 品牌名称
    private String carSeriesName; // 车系名称
    private String riskType; // 风险类型
    private String riskName; // 风险问题
    private String focusName; // 聚焦问题
    private String labelType;
    private String labelTypeLevelFirst;
    private String labelTypeLevelSecond;
    private String labelTypeLevelThree;
    private String labelTypeLevelFour;
    private String labelTypeLevelFive;
    private String opinionWords; // 观点热词
    private String negativeNum; // 负面提及量
    private String complainNum; // 投诉提及量
    private String emotionNum; // 净情感值
    private String riskWordsNum; // 风险词提及量
    private String userNum; // 发声用户(累加)
    private String channelNum; // 发声渠道(累加)
    private String riskLevel; // 当前风险等级
    private String statisticType; // 洞察周期
    private String createTime; // 预警时间
    private String newIdArray;
}
