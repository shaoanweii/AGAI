package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.SentimentRuleKeywordsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 
 * @version 1.0.0
 * @ClassName SentimentRuleKeywordsMapper
 * @description 情感规则关键词Mapper接口
 * @createTime 
 */
@Mapper
public interface SentimentRuleKeywordsMapper extends BaseMapper<SentimentRuleKeywordsEntity> {
    
    /**
     * 根据主情感和规则类型查询规则关键词
     * @param primarySentiment 主情感
     * @param ruleType 规则类型
     * @return 规则关键词列表
     */
    @Select("SELECT * FROM voc_ins_sentiment_rule_keywords_mv WHERE primary_sentiment = #{primarySentiment} AND rule_type = #{ruleType}")
    List<SentimentRuleKeywordsEntity> selectByPrimarySentimentAndRuleType(String primarySentiment, String ruleType);
}
