package com.voc.service.common.exception;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName SystemException
 * @Description ckcui
 * @createTime 2023年09月06日 17:55
 * @Copyright futong
 */
public class ExpiredJwtException extends SystemException {

    public ExpiredJwtException(Exception e) {
        super(e);
    }

    public ExpiredJwtException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ExpiredJwtException(String message) {
        super(message);
    }

    public ExpiredJwtException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public ExpiredJwtException(Integer errorCode, String message) {
        super(errorCode, message);
    }

    public ExpiredJwtException(ErrorCode errorCode, Throwable e) {
        super(errorCode, e);
    }

    public ExpiredJwtException(ErrorCode errorCode, Throwable e, Object... args) {
        super(errorCode, e, args);
    }
}
