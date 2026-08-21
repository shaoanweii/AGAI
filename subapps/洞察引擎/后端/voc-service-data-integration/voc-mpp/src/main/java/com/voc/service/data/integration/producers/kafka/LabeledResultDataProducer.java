package com.voc.service.data.integration.producers.kafka;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 * @Description 再推送数据清洗服务前的【数据结果表】
 */
@Component("labeledResultData.producer.kafka")
public class LabeledResultDataProducer {
    public static final String TOPIC_MPP_OUTPUT_LABELED = "voc_mpp_output_labeled";
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void pushData(MessageDTO msg) {
//        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
//        List<Object> list = (List<Object>) msg.getData();

        String sendText = JSONUtil.toJsonStr(msg.getData(), JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
        kafkaTemplate.send(TOPIC_MPP_OUTPUT_LABELED, sendText);
    }
}

