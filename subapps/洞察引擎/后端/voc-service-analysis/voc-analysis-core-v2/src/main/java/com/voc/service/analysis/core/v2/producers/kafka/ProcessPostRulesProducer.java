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
@Component("processPostRules.producer.kafka")
public class ProcessPostRulesProducer {
//    public static final String TOPIC_EVENT = "VDP_processPostRulesEvent";
//    public static final String TOPIC_DATA = "VDP_voc2_post_rules_result_data";
//    public static final String TOPIC_VALID_DATA = "VDP_processValidPostRulesData";
//    public static final String TOPIC_DEL_DATA = "VDP_processPostRulesDataDel";
//
//    @Autowired
//    KafkaTemplate<String, String> kafkaTemplate;
//    @Resource
//    private FlowExecutor flowExecutor;
//    @Autowired
//    AnalysisConfig config;
//
//    public void deleteData(MessageDTO msg) throws Exception {
//        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
//        final String str = JSONUtil.toJsonStr(msg);
//        kafkaTemplate.send(TOPIC_DEL_DATA, str);
//    }
//
//    public void pushEvent(MessageDTO msg) throws Exception {
//        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
////        final String str = Base64.encode(JSONUtil.toJsonStr(msg), CharsetUtil.CHARSET_UTF_8);
//        final String str = JSONUtil.toJsonStr(msg);
//        kafkaTemplate.send(TOPIC_EVENT, str);
//    }
//
//    public void pushData(MessageDTO msg) throws Exception {
//        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
////        final String str = Base64.encode(JSONUtil.toJsonStr(msg), CharsetUtil.CHARSET_UTF_8);
////        final String str = JSONUtil.toJsonStr(msg.getData());
//        List<Object> list = (List<Object>) msg.getData();
//
//        for (Object obj : list) {
//            /*final Object data = BeanUtil.getFieldValue(obj, "data");
//            if (ObjUtil.isNotNull(data)) {
//                String d = String.valueOf(data).replaceAll("\"", "");
//                BeanUtil.setFieldValue(obj, "data", d);
//            }*/
//            Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "msg.getSource() cannot be empty");
//            final String clientId = msg.getSource();
//
//            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
//            kafkaTemplate.send(TOPIC_DATA, sendText);
//        }
//    }
//
//
//    public void pushValidData(MessageDTO msg) throws Exception {
//        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
//        List<Object> list = (List<Object>) msg.getData();
//        for (Object obj : list) {
//            Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "msg.getSource() cannot be empty");
//            final String clientId = msg.getSource();
//
//            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
//            kafkaTemplate.send(TOPIC_VALID_DATA.concat("_").concat(clientId), sendText);
//        }
//    }

}


