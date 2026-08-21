package com.voc.service.insights.engine.data.consumers.kafka;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.insights.engine.api.IAysMetaDataAnalysisService;
import com.voc.service.insights.engine.api.model.RawDataParamModel;
import com.voc.service.insights.engine.api.model.TaskPartModel;
import com.voc.service.insights.engine.data.producers.kafka.LargeDigitaFileProducer;
import com.voc.service.logs.dto.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
//@Component("largeDigitaFile.consumer.kafka")
public class LargeDigitaFileCusumer {

    private static final Logger log = LoggerFactory.getLogger(LargeDigitaFileCusumer.class);
    @Autowired
    IAysMetaDataAnalysisService aysMetaDataAnalysisService;;

//    @KafkaListener(topics = {LargeDigitaFileProducer.TOPIC_EVENT}, groupId = "${kafkaEvent.groupId}")
    public void onMessage(String message) {
        log.debug(">>>>>>> 收到 {} 的请求 <<<<<<<<<<<<<<", message);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("消息处理开始".concat(LargeDigitaFileProducer.TOPIC_EVENT));
        try {

            final MessageDTO dto = JSONUtil.toBean(message, MessageDTO.class);
            if (ObjUtil.isNull(dto)) {
                log.error("dto {}", dto);
                return;
            }
            Assert.isTrue(StrUtil.isNotBlank(dto.getType()), "getType cannot be empty");
            Assert.isTrue(ObjUtil.isNotNull(dto.getData()),"getData cannot be empty");

            TaskPartModel partTask = JSONUtil.toBean(JSONUtil.parseObj(dto.getData()), TaskPartModel.class);
            final String type = dto.getType();
            if(IAysMetaDataAnalysisService.TYPE.equalsIgnoreCase(type)){
                log.info(">>{}", type);
                RawDataParamModel model = JSONUtil.toBean(JSONUtil.parseObj(partTask.getParamModel()), RawDataParamModel.class);
//                aysMetaDataAnalysisService.exportRawDataResult(partTask,model);
            }


        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            stopWatch.stop();
            log.info("消息处理总耗时：{}：{}", LargeDigitaFileProducer.TOPIC_EVENT, stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        }
    }
}

