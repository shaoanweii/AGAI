package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.data.IInsDataExpectService;
import com.voc.service.insights.engine.data.entity.InsDataExpectEntity;
import com.voc.service.insights.engine.data.impl.converts.InsDataExpectConvertService;
import com.voc.service.insights.engine.data.mapper.InsDataExpectMapper;
import com.voc.service.insights.engine.model.data.InsDataExpectModel;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 语料库数据集(InsDataExpect)表服务实现类
 *
 * @author leiww
 * @since 2024-03-05 14:44:44
 */
@Service
public class InsDataExpectServiceImpl extends ServiceImpl<InsDataExpectMapper, InsDataExpectEntity> implements IInsDataExpectService {

    @Resource
    private InsDataExpectConvertService convertService;

    private QueryWrapper<InsDataExpectEntity> createQueryWrapper(InsDataExpectModel model) {
        InsDataExpectEntity entity = convertService.convertTo(model);
        QueryWrapper<InsDataExpectEntity> qw = new QueryWrapper<>(entity);
        LambdaQueryWrapper<InsDataExpectEntity> queryWrapper = qw.lambda();
        if (StrUtil.isNotBlank(model.getNameFilter())) {
            queryWrapper.like(InsDataExpectEntity::getName, model.getNameFilter());
        }
        if (CollUtil.isNotEmpty(model.getFormatFilters())) {
            queryWrapper.in(InsDataExpectEntity::getFormat, model.getFormatFilters());
        }
        model.orderBy(qw);
        return qw;
    }

    @Override
    public Result<?> queryBySelect(InsDataExpectModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        model.setClientId(null);
        List<InsDataExpectEntity> entityList = this.list(this.createQueryWrapper(model));
        List<InsDataExpectModel> list = convertService.convertToList(entityList);
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
    }

    @Override
    public Integer countBySelect(InsDataExpectModel insDataExpect) {
        insDataExpect.setClientId(null);
        QueryWrapper<InsDataExpectEntity> wrapper = createQueryWrapper(insDataExpect);
        wrapper.select("sum(`count`) AS count");
        // 使用 sum 方法获取字段的总和
        return this.baseMapper.selectOne(wrapper).getCount();
    }

    @Override
    public Boolean insert(InsDataExpectModel model) {
        this.checkParameter(model);
        model.setId(IdWorker.getId());
        return this.save(convertService.convertTo(model));
    }

    @Override
    public Boolean update(InsDataExpectModel model) {
        return this.updateById(convertService.convertTo(model));
    }

    @Override
    public Boolean deleteByIds(List<Serializable> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public InsDataExpectModel queryById(Serializable id) {
        InsDataExpectEntity entity = this.getById(id);
        return convertService.convertTo(entity);
    }

    @Override
    public List<InsDataExpectModel> queryByParam(InsDataExpectModel model) {
        List<InsDataExpectEntity> list = this.list(this.createQueryWrapper(model));
        return list.stream().map(e -> convertService.convertTo(e)).collect(Collectors.toList());
    }

    private void checkParameter(InsDataExpectModel model) {
        Assert.hasLength(model.getName(), "名称不允许为空");
        Assert.isTrue(!model.getName().isEmpty()
                && model.getName().length() <= 50, "名称长度不符,长度不允许超过50个字");
        Assert.hasLength(model.getFormat(), "数据格式不允许为空");
    }
}

