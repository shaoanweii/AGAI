package com.voc.service.insights.engine.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsStaSysDepartService;
import com.voc.service.insights.engine.entity.InsStaSysDepartEntity;
import com.voc.service.insights.engine.mapper.InsStaSysDepartMapper;
import com.voc.service.insights.engine.model.InsAccountInfoModel;
import com.voc.service.insights.engine.model.InsSysDepartModel;
import com.voc.service.insights.engine.vo.InsSysDepartVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InsStaSysDepartServiceImpl extends ServiceImpl<InsStaSysDepartMapper, InsStaSysDepartEntity>
        implements IInsStaSysDepartService {



    @Override
    @SwitchClientDS(objectAttribute = "accountInfoModel.clientId")
    public List<InsSysDepartModel> getClientDepartList(InsAccountInfoModel accountInfoModel) {
        QueryWrapper<InsStaSysDepartEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsStaSysDepartEntity::getStatus, "1");
        List<InsStaSysDepartEntity> list = this.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(l->InsSysDepartModel.builder().name(l.getName()).value(l.getId()).code(l.getCode()).build()).toList();
    }

    @Override
    @SwitchClientDS
    public List<InsSysDepartVo> getDepartList(String clientId) {
        QueryWrapper<InsStaSysDepartEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsStaSysDepartEntity::getStatus, "1");
        List<InsStaSysDepartEntity> list = this.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(l->InsSysDepartVo.builder().name(l.getName()).id(l.getId()).code(l.getCode()).parentId(l.getParentId()).build()).toList();
    }

    @Override
    @SwitchClientDS
    public List<InsSysDepartVo> getDepartSubtree(String clientId, String deptId) {
        List<InsStaSysDepartEntity> list = this.baseMapper.findSubDepartListByDeptId(deptId);
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream()
                .map(l -> InsSysDepartVo.builder()
                        .name(l.getName())
                        .id(l.getId())
                        .code(l.getCode())
                        .parentId(l.getParentId())
                        .build())
                .toList();
    }

    @Override
    @SwitchClientDS
    public List<InsSysDepartVo> getDepartAncestorList(String clientId, String deptId) {
        List<InsStaSysDepartEntity> list = this.baseMapper.findParentDepartListByDeptId(deptId);
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream()
                .map(l -> InsSysDepartVo.builder()
                        .name(l.getName())
                        .id(l.getId())
                        .code(l.getCode())
                        .parentId(l.getParentId())
                        .build())
                .toList();
    }


}
