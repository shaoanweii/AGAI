package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.StopWatch;
import com.voc.service.insights.engine.api.IInsAccountInfoService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsAccountInfoModel;
import com.voc.service.insights.engine.model.InsRoleQueryModel;
import com.voc.service.insights.engine.model.InsSysDepartModel;
import com.voc.service.insights.engine.vo.InsAccountInfoVo;
import com.voc.service.insights.engine.vo.InsSysDepartVo;
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
 * @创建时间: 2024/3/4 10:44
 * @描述:
 **/
@Tag(name = "账号管理服务")
@RestController
@RequestMapping("/accountInfo")
public class InsAccountManagerController extends AbstractConditionFilters {

    private static final Logger log = LoggerFactory.getLogger(InsAccountManagerController.class);
    @Autowired
    IInsAccountInfoService accountInfoService;

    @Override
    @GetMapping("/conditions")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, STATUS, STOP_OR_ENABLE,COMPLETION_RATE)));
    }


    @AutoLog(value = "账号管理服务-新增账号信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增账号信息")
    @PostMapping("/saveAccountInfo")
    Result<?> saveAccountInfo(@RequestBody InsAccountInfoModel accountInfoModel) {
        try {
            accountInfoService.saveAccountInfo(accountInfoModel);
            return Result.OK();
        }catch (Exception e) {
            log.error("账号管理服务-新增账号信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "角色信息-获取角色名称下拉")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取角色名称下拉")
    @PostMapping("/queryRoleALlList")
    public Result<?> queryRoleALlList(@RequestBody InsRoleQueryModel model) {
        try {
            return accountInfoService.queryRoleALlList(model);
        } catch (Exception e) {
            log.error("角色信息-分页查询异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "账号管理服务-更新账号信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新账号信息")
    @PostMapping("/updateAccountInfo")
    Result<?> updateRegulationInfo(@RequestBody InsAccountInfoModel accountInfoModel) {
        try {
            accountInfoService.updateAccountInfo(accountInfoModel);
            return Result.OK();
        } catch (Exception e) {
            log.error("账号管理服务-更新账号信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "账号管理服务-根据id删除账号信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据id删除账号信息")
    @PostMapping("/deleteAccountInfo")
    Result<?> deleteAccountInfo(@RequestBody InsAccountInfoModel accountInfoModel) {
        try {
            accountInfoService.deleteAccountInfo(accountInfoModel);
            return Result.OK();
        }  catch (Exception e) {
            log.error("账号管理服务-根据id删除账号信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "账号管理服务-分页查询账号信息列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询账号信息列表")
    @PostMapping("/findAccountInfoList")
    Result<?> findAccountInfoList(@RequestBody InsAccountInfoModel accountInfoModel) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("findAccountInfoList开始");
        try {
            PageInfo accountInfoList = accountInfoService.findAccountInfoList(accountInfoModel);
            return Result.OK(accountInfoList);
        } catch (Exception e) {
            log.error("账号管理服务-分页查询账号信息列表异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        } finally {
            stopWatch.stop();
            stopWatch.prettyPrint();
        }
    }

    @AutoLog(value = "账号管理服务-根据id查询账号信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据id查询账号信息")
    @PostMapping("/findAccountInfo")
    Result<?> findRegulationInfo(@RequestBody InsAccountInfoModel accountInfoModel) {
        try {
            InsAccountInfoVo accountInfo = accountInfoService.findAccountInfo(accountInfoModel);
            return Result.OK(accountInfo);
        }catch (Exception e) {
            log.error("账号管理服务-根据id查询账号信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取列表")
    @PostMapping("/findDepartList")
    Result<?> findDepartList(@RequestBody InsAccountInfoModel accountInfoModel) {
        try {
            List<InsSysDepartModel> departList = accountInfoService.findDepartList(accountInfoModel);
            return Result.OK(departList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("操作日志-获取部门列表异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取部门树")
    @PostMapping("/findDepartTree")
    Result<?> findDepartTree(@RequestBody InsAccountInfoModel accountInfoModel) {
        try {
            List<InsSysDepartVo> departList = accountInfoService.findDepartTree(accountInfoModel);
            return Result.OK(departList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("操作日志-获取部门列表异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据部门id获取用户列表")
    @PostMapping("/findAccountByDeptId")
    Result<List<InsAccountInfoVo>> findAccountByDeptId(@RequestBody InsAccountInfoModel accountInfoModel) {
        try {
            List<InsAccountInfoVo> departList = accountInfoService.findAccountByDeptId(accountInfoModel);
            return Result.OK(departList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("操作日志-根据部门id获取用户列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取部门用户树")
    @GetMapping("/findDepartAccountTree")
    @AutoLog(value = "账号管理服务-获取部门用户树")
    Result<List<InsSysDepartVo>> findDepartAccountTree() {
        try {
            List<InsSysDepartVo> departList = accountInfoService.findDepartAccountTree();
            return Result.OK(departList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("操作日志-获取部门用户树异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据部门id获取部门用户树")
    @PostMapping("/findDepartAccountTreeByDeptId")
    @AutoLog(value = "账号管理服务-根据部门id获取部门用户树")
    Result<List<InsSysDepartVo>> findDepartAccountTreeByDeptId(@RequestBody InsAccountInfoModel accountInfoModel) {
        try {
            List<InsSysDepartVo> departList = accountInfoService.findDepartAccountTreeByDeptId(accountInfoModel);
            return Result.OK(departList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("操作日志-根据部门id获取部门用户树异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }
}
