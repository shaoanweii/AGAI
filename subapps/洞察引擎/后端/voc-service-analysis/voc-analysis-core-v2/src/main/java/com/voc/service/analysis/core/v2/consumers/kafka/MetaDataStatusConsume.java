package com.voc.service.analysis.core.v2.consumers.kafka;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.producers.kafka.MetaDataAnalysisProducer;
import com.voc.service.analysis.core.v2.service.BatchMetaDataStatusService;
import com.voc.service.analysis.dto.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
@Component("metaDataAnalysis.cusumer.kafka")
public class MetaDataStatusConsume {
    private static final Logger log = LoggerFactory.getLogger(MetaDataStatusConsume.class);
    @Autowired
    BatchMetaDataStatusService batchMetaDataStatusService;
    @Autowired
    AnalysisConfig config;
    @KafkaListener(topics = {MetaDataAnalysisProducer.TOPIC_EVENT}, groupId = "${kafkaEvent.groupId}")
    public void onMessage(String message) {
        log.debug(">>>>>>> 收到 {} 的请求 <<<<<<<<<<<<<<", message);
        if(!config.isSaveMateDataStatusModify()) {
            log.debug("未开启保存数据状态修改事件配置，请检查配置项[saveMateDataStatusModify]");
            return ;
        }
        MessageDTO dto = null;
        try {
            dto = JSONUtil.toBean(message, MessageDTO.class);
            if (ObjUtil.isNull(dto)) {
                log.error("dto {}", dto);
                return;
            }
            log.debug(">>>>>>> 接收到状态更新消息 <<<<<<<<<<<<<< {}", dto.getData());
            final String clientId = dto.getSource();

            Map<String, Integer> dataStatusMap = JSONUtil.toBean(JSONUtil.parseObj(dto.getData()), Map.class);
            log.debug("<<<<<添加状态到批量更新缓存>>>>：{}", dataStatusMap);
            if (CollUtil.isNotEmpty(dataStatusMap)) {
                // 将数据添加到批量处理缓存，达到阈值(200条)或超时(20秒)后自动批量更新
                batchMetaDataStatusService.addToCache(clientId, dataStatusMap);
                log.debug("Status update data added to cache, clientId: {}, items: {}, current cache size: {}",
                        clientId, dataStatusMap.size(), batchMetaDataStatusService.getCacheSize(clientId));
            } else {
                log.warn("dataStatusMap is empty");
            }
        } catch (Exception e) {
            log.error("Error processing status update message", e);
        }
    }
}

