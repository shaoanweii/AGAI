package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/30 上午10:15
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelAndModelVo implements Serializable {
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
     * 处理模型
     */
    @Schema(description = "处理模型")
    private String processingModel;
    /**
     * 分类编码
     */
    @Schema(description = "分类编码")
    private String classifyCode;
}
