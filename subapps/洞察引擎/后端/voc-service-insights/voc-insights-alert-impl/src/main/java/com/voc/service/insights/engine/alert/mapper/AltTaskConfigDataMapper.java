package com.voc.service.insights.engine.alert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.alert.entity.AltTaskConfigDataEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据监控-任务配置表(AltTaskConfigData)表持久层
 *
 * @author leiww
 * @since 2024-04-30 17:11:55
 */
@Mapper
public interface AltTaskConfigDataMapper extends BaseMapper<AltTaskConfigDataEntity> {

}

