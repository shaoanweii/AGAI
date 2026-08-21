package com.voc.service.insights.engine.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.AysModelResltDataAnalysisEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface AysLabelModelResltAnalysisMapper extends BaseMapper<AysModelResltDataAnalysisEntity> {

}
