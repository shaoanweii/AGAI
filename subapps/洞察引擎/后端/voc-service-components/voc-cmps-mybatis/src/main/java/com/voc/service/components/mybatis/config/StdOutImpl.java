package com.voc.service.components.mybatis.config;

/**
 * @Title: StdOutImpl
 * @Package: com.voc.service.components.mybatis.config
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/22 17:48
 * @Version:1.0
 */

import org.apache.ibatis.logging.Log;

public class StdOutImpl implements Log {
    public StdOutImpl(String clazz) {
    }

    public boolean isDebugEnabled() {
        return true;
    }

    public boolean isTraceEnabled() {
        return true;
    }

    public void error(String s, Throwable e) {
        System.err.println(s);
        e.printStackTrace(System.err);
    }

    public void error(String s) {
        System.err.println(s);
    }

    public void debug(String s) {
        System.out.println(s);
    }

    public void trace(String s) {
//        System.out.println(s);
    }

    public void warn(String s) {
        System.out.println(s);
    }
}