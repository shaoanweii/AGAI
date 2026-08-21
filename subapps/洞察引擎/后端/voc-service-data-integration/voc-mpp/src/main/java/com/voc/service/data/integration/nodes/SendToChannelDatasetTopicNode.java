package com.voc.service.data.integration.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.data.integration.api.ChannelExecutionResultService;
import com.voc.service.data.integration.api.IMppInputDataService;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.voc.service.data.integration.nodes.context.ChannelDatasetContext;
import com.voc.service.data.integration.producers.kafka.ChannelDatasetProducer;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: SendToChannelDatasetTopicNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "sendToChannelDatasetTopicNode", name = "发送各渠道数据到消息队列节点")
public class SendToChannelDatasetTopicNode extends NodeComponent {
    private static final Logger log = LoggerFactory.getLogger(SendToChannelDatasetTopicNode.class);
    public static final DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA);
    @Autowired
    ChannelDatasetProducer channelDatasetProducer;
//    @Autowired
//    ChannelExecutionResultService dataIntegrationRecordService;
    @Autowired
    IMppInputDataService inputDataService;
//    @Autowired
//    DataIntegrationConfig config;

    @Override
    public void process() throws Exception {
        ChannelDatasetContext context = this.getRequestData();
        final List<String> result = this.getCurrLoopObj();
        log.info("getCurrLoopObj： {}", result);
        if (CollUtil.isEmpty(result)) {
            log.warn("本次数据为空");
            //是否结束整个流程
            super.setIsEnd(true);
            return;
        }
        //推送队列
        log.info("推送 {} 条数据", result.size());
        List<DataIntegrationRecordModel> list = this.loadData(context.getChannelType(), context.getWorkId(), result);
        channelDatasetProducer.pushChannelData(MessageDTO.builder().source(context.getChannelType()).data(list).build());
        log.info("完成topic推送推送 {}条", list.size());
    }

    private List<DataIntegrationRecordModel> loadData(String channelType, String workId, List<String> ids) {
        List<ChannelMetaDataModel> list = inputDataService.loadData(new HashSet<>(ids));

        if (CollUtil.isNotEmpty(list)) {
            log.info("【{}】本次加载数据量：{}", channelType, list.size());
            return list.stream().map(item -> {
                try {
                    // 去重策略中使用，本条数据的唯一值
                    final String md5 = DigestUtil.md5Hex(StrUtil.concat(true
                            , item.getChannelCode(), item.getUserId(), item.getTitle(), item.getContent()
                            , ObjectUtil.isNull(item.getDataCreateTime()) ? null : item.getDataCreateTime().format(formatter2)));
                    JSONObject jsonExtObj;
                    if (ObjectUtil.isNotNull(item.getAttrs2())) {
                        jsonExtObj = JSONUtil.parseObj(item.getAttrs2());
                    }else{
                        jsonExtObj = JSONUtil.createObj();
                    }
                    jsonExtObj.set("md5",md5);
                    item.setAttrs2(jsonExtObj);

                    item.setAttrs( safeConvertMap(item.getAttrs()));
//                    item.setAttrs2( safeConvertMap(item.getAttrs2()));
                    item.setAttrs3( safeConvertMap(item.getAttrs3()));
                } catch (Exception e) {
                    log.error("数据解析异常：id:{}", item.getId());
                    log.error(e.getMessage(), e);
                }
                return DataIntegrationRecordModel.builder()
                        .id(item.getId())
                        .data(item)
                        .dataId(item.getDataId())
                        .channelType(item.getChannelCode())
                        .createTime(item.getCreateTime())
                        .workId(workId)
                        .tid(ServiceContextHolder.traceId())
                        .build();
            }).collect(Collectors.toList());
        } else {
            log.error("加载数据异常：channelType：{} , ids:{}", channelType, ids);
        }
        return Collections.EMPTY_LIST;
    }
    private Map<String, Object> safeConvertMap(Object obj) {
        if (obj == null || obj instanceof JSONNull) return null;

        // 递归清理JSONNull
        if (obj instanceof JSONObject) {
            JSONObject json = (JSONObject) obj;
            Map<String, Object> result = new HashMap<>();
            json.forEach((key, val) -> {
                if (val instanceof JSONNull) {
                    result.put(key, null);
                } else if (val instanceof JSONObject || val instanceof JSONArray) {
                    result.put(key, safeConvertComplexType(val)); // 递归处理嵌套
                } else {
                    result.put(key, val);
                }
            });
            return result.isEmpty() ? null : result;
        }

        // 处理String类型的JSON
        if (obj instanceof String && StrUtil.isNotBlank((String) obj)) {
            try {
                JSONObject parsed = JSONUtil.parseObj((String) obj);
                return safeConvertMap(parsed);
            } catch (Exception ignored) {
                // 非JSON字符串直接返回
            }
        }

        // 已是标准Map直接返回（需清理JSONNull）
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Map<String, Object> cleanMap = new HashMap<>();
            map.forEach((k, v) -> cleanMap.put(k.toString(),
                    v instanceof JSONNull ? null : v));
            return cleanMap;
        }

        return null; // 其他类型返回null
    }

    // 处理嵌套复杂类型
    private Object safeConvertComplexType(Object obj) {
        if (obj instanceof JSONArray) {
            JSONArray arr = (JSONArray) obj;
            return arr.stream()
                    .map(item -> item instanceof JSONNull ? null : item)
                    .collect(Collectors.toList());
        }
        return safeConvertMap(obj); // 递归处理JSONObject
    }
    @Override
    public boolean isAccess() {
        ChannelDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getChannelId cannot be empty");

        return true;
    }
}
