package com.voc.service.common.exception;

/**
 * 业务逻辑异常接口
 */
public interface ErrorCode {
    /**
     * 异常编码
     *
     * @return 异常编码
     */
    Integer getCode();

    /**
     * 异常信息
     *
     * @return 异常信息
     */
    String getMessage();
}
