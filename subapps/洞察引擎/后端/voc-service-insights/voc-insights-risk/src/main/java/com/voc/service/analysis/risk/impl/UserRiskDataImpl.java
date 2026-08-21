package com.voc.service.analysis.risk.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IUserRiskDataService;
import com.voc.service.analysis.model.AnalysisUserRiskModel;
import com.voc.service.analysis.model.UserRiskDataModel;
import com.voc.service.analysis.risk.entity.UserRiskDataEntity;
import com.voc.service.analysis.risk.mapper.UserRiskDataMapper;
import com.voc.service.insights.engine.vo.BrandVo;
import com.voc.service.insights.engine.vo.InsRiskSettingVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
@DS("starrock_dndc")
public class UserRiskDataImpl extends ServiceImpl<UserRiskDataMapper, UserRiskDataEntity> implements IUserRiskDataService {

    @Override
    public void saveBatch(String clientId, List<AnalysisUserRiskModel> analysisUserRiskModelList) {
        List<UserRiskDataEntity> userRiskDataEntities = new ArrayList<>();
        for (AnalysisUserRiskModel analysisUserRiskModel : analysisUserRiskModelList) {
            UserRiskDataEntity entity = new UserRiskDataEntity();
            BeanUtils.copyProperties(analysisUserRiskModel, entity);
            entity.setRiskIndex(String.valueOf(analysisUserRiskModel.getRiskIndex()));
            entity.setEmotionNum(BigDecimal.valueOf(analysisUserRiskModel.getEmotionNum()));
            entity.setVoiceNum(analysisUserRiskModel.getOriginalNum());
            entity.setId(SecureUtil.md5(analysisUserRiskModel.getPublishDate() + analysisUserRiskModel.getStatisticType()
                    + analysisUserRiskModel.getUserName() + analysisUserRiskModel.getBrandName() + analysisUserRiskModel.getNewIdArray()));
            if (StringUtils.isNotEmpty(analysisUserRiskModel.getKeywords())) {
                String[] split = StringUtils.split(analysisUserRiskModel.getKeywords(), ",");
                List<String> labelTypeLevelFourList = Arrays.asList(split);
                JSONObject jsonObject = new JSONObject();
                StringBuilder stringBuilder = new StringBuilder();
                for (String label : labelTypeLevelFourList) {
                    String replaced = label.replaceAll("\"", "");
                    if (jsonObject.containsKey(replaced)) {
                        Integer anInt = jsonObject.getInt(replaced);
                        jsonObject.put(replaced, anInt + 1);
                    } else {
                        jsonObject.put(replaced, 1);
                    }
                }
                for (String key : jsonObject.keySet()) {
                    String value = jsonObject.getStr(key);
                    stringBuilder.append(key).append("(" + value + ")").append(",");
                }
                entity.setFocusProblem(stringBuilder.substring(0, stringBuilder.length() - 1));
            }
            userRiskDataEntities.add(entity);
        }
        List<List<UserRiskDataEntity>> resultBatch = ListUtil.split(userRiskDataEntities, 1000);
        for (List<UserRiskDataEntity> r : resultBatch) {
            try {
                Thread.sleep(5000);
                this.saveBatch(r);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public void deleteRisk() {
        QueryWrapper<UserRiskDataEntity> query = new QueryWrapper<>();
        query.lambda().isNotNull(UserRiskDataEntity::getBrandName);
        this.remove(query);
    }

    @Override
    public List<UserRiskDataModel> riskUserFilter(String clientId, InsRiskSettingVo insRiskSettingVo, BrandVo brandVo,String beginTime, String endTime) {
        return this.baseMapper.riskUserFilter(insRiskSettingVo, brandVo,beginTime, endTime);
    }

}
