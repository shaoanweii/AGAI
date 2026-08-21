package com.voc.service.analysis.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.model.RiskDataParamModel;
import com.voc.service.analysis.risk.entity.AllTypesRiskDataEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface RpAllTypesRiskDataMapper extends BaseMapper<AllTypesRiskDataEntity> {


    List<AllTypesRiskDataEntity> pageRiskDataList(RiskDataParamModel paramModel);
}

