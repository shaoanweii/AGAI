package com.voc.service.insights.engine.model.impl.converts;

import com.voc.service.insights.engine.model.entity.InsModelDescEntity;
import com.voc.service.insights.engine.model.model.InsModelDescModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsModelDescConvertService {

    public InsModelDescEntity convertTo(InsModelDescModel model);

    public InsModelDescModel convertTo(InsModelDescEntity model);

    List<InsModelDescModel> convertToList(List<InsModelDescEntity> records);
}

