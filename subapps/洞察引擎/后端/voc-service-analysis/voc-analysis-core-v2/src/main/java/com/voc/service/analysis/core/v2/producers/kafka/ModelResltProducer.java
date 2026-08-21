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
@Component("modelReslt.producer.kafka")
public class ModelResltProducer {

    public static final String TOPIC_EVENT = "VDP_modelResltEvent";
    public static final String TOPIC_DATA = "VDP_modelResltData";
    public static final String EVENT_TYPE_UPDATE = "VDP_update";

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private FlowExecutor flowExecutor;
    @Autowired
    AnalysisConfig config;

//    @KafkaListener(topics = {ModelResltCusumer.TOPIC_EVENT}, groupId = "default")
    /*public void onMessage(String message, Acknowledgment ack) {
        log.info(">>>>>>> 收到 {} 的请求 <<<<<<<<<<<<<<", message);

        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();
        try {
            final String str = Base64.decodeStr(message.replaceAll("\"", ""), CharsetUtil.CHARSET_UTF_8);
            final MessageDTO dto = JSONUtil.toBean(str, MessageDTO.class);
            if (ObjUtil.isNull(dto)) {
                log.error("dto {}", dto);
                return;
            }
            log.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< {}", dto.getData());
            if ("".equalsIgnoreCase(dto.getType())) {
                final List<String> ids = JSONUtil.toList(JSONUtil.parseArray(dto.getData()), String.class);
                context.setIds(new HashSet<>(ids));

                //查询数据，当未完成插入数据操作时，再次放入队列中
                if (true) {
                    //更新
//                    resltDataService.modifyToDone(new HashSet<>(ids));
                } else {
                    pushEvent(MessageDTO.builder().data(context.getIds()).type(EVENT_TYPE_UPDATE).build());
                }
            }
            ack.acknowledge();
        } catch (Exception e) {
            ack.nack(1000);
            try {
//                this.pushEvent(MessageDTO.builder().data(context.getIds()).build());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            log.error(e.getMessage(), e);
        } finally {
            log.info("workId {} 完成 ", context.getWorkId());
            context.getStopWatch().prettyPrint();
        }
    }*/

    public void pushEvent(MessageDTO msg) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
        final String str = JSONUtil.toJsonStr(msg);
        kafkaTemplate.send(TOPIC_EVENT, str);
    }


    public void pushData(MessageDTO msg) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "source cannot be empty");
        List<Object> list = (List<Object>) msg.getData();

        for (Object obj : list) {
            /*final Object data = BeanUtil.getFieldValue(obj, "data");
            if (ObjUtil.isNotNull(data)) {
                String d = String.valueOf(data).replaceAll("\"", "");
                BeanUtil.setFieldValue(obj, "data", d);
            }*/
            Assert.isTrue(StrUtil.isNotBlank(msg.getSource()), "msg.getSource() cannot be empty");
            final String clientId =msg.getSource();

            String sendText = JSONUtil.toJsonStr(obj, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
            kafkaTemplate.send(TOPIC_DATA.concat("_").concat(clientId),sendText);
        }
    }
}

