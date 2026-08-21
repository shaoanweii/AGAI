package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsCustomerInfoService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsCustomerInfoModel;
import com.voc.service.insights.engine.vo.RoleAuthTree;
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
 * @创建时间: 2024/2/21 17:57
 * @描述:
 **/
@Tag(name = "客户管理服务")
@RestController
@RequestMapping("/customer")
public class InsCustomerManagementController extends AbstractConditionFilters {

    private static final Logger log = LoggerFactory.getLogger(InsCustomerManagementController.class);
    @Autowired
    IInsCustomerInfoService customerInfoService;

    /**
     * @return com.voc.service.common.response.Result<?>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 08:58
     * @描述 查询条件
     **/
    @Override
    @GetMapping("/conditions")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, PROVINCE, STATUS, STOP_OR_ENABLE)));
    }


    @AutoLog(value = "客户管理服务-根据客户ID查询客户编码")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据客户ID查询客户编码")
    @GetMapping("/queryCodeById")
    Result<?> queryCodeById(@RequestParam(value = "clientId") String clientId) {
        try {
            String code = customerInfoService.queryCodeById(clientId);
            return Result.OK(code);
        }catch (Exception e) {
            log.error("客户管理服务-根据客户ID查询客户编码:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    /**
     * @param customerInfoModel
     * @return com.voc.service.common.response.Result<?>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 10:47
     * @描述 新增客户信息
     **/
    @AutoLog(value = "客户管理服务-新增客户信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增客户信息")
    @PostMapping("/saveCustomerInfo")
    Result<?> saveCustomerInfo(@RequestBody InsCustomerInfoModel customerInfoModel) {
        try {
            customerInfoService.saveCustomerInfo(customerInfoModel);
            return Result.OK();
        }catch (Exception e) {
            log.error("客户管理服务-新增客户信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    /**
     * @param customerInfoModel
     * @return com.voc.service.common.response.Result<?>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 10:56
     * @描述 更新客户信息
     **/
    @AutoLog(value = "客户管理服务-更新客户信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新客户信息")
    @PostMapping("/updateCustomerInfo")
    Result<?> updateCustomerInfo(@RequestBody InsCustomerInfoModel customerInfoModel) {
        try {
            customerInfoService.updateCustomerInfo(customerInfoModel);
            return Result.OK();
        } catch (Exception e) {
            log.error("客户管理服务-更新客户信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    /**
     * @param customerInfoModel
     * @return com.voc.service.common.response.Result<?>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 11:20
     * @描述 根据id删除客户信息
     **/
    @AutoLog(value = "客户管理服务-根据id删除客户信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据id删除客户信息")
    @PostMapping("/deleteCustomerInfo")
    Result<?> deleteCustomerInfo(@RequestBody InsCustomerInfoModel customerInfoModel) {
        try {
            customerInfoService.deleteCustomerInfo(customerInfoModel);
            return Result.OK();
        }  catch (Exception e) {
            log.error("客户管理服务-根据id删除客户信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    /**
     * @param customerInfoModel
     * @return com.voc.service.common.response.Result<?>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 14:11
     * @描述 根据id查询客户信息
     **/
    @AutoLog(value = "客户管理服务-根据id查询客户信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据id查询客户信息")
    @PostMapping("/findCustomerInfo")
    Result<?> findCustomerInfo(@RequestBody InsCustomerInfoModel customerInfoModel) {
        try {
            return Result.OK(customerInfoService.findCustomerInfo(customerInfoModel));
        }  catch (Exception e) {
            log.error("客户管理服务-根据id查询客户信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    /**
     * @param customerInfoModel
     * @return com.voc.service.common.response.Result<?>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 16:36
     * @描述 分页查询客户列表
     **/
    @AutoLog(value = "客户管理服务-分页查询客户列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询客户列表")
    @PostMapping("/findCustomerList")
    Result<?> findCustomerList(@RequestBody InsCustomerInfoModel customerInfoModel) {
        try {
            PageInfo pageInfo = customerInfoService.findCustomerList(customerInfoModel);
            return Result.OK(pageInfo);
        }  catch (Exception e) {
            log.error("客户管理服务-分页查询客户列表异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "客户管理服务-检验客户编码是否存在")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "检验客户编码是否存在")
    @PostMapping("/checkCustomerCode")
    Result<?> checkCustomerCode(@RequestBody InsCustomerInfoModel customerInfoModel) {
        try {
            Boolean checked = customerInfoService.checkCustomerCode(customerInfoModel);
            return Result.OK(checked);
        } catch (Exception e) {
            log.error("客户管理服务-检验客户编码是否存在异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "客户管理服务-根据ID查询客户权限树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据ID查询客户权限树")
    @PostMapping("/queryCustomerPermissionList")
    Result<?> queryCustomerPermissionList(@RequestBody InsCustomerInfoModel customerInfoModel) {
        try {
            List<RoleAuthTree> roleAuthTreeList = customerInfoService.queryCustomerPermissionList(customerInfoModel);
            return Result.OK(roleAuthTreeList);
        } catch (Exception e) {
            log.error("客户管理服务-根据ID查询客户权限树异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


}
