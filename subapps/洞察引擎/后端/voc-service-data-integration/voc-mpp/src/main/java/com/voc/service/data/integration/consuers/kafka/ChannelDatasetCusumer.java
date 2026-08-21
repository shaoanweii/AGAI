package com.voc.service.data.integration.consuers.kafka;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.common.util.IdWorker;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.voc.service.data.integration.nodes.context.ChannelDatasetContext;
import com.voc.service.data.integration.producers.kafka.ChannelDatasetProducer;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CatListener
 * @createTime 2024年03月14日 9:46
 * @Copyright cuick
 * @Description 再推送数据清洗服务前的【数据结果表】
 */
@Component("channelDataset.cusumer.kafka")
public class ChannelDatasetCusumer {

    private static final Logger log = LoggerFactory.getLogger(ChannelDatasetCusumer.class);
    @Resource
    private RedissonClient redissonClient;
    RLock rlock;
    @Resource
    private FlowExecutor flowExecutor;
    @Autowired
    DataIntegrationConfig config;

    @KafkaListener(topics = {ChannelDatasetProducer.TOPIC_MPP_INPUT_CHANNEL}, groupId = "${data.integration.mpp.customer.group}")
    public void onMessage(@Payload String message) {
        log.info(">>>>>>> 收到 {} {}的请求 <<<<<<<<<<<<<<", ChannelDatasetProducer.TOPIC_MPP_INPUT_CHANNEL);
        MessageDTO dto = null;
        try {
            dto = JSONUtil.toBean(message, MessageDTO.class);
            if (ObjUtil.isNull(dto)) {
                //           if (true) {
                log.error("dto {}", dto);
                return;
            }

            final List<DataIntegrationRecordModel> channelDatasetList = JSONUtil.toList(String.valueOf(dto.getData()), DataIntegrationRecordModel.class);
            log.info("接收到的数据 {}", channelDatasetList.size());
            if (CollUtil.isEmpty(channelDatasetList)) {
                log.error("ID集合未空 {}", message);
                return;
            }

            ChannelDatasetContext context = ChannelDatasetContext.builder().build();
            try {
                context.setClientId(config.getClientId());
                context.setChannelType("");
                context.setWorkId(IdWorker.getId());
                context.setChannelDataset(channelDatasetList);

                // 记录开始时间
                long startTime = System.currentTimeMillis();
                log.info(">>>>>>>>>>>>>>>>> 开始处理 {} 的请求 <<<<<<<<<<<<<<<<<<<<<<", context.getWorkId());
                // 执行原有逻辑
//                LiteflowResponse response = flowExecutor.execute2Resp("mpp_input_channel_order_type_flow", context,context.getWorkId());
                LiteflowResponse response = flowExecutor.execute2RespWithRid("mpp_input_channel_order_type_flow", context, context.getWorkId());
                if (!response.isSuccess()) {
                    log.error("workId:{}  {}", context.getWorkId(), response.getCause());
                    throw response.getCause();
                }

                // 计算并记录执行耗时
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime) / 1000; // 转换为秒
                log.info("工作ID: {} 执行耗时: {} 秒", context.getWorkId(), duration);
            } catch (Exception e) {
                log.error("error:{} ids: {}", e.getMessage(), channelDatasetList);
                log.error(e.getMessage(), e);
            }finally {
                log.info("workId {} 完成 ", context.getWorkId());
            }

        } catch (Exception e) {
            log.error("{} 异常:{}", ChannelDatasetProducer.TOPIC_MPP_INPUT_CHANNEL, e.getMessage());
            log.error(e.getMessage(), e);
        }
    }
}

