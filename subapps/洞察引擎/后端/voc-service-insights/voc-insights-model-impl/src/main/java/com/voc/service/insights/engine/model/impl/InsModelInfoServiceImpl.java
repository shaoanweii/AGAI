package com.voc.service.insights.engine.model.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.model.IInsModelInfoService;
import com.voc.service.insights.engine.model.entity.InsModelInfoEntity;
import com.voc.service.insights.engine.model.impl.converts.InsModelInfoConvertService;
import com.voc.service.insights.engine.model.mapper.InsModelInfoMapper;
import com.voc.service.insights.engine.model.model.InsModelInfoModel;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 模型配置数据(InsModelInfo)表服务实现类
 *
 * @author leiww
 * @since 2024-02-21 14:57:10
 */
@Service
public class InsModelInfoServiceImpl extends ServiceImpl<InsModelInfoMapper, InsModelInfoEntity> implements IInsModelInfoService {

    @Resource
    private InsModelInfoConvertService convertService;

    private QueryWrapper<InsModelInfoEntity> createQueryWrapper(InsModelInfoModel model) {
        InsModelInfoEntity entity = convertService.convertTo(model);
        QueryWrapper<InsModelInfoEntity> queryWrapper = new QueryWrapper<>(entity);
        LambdaQueryWrapper<InsModelInfoEntity> lambdaQueryWrapper = queryWrapper.lambda();
        if (CollUtil.isNotEmpty(model.getModelTypeFilters())) {
            lambdaQueryWrapper.in(InsModelInfoEntity::getModelType, model.getModelTypeFilters());
        }
        if (CollUtil.isNotEmpty(model.getFormatFilters())) {
            lambdaQueryWrapper.in(InsModelInfoEntity::getFormat, model.getFormatFilters());
        }
        if (StrUtil.isNotBlank(model.getNameFilter())) {
            lambdaQueryWrapper.like(InsModelInfoEntity::getModelName, model.getNameFilter());
        }
        model.orderBy(queryWrapper);
        return queryWrapper;
    }

    @Override
    public Result<?> queryBySelect(InsModelInfoModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsModelInfoEntity> entityList = this.list(this.createQueryWrapper(model));
        List<InsModelInfoModel> list = convertService.convertToList(entityList);
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
    }

    @Override
    public Boolean insert(InsModelInfoModel model) {
        this.checkParameter(model);
        model.setId(IdWorker.getId());
        return this.save(convertService.convertTo(model));
    }

    @Override
    public Boolean update(InsModelInfoModel model) {
        return this.updateById(convertService.convertTo(model));
    }

    @Override
    public Boolean deleteByIds(List<Serializable> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public InsModelInfoModel queryById(Serializable id) {
        InsModelInfoEntity entity = this.getById(id);
        return convertService.convertTo(entity);
    }

    @Override
    public List<InsModelInfoModel> queryByParam(InsModelInfoModel model) {
        List<InsModelInfoEntity> list = this.list(this.createQueryWrapper(model));
        return list.stream().map(e -> convertService.convertTo(e)).collect(Collectors.toList());
    }

    protected void checkParameter(InsModelInfoModel model) {
        Assert.hasLength(model.getModelName(), "模型名称不允许为空");
        Assert.isTrue(!model.getModelName().isEmpty()
                && model.getModelName().length() <= 20, "模型名称长度不符,长度不允许超过20个字");

    }
}

