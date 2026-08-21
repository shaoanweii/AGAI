package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsRoleService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsRoleQueryModel;
import com.voc.service.insights.engine.model.RoleAuthModel;
import com.voc.service.insights.engine.model.RoleInfoQueryModel;
import com.voc.service.insights.engine.model.UserRoleQueryModel;
import com.voc.service.insights.engine.vo.RoleAuthTree;
import com.voc.service.insights.engine.vo.UserRoleInfoVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@Tag(name = "角色信息", description = "角色信息")
@RequestMapping("/role")
public class InsRoleController extends AbstractConditionFilters {


    private static final Logger log = LoggerFactory.getLogger(InsRoleController.class);
    @Resource
    private IInsRoleService iInsRoleService;


    @AutoLog(value = "角色信息-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询")
    @PostMapping("/queryRoleList")
    public Result<?> queryRoleList(@RequestBody @Validated InsRoleQueryModel model) {
        try {
            PageInfo roleList = iInsRoleService.queryRoleList(model);
            return Result.OK(roleList);
        } catch (Exception e) {
            log.error("角色信息-分页查询异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "角色信息-获取权限菜单下拉")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取权限菜单下拉")
    @PostMapping("/queryMenuPermissionList")
    public Result<?> queryMenuPermissionList(@RequestBody InsRoleQueryModel model) {
        try {
            List<RoleAuthTree> roleList = iInsRoleService.queryMenuPermissionList(model);
            return Result.OK(roleList);
        } catch (Exception e) {
            log.error("角色信息-获取权限菜单下拉异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "关联账户列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "关联账户列表")
    @PostMapping("/getUserRoleList")
    public Result<?> getUserRoleList(@RequestBody @Validated InsRoleQueryModel model) {
        try {
            PageInfo userRoleListVo = iInsRoleService.getUserRoleList(model);
            return Result.OK(userRoleListVo);
        } catch (Exception e) {
            log.error("角色信息-分页查询异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "角色信息-新增信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增信息")
    @PostMapping("/saveOrUpdateRole")
    public Result<?> saveOrUpdateRole(@RequestBody @Validated RoleAuthModel model) {
        try {
            return iInsRoleService.saveOrUpdateRole(model);
        } catch (Exception e) {
            log.error("角色信息-新增信息:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "角色信息-根据Id查询单条信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据Id查询单条信息")
    @PostMapping("/queryRoleInfo")
    public Result<?> queryRoleInfo(@RequestBody RoleInfoQueryModel model) {
        try {
            return iInsRoleService.queryRoleInfo(model);
        } catch (Exception e) {
            log.error("角色信息-根据Id查询单条信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, ENABLE_STATUS, STOP_OR_ENABLE)));
    }


    @AutoLog(value = "角色信息-根据Id查询单条信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据Id查询单条信息")
    @PostMapping("/queryUserPermission")
    public Result<?> queryRoleInfo(@RequestBody UserRoleQueryModel model) {
        try {
            UserRoleInfoVo userRoleInfoVo = iInsRoleService.queryUserPermission(model);
            return Result.OK(userRoleInfoVo);
        } catch (Exception e) {
            log.error("角色信息-根据Id查询单条信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "角色信息-删除角色")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除角色")
    @PostMapping("/deleteRole")
    public Result<?> deleteRole(@RequestBody RoleInfoQueryModel model) {
        try {
            return iInsRoleService.deleteRole(model);
        } catch (Exception e) {
            log.error("角色信息-删除角色异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

}
