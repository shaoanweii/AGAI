package com.voc.service.components.sms.tool;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Thinkpad
 */

@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {


    SMS_SEND_FAILED(701036, "短信发送失败");

    /**
     * 业务异常编码
     */
    private Integer code;
    /**
     * 业务异常描述
     */
    private String message;
}
