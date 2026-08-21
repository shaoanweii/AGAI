package com.voc.service.insights.engine.impl;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.model.auth.PermissionModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.util.ClientMappings;
import com.voc.service.insights.engine.api.IInsCustomerInfoService;
import com.voc.service.insights.engine.api.IInsRoleService;
import com.voc.service.insights.engine.api.IInsUserRoleService;
import com.voc.service.insights.engine.model.RoleInfoQueryModel;
import com.voc.service.insights.engine.vo.*;
import com.voc.service.security.api.ICustomInfoService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Title: InsICustomInfoServiceImpl
 * @Package: com.voc.service.insights.engine.impl
 * @Description:
 * @Author: cuick
 * @Date: 2024/7/1 10:24
 * @Version:1.0
 */
@Service
@Primary
public class InsICustomInfoServiceImpl implements ICustomInfoService {
    private static final Logger log = LoggerFactory.getLogger(InsICustomInfoServiceImpl.class);
    @Autowired
    private IInsCustomerInfoService iInsCustomerInfoService;

    @Autowired
    private ClientMappings clientMappings;
    @Autowired
    private IInsRoleService roleService;
    @Resource
    private IInsUserRoleService iInsUserRoleService;

    @Override
    public Object getUserInfo() {
        final UserModel user = ServiceContextHolder.getUser();
        // 获取当前登录用户id
        final String userId1 = user.getUserId();
        // 获取当前登录用户名称
        final String username = user.getUsername();
        // 获取当前登录用户邮箱
        final String email = user.getEmail();
        final String phone = user.getPhone();

        InsUserInfoVo build = InsUserInfoVo.builder()
                .userId(userId1)
                .username(username)
                .email(email)
                .phone(phone)
                .build();
        return build;
    }

    @Override
    public Object getUserPermissions() {
        StopWatch stopWatch = StopWatch.create("getUserPermissions");
        final UserModel user = ServiceContextHolder.getUser();
        final boolean admin = ServiceContextHolder.isAdmin();
        final String clientId = user.getClientId();
        InsUserInfoVo build = InsUserInfoVo.builder()
                .isAdmin(admin)
                .username(user.getUsername())
                .name(user.getFirstname())
                .userId(user.getUserId())
                .employeeId(ObjectUtils.isNotEmpty(user.getEmployeeId())?user.getEmployeeId():null)
                .build();

        try {
            stopWatch.start("getRoleIdByUserId");
            // 获取当前登录用户所属的角色id
            String roleId = iInsUserRoleService.getRoleIdByUserId(user.getUserId(), clientId);
            if(ObjectUtils.isEmpty(roleId)){
                log.info("用户没有关联角色信息");
                return build;
            }
            final RoleAuthVo role = roleService.getRoleInfo(RoleInfoQueryModel.builder().id(roleId).clientId(clientId).build());
            if(ObjectUtils.isEmpty(role)){
                log.info("角色信息为空");
                return build;
            }
            // 获取当前登录用户所属的角色名称
            build.setRoleId(ObjectUtils.isNotEmpty(role) && ObjectUtils.isNotEmpty(role.getId()) ? role.getId() : StrUtil.isNotEmpty(roleId) ? roleId : "");
            build.setRoleName(ObjectUtils.isNotEmpty(role) && ObjectUtils.isNotEmpty(role.getRoleName()) ? role.getRoleName() : "");
            stopWatch.stop();

            //获取权限菜单
            PermissionModel systemPermissions = user.getSystemPermissions();
            if (ObjectUtils.isEmpty(systemPermissions) || systemPermissions.getValues().isEmpty()) {
                return build;
            }
            //菜单
            final List<InsRolePermissionVo> menus = (List<InsRolePermissionVo>) ServiceContextHolder.getMenus();
            //按钮
            final List<InsRolePermissionVo> buttons = (List<InsRolePermissionVo>) ServiceContextHolder.getButtons();
            //下钻
            final List<InsRolePermissionVo> drillDown = (List<InsRolePermissionVo>) ServiceContextHolder.getDrillDown();
            Set<String> collect = buttons.stream().filter(e -> ObjectUtils.isNotEmpty(e.getPermissionKey())).map(e -> e.getPermissionKey()).collect(Collectors.toSet());
            build.setMenus(menus);
            build.setButton(collect);
            if(ObjectUtils.isNotEmpty(drillDown)){
                Set<String> collect1 = drillDown.stream().filter(e -> ObjectUtils.isNotEmpty(e.getPermissionKey())).map(e -> e.getPermissionKey()).collect(Collectors.toSet());
                build.setDrillDowns(collect1);
            }
            if (log.isDebugEnabled()) {
                log.debug("获取用户:[{}]权限信息:{}", user.getUsername(), JSONObject.toJSONString(build));
            }
        } finally {
//            stopWatch.stop();
            log.info("用户:[{}]获取权限信息耗时:{}ms", user.getUsername(),stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        }

        return build;
    }
}
