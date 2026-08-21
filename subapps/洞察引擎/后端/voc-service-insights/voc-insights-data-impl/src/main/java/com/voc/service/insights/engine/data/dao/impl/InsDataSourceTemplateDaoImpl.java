package com.voc.service.insights.engine.data.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.data.dao.InsDataSourceTemplateDao;
import com.voc.service.insights.engine.data.entity.InsDataSourceTemplateEntity;
import com.voc.service.insights.engine.data.mapper.InsDataSourceTemplateMapper;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/14 上午10:07
 * @描述:
 **/
@Repository
public class InsDataSourceTemplateDaoImpl extends ServiceImpl<InsDataSourceTemplateMapper, InsDataSourceTemplateEntity> implements InsDataSourceTemplateDao {
    private static final Logger log = LoggerFactory.getLogger(InsDataSourceTemplateDaoImpl.class);
    @Autowired
    InsDataSourceTemplateMapper dataSourceTemplateMapper;


    @Override
    @SwitchClientDS
    public void saveBatchDataSource(List<InsDataSourceTemplateEntity> dataSourceEntities, String clientId) {
        try {
            dataSourceTemplateMapper.insertBatchDataSource(dataSourceEntities);
            log.info("批量保存数据源临时表数据成功");
        }catch (Exception e){
            log.error("异常:{}",e);
            throw new BussinessException(InsCommonErrorEnum.SAVE_REGULATION_ERROR);
        }
//        boolean saveBatch = this.saveBatch(dataSourceEntities);
//        if (saveBatch) {
//            log.info("批量保存数据源临时表数据成功");
//        } else {
//            throw new BussinessException(InsCommonErrorEnum.SAVE_REGULATION_ERROR);
//        }
    }

    @Override
    @SwitchClientDS
    public void insertDataSource(InsDataSourceTemplateEntity dataSourceTemplateEntity, String clientId) {
        boolean save = this.save(dataSourceTemplateEntity);
        if (save) {
            log.info("保存数据源临时表数据成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_REGULATION_ERROR);
        }
    }

    @Override
    @SwitchClientDS
    public void updateBatchDataSource(InsDataSourceTemplateEntity dataSourceTemplateEntity, String clientId) {
        dataSourceTemplateMapper.updateBatchDataSource(dataSourceTemplateEntity);
    }

    @Override
    @SwitchClientDS(objectAttribute = "insDataSourceModel.clientId")
    public List<InsDataSourceTemplateEntity> findByBatchId(InsDataSourceModel insDataSourceModel) {
        return dataSourceTemplateMapper.findByBatchId(insDataSourceModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "insDataSourceModel.clientId")
    public void deleteDataSourceTemplate(InsDataSourceModel insDataSourceModel) {
        dataSourceTemplateMapper.deleteDataSourceTemplate(insDataSourceModel);
    }
}
