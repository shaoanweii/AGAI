package com.voc.service.data.integration.nodes.public_domain.opinion;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.config.PublicDomainConfig;
import com.voc.service.data.integration.enums.ErrorDataMsgEnums;
import com.voc.service.data.integration.nodes.public_domain.context.PublicDomainDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.core.NodeComponent;
import com.yomahub.liteflow.flow.LiteflowResponse;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "opinionChannelMatchedEventNode", name = "处理渠道数据-投诉类")
public class OpinionChannelMatchedEventNode extends NodeComponent {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private static final Logger log = LoggerFactory.getLogger(OpinionChannelMatchedEventNode.class);

    @Resource
    private FlowExecutor flowExecutor;
    @Autowired
    PublicDomainConfig config;

    @Override
    public void process() throws Exception {
        PublicDomainDatasetContext context = this.getRequestData();
        try {
            if (CollUtil.isEmpty(context.getSuccessfulDataset())) {
                throw new Exception("【".concat(context.getChannelType()).concat("】数据验证后无成功数据："));
            } else {
                //保存记录成功数据集
                List<DataIntegrationRecordModel> list = context.getSuccessfulDataset();

                list = this.cleanAndNormalizeRawData(context, list);
//                this.pushSuccessfulData(model.getId(), context.getClientId(), context.getChannelType(), list);
                context.setSuccessfulDataset(list);
            }
        } catch (Exception e) {
            log.info("【{}】推送数据错误信息：", context.getWorkId(), e);
            //出现服务异常时，将所有数据归为异常处理数据集
            final List<DataIntegrationRecordModel> errorList = context.getSuccessfulDataset().stream().map(data -> {
                data.setErrorCode(ErrorDataMsgEnums.PushServiceHasFailed.getCode());
                data.setErrorMsg(ErrorDataMsgEnums.PushServiceHasFailed.getText());
                return data;
            }).toList();
            context.getFailedDataset().addAll(errorList);
            context.setSuccessfulDataset(null);
        }
    }

    /**
     * 数据分类处理流程
     */
    public List<DataIntegrationRecordModel> cleanAndNormalizeRawData(PublicDomainDatasetContext context, List<DataIntegrationRecordModel> list) {
        // 创建数据集用户存储扩展出集合 ，例如依据需要将options字段值拆分出多条数据
       /* AtomicReference<List<DataIntegrationRecordModel>> extendedDataList
                = new AtomicReference<>(CollUtil.newCopyOnWriteArrayList(null));*/

        List<DataIntegrationRecordModel> extendedDataList = CollUtil.newCopyOnWriteArrayList(null);

        // 1. 初始化引擎（使用默认选项）
        list.stream()
                .filter(Objects::nonNull)
                .filter(model -> Objects.nonNull(model.getData()))
                .forEach(model -> {
                    try {
                        ChannelMetaDataModel data = JSONUtil.toBean(String.valueOf(model.getData()), ChannelMetaDataModel.class);
                        if (!"opinion".equalsIgnoreCase(data.getContentType())) {
//                            log.error("【{}】数据分类处理错误：contentType is null -> {}", model.getId(), model.getData());
                            return;
                        }
                        log.debug("【{}】{}数据分类处理：{}", context.getWorkId(), model.getId(), model.getData());
                        if (Objects.equals(data.getIsDeleted(), 1)) {
                            log.debug("【{}】{}数据已删除：{}", context.getWorkId(), model.getId(), model.getData());
                            return;
                        }

                        final JSONObject attrs = JSONUtil.isTypeJSONObject(String.valueOf(data.getAttrs()))
                                ? JSONUtil.parseObj(data.getAttrs()) : new JSONObject();
                        final JSONObject attrs2 = JSONUtil.isTypeJSONObject(String.valueOf(data.getAttrs2()))
                                ? JSONUtil.parseObj(data.getAttrs2()) : new JSONObject();
                        log.debug("【{}】{}数据分类处理：{}", context.getWorkId(), model.getId(), attrs2);
//                        final String site_domain = attrs2.getStr("site_domain");
//                        final List<String> sub_domain = JSONUtil.toList(JSONUtil.parseArray(attrs2.get("sub_domain")), String.class);

                        // rule2 -转换渠道编码
                        AtomicReference<String> channelCode = new AtomicReference<>(data.getChannelCode());
                        config.getChannelMappingOpinionRule2().stream()
                                .filter(c -> StrUtil.isNotBlank(c.getSourceChannelCode()))
                                .filter(c -> c.getSourceChannelCode().equals(channelCode.get()))
                                .forEach(c -> {
                                    log.debug("【{}】数据分类处理： 渠道配置值-rule2 {}", context.getWorkId(), c);

                                    if (CollUtil.isNotEmpty(c.getRequiredFields())) {
                                        final boolean hasMatch = c.getRequiredFields().stream().allMatch(attrs::containsKey);
                                        if (hasMatch) {
                                            channelCode.set(c.getTargetChannelCode());
                                            log.info("【{}】 {} 数据分类处理： 渠道配置值1-replace {}", context.getWorkId(), model.getId(), channelCode.get());
                                        } else {
                                            log.warn("【{}】数据分类处理： 渠道配置值-rule2 {}", context.getWorkId(), c.getRequiredFields());
                                            return;
                                        }
                                    }
                                });


                        // 设置渠道值（匹配值优先，否则使用 site_domain）
                        data.setChannelCode(channelCode.get());
                        log.debug("【{}】{}数据分类处理结果：{}", context.getWorkId(), model.getId(), data);

                        model.setData(JSONUtil.toJsonStr(data));

                        // 扩展数据 - 评分
                        this.extendedDataListGenerated(context.getWorkId(),channelCode.get(), extendedDataList, model);

                    } catch (Exception e) {
                        log.error("【{}】数据分类处理错误：{}", context.getWorkId(), model);
                        log.error(e.getMessage(), e);
                        model.setErrorCode(ErrorDataMsgEnums.FailedToParseOriginalData.getCode());
                    }
                });

        if (CollUtil.isNotEmpty(extendedDataList)) {
            log.info("【{}】本次循环中扩展出的数据大小：{}", context.getWorkId(), extendedDataList.size());
            PublicDomainDatasetContext context_ext = PublicDomainDatasetContext.builder().build();
            try {
                context_ext.setClientId(context.getClientId());
                context_ext.setChannelType(context.getChannelType());
                context_ext.setWorkId(context.getWorkId().concat("_ext"));
                context_ext.setSuccessfulDataset(extendedDataList);
                LiteflowResponse response = flowExecutor.execute2RespWithRid("mpp_input_public_domain_data_opinion_flow", context_ext, context_ext.getWorkId());
                if (!response.isSuccess()) {
                    log.error("workId:{}  {}", context_ext.getWorkId(), response.getCause());
                    throw response.getCause();
                }
            } catch (Exception e) {
                log.error("【{}】数据分类处理错误：{}", context_ext.getWorkId(), e.getMessage());
                log.error(e.getMessage(), e);
            }finally {
                extendedDataList.clear();
            }
        }else{
            log.warn("【{}】本次循环中无扩展数据", context.getWorkId());
        }
        log.info("【{}】数据分类处理结果：{}", context.getWorkId(), list.size());
        return list;
    }

    /**
     * 标题&取值来源分别为
     * 外观，=“score_exterior”
     * 内饰，=“score_interior”
     * 空间，=“score_space”
     * 续航，=“score_battery_life”
     * 配置，=“score_configuration”
     * 驾驶感受，=“score_driving”
     * 智能座舱，=“score_smart_cockpit”
     * 辅助驾驶，=“score_assist_driving”
     *
     * @param workId
     * @param dataIntegrationRecordModels
     * @param model
     */
    private void extendedDataListGenerated(String workId, String channelCode, List<DataIntegrationRecordModel> dataIntegrationRecordModels, DataIntegrationRecordModel model) {
        log.debug("【{}】{}数据分类处理：{}", workId, model.getId(), model.getData());
        ChannelMetaDataModel data = JSONUtil.toBean(String.valueOf(model.getData()), ChannelMetaDataModel.class);
        final JSONObject attrs = JSONUtil.isTypeJSONObject(String.valueOf(data.getAttrs()))
                ? JSONUtil.parseObj(data.getAttrs()) : new JSONObject();

        Object opinionsValObj = attrs.getByPath("opinions");
        if (ObjectUtil.isEmpty(opinionsValObj)) {
            log.error("【{}】数据格式错误，未获取到 opinions 值：{}", workId, opinionsValObj);
            return;
        }

        // 口碑评分#油耗#4
        final String contentFormat = "口碑评分#{}#{}";
        final String contentType = "quest";

        /**
         * {
         *   "type": "评分",
         *   "quest_name": "交付服务",
         *   "quest_id": "QY72381768190513956-1_34626420",
         *   "quest_url": "https://caccmcs.changan.com.cn/qn/974ccfdsJspd69",
         *   "quest_answer_score": "10",
         *   "url": "https://caccmcs.changan.com.cn/qn/974ccfdsJspd69"
         * }
         */
        AtomicInteger count = new AtomicInteger(0);
        JSONObject opinions = JSONUtil.parseObj(opinionsValObj);
        /**
         * 外观，=“score_exterior”
         * 内饰，=“score_interior”
         * 空间，=“score_space”
         * 续航，=“score_battery_life”
         * 配置，=“score_configuration”
         * 驾驶感受，=“score_driving”
         * 智能座舱，=“score_smart_cockpit”
         * 辅助驾驶，=“score_assist_driving”
         */
        config.getChannelMappingOpinionScoreList().stream()
                .filter(c -> c.getChannelCode().equals(channelCode))
//                .map(PublicDomainConfig.ScoreMap::getField)
                .forEach(c -> {
                    final String key = c.getField();
                    final String val = c.getName();
                    log.debug("【{}】{}数据分类处理：{}", workId, model.getId(), key);

                    DataIntegrationRecordModel newModel = DataIntegrationRecordModel.builder().build();
                    BeanUtil.copyProperties(model, newModel);

                    ChannelMetaDataModel newData = ChannelMetaDataModel.builder().build();
                    BeanUtil.copyProperties(data, newData);

                    final int index_ = count.incrementAndGet();
                    newData.setDataId(newData.getDataId().concat("_").concat(String.valueOf(index_)));
                    newData.setId(newData.getId().concat("_").concat(String.valueOf(index_)));

                    // 设置评分 -- 默认
                    final String score = String.valueOf(ObjUtil.defaultIfNull(opinions.getByPath(key), "0"));
                    final String content = StrUtil.format(contentFormat, val, score);
                    log.info("【{}】{}数据扩展-》{}：{}", workId, newModel.getId(), key, score);
                    // 移除 status
                    final JSONObject attrs2 = JSONUtil.isTypeJSONObject(String.valueOf(data.getAttrs2()))
                            ? JSONUtil.parseObj(data.getAttrs2()) : new JSONObject();

                    // 设置评分
                    attrs2.append("type", "评分")
                            .append("quest_name", "交付服务")
                            .append("quest_id", data.getDataId())
                            .append("quest_url", attrs.getStr("url"))
                            .append("quest_answer_score", score);

                    // 移除 status
                    attrs.remove("status");

                    newData.setTitle(val);
                    newData.setContent(content);
                    newData.setContentType(contentType);
                    newData.setAttrs(JSONUtil.parseObj(attrs));
                    newData.setAttrs2(JSONUtil.parseObj(attrs2));

                    newModel.setData(JSONUtil.toJsonStr(newData));
                    dataIntegrationRecordModels.add(newModel);
                });

        log.info("【{}】本次循环中扩展出的数据大小：{}", workId, dataIntegrationRecordModels.size());


    }

    @Override
    public boolean isAccess() {
        PublicDomainDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");

        return true;
    }
}
