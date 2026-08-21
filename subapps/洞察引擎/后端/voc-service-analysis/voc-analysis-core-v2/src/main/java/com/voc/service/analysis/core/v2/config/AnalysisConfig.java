package com.voc.service.analysis.core.v2.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.*;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AnalysisConfig
 * @createTime 2024年03月08日 15:37
 * @Copyright cuick
 */
@Configuration
@SuppressWarnings("all")
@Data
@EnableScheduling
//@RefreshScope
public class AnalysisConfig {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisConfig.class);
    //缓存数据有效期
    public static final int CACHE_DURATION = 30;

    public static final TimeUnit CACHE_UNIT = TimeUnit.DAYS;
    @Resource
    private RedissonClient redissonClient;


    private RLock modelLock;
    private RLock proRulesLock;
    private RLock postRulesLock;
    private RLock templateLock;

    private RLock validLock;

    //接收原始数据时，数据项中标识客户编号的字段名
    public Set<String> pushDataRequired;
    @Value("${client_id:764547797eb2e192763f5334028d49c9}")
    public String clientId;

    @Value("${pushData.attrs.id:id}")
    public String id_attr_name;
    @Value("${pushData.attrs.clientId:clientId}")
    public String client_id_attr_name;
    @Value("${pushData.attrs.dataId:data_id}")
    public String data_id_attr_name;
    @Value("${pushData.attrs.channelId:channelId}")
    public String channel_id_attr_name;
    @Value("${pushData.attrs.content_type:content_type}")
    public String content_type_attr_name;
    @Value("${pushData.attrs.content:content}")
    public String content_attr_name;
    @Value("${pushData.attrs.title:title}")
    public String title_attr_name;
    @Value("${pushData.attrs.city:city}")
    public String city_attr_name;
    @Value("${pushData.attrs.user_name:user_name}")
    public String userName_attr_name;
    @Value("${pushData.attrs.publish_time:publish_time}")
    public String publishTime_attr_name;
    //    @Value("#{'${pushData.topics.newCreateList}'.split(',')}")
//    public Set<String> newCreateList = new HashSet<>();
    @Value("${cleanProcessPreRulesEventTopic:false}")
    public boolean cleanProcessPreRulesEventTopic;
    //模型每批次计算调试设置
    @Value("${model_params_size:4}")
    int modelParamsSize;
    @Value("${invoke_model:true}")
    boolean invokeModel;
    @Value("${discard_data_items:true}")
    boolean discardDataItems;

    // 此处设置分批次处理的规则计算的数据集大小 ：50
    @Value("${validDataSplitSize:1000}")
    int validDataSplitSize;

    //保留历史数据天数
    @Value("${historyDataDays:1000}")
    int historyDataDays;

    //历史数据保存时间
    @Value("${metaDatahistoryDays:1000}")
    int metaDataHistoryDays;
    @Value("${metaDataAnalysisHistoryDays:1000}")
    int metaDataAnalysisHistoryDays;
    @Value("${preProcessDataHistoryDays:1000}")
    int preProcessDataHistoryDays;
    @Value("${postProcessDataHistoryDays:1000}")
    int postProcessDataHistoryDays;
    @Value("${modelApiResltDataHistoryDays:1000}")
    int modelApiResltDataHistoryDays;
    @Value("${modelApiResltDataAnalysisHistoryDays:1000}")
    int modelApiResltDataAnalysisHistoryDays;
    @Value("${validPostProcessDataHistoryDays:1000}")
    int validPostProcessDataHistoryDays;
    @Value("${validModelApiResltDataAnalysisHistoryDays:1000}")
    int validModelApiResltDataAnalysisHistoryDays;

    @Value("${analysis.resultdata_allowed_editable_fields:}")
    List<String> resultdataAllowedEditableFields;
    @Value("${analysis.remove_tags_with_regex_enable:true}")
    boolean removeTagsWithRegexEnable;
    //voc_anal_flow_mate_data_status topic 是否写入数据和消费数据开关
    @Value("${analysis.saveMateDataStatusModify.enable:true}")
    boolean saveMateDataStatusModify;

    @Value("${analysis.remove_tags_with_regex:}")
    List<String> removeTagsWithRegex;

    @Value("${analysis.modify_result_data_rows_size:10}")
    Integer modifyResultDataRowsSize;

    @Getter
    @Value("${feign.default.token:eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYW5hbHlzaXNfYXBpIiwiaWRlbnRpdHlfdHlwZSI6ImJhc2UiLCJhcHBfaWQiOiJhbmFseXNpcyIsInVzZXJuYW1lIjoiY1Fkb094bmg2eVEwMW5lc2ZLTlhVNjFKQmx5RFg3dHc4YXhod0JjNVl4aXl1MC9CYjdDQWZwQjJ5QTFxYjQ4QiIsInN1YiI6ImFuYWx5c2lzX2FwaSIsImlhdCI6MTcxMDQxMTQyMSwiZXhwIjo0MDc1NjExNDIxfQ.G1kAeqwp0udBimnDdIAqL1nSIcgV0u6YrU0bb5OchJ0}")
    String defaultToken;

    //是否启用现成处理前置规则
    @Value("${runPreRulesScheduled:true}")
    boolean runPreRulesScheduled;

    //是否启用现成处理后置规则
    @Value("${runPostRulesScheduled:true}")
    boolean runPostRulesScheduled;

    //是否启用现成处理调用模型
    @Value("${runCallModelScheduled:true}")
    boolean runCallModelScheduled;

    //模型调用是否合并数据
    @Value("${modelMergeDataSwitch:true}")
    boolean modelMergeDataSwitch;

    //是否开启MQ批量推送
    @Value("${enbaledMQPush:false}")
    boolean enbaledMQPush;

    //内容长度限制
    @Value("${contentLimit:3000}")
    Integer contentLimit;

    @PostConstruct
    public void init() {

        pushDataRequired = CollUtil.newHashSet(Arrays.asList(
                id_attr_name,
//                client_id_attr_name,
                channel_id_attr_name,
                content_type_attr_name,
                content_attr_name
        ));
        pushDataRequired = pushDataRequired.stream().filter(StrUtil::isNotBlank).collect(Collectors.toSet());

        modelLock = redissonClient.getLock("model");
        proRulesLock = redissonClient.getLock("proRulesLock");
        postRulesLock = redissonClient.getLock("postRulesLock");
        validLock = redissonClient.getLock("valid");
        templateLock = redissonClient.getLock("templateLock");

        logger.info("pushDataRequired: {}", pushDataRequired);
        logger.info("modelParamsSize: {}", modelParamsSize);
        logger.info("validDataSplitSize: {}", validDataSplitSize);
        logger.info("historyDataDays: {}", historyDataDays);

        /*IStaticDataServcie staticDataServcie = applicationContext.getBean(IStaticDataServcie.class);
        if(ObjUtil.isNotNull(staticDataServcie)) {
            Object rs = staticDataServcie.getResourceGroup(this.getClientId());
            logger.info("getResourceGroup： {}", rs.toString());

            rs = staticDataServcie.getValidResourceGroup(this.getClientId());
            logger.info("getValidResourceGroup： {}", rs.toString());

            rs = staticDataServcie.getAllEnabledResourceGroup(this.getClientId());
            logger.info("getAllEnabledResourceGroup： {}", rs.toString());
        }else{
            logger.error("staticDataServcie is null");
        }*/
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldConfig {
        private String field;
//        private String value;
    }
}
