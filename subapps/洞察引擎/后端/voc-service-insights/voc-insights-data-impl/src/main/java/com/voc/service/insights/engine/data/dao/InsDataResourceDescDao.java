package com.voc.service.insights.engine.data.dao;

import com.voc.service.insights.engine.model.data.InsDataResourceDescModel;
import com.voc.service.insights.engine.vo.data.ResourceDescDto;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/13 下午3:17
 * @描述:
 **/
public interface InsDataResourceDescDao {

    List<ResourceDescDto> queryCustomByParam(InsDataResourceDescModel model);
    List<ResourceDescDto> findAllDataResourceDesc(InsDataResourceDescModel model);
}
