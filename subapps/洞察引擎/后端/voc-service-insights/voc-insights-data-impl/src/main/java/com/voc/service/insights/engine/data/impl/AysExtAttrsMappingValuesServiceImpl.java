package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.AysExtAttrsMappingValuesService;
import com.voc.service.insights.engine.data.entity.AysExtAttrsMappingValuesEntity;
import com.voc.service.insights.engine.data.mapper.AysExtAttrsMappingValuesMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class AysExtAttrsMappingValuesServiceImpl extends
        ServiceImpl<AysExtAttrsMappingValuesMapper, AysExtAttrsMappingValuesEntity>
        implements AysExtAttrsMappingValuesService {


    @SwitchClientDS(datasource ="starrock_dndc")
    @Override
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
