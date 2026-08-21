package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsRegionEntity;
import com.voc.service.insights.engine.entity.InsRegulationConditionConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @创建者: fanrong
 * @创建时间: 2025/11/10 15:56
 * @描述:
 **/
@Mapper
@Repository
public interface InsRegulationConditionConfigMapper extends BaseMapper<InsRegulationConditionConfigEntity> {
}
