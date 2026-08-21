package com.voc.service.data.integration.api;

import com.voc.service.data.integration.api.model.ChannelInfoDataModel;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.api.model.PublicDomainInfoDataModel;

import java.util.List;
import java.util.Set;

/**
 * @Title: IChannelService
 * @Package: com.voc.service.data.integration.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/6 13:27
 * @Version:1.0
 */
public interface IPublicDomainService {
    Set<String> findAllIds();

    List<PublicDomainInfoDataModel> findByIds(final Set<String> ids);


    int saveList(String clientId, List<ChannelMetaDataModel> list);

    int saveErrorList(String clientId, List<DataIntegrationRecordModel> list);

    int saveRecordList(String clientId, List<DataIntegrationRecordModel> list);

}
