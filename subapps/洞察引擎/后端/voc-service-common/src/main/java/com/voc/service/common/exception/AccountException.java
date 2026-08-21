package com.voc.service.common.exception;

import org.springframework.security.authentication.AccountStatusException;

import java.text.MessageFormat;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AccountException
 * @Description ckcui
 * @createTime 2023年12月05日 11:21
 * @Copyright futong
 */
public class AccountException extends RuntimeException {
    /**
     * 业务逻辑异常编码
     */
    private Integer code;
    /**
     * 业务异常提示
     */
    private String message;

    /**
     * 业务异常构造类
     */
    public AccountException(Exception e) {
        this(e.getMessage());
    }

    public AccountException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public AccountException(String message) {
        this.code = CommonErrorEnum.UNKNOW_EXECPTION.getCode();
        this.message = message;
    }

    /**
     * 业务异常构造类 使用参数替换定义异常类型常量消息里的占位符号
     *
     * @param errorCode 定义的异常类型常量
     * @param args      替换占位符的参数
     */

    public AccountException(ErrorCode errorCode, Object... args) {
        this.code = errorCode.getCode();
        this.message = MessageFormat.format(errorCode.getMessage(), args);
    }

    /**
     * 业务异常构造类 使用参数替换定义异常类型常量消息里的占位符号
     *
     * @param errorCode 定义的异常类型编码
     * @param message   定义异常类型消息
     */
    public AccountException(Integer errorCode, String message) {
        this.code = errorCode;
        this.message = message;
    }

    /**
     * 将系统要处理的检查异常转成业务逻辑异常
     *
     * @param errorCode 定义的异常类型常量
     * @param e         引起业务异常的异常
     */
    public AccountException(ErrorCode errorCode, Throwable e) {
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
    public AccountException(ErrorCode errorCode, Throwable e, Object... args) {
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
