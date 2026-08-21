package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IAysErrorPushService;
import com.voc.service.analysis.api.IAysPreprocessDataService;
import com.voc.service.analysis.core.v2.entity.AysPreprocessDataEntity;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.mapper.AysPreprocessDataMapper;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessPreRulesProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.model.AysPreprocessDataModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.analysis.model.ErrorPushModel;
import com.voc.service.analysis.model.RuleModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AysMetaDataService
 * @createTime 2024年03月07日 15:54
 * @Copyright cuick
 */
@DS("voc")
@Service
public class AysPreprocessDataServiceImpl extends ServiceImpl<AysPreprocessDataMapper, AysPreprocessDataEntity>
        implements IAysPreprocessDataService {
    private static final Logger log = LoggerFactory.getLogger(AysPreprocessDataServiceImpl.class);
    @Autowired
    AysConvertMapperService aysConvertMapperService;
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    ProcessPreRulesProducer processPreRulesProducer;
    @Autowired
    IAysErrorPushService errorPushService;

    @SwitchClientDS
    @Override
    public Set<String> saveBatch(String clientId, List<AysProcessDataModel> data) throws Exception {
        Assert.isTrue(StrUtil.isNotEmpty(clientId), "clientId clientId be empty");
        List<AysPreprocessDataEntity> saveList = new ArrayList<>();
        try {
            for (AysProcessDataModel model : data) {
                AysPreprocessDataEntity entity = new AysPreprocessDataEntity();
                BeanUtil.copyProperties(model, entity);
                entity.setPublishTime(model.getPublishTime());
                Map<String, String> contentMD5 = new HashMap<>();
                contentMD5.put("content", DigestUtil.md5Hex(StrUtil.trim(String.valueOf(entity.getData()))));
                entity.setDataMd5(JSONUtil.toJsonStr(contentMD5));
                entity.setDone("0");
                entity.setCreateTime(LocalDateTime.now());
                if (ObjectUtil.isNotNull(model.getExtFields())) {
                    entity.setExtFields(JSONUtil.parseObj(model.getExtFields()));
                }

                if (CollUtil.isNotEmpty(model.getHitRuleList())) {
                    final String hitRules = JSONUtil.toJsonStr(JSONUtil.createObj().putOpt(
                            "rule_ids", model.getHitRuleList().stream().map(RuleModel::getId).collect(Collectors.toSet())));
                    entity.setHitRules(hitRules);
                    final String dataStr = String.valueOf(entity.getData());
                    JSONObject jsonObject = JSON.parseObject(dataStr);
                    if (StrUtil.isBlank(jsonObject.getString("content"))) {
                        log.info("规则清洗以后原文为空直接过滤:{}", entity.getDataId());
                        entity.setAbandon("1");
                        entity.setDone("1");
                    }
                }
                try {
                    Assert.isTrue(StrUtil.isNotBlank(entity.getDataId()), "getDataId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getId()), "getId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getWorkId()), "getWorkId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getClientId()), "getClientId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getChannelId()), "getChannelId cannot be empty");

                } catch (IllegalArgumentException e) {
                    //异常入库数据纪录
                    errorPushService.push(ErrorPushModel
                            .builder()
                            .table("voc_anal_flow_pre_rules_result_data_full")
                            .clientId(clientId)
                            .action(IAysErrorPushService.ACTION_ADD)
                            .data(entity)
                            .workId(entity.getWorkId())
                            .tid(ServiceContextHolder.traceId())
                            .build());
                    log.error(e.getMessage(), e);
                    continue;
                }

                saveList.add(entity);
            }

            log.info("本次解析后需保存{}条数据", saveList.size());
            //保存到mq队列，将会自动同步到数据库
            processPreRulesProducer.pushData(MessageDTO.builder().source(clientId).data(saveList).build());

            log.info("saveBatch success");
        } catch (Exception e) {
            throw e;
        }
        return saveList.stream().map(AysPreprocessDataEntity::getDataId).collect(Collectors.toSet());
    }

    /**
     * 修改状态为已完成
     *
     * @param ids
     */
    @SwitchClientDS
    @Override
    public int modifyToDone(String clientId, Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        List<List<String>> subList = CollUtil.split(ids, 200);
        int count = 0;
        for (List<String> subs : subList) {
            UpdateWrapper<AysPreprocessDataEntity> wrapper = new UpdateWrapper<>();
            wrapper.in("data_id", subs);
            wrapper.set("done", "1");
            count += this.baseMapper.update(null, wrapper);
        }

        return count;
    }


    @SwitchClientDS
    @Override
    public List<AysPreprocessDataModel> findByIds(String clientId, Set<String> ids) {
        List<AysPreprocessDataEntity> entityList = this.list(
                new QueryWrapper<AysPreprocessDataEntity>()
                        .in("data_id", ids)
        );

        if (CollectionUtil.isEmpty(entityList)) {
            return new ArrayList<>();
        }

        entityList = entityList.stream().map(entity -> {
            entity.setData(JSONUtil.parseObj(entity.getData()));
            return entity;
        }).collect(Collectors.toList());
        List<AysPreprocessDataModel> list = aysConvertMapperService.converToAysPreprocessDataModelList(entityList);
        return list;
    }

    /*@SwitchClientDS
    @Override
    public Set<String> findIincompleteData(String clientId) {
        return this.baseMapper.findIincompleteData();
    }*/
    @SwitchClientDS
    @Override
    public String findWorkId(String clientId) {
        return this.baseMapper.findWorkId();
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
            List<AysPreprocessDataEntity> aysMetaDataAnalysisEntities = baseMapper.selectList(
                    new QueryWrapper<AysPreprocessDataEntity>()
                            .select("data_id")
                            .in("data_id", paramIds));
            if (ObjectUtils.isNotEmpty(aysMetaDataAnalysisEntities) ) {
                return aysMetaDataAnalysisEntities.stream().map(AysPreprocessDataEntity::getDataId).collect(Collectors.toSet());
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
            List<AysPreprocessDataEntity> aysMetaDataAnalysisEntities = this.baseMapper.selectList(
                    new QueryWrapper<AysPreprocessDataEntity>()
                            .select("id","data_id")
                            .in("data_id", paramIds)
//                            .eq("done", "0")
                            .eq("abandon", "0"));
            if (ObjectUtils.isNotEmpty(aysMetaDataAnalysisEntities)) {
                //已处理完成的ids
                Set<String> processedIds = aysMetaDataAnalysisEntities.stream().map(AysPreprocessDataEntity::getDataId).collect(Collectors.toSet());
                Collection<String> rs = CollUtil.intersection(paramIds, processedIds);
                if (CollUtil.isNotEmpty(rs)) {
                    return new HashSet<>(rs);
                }
            }
        }
        return null;
    }
}
