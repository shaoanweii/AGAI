package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DictItemVo  implements Serializable {
    private String id;

    /**
     * 字典id
     */
    private String dictId;

    /**
     * 字典项文本
     */
    //@Excel(name = "字典项文本", width = 20)
    private String itemText;
    private String itemTextEn;
    private String itemKey;

    /**
     * 字典项值
     */
    //@Excel(name = "字典项值", width = 30)
    private String itemValue;

    /**
     * 描述
     */
    //@Excel(name = "描述", width = 40)
    private String description;

    /**
     * 排序
     */
    //@Excel(name = "排序", width = 15,type=4)
    private Integer sortOrder;


    /**
     * 状态（1启用 0不启用）
     */
    //@Dict(dicCode = "dict_item_status")
    private Integer status;

    private String operator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
