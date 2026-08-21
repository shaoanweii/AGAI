package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/17 上午11:44
 * @描述:
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsDataSourceVo implements Serializable {
    private String id;
    /**
     * 数据源名称
     */
    private String dataSourceName;
    /**
     * 数据源类型
     */
    private String dataSourceType;
    /**
     * 数据源接入方式
     */
    private String dataSourceAccessWay;

    private List<String> labelType;
    /**
     * 模型类型
     */
    private String modelType;

    private List<InsDataSourceDescVo> dataSourceDesc;
}
