package com.voc.service.insights.engine.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsProvinceAreaService;
import com.voc.service.insights.engine.entity.InsProvinceAreaEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.mapper.InsProvinceAreaMapper;
import com.voc.service.insights.engine.model.InsProvinceArea;
import com.voc.service.insights.engine.model.InsProvinceAreaModel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 区域城市信息表(InsProvinceArea)表服务实现类
 *
 * @author leiww
 * @since 2024-01-25 13:56:33
 */
@Service
public class InsProvinceAreaServiceImpl extends ServiceImpl<InsProvinceAreaMapper, InsProvinceAreaEntity> implements IInsProvinceAreaService {

    @Resource
    private InsConvertMapperService insConvertMapperService;

    private QueryWrapper<InsProvinceAreaEntity> createQueryWrapper(InsProvinceAreaModel model) {
        InsProvinceAreaEntity entity = insConvertMapperService.convertTo(model);
        QueryWrapper<InsProvinceAreaEntity> queryWrapper = new QueryWrapper<>(entity);
        return queryWrapper;
    }

    @Override
    public Result<?> queryBySelect(InsProvinceAreaModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsProvinceAreaEntity> entityList = this.list(this.createQueryWrapper(model));
        List<InsProvinceAreaModel> list = insConvertMapperService.convertToList(entityList);
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
    }

    @Override
    public Boolean insert(InsProvinceAreaModel model) {
        return this.save(insConvertMapperService.convertTo(model));
    }

    @Override
    public Boolean update(InsProvinceAreaModel model) {
        return this.updateById(insConvertMapperService.convertTo(model));
    }

    @Override
    public Boolean deleteByIds(List<Serializable> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public InsProvinceAreaModel queryById(Serializable id) {
        InsProvinceAreaEntity entity = this.getById(id);
        return insConvertMapperService.convertTo(entity);
    }

    @Override
    public List<InsProvinceAreaModel> queryByParam(InsProvinceAreaModel model) {
        List<InsProvinceAreaEntity> list = this.list(this.createQueryWrapper(model));
        return list.stream().map(e -> insConvertMapperService.convertTo(e)).collect(Collectors.toList());
    }
    
    @Override
    public List<InsProvinceArea> getProvinceList() {
        // 查询所有数据
        List<InsProvinceAreaEntity> allEntities = this.list();
        
        // 按省份编码去重，只保留每个省份的一条记录
        return allEntities.stream()
                .collect(Collectors.toMap(
                        InsProvinceAreaEntity::getProvinceCode, // 按省份编码分组
                        entity -> entity, // 保留实体
                        (existing, replacement) -> existing // 重复时保留第一个
                ))
                .values() // 获取去重后的实体集合
                .stream()
                .map(entity -> {
                    // 只设置省份编码和名称
                    InsProvinceArea model = new InsProvinceArea();
                    model.setId(entity.getId());
                    model.setProvinceCode(entity.getProvinceCode());
                    model.setProvinceName(entity.getProvinceName());
                    return model;
                })
                .collect(Collectors.toList());
    }
}

