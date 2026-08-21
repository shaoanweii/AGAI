package com.voc.service.components.redis.service;

import com.fasterxml.jackson.databind.JavaType;
import com.voc.service.common.util.JsonMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 封装一些基本的redis操作方法
 *
 * @author wanght 2021-06-02 20:50:01
 */
@Service
public class RedisService {
    private static final Logger log = LoggerFactory.getLogger(RedisService.class);
    /**
     * redis 操作类
     */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Redis中存值
     *
     * @param key    存储键
     * @param object 存储对象 如果为空则不存储
     * @param <T>    对象类型
     */
    public <T> void set(String key, T object) {
        if (object != null) {
            stringRedisTemplate.boundValueOps(key).set(JsonMapper.getInstances().toJson(object));
        }
        log.info("存储对象到Redis中，键：{},值:{}", key, object);
    }

    /**
     * redis 中存储对象
     *
     * @param key      存储键
     * @param object   存储对象 如果为空则不存储
     * @param times    存储时长
     * @param timeUnit 存储时长单位
     * @param <T>      存储对象类型
     */
    public <T> void set(String key, T object, long times, TimeUnit timeUnit) {
        if (object != null) {
            stringRedisTemplate.boundValueOps(key).set(JsonMapper.getInstances().toJson(object), times, timeUnit);
        }
        log.info("存储对象到Redis中，键：{},值:{},存储时长{}{}", key, object, times, timeUnit);
    }

    /**
     * Redis 中存储对象，如果对象存在才存储
     *
     * @param key    存储的键
     * @param object 存储的对象
     * @param <T>    存储对象的类型
     * @return 是否存储成功
     */
    public <T> Boolean setIfAbsent(String key, T object) {
        Boolean result = false;
        if (object != null) {
            result = stringRedisTemplate.boundValueOps(key).setIfAbsent(JsonMapper.getInstances().toJson(object));
            log.info("如果键不存在，存储对象到Redis中，键：{},值:{},结果：{}", key, object, result);
        }
        return result;
    }

    /**
     * Redis 中存储对象，如果对象存在才存储
     *
     * @param key      存储的键
     * @param object   存储的对象
     * @param times    存储时长
     * @param timeUnit 存储时长单位
     * @param <T>      存储对象的类型
     * @return 是否存储成功
     */
    public <T> Boolean setIfAbsent(String key, T object, long times, TimeUnit timeUnit) {
        Boolean result = false;
        if (object != null) {
            result = stringRedisTemplate.boundValueOps(key).setIfAbsent(JsonMapper.getInstances().toJson(object), times, timeUnit);
            log.info("如果键不存在，存储对象到Redis中，键：{},值:{},存储时长{}{}.结果：{}", key, object, times, timeUnit, result);
        }
        return result;
    }

    /**
     * Redis中获取对象值
     *
     * @param key    存储键
     * @param tClass 存储对象 如果为空则不存储
     * @param <T>    对象类型
     * @return 获取到的对象
     */
    public <T> T get(String key, Class<T> tClass) {
        if (StringUtils.isNotBlank(key)) {
            String json = stringRedisTemplate.boundValueOps(key).get();
            log.info("获取Redis缓存中的对象，键：{},值:{}", key, json);
            if (StringUtils.isNotBlank(json)) {
                T result = JsonMapper.getInstances().fromJson(json, tClass);
                return result;
            }
        }
        return null;
    }

    /**
     * Redis 中获取对象列表值
     *
     * @param key    存储键
     * @param tClass 存储对象 如果为空则不存储
     * @param <T>    对象类型
     * @return 对象列表
     */
    public <T> List<T> getList(String key, Class<T> tClass) {
        JavaType javaType = JsonMapper.getInstances().buildCollectionType(List.class, tClass);
        if (StringUtils.isNotBlank(key)) {
            String json = stringRedisTemplate.boundValueOps(key).get();
            log.info("获取Redis缓存中的对象，键：{},值:{}", key, json);
            if (StringUtils.isNotBlank(json)) {
                List<T> result = JsonMapper.getInstances().fromJson(json, javaType);
                return result;
            }
        }
        return null;
    }

    /**
     * 获取过期时间
     *
     * @param key redis 键
     * @return redis过期时间 秒
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 缓存一个对象
     *
     * @param key
     * @param object
     * @param <T>
     */
    public <T> void putObject(String key, T object) {
        if (object != null) {
            this.stringRedisTemplate.boundHashOps(key).put(key, JsonMapper.getInstances().toJson(object));
        }

        log.info("存储对象到Redis中，键：{},值:{}", key, object);
    }

    public <T> Map<String, T> getMap(String key, Class<T> tClass) {
        BoundHashOperations<String, String, String> boundHash = this.stringRedisTemplate.boundHashOps(key);
        Map<String, String> map = boundHash.entries();
        log.info("获取Redis中的对象，键：{},值:{}", key, map);
        if (map != null && !map.isEmpty()) {
            Map<String, T> result = new HashMap<>();
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (StringUtils.isNotBlank(entry.getValue())) {
                    result.put(entry.getKey(), JsonMapper.getInstances().fromJson(entry.getValue(), tClass));
                }
            }

            return result;
        } else {
            return null;
        }
    }

    public <T> T getObject(String key, String hk, Class<T> tClass) {
        BoundHashOperations<String, String, String> boundHash = this.stringRedisTemplate.boundHashOps(key);
        String value = (String) boundHash.get(hk);
        return StringUtils.isBlank(value) ? null : JsonMapper.getInstances().fromJson(value, tClass);
    }

    /**
     * 删除缓存中的对象
     *
     * @param key 要删除的对象的 Key
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }
}