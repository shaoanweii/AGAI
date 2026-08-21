package com.voc.service.analysis.risk.mapper;


import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.risk.api.model.BatchTaskConditionsModel;
import com.voc.service.risk.api.model.BatchWarningTaskRunModel;
import com.voc.service.risk.api.vo.AccountLexiconVo;
import com.voc.service.risk.api.vo.BatchRuleDataVo;
import com.voc.service.risk.api.vo.BatchTaskResultVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface BatchRuleDataMapper {

    List<BatchRuleDataVo> queryBatchRuleData(BatchWarningTaskRunModel param);

    int deleteJobIdList();

    int addJob(@Param("ruleId") String ruleId,@Param("cron") String cron);

    List<AccountLexiconVo> queryInsAccountLexicon();

    List<String> queryInsPropertyTag(@Param("idList") List<String> idList);

    @SwitchClientDS(datasource = "starrock_dndc")
    List<BatchTaskResultVo> queryBatchSoundsData(BatchTaskConditionsModel model);
}
