package com.voc.service.insights.engine.data.impl.converts;

import com.voc.service.insights.engine.data.entity.InsDataSourceDescEntity;
import com.voc.service.insights.engine.data.entity.InsDataSourceEntity;
import com.voc.service.insights.engine.data.entity.InsDataSourceTemplateEntity;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.InsDataSourceDescVo;
import com.voc.service.insights.engine.vo.InsDataSourceOriginDataVo;
import com.voc.service.insights.engine.vo.InsDataSourceTemplateVo;
import com.voc.service.insights.engine.vo.InsDataSourceVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsDataSourceConvertService {

    InsDataSourceEntity convertTo(InsDataSourceModel model);

    InsDataSourceModel convertTo(InsDataSourceEntity model);

    List<InsDataSourceModel> convertToList(List<InsDataSourceEntity> entityList);

    InsDataSourceTemplateEntity dataSourceTemplateVoToEntity(InsDataSourceTemplateVo insDataSourceTemplateVo);

    List<InsDataSourceDescEntity> dataSourceTemplateEntityToDescEntity(List<InsDataSourceTemplateEntity> insDataSourceTemplateEntityList);

    List<InsDataSourceVo> dataSourceEntityToVo(List<InsDataSourceEntity> insDataSourceEntityList);

    InsDataSourceDescVo dataSourceDescEntityToVo(InsDataSourceDescEntity e);

    List<InsDataSourceOriginDataVo> dataSourceDescEntityToOriginDataVo(List<InsDataSourceDescEntity> insDataSourceDescEntityList);

    List<InsDataSourceDescVo> dataSourceDescEntityListToVoList(List<InsDataSourceDescEntity> dataSourceDetail);
}

