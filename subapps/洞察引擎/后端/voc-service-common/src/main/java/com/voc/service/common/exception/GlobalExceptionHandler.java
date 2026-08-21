package com.voc.service.common.exception;

import com.voc.service.common.response.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.management.ServiceNotFoundException;
import java.util.Iterator;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    public GlobalExceptionHandler() {
        logger.info("--->> init GlobalExceptionHandler");
    }

    @ExceptionHandler(Exception.class)
    public Result exceptionHandle(Exception e) {
        if (!(e instanceof MethodArgumentNotValidException)) {
            if (e instanceof HttpMessageNotReadableException) {
                logger.error("参数解析失败", e);
                return Result.error(CommonErrorEnum.HTTPMESSAGENOTREADABLE_EXECPTION);
            } else if (e instanceof BindException) {
                logger.error("参数绑定失败", e);
                return Result.error(CommonErrorEnum.BIND_EXECPTION);
            } /*else if (e instanceof ValidationException) {
                String[] msg = e.getMessage().split(",");
                StringBuffer errorMessage = new StringBuffer();
                for (String s : msg) {
                    errorMessage.append(s.substring(s.indexOf(":"), s.length()));
                }
                logger.error("参数验证失败", e);
                return Result.error(CommonErrorEnum.VALIDATION_EXECPTION);
            }*/ else if (e instanceof HttpRequestMethodNotSupportedException) {
                logger.error("不支持当前请求方法", e);
                return Result.error(CommonErrorEnum.HTTPREQUESTMETHODNOTSUPPORTED_EXECPTION);
            } else if (e instanceof HttpMediaTypeNotSupportedException) {
                logger.error("不支持当前媒体类型", e);
                return Result.error(CommonErrorEnum.HTTPMEDIATYPENOTSUPPORTED_EXECPTION);
            } else if (e instanceof ServiceNotFoundException) {
                logger.error("请求方法缺失", e);
                return Result.error(CommonErrorEnum.SERVICENOTFOUND_EXECPTION);
            } else if (e instanceof ExpiredJwtException) {
//                logger.error("登录信息过期", e);
                return Result.error(CommonErrorEnum.EXPIRED_JWT_EXECPTION);
            } else if (e instanceof GetUserInfoException) {
//                logger.error("登录信息过期", e);
                return Result.error(CommonErrorEnum.USERINFO_EXECPTION);
            } else if (e instanceof IllegalArgumentException) {
                logger.error("请求参数校验失败", e);
                return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
            } else if (e instanceof AccountException) {
                AccountException ex = (AccountException) e;
                logger.error("{}", ex.getMessage());
                return Result.error(ex.getCode(),ex.getMessage());
            } else if (e instanceof BussinessException) {
                logger.error("请求参数校验失败", e);
                return Result.error(e.getMessage());
            } else if (e instanceof SecurityException) {
                logger.error("请求业务执行失败", e);
                if(((SecurityException) e).getCode()!=401){
                    return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
                }else{
                    return Result.error(((SecurityException) e).getCode(), ((SecurityException) e).getMessage());
                }
            } else {
                logger.error("系统内部错误", e);
                return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
            }
        } else {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
            List<FieldError> errorList = exception.getBindingResult().getFieldErrors();
            String message = "";
            if (null != errorList) {
                FieldError fieldError;
                for (Iterator iterator = errorList.iterator(); iterator.hasNext(); message = message + fieldError.getDefaultMessage() + ",") {
                    fieldError = (FieldError) iterator.next();
                }

                message = message.substring(0, message.lastIndexOf(","));
            }
            logger.error("请求方法参数错误", e);
            return Result.error(message);
        }
    }
}
