package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import com.voc.service.insights.engine.model.InsTableInfoModel;
import com.voc.service.insights.engine.model.InsValidateModel;
import com.voc.service.insights.engine.model.InsValidateRuleInfoModel;
import com.voc.service.insights.engine.vo.*;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 09:40
 * @描述:
 **/
public interface IInsRegulationInfoService {


    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/27 14:31
     * @描述   新增规则信息
     * @param regulationInfoModel
     * @return void
     **/
    void saveRegulationInfo(InsRegulationInfoModel regulationInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/27 14:31
     * @描述   更新规则信息
     * @param regulationInfoModel
     * @return void
     **/
    void updateRegulationInfo(InsRegulationInfoModel regulationInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/27 16:15
     * @描述   删除规则信息
     * @param regulationInfoModel
     * @return void
     **/
    void deleteRegulationInfo(InsRegulationInfoModel regulationInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/27 16:53
     * @描述  分页查询规则信息列表
     * @param regulationInfoModel
     * @return com.github.pagehelper.PageInfo
     **/
    PageInfo findRegulationInfoList(InsRegulationInfoModel regulationInfoModel);

    void copyRegulationInfo(InsRegulationInfoModel regulationInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/27 17:30
     * @描述  查询规则信息
     * @param regulationInfoModel
     * @return com.voc.service.insights.engine.vo.RegulationInfoVo
     **/
    RegulationInfoVo findRegulationInfo(InsRegulationInfoModel regulationInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/15 13:20
     * @描述   停用/启用规则
     * @param regulationInfoModel
     * @return void
     **/
    void disabledOrEnableRegulationInfo(InsRegulationInfoModel regulationInfoModel);


    /**
     * @param regulationInfoModel
     * @return java.util.List<com.voc.service.insights.engine.vo.RegulationInfoVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/28 10:51
     * @描述 查询全部规则信息
     **/
    List<AysRegulationInfoVo> findAllRegulationInfo(InsRegulationInfoModel regulationInfoModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/12 17:39
     * @描述   查询所有表信息
     * @param tableInfoModel
     * @return java.util.List<com.voc.service.insights.engine.vo.InsTableInfoVo>
     **/
    List<InsTableInfoVo> findTableInfoList(InsTableInfoModel tableInfoModel);

    /**
     * @param regulationInfoMode
     * @return java.util.List<com.voc.service.insights.engine.vo.RegulationInfoVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/2 09:58
     * @描述 查询规则信息列表
     **/
    List<AysRegulationInfoVo> findRegulationList(InsRegulationInfoModel regulationInfoMode);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/4/2 10:01
     * @描述   查询验证规则条件
     * @param validateRuleInfoModel
     * @return com.voc.service.insights.engine.vo.InsValidateRuleInfoVo
     **/
    InsValidateRuleInfoVo findValidateRegulationCondition(InsValidateRuleInfoModel validateRuleInfoModel);

    void startValidateRegulationInfo(InsValidateRuleInfoModel validateRuleInfoModel);

    ValidateRuleResult findValidateRegulationResult(InsValidateModel insValidateModel);

    void pushValidateRegulationStatus(InsValidateRuleInfoModel validateRuleInfoModel);

    Boolean checkRegulationName(InsRegulationInfoModel regulationInfoMode);

    void startTestRegulationInfo(InsValidateRuleInfoModel validateRuleInfoModel);

    PageInfo findResourceGroupRegulationList(InsRegulationInfoModel regulationInfoModel);

    List<RegulationInfoVo> findResourceGroupRegulationStatusCount(InsRegulationInfoModel regulationInfoModel);

    List<InsValidateInfoVo> findNewestValidateRuleInfo();
}
