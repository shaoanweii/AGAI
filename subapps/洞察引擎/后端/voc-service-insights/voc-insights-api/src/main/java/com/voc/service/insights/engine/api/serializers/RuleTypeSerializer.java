package com.voc.service.insights.engine.api.serializers;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.voc.service.insights.engine.api.annotation.RuleType;
import com.voc.service.insights.engine.enums.RuleActionConditionType;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * @创建者: fanrong
 * @创建时间: 2024/4/9 10:54
 * @描述:
 **/
public class RuleTypeSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private static final Logger log = LoggerFactory.getLogger(RuleTypeSerializer.class);
    String code;
    String defaultValue;


    public RuleTypeSerializer() {
        log.info("--->> init {}", this.getClass().getSimpleName());
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            RuleActionConditionType ruleActionConditionType = RuleActionConditionType.getByCode(String.valueOf(value));
            if (ObjectUtils.isNotEmpty(ruleActionConditionType)) {
                gen.writeString(String.valueOf(value));
                gen.writeFieldName(gen.getOutputContext().getCurrentName().concat("Text"));
                gen.writeString(ruleActionConditionType.getText());
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
        RuleType annotation = property.getAnnotation(RuleType.class);
        // 只针对String类型属性进行脱敏
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            code = annotation.code();
            defaultValue = annotation.defaultText();
            return this;
        } else {
            log.error("{}属性类型不支持，当前只支持字符串类型，请注意！", RuleType.class.getSimpleName());
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
