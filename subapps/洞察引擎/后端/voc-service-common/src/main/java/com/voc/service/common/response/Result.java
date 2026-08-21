package com.voc.service.common.response;

/**
 * @version 1.0.0
 * @ClassName Result.java
 * @Description
 * @createTime 2022年09月06日 10:45
 * @Copyright futong
 */

import cn.hutool.core.util.ObjectUtil;
import com.voc.service.common.constant.CommonConstant;
import com.voc.service.common.exception.ErrorCode;
import com.voc.service.common.util.ServiceContextHolder;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.io.Serializable;

/**
 * 接口返回数据格式
 *
 * @date 2019年1月19日
 */
@Data
@Tag(name = "接口返回对象", description = "接口返回对象")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功标志
     */
    @Schema(description = "成功标志",example = "true")
    private boolean success = true;

    /**
     * 返回处理消息
     */
    @Schema(description = "返回处理消息",example = "操作成功！")
    private String message = "操作成功！";

    /**
     * 返回代码
     */
    @Schema(description = "返回代码" ,example = "200")
    private String code = "500";

    /**
     * 返回数据对象 data
     */
    @Schema(description = "返回数据对象")
    private T result;

    /**
     * 时间戳
     */
    @Schema(description = "请求标识" ,example = "634b859b92854c9ea8dd6c8c06c32aa7.134.17095415899050001")
    private String tid = String.valueOf(System.currentTimeMillis());

    public Result() {

    }

    public static <T> Result<T> OK() {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(CommonConstant.SC_OK_200);
        r.setMessage("成功");
        return r;
    }

    public static <T> Result<T> OK(T data) {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(CommonConstant.SC_OK_200);
        r.setResult(data);
        return r;
    }

    public static <T> Result<T> OK(String msg, T data) {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(CommonConstant.SC_OK_200);
        r.setMessage(msg);
        r.setResult(data);
        return r;
    }

    public static Result<Object> error(String msg) {
        return error(Integer.parseInt(CommonConstant.SC_INTERNAL_SERVER_ERROR_500), msg);
    }

    public static <T> Result<T> errors(String msg) {
        return errors(Integer.parseInt(CommonConstant.SC_INTERNAL_SERVER_ERROR_500), msg);
    }


    public static Result<Object> error(String msg,Object data) {
        return error(Integer.parseInt(CommonConstant.SC_INTERNAL_SERVER_ERROR_500), msg,data);
    }

    public static Result<Object> error(ErrorCode err) {
        return error(err.getCode(), err.getMessage());
    }

    public static Result<Object> error(int code, String msg) {
        Result<Object> r = new Result<Object>();
        r.setCode(String.valueOf(code));
        r.setMessage(msg);
        r.setSuccess(false);
        return r;
    }

    public static <T> Result<T> errors(int code, String msg) {
        Result<T> r = new Result<T>();
        r.setCode(String.valueOf(code));
        r.setMessage(msg);
        r.setSuccess(false);
        return r;
    }

    public static Result<Object> error(int code, String msg,Object data) {
        Result<Object> r = new Result<Object>();
        r.setCode(String.valueOf(code));
        r.setMessage(msg);
        r.setSuccess(false);
        r.setResult(data);
        return r;
    }

    /**
     * 无权限访问返回结果
     */
//    public static Result<Object> noauth(String msg) {
//        return error(Integer.parseInt(CommonConstant.SC_NO_AUTHZ), msg);
//    }
    public Result<T> success(String message) {
        this.message = message;
        this.code = CommonConstant.SC_OK_200;
        this.success = true;
        return this;
    }

    public Result<T> error500(String message) {
        this.message = message;
        this.code = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;
        this.success = false;
        return this;
    }

    public String getTid() {
        if(ObjectUtil.isNull(ServiceContextHolder.traceId()) || "Ignored_Trace".equalsIgnoreCase(ServiceContextHolder.traceId())){
//            log.info("tid: {}", tid);
            return this.tid;
        }
//        log.info("tid: {}", ServiceContextHolder.traceId());
        return ServiceContextHolder.traceId();

    }
/*
    @JsonIgnore
    private String onlTable;*/

}
