package com.voc.service.common.aspect;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.io.IOException;


@ControllerAdvice
public class GlobalControllerAdviceController {
    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdviceController.class);
    //WebDataBinder是用来绑定请求参数到指定的属性编辑器，可以继承WebBindingInitializer
    //来实现一个全部controller共享的dataBinder Java代码
    @InitBinder
    public void dataBind(WebDataBinder binder) {
        ///注册
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
                .deserializerByType(String.class, new StdScalarDeserializer<String>(String.class) {
                    @Override
                    public String deserialize(JsonParser jsonParser, DeserializationContext ctx)
                            throws IOException {
                        // 重点在这儿:如果为空白则返回null
                        logger.trace("如果为空白则返回null");
                        String value = jsonParser.getValueAsString();
                        if (value == null || value.trim().isEmpty()) {
                            return null;
                        }
                        return value;
                    }
                });
    }
}
