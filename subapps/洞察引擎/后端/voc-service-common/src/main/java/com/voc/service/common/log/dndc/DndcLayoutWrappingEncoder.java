package com.voc.service.common.log.dndc;

import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.common.util.ServiceContextHolder;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * @Title: DndcLayoutWrappingEncoder
 * @Package: com.voc.service.common.log
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/29 19:20
 * @Version:1.0
 */
public class DndcLayoutWrappingEncoder<E> extends LayoutWrappingEncoder {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public byte[] encode(Object event) {
        JSONObject jsonObject = JSONUtil.createObj();
        ch.qos.logback.classic.spi.LoggingEvent loggingEvent = (ch.qos.logback.classic.spi.LoggingEvent) event;
        try {
            jsonObject.put("logid", UUID.randomUUID().toString().replace("-", ""));
            jsonObject.put("logfrom", ServiceContextHolder.getSystemId());
            jsonObject.put("busid", UUID.randomUUID().toString().replace("-", ""));
            jsonObject.put("operid", ServiceContextHolder.getUserId());
            final String dateStr = DateUtil.format(LocalDateTime.ofInstant(loggingEvent.getInstant(), ZoneId.systemDefault()), sdf.toPattern());
            jsonObject.put("opertime", dateStr);

            //biz(业务),pfm(性能),err(错误)
            StackTraceElement[] cda = loggingEvent.getCallerData();
            switch (loggingEvent.getLevel().levelInt) {
                case ch.qos.logback.classic.Level.INFO_INT:
                    jsonObject.put("logtype", "biz");
                    jsonObject.put("biztype", "业务");
                    break;
                case ch.qos.logback.classic.Level.ERROR_INT:
                    jsonObject.put("logtype", "err");
                    jsonObject.put("biztype", "错误");
                    break;
                default:
                    jsonObject.put("logtype", "pfm");
                    jsonObject.put("biztype", "性能");
            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            jsonObject.put("opercontext", this.layout.doLayout(event).replaceAll("\"","\\\"").replace("\r\n",""));
            System.out.println(jsonObject.get("opercontext").toString());
        }
        return this.convertToBytes(jsonObject.toString().concat("\r\n"));
    }

    private byte[] convertToBytes(String s) {
//        return this.charset == null ? s.getBytes() : s.getBytes(this.charset);
        return this.getCharset() == null ? s.getBytes() : s.getBytes(this.getCharset());
    }
}
