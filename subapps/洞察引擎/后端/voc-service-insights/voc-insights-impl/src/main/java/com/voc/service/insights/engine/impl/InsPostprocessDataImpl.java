package com.voc.service.insights.engine.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.voc.service.analysis.clients.IAnalysisDataServiceClient;
import com.voc.service.analysis.model.ModifyDataModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.data.IInsCqCaDataSourceService;
import com.voc.service.insights.engine.api.model.InsCqCaLabelCorrectionRecordModel;
import com.voc.service.insights.engine.common.util.QueryUtil;
import com.voc.service.insights.engine.entity.AysPostprocessDataEntity;
import com.voc.service.insights.engine.entity.InsLabelCorrectionInfoEntity;
import com.voc.service.insights.engine.entity.InsLabelCorrectionRecordEntity;
import com.voc.service.insights.engine.mapper.*;
import com.voc.service.insights.engine.model.InsCqCaLabelCorrectionRecordQueryModel;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import com.voc.service.insights.engine.vo.InsCqCaCorrectionGroupDataVo;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class InsPostprocessDataImpl extends ServiceImpl<AysLabelPostprocessDataMapper, AysPostprocessDataEntity> {


    private static final Logger log = LoggerFactory.getLogger(InsPostprocessDataImpl.class);

    @Autowired
    private InsLabelCorrectionInfoMapper insLabelCorrectionInfoMapper;

    @Autowired
    private AysLabelModelResltAnalysisMapper aysModelResltAnalysisMapper;

    @Autowired
    private AysLabelModelResltMapper aysModelResltMapper;

    @Autowired
    private AysLabelMetaDataAnalysisMapper aysMetaDataAnalysisMapper;

    @Autowired
    private IInsCqCaDataSourceService iInsCqCaDataSourceService;
    @Autowired
    private IAnalysisDataServiceClient dataServiceClient;

    private static final int BATCH_SIZE = 200;


    @SwitchClientDS(datasource = "starrock_dndc")
    public PageInfo queryAnalysisData(InsCqCaLabelCorrectionRecordQueryModel model) {
        QueryWrapper<InsLabelCorrectionInfoEntity> dataEntityQueryWrapper = new QueryWrapper<>();
        dataEntityQueryWrapper.eq("correction_record_id", model.getId());
        dataEntityQueryWrapper.select("id");
        List<InsLabelCorrectionInfoEntity> insLabelCorrectionInfoEntities = insLabelCorrectionInfoMapper.selectList(dataEntityQueryWrapper);
        if (CollUtil.isEmpty(insLabelCorrectionInfoEntities)) {
            return new PageInfo<>();
        }
        List<String> idList = insLabelCorrectionInfoEntities.stream().map(InsLabelCorrectionInfoEntity::getId).toList();
        InsCqCaDataQueryModel insCqCaDataQueryModel = new InsCqCaDataQueryModel();
        insCqCaDataQueryModel.setIdList(idList);
        insCqCaDataQueryModel.setPageNum(model.getPageNum());
        insCqCaDataQueryModel.setPageSize(model.getPageSize());
        return iInsCqCaDataSourceService.getResultData(insCqCaDataQueryModel);
    }

    @SwitchClientDS(datasource = "starrock_dndc")
    public List<InsCqCaCorrectionGroupDataVo> queryGroupData(InsCqCaLabelCorrectionRecordQueryModel model) {
        QueryWrapper<InsLabelCorrectionInfoEntity> dataEntityQueryWrapper = new QueryWrapper<>();
        dataEntityQueryWrapper.eq("correction_record_id", model.getId());
        List<InsLabelCorrectionInfoEntity> insLabelCorrectionInfoEntities = insLabelCorrectionInfoMapper.selectList(dataEntityQueryWrapper);
        if (CollUtil.isEmpty(insLabelCorrectionInfoEntities)) {
            return new ArrayList<>();
        }
        List<String> idList = insLabelCorrectionInfoEntities.stream().map(InsLabelCorrectionInfoEntity::getId).toList();
        List<InsCqCaCorrectionGroupDataVo> insCqCaCorrectionGroupDataVos = this.baseMapper.queryGroupData(idList);
        return insCqCaCorrectionGroupDataVos;
    }

    @SwitchClientDS(datasource = "starrock_dndc")
    public List<InsLabelCorrectionRecordEntity> updateAnalysisData(List<InsLabelCorrectionRecordEntity> entityList) {
        log.info("开始处理纠错数据:{}", entityList.size());
        QueryWrapper<InsLabelCorrectionInfoEntity> dataEntityQueryWrapper = new QueryWrapper<>();
        List<String> list = entityList.stream().map(InsLabelCorrectionRecordEntity::getId).toList();
        dataEntityQueryWrapper.in("correction_record_id", list);
        dataEntityQueryWrapper.select("id", "correction_record_id");
        List<InsLabelCorrectionInfoEntity> insLabelCorrectionInfoEntities = insLabelCorrectionInfoMapper.selectList(dataEntityQueryWrapper);
        if (CollUtil.isEmpty(insLabelCorrectionInfoEntities)) {
            log.info("无效数据处理完成:{}", entityList.size());
            return List.of();
        }
        Map<String, List<String>> collect = insLabelCorrectionInfoEntities.stream().collect(Collectors.groupingBy(
                InsLabelCorrectionInfoEntity::getCorrectionRecordId,
                Collectors.mapping(InsLabelCorrectionInfoEntity::getId, Collectors.toList())));
        log.info("开始处理数据:{}", entityList.size());
        // 公共删除操作
//        QueryWrapper<AysPostprocessDataEntity> aysPostprocessDataEntityQueryWrapper = new QueryWrapper<>();
//        aysPostprocessDataEntityQueryWrapper.in("id", entityList);
        try {
            List<String> delIdList = new ArrayList<>();
//            List<String> updateList = new ArrayList<>();
            List<InsLabelCorrectionRecordEntity> allList = new ArrayList<>();
            //errorType 为1
            entityList.stream()
                    .filter(e->ObjectUtils.isNotEmpty(e.getErrorType())&&"1".equals(e.getErrorType()))
                    .filter(e->CollUtil.isNotEmpty(collect.get(e.getId())))
                    .forEach(e->{
                       final List<String> strings = collect.get(e.getId());
                        delIdList.addAll(strings);
                        allList.add(e);
                    });
            log.info("开始禁用结果数据");
            final List<String> delIds = this.modifyResultData(delIdList, "1",null);
            if(ObjectUtils.isEmpty(delIds)){
                log.warn("数据清洗服务未禁用结果数据");
                allList.clear();
            }
            log.info("禁用结果数据结束");

            //errorType 不为1
            log.info("开始修改结果数据");
            entityList.stream()
                    .filter(e->ObjectUtils.isNotEmpty(e.getErrorType())&&!"1".equals(e.getErrorType()))
                    .filter(e->CollUtil.isNotEmpty(collect.get(e.getId())))
                    .forEach(e->{
                       final List<String> strings = collect.get(e.getId());
                       final InsCqCaLabelCorrectionRecordModel bean = JSONUtil.toBean(e.getCorrectionInfo(), InsCqCaLabelCorrectionRecordModel.class);
                        final List<String> updateIds = this.modifyResultData(strings, "0", bean);
                        if(ObjectUtils.isEmpty(updateIds)){
                            log.warn("数据清洗服务未更新结果数据");
                        }else{
                            allList.add(e);
                        }
                    });
            log.info("修改结果数据结束");
//            for (InsLabelCorrectionRecordEntity entity : entityList) {
//                String errorType = entity.getErrorType();
//                List<String> idList = collect.get(entity.getId());
//                if (CollUtil.isEmpty(idList)){
//                    continue;
//                }
//                if (errorType.equals("1")) {
//                    for (String id : idList) {
//                        AysPostprocessDataEntity aysPostprocessDataEntity = new AysPostprocessDataEntity();
//                        aysPostprocessDataEntity.setId(id);
//                        aysPostprocessDataEntity.setAbandon("1");
//                        delIdList.add(aysPostprocessDataEntity);
//                    }
//                } else {
//                    if (StringUtils.isNotBlank(entity.getCorrectionInfo())) {
//                        InsCqCaLabelCorrectionRecordModel bean = JSONUtil.toBean(entity.getCorrectionInfo(), InsCqCaLabelCorrectionRecordModel.class);
//                        for (String id : idList) {
//                            AysPostprocessDataEntity aysPostprocessDataEntity = new AysPostprocessDataEntity();
//                            aysPostprocessDataEntity.setId(id);
//                            if (StringUtils.isNotBlank(bean.getTopicCode())) {
//                                aysPostprocessDataEntity.setTopic(bean.getTopicCode());
//                            }
//                            if (StringUtils.isNotBlank(bean.getBrandCode())) {
//                                aysPostprocessDataEntity.setBrandCode(bean.getBrandCode());
//                            }
//                            if (StringUtils.isNotBlank(bean.getCarSeriesCode())) {
//                                aysPostprocessDataEntity.setCarSeriesCode(bean.getCarSeriesCode());
//                            }
//                            if (StringUtils.isNotBlank(bean.getSentiment())) {
//                                aysPostprocessDataEntity.setSentiment(bean.getSentiment());
//                            }
//
//                            if (StringUtils.isNotBlank(bean.getIntention())) {
//                                aysPostprocessDataEntity.setIntentionType(bean.getIntention());
//                            }
//                            aysPostprocessDataEntity.setAbandon("0");
//                            updateList.add(aysPostprocessDataEntity);
//                        }
//                    }
//                }
//            }
//            log.info("处理要删除数据条数:{}", delIdList.size());
//            log.info("处理要更新数据条数:{}", updateList.size());
//            List<AysPostprocessDataEntity> allList = new ArrayList<>();
//            allList.addAll(delIdList);
//            allList.addAll(updateList);
//            int totalSize = allList.size();
//            long totalCount = 0L;
//            // 循环截取集合，左闭右开 [start, end)
//            for (int start = 0; start < totalSize; start += BATCH_SIZE) {
//                // 计算当前批次结束索引（避免最后一批越界）
//                int end = Math.min(start + BATCH_SIZE, totalSize);
//                List<AysPostprocessDataEntity> batchList = allList.subList(start, end);
//                // 注意：MyBatis的update方法默认返回int（影响行数），需转为Long累加
//                int batchUpdateCount = this.baseMapper.batchUpdateByDtoList(batchList);
//                totalCount += batchUpdateCount;
//            }
//            log.info("处理数据结束:{}", totalCount);
            return allList;
        } catch (Exception e) {
            log.error("操作错误:", e);
            return List.of();
        }
    }



    private List<String> modifyResultData(List<String> dataIds,String status,InsCqCaLabelCorrectionRecordModel recordModel){
        List<List<String>> lists = QueryUtil.splitInParams(1000, dataIds);
        log.info("待修改结果数据总数为:{}条,拆分成{}批进行推送", dataIds, lists.size());
        List<String> ids = new ArrayList<>();
        Integer batchNum = 1;
        for (List<String> list : lists) {
            final String id = IdWorker.getId();
            log.info("开始推送第{}批,请求id:{}",batchNum,id);
            List<ModifyDataModel.ModifyAttrs> attrsList = new ArrayList<>();
            ModifyDataModel.ModifyAttrs resultModel = ModifyDataModel.ModifyAttrs.builder().field("abandon").value(status).build();
            attrsList.add(resultModel);
            if(ObjectUtils.isNotEmpty(recordModel)){
                if (StringUtils.isNotBlank(recordModel.getTopicCode())) {
                    attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("topic").value(recordModel.getTopicCode()).build());
                }
                if(StringUtils.isNotBlank(recordModel.getTopicName())){
                    attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("topic_text").value(recordModel.getTopicName()).build());
                }
                if (StringUtils.isNotBlank(recordModel.getBrandCode())) {
                    attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("brand_code").value(recordModel.getBrandCode()).build());
                }
                if(StringUtils.isNotBlank(recordModel.getBrandName())){
                    attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("brand_name").value(recordModel.getBrandName()).build());
                }
                if (StringUtils.isNotBlank(recordModel.getCarSeriesCode())) {
                    attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("car_series_code").value(!"null".equalsIgnoreCase(recordModel.getCarSeriesCode())?recordModel.getCarSeriesCode():null).build());
                }
                if(StringUtils.isNotBlank(recordModel.getCarSeriesName())){
                    attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("car_series_name").value(!"null".equalsIgnoreCase(recordModel.getCarSeriesName())?recordModel.getCarSeriesName():null).build());
                }
                if (StringUtils.isNotBlank(recordModel.getSentiment())) {
                    attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("sentiment").value(recordModel.getSentiment()).build());
                }

                if (StringUtils.isNotBlank(recordModel.getIntention())) {
                    attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("intention").value(recordModel.getIntention()).build());
                }
            }

            ModifyDataModel build = ModifyDataModel.builder().requestId(id).ids(list).attrs(attrsList).build();
            Result<?> result = dataServiceClient.modifyResultdata(build);
            if(ObjectUtils.isEmpty(result)||!"200".equals(result.getCode())||ObjectUtils.isEmpty(result.getResult())){
                log.error("调用数据清洗服务修改[dataId:{}]结果数据接口异常", dataIds);
                return ids;
            }else{
                log.info("调用数据清洗服务修改结果数据成功");
            }
            batchNum++;
            ids.addAll(list);
        }
        log.info("推送完成，共推送{}批,总计:{}条",batchNum,ids.size());
        return ids;
    }

}
