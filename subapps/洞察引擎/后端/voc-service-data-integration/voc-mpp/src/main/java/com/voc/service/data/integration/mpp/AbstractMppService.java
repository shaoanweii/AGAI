package com.voc.service.data.integration.mpp;

/**
 * @Title: AbstractMppService
 * @Package: com.voc.service.data.integration.in.scheduled
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 10:50
 * @Version:1.0
 */
public abstract class AbstractMppService {

    public abstract void log(String msg);
    public abstract void log(String msg,Object...args);
    public abstract void log(Throwable e);
}
