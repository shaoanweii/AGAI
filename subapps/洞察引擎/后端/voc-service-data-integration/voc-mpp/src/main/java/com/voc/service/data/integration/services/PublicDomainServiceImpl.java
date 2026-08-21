package com.voc.service.data.integration.services;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.data.integration.api.IPublicDomainService;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.api.model.PublicDomainInfoDataModel;
import com.voc.service.data.integration.entity.PublicDomainInfoDataEntity;
import com.voc.service.data.integration.mapper.PublicDomainInfoDataMapper;
import com.voc.service.data.integration.mpp.cenvert.DataIntegrationConvertMapperService;
import com.voc.service.data.integration.producers.kafka.PublicDomainExecutionResultProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @Title: ChannelServiceImpl
 * @Package: com.voc.service.data.integration.services
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/6 13:26
 * @Version:1.0
 */
@Slf4j
@Service
public class PublicDomainServiceImpl extends ServiceImpl<PublicDomainInfoDataMapper, PublicDomainInfoDataEntity>
        implements IPublicDomainService {
    @Autowired
    DataIntegrationConvertMapperService convertMapperService;

    @Autowired
    PublicDomainExecutionResultProducer publicDomainExecutionResultProducer;


    @Override
    public Set<String> findAllIds() {
        List<String> ids = this.baseMapper.selectObjs(new QueryWrapper<PublicDomainInfoDataEntity>()
                .select("id"));
        log.info("【PublicDomain】本次加载数据量：{}", ids.size());
        return CollUtil.newHashSet(ids);
    }

    @Override
    public List<PublicDomainInfoDataModel> findByIds(final Set<String> ids) {
//        QueryWrapper wrapper = new QueryWrapper();
//        wrapper.eq("type","Channel");
        List<PublicDomainInfoDataEntity> list = this.baseMapper.findByIds(ids);
        if (CollUtil.isNotEmpty(list)) {
            return convertMapperService.convertToPulibcDomainInfoDataModelList(list);
        }
        return List.of();
    }

    @Override
    public int saveList(String clientId, List<ChannelMetaDataModel> list) {
        if (CollUtil.isEmpty(list)) {
            return 0;
        }

        publicDomainExecutionResultProducer.push(MessageDTO.builder().data(list).build());

        return list.size();
    }

    @Override
    public int saveErrorList(String clientId, List<DataIntegrationRecordModel> list) {
        if (CollUtil.isEmpty(list)) {
            return 0;
        }

        publicDomainExecutionResultProducer.pushError(MessageDTO.builder().data(list).build());

        return list.size();
    }

    @Override
    public int saveRecordList(String clientId, List<DataIntegrationRecordModel> list) {
        if (CollUtil.isEmpty(list)) {
            return 0;
        }

        publicDomainExecutionResultProducer.pushRecord(MessageDTO.builder().data(list).build());

        return list.size();
    }

}
