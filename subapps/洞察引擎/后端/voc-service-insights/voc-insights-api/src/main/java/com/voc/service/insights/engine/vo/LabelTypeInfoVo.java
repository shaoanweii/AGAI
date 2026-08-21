package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/15 下午4:56
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelTypeInfoVo  implements Serializable {
    /**
     * 类型名称
     */
    private String typeName;
    /**
     * 分类名称
     */
    private String classifyName;

    /**
     * 描述
     */
    private String description;
    /**
     * 处理模型
     */
    private String processingModel;
}
