package com.voc.service.insights.engine.data.dao;

import com.voc.service.insights.engine.data.entity.InsDataSourceTemplateEntity;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/14 上午10:07
 * @描述:
 **/
public interface InsDataSourceTemplateDao {
    void saveBatchDataSource(List<InsDataSourceTemplateEntity> dataSourceEntities, String clientId);
    void insertDataSource(InsDataSourceTemplateEntity dataSourceTemplateEntity, String clientId);
    void updateBatchDataSource(InsDataSourceTemplateEntity dataSourceTemplateEntity, String clientId);
    List<InsDataSourceTemplateEntity> findByBatchId(InsDataSourceModel insDataSourceModel);

    void deleteDataSourceTemplate(InsDataSourceModel insDataSourceModel);
}
