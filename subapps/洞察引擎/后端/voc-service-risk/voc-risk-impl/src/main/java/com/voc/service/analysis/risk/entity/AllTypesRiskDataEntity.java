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
@TableName(value = "rp_all_types_risk_data")
public class AllTypesRiskDataEntity {
    private String id; // 主键
    private String riskId; // 风险点Id
    private String risk; // 风险code或用户Id
    private String channelId;
    private String brandCodeName; // 品牌名称
    private String carSeriesName; // 车系名称
    private String projectId;
    private String riskType; // 风险类型
    private String labelType;
    private String labelTypeLevelFirst;
    private String labelTypeLevelSecond;
    private String labelTypeLevelThree;
    private String labelTypeLevelFour;
    private String labelTypeLevelFive;
    private String riskName; // 风险问题
    private String focusName; // 聚焦问题
    private String opinionWords; // 观点热词
    private Long negativeNum; // 负面提及量
    private Long complainNum; // 投诉提及量
    private BigDecimal emotionNum; // 净情感值
    private Long riskWordsNum; // 风险词提及量
    private Long userNum; // 发声用户(累加)
    private Long channelNum; // 发声渠道(累加)
    private String riskLevel; // 当前风险等级
    private String statisticType; // 洞察周期
    private LocalDateTime createTime; // 预警时间
    private String newIdArray;
    private String riskIndex;
    private String opinionWordsJson;
    private String cityCode;
}
