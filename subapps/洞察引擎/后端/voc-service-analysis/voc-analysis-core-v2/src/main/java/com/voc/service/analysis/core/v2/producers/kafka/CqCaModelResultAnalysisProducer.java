package com.voc.service.analysis.core.v2.producers.kafka;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.dto.MessageDTO;
import com.yomahub.liteflow.core.FlowExecutor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
@Component("modelResultAnalysis.producer.kafka")
public class CqCaModelResultAnalysisProducer {

    public static final String TOPIC_DATA = "voc_anal_flow_model_tags_result_data";
    public static final String TOPIC_DATA_EXT = "voc_anal_flow_model_tags_result_data_ext";
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void pushData(MessageDTO msg) throws Exception {
        List<Object> list = (List<Object>) msg.getData();
        for (Object obj : list) {
            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
            kafkaTemplate.send(TOPIC_DATA, sendText);
        }
    }
    public void pushExtData(MessageDTO msg) throws Exception {
        List<Object> list = (List<Object>) msg.getData();
        for (Object obj : list) {
            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
            kafkaTemplate.send(TOPIC_DATA_EXT, sendText);
        }
    }

}

