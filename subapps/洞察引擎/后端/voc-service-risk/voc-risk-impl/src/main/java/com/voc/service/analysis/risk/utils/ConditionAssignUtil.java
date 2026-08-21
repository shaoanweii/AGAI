package com.voc.service.analysis.risk.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.voc.service.analysis.risk.mapper.BatchRuleDataMapper;
import com.voc.service.insights.engine.api.constants.ContentTypeEnum;
import com.voc.service.risk.api.model.BatchRuleUserModel;
import com.voc.service.risk.api.model.BatchTaskConditionsModel;
import com.voc.service.risk.api.vo.AccountLexiconVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ConditionAssignUtil {


    @Autowired
    BatchRuleDataMapper batchRuleDataMapper;


    /**
     * 解析JSON配置并给BatchTaskConditionsModel赋值
     *
     * @param jsonConfig 你提供的JSON字符串
     * @return 赋值完成的BatchTaskConditionsModel
     */
    public BatchTaskConditionsModel buildConditionsModel(String jsonConfig) {
        // 1. 将JSON字符串转为Map<String, Map<String, Object>>（你的JSON是嵌套结构）
        Map<String, Map<String, Object>> configMap = JSON.parseObject(
                jsonConfig,
                new TypeReference<>() {
                }
        );

        // 2. 初始化模型
        BatchTaskConditionsModel conditionsModel = new BatchTaskConditionsModel();
        List<AccountLexiconVo> allAccountLexiconList = getAllAccountLexiconList();
        // 3. 遍历所有条件，逐个赋值
        for (Map.Entry<String, Map<String, Object>> entry : configMap.entrySet()) {
            // 条件字段名：intention / data_source / affective_level ...
            String fieldName = entry.getKey();
            // 条件完整配置
            Map<String, Object> fieldConfig = entry.getValue();

            log.info("当前处理维度字段：{}", fieldName);
            log.info("维度完整配置：{}", fieldConfig);

            // 核心：根据字段名，给model设置对应的值
            assignFieldValue(conditionsModel, fieldName, fieldConfig, allAccountLexiconList);
        }
        return conditionsModel;
    }

    /**
     * 根据字段名，给BatchTaskConditionsModel赋值
     */
    private void assignFieldValue(BatchTaskConditionsModel model, String fieldName,
                                  Map<String, Object> config, List<AccountLexiconVo> allAccountLexiconList) {
        Object valuesObj = config.get("values");
        List<String> valuesList = (List<String>) valuesObj;
        Object calculationMethod = config.get("calculation_method");
        switch (fieldName) {
            case "intention":
                model.setIntentionType(valuesList.get(0));
                break;
            case "data_source":
                model.setChannelIds(valuesList);
                if (calculationMethod.equals("alone")) {
                    model.setGroupByChannel(Boolean.TRUE);
                }
                break;
            case "affective_level":
                model.setSentimentList(valuesList);
                break;
            case "publish_user":
                handlePublishUserCondition(valuesList, model, allAccountLexiconList, "publish_user");
                break;
            case "OP":
                handlePublishUserCondition(valuesList, model, allAccountLexiconList, "OP");
                break;
            case "regulation_content_type":
                handleRegulationContentType(config, model, valuesList);
                break;
            case "AD_type":
                model.setAdTypeList(valuesList);
                break;
            case "province":
                model.setProvinceList(valuesList);
                break;
            case "customer_gender":
                model.setCustomerGender(valuesList.get(0).equals("M") ? SM4DecryptUtil.encryptMobile("男") : SM4DecryptUtil.encryptMobile("女"));
                break;
            case "batch_kh_type":
                model.setCustClassifyList(valuesList);
                break;
            case "water_man":
                model.setWaterMan(valuesList.get(0).equals("1") ? "Y" : "N");
                break;
            case "V_man":
                model.setVMan(valuesList.get(0));
                break;
            case "attribute":
                model.setStandpoint(this.getInsPropertyTag(valuesList));
                break;
            case "title":
                handleText(config, model, valuesList, "title", allAccountLexiconList);
                break;
            case "content":
                handleText(config, model, valuesList, "content", allAccountLexiconList);
                break;
            case "carSeries":
                model.setCarSeriesCode(valuesList);
                if (calculationMethod.equals("alone")) {
                    model.setGroupByCarSeries(Boolean.TRUE);
                }
                break;
            case "experience_code":
                handleExperienceCode(config, model, valuesList);
                break;
            default:
                log.warn("未识别的条件字段：{}", fieldName);
        }
    }


    private void handleText(Map<String, Object> config, BatchTaskConditionsModel model,
                            List<String> valuesList, String textType, List<AccountLexiconVo> allAccountLexiconList) {

        String valueType = config.get("value_type").toString();
        String valueLevel = config.get("value_level").toString();
        if ("value".equals(valueType)) {
            if ("content".equals(textType)) {
                model.setContent(valuesList);
            }
            if ("title".equals(textType)) {
                model.setTitle(valuesList);
            }
        }
        Object lexiconType = config.get("lexicon_type");
        if ("lexicon".equals(valueType) && "rule".equals(lexiconType)) {
            if ("1".equals(valueLevel) && "content".equals(textType)) {
                model.setContentResourceIdList(valuesList);
            }
            if ("1".equals(valueLevel) && "title".equals(textType)) {
                model.setTitleResourceIdList(valuesList);
            }
            if ("2".equals(valueLevel) && "content".equals(textType)) {
                model.setContentRuleIdList(valuesList);
            }
            if ("2".equals(valueLevel) && "title".equals(textType)) {
                model.setTitleRuleIdList(valuesList);
            }
        }
        if ("lexicon".equals(valueType) && "account".equals(lexiconType)) {

            if ("1".equals(valueLevel) && "content".equals(textType)) {
                List<String> descDtoList = allAccountLexiconList.stream()
                        .filter(item -> valuesList.contains(item.getResourceId())).
                        map(AccountLexiconVo::getAccountName).toList();
                model.setContent(descDtoList);
            }
            if ("1".equals(valueLevel) && "title".equals(textType)) {
                List<String> descDtoList = allAccountLexiconList.stream()
                        .filter(item -> valuesList.contains(item.getResourceId())).
                        map(AccountLexiconVo::getAccountName).toList();
                model.setTitle(descDtoList);
            }
            if ("2".equals(valueLevel) && "content".equals(textType)) {
                List<String> descDtoList = allAccountLexiconList.stream()
                        .filter(item -> valuesList.contains(item.getId())).
                        map(AccountLexiconVo::getAccountName).toList();
                model.setTitle(descDtoList);

            }
            if ("2".equals(valueLevel) && "title".equals(textType)) {
                List<String> descDtoList = allAccountLexiconList.stream()
                        .filter(item -> valuesList.contains(item.getId())).
                        map(AccountLexiconVo::getAccountName).toList();
                model.setTitle(descDtoList);
            }
        }
    }

    private void handleExperienceCode(Map<String, Object> config, BatchTaskConditionsModel model, List<String> valuesList) {
        String valueLevel = config.get("value_level").toString();
        String calculationMethod = config.get("calculation_method").toString();
        if (valueLevel.equals("5")) {
            model.setTopicCodeList(valuesList);
            if (calculationMethod.equals("alone")) {
                model.setGroupField("topic");
            }
        } else {
            model.setLevel(valueLevel);
            model.setTagCodeList(valuesList);
            if (calculationMethod.equals("alone")) {
                if (valueLevel.equals("1")) {
                    model.setGroupField("dom_tag_first_code");
                }
                if (valueLevel.equals("2")) {
                    model.setGroupField("dom_tag_second_code");
                }
                if (valueLevel.equals("3")) {
                    model.setGroupField("dom_tag_three_code");
                }
                if (valueLevel.equals("4")) {
                    model.setGroupField("dom_tag_four_code");
                }
            }
        }
    }

    private void handleRegulationContentType(Map<String, Object> config, BatchTaskConditionsModel model, List<String> valuesList) {
        String valueLevel = config.get("value_level").toString();
        if (valueLevel.equals("1")) {
            model.setContentType(valuesList.get(0));
        }
        if (valueLevel.equals("2")) {
            model.setContentType(ContentTypeEnum.PERSONAGE.getCode());
            if (valuesList.contains("originalPost")) {
                model.setContentTypeMin("Y");
            } else {
                model.setContentTypeMin("N");
            }
        }
    }

    private void handlePublishUserCondition(List<String> valuesList,
                                            BatchTaskConditionsModel model,
                                            List<AccountLexiconVo> allAccountLexiconList, String type) {

        if (CollectionUtils.isNotEmpty(valuesList)) {
            List<BatchRuleUserModel> userResourceIdList = buildWarningUserList(
                    valuesList,
                    allAccountLexiconList,
                    AccountLexiconVo::getResourceId
            );
            List<BatchRuleUserModel> userIdList = buildWarningUserList(
                    valuesList,
                    allAccountLexiconList,
                    AccountLexiconVo::getId
            );

            if ("publish_user".equals(type)) {
                if (CollectionUtils.isNotEmpty(userResourceIdList)) {
                    model.setMainPostUser(userResourceIdList);
                }
                if (CollectionUtils.isNotEmpty(userIdList)) {
                    model.setMainPostUser(userIdList);
                }
            } else if ("OP".equals(type)) {
                if (CollectionUtils.isNotEmpty(userResourceIdList)) {
                    model.setPostUser(userResourceIdList);
                }
                if (CollectionUtils.isNotEmpty(userIdList)) {
                    model.setPostUser(userIdList);
                }
            }
        }
    }


    private List<BatchRuleUserModel> buildWarningUserList(
            List<String> idList,
            List<AccountLexiconVo> accountLexiconList,
            java.util.function.Function<AccountLexiconVo, String> idExtractor) {

        return accountLexiconList.stream()
                .filter(account -> idList.contains(idExtractor.apply(account)))
                .map(account -> BatchRuleUserModel.builder()
                        .accountId(account.getAccountId())
                        .accountName(account.getAccountName())
                        .channel(account.getFinalChannel())
                        .build())
                .toList();
    }

    public List<String> getInsPropertyTag(List<String> idList) {
        List<String> nameList = batchRuleDataMapper.queryInsPropertyTag(idList);
        if (CollectionUtils.isNotEmpty(nameList)) {
            return nameList;
        }
        return Collections.emptyList();
    }


    public List<AccountLexiconVo> getAllAccountLexiconList() {
        List<AccountLexiconVo> allAccountLexiconList = batchRuleDataMapper.queryInsAccountLexicon();
        if (CollectionUtils.isNotEmpty(allAccountLexiconList)) {
            return allAccountLexiconList;
        }
        return Collections.emptyList();
    }
}