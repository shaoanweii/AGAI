package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author leiww
 * @version 1.0.0
 * @ClassName
 * @Description
 * @createTime 2024/2/26 14:36
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarTypeVo implements Serializable {
    /**
     * 类型名称
     */
    @Schema(description = "类型名称")
    private String typeName;
    /**
     * 类型编码
     */
    @Schema(description = "类型编码")
    private String typeCode;
    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String classifyName;
    /**
     * 分类编码
     */
    @Schema(description = "分类编码")
    private String classifyCode;
    /**
     * 关联
     */
    @Schema(description = "关联")
    private String correlation;
    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;
}
