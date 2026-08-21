package com.voc.service.insights.engine.impl.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.model.auth.PermissionModel;
import com.voc.service.insights.engine.api.IInsRoleService;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import com.voc.service.insights.engine.api.model.ClientModel;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.model.UserRoleQueryModel;
import com.voc.service.insights.engine.vo.InsRolePermissionVo;
import com.voc.service.insights.engine.vo.UserRoleInfoVo;
import com.voc.service.security.service.AbstractIUserService;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName UserService
 * @Description ckcui
 * @createTime 2023年10月09日 14:54
 * @Copyright futong
 */
@Service
@Primary
public class InsightsEngineUserService extends AbstractIUserService {
    private static final Logger log = LoggerFactory.getLogger(InsightsEngineUserService.class);
    @Autowired
    IInsRoleService insRoleService;

    public InsightsEngineUserService() {
        log.info("--->> init {}", this.getClass().getSimpleName());
    }

    @Override
    public Optional<PermissionModel> readBusinessPermissions(UserModel user) {
        //
        log.trace("使用洞察引擎系统实现服务 ,{}", "readBusinessPermissions");
        PermissionModel model = PermissionModel.builder().build();
        model.setValues(InsightsConstants.PERMS_BIZ_USER_INFO_KEY
                , Arrays.asList(ClientModel.builder().id("test_id").build()));
        model.setValues(InsightsConstants.PERMS_BIZ_USER_INFO_KEY
                , Arrays.asList(ClientModel.builder().id("test_id").build()));
        model.setValues(InsightsConstants.PERMS_BIZ_USER_INFO_KEY
                , Arrays.asList(ClientModel.builder().id("test_id").build()));
        model.setValues(InsightsConstants.PERMS_BIZ_USER_INFO_KEY
                , Arrays.asList(ClientModel.builder().id("test_id").build()));
        model.setValues(InsightsConstants.PERMS_BIZ_USER_INFO_KEY
                , Arrays.asList(ClientModel.builder().id("test_id").build()));

        //TODO fanrong 需要补充渠道用户权限
        model.setValues(InsightsConstants.PERMS_BIZ_CHANEL_DATA_KEY, Set.of(InsChannelInfoModel.builder().id("a9f34253a58e855f0fa8dee5164c6764").build()));
        //读取车系权限
//        model.setValues(InsightsConstants.PERMS_CAR_SERIES_KEY, ClientModel.builder().id("test_id").build());


        return Optional.of(model);
    }

    @Override
    public Optional<PermissionModel> readSystemPermissions(UserModel user) {
        log.trace("使用洞察引擎系统实现服务 ,{}", "readSystemPermissions");
        PermissionModel model = PermissionModel.builder().build();
        final UserRoleQueryModel userRole = UserRoleQueryModel.builder().userId(user.getUserId()).clientId(user.getClientId()).tree(true).admin(user.getAdmin().equalsIgnoreCase(Boolean.TRUE.toString()) ? Boolean.TRUE : Boolean.FALSE).build();
        log.info("获取权限入参:{}", JSONObject.toJSONString(userRole));
        //读取用户菜单+按钮权限
        UserRoleInfoVo userRoleInfoVo = insRoleService.queryUserPermission(userRole);
        if (ObjectUtils.isEmpty(userRoleInfoVo)) {
            log.info("当前用户未授权菜单及按钮权限");
        } else {
            //菜单权限
            List<InsRolePermissionVo> roleAuthTreeList = userRoleInfoVo.getInsRolePermissionVos();
            Map<Integer, List<InsRolePermissionVo>> collect = roleAuthTreeList.stream().collect(Collectors.groupingBy(InsRolePermissionVo::getPermissionType));
            if (ObjectUtils.isEmpty(collect) || !collect.containsKey(Integer.valueOf(2))) {
                log.info("当前用户无菜单权限");
            } else {
                //设置当前用户可用菜单集合
                List<InsRolePermissionVo> menu = collect.get(Integer.valueOf(2));
                model.setValues(InsightsConstants.PERMS_MENUS_KEY, menu);
                model.setValues(InsightsConstants.PERMS_MENUS_TREE_KEY, userRoleInfoVo.getRoleAuthListVoList());
                if (collect.containsKey(1)) {
                    List<InsRolePermissionVo> button = collect.get(Integer.valueOf(1));
                    model.setValues(InsightsConstants.PERMS_BUTTON_KEY, button);
                } else {
                    log.info("当前用户无按钮权限");
                    model.setValues(InsightsConstants.PERMS_BUTTON_KEY, List.of());
                }
            }
        }
        return Optional.of(model);
    }

    @Override
    public Optional<PermissionModel> readAccessPermissions(UserModel user) {
        log.info("使用洞察引擎系统实现服务 ,{}", "readAccessPermissions");
        //读取访问路径
        PermissionModel model = PermissionModel.builder().build();
        PermissionModel systemPermissions = user.getSystemPermissions();
        if (ObjectUtil.isNotNull(systemPermissions) && CollUtil.isNotEmpty(systemPermissions.getValues()) && systemPermissions.getValues().containsKey(InsightsConstants.PERMS_MENUS_KEY)) {
            //读取访问路径
            List<InsRolePermissionVo> menus = systemPermissions.getValue(InsightsConstants.PERMS_MENUS_KEY);
            List<InsRolePermissionVo> buttons = systemPermissions.getValue(InsightsConstants.PERMS_BUTTON_KEY);
            List<InsRolePermissionVo> all = new ArrayList<>();
            all.addAll(menus);
            all.addAll(buttons);
            //读取单路径访问路径
            final Set<String> paths = all.stream().filter(e -> ObjectUtil.isNotNull(e.getApiUrl()) && !e.getApiUrl().contains(",")).map(InsRolePermissionVo::getApiUrl).collect(Collectors.toSet());
            all.stream().filter(e -> ObjectUtil.isNotNull(e.getApiUrl()) && e.getApiUrl().contains(",")).forEach(e -> {
                String[] split = e.getApiUrl().split(",");
                paths.addAll(Arrays.asList(split));
            });
            if (CollUtil.isNotEmpty(paths)) {
                model.setValues("paths", paths);
                log.debug(" {} paths {},", user.getUserId(), paths);
            } else {
                log.debug("当前用户无API访问数据");
            }
            return Optional.of(model);
        } else {
            log.error("当前用户无菜单访问权限 {}", user.getUserId());
        }
        return Optional.empty();
    }
}
