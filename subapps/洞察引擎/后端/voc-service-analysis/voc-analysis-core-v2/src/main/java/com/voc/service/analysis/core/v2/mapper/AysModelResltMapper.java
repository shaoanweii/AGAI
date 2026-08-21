package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.AysModelResltDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
@Repository
public interface AysModelResltMapper extends BaseMapper<AysModelResltDataEntity> {
    //    long dataCount(AysValidDataModel param);
    long removeHistoryData(@Param("days") int days);
}
