package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IAysErrorPushService;
import com.voc.service.analysis.api.IAysPostprocessValidDataService;
import com.voc.service.analysis.core.v2.entity.AysPostprocessValidDataEntity;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.mapper.AysPostprocessValidDataMapper;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessPostRulesProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.analysis.model.AysProcessValidDataModel;
import com.voc.service.analysis.model.ErrorPushModel;
import com.voc.service.analysis.model.RuleModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.clients.IInsRegulationServiceClient;
import com.voc.service.insights.engine.vo.InsValidateInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AysPostprocessValidDataServiceImpl extends ServiceImpl<AysPostprocessValidDataMapper, AysPostprocessValidDataEntity>
        implements IAysPostprocessValidDataService {

    private static final Logger logger = LoggerFactory.getLogger(AysPostprocessValidDataServiceImpl.class);
    @Autowired
    AysConvertMapperService aysConvertMapperService;
    @Autowired
    IInsRegulationServiceClient regulationServiceClient;
    @Autowired
    IAysErrorPushService errorPushService;
    @Autowired
    ProcessPostRulesProducer processPostRulesProducer;

    @Override
    @SwitchClientDS
    public List<AysProcessDataModel> saveBatch(String clientId, String oldWorkId, List<AysProcessDataModel> data) throws Exception {
        List<AysPostprocessValidDataEntity> saveList = new ArrayList<>();
        try {
            for (AysProcessDataModel model : data) {
                AysPostprocessValidDataEntity entity = JSONUtil.toBean(String.valueOf(model.getData()), AysPostprocessValidDataEntity.class);
                //与模型解析数据ID保持一致
                entity.setNewId(model.getId());
//                String clientId=entity.getClientId();
                /*if (CollUtil.isNotEmpty(model.getHitValidRuleList())) {
                    final String hitRules = JSONUtil.toJsonStr(JSONUtil.createObj().putOpt(
                            "rule_ids", model.getHitValidRuleList().stream().map(RuleModel::getId).collect(Collectors.toSet())));
                    entity.setHitValidRules(hitRules);
                }*/
                if (CollUtil.isNotEmpty(model.getHitRuleList())) {
                    JSONObject entries = JSONUtil.createObj().putOpt(
                            "rule_ids", model.getHitRuleList().stream().map(RuleModel::getId).collect(Collectors.toSet()));
                    entity.setHitRules(entries);
                }
                entity.setId(model.getId());
                entity.setDone("1");
                entity.setPublishTime(model.getPublishTime());
                entity.setUpdateTime(LocalDateTime.now());
                entity.setAbandon(model.getAbandon());
                entity.setOldWorkId(oldWorkId);
                entity.setModelType(model.getModelType());

                try {
                    Assert.isTrue(StrUtil.isNotBlank(entity.getNewId()), "getNewId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getId()), "getId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getWorkId()), "getWorkId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getClientId()), "getClientId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getChannelId()), "getChannelId cannot be empty");

                } catch (IllegalArgumentException e) {
                    //异常入库数据纪录
                    errorPushService.push(ErrorPushModel
                            .builder()
                            .table("ays_post_process_data_valid")
                            .clientId(clientId)
                            .action(IAysErrorPushService.ACTION_ADD)
                            .data(entity)
                            .workId(entity.getWorkId())
                            .tid(ServiceContextHolder.traceId())
                            .build());
                    logger.error(e.getMessage(), e);
                    continue;
                }

                saveList.add(entity);
            }
    //        processPostRulesProducer.pushValidData(MessageDTO.builder().source(clientId).data(saveList).build());
            Thread.sleep(10000);
            logger.info("saveBatch本次解析后需保存条数据:{},{}", saveList.size(), oldWorkId);
        } catch (Exception e) {
            throw e;
        }
        return aysConvertMapperService.converToAysPostprocessValidDataModel(saveList);
    }

    @SwitchClientDS
    @Override
    public int modifyToDone(String clientId, Set<String> ids, String workId) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        UpdateWrapper<AysPostprocessValidDataEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(AysPostprocessValidDataEntity::getWorkId, workId);
        wrapper.in("new_id", ids);
        wrapper.set("done", "1");
        return this.baseMapper.update(null, wrapper);
    }

    /**
     * 获取验证数据
     *
     * @param workId
     * @param clientId
     * @return
     */
    @SwitchClientDS
    @Override
    public List<AysProcessValidDataModel> getProcessValidData(String clientId, String workId, List<String> channelId) {
        return this.baseMapper.queryProcessValidData(workId, clientId, channelId);
    }

    /**
     * 删除历史数据
     * 1、获取规则数据集中，最后一次单条校验+整体测试workid
     * 2、保留上一步骤中的workid集合数据，其他数据进行清楚
     *
     * @param days
     */
    @SwitchClientDS
    @Override
    public long removeHistoryData(String clientId, int days) {
        Result<List<InsValidateInfoVo>> rs = regulationServiceClient.findNewestValidateRuleInfo();
        if ("200".equalsIgnoreCase(rs.getCode())) {
            Set<String> workIds = rs.getResult().stream()
                    .map(InsValidateInfoVo::getWorkId).collect(Collectors.toSet());
            logger.info("获取目前已使用到校验workId集合：{}", workIds);
            if (CollUtil.isNotEmpty(workIds)) {
                return this.baseMapper.removeHistoryData(days, workIds);
            }
        }
        return 0;
    }
}
