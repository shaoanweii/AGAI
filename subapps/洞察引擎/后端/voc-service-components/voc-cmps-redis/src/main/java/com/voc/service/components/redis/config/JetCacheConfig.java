package com.voc.service.components.redis.config;

import com.alicp.jetcache.CacheValueHolder;
import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.support.Fastjson2KeyConvertor;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.voc.service.components.redis.serializers.BigDecimalSerializer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName RedissonConfig
 * @createTime 2024年03月14日 13:10
 * @Copyright futong
 */


//@ConditionalOnProperty(prefix = "jetcache", name = "enabled", havingValue = "true", matchIfMissing = false)
@Configuration
@EnableMethodCache(basePackages = "com.voc")
@EnableCreateCacheAnnotation
public class JetCacheConfig {
    private static final Logger log = LoggerFactory.getLogger(JetCacheConfig.class);

    public JetCacheConfig() {

        log.info("--->> {}", this.getClass().getSimpleName());
    }
   /* @Autowired
    ConfigProvider configProvider;
    @Autowired
    private CacheManager cacheManager;*/

    @PostConstruct
    public void init(){
    }

    @Bean("myFastjson2KeyConvertor")
    @Primary
    public Function<Object, Object> MyFastjson2KeyConvertor(){
        final Fastjson2KeyConvertor INSTANCE = new Fastjson2KeyConvertor();
        return (originalKey) ->{
            if("_$JETCACHE_NULL_KEY$_".equalsIgnoreCase(String.valueOf(originalKey))){
                return INSTANCE.apply("defalut");
            }

            return INSTANCE.apply(originalKey);
        };
    }


    @Bean(name = "cacheJackson2")
    JxJsonSerialPolicy jxJsonSerialPolicy() {
        ObjectMapper om = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME.localizedBy(Locale.CHINA)));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        om.registerModule(javaTimeModule);

        SimpleModule xssModule = new SimpleModule();
        xssModule.addSerializer(BigDecimal.class, new BigDecimalSerializer());
        om.registerModule(xssModule);
        JxJsonSerialPolicy serialPolicy = new JxJsonSerialPolicy();
        Jackson2JsonRedisSerializer<CacheValueHolder> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(om, CacheValueHolder.class);

        serialPolicy.setJackson2JsonRedisSerializer(jackson2JsonRedisSerializer);
        return serialPolicy;
    }


     class JxJsonSerialPolicy implements SerialPolicy {


        private Jackson2JsonRedisSerializer<CacheValueHolder> jackson2JsonRedisSerializer;

        public void setJackson2JsonRedisSerializer(Jackson2JsonRedisSerializer<CacheValueHolder> jackson2JsonRedisSerializer) {
            this.jackson2JsonRedisSerializer = jackson2JsonRedisSerializer;
        }

        @Override
        public Function<Object, byte[]> encoder() {
            return (value) -> jackson2JsonRedisSerializer.serialize((CacheValueHolder) value);
        }

        @Override
        public Function<byte[], Object> decoder() {
            return bytes -> jackson2JsonRedisSerializer.deserialize(bytes);
        }
    }
}
