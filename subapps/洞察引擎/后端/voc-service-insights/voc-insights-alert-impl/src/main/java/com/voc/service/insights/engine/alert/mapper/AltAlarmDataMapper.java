package com.voc.service.insights.engine.alert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.alert.entity.AltAlarmDataEntity;
import com.voc.service.insights.engine.model.alert.AltAlarmDataDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据监控-告警数据表(AltCoreData)表持久层
 *
 * @author leiww
 * @since 2024-04-26 10:42:22
 */
@Mapper
public interface AltAlarmDataMapper extends BaseMapper<AltAlarmDataEntity> {

    List<AltAlarmDataDto> alertBarChart(String code);
}

