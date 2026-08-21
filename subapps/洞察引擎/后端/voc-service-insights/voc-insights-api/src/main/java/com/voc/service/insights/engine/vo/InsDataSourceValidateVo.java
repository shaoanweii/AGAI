package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/14 下午1:23
 * @描述:
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsDataSourceValidateVo  implements Serializable {
    private String message;
    private String batchId;
    private String success;
    private String total;
}
