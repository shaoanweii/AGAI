package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IAysErrorPushService;
import com.voc.service.analysis.api.IAysModelResltAnalysisMissService;
import com.voc.service.analysis.core.v2.entity.AysModelResultDataAnalysisMissEntity;
import com.voc.service.analysis.core.v2.mapper.AysModelResltAnalysisMissMapper;
import com.voc.service.analysis.core.v2.producers.kafka.CaCaModelResultAnalysisMissProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.model.AysModelResltDataAnalysisMissModel;
import com.voc.service.analysis.model.ErrorPushModel;
import com.voc.service.analysis.model.MissDataParamModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
public class AysModelResltAnalysisMissServiceImpl extends ServiceImpl<AysModelResltAnalysisMissMapper, AysModelResultDataAnalysisMissEntity>
        implements IAysModelResltAnalysisMissService {
    private static final Logger logger = LoggerFactory.getLogger(AysModelResltAnalysisMissServiceImpl.class);
    @Autowired
    CaCaModelResultAnalysisMissProducer modelResultAnalysisMissProducer;
    @Autowired
    IAysErrorPushService errorPushService;

    @SwitchClientDS
    @Override
    public void saveBatch(String clientId, List<AysModelResltDataAnalysisMissModel> modelNotLabelDataList) throws Exception {
        List<AysModelResultDataAnalysisMissEntity> aysModelResltDataAnalysisMissEntities = new ArrayList<>();
        for (AysModelResltDataAnalysisMissModel aysModelResltDataAnalysisMissModel : modelNotLabelDataList) {
            AysModelResultDataAnalysisMissEntity entity = new AysModelResultDataAnalysisMissEntity();
            BeanUtil.copyProperties(aysModelResltDataAnalysisMissModel, entity);
            if (ObjectUtil.isNotNull(aysModelResltDataAnalysisMissModel.getExtFields())) {
                entity.setExtFields(JSONUtil.parseObj(aysModelResltDataAnalysisMissModel.getExtFields()));
            }

            try {
                Assert.isTrue(StrUtil.isNotBlank(entity.getDataId()), "getDataId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(entity.getId()), "getId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(entity.getWorkId()), "getWorkId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(entity.getClientId()), "getClientId cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(entity.getChannelId()), "getChannelId cannot be empty");


            } catch (Exception e) {
                //异常入库数据纪录
                errorPushService.push(ErrorPushModel
                        .builder()
                        .table("voc_anal_flow_model_tags_unlabeled_data_full")
                        .clientId(clientId)
                        .action(IAysErrorPushService.ACTION_ADD)
                        .data(entity)
                        .workId(entity.getWorkId())
                        .tid(ServiceContextHolder.traceId())
                        .build());
                logger.error(e.getMessage(), e);
                continue;
            }
            aysModelResltDataAnalysisMissEntities.add(entity);
        }
        modelResultAnalysisMissProducer.pushData(MessageDTO.builder().data(aysModelResltDataAnalysisMissEntities).build());
        logger.info("未打标签数据入库:{}", aysModelResltDataAnalysisMissEntities.size());
    }

    @SwitchClientDS
    @Override
    public List<Object> getMissDataList(String clientId, MissDataParamModel model) {

        QueryWrapper<AysModelResultDataAnalysisMissEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("client_id", clientId);
        if (StringUtils.isNotBlank(model.getStartTime())) {
            wrapper.between("create_time", model.getStartTime(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        List<AysModelResultDataAnalysisMissEntity> entityList = this.list(wrapper);
        return Collections.singletonList(entityList);
    }
}
