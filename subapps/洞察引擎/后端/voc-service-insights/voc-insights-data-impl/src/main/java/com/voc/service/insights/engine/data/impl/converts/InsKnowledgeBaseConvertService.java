package com.voc.service.insights.engine.data.impl.converts;

import com.voc.service.insights.engine.data.entity.InsKnowledgeBase;
import com.voc.service.insights.engine.data.entity.InsKnowledgeBaseDetails;
import com.voc.service.insights.engine.model.knowledgeBase.InsKnowledgeBaseDetailsModel;
import com.voc.service.insights.engine.model.knowledgeBase.InsKnowledgeBaseModel;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseDetailsVo;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseTemplateVo;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsKnowledgeBaseConvertService {

    InsKnowledgeBase convertTo(InsKnowledgeBaseModel model);
    InsKnowledgeBaseModel convertTo(InsKnowledgeBase model);
    InsKnowledgeBaseVo convertToVo(InsKnowledgeBase model);
    List<InsKnowledgeBaseVo> convertToList(List<InsKnowledgeBase> entityList);

    InsKnowledgeBaseDetails convertToDetail(InsKnowledgeBaseDetailsModel model);
    InsKnowledgeBaseDetailsModel convertToDetail(InsKnowledgeBaseDetails model);
    InsKnowledgeBaseDetailsVo convertToDetailVo(InsKnowledgeBaseDetailsModel model);
    List<InsKnowledgeBaseDetailsVo> convertToDetailList(List<InsKnowledgeBaseDetails> entityList);

    InsKnowledgeBaseDetails klbtemplate2Details(InsKnowledgeBaseTemplateVo templateVo);

    List<InsKnowledgeBaseTemplateVo> convertToOriginDataVoList(List<InsKnowledgeBaseDetails> entityList);
}

