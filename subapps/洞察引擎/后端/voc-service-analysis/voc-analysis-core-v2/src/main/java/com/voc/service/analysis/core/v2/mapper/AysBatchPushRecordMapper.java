package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.AysBatchPushRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @创建者: cuick
 * @创建时间: 2024/6/11 13:23
 * @描述:
 **/
@Mapper
@Repository
public interface AysBatchPushRecordMapper extends BaseMapper<AysBatchPushRecordEntity> {

    String selectByReqeustId(AysBatchPushRecordEntity param);

    void cumulativeSum(@Param("workId")String workId, @Param("currentBatchTotal")Integer currentBatchTotal);

    void cumulativePreFinishedSum(@Param("workId")String workId, @Param("currentBatchTotal")Integer currentBatchTotal);

    void cumulativeModelAnalysisSum(@Param("workId")String workId, @Param("currentBatchTotal")Integer currentBatchTotal);

    void cumulativePostFinishedSum(@Param("workId")String workId, @Param("currentBatchTotal")Integer currentBatchTotal);
}
