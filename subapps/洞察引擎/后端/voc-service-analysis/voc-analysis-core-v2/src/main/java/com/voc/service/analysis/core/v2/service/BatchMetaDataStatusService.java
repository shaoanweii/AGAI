package com.voc.service.analysis.core.v2.service;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 批量处理元数据状态更新服务
 * 将接收到的状态更新请求暂存到Redis，达到阈值或超时后批量执行
 *
 * @author ckcui
 * @version 1.0.0
 * @createTime 2024年11月21日
 */
@Service
public class BatchMetaDataStatusService {

    private static final Logger log = LoggerFactory.getLogger(BatchMetaDataStatusService.class);

    /**
     * Redis中存储待更新数据的Hash Key前缀
     */
    private static final String REDIS_BATCH_KEY_PREFIX = ":metadata:status:batch:";
    
    /**
     * Redis中存储最后更新时间的Key前缀
     */
    private static final String REDIS_LAST_UPDATE_TIME_PREFIX = ":metadata:status:lasttime:";
    
    /**
     * Redis中存储处理中状态的Key前缀（防重复执行）
     */
    private static final String REDIS_PROCESSING_KEY_PREFIX = ":metadata:status:processing:";
    
    /**
     * Redis中存储活跃clientId集合的Key
     */
    private static final String REDIS_ACTIVE_CLIENTS_KEY = ":metadata:status:active:clients";

    /**
     * 批量更新的阈值
     */
    private static final int BATCH_THRESHOLD = 200;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IAysMetaDataAnalysisService iAysMetaDataAnalysisService;

    @Value("${analysis.mate_data_status_cache_elapsed_seconds:60}")
    Integer dataStatusElapsedSeconds;

    /**
     * 添加状态更新数据到Redis缓存
     *
     * @param clientId      客户端ID
     * @param dataStatusMap 数据状态映射 (dataId -> status)
     */
    public void addToCache(String clientId, Map<String, Integer> dataStatusMap) {
        if (CollUtil.isEmpty(dataStatusMap)) {
            log.warn("dataStatusMap is empty, skip adding to cache");
            return;
        }
        if(StringUtils.isEmpty(clientId)){
            log.warn("clientId is empty");
            clientId = "default_client";
        }
        String batchKey = REDIS_BATCH_KEY_PREFIX + clientId;
        String lastTimeKey = REDIS_LAST_UPDATE_TIME_PREFIX + clientId;

        try {
            // 将数据添加到Redis Hash中
            for (Map.Entry<String, Integer> entry : dataStatusMap.entrySet()) {
                stringRedisTemplate.opsForHash().put(batchKey, entry.getKey(), String.valueOf(entry.getValue()));
            }

            // 更新最后添加时间
            stringRedisTemplate.opsForValue().set(lastTimeKey, String.valueOf(System.currentTimeMillis()), 30, TimeUnit.SECONDS);
            
            // 将clientId添加到活跃集合
            stringRedisTemplate.opsForSet().add(REDIS_ACTIVE_CLIENTS_KEY, clientId);
            stringRedisTemplate.expire(REDIS_ACTIVE_CLIENTS_KEY, 60, TimeUnit.SECONDS);

            // 检查是否达到批量更新阈值
            Long cacheSize = stringRedisTemplate.opsForHash().size(batchKey);
            log.info("Added {} items to cache for clientId: {}, current cache size: {}", 
                    dataStatusMap.size(), clientId, cacheSize);

            if (cacheSize != null && cacheSize >= BATCH_THRESHOLD) {
                log.info("Cache size {} reached threshold {}, triggering batch update", cacheSize, BATCH_THRESHOLD);
                executeBatchUpdate(clientId);
            }
        } catch (Exception e) {
            log.error("Error adding data to cache for clientId: {}", clientId, e);
            throw e;
        }
    }

    /**
     * 执行批量更新
     *
     * @param clientId 客户端ID
     */
    public void executeBatchUpdate(String clientId) {
        String batchKey = REDIS_BATCH_KEY_PREFIX + clientId;
        String lastTimeKey = REDIS_LAST_UPDATE_TIME_PREFIX + clientId;
        String processingKey = REDIS_PROCESSING_KEY_PREFIX + clientId;

        try {
            // 使用Redis原子操作设置处理中标识，防止重复执行（5秒过期）
            Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(
                    processingKey, "1", 5, TimeUnit.SECONDS);
            
            if (locked == null || !locked) {
                log.debug("Batch update is already in progress for clientId: {}, skip this execution", clientId);
                return;
            }
            
            // 从Redis中获取所有待更新的数据
            Map<Object, Object> rawMap = stringRedisTemplate.opsForHash().entries(batchKey);
            
            if (CollUtil.isEmpty(rawMap)) {
                log.debug("No data to update for clientId: {}", clientId);
                return;
            }

            // 转换数据类型
            Map<String, Integer> dataStatusMap = new HashMap<>(rawMap.size());
            for (Map.Entry<Object, Object> entry : rawMap.entrySet()) {
                try {
                    final String dataId = String.valueOf(entry.getKey());
                    final Integer status = Integer.valueOf(String.valueOf(entry.getValue()));
                    dataStatusMap.put(dataId, status);
                } catch (NumberFormatException e) {
                    log.error("Invalid status value for key: {}, value: {}", entry.getKey(), entry.getValue(), e);
                }
            }

            if (CollUtil.isEmpty(dataStatusMap)) {
                log.warn("No valid data to update after conversion for clientId: {}", clientId);
                stringRedisTemplate.delete(batchKey);
                stringRedisTemplate.delete(lastTimeKey);
                return;
            }

            log.info("Start batch update for clientId: {}, total items: {}", clientId, dataStatusMap.size());

            // 执行批量更新
            int updateCount = iAysMetaDataAnalysisService.modifyToDataStatus(clientId, dataStatusMap);
            
            log.info("Batch update completed for clientId: {}, updated {} items", clientId, updateCount);

            // 清除已处理的Redis数据
            stringRedisTemplate.delete(batchKey);
            stringRedisTemplate.delete(lastTimeKey);
            stringRedisTemplate.opsForSet().remove(REDIS_ACTIVE_CLIENTS_KEY, clientId);

        } catch (Exception e) {
            log.error("Error executing batch update for clientId: {}", clientId, e);
            throw e;
        } finally {
            // 确保处理完成后释放锁
            stringRedisTemplate.delete(processingKey);
        }
    }

    /**
     * 检查并执行超时的批量更新
     * 由定时任务调用
     */
    public void checkAndExecuteTimeoutUpdate() {
        try {
            // 获取所有活跃的clientId
            Set<String> activeClients = stringRedisTemplate.opsForSet().members(REDIS_ACTIVE_CLIENTS_KEY);
            
            if (CollUtil.isEmpty(activeClients)) {
                log.debug("No active clients found");
                return;
            }
            log.info("Active clients: {}", activeClients);
            for (String clientId : activeClients) {
                checkAndExecuteTimeoutUpdateForClient(clientId);
            }
        } catch (Exception e) {
            log.error("Error checking and executing timeout update", e);
        }
    }
    
    /**
     * 检查指定clientId是否超时并执行更新
     * 
     * @param clientId 客户端ID
     */
    public void checkAndExecuteTimeoutUpdateForClient(String clientId) {
        try {
            String lastTimeKey = REDIS_LAST_UPDATE_TIME_PREFIX + clientId;
            String lastTimeStr = stringRedisTemplate.opsForValue().get(lastTimeKey);
            
            if (lastTimeStr != null) {
                long lastTime = Long.parseLong(lastTimeStr);
                long currentTime = System.currentTimeMillis();
                long elapsedSeconds = (currentTime - lastTime) / 1000;

                // 检查是否超过20秒
                if (elapsedSeconds >= dataStatusElapsedSeconds) {
                    log.info("Batch data for clientId: {} has been idle for {} seconds, triggering update", 
                            clientId, elapsedSeconds);
                    executeBatchUpdate(clientId);
                }
            }
        } catch (Exception e) {
            log.error("Error checking timeout for clientId: {}", clientId, e);
        }
    }

    /**
     * 获取指定客户端当前缓存的数据量
     *
     * @param clientId 客户端ID
     * @return 缓存数据量
     */
    public long getCacheSize(String clientId) {
        String batchKey = REDIS_BATCH_KEY_PREFIX + clientId;
        Long size = stringRedisTemplate.opsForHash().size(batchKey);
        return size != null ? size : 0;
    }
}
