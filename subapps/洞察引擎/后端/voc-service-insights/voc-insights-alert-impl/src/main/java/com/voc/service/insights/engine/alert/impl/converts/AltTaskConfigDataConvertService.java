package com.voc.service.insights.engine.alert.impl.converts;

import com.voc.service.insights.engine.alert.entity.AltTaskConfigDataEntity;
import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import com.voc.service.insights.engine.model.alert.AltTaskConfigDataModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;


/**
 * 数据监控-任务配置表(AltTaskConfigData)转换类
 *
 * @author leiww
 * @since 2024-04-30 17:11:55
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AltTaskConfigDataConvertService {

    AltTaskConfigDataModel convertTo(AltTaskConfigDataEntity o);

    AltTaskConfigDataEntity convertTo(AltTaskConfigDataModel o);

    List<AltTaskConfigDataEntity> convertModelToList(List<AltTaskConfigDataModel> model);

    List<AltTaskConfigDataModel> convertEntityToList(List<AltTaskConfigDataEntity> entity);

    AlertTaskModel convertEntityTask(AltTaskConfigDataEntity entity);

}

