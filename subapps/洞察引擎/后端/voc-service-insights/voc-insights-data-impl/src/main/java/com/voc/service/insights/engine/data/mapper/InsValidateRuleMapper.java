package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.InsValidateRuleEntity;
import com.voc.service.insights.engine.model.InsValidateModel;
import com.voc.service.insights.engine.model.InsValidateRuleInfoModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/29 10:23
 * @描述:
 **/
@Mapper
@Repository
public interface InsValidateRuleMapper extends BaseMapper<InsValidateRuleEntity> {

   void updateValidateStatusByWordId(@Param("validateRuleInfoModel") InsValidateRuleInfoModel validateRuleInfoModel);

    List<InsValidateRuleEntity> findValidateRuleInfo(@Param("validateRuleInfoModel") InsValidateModel validateRuleInfoModel);

    List<InsValidateRuleEntity> findNewestValidateRuleInfo();

    List<InsValidateRuleEntity> findValidateInfoList(@Param("regulationId")String regulationId);

    String findValidateTypeByWorkId(@Param("workId")String workId);

    List<InsValidateRuleEntity> findValidateInfoListByIds(@Param("regulationIds") List<String> regulationIds);
}
