package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsClosedRuleAlertEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 规则预警推送配置表数据访问接口
 */
@Mapper
@Repository
public interface InsClosedRuleAlertMapper extends BaseMapper<InsClosedRuleAlertEntity> {

    InsClosedRuleAlertEntity queryRuleAlertById(@Param("ruleId") String ruleId);
}