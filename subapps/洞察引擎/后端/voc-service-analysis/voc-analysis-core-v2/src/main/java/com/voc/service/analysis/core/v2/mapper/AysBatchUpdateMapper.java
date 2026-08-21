package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.AysBatchUpdateEntity;
import com.voc.service.analysis.model.ModifyDataModel;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
@Repository
public interface AysBatchUpdateMapper extends BaseMapper<AysBatchUpdateEntity> {
    int updateStatusToDone(List<String> ids);

    int batchUpdateResultData(List<String> ids, Map<String, String>  filters, Map<String, String> updateValues);
}
