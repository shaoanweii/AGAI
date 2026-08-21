package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsTagClientEntity;
import com.voc.service.insights.engine.model.InsTagInfoQueryModel;
import com.voc.service.insights.engine.vo.InsTagClientVo;
import com.voc.service.insights.engine.vo.TagClientCustomerVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Mapper
@Repository
public interface InsTagClientMapper extends BaseMapper<InsTagClientEntity> {
    List<InsTagClientVo> queryInsTagClientInfo(@Param("model") InsTagInfoQueryModel model);

    InsTagClientVo queryInsTagClientVoById(@Param("id") Serializable id);

    List<TagClientCustomerVo> queryTagClientCustomerVo(@Param("codeList") List<String> codeList);
}
