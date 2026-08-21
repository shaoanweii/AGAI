package com.voc.service.data.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.data.integration.entity.CleanHistoryDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface CleanHistoryDataMapper extends BaseMapper<CleanHistoryDataEntity> {

    Long checkChannelMetaData(int days);

    void moveChannelMetaDataToHistoryData(int days);

    Long checkChannelMetaDataHistoryData();

    Long deleteChannelMetaData(int days);

    Long deleteAysMetaData(int days);

    Long checkAysMetaAnalysisData(int days);

    void moveAysMetaAnalysisDataToHistoryData(int days);

    Long checkAysMetaAnalysisDataHistoryData();

    Long deleteAysMetaAnalysisData(int days);

    Long deleteAysPreProcessData(int days);

    Long deleteAysBatchPushRecord(int days);

    Long deleteAysApiResltData(int days);

    Long deleteApiResltDataAnalysis(int days);

    Long deleteApiResltDataAnalysisMiss(int days);

    Long checkAysPostProcessData(int days);

    void moveAysPostProcessDataToHistoryData(int days);

    Long checkAysPostProcessDataHistoryData();

    Long deleteAysPostProcessData(int days);

    Long deleteDmLabeledResultData(int days);

    Long deleteoDsChannelExecutionResult(int days);

    Long deleteoOdsChannelOutputRecord(int days);

    @SwitchClientDS(datasource = "voc-mysql")
    Long deleteInsRecordLogs(int days);
}
