package com.voc.service.data.integration.services;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.data.integration.api.IChannelService;
import com.voc.service.data.integration.api.model.ChannelInfoDataModel;
import com.voc.service.data.integration.entity.ChannelInfoDataEntity;
import com.voc.service.data.integration.mapper.ChannelInfoDataMapper;
import com.voc.service.data.integration.mpp.cenvert.DataIntegrationConvertMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: ChannelServiceImpl
 * @Package: com.voc.service.data.integration.services
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/6 13:26
 * @Version:1.0
 */
@Service
public class ChannelServiceImpl extends ServiceImpl<ChannelInfoDataMapper, ChannelInfoDataEntity>
        implements IChannelService {
    @Autowired
    DataIntegrationConvertMapperService convertMapperService;

    @Override
    public List<ChannelInfoDataModel> findAll() {
//        QueryWrapper wrapper = new QueryWrapper();
//        wrapper.eq("type","Channel");
        List<ChannelInfoDataEntity> list = this.baseMapper.findAll();
        if(CollUtil.isNotEmpty(list)){
            return convertMapperService.convertToChannelInfoDataModelList(list);
        }
        return List.of();
    }
}
