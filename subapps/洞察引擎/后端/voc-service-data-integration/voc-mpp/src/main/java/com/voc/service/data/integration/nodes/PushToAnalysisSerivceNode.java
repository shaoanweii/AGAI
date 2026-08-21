package com.voc.service.data.integration.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.*;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.data.integration.api.IMppInputDataService;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.voc.service.data.integration.enums.ErrorDataMsgEnums;
import com.voc.service.data.integration.nodes.context.ChannelDatasetContext;
import com.voc.service.insights.engine.api.clients.IAysCoreServiceClient;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "pushToAnalysisSerivceNode", name = "推送数据到数据清洗服务")
public class PushToAnalysisSerivceNode extends NodeComponent {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private static final Logger log = LoggerFactory.getLogger(PushToAnalysisSerivceNode.class);

    @Autowired
    DataIntegrationConfig config;
    @Autowired
    IMppInputDataService inputDataService;
    @Autowired
    IAysCoreServiceClient aysCoreServiceClient;


    @Override
    public void process() throws Exception {
        ChannelDatasetContext context = this.getRequestData();
        try {
            if (CollUtil.isEmpty(context.getSuccessfulDataset())) {
                throw new Exception("【".concat(context.getChannelType()).concat("】数据验证后无成功数据："));
            } else {
                log.info("【{}】数据验证成功，成功数据：{}", context.getWorkId(), context.getSuccessfulDataset().size());
                //保存记录成功数据集
                final List<ChannelMetaDataModel> list = context.getSuccessfulDataset().stream().map(item -> {
                    JSONObject jsonObj = JSONUtil.parseObj(item.getData());
                    return JSONUtil.toBean(jsonObj, ChannelMetaDataModel.class);
                }).collect(Collectors.toList());
                this.pushSuccessfulData(context.getWorkId(), context.getClientId(), context.getChannelType(), list);
            }
        } catch (Exception e) {
            log.info("推送数据错误信息：", e);
            //出现服务异常时，将所有数据归为异常处理数据集
            final List<DataIntegrationRecordModel> errorList = context.getSuccessfulDataset().stream().map(data -> {
                data.setErrorCode(ErrorDataMsgEnums.PushServiceHasFailed.getCode());
                data.setErrorMsg(ErrorDataMsgEnums.PushServiceHasFailed.getText());
                return data;
            }).collect(Collectors.toList());
            context.getFailedDataset().addAll(errorList);
            context.setSuccessfulDataset(null);
//            throw new Exception(e.getMessage(), e);
        }
    }

    /**
     * 保存记录成功数据集
     */
    private Set<String> pushSuccessfulData(final String workId, final String clientId, String channelType, List<ChannelMetaDataModel> successfulDataset) {
        if (CollUtil.isEmpty(successfulDataset)) {
            throw new RuntimeException("【".concat(channelType).concat("】未读取到新数据"));
        }
        if (StrUtil.isBlank(config.getRequestAuthorizationTokens())) {
            throw new RuntimeException("【".concat(channelType).concat("】未配置token"));
        }

        for (List<ChannelMetaDataModel> list : CollUtil.split(successfulDataset, config.getBatchPushApiDataSetSize())) {
            log.info("【{}】开始执行数据推送：{}/{}", workId, list.size(), successfulDataset.size());
            log.info("work_id:{} data_id->list:{}", workId, list.stream().map(ChannelMetaDataModel::getDataId).collect(Collectors.toSet()));
            ServiceContextHolder.setToken(config.getRequestAuthorizationTokens());

            List<Object> data = new ArrayList<>();
            for (ChannelMetaDataModel model : list) {
                Map<String, Object> item = MapUtil.newHashMap();
                //原始数据ID
                item.put("id", model.getId());
                item.put("data_create_time", model.getDataCreateTime());
                item.put("create_time", model.getCreateTime());
                item.put("content_type", model.getContentType());
                item.put("data_update_time", model.getDataUpdateTime());
                item.put("channel_code", model.getChannelCode());
                item.put("data_id", model.getDataId());
                item.put("brand", model.getBrand());
                item.put("series", model.getSeries());
                item.put("model", model.getModel());
                item.put("is_outer", model.getIsOuter());
                item.put("id_car_no", model.getIdCarNo());
                item.put("one_id", model.getOneId());
                item.put("mobile", model.getMobile());
                item.put("email", model.getEmail());
                item.put("global_id", model.getGlobalId());
                item.put("user_id", model.getUserId());
                item.put("user_name", model.getUserName());
                item.put("vhl_id", model.getVhlId());
                item.put("vhl_vin", model.getVhlVin());
                item.put("dlr_id", model.getDlrId());
                item.put("dlr_code", model.getDlrCode());
                item.put("dlr_type", model.getDlrType());
                item.put("market_id", model.getMarketId());
                item.put("title", model.getTitle());
                item.put("content", model.getContent());
                item.put("is_wsater_army", model.getIsWsaterArmy());
                item.put("weight", model.getWeight());
                //设置数据清洗接口必填字段  ->data
                item.put("type", config.getProcessContentType());  //加载配置
                item.put("channelId", model.getChannelCode());
                item.put("publish_time", ObjUtil.isNull(model.getDataCreateTime().format(formatter)) ? null : model.getDataCreateTime().format(formatter));
                try {
                    item.put("attrs", safeConvertMap(model.getAttrs()));
                    item.put("attrs2", safeConvertMap(model.getAttrs2()));
                    item.put("attrs3", safeConvertMap(model.getAttrs3()));
                } catch (Exception e) {
                    log.error("attrs【".concat(channelType).concat("】数据清洗接口扩展字段1设置异常：{}"), e.getMessage());
                    throw e;
                }
                try {
                    item.put("cust_ext_attrs", safeConvertMap(model.getCustExtAttrs()));
                    item.put("vhl_ext_attrs", safeConvertMap(model.getVhlExtAttrs()));
                    item.put("dealer_ext_attrs", safeConvertMap(model.getDealerExtAttrs()));
                    item.put("prd_ext_attrs", safeConvertMap(model.getPrdExtAttrs()));
                } catch (Exception e) {
                    log.error("ext_attrs【".concat(channelType).concat("】数据清洗接口扩展字段2设置异常：{}"), e.getMessage());
                    throw e;
                }

                data.add(item);

            }
            final InsDataSourceModel pushData = InsDataSourceModel.builder()
                    .requestId(workId)
                    .clientId(clientId)
                    .modelType("3")
                    .modelType(config.getProcessModelType())    //加载配置
                    .data(data)
                    .workId(workId)
                    .build();
            if(log.isDebugEnabled()) {
                log.debug("aysCoreServiceClient_batchPushData -> {}", JSONUtil.toJsonPrettyStr(pushData));
            }

            final Result<?> rs = aysCoreServiceClient.batchPushData(pushData);
            log.info("本次成功推送到数据清洗服务 size:{}，resutlMsg：{}", list.size(), rs.getMessage());
            if (StrUtil.equals(rs.getCode(), "200")) {
                try {
                    JSONObject jsonObj = JSONUtil.parseObj(rs.getResult());
                    log.info("requestId:{} workId:{}", workId, jsonObj.get("workId"));
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                //将处理ids做记录
                final Set<String> ids = this.getIds(successfulDataset);
//                loadDataIdsCache.putAll(ids.stream().collect(Collectors.toMap(id -> id, id -> "1", (v1, v2) -> v2)));
                log.info("successfulDataset.ids: {}", ids);
                return ids;
            } else {
                throw new RuntimeException("【".concat(channelType).concat("】向数据清洗推送数据失败：").concat(rs + ""));
            }
        }
        return Collections.EMPTY_SET;
    }
    // 新增工具方法（替换原有的convertToJsonObj）
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
    private Set<String> getIds(List<ChannelMetaDataModel> list) {
        return list.stream()
                .map(item -> {
                    JSONObject jsonObj = JSONUtil.parseObj(item);
                    return this.getId(jsonObj, "id");
                })
                .collect(Collectors.toSet());
    }

    private String getId(JSONObject jsonObj, String field) {
        return jsonObj.getStr(field);
    }

    @Override
    public boolean isAccess() {
        ChannelDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");

        return true;
    }

    public static void main(String[] args) {
        /*Test2 t2 = new Test2();
        t2.setName("test222222222");
        Test t = new Test();
        t.setBizExt(new HashMap<>());
        t.setTest(t2);

        System.out.println(JSONUtil.toJsonStr(t));*/

        String json = "{\"biz_ext\": {\"brand1\": \"日产\",\"series1\": \"第14代轩逸\",\"desc1\": \"\"},}";

        JSONObject jsonObject = JSONUtil.parseObj(json);
        Test t3 = JSONUtil.toBean(jsonObject, Test.class);
        System.out.println(t3);
    }

    @ToString
    public static class Test {
        Test() {
        }

        Map<String, String> bizExt;

        public Map<String, String> getBizExt() {
            return bizExt;
        }

        public void setBizExt(Map<String, String> bizExt) {
            this.bizExt = bizExt;
        }

        public Test2 getTest() {
            return test;
        }

        public void setTest(Test2 test) {
            this.test = test;
        }

        Test2 test;


    }

    public static class Test2 {
        Test2() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        String name;
    }
}
