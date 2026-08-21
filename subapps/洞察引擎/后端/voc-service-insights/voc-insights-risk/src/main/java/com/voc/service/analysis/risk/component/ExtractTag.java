package com.voc.service.analysis.risk.component;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.enums.StartorParamEnum;
import com.voc.service.analysis.model.RiskStatisticModel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author ding
 * @version 1.0.0
 * @ClassName ExtractTag.java
 * @Description TODO
 * @createTime 2022年11月14日 10:40
 * @Copyright tinfy
 */
@Component
public class ExtractTag {


    private static final Logger log = LoggerFactory.getLogger(ExtractTag.class);
    @Getter
    @Value("${feign.default.token:eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYW5hbHlzaXNfYXBpIiwiaWRlbnRpdHlfdHlwZSI6ImJhc2UiLCJhcHBfaWQiOiJhbmFseXNpcyIsInVzZXJuYW1lIjoiY1Fkb094bmg2eVEwMW5lc2ZLTlhVNjFKQmx5RFg3dHc4YXhod0JjNVl4aXl1MC9CYjdDQWZwQjJ5QTFxYjQ4QiIsInN1YiI6ImFuYWx5c2lzX2FwaSIsImlhdCI6MTcxMDQxMTQyMSwiZXhwIjo0MDc1NjExNDIxfQ.G1kAeqwp0udBimnDdIAqL1nSIcgV0u6YrU0bb5OchJ0}")
    public String defaultToken;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);
    public static final String LAST_WARNING_TIME = "analysis-risk".concat("::lastWarningTime::");


    public String getLastWarningTime(String clientId, String id) {
        String time = stringRedisTemplate.opsForValue().get(LAST_WARNING_TIME.concat(clientId).concat("@").concat(id));
        if (StrUtil.isBlank(time)) {
            time = new DateTime().toString();
            String concat = clientId.concat("@").concat(id);
            stringRedisTemplate.opsForValue().set(LAST_WARNING_TIME.concat(concat), time);
        }
        return time;
    }

    public void setLastWarningTime(String clientId, String id) {
        String concat = clientId.concat("@").concat(id);
        stringRedisTemplate.opsForValue().set(LAST_WARNING_TIME.concat(concat), new DateTime().toString());
    }

    public void completeTime(RiskStatisticModel paramModel) {

        log.info("有需要特定跑的时间:{}", paramModel);
        if (StrUtil.isNotEmpty(paramModel.getBeginTime()) && StrUtil.isNotEmpty(paramModel.getEndTime())) {
            return;
        }
        String endDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String startDate = LocalDateTime.now().plusDays(-15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (StrUtil.equalsIgnoreCase("w", paramModel.getStatisticType())) {
            startDate = LocalDateTime.now().plusWeeks(-1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (StrUtil.equalsIgnoreCase("m", paramModel.getStatisticType())) {
            startDate = LocalDateTime.now().plusMonths(-1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (StrUtil.equalsIgnoreCase("q", paramModel.getStatisticType())) {
            startDate = LocalDateTime.now().plusMonths(-3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (StrUtil.equalsIgnoreCase("y", paramModel.getStatisticType())) {
            startDate = LocalDateTime.now().plusYears(-1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        paramModel.setBeginTime(startDate);
        paramModel.setEndTime(endDate);
    }

    public void initParam(RiskStatisticModel statisticDto, String key, String value) {
        switch (StartorParamEnum.targetEnum(key)) {
            case CLIENT_ID:
                statisticDto.setClientId(value);
                break;
            case BEGIN_TIME:
                statisticDto.setBeginTime(value);
                break;
            case END_TIME:
                statisticDto.setEndTime(value);
                break;
            case TAG_TYPE:
                statisticDto.setTagType(value);
                break;
            case GROUP_TYPE:
                statisticDto.setGroupType(value);
                break;
            case STATISTIC_TYPE:
                statisticDto.setStatisticType(value);
                break;
            case BUS_TYPE:
                statisticDto.setBusType(value);
                break;
            case METHOD_NAME:
                statisticDto.setMethodName(value);
                break;
            case METHOD_TYPE:
                statisticDto.setMethodType(value);
                break;
            case DATE_TYPE:
                statisticDto.setDateType(value);
                break;
            case IS_RISK:
                statisticDto.setIsRisk(value);
                break;
            case BRAND:
                statisticDto.setBrand(value);
                break;
            default:
                log.info("invalid param");
                break;
        }
    }

}
