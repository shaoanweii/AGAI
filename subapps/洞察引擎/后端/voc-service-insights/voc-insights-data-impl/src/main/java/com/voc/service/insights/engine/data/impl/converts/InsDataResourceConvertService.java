package com.voc.service.insights.engine.data.impl.converts;

import com.voc.service.insights.engine.data.entity.InsDataResourceEntity;
import com.voc.service.insights.engine.model.data.InsDataResourceModel;
import com.voc.service.insights.engine.vo.InsAccountLexiconVo;
import com.voc.service.insights.engine.vo.InsDataResourceDetailVo;
import com.voc.service.insights.engine.vo.data.ResourceDescDto;
import org.mapstruct.*;

import java.util.List;


/**
 * 资源库(InsDataResource)转换类
 *
 * @author leiww
 * @since 2024-04-02 16:37:37
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsDataResourceConvertService {

    InsDataResourceModel convertTo(InsDataResourceEntity o);

    InsDataResourceEntity convertTo(InsDataResourceModel o);

    List<InsDataResourceEntity> convertModelToList(List<InsDataResourceModel> model);

    List<InsDataResourceModel> convertEntityToList(List<InsDataResourceEntity> entity);

    @Mapping(source = "accountName", target = "name")
    InsDataResourceDetailVo convertAccountLexiconToDataResourceDetailVo(InsAccountLexiconVo o);

    InsDataResourceDetailVo convertResourceDesToDataResourceDetailVo(ResourceDescDto o);


}

