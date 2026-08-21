package com.voc.service.analysis.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.model.AnalysisEmotionRiskModel;
import com.voc.service.analysis.model.AnalysisQualityRiskModel;
import com.voc.service.analysis.model.AnalysisUserRiskModel;
import com.voc.service.analysis.model.RiskStatisticModel;
import com.voc.service.analysis.risk.entity.AysPostprocessDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;


@Mapper
public interface AysPostProcessDataMapper extends BaseMapper<AysPostprocessDataEntity> {


    List<AnalysisEmotionRiskModel> emotionRisk(RiskStatisticModel paramModel);


    List<AnalysisQualityRiskModel> qualityRisk(RiskStatisticModel paramModel);


    List<AnalysisUserRiskModel> userRisk(RiskStatisticModel paramModel);


    List<String> getSentenceList(@Param("startTime") String startTime,
                                 @Param("endTime") String endTime,
                                 @Param("tagName") String tagName,
                                 @Param("brandName") String brandName,
                                 @Param("tagType") String tagType,
                                 @Param("labelTypeLevelFourDisableList") Set<String> labelTypeLevelFourDisableList);
}

