package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 区域城市信息表(InsProvinceArea)请求实体对象
 *
 * @author leiww
 * @since 2024-01-25 13:56:33
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Tag(name = "区域城市信息表", description = "区域城市信息表")
public class InsProvinceAreaModel extends Page  implements Serializable {

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;
    /**
     * 区域编码
     */
    @Schema(description = "区域编码")
    private String areaCode;
    /**
     * 区域名称
     */
    @Schema(description = "区域名称")
    private String areaName;
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

