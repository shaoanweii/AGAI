package com.voc.service.analysis.risk.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IEmotionRiskDataService;
import com.voc.service.analysis.api.IEmotionRiskStatisticsService;
import com.voc.service.analysis.enums.StartorParamEnum;
import com.voc.service.analysis.model.AnalysisEmotionRiskModel;
import com.voc.service.analysis.model.RiskStatisticModel;
import com.voc.service.analysis.risk.component.ExtractTag;
import com.voc.service.analysis.risk.entity.AysPostprocessDataEntity;
import com.voc.service.analysis.risk.mapper.AysPostProcessDataMapper;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.clients.InsRiskServiceClient;
import com.voc.service.insights.engine.api.clients.InsTagLibServiceClient;
import com.voc.service.insights.engine.model.InsRiskKeywordsModel;
import com.voc.service.insights.engine.model.InsRiskKeywordsQueryModel;
import com.voc.service.insights.engine.model.InsTagLibClientModel;
import com.voc.service.insights.engine.vo.InsTagLibVo;
import com.voc.service.insights.engine.vo.TagLibClientTreeVo;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service
@DS("starrock_dndc")
public class EmotionRiskStatisticsImpl extends ServiceImpl<AysPostProcessDataMapper, AysPostprocessDataEntity> implements IEmotionRiskStatisticsService {
    private static final Logger log = LoggerFactory.getLogger(EmotionRiskStatisticsImpl.class);
    @Resource
    IEmotionRiskDataService iEmotionRiskDataService;

    @Resource
    InsRiskServiceClient insRiskServiceClient;

    @Resource
    ExtractTag extractTag;

    @Resource
    InsTagLibServiceClient insTagLibServiceClient;

    /**
     * flowchart TD
     * A[获取风险关键词] --> B{关键词是否为空}
     * B -->|是| C[返回失败]
     * B -->|否| D[获取业务风险数据]
     * D --> E[数据预处理]
     * E --> F[风险关键词分析]
     * F --> G[风险过滤]
     * G --> H{统计类型是否为季度或年度}
     * H -->|是| I[设置时间窗口为7]
     * H -->|否| J[设置时间窗口为10]
     * I --> K[遍历风险数据]
     * J --> K
     * K --> L{风险数据是否有效}
     * L -->|是| M[计算风险指数均值和标准差]
     * L -->|否| K
     * M --> N{风险指数是否超过均值加标准差}
     * N -->|是| O[调整风险指数并加入结果列表]
     * N -->|否| K
     * O --> P[保存结果]
     * P --> Q[返回成功]
     *
     * @param clientId
     * @param paramModel
     * @param tagType
     * @return
     */
    @Override
    public Boolean emotionRiskStatistics(String clientId, RiskStatisticModel paramModel, String tagType) {

        log.info("业务风险统计数据入参：{},{}", clientId, JSON.toJSONString(paramModel));
        Map<String, String> riskKeyword = this.getRiskKeyword(clientId);
        if (CollUtil.isEmpty(riskKeyword)) {
            log.info("获取风险关键词为空");
            return Boolean.FALSE;
        }
        paramModel.setTagType(tagType);
        Set<String> disableTagLib = getDisableTagLib(clientId);
        log.info("业务风险不统计禁用标签集合:{}", disableTagLib);
        paramModel.setLabelTypeLevelFourDisableList(disableTagLib);
        List<AnalysisEmotionRiskModel> risks = this.baseMapper.emotionRisk(paramModel);
        log.info("获取业务风险数据:{}", risks.size());
        preRule(risks);
        List<AnalysisEmotionRiskModel> result = new ArrayList<>();
        Map<String, List<AnalysisEmotionRiskModel>> riskMap = new HashMap<>();
        for (AnalysisEmotionRiskModel risk : risks) {
            if (!riskMap.containsKey(risk.getLabelTypeLevelFour())) {
                riskMap.put(risk.getLabelTypeLevelFour(), new ArrayList<>());
            }
            riskMap.get(risk.getLabelTypeLevelFour()).add(risk);
            if (risk.getNegativeNum() == null || risk.getNegativeNum() < 1) {
                continue;
            }
            risk.setStartTime(paramModel.getBeginTime());
            risk.setEndTime(paramModel.getEndTime());
            this.riskKeyAnalysis(risk, riskKeyword, disableTagLib);
        }
        log.info("处理条数:{}", riskMap.size());
        for (Map.Entry<String, List<AnalysisEmotionRiskModel>> entry : riskMap.entrySet()) {
            List<AnalysisEmotionRiskModel> topicRisks = entry.getValue();
            topicRisks.sort(Comparator.comparing(AnalysisEmotionRiskModel::getPublishDate, Comparator.nullsLast(Comparator.naturalOrder())));
            riskFilter(result, topicRisks, paramModel);
        }
        log.info("emotionRiskStatistics处理结果:{}", result.size());
        if (CollUtil.isNotEmpty(result)) {
            iEmotionRiskDataService.saveBatch(clientId, result);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 风险关键词计算
     *
     * @param risk
     * @return
     */
    private void riskKeyAnalysis(AnalysisEmotionRiskModel risk, Map<String, String> riskKeyword, Set<String> disableTagLib) {

        Map<String, Long> riskMap = this.getKeywordCountsMap(risk, riskKeyword, disableTagLib);
        BigDecimal keywordIndex = new BigDecimal(0);
        BigDecimal KeywordNum = new BigDecimal(0);
        for (Map.Entry<String, Long> entity : riskMap.entrySet()) {
            KeywordNum = NumberUtil.add(KeywordNum, entity.getValue());
            keywordIndex = NumberUtil.add(keywordIndex, NumberUtil.mul(entity.getValue().toString(), riskKeyword.get(entity.getKey())));
        }
        if (NumberUtil.compare(keywordIndex.doubleValue(), 0) > 0) {
            risk.setRNum(Math.log(1 + keywordIndex.doubleValue()));
        } else {
            risk.setRNum(0d);
        }
        risk.setRiskKeywordsNum(KeywordNum.longValue());
        risk.setRiskIndex(BigDecimal.valueOf(NumberUtil.add(NumberUtil.mul(0.7, risk.getSNum().doubleValue()), NumberUtil.mul(0.3, risk.getRNum().doubleValue()))).doubleValue());
    }


    public Map<String, Long> getKeywordCountsMap(AnalysisEmotionRiskModel risk, Map<String, String> riskKeyword, Set<String> disableTagLib) {
        Map<String, Long> riskMap = new HashMap<>();
        List<String> sentenceList = this.baseMapper.getSentenceList(risk.getStartTime(),
                risk.getEndTime(), risk.getLabelTypeLevelFour(), risk.getBrandName(), risk.getLabelType(), disableTagLib);
        for (String sentence : sentenceList) {
            List<String> keywords = ReUtil.findAll(String.join("|", riskKeyword.keySet()), sentence, 0);
            for (String keyword : keywords) {
                if (riskMap.containsKey(keyword)) {
                    riskMap.put(keyword, riskMap.get(keyword) + 1);
                } else {
                    riskMap.put(keyword, 1L);
                }
            }

        }
        return riskMap;
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


    private Map<String, String> getRiskKeyword(String clientId) {
        ServiceContextHolder.setToken(extractTag.defaultToken);
        Map<String, String> riskKeyword = new HashMap<>();
        Result<List<InsRiskKeywordsModel>> listResult = insRiskServiceClient.queryRiskList(InsRiskKeywordsQueryModel.builder().clientId(clientId).build());
        if (!listResult.isSuccess()) {
            return new HashMap<>();
        }
        List<InsRiskKeywordsModel> keywordsModels = listResult.getResult();
        for (InsRiskKeywordsModel keywordsModel : keywordsModels) {
            riskKeyword.put(keywordsModel.getRiskKeywords(), loadCodeSerious(keywordsModel.getSeriousLevel()));
        }
        return riskKeyword;
    }

    /**
     * 获取风险关键词等级对应数值
     */
    private String loadCodeSerious(String faultLevel) {
        String serious = "1";
        switch (faultLevel) {
            case "Higher":
                serious = "0.8";
                break;
            case "Middle":
                serious = "0.6";
                break;
            case "Inferiority":
                serious = "0.4";
                break;
            case "Low":
                serious = "0.2";
                break;
            case "NotEvaluate":
                serious = "0.1";
                break;
            case "High":
                serious = "1";
                break;
            default:
                serious = "1";
                break;
        }
        return serious;
    }


    private void getFinalRiskIndex(AnalysisEmotionRiskModel risk, double meanVal) {
        risk.setRiskIndex(BigDecimal.valueOf(NumberUtil.sub(1, NumberUtil.div(meanVal, risk.getRiskIndex().floatValue())) * 100).doubleValue());
    }

    /**
     * 预置处理
     * 1、默认值赋予
     * 2、码框补全
     *
     * @param risks
     */
    private void preRule(List<AnalysisEmotionRiskModel> risks) {
        risks.forEach(record -> {
            if (record.getSNum() == null) {
                record.setSNum(0d);
            }
            record.setRiskIndex(0d);
            record.setRiskType(1);
            record.setId(SecureUtil.md5(record.getChannelId() + record.getStatisticType() + record.getPublishDate() + record.getLabelTypeLevelFour() + record.getRiskType() + record.getBrandName()));
            if (record.getNegativeNum() == null) {
                record.setNegativeNum(0L);
            }
            if (record.getComplainNum() == null) {
                record.setComplainNum(0L);
            }
        });
    }

    /**
     * A[初始化变量] --> B{统计类型是否为季度或年度}
     * B -->|是| C[设置时间窗口为7]
     * B -->|否| D[设置时间窗口为10]
     * C --> E[遍历风险数据]
     * D --> E
     * E --> F{风险数据是否有效}
     * F -->|是| G[获取当前时间窗口的风险数据]
     * F -->|否| E
     * G --> H[计算风险指数均值和标准差]
     * H --> I{风险指数是否超过均值加标准差}
     * I -->|是| J[调整风险指数并加入结果列表]
     * I -->|否| E
     *
     * @param result
     * @param topicRisks
     * @param statisticDto
     */
    private void riskFilter(List<AnalysisEmotionRiskModel> result, List<AnalysisEmotionRiskModel> topicRisks, RiskStatisticModel statisticDto) {
        List<AnalysisEmotionRiskModel> cycleRisk;
        StandardDeviation standardDeviation = new StandardDeviation();
        int round = 10;
        if (statisticDto.getStatisticType().equals(StartorParamEnum.STATISTIC_DATE_Q.getName()) || statisticDto.getStatisticType().equals(StartorParamEnum.STATISTIC_DATE_Y.getName())) {
            round = 7;
        }
        for (int i = topicRisks.size() - 1; i > round - 2; i--) {
            if (topicRisks.get(i).getNegativeNum() == null || topicRisks.get(i).getNegativeNum() < 1) {
                continue;
            }
            cycleRisk = ListUtil.sub(topicRisks, i - (round - 1), i + 1);

            List<Double> riskList = new ArrayList<>();
            for (int cycleIndex = 0; cycleIndex < cycleRisk.size(); cycleIndex++) {
                if (cycleRisk.get(cycleIndex).getRiskIndex().doubleValue() > 0) {

                    riskList.add(cycleRisk.get(cycleIndex).getRiskIndex().doubleValue());
                    if (topicRisks.get(i).getMaxComplainNum() == null || NumberUtil.compare(topicRisks.get(i).getMaxComplainNum(),
                            cycleRisk.get(cycleIndex).getComplainNum()) < 0) {
                        topicRisks.get(i).setMaxComplainNum(cycleRisk.get(cycleIndex).getComplainNum());
                    }
                    if (topicRisks.get(i).getMaxNegativeNum() == null || NumberUtil.compare(topicRisks.get(i).getMaxNegativeNum(),
                            cycleRisk.get(cycleIndex).getNegativeNum()) < 0) {
                        topicRisks.get(i).setMaxNegativeNum(cycleRisk.get(cycleIndex).getNegativeNum());
                    }
                }
            }
            double[] riskArray = riskList.stream().mapToDouble(Double::doubleValue).toArray();
            double meanVal = StatUtils.mean(riskArray);
            double standardDeviationVal = standardDeviation.evaluate(riskArray);
            log.info("mean is：{},standardDeviation is：{},emotionIndex is：{}", meanVal, standardDeviationVal, topicRisks.get(i).getRiskIndex());
            log.info("均值加标准差：{}", NumberUtil.add(meanVal, NumberUtil.mul(standardDeviationVal, 1)));
            try {
                if (NumberUtil.compare(topicRisks.get(i).getRiskIndex(), NumberUtil.add(meanVal, NumberUtil.mul(standardDeviationVal, 1))) >= 0) {
                    getFinalRiskIndex(topicRisks.get(i), meanVal);
                    result.add(topicRisks.get(i));
                } else {
                    log.info("异常数据:{},id集合：{},发布时间：{}", topicRisks.get(i).getLabelTypeLevelFour(),
                            topicRisks.get(i).getNewIdArray(), topicRisks.get(i).getPublishDate());
                }
            } catch (Exception e) {
                continue;
            }
            log.info("异常值处理完成");
        }
    }
}
