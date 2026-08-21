package com.voc.service.insights.engine.data.dao;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.data.entity.InsDataSourceDescEntity;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.InsDataSourceOriginDataVo;
import com.voc.service.insights.engine.vo.InsDataSourceResultDataVo;
import com.voc.service.insights.engine.vo.InsDataSourceResultVo;
import com.voc.service.insights.engine.vo.InsDataSourceSearchCriteriaVo;

import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/17 上午10:50
 * @描述:
 **/
public interface InsDataSourceDetailDao {

    void saveBatchDataSourceDetail(List<InsDataSourceDescEntity> dataSourceEntities, String clientId);

    List<InsDataSourceDescEntity> findDataSourceDetail(String clientId,String dataSourceId);
    String findDataSourceName(String clientId,String dataSourceId,String dataName);

    List<InsDataSourceDescEntity> findDataSourceDetailMaxStatus(String clientId,List<String> batchIds);

    void deleteDataSourceDetail(String clientId,String batchId);

    List<InsDataSourceDescEntity> findDataSourceDetailAll(String clientId, String batchId,String dataSourceId,List<String> status);
    List<InsDataSourceDescEntity> findDataSourceDetails(String clientId, List<String> batchIds, String dataSourceId, List<String> status);
    List<InsDataSourceDescEntity> findFailDataSourceDetails(String clientId, List<String> batchIds, String dataSourceId, List<String> status);

    void updateDataSourceDetail(String clientId, String batchId,String status);
//    void updateDataSourceDetailBatch(String clientId, Set<String> batchId,String status);

    void batchUpdateDataSourceDetail(String clientId, String batchId,String status,List<String> newIds);
    void updateDataSourceDetailStatusAndWorkId(String clientId, String batchId,String status,String workId);

    PageInfo getRawData(InsDataSourceModel insDataSourceModel);

    PageInfo getRawDataResult(InsDataSourceModel insDataSourceModel);

    List<InsDataSourceOriginDataVo> exportRawData(InsDataSourceModel insDataSourceModel);

    List<InsDataSourceOriginDataVo> getFailDataList(InsDataSourceModel insDataSourceModel);

    List<InsDataSourceResultDataVo> exportRawDataResult(InsDataSourceModel insDataSourceModel);

    String batchPushData(InsDataSourceModel insDataSourceModelList);

    InsDataSourceSearchCriteriaVo getDataSourceSearchCriteria(InsDataSourceModel insDataSourceModel);

    List<InsDataSourceDescEntity> findAllDataSourceDetail(String clientId, List<String> dataSourceIds);

    Set<String> findDataSourceWorkIds(String clientId, String batchId, String dataSourceId, List<String> list);

    List<InsDataSourceDescEntity> findDataSourceDetailsByBatchIds(String clientId, List<String> batchIds, String dataSourceId);
    List<InsDataSourceResultVo> getDataResultStatus(InsDataSourceModel insDataSourceModel);
}
