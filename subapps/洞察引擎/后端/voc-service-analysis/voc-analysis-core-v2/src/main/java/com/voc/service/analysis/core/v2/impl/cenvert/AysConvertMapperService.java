package com.voc.service.analysis.core.v2.impl.cenvert;

import com.voc.service.analysis.core.v2.entity.*;
import com.voc.service.analysis.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * @Title: AysConvertMapperService
 * @Package: com.voc.service.analysis.core.v2.impl.cenvert
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 11:54
 * @Version:1.0
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AysConvertMapperService {
    AysMetaDataAnalysisModel cenvertToModel(AysMetaDataAnalysisEntity entity);

    List<AysMetaDataAnalysisModel> cenvertToModelList(List<AysMetaDataAnalysisEntity> entityList);

    List<AysMetaDataAnalysisModel> cenvertToModelExtList(List<AysMetaDataExtAnalysisEntity> entityList);

    AysProcessDataModel converToAysProcessDataModel(AysMetaDataAnalysisModel data);

    AysPreprocessDataEntity converToAysPreprocessDataEntity(AysProcessDataModel model);

    List<AysProcessDataModel> converToAysPreprocessDataModel(List<AysPreprocessDataEntity> saveList);

    List<AysProcessDataModel> converToAysProcessDataModel2(List<AysPreprocessDataEntity> list);

    List<AysProcessDataModel> converToAysPostprocessValidDataModel(List<AysPostprocessValidDataEntity> saveList);

    List<AysProcessDataModel> converToAysPostprocessDataModel(List<AysPostprocessDataEntity> saveList);

    AysModelResltDataAnalysisValidModel converToAysModelResltDataAnalysisValidModel2(AysModelResltDataAnalysisValidEntity entity);

    List<AysModelResltDataAnalysisValidModel> converToAysModelResltDataAnalysisValidModel(List<AysModelResltDataAnalysisValidEntity> list);

    List<AysModelResltDataAnalysisModel> converToAysModelResltDataAnalysisModel(List<AysModelResultDataAnalysisEntity> entityList);

    List<AysMetaDataAnalysisModel> converToMetaItemsDataModel(List<AysMetaDataAnalysisEntity> saveList);

    List<AysMetaDataAnalysisEntity> converToMetaItemsEntity(List<AysMetaDataAnalysisModel> errorList);

    AysModelResltDataAnalysisModel cenvertToAysModelResltDataAnalysisModel(AysModelResultDataAnalysisEntity entity);

    AysProcessDataModel converToAysProcessDataModel3(AysModelResltDataAnalysisModel data);

    List<AysPreprocessDataModel> cenvertToAysPreprocessDataModelList(List<AysPreprocessDataEntity> entityList);

    AysProcessDataModel converToAysProcessDataModel4(AysPreprocessDataModel data);

    AysProcessDataModel convertToAysModelResltDataAnalysisModel(AysModelResltDataAnalysisModel model);

    List<AysModelResltDataAnalysisModel> cenvertToAysModelResltDataAnalysisModelList2(List<AysModelResultDataAnalysisEntity> entityList);

    AysProcessDataModel converToAysModelResltDataAnalysisValidModel1(AysModelResltDataAnalysisValidModel data);

    List<AysModelResltDataAnalysisValidModel> cenvertToAysModelResltDataAnalysisValidEntityList(List<AysModelResltDataAnalysisValidEntity> entityList);

    AysProcessDataModel convertToAysModelResltDataAnalysisValidModel(AysModelResltDataAnalysisValidModel model);

    AysMetaDataModel converToAysMetaDataEntity(AysMetaDataEntity entity);

    List<AysMetaDataModel> converToAysMetaDataList(List<AysMetaDataEntity> entity);

    AysBatchPushRecordModel converToAiBatchPushModel(AysBatchPushRecordEntity aysBatchPushRecordEntity);

    List<AysPreprocessDataModel> converToAysPreprocessDataModelList(List<AysPreprocessDataEntity> entityList);

    List<AysPreprocessDataModel> converToAysPostprocessDataModelList(List<AysPostprocessDataEntity> entityList);

    List<AysProcessDataModel> converToAysProcessDataModelList(List<AysModelResltDataEntity> entityList);
}
