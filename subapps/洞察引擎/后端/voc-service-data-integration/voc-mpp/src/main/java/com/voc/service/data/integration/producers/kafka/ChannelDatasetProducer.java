package com.voc.service.data.integration.producers.kafka;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
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
 * @Description 再推送数据清洗服务前的【数据结果表】
 */
@Component("channelDataset.producer.kafka")
public class ChannelDatasetProducer {
    public static final String TOPIC_MPP_INPUT_CHANNEL = "voc_anal_di_mpp_input_channel";
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void pushChannelData(MessageDTO msg) {
        String sendText = JSONUtil.toJsonStr(msg, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
        kafkaTemplate.send(TOPIC_MPP_INPUT_CHANNEL, sendText);
    }
}

