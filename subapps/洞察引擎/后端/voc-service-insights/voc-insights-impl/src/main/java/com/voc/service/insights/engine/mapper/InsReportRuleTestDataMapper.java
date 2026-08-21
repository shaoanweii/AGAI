package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.model.InsRuleTestListModel;
import com.voc.service.insights.engine.entity.InsLabelCorrectionRecordEntity;
import com.voc.service.insights.engine.entity.InsReportRuleTestDataEntity;
import com.voc.service.insights.engine.model.InsCqCaLabelCorrectionRecordQueryModel;
import com.voc.service.insights.engine.vo.InsCategoryRuleVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface InsReportRuleTestDataMapper extends BaseMapper<InsReportRuleTestDataEntity> {

    List<InsReportRuleTestDataEntity> selectPageList(InsRuleTestListModel model);

    List<InsCategoryRuleVo> getCategoryRuleList();

    @SwitchClientDS(datasource = "starrock_dndc")
    Boolean updateTaskStatus(String id);

    @SwitchClientDS(datasource = "starrock_dndc")
    InsReportRuleTestDataEntity getRuleTestData(String id);

}
