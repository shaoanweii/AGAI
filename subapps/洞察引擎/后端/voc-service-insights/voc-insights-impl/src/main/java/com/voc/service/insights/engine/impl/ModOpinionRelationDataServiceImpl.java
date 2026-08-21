package com.voc.service.insights.engine.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.insights.engine.api.IInsHighFrequencyOpinionsService;
import com.voc.service.insights.engine.entity.ModOpinionRelationDataEntity;
import com.voc.service.insights.engine.mapper.ModOpinionRelationDataMapper;
import com.voc.service.insights.engine.model.AddHighFrequencyOpinionModel;
import com.voc.service.insights.engine.model.OpinionInfoModel;
import com.voc.service.insights.engine.vo.ModOpinionRelationDataVo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@DS("starrocks1")
public class ModOpinionRelationDataServiceImpl extends ServiceImpl<ModOpinionRelationDataMapper, ModOpinionRelationDataEntity> {
    private static final Logger log = LoggerFactory.getLogger(ModOpinionRelationDataServiceImpl.class);

    @Resource
    private IInsHighFrequencyOpinionsService iInsHighFrequencyOpinionsService;

    @Resource
    private ModOpinionRelationDataMapper modOpinionRelationDataMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 0 0 * * ?") // 每天的凌晨零点
    public Boolean statisticsOpinion() {

        log.info("定时任务开始处理观点数据");
        String startTimeCache = stringRedisTemplate.opsForValue().get("ins_startTime_cache");
        log.info("观点数据拉取开始时间:{}", startTimeCache);
        if (StringUtils.isEmpty(startTimeCache)) {
            startTimeCache = LocalDateTime.now().plusDays(-7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        List<ModOpinionRelationDataVo> modOpinionRelationDataVos = modOpinionRelationDataMapper.queryModOpinionRelationList(startTimeCache);
        if (CollectionUtil.isEmpty(modOpinionRelationDataVos)) {
            log.info("观点数据模型聚合表为空");
            return Boolean.FALSE;
        }
        modOpinionRelationDataVos = modOpinionRelationDataVos.stream().filter(m -> StringUtils.isNotBlank(m.getClientId())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(modOpinionRelationDataVos)) {
            log.info("观点数据客户ID为空");
            return Boolean.FALSE;
        }
        Map<String, List<ModOpinionRelationDataVo>> modOpinionRelationMap = modOpinionRelationDataVos.stream().collect(Collectors.groupingBy(ModOpinionRelationDataVo::getClientId));
        AddHighFrequencyOpinionModel opinionModel = new AddHighFrequencyOpinionModel();
        for (Map.Entry<String, List<ModOpinionRelationDataVo>> entry : modOpinionRelationMap.entrySet()) {
            List<ModOpinionRelationDataVo> opinionRelationDataVoList = entry.getValue();
            Map<String, OpinionInfoModel> opinionInfoModelMap = new HashMap<>();
            for (ModOpinionRelationDataVo dataEntity : opinionRelationDataVoList) {
                if (MapUtil.isNotEmpty(opinionInfoModelMap) && opinionInfoModelMap.containsKey(dataEntity.getTopic())) {
                    log.info("归一观点相同进行聚合");
                    OpinionInfoModel opinionInfoModel = opinionInfoModelMap.get(dataEntity.getTopic());
                    Set<String> correspondingOpinions = opinionInfoModel.getCorrespondingOpinions();
                    Set<String> systemSuggestedQuality = opinionInfoModel.getSystemSuggestedQuality();
                    Set<String> systemSuggestedBusiness = opinionInfoModel.getSystemSuggestedBusiness();
                    Set<String> channelSource = opinionInfoModel.getChannelSource();
                    correspondingOpinions.add(dataEntity.getOpinion());
                    opinionInfoModel.setCorrespondingOpinions(correspondingOpinions);
                    if (dataEntity.getLabelType() == 1) {
                        if (CollectionUtil.isEmpty(systemSuggestedBusiness)) {
                            systemSuggestedBusiness = new HashSet<>();
                        }
                        systemSuggestedBusiness.add(dataEntity.getRelationLabel());
                        opinionInfoModel.setSystemSuggestedBusiness(systemSuggestedBusiness);
                    } else if (dataEntity.getLabelType() == 2) {
                        if (CollectionUtil.isEmpty(systemSuggestedQuality)) {
                            systemSuggestedQuality = new HashSet<>();
                        }
                        systemSuggestedQuality.add(dataEntity.getRelationLabel());
                        opinionInfoModel.setSystemSuggestedQuality(systemSuggestedQuality);
                    }
                    channelSource.add(dataEntity.getChannelId());
                    opinionInfoModel.setChannelSource(channelSource);
                    opinionInfoModel.setCurrentFrequency(opinionInfoModel.getCurrentFrequency() + 1);
                    continue;
                }
                OpinionInfoModel opinionInfoModel = new OpinionInfoModel();
                opinionInfoModel.setNormalizedOpinions(dataEntity.getTopic());
                Set<String> correspondingOpinions = new HashSet<>();
                correspondingOpinions.add(dataEntity.getOpinion());
                opinionInfoModel.setCorrespondingOpinions(correspondingOpinions);
                if (dataEntity.getLabelType() == 1) {
                    Set<String> systemSuggestedBusiness = new HashSet<>();
                    systemSuggestedBusiness.add(dataEntity.getRelationLabel());
                    opinionInfoModel.setSystemSuggestedBusiness(systemSuggestedBusiness);
                } else if (dataEntity.getLabelType() == 2) {
                    Set<String> systemSuggestedQuality = new HashSet<>();
                    systemSuggestedQuality.add(dataEntity.getRelationLabel());
                    opinionInfoModel.setSystemSuggestedQuality(systemSuggestedQuality);
                }
                opinionInfoModel.setCurrentFrequency(1L);
                Set<String> channelSource = new HashSet<>();
                channelSource.add(dataEntity.getChannelId());
                opinionInfoModel.setChannelSource(channelSource);
                opinionInfoModelMap.put(dataEntity.getTopic(), opinionInfoModel);
            }
            opinionModel.setClientId(entry.getKey());
            opinionModel.setAddOpinionInfoModelList(new ArrayList<>(opinionInfoModelMap.values()));
            try {
                iInsHighFrequencyOpinionsService.addHighFrequencyOpinion(opinionModel);
            } catch (Exception e) {
                log.info("观点数据新增异常:", e);
            }
        }
        Set<LocalDateTime> dateTimes = modOpinionRelationDataVos.stream().map(ModOpinionRelationDataVo::getCreateTime).collect(Collectors.toSet());
        // 获取最大日期
        String maxDate = Collections.max(dateTimes).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("观点数据记录本次处理最大时间:{}", maxDate);
        stringRedisTemplate.opsForValue().set("ins_startTime_cache", maxDate);
        log.info("定时任务结束处理观点数据");
        return Boolean.TRUE;
    }

}
