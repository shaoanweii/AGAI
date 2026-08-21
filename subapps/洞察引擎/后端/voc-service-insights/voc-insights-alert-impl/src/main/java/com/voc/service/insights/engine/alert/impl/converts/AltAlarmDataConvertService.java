package com.voc.service.insights.engine.alert.impl.converts;

import com.voc.service.insights.engine.alert.entity.AltAlarmDataEntity;
import com.voc.service.insights.engine.model.alert.AltAlarmDataModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;


/**
 * 数据监控-告警数据表(AltCoreData)转换类
 *
 * @author leiww
 * @since 2024-04-26 10:42:21
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AltAlarmDataConvertService {

    AltAlarmDataModel convertTo(AltAlarmDataEntity o);

    AltAlarmDataEntity convertTo(AltAlarmDataModel o);

    List<AltAlarmDataEntity> convertModelToList(List<AltAlarmDataModel> model);

    List<AltAlarmDataModel> convertEntityToList(List<AltAlarmDataEntity> entity);


}

