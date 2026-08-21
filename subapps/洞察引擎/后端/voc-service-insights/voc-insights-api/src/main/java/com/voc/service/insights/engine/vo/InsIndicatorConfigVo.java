package com.voc.service.insights.engine.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 指标配置VO
 * 用于返回指标配置信息给前端
 */
@Data
@Builder
public class InsIndicatorConfigVo {

    /**
     * 指标名称
     */
    private String name;

    /**
     * 指标编码
     */
    private String code;

    /**
     * 指标类型列表
     */
    private List<InsIndicatorTypeVo> types;
}
