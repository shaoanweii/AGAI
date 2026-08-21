package com.voc.service.insights.engine.dao.impl;

import com.voc.service.insights.engine.dao.InsTagInfoDao;
import com.voc.service.insights.engine.entity.InsTagInfoEntity;
import com.voc.service.insights.engine.mapper.InsTagInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 14:11
 * @描述:
 **/
@Repository
public class InsTagInfoDaoImpl implements InsTagInfoDao {
    @Autowired
    InsTagInfoMapper tagInfoMapper;

    @Override
    public List<InsTagInfoEntity> findTagInfoByType(String type) {
        return tagInfoMapper.findTagInfoByType(type);
    }
}
