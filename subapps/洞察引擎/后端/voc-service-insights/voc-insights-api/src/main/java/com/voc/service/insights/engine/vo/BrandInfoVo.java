package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author leiww
 * @version 1.0.0
 * @ClassName
 * @Description 品牌返回实体
 * @createTime 2024/2/23 14:46
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandInfoVo implements Serializable {
    /**
     * 品牌名称
     */
    private String name;
    /**
     * 品牌编码
     */
    private String code;
    /**
     * 品牌id
     */
    private String id;
    private List<BrandInfoVo> competitiveProduct;
    /**
     * 车系
     */
    private List<CarInfoVo> cars;

}
