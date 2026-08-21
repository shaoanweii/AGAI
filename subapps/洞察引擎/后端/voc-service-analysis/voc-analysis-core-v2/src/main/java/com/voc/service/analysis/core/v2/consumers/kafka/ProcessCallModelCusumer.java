package com.voc.service.analysis.core.v2.consumers.kafka;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.api.IAysPreprocessDataService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.BatchPushProducer;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessCallModelProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.dto.MessageExt;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 */
@Component("processCallModel.consumer.kafka")
public class ProcessCallModelCusumer {


    private static final Logger log = LoggerFactory.getLogger(ProcessCallModelCusumer.class);
    @Autowired
    ProcessCallModelProducer processCallModelProducer;
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private FlowExecutor flowExecutor;
    @Autowired
    IAysPreprocessDataService preprocessDataService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    AnalysisConfig config;

    @Autowired
    IAysBatchPushRecordV2Service batchPushRecordV2Service;
//    static  TimedCache<String, Integer> temp = CacheUtil.newTimedCache(1000 * 60 * 60 * 10);

    @KafkaListener(topics = {ProcessCallModelProducer.TOPIC_EVENT}, groupId = "${kafkaEvent.groupId}", concurrency = "15")
    public void onMessage(String message) {
        log.debug(">>>>>>> 收到 {} 的请求 <<<<<<<<<<<<<<");
        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();
        MessageDTO dto = null;
        cn.hutool.core.date.StopWatch stopWatch = new StopWatch();
        stopWatch.start("消息处理开始".concat(ProcessCallModelProducer.TOPIC_EVENT));

        try {
            dto = JSONUtil.toBean(message, MessageDTO.class);
            if (ObjUtil.isNull(dto)) {
                log.error("dto {}", dto);
                return;
            }
            log.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< {}", dto.getData());
            final List<String> ids = JSONUtil.toList(JSONUtil.parseArray(dto.getData()), String.class);
            context.setIds(new HashSet<>(ids));
            Assert.isTrue(StrUtil.isNotBlank(dto.getSource()), "source cannot be empty");
            context.setClientId(dto.getSource());
            context.setWorkflowType(dto.getType());

            final Optional<MessageExt> retry = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny();
            if (retry.isPresent()) {
                if (ObjectUtil.isNotNull(retry.get().getValue()) && (int) retry.get().getValue() >= 20) {
                    log.error(">>>>>>> 重试次数过多，放弃重试 <<<<<<<<<<<<<< {}", dto.getData());
                    return;
                }
            }

            if (config.isDiscardDataItems()) {
                LiteflowResponse response = flowExecutor.execute2Resp( "call_model_flow_mq_push", context,context.getWorkId());
                if (!response.isSuccess()) {
                    log.error("workId:{}  {}", context.getWorkId(), response.getCause());
                    throw new Exception(response.getCause().getMessage());
                }
            }

        } catch (RetryException e) {
            this.onError(dto);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("workId {} 完成 ", context.getWorkId());
            context.getStopWatch().prettyPrint();
            stopWatch.stop();
            log.info("消息处理总耗时：{}：{}", ProcessCallModelProducer.TOPIC_EVENT, stopWatch.prettyPrint(java.util.concurrent.TimeUnit.MILLISECONDS));
        }
    }

    /**
     * 异常处理
     */
    private void onError(MessageDTO dto) {
        try {
            MessageExt retry = dto.getExt().stream().filter(att -> att.getKey().equals("retry")).findAny().orElse(MessageExt.builder().key("retry").value(0).build());
            retry.setValue((int) retry.getValue() + 1);
            dto.getExt().add(retry);
  //          processCallModelProducer.pushEvent(dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}

