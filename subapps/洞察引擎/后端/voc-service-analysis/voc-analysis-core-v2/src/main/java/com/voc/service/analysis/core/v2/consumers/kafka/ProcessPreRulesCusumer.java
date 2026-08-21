package com.voc.service.analysis.core.v2.consumers.kafka;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.api.IAysPreprocessDataService;
import com.voc.service.analysis.api.IStaticDataServcie;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessPreRulesProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.dto.MessageExt;
import com.voc.service.analysis.model.AysPreprocessDataModel;
import com.voc.service.common.util.IdWorker;
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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
@Component("processPreRules.consumer.kafka")
public class ProcessPreRulesCusumer {


    private static final Logger log = LoggerFactory.getLogger(ProcessPreRulesCusumer.class);
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private FlowExecutor flowExecutor;
    @Autowired
    AnalysisConfig config;
    @Autowired
    ProcessPreRulesProducer processPreRulesProducer;
    @Autowired
    IAysPreprocessDataService preprocessDataService;
    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;
    @Autowired
    IStaticDataServcie staticDataServcie;

    @KafkaListener(topics = {ProcessPreRulesProducer.TOPIC_EVENT}, groupId = "${kafkaEvent.groupId}")
    public void onMessage(String message) {
        log.debug(">>>>>>> 收到前置 {} 的请求 <<<<<<<<<<<<<<", message);
        if (config.isCleanProcessPreRulesEventTopic()) {
            log.warn("清理:{} ", ProcessPreRulesProducer.TOPIC_EVENT);
            return;
        }
        AnlysisDefaultContext context = AnlysisDefaultContext.builder().workId(IdWorker.getId()).build();
        MessageDTO dto = null;

        cn.hutool.core.date.StopWatch stopWatch = new StopWatch();
        stopWatch.start("消息处理开始".concat(ProcessPreRulesProducer.TOPIC_EVENT).concat(context.getWorkId()));

        try {
            dto = JSONUtil.toBean(message, MessageDTO.class);
            if (ObjUtil.isNull(dto)) {
                log.error("dto {}", dto);
                return;
            }

            log.info(">>>>>>> 开始执行前置任务 <<<<<<<<<<<<<< {} {}", context.getWorkId(), dto.getToken());
            final List<String> ids = JSONUtil.toList(JSONUtil.parseArray(dto.getData()), String.class);
            if (CollectionUtil.isEmpty(ids)) {
                log.error(">>>>>>> ids cannot be empty <<<<<<<<<<<<<< {} {}", context.getWorkId(), dto.getData());
                return;
            }

            context.setIds(new HashSet<>(ids));
            context.setWorkflowType(dto.getType());

            Assert.isTrue(StrUtil.isNotBlank(dto.getSource()), "source cannot be empty");
            context.setClientId(dto.getSource());

            //保存解析后的数据， 再过程中异常数据将被遗弃
            final Optional<MessageExt> retry = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny();
            if (retry.isPresent()) {
                if (ObjectUtil.isNotNull(retry.get().getValue()) && (int) retry.get().getValue() >= 10) {
                    log.error(">>>>>>> 重试次数过多，放弃重试 <<<<<<<<<<<<<< {} {}", context.getWorkId(), dto.getData());
                    processPreRulesProducer.abandonData(dto);
                    return;
                }
            }
            log.info("前置正常执行 {}", context.getWorkId());
            /**
             * 实现异步保存数据时，只有当数据[数据分析+批次记录]落地后，才执行
             */
            //判断数据是否落地
            log.info("context.getIds() {}:{}", context.getWorkId(), context.getIds().size());
            log.debug("context.getIds() {}:{}", context.getWorkId(), context.getIds().size(), context.getIds());
            final Set<String> savedProcessData = metaDataAnalysisService.isExitsIds(context.getClientId(), context.getIds());
            log.info("savedProcessData {}:{}", context.getWorkId(), savedProcessData.size());
            log.debug("savedProcessData {}:{}", context.getWorkId(), savedProcessData.size(), savedProcessData);

            //判断数据是否一致
            if (CollectionUtil.isEmpty(savedProcessData)) {
                log.info("查询原始数据为空");
                //数据不一致是重跑
                log.info("重新执行");
                MessageDTO retryDto = MessageDTO.builder().source(dto.getSource()).data(dto.getData()).type(dto.getType()).ext(dto.getExt()).build();
                MessageExt retryExt = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny()
                        .orElse(MessageExt.builder().key("retry").value(0).build());
                retryExt.setValue((int) retryExt.getValue() + 1);
                retryDto.getExt().add(retryExt);
                Thread.sleep(1500);
                retryDto.setToken("重新写入：重新执行");
                processPreRulesProducer.pushEvent(retryDto);
                return;
            } else {
                log.info("原始数据部分已经落表处理前置 {}:{}", context.getIds().size(), savedProcessData.size());
                Set<String> paramIds = context.getIds();
                paramIds = paramIds.stream().filter(id -> !savedProcessData.contains(id)).collect(Collectors.toSet());
                log.info("原始数据还没有落表的数据 {}:{}", context.getWorkId(), paramIds.size());
                if (CollectionUtil.isNotEmpty(paramIds)) {
                    MessageDTO retryDto = MessageDTO.builder().source(dto.getSource()).data(paramIds).type(dto.getType()).ext(dto.getExt()).build();
                    MessageExt retryExt = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny()
                            .orElse(MessageExt.builder().key("retry").value(0).build());
                    retryExt.setValue((int) retryExt.getValue() + 1);
                    retryDto.getExt().add(retryExt);
                    Thread.sleep(1500);
                    retryDto.setToken("部分重新写入：重新执行");
                    processPreRulesProducer.pushEvent(retryDto);
                }
                context.setIds(savedProcessData);
            }

            if (config.isDiscardDataItems()) {
                log.info("调用流程：{}", "pre_process_flow {}", context.getWorkId());
                context.getStopWatch().start("执行 pre_process_flow {}".concat(context.getWorkId()));

                log.info("加载动态资源-》getResourceGroup 开始 {}", context.getWorkId());
                staticDataServcie.getResourceGroup(context.getClientId());
                log.info("加载动态资源-》getResourceGroup 结束 {}", context.getWorkId());

                LiteflowResponse response = flowExecutor.execute2Resp("pre_process_flow", context, context.getWorkId());
                if (!response.isSuccess()) {
                    log.error("workId:{}  {}", context.getWorkId(), response.getCause());
                    throw response.getCause();
                }
            } else {
                log.warn("跳过流程：{}", "pre_process_flow {}", context.getWorkId());
            }
        } catch (RetryException e) {
            this.onError(dto, e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("workId {} 完成 ", context.getWorkId());
            context.getStopWatch().prettyPrint();

            stopWatch.stop();
            log.info("消息处理总耗时：{} {}：{}", context.getWorkId(), ProcessPreRulesProducer.TOPIC_EVENT, stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        }
    }

    /*@KafkaListener(topics = {ProcessPreRulesProducer.TOPIC_DEL_DATA}, groupId = "${kafkaEvent.groupId}")
    public void onMessage2(String message) {
        log.debug(">>>>>>> 收到 {} 的请求 <<<<<<<<<<<<<<", message);
        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();
        MessageDTO dto = null;
        cn.hutool.core.date.StopWatch stopWatch = new StopWatch();
        stopWatch.start("消息处理开始".concat(ProcessPreRulesProducer.TOPIC_DEL_DATA));
        try {
            dto = JSONUtil.toBean(message, MessageDTO.class);
            if (ObjUtil.isNull(dto)) {
                log.error("dto {}", dto);
                return;
            }
            log.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< {}", dto.getData());
            final List<String> ids = JSONUtil.toList(JSONUtil.parseArray(dto.getData()), String.class);
            Assert.isTrue(StrUtil.isNotBlank(dto.getSource()), "source cannot be empty");


            final Optional<MessageExt> retry = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny();
            if (retry.isPresent()) {
                if (ObjectUtil.isNotNull(retry.get().getValue()) && (int) retry.get().getValue() >= 10) {
                    log.error(">>>>>>> 重试次数过多，放弃重试 <<<<<<<<<<<<<< {}", dto.getData());
                    return;
                }
            }

            List<AysPreprocessDataModel> findModelList = preprocessDataService.findByIds(dto.getSource(), new HashSet<>(ids));
            final List<String> findIds = findModelList.stream().map(AysPreprocessDataModel::getDataId).collect(Collectors.toList());

            //提取已完成落库的数据进行删除
            context.getStopWatch().start("删除异常数据开始");
            final Collection<String> finalIds = CollUtil.intersection(ids, findIds);
            preprocessDataService.removeDB(dto.getSource(), new HashSet<>(finalIds));

            if (!findIds.containsAll(ids)) {
                HashSet<String> retryIds = new HashSet<>(ids);
                retryIds.removeAll(finalIds);
                if (CollUtil.isNotEmpty(retryIds)) {
                    log.info("重试删除数据 {}", retryIds);
                    MessageDTO retryDto = MessageDTO.builder().source(dto.getSource()).data(retryIds).build();
                    MessageExt retryExt = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny()
                            .orElse(MessageExt.builder().key("retry").value(0).build());
                    retryExt.setValue((int) retryExt.getValue() + 1);
                    retryDto.getExt().add(retryExt);
                    Thread.sleep(1000);
                    processPreRulesProducer.deleteData(retryDto);
                }
            }

        } catch (Exception e) {
//            ack.nack(1000);
            log.error(e.getMessage(), e);
        } finally {
            log.info("workId {} 完成 ", context.getWorkId());
            context.getStopWatch().prettyPrint();

            stopWatch.stop();
            log.info("消息处理总耗时：{}：{}", ProcessPreRulesProducer.TOPIC_DEL_DATA, stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        }
    }*/

    /**
     * 异常处理
     */
    private void onError(MessageDTO dto, String msg) {
        try {
            MessageExt retry = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny().orElse(MessageExt.builder().key("retry").value(0).build());
            retry.setValue((int) retry.getValue() + 1);
            dto.getExt().add(retry);
            dto.setToken("重新写入：onError:".concat(msg));
            //         processPreRulesProducer.pushEvent(dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

