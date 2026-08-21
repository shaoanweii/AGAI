package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsClosedRuleConditionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 规则条件配置表数据访问接口
 */
@Mapper
@Repository
public interface InsClosedRuleConditionMapper extends BaseMapper<InsClosedRuleConditionEntity> {

    void insertBatch(@Param("list") List<InsClosedRuleConditionEntity> list);

    List<InsClosedRuleConditionEntity> listByRuleId(@Param("ruleId") String ruleId);

    Integer findQuoteCount(@Param("ruleId") List<String> id);
}