package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IAysBatchPushRecordService;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.api.IAysMetaDataService;
import com.voc.service.analysis.core.v2.entity.AysBatchPushRecordEntity;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.mapper.AysBatchPushRecordMapper;
import com.voc.service.analysis.model.AiBatchPushModel;
import com.voc.service.analysis.model.AysBatchPushRecordModel;
import com.voc.service.analysis.model.AysMetaDataModel;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: AysBatchPushRecordServiceImpl
 * @Package: com.voc.service.analysis.core.v2.impl
 * @Description:
 * @Author: cuick
 * @Date: 2024/6/11 10:24
 * @Version:1.0
 */
@Service
public class AysBatchPushRecordServiceImpl
        extends ServiceImpl<AysBatchPushRecordMapper, AysBatchPushRecordEntity>
        implements IAysBatchPushRecordService {
    private static final Logger log = LoggerFactory.getLogger(AysBatchPushRecordServiceImpl.class);
    @Autowired
    IAysMetaDataService metaDataService;
    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;
    @Autowired
    AysConvertMapperService convertMapperService;
    /*@SwitchClientDS
    @Override
    public void cumulativePreFinishedSum(final String clientId,String workId, Integer currentBatchTotal){
        this.baseMapper.cumulativePreFinishedSum(workId, currentBatchTotal);
    }
    @SwitchClientDS
    @Override
    public void cumulativePostFinishedSum(final String clientId,String workId, Integer currentBatchTotal) {
        this.baseMapper.cumulativePostFinishedSum(workId, currentBatchTotal);
    }
    @SwitchClientDS
    @Override
    public void cumulativeModelAnalysisSum(final String clientId,String workId, Integer currentBatchTotal) {
        this.baseMapper.cumulativeModelAnalysisSum(workId, currentBatchTotal);
    }*/
    /*@SwitchClientDS
    @Override
    public boolean modifyProcessStatus(final String clientId,String workId) {
        UpdateWrapper<AysBatchPushRecordEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(AysBatchPushRecordEntity::getWorkId, workId);
        wrapper.set("process_status", "1");
        int update = this.baseMapper.update(null, wrapper);

        return update > 0;
    }*/
    /*@SwitchClientDS
    @Override
    public AysBatchPushRecordModel findByWorkId(final String clientId,String workId) {
        QueryWrapper<AysBatchPushRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AysBatchPushRecordEntity::getWorkId, workId);
        queryWrapper.last("limit 1");

        AysBatchPushRecordEntity aysBatchPushRecordEntity = this.baseMapper.selectOne(queryWrapper);
        AysBatchPushRecordModel model = convertMapperService.converToAiBatchPushModel(aysBatchPushRecordEntity);

        return model;
    }*/
    /*@SwitchClientDS
    @Override
    public String findByReqeustId(final String clientId,AiBatchPushModel param) {
        AysBatchPushRecordEntity entity = AysBatchPushRecordEntity.builder().reqeutId(param.getRequestId()).build();
        return this.baseMapper.selectByReqeustId(entity);
    }*/
    /*@SwitchClientDS
    @Override
    public void save(final String clientId,String workId, AiBatchPushModel param) {
        Assert.isTrue(CollUtil.isNotEmpty(param.getData()), "getData cannot be empty");

        this.save(AysBatchPushRecordEntity.builder()
                .id(IdWorker.getId())
                .reqeutId(param.getRequestId())
                .workId(workId)
                .batchTotal(param.getTotal())
                .receivedTotal(0)
                .preFinishedDataSize(0)
                .postFinishedDataSize(0)
                .modelMissAnalysisSize(0)
                .processStatus(0)
                .tid(ServiceContextHolder.traceId())
                .build());
    }*/
   /* @SwitchClientDS
    @Override
    public void cumulativeSum(final String clientId,String workId, Integer currentBatchTotal) {
        this.baseMapper.cumulativeSum(workId, currentBatchTotal);
    }*/
    /*@SwitchClientDS
    @Override
    public void batchAnalysisMeateData(final String clientId,final String workId) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(workId), "workId cannot be empty");
        List<AysMetaDataModel> metaDataList = metaDataService.findByWorkId(clientId,workId);

        if (CollUtil.isEmpty(metaDataList)) {
            log.error("metaDataList cannot be empty");
            return;
        }

        List<Object> dataList = new ArrayList<>();
        metaDataList.stream().map(AysMetaDataModel::getData)
                .forEach(content -> {
                    JSONArray objects = JSONUtil.parseArray(content);
                    List<Object> list = JSONUtil.toList(objects, Object.class);
                    dataList.addAll(list);
                });
        //保存解析数据
        metaDataAnalysisService.saveBatch(clientId,workId, dataList);
        //更新原始数据ID
        metaDataService.updateStatus(clientId,workId);
    }*/
}
