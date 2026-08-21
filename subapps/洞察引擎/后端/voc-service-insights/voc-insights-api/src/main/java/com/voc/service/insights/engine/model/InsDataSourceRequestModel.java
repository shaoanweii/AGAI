package com.voc.service.insights.engine.model;

import lombok.*;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/10/24 下午3:39
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsDataSourceRequestModel implements Serializable {
    /**
     * 客户标识
     */
    private String clientId;
    /**
     * 时间
     */
    private String date;

    private String status;
}
