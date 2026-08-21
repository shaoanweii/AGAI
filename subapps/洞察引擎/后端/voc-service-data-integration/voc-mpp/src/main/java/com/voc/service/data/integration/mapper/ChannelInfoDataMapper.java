package com.voc.service.data.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.data.integration.entity.ChannelInfoDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface ChannelInfoDataMapper extends BaseMapper<ChannelInfoDataEntity> {
    List<ChannelInfoDataEntity> findAll();
}
