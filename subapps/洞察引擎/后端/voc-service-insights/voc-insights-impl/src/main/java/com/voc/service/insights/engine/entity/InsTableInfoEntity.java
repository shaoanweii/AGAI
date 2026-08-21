package com.voc.service.insights.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/12 17:40
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsTableInfoEntity implements Serializable {
    /**
     * 表名
     */
    private String tableName;
    /**
     * 表注释
     */
    private String tableComment;
    /**
     * 列名
     */
    private String columnName;
    /**
     * 列注释
     */
    private String columnComment;
}
