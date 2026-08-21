package com.voc.service.insights.engine.web;

import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheResultCode;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/22 09:01
 * @描述:
 **/
@Tag(name = "缓存管理")
@RestController
@RequestMapping("/caches")
public class InsCacheController {

    private static final Logger log = LoggerFactory.getLogger(InsCacheController.class);
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/clearCache")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "清除缓存")
    public Result<?> clearCache() {
        try {
            Boolean result = this.cleanCache(null);
            if (result) {
                return Result.OK("清理缓存成功");
            } else {
                return Result.error("清理缓存失败");
            }
        } catch (Exception e) {
            log.error("清理全部缓存异常:", e);
            return Result.error("清理缓存异常");
        }
    }

    @GetMapping("/clearDataCache")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "清除data缓存")
    public Result<?> clearDataCache() {
        try {
            log.info("清理data缓存");
            Boolean result = this.cleanCache("data");
            if (result) {
                return Result.OK("清理缓存成功");
            } else {
                return Result.error("清理缓存失败");
            }
        } catch (Exception e) {
            log.error("清理data缓存异常:", e);
            return Result.error("清理缓存异常");
        }
    }

    @GetMapping("/clearUsersCache")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "清除users缓存")
    public Result<?> clearUsersCache() {
        try {
            log.info("清理users缓存");
            Boolean result = this.cleanCache("users");
            if (result) {
                return Result.OK("清理缓存成功");
            } else {
                return Result.error("清理缓存失败");
            }
        } catch (Exception e) {
            log.error("清理data缓存异常:", e);
            return Result.error("清理缓存异常");
        }
    }


    private Boolean cleanCache(String cacheNames) {
        final String appId = ServiceContextHolder.getSystemId();
        Set<String> cachesName = new HashSet<>();
        if (StrUtil.isBlank(cacheNames)) {
            log.info("清除全部缓存");
            cachesName.add("data");
            cachesName.add("users");
        } else {
            log.info("清除指定缓存:{}", cacheNames);
            cachesName.add(cacheNames);
        }
        List<String> errorMessage = new ArrayList<>();
        cachesName.stream().forEach(e -> {
            String key = appId + ":" + e + ":*";
            Set<String> keys = redisTemplate.keys(key);
            Cache<Object, Object> caches = cacheManager.getCache(e + ":");
            Set<String> collect = keys.stream().map(k -> {
                String[] split = k.split(e + ":");
                return split[1];
            }).collect(Collectors.toSet());
            CacheResult cacheResult = caches.REMOVE_ALL(collect);
            if (cacheResult.isSuccess() || cacheResult.getResultCode() == CacheResultCode.PART_SUCCESS) {
                log.info("清除{}缓存成功", e);
            } else {
                log.error("清除{}缓存失败", e);
                errorMessage.add("清除" + e + "缓存失败");
            }
        });
        if (ObjectUtils.isEmpty(errorMessage)) {
            return true;
        }

        return false;
    }


}
