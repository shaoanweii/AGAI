package com.voc.service.insights.engine.alert.impl.factory;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.alert.entity.AltMonitoringDataEntity;
import com.voc.service.insights.engine.alert.impl.converts.AltMonitoringDataConvertService;
import com.voc.service.insights.engine.alert.listeners.AltNotificationListener;
import com.voc.service.insights.engine.alert.mapper.AltMeteDataMapper;
import com.voc.service.insights.engine.api.alert.IInsAlertMetaDataService;
import com.voc.service.insights.engine.api.alert.IInsAltMonitoringDataService;
import com.voc.service.insights.engine.api.constants.AlertTaskEnum;
import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import com.voc.service.insights.engine.model.alert.AltAlarmDataModel;
import com.voc.service.insights.engine.model.alert.AltMonitoringDataModel;
import com.voc.service.insights.engine.model.alert.InsAltDataModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Title: InsAlertMetaDataServiceImpl
 * @Package: com.voc.service.insights.engine.alert.impl
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/25 11:07
 * @Version:1.0
 */
@Service("metaData")
@DS("starrocks1")
@AllArgsConstructor
public class AlertMetaDataServiceImpl extends ServiceImpl<AltMeteDataMapper, AltMonitoringDataEntity>
        implements IInsAlertMetaDataService {
    private static final Logger log = LoggerFactory.getLogger(AlertMetaDataServiceImpl.class);
    @Getter
    final String dataType = AlertTaskEnum.META_DATA.getCode();

    private final AltMonitoringDataConvertService convertService;

    private final IInsAltMonitoringDataService monitoringDataService;

    private final RedisTemplate redisTemplate;

    /**
     * 获取NLP获取统计数据
     */
    @Override
    public boolean execute(AlertTaskModel task) {
        log.info("任务[{}]执行开始", dataType);
        Assert.isTrue(StrUtil.isNotBlank(task.getChannelId()), "getTaskChannelId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(task.getClientId()), "getTaskClientId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(task.getDataType()), "getTaskDataType cannot be empty");

        try {
            //获取metaData数据获取统计数据
            final String todayTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd");
            final String workId = IdWorker.getId();
            final AltMonitoringDataEntity pullData = this.baseMapper.pullData(
                    AltMonitoringDataEntity.builder()
                            .channelId(task.getChannelId())
                            .clientId(task.getClientId())
                            .dataType(task.getDataType())
                            .workId(workId)
                            .tid(ServiceContextHolder.traceId())
                            .build()
                    , todayTime);
            if (ObjectUtil.isEmpty(pullData)) {
                log.info("本次未读取数据 {}", dataType);
                return false;
            }

            final AltMonitoringDataModel model = convertService.convertTo(pullData);

//            final List<AltMonitoringDataModel> list = pullData.stream().map(convertService::toModel).collect(Collectors.toList());
            //T保存至数据监控数据库
            monitoringDataService.save(model, task, workId);
            task.setDataSize(pullData.getDataSize());

            //生成报告
            final String str = Base64.encode(JSONUtil.toJsonStr(task), CharsetUtil.CHARSET_UTF_8);
            log.debug("validTaskAdapter.param：{}", str);
            redisTemplate.convertAndSend(AltNotificationListener.TOPIC, str);

            log.info("任务[{}]执行完毕", dataType);
        } catch (Exception e) {
            log.error("任务[{}]执行异常", dataType, e);
            throw e;
        }
        return true;
    }


    // 原始数据数量 =（今日推送量-历史均值）/历史均值*100%）
    @Override
    public List<InsAltDataModel> alertBarChart(AltAlarmDataModel altAlarmDataModel) {
        return monitoringDataService.metaDataAlertBarChart(altAlarmDataModel);
    }


}
