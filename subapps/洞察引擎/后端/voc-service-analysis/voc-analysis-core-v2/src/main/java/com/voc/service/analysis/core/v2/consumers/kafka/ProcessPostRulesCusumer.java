package com.voc.service.analysis.core.v2.consumers.kafka;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.api.IAysPostprocessDataService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessPostRulesProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.dto.MessageExt;
import com.voc.service.analysis.model.AysPreprocessDataModel;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
@Component("processPostRules.consumer.kafka")
public class ProcessPostRulesCusumer {


//    private static final Logger log = LoggerFactory.getLogger(ProcessPostRulesCusumer.class);
//    @Autowired
//    KafkaTemplate<String, String> kafkaTemplate;
//    @Resource
//    private FlowExecutor flowExecutor;
//    @Autowired
//    AnalysisConfig config;
//    @Autowired
//    ProcessPostRulesProducer processPostRulesProducer;
//    @Autowired
//    IAysBatchPushRecordV2Service batchPushRecordV2Service;
//    @Autowired
//    IAysPostprocessDataService postprocessDataService;
//
//    @KafkaListener(topics = {ProcessPostRulesProducer.TOPIC_EVENT}, groupId = "${kafkaEvent.groupId}")
//    public void onMessage(String message) {
//        log.debug(">>>>>>> 收到 {} 的请求 <<<<<<<<<<<<<<", message);
//        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();
//        MessageDTO dto = null;
//        cn.hutool.core.date.StopWatch stopWatch = new StopWatch();
//        stopWatch.start("消息处理开始".concat(ProcessPostRulesProducer.TOPIC_EVENT));
//
//
//        try {
//            dto = JSONUtil.toBean(message, MessageDTO.class);
//            if (ObjUtil.isNull(dto)) {
//                log.error("dto {}", dto);
//                return;
//            }
//            log.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< {}", dto.getData());
//            final List<String> ids = JSONUtil.toList(JSONUtil.parseArray(dto.getData()), String.class);
//            context.setIds(new HashSet<>(ids));
//
//            Assert.isTrue(StrUtil.isNotBlank(dto.getSource()), "source cannot be empty");
//            context.setClientId(dto.getSource());
//            final Optional<MessageExt> modelCount = dto.getExt().stream().filter(att -> att.getKey().equals("count")).findAny();
//            modelCount.ifPresent(messageExt -> context.setModelCount((Integer) messageExt.getValue()));
//            final Optional<MessageExt> retry = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny();
//            retry.ifPresent(messageExt -> context.setRetry((Integer) messageExt.getValue()));
//            if (retry.isPresent()) {
//                if (ObjectUtil.isNotNull(retry.get().getValue()) && (int) retry.get().getValue() >= 100) {
//                    log.error(">>>>>>> 重试次数过多，放弃重试 <<<<<<<<<<<<<< {}", dto.getData());
//                    try {
//                        batchPushRecordV2Service.modifyStatus(context.getClientId(), context.getIds(), "-1", "B");
//                    } catch (Exception e) {
//                        log.error(e.getMessage(), e);
//                    }
//                    return;
//                }
//            }
//
//            if (config.isDiscardDataItems()) {
//                context.getStopWatch().start("执行 post_process_flow");
//                LiteflowResponse response = flowExecutor.execute2Resp("post_process_flow", context,context.getWorkId());
//                if (!response.isSuccess()) {
//                    log.error("workId:{}  {}", context.getWorkId(), response.getCause());
//                    throw response.getCause();
//                }
//            }
//
//        } catch (RetryException e) {
//            this.onError(dto);
//        } catch (Exception e) {
////            ack.nack(1000);
//            log.error(e.getMessage(), e);
//        } finally {
//            log.info("workId {} 完成 ", context.getWorkId());
//            context.getStopWatch().prettyPrint();
//
//            stopWatch.stop();
//            log.info("消息处理总耗时：{}：{}", ProcessPostRulesProducer.TOPIC_EVENT, stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
//        }
//    }
//
//    @KafkaListener(topics = {ProcessPostRulesProducer.TOPIC_DEL_DATA}, groupId = "${kafkaEvent.groupId}")
//    public void onMessage2(String message) {
//        log.debug(">>>>>>> 收到 {} 的请求 <<<<<<<<<<<<<<", message);
//        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();
//        MessageDTO dto = null;
//        cn.hutool.core.date.StopWatch stopWatch = new StopWatch();
//        stopWatch.start("消息处理开始".concat(ProcessPostRulesProducer.TOPIC_DEL_DATA));
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
//                if (ObjectUtil.isNotNull(retry.get().getValue()) && (int) retry.get().getValue() >= 3) {
//                    log.error(">>>>>>> 重试次数过多，放弃重试 <<<<<<<<<<<<<< {}", dto.getData());
//                    try {
//                        batchPushRecordV2Service.modifyStatus(context.getClientId(), context.getIds(), "-1", "A");
//                    } catch (Exception e) {
//                        log.error(e.getMessage(), e);
//                    }
//                    return;
//                }
//            }
//
//            List<AysPreprocessDataModel> findModelList = postprocessDataService.findByIds(dto.getSource(), new HashSet<>(ids));
//            final List<String> findIds = findModelList.stream().map(AysPreprocessDataModel::getDataId).collect(Collectors.toList());
//
//            //提取已完成落库的数据进行删除
//            context.getStopWatch().start("删除异常数据开始");
//            final Collection<String> finalIds = CollUtil.intersection(ids, findIds);
//            postprocessDataService.removeDB(dto.getSource(), new HashSet<>(finalIds));
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
//                    Thread.sleep(1000);
//                    processPostRulesProducer.deleteData(retryDto);
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
//            log.info("消息处理总耗时：{}：{}", ProcessPostRulesProducer.TOPIC_DEL_DATA, stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
//        }
//    }
//
//    /**
//     * 异常处理
//     */
//    private void onError(MessageDTO dto) {
//        try {
//            MessageExt retry = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny().orElse(MessageExt.builder().key("retry").value(0).build());
//            retry.setValue((int) retry.getValue() + 1);
//            dto.getExt().add(retry);
//            processPostRulesProducer.pushEvent(dto);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//    }
}


