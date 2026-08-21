package com.voc.service.analysis.core.v2.producers.kafka;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
@Component("metaDataAnalysisMiss.producer.kafka")
public class CaCaModelResultAnalysisMissProducer {
    public static final String TOPIC_DATA = "voc_anal_flow_model_tags_unlabeled_data";
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void pushData(MessageDTO msg) throws Exception {
        List<Object> list = (List<Object>) msg.getData();

        Set<String> pushIds = Collections.synchronizedSet(new HashSet<>());
        for (Object obj : list) {
            final Object id = BeanUtil.getFieldValue(obj, "newId");
            if (ObjUtil.isNotNull(id)) {
                pushIds.add(String.valueOf(id));
            }
            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
            kafkaTemplate.send(TOPIC_DATA, sendText);
        }
    }

}

