package com.voc.service.insights.engine.data.impl.converts;

import com.voc.service.insights.engine.data.entity.InsDataResourceDescEntity;
import com.voc.service.insights.engine.model.data.InsDataResourceDescModel;
import com.voc.service.insights.engine.vo.data.ResourceDescDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;


/**
 * 资源详情(InsDataResourceDesc)转换类
 *
 * @author leiww
 * @since 2024-04-02 17:00:18
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsDataResourceDescConvertService {

    InsDataResourceDescModel convertTo(InsDataResourceDescEntity o);

    InsDataResourceDescEntity convertTo(InsDataResourceDescModel o);

    List<InsDataResourceDescEntity> convertModelToList(List<InsDataResourceDescModel> model);

    List<InsDataResourceDescModel> convertEntityToList(List<InsDataResourceDescEntity> entity);


    ResourceDescDto convertToDto(InsDataResourceDescEntity entity);
}

