package com.voc.service.insights.engine.data.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.data.dao.InsDataSourceDao;
import com.voc.service.insights.engine.data.entity.InsDataSourceEntity;
import com.voc.service.insights.engine.data.mapper.InsDataSourceMapper;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/17 上午10:06
 * @描述:
 **/
@Repository
public class InsDataSourceDaoImpl extends ServiceImpl<InsDataSourceMapper, InsDataSourceEntity> implements InsDataSourceDao {
    private static final Logger log = LoggerFactory.getLogger(InsDataSourceDaoImpl.class);
    @Autowired
    InsDataSourceMapper insDataSourceMapper;



    @Override
    @SwitchClientDS(objectAttribute = "insDataSourceEntity.clientId")
    public void saveDataSource(InsDataSourceEntity insDataSourceEntity) {
        boolean save = this.save(insDataSourceEntity);
        if (save) {
            log.info("保存数据源信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_DATA_SOURCE_ERROR);
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "insDataSourceEntity.clientId")
    public void updateDataSource(InsDataSourceEntity insDataSourceEntity) {
        boolean update = this.updateById(insDataSourceEntity);
        if (update) {
            log.info("更新数据源信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.UPDATE_DATA_SOURCE_ERROR);
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "insDataSourceModel.clientId")
    public List<InsDataSourceEntity> findDataSource(InsDataSourceModel insDataSourceModel) {
        return insDataSourceMapper.findDataSource(insDataSourceModel);
    }

    @Override
    @SwitchClientDS
    public InsDataSourceEntity findDataSourceByName(String clientId, String dataSourceName) {
        return insDataSourceMapper.findDataSourceByName(dataSourceName);
    }

    @Override
    @SwitchClientDS
    public void deleteDataSource(String clientId, String id) {
        insDataSourceMapper.deleteDataSource(id);
    }

    @Override
    @SwitchClientDS
    public InsDataSourceEntity findDataSourceById(String clientId, String id) {
        return insDataSourceMapper.findDataSourceById(id);
    }
}
