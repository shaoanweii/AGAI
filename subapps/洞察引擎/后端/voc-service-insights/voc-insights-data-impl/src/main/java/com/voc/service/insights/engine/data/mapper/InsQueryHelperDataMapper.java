package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.InsQueryHelperDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @创建者: fanrong
 * @创建时间: 2025/12/23 10:23
 * @描述:
 **/
@Mapper
@Repository
public interface InsQueryHelperDataMapper extends BaseMapper<InsQueryHelperDataEntity> {
}
