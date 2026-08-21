package com.voc.service.risk.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 舆情分析-多维度统计结果实体
 * 对应：queryBatchSoundsData 完整 SQL 结果集
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchTaskResultVo {

    // ===================== 动态分组字段（根据 groupField 自动返回） =====================
    /**
     * 车系名称
     */
    private String carSeriesName;

    /**
     * 渠道编码
     */
    private String channelCode;

    /**
     * 一级标签
     */
    private String domTagFirstCode;

    /**
     * 二级标签
     */
    private String domTagSecondCode;

    /**
     * 三级标签
     */
    private String domTagThreeCode;

    /**
     * 四级标签
     */
    private String domTagFourCode;

    /**
     * 话题
     */
    private String topic;

    private String currIdList;

    // ===================== TOP 排行 =====================
    /**
     * 本期TOP列表
     */
    private String currTopList;

    /**
     * 是否本期TOP
     */
    private String isCurrTop;

    /**
     * 上期TOP列表
     */
    private String prevTopList;

    /**
     * 是否上期TOP
     */
    private String isPrevTop;

    // ===================== 本期指标 =====================
    /**
     * 本期提及量
     */
    private Long currTotal;

    /**
     * 本期有效声量
     */
    private Long currVoiceCount;

    /**
     * 本期负面率
     */
    private BigDecimal currNegativeRate;

    /**
     * 本期用户数
     */
    private Long currUsercount;

    // ===================== 本期日均/周均/月均指标 =====================
    /**
     * 本期日均/周均/月均提及量
     */
    private BigDecimal currTotalAvg;

    /**
     * 本期日均/周均/月均有效声量
     */
    private BigDecimal currVoiceCountAvg;

    /**
     * 本期日均/周均/月均负面率
     */
    private BigDecimal currNegativeRateAvg;

    /**
     * 本期日均/周均/月均用户数
     */
    private BigDecimal currUsercountAvg;

    // ===================== 上期指标 =====================
    private Long prevTotal;
    private Long prevVoiceCount;
    private BigDecimal prevNegativeRate;
    private Long prevUsercount;

    // ===================== 上上期指标 =====================
    private Long prevPrevTotal;
    private Long prevPrevVoiceCount;
    private BigDecimal prevPrevNegativeRate;
    private Long prevPrevUsercount;

    // ===================== 去年同期指标 =====================
    private Long lyTotal;
    private Long lyVoiceCount;
    private BigDecimal lyNegativeRate;
    private Long lyUsercount;

    // ===================== 前年同期指标 =====================
    private Long lyPrevTotal;
    private Long lyPrevVoiceCount;
    private BigDecimal lyPrevNegativeRate;
    private Long lyPrevUsercount;

    // ===================== 全局年均值 =====================
    private BigDecimal yearTotalAvg;
    private BigDecimal yearUserAvg;
    private BigDecimal yearVoiceAvg;
    private BigDecimal yearNegAvg;

    // ===================== 全局月均值 =====================
    private BigDecimal monthTotalAvg;
    private BigDecimal monthUserAvg;
    private BigDecimal monthVoiceAvg;
    private BigDecimal monthNegAvg;

    // ===================== 环比率（%） =====================
    private BigDecimal currTotalMom;
    private BigDecimal currVoiceMom;
    private BigDecimal currNegMom;
    private BigDecimal currUserMom;

    // ===================== 同比率（%） =====================
    private BigDecimal currTotalYoy;
    private BigDecimal currVoiceYoy;
    private BigDecimal currNegYoy;
    private BigDecimal currUserYoy;

    // ===================== 上期环同比 =====================
    private BigDecimal prevTotalMom;
    private BigDecimal prevVoiceMom;
    private BigDecimal prevNegMom;
    private BigDecimal prevUserMom;

    private BigDecimal prevTotalYoy;
    private BigDecimal prevVoiceYoy;
    private BigDecimal prevNegYoy;
    private BigDecimal prevUserYoy;
}