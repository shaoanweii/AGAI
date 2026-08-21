package com.voc.service.analysis.risk.kafka;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.risk.api.model.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("riskData.producer.kafka")
public class RiskDataProducer {
    public static final String TOPIC_DATA = "voc_report_risk_data";
    public static final String TOPIC_DATA_TEST = "voc_report_risk_data_test";
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void pushData(MessageDTO msg) {
        List<Object> list = (List<Object>) msg.getData();
        for (Object obj : list) {
            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
            kafkaTemplate.send(TOPIC_DATA, sendText);
        }
    }

    public void pushTestData(MessageDTO msg) {
        List<Object> list = (List<Object>) msg.getData();
        for (Object obj : list) {
            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
            kafkaTemplate.send(TOPIC_DATA_TEST, sendText);
        }
    }
}

