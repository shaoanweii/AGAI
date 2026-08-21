package com.voc.service.insights.engine.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/12 17:22
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsTableInfoVo  implements Serializable {
    /**
     * 表名
     */
    private String tableName;
    /**
     * 表注释
     */
    private String tableComment;
    /**
     * 列名集合
     */
    private List<String> columnsList;
    /**
     * 列名和注释的map
     * key:列名 value:注释
     */
    private Map<String,String> columnsMap;
    /**
     * 数据集
     */
    private List<JSONObject> data;
}
