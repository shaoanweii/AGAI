package com.voc.service.analysis.core.v2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author 
 * @version 1.0.0
 * @ClassName SentimentRuleKeywordsEntity
 * @description 情感规则关键词实体类 (对应表: voc_ins_sentiment_rule_keywords_mv)
 * @createTime 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "voc_ins_sentiment_rule_keywords_mv")
public class SentimentRuleKeywordsEntity implements Serializable {
    
    /**
     * 主情感倾向
     */
    private String primarySentiment;
    
    /**
     * 规则类型
     */
    private String ruleType;
    
    /**
     * 规则关键词
     */
    private String ruleKeywords;
    
    /**
     * 二级情感倾向
     */
    private String secondarySentiment;
}
