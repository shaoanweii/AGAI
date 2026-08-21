package com.voc.service.components.kafka.runners;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.components.kafka.config.TopicConfig;
import lombok.Cleanup;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Title: ListeningAiResultFlowRunner
 * @Package: com.voc.service.analysis.core.v2.runner
 * @Description:
 * @Author: cuick
 * @Date: 2024/7/30 16:14
 * @Version:1.0
 */
@Component
public class CreateTopicsRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(CreateTopicsRunner.class);
    @Autowired
    TopicConfig config;
    @Autowired
    KafkaAdmin kafkaAdmin;


    public CreateTopicsRunner() {
        log.info("--->> {}", this.getClass().getSimpleName());
    }

    @Override
    public void run(String... args) throws Exception {
        if (CollUtil.isEmpty(config.getTopicList())) {
            return;
        }else{
            log.info("getTopicList is null");
        }
        try {
            log.info("deleteFrist:{}",config.isDeleteFrist());
            @Cleanup
            AdminClient kafkaAdminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
            final Set<String> allList = kafkaAdminClient.listTopics().names().get();
            final Set<String> list = allList.stream().filter(topic -> topic.startsWith("VDP_")).collect(Collectors.toSet());
            log.info("topcis-list:{}",list);
            if (config.isDeleteFrist()) {
                final Set<String> deleteList = config.getTopicList().stream().filter(topic -> list.contains(topic)).collect(Collectors.toSet());
                if (CollUtil.isNotEmpty(deleteList)) {
                    log.info("remove topics: {}", config.getTopicList());
                    kafkaAdminClient.deleteTopics(deleteList);
                }
                return ;
            } else {
                log.info("no need to delete topics");
            }

            log.info("已存在的tipics:{}",list);
            final Set<NewTopic> createList = config.getTopicList().stream()
                    .filter(topic -> !list.contains(topic))
                    .map(topic -> new NewTopic(topic, config.partitions, (short) 1)).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(createList)) {
                log.info("create topics: {}", config.getTopicList());
                kafkaAdminClient.createTopics(createList);
            } else {
                log.info("no need to create topics！");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
