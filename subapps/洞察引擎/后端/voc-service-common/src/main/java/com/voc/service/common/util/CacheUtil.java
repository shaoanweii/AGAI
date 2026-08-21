package com.voc.service.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName CacheService
 * @createTime 2024年02月26日 11:26
 * @Copyright futong
 */
@Component
public class CacheUtil {
    @Autowired
    static RedisTemplate redisTemplate;
    private static Object a;
    private static final Logger logger = LoggerFactory.getLogger(CacheUtil.class);
    public static RedisTemplate getRedisTemplate() {
        if (ObjectUtil.isNull(redisTemplate)) {
            RedisTemplate redis = ServiceContextHolder.getApplicationContext().getBean("redisTemplate", RedisTemplate.class);
            redisTemplate = redis;
        }
        return redisTemplate;
    }


    public static String getKey(@Nonnull String cacheKey) {
        logger.debug("appId {}", ServiceContextHolder.getSystemId());
        Assert.isTrue(StrUtil.isNotBlank(ServiceContextHolder.getSystemId()), "appId cannot be empty");
        //{}:users:{}:data:dict:{}

        return StrUtil.format("{}:data:".concat(cacheKey).concat(":{}")
                , ServiceContextHolder.getSystemId());
    }
    /**
     * 通用缓存
     *
     * @param cacheKey
     * @param code
     * @return
     */
    public static String getKey(@Nonnull String cacheKey, @Nonnull String code) {
        logger.debug("code {}", code);
        logger.debug("appId {}", ServiceContextHolder.getSystemId());
        Assert.isTrue(StrUtil.isNotBlank(ServiceContextHolder.getSystemId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(code), "dict code cannot be empty");
        //{}:users:{}:data:dict:{}

        return StrUtil.format("{}:data:".concat(cacheKey).concat(":{}")
                , ServiceContextHolder.getSystemId()
                , code);
    }

    /**
     * 保存数据： 保存再用户对应token下的目录
     *
     * @param userId
     * @param cacheKey
     * @param code
     * @return
     */
    public static String getKey(@Nonnull String userId, @Nonnull String cacheKey, String code) {
        logger.debug("code {}", code);
        logger.debug("userId {}", userId);
        logger.debug("cacheKey {}", cacheKey);
        logger.debug("tokenKey {}", ServiceContextHolder.getUser().getTokenKey());
        Assert.isTrue(StrUtil.isNotBlank(cacheKey), "cacheKey cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(userId), "userId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(ServiceContextHolder.getUser().getTokenKey()), "tokenKey cannot be empty");

        return StrUtil.format("{}:users:{}:tokens:{}:data:".concat(cacheKey).concat(StrUtil.isNotBlank(code) ? ":{}" : "")
                , ServiceContextHolder.getSystemId()
                , userId
                , ServiceContextHolder.getUser().getTokenKey()
                , StrUtil.isNotBlank(code) ? code : null
                , code);
    }

    private static String getKeyAll1(String cacheKey) {
        logger.debug("code {}", cacheKey);
        logger.debug("appId {}", ServiceContextHolder.getSystemId());
        Assert.isTrue(StrUtil.isNotBlank(ServiceContextHolder.getSystemId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(cacheKey), "cacheKey cannot be empty");

        return StrUtil.format("{}:data:".concat(cacheKey)
                , ServiceContextHolder.getSystemId());
    }


    static final ConcurrentHashMap<String, List<Cache>> cacheMap = new ConcurrentHashMap();

    public static void cleanCache(String cacheKey, Cache<String, ?>... cahes) {
        if (!cacheMap.contains(cacheKey)) {
            cacheMap.put(cacheKey, CollUtil.newArrayList(cahes));
        }
        //删除缓存-redis

        if(getRedisTemplate().hasKey(cacheKey)){
            getRedisTemplate().delete(cacheKey);
        }
        final String key1 = getKeyAll1(cacheKey);
        final Set keys1 = getRedisTemplate().keys(key1.concat(":*"));
        if (CollUtil.isNotEmpty(keys1)) {
            getRedisTemplate().delete(keys1);
        }


        //删除缓存-本地
        Stream.of(cahes).forEach(cache -> {
            /*final Set<String> ks = cache.asMap().keySet().stream()
                    .filter(key -> StrUtil.startWith(String.valueOf(key), key1))
                    .map(key -> String.valueOf(key))
                    .collect(Collectors.toSet());
            cache.invalidateAll(ks);*/
            cache.invalidateAll();
        });

    }

    public static void cleanAll() {
        /*if (!cacheMap.isEmpty()) {
            Stream.of(cacheMap.keys()).forEach(key -> {
                cacheMap.get(key).stream().forEach( val ->{
                    cleanCache(String.valueOf(key), val);
                });
            });
        }*/

    }
}
