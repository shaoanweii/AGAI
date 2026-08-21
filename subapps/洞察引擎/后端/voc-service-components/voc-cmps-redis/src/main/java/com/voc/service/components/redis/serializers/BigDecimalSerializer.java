package com.voc.service.components.redis.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @Title: BigDecimalSerializer
 * @Package: com.voc.service.insights.report.serializer
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/12 17:25
 * @Version:1.0
 */
public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {
    public BigDecimalSerializer() {
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            BigDecimal number = value.setScale(1, BigDecimal.ROUND_HALF_UP);
//            gen.writeNumber(number);
            gen.writeString(number.toPlainString());
        } else {
            gen.writeNumber(value);
        }

    }

    @Override
    public void serializeWithType(BigDecimal value, JsonGenerator gen,
                                  SerializerProvider serializers, TypeSerializer typeSer) throws IOException {

        // 1. 写入类型信息前缀
        WritableTypeId typeId = typeSer.writeTypePrefix(gen,
                typeSer.typeId(value, JsonToken.VALUE_STRING)); // 使用 VALUE_STRING

        // 2. 写入实际值
        serialize(value, gen, serializers);

        // 3. 写入类型信息后缀
        typeSer.writeTypeSuffix(gen, typeId);
    }

    @Override
    public Class<BigDecimal> handledType() {
        return BigDecimal.class;
    }
}
