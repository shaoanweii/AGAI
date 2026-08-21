package com.voc.service.insights.engine.alert.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.alert.entity.AltTaskConfigDataEntity;
import com.voc.service.insights.engine.alert.impl.converts.AltTaskConfigDataConvertService;
import com.voc.service.insights.engine.alert.mapper.AltTaskConfigDataMapper;
import com.voc.service.insights.engine.api.alert.AltTaskConfigDataService;
import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import com.voc.service.insights.engine.model.alert.AltTaskConfigDataModel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据监控-任务配置表(AltTaskConfigData)表业务层
 *
 * @author leiww
 * @since 2024-04-30 17:11:56
 */
@Service
@DS("starrock_voc")
@AllArgsConstructor
public class AltTaskConfigDataServiceImpl extends ServiceImpl<AltTaskConfigDataMapper, AltTaskConfigDataEntity> implements AltTaskConfigDataService {

    private final AltTaskConfigDataConvertService convertService;

    private QueryWrapper<AltTaskConfigDataEntity> createQueryWrapper(AltTaskConfigDataModel model) {
        AltTaskConfigDataEntity entity = convertService.convertTo(model);
        QueryWrapper<AltTaskConfigDataEntity> queryWrapper = new QueryWrapper<>(entity);
        LambdaQueryWrapper<AltTaskConfigDataEntity> lambdaQueryWrapper = queryWrapper.lambda();
        model.orderBy(queryWrapper);
        return queryWrapper;
    }

    @Override
    public Result<?> queryBySelect(AltTaskConfigDataModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<AltTaskConfigDataEntity> entityList = this.list(this.createQueryWrapper(model));
        List<AltTaskConfigDataModel> list = convertService.convertEntityToList(entityList);
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
    }


    @Override
    public Boolean insert(AltTaskConfigDataModel model) {
        model.setId(IdWorker.getId());
        return this.save(convertService.convertTo(model));
    }


    @Override
    public Boolean update(AltTaskConfigDataModel model) {
        return this.updateById(convertService.convertTo(model));
    }


    @Override
    public Boolean deleteByIds(List<Serializable> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public AltTaskConfigDataModel queryById(Serializable id) {
        AltTaskConfigDataEntity entity = this.getById(id);
        return convertService.convertTo(entity);
    }

    @Override
    public List<AltTaskConfigDataModel> queryByParam(AltTaskConfigDataModel model) {
        List<AltTaskConfigDataEntity> list = this.list(this.createQueryWrapper(model));
        return list.stream().map(convertService::convertTo).collect(Collectors.toList());
    }

    @Override
    public Map<String, AltTaskConfigDataModel> queryIds(Set<String> ids) {
        List<AltTaskConfigDataEntity> list = this.listByIds(ids);
        return list.stream().map(convertService::convertTo).collect(Collectors.toMap(AltTaskConfigDataModel::getId, value -> value,(v1, v2) -> v1));
    }

    @Override
    public List<AlertTaskModel> findAllEnable() {
        List<AltTaskConfigDataEntity> list = this.list(this.createQueryWrapper(new AltTaskConfigDataModel()));
        return list.stream().map(convertService::convertEntityTask).collect(Collectors.toList());
    }

}
