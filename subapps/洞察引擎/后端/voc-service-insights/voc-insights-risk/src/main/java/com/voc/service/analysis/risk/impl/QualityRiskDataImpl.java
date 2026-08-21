package com.voc.service.analysis.risk.impl;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IQualityRiskDataService;
import com.voc.service.analysis.model.AnalysisQualityRiskModel;
import com.voc.service.analysis.model.QualityRiskDataModel;
import com.voc.service.analysis.risk.entity.QualityRiskDataEntity;
import com.voc.service.analysis.risk.mapper.QualityRiskDataMapper;
import com.voc.service.insights.engine.vo.BrandVo;
import com.voc.service.insights.engine.vo.InsRiskSettingVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@DS("starrock_dndc")
public class QualityRiskDataImpl extends ServiceImpl<QualityRiskDataMapper, QualityRiskDataEntity> implements IQualityRiskDataService {

    @Override
    public void saveBatch(String clientId, List<AnalysisQualityRiskModel> analysisQualityRiskModelList) {
        List<QualityRiskDataEntity> qualityRiskDataEntityList = new ArrayList<>();
        for (AnalysisQualityRiskModel analysisQualityRiskModel : analysisQualityRiskModelList) {
            QualityRiskDataEntity entity = new QualityRiskDataEntity();
            BeanUtils.copyProperties(analysisQualityRiskModel, entity);
            entity.setSNum(String.valueOf(analysisQualityRiskModel.getSNum()));
            entity.setNegativeNum(analysisQualityRiskModel.getVoiceNum());
            entity.setPNum(String.valueOf(analysisQualityRiskModel.getPNum()));
            entity.setUserNum(analysisQualityRiskModel.getVoiceNum());
            entity.setRiskIndex(String.valueOf(analysisQualityRiskModel.getRiskIndex()));
            qualityRiskDataEntityList.add(entity);
        }
        QueryWrapper<QualityRiskDataEntity> query = new QueryWrapper<>();
        query.lambda().isNotNull(QualityRiskDataEntity::getBrandName);
        //this.remove(query);
        List<List<QualityRiskDataEntity>> resultBatch = ListUtil.split(qualityRiskDataEntityList, 100);
        for (List<QualityRiskDataEntity> r : resultBatch) {
            this.saveBatch(r);
        }
    }

    @Override
    public List<QualityRiskDataModel> riskQualityFilter(String clientId, InsRiskSettingVo insRiskSettingVo, BrandVo brandVo,String beginTime, String endTime) {
        return this.baseMapper.riskQualityFilter(insRiskSettingVo, brandVo, beginTime, endTime);
    }

}
