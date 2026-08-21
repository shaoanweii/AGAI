package com.voc.service.analysis.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.model.UserRiskDataModel;
import com.voc.service.analysis.risk.entity.UserRiskDataEntity;
import com.voc.service.insights.engine.vo.BrandVo;
import com.voc.service.insights.engine.vo.InsRiskSettingVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface UserRiskDataMapper extends BaseMapper<UserRiskDataEntity> {


    List<UserRiskDataModel> riskUserFilter(@Param("model") InsRiskSettingVo insRiskSettingVo, @Param("vo") BrandVo brandVo,
                                           @Param("beginTime")String beginTime, @Param("endTime")String endTime);

}

