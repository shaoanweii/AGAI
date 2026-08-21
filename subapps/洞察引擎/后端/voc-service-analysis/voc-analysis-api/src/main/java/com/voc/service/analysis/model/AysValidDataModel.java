package com.voc.service.analysis.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.enums.RuLerelations;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName msg_event_data
 * @createTime 2024年01月15日 12:00
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AysValidDataModel extends Page implements Serializable {
    /**
     * 接收处理标识
     */
    String workId;

    String clientId;

    List<String> channel;
    String contentType;
    @Builder.Default
    String matchingRule = RuLerelations.And.getCode();
    String startTime;

    String endTime;
    @Builder.Default
    List<AysValidAttributeModel> attrs = new ArrayList<>();

    //本次执行校验规则的id集合
    @Builder.Default
    Set<String> validRuleIds = new HashSet<>();

    //启用的规则的id集合
    @Builder.Default
    Set<String> enabledRuleIds = new HashSet<>();

}
