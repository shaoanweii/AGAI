package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IAysErrorPushService;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.api.IAysMetaDataService;
import com.voc.service.analysis.core.v2.entity.AysMetaDataEntity;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.mapper.AysMetaDataMapper;
import com.voc.service.analysis.core.v2.producers.kafka.MetaDataProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.model.AysMetaDataModel;
import com.voc.service.analysis.model.ErrorPushModel;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AysMetaDataService
 * @createTime 2024年03月07日 15:54
 * @Copyright cuick
 */
@Service
public class AysMetaDataServiceImpl extends ServiceImpl<AysMetaDataMapper, AysMetaDataEntity>
        implements IAysMetaDataService {
    private static final Logger logger = LoggerFactory.getLogger(AysMetaDataServiceImpl.class);
    @Autowired
    AysConvertMapperService convertMapperService;
    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;
    @Autowired
    MetaDataProducer metaDataProducer;
    @Autowired
    IAysErrorPushService errorPushService;

    @SwitchClientDS
    @Override
    public void save(final String workId, final String source, final String clientId, final String channelId, final String contentType, List<Object> data,String dataSource)
            throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(clientId), "clientId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(channelId), "channelId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(contentType), "contentType cannot be empty");

        //扩展字段
        Map<String, Object> extAttrMaps = new ConcurrentReferenceHashMap<>();
        extAttrMaps.put("dataSource", dataSource);
//        final String dataJson = JSONUtil.toJsonStr(data);
        final AysMetaDataEntity entity = AysMetaDataEntity.builder()
                .id(IdWorker.getId())
                .workId(workId)
//                    .clientId(clientId)
//                    .channelId(channelId)
//                    .contentType(contentType)
                .tid(ServiceContextHolder.traceId())
                .source(source)
                .operator(null)
                .done("1")
                .createTime(LocalDateTime.now())
                .extFields(dataSource)
                .data(data)
                .build();

        metaDataProducer.pushData(MessageDTO.builder().source(clientId).data(Arrays.asList(entity)).build());
        logger.info("saveBatch success");
    }

    @SwitchClientDS
    @Override
    public void save(final String clientId, final String workId, final String source, List<Object> data,Integer modelType,String dataSource) throws Exception {
        this.save(clientId, workId, source, data, true,modelType ,dataSource);
    }

    @SwitchClientDS
    @Override
    public void save(final String clientId, String workId, String source, List<Object> data, boolean isDone,Integer modelType,String dataSource) throws Exception {
        Assert.isTrue(StrUtil.isNotBlank(workId), "workId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(source), "source cannot be empty");
        Assert.isTrue(CollUtil.isNotEmpty(data), "data cannot be empty");

        //扩展字段
        Map<String, Object> extAttrMaps = new ConcurrentReferenceHashMap<>();
        extAttrMaps.put("dataSource", dataSource);
        try {

            final AysMetaDataEntity entity = AysMetaDataEntity.builder()
                    .id(IdWorker.getId())
                    .workId(workId)
//                    .clientId(clientId)
//                    .channelId(channelId)
//                    .contentType(contentType)
                    .tid(ServiceContextHolder.traceId())
                    .source(source)
                    .operator(null)
                    .modelType(modelType)
                    .done(isDone ? "1" : "0")
                    .createTime(LocalDateTime.now())
                    .extFields(extAttrMaps)
//                    .data(dataJson.replaceAll("\\\\", ""))
                    .data(data)
                    .build();

            try {
                Assert.isTrue(StrUtil.isNotBlank(entity.getId()), "getId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(entity.getWorkId()), "getWorkId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(entity.getSource()), "getSource cannot be empty");

            } catch (IllegalArgumentException e) {
                //异常入库数据纪录
                errorPushService.push(ErrorPushModel
                        .builder()
                        .table("ays_meta_data")
                        .clientId(clientId)
                        .action(IAysErrorPushService.ACTION_ADD)
                        .data(entity)
                        .workId(entity.getWorkId())
                        .tid(ServiceContextHolder.traceId())
                        .build());
                throw e;
            }

            //this.save(entity);
            metaDataProducer.pushData(MessageDTO.builder().source(clientId).data(Arrays.asList(entity)).source(clientId).build());
            logger.info("saveBatch success");
        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            e.printStackTrace();
            throw e;
        }
    }

   /* @Override
    public List<AnalysisDataModel> findIds(Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return null;
        }
        List<AnalysisDataModel> rs = new ArrayList<>();
        List<List<String>> list = CollUtil.split(ids, 400);

        for (List<String> sub : list) {
            LambdaQueryWrapper<AysMetaDataEntity> wrapper = new QueryWrapper<AysMetaDataEntity>()
                    .lambda().in(AysMetaDataEntity::getId, sub);
            List<AysMetaDataEntity> entityList = this.list(wrapper);

            List<String> dataList = entityList.stream().map(AysMetaDataEntity::getData).collect(Collectors.toList());

            for (String dataStr : dataList) {
                List<AnalysisDataModel> modelList = JSONUtil.toList(dataStr, AnalysisDataModel.class);
                rs.addAll(modelList);
            }

        }
        return rs;
    }*/

   /* @Override
    public AysMetaDataModel findIincompleteData() {
        AysMetaDataEntity entity = this.baseMapper.findIincompleteData();
        if (entity == null) {
            logger.trace("no data to copy");
            return null;
        }

        return convertMapperService.converToAysMetaDataEntity(entity);
    }*/

   /* @Override
    public int modifyByWorkId(String workId) {
        return this.baseMapper.modifyByWorkId(workId);
    }*/

   /* @Override
    public String copySourceData(Set<String> metaDataIds) {
        final String workId = "r_".concat(DigestUtil.md5Hex(IdWorker.getId()));

        long count = this.baseMapper.copySourceData(metaDataIds, workId, ServiceContextHolder.traceId());
        logger.info("new workId:{}", workId);
        return workId;
    }*/

    /*@Override
    public void retryingRecords(List<String> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        this.baseMapper.retryingRecords(list);
    }*/
    @SwitchClientDS
    @Override
    public long removeHistoryData(String clientId, int days) {
        return this.baseMapper.removeHistoryData(days);
    }

    @SwitchClientDS
    @Override
    public List<AysMetaDataModel> findByWorkId(String clientId, String workId) {

        List<AysMetaDataEntity> entityList = this.list(
                new QueryWrapper<AysMetaDataEntity>()
                        .eq("work_id", workId)
                        .eq("done", "0")
        );

        return convertMapperService.converToAysMetaDataList(entityList);
    }

    @SwitchClientDS
    @Override
    public int updateStatus(String clientId, String workId) {
        UpdateWrapper<AysMetaDataEntity> wrapper = new UpdateWrapper<>();
        wrapper.in("work_id", workId);
        wrapper.set("done", "1");
        return this.baseMapper.update(null, wrapper);
    }
}
