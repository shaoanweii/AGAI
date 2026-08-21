package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.AysMetaDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
@Repository
public interface AysMetaDataMapper extends BaseMapper<AysMetaDataEntity> {

    AysMetaDataEntity findIincompleteData();

    int modifyByWorkId(@Param("work_id") String workId);

    long copySourceData(@Param("ids") Set<String> metaDataIds, @Param("work_id") String workId, @Param("tid") String tid);

    void retryingRecords(@Param("ids") List<String> list);

    long removeHistoryData(@Param("days") int days);
}
