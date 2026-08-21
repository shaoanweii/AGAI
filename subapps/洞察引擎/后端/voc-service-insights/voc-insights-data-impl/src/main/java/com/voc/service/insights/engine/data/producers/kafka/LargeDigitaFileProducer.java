package com.voc.service.insights.engine.data.producers.kafka;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.logs.dto.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
//@Component("largeDigitaFile.producer.kafka")
public class LargeDigitaFileProducer {
    public static final String TOPIC_EVENT = "VDP_largeDigitaFiles";
    private static final Logger log = LoggerFactory.getLogger(LargeDigitaFileProducer.class);

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void pushData(MessageDTO msg) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(msg.getType()), "getType cannot be empty");
        Assert.isTrue(ObjUtil.isNotNull(msg.getData()), "getData cannot be empty");

        log.info("被大文件分片数据[{}]-数据项: ", msg.getType()
                , JSONUtil.toJsonStr(msg.getData(), JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false)));

        kafkaTemplate.send(TOPIC_EVENT, JSONUtil.toJsonStr(msg));
    }
}

