package com.voc.service.data.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.data.integration.entity.ModelOutputRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;


@Mapper
@Repository
public interface ModelOutputRecordMapper extends BaseMapper<ModelOutputRecordEntity> {

    long record(@Param("channelList") Set<String> channelList, @Param("attrs") Set<String> attrs);
}
