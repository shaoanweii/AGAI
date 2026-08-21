package com.voc.service.insights.engine.data.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.InsDataSourceEntity;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据源集(InsDataSource)表数据库访问层
 *
 * @author leiww
 * @since 2024-02-27 15:31:45
 */
@Mapper
@Repository
public interface InsDataSourceMapper extends BaseMapper<InsDataSourceEntity> {

    void updateDataSource(@Param("id")String id, @Param("createTime")LocalDateTime createTime,@Param("batchId") String batchId);

    List<InsDataSourceEntity> findDataSource(@Param("insDataSourceModel") InsDataSourceModel insDataSourceModel);

    InsDataSourceEntity findDataSourceByName(@Param("dataSourceName") String dataSourceName);

   void deleteDataSource(@Param("id")String id);

    InsDataSourceEntity findDataSourceById(String id);
}
