package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.api.INotificationInsDataStatusService;
import com.voc.service.analysis.clients.IDataSourceServiceClient;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.model.AysBatchPushRecordExceptionModel;
import com.voc.service.analysis.model.AysBatchPushRecordGroupByModel;
import com.voc.service.analysis.model.DateSourceModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Title: notificationInsDataStatusNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: liuhb
 * @Date: 2024/7/23 10:34
 * @Version:1.0
 */
@Service
public class NotificationInsDataStatusServiceImpl implements INotificationInsDataStatusService {

    private static final Logger log = LoggerFactory.getLogger(NotificationInsDataStatusServiceImpl.class);
    @Autowired
    IAysBatchPushRecordV2Service iAysBatchPushRecordV2Service;
    @Autowired
    IDataSourceServiceClient iDataSourceServiceClient;
    @Autowired
    AnalysisConfig config;
    @Autowired
    Executor executor;

    @Override
    public void notificationStatus(String clientId, String workId) {
        try {
            List<AysBatchPushRecordGroupByModel> recordGroupByList = iAysBatchPushRecordV2Service.findGroupByRequestId(clientId, workId);
            log.info("查询分批处理通知数据:{}", JSONUtil.toJsonStr(recordGroupByList));
            if (ObjectUtils.isNotEmpty(recordGroupByList)) {
                List<AysBatchPushRecordGroupByModel> batchPushRecordGroupByModels = recordGroupByList.stream().filter(f -> f.getHandleType() == 0).toList();
                List<AysBatchPushRecordGroupByModel> pushRecordGroupByModels = recordGroupByList.stream().filter(f -> f.getHandleType() == 1).toList();
                if (ObjectUtils.isNotEmpty(pushRecordGroupByModels)) {

                    Map<String, AysBatchPushRecordGroupByModel> aysBatchPushRecordGroupByModelMap = batchPushRecordGroupByModels.stream().collect(Collectors.toMap(AysBatchPushRecordGroupByModel::getReqeutId, Function.identity()));
                    List<DateSourceModel> dateSourceModelList = new ArrayList<>();
                    for (AysBatchPushRecordGroupByModel aysBatchPushRecordGroupByModel : pushRecordGroupByModels) {
                        AysBatchPushRecordGroupByModel model = aysBatchPushRecordGroupByModelMap.get(aysBatchPushRecordGroupByModel.getReqeutId());
                        if (Objects.equals(aysBatchPushRecordGroupByModel.getNum(), model.getNum())) {
                            DateSourceModel dateSourceModel = new DateSourceModel();
                            dateSourceModel.setClientId(clientId);
                            dateSourceModel.setBatchId(aysBatchPushRecordGroupByModel.getReqeutId());
                            dateSourceModel.setStatus("2");
                            dateSourceModelList.add(dateSourceModel);
                        }
                    }
                    if (CollectionUtil.isNotEmpty(dateSourceModelList)) {

                        List<String> idList = dateSourceModelList.stream().map(DateSourceModel::getBatchId).toList();
                        List<AysBatchPushRecordExceptionModel> exceptionRecordList = iAysBatchPushRecordV2Service.findExceptionRecordList(clientId, idList);
                        log.info("通知洞察引擎异常数据信息:{}", exceptionRecordList.size());
                        if (CollectionUtil.isNotEmpty(exceptionRecordList)) {
                            Map<String, List<AysBatchPushRecordExceptionModel>> listMap = exceptionRecordList.stream().collect(Collectors.groupingBy(AysBatchPushRecordExceptionModel::getReqeutId));
                            for (DateSourceModel dateSourceModel : dateSourceModelList) {
                                List<String> errorIdList = new ArrayList<>();
                                if (ObjectUtils.isNotEmpty(listMap) && listMap.containsKey(dateSourceModel.getBatchId())) {
                                    List<AysBatchPushRecordExceptionModel> aysBatchPushRecordExceptionModels = listMap.get(dateSourceModel.getBatchId());
                                    for (AysBatchPushRecordExceptionModel aysBatchPushRecordExceptionModel : aysBatchPushRecordExceptionModels) {
                                        JSONObject jsonObj = JSONUtil.parseObj(aysBatchPushRecordExceptionModel.getMetaData());
                                        errorIdList.add(jsonObj.getStr("new_id"));
                                    }
                                }
                                if (CollectionUtil.isNotEmpty(errorIdList)) {
                                    log.info("通知洞察引擎异常ID集合:{}", exceptionRecordList.size());
                                    dateSourceModel.setErrorIds(errorIdList);
                                }
                            }
                        }

                        long startTime = System.currentTimeMillis();
                        log.debug("异步调用 notificationStatus ");
                        executor.execute(() -> {
                            ServiceContextHolder.setToken(config.getDefaultToken());
                            log.info("通知洞察引擎状态入参:{}", JSONUtil.toJsonStr(dateSourceModelList));
                            Result result = iDataSourceServiceClient.notificationStatus(dateSourceModelList);
                            log.info("通知洞察引擎状态返回:{}", result);
                        });
                        log.debug("异步调用 notificationStatus {}", System.currentTimeMillis() - startTime);
                    }
                }
            }
        } catch (Exception e) {
            log.error("通知洞察引擎失败", e);
        }
    }
}
