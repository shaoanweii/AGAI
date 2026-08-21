package com.voc.service.components.kafka.web;

import com.voc.service.common.response.Result;
import com.voc.service.components.kafka.config.TopicConfig;
import lombok.Cleanup;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * voc总览
 */
@RestController
@RequestMapping("/kafka")
public class ToolController {
    private static final Logger log = LoggerFactory.getLogger(ToolController.class);
    @Autowired
    KafkaAdmin kafkaAdmin;
    @Autowired
    TopicConfig config;

    @PostMapping("/list")
    public Result<?> test(String groupId) throws ExecutionException, InterruptedException {
       @Cleanup
        AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
        Map<String, TopicListing> map = adminClient.listTopics().namesToListings().get();


        Set<String> v = map.keySet().stream().filter(topic -> topic.startsWith("VDP_"))
                .filter(topic -> config.getTopicList().contains(topic))
                .collect(Collectors.toSet());
        log.info("topic: {}", v);

         /*
        Object rs = map.keySet().stream()
                .filter(topic -> topic.startsWith("VDP_"))
                .filter(topic -> config.getTopicList().contains(topic))
                .map(topic -> {
                    try {

                        List<TopicPartition> topicPartitions = new ArrayList<>();
                        topicPartitions.add(new TopicPartition(topic, 1));
                        Map<TopicPartition, OffsetSpec> map_ = topicPartitions.stream().collect(Collectors.toMap(tp -> tp, tp -> OffsetSpec.latest()));

                        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> endOffsets = adminClient.listOffsets(map_).all().get();

//        Map<TopicPartition, Long> endOffsets = adminClient.listOffsets(
//        ).all().get();

                        // 获取消费者组的偏移量
                        ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(groupId);
                        Map<TopicPartition, OffsetAndMetadata> consumerOffsets = offsetsResult.partitionsToOffsetAndMetadata().get();

                        // 计算已消费和未消费的数据量
                        for (TopicPartition tp : topicPartitions) {
                            ListOffsetsResult.ListOffsetsResultInfo endOffset = endOffsets.get(tp);
                            long consumerOffset = consumerOffsets.getOrDefault(tp, new OffsetAndMetadata(0L)).offset();

                            long unConsumedDataSize = endOffset.offset() - consumerOffset;
                            long consumedDataSize = consumerOffset;

                            log.info("  Topic: " + tp.topic() + ", Partition: " + tp.partition());
                            log.info("  End Offset: " + endOffset);
                            log.info("  Consumer Offset: " + consumerOffset);
                            log.info("  Unconsumed Data Size: " + unConsumedDataSize);
                            log.info("  Consumed Data Size: " + consumedDataSize);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }).collect(Collectors.toSet());

        // 获取每个分区的最新偏移量
//        adminClient.listTopics().namesToListings().get().
*/

        return Result.OK();
    }
}
