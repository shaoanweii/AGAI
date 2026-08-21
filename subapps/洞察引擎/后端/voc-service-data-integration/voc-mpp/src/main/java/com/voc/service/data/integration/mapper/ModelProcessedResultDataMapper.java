package com.voc.service.data.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.data.integration.entity.ModelProcessedResultDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;


@Mapper
@Repository
public interface ModelProcessedResultDataMapper extends BaseMapper<ModelProcessedResultDataEntity> {
    String refreshData();

    long saveOutputData(@Param("channelList")Set<String> channelList, @Param("attrs") Set<String> attrs);
}
