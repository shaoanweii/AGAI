package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 10:59
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionDetailsVo  implements Serializable {
    String key;
    String value;
    String code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<ConditionDetailsVo> children;
}
