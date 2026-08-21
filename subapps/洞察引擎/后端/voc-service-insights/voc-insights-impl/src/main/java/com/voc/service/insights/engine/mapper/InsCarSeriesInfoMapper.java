package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.entity.InsCarSeriesInfoEntity;
import com.voc.service.insights.engine.model.InsCarSeriesInfoModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//@DS("starrocks-service")
@Mapper
public interface InsCarSeriesInfoMapper extends BaseMapper<InsCarSeriesInfoEntity> {

    void insertInsCarSeriesInfoEntity(InsCarSeriesInfoEntity insCarSeriesInfoEntity);

    void updateInsCarSeriesInfoEntity(InsCarSeriesInfoEntity insCarSeriesInfoEntity);

    void delInsCarSeriesInfoEntity(@Param("id") String id);

    List<InsCarSeriesInfoEntity> selectMultiInsCarSeriesInfoEntity(InsCarSeriesInfoModel InsCarSeriesInfoModel);

    IPage<InsCarSeriesInfoEntity> findInsCarSeriesInfoList(IPage<InsCarSeriesInfoEntity> page,@Param("carSeriesModel")InsCarSeriesInfoModel InsCarSeriesInfoModel);

}