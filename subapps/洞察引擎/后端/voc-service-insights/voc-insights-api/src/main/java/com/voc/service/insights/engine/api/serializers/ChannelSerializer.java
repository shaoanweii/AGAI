package com.voc.service.insights.engine.api.serializers;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsChannelInfoService;
import com.voc.service.insights.engine.api.annotation.Channel;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.vo.ChannelInfoVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @创建者: fanrong
 * @创建时间: 2024/4/15 11:24
 * @描述:
 **/
public class ChannelSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private static final Logger log = LoggerFactory.getLogger(ChannelSerializer.class);
    String code;
    String defaultValue;

    Class<?> type;

    public ChannelSerializer() {
        log.info("--->> init {}", this.getClass().getSimpleName());
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            IInsChannelInfoService channelInfoService = ServiceContextHolder.getApplicationContext().getBean(IInsChannelInfoService.class);
            List<ChannelInfoVo> allChannelInfo = channelInfoService.findAllChannelInfo(InsChannelInfoModel.builder().clientId(ServiceContextHolder.getClientId()).build());
            if (Objects.equals(String.class, type) && ObjectUtils.isNotEmpty(allChannelInfo)) {
                Optional<ChannelInfoVo> optional = allChannelInfo.stream().filter(e1 -> e1.getId().equals(value)).findFirst();
                if (optional.isPresent()) {
                    gen.writeString(String.valueOf(value));
                    gen.writeFieldName(gen.getOutputContext().getCurrentName().concat("Text"));
                    gen.writeString(optional.get().getName());
                }else {
                    gen.writeObject(value);
                }
            } else if (Objects.equals(List.class, type) && ObjectUtils.isNotEmpty(allChannelInfo)) {
                List<String> channels = JSONArray.parseArray(JSON.toJSONString(value), String.class);
                List<String> channelText = new ArrayList<>();
                channels.stream().forEach(e -> {
                    Optional<ChannelInfoVo> optional = allChannelInfo.stream().filter(e1 -> e1.getId().equals(e)).findFirst();
                    if (optional.isPresent()) {
                        ChannelInfoVo channelInfoVo = optional.get();
                        channelText.add(channelInfoVo.getName());
                    }
                });
                gen.writeObject(value);
                gen.writeFieldName(gen.getOutputContext().getCurrentName().concat("Text"));
                gen.writeObject(channelText);
            } else if (StrUtil.isNotBlank(defaultValue)) {
                gen.writeObject(defaultValue);
            } else {
                gen.writeObject(value);
            }
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
            log.warn("{} {}", "可能在其他服务调用fegn客户端时,无法有效过去到 bean对象导致，", e.getMessage());

            if (StrUtil.isNotBlank(defaultValue)) {
                gen.writeObject(defaultValue);
            } else {
                gen.writeObject(value);
            }
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Channel annotation = property.getAnnotation(Channel.class);
        if (Objects.nonNull(annotation) && Objects.equals(List.class, property.getType().getRawClass())) {
            code = annotation.code();
            defaultValue = annotation.defaultText();
            type = List.class;
            return this;
        } else if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            code = annotation.code();
            defaultValue = annotation.defaultText();
            type = String.class;
            return this;
        } else {
            log.error("{}属性类型不支持，当前只支持字符串类型，请注意！", Channel.class.getSimpleName());
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
