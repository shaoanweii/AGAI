package com.voc.service.insights.engine.alert.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.alert.entity.AltMonitoringDataEntity;
import com.voc.service.insights.engine.alert.impl.converts.AltMonitoringDataConvertService;
import com.voc.service.insights.engine.alert.mapper.AltMonitoringDataMapper;
import com.voc.service.insights.engine.api.alert.IInsAltMonitoringDataService;
import com.voc.service.insights.engine.api.constants.DateEnum;
import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import com.voc.service.insights.engine.model.alert.AltAlarmDataModel;
import com.voc.service.insights.engine.model.alert.AltMonitoringDataModel;
import com.voc.service.insights.engine.model.alert.InsAltDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据监控-监控数据表(AltMonitoringData)表业务层
 *
 * @author leiww
 * @since 2024-04-26 15:11:35
 */
@Service
@DS("starrock_voc")
public class AltMonitoringDataServiceImpl extends ServiceImpl<AltMonitoringDataMapper, AltMonitoringDataEntity>
        implements IInsAltMonitoringDataService {
    private static final Logger log = LoggerFactory.getLogger(AltMonitoringDataServiceImpl.class);
    @Autowired
    AltMonitoringDataConvertService convertService;

    @Override
    public void save(AltMonitoringDataModel model, AlertTaskModel task, String workId) {

        log.info("task {}", task);
        try {
            //获取当前的type监控数据
            final String todayTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd");
            QueryWrapper<AltMonitoringDataEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(AltMonitoringDataEntity::getDataType, task.getDataType());
            queryWrapper.lambda().eq(AltMonitoringDataEntity::getChannelId, model.getChannelId());
            queryWrapper.lambda().eq(AltMonitoringDataEntity::getClientId, model.getClientId());
            queryWrapper.lambda().eq(AltMonitoringDataEntity::getWorkId, task.getId());
            DateEnum period = DateEnum.getByCode(task.getPeriod());
            queryWrapper.apply(period.getValue());
            queryWrapper.last("limit 1");
            final AltMonitoringDataEntity entitiy = this.baseMapper.selectOne(queryWrapper);
//            为空时默认插入一条默认数据
            if (ObjectUtil.isNull(entitiy)) {
                AltMonitoringDataEntity param = convertService.convertTo(model);
                final String md5 = DigestUtil.md5Hex(StrUtil.format("{}{}{}{}{}"
                        , param.getClientId(), param.getChannelId(), param.getDataType(), param.getDataSize()));
                param.setId(md5);
                param.setTaskId(task.getId());
                param.setCreateTime(LocalDateTime.now());
                param.setTid(ServiceContextHolder.traceId());
                this.baseMapper.insert(param);
                return;
            }

            final String md5 = DigestUtil.md5Hex(StrUtil.format("{}{}{}{}{}"
                    , model.getClientId(), model.getChannelId(), model.getDataType(), model.getDataSize()));
            if (!StrUtil.equalsAnyIgnoreCase(entitiy.getId(), md5)) {
                //先插入一条更新数据
                AltMonitoringDataEntity param = convertService.convertTo(model);
                param.setId(md5);
                param.setTaskId(task.getId());
                param.setCreateTime(LocalDateTime.now());
                param.setTid(ServiceContextHolder.traceId());
                this.baseMapper.insert(param);
                log.info("先插入一条更新数据 {}", param);
                //迁移旧数据到历史表
                this.baseMapper.moveToHistory(CollUtil.newHashSet(entitiy.getId()), workId);
                log.info("迁移旧数据到历史表 {}", CollUtil.newHashSet(entitiy.getId()));
                //删除就数据
                this.baseMapper.deleteById(entitiy.getId());
                log.info("删除就数据 {}", entitiy.getId());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private QueryWrapper<AltMonitoringDataEntity> createQueryWrapper(AltMonitoringDataModel model) {
        AltMonitoringDataEntity entity = convertService.convertTo(model);
        QueryWrapper<AltMonitoringDataEntity> queryWrapper = new QueryWrapper<>(entity);
        LambdaQueryWrapper<AltMonitoringDataEntity> lambdaQueryWrapper = queryWrapper.lambda();
        model.orderBy(queryWrapper);
        return queryWrapper;
    }

    @Override
    public Result<?> queryBySelect(AltMonitoringDataModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<AltMonitoringDataEntity> entityList = this.list(this.createQueryWrapper(model));
        List<AltMonitoringDataModel> list = entityList.stream().map(convertService::toModel).collect(Collectors.toList());
//        List<AltMonitoringDataModel> list = convertService.convertEntityToList(entityList);
        PageInfo page = new PageInfo<>(entityList);
        page.setList(list);
        return Result.OK(page);
    }


    @Override
    public Boolean insert(AltMonitoringDataModel model) {
        model.setId(IdWorker.getId());
        return this.save(convertService.convertTo(model));
    }


    @Override
    public Boolean update(AltMonitoringDataModel model) {
        return this.updateById(convertService.convertTo(model));
    }


    @Override
    public Boolean deleteByIds(List<Serializable> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public AltMonitoringDataModel queryById(Serializable id) {
        AltMonitoringDataEntity entity = this.getById(id);
        return convertService.convertTo(entity);
    }

    @Override
    public List<AltMonitoringDataModel> queryByParam(AltMonitoringDataModel model) {
        List<AltMonitoringDataEntity> list = this.list(this.createQueryWrapper(model));
        return list.stream().map(convertService::convertTo).collect(Collectors.toList());
    }


    @Override
    public List<AltMonitoringDataModel> findAlertBarChart(AltAlarmDataModel model) {
        AltMonitoringDataModel entity = convertService.convertTo(model);
        QueryWrapper<AltMonitoringDataEntity> queryWrapper = this.createQueryWrapper(entity);
        queryWrapper.apply("DATE ( create_time ) = '".concat(LocalDateTime.now() + "'"));
        List<AltMonitoringDataEntity> list = this.list(queryWrapper);
        return list.stream().map(convertService::convertTo).collect(Collectors.toList());
    }

    @Override
    public Map<String, Double> historicalRatioMean(AltAlarmDataModel altAlarmDataModel) {
        List<Map> list = baseMapper.historicalRatioMean(altAlarmDataModel);
        Map res = list.stream().collect(Collectors.toMap(map -> map.get("key"), map -> map.get("value"), (v1, v2) -> v1));
        return res;
    }

    @Override
    public Map<String, Double> findHistoricalAvg(AltAlarmDataModel altAlarmDataModel) {
        List<Map> list = baseMapper.getHistoricalAvg(altAlarmDataModel);
        Map res = list.stream().collect(Collectors.toMap(map -> map.get("key"), map -> map.get("value"), (v1, v2) -> v1));
        return res;
    }

    @Override
    public List<InsAltDataModel> nlpDataAlertBarChart(AltAlarmDataModel model) {
        return baseMapper.nlpDataAlertBarChart(model);
    }

    @Override
    public List<InsAltDataModel> metaDataAlertBarChart(AltAlarmDataModel model) {
        return baseMapper.metaDataAlertBarChart(model);
    }

    @Override
    public List<InsAltDataModel> pushDataAlertBarChart(AltAlarmDataModel model) {
        model.setCreateTime(LocalDateTime.now());
        return baseMapper.pushDataAlertBarChart(model);
    }


}
