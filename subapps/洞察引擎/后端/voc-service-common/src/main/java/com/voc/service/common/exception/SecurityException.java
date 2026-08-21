package com.voc.service.common.exception;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName SystemException
 * @Description ckcui
 * @createTime 2023年09月06日 17:55
 * @Copyright futong
 */
public class SecurityException extends SystemException {

    public SecurityException(Exception e) {
        super(e);
    }

    public SecurityException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public SecurityException(Integer errorCode, String message) {
        super(errorCode, message);
    }

    public SecurityException(ErrorCode errorCode, Throwable e) {
        super(errorCode, e);
    }

    public SecurityException(ErrorCode errorCode, Throwable e, Object... args) {
        super(errorCode, e, args);
    }
}
