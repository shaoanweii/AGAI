package com.voc.service.insights.engine.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.internal.NonNullElementWrapperList;
import com.voc.service.insights.engine.api.IInsRuleTestDataInfoService;
import com.voc.service.insights.engine.api.model.InsRuleTestListModel;
import com.voc.service.insights.engine.entity.InsReportRuleTestDataInfoEntity;
import com.voc.service.insights.engine.entity.InsReportRuleTestDataResultEntity;
import com.voc.service.insights.engine.mapper.InsReportRuleTestDataInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InsRuleTestDataInfoServiceImpl extends ServiceImpl<InsReportRuleTestDataInfoMapper, InsReportRuleTestDataInfoEntity>
        implements IInsRuleTestDataInfoService {
    private static final Logger log = LoggerFactory.getLogger(InsRuleTestDataInfoServiceImpl.class);


    public Boolean batchRuleTest(List<InsReportRuleTestDataInfoEntity> entityList) {
        return this.saveBatch(entityList);
    }

    public List<InsReportRuleTestDataResultEntity> selectPageList(InsRuleTestListModel model) {
        return this.baseMapper.selectPageList(model);
    }

    public void deleteList(String batchId) {
        this.remove(new QueryWrapper<InsReportRuleTestDataInfoEntity>().eq("batch_id", batchId));
    }

}
