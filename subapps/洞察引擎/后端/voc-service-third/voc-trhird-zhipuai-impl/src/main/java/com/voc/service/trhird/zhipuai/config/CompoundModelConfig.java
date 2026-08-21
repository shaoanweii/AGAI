package com.voc.service.trhird.zhipuai.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "third")
public class CompoundModelConfig {

    Map<String, Object> compoundModels;

    List<Map<String, String>> compoundPrompt;
}

