package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsProjectInfoEntity;
import com.voc.service.insights.engine.model.InsProjectInfoModel;
import com.voc.service.insights.engine.model.InsProjectInfoQueryModel;
import com.voc.service.insights.engine.vo.ProjectInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InsProjectInfoMapper extends BaseMapper<InsProjectInfoEntity> {

    List<InsProjectInfoEntity> findProjectList(@Param("insProjectInfoModel") InsProjectInfoModel insProjectInfoModel);

    InsProjectInfoEntity findProjectInfo(@Param("insProjectInfoModel")InsProjectInfoModel insProjectInfoModel);
}
