package com.voc.service.analysis.core.v2.producers.kafka;

import cn.hutool.core.util.StrUtil;
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
@Component("batchPush.producer.kafka")
public class BatchPushProducer {
    public static final String TOPIC_EVENT = "VDP_batchPushEvent";
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    AnalysisConfig config;

    public void pushEvent(MessageDTO msg) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
        final String str = JSONUtil.toJsonStr(msg);
//       kafkaTemplate.send(TOPIC_EVENT, str);
    }
}

