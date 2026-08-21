package com.voc.service.data.integration.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/10/24 下午3:57
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataRequestModel implements Serializable {
    /**
     * 客户标识
     */
    private String clientId;
    /**
     * 日期
     */
    private String date;
    /**
     * 数据状态
     */
    private String status;
}
