package com.voc.service.data.integration.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.data.integration.api.IChannelService;
import com.voc.service.data.integration.api.model.ChannelInfoDataModel;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/3 下午2:48
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "data.integration.mpp")
public class DataIntegrationConfig {
    @Autowired
    IChannelService channelService;
    //批量提交数据集大小
    @Builder.Default
    int batchPushApiDataSetSize = 100;

    @Builder.Default
    int batchCleanedAppEventsDataSetSize = 500;

    @Value("${use_cache_record_data_id:false}")
    //是否启用缓存记录业务数据ID，用于判断ID重复执行逻辑加固
    boolean useCache;

    String requestAuthorizationTokens;

    String clientId;
    @Builder.Default
    String processContentType = "text";   //text、order

    @Builder.Default
    String processModelType = "3";   //1,2,3

    @Value("${voc_data_source:1850720952091041794}")
    String vocDataSource;
    @Value("${project_id:4cb464bb8f604284dd83c92356fd62a4}")
    String projectId;

    @Value("${channelExecutionHistoryDays:1000}")
    int channelExecutionHistoryDays;

    @Value("${unprocessedDataCount:10}")
    int unprocessedDataCount;

    @Value("${channelMetaDataMapperHistoryDays:1000}")
    int channelMetaDataMapperHistoryDays;

    @Builder.Default
    //程序启动时自动创建tppics
    public Set<String> initCreateTopics = new HashSet<>();

    @Builder.Default
    Set<String> channelValidDefaultRequiredFields = new HashSet<>();
    @Builder.Default
    Set<String> channelOutputRequiredFields = new HashSet<>();
    @Builder.Default
    Map<String, Set<String>> channelValidExtRequiredFields = new HashMap<>();

    @PostConstruct
    public void init() throws Exception {
        //加载项校验
        /*if (CollUtil.isEmpty(channelValidDefaultRequiredFields)) {
            throw new Exception("未配置必填字段");
        }*/
        if (CollUtil.isEmpty(initCreateTopics)) {
            throw new Exception("程序启动时自动创建tppics为空");
        }


        if (StrUtil.isBlank(requestAuthorizationTokens)) {
            throw new Exception("未配置token");
        }
        /*if(StrUtil.isBlank(outputListenerTopic)){
            throw new Exception("未配置[清洗服务标签结果表topic]");
        }*/
        final List<ChannelInfoDataModel> channelList = channelService.findAll();
        channelValidExtRequiredFields.clear();
        channelList.stream().forEach(model -> {
            channelValidExtRequiredFields.put(model.getCode(), Set.of());
        });
        if (CollUtil.isEmpty(channelValidExtRequiredFields)) {
            throw new Exception("渠道配置数据为空");
        }
    }
}
