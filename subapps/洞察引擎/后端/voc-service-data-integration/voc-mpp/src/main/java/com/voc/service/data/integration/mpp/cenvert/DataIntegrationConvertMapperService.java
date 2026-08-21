package com.voc.service.data.integration.mpp.cenvert;

import com.voc.service.data.integration.api.model.ChannelInfoDataModel;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.api.model.PublicDomainInfoDataModel;
import com.voc.service.data.integration.entity.ChannelInfoDataEntity;
import com.voc.service.data.integration.entity.ChannelMetaDataEntity;
import com.voc.service.data.integration.entity.ChannelExecutionResultEntity;
import com.voc.service.data.integration.entity.PublicDomainInfoDataEntity;
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
public interface DataIntegrationConvertMapperService {

    List<ChannelExecutionResultEntity> cenvertToEntityList(List<DataIntegrationRecordModel> list);

    List<DataIntegrationRecordModel> cenvertToModelList(List<ChannelExecutionResultEntity> entityList);

    List<ChannelMetaDataModel> cenvertToChannelMetaDataModelList(List<ChannelMetaDataEntity> entityList);

    List<ChannelInfoDataModel> convertToChannelInfoDataModelList(List<ChannelInfoDataEntity> list);


    List<PublicDomainInfoDataModel> convertToPulibcDomainInfoDataModelList(List<PublicDomainInfoDataEntity> list);
}
