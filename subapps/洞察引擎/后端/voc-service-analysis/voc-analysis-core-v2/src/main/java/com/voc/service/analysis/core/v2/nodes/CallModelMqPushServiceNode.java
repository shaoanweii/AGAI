package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.CqCaToModelProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.enums.ContentTypeEnum;
import com.voc.service.analysis.largeModel.ModelTopicRequest;
import com.voc.service.analysis.largeModel.SourceData;
import com.voc.service.analysis.largeModel.TopicText;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author liuhb
 * @version 1.0.0
 * @ClassName callModelMqPushServiceNode
 * @createTime 2024年07月23日 10:49
 * @Copyright liuhb
 * @Description 调用模型后，保存原始数据，成功后标记前置处理数据为已完成状态
 */
@LiteflowComponent(id = "callModelMqPushServiceNode", name = "调用模型计算服务节点")
public class CallModelMqPushServiceNode extends AbstractNode {


    private static final Logger log = LoggerFactory.getLogger(CallModelMqPushServiceNode.class);
    @Autowired
    CqCaToModelProducer cqCaToModelProducer;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Value("${analysis.channelCode:pd_post_jrtt}")
    private String channelCode;


    @Override
    public void process() throws Exception {
        log.info("{}", this.getClass().getSimpleName());
        AnlysisDefaultContext context = this.getRequestData();
        List<AysProcessDataModel> processData = context.getProcessData();
        List<String> idList = new ArrayList<>();
        List<ModelTopicRequest> aiRequestDateModelList = new ArrayList<>();
        try {
            aiRequestDateModelList = convertToParams(processData);
            if (log.isDebugEnabled()) {
                log.debug("发送MQ消息参数:{}", JSON.toJSONString(aiRequestDateModelList));
            }
            log.info("发送MQ消息参数:{}, {}", aiRequestDateModelList.size(), aiRequestDateModelList);
            List<String> channels = new ArrayList<>();
            if (StringUtils.isNotEmpty(channelCode)) {
                channels = Arrays.stream(channelCode.split(","))
                        .toList();
            }
            for (ModelTopicRequest aiRequestDateModel : aiRequestDateModelList) {
                // 1. 获取当前模型的topic_id
                String topicId = aiRequestDateModel.getTopic_id();
                // 空值校验（避免空指针和无效Redis操作）
                if (topicId == null || topicId.isEmpty()) {
                    continue;
                }
                cqCaToModelProducer.pushData(MessageDTO.builder().data(aiRequestDateModel).build());
                try {
                    if (channels.contains(aiRequestDateModel.getSource_data().getDataSource())) {
                        cqCaToModelProducer.pushDataTopic2(MessageDTO.builder().data(aiRequestDateModel).build());
                    }
                } catch (Exception e) {
                    log.info("发送voc_toModel_topic2消息异常:", e);
                }
            }
        } catch (Exception e) {
            log.info("发送MQ消息异常:{}", idList.size(), e);
            Thread.sleep(1000);
            List<ModelTopicRequest> aiRequestDateModels = aiRequestDateModelList.stream().filter(f -> !idList.contains(f.getTopic_id())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(aiRequestDateModels)) {
                int maxRetries = 3; // 最大重试次数
                int currentRetry = 0; // 当前重试次数
                boolean conditionMet = false; // 条件是否满足
                while (!conditionMet && currentRetry < maxRetries) {
                    try {
                        List<String> list = new ArrayList<>();
                        for (ModelTopicRequest aiRequestDateModel : aiRequestDateModels) {
                            cqCaToModelProducer.pushData(MessageDTO.builder().data(aiRequestDateModel).build());
                            list.add(aiRequestDateModel.getTopic_id());
                        }
                        aiRequestDateModels = aiRequestDateModelList.stream().filter(f -> !list.contains(f.getTopic_id())).collect(Collectors.toList());
                        if (CollectionUtil.isEmpty(aiRequestDateModels)) {
                            conditionMet = Boolean.TRUE;
                        }
                    } catch (Exception ex) {
                        currentRetry++;
                    }
                }
                if (CollectionUtil.isNotEmpty(aiRequestDateModels)) {
                    log.info("发送MQ消息异常异常数据 ids:{}", processData);

                }
            } else {
                log.info("失败前都已发送成功");
            }
        }
    }

    /**
     * 组装请求模型入参
     *
     * @param processData
     * @return
     */
    public List<ModelTopicRequest> convertToParams(List<AysProcessDataModel> processData) {
        List<ModelTopicRequest> aiRequestDateModelList = new ArrayList<>();
        for (AysProcessDataModel aysProcessDataModel : processData) {
            if (ObjectUtil.isNull(aysProcessDataModel.getData())) {
                continue;
            }
            ModelTopicRequest aiRequestDateModel = new ModelTopicRequest();
            aiRequestDateModel.setTopic_id(aysProcessDataModel.getId());
            SourceData sourceData = new SourceData();
            if (StringUtils.isNotEmpty(aysProcessDataModel.getContentType())) {
                ContentTypeEnum contentTypeEnum = ContentTypeEnum.getByCode(aysProcessDataModel.getContentType());
                sourceData.setDataSource_type(contentTypeEnum.getText());
                // 根据不同的ContentType执行不同的处理逻辑
                switch (contentTypeEnum) {
                    case OTHER: // 工单
                        handleOtherContent(aysProcessDataModel, sourceData);
                        break;
                    case PERSONAGE: // 帖子回评
                        handlePersonageContent(aysProcessDataModel, sourceData);
                        break;
                    case GUARANTOR: // 咨询
                        handleGuarantorContent(aysProcessDataModel, sourceData);
                        break;
                    case CORPORATION: // 意见反馈
                        handleCorporationContent(aysProcessDataModel, sourceData);
                        break;
                    case PROWLER: // 问卷
                        handleProwlerContent(aysProcessDataModel, sourceData);
                        break;
                    default:
                        log.info("数据错误:{}", JSONUtil.toJsonStr(aysProcessDataModel));
                        break;
                }
            } else {
                log.info("未知类型:{}", JSONUtil.toJsonStr(aysProcessDataModel));
            }
            if (ObjectUtil.isNotNull(sourceData)) {
                aiRequestDateModel.setSource_data(sourceData);
                aiRequestDateModelList.add(aiRequestDateModel);
            }
        }
        return aiRequestDateModelList;
    }

    /**
     * 处理问卷内容
     * "客户直评#售后问题-保养服务-会员活动及权益满意度-会员积分不容易获取#1-6分"
     *
     * @param aysProcessDataModel
     * @param sourceData
     */
    private void handleProwlerContent(AysProcessDataModel aysProcessDataModel, SourceData sourceData) {
        String dataStr = String.valueOf(aysProcessDataModel.getData());
        JSONObject jsonObject = JSON.parseObject(dataStr);
        sourceData.setBrand(ObjectUtil.isEmpty(jsonObject.getString("brand")) ? "" : jsonObject.getString("brand"));
        if (StringUtils.isNotEmpty(aysProcessDataModel.getContentType())) {
            sourceData.setDataSource_type(ContentTypeEnum.getByCode(aysProcessDataModel.getContentType()).getText());
        }
        Object bizExtAttrs2 = aysProcessDataModel.getBizExtAttrs2();
        String questType = "";
        if (ObjectUtil.isNotEmpty(bizExtAttrs2)) {
            cn.hutool.json.JSONObject entries = JSONUtil.parseObj(bizExtAttrs2);
            if (entries.containsKey("type")) {
                questType = entries.getStr("type");
            }
        }
        sourceData.setQuestType(questType);
        sourceData.setDataSource(aysProcessDataModel.getChannelId());
        sourceData.setTitle(ObjectUtil.isEmpty(jsonObject.getString("title")) ? "" : jsonObject.getString("title"));
        sourceData.setSeries(ObjectUtil.isEmpty(jsonObject.getString("series")) ? "" : jsonObject.getString("series"));
        sourceData.setCreate_time(LocalDateTime.now());
        sourceData.setExt(JSONUtil.parseObj(aysProcessDataModel));
        List<TopicText> topicTexts = new ArrayList<>();
        TopicText topicText = new TopicText();
        topicText.setRole("");
        topicText.setContent(jsonObject.getString("content"));
        topicTexts.add(topicText);
        sourceData.setTopic_text(topicTexts);
    }

    private void handleCorporationContent(AysProcessDataModel aysProcessDataModel, SourceData sourceData) {
        String dataStr = String.valueOf(aysProcessDataModel.getData());
        JSONObject jsonObject = JSON.parseObject(dataStr);
        sourceData.setBrand(ObjectUtil.isEmpty(jsonObject.getString("brand")) ? "" : jsonObject.getString("brand"));
        if (StringUtils.isNotEmpty(aysProcessDataModel.getContentType())) {
            sourceData.setDataSource_type(ContentTypeEnum.getByCode(aysProcessDataModel.getContentType()).getText());
        }
        sourceData.setDataSource(aysProcessDataModel.getChannelId());
        sourceData.setTitle(ObjectUtil.isEmpty(jsonObject.getString("title")) ? "" : jsonObject.getString("title"));
        sourceData.setSeries(ObjectUtil.isEmpty(jsonObject.getString("series")) ? "" : jsonObject.getString("series"));
        sourceData.setCreate_time(LocalDateTime.now());
        sourceData.setExt(JSONUtil.parseObj(aysProcessDataModel));
        List<TopicText> topicTexts = new ArrayList<>();
        TopicText topicText = new TopicText();
        topicText.setRole("");
        topicText.setContent(jsonObject.getString("content"));
        topicTexts.add(topicText);
        sourceData.setTopic_text(topicTexts);
    }


    private void handleGuarantorContent(AysProcessDataModel aysProcessDataModel, SourceData sourceData) {
        String dataStr = String.valueOf(aysProcessDataModel.getData());
        JSONObject jsonObject = JSON.parseObject(dataStr);
        sourceData.setBrand(ObjectUtil.isEmpty(jsonObject.getString("brand")) ? "" : jsonObject.getString("brand"));
        if (StringUtils.isNotEmpty(aysProcessDataModel.getContentType())) {
            sourceData.setDataSource_type(ContentTypeEnum.getByCode(aysProcessDataModel.getContentType()).getText());
        }
        Object bizExtAttrs3 = aysProcessDataModel.getBizExtAttrs3();
        List<TopicText> topicTexts = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(bizExtAttrs3)) {
            cn.hutool.json.JSONObject entries = JSONUtil.parseObj(bizExtAttrs3);
            if (entries.containsKey("content")) {
                topicTexts = JSONUtil.toList(JSONUtil.toJsonStr(entries.get("content")), TopicText.class);
            }
        }
        sourceData.setDataSource(aysProcessDataModel.getChannelId());
        sourceData.setTitle(ObjectUtil.isEmpty(jsonObject.getString("title")) ? "" : jsonObject.getString("title"));
        sourceData.setSeries(ObjectUtil.isEmpty(jsonObject.getString("series")) ? "" : jsonObject.getString("series"));
        sourceData.setCreate_time(LocalDateTime.now());
        sourceData.setExt(JSONUtil.parseObj(aysProcessDataModel));
        if (CollectionUtil.isNotEmpty(topicTexts)) {
            sourceData.setTopic_text(topicTexts);
        }
    }

    private void handleOtherContent(AysProcessDataModel aysProcessDataModel, SourceData sourceData) {
        String dataStr = String.valueOf(aysProcessDataModel.getData());
        JSONObject jsonObject = JSON.parseObject(dataStr);
        sourceData.setBrand(ObjectUtil.isEmpty(jsonObject.getString("brand")) ? "" : jsonObject.getString("brand"));
        if (StringUtils.isNotEmpty(aysProcessDataModel.getContentType())) {
            sourceData.setDataSource_type(ContentTypeEnum.getByCode(aysProcessDataModel.getContentType()).getText());
        }
        sourceData.setDataSource(aysProcessDataModel.getChannelId());
        sourceData.setTitle(ObjectUtil.isEmpty(jsonObject.getString("title")) ? "" : jsonObject.getString("title"));
        sourceData.setSeries(ObjectUtil.isEmpty(jsonObject.getString("series")) ? "" : jsonObject.getString("series"));
        sourceData.setCreate_time(LocalDateTime.now());
        sourceData.setExt(JSONUtil.parseObj(aysProcessDataModel));
        List<TopicText> topicTexts = new ArrayList<>();
        TopicText topicText = new TopicText();
        topicText.setRole("");
        topicText.setContent(jsonObject.getString("content"));
        topicTexts.add(topicText);
        sourceData.setTopic_text(topicTexts);
    }

    private void handlePersonageContent(AysProcessDataModel aysProcessDataModel, SourceData sourceData) {
        String dataStr = String.valueOf(aysProcessDataModel.getData());
        JSONObject jsonObject = JSON.parseObject(dataStr);
        sourceData.setBrand(ObjectUtil.isEmpty(jsonObject.getString("brand")) ? "" : jsonObject.getString("brand"));
        Object bizExtAttrs2 = aysProcessDataModel.getBizExtAttrs2();
        String desc = "";
        if (ObjectUtil.isNotEmpty(bizExtAttrs2)) {
            cn.hutool.json.JSONObject entries = JSONUtil.parseObj(bizExtAttrs2);
            if (entries.containsKey("is_main_post")) {
                desc = entries.getStr("is_main_post").equals("Y") ? "主贴" : "回评";
            }
        }
        if (StringUtils.isNotEmpty(aysProcessDataModel.getContentType())) {
            sourceData.setDataSource_type(ContentTypeEnum.getByCode(aysProcessDataModel.getContentType()).getText() + "-" + desc);
        }
        sourceData.setDataSource(aysProcessDataModel.getChannelId());
        sourceData.setTitle(ObjectUtil.isEmpty(jsonObject.getString("title")) ? "" : jsonObject.getString("title"));
        sourceData.setSeries(ObjectUtil.isEmpty(jsonObject.getString("series")) ? "" : jsonObject.getString("series"));
        sourceData.setCreate_time(LocalDateTime.now());
        sourceData.setExt(JSONUtil.parseObj(aysProcessDataModel));
        List<TopicText> topicTexts = new ArrayList<>();
        TopicText topicText = new TopicText();
        topicText.setRole("");
        topicText.setContent(jsonObject.getString("content"));
        topicTexts.add(topicText);
        sourceData.setTopic_text(topicTexts);
    }
}

