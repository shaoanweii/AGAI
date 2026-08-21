package com.voc.service.insights.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/14 上午9:42
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitiveProductEntity {
    /**
     * 竞品品牌编码
     */
    private String competitiveBrandCode;
    /**
     * 竞品品牌名称
     */
    private String competitiveBrandName;
}
