package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.voc.service.analysis.api.IAysCacheService;
import com.voc.service.analysis.api.IRuleDataServcie;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.model.rule.ComputLogicModel;
import com.voc.service.analysis.model.rule.ConditionAttrModel;
import com.voc.service.analysis.model.rule.ConditionModel;
import com.voc.service.analysis.model.rule.ResultDataModel;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.clients.IInsRegulationServiceClient;
import com.voc.service.insights.engine.enums.RuleConditionType;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import com.voc.service.insights.engine.model.InsValidateRuleInfoModel;
import com.voc.service.insights.engine.vo.AysRegulationInfoVo;
import com.voc.service.insights.engine.vo.RegulationDetailsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName RuleDataServcieImpl
 * @createTime 2024年03月12日 18:18
 * @Copyright cuick
 */

@Service
public class RuleDataServcieImpl implements IRuleDataServcie, IAysCacheService {

    private static final Logger log = LoggerFactory.getLogger(RuleDataServcieImpl.class);
    @Autowired
    IInsRegulationServiceClient ruleServiceClient;
    @Autowired
    AnalysisConfig config;
    /*@Autowired
    private CacheUtils cacheUtils;*/

    //tableName, rowsObj
   /* static Cache<String, List<RuleModel>> RULE_DATA_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(AnalysisConfig.CACHE_DURATION, AnalysisConfig.CACHE_UNIT)
//            .maximumSize(100)  // 设置最大缓存条目数
            .build();*/

    @CreateCache(area = "VDP", name = ":", expire = 15, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.LOCAL)
    Cache<String, List<ComputLogicModel>> rulesCache;
    @Autowired
    private CacheManager cacheManager;

    private static final String RULES_CACHE_KEY = "{}:rules:{}";

    private String getRulesCacheKey(Object... params) {
        return StrUtil.format(RULES_CACHE_KEY, ServiceContextHolder.getSystemId(), params);
    }

    /**
     * 初始化： 接口-》redis-》本地
     * 场景一： redis有key值-》 使用本地
     * 场景一： redis无key值-》 使用接口-》redis-》本地
     * <p>
     * CachePenetrationProtect 注解:
     * 当缓存访问【未命中】的情况下,对并发进行的加载行为进行保护.
     * 当前版本实现的是单JVM内的保护，即同一个JVM中同一个key只有一个线程去加载，其它线程等待结果
     */
    @Override
//    @Cached(name = "data:rules:", key = "'client_'+#clientId", expire = 1, cacheType = CacheType.BOTH
//             , timeUnit = TimeUnit.MINUTES, cacheNullValue = false)
//    @CachePenetrationProtect
    public List<ComputLogicModel> getRuleData(final String clientId, Set<String> validRuleIds) {
        //设置固定token，此token需添加到auth服务白名单token集合
        ServiceContextHolder.setToken(config.getDefaultToken());

        InsRegulationInfoModel build = InsRegulationInfoModel.builder()
                .clientId(clientId)
                .ruleIds(validRuleIds)
                .build();
        log.info("加载规则数据-开始：hashCode：{}", build, hashCode());
//        final String key = this.getRulesCacheKey(String.valueOf(build.hashCode()));
        final String key = this.getRulesCacheKey("clientId_".concat(clientId.concat("_").concat(String.valueOf(build.hashCode()))));
        final List<ComputLogicModel> rslist = rulesCache.computeIfAbsent(key, k -> {
            log.info("重新加载规则数据-开始");
            final Result<List<AysRegulationInfoVo>> result = ruleServiceClient.findRulesList(build);
            log.info("重新加载规则数据-{}", result.getCode());
            CopyOnWriteArrayList<ComputLogicModel> list = new CopyOnWriteArrayList<>();
            if ("200".equals(result.getCode())) {

                for (AysRegulationInfoVo vo : result.getResult()) {
                    final ComputLogicModel rule = this.getComputLogicModel(vo);
                    list.add(rule);
                }
            }
            if (CollUtil.isEmpty(list)) {
                return null;
            }

            return Collections.unmodifiableList(list);
        });

        //http 调用接口 - 根据clientId获取当前所有【已启用+未启用】的规则集合

        log.info("加载规则数据-完成：{}：hashCode：{}", rslist, build, hashCode());
        return rslist;
    }

   /* public List<ComputLogicModel> test(List<ComputLogicModel> list) {
        String id = "b1dbd704ae6cb0d63752cbfd17e85d25";
        return list.stream().filter(e -> id.equals(e.getRuleId())).collect(Collectors.toList());
    }*/

    /**
     * 数据拼装
     *
     * @return
     */
    private ComputLogicModel getComputLogicModel(AysRegulationInfoVo rule) {
        Assert.isTrue(StrUtil.isNotBlank(rule.getId()), "getId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(rule.getName()), "getRuleName cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(rule.getRegulationType()), "getRegulationType cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(rule.getRegulationWeight()), "getRegulationWeight cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(rule.getContentType()), "getContentType cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(rule.getStatus()), "getStatus cannot be empty");
        Assert.isTrue(CollUtil.isNotEmpty(rule.getChannel()), "getChannel cannot be empty");
//        Assert.isTrue(CollUtil.isNotEmpty(rule.getRegulationConditions()), "getRegulationConditions cannot be empty ".concat(JSONUtil.toJsonStr(rule)));

        //规则条件字段类型转换
        final List<ConditionAttrModel> contditonAttrs = this.getConditionAttrValueTypes(rule);
        //规则事件字段类型转换
        final List<ResultDataModel> actionAttrs = this.getActionAttrValueTypes(rule);
        final ComputLogicModel model = ComputLogicModel.builder()
                .ruleId(rule.getId())
                .ruleName(rule.getName())
                .contentType(rule.getContentType())
                .channelIds(new HashSet<>(rule.getChannel()))
                .eventCode(rule.getRegulationType())
                .stage(rule.getProcessPhase())
                .createTime(rule.getCreateTime())
                .status(rule.getStatus())
                .weight(NumberUtil.isNumber(rule.getRegulationWeight()) ? NumberUtil.parseInt(rule.getRegulationWeight()) : -1)
                .condition(ConditionModel.builder()
                        .logicalSymbol(rule.getMatchingRule())
                        .attrs(contditonAttrs)
                        .build())
                .resultData(actionAttrs)
                .build();
        log.debug("model:{}", model);

        return model;
    }

    private List<ConditionAttrModel> getConditionAttrValueTypes(AysRegulationInfoVo rule) {
        try {
            if (CollUtil.isEmpty(rule.getRegulationConditions())) {
                return Collections.EMPTY_LIST;
            }
            List<ConditionAttrModel> attrs = new CopyOnWriteArrayList<>();
            for (RegulationDetailsVo vo : rule.getRegulationConditions()) {
                Assert.isTrue(StrUtil.isNotBlank(vo.getFieldName()), "getFieldName cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(vo.getLogicalOperator()), "getLogicalOperator cannot be empty");
                //               Assert.isTrue(StrUtil.isNotBlank(vo.getConditionDetail()), "getConditionDetail cannot be empty");

                //规则字段类型转换
                ConditionAttrModel attr = ConditionAttrModel.builder()
//                        .sourceAttr(StrUtil.toCamelCase(vo.getFieldName()))
                        .sourceAttr(vo.getFieldName())
                        .contionType(vo.getConditionType())
                        .value(vo.getConditionDetail())
                        .signOperation(vo.getLogicalOperator())
                        .targetAttr(vo.getVariableValue())
                        .build();

//                if (ObjectUtil.isEmpty(RuleConditionType.getByCode(vo.getConditionType()))) {
//                    throw new BussinessException(StrUtil.format("未匹配到判断内容 {}", vo));
//                }

                log.trace("转换后的 attr {}", attr);
                attrs.add(attr);
            }
            return Collections.unmodifiableList(attrs);
        } catch (Exception e) {
            log.error("转换规则【条件】异常 {}", rule);
            log.error(e.getMessage(), e);
            throw e;
        }
    }


    /**
     * 规则事件字段类型转换
     */
    private List<ResultDataModel> getActionAttrValueTypes(AysRegulationInfoVo rule) {
        try {
            if (CollUtil.isEmpty(rule.getRegulationPerformAction())) {
                return null;
            }
            List<ResultDataModel> attrs = new CopyOnWriteArrayList<>();
            for (RegulationDetailsVo vo : rule.getRegulationPerformAction()) {
                Assert.isTrue(StrUtil.isNotBlank(vo.getFieldName()), "getFieldName cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(vo.getLogicalOperator()), "getLogicalOperator cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(vo.getConditionDetail()), "getConditionDetail cannot be empty");

                ResultDataModel attr = ResultDataModel.builder()
//                        .targetAttr(StrUtil.toCamelCase(vo.getFieldName()))
                        .targetAttr(vo.getFieldName())
                        .contionType(vo.getConditionType())
                        .value(vo.getConditionDetail())
                        .signOperation(vo.getLogicalOperator())
                        .build();

                if (ObjectUtil.isEmpty(RuleConditionType.getByCode(vo.getConditionType()))) {
                    throw new BussinessException(StrUtil.format("未匹配到判断内容 {}", vo));
                }
                log.trace("转换后的 attr {}", attr);
                attrs.add(attr);
            }

            return Collections.unmodifiableList(attrs);
        } catch (Exception e) {
            log.error("转换规则【事件】异常 {}", rule);
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 需通知洞察引擎本次执行失败
     * 校验状态 -1 未校验 0 校验中 1 校验成功 2 校验失败 默认为-1
     *
     * @return
     */
    @Override
    public boolean setRuleStatusOk(final String workId, final String clientId) {
        //设置固定token，此token需添加到auth服务白名单token集合
        ServiceContextHolder.setToken(config.getDefaultToken());
        final Result<?> result = ruleServiceClient.pushValidateRuleStatus(InsValidateRuleInfoModel.builder()
                .workId(workId)
                .validateStatus("1")
                .clientId(clientId)
                .build());

        if ("200".equals(result.getCode())) {
            log.info("知洞察引擎本次执行成功 {}", workId);
        } else {
            throw new BussinessException(StrUtil.format("获取洞察引擎规则数据请求失败 workId:{} errmsg: {}", result.getMessage(), workId));
        }

        return true;
    }

    @Override
    public boolean setRuleStatusErr(final String workId, final String clientId) {
        //设置固定token，此token需添加到auth服务白名单token集合
        ServiceContextHolder.setToken(config.getDefaultToken());
        final Result<?> result = ruleServiceClient.pushValidateRuleStatus(InsValidateRuleInfoModel.builder()
                .workId(workId)
                .validateStatus("2")
                .clientId(clientId)
                .build());

        if ("200".equals(result.getCode())) {
            log.info("知洞察引擎本次执行成功 {}", workId);
        } else {
            throw new BussinessException(StrUtil.format("获取洞察引擎规则数据请求失败 workId:{} errmsg: {}", result.getMessage(), workId));
        }

        return true;
    }

    @Override
    public void removeCache() {
       /* Set<String> allResourceGroupKeys = cacheUtils.getResourceGroupKeys(ServiceContextHolder.getSystemId(), "rules");
        log.info("全部key:{}", allResourceGroupKeys.size());
        allResourceGroupKeys.stream().forEach(key -> {
            if (key.startsWith("VDP_:")) {
                String replace = key.replace("VDP_:", "");
                boolean remove = rulesCache.remove(replace);
                log.info("删除缓存 {} {}", key, remove);
            } else {
                boolean remove = rulesCache.remove(key);
                log.info("删除缓存 {} {}", key, remove);
            }
        });*/
    }


    @Override
    public boolean cleanCache() {
//        CacheUtil.cleanCache(RULE_DATA_KEY_TAG, RULE_DATA_CACHE);
        return true;
    }
}
