package com.voc.service.common.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.TimeZone;

/**
 * 功能描述
 * <p>
 * 2021-06-01 15:45:34
 *
 * @version V1.0
 */
public class JsonMapper {
    private static final Logger logger = LoggerFactory.getLogger(JsonMapper.class);
    /**
     * 原生jackson mapper
     */
    private ObjectMapper mapper;

    private JsonMapper() {
        mapper = new ObjectMapper();
        //设置命名转换方式,规范属性命名为驼峰式,数据库为下划线分割方式.例如:dataTime,序列化后为date_Time,
//        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        //设置所有类型的数据都会被检测
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //设置时间序列化格式处理
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        mapper.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
        mapper.configure(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        //设置序列化空值转为[]或者""
//        mapper.setSerializerFactory(mapper.getSerializerFactory().withSerializerModifier(new JackSonNULLSerializer()));
        //防止数值类型精度丢失
        SimpleModule xssModule = new SimpleModule();
        xssModule.addSerializer(Long.class, ToStringSerializer.instance);
        xssModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        xssModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
        mapper.registerModule(xssModule);
        //对时间类型的处理
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        mapper.registerModule(javaTimeModule);
    }

    /**
     * 单例模式,获取实例
     *
     * @return 实例对象
     */
    public static JsonMapper getInstances() {
        return JsonMapperHolder.jsonMapper;
    }

    /**
     * Object可以是POJO，也可以是Collection或数组。 如果对象为Null, 返回"null". 如果集合为空集合, 返回"[]".
     *
     * @param object 要转换成JSON 字符串的POJO对象
     * @return 对象的json串
     */
    public String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            logger.warn("write to json string error:{}", object, e);
            return null;
        }
    }

    /**
     * 反序列化POJO或简单Collection如List<String>.
     * 如果JSON字符串为Null或"null"字符串, 返回Null. 如果JSON字符串为"[]", 返回空集合.
     * 如需反序列化复杂Collection如List<MyBean>;, 请使用fromJson(String, JavaType)
     *
     * @param <T>        需要转换成对象类型
     * @param jsonString 要转换成对象的JSON字符串
     * @param clazz      转换成的对象类型
     * @return 转换后的对象
     */
    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            logger.warn("parse json string error:{}", jsonString, e);
            return null;
        }
    }

    public <T> T fromJson(Object obj, Class<T> clazz) {
        if (ObjectUtils.isEmpty(obj)) {
            return null;
        }
        try {

            return mapper.readValue(mapper.writeValueAsString(obj), clazz);
        } catch (IOException e) {
            logger.warn("parse json string error:{}", obj, e);
            return null;
        }
    }

    /**
     * 反序列化复杂Collection如List<Bean>
     * contructCollectionType()或contructMapType()构造类型, 然后调用本函数.
     *
     * @param <T>        需要转换成对象类型
     * @param jsonString 要转换成对象的JSON字符串
     * @param javaType   转换成的复杂类型
     * @return 转换后的对象
     */
    public <T> T fromJson(String jsonString, JavaType javaType) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return (T) mapper.readValue(jsonString, javaType);
        } catch (IOException e) {
            logger.warn("parse json string error{}", jsonString, e);
            return null;
        }
    }

    /**
     * 反序列化复杂的带有泛型的对象
     * RestResponse<Response> object = JsonMapper.nonNullMapper().fromJson(json, new TypeReference<RestResponse<Response>>() {});
     *
     * @param <T>           需要转换成对象类型
     * @param jsonString    要转换成对象的JSON字符串
     * @param typeReference 带泛型对象的类型
     * @return 转换后的对象
     */
    public <T> T fromJson(String jsonString, TypeReference typeReference) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return (T) mapper.readValue(jsonString, typeReference);
        } catch (IOException e) {
            logger.warn("parse json string error:{}", jsonString, e);
            return null;
        }
    }

    /**
     * 构造Collection类型. 构造集合类型的javaType
     *
     * @param collectionClass 集合类型
     * @param elementClass    集合内元素的类型
     * @return 复杂集合类型的JavaType
     */
    public JavaType buildCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass) {
        return mapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);
    }

    /**
     * 构造Map类型.
     *
     * @param mapClass   map类型
     * @param keyClass   map类型key的类型
     * @param valueClass map类型value的类型
     * @return 复杂Map类型类型的JavaType
     */
    public JavaType buildMapType(Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return mapper.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

    /**
     * 当JSON里只含有Bean的部分属性時，更新一個已存在Bean，只覆盖該部分的属性.
     *
     * @param jsonString json 字符串
     * @param object     更新对象
     */
    public void update(String jsonString, Object object) {
        try {
            mapper.readerForUpdating(object).readValue(jsonString);
        } catch (Exception e) {
            logger.warn("update json string:{} to object:{} error.", jsonString, object, e);
        }
    }

    /**
     * 输出JSONP格式的数据
     *
     * @param functionName 前端javascript回调的方法名称
     * @param object       要转成jsonP 格式的对象
     * @return JSONP格式数据
     */
    public String toJsonP(String functionName, Object object) {
        return toJson(new JSONPObject(functionName, object));
    }

    /**
     * 是否使用Enum的toString函数来读写Enum,
     * 为false时使用Enum的name()函数来读写Enum, 默认为false.
     * 注意此方法Mapper创建后,在所有读写动作之前调用.
     */
    public void enableEnumUseToString() {
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    }

    /**
     * 取出Mapper做进一步的设置或使用其他序列化API.
     *
     * @return jackson原生的 ObjectMapper
     */
    public ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * 静态内部类
     */
    private static class JsonMapperHolder {
        private static JsonMapper jsonMapper = new JsonMapper();
    }
}
