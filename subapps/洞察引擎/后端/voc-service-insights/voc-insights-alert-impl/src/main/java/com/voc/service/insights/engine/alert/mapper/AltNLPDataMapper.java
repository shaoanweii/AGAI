package com.voc.service.insights.engine.alert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.alert.entity.AltMonitoringDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 数据监控-告警数据表(AltCoreData)表持久层
 *
 * @author leiww
 * @since 2024-04-26 10:42:22
 */
@Mapper
public interface AltNLPDataMapper extends BaseMapper<AltMonitoringDataEntity> {

    AltMonitoringDataEntity pullData(AltMonitoringDataEntity entity ,@Param("todayTime")String todayTime);
}

