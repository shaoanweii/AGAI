package com.voc.service.insights.engine.alert.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.voc.service.insights.engine.model.alert.AltAlarmDataDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
@DS("starrocks1")
public interface DataSimulationMapper {

    void insertDataSimulation(@Param("model") AltAlarmDataDto model);

    void insertPostProcessData(@Param("model") AltAlarmDataDto model);

    void insertMetaDataAnalysis(@Param("model") AltAlarmDataDto model);


}
