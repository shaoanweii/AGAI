package com.voc.service.insights.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/12 17:20
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsTableInfoModel  implements Serializable {
    /**
     * 表名
     */
    private String tableName;
    /**
     * 列名
     */
    @Builder.Default
    private List<String> columns = new ArrayList<>();
    /**
     * 客户编码
     */
    private String clientId;
}
