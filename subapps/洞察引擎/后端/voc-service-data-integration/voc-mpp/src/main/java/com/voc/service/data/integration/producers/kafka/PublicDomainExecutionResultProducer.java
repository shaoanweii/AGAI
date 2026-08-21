package com.voc.service.data.integration.producers.kafka;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 * @Description 再推送数据清洗服务前的【数据结果表】
 */
@Component("publicDomainExecutionResultProducer.producer.kafka")
public class PublicDomainExecutionResultProducer {
    public static final String TOPIC_MPP_INPUT_EXECUTION = "voc_anal_di_stg_mate_data_pub_m_inc";
    public static final String TOPIC_MPP_INPUT_EXECUTION_ERROR = "voc_anal_di_stg_mate_data_pub_m_inc_error";
    public static final String TOPIC_MPP_INPUT_EXECUTION_RECORD = "voc_anal_di_pub_domain_data_finished_record";
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void push(MessageDTO msg) {
        List<Object> list = (List<Object>) msg.getData();
//        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
//        List<Object> list = (List<Object>) msg.getData();
        for (Object obj : list) {
            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
            kafkaTemplate.send(TOPIC_MPP_INPUT_EXECUTION, sendText);
        }
    }
    public void pushError(MessageDTO msg) {
        List<Object> list = (List<Object>) msg.getData();
//        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
//        List<Object> list = (List<Object>) msg.getData();
        for (Object obj : list) {
            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
            kafkaTemplate.send(TOPIC_MPP_INPUT_EXECUTION_ERROR, sendText);
        }
    }

    public void pushRecord(MessageDTO msg) {
        List<Object> list = (List<Object>) msg.getData();
//        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
//        List<Object> list = (List<Object>) msg.getData();
        for (Object obj : list) {
            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
            kafkaTemplate.send(TOPIC_MPP_INPUT_EXECUTION_RECORD, sendText);
        }
    }
}

