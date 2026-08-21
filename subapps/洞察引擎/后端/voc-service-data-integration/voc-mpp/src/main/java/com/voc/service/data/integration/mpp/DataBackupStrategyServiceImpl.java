package com.voc.service.data.integration.mpp;

import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.data.integration.api.ChannelExecutionResultService;
import com.voc.service.data.integration.api.IDataBackupStrategyService;
import com.voc.service.data.integration.api.IMppInputDataService;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Title: DataBackupStrategyServiceImpl
 * @Package: com.voc.service.data.integration.mpp
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/8 16:38
 * @Version:1.0
 */
@Service
public class DataBackupStrategyServiceImpl implements IDataBackupStrategyService {
    private static final Logger log = LoggerFactory.getLogger(DataBackupStrategyServiceImpl.class);
    @Autowired
    ChannelExecutionResultService channelExecutionResultService;
    @Autowired
    IMppInputDataService inputDataService;
    @Autowired
    DataIntegrationConfig config;

    @Override
    @XxlJob("removeHistoryData")
//    @Scheduled(cron = "0 0 0 * * ?") // 每天的凌晨零点
    public String removeHistoryData() throws Exception {
        StringBuilder sb = new StringBuilder();

        try {
            String clientId = ServiceContextHolder.getClientId();
//            long size1 = metaDataService.removeHistoryData(clientId, config.getMetaDataHistoryDays());
//            sb.append("metaDataService：").append(size1).append("| ");

            long size1 = channelExecutionResultService.removeHistoryData(clientId, config.getChannelExecutionHistoryDays());
            sb.append("channelExecutionResultService：").append(size1).append("| ");
            long size2 = inputDataService.removeHistoryData(clientId, config.getChannelMetaDataMapperHistoryDays());
            sb.append("inputDataService：").append(size2).append("| ");


            log.info("清理历史数据完成 {}", sb.toString());
        } catch (Exception e) {
            log.error("清理历史数据异常");
            log.error(e.getMessage(), e);
        }
        return sb.toString();

    }
}
