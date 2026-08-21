package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.AysPreprocessDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
public interface AysPreprocessDataMapper extends BaseMapper<AysPreprocessDataEntity> {
    long findByDistinctByAttr(@Param("attr") String attr
            , @Param("value") String value, @Param("startTime") String startTime);

    List<AysPreprocessDataEntity> readUnprocessedData(@Param("size") int size);

    void retryingRecords(@Param("ids") List<String> list);

    long removeHistoryData(@Param("days") int days);

    Set<String> findIincompleteData();

    String findWorkId();
}
