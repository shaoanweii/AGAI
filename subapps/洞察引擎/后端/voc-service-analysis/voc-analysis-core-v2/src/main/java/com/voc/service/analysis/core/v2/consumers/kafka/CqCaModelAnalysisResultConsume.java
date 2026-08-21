package com.voc.service.analysis.core.v2.consumers.kafka;

import cn.hutool.core.date.StopWatch;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.largeModel.ModelTopicRequest;
import com.voc.service.analysis.largeModel.vo.ModelResponseVo;
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Component("cqCaModelResultConsume.consumer.kafka")
public class CqCaModelAnalysisResultConsume {

    public static final String TOPIC_DATA = "voc_final_topic";
    private static final Logger log = LoggerFactory.getLogger(CqCaModelAnalysisResultConsume.class);
    @Autowired
    AnalysisConfig config;
    @Resource
    private FlowExecutor flowExecutor;

    @KafkaListener(topics = {CqCaModelAnalysisResultConsume.TOPIC_DATA}, groupId = "${kafkaEvent.groupId}")
    public void onMessage(String message) {
        log.debug(">>>>>>> 收到 {} 的请求 <<<<<<<<<<<<<<");
        cn.hutool.core.date.StopWatch stopWatch = new StopWatch();
        stopWatch.start("消息处理开始".concat(CqCaModelAnalysisResultConsume.TOPIC_DATA));

        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();
        try {
            log.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< ");
            ModelResponseVo resultData = JSONUtil.toBean(JSONUtil.toJsonStr(message), ModelResponseVo.class);
            log.info("MQ消息模型结果数据:{}", resultData);
            Assert.isTrue(ObjectUtils.isNotEmpty(resultData), "source cannot be empty");
            if (Objects.isNull(resultData.getNlpParam())) {
                log.error("MQ消息模型结果数据不合法");
                return;
            }
            ModelTopicRequest nlpParam = resultData.getNlpParam();
            Set<String> idList = new HashSet<>();
            idList.add(nlpParam.getTopic_id());
            log.info("要查询的前置数据ID:{}", idList);
            context.setModelResponseVo(resultData);
            if (config.isDiscardDataItems()) {
                LiteflowResponse response = flowExecutor.execute2Resp("call_model_flow_mq_receive", context, context.getWorkId());
                if (!response.isSuccess()) {
                    log.error("workId:{}{}", context.getWorkId(), response.getCause());
                    throw new Exception(response.getCause().getMessage());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("workId {} 完成 ", context.getWorkId());
            context.getStopWatch().prettyPrint();
            stopWatch.stop();
            log.info("消息处理总耗时：{}：{}", CqCaModelAnalysisResultConsume.TOPIC_DATA, stopWatch.prettyPrint(java.util.concurrent.TimeUnit.MILLISECONDS));
        }
    }
}
