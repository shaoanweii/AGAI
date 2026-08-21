package com.voc.service.data.integration.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/10/28 下午3:35
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataValidateResultVo implements Serializable {
    /**
     * 数据总量
     */
    private Integer totalCount;
    /**
     * 数据校验成功的数量
     */
    private Integer verificationSuccessCount;
    /**
     * 数据
     */
    private String data;
}
