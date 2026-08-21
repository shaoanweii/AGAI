package com.voc.service.insights.engine.producer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.logs.dto.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/10 上午9:48
 * @描述:
 **/
@Component("cleanCacheEvent.producer.kafka")
public class CleanCacheEventProducer {

    public static final String TOPIC_EVENT = "cleanCache";
    private static final Logger log = LoggerFactory.getLogger(CleanCacheEventProducer.class);

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    public void pushEvent(MessageDTO msg) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(msg.getType()), "getType cannot be empty");
        log.info("推送事件数据 {}", msg.getType());
        String sendText = JSONUtil.toJsonStr(msg, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
        kafkaTemplate.send(TOPIC_EVENT, sendText);
    }
}
