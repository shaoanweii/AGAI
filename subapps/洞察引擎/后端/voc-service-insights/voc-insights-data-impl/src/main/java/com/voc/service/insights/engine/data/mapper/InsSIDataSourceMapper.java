package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.InsSIDataSourceEntity;
import com.voc.service.insights.engine.model.InsDataSourceRequestModel;
import com.voc.service.insights.engine.vo.AttrMappingVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/10/28 下午4:12
 * @描述:
 **/
@Mapper
@Repository
public interface InsSIDataSourceMapper extends BaseMapper<InsSIDataSourceEntity> {
    List<AttrMappingVo> findAllAttrMapping();

    Integer findDataSourceResult(@Param("insDataSourceRequestModel") InsDataSourceRequestModel insDataSourceRequestModel);

}
