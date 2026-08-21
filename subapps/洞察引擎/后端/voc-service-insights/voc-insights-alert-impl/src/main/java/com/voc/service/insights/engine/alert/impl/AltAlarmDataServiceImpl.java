package com.voc.service.insights.engine.alert.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.alert.entity.AltAlarmDataEntity;
import com.voc.service.insights.engine.alert.impl.converts.AltAlarmDataConvertService;
import com.voc.service.insights.engine.alert.mapper.AltAlarmDataMapper;
import com.voc.service.insights.engine.api.alert.AltAlarmDataService;
import com.voc.service.insights.engine.api.alert.AltTaskConfigDataService;
import com.voc.service.insights.engine.api.constants.DateEnum;
import com.voc.service.insights.engine.model.alert.AltAlarmDataDto;
import com.voc.service.insights.engine.model.alert.AltAlarmDataModel;
import com.voc.service.insights.engine.model.alert.AltTaskConfigDataModel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据监控-告警数据表(AltCoreData)表业务层
 *
 * @author leiww
 * @since 2024-04-26 10:42:23
 */
@Service
@DS("starrock_voc")
@AllArgsConstructor
public class AltAlarmDataServiceImpl extends ServiceImpl<AltAlarmDataMapper, AltAlarmDataEntity> implements AltAlarmDataService {

    private final AltAlarmDataConvertService convertService;

    private final AltTaskConfigDataService taskConfigDataService;

    private QueryWrapper<AltAlarmDataEntity> createQueryWrapper(AltAlarmDataModel model) {
        AltAlarmDataEntity entity = convertService.convertTo(model);
        QueryWrapper<AltAlarmDataEntity> queryWrapper = new QueryWrapper<>(entity);
        LambdaQueryWrapper<AltAlarmDataEntity> lambdaQueryWrapper = queryWrapper.lambda();
        if (CollUtil.isNotEmpty(model.getClientFilters())) {
            lambdaQueryWrapper.in(AltAlarmDataEntity::getClientId, model.getClientFilters());
        }
        if (CollUtil.isNotEmpty(model.getDataTypeFilters())) {
            lambdaQueryWrapper.in(AltAlarmDataEntity::getDataType, model.getDataTypeFilters());
        }
        if (CollUtil.isNotEmpty(model.getChannelFilters())) {
            lambdaQueryWrapper.in(AltAlarmDataEntity::getChannelId, model.getChannelFilters());
        }
        if (CollUtil.isNotEmpty(model.getLevelFilters())) {
            lambdaQueryWrapper.in(AltAlarmDataEntity::getLevel, model.getLevelFilters());
        }
        if (ObjectUtil.isNotEmpty(model.getPushStatus())) {
            lambdaQueryWrapper.eq(AltAlarmDataEntity::getPushStatus, model.getPushStatus());
        }
        model.orderBy(queryWrapper);
        return queryWrapper;
    }

    @Override
    public Result<?> queryBySelect(AltAlarmDataModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<AltAlarmDataEntity> entityList = this.list(this.createQueryWrapper(model));
        List<AltAlarmDataModel> list = convertService.convertEntityToList(entityList);
        if (CollUtil.isNotEmpty(list)) {
            Set<String> taskIds = list.stream().map(AltAlarmDataModel::getTaskId).collect(Collectors.toSet());
            Map<String, AltTaskConfigDataModel> taskConfigDataModels = taskConfigDataService.queryIds(taskIds);
            list.forEach(altCoreDataModel -> {
                if (taskConfigDataModels.containsKey(altCoreDataModel.getTaskId())) {
                    AltTaskConfigDataModel altTaskConfigDataModel = taskConfigDataModels.get(altCoreDataModel.getTaskId());
                    altCoreDataModel.setTaskIdTEXT(altTaskConfigDataModel.getName());
                    BigDecimal nowTime = BigDecimal.valueOf(LocalDateTime.now().getHour());
                    BigDecimal createTime = BigDecimal.valueOf(altCoreDataModel.getCreateTime().getHour());
                    BigDecimal timeliness = BigDecimal.valueOf(Long.parseLong(altTaskConfigDataModel.getTimeliness()));
                    BigDecimal subtract1 = nowTime.subtract(createTime);
                    BigDecimal subtract = timeliness.subtract(subtract1.abs());
                    altCoreDataModel.setTimeliness(subtract.toString());
                }
            });
        }
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
    }


    @Override
    public Boolean insert(AltAlarmDataModel model) {
        model.setId(IdWorker.getId());
        model.setCreateTime(LocalDateTime.now());
        model.setUpdateTime(LocalDateTime.now());
        return this.save(convertService.convertTo(model));
    }


    @Override
    public Boolean update(AltAlarmDataModel model) {
        return this.updateById(convertService.convertTo(model));
    }

    @Override
    public Boolean updateBatchById(List<AltAlarmDataModel> model) {
        List<AltAlarmDataEntity> dataEntities = model.stream().map(convertService::convertTo).toList();
        return this.updateBatchById(dataEntities);
    }


    @Override
    public Boolean deleteByIds(List<Serializable> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public AltAlarmDataModel queryById(Serializable id) {
        AltAlarmDataEntity entity = this.getById(id);
        return convertService.convertTo(entity);
    }

    @Override
    public List<AltAlarmDataModel> queryByParam(AltAlarmDataModel model) {
        List<AltAlarmDataEntity> list = this.list(this.createQueryWrapper(model));
        return list.stream().map(convertService::convertTo).collect(Collectors.toList());
    }

    @Override
    public List<AltAlarmDataDto> alertBarChart(String code) {
        return this.baseMapper.alertBarChart(code);
    }

    @Override
    public List<AltAlarmDataModel> queryByParamAndPeriod(AltAlarmDataModel model, String period) {
        QueryWrapper<AltAlarmDataEntity> queryWrapper = this.createQueryWrapper(model);
        DateEnum code = DateEnum.getByCode(period);
        queryWrapper.apply(code.getValue());
        List<AltAlarmDataEntity> list = this.list(queryWrapper);
        return list.stream().map(convertService::convertTo).collect(Collectors.toList());
    }

}
