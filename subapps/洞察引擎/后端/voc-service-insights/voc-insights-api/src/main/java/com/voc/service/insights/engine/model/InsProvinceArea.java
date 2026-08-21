package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 区域城市信息表(InsProvinceArea)请求实体对象
 *
 * @author leiww
 * @since 2024-01-25 13:56:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Tag(name = "区域城市信息表(无分页信息)", description = "区域城市信息表(无分页信息)")
public class InsProvinceArea implements Serializable {

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;
    /**
     * 城市编码
     */
    @Schema(description = "城市编码")
    private String provinceCode;
    /**
     * 城市名称
     */
    @Schema(description = "城市名称")
    private String provinceName;
}

