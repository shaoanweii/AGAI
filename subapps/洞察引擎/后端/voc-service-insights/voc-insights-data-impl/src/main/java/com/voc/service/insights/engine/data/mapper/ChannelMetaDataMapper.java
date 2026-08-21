package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.ChannelMetaDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.ResultSetType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


@Mapper
@Repository
public interface ChannelMetaDataMapper extends BaseMapper<ChannelMetaDataEntity> {
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    List<ChannelMetaDataEntity> findRawData(@Param("start") String start, @Param("status") String status, @Param("errorCode")String errorCode);

}
