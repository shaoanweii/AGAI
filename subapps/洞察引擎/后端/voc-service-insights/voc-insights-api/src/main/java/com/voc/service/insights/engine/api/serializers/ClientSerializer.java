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
import com.voc.service.insights.engine.api.IInsCustomerInfoService;
import com.voc.service.insights.engine.api.annotation.Client;
import com.voc.service.insights.engine.vo.CustomerInfoVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


public class ClientSerializer extends JsonSerializer<Object> implements ContextualSerializer {


    private static final Logger log = LoggerFactory.getLogger(ClientSerializer.class);
    String code;
    String defaultValue;
    Class<?> type;

    public ClientSerializer() {
        log.info("--->> init {}", this.getClass().getSimpleName());
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            IInsCustomerInfoService dictService = ServiceContextHolder.getApplicationContext().getBean(IInsCustomerInfoService.class);

            List<CustomerInfoVo> dictItemsByCode = dictService.findAllCustomerInfo();
            if(Objects.equals(String.class, type) && ObjectUtils.isNotEmpty(dictItemsByCode) ){
                Optional<CustomerInfoVo> vo = dictItemsByCode.stream().filter(item -> item.getId().equalsIgnoreCase(String.valueOf(value))).findFirst();
                if (vo.isPresent()) {
                    gen.writeString(String.valueOf(value));
                    gen.writeFieldName(gen.getOutputContext().getCurrentName().concat("Text"));
                    gen.writeString(vo.get().getAbbreviation());
                } else if (StrUtil.isNotBlank(defaultValue)) {
                    gen.writeObject(defaultValue);
                } else {
                    gen.writeObject(value);
                }
            } else if ((Objects.equals(List.class, type)||Objects.equals(Set.class, type) )&& ObjectUtils.isNotEmpty(dictItemsByCode)) {
                List<String> values = JSONArray.parseArray(JSON.toJSONString(value), String.class);
                List<String> valuesText = new ArrayList<>();
                values.forEach(item -> {
                    Optional<CustomerInfoVo> vo = dictItemsByCode.stream().filter(i -> i.getId().equalsIgnoreCase(item)).findFirst();
                    if (vo.isPresent()) {
                        valuesText.add(vo.get().getAbbreviation());
                    }
                });

                if (ObjectUtils.isNotEmpty(valuesText)) {
                    gen.writeObject(value);
                    gen.writeFieldName(gen.getOutputContext().getCurrentName().concat("Text"));
                    gen.writeObject(valuesText);
                } else if (StrUtil.isNotBlank(defaultValue)) {
                    gen.writeObject(defaultValue);
                } else {
                    gen.writeObject(value);
                }
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
        Client annotation = property.getAnnotation(Client.class);
        // 只针对String类型属性进行脱敏
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            code = annotation.code();
            defaultValue = annotation.defaultText();
            type = String.class;
            return this;
        } else if (Objects.nonNull(annotation) && (Objects.equals(List.class, property.getType().getRawClass())||Objects.equals(Set.class, property.getType().getRawClass()))) {
            code = annotation.code();
            defaultValue = annotation.defaultText();
            type = Objects.equals(List.class, property.getType().getRawClass())?List.class:Set.class;
            return this;
        } else {
            log.error("{}属性类型不支持，当前只支持字符串类型，请注意！", Client.class.getSimpleName());
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
