package com.voc.service.insights.engine.data.dao;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.data.entity.InsValidateRuleEntity;
import com.voc.service.insights.engine.model.InsValidateModel;
import com.voc.service.insights.engine.model.InsValidateRuleInfoModel;
import com.voc.service.insights.engine.vo.InsValidateRuleInfoVo;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/29 10:24
 * @描述:
 **/
public interface InsValidateRuleDao {
    InsValidateRuleInfoVo findValidateRuleCondition(InsValidateRuleInfoModel validateRuleInfoModel);
    void startValidateRuleInfo(InsValidateRuleInfoModel validateRuleInfoModel);
    PageInfo  findValidateRuleResult(InsValidateModel insValidateModel);
    void pushValidateRuleStatus(InsValidateRuleInfoModel validateRuleInfoModel);

    List<InsValidateRuleEntity> findValidateRuleInfo(InsValidateModel insValidateModel);
    List<InsValidateRuleEntity> findNewestValidateRuleInfo();
    List<InsValidateRuleEntity> findValidateInfoList(String regulationId);
    List<InsValidateRuleEntity> findValidateInfoListByIds(List<String> regulationIds);

    String findValidateTypeByWorkId(String workId);
}
