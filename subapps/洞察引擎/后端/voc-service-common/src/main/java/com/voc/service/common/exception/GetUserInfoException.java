package com.voc.service.common.exception;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName SystemException
 * @Description ckcui
 * @createTime 2023年09月06日 17:55
 * @Copyright futong
 */
public class GetUserInfoException extends SystemException {

    public GetUserInfoException(Exception e) {
        super(e);
    }

    public GetUserInfoException(ErrorCode errorCode) {
        super(errorCode);
    }

    public GetUserInfoException(String message) {
        super(message);
    }

    public GetUserInfoException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public GetUserInfoException(Integer errorCode, String message) {
        super(errorCode, message);
    }

    public GetUserInfoException(ErrorCode errorCode, Throwable e) {
        super(errorCode, e);
    }

    public GetUserInfoException(ErrorCode errorCode, Throwable e, Object... args) {
        super(errorCode, e, args);
    }
}
