package com.voc.service.insights.engine.alert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.alert.entity.AltMonitoringDataEntity;
import com.voc.service.insights.engine.model.alert.AltAlarmDataModel;
import com.voc.service.insights.engine.model.alert.InsAltDataModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据监控-监控数据表(AltMonitoringData)表持久层
 *
 * @author leiww
 * @since 2024-04-26 15:11:35
 */
@Mapper
public interface AltMonitoringDataMapper extends BaseMapper<AltMonitoringDataEntity> {

    List<Map> historicalRatioMean(@Param("model") AltAlarmDataModel model);

    List<Map> getHistoricalAvg(@Param("model") AltAlarmDataModel model);

    List<InsAltDataModel> nlpDataAlertBarChart(@Param("model") AltAlarmDataModel model);

    List<InsAltDataModel> metaDataAlertBarChart(@Param("model") AltAlarmDataModel model);

    List<InsAltDataModel> pushDataAlertBarChart(@Param("model") AltAlarmDataModel model);




    void moveToHistory(@Param("ids")Set<String> updatedIds, @Param("workId")String workId);

    void reverseData(@Param("ids")Set<String> updatedIds, @Param("workId")String workId);

}

