package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.AysExtAttrsMappingValuesService;
import com.voc.service.analysis.core.v2.entity.AysExtAttrsMappingValuesEntity;
import com.voc.service.analysis.core.v2.mapper.AysExtAttrsMappingValuesMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class AysExtAttrsMappingValuesServiceImpl extends
        ServiceImpl<AysExtAttrsMappingValuesMapper, AysExtAttrsMappingValuesEntity>
        implements AysExtAttrsMappingValuesService {


    @Override
    @SwitchClientDS
    public Map<String, String> getAttrs(String clientId) {
        QueryWrapper<AysExtAttrsMappingValuesEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        List<AysExtAttrsMappingValuesEntity> aysExtAttrsMappingValuesEntities = this.baseMapper.selectList(queryWrapper);
        if (CollUtil.isNotEmpty(aysExtAttrsMappingValuesEntities)) {
            Map<String, String> map = new LinkedHashMap<>();
            for (AysExtAttrsMappingValuesEntity aysExtAttrsMappingValuesEntity:aysExtAttrsMappingValuesEntities){
                map.put(aysExtAttrsMappingValuesEntity.getAttr(),aysExtAttrsMappingValuesEntity.getName());
            }
            return map;
        }
        return new HashMap<>();
    }

}
