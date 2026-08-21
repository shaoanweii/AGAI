package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IAysErrorPushService;
import com.voc.service.analysis.api.IAysModelResltService;
import com.voc.service.analysis.core.v2.entity.AysModelResltDataEntity;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.mapper.AysModelResltMapper;
import com.voc.service.analysis.core.v2.producers.kafka.ModelProducer;
import com.voc.service.analysis.core.v2.producers.kafka.ModelResltProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.analysis.model.ErrorPushModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AysMetaDataService
 * @createTime 2024年03月07日 15:54
 * @Copyright cuick
 */
@Service
@Deprecated
public class AysModelResltServiceImpl extends ServiceImpl<AysModelResltMapper, AysModelResltDataEntity>
        implements IAysModelResltService {
    private static final Logger logger = LoggerFactory.getLogger(AysModelResltServiceImpl.class);
    @Autowired
    AysConvertMapperService aysConvertMapperService;
    @Autowired
    ModelResltProducer modelResltProducer;
    @Autowired
    IAysErrorPushService errorPushService;
    @Autowired
    ModelProducer modelProducer;

    /**
     * 保存原始数据时，需要将入参 clientCode， workId 赋值。
     *
     * @param data
     * @return
     * @throws Exception
     */
//    @Transactional
    @Override
    public void saveBatch(String clientId, List<AysProcessDataModel> data) throws Exception {
        Assert.isTrue(StrUtil.isNotEmpty(clientId), "clientId clientId be empty");

        List<AysModelResltDataEntity> saveList = new ArrayList<>();
        for (AysProcessDataModel model : data) {
            AysModelResltDataEntity entity = new AysModelResltDataEntity();
            entity.setId(model.getId());
            entity.setWorkId(model.getWorkId());
            entity.setChannelId(model.getChannelId());
            entity.setContentType(model.getContentType());
            entity.setClientId(model.getClientId());
            entity.setDataId(model.getDataId());
            entity.setOriginalId(model.getId());
            entity.setPublishTime(model.getPublishTime());
            entity.setModelType(model.getModelType());
            entity.setOneId(model.getOneId());
//            model.setNewId(entity.getNewId());
//            model.setId(entity.getId());
//            model.setOriginalId(model.getId());
            Map<String, String> contentMD5 = new HashMap<>();
            contentMD5.put("content", DigestUtil.md5Hex(StrUtil.trim(String.valueOf(model.getData()))));
            entity.setDataMd5(JSONUtil.toJsonStr(contentMD5));
            entity.setData(model.getData());
            if (ObjectUtil.isNotNull(model.getExtFields())) {
                entity.setExtFields(JSONUtil.parseObj(model.getExtFields()));
            }
            if (ObjectUtil.isNotNull(model.getBizExtAttrs())) {
                entity.setBizExtAttrs(JSONUtil.parseObj(model.getBizExtAttrs()));
            }
            if (ObjectUtil.isNotNull(model.getBizExtAttrs2())) {
                entity.setBizExtAttrs2(JSONUtil.parseObj(model.getBizExtAttrs2()));
            }
            if (ObjectUtil.isNotNull(model.getBizExtAttrs3())) {
                entity.setBizExtAttrs3(JSONUtil.parseObj(model.getBizExtAttrs3()));
            }

            try {
                Assert.isTrue(StrUtil.isNotBlank(entity.getDataId()), "getDataId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(entity.getId()), "getId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(entity.getWorkId()), "getWorkId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(entity.getClientId()), "getClientId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(entity.getChannelId()), "getChannelId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(entity.getOriginalId()), "getOriginalId cannot be empty");

            } catch (Exception e) {
                //异常入库数据纪录
                errorPushService.push(ErrorPushModel
                        .builder()
                        .table("ays_api_reslt_data")
                        .clientId(clientId)
                        .action(IAysErrorPushService.ACTION_ADD)
                        .data(entity)
                        .workId(entity.getWorkId())
                        .tid(ServiceContextHolder.traceId())
                        .build());
                logger.error(e.getMessage(), e);
                continue;
            }

            saveList.add(entity);
        }
        logger.info("保存解析后原始数据条数:{}", saveList.size());
        if (CollUtil.isNotEmpty(saveList)) {
//            this.saveBatch(saveList);
            modelResltProducer.pushData(MessageDTO.builder().source(clientId).data(saveList).build());
        }
    }

    @SwitchClientDS
    @Override
    public List<AysProcessDataModel> findByIds(String clientId, Set<String> ids) {
        List<AysModelResltDataEntity> entityList = this.list(
                new QueryWrapper<AysModelResltDataEntity>()
                        .in("data_id", ids)
        );

        if (CollectionUtil.isEmpty(entityList)) {
            return new ArrayList<>();
        }

        entityList = entityList.stream().map(entity -> {
            entity.setData(JSONUtil.parseObj(entity.getData()));
            return entity;
        }).collect(Collectors.toList());
        List<AysProcessDataModel> list = aysConvertMapperService.converToAysProcessDataModelList(entityList);
        return list;
    }

    @SwitchClientDS
    @Override
    public int modifyToDone(@NotNull final String clientId, Set<String> ids) throws Exception {
        if (CollUtil.isEmpty(ids)) {
            return -1;
        }
        //modelProducer.updateEvent(MessageDTO.builder().source(clientId).data(ids).build());
        return ids.size();
    }

    @SwitchClientDS
    @Override
    public int modifyToDoneDB(String clientId, Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        int count = 0;
        List<List<String>> subList = CollUtil.split(ids, 200);
        for (List<String> subs : subList) {
            UpdateWrapper<AysModelResltDataEntity> wrapper = new UpdateWrapper<>();
            wrapper.in("data_id", subs);
            wrapper.set("done", "1");
            count += this.baseMapper.update(null, wrapper);
        }

        return count;
    }


    @SwitchClientDS
    @Override
    public int modifyToException(String clientId, Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        UpdateWrapper<AysModelResltDataEntity> wrapper = new UpdateWrapper<>();
        wrapper.in("data_id", ids);
        wrapper.set("done", "-1");
        return this.baseMapper.update(null, wrapper);
    }

    @SwitchClientDS
    @Override
    public long removeHistoryData(String clientId, int days) {
        return this.baseMapper.removeHistoryData(days);
    }
}
