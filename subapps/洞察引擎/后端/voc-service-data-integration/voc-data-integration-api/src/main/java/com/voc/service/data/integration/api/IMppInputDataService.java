package com.voc.service.data.integration.api;

import com.voc.service.data.integration.api.model.ChannelMetaDataModel;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Title: MppInputDataService
 * @Package: com.voc.service.data.integration.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 14:57
 * @Version:1.0
 */
public interface IMppInputDataService {

     Set<String> loadDataIds(final String channelType, Date startDate, Date endDate);
     List<ChannelMetaDataModel> loadData(final Set<String> ids);
//     List<DataIntegrationRecordModel> loadRetryData(final String channelType, Date startDate, Date endDate);
     long removeHistoryData(final String clientId, int days);

     long loadDataCount();
}
