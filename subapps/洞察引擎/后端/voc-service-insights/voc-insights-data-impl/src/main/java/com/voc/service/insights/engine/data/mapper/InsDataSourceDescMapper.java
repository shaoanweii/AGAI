package com.voc.service.insights.engine.data.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.InsDataSourceDescEntity;
import com.voc.service.insights.engine.model.data.DataProcessingTaskQuery;
import com.voc.service.insights.engine.vo.DataProcessingTaskVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 数据集详情(InsDataDesc)表数据库访问层
 *
 * @author leiww
 * @since 2024-02-27 16:48:54
 */
@Mapper
@Repository
public interface InsDataSourceDescMapper extends BaseMapper<InsDataSourceDescEntity> {

    List<InsDataSourceDescEntity> findDataSourceDetail(@Param("dataSourceId") String dataSourceId);
    List<InsDataSourceDescEntity> findDataSourceDetailByBatchIds(@Param("batchIds") List<String> batchIds, @Param("dataSourceId") String dataSourceId);
    List<InsDataSourceDescEntity> findDataSourceDetailMaxStatus(@Param("batchIds")List<String> batchIds);

    void deleteDataSourceDetail(@Param("batchId") String batchId);

    List<InsDataSourceDescEntity> findDataSourceDetailAll(@Param("dataSourceId") String dataSourceId, @Param("batchId") String batchId, @Param("status") List<String> status);

    void updateDataSourceDetail(@Param("batchId")String batchId, @Param("status")String status);

    void updateDataSourceDetailStatusAndWorkId(@Param("batchId")String batchId, @Param("status")String status, @Param("workId")String workId);

    void batchUpdateDataSourceDetailStatus(@Param("batchId")String batchId, @Param("status")String status,@Param("newIds")List<String> newIds);

    void insertBatchDataSourceDetail(@Param("dataSourceEntities") List<InsDataSourceDescEntity> dataSourceEntities);

    List<InsDataSourceDescEntity> findDataSourceDetails(@Param("dataSourceId")String dataSourceId, @Param("batchIds")List<String> batchIds, @Param("status")List<String> status);
    List<InsDataSourceDescEntity> findFailDataSourceDetails(@Param("dataSourceId")String dataSourceId, @Param("batchIds")List<String> batchIds, @Param("status")List<String> status);

    List<InsDataSourceDescEntity> findAllDataSourceDetail(@Param("dataSourceIds")List<String> dataSourceIds);

    Set<String> findDataSourceWorkIds(@Param("dataSourceId") String dataSourceId, @Param("batchId") String batchId, @Param("status") List<String> status);

    String findDataSourceName(@Param("dataSourceId")String dataSourceId,@Param("dataName")String dataName);

    List<DataProcessingTaskVo> findDataProcessingTasks(@Param("query") DataProcessingTaskQuery query);
}
