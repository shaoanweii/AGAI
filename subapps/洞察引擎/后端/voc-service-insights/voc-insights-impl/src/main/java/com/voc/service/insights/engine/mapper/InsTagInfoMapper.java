package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsTagInfoEntity;
import com.voc.service.insights.engine.model.InsTagInfoQueryModel;
import com.voc.service.insights.engine.vo.InsTagInfoListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 14:12
 * @描述:
 **/
@Mapper
@Repository
public interface InsTagInfoMapper extends BaseMapper<InsTagInfoEntity> {
    List<InsTagInfoEntity> findTagInfoByType(@Param("type") String type);

    List<InsTagInfoListVo> queryInsTagInfo(@Param("model") InsTagInfoQueryModel model);

    InsTagInfoListVo queryInsTagInfoVoById(@Param("id") Serializable id);

    List<InsTagInfoEntity> findAllTaginfo();

}
