package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.AysBatchPushRecordV2Entity;
import com.voc.service.analysis.model.AysBatchPushRecordExceptionModel;
import com.voc.service.analysis.model.AysBatchPushRecordGroupByModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: cuick
 * @创建时间: 2024/6/11 13:23
 * @描述:
 **/
@Mapper
@Repository
public interface AysBatchPushRecordV2Mapper extends BaseMapper<AysBatchPushRecordV2Entity> {

    List<AysBatchPushRecordGroupByModel> findGroupByRequestId(@Param("workId") String workId);

    List<AysBatchPushRecordExceptionModel> findExceptionRecordList(List<String> ids);
}
