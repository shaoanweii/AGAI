package com.voc.service.components.milvus.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/2 下午2:57
 * @描述:
 **/
@Configuration
@Data
public class MilvusConfig {
    private static final Logger log = LoggerFactory.getLogger(MilvusConfig.class);
    @Value("${milvus.host}")
    String host;
    @Value("${milvus.port}")
    Integer port;
    @Value("${milvus.database}")
    String database;
    @Value("${milvus.username}")
    String username;
    @Value("${milvus.password}")
    String password;


    @Bean
    public MilvusServiceClient milvusServiceClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .withAuthorization(username, password)
                .withDatabaseName(database)
                .build();
        log.info("初始化milvus->host:{},port:{},userName:{},password:{},databaseName:{}", host, port, username, password, database);
        return new MilvusServiceClient(connectParam);
    }

}
