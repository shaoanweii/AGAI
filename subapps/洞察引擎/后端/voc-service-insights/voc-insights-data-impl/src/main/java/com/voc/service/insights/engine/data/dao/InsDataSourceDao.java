package com.voc.service.insights.engine.data.dao;

import com.voc.service.insights.engine.data.entity.InsDataSourceEntity;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/17 上午10:06
 * @描述:
 **/
public interface InsDataSourceDao {

    void saveDataSource(InsDataSourceEntity insDataSourceEntity);

    void updateDataSource(InsDataSourceEntity insDataSourceEntity);

    List<InsDataSourceEntity> findDataSource(InsDataSourceModel insDataSourceModel);

    InsDataSourceEntity findDataSourceByName(String clientId, String dataSourceName);

    void deleteDataSource(String clientId, String id);
    InsDataSourceEntity findDataSourceById(String clientId,String id);
}
