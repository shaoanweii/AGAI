package com.voc.service.analysis.core.v2.producers.kafka;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
@Component("processPreRules.producer.kafka")
public class ProcessPreRulesProducer {
    public static final String TOPIC_EVENT = "voc_anal_flow_pre_rules_event";
    public static final String TOPIC_DATA = "voc_anal_flow_pre_rules_result_data";
    //    public static final String TOPIC_DEL_DATA = "voc_processPreRulesDataDel";
    public static final String TOPIC_EVENT_ABANDON = "voc_anal_flow_pre_rules_abandon";

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;


    public void abandonData(MessageDTO msg) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
        final List<String> ids = JSONUtil.toList(JSONUtil.parseArray(msg.getData()), String.class);
        for (String id : ids) {
            Map<String, String> map = Map.of("id", id);
            kafkaTemplate.send(TOPIC_EVENT_ABANDON, JSONUtil.toJsonStr(map));
        }
    }

    /*public void deleteData(MessageDTO msg) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
        final String str = JSONUtil.toJsonStr(msg);
        kafkaTemplate.send(TOPIC_DEL_DATA, str);
    }*/

    public void pushEvent(MessageDTO msg) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
        final String str = JSONUtil.toJsonStr(msg);
        kafkaTemplate.send(TOPIC_EVENT, str);
    }

    public void pushData(MessageDTO msg) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
//        final String str = Base64.encode(JSONUtil.toJsonStr(msg), CharsetUtil.CHARSET_UTF_8);
//        final String str = JSONUtil.toJsonStr(msg.getData());
        List<Object> list = (List<Object>) msg.getData();

        for (Object obj : list) {
            /*final Object data = BeanUtil.getFieldValue(obj, "data");
            if (ObjUtil.isNotNull(data)) {
                String d = String.valueOf(data).replaceAll("\"", "");
                BeanUtil.setFieldValue(obj, "data", d);
            }*/
            Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "msg.getSource() cannot be empty");
            final String clientId = msg.getSource();

            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
            kafkaTemplate.send(TOPIC_DATA, sendText);

        }
    }

}

