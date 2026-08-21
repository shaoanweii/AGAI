package com.voc.service.data.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.data.integration.entity.ChannelExecutionResultEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface ChannelExecutionResultMapper extends BaseMapper<ChannelExecutionResultEntity> {
    List<ChannelExecutionResultEntity> findUnprocessedDatasetChannelType(ChannelExecutionResultEntity entity);
    long checkData(@Param("days") int days);
    void deleteHistoryData(@Param("days") int days);
    void moveToHistoryData(@Param("days") int days);
    long checkHistoryData();

    List<String> findExecutionResult(@Param("start") String start, @Param("end")String end, @Param("status") String status, @Param("errorCode")String errorCode);
}
