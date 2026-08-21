package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.ModOpinionRelationDataService;
import com.voc.service.analysis.core.v2.entity.ModOpinionRelationDataEntity;
import com.voc.service.analysis.core.v2.mapper.ModOpinionRelationDataMapper;
import com.voc.service.analysis.model.OpinionRelationDataParamModel;
import com.voc.service.common.util.IdWorker;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class ModOpinionRelationDataServiceImpl extends ServiceImpl<ModOpinionRelationDataMapper, ModOpinionRelationDataEntity>
        implements ModOpinionRelationDataService {
    private static final Logger logger = LoggerFactory.getLogger(ModOpinionRelationDataServiceImpl.class);

    @Override
    public Boolean saveOpinionRelationData(List<OpinionRelationDataParamModel> paramModelList) {
        logger.info("模型调用标签归一入库参数:{}", JSON.toJSONString(paramModelList));
        List<ModOpinionRelationDataEntity> lmOpinionRelationDataEntities = new ArrayList<>();
        for (OpinionRelationDataParamModel model : paramModelList) {
            ModOpinionRelationDataEntity lmOpinionRelationDataEntity = new ModOpinionRelationDataEntity();
            lmOpinionRelationDataEntity.setOpinion(model.getOpinion());
            lmOpinionRelationDataEntity.setClientId(model.getClientId());
            lmOpinionRelationDataEntity.setRelationLabel(model.getRelationLabel());
            lmOpinionRelationDataEntity.setChannelId(model.getChannelId());
            lmOpinionRelationDataEntity.setClusterId(model.getClusterId());
            lmOpinionRelationDataEntity.setOpinionId(model.getOpinionId());
            lmOpinionRelationDataEntity.setLabelType(model.getLabelType());
            lmOpinionRelationDataEntity.setTopic(model.getTopic());
            lmOpinionRelationDataEntity.setId(IdWorker.getId());
            lmOpinionRelationDataEntity.setCreateTime(LocalDateTime.now());
            lmOpinionRelationDataEntities.add(lmOpinionRelationDataEntity);
        }
        logger.info("标签归一入库:{}", lmOpinionRelationDataEntities.size());
        List<List<ModOpinionRelationDataEntity>> subLists = CollUtil.split(lmOpinionRelationDataEntities, 500);
        for (List<ModOpinionRelationDataEntity> subList : subLists) {
            this.saveBatch(subList);
        }
        return Boolean.TRUE;
    }

    @Override
    public OpinionRelationDataParamModel queryLastData(String clientId) {
        QueryWrapper<ModOpinionRelationDataEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("client_id", clientId);
        wrapper.orderByDesc("create_time");
        wrapper.last("limit 1");
        ModOpinionRelationDataEntity modOpinionRelationDataEntity = this.getOne(wrapper);
        OpinionRelationDataParamModel opinionRelationDataParamModel = new OpinionRelationDataParamModel();
        if (ObjectUtils.isNotEmpty(modOpinionRelationDataEntity)) {
            BeanUtil.copyProperties(modOpinionRelationDataEntity, opinionRelationDataParamModel);
        }
        return opinionRelationDataParamModel;
    }

    @Override
    public Long queryDataCount(String opinionId, String clientId) {

        QueryWrapper<ModOpinionRelationDataEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("opinion_id", opinionId);
        wrapper.eq("client_id", clientId);
        return this.count(wrapper);
    }

}
