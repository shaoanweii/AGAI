package com.voc.service.insights.engine.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.insights.engine.api.IInsRoleRelationPermissionService;
import com.voc.service.insights.engine.entity.InsRoleRelationPermissionEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.mapper.InsRoleRelationPermissionMapper;
import com.voc.service.insights.engine.model.InsRoleRelationPermissionModel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class InsRoleRelationPermissionServiceImpl extends ServiceImpl<InsRoleRelationPermissionMapper, InsRoleRelationPermissionEntity> implements IInsRoleRelationPermissionService {


    @Resource
    private InsConvertMapperService insConvertMapperService;

    @Override
    public int insertBatch(List<InsRoleRelationPermissionModel> model) {
        return this.baseMapper.insertBatch(model);
    }

    @Override
    public int delete(String roleId) {
        QueryWrapper<InsRoleRelationPermissionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return this.baseMapper.delete(queryWrapper);
    }

    @Override
    public List<InsRoleRelationPermissionModel> queryList(String roleId) {
        QueryWrapper<InsRoleRelationPermissionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        List<InsRoleRelationPermissionEntity> insRoleRelationPermissionEntityList = this.baseMapper.selectList(queryWrapper);
        return insConvertMapperService.roleRelationEntityListConvertVoList(insRoleRelationPermissionEntityList);
    }
}