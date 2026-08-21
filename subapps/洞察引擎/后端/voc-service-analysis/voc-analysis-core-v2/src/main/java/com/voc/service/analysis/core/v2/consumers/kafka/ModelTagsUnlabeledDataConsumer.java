package com.voc.service.analysis.core.v2.consumers.kafka;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.voc.service.analysis.core.v2.entity.ModelSentimentResultEntity;
import com.voc.service.analysis.core.v2.entity.SentimentRuleKeywordsEntity;
import com.voc.service.analysis.core.v2.mapper.ModelSentimentResultMapper;
import com.voc.service.analysis.core.v2.mapper.SentimentRuleKeywordsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @ClassName ModelTagsUnlabeledDataConsumer
 * @description 消费模型标签未标记数据的Kafka消息 (Optimized Strategy)
 */
@Component("modelTagsUnlabeledDataConsumer")
@Slf4j
@DS("voc")
public class ModelTagsUnlabeledDataConsumer {

    public static final String TOPIC_NAME = "voc_anal_flow_model_tags_unlabeled_data";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ModelSentimentResultMapper modelSentimentResultMapper;

    @Autowired
    private SentimentRuleKeywordsMapper sentimentRuleKeywordsMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${model_sentiment_url}")
    private String analysisUrl;

    @Value("${model_sentiment_key}")
    private String apiKey;

//    @KafkaListener(topics = {TOPIC_NAME},groupId = "${kafkaEvent.groupId}")
    public void onMessage(String message) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("消息处理: " + TOPIC_NAME);

        try {
            // 1. 解析消息
            final Map<String, Object> messageMap = JSONUtil.toBean(message, Map.class);
            if (ObjUtil.isNull(messageMap)) {
                log.error("消息解析失败: {}", message);
                return;
            }

            // 2. 幂等性检查 (DataId)
            String dataId = safeGetString(messageMap, "dataId");
            if (StrUtil.isBlank(dataId)) {
                log.warn("消息中dataId为空，跳过处理");
                return;
            }
            Long count = modelSentimentResultMapper.selectCount(
                    new QueryWrapper<ModelSentimentResultEntity>().eq("data_id", dataId));
            if (count > 0) {
                return;
            }

            // 3. 预处理判断
            String inputSentiment = safeGetString(messageMap, "sentiment");
            if ("正面".equals(inputSentiment)) {
                return;
            }

            // 4. 提取原始内容
            Object rawDataObj = messageMap.get("rawData");
            if (!(rawDataObj instanceof Map)) {
                log.warn("消息中rawData格式错误: {}", message);
                return;
            }
            Map<String, Object> rawData = (Map<String, Object>) rawDataObj;
            String content = (String) rawData.get("content");
            if (StrUtil.isBlank(content)) {
                log.warn("消息中content为空: {}", message);
                return;
            }
            
            // 提取标题
            String title = (String) rawData.get("title");

            // 5. 确定最终的情感和意图
            String finalSentiment = inputSentiment;
            String finalIntention = null;

            if (!"负面".equals(inputSentiment)) {
                JSONObject analysisResult = callAnalysisApi(content);
                if (analysisResult != null) {
                    finalSentiment = getFirstTextFromJson(analysisResult, "情感倾向");
                    finalIntention = getFirstTextFromJson(analysisResult, "意图");
                } else {
                    return;
                }
            }

            // 6. 构建并保存实体
            ModelSentimentResultEntity entity = new ModelSentimentResultEntity();
            fillCommonFields(entity, messageMap);

            entity.setOriginalTextScene(content);
            entity.setSentiment(finalSentiment);
            entity.setIntentionType(finalIntention);
            entity.setTitle(title); // 设置标题字段

            // 7. 如果最终判定为负面，计算二级情感分数
            if ("负面".equals(finalSentiment)) {
                // 修改后的方法调用：规则列表缓存策略
                String sentimentScore = determineSubSentiment(finalSentiment, content, "关键词");
                if (StrUtil.isNotBlank(sentimentScore)) {
                    entity.setSentimentScore(sentimentScore);
                }
            }

            // 落库
            modelSentimentResultMapper.insert(entity);
            log.info("数据处理完成并落库，dataId: {}, sentiment: {}", dataId, finalSentiment);

        } catch (Exception e) {
            log.error("处理消息时发生异常: {}", e.getMessage(), e);
        } finally {
            stopWatch.stop();
            if (stopWatch.getTotalTimeMillis() > 1000) {
                log.warn("消息处理耗时较长: {}", stopWatch.prettyPrint());
            }
        }
    }

    /**
     * 确定二级情感（修改版：缓存规则列表，而非缓存匹配结果）
     * 策略：
     * 1. 尝试从Redis获取该情感下的规则列表(List)
     * 2. 如果Redis没有，查DB，放入Redis
     * 3. 在内存中遍历List进行匹配
     */
    private String determineSubSentiment(String sentiment, String originalText, String ruleType) {
        if ("中性".equals(sentiment) || StrUtil.isBlank(originalText)) {
            return null;
        }

        String actualRuleType = StrUtil.isBlank(ruleType) ? "关键词" : ruleType;

        // 1. 定义Key：voc:rules:{情感}:{规则类型}
        // 例如：voc:rules:负面:关键词
        String cacheKey = "voc:sentiment:rules:" + sentiment + ":" + actualRuleType;

        List<SentimentRuleKeywordsEntity> rules = null;

        try {
            // 2. 尝试从缓存获取规则列表
            String jsonRules = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isNotBlank(jsonRules)) {
                // 反序列化 JSON 数组为 List 对象
                rules = JSONUtil.toList(jsonRules, SentimentRuleKeywordsEntity.class);
            }
        } catch (Exception e) {
            log.warn("从Redis获取规则配置失败，将降级查询数据库: {}", e.getMessage());
        }

        // 3. 缓存未命中，查询数据库
        if (rules == null) {
            rules = sentimentRuleKeywordsMapper.selectList(
                    new QueryWrapper<SentimentRuleKeywordsEntity>()
                            .eq("primary_sentiment", sentiment)
                            .eq("rule_type", actualRuleType)
            );

            // 防止空指针，初始化为空列表
            if (rules == null) {
                rules = new ArrayList<>();
            }

            // 4. 将查到的列表（即使为空）序列化后存入 Redis
            // 设置过期时间，例如 1 小时。因为规则配置不会频繁变更，长一点没关系。
            try {
                stringRedisTemplate.opsForValue().set(
                        cacheKey,
                        JSONUtil.toJsonStr(rules),
                        Duration.ofHours(1)
                );
            } catch (Exception e) {
                log.warn("将规则配置写入Redis失败: {}", e.getMessage());
            }
        }

        // 5. 内存匹配逻辑 (完全不需要再查库)
        if (rules.isEmpty()) {
            return null;
        }

        boolean hasMedium = false;
        boolean hasNormal = false;

        for (SentimentRuleKeywordsEntity rule : rules) {
            String keyword = rule.getRuleKeywords();
            // 判空保护
            if (StrUtil.isNotBlank(keyword) && originalText.contains(keyword)) {
                String level = rule.getSecondarySentiment();
                if ("高".equals(level)) {
                    return "高"; // 遇到高直接返回
                }
                if ("中".equals(level)) {
                    hasMedium = true;
                } else {
                    hasNormal = true;
                }
            }
        }

        if (hasMedium) return "中";
        if (hasNormal) return "一般";

        return null;
    }

    // --- 辅助方法 ---

    private void fillCommonFields(ModelSentimentResultEntity entity, Map<String, Object> map) {
        entity.setId(safeGetString(map, "id"));
        entity.setPublishTime(parsePublishTime(safeGetString(map, "publishTime")));
        entity.setChannelId(safeGetString(map, "channelId"));
        entity.setDataId(safeGetString(map, "dataId"));
        entity.setClientId(safeGetString(map, "clientId"));
        entity.setOneId(safeGetString(map, "oneId"));
        entity.setContentType(safeGetString(map, "contentType"));

        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setInsertDt(now);

        entity.setWorkId(safeGetString(map, "workId"));
        entity.setOriginalId(safeGetString(map, "originalId"));
        entity.setInputDataId(safeGetString(map, "inputDataId"));
        entity.setSampleDataType(safeGetString(map, "sampleDataType"));
        entity.setBrandCode(safeGetString(map, "brandCode"));
        entity.setCarSeriesCode(safeGetString(map, "carSeriesCode"));
        entity.setLabelType(safeGetString(map, "labelType"));
        entity.setScenario(safeGetString(map, "scenario"));
        entity.setTopic(safeGetString(map, "topic"));
        entity.setOpinion(safeGetString(map, "opinion"));
        entity.setSubject(safeGetString(map, "subject"));
        entity.setFaultLevel(safeGetString(map, "faultLevel"));
        entity.setDescription(safeGetString(map, "description"));
        entity.setKeywords(safeGetString(map, "keywords"));
        entity.setSentimentScore(safeGetString(map, "sentimentScore"));
        entity.setModelType(safeGetInteger(map, "modelType"));

        entity.setExtFields(toJsonStrOrNull(map.get("extFields")));
        entity.setBizExtAttrs(toJsonStrOrNull(map.get("bizExtAttrs")));
        entity.setBizExtAttrs2(toJsonStrOrNull(map.get("bizExtAttrs2")));
        entity.setBizExtAttrs3(toJsonStrOrNull(map.get("bizExtAttrs3")));
        entity.setCustExtAttrs(toJsonStrOrNull(map.get("custExtAttrs")));
        entity.setVhlExtAttrs(toJsonStrOrNull(map.get("vhlExtAttrs")));
        entity.setDealerExtAttrs(toJsonStrOrNull(map.get("dealerExtAttrs")));
        entity.setPrdExtAttrs(toJsonStrOrNull(map.get("prdExtAttrs")));
        entity.setTitle(safeGetString(map, "title"));
    }

    private JSONObject callAnalysisApi(String content) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("texts", Collections.singletonList(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(analysisUrl, requestEntity, String.class);

            JSONObject jsonResponse = JSONUtil.parseObj(response);
            Integer code = jsonResponse.getInt("code");

            if (code != null && code == 200) {
                return jsonResponse;
            } else {
                log.error("分析接口返回非200: {}, msg: {}", code, jsonResponse.getStr("message"));
                return null;
            }
        } catch (Exception e) {
            log.error("调用分析接口异常", e);
            return null;
        }
    }

    private String getFirstTextFromJson(JSONObject analyzeResponse, String type) {
        JSONArray dataArray = analyzeResponse.getJSONArray("data");
        if (dataArray == null || dataArray.isEmpty()) return null;
        JSONObject dataItem = dataArray.getJSONObject(0);
        if (dataItem == null) return null;
        JSONArray targetArray = dataItem.getJSONArray(type);
        if (targetArray != null && !targetArray.isEmpty()) {
            JSONObject textItem = targetArray.getJSONObject(0);
            return textItem != null ? textItem.getStr("text") : null;
        }
        return null;
    }

    private LocalDateTime parsePublishTime(String publishTimeStr) {
        if (StrUtil.isNotBlank(publishTimeStr)) {
            try {
                return LocalDateTime.parse(publishTimeStr, DATE_FORMATTER);
            } catch (Exception e) {
                log.warn("解析发布时间失败: {}", publishTimeStr);
            }
        }
        return LocalDateTime.now();
    }

    private String safeGetString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        String strValue = value.toString();
        return ("null".equals(strValue) || strValue.isEmpty()) ? null : strValue;
    }

    private Integer safeGetInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toJsonStrOrNull(Object obj) {
        return obj != null ? JSONUtil.toJsonStr(obj) : null;
    }
}
