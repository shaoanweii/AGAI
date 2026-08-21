package com.voc.service.components.redis.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.MD5;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.lettuce.core.ReadFrom;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.*;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Configuration
@EnableCaching
@SuppressWarnings(value={"unchecked", "deprecation"})
public class RedisConfig extends CachingConfigurerSupport {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);
    @Autowired
    RedisProperties redisProperties;
    public RedisConfig() {
        log.info("--->> {}", this.getClass().getSimpleName());
    }
    @Lazy
    @PostConstruct
    public void init() {
//        log.info("--->> redis: {}:{}", redisProperties.getHost(), redisProperties.getPort());
        log.info("--->> redis.database: {}", redisProperties.getDatabase());
        if (ObjectUtil.isNotNull(redisProperties.getUrl())) {
            log.info("--->> redis.url{}:{}", redisProperties.getUrl(), redisProperties.getPort());
        }
        if (ObjectUtil.isNotNull(redisProperties.getCluster())) {
            log.info("--->> cluster: {}", redisProperties.getCluster().getNodes());
        }
        if (ObjectUtil.isNotNull(redisProperties.getSentinel())) {
            log.info("--->> sentinel: {}", redisProperties.getSentinel().getNodes());
        }
        if (ObjectUtil.isNotNull(redisProperties.getLettuce())) {
            log.info("--->> lettuce: {}", redisProperties.getLettuce());
        }
    }

    /**
     * RedisTemplate模板
     */
    @Bean("redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, LettuceConnectionFactory lettuceConnectionFactory) {
        log.info("--->> init {}" , this.getClass().getSimpleName());

        ObjectMapper om = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(om, Object.class);
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME.localizedBy(Locale.CHINA)));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME.localizedBy(Locale.CHINA)));
        om.registerModule(javaTimeModule);

        // 配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
//        RedisSerializer<?> stringSerializer = new StringRedisSerializer();
        RedisSerializer<?> stringSerializer = new PrefixRedisSerializer();
        // key序列化
        redisTemplate.setKeySerializer(stringSerializer);
        // value序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        // Hash key序列化
        redisTemplate.setHashKeySerializer(stringSerializer);
        // Hash value序列化
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * StringRedisTemplate模板
     */
    @Bean("strRedisTemplate")
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory, LettuceConnectionFactory lettuceConnectionFactory) {
        log.info("--->> init stringRedisTemplate");

        ObjectMapper om = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        jackson2JsonRedisSerializer.setObjectMapper(om);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(om, Object.class);
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME.localizedBy(Locale.CHINA)));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME.localizedBy(Locale.CHINA)));
        om.registerModule(javaTimeModule);
        // 配置redisTemplate
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
//        RedisSerializer<?> stringSerializer = new StringRedisSerializer();
        RedisSerializer<?> stringSerializer = new PrefixRedisSerializer();
        // key序列化
        redisTemplate.setKeySerializer(stringSerializer);
        // value序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        // Hash key序列化
        redisTemplate.setHashKeySerializer(stringSerializer);
        // Hash value序列化
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory factory) {
        // 配置序列化（缓存默认有效期 6小时）
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(24 * 30));
        RedisCacheConfiguration redisCacheConfiguration = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 以锁写入的方式创建RedisCacheWriter对象
        //RedisCacheWriter writer = RedisCacheWriter.lockingRedisCacheWriter(factory);
        // 创建默认缓存配置对象
        /* 默认配置，设置缓存有效期 1小时*/
        //RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1));
        /* 自定义配置test:demo 的超时时间为 5分钟*/
        RedisCacheManager cacheManager = RedisCacheManager.builder(RedisCacheWriter.lockingRedisCacheWriter(factory)).cacheDefaults(redisCacheConfiguration)
//                .withInitialCacheConfigurations(singletonMap("test:demo", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)).disableCachingNullValues()))
                .transactionAware().build();
        return cacheManager;
    }


    @Bean
    public LettuceClientConfigurationBuilderCustomizer clientConfigurationBuilderCustomizer() {
        return clientConfigurationBuilder -> clientConfigurationBuilder.readFrom(ReadFrom.REPLICA_PREFERRED);
    }

    @Bean("keyGenerator") //在没有指定缓存Key的情况下，key生成策略
    @Override
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                try {
                    final String packageCode = MD5.create().digestHex(target.getClass().getName().getBytes(StandardCharsets.UTF_8));
                    sb.append(packageCode);
                } catch (Exception e) {
                    sb.append(target.getClass().getName().hashCode());
                }
                sb.append(".");
                sb.append(method.getName());
                if (ObjectUtils.isNotEmpty(params)) {
                    sb.append("-");
                    sb.append(StringUtils.join(params, ","));
                }
                log.debug("target: {},method:{}, key: {}", target.getClass().getSimpleName(), method.getName(), sb.toString());
                return sb.toString();
            }
        };
    }

    /**
     * Redis分布式锁
     */
    /*@Bean
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory factory){
        // 1、锁的密钥前缀：REDIS-LOCK
        // 2、锁的过期时间：20秒
        return new RedisLockRegistry(factory, "REDIS-LOCK",20000L);
    }*/

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, Map<String, MessageListenerAdapter> adapterMap) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        adapterMap.keySet().forEach(topic-> container.addMessageListener(adapterMap.get(topic),new PatternTopic(topic)));
        return container;
    }

}
