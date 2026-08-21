package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsClosedRuleConditionHisEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 规则条件配置历史表数据访问接口
 */
@Mapper
@Repository
public interface InsClosedRuleConditionHisMapper extends BaseMapper<InsClosedRuleConditionHisEntity> {

    void insertBatch(@Param("list") List<InsClosedRuleConditionHisEntity> list);
}