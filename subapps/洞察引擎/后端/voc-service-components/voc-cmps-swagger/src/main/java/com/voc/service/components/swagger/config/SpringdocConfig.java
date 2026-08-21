package com.voc.service.components.swagger.config;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.xiaoymin.knife4j.spring.configuration.Knife4jProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Lazy(false)
@Configuration(proxyBeanMethods = false)
public class SpringdocConfig {
    private static final Logger log = LoggerFactory.getLogger(SpringdocConfig.class);
//    @Value("${springdoc.swagger-ui.urls}")
//    Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls;

    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        log.info("--->> init swagger ");
        return openApi -> {

            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("x-order", RandomUtil.randomInt(0, 100));
                    tag.setExtensions(map);
                });
            }

//            openApi.externalDocs(new ExternalDocumentation().description("日志服务").url("http://172.16.80.16:30303/doc.html"));
//            openApi.externalDocs(new ExternalDocumentation().description("洞察引擎服务").url("http://172.16.80.16:30310/doc.html"));

            // servers 提供调用的接口地址前缀
//            String path = swaggerProperties.getServices().get(serviceInstance.getServiceId());
//            openApi.addServersItem((new Server().description("日志服务").url("http://172.16.80.16:30303")));
//            openApi.addServersItem((new Server().description("洞察引擎服务").url("http://172.16.80.16:30310")));

        };
    }


    @Bean
    public OpenAPI customOpenAPI(SwaggerUiConfigProperties uiConfigProperties, Knife4jProperties knife4jProperties, Environment env) {
        if(ArrayUtil.isNotEmpty(env.getActiveProfiles())){
            try{
                if("local".equalsIgnoreCase(env.getActiveProfiles()[0])){
                    uiConfigProperties.setUrls(null);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        log.info("--->> API nginx代理地址: ".concat(knife4jProperties.getSetting().getEnableHostText()));
        return new OpenAPI()
//                .paths(paths)

                .info(new Info()
                        .title("VOC管理平台")
                        .version("2.0")
                                .description("API nginx代理地址：".concat(knife4jProperties.getSetting().getEnableHostText()))
//                        .description("VOC管理平台".concat(docLinks.toString()))
                        .termsOfService("http://doc.xiaominfo.com")
                        .license(new License().name("Apache 2.0")
                                .url("http://doc.xiaominfo.com")));
    }



   /* @Bean
    @Primary
    public List<GroupedOpenApi> apis(SwaggerUiConfigParameters swaggerUiConfigParameters) {
        List<GroupedOpenApi> groups = new ArrayList<>();
//        swaggerUiConfigParameters.addGroup("voc-app-insights");
//        swaggerUiConfigParameters.addUrl("http://172.16.80.16:30310");
        groups.add(GroupedOpenApi.builder().pathsToMatch("http://172.16.80.16:30310").group("voc-app-insights").build());
        groups.add(GroupedOpenApi.builder().pathsToMatch("http://172.16.80.16:30303").group("voc-app-ops").build());
//        GroupedOpenApi.builder().addOpenApiCustomizer(new OpenApiCustomizer());
        return groups;
    }*/


}
