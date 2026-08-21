package com.voc.service.insights.engine.vo;

import lombok.Data;

/**
 * 批量规则计数VO
 * 用于返回分类规则数量的统计结果
 */
@Data
public class InsBatchRuleCountVo {

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 规则数量
     */
    private Integer count;
}
