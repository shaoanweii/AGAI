package com.voc.service.insights.engine.model.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.model.IInsModelDescService;
import com.voc.service.insights.engine.model.entity.InsModelDescEntity;
import com.voc.service.insights.engine.model.impl.converts.InsModelDescConvertService;
import com.voc.service.insights.engine.model.mapper.InsModelDescMapper;
import com.voc.service.insights.engine.model.model.InsModelDescModel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (InsModelDesc)表服务实现类
 *
 * @author leiww
 * @since 2024-02-21 15:35:23
 */
@Service
public class InsModelDescServiceImpl extends ServiceImpl<InsModelDescMapper, InsModelDescEntity> implements IInsModelDescService {

    @Resource
    private InsModelDescConvertService convertService;

    private QueryWrapper<InsModelDescEntity> createQueryWrapper(InsModelDescModel model) {
        InsModelDescEntity entity = convertService.convertTo(model);
        QueryWrapper<InsModelDescEntity> queryWrapper = new QueryWrapper<>(entity);
        LambdaQueryWrapper<InsModelDescEntity> lambdaQueryWrapper = queryWrapper.lambda();
        model.orderBy(queryWrapper);
        return queryWrapper;
    }

    @Override
    public Result<?> queryBySelect(InsModelDescModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsModelDescEntity> entityList = this.list(this.createQueryWrapper(model));
        List<InsModelDescModel> list = convertService.convertToList(entityList);
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
    }

    @Override
    public Boolean insert(InsModelDescModel model) {
        model.setId(IdWorker.getId());
        return this.save(convertService.convertTo(model));
    }

    @Override
    public Boolean update(InsModelDescModel model) {
        return this.updateById(convertService.convertTo(model));
    }

    @Override
    public Boolean deleteByIds(List<Serializable> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public InsModelDescModel queryById(Serializable id) {
        InsModelDescEntity entity = this.getById(id);
        return convertService.convertTo(entity);
    }

    @Override
    public List<InsModelDescModel> queryByParam(InsModelDescModel model) {
        List<InsModelDescEntity> list = this.list(this.createQueryWrapper(model));
        return list.stream().map(e -> convertService.convertTo(e)).collect(Collectors.toList());
    }
}

