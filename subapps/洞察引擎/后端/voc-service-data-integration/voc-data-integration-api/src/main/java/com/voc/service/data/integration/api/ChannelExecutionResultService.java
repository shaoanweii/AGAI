package com.voc.service.data.integration.api;

import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.api.model.DataRequestModel;
import com.voc.service.data.integration.api.vo.DataValidateResultVo;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Title: IErrorDataMsgService
 * @Package: com.voc.service.data.integration.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/25 11:25
 * @Version:1.0
 */
public interface ChannelExecutionResultService {

    int saveList(String clientId, List<DataIntegrationRecordModel> list);

    List<DataIntegrationRecordModel> loadRetryData(String channelType, Date startDate, Date endDate);

    List<DataIntegrationRecordModel> finshishData(final String channelType, Set<String> ids, Date startDate, Date endDate);

    DataValidateResultVo findVerificationResult(DataRequestModel dataRequestModel);

    List<DataValidateResultVo> findVerificationResultByCondition(DataRequestModel dataRequestModel);

    long removeHistoryData(final String clientId, int days);

    Set<String> filterData(Set<String> entityList);
}
