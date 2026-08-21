package com.voc.service.analysis.core.v2.events.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @Title: ConditionItemModel
 * @Package: com.voc.service.analysis.core.events
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/8 17:28
 * @Version:1.0
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConditionItemModel {
    boolean result;
    String attrName;
    String attrValue;
    String value;
    Set<String> values;
    String sign;

}
