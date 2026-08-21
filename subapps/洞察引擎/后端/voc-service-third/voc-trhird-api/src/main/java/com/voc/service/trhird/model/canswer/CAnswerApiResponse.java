package com.voc.service.trhird.model.canswer;

import lombok.Data;

/**
 * @description:
 * @author: yonglongjiang
 * @time: 2025/11/24
 */
@Data
public class CAnswerApiResponse {

    private int errcode;

    private String errmsg;

    private Object data;
}
