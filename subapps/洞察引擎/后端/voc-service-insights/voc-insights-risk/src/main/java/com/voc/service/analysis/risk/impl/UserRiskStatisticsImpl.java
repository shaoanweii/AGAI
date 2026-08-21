package com.voc.service.analysis.risk.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IUserRiskDataService;
import com.voc.service.analysis.api.IUserRiskStatisticsService;
import com.voc.service.analysis.model.AnalysisUserRiskModel;
import com.voc.service.analysis.model.RiskStatisticModel;
import com.voc.service.analysis.risk.component.ExtractTag;
import com.voc.service.analysis.risk.entity.AysPostprocessDataEntity;
import com.voc.service.analysis.risk.mapper.AysPostProcessDataMapper;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.clients.InsTagLibServiceClient;
import com.voc.service.insights.engine.model.InsTagLibClientModel;
import com.voc.service.insights.engine.vo.InsTagLibVo;
import com.voc.service.insights.engine.vo.TagLibClientTreeVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@DS("starrock_dndc")
public class UserRiskStatisticsImpl extends ServiceImpl<AysPostProcessDataMapper, AysPostprocessDataEntity> implements IUserRiskStatisticsService {

    private static final Logger log = LoggerFactory.getLogger(UserRiskStatisticsImpl.class);
    @Resource
    IUserRiskDataService iUserRiskDataService;

    @Resource
    InsTagLibServiceClient insTagLibServiceClient;

    @Resource
    ExtractTag extractTag;

    @Override
    public Boolean userRiskStatistics(String clientId, RiskStatisticModel statisticDto) {

        log.info("用户风险统计数据入参：{},{}", clientId, JSON.toJSONString(statisticDto));
        if (StrUtil.isEmpty(statisticDto.getBeginTime())) {
            statisticDto.setBeginTime(DateUtil.now().substring(0, 10));
        }
        ServiceContextHolder.setToken(extractTag.defaultToken);
        Set<String> disableTagLib = getDisableTagLib(clientId);
        log.info("用户风险不统计禁用标签集合:{}", disableTagLib);
        statisticDto.setLabelTypeLevelFourDisableList(disableTagLib);
        List<AnalysisUserRiskModel> result = baseMapper.userRisk(statisticDto);
        if (CollUtil.isEmpty(result)) {
            return Boolean.TRUE;
        }
        result = result.stream().filter(r -> r.getNegativeNum() >= 5 && r.getComplainNum() >= 1).collect(Collectors.toList());
        log.info("用户风险数据:{}", result.size());
        List<List<AnalysisUserRiskModel>> resultBatch = ListUtil.split(result, 500);
        for (List<AnalysisUserRiskModel> r : resultBatch) {
            iUserRiskDataService.saveBatch(clientId, r);
        }
        return Boolean.TRUE;
    }

    //获取全部禁用的标签
    private Set<String> getDisableTagLib(String clientId) {
        final Result<InsTagLibVo> allDisableTagLibRS = insTagLibServiceClient.findAllDisableTagLibClient(
                InsTagLibClientModel.builder().appClient(clientId).build());
        if ("200".equals(allDisableTagLibRS.getCode())) {
            if (ObjUtil.isNotNull(allDisableTagLibRS.getResult()) && CollUtil.isNotEmpty(allDisableTagLibRS.getResult().getFinalTagLib())) {
                InsTagLibVo list = allDisableTagLibRS.getResult();
                if (CollUtil.isNotEmpty(list.getFinalTagLib())) {
                    Set<String> collect = list.getFinalTagLib().stream().map(TagLibClientTreeVo::getTagCode).collect(Collectors.toSet());
                    log.info("获取全部禁用标签:{}", collect);
                    return collect;
                } else {
                    log.info("获取全部禁用标签为空");
                }
            }
        }
        return new HashSet<>();
    }
}
