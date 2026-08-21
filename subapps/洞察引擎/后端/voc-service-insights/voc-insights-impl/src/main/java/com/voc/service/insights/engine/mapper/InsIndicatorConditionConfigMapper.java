package com.voc.service.insights.engine.mapper;

import com.voc.service.insights.engine.entity.InsIndicatorConditionConfigEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 指标条件配置Mapper
 * 对应数据库表：ins_indicator_condition_config
 */
public interface InsIndicatorConditionConfigMapper {

    /**
     * 根据使用范围查询指标条件配置
     *
     * @param canuse 使用范围（1、单点；2、批量）
     * @return 指标条件配置列表
     */
    List<InsIndicatorConditionConfigEntity> selectByCanuse(@Param("canuse") String canuse);
}
