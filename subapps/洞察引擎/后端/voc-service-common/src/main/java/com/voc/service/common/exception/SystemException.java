package com.voc.service.common.exception;

import java.text.MessageFormat;

/**
 * 自定义业务异常处理类
 *
 * @version V1.0
 * @Description:
 * @Date: 2021/6/1-9:35 下午
 * @Copyright: http://www.futong.com.cn . All rights reserved.
 */
public class SystemException extends RuntimeException {
    /**
     * 业务逻辑异常编码
     */
    private Integer code;
    /**
     * 业务异常提示
     */
    private String message;

    public SystemException(Exception e) {
        this(e.getMessage());
    }

    /**
     * 业务异常构造类
     *
     * @param errorCode 定义的异常类型常量
     */
    public SystemException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public SystemException(String message) {
        this.code = CommonErrorEnum.UNKNOW_EXECPTION.getCode();
        this.message = message;
    }

    /**
     * 业务异常构造类 使用参数替换定义异常类型常量消息里的占位符号
     *
     * @param errorCode 定义的异常类型常量
     * @param args      替换占位符的参数
     */
    public SystemException(ErrorCode errorCode, Object... args) {
        this.code = errorCode.getCode();
        this.message = MessageFormat.format(errorCode.getMessage(), args);
    }

    /**
     * 业务异常构造类 使用参数替换定义异常类型常量消息里的占位符号
     *
     * @param errorCode 定义的异常类型编码
     * @param message   定义异常类型消息
     */
    public SystemException(Integer errorCode, String message) {
        this.code = errorCode;
        this.message = message;
    }

    /**
     * 将系统要处理的检查异常转成业务逻辑异常
     *
     * @param errorCode 定义的异常类型常量
     * @param e         引起业务异常的异常
     */
    public SystemException(ErrorCode errorCode, Throwable e) {
        super(e);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * 将系统要处理的检查异常转成业务逻辑异常
     *
     * @param errorCode 定义的异常类型常量
     * @param e         引起业务异常的异常
     */
    public SystemException(ErrorCode errorCode, Throwable e, Object... args) {
        super(e);
        this.code = errorCode.getCode();
        this.message = MessageFormat.format(errorCode.getMessage(), args);
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
