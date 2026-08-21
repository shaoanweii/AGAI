package com.voc.service.insights.engine.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsMenuPermissionService;
import com.voc.service.insights.engine.entity.InsMenuPermissionEntity;
import com.voc.service.insights.engine.mapper.InsMenuPermissionMapper;
import com.voc.service.insights.engine.model.InsMenuPermissionsModel;
import com.voc.service.insights.engine.vo.InsRolePermissionVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InsMenuPermissionServiceImpl extends ServiceImpl<InsMenuPermissionMapper, InsMenuPermissionEntity> implements IInsMenuPermissionService {

    private static final Logger log = LoggerFactory.getLogger(InsMenuPermissionServiceImpl.class);
    @Override
    public List<InsRolePermissionVo> getMenuPermission() {
        return this.baseMapper.getMenuPermission();
    }

    @Override
    public List<InsRolePermissionVo> getUserMenuPermission(List<String> permissionIdList) {
        return this.baseMapper.getUserMenuPermission(permissionIdList);
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public Boolean updateUserMenuPermission(InsMenuPermissionsModel model) {

        String clientId = model.getClientId();
        int deleteMenu = this.baseMapper.deleteMenuPermission(clientId);
        log.info("删除客户菜单权限:{}", deleteMenu);
        int deleteButton = this.baseMapper.deleteButtonPermission(clientId);
        log.info("删除客户按钮权限:{}", deleteButton);
        int updateUserButton = this.baseMapper.updateUserButtonPermission(clientId);
        log.info("修改客户按钮权限:{}", updateUserButton);
        int updateUserMenu = this.baseMapper.updateUserMenuPermission(clientId);
        log.info("修改客户菜单权限:{}", updateUserMenu);
        return Boolean.TRUE;
    }
}