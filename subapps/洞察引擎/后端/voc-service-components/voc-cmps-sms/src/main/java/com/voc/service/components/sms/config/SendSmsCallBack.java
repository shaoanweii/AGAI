package com.voc.service.components.sms.config;

public interface SendSmsCallBack {

    /**
     * 短信回调函数
     *
     * @param result 发送结果
     */
    void callBack(String result);

}
