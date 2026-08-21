package com.voc.service.insights.engine.data.dao.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.data.dao.InsDataResourceDescDao;
import com.voc.service.insights.engine.data.entity.InsDataResourceDescEntity;
import com.voc.service.insights.engine.data.impl.converts.InsDataResourceDescConvertService;
import com.voc.service.insights.engine.data.mapper.InsDataResourceDescMapper;
import com.voc.service.insights.engine.model.data.InsDataResourceDescModel;
import com.voc.service.insights.engine.vo.data.ResourceDescDto;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/13 下午3:18
 * @描述:
 **/
@Repository
public class InsDataResourceDescDaoImpl extends ServiceImpl<InsDataResourceDescMapper, InsDataResourceDescEntity>  implements InsDataResourceDescDao {
    @Resource
    private InsDataResourceDescConvertService convertService;
    private QueryWrapper<InsDataResourceDescEntity> createQueryWrapper(InsDataResourceDescModel model) {
        InsDataResourceDescEntity entity = convertService.convertTo(model);
        QueryWrapper<InsDataResourceDescEntity> queryWrapper = new QueryWrapper<>(entity);
        LambdaQueryWrapper<InsDataResourceDescEntity> lambdaQueryWrapper = queryWrapper.lambda();
        if (StrUtil.isNotBlank(model.getNameDescFilter())) {
            lambdaQueryWrapper.eq(InsDataResourceDescEntity::getName, model.getNameDescFilter());
        }
        if (StrUtil.isNotBlank(model.getNotIdFilter())) {
            lambdaQueryWrapper.ne(InsDataResourceDescEntity::getId, model.getNotIdFilter());
        }
        if (CollUtil.isNotEmpty(model.getStatusFilters())) {
            lambdaQueryWrapper.in(InsDataResourceDescEntity::getStatus, model.getStatusFilters());
        }
        model.orderBy(queryWrapper);
        return queryWrapper;
    }


    @Override
//    @SwitchClientDS(objectAttribute = "model.customer")
    public List<ResourceDescDto> queryCustomByParam(InsDataResourceDescModel model) {
        List<InsDataResourceDescEntity> list = this.list(this.createQueryWrapper(model));
        return list.stream().map(e -> convertService.convertToDto(e)).collect(Collectors.toList());
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.customer")
    public List<ResourceDescDto> findAllDataResourceDesc(InsDataResourceDescModel model) {
        QueryWrapper<InsDataResourceDescEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsDataResourceDescEntity::getStatus, "Enabled");
        List<InsDataResourceDescEntity> list = this.list(queryWrapper);
        if(ObjectUtils.isEmpty(list)){
            log.warn("暂无数据源详情");
            return List.of();
        }
        return list.stream().map(e -> convertService.convertToDto(e)).collect(Collectors.toList());
    }

}
