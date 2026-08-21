package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/4/23 16:51
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsValidateInfoVo  implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    /**
     * 规则id
     */
    private String regulationId;

    /**
     * 数据处理链路标识
     */
    private String workId;
    /**
     * 数据处理类型 单规则类型：0 测试类型：1
     */
    private String singleOrFullType;
}
