package com.voc.service.analysis.core.v2.consumers.kafka;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.api.INotificationInsDataStatusService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.producers.kafka.BatchPushProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.model.AysMetaDataAnalysisModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
@Component("batchPush.consumer.kafka")
public class BatchPushCusumer {

    private static final Logger log = LoggerFactory.getLogger(BatchPushCusumer.class);
    @Autowired
    IAysBatchPushRecordV2Service batchPushRecordV2Service;
    @Autowired
    AnalysisConfig config;
    @Autowired
    INotificationInsDataStatusService iNotificationInsDataStatusService;
    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;

   // @KafkaListener(topics = {BatchPushProducer.TOPIC_EVENT}, groupId = "${kafkaEvent.groupId}")
    public void onMessage(String message) {
        log.debug(">>>>>>> 收到 {} 的请求 <<<<<<<<<<<<<<", message);
        StopWatch stopWatch = new StopWatch();
     //   stopWatch.start("消息处理开始".concat(BatchPushProducer.TOPIC_EVENT));
        try {
            final MessageDTO dto = JSONUtil.toBean(message, MessageDTO.class);
            if (ObjUtil.isNull(dto)) {
                log.error("dto {}", dto);
                return;
            }
            final String clientId = dto.getSource();
            Assert.isTrue(StrUtil.isNotBlank(clientId), "clientId cannot be empty");
            final List<String> ids = JSONUtil.toList(JSONUtil.parseArray(dto.getData()), String.class);
            Assert.isTrue(CollUtil.isNotEmpty(ids), "ids cannot be empty");
            final String status = (String) dto.getExt().stream().filter(ext -> StrUtil.equals(ext.getKey(), "status")).findAny().get().getValue();
            Assert.isTrue(StrUtil.isNotBlank(status), "status cannot be empty");
            final String source = (String) dto.getExt().stream().filter(ext -> StrUtil.equals(ext.getKey(), "source")).findAny().get().getValue();
            Assert.isTrue(StrUtil.isNotBlank(source), "source cannot be empty");

            long count = batchPushRecordV2Service.modifyStatusDB(clientId, new HashSet<>(ids), status, source);
            /*if (count > 0) {
                if (status.equals("-1")) {
                    metaDataAnalysisService.modifyToDataStatus(clientId, new HashSet<>(ids), "-1");
                }
                Set<String> newIds = metaDataAnalysisService.findDataIdListByIds(clientId, new HashSet<>(ids));
                if (CollUtil.isNotEmpty(newIds)) {
                    *//*List<String> newIds = new ArrayList<>();
                    for (AysMetaDataAnalysisModel aysMetaDataAnalysisModel : dataAnalysisServiceByIds) {
                        JSONObject entries = JSONUtil.parseObj(aysMetaDataAnalysisModel.getExtFields());
                        if (entries.getStr("showType").equals("2")) {
                        } else {
                            newIds.add(aysMetaDataAnalysisModel.getDataId());
                        }
                    }*//*

                    log.info("BatchPushCusumer开始调用洞察引擎通知");
                    Set<String> workIdList = batchPushRecordV2Service.fin(clientId, newIds);
                    if (CollUtil.isNotEmpty(workIdList)) {
                        for (String workId : workIdList) {
                            iNotificationInsDataStatusService.notificationStatus(clientId, workId);
                        }
                    }
                }else{
                    log.info("BatchPushCusumer系统集成不调用洞察引擎通知");
                }
            }*/
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            stopWatch.stop();
      //      log.info("消息处理总耗时：{}：{}", BatchPushProducer.TOPIC_EVENT, stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        }
    }
}

