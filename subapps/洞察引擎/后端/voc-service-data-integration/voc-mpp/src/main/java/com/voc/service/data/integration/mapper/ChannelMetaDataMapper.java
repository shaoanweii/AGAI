package com.voc.service.data.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.data.integration.entity.ChannelMetaDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


@Mapper
@Repository
public interface ChannelMetaDataMapper extends BaseMapper<ChannelMetaDataEntity> {
    long checkData(@Param("days") int days);
    void deleteHistoryData(@Param("days") int days);
    void moveToHistoryData(@Param("days") int days);
    long checkHistoryData();

    List<ChannelMetaDataEntity> findRawData(@Param("start") String start, @Param("status") String status, @Param("errorCode")String errorCode);

    List<ChannelMetaDataEntity> findAll(@Param("channelList") Set<String> channelList, @Param("startDate")String startDate, @Param("endDate")String endDate);

    long checkAllMetaData();

}
