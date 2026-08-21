package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.AysPostprocessDataEntity;
import com.voc.service.analysis.model.ProjectResultDataParamModel;
import com.voc.service.analysis.model.ResultDataParamModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
public interface AysPostprocessDataMapper extends BaseMapper<AysPostprocessDataEntity> {
    long findByDistinctByAttr(@Param("attr") String attr
            , @Param("value") String value, @Param("startTime") String startTime);

    List<AysPostprocessDataEntity> pagePostprocessDataList(ResultDataParamModel paramModel);

    List<AysPostprocessDataEntity> pageProjectPostDataList(ProjectResultDataParamModel dataParamModel);
    AysPostprocessDataEntity projectPostData(ProjectResultDataParamModel dataParamModel);

    String projectMentionCarSeries(ProjectResultDataParamModel dataParamModel);

    Long pageProjectPostDataCount(ProjectResultDataParamModel dataParamModel);

    List<AysPostprocessDataEntity> pagePostprocessMetaDataList(@Param("paramModel") ResultDataParamModel paramModel);

    List<AysPostprocessDataEntity> selectErrorData();

//    long checkData(@Param("days") int days);
//    void deleteHistoryData(@Param("days") int days);
//    void moveToHistoryData(@Param("days") int days);
//    long checkHistoryData();
}
