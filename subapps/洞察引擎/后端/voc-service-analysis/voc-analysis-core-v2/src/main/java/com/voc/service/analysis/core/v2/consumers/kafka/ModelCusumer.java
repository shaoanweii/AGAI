package com.voc.service.analysis.core.v2.consumers.kafka;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysModelResltService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.ModelProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.dto.MessageExt;
import com.voc.service.analysis.largeModel.ModelTopicRequest;
import com.voc.service.analysis.largeModel.vo.ModelResponseVo;
import com.voc.service.analysis.largeModel.vo.NlpResult;
import com.voc.service.analysis.model.AiData;
import com.voc.service.analysis.model.AiResultDataModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.analysis.model.ResultData;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
@Component("model.consumer.kafka")
public class ModelCusumer {

//    public static final String TOPIC_DATA = "final_topic";
//    private static final Logger log = LoggerFactory.getLogger(ModelCusumer.class);
//    @Autowired
//    AnalysisConfig config;
//    @Resource
//    private FlowExecutor flowExecutor;
//
//    @Autowired
//    ModelProducer modelProducer;
//    @Autowired
//    IAysModelResltService modelResltService;
//
//    @KafkaListener(topics = {ModelCusumer.TOPIC_DATA}, groupId = "${kafkaEvent.groupId}")
//    public void onMessage(String message) {
//        log.debug(">>>>>>> 收到 {} 的请求 <<<<<<<<<<<<<<", message);
//        cn.hutool.core.date.StopWatch stopWatch = new StopWatch();
//        stopWatch.start("消息处理开始".concat(ModelCusumer.TOPIC_DATA));
//
//        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();
//        try {
//            log.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< ");
//            ModelResponseVo resultData = JSONUtil.toBean(JSONUtil.toJsonStr(message), ModelResponseVo.class);
//            log.info("MQ消息模型结果数据:{}", resultData);
//            Assert.isTrue(ObjectUtils.isNotEmpty(resultData), "source cannot be empty");
//            if (Objects.isNull(resultData.getNlpParam())) {
//                log.error("MQ消息模型结果数据不合法");
//                return;
//            }
//            NlpResult nlpResult = resultData.getNlpResult();
//            ModelTopicRequest nlpParam = resultData.getNlpParam();
//            Set<String> idList = new HashSet<>();
//            idList.add(nlpParam.getTopic_id());
//            log.info("要查询的前置数据ID:{}", idList);
//            context.setIds(idList);
//            context.setModelResponseVo(resultData);
//            if (config.isDiscardDataItems()) {
//                LiteflowResponse response = flowExecutor.execute2Resp("call_model_flow_mq_receive", context, context.getWorkId());
//                if (!response.isSuccess()) {
//                    log.error("workId:{}{}", context.getWorkId(), response.getCause());
//                    throw new Exception(response.getCause().getMessage());
//                }
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        } finally {
//            log.info("workId {} 完成 ", context.getWorkId());
//            context.getStopWatch().prettyPrint();
//
//            stopWatch.stop();
//            log.info("消息处理总耗时：{}：{}", ModelCusumer.TOPIC_DATA, stopWatch.prettyPrint(java.util.concurrent.TimeUnit.MILLISECONDS));
//        }
//    }
//
//    @KafkaListener(topics = {ModelProducer.TOPIC_EVENT_UPDATE}, groupId = "${kafkaEvent.groupId}")
//    public void onMessage2(String message) {
//        log.debug(">>>>>>> 收到 {} 的请求 <<<<<<<<<<<<<<", message);
//        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();
//        MessageDTO dto = null;
//
//        cn.hutool.core.date.StopWatch stopWatch = new StopWatch();
//        stopWatch.start("消息处理开始".concat(ModelProducer.TOPIC_EVENT_UPDATE));
//
//        try {
//            dto = JSONUtil.toBean(message, MessageDTO.class);
//            if (ObjUtil.isNull(dto)) {
//                log.error("dto {}", dto);
//                return;
//            }
//            log.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< {}", dto.getData());
//            final List<String> ids = JSONUtil.toList(JSONUtil.parseArray(dto.getData()), String.class);
//            Assert.isTrue(StrUtil.isNotBlank(dto.getSource()), "source cannot be empty");
//
//
//            final Optional<MessageExt> retry = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny();
//            if (retry.isPresent()) {
//                if (ObjectUtil.isNotNull(retry.get().getValue()) && (int) retry.get().getValue() >= 10) {
//                    log.error(">>>>>>> 重试次数过多，放弃重试 <<<<<<<<<<<<<< {}", dto.getData());
//
//                    return;
//                }
//            }
//
//            List<AysProcessDataModel> findModelList = modelResltService.findByIds(dto.getSource(), new HashSet<>(ids));
//            final Set<String> findIds = findModelList.stream().map(AysProcessDataModel::getId).collect(Collectors.toSet());
//
//            //提取已完成落库的数据进行删除
//            context.getStopWatch().start("删除异常数据开始");
//            final Collection<String> finalIds = CollUtil.intersection(ids, findIds);
//            modelResltService.modifyToDoneDB(dto.getSource(), findIds);
//
//            if (!findIds.containsAll(ids)) {
//                HashSet<String> retryIds = new HashSet<>(ids);
//                retryIds.removeAll(finalIds);
//                if (CollUtil.isNotEmpty(retryIds)) {
//                    log.info("重试删除数据 {}", retryIds);
//                    MessageDTO retryDto = MessageDTO.builder().source(dto.getSource()).data(retryIds).build();
//                    MessageExt retryExt = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny()
//                            .orElse(MessageExt.builder().key("retry").value(0).build());
//                    retryExt.setValue((int) retryExt.getValue() + 1);
//                    retryDto.getExt().add(retryExt);
//                    Thread.sleep(3000);
//                    modelProducer.updateEvent(retryDto);
//                }
//            }
//
//        } catch (Exception e) {
////            ack.nack(1000);
//            log.error(e.getMessage(), e);
//        } finally {
//            log.info("workId {} 完成 ", context.getWorkId());
//            context.getStopWatch().prettyPrint();
//
//            stopWatch.stop();
//            log.info("消息处理总耗时：{}：{}", ModelProducer.TOPIC_EVENT_UPDATE, stopWatch.prettyPrint(java.util.concurrent.TimeUnit.MILLISECONDS));
//        }
//    }
}

