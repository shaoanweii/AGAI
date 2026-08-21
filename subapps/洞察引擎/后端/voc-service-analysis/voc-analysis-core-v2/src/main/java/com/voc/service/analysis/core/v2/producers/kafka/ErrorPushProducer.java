package com.voc.service.analysis.core.v2.producers.kafka;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.dto.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Component("errorPush.producer.kafka")
public class ErrorPushProducer {

    public static final String TOPIC_DATA = "voc_anal_flow_error_data_record";
    private static final Logger log = LoggerFactory.getLogger(ErrorPushProducer.class);

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    AnalysisConfig config;

    public void pushData(MessageDTO msg) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(msg.getType()), "getType cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "getSource cannot be empty");
        Assert.isTrue(ObjUtil.isNotNull(msg.getData()), "getData cannot be empty");
        msg.getExt().stream().filter(ext -> ext.getKey().equals("table")).findAny().orElseThrow(() -> new RuntimeException("getExt('table') cannot be empty"));
        msg.getExt().stream().filter(ext -> ext.getKey().equals("workId")).findAny().orElseThrow(() -> new RuntimeException("getExt('workId') cannot be empty"));

        log.error("被遗弃的数据[{}]-[]操作-数据项: ", msg.getSource(), msg.getType()
                , JSONUtil.toJsonStr(msg.getData(), JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false)));

        String sendText = JSONUtil.toJsonStr(msg.getData(), JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
        kafkaTemplate.send(TOPIC_DATA,sendText);
    }
}

