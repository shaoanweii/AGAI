package com.voc.service.insights.engine.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.IInsCustomerPermissionService;
import com.voc.service.insights.engine.api.IInsMenuPermissionService;
import com.voc.service.insights.engine.api.IInsRoleService;
import com.voc.service.insights.engine.entity.InsCustomerPermissionEntity;
import com.voc.service.insights.engine.mapper.InsCustomerPermissionMapper;
import com.voc.service.insights.engine.model.InsRoleQueryModel;
import com.voc.service.insights.engine.vo.InsRolePermissionVo;
import com.voc.service.insights.engine.vo.RoleAuthTree;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InsCustomerPermissionServiceImpl extends ServiceImpl<InsCustomerPermissionMapper, InsCustomerPermissionEntity> implements IInsCustomerPermissionService {


    @Resource
    private IInsMenuPermissionService iInsMenuPermissionService;

    @Resource
    private IInsRoleService iInsRoleService;

    @Override
    public Boolean saveOrUpdate(String clientId, List<String> permissionIdList) {

        if (CollectionUtil.isEmpty(permissionIdList)) {
            return Boolean.FALSE;
        }
        List<InsRolePermissionVo> permissionModelList = iInsMenuPermissionService.getMenuPermission();
        if (CollectionUtil.isEmpty(permissionModelList)) {
            return Boolean.FALSE;
        }
        QueryWrapper<InsCustomerPermissionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("client_id", clientId);
        this.remove(queryWrapper);
        List<InsCustomerPermissionEntity> insCustomerPermissionEntities = new ArrayList<>();
        Map<String, InsRolePermissionVo> permissionModelMap = permissionModelList.stream().collect(Collectors.toMap(InsRolePermissionVo::getId, Function.identity()));
        for (String str : permissionIdList) {
            InsCustomerPermissionEntity entity = new InsCustomerPermissionEntity();
            entity.setClientId(clientId);
            entity.setCreateTime(LocalDateTime.now());
            entity.setPermissionId(str);
            entity.setId(IdWorker.getId());
            if (permissionModelMap.containsKey(str)) {
                InsRolePermissionVo insRolePermissionModel = permissionModelMap.get(str);
                entity.setPermissionType(insRolePermissionModel.getPermissionType());
                entity.setButtonPermission(insRolePermissionModel.getButtonCode());
            }
            insCustomerPermissionEntities.add(entity);
        }
        if (CollectionUtil.isNotEmpty(insCustomerPermissionEntities)) {
            this.saveBatch(insCustomerPermissionEntities);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<RoleAuthTree> queryCustomerPermissionList(String clientId) {

        QueryWrapper<InsCustomerPermissionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("client_id", clientId);
        List<InsCustomerPermissionEntity> customerPermissionEntities = this.list(queryWrapper);
        if (CollectionUtil.isEmpty(customerPermissionEntities)) {
            customerPermissionEntities = new ArrayList<>();
        }
        List<String> permissionList = customerPermissionEntities.stream().map(InsCustomerPermissionEntity::getPermissionId).toList();
        InsRoleQueryModel queryModel = new InsRoleQueryModel();
        queryModel.setPermissionIdList(permissionList);
        return iInsRoleService.queryMenuPermissionList(queryModel);
    }
}