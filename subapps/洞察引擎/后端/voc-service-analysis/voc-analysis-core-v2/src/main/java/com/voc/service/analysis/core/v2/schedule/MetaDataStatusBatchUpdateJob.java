package com.voc.service.analysis.core.v2.schedule;

import com.voc.service.analysis.core.v2.service.BatchMetaDataStatusService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 元数据状态批量更新定时任务
 * 每30秒自动执行一次，检查并更新超时的批量数据
 *
 * @author ckcui
 * @version 1.0.0
 * @createTime 2024年11月21日
 */
@Component
public class MetaDataStatusBatchUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(MetaDataStatusBatchUpdateJob.class);

    @Autowired
    private BatchMetaDataStatusService batchMetaDataStatusService;

    /**
     * 批量更新元数据状态定时任务
     * 每30秒自动执行一次，检查并更新超时的批量数据
     *  @Scheduled(cron = "0/30 * * * * ?")0
     */
    @XxlJob("modify_mate_data_status")
    public void execute() {
        log.info("Start executing modify_mate_data_status");
        try {
            batchMetaDataStatusService.checkAndExecuteTimeoutUpdate();
            log.info("modify_mate_data_status completed successfully");
        } catch (Exception e) {
            log.error("Error executing modify_mate_data_status", e);
        }
    }
}
