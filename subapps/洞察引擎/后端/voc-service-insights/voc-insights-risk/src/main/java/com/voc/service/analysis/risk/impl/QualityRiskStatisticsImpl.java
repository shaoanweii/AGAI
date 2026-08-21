package com.voc.service.analysis.risk.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.*;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IQualityRiskDataService;
import com.voc.service.analysis.api.IQualityRiskStatisticsService;
import com.voc.service.analysis.enums.StartorParamEnum;
import com.voc.service.analysis.model.AnalysisQualityRiskModel;
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
public class QualityRiskStatisticsImpl extends ServiceImpl<AysPostProcessDataMapper, AysPostprocessDataEntity> implements IQualityRiskStatisticsService {

    private static final Logger log = LoggerFactory.getLogger(QualityRiskStatisticsImpl.class);
    @Resource
    IQualityRiskDataService iQualityRiskDataService;

    @Resource
    InsRiskServiceClient insRiskServiceClient;

    @Resource
    ExtractTag extractTag;

    @Resource
    InsTagLibServiceClient insTagLibServiceClient;

    @Override
    public Boolean qualityRiskStatistics(String clientId, RiskStatisticModel paramModel, String tagType) {

        log.info("质量风险统计数据入参：{},{}", clientId, JSON.toJSONString(paramModel));
        Map<String, String> riskKeyword = this.getRiskKeyword(clientId);
        if (CollUtil.isEmpty(riskKeyword)) {
            return Boolean.FALSE;
        }
        paramModel.setTagType(tagType);
        Set<String> disableTagLib = getDisableTagLib(clientId);
        log.info("质量风险不统计禁用标签集合:{}", disableTagLib);
        paramModel.setLabelTypeLevelFourDisableList(disableTagLib);
        List<AnalysisQualityRiskModel> originalList = this.baseMapper.qualityRisk(paramModel);
        preRule(originalList);
        log.info("获取质量风险数据:{}", originalList.size());
        Map<String, List<AnalysisQualityRiskModel>> topicRiskMap = originalList.stream().collect(Collectors.
                groupingBy(e -> e.getLabelTypeLevelFour() + e.getBrandName() + e.getPublishDate()));
        List<AnalysisQualityRiskModel> risks = new ArrayList<>();
        for (Map.Entry<String, List<AnalysisQualityRiskModel>> entry : topicRiskMap.entrySet()) {
            AnalysisQualityRiskModel risk = ObjectUtil.cloneByStream(entry.getValue().get(0));
            if (NumberUtil.compare(risk.getTotalNum(), 0) > 0) {
                risk.setStartTime(paramModel.getBeginTime());
                risk.setEndTime(paramModel.getEndTime());
                getKeywordIndex(entry.getValue(), risk, riskKeyword, disableTagLib);
            }
            risks.add(risk);
        }
        List<AnalysisQualityRiskModel> result = new ArrayList<>();
        Map<String, List<AnalysisQualityRiskModel>> riskMap = new HashMap<>();
        for (AnalysisQualityRiskModel risk : risks) {
            if (!riskMap.containsKey(risk.getLabelTypeLevelFour())) {
                riskMap.put(risk.getLabelTypeLevelFour(), new ArrayList<>());
            }
            riskMap.get(risk.getLabelTypeLevelFour()).add(risk);
        }
        log.info("处理条数:{}", riskMap.size());
        for (Map.Entry<String, List<AnalysisQualityRiskModel>> entry : riskMap.entrySet()) {
            List<AnalysisQualityRiskModel> topicRisks = entry.getValue();
            topicRisks.sort(Comparator.comparing(AnalysisQualityRiskModel::getPublishDate, Comparator.nullsLast(Comparator.naturalOrder())));
            riskFilter(result, topicRisks, paramModel);
        }
        log.info("qualityRiskStatistics处理结果:{}", result.size());
        List<List<AnalysisQualityRiskModel>> resultBatch = ListUtil.split(result, 1000);
        for (List<AnalysisQualityRiskModel> r : resultBatch) {
            iQualityRiskDataService.saveBatch(clientId, r);
        }
        return Boolean.TRUE;
    }


    private void preRule(List<AnalysisQualityRiskModel> originalList) {
        originalList.forEach(record -> {
            record.setId(SecureUtil.md5(record.getChannelId() + record.getPublishDate() + record.getLabelTypeLevelFour() + record.getStatisticType()
                    + record.getRiskType() + record.getBrandName()));

            if (StrUtil.isNotEmpty(record.getLabelTypeLevelFour())) {
                log.info("topic code is {}", record.getLabelTypeLevelFour());
                record.setSNum(this.loadCodeSerious(record.getFaultLevel()).doubleValue());
            } else {
                record.setTotalNum(0L);
            }
            if (record.getTotalNum() == null) {
                record.setTotalNum(0L);
            }
            record.setRiskIndex(0d);
        });
    }


    /**
     * 获取风险等级对应数值
     */
    private Integer loadCodeSerious(String faultLevel) {
        int serious = 1;
        if (StrUtil.isBlank(faultLevel)) {
            return serious;
        }
        switch (faultLevel) {
            case "高":
                serious = 10;
                break;
            case "较高":
                serious = 8;
                break;
            case "中":
                serious = 6;
                break;
            case "较低":
                serious = 4;
                break;
            case "低":
                serious = 2;
                break;
            default:
                break;
        }
        return serious;
    }


    private void getKeywordIndex(List<AnalysisQualityRiskModel> risks, AnalysisQualityRiskModel originRisk, Map<String, String> riskKeyword, Set<String> disableTagLib) {


        double pNum = 0L;
        Map<String, Long> keywordMap = new HashMap<>();
        Long total = 0L;
        for (AnalysisQualityRiskModel risk : risks) {
            total = risk.getTotalNum() + total;
            float channelIndex = getChannelIndex(risk.getChannelId());
            List<String> sentenceList = this.baseMapper.getSentenceList(originRisk.getStartTime(),
                    originRisk.getEndTime(), originRisk.getLabelTypeLevelFour(), originRisk.getBrandName(), risk.getLabelType(), disableTagLib);
            Map<String, Long> riskMap = new HashMap<>();
            for (String sentence : sentenceList) {
                List<String> keywords = ReUtil.findAll(String.join("|", riskKeyword.keySet()), sentence, 0);
                for (String keyword : keywords) {
                    if (riskMap.containsKey(keyword)) {
                        riskMap.put(keyword, riskMap.get(keyword) + 1);
                    } else {
                        riskMap.put(keyword, 1L);
                    }

                    if (keywordMap.containsKey(keyword)) {
                        keywordMap.put(keyword, riskMap.get(keyword) + 1);
                    } else {
                        keywordMap.put(keyword, 1L);
                    }
                }

            }
            BigDecimal keywordIndex = new BigDecimal(0);

            for (Map.Entry<String, Long> entity : riskMap.entrySet()) {
                keywordIndex = NumberUtil.add(keywordIndex, NumberUtil.mul(entity.getValue().toString(), riskKeyword.get(entity.getKey())));
            }
            pNum = NumberUtil.add(NumberUtil.mul(channelIndex, Math.log(NumberUtil.add(keywordIndex, risk.getTotalNum(), 1).doubleValue())), pNum);
        }
        originRisk.setTotalNum(total);
        originRisk.setPNum(pNum);
        originRisk.setRiskIndex(NumberUtil.mul(originRisk.getPNum(), originRisk.getSNum()));
        BigDecimal KeywordNum = new BigDecimal(0);
        for (Map.Entry<String, Long> entity : keywordMap.entrySet()) {
            KeywordNum = NumberUtil.add(KeywordNum, entity.getValue());
        }
        originRisk.setRiskKeywordsNum(KeywordNum.longValue());
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
            riskKeyword.put(keywordsModel.getRiskKeywords(), getCodeSerious(keywordsModel.getSeriousLevel()));
        }
        return riskKeyword;
    }

    /**
     * 获取风险关键词等级对应数值
     */
    private String getCodeSerious(String faultLevel) {
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


    /**
     * 根据正太分布计算异常值
     *
     * @param result
     * @param topicRisks
     */
    private void riskFilter(List<AnalysisQualityRiskModel> result, List<AnalysisQualityRiskModel> topicRisks, RiskStatisticModel statisticDto) {
        StandardDeviation standardDeviation = new StandardDeviation();
        List<AnalysisQualityRiskModel> cycleRisk;
        int round = 10;
        if (statisticDto.getStatisticType().equals(StartorParamEnum.STATISTIC_DATE_Q.getName()) || statisticDto.getStatisticType().equals(StartorParamEnum.STATISTIC_DATE_Y.getName())) {
            round = 7;
        }
        for (int i = topicRisks.size() - 1; i > round - 2; i--) {
            if (topicRisks.get(i).getTotalNum() == null || topicRisks.get(i).getTotalNum() < 1) {
                continue;
            }
            cycleRisk = ListUtil.sub(topicRisks, i - (round - 1), i + 1);

            List<Double> riskList = new ArrayList<>();
            for (int cycleIndex = 0; cycleIndex < cycleRisk.size(); cycleIndex++) {
                if (cycleRisk.get(cycleIndex).getRiskIndex().doubleValue() > 0) {
                    riskList.add(cycleRisk.get(cycleIndex).getRiskIndex().doubleValue());

                    if (topicRisks.get(i).getMaxTotalNum() == null || NumberUtil.compare(topicRisks.get(i).getMaxTotalNum(),
                            cycleRisk.get(cycleIndex).getTotalNum()) < 0) {
                        topicRisks.get(i).setMaxTotalNum(cycleRisk.get(cycleIndex).getTotalNum());
                    }
                    if (topicRisks.get(i).getMaxChannelNum() == null || NumberUtil.compare(topicRisks.get(i).getMaxChannelNum(),
                            cycleRisk.get(cycleIndex).getChannelNum()) < 0) {
                        topicRisks.get(i).setMaxChannelNum(cycleRisk.get(cycleIndex).getChannelNum());
                    }
                }
            }
            double[] riskArray = riskList.stream().mapToDouble(Double::doubleValue).toArray();
            double meanVal = StatUtils.mean(riskArray);
            double standardDeviationVal = standardDeviation.evaluate(riskArray);
            log.info("mean is {},standardDeviation is {},emotionIndex is{}", meanVal, standardDeviationVal, topicRisks.get(i).getRiskIndex());
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

    private void getFinalRiskIndex(AnalysisQualityRiskModel risk, double meanVal) {
        risk.setRiskIndex(BigDecimal.valueOf(NumberUtil.sub(1, NumberUtil.div(meanVal, risk.getRiskIndex().floatValue())) * 100).doubleValue());
    }

    private float getChannelIndex(String channelId) {
        float channelIndex = 0.2f;
//        if (StrUtil.isEmpty(channelId)) {
//            return channelIndex;
//        }
//        switch (channelId) {
//            case "1356178730703224834":
//            case "1356178730703224001":
//            case "1356178730703224002":
//            case "1356178730703224003":
//            case "1356178730703224004":
//                channelIndex = 0.3f;
//                break;
//            case "1356178730703224801":
//                channelIndex = 0.2f;
//                break;
//            case "1356178730703224804":
//            case "1356178730703224006":
//            case "1356178730703224007":
//                channelIndex = 0.1f;
//                break;
//            default:
//                break;
//        }
        return channelIndex;
    }

}
