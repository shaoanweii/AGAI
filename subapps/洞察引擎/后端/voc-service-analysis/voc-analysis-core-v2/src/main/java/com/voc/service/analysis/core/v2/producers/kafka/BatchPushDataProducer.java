package com.voc.service.analysis.core.v2.producers.kafka;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
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
@Component("batchPushData.producer.kafka")
public class BatchPushDataProducer {

    public static final String TOPIC_DATA = "VDP_voc2_batch_push_record";
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    AnalysisConfig config;

    public void pushData(MessageDTO msg) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
//        final String str = Base64.encode(JSONUtil.toJsonStr(msg), CharsetUtil.CHARSET_UTF_8);
//        final String str = JSONUtil.toJsonStr(msg.getData());
        List<Object> list = (List<Object>) msg.getData();

        Set<String> pushIds = Collections.synchronizedSet(new HashSet<>());
        for (Object obj : list) {

            final Object id = BeanUtil.getFieldValue(obj, "newId");
            if (ObjUtil.isNotNull(id)) {
                pushIds.add(String.valueOf(id));
            }
            /*final Object data = BeanUtil.getFieldValue(obj, "data");
            if (ObjUtil.isNotNull(data)) {
                String d = Base64.encode(String.valueOf(data), CharsetUtil.CHARSET_UTF_8);
                BeanUtil.setFieldValue(obj, "data", d);
            }*/
            Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "msg.getSource() cannot be empty");
            final String clientId =msg.getSource();

//            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
//            kafkaTemplate.send(TOPIC_DATA,sendText);
        }
    }

}

