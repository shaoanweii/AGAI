package com.voc.service.analysis.risk.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.risk.entity.ReportModelTagsResultDataRiskEntity;
import com.voc.service.analysis.risk.mapper.ReportModelTagsResultDataRiskMapper;
import org.springframework.stereotype.Service;


@Service
@DS("starrock_dndc")
public class ReportModelTagsResultDataRiskImpl extends ServiceImpl<ReportModelTagsResultDataRiskMapper, ReportModelTagsResultDataRiskEntity> {


}
