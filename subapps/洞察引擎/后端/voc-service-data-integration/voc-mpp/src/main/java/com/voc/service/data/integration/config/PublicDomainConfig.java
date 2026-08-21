package com.voc.service.data.integration.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/3 下午2:48
 * @描述:
 **/
@Data
@Builder
@Component
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@ConfigurationProperties(prefix = "public-domain")
public class PublicDomainConfig {
    @Builder.Default
    String userIdDefaultRule = "user.uid";
    @Builder.Default
    Set<UserIdRuleConfig> userIdRule = new HashSet<>();

    @Builder.Default
    Set<ChannelExcludeItem> channelMappingExcludeList = new HashSet<>();

    @Builder.Default
    Set<ChannelMappingConfig> channelMappingSubDomain = new HashSet<>();

    @Builder.Default
    Set<ReplaceChannelMappingConfig> channelMappingPathRule = new HashSet<>();

    @Builder.Default
    Set<CityItem> brandMappingPostCmtCityList = new HashSet<>();

    @Builder.Default
    Set<WeiboBrandMap> brandMappingPostCmtWeiboBrandMapping = new HashSet<>();
    @Builder.Default
    Set<ReplaceChannelMappingRule3Config> channelMappingOpinionRule2 = new HashSet<>();
    @Builder.Default
    Set<ReplaceChannelMappingRule3Config> channelMappingOpinionRule3 = new HashSet<>();
    @Builder.Default
    Set<ScoreMap> channelMappingOpinionScoreList = new HashSet<>();
    @Builder.Default
    Set<SeriesConfig> opinionSeriesMappingList = new HashSet<>();
    @Builder.Default
    Set<SeriesConfig> opinionModelMappingList = new HashSet<>();
    //手机号正则表达式模式
    @Builder.Default
    String phonePattern = "(?<!\\d)(?:86)?(1[3-9]\\d)(\\d{4})(\\d{4})(?!\\d)";

    @Builder.Default
    String idCardPattern = "(?<!\\d)([1-9]\\d{5})(\\d{8})(\\d{4})(?!\\d)";
    // 车牌号正则表达式
    @Builder.Default
    String licensePlatePattern = "([^\\u4e00-\\u9fa5])?([冀豫云辽黑湘皖鲁苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼渝京津沪新京军空海北沈兰济南广成使领])([A-Z])([A-HJ-NPR-Z0-9]{2,4})([A-HJ-NPR-Z0-9挂学警港澳]{2})(?![\\u4e00-\\u9fa5])";

    @Builder.Default
    String vinPattern = "[A-HJ-NPR-Z0-9]{12,16}([A-HJ-NPR-Z0-9]{5})";

    public Pattern getPhonePattern() {
        return Pattern.compile(phonePattern);
    }

    public Pattern getIdCardPattern() {
        return Pattern.compile(idCardPattern);
    }

    public Pattern getLicensePlatePattern() {
        return Pattern.compile(licensePlatePattern);
    }

    public Pattern getVinPattern() {
        return Pattern.compile(vinPattern);
    }


    @PostConstruct
    public void init() throws Exception {
        log.info("--->> init");
//        log.info("channelMappingPostCmtExcludeList:{}", channelMappingPostCmtExcludeList);
    }

    private Map<String, String> pathValueMap = new HashMap<>();
    private Map<String, String> encryptionTypeMap = new HashMap<>();

    public String geUserIdPathValue(final String siteDomain) {
        if(CollUtil.isEmpty(pathValueMap)) {
            pathValueMap = userIdRule.stream().collect(Collectors.toMap(UserIdRuleConfig::getSiteDomain, UserIdRuleConfig::getPathValue));
        }

        if(pathValueMap.containsKey(siteDomain)){
            return pathValueMap.get(siteDomain);
        }

        return userIdDefaultRule;
    }

    public String getEncryptionTypeMap(final String pathValue) {
        if(CollUtil.isEmpty(encryptionTypeMap)) {
            encryptionTypeMap = userIdRule.stream().filter( e-> StrUtil.isNotBlank(e.getEncryptionType()))
                    .collect(Collectors.toMap(UserIdRuleConfig::getPathValue, UserIdRuleConfig::getEncryptionType));
        }

        if(encryptionTypeMap.containsKey(pathValue)){
            return encryptionTypeMap.get(pathValue);
        }

        return null;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelMappingConfig {
        private String channelCode;
        private String siteDomain;
        private String type ;
        private List<String> subDomain;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserIdRuleConfig {

        private String pathValue;
        private String siteDomain;
        private String encryptionType;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplaceChannelMappingConfig {

        private String targetChannelCode;
        private String channelCode;
        private String path;
        private List<String> val;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplaceChannelMappingRule3Config {

        private String targetChannelCode;
        private String sourceChannelCode;
        private List<String> requiredFields;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityItem {
        private String name;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExcludeItem {
        private String channelCode;
        private String platform;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelExcludeItem {
        private String siteDomain;
        private String path;
        private List<String> val;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeiboBrandMap {
        private String userId;
        private String brand;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpinionBrandMap {
        private String channelCode;
        private String regexp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreMap {
        private String channelCode;
        private String field;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeriesConfig {
        private String channelCode;
        private String path;
        private String val;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelConfig {
        private String channelCode;
        private String path;
        private String val;
    }
}

