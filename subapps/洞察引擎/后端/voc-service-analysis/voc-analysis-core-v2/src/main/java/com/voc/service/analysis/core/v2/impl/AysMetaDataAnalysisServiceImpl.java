package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.AysExtAttrsMappingValuesService;
import com.voc.service.analysis.api.IAysErrorPushService;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.entity.AysMetaDataAnalysisEntity;
import com.voc.service.analysis.core.v2.entity.AysMetaDataExtAnalysisEntity;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.mapper.AysMetaDataAnalysisMapper;
import com.voc.service.analysis.core.v2.producers.kafka.MetaDataAnalysisProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.model.AysMetaDataAnalysisModel;
import com.voc.service.analysis.model.ErrorPushModel;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.common.util.StopWatch;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.clients.InsTagLibServiceClient;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Title: AysMetaDataAnalysisServiceImpl
 * @Package: com.voc.service.analysis.core.v2.impl
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 11:49
 * @Version:1.0
 */
@DS("voc")
@Service
public class AysMetaDataAnalysisServiceImpl
        extends ServiceImpl<AysMetaDataAnalysisMapper, AysMetaDataExtAnalysisEntity>
        implements IAysMetaDataAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AysMetaDataAnalysisServiceImpl.class);

    @Autowired
    AysConvertMapperService aysConvertMapperService;
    @Autowired
    AnalysisConfig config;
    //    @Autowired
//    IChannelServiceClient iChannelServiceClient;
//    @Autowired
//    KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    MetaDataAnalysisProducer metaDataAnalysisProducer;
    @Autowired
    IAysErrorPushService errorPushService;
    @Autowired
    AysExtAttrsMappingValuesService aysExtAttrsMappingValuesService;
    @Autowired
    InsTagLibServiceClient insTagLibServiceClient;


    //    @Transactional
    @Override
    public Set<String> saveBatchMq(String clientId, String workId, String reqeustId, String type, String dataSource, List<Object> data, Integer modelType, Integer showType) throws Exception {
        Assert.isTrue(StrUtil.isNotEmpty(clientId), "clientId  be empty");
        Assert.isTrue(StrUtil.isNotEmpty(workId), "workId  be empty");
        List<AysMetaDataAnalysisEntity> saveList;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("saveBatchMq");
        try {
            //扩展字段
            Map<String, Object> extAttrMaps = new HashMap<>();
            saveList = data.stream().filter(ObjUtil::isNotNull)
                    .map(obj -> {
                        JSONObject jsonObj = JSONUtil.parseObj(obj);
                        final String id = this.getId(obj);
                        final String dataId = jsonObj.getStr(config.getData_id_attr_name());
                        final String channelId = jsonObj.getStr(config.getChannel_id_attr_name());
                        final String contentType = jsonObj.getStr(config.getContent_type_attr_name());
                        String content = jsonObj.getStr(config.getContent_attr_name());
                        String time = jsonObj.get(config.getPublishTime_attr_name(), null);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime localDateTime = LocalDateTime.parse(time, formatter);
                        log.info("时间：{}", jsonObj.getStr(config.getPublishTime_attr_name(), null));
                        String userName = jsonObj.getStr(config.getUserName_attr_name());
                        String title = jsonObj.getStr(config.getTitle_attr_name());
                        jsonObj.set(config.getContent_attr_name(), content);
                        final String oneId = jsonObj.getStr("one_id");


                        try {
                            AysMetaDataAnalysisEntity entity = AysMetaDataAnalysisEntity.builder()
                                    .id(id)
                                    .dataId(dataId)
                                    .workId(workId)
                                    .clientId(clientId)
                                    .channelId(channelId)
                                    .contentType(contentType)
                                    .publishTime(localDateTime)
                                    .createTime(LocalDateTime.now())
                                    .title(title)
                                    .userName(userName)
                                    .modelType(modelType)
                                    .content(content)
//                                    .bizExtAttrs(bizExtAttrsMap)
//                                    .bizExtAttrs2(bizExtAttrsMap2)
//                                    .bizExtAttrs3(bizExtAttrsMap3)
//                                    .custExtAttrs(custExtAttrs)
//                                    .vhlExtAttrs(vhlExtAttrs)
//                                    .dealerExtAttrs(dealerExtAttrs)
//                                    .prdExtAttrs(prdExtAttrs)
                                    .oneId(oneId)
                                    .extFields(MapUtil.isEmpty(extAttrMaps) ? null : extAttrMaps)
                                    .data(obj)
                                    .done("0")
                                    .dataStatus(0)
                                    .build();
                            try {
                                entity.setBizExtAttrs(JSONUtil.parseObj(jsonObj.getStr("attrs")));
                                entity.setBizExtAttrs2(JSONUtil.parseObj(jsonObj.getStr("attrs2")));
                                entity.setBizExtAttrs3(JSONUtil.parseObj(jsonObj.getStr("attrs3")));
                            } catch (Exception e) {
                                log.error("attrs JSON parse error:{}", e.getMessage());
                            }
                            try {
                                entity.setCustExtAttrs(JSONUtil.parseObj(jsonObj.getStr("cust_ext_attrs")));
                                entity.setVhlExtAttrs(JSONUtil.parseObj(jsonObj.getStr("vhl_ext_attrs")));
                                entity.setDealerExtAttrs(JSONUtil.parseObj(jsonObj.getStr("dealer_ext_attrs")));
                                entity.setPrdExtAttrs(jsonObj.getJSONObject("prd_ext_attrs"));
                            } catch (Exception e) {
                                log.error("extAttrs JSON parse error:{}", e.getMessage());
                            }

                            try {
                                Assert.isTrue(StrUtil.isNotBlank(entity.getId()), "getId cannot be empty");
                                Assert.isTrue(StrUtil.isNotBlank(entity.getDataId()), "getDataId cannot be empty");
                                Assert.isTrue(StrUtil.isNotBlank(entity.getWorkId()), "getWorkId cannot be empty");
                                Assert.isTrue(StrUtil.isNotBlank(entity.getClientId()), "getClientId cannot be empty");
                                Assert.isTrue(StrUtil.isNotBlank(entity.getChannelId()), "getChannelId cannot be empty");
                                Assert.isTrue(StrUtil.isNotBlank(entity.getOneId()), "getOneId cannot be empty");

                            } catch (IllegalArgumentException e) {
                                //异常入库数据纪录
                                errorPushService.push(ErrorPushModel
                                        .builder()
                                        .table("voc_anal_flow_mate_data_full")
                                        .clientId(clientId)
                                        .action(IAysErrorPushService.ACTION_ADD)
                                        .data(entity)
                                        .workId(entity.getWorkId())
                                        .tid(ServiceContextHolder.traceId())
                                        .build());
                                throw e;
                            }

                            return entity;
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                        return null;
                    })
                    .filter(ObjUtil::isNotNull)
                    .collect(Collectors.toList());

            log.info("本次解析后需保存{}条数据", saveList.size());
            if (CollUtil.isEmpty(saveList)) {
                return Collections.EMPTY_SET;
            }
            Set<String> newIds = saveList.stream().map(AysMetaDataAnalysisEntity::getDataId).collect(Collectors.toSet());
            log.info("开始发送前置MQ消息 初始要推送原文ID数量:{}", newIds.size());
            Set<String> modelList = new HashSet<>();
            try {
                modelList = this.findDataIdListByIds(clientId, newIds);
            } catch (Exception e) {
                log.error("查询原文数据错误:", e);
            }
            log.info("过去执行过原文ID数量:{}", modelList.size());
            List<String> filterNewIdList = new ArrayList<>();
            for (String n : newIds) {
                if (!modelList.contains(n)) {
                    filterNewIdList.add(n);
                }
            }
            log.info("现在要执行原文ID数量:{}", filterNewIdList.size());
            //将数据放入MQ中
            if (CollUtil.isNotEmpty(filterNewIdList)) {
                List<AysMetaDataAnalysisEntity> filterMetaDataAnalysisEntity = saveList.stream().filter(s -> filterNewIdList.contains(s.getDataId()) && ObjectUtils.isNotEmpty(s.getOneId())).collect(Collectors.toList());
                log.info("保存入库要执行的原始数据条数:{}", filterMetaDataAnalysisEntity.size());
                metaDataAnalysisProducer.pushData(MessageDTO.builder().source(clientId).type(type).data(filterMetaDataAnalysisEntity).build());
                //开启前置任务执行
//                processPreRulesProducer.pushEvent(MessageDTO.builder().token("初始化：前置任务执行收到条数" + saveList.size() + "推送条数" + filterNewIdList.size()).source(clientId).type(type).data(filterNewIdList).build());
            }
            log.info("saveBatch success");
            stopWatch.stop();
            return newIds;
        } catch (Exception e) {
            throw e;
        }
    }

    private String removeTagsWithRegex(String html) {
        if (!config.isRemoveTagsWithRegexEnable()) {
            log.debug("removeTagsWithRegex disable");
            return html;
        }
        if (html == null || html.isEmpty()) {
            return html;
        }
        log.debug("removeTagsWithRegex html:{}", html);
        // 先移除script/style标签及其内容（注意：正则处理嵌套标签不可靠）
        AtomicReference<String> noTag = new AtomicReference<>(html);
        config.getRemoveTagsWithRegex()
                .stream().filter(StrUtil::isNotBlank)
                .map(regex -> Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL))
                .forEach(pattern -> {
                    try {
                        // 使用Pattern编译正则，提高效率（若多次调用）
//                Pattern pattern = Pattern.compile("<span\\b[^>]*>(.*?)</span>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
//                final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                        String result = noTag.get();
                        // 循环替换，直到没有匹配
                        while (true) {
                            Matcher matcher = pattern.matcher(result);
                            if (!matcher.find()) {
                                break;
                            }
                            // 替换所有匹配（非贪婪匹配会匹配最内层的span）
                            result = matcher.replaceAll("$1");
                        }
                        noTag.set(result);
                        log.debug("removeTagsWithRegex regex:{},html:{}", pattern.pattern(), noTag.get());
                    } catch (Exception e) {
                        log.error("removeTagsWithRegex error:{}", e.getMessage());
                    }
                });

        return noTag.get();
    }

    @Override
    public Set<String> saveBatchExtMq(String clientId, String workId, String reqeustId, String type
            , String dataSource, List<Object> data, Integer modelType, Integer showType) throws Exception {
        Assert.isTrue(StrUtil.isNotEmpty(clientId), "clientId  be empty");
        Assert.isTrue(StrUtil.isNotEmpty(workId), "workId  be empty");
        List<AysMetaDataExtAnalysisEntity> saveList;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("saveBatchExtMq");
        try {
            saveList = data.stream().filter(ObjUtil::isNotNull)
                    .map(obj -> {
                        JSONObject jsonObj = JSONUtil.parseObj(obj);
                        final String id = DigestUtil.md5Hex(IdWorker.getId());

                        String time = jsonObj.getStr("publish_time");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        final LocalDateTime dataCreateTime = LocalDateTime.parse(time, formatter);


                        final String rawContent = jsonObj.getStr("content");
                        final String content = this.removeTagsWithRegex(rawContent);

                        AysMetaDataExtAnalysisEntity entity = AysMetaDataExtAnalysisEntity.builder()
                                .id(id)
                                .clientId(clientId)
                                .dataCreateTime(dataCreateTime)
                                .dataId(jsonObj.getStr("data_id"))
                                .createTime(LocalDateTime.now())
                                .workId(workId)
                                .dataUpdateTime(null)
                                .contentType(jsonObj.getStr("content_type"))
                                .channelCode(jsonObj.getStr("channel_code"))
                                .brand(jsonObj.getStr("brand"))
                                .series(jsonObj.getStr("series"))
                                .model(jsonObj.getStr("model"))
                                .isOuter(jsonObj.getStr("is_outer"))
                                .oneId(jsonObj.getStr("one_id"))
                                .idCarNo(jsonObj.getStr("id_car_no"))
                                .mobile(jsonObj.getStr("mobile"))
                                .email(jsonObj.getStr("email"))
                                .globalId(jsonObj.getStr("global_id"))
                                .userId(jsonObj.getStr("user_id"))
                                .userName(jsonObj.getStr("user_name"))
                                .vhlId(jsonObj.getStr("vhl_id"))
                                .vhlVin(jsonObj.getStr("vhl_vin"))
                                .dlrId(jsonObj.getStr("dlr_id"))
                                .dlrCode(jsonObj.getStr("dlr_code"))
                                .dlrType(jsonObj.getStr("dlr_type"))
                                .marketId(jsonObj.getStr("market_id"))
                                .title(jsonObj.getStr("title"))
                                .content(content)
                                .isWsaterArmy(jsonObj.getStr("is_wsater_army"))
                                .weight(jsonObj.getStr("weight"))
//                                .attrs(JSONUtil.parseObj(jsonObj.getStr("attrs")))
//                                .attrs2(JSONUtil.parseObj(jsonObj.getStr("attrs2")))
//                                .attrs3(JSONUtil.parseObj(jsonObj.getStr("attrs3")))
//                                .custExtAttrs(JSONUtil.parseObj(jsonObj.getStr("cust_ext_attrs")))
//                                .vhlExtAttrs(JSONUtil.parseObj(jsonObj.getStr("vhl_ext_attrs")))
//                                .dealerExtAttrs(JSONUtil.parseObj(jsonObj.getStr("dealer_ext_attrs")))
//                                .prdExtAttrs(jsonObj.getJSONObject("prd_ext_attrs"))
                                .done("0")
                                .dataStatus(0)
                                .modelType(jsonObj.getInt("model_type", null))
                                .ds(jsonObj.getStr("ds"))
                                .insertDt(LocalDateTime.now())
                                .build();

                        try {
                            entity.setAttrs(safeConvertMap(jsonObj.getStr("attrs")));
                            entity.setAttrs2(safeConvertMap(jsonObj.getStr("attrs2")));
                            entity.setAttrs3(safeConvertMap(jsonObj.getStr("attrs3")));
                        } catch (Exception e) {
                            log.error("attrs JSON parse error:{}", e.getMessage());
                        }
                        try {
                            entity.setCustExtAttrs(safeConvertMap(jsonObj.getStr("cust_ext_attrs")));
                            entity.setVhlExtAttrs(safeConvertMap(jsonObj.getStr("vhl_ext_attrs")));
                            entity.setDealerExtAttrs(safeConvertMap(jsonObj.getStr("dealer_ext_attrs")));
                            entity.setPrdExtAttrs(safeConvertMap(jsonObj.getJSONObject("prd_ext_attrs")));
                        } catch (Exception e) {
                            log.error("extAttrs JSON parse error:{}", e.getMessage());
                        }

                        try {
                            Assert.isTrue(StrUtil.isNotBlank(entity.getId()), "getId cannot be empty");
                            Assert.isTrue(StrUtil.isNotBlank(entity.getDataId()), "getDataId cannot be empty");
                            Assert.isTrue(StrUtil.isNotBlank(entity.getWorkId()), "getWorkId cannot be empty");
                            Assert.isTrue(StrUtil.isNotBlank(entity.getClientId()), "getClientId cannot be empty");
                            Assert.isTrue(StrUtil.isNotBlank(entity.getChannelCode()), "getChannelId cannot be empty");
                            Assert.isTrue(StrUtil.isNotBlank(entity.getOneId()), "getOneId cannot be empty");

                        } catch (IllegalArgumentException e) {
                            //异常入库数据纪录
                            try {
                                errorPushService.push(ErrorPushModel
                                        .builder()
                                        .table("voc_anal_flow_mate_data_full")
                                        .clientId(clientId)
                                        .action(IAysErrorPushService.ACTION_ADD)
                                        .data(entity)
                                        .workId(entity.getWorkId())
                                        .tid(ServiceContextHolder.traceId())
                                        .build());
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                            throw e;
                        }

                        // 将 entity 中属性值为空字符串的设置为 null（Hutool 实现）
                        Field[] fields = ReflectUtil.getFields(entity.getClass());
                        for (Field field : fields) {
                            try {
                                Object value = ReflectUtil.getFieldValue(entity, field);
                                if (value instanceof String && StrUtil.isBlank((String) value)) {
                                    ReflectUtil.setFieldValue(entity, field, null);
                                }
                            } catch (Exception e) {
                                log.warn("Failed to process field: {}", field.getName(), e);
                            }
                        }

                        return entity;
                    })
                    .filter(ObjUtil::isNotNull)
                    .collect(Collectors.toList());


            log.info("本次解析后需保存{}条数据", saveList.size());
            if (CollUtil.isEmpty(saveList)) {
                return Collections.EMPTY_SET;
            }
            final Set<String> newIds = saveList.stream().map(AysMetaDataExtAnalysisEntity::getDataId).collect(Collectors.toSet());
            log.info("开始发送前置MQ消息 初始要推送原文ID数量:{}", newIds.size());
            Set<String> modelList = new HashSet<>();
            try {
                modelList = this.findDataIdListByIds(clientId, newIds);
            } catch (Exception e) {
                log.error("查询原文数据错误:", e);
            }
            log.info("过去执行过原文ID数量:{}", modelList.size());
            List<String> filterNewIdList = new ArrayList<>();
            for (String n : newIds) {
                if (!modelList.contains(n)) {
                    filterNewIdList.add(n);
                }
            }
            log.info("现在要执行原文ID数量:{}", filterNewIdList.size());
            //将数据放入MQ中
            if (CollUtil.isNotEmpty(filterNewIdList)) {
                List<AysMetaDataExtAnalysisEntity> filterMetaDataAnalysisEntity = saveList.stream()
                        .filter(s -> filterNewIdList.contains(s.getDataId()) && ObjectUtils.isNotEmpty(s.getOneId()))
                        .collect(Collectors.toList());
                log.info("保存入库要执行的原始数据条数:{}", filterMetaDataAnalysisEntity.size());
                if (log.isDebugEnabled()) {
                    log.debug("filterMetaDataAnalysisEntity -> {}", JSONUtil.toJsonPrettyStr(filterMetaDataAnalysisEntity));
                }

                metaDataAnalysisProducer.pushExtData(MessageDTO.builder().source(clientId).type(type).data(filterMetaDataAnalysisEntity).build());

            }
            log.info("saveBatch success");
            stopWatch.stop();
            return newIds;
        } catch (Exception e) {
            throw e;
        }
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

    /*@SwitchClientDS
    @Override
    public List<AysMetaDataAnalysisModel> saveBatch(final String clientId, String workId, List<Object> data, boolean isSave) throws Exception {
        List<AysMetaDataAnalysisEntity> saveList = new ArrayList<>();
        try {

            final Stack<String> ids = IdWorker.getIds(data.size());
            saveList = data.stream().filter(ObjUtil::isNotNull)
                    .map(obj -> {
                        JSONObject jsonObj = JSONUtil.parseObj(obj);
                        final String id = this.getId(obj);
                        final String clientId_ = jsonObj.getStr(config.getClient_id_attr_name());
                        final String channelId = jsonObj.getStr(config.getChannel_id_attr_name());
                        final String contentType = jsonObj.getStr(config.getContent_type_attr_name());
                        final String content = jsonObj.getStr(config.getContent_attr_name());
                        LocalDateTime localDateTime = jsonObj.getLocalDateTime(config.getPublishTime_attr_name(), LocalDateTime.now());
                        final String userName = jsonObj.getStr(config.getUserName_attr_name());
                        final String title = jsonObj.getStr(config.getTitle_attr_name());
                        jsonObj.set(config.getContent_attr_name(), content);
                        try {
                            Assert.isTrue(StrUtil.isNotEmpty(clientId), "getClientId cannot be empty");
                            Assert.isTrue(StrUtil.isNotEmpty(channelId), "getChannelId cannot be empty");
                            Assert.isTrue(StrUtil.isNotEmpty(contentType), "getContentType cannot be empty");

                            return AysMetaDataAnalysisEntity.builder()
                                    .id(ids.pop())
                                    .dataId(id)
                                    .workId(workId)
                                    .clientId(clientId_)
                                    .channelId(channelId)
                                    .contentType(contentType)
//                                    .publishTime(localDateTime)
                                    .title(title)
                                    .userName(userName)
                                    .content(content)
                                    .data(JSONUtil.toJsonStr(obj))
                                    .done("0")
                                    .dataStatus(0)
                                    .build();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                        return null;
                    })
                    .filter(ObjUtil::isNotNull)
                    .collect(Collectors.toList());

            log.info("本次解析后需保存{}条数据", saveList.size());
            if (CollUtil.isEmpty(saveList)) {
                return null;
            }

            if (isSave) {
                List<List<AysMetaDataExtAnalysisEntity>> subList = CollUtil.split(saveList, 50);
                for (List<AysMetaDataExtAnalysisEntity> aysMetaItemsDataEntities : subList) {
                    //调用starrocks api
                    this.saveBatch(aysMetaItemsDataEntities);

                    //将数据放入MQ中
//                    final Set<String> pushIds = aysMetaItemsDataEntities.stream().map(AysMetaDataAnalysisEntity::getNewId).collect(Collectors.toSet());
//                    processPreRulesCusumer.push(MessageDTO.builder().data(pushIds).build());
                }

                log.info("saveBatch success");
            } else {
                log.info("success");
            }
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            e.printStackTrace();
            throw e;
        }

        return aysConvertMapperService.converToMetaItemsDataModel(saveList);
    }*/

    /*@Override
    public List<AysMetaDataAnalysisModel> saveBatch(final String clientId, String workId, List<Object> data) throws Exception {
        return saveBatch(clientId, workId, data, true);
    }*/

    /*@SwitchClientDS
    @Override
    public List<AysMetaDataAnalysisModel> findByWorkId(final String clientId, List<String> workIdList) {
        List<AysMetaDataExtAnalysisEntity> entityList = this.list(
                new QueryWrapper<AysMetaDataExtAnalysisEntity>()
                        .in("work_id", workIdList)
                        .eq("done", "1")
        );

        return aysConvertMapperService.cenvertToModelExtList(entityList);
    }*/

    /*@SwitchClientDS
    @Override
    public void saveErrorMsg(String clientId, List<AysMetaDataAnalysisModel> errorList) {
        if (CollUtil.isEmpty(errorList)) {
            return;
        }
        List<AysMetaDataAnalysisEntity> list = aysConvertMapperService.converToMetaItemsEntity(errorList);
        list.stream().map(e ->
                AysMetaDataAnalysisEntity.builder()
                        .id(e.getId())
                        .build()
        ).forEach(entity -> {
            this.baseMapper.updateErrorMsgById(entity);
        });


    }*/

    @SwitchClientDS
    @Override
    public int modifyToDone(final String clientId, Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }

        List<List<String>> subList = CollUtil.split(ids, 200);
        for (List<String> subs : subList) {
            UpdateWrapper<AysMetaDataExtAnalysisEntity> wrapper = new UpdateWrapper<>();
            wrapper.in("data_id", subs);
            wrapper.set("done", "1");

            this.update(wrapper);
        }

        return 1;
    }

    /*@SwitchClientDS
    @Override
    public int modifyToDataStatus(final String clientId, Set<String> ids, String dataStatus) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }

        try {
            // 将字符串状态值转换为Integer
            Integer status = Integer.valueOf(dataStatus);

            // 调用Mapper的批量更新方法，一次SQL更新所有数据
            //  int updated = this.baseMapper.batchUpdateDataStatus(ids, status);

            log.debug("Batch updated {} records with status {}", 0, status);
            return 0;
        } catch (NumberFormatException e) {
            log.error("Invalid dataStatus value: {}, must be a valid integer", dataStatus, e);
            return 0;
        } catch (Exception e) {
            log.error("Failed to batch update status {} for {} records", dataStatus, ids.size(), e);
            return 0;
        }
    }*/

    /* public int modifyToDone(Set<String> ids, List<AysProcessDataModel> aysPreprocessData) {
         if (CollUtil.isEmpty(ids)) {
             return 0;
         }
         List<AysMetaDataAnalysisEntity> entityList = this.list(
                 new QueryWrapper<AysMetaDataAnalysisEntity>()
                         .in("new_id", ids)
         );
         Map<String, AysMetaDataAnalysisEntity> aysMetaDataAnalysisModelMap = entityList.stream().collect(Collectors.toMap(AysMetaDataAnalysisEntity::getNewId, Function.identity()));
         for (AysProcessDataModel aysProcessDataModel : aysPreprocessData) {
             UpdateWrapper<AysMetaDataAnalysisEntity> wrapper = new UpdateWrapper<>();
             wrapper.in("new_id", aysProcessDataModel.getNewId());
             wrapper.set("done", "1");
             if (aysProcessDataModel.getAbandon().equals("1")) {
                 wrapper.set("data_status", "1");
             }
             this.update(wrapper);
         }
         return aysMetaDataAnalysisModelMap.values().size();
     }*/

    @Override
    public int modifyToDataStatus(String clientId, Map<String, Integer> dataStatusMap) {

        if (CollUtil.isEmpty(dataStatusMap)) {
            return 0;
        }

        // 按状态值分组，每个状态值对应一个data_id集合
        Map<Integer, Set<String>> statusGroupMap = new HashMap<>();
        dataStatusMap.forEach((dataId, status) -> {
            statusGroupMap.computeIfAbsent(status, k -> new HashSet<>()).add(dataId);
        });

        // 记录更新的总记录数
        int totalUpdated = 0;

        // 根据status值分批执行更新，每个状态值使用一条SQL批量更新
        for (Map.Entry<Integer, Set<String>> entry : statusGroupMap.entrySet()) {
            final Integer status = entry.getKey();
            final Set<String> dataIds = entry.getValue();

            if (CollUtil.isEmpty(dataIds)) {
                continue;
            }
            try {
                List<List<String>> subList = CollUtil.split(dataIds, 500);

                subList.forEach(subs -> {
                    // 调用Mapper的批量更新方法，一次SQL更新同一状态的所有数据
                    UpdateWrapper<AysMetaDataExtAnalysisEntity> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.lambda().in(AysMetaDataExtAnalysisEntity::getDataId, dataIds);
                    updateWrapper.lambda().set(AysMetaDataExtAnalysisEntity::getDataStatus, status);

//                 int updated = this.baseMapper.batchUpdateDataStatus(dataIds, status);
                    boolean updated = this.update(updateWrapper);
                   log.debug("Batch updated {} records with status {}", updated, status);
                });

                log.info("Batch updated {} records with status {}", totalUpdated, status);
                return  dataIds.size();
            } catch (Exception e) {
                log.error("Failed to batch update status {} for {} records", status, dataIds.size(), e);
                // 继续处理其他状态的数据
            }
        }

        return totalUpdated;
    }

    @SwitchClientDS
    @Override
    public List<AysMetaDataAnalysisModel> findByIds(String clientId, Set<String> ids) {
        List<AysMetaDataExtAnalysisEntity> entityList = this.list(
                new QueryWrapper<AysMetaDataExtAnalysisEntity>()
                        .in("data_id", ids)
        );
        List<AysMetaDataAnalysisModel> aysMetaDataAnalysisModels = new ArrayList<>();
        if (CollectionUtil.isEmpty(entityList)) {
            return aysMetaDataAnalysisModels;
        }

        for (AysMetaDataExtAnalysisEntity entity : entityList) {
            AysMetaDataAnalysisModel model = new AysMetaDataAnalysisModel();

            model.setId(entity.getId());
            model.setDataId(entity.getDataId());
            model.setWorkId(entity.getWorkId());
            model.setChannelId(entity.getChannelCode());
            model.setModelType(entity.getModelType());
            model.setClientId(entity.getClientId());
            model.setContentType(entity.getContentType());
            model.setOneId(entity.getOneId());
            model.setCreateTime(entity.getCreateTime());
            model.setDone(entity.getDone());
            model.setDataStatus(entity.getDataStatus());
            model.setTitle(entity.getTitle());
            model.setContent(entity.getContent());
            model.setUserName(entity.getUserName());
            model.setPublishTime(entity.getDataCreateTime());
            model.setModelType(entity.getModelType());
            model.setExtFields(null);
            model.setBizExtAttrs(entity.getAttrs());
            model.setBizExtAttrs2(entity.getAttrs2());
            model.setBizExtAttrs3(entity.getAttrs3());
            model.setCustExtAttrs(entity.getCustExtAttrs());
            model.setVhlExtAttrs(entity.getVhlExtAttrs());
            model.setDealerExtAttrs(entity.getDealerExtAttrs());
            model.setPrdExtAttrs(entity.getPrdExtAttrs());
            model.setHitRuleList(null);

            JSONObject data = JSONUtil.parseObj(entity);
            data.remove("attrs");
            data.remove("attrs2");
            data.remove("attrs3");
            data.remove("custExtAttrs");
            data.remove("vhlExtAttrs");
            data.remove("dealerExtAttrs");
            data.remove("prdExtAttrs");

            model.setData(JSONUtil.toJsonStr(data));
            aysMetaDataAnalysisModels.add(model);
        }
        return aysMetaDataAnalysisModels;
    }

    @SwitchClientDS
    @Override
    public Set<String> findDataIdListByIds(String clientId, Set<String> ids) {
        List<AysMetaDataExtAnalysisEntity> entityList = this.list(
                new QueryWrapper<AysMetaDataExtAnalysisEntity>()
                        .select("id", "data_id")
                        .in("data_id", ids)
        );
        if (CollectionUtil.isEmpty(entityList)) {
            return Set.of();
        }

        return entityList.stream().map(AysMetaDataExtAnalysisEntity::getDataId).collect(Collectors.toSet());
    }


    private String getId(Object model) {
        JSONObject data = JSONUtil.parseObj(model);
        if (ObjUtil.isNotNull(data)) {
            if (data.containsKey(config.getId_attr_name())) {
                return data.getStr(config.getId_attr_name());
            } else {
                log.error("{} 字段数据为空， {}", config.getId_attr_name(), JSONUtil.toJsonStr(model));
            }
        }
        return null;
    }

    @Override
    public Map<String, String> getMD5Values(AysMetaDataAnalysisModel data) {
        Map<String, String> contentMD5 = new HashMap<>();

        if (ObjectUtil.isNotEmpty(data.getData())) {
            Object content = JSONUtil.parseObj(data.getData()).get("content");
            if (ObjectUtil.isNotEmpty(content)) {
                contentMD5.put("content", DigestUtil.md5Hex(StrUtil.trim(String.valueOf(content))));
            }
        }
        return contentMD5;
    }


    @Override
    public void modifyToDataStatusMq(String clientId, Map<String, Integer> dataStatusMap) throws Exception {
        metaDataAnalysisProducer.pushEvent(MessageDTO.builder().source(clientId).data(dataStatusMap).build());
    }

    /**
     * 返回未处理的数据
     * }
     *
     * @param paramIds
     * @return
     */
    @SwitchClientDS
    @Override
    public Set<String> isExitsIds(String clientId, Set<String> paramIds) {
        if (ObjectUtils.isNotEmpty(paramIds)) {
            List<AysMetaDataExtAnalysisEntity> aysMetaDataAnalysisEntities = this.baseMapper.selectList(
                    new QueryWrapper<AysMetaDataExtAnalysisEntity>()
                            .select("data_id")
                            .in("data_id", paramIds));
            if (ObjectUtils.isNotEmpty(aysMetaDataAnalysisEntities)) {
                /*//已处理完成的ids
                Set<String> processedIds = aysMetaDataAnalysisEntities.stream().map(AysMetaDataAnalysisEntity::getNewId).collect(Collectors.toSet());
                return new HashSet<>(CollUtil.intersection(paramIds, processedIds));*/
//                return paramIds;
                return aysMetaDataAnalysisEntities.stream().map(AysMetaDataExtAnalysisEntity::getDataId).collect(Collectors.toSet());

            }
        }
        return new HashSet<>();
    }

    /**
     * 未处理数据集合
     *
     * @param paramIds
     * @return
     */
    @SwitchClientDS
    @Override
    public Set<String> unprocessedIds(String clientId, Set<String> paramIds) {
        if (ObjectUtils.isNotEmpty(paramIds)) {
            List<AysMetaDataExtAnalysisEntity> aysMetaDataAnalysisEntities = this.baseMapper.selectList(
                    new QueryWrapper<AysMetaDataExtAnalysisEntity>()
                            .select("id", "data_id")
                            .in("data_id", paramIds)
                            .eq("done", "0"));
            if (ObjectUtils.isNotEmpty(aysMetaDataAnalysisEntities)) {
                //已处理完成的ids
                Set<String> processedIds = aysMetaDataAnalysisEntities.stream().map(AysMetaDataExtAnalysisEntity::getDataId).collect(Collectors.toSet());
                Collection<String> rs = CollUtil.intersection(paramIds, processedIds);
                if (CollUtil.isNotEmpty(rs)) {
                    return new HashSet<>(rs);
                }
            }
        }
        return null;
    }
}
