package com.voc.service.components.redis.config;

import com.alibaba.cloud.commons.lang.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.NameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName RedissonConfig
 * @createTime 2024年03月14日 13:10
 * @Copyright futong
 */

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedissonConfig {
    private static final Logger log = LoggerFactory.getLogger(RedissonConfig.class);
    @Setter
    @Getter
    private Cluster cluster = new Cluster();
    @Setter
    private String host;
    @Setter
    private String password;
    @Setter
    private int port;
    @Setter
    private int database;
    @Setter
    private String redissonStartWith = "redis://";

    public RedissonConfig() {
        log.info("--->> {}", this.getClass().getSimpleName());
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.data.redis", name = "type", havingValue = "cluster")
    public RedissonClient getRedissonCluster() {
        log.info("RedissonClient---------->cluster:{} db:{}", cluster.getNodes(), database);
        String[] nodes = cluster.getNodes().stream().map(node -> redissonStartWith.concat(StringUtils.trim(node)))
                .collect(Collectors.toSet()).toArray(new String[cluster.getNodes().size()]);
        log.info("convert:{}", (Object) nodes);
        Config config = new Config();
        config.useClusterServers() //这是用的集群server
                .setNameMapper(new MyNameMapper())  //添加前缀
//                .setScanInterval(redissonScanInterval) //设置集群状态扫描时间
                .addNodeAddress(nodes);
        if (StringUtils.isNotEmpty(password)) {
            config.useClusterServers().setPassword(password);
        }
        return Redisson.create(config);
    }


    @Bean
    @ConditionalOnProperty(prefix = "spring.data.redis", name = "type", havingValue = "host")
    public RedissonClient getRedissonHost() {
        log.info("RedissonClient---------->host:{}:{},db:{}", host, port, database);
        Config config = new Config();
        config.useSingleServer()
                .setNameMapper(new MyNameMapper())  //添加前缀
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database)
                .setPassword(password);
        config.setCodec(new JsonJacksonCodec() {
        });
        return Redisson.create(config);
    }

    class MyNameMapper implements NameMapper {
        @Override
        public String map(String s) {
            return PrefixRedisSerializer.DEFUAL_PREFIX.concat(s);
        }

        @Override
        public String unmap(String s) {
            return PrefixRedisSerializer.DEFUAL_PREFIX.concat(s);
        }
    }

    class Cluster {
        @Setter
        @Getter
        List<String> nodes = new ArrayList<>();
    }
}
