package com.voc.service.insights.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 11:10
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsDictInfoEntity implements Serializable {
    /**
     * 类型名称
     */
    private String typeName;
    /**
     * 类型编码
     */
    private String typeCode;
    /**
     * 分类名称
     */
    private String classifyName;
    /**
     * 分类编码
     */
    private String classifyCode;
    /**
     * 处理模型
     */
    private String processingModel;
}
