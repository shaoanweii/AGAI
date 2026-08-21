package com.voc.service.insights.engine.data.mapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.InsRegulationInfoEntity;
import com.voc.service.insights.engine.entity.InsChannelInfoEntity;
import com.voc.service.insights.engine.entity.InsTableInfoEntity;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import com.voc.service.insights.engine.model.InsTableInfoModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 13:28
 * @描述:
 **/
@Mapper
@Repository
public interface InsRegulationInfoMapper extends BaseMapper<InsRegulationInfoEntity> {

    void deleteRegulationInfo(@Param("id") String id, @Param("delFlag") Integer delFlag, @Param("status")Integer status, @Param("updateTime") LocalDateTime updateTime, @Param("updateUser") String updateUser);
    List<InsRegulationInfoEntity> findRegulationInfoList(@Param("regulationInfoModel") InsRegulationInfoModel regulationInfoModel);

    InsRegulationInfoEntity findRegulationInfoById(@Param("id") String id);

    Integer checkRegulationName(@Param("regulationInfoModel") InsRegulationInfoModel regulationInfoModel);

    String checkRegulationStatusById(@Param("id") String id);

    Set<String> findStaticTableNames(@Param("regulationType")String regulationType);

    Set<String> findTableNames(@Param("tableInfoModel") InsTableInfoModel tableInfoModel);

    List<InsTableInfoEntity> findTableInfoList(@Param("tableNames")Set<String> tableNames, @Param("tableColumns")List<String> tableColumns);

    List<JSONObject> findTableDataInfo(@Param("tableName") String tableName, @Param("columns") List<String> columns);

    List<InsRegulationInfoEntity> findResourceGroupRegulationList(@Param("detailsModel") InsRegulationInfoModel detailsModel);

    List<InsRegulationInfoEntity> findResourceGroupRegulationStatusCount(@Param("detailsModel") InsRegulationInfoModel detailsModel);

    List<InsChannelInfoEntity> findChannelHierarchical(@Param("channelIds") List<String> channelIds);

    String findRegulationName(@Param("name") String name);
}
