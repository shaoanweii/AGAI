package com.voc.service.insights.engine.alert.impl.converts;

import com.voc.service.insights.engine.alert.entity.AltMonitoringDataEntity;
import com.voc.service.insights.engine.model.alert.AltAlarmDataModel;
import com.voc.service.insights.engine.model.alert.AltMonitoringDataModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;


/**
 * 数据监控-监控数据表(AltMonitoringData)转换类
 *
 * @author leiww
 * @since 2024-04-26 15:11:34
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AltMonitoringDataConvertService {

    AltMonitoringDataModel convertTo(AltMonitoringDataEntity o);

    AltMonitoringDataModel convertTo(AltAlarmDataModel o);

    AltMonitoringDataEntity convertTo(AltMonitoringDataModel o);


//    List<AltMonitoringDataModel> convertEntityToList(List<AltMonitoringDataEntity> entity);


    AltMonitoringDataEntity toEntity(AltMonitoringDataModel altMonitoringDataModel);

    AltMonitoringDataModel toModel(AltMonitoringDataEntity altMonitoringDataEntity);
}

