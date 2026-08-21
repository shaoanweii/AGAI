package com.voc.service.insights.engine.model.impl.converts;

import com.voc.service.insights.engine.model.model.InsModelInfoModel;
import com.voc.service.insights.engine.model.entity.InsModelInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * @author leiww
 * @version 1.0.0
 * @ClassName
 * @Description 转换类
 * @createTime 2024/2/21 15:14
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsModelInfoConvertService {

    public InsModelInfoEntity convertTo(InsModelInfoModel model);

    public InsModelInfoModel convertTo(InsModelInfoEntity model);

    List<InsModelInfoModel> convertToList(List<InsModelInfoEntity> entity);
}

