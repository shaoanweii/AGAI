package com.voc.service.data.integration.services;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.data.integration.entity.CleanHistoryDataEntity;
import com.voc.service.data.integration.mapper.CleanHistoryDataMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Title: CleanHistoryDataServiceImpl
 * @Package: com.voc.service.data.integration.services
 * @Description:
 * @Author: cuick
 * @Date: 2024/12/12 20:47
 * @Version:1.0
 */
@Service
public class CleanHistoryDataServiceImpl extends ServiceImpl<CleanHistoryDataMapper, CleanHistoryDataEntity> {
    private static final Logger log = LoggerFactory.getLogger(CleanHistoryDataServiceImpl.class);
    @Value("${ays_api_reslt_data_analysis_miss:1095}")
    int ays_api_reslt_data_analysis_miss;
    @Value("${ays_api_reslt_data_analysis:1095}")
    int ays_api_reslt_data_analysis;
    @Value("${ays_meta_data_analysis:1095}")
    int ays_meta_data_analysis;
    @Value("${ays_api_reslt_data:30}")
    int ays_api_reslt_data;
    @Value("${ays_pre_process_data:30}")
    int ays_pre_process_data;
    @Value("${ays_pre_process_data:30}")
    int ays_meta_data;
    @Value("${ays_post_process_data:1095}")
    int ays_post_process_data;
    @Value("${ins_record_logs:90}")
    int ins_record_logs;
    @Value("${dwd_voc_platform_label_result_origin:30}")
    int dwd_voc_platform_label_result_origin;
    @Value("${ods_channel_execution_result:30}")
    int ods_channel_execution_result;
    @Value("${ays_batch_push_record:30}")
    int ays_batch_push_record;
    @Value("${ods_channel_meta_data:30}")
    int ods_channel_meta_data;

    /**
     * 清理数据接收原始表
     * dwd_voc_platform_all_channel_onversation_details
     *
     * @param clientId
     * @param days
     * @return
     */
    @XxlJob("ods_channel_meta_data")
    public void ods_channel_meta_data(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("dwd_voc_platform_all_channel_onversation_details");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = ods_channel_meta_data;
            }
            //查询
            Long count = this.baseMapper.checkChannelMetaData(days);
            sb.append(" ->查询到数据项:").append(String.valueOf(count));
            if (count <= 0) {
                return;
            }

            this.baseMapper.moveChannelMetaDataToHistoryData(days);
            sb.append(" \n\t->备份数据迁移项:").append(String.valueOf(count));

//            this.baseMapper.checkChannelMetaDataHistoryData();

            long checkLen = this.baseMapper.deleteChannelMetaData(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }


    @XxlJob("ays_meta_data_analysis")
    public void ays_meta_data_analysis(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("ays_meta_data_analysis");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = ays_meta_data_analysis;
            }
            //查询
            Long count = this.baseMapper.checkAysMetaAnalysisData(days);
            sb.append(" ->查询到数据项:").append(String.valueOf(count));
            if (count <= 0) {
                return;
            }

            this.baseMapper.moveAysMetaAnalysisDataToHistoryData(days);
            sb.append(" \n\t->备份数据迁移项:").append(String.valueOf(count));

//            this.baseMapper.checkAysMetaAnalysisDataHistoryData();

            long checkLen = this.baseMapper.deleteAysMetaAnalysisData(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }


    @XxlJob("ays_post_process_data")
    public void ays_post_process_data(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("ays_post_process_data");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = ays_post_process_data;
            }
            //查询
            Long count = this.baseMapper.checkAysPostProcessData(days);
            sb.append(" ->查询到数据项:").append(String.valueOf(count));
            if (count <= 0) {
                return;
            }

            this.baseMapper.moveAysPostProcessDataToHistoryData(days);
            sb.append(" \n\t->备份数据迁移项:").append(String.valueOf(count));

//            this.baseMapper.checkAysPostProcessDataHistoryData();

            long checkLen = this.baseMapper.deleteAysPostProcessData(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }

    @XxlJob("ays_meta_data")
    public void ays_meta_data(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("ays_meta_data");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = ays_meta_data;
            }

            long checkLen = this.baseMapper.deleteAysMetaData(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }

    @XxlJob("ays_pre_process_data")
    public void ays_pre_process_data(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("ays_pre_process_data");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = ays_pre_process_data;
            }

            long checkLen = this.baseMapper.deleteAysPreProcessData(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }

    @XxlJob("ays_api_reslt_data")
    public void ays_api_reslt_data(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("ays_api_reslt_data");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = ays_api_reslt_data;
            }

            long checkLen = this.baseMapper.deleteAysApiResltData(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }

    @XxlJob("ays_api_reslt_data_analysis")
    public void ays_api_reslt_data_analysis(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("ays_api_reslt_data_analysis");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = ays_api_reslt_data_analysis;
            }

            long checkLen = this.baseMapper.deleteApiResltDataAnalysis(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }


    @XxlJob("ays_api_reslt_data_analysis_miss")
    public void ays_api_reslt_data_analysis_miss(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("ays_api_reslt_data_analysis_miss");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = ays_api_reslt_data_analysis_miss;
            }

            long checkLen = this.baseMapper.deleteApiResltDataAnalysisMiss(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }

    @XxlJob("ays_batch_push_record")
    public void ays_batch_push_record(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("deleteAysApiResltData");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = ays_batch_push_record;
            }

            long checkLen = this.baseMapper.deleteAysBatchPushRecord(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }

    @XxlJob("ods_channel_execution_result")
    public void ods_channel_execution_result(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("ods_channel_execution_result");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = ods_channel_execution_result;
            }

            long checkLen = this.baseMapper.deleteoDsChannelExecutionResult(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }

    /*@XxlJob("ods_channel_output_record")
    public void dwd_voc_platform_label_result_origin(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("ods_channel_output_record");
        sb.append(" \n\t->接收参数:").append(param);

        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = 365;
            }

            long checkLen = this.baseMapper.deleteoOdsChannelOutputRecord(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }*/

    @XxlJob("dwd_voc_platform_label_result_origin")
    public void dwd_voc_platform_label_result_origin(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("dwd_voc_platform_label_result_origin");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = dwd_voc_platform_label_result_origin;
            }

            long checkLen = this.baseMapper.deleteDmLabeledResultData(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }

    @XxlJob("ins_record_logs")
    public void ins_record_logs(String clientId, int days) {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        StringBuilder sb = new StringBuilder("dwd_voc_platform_label_result_origin");
        sb.append(" \n\t->接收参数:").append(param);
        try {
            if (NumberUtil.isNumber(param)) {
                days = Integer.parseInt(param);
            }
            if (days < 30) {
                days = ins_record_logs;
            }

            long checkLen = this.baseMapper.deleteInsRecordLogs(days);
            sb.append(" \n\t->本次删除数据项:").append(String.valueOf(checkLen));
        } catch (Exception e) {
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log(sb.toString());
            log("========================== 本次调度结束 ==========================");
        }
    }

    public void log(String msg) {
        XxlJobHelper.log(msg);
        log.info(msg);
    }

    public void log(Throwable e) {
        XxlJobHelper.log(e);
        log.error(e.getMessage(), e);
    }
}
