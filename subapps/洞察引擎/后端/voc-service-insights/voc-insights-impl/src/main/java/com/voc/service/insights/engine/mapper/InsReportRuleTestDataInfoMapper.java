package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.model.InsRuleTestListModel;
import com.voc.service.insights.engine.entity.InsReportRuleTestDataEntity;
import com.voc.service.insights.engine.entity.InsReportRuleTestDataInfoEntity;
import com.voc.service.insights.engine.entity.InsReportRuleTestDataResultEntity;
import com.voc.service.insights.engine.vo.InsCategoryRuleVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface InsReportRuleTestDataInfoMapper extends BaseMapper<InsReportRuleTestDataInfoEntity> {

    @SwitchClientDS(datasource = "starrock_dndc")
    List<InsReportRuleTestDataResultEntity> selectPageList(InsRuleTestListModel model);
}
