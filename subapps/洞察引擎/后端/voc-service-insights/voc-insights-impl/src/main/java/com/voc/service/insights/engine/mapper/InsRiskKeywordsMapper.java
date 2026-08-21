package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsRiskKeywordsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface InsRiskKeywordsMapper extends BaseMapper<InsRiskKeywordsEntity> {

}
