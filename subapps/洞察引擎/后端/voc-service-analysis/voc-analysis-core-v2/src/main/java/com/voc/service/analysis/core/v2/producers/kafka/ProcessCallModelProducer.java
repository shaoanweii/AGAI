package com.voc.service.analysis.core.v2.producers.kafka;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.voc.service.analysis.api.IAysPreprocessDataService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.dto.MessageDTO;
import com.yomahub.liteflow.core.FlowExecutor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
@Component("processCallModel.producer.kafka")
public class ProcessCallModelProducer {

    public static final String TOPIC_EVENT = "voc_anal_flow_push_model_event";

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private FlowExecutor flowExecutor;
    @Autowired
    IAysPreprocessDataService preprocessDataService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    AnalysisConfig config;
//    static  TimedCache<String, Integer> temp = CacheUtil.newTimedCache(1000 * 60 * 60 * 10);


    public void pushEvent(MessageDTO msg) throws JsonProcessingException {
        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
        final String str = JSONUtil.toJsonStr(msg);
        kafkaTemplate.send(TOPIC_EVENT, str);
    }


}

