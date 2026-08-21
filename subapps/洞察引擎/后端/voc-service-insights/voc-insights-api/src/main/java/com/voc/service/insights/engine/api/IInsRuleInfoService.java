package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.InsRuleInfoModel;
import com.voc.service.insights.engine.model.InsTableInfoModel;
import com.voc.service.insights.engine.model.InsValidateRuleInfoModel;
import com.voc.service.insights.engine.vo.InsRuleInfoVo;
import com.voc.service.insights.engine.vo.InsTableInfoVo;
import com.voc.service.insights.engine.vo.InsValidateRuleInfoVo;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/13 09:41
 * @描述:
 **/
public interface IInsRuleInfoService {
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/13 10:13
     * @描述   新增规则信息
     * @param insRuleInfoModel
     * @return void
     **/
    void saveRuleInfo(InsRuleInfoModel insRuleInfoModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/13 14:00
     * @描述   更新规则信息
     * @param insRuleInfoModel
     * @return void
     **/
    void updateRuleInfo(InsRuleInfoModel insRuleInfoModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/13 14:08
     * @描述  删除规则信息
     * @param insRuleInfoModel
     * @return void
     **/
    void deleteRuleInfo(InsRuleInfoModel insRuleInfoModel);
    /**
     * @param ruleInfoModel
     * @return com.voc.service.common.pagination.Page
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/13 14:19
     * @描述 按条件查询分页规则信息
     **/
    PageInfo findRuleInfoList(InsRuleInfoModel ruleInfoModel);
    List<InsRuleInfoVo> findRulesList(InsRuleInfoModel ruleInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/13 14:49
     * @描述   根据id查询规则信息
     * @param ruleInfoModel
     * @return com.voc.service.insights.engine.vo.InsRuleInfoVo
     **/
    InsRuleInfoVo findRuleInfo(InsRuleInfoModel ruleInfoModel);



    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/12 17:39
     * @描述   查询所有表信息
     * @param tableInfoModel
     * @return java.util.List<com.voc.service.insights.engine.vo.InsTableInfoVo>
     **/
    List<InsTableInfoVo> findTableInfoList(InsTableInfoModel tableInfoModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/29 09:13
     * @描述   查询验证规则条件
     * @param validateRuleInfoModel
     * @return com.voc.service.insights.engine.vo.InsValidateRuleInfoVo
     **/
    InsValidateRuleInfoVo findValidateRuleCondition(InsValidateRuleInfoModel validateRuleInfoModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/29 09:14
     * @描述   开始验证规则信息
     * @param validateRuleInfoModel
     * @return void
     **/
    void startValidateRuleInfo(InsValidateRuleInfoModel validateRuleInfoModel);


    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/29 09:20
     * @描述   接收规则验证状态
     * @param validateRuleInfoModel
     * @return void
     **/
    void pushValidateRuleStatus(InsValidateRuleInfoModel validateRuleInfoModel);
}
