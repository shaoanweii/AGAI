package com.voc.service.data.integration.consuers.kafka;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.model.AysPostprocessDataModel;
import com.voc.service.data.integration.api.model.LabeledResultDataModel;
import com.voc.service.data.integration.producers.kafka.LabeledResultDataProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 * @Description 再推送数据清洗服务前的【数据结果表】
 */
@Component("labeledResultData.cusumer.kafka")
public class LabeledResultDataCusumer {
    //数据清洗结果数据表对应TOPIC
    public static final String TOPIC_PROCESS_POST_DATA = "VDP_processPostRulesData_764547797eb2e192763f5334028d49c9";
    private static final Logger log = LoggerFactory.getLogger(LabeledResultDataCusumer.class);

    @Autowired
    LabeledResultDataProducer labeledResultDataProducer;

    /**
     * 打标后的数据推送服务入口
     */
//    @KafkaListener(topics = {TOPIC_PROCESS_POST_DATA}, groupId = "${data.integration.mpp.customer.group}")
    public void onMessage(@Payload String message) {
        log.debug(">>>>>>> 收到 {} {}的请求 <<<<<<<<<<<<<<", TOPIC_PROCESS_POST_DATA, message);

        try {
            if (StrUtil.isBlank(message)) {
                return;
            }
            final AysPostprocessDataModel model;
            try {
                model = JSONUtil.toBean(message, AysPostprocessDataModel.class);
                log.debug("对象转换完成");
            } catch (Exception e) {
                throw new Exception("数据解析失败 ".concat(message));
            }

            //完成数据整合后发不到输出
            final LabeledResultDataModel output = this.cenvertToModel(model);
            log.debug("数据转换完成: {}",output);
            labeledResultDataProducer.pushData(MessageDTO.builder().data(output).build());
            log.info("数据发送完成： id:{} ,data_id: {}",output.getId(),output.getDataId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 完成数据转换
     * ays_post_process_data -》 dm_labeled_result_data
     * @param model
     * @return
     */
    private LabeledResultDataModel cenvertToModel(AysPostprocessDataModel model) {
        //TODO 待实现 ckcui
        return LabeledResultDataModel.builder().build();
    }
}

