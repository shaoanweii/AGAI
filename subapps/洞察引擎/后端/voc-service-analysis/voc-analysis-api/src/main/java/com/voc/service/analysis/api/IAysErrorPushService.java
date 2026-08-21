package com.voc.service.analysis.api;

import com.voc.service.analysis.model.ErrorPushModel;

/**
 * @Title: IAysErrorPushService
 * @Package: com.voc.service.analysis.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/7/25 14:24
 * @Version:1.0
 */
public interface IAysErrorPushService {
    public static final String ACTION_ADD = "add";
    void push(ErrorPushModel model) throws Exception;
}
