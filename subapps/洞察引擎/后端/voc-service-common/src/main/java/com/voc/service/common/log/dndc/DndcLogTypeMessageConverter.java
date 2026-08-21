package com.voc.service.common.log.dndc;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @Title: DndcMessageConverter
 * @Package: com.voc.service.common.log
 * @Description:
 * @Author: cuick
 * @Date: 2024/12/3 17:02
 * @Version:1.0
 */
public class DndcLogTypeMessageConverter extends MessageConverter {
    @Override
    public String convert(ILoggingEvent event) {
        return "eeeeeeee";
    }
}
