package com.voc.service.insights.engine.api.serializers;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.voc.service.insights.engine.api.annotation.Weight;
import com.voc.service.insights.engine.enums.RuleWeight;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * @创建者: fanrong
 * @创建时间: 2024/4/15 11:07
 * @描述:
 **/
public class WeightSerializer extends JsonSerializer<Object> implements ContextualSerializer {
    private static final Logger log = LoggerFactory.getLogger(WeightSerializer.class);
    String code;
    String defaultValue;


    public WeightSerializer() {
        log.info("--->> init {}", this.getClass().getSimpleName());
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            RuleWeight ruleWeight = RuleWeight.getByText(Long.valueOf(String.valueOf(value)));
            if (ObjectUtils.isNotEmpty(ruleWeight)) {
                gen.writeString(String.valueOf(ruleWeight.getCode()));
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
        Weight annotation = property.getAnnotation(Weight.class);
        // 只针对String类型属性进行脱敏
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            code = annotation.code();
            defaultValue = annotation.defaultText();
            return this;
        } else {
            log.error("{}属性类型不支持，当前只支持字符串类型，请注意！", Weight.class.getSimpleName());
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
