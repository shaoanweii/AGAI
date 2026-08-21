package com.voc.service.data.integration.api;

import com.voc.service.data.integration.api.model.ChannelInfoDataModel;

import java.util.List;

/**
 * @Title: IChannelService
 * @Package: com.voc.service.data.integration.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/6 13:27
 * @Version:1.0
 */
public interface IChannelService {
    List<ChannelInfoDataModel> findAll();
}
