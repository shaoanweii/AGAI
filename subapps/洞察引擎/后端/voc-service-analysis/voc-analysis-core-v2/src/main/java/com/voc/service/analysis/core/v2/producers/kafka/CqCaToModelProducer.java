package com.voc.service.analysis.core.v2.producers.kafka;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@Component("CqCaToModelProducer.producer.kafka")
public class CqCaToModelProducer {
    public static final String TOPIC_DATA = "voc_toModel_topic";

    public static final String TOPIC_DATA1 = "voc_toModel_topic_v2";

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void pushData(MessageDTO msg) throws Exception {
        Assert.isTrue(ObjUtil.isNotNull(msg.getData()), "getData cannot be empty");
        final String sendText = JSONUtil.toJsonStr(msg.getData(), JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
        kafkaTemplate.send(TOPIC_DATA, sendText);
    }


    public void pushDataTopic2(MessageDTO msg) throws Exception {
        Assert.isTrue(ObjUtil.isNotNull(msg.getData()), "getData cannot be empty");
        final String sendText = JSONUtil.toJsonStr(msg.getData(), JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
        kafkaTemplate.send(TOPIC_DATA1, sendText);
    }
}


