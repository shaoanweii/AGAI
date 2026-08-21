package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/11/29 下午4:14
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsDataSourceTreeVo {

    private String id;
    /**
     * 数据源名称
     */
    private String dataSourceName;

    private List<InsDataSourceTreeVo> child;
}
