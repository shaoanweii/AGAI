package com.voc.service.trhird.model.ktm;

import lombok.Data;

/**
 * @description:
 * @author: yonglongjiang
 * @time: 2025/10/13
 */
@Data
public class KtmApiResponse<T> {
    private String result;
    private String errorMsg;
    private T data;
    private Object extInfo;
}
