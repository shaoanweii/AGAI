package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.api.IAysErrorPushService;
import com.voc.service.analysis.core.v2.entity.AysBatchPushRecordV2Entity;
import com.voc.service.analysis.core.v2.mapper.AysBatchPushRecordV2Mapper;
import com.voc.service.analysis.core.v2.producers.kafka.AnalysisBatchPushRecordProducer;
import com.voc.service.analysis.core.v2.producers.kafka.BatchPushProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.dto.MessageExt;
import com.voc.service.analysis.model.AysBatchPushRecordExceptionModel;
import com.voc.service.analysis.model.AysBatchPushRecordGroupByModel;
import com.voc.service.analysis.model.ErrorPushModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Title: AysBatchPushRecordV2ServiceImpl
 * @Package: com.voc.service.analysis.core.v2.impl
 * @Description:
 * @Author: cuick
 * @Date: 2024/7/22 15:25
 * @Version:1.0
 */
@Service
public class AysBatchPushRecordV2ServiceImpl
        extends ServiceImpl<AysBatchPushRecordV2Mapper, AysBatchPushRecordV2Entity>
        implements IAysBatchPushRecordV2Service {
    @Autowired
    BatchPushProducer batchPushProducer;
    @Autowired
    IAysErrorPushService errorPushService;
    @Autowired
    AnalysisBatchPushRecordProducer batchPushRecordProducer;

    @SwitchClientDS
    @Override
    public void save(String clientId, String workId, String reqeustId, Set<String> ids, Integer modelType) throws Exception {
        Assert.isTrue(StrUtil.isNotEmpty(clientId), "clientId  be empty");
        Assert.isTrue(StrUtil.isNotEmpty(workId), "reqeustId  be empty");
        Assert.isTrue(CollUtil.isNotEmpty(ids), "ids be empty");

        final List<AysBatchPushRecordV2Entity> list = ids.stream()
                .map(id ->
                        {
                            AysBatchPushRecordV2Entity entity = AysBatchPushRecordV2Entity.builder()
                                    .id(id)
                                    .status("0")
                                    .source(null)
                                    .reqeutId(StrUtil.isBlank(reqeustId) ? "-1" : reqeustId)
                                    .workId(workId)
                                    .modelType(modelType)
                                    .tid(ServiceContextHolder.traceId())
                                    .build();

                            try {
                                Assert.isTrue(StrUtil.isNotBlank(entity.getId()), "getId cannot be empty");
                                Assert.isTrue(StrUtil.isNotBlank(entity.getWorkId()), "getWorkId cannot be empty");
                                Assert.isTrue(StrUtil.isNotBlank(entity.getReqeutId()), "getReqeutId cannot be empty");
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                //异常入库数据纪录
                                try {
                                    errorPushService.push(ErrorPushModel
                                            .builder()
                                            .table("ays_batch_push_record")
                                            .clientId(clientId)
                                            .action(IAysErrorPushService.ACTION_ADD)
                                            .data(entity)
                                            .workId(entity.getWorkId())
                                            .tid(ServiceContextHolder.traceId())
                                            .build());
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                            }

                            return entity;
                        }
                )
                .collect(Collectors.toList());
        /*List<List<AysBatchPushRecordV2Entity>> subLists = CollUtil.split(list, 200);
        for (List<AysBatchPushRecordV2Entity> subList : subLists) {
            this.saveBatch(subList);
        }*/
        this.saveMq(clientId, list);
    }

    private void saveMq(String clientId, List<AysBatchPushRecordV2Entity> list) throws Exception {
        batchPushRecordProducer.pushData(MessageDTO.builder().source(clientId).data(list).build());
    }

    /*@SwitchClientDS
    @Override
    public String findByReqeustId(String clientId, String reqeustId) {
        Assert.isTrue(StrUtil.isNotEmpty(reqeustId), "reqeustId  be empty");
        AysBatchPushRecordV2Entity entity = this.lambdaQuery()
                .eq(AysBatchPushRecordV2Entity::getReqeutId, reqeustId)
                .last("limit 1")
                .one();
        if (ObjectUtil.isNull(entity)) {
            return null;
        }
        return entity.getWorkId();
    }*/

    /*@SwitchClientDS
    @Override
    public Set<String> findByNewId(String clientId, List<String> newIdList) {
        Assert.isTrue(CollUtil.isNotEmpty(newIdList), "newIdList  be empty");
        List<AysBatchPushRecordV2Entity> entityList = this.lambdaQuery()
                .in(AysBatchPushRecordV2Entity::getId, newIdList).list();
        if (ObjectUtil.isNull(entityList)) {
            return null;
        }
        return entityList.stream().map(AysBatchPushRecordV2Entity::getWorkId).collect(Collectors.toSet());
    }*/

    //    @SwitchClientDS
    @Override
    public boolean modifyStatus(String clientId, final Set<String> ids, final String status, final String source) throws Exception {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        Assert.isTrue(StrUtil.isNotEmpty(status), "status  be empty");
        Assert.isTrue(StrUtil.isNotEmpty(source), "source  be empty");

        /*int count = 0;
        List<List<String>> subList = CollUtil.split(ids, 200);
        for (List<String> subs : subList) {
            UpdateWrapper<AysBatchPushRecordV2Entity> wrapper = new UpdateWrapper<>();
            wrapper.in("id", subs);
            wrapper.set("status", status);
            wrapper.set("source", source);
            wrapper.set("update_time", LocalDateTime.now());

            count += this.baseMapper.update(null, wrapper);
        }*/

        MessageExt statusExt = MessageExt.builder().key("status").value(status).build();
        MessageExt sourceExt = MessageExt.builder().key("source").value(source).build();
//        batchPushProducer.pushEvent(MessageDTO.builder().data(ids).source(clientId).ext(Set.of(statusExt, sourceExt)).build());

        return true;
    }

    @SwitchClientDS
    @Override
    public long modifyStatusDB(String clientId, final Set<String> ids, final String status, final String source) throws Exception {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        Assert.isTrue(StrUtil.isNotEmpty(status), "status  be empty");
        Assert.isTrue(StrUtil.isNotEmpty(source), "source  be empty");

        int count = 0;
        List<List<String>> subList = CollUtil.split(ids, 200);
        for (List<String> subs : subList) {
            UpdateWrapper<AysBatchPushRecordV2Entity> wrapper = new UpdateWrapper<>();
            wrapper.in("id", subs);
            wrapper.set("status", status);
            wrapper.set("source", source);
            wrapper.set("update_time", LocalDateTime.now());

            count += this.baseMapper.update(null, wrapper);
        }
        return count;
    }

    @SwitchClientDS
    @Override
    public List<AysBatchPushRecordGroupByModel> findGroupByRequestId(String clientId, String workId) {
        return this.baseMapper.findGroupByRequestId(workId);
    }

    @SwitchClientDS
    @Override
    public List<AysBatchPushRecordExceptionModel> findExceptionRecordList(String clientId, List<String> ids) {
        return this.baseMapper.findExceptionRecordList(ids);
    }

    @SwitchClientDS
    @Override
    public Set<String> isExitsIds(String clientId, Set<String> paramIds) {
        /*if (ObjectUtils.isNotEmpty(paramIds)) {
            List<AysBatchPushRecordV2Entity> entityList = baseMapper.selectList(
                    new QueryWrapper<AysBatchPushRecordV2Entity>()
                            .in("id", paramIds));
            if (ObjectUtils.isNotEmpty(entityList) && entityList.size() == paramIds.size()) {
                //已处理完成的ids
                *//*Set<String> processedIds = aysMetaDataAnalysisEntities.stream().map(AysPreprocessDataEntity::getNewId).collect(Collectors.toSet());
                return new HashSet<>(CollUtil.union(paramIds, processedIds));*//*
                return paramIds;
            }
        }*/
        return new HashSet<>();
    }
}
