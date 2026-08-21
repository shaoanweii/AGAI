package com.voc.service.components.sms.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.exception.ErrorCode;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class KYSmsSendResponse<T> {
    /**
     * 调用是否成功
     */
    private boolean success;
    /**
     * 调用返回结果码
     */
    private Integer code;
    /**
     * 错误信息
     */
    private String msg;
    /**
     * 返回的结果数据
     */
    @JsonProperty("data")
    private T data;

    /**
     * 发生异常后返回相应信息
     *
     * @param e 异常信息
     * @return 发生异常后的相应信息
     */
    public static KYSmsSendResponse failResponse(BussinessException e) {
        KYSmsSendResponse commonResponse = new KYSmsSendResponse();
        commonResponse.setSuccess(Boolean.FALSE);
        commonResponse.setCode(e.getCode());
        commonResponse.setMsg(e.getMessage());
        return commonResponse;
    }

    /**
     * 发生异常后返回相应信息
     *
     * @param e 异常信息编码
     * @return 发生异常后的相应信息
     */
    public static KYSmsSendResponse failResponse(ErrorCode e) {
        KYSmsSendResponse commonResponse = new KYSmsSendResponse();
        commonResponse.setSuccess(Boolean.FALSE);
        commonResponse.setCode(e.getCode());
        commonResponse.setMsg(e.getMessage());
        return commonResponse;
    }

    /**
     * 正常调用后返回相关的信息
     *
     * @param <T>        返回的被封装对象的泛型
     * @param resultData 返回的对象
     * @return 封装相应对象的rest 返回对象
     */
    public static <T> KYSmsSendResponse successResponse(T resultData) {
        KYSmsSendResponse commonResponse = new KYSmsSendResponse();
        commonResponse.setSuccess(Boolean.TRUE);
        final Integer code = 200;
        commonResponse.setCode(code);
        commonResponse.setMsg("操作成功");
        commonResponse.setData(resultData);
        return commonResponse;
    }

    /**
     * 调用微服务 因服务故障 或者服务降级 快速返回给客户端的信息
     *
     * @return 服务故障或服务降级 快速返回给客户端消息
     */
    public static KYSmsSendResponse fallbackResponse() {
        return failResponse(new BussinessException(CommonErrorEnum.FALLBACK_EXECPTION));
    }
}
