package com.voc.service.insights.engine.dao;

import com.voc.service.insights.engine.entity.InsTagInfoEntity;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 14:11
 * @描述:
 **/
public interface InsTagInfoDao {
    List<InsTagInfoEntity> findTagInfoByType(String type);
}
