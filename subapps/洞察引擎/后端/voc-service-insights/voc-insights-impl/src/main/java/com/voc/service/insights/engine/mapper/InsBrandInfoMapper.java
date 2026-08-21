package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.entity.InsBrandInfoEntity;
import com.voc.service.insights.engine.model.InsBrandInfoModel;
import com.voc.service.insights.engine.vo.InsALlBrandAndCarSeriesVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//@DS("starrocks-service")
@Mapper
public interface InsBrandInfoMapper extends BaseMapper<InsBrandInfoEntity> {

    void insertInsBrandInfoEntity(InsBrandInfoEntity insBrandInfoEntity);

    void updateInsBrandInfoEntity(InsBrandInfoEntity insBrandInfoEntity);

    void delInsBrandInfoEntity(@Param("id") String id);

    List<InsBrandInfoEntity> selectMultiInsBrandInfoEntity(InsBrandInfoModel InsBrandInfoModel);

    List<InsALlBrandAndCarSeriesVo> findAllBrandAndCarSeries();

    IPage<InsBrandInfoEntity> findBrandInfoList(IPage<InsBrandInfoEntity> page,@Param("brandModel")InsBrandInfoModel brandModel);

    String getSelfBrandCodes();
}