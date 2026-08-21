package com.voc.service.insights.engine.data.dao;

import com.alibaba.fastjson.JSONObject;
import com.voc.service.insights.engine.data.entity.InsRegulationInfoEntity;
import com.voc.service.insights.engine.entity.InsTableInfoEntity;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import com.voc.service.insights.engine.model.InsTableInfoModel;

import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 13:28
 * @描述:
 **/
public interface InsRegulationInfoDao {

    void saveRegulationInfo(InsRegulationInfoEntity insRegulationInfoEntity);
    void updateRegulationInfo(InsRegulationInfoEntity insRegulationInfoEntity);

    void deleteRegulationInfo(String id,String userName);

    List<InsRegulationInfoEntity> findRegulationInfoList(InsRegulationInfoModel regulationInfoModel);

    InsRegulationInfoEntity findRegulationInfo(InsRegulationInfoModel regulationInfoModel);

    Boolean checkRegulationName(InsRegulationInfoModel regulationInfoModel);

    Boolean checkRegulationStatusById(String id);

    Set<String> findStaticTableNames(String regulationType);

    Set<String> findTableNames(InsTableInfoModel tableInfoModel);
    List<InsTableInfoEntity> findTableInfoList(Set<String> tableNames, List<String> tableColumns);

    List<JSONObject> findTableData(String tableName, List<String> columns);


    List<InsRegulationInfoEntity> findRuleInfoList(InsRegulationInfoModel regulationInfoMode);
    List<InsRegulationInfoEntity> findStandardRuleInfoList(InsRegulationInfoModel regulationInfoMode);

    List<InsRegulationInfoEntity> findResourceGroupRegulationList(InsRegulationInfoModel detailsModel);

    List<InsRegulationInfoEntity> findResourceGroupRegulationStatusCount(InsRegulationInfoModel detailsModel);

    List<String> findChannelHierarchical(String clientId,List<String> channelIds,Boolean isFinal);
    List<String> findChannelCodeHierarchical(String clientId,List<String> channelIds,Boolean isFinal);

    String findRegulationName(String clientId,String name);

}
