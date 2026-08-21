package com.voc.service.insights.engine.data.impl.converts;

import com.voc.service.insights.engine.data.entity.InsDataExpectDescEntity;
import com.voc.service.insights.engine.model.data.InsDataExpectDescModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsDataExpectDescConvertService {

    InsDataExpectDescEntity convertTo(InsDataExpectDescModel model);

    InsDataExpectDescModel convertTo(InsDataExpectDescEntity model);

    List<InsDataExpectDescModel> convertToList(List<InsDataExpectDescEntity> entityList);


}

