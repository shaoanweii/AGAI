package com.voc.service.analysis.risk.impl;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IEmotionRiskDataService;
import com.voc.service.analysis.model.AnalysisEmotionRiskModel;
import com.voc.service.analysis.model.EmotionRiskDataModel;
import com.voc.service.analysis.risk.entity.EmotionRiskDataEntity;
import com.voc.service.analysis.risk.mapper.EmotionRiskDataMapper;
import com.voc.service.insights.engine.vo.BrandVo;
import com.voc.service.insights.engine.vo.InsRiskSettingVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@DS("starrock_dndc")
public class EmotionRiskDataImpl extends ServiceImpl<EmotionRiskDataMapper, EmotionRiskDataEntity> implements IEmotionRiskDataService {

    @Override
    public void saveBatch(String clientId, List<AnalysisEmotionRiskModel> analysisEmotionRiskModelList) {
        List<EmotionRiskDataEntity> emotionRiskDataEntityList = new ArrayList<>();
        for (AnalysisEmotionRiskModel analysisEmotionRiskModel : analysisEmotionRiskModelList) {
            EmotionRiskDataEntity entity = new EmotionRiskDataEntity();
            BeanUtils.copyProperties(analysisEmotionRiskModel, entity);
            entity.setRNum(String.valueOf(analysisEmotionRiskModel.getRNum()));
            entity.setSNum(String.valueOf(analysisEmotionRiskModel.getSNum()));
            entity.setUserNum(analysisEmotionRiskModel.getVoiceNum());
            entity.setRiskIndex(String.valueOf(analysisEmotionRiskModel.getRiskIndex()));
            emotionRiskDataEntityList.add(entity);
        }
        QueryWrapper<EmotionRiskDataEntity> query = new QueryWrapper<>();
        query.lambda().isNotNull(EmotionRiskDataEntity::getBrandName);
        //this.remove(query);
        List<List<EmotionRiskDataEntity>> resultBatch = ListUtil.split(emotionRiskDataEntityList, 1000);
        for (List<EmotionRiskDataEntity> r : resultBatch) {
            this.saveBatch(r);
        }
    }


    @Override
    public List<EmotionRiskDataModel> riskEmotionFilter(String clientId, InsRiskSettingVo insRiskSettingVo, BrandVo brandVo, String beginTime, String endTime) {
        return this.baseMapper.riskEmotionFilter(insRiskSettingVo, brandVo, beginTime, endTime);
    }

}
