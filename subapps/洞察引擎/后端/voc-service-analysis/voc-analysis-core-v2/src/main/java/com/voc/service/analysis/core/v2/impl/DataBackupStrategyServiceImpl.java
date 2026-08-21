package com.voc.service.analysis.core.v2.impl;

import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.api.IAysPostprocessDataService;
import com.voc.service.analysis.api.IDataBackupStrategyService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.common.util.ServiceContextHolder;
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
    IAysMetaDataAnalysisService metaDataAnalysisService;
    @Autowired
    IAysPostprocessDataService aysPostprocessDataService;
    @Autowired
    AnalysisConfig config;
    @Override
    @XxlJob("removeHistoryData")
//    @Scheduled(cron = "0 0 0 * * ?") // 每天的凌晨零点
    public String removeHistoryData() throws Exception {
        StringBuilder sb = new StringBuilder();

        try {
            //ckcui clientId
            String clientId = ServiceContextHolder.getClientId();
//            long size1 = metaDataService.removeHistoryData(clientId, config.getMetaDataHistoryDays());
//            sb.append("metaDataService：").append(size1).append("| ");

            /*long size2 = metaDataAnalysisService.removeHistoryData(clientId, config.getMetaDataAnalysisHistoryDays());
            sb.append("metaDataAnalysisService：").append(size2).append("| ");

            long size3 = aysPostprocessDataService.removeHistoryData(clientId, config.getPostProcessDataHistoryDays());
            sb.append("aysPostprocessDataService：").append(size2).append("| ");*/

            /*long size3 = modelResltService.removeHistoryData(clientId, config.getModelApiResltDataHistoryDays());
            sb.append("modelResltService：").append(size3).append("| ");

            long size4 = modelResltAnalysisService.removeHistoryData(clientId, config.getModelApiResltDataAnalysisHistoryDays());
            sb.append("modelResltAnalysisService：").append(size4).append("| ");

            long size5 = preprocessDataService.removeHistoryData(clientId, config.getPostProcessDataHistoryDays());
            sb.append("preprocessDataService：").append(size5).append("| ");

            long size6 = modelResltAnalysisValidService.removeHistoryData(clientId, config.getValidModelApiResltDataAnalysisHistoryDays());
            sb.append("modelResltAnalysisValidService：").append(size6).append("| ");

            long size7 = postprocessValidDataService.removeHistoryData(clientId, config.getValidPostProcessDataHistoryDays());
            sb.append("postprocessValidDataService：").append(size7).append("| ");*/

            log.info("清理历史数据完成 {}", sb.toString());
        } catch (Exception e) {
            log.error("清理历史数据异常");
            log.error(e.getMessage(), e);
        }
        return sb.toString();

    }
}
