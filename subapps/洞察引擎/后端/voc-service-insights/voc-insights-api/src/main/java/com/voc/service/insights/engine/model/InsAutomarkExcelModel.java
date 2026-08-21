package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import com.voc.service.insights.engine.vo.AutomarkVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2026/2/11 15:27
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsAutomarkExcelModel implements Serializable {
    /**
     * 车企名称
     */
    private String name;
    /**
     * 是否核心
     */
    private Integer isCore;
    /**
     * 本竞品类型 1本品，2竞品，3非关注范围
     */
    private Integer competitiveType;
    /**
     * 本竞品关系
     */
    private String competitiveProduct;

}
