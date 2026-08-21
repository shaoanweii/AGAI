package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.ModOpinionRelationDataEntity;
import com.voc.service.insights.engine.vo.ModOpinionRelationDataVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ModOpinionRelationDataMapper extends BaseMapper<ModOpinionRelationDataEntity> {

    List<ModOpinionRelationDataVo> queryModOpinionRelationList(String startTime);

}
