package com.voc.service.data.integration.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.voc.service.data.integration.enums.ErrorDataMsgEnums;
import com.voc.service.data.integration.nodes.context.ChannelDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "validateChannelDatasetNode", name = "校验数据有效性，分出有效数据和无效数据")
public class ValidateChannelDatasetNode extends NodeComponent {
    private static final Logger log = LoggerFactory.getLogger(ValidateChannelDatasetNode.class);
    @Autowired
    DataIntegrationConfig config;

    @Override
    public void process() throws Exception {
        ChannelDatasetContext context = this.getRequestData();
        try {
            /*//数据必填项校验
            final String requiredFields = config.getRequiredFields().get(context.getChannelType());
            //必填字段
            final Set<String> requiredFieldList = new HashSet<>(StrUtil.split(requiredFields, ","));*/

            List<DataIntegrationRecordModel> successfulDataset = new CopyOnWriteArrayList();
            List<DataIntegrationRecordModel> failedDataset = new CopyOnWriteArrayList<>();
            context.getChannelDataset().stream().forEach(model -> {
                final DataIntegrationRecordModel validateResult = this.meetRequirements(context.getChannelType(), model);
                if (StrUtil.isBlank(validateResult.getErrorCode())) {
                    //记录成功数据
                    successfulDataset.add(validateResult);
                } else {
                    log.error("【{}】数据未通过校验：{}", context.getChannelType(), Optional.ofNullable(model));
                    //记录失败数据
                    failedDataset.add(validateResult);
                }
            });

            context.setSuccessfulDataset(successfulDataset);
            context.setFailedDataset(failedDataset);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
//            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        ChannelDatasetContext context = this.getRequestData();
        Assert.isTrue(CollUtil.isNotEmpty(context.getChannelDataset()), "getIds cannot be empty");

        return true;
    }

    /**
     * 判断是否满足必填项校验要求
     *
     * @param model
     * @return
     */
    private DataIntegrationRecordModel meetRequirements(String channelType, DataIntegrationRecordModel model) {
        final Set<String> defaultRequiredFields = config.getChannelValidDefaultRequiredFields();
        if (CollUtil.isEmpty(defaultRequiredFields)) {
            log.debug("【".concat(channelType).concat("】未配置必填项配置为空"));
            return model;
        }
        if (ObjUtil.isNull(model.getData())) {
            log.error("【{}}】数据为空", channelType);
            model.setErrorCode(ErrorDataMsgEnums.OriginalDataIsEmpty.getCode());
            model.setErrorMsg(ErrorDataMsgEnums.OriginalDataIsEmpty.getText());
            return model;
        }



        //默认必填属性校验
        final JSONObject jsonObj = JSONUtil.parseObj(model.getData());
//        String a = jsonObj.getByPath("extAttrs.one_id_type",String.class);

        final Set<String> invalidAttrsSet = defaultRequiredFields.stream()
                .map(field -> StrUtil.split(field, ":").get(0))
//                .map(StrUtil::toCamelCase)  // 驼峰转下划线
                .filter(field -> {
                    if(StrUtil.indexOf(field,'.') > -1){
                        return StrUtil.isBlank(jsonObj.getByPath(field,String.class));
                    }
                    return !jsonObj.containsKey(field);
                })
                .collect(Collectors.toSet());
        log.debug("【{}】默认必填项校验结果：{}", channelType, invalidAttrsSet);
        if (CollUtil.isNotEmpty(invalidAttrsSet)) {
            model.setErrorCode(ErrorDataMsgEnums.RequiredFieldValidationHasFailed.getCode());
            model.setErrorMsg(ErrorDataMsgEnums.RequiredFieldValidationHasFailed.getText().concat(" attrs:").concat(invalidAttrsSet.toString()));
            return model;
        }

        //默认属性数据类型校验
        final Set<String> invalidValuesSet = defaultRequiredFields.stream()
                .filter(field -> StrUtil.split(field, ":").size() == 2)
                .filter(field -> {
                    String type = StrUtil.split(field, ":").get(1);
                    return StrUtil.isNotBlank(type) && !StrUtil.equalsIgnoreCase(type, "string");
                })
                .map(field -> {
                    String attr = StrUtil.split(field, ":").get(0);
                    String type = StrUtil.split(field, ":").get(1);
                    if (StrUtil.equalsIgnoreCase("datetime", type)) {  // 日期格式校验
                        try {
                            DateUtil.parse(jsonObj.getStr(attr), DatePattern.NORM_DATETIME_PATTERN);
                        } catch (Exception e) {
                            return attr;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        log.debug("【{}】默认数据类型校验结果：{}", channelType, invalidValuesSet);
        if (CollUtil.isNotEmpty(invalidValuesSet)) {
            model.setErrorCode(ErrorDataMsgEnums.RequiredFieldTypeValidationHasFailed.getCode());
            model.setErrorMsg(ErrorDataMsgEnums.RequiredFieldTypeValidationHasFailed.getText().concat(" attrs_type:").concat(invalidValuesSet.toString()));
            return model;
        }

        //必填扩展属性校验
        final Set<String> extRequiredFields = config.getChannelValidExtRequiredFields().get(channelType);
        if (CollUtil.isEmpty(extRequiredFields)) {
            return model;
        }
        final JSONObject jsonExtObj = jsonObj.getJSONObject("extAttrs");
        final JSONObject jsonExt2Obj = jsonObj.getJSONObject("extAttrs2");
        final JSONObject jsonExt3Obj = jsonObj.getJSONObject("extAttrs3");
        final Set<String> invalidExtAttrsSet = extRequiredFields.stream()
                .map(field -> StrUtil.split(field, ":").get(0))
                .map(StrUtil::toCamelCase)
                .filter(field -> ObjUtil.isNull(jsonExtObj.get(field)) &&  ObjUtil.isNull(jsonExt2Obj.get(field)) &&  ObjUtil.isNull(jsonExt3Obj.get(field)))
                .collect(Collectors.toSet());
        log.debug("【{}】扩展属性必填项校验结果：{}", channelType, invalidExtAttrsSet);
        if (CollUtil.isNotEmpty(invalidExtAttrsSet)) {
            model.setErrorCode(ErrorDataMsgEnums.RequiredExtFieldValidationHasFailed.getCode());
            model.setErrorMsg(ErrorDataMsgEnums.RequiredExtFieldValidationHasFailed.getText().concat(" ext_attrs:").concat(invalidExtAttrsSet.toString()));
            return model;
        }

        //默认属性数据类型校验
        final Set<String> invalidExtValuesSet = extRequiredFields.stream()
                .filter(field -> StrUtil.split(field, ":").size() == 2)
                .filter(field -> {
                    String type = StrUtil.split(field, ":").get(1);
                    return StrUtil.isNotBlank(type) && !StrUtil.equalsIgnoreCase(type, "string");
                })
                .map(field -> {
                    String attr = StrUtil.split(field, ":").get(0);
                    String type = StrUtil.split(field, ":").get(1);
                    if (StrUtil.equalsIgnoreCase("datetime", type)) {  // 日期格式校验
                        try {
                            DateUtil.parse(jsonObj.getStr(attr), DatePattern.NORM_DATETIME_PATTERN);
                        } catch (Exception e) {
                            return attr;
                        }
                    }else if (StrUtil.equalsIgnoreCase("sex", type)){
                        if(!Arrays.asList("0","1").contains(jsonObj.getStr(attr))){
                            return attr;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        log.debug("【{}】扩展属性必填项数据类型校验结果：{}", channelType, invalidExtValuesSet);
        if (CollUtil.isNotEmpty(invalidExtValuesSet)) {
            model.setErrorCode(ErrorDataMsgEnums.RequiredExtFieldTypeValidationHasFailed.getCode());
            model.setErrorMsg(ErrorDataMsgEnums.RequiredExtFieldTypeValidationHasFailed.getText().concat(" attrs_type:").concat(invalidExtValuesSet.toString()));
            return model;
        }

        return model;
    }
}
