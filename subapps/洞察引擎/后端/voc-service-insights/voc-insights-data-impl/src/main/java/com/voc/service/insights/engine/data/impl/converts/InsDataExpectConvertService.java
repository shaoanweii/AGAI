package com.voc.service.insights.engine.data.impl.converts;

import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.data.entity.InsDataExpectEntity;
import com.voc.service.insights.engine.data.entity.LargeDigitaFilesEntity;
import com.voc.service.insights.engine.model.data.InsDataExpectModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsDataExpectConvertService {

    InsDataExpectEntity convertTo(InsDataExpectModel model);

    InsDataExpectModel convertTo(InsDataExpectEntity model);

    List<InsDataExpectModel> convertToList(List<InsDataExpectEntity> entityList);

    List<LargeDigitaFilesModel> convertToLargeDigitaFilesModelList(List<LargeDigitaFilesEntity> list);

    LargeDigitaFilesEntity convertToLargeDigitaFilesEntity(LargeDigitaFilesModel model);

    LargeDigitaFilesModel convertToLargeDigitaFilesModel(LargeDigitaFilesEntity largeDigitaFilesEntity);
}

