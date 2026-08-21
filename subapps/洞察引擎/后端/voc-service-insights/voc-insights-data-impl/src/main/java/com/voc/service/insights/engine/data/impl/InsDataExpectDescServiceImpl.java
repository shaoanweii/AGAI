package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.data.IInsDataExpectDescService;
import com.voc.service.insights.engine.data.entity.InsDataExpectDescEntity;
import com.voc.service.insights.engine.data.impl.converts.InsDataExpectDescConvertService;
import com.voc.service.insights.engine.data.mapper.InsDataExpectDescMapper;
import com.voc.service.insights.engine.model.data.InsDataExpectDescModel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 语料库数据详情(InsDataExpectDesc)表服务实现类
 *
 * @author leiww
 * @since 2024-03-05 14:51:15
 */
@Service
public class InsDataExpectDescServiceImpl extends ServiceImpl<InsDataExpectDescMapper, InsDataExpectDescEntity> implements IInsDataExpectDescService {

    @Resource
    private InsDataExpectDescConvertService convertService;

    private QueryWrapper<InsDataExpectDescEntity> createQueryWrapper(InsDataExpectDescModel model) {
        InsDataExpectDescEntity entity = convertService.convertTo(model);
        QueryWrapper<InsDataExpectDescEntity> qw = new QueryWrapper<>(entity);
        LambdaQueryWrapper<InsDataExpectDescEntity> queryWrapper = qw.lambda();
        if (CollUtil.isNotEmpty(model.getExpectFilters())) {
            queryWrapper.in(InsDataExpectDescEntity::getExpectId, model.getExpectFilters());
        }
        model.orderBy(qw);
        return qw;
    }

    @Override
    public Result<?> queryBySelect(InsDataExpectDescModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsDataExpectDescEntity> entityList = this.list(this.createQueryWrapper(model));
        List<InsDataExpectDescModel> list = convertService.convertToList(entityList);
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
    }

    @Override
    public Boolean insert(InsDataExpectDescModel model) {
        model.setId(IdWorker.getId());
        return this.save(convertService.convertTo(model));
    }

    @Override
    public Boolean update(InsDataExpectDescModel model) {
        return this.updateById(convertService.convertTo(model));
    }

    @Override
    public Boolean deleteByIds(List<Serializable> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public InsDataExpectDescModel queryById(Serializable id) {
        InsDataExpectDescEntity entity = this.getById(id);
        return convertService.convertTo(entity);
    }

    @Override
    public List<InsDataExpectDescModel> queryByParam(InsDataExpectDescModel model) {
        List<InsDataExpectDescEntity> list = this.list(this.createQueryWrapper(model));
        return list.stream().map(e -> convertService.convertTo(e)).collect(Collectors.toList());
    }
}

