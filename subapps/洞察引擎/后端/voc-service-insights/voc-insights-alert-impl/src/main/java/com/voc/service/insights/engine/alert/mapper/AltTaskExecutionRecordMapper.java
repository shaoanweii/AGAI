package com.voc.service.insights.engine.alert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.alert.entity.AlertTaskExecutionRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author cuick
 * @since 2024-04-26 10:42:22
 */
@Mapper
public interface AltTaskExecutionRecordMapper extends BaseMapper<AlertTaskExecutionRecordEntity> {

    /**
     * 查询已执行的任务
     *
     * @return
     */
    List<AlertTaskExecutionRecordEntity> executedTasks(@Param("now") String now);
}

