package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.voc.service.analysis.api.IAysCacheService;
import com.voc.service.analysis.api.IStaticDataServcie;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.clients.InsDataResourceClient;
import com.voc.service.insights.engine.enums.RuleStatusType;
import com.voc.service.insights.engine.model.data.InsDataResourceDescModel;
import com.voc.service.insights.engine.vo.data.ResourceDescDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName StaticDataServcieImpl
 * @createTime 2024年03月12日 18:18
 * @Copyright cuick
 */

@Service
public class StaticDataServcieImpl implements IStaticDataServcie, IAysCacheService {
    /*static final String STAITC_DATA_KEY = "staitc_data";
    static final String STAITC_DATA_KEY_RESOURCE_GROUP = "staitc_data_key_resource_group";
    @Autowired
    AnalysisFeignConfig config;
    @CreateCache(name = STAITC_DATA_KEY, expire = 24 * 30, cacheType = CacheType.LOCAL
            , timeUnit = TimeUnit.HOURS )
    private Cache<String, Map<String, List<Object>>> localCache;*/
    final static Set<String> statusFilters = CollUtil.newHashSet(
//                        RuleStatusType.Disabled.getCode(),
            RuleStatusType.Enabled.getCode(),
            RuleStatusType.NotEnabled.getCode());
    private static final Logger logger = LoggerFactory.getLogger(StaticDataServcieImpl.class);
    @Autowired
    InsDataResourceClient dataResourceClient;
    @Autowired
    AnalysisConfig config;

    @CreateCache(area = "VDP", name = ":",  expire = 30, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE)
    Cache<String,  List<ResourceDescDto>> resourceGroupCache ;
    private static final String RES_GROUP_KEY = "{}:rule_res_group:{}";

    private String getResourceGroupCacheKey(Object... params){
        return StrUtil.format(RES_GROUP_KEY, ServiceContextHolder.getSystemId(),params);
    }

    /*@Autowired
    private CacheUtils cacheUtils;*/
    @Override
    //TODO 需根据情况重置缓存时间长度
//    @Cached(area = "VDP", name = ":data:res_group:", key = "'enabledAndNotEnabled'", expire = 60 * 6, cacheType = CacheType.LOCAL
//             , timeUnit = TimeUnit.MINUTES, cacheNullValue = false)
//    @CachePenetrationProtect
    public Map<String, List<ResourceDescDto>> getResourceGroup(final String clientId) {
        logger.info("调用 getResourceGroup");
        final String key = this.getResourceGroupCacheKey("clientId_".concat(clientId));

        List<ResourceDescDto> list = resourceGroupCache.computeIfAbsent(key, k -> {
            //设置固定token，此token需添加到auth服务白名单token集合
            ServiceContextHolder.setToken(config.getDefaultToken());
            //使用接口数据
            Result<List<ResourceDescDto>> result = dataResourceClient.findByConditon(InsDataResourceDescModel.builder()
                    .statusFilters(statusFilters)
                    .customer(clientId)
                    .build());
            logger.info("获取资源组getResourceGroup result:{}", result.getResult().size());
            if ("200".equals(result.getCode())) {
                //           Assert.isTrue(CollUtil.isNotEmpty(result.getResult()), "getResult cannot be empty");
                if (result.getResult() == null) {
                    logger.error("获取洞察引擎规则资源组请求失败，返回数据为空 statusFilters：{} , {}", statusFilters, result);
                    return null;
                }
                return result.getResult();
            }
            return null;
        });
        logger.debug("调用 getResourceGroup-> 使用缓存");
        logger.info("getResourceGroup result:{}", list.size());
        return Collections.unmodifiableMap(list.stream().collect(Collectors.groupingBy(ResourceDescDto::getResourceId)));
    }

//    @Cached(area = "VDP", name = ":data:res_group:", key = "#clientId+'getAllEnabledResourceGroup'", expire = 60 * 6, cacheType = CacheType.LOCAL
//             , timeUnit = TimeUnit.MINUTES, cacheNullValue = false)
//   @CachePenetrationProtect
    @Override
    public Map<String, Set<String>> getAllEnabledResourceGroup(final String clientId) {
        logger.info("调用 getValidResourceGroup");
        final Map<String, List<ResourceDescDto>> source = getResourceGroup(clientId);
        if (CollUtil.isEmpty(source)) {
            return null;
        }

        AtomicReference<Map<String, Set<String>>> map = new AtomicReference<>(new HashMap<>());
        source.values().forEach(e -> {
            e.stream()
                    .filter(Objects::nonNull)
                    .filter(e1 -> StrUtil.isNotBlank(e1.getResourceId()))
                    .filter(e1 -> StrUtil.isNotBlank(e1.getName()))
                    .filter(e1 -> StrUtil.isNotBlank(e1.getStatus()))
                    .filter(e1 -> RuleStatusType.Enabled.getCode().equals(e1.getStatus()))
                    .forEach(e1 -> {
                        if (map.get().containsKey(e1.getResourceId())) {
                            map.get().get(e1.getResourceId()).add(e1.getName());
                        } else {
                            map.get().put(e1.getResourceId(), new HashSet<>(CollUtil.newArrayList(e1.getName())));
                        }
                    });
        });

        return Collections.unmodifiableMap(map.get());
    }

//    @Cached(area = "VDP", name = ":data:res_group:", key = "#clientId+'getValidResourceGroup'", expire = 60 * 6, cacheType = CacheType.LOCAL
//             , timeUnit = TimeUnit.MINUTES, cacheNullValue = false)
//    @CachePenetrationProtect
    @Override
    public Map<String, Set<String>> getValidResourceGroup(String clientId) {
        logger.info("调用 getValidResourceGroup");
        final Map<String, List<ResourceDescDto>> source = getResourceGroup(clientId);
        if (CollUtil.isEmpty(source)) {
            return null;
        }

        AtomicReference<Map<String, Set<String>>> map = new AtomicReference<>(new HashMap<>());

        source.values().forEach(e -> {
            e.stream()
                    .filter(Objects::nonNull)
                    .filter(e1 -> StrUtil.isNotBlank(e1.getResourceId()))
                    .filter(e1 -> StrUtil.isNotBlank(e1.getName()))
//                    .filter(e1 -> RuleStatusType.Enabled.getCode().equals(e1.getStatus()))
                    .forEach(e1 -> {
                        if (map.get().containsKey(e1.getResourceId())) {
                            map.get().get(e1.getResourceId()).add(e1.getName());
                        } else {
                            map.get().put(e1.getResourceId(), new HashSet<>(CollUtil.newArrayList(e1.getName())));
                        }
                    });
        });
        return Collections.unmodifiableMap(map.get());
    }


    @Override
    public void removeCache() {
        /*Set<String> allResourceGroupKeys = cacheUtils.getResourceGroupKeys(ServiceContextHolder.getSystemId(),"rule_res_group");
        logger.info("全部key:{}",allResourceGroupKeys.size());
        allResourceGroupKeys.stream().forEach(key -> {
            if(key.startsWith("VDP_:")){
                String replace = key.replace("VDP_:", "");
                boolean remove = resourceGroupCache.remove(replace);
                logger.info("删除缓存 {} {}", key, remove);
            }else{
                boolean remove = resourceGroupCache.remove(key);
                logger.info("删除缓存 {} {}", key, remove);
            }
        });*/
    }

    /*public Map<String, Set<String>> getValidResourceGroup() {
        if (CollUtil.isEmpty(getResourceGroup())) {
            return null;
        }
        return getResourceGroup().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getName()));
    }*/


    /*
        @Autowired
        private CacheManager cacheManager;
        @Autowired
        private ConfigMap cacheConfigMap;*/
    @Override

    public boolean cleanCache() {
//        CacheUtil.cleanCache(STAITC_DATA_KEY_TAG, STAITC_DATA_CACHE);
//        localCache.close();
       /* Cache<Object, Object> cache = cacheManager.getCache(STAITC_DATA_KEY_TAG);
        CachedAnnoConfig cac = cacheConfigMap.getByCacheName("default", STAITC_DATA_KEY_TAG);
        if (cac == null ) {
            return false;
        }*/
//        localCache.put(STAITC_DATA_KEY_TAG, null);

//        System.out.println(localCache);
//        System.out.println(cache.config().getKeyConvertor());
        return true;
    }

}
