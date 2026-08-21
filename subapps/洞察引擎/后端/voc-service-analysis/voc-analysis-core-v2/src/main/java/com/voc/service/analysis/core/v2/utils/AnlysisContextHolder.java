package com.voc.service.analysis.core.v2.utils;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import com.voc.service.insights.engine.api.model.ClientModel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName PermissionContextUtil
 * @createTime 2024年01月31日 9:14
 * @Copyright futong
 */
//@Component
public class AnlysisContextHolder extends ServiceContextHolder {

    public static Map<String, Map<String, Set<String>>> resourcesGroupData = new HashMap<>();

    public static Map<String, Set<String>> getResourcesGroupData(String clientId){
        return resourcesGroupData.get(clientId);
    }

    public static void setResourcesGroupData(String clientId,Map<String, Set<String>> map){
        resourcesGroupData.put(clientId, map);
    }
}
