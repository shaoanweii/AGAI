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
import com.voc.service.insights.engine.api.IInsDictService;
import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.vo.DictInfoVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class DictJsonSerializer extends JsonSerializer<Object> implements ContextualSerializer {
    private static final Logger log = LoggerFactory.getLogger(DictJsonSerializer.class);

//    @Autowired
//    IInsDictService dictService;

    String code;
    String defaultValue;

    Class<?> type;


    public DictJsonSerializer() {
        log.info("--->> init {}", this.getClass().getSimpleName());
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            IInsDictService dictService = ServiceContextHolder.getApplicationContext().getBean(IInsDictService.class);
            List<DictInfoVo> dictInfoByCode = dictService.findDictInfoByCode(code);
            if(Objects.equals(String.class, type) && ObjectUtils.isNotEmpty(dictInfoByCode) ){
                Optional<DictInfoVo> vo = dictInfoByCode.stream().filter(item -> item.getTypeCode().equalsIgnoreCase(String.valueOf(value))).findFirst();
                Optional<DictInfoVo> next = dictInfoByCode.stream().filter(item -> ObjectUtils.isNotEmpty(item.getClassifyCode())).filter(item -> item.getClassifyCode().equalsIgnoreCase(String.valueOf(value))).findFirst();
                if (vo.isPresent()) {
                    gen.writeString(vo.get().getTypeCode());
                    gen.writeFieldName(gen.getOutputContext().getCurrentName().concat("Text"));
                    gen.writeString(vo.get().getTypeName());
                } else if (next.isPresent()) {
                    gen.writeString(next.get().getClassifyCode());
                    gen.writeFieldName(gen.getOutputContext().getCurrentName().concat("Text"));
                    gen.writeString(next.get().getClassifyName());
                } else if (StrUtil.isNotBlank(defaultValue)) {
                    gen.writeObject(defaultValue);
                } else {
                    gen.writeObject(value);
                }
            }else if(Objects.equals(List.class, type) && ObjectUtils.isNotEmpty(dictInfoByCode) ){
                List<String> values = JSONArray.parseArray(JSON.toJSONString(value), String.class);
                List<String> valuesText = new ArrayList<>();
                values.stream().forEach(item -> {
                    Optional<DictInfoVo> vo1 = dictInfoByCode.stream().filter(e -> e.getTypeCode().equalsIgnoreCase(String.valueOf(item))).findFirst();
                    Optional<DictInfoVo> next1 = dictInfoByCode.stream().filter(e -> ObjectUtils.isNotEmpty(e.getClassifyCode())).filter(e -> e.getClassifyCode().equalsIgnoreCase(String.valueOf(item))).findFirst();
                    if(vo1.isPresent()){
                        valuesText.add(vo1.get().getTypeName());
                    } else if (next1.isPresent()) {
                        valuesText.add(next1.get().getClassifyName());
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
            }else{
                gen.writeObject(value);
            }

        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            log.warn("{} {}", "可能在其他服务调用fegn客户端时,无法有效过去到 bean对象导致，", e.getMessage());

            if (StrUtil.isNotBlank(defaultValue)) {
                gen.writeObject(defaultValue);
            } else {
                gen.writeObject(value);
            }
        }
    }


    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Dict annotation = property.getAnnotation(Dict.class);
        // 只针对String类型属性进行脱敏
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            code = annotation.code();
            defaultValue = annotation.defaultText();
            type = String.class;
            return this;
        }else if(Objects.nonNull(annotation) && Objects.equals(List.class, property.getType().getRawClass())){
            code = annotation.code();
            defaultValue = annotation.defaultText();
            type = List.class;
            return this;
        }
        else {
            log.error("{}属性类型不支持，当前只支持字符串类型，请注意！", Dict.class.getSimpleName());
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
