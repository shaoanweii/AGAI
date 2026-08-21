package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.InsDataSourceTemplateEntity;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/14 上午10:40
 * @描述:
 **/
@Mapper
@Repository
public interface InsDataSourceTemplateMapper extends BaseMapper<InsDataSourceTemplateEntity> {
    void updateBatchDataSource(@Param("dataSourceTemplateEntity") InsDataSourceTemplateEntity dataSourceTemplateEntity);
    List<InsDataSourceTemplateEntity> findByBatchId(@Param("insDataSourceModel")InsDataSourceModel insDataSourceModel) ;
    void deleteDataSourceTemplate(@Param("insDataSourceModel")InsDataSourceModel insDataSourceModel);

    void insertBatchDataSource(@Param("dataSourceTemplateEntities") List<InsDataSourceTemplateEntity> dataSourceTemplateEntities);
}
