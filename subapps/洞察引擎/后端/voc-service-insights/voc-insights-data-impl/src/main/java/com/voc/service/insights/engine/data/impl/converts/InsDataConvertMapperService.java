package com.voc.service.insights.engine.data.impl.converts;

import com.voc.service.insights.engine.data.entity.*;
import com.voc.service.insights.engine.model.InsAccountLexiconModel;
import com.voc.service.insights.engine.model.InsRegulationDetailsModel;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import com.voc.service.insights.engine.model.InsRuleInfoModel;
import com.voc.service.insights.engine.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 13:26
 * @描述:
 **/
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsDataConvertMapperService {

    InsRegulationInfoEntity regulationInfoModelConvertEntity(InsRegulationInfoModel regulationInfoModel);

    List<InsRegulationDetailEntity> regulationDetailsModelListConvertEntityList(List<InsRegulationDetailsModel> regulationDetailsModels);

    List<RegulationInfoVo> regulationInfoEntityListConvertVoList(List<InsRegulationInfoEntity> regulationInfoEntities);
    List<AysRegulationInfoVo> regulationInfoEntityListConvertAysVoList(List<InsRegulationInfoEntity> regulationInfoEntities);

    RegulationInfoVo regulationInfoEntityConvertVo(InsRegulationInfoEntity regulationInfoEntity);

    List<RegulationDetailsVo> regulationDetailsEntityListConvertVoList(List<InsRegulationDetailEntity> regulationDetailEntities);

    InsRuleInfoEntity ruleInfoModelConvertEntity(InsRuleInfoModel ruleInfoModel);

    List<InsRuleInfoVo> ruleInfoEntityListConvertVoList(List<InsRuleInfoEntity> ruleInfoEntities);

    InsRuleInfoVo ruleInfoEntityConvertVo(InsRuleInfoEntity ruleInfoEntity);

    List<InsValidateInfoVo> validateInfoEntityListConvertVoList(List<InsValidateRuleEntity> validateRuleEntities);

    InsAccountLexiconEntity accountLexiconModelConvertEntity(InsAccountLexiconModel insAccountLexicon);

    InsAccountLexiconVo accountLexiconEntityConvertVo(InsAccountLexiconEntity insAccountLexiconEntity);

    List<InsAccountLexiconVo> accountLexiconEntityListConvertVoList(List<InsAccountLexiconEntity> records);
}
