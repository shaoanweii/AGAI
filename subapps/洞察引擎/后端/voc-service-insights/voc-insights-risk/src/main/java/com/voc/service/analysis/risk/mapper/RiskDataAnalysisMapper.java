package com.voc.service.analysis.risk.mapper;

import com.voc.service.analysis.risk.entity.CarDataEntity;
import com.voc.service.analysis.risk.entity.ReportModelTagsResultDataRiskEntity;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.model.WarningTaskConditionsModel;
import com.voc.service.insights.engine.model.WarningTaskResultModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface RiskDataAnalysisMapper {

    List<String> queryRuleIdList();

    int addJob(@Param("ruleId") String ruleId,@Param("cron") String cron);

    int delJob(List<String> jobIdList);


    int deleteJobIdList();

    @SwitchClientDS(datasource = "starrock_dndc")
    List<WarningTaskResultModel> querySoundsData(WarningTaskConditionsModel model);


    @SwitchClientDS(datasource = "starrock_dndc")
    List<WarningTaskResultModel> executeRuleTestInfo(WarningTaskConditionsModel model);

    @SwitchClientDS(datasource = "starrock_dndc")
    List<CarDataEntity> toDayApiData();

    @SwitchClientDS(datasource = "starrock_dndc")
    List<ReportModelTagsResultDataRiskEntity> queryResultDataRisk();
}

