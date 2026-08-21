package com.voc.service.common.constant;

import java.util.concurrent.TimeUnit;

public class GlobalConstants {
    public static final String BASE_CHECK_CODES = "qwertyuiplkjhgfdsazxcvbnmQWERTYUPLKJHGFDSAZXCVBNM1234567890";

    //token 默认有效期默认值 单位：小时
    public static final long TOKEN_EXPIRATION = 24;
    //token 默认有效期 单位：小时
    public static final TimeUnit  TOKEN_TIME_UNIT =  TimeUnit.HOURS;
}
