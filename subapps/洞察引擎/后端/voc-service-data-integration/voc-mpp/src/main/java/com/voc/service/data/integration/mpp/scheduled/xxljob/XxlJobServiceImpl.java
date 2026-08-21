package com.voc.service.data.integration.mpp.scheduled.xxljob;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.common.util.IdWorker;
import com.voc.service.data.integration.api.IChannelService;
import com.voc.service.data.integration.api.IModelOutputRecordService;
import com.voc.service.data.integration.api.IModelProcessedResultDataService;
import com.voc.service.data.integration.api.IMppInputDataService;
import com.voc.service.data.integration.api.model.ChannelInfoDataModel;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.voc.service.data.integration.nodes.context.ChannelDatasetContext;
import com.voc.service.data.integration.nodes.public_domain.context.PublicDomainDatasetContext;
import com.voc.service.insights.engine.api.clients.IAysCoreServiceClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Title: XxlJobServiceImpl
 * @Package: com.voc.service.data.integration.in.scheduled
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/18 16:02
 * @Version:1.0
 */
@Service
@RequiredArgsConstructor
@RestController
public class XxlJobServiceImpl extends XxlJobAbstractMppService {

    private static final Logger log = LoggerFactory.getLogger(XxlJobServiceImpl.class);
    @Value("${xxl.job.executor.appname}")
    private String appName;
    @Autowired
    DataIntegrationConfig config;
    @Autowired
    IChannelService channelService;
    @Autowired
    IAysCoreServiceClient aysCoreServiceClient;
    @Autowired
    IMppInputDataService inputDataService;
    @Autowired
    IModelOutputRecordService modelOutputRecordService;
    @Autowired
    IModelProcessedResultDataService modelProcessedResultDataService;
    @Resource
    private FlowExecutor flowExecutor;
    @Resource
    private RedissonClient redissonClient;


    @XxlJob("mpp-v1")
    public void init() {
        // 任务执行逻辑
        log("MyXxlJobHandler is running.");

    }

    /**
     * 各渠道数据接入调用入口
     */
    @XxlJob("mpp-starrock-channel")
    @GetMapping("/inputChannelMeateData")
    public void inputChannelMeateData(@RequestParam(value = "paramReq", required = false) String paramReq) {
        log("========================== 本次调度开始 ==========================");
        final String param;
        if (StrUtil.isBlank(paramReq)) {
            param = XxlJobHelper.getJobParam();
        } else {
            param = paramReq;
        }
        log("param[{}] .", param);
        List<String> params = new ArrayList<>();
        try {
            if (param == null || param.trim().length() == 0) {
                log("param[{}] invalid.", param);
            } else {
                params = StrUtil.splitTrim(param, ",");
            }
            // 读取参数
            final List<ChannelInfoDataModel> channelList = channelService.findAll();
            Set<String> channelCodeList = channelList.stream().map(ChannelInfoDataModel::getCode).collect(Collectors.toSet());
            log.debug("channelCodeList:{}", channelCodeList);
            //参数校验
            if (CollUtil.isNotEmpty(params)) {
                log.info("执行单个渠道:{}", params.size());
                params.stream().forEach(channel -> {
                    if (channelCodeList.contains(channel)) {
//                if (config.getChannelValidExtRequiredFields().keySet().contains(channel)) {
                        log("开始执行：渠道->".concat(channel));
                        this.loadChannelDataset(channel);
                        log("结束执行：渠道->".concat(channel));
                    } else {
                        log(new Exception("渠道数配置异常->".concat(channel)));
                    }
                });
            } else {
                log.info("执行全量渠道数据");
                this.loadChannelDataset("");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log("========================== 本次调度结束 ==========================");
        }

    }

    /**
     * 各渠道数据接入调用入口
     */
    @XxlJob("mpp-starrock-channel-out")
    public void outputChannelMeateData() {
        log("========================== 本次调度开始 ==========================");
        final String param = XxlJobHelper.getJobParam();
        log("param[{}] .", param);
        try {
            if (param == null || param.trim().length() == 0) {
                log("param[" + param + "] invalid.");
                log("param[{}] invalid.", param);
//                XxlJobHelper.handleFail();
//                return;
            }

            final List<ChannelInfoDataModel> channelList = channelService.findAll();

            // 读取参数
//            List<String> params = StrUtil.splitTrim(param, ",");
            Set<String> channelCodeList = channelList.stream().map(ChannelInfoDataModel::getCode).collect(Collectors.toSet());
//            Set<String> list = params.stream().filter(channel -> channelCodeList.contains(channel)).collect(Collectors.toSet());
            if (CollUtil.isEmpty(channelCodeList)) {
                log.error("无有效的渠道数据，退出本次执行任务");
                throw new Exception("无有效的渠道数据，退出本次执行任务");
            }
            log("channelCodeList:{}", channelCodeList);

            //   this.saveOutputData(channelCodeList);
            log("执行成功".concat(channelCodeList.toString()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log("========================== 本次调度结束 ==========================");
        }

    }

    public void saveOutputData(Set<String> channelList) {
        try {
//            String rsId = modelProcessedResultDataService.refreshData();
//            log("刷新视图完成：{}", rsId);
            final Set<String> defaultRequiredFields = config.getChannelOutputRequiredFields();
            final Set<String> invalidAttrsSet = defaultRequiredFields.stream()
                    .map(field -> StrUtil.split(field, ":").get(0))
                    .collect(Collectors.toSet());
            log("requiredFields:{}", defaultRequiredFields);
            long size = modelProcessedResultDataService.saveOutputData(channelList, invalidAttrsSet);
            log("推送数据执行完成：{}", size);
            long length = modelOutputRecordService.record(channelList, invalidAttrsSet);
            log("记录本次执行结果: {}", length);
        } catch (Exception e) {
            log(e);
        }
    }

    public void loadChannelDataset(String channelType) {
        ChannelDatasetContext context = ChannelDatasetContext.builder().build();
        try {
            context.setClientId(config.getClientId());
            context.setChannelType(channelType);
            context.setWorkId(IdWorker.getId());
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            log.info(">>>>>>>>>>>>>>>>> 开始处理 {} 的请求 <<<<<<<<<<<<<<<<<<<<<<", context.getWorkId());
            // 执行原有逻辑
//            LiteflowResponse response = flowExecutor.execute2Resp("mpp_input_channel_load_flow", context, context.getWorkId());
            LiteflowResponse response = flowExecutor.execute2RespWithRid("mpp_input_channel_load_flow", context, context.getWorkId());
            if (!response.isSuccess()) {
                log.error("workId:{}  {}", context.getWorkId(), response.getCause());
                throw response.getCause();
            }
            // 计算并记录执行耗时
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000; // 转换为秒
            log("工作ID: {} 执行耗时: {} 秒", context.getWorkId(), duration);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log("workId {} 完成 ", context.getWorkId());
        }
    }

    //定时
    // @Scheduled(cron = "0/30 * * * * ?")
    public void loadRetryChannelDataset() {
        ChannelDatasetContext context = ChannelDatasetContext.builder().build();
        try {
            context.setClientId(config.getClientId());
//            context.setChannelType(channelType);
            context.setWorkId(IdWorker.getId());

            // 记录开始时间
            long startTime = System.currentTimeMillis();
            log.info(">>>>>>>>>>>>>>>>> 开始处理 {} 的请求 <<<<<<<<<<<<<<<<<<<<<<", context.getWorkId());
            // 执行原有逻辑
//            LiteflowResponse response = flowExecutor.execute2Resp("mpp_input_channel_load_retry_flow", context, context.getWorkId());
            LiteflowResponse response = flowExecutor.execute2RespWithRid("mpp_input_channel_load_retry_flow", context, context.getWorkId());
            if (!response.isSuccess()) {
                log.error("workId:{}  {}", context.getWorkId(), response.getCause());
                throw response.getCause();
            }

            // 计算并记录执行耗时
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000; // 转换为秒
            log("工作ID: {} 执行耗时: {} 秒", context.getWorkId(), duration);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }finally {
            log.info("workId {} 完成 ", context.getWorkId());
        }
    }


    @XxlJob("mpp-public-domain")
    @GetMapping("/mppPublicDomainData")
    public void mppPublicDomainData(@RequestParam(value = "paramReq", required = false) String paramReq) {
        log("========================== 本次调度开始 ==========================");
        final String param;
        if (StrUtil.isBlank(paramReq)) {
            param = XxlJobHelper.getJobParam();
        } else {
            param = paramReq;
        }
        log("param[{}] .", param);
        List<String> params = new ArrayList<>();
        try {
            if (param == null || param.trim().length() == 0) {
                log("param[{}] invalid.", param);
            } else {
                params = StrUtil.splitTrim(param, ",");
            }
            // 读取参数
            final List<ChannelInfoDataModel> channelList = channelService.findAll();
            Set<String> channelCodeList = channelList.stream().map(ChannelInfoDataModel::getCode).collect(Collectors.toSet());
            log.debug("channelCodeList:{}", channelCodeList);
            //参数校验
            if (CollUtil.isNotEmpty(params)) {
                log.info("执行单个渠道:{}", params.size());
                params.stream().forEach(channel -> {
                    if (channelCodeList.contains(channel)) {
//                if (config.getChannelValidExtRequiredFields().keySet().contains(channel)) {
                        log("开始执行：渠道->".concat(channel));
                        this.loadPublicDomainDataset(channel);
                        log("结束执行：渠道->".concat(channel));
                    } else {
                        log(new Exception("渠道数配置异常->".concat(channel)));
                    }
                });
            } else {
                log.info("执行全量渠道数据");
                this.loadPublicDomainDataset("");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log(e);
            XxlJobHelper.handleFail();
        } finally {
            log("========================== 本次调度结束 ==========================");
        }

    }


    public void loadPublicDomainDataset(String channelType) {
        PublicDomainDatasetContext context = PublicDomainDatasetContext.builder().build();
        try {
            context.setClientId(config.getClientId());
            context.setChannelType(channelType);
            context.setWorkId(IdWorker.getId());
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            log.info(">>>>>>>>>>>>>>>>> 开始处理 {} 的请求 <<<<<<<<<<<<<<<<<<<<<<", context.getWorkId());
            // 执行原有逻辑
//            flowExecutor.execute2RespWithRid()
            LiteflowResponse response = flowExecutor.execute2RespWithRid("mpp_input_public_domain_data_flow", context, context.getWorkId());
            if (!response.isSuccess()) {
                log.error("workId:{}  {}", context.getWorkId(), response.getCause());
                throw response.getCause();
            }
            // 计算并记录执行耗时
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000; // 转换为秒
            log("工作ID: {} 执行耗时: {} 秒", context.getWorkId(), duration);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("workId {} 完成 ", context.getWorkId());
            log("workId {} 完成 ", context.getWorkId());
        }
    }
}
