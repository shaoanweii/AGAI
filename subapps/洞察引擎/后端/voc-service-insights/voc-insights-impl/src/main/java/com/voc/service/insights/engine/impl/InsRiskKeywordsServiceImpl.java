package com.voc.service.insights.engine.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.util.IdWorker;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsRiskKeywordsService;
import com.voc.service.insights.engine.api.constants.CategoryTypeEnum;
import com.voc.service.insights.engine.api.constants.EnableStatusEnum;
import com.voc.service.insights.engine.api.constants.IncreaseTypeEnum;
import com.voc.service.insights.engine.entity.InsRiskKeywordsEntity;
import com.voc.service.insights.engine.mapper.InsRiskKeywordsMapper;
import com.voc.service.insights.engine.model.AddRiskKeywordsModel;
import com.voc.service.insights.engine.model.InsRiskKeywordsModel;
import com.voc.service.insights.engine.model.InsRiskKeywordsQueryModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class InsRiskKeywordsServiceImpl extends ServiceImpl<InsRiskKeywordsMapper, InsRiskKeywordsEntity> implements IInsRiskKeywordsService {


    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public PageInfo queryRisKeywordsList(InsRiskKeywordsQueryModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        QueryWrapper<InsRiskKeywordsEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(model.getTagCategory())) {
            queryWrapper.eq("tag_category", model.getTagCategory());
        }
        if (StringUtils.isNotBlank(model.getSeriousLevel())) {
            queryWrapper.eq("serious_level", model.getSeriousLevel());
        }
        if (StringUtils.isNotBlank(model.getIncreaseType())) {
            queryWrapper.eq("increase_type", model.getIncreaseType());
        }
        if (StringUtils.isNotBlank(model.getRiskKeywords())) {
            queryWrapper.like("risk_keywords", "%" + model.getRiskKeywords() + "%");
        }
        queryWrapper.orderByDesc("operate_time");
        List<InsRiskKeywordsEntity> insRiskKeywordsEntityList = this.list(queryWrapper);
        PageInfo page = new PageInfo<>(insRiskKeywordsEntityList);
        if (CollectionUtil.isEmpty(insRiskKeywordsEntityList)) {
            return page;
        }
        List<InsRiskKeywordsModel> insRiskKeywordsModels = this.assemblyData(insRiskKeywordsEntityList);
        page.setList(insRiskKeywordsModels);
        return page;
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public List<InsRiskKeywordsModel> queryRiskList(InsRiskKeywordsQueryModel model) {

        QueryWrapper<InsRiskKeywordsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("enable_status", "1");
        List<InsRiskKeywordsEntity> insRiskKeywordsEntityList = this.list(queryWrapper);
        if (CollectionUtil.isEmpty(insRiskKeywordsEntityList)) {
            return new ArrayList<>();
        }
        return this.assemblyData(insRiskKeywordsEntityList);
    }

    private List<InsRiskKeywordsModel> assemblyData(List<InsRiskKeywordsEntity> insRiskKeywordsEntityList) {
        List<InsRiskKeywordsModel> insRiskKeywordsModelList = new ArrayList<>();
        for (InsRiskKeywordsEntity insRiskKeywordsEntity : insRiskKeywordsEntityList) {
            InsRiskKeywordsModel insRiskKeywordsModel = new InsRiskKeywordsModel();
            BeanUtil.copyProperties(insRiskKeywordsEntity, insRiskKeywordsModel);
            insRiskKeywordsModel.setEnableStatusName(EnableStatusEnum.getByCode(String.valueOf(insRiskKeywordsEntity.getEnableStatus())).getName());
            insRiskKeywordsModel.setIncreaseTypeName(IncreaseTypeEnum.getByCode(String.valueOf(insRiskKeywordsEntity.getIncreaseType())).getName());
            insRiskKeywordsModel.setTagCategoryName(CategoryTypeEnum.getByCode(insRiskKeywordsEntity.getTagCategory()).getName());
            insRiskKeywordsModelList.add(insRiskKeywordsModel);
        }
        return insRiskKeywordsModelList;
    }


    @Override
    @SwitchClientDS(objectAttribute = "riskKeywordsModel.clientId")
    public Boolean addRisKeywords(AddRiskKeywordsModel riskKeywordsModel) {
        InsRiskKeywordsEntity insRiskKeywordsEntity = new InsRiskKeywordsEntity();
        BeanUtil.copyProperties(riskKeywordsModel, insRiskKeywordsEntity);
        insRiskKeywordsEntity.setCreateTime(LocalDate.now());
        insRiskKeywordsEntity.setOperateTime(LocalDateTime.now());
        if (StringUtils.isEmpty(riskKeywordsModel.getId())) {
            insRiskKeywordsEntity.setId(IdWorker.getId());
            insRiskKeywordsEntity.setIncreaseType(2);
        }
        return this.saveOrUpdate(insRiskKeywordsEntity);
    }
}
