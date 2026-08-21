package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsBatchRuleHisEntity;

import java.util.List;

/**
 * 批量规则历史记录Mapper
 * 对应数据库表：ins_batch_rule_his
 */
public interface InsBatchRuleHisMapper extends BaseMapper<InsBatchRuleHisEntity> {

    /**
     * 批量插入历史记录
     *
     * @param hisEntities 历史记录实体列表
     * @return 插入成功的数量
     */
    int insertBatch(List<InsBatchRuleHisEntity> hisEntities);

    /**
     * 根据规则ID查询历史记录列表
     *
     * @param ruleId 规则ID
     * @return 历史记录列表
     */
    List<InsBatchRuleHisEntity> selectListByRuleId(String ruleId);

    /**
     * 根据历史记录ID查询历史详情
     *
     * @param hisId 历史记录ID
     * @return 历史记录详情
     */
    InsBatchRuleHisEntity selectByHisId(String hisId);
}
