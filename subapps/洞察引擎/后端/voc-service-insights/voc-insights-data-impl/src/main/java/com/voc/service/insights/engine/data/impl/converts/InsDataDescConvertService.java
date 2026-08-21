package com.voc.service.insights.engine.data.impl.converts;

import com.voc.service.insights.engine.data.entity.InsDataSourceDescEntity;
import com.voc.service.insights.engine.model.data.InsDataSourceDescModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsDataDescConvertService {

    InsDataSourceDescEntity convertTo(InsDataSourceDescModel model);

    InsDataSourceDescModel convertTo(InsDataSourceDescEntity model);

    List<InsDataSourceDescModel> convertToList(List<InsDataSourceDescEntity> entityList);
}

