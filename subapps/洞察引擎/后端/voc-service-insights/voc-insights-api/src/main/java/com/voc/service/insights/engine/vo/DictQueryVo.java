package com.voc.service.insights.engine.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典查询参数实体
 */
@Data
public class DictQueryVo  implements Serializable {
    /**
     * 表名
     */
    private String table;
    /**
     * 存储列
     */
    private String code;

    /**
     * 显示列
     */
    private String text;

    /**
     * 关键字查询
     */
    private String keyword;

    /**
     * 存储列的值 用于回显查询
     */
    private String codeValue;

}
