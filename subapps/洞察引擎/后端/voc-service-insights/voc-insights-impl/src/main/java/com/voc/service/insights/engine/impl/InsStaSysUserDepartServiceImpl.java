package com.voc.service.insights.engine.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsStaSysUserDepartService;
import com.voc.service.insights.engine.entity.StaSysUserDepartEntity;
import com.voc.service.insights.engine.mapper.InsStaSysUserDepartMapper;
import com.voc.service.insights.engine.model.InsStaSysUserDepartModel;
import com.voc.service.insights.engine.vo.InsStaSysUserDepartVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Service
public class InsStaSysUserDepartServiceImpl extends ServiceImpl<InsStaSysUserDepartMapper, StaSysUserDepartEntity>
        implements IInsStaSysUserDepartService {
    private static final Logger log = LoggerFactory.getLogger(InsStaSysUserDepartServiceImpl.class);

    @Override
    @SwitchClientDS
    public Boolean saveOrUpdateDepart(InsStaSysUserDepartModel sysUserDepartModel, String clientId) {
        if (ObjectUtil.isNotNull(sysUserDepartModel)) {
            StaSysUserDepartEntity staSysUserDepartEntity = new StaSysUserDepartEntity();
            BeanUtil.copyProperties(sysUserDepartModel, staSysUserDepartEntity);
            return this.saveOrUpdate(staSysUserDepartEntity);
        }
        return false;
    }

    @Override
    @SwitchClientDS
    public List<InsStaSysUserDepartVo> findStaSysUserDepartList(InsStaSysUserDepartModel InsStaSysUserDepartModel, String clientId) {
        List<StaSysUserDepartEntity> staSysUserDepartEntityList = this.baseMapper.findStaSysUserDepartList(InsStaSysUserDepartModel);
        if (ObjectUtil.isEmpty(staSysUserDepartEntityList)) {
            log.info("暂无角色与部门关联关系");
            return List.of();
        }

        return staSysUserDepartEntityList.stream().map(e -> {
            InsStaSysUserDepartVo staSysUserDepartVo = new InsStaSysUserDepartVo();
            BeanUtil.copyProperties(e, staSysUserDepartVo);
            return staSysUserDepartVo;
        }).toList();
    }

    @Override
    @SwitchClientDS
    public List<String> getDepIdByUserIdList(List<String> depIdList, String clientId) {
        QueryWrapper<StaSysUserDepartEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("dep_id", depIdList);
        List<StaSysUserDepartEntity> list = this.list(queryWrapper);
        if (ObjectUtil.isNull(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(StaSysUserDepartEntity::getUserId).toList();
    }

    @Override
    @SwitchClientDS
    public List<String> findUserIdByDepId(String deptId, String clientId) {
        Assert.hasLength(deptId, "部门ID不能为空");
        return this.baseMapper.findDownAllHierarchical(deptId);
    }

    @Override
    @SwitchClientDS
    public List<InsStaSysUserDepartVo> findUserDepartMapping(String clientId) {
        final List<StaSysUserDepartEntity> downAllHierarchicalList = this.baseMapper.findDownAllHierarchicalList();
        if(ObjectUtil.isEmpty(downAllHierarchicalList)){
            log.info("暂无部门与用户关系");
            return List.of();
        }
        return downAllHierarchicalList.stream().map(e -> {
            InsStaSysUserDepartVo staSysUserDepartVo = new InsStaSysUserDepartVo();
            BeanUtil.copyProperties(e, staSysUserDepartVo);
            return staSysUserDepartVo;
        }).toList();
    }


}
