package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 11:07
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictInfoVo  implements Serializable {
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
}
