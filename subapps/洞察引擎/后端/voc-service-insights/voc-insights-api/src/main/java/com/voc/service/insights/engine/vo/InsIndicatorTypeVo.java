package com.voc.service.insights.engine.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 指标类型VO
 * 用于返回指标类型信息
 */
@Data
@Builder
public class InsIndicatorTypeVo {

    /**
     * 类型名称
     */
    private String name;

    /**
     * 类型编码
     */
    private String code;

    /**
     * 条件列表
     */
    private List<InsIndicatorConditionVo> conditions;
}
