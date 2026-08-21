package com.voc.service.insights.engine.data.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsRegulationInfoService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import com.voc.service.insights.engine.model.InsTableInfoModel;
import com.voc.service.insights.engine.model.InsValidateModel;
import com.voc.service.insights.engine.model.InsValidateRuleInfoModel;
import com.voc.service.insights.engine.vo.*;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/26 10:18
 * @描述:
 **/
@Tag(name = "数据处理服务")
@RestController
@RequestMapping("/regulation")
public class InsRegulationInfoController extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsRegulationInfoController.class);
    @Autowired
    IInsRegulationInfoService regulationInfoService;
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/27 13:24
     * @描述  查询条件
     * @return java.lang.Object
     **/
    @Override
    @GetMapping("/conditions")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false,
                REGULATION_PRE_TYPE,REGULATION_POST_TYPE,REGULATION_CONTENT_TYPE,REGULATION_STATUS_TYPE,REGULATION_STAGE,REGULATION_RELATIONS,REGULATION_CLASSIFY
        ,RULE_WEIGHT,RULE_LOGICAL_OPERATOR,RULE_CONDITION_TYPE,
                VOC_TEXT_TYPE,VOC_ORDER_TYPE,POST_FIELDS,VARIABLE_VALUE,HIT_STATE,DATA_COMPARISON)));
    }

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/28 10:43
     * @描述   新增规则信息
     * @param regulationInfoMode
     * @return com.voc.service.common.response.Result<?>
     **/
    @AutoLog(value = "数据处理服务-新增规则信息")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "新增规则信息")
    @PostMapping("/saveRegulationInfo")
     Result<?> saveRegulationInfo(@RequestBody InsRegulationInfoModel regulationInfoMode){
        try {
            regulationInfoService.saveRegulationInfo(regulationInfoMode);
            return Result.OK();
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("数据处理服务-新增规则信息异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据处理服务-更新规则信息")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "更新规则信息")
    @PostMapping("/updateRegulationInfo")
    Result<?> updateRegulationInfo(@RequestBody InsRegulationInfoModel regulationInfoMode){
        try {
            regulationInfoService.updateRegulationInfo(regulationInfoMode);
            return Result.OK();
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("数据处理服务-更新规则信息异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据处理服务-根据id删除规则信息")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "根据id删除规则信息")
    @PostMapping("/deleteRegulationInfo")
    Result<?> deleteRegulationInfo(@RequestBody InsRegulationInfoModel regulationInfoMode){
        try {
            regulationInfoService.deleteRegulationInfo(regulationInfoMode);
            return Result.OK();
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("数据处理服务-根据id删除规则信息异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据处理服务-分页查询规则信息列表")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "分页查询规则信息列表")
    @PostMapping("/findRegulationInfoList")
    Result<?> findRegulationInfoList(@RequestBody InsRegulationInfoModel regulationInfoMode){
        try {
            PageInfo regulationInfoList = regulationInfoService.findRegulationInfoList(regulationInfoMode);
            return Result.OK(regulationInfoList);
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("数据处理服务-分页查询规则信息列表异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据处理服务-复制规则")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "复制规则")
    @PostMapping("/copyRegulationInfo")
    Result<?> copyRegulationInfo(@RequestBody InsRegulationInfoModel regulationInfoMode){
        try {
            regulationInfoService.copyRegulationInfo(regulationInfoMode);
            return Result.OK();
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("数据处理服务-复制规则异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据处理服务-根据id查询规则信息")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "根据id查询规则信息")
    @PostMapping("/findRegulationInfo")
    Result<?> findRegulationInfo(@RequestBody InsRegulationInfoModel regulationInfoMode){
        try {
            RegulationInfoVo regulationInfo = regulationInfoService.findRegulationInfo(regulationInfoMode);
            return Result.OK(regulationInfo);
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("数据处理服务-根据id查询规则信息异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据处理服务-根据id停用或启用规则信息")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "根据id停用或启用规则信息")
    @PostMapping("/disabledOrEnableRegulationInfo")
    Result<?> disabledOrEnableRegulationInfo(@RequestBody InsRegulationInfoModel regulationInfoMode){
        try {
            regulationInfoService.disabledOrEnableRegulationInfo(regulationInfoMode);
            return Result.OK();
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("数据处理服务-根据id停用或启用规则信息:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "数据处理服务-获取全部规则")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "获取全部规则")
    @PostMapping("/findAllRegulationInfo")
    Result<?> findAllRegulationInfo(@RequestBody InsRegulationInfoModel regulationInfoMode){
        try {
            List<AysRegulationInfoVo> allRegulationInfo = regulationInfoService.findAllRegulationInfo(regulationInfoMode);
            return Result.OK(allRegulationInfo);
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("数据处理服务-获取全部规则异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    /**
     * @param tableInfoModel
     * @return com.voc.service.common.response.Result<?>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/14 13:41
     * @描述 获取全部表信息
     **/
    @AutoLog(value = "规则配置服务-获取全部表信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取全部表信息")
    @PostMapping("/findTableInfoList")
    Result<List<InsTableInfoVo>> findTableInfoList(@RequestBody InsTableInfoModel tableInfoModel) {
        try {
            List<InsTableInfoVo> tableInfoList = regulationInfoService.findTableInfoList(tableInfoModel);
            return Result.OK(tableInfoList);
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.errors(bussinessException.getCode(),bussinessException.getMessage());
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (Exception e) {
            log.error("",e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

//    @AutoLog(value = "规则配置服务-查询规则信息列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询规则信息列表")
    @PostMapping("/findRegulationList")
    Result<List<AysRegulationInfoVo>> findRegulationList(@RequestBody InsRegulationInfoModel regulationInfoMode) {
        try {
            List<AysRegulationInfoVo> rulesList = regulationInfoService.findRegulationList(regulationInfoMode);
            return Result.OK(rulesList);
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.errors(bussinessException.getCode(),bussinessException.getMessage());
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (Exception e) {
            log.error("",e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @AutoLog(value = "规则配置服务-查询资源组规则信息列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询资源组规则信息列表")
    @PostMapping("/findResourceGroupRegulationList")
    Result<?> findResourceGroupRegulationList(@RequestBody InsRegulationInfoModel regulationInfoMode) {
        try {
            PageInfo pageInfo = regulationInfoService.findResourceGroupRegulationList(regulationInfoMode);
            return Result.OK(pageInfo);
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.errors(bussinessException.getCode(),bussinessException.getMessage());
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (Exception e) {
            log.error("",e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "规则配置服务-查询资源组规则状态总数")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询资源组规则状态总数")
    @PostMapping("/findResourceGroupRegulationStatusCount")
    Result<?> findResourceGroupRegulationStatusCount(@RequestBody InsRegulationInfoModel regulationInfoMode) {
        try {
            List<RegulationInfoVo> statusCount = regulationInfoService.findResourceGroupRegulationStatusCount(regulationInfoMode);
            return Result.OK(statusCount);
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.errors(bussinessException.getCode(),bussinessException.getMessage());
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (Exception e) {
            log.error("",e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "规则配置服务-查询验证规则条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询验证规则条件")
    @PostMapping("/findValidateRegulationCondition")
    Result<?> findValidateRegulationCondition(@RequestBody InsValidateRuleInfoModel validateRuleInfoModel){
        try {
            InsValidateRuleInfoVo validateRuleCondition = regulationInfoService.findValidateRegulationCondition(validateRuleInfoModel);
            return Result.OK(validateRuleCondition);
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("规则配置服务-查询验证规则条件异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "规则配置服务-开始验证规则信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "开始验证规则信息")
    @PostMapping("/startValidateRegulationInfo")
    Result<?> startValidateRegulationInfo(@RequestBody InsValidateRuleInfoModel validateRuleInfoModel){
        try {
            regulationInfoService.startValidateRegulationInfo(validateRuleInfoModel);
            return Result.OK();
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("规则配置服务-开始验证规则信息异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "规则配置服务-开始测试规则信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "开始测试规则信息")
    @PostMapping("/startTestRegulationInfo")
    Result<?> startTestRegulationInfo(@RequestBody InsValidateRuleInfoModel validateRuleInfoModel){
        try {
            regulationInfoService.startTestRegulationInfo(validateRuleInfoModel);
            return Result.OK();
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("规则配置服务-开始验证规则信息异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "规则配置服务-查询验证规则结果")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询验证规则结果")
    @PostMapping("/findValidateRegulationResult")
    Result<?> findValidateRegulationResult(@RequestBody InsValidateModel insValidateModel){
        try {
            ValidateRuleResult validateRegulationResult = regulationInfoService.findValidateRegulationResult(insValidateModel);
            return Result.OK(validateRegulationResult);
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException exception){
            log.error("",exception);
            return Result.error(exception.getCode(),exception.getMessage());
        }catch (Exception e){
            log.error("规则配置服务-查询验证规则结果异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "规则配置服务-接收规则验证状态")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "接收规则验证状态")
    @PostMapping("/pushValidateRegulationStatus")
    Result<?> pushValidateRegulationStatus(@RequestBody InsValidateRuleInfoModel validateRuleInfoModel){
        try {
            regulationInfoService.pushValidateRegulationStatus(validateRuleInfoModel);
            return Result.OK();
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("规则配置服务-接收规则验证状态异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }

    }

    @AutoLog(value = "规则配置服务-获取最新校验信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取最新校验信息")
    @GetMapping("/findNewestValidateRuleInfo")
    Result<List<InsValidateInfoVo>> findNewestValidateRuleInfo(){
        try {
            List<InsValidateInfoVo> newestValidateRuleInfo = regulationInfoService.findNewestValidateRuleInfo();
            return Result.OK(newestValidateRuleInfo);
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.errors(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("规则配置服务-接收规则验证状态异常:",e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


}
