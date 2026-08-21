package com.voc.service.analysis.core.v2.producers.kafka;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
@Component("model.producer.kafka")
public class ModelProducer {
//    public static final String TOPIC_DATA = "VDP_toModel";
//
//    public static final String TOPIC_EVENT_UPDATE = "VDP_modelResltEventUpdate";
//    @Autowired
//    KafkaTemplate<String, String> kafkaTemplate;
//    @Autowired
//    AnalysisConfig config;
//
//    public void pushData(MessageDTO msg) throws Exception {
//        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "getSource cannot be empty");
//        Assert.isTrue(ObjUtil.isNotNull(msg.getData()), "getData cannot be empty");
//
//        final String sendText = JSONUtil.toJsonStr(msg.getData(), JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
//        kafkaTemplate.send(TOPIC_DATA,sendText);
//    }
//
//
//    public void updateEvent(MessageDTO msg) throws Exception {
//        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
//        final String str = JSONUtil.toJsonStr(msg);
//        kafkaTemplate.send(TOPIC_EVENT_UPDATE, str);
//    }
}

