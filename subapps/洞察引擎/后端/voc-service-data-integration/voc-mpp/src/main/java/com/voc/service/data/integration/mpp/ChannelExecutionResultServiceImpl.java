package com.voc.service.data.integration.mpp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.common.util.StringUtil;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.data.integration.api.ChannelExecutionResultService;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.api.model.DataRequestModel;
import com.voc.service.data.integration.api.vo.DataValidateResultVo;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.voc.service.data.integration.entity.ChannelExecutionResultEntity;
import com.voc.service.data.integration.entity.ChannelMetaDataEntity;
import com.voc.service.data.integration.enums.ErrorDataMsgEnums;
import com.voc.service.data.integration.mapper.ChannelExecutionResultMapper;
import com.voc.service.data.integration.mapper.ChannelMetaDataMapper;
import com.voc.service.data.integration.mpp.cenvert.DataIntegrationConvertMapperService;
import com.voc.service.data.integration.producers.kafka.ChannelExecutionResultProducer;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Title: DataIntegrationRecordServiceImpl
 * @Package: com.voc.service.data.integration.in.mpp
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/26 10:48
 * @Version:1.0
 */
@Service
public class ChannelExecutionResultServiceImpl extends ServiceImpl<ChannelExecutionResultMapper, ChannelExecutionResultEntity>
        implements ChannelExecutionResultService {
    @Autowired
    DataIntegrationConvertMapperService convertMapperService;
    @Autowired
    ChannelExecutionResultProducer channelExecutionResultProducer;
    @Autowired
    ChannelMetaDataMapper channelMetaDataMapper;
    @Autowired
    DataIntegrationConfig config;
    @CreateCache(area = "VDP", name = ":nissan-dndc-data-integration:exec-result:", expire = 60 * 4, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE)
    private Cache<String, String> execResultCache;


    @SwitchClientDS
    @Override
    public int saveList(String clientId, List<DataIntegrationRecordModel> list) {
        if (CollUtil.isEmpty(list)) {
            return 0;
        }

//        List<ChannelExecutionResultEntity> rs = convertMapperService.cenvertToEntityList(list);
//        this.saveBatch(rs);
        channelExecutionResultProducer.push(MessageDTO.builder().data(list).build());

        this.saveToCache(list.stream().map(DataIntegrationRecordModel::getDataId).collect(Collectors.toSet()));
        return list.size();
    }

    /**
     * 已完成的业务数据ID记录到缓存中
     */
    private void saveToCache(Set<String> values) {
        if (CollUtil.isEmpty(values)) {
            log.warn("values is empty");
            return;
        }
        if (config.isUseCache()) {
            execResultCache.putAll(values.stream().collect(Collectors.toMap(v -> v, v -> "1")));
        }
    }

    /**
     * 过滤掉已经完成的数据
     */
    @Override
    public Set<String> filterData(Set<String> values) {
        if (CollUtil.isEmpty(values)) {
            log.warn("values is empty");
            return values;
        }

        Map<String, String> getResult = execResultCache.getAll(values);
        if (CollUtil.isEmpty(getResult)) {
            return values;
        }

        if (config.isUseCache()) {
            return values.stream().filter(v -> !getResult.containsKey(v)).collect(Collectors.toSet());
        } else {
            return values;
        }
    }

    @Override
    public List<DataIntegrationRecordModel> loadRetryData(String channelType, Date startDate, Date endDate) {
        String startFormat = new SimpleDateFormat("yyyy-MM-dd").format(startDate);
        String endFormat = new SimpleDateFormat("yyyy-MM-dd").format(endDate);
        String start = startFormat + " 00:00:00";
        String end = endFormat + " 23:59:59";

        QueryWrapper<ChannelExecutionResultEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(DataIntegrationRecordEntity::getChannelType, channelType);
        queryWrapper.lambda().eq(ChannelExecutionResultEntity::getStatus, -1);
        queryWrapper.lambda().lt(ChannelExecutionResultEntity::getRetryCount, 3);
        queryWrapper.lambda().isNotNull(ChannelExecutionResultEntity::getDataId);
        queryWrapper.lambda().in(ChannelExecutionResultEntity::getErrorCode,
                Arrays.asList(
                        ErrorDataMsgEnums.PushServiceHasFailed.getCode(),
                        ErrorDataMsgEnums.ChannelHasNotConfiguredRequiredFields.getCode()
                ));
        queryWrapper.lambda().between(ChannelExecutionResultEntity::getCreateTime, start, end);
        queryWrapper.last("limit 100");
//        queryWrapper.lambda().groupBy(DataIntegrationRecordEntity::getChannelType);

        List<ChannelExecutionResultEntity> entityList = this.baseMapper.selectList(queryWrapper);

        if (CollUtil.isEmpty(entityList)) {
            return CollUtil.newArrayList();
        }

        Map<String, List<ChannelExecutionResultEntity>> map = entityList.stream()
                .map(entity -> {
                    JSONObject jsonObj = JSONUtil.parseObj(entity.getData());
                    entity.setData(jsonObj);
                    return entity;
                })
                .collect(Collectors.groupingBy(ChannelExecutionResultEntity::getChannelType));
        return convertMapperService.cenvertToModelList(map.values().stream().findAny().get());
    }

    @Override
    public List<DataIntegrationRecordModel> finshishData(final String channelType, Set<String> ids, Date startDate, Date endDate) {
        String startFormat = new SimpleDateFormat("yyyy-MM-dd").format(startDate);
        String endFormat = new SimpleDateFormat("yyyy-MM-dd").format(endDate);
        String start = startFormat + " 00:00:00";
        String end = endFormat + " 23:59:59";

        QueryWrapper<ChannelExecutionResultEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ChannelExecutionResultEntity::getChannelType, channelType);
        queryWrapper.lambda().in(ChannelExecutionResultEntity::getId, ids);
        queryWrapper.lambda().between(ChannelExecutionResultEntity::getCreateTime, start, end);
        queryWrapper.lambda().le(ChannelExecutionResultEntity::getRetryCount, 3);

        List<ChannelExecutionResultEntity> entityList = this.baseMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(entityList)) {
            return CollUtil.newArrayList();
        }

        return convertMapperService.cenvertToModelList(entityList);
    }

    @Override
    @SwitchClientDS(objectAttribute = "dataRequestModel.clientId")
    public DataValidateResultVo findVerificationResult(DataRequestModel dataRequestModel) {
        Assert.hasLength(dataRequestModel.getClientId(), "clientId cannot be empty");
        Assert.isTrue(ObjectUtils.isNotEmpty(dataRequestModel.getDate()), "date cannot be empty");
        String start = dataRequestModel.getDate() + " 00:00:00";
        String end = dataRequestModel.getDate() + " 23:59:59";

        QueryWrapper<ChannelExecutionResultEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().between(ChannelExecutionResultEntity::getCreateTime, start, end);
        queryWrapper.lambda().groupBy(ChannelExecutionResultEntity::getDataId);
        Long total = this.baseMapper.selectCount(queryWrapper);
        if (ObjectUtils.isEmpty(total) || total == 0) {
            return DataValidateResultVo.builder().build();
        }
        queryWrapper.lambda().eq(ChannelExecutionResultEntity::getStatus, 1);
        Long success = this.baseMapper.selectCount(queryWrapper);
//        final int total = entityList.size();
//        List<ChannelExecutionResultEntity> collect = entityList.stream().filter(e -> e.getStatus() == 1).collect(Collectors.toList());
//        final int success = entityList.size();
        return DataValidateResultVo.builder()
                .totalCount(Math.toIntExact(total))
                .verificationSuccessCount(Math.toIntExact(success))
                .build();
    }

    @Override
//    @SwitchClientDS(objectAttribute = "dataRequestModel.clientId")
    public List<DataValidateResultVo> findVerificationResultByCondition(DataRequestModel dataRequestModel) {
        Assert.hasLength(dataRequestModel.getClientId(), "clientId cannot be empty");
        Assert.isTrue(ObjectUtils.isNotEmpty(dataRequestModel.getDate()), "date cannot be empty");
//        String start = dataRequestModel.getDate() + " 00:00:00";
//        String end = dataRequestModel.getDate() + " 23:59:59";
//        QueryWrapper<ChannelExecutionResultEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().between(ChannelExecutionResultEntity::getCreateTime, start, end);
//        if (ObjectUtils.isEmpty(dataRequestModel.getStatus())) {
//            //获取全部数据:成功+失败+无效
//        } else if (ObjectUtils.isNotEmpty(dataRequestModel.getStatus()) && "0".equalsIgnoreCase(dataRequestModel.getStatus())) {
//            //获取无效数据
//            queryWrapper.lambda().ne(ChannelExecutionResultEntity::getStatus, 1);
//            queryWrapper.lambda().ne(ChannelExecutionResultEntity::getErrorCode, ErrorDataMsgEnums.PushServiceHasFailed.getCode());
//        }
//        queryWrapper.lambda().groupBy(ChannelExecutionResultEntity::getDataId);
//        List<String> executionResult = this.baseMapper.findExecutionResult(start, end, ObjectUtils.isEmpty(dataRequestModel.getStatus()) ? null : dataRequestModel.getStatus(), ErrorDataMsgEnums.PushServiceHasFailed.getCode());
//        List<ChannelExecutionResultEntity> entityList = this.baseMapper.selectList(queryWrapper);
//        if (CollUtil.isEmpty(executionResult)) {
//            return List.of();
//        }
        //根据校验结果，获取原始数据
//        List<String> dataIds = entityList.stream().map(e -> e.getDataId()).collect(Collectors.toList());
        List<ChannelMetaDataEntity> rawData = channelMetaDataMapper.findRawData(dataRequestModel.getDate(), ObjectUtils.isEmpty(dataRequestModel.getStatus()) ? null : dataRequestModel.getStatus(), ErrorDataMsgEnums.PushServiceHasFailed.getCode());
        if (ObjectUtils.isEmpty(rawData)) {
            return List.of();
        }
        //数据重组
        List<DataValidateResultVo> collect = rawData.stream().map(e -> {
            DataValidateResultVo build = DataValidateResultVo.builder().build();
            //转换json对象
            final String json = JSON.toJSONString(e, SerializerFeature.WriteMapNullValue);
            final com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(json);
            com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
            jsonObject.entrySet().stream().forEach(k -> {
                String key = k.getKey();
                Object value = k.getValue();
                if ("extAttrs".equals(key) && ObjectUtils.isNotEmpty(value)) {
//                    final String jsonString = JSON.toJSONString(value);
                    com.alibaba.fastjson.JSONObject extAttrs = JSON.parseObject(String.valueOf(value));
                    extAttrs.entrySet().stream().forEach(ext -> object.put(ext.getKey(), ext.getValue()));
                } else if ("extAttrs2".equals(key) && ObjectUtils.isNotEmpty(value)) {
//                    final String jsonString = JSON.toJSONString(value);
                    com.alibaba.fastjson.JSONObject extAttrs2 = JSON.parseObject(String.valueOf(value));
                    extAttrs2.entrySet().stream().forEach(ext -> object.put(ext.getKey(), ext.getValue()));
                } else if ("extAttrs3".equals(key) && ObjectUtils.isNotEmpty(value)) {
//                    final String jsonString = JSON.toJSONString(value);
                    com.alibaba.fastjson.JSONObject extAttrs3 = JSON.parseObject(String.valueOf(value));
                    extAttrs3.entrySet().stream().forEach(ext -> object.put(ext.getKey(), ext.getValue()));
                } else {
                    object.put(key, value);
                }
            });
            String compress = StringUtil.compress(String.valueOf(object));
            build.setData(compress);
            return build;
        }).collect(Collectors.toList());

        return collect;
    }

    @SwitchClientDS("dataRequestModel.clientId")
    @Override
    public long removeHistoryData(String clientId, int days) {
        //查询
        Long count = this.baseMapper.checkData(days);
        if (count <= 0) {
            return 0;
        }
        //备份
        this.baseMapper.moveToHistoryData(days);

        long checkLen = this.baseMapper.checkHistoryData();
        if (checkLen > 0) {
            this.baseMapper.deleteHistoryData(days);
        }

        return checkLen;
    }
}
