package com.voc.service.data.integration.api.model;

import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Title: FieldsValidModel
 * @Package: com.voc.service.data.integration.api.model
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/29 8:52
 * @Version:1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class FieldsValidModel implements Serializable {
    @Builder.Default
    Set<String> defaultFields = new HashSet<>();
    @Builder.Default
    Map<String, Set<String>> extAttrs = new HashMap<>();
}
