package com.voc.service.analysis.api;

import com.voc.service.insights.engine.vo.data.ResourceDescDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IStaticDataServcie {
    static final String RESOURCE_GROUP_CACHE_KEY = "data:res_group:";
    Map<String, List<ResourceDescDto>> getResourceGroup(String clientId);

    Map<String, Set<String>> getAllEnabledResourceGroup(String clientId);

    Map<String, Set<String>> getValidResourceGroup(String clientId);

    void removeCache();
}
