package com.voc.service.analysis.core.v2.service;

import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 批量元数据状态更新服务测试
 *
 * @author ckcui
 * @version 1.0.0
 * @createTime 2024年11月21日
 */
class BatchMetaDataStatusServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private IAysMetaDataAnalysisService iAysMetaDataAnalysisService;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private BatchMetaDataStatusService batchMetaDataStatusService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testAddToCache_shouldAddToRedis() {
        // Arrange
        String clientId = "testClient";
        Map<String, Integer> dataStatusMap = new HashMap<>();
        dataStatusMap.put("data1", 1);
        dataStatusMap.put("data2", 2);

        when(hashOperations.size(anyString())).thenReturn(2L);

        // Act
        batchMetaDataStatusService.addToCache(clientId, dataStatusMap);

        // Assert
        verify(hashOperations, times(2)).put(anyString(), anyString(), anyString());
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void testAddToCache_shouldTriggerBatchUpdateWhenThresholdReached() {
        // Arrange
        String clientId = "testClient";
        Map<String, Integer> dataStatusMap = new HashMap<>();
        for (int i = 0; i < 200; i++) {
            dataStatusMap.put("data" + i, i);
        }

        when(hashOperations.size(anyString())).thenReturn(200L);
        Map<Object, Object> redisData = new HashMap<>();
        for (int i = 0; i < 200; i++) {
            redisData.put("data" + i, String.valueOf(i));
        }
        when(hashOperations.entries(anyString())).thenReturn(redisData);
        when(iAysMetaDataAnalysisService.modifyToDataStatus(anyString(), anyMap())).thenReturn(200);

        // Act
        batchMetaDataStatusService.addToCache(clientId, dataStatusMap);

        // Assert
        verify(iAysMetaDataAnalysisService).modifyToDataStatus(eq(clientId), anyMap());
        verify(stringRedisTemplate, times(2)).delete(anyString());
    }

    @Test
    void testExecuteBatchUpdate_shouldUpdateAndClearRedis() {
        // Arrange
        String clientId = "testClient";
        Map<Object, Object> redisData = new HashMap<>();
        redisData.put("data1", "1");
        redisData.put("data2", "2");

        when(hashOperations.entries(anyString())).thenReturn(redisData);
        when(iAysMetaDataAnalysisService.modifyToDataStatus(anyString(), anyMap())).thenReturn(2);

        // Act
        batchMetaDataStatusService.executeBatchUpdate(clientId);

        // Assert
        verify(iAysMetaDataAnalysisService).modifyToDataStatus(eq(clientId), anyMap());
        verify(stringRedisTemplate, times(2)).delete(anyString());
    }

    @Test
    void testGetCacheSize_shouldReturnCorrectSize() {
        // Arrange
        String clientId = "testClient";
        when(hashOperations.size(anyString())).thenReturn(10L);

        // Act
        long size = batchMetaDataStatusService.getCacheSize(clientId);

        // Assert
        assert size == 10L;
    }

    @Test
    void testAddToCache_emptyMap_shouldNotProcess() {
        // Arrange
        String clientId = "testClient";
        Map<String, Integer> emptyMap = new HashMap<>();

        // Act
        batchMetaDataStatusService.addToCache(clientId, emptyMap);

        // Assert
        verify(hashOperations, never()).put(anyString(), anyString(), anyString());
    }
}
