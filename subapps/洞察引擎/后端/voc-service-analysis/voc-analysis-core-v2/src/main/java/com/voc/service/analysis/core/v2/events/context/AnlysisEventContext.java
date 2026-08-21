package com.voc.service.analysis.core.v2.events.context;

/**
 * @Title: AnlysisEventContext
 * @Package: com.voc.service.analysis.core.v2.events.context
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 13:36
 * @Version:1.0
 */

import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.common.util.StopWatch;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AnlysisEventContextDataModel
 * @createTime 2024年03月07日 13:07
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnlysisEventContext implements Serializable {

    @Builder.Default
    StopWatch stopWatch = new StopWatch();
    @Nonnull
    String workId;
    @Nonnull
    String clientId;
    //规则计算后的数据
    @Builder.Default
    AysProcessDataModel finshData = AysProcessDataModel.builder().build();
//    @Builder.Default
//    Map<String, Set<String>> resourcesGroupData = new HashMap<>();

}


