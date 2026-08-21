package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.AysMetaDataExtAnalysisEntity;
import com.voc.service.analysis.core.v2.entity.AysMetaDataExtAnalysisEntity;
import com.voc.service.analysis.model.DataStatusModel;
import com.voc.service.analysis.model.ProjectRawDataParamModel;
import com.voc.service.analysis.model.RawDataParamModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.mapping.ResultSetType;

import java.util.List;
import java.util.Set;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
public interface AysMetaDataAnalysisMapper extends BaseMapper<AysMetaDataExtAnalysisEntity> {

//    void updateErrorMsgById(AysMetaDataExtAnalysisEntity entity);

//    List<AysMetaDataExtAnalysisEntity> readUnprocessedData(@Param("size") int size);

    //    List<AysMetaDataExtAnalysisEntity> pageMetaDataAnalysisList(RawDataParamModel paramModel);
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    @ResultType(AysMetaDataExtAnalysisEntity.class)
    List<AysMetaDataExtAnalysisEntity> pageMetaDataAnalysisList(RawDataParamModel paramModel);
//    Cursor<AysMetaDataExtAnalysisEntity> pageMetaDataAnalysisList(RawDataParamModel paramModel, StreamingStatementHandler<AysMetaDataExtAnalysisEntity> handler);

    List<DataStatusModel> getDataResultStatus(RawDataParamModel paramModel);

//    List<AysMetaDataExtAnalysisEntity> pageProjectDataAnalysisList(ProjectRawDataParamModel paramModel);

//    Long pageProjectDataAnalysisCount(ProjectRawDataParamModel paramModel);

//    long checkData(@Param("days") int days);


//    void moveToHistoryData(@Param("days") int days);

//    long checkHistoryData();

//    Set<String> findIincompleteData();

//    Set<String> findByNewIdList(@Param("newIdList") Set<String> newIdList);

    /**
     * 批量更新数据状态
     *
     * @param dataIds 数据ID集合
     * @param status 要更新的状态值
     * @return 更新的记录数
     */
}
