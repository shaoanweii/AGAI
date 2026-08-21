package com.voc.service.insights.engine.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.util.IdWorker;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsUserRoleService;
import com.voc.service.insights.engine.entity.InsRoleEntity;
import com.voc.service.insights.engine.entity.InsUserRoleEntity;
import com.voc.service.insights.engine.mapper.InsRoleMapper;
import com.voc.service.insights.engine.mapper.InsUserRoleMapper;
import com.voc.service.insights.engine.model.InsUserRoleModel;
import com.voc.service.insights.engine.vo.InsUserRoleVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InsUserRoleServiceImpl extends ServiceImpl<InsUserRoleMapper, InsUserRoleEntity> implements IInsUserRoleService {


    @Resource
    private InsRoleMapper insRoleMapper;

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public Boolean saveOrUpdate(InsUserRoleModel model) {

        QueryWrapper<InsUserRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", model.getUserId());
        InsUserRoleEntity userRoleEntity = this.getOne(queryWrapper);
        if (ObjectUtil.isNull(userRoleEntity)) {
            InsUserRoleEntity insUserRole = new InsUserRoleEntity();
            BeanUtil.copyProperties(model, insUserRole);
            insUserRole.setId(IdWorker.getId());
            insUserRole.setCreateTime(LocalDateTime.now());
            return this.save(insUserRole);
        } else {
            userRoleEntity.setRoleId(model.getRoleId());
            userRoleEntity.setCreateTime(LocalDateTime.now());
            return this.updateById(userRoleEntity);
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public Boolean deleteRole(InsUserRoleModel insUserRoleModel) {
        QueryWrapper<InsUserRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsUserRoleEntity::getUserId, insUserRoleModel.getUserId());
        return this.remove(queryWrapper);
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public List<InsUserRoleVo> getRoleInfo(InsUserRoleModel model) {
        List<InsUserRoleVo> userRoleVoList = new ArrayList<>();
        try {
            QueryWrapper<InsUserRoleEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("user_id", model.getUserIdList());
            List<InsUserRoleEntity> list = this.list(queryWrapper);
            if (ObjectUtil.isNull(list)) {
                return userRoleVoList;
            }
            List<String> roleIdList = list.stream().map(InsUserRoleEntity::getRoleId).collect(Collectors.toList());
            QueryWrapper<InsRoleEntity> entityQueryWrapper = new QueryWrapper<>();
            entityQueryWrapper.in("id", roleIdList);
            List<InsRoleEntity> insRoleEntityList = insRoleMapper.selectList(entityQueryWrapper);
            if (ObjectUtil.isNull(insRoleEntityList)) {
                return userRoleVoList;
            }
            Map<String, InsRoleEntity> insRoleEntityMap = insRoleEntityList.stream().collect(Collectors.toMap(InsRoleEntity::getId, Function.identity()));
            for (InsUserRoleEntity entity : list) {
                if (StringUtils.isEmpty(entity.getRoleId())) {
                    continue;
                }
                InsUserRoleVo userRoleVo = new InsUserRoleVo();
                if (insRoleEntityMap.containsKey(entity.getRoleId())) {
                    InsRoleEntity insRoleEntity = insRoleEntityMap.get(entity.getRoleId());
                    userRoleVo.setRoleName(insRoleEntity.getRoleName());
                    userRoleVo.setRoleType(insRoleEntity.getRoleType());
                }
                userRoleVo.setUserId(entity.getUserId());
                userRoleVo.setRoleId(entity.getRoleId());
                userRoleVoList.add(userRoleVo);
            }
        } catch (Exception e) {
            return userRoleVoList;
        }
        return userRoleVoList;
    }

    @Override
    @SwitchClientDS
    public String getRoleIdByUserId(String userId, String clientId) {
        QueryWrapper<InsUserRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        InsUserRoleEntity insUserRoleEntity = baseMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNotEmpty(insUserRoleEntity)) {
            return insUserRoleEntity.getRoleId();
        }
        return null;
    }

    @Override
    public Integer getCountByRole(String roleId) {
        QueryWrapper<InsUserRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        List<InsUserRoleEntity> insUserRoleEntities = baseMapper.selectList(queryWrapper);
        return insUserRoleEntities.size();
    }

    @Override
    @SwitchClientDS
    public List<String> getUserIdList(String roleId, String clientId) {
        QueryWrapper<InsUserRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        List<InsUserRoleEntity> insUserRoleEntities = baseMapper.selectList(queryWrapper);
        if (ObjectUtil.isEmpty(insUserRoleEntities)) {
            return null;
        }
        return insUserRoleEntities.stream().map(InsUserRoleEntity::getUserId).toList();
    }

    @Override
    public Boolean batchSaveOrUpdate(InsUserRoleModel models) {
        QueryWrapper<InsUserRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", models.getRoleId());
        this.baseMapper.delete(queryWrapper);
        List<InsUserRoleEntity> insUserRoleEntityList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(models.getUserIdList())) {
            for (String userId : models.getUserIdList()) {
                InsUserRoleEntity insUserRole = new InsUserRoleEntity();
                insUserRole.setId(IdWorker.getId());
                insUserRole.setUserId(userId);
                insUserRole.setRoleId(models.getRoleId());
                insUserRole.setCreateTime(LocalDateTime.now());
                insUserRoleEntityList.add(insUserRole);
            }
            return this.saveBatch(insUserRoleEntityList);
        }
        return false;
    }

    @Override
    @SwitchClientDS(objectAttribute = "models.clientId")
    public Boolean deleteRoleByRoleId(InsUserRoleModel models) {
        QueryWrapper<InsUserRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", models.getRoleId());
        return this.remove(queryWrapper);
    }

}