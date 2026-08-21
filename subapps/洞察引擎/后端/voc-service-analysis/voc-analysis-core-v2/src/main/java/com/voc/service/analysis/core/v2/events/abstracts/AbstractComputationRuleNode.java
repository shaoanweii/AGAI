package com.voc.service.analysis.core.v2.events.abstracts;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.events.context.AnlysisEventContext;
import com.voc.service.analysis.core.v2.events.model.ConditionItemModel;
import com.voc.service.analysis.core.v2.utils.AnlysisContextHolder;
import com.voc.service.analysis.model.RuleModel;
import com.voc.service.analysis.model.rule.ComputLogicModel;
import com.voc.service.analysis.model.rule.ConditionAttrModel;
import com.voc.service.analysis.model.rule.ResultDataModel;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.insights.engine.enums.RuLerelations;
import com.voc.service.insights.engine.enums.RuleConditionType;
import com.voc.service.insights.engine.enums.RuleLogicalOperator;
import com.voc.service.insights.engine.enums.RuleVariableType;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Title: AbstractComputationRuleNode
 * @Package: com.voc.service.analysis.core.events
 * @Description: 事件处理抽象类
 * @Author: cuick
 * @Date: 2024/4/8 16:29
 * @Version:1.0
 */
public abstract class AbstractComputationRuleNode extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(AbstractComputationRuleNode.class);
    @Setter
    @Getter
    public ComputLogicModel computLogicModel;
    public List<ConditionItemModel> conditionResult;
    @Autowired
    AnalysisConfig analysisConfig;

    /**
     * 事件处理方法实现
     *
     * @param rule
     */
    public void action(String clientId, ComputLogicModel rule) {
        log.info("data.getCondition() {}", rule);
        JSONObject data = this.getContentItem();
        if (CollUtil.isEmpty(rule.getResultData())) {
            log.info("规则已命中，执行默认操作");
            // 获取resultDatum对象的目标属性和值
            List<ConditionAttrModel> attrs = rule.getCondition().getAttrs();
            if(CollUtil.isNotEmpty(attrs)){
                final Map<String, Set<String>> map = AnlysisContextHolder.getResourcesGroupData(clientId);
                attrs.stream().forEach(e->{
                    if(RuleConditionType.Regex.getCode().equalsIgnoreCase(e.getContionType())){
                        this.action_regexs(data,analysisConfig.content_attr_name, CollUtil.newHashSet(e.getValue()));
                    } else if (RuleConditionType.ResourceGroup.getCode().equalsIgnoreCase(e.getContionType())) {
                        if(CollUtil.isNotEmpty(map)){
                            final Set<String> values = map.get(e.getValue());
                            this.action_default(data,analysisConfig.content_attr_name , values);
                        }
                    }else {
                        this.action_default(data,analysisConfig.content_attr_name , CollUtil.newHashSet(e.getValue()));
                    }
                });
            }
            log.info("规则set值1:{}",this.getComputLogicModel());
            AnlysisEventContext context = this.getRequestData();
            //记录本条内容命中的规则
            if(ObjectUtil.isEmpty(context.getFinshData().getHitRuleList())){
                context.getFinshData().setHitRuleList(new ArrayList<>());
            }
            context.getFinshData().getHitRuleList().add(RuleModel.builder()
                    .eventCode(this.getComputLogicModel().getEventCode())
                    .id(this.getComputLogicModel().getRuleId())
                    .title(this.getComputLogicModel().getRuleName())
                    .build());
            return;
        }
        for (ResultDataModel resultDatum : rule.getResultData()) {
            // 获取resultDatum对象的目标属性和值
            final String attr = resultDatum.getTargetAttr();
            final String value = resultDatum.getValue();
            Boolean isHandle = Boolean.FALSE;
            log.info("本次处理事件类型:{}", this.getNodeId());
            if (RuleConditionType.Value.getCode().equalsIgnoreCase(resultDatum.getContionType())) {
                log.trace("规则已命中，执行操作【value】");
                isHandle = this.action_values(data, attr, CollUtil.newHashSet(value));
            } else if (RuleConditionType.Regex.getCode().equalsIgnoreCase(resultDatum.getContionType())) {
                log.trace("规则已命中，执行操作【regex】");
                isHandle = this.action_regexs(data, attr, CollUtil.newHashSet(value));
                //只有在清洗类（R04)事件时，才能处理资源组属性配置
            } else if (RuleConditionType.ResourceGroup.getCode().equalsIgnoreCase(resultDatum.getContionType())) {
                log.trace("规则已命中，执行操作R04【resourceGroup】");
                Map<String, Set<String>> map = AnlysisContextHolder.getResourcesGroupData(clientId);
                if (map.containsKey(resultDatum.getValue())) {
                    final Set<String> values = map.get(resultDatum.getValue());
                    if (CollUtil.isEmpty(values)) {
                        log.info(StrUtil.format("未获取到静态数据集【资源组】 {}", attr));
                        continue;
                    }
                    isHandle = this.action_values(data, attr, values);
                }
            } else {
                throw new BussinessException(StrUtil.format("执行事件中未匹配到逻辑运算符 {}，只能包含 {}", resultDatum.getContionType()
                        , Arrays.asList(RuleConditionType.Value.getCode(), RuleConditionType.Regex.getCode())));
            }
            log.info("动作执行结果:{}", isHandle);
            if (isHandle) {
                AnlysisEventContext context = this.getRequestData();
                //记录本条内容命中的规则
                log.info("规则set值2:{}",this.getComputLogicModel());
                if(ObjectUtil.isEmpty(context.getFinshData().getHitRuleList())){
                    context.getFinshData().setHitRuleList(new ArrayList<>());
                }
                context.getFinshData().getHitRuleList().add(RuleModel.builder()
                        .eventCode(this.getComputLogicModel().getEventCode())
                        .id(this.getComputLogicModel().getRuleId())
                        .title(this.getComputLogicModel().getRuleName())
                        .build());
            }
        }
    }

    /**
     * 默认操作
     */
    public abstract void action_default(JSONObject data, String attrName, Set<String> values);

    /**
     * value操作
     */
    public abstract Boolean action_values(JSONObject data, String attrName, Set<String> values);

//    public abstract void action_value(JSONObject data, String attrName, String value);

    /**
     * regex操作
     */
    public abstract Boolean action_regexs(JSONObject data, String attrName, Set<String> values);

    /**
     * 条件关系判断
     *
     * @return
     */
    public boolean relationshipProcess(String clientId, ComputLogicModel rule) {
//        AnlysisEventContext context = this.getRequestData();
        // 获取条件关系
        List<ConditionItemModel> conditionResult = new CopyOnWriteArrayList<>();
        log.info("data.getCondition() {}", rule);

        for (ConditionAttrModel attr : rule.getCondition().getAttrs()) {
            final String sign = attr.getSignOperation();
            //记录每条规则判断结果
            boolean rs = false;
            List<String> extAttrs4 = new ArrayList<>();
            String attrValue = this.getContentItem(attr.getSourceAttr());

            if(ObjectUtil.isEmpty(attrValue)&&!attr.getSourceAttr().contains(",")&&attr.getSourceAttr().contains(".")){
                attrValue = this.getExtAttrs6(attr.getSourceAttr());
            }

            if(ObjectUtil.isEmpty(attrValue)&&!attr.getSourceAttr().contains(",")&&attr.getSourceAttr().contains(".")){
                attrValue = this.getExtAttrs5(attr.getSourceAttr());
            }

            if(ObjectUtil.isEmpty(attrValue)&&!attr.getSourceAttr().contains(",")&&!attr.getSourceAttr().contains(".")){
                attrValue = this.getExtAttrs(attr.getSourceAttr());
            }

            if(ObjectUtil.isEmpty(attrValue)&&!attr.getSourceAttr().contains(",")&&!attr.getSourceAttr().contains(".")){
                attrValue = this.getExtAttrs2(attr.getSourceAttr());
            }

            if(ObjectUtil.isEmpty(attrValue)&&!attr.getSourceAttr().contains(",")&&!attr.getSourceAttr().contains(".")){
                attrValue = this.getExtAttrs3(attr.getSourceAttr());
            }

            if(ObjectUtil.isEmpty(attrValue)&&attr.getSourceAttr().contains(",")&&!attr.getSourceAttr().contains(".")){
                extAttrs4 = this.getExtAttrs4(attr.getSourceAttr());
            }

            final String targetAttr = attr.getTargetAttr();

            //判断逻辑运算符
            if (Arrays.asList(
                    RuleLogicalOperator.Empty.getCode(),
                    RuleLogicalOperator.NotEmpty.getCode()
            ).contains(sign)) {
                rs = signOperation(sign, attrValue, attr.getValue());
                conditionResult.add(ConditionItemModel.builder()
                        .sign(sign)
                        .attrName(attr.getSourceAttr()).attrValue(attrValue)
                        .value(attr.getValue())
                        .result(rs).build());
                continue;
            }else if(RuleVariableType.TextLength.getCode().equalsIgnoreCase(targetAttr)){
                rs = signOperation(sign, ObjectUtil.isNotEmpty(attrValue)?String.valueOf(attrValue.length()): "0", attr.getValue());

                conditionResult.add(ConditionItemModel.builder()
                        .sign(sign)
                        .attrName(attr.getSourceAttr()).attrValue(attrValue)
                        .value(attr.getValue())
                        .result(rs).build());
                continue;
            }
            if (StrUtil.isBlank(attrValue)&&ObjectUtil.isEmpty(extAttrs4)) {
                continue;
            }
            if (RuleConditionType.Value.getCode().equalsIgnoreCase(attr.getContionType())) {
                rs = signOperation(sign, attrValue, attr.getValue());
                conditionResult.add(ConditionItemModel.builder()
                        .sign(sign)
                        .attrName(attr.getSourceAttr()).attrValue(attrValue)
                        .value(attr.getValue())
                        .result(rs).build());
            } else if (RuleConditionType.Regex.getCode().equalsIgnoreCase(attr.getContionType())) {
                rs = regularExpressionOperation(sign, attr.getValue(), attrValue);
                conditionResult.add(ConditionItemModel.builder()
                        .sign(sign)
                        .attrName(attr.getSourceAttr()).attrValue(attrValue)
                        .value(attr.getValue())
                        .result(rs).build());
            } else if (RuleConditionType.ResourceGroup.getCode().equalsIgnoreCase(attr.getContionType())) {
                Map<String, Set<String>> map = AnlysisContextHolder.getResourcesGroupData(clientId);
                if (map.containsKey(attr.getValue())) {
                    final Set<String> list = map.get(attr.getValue());
                    if (CollUtil.isEmpty(list)) {
                        log.info(StrUtil.format("未获取到静态数据集【资源组】 {}", attr));
                        continue;
                    }
                    if(ObjectUtil.isEmpty(attrValue)&&ObjectUtil.isNotEmpty(extAttrs4)){
                        Set<Boolean> collect = extAttrs4.stream().map(e -> {
                            return signOperation2(sign, e, list);
                        }).collect(Collectors.toSet());
                        rs =  collect.stream().allMatch(rs1 -> Boolean.FALSE.booleanValue() == rs1);
                    }else{
                        // 对函数进行注释
                        rs = signOperation2(sign, attrValue, list);
                    }

                    conditionResult.add(ConditionItemModel.builder()
                            .sign(sign)
                            .attrName(attr.getSourceAttr()).attrValue(attrValue)
                            .value(attr.getValue())
                            .values(list)
                            .result(rs).build());
                } else {
                    log.info(StrUtil.format("未获取到静态数据集【资源组】 {}", attr));
                }
            } else {
                throw new BussinessException(StrUtil.format("未匹配到判断内容 {}", attr));
            }
        }
        if (CollUtil.isEmpty(conditionResult)) {
            return false;
        }

        this.conditionResult = conditionResult;

        if (log.isTraceEnabled()) {
            log.trace("conditionResult ：{}", JSONUtil.toJsonStr(conditionResult));
        }
        //逻辑运算
        return this.logicalSymbolOperation(rule.getCondition().getLogicalSymbol(), conditionResult);
    }



    /**
     * 获取本次处理内容项的属性值
     *
     * @param attr
     * @return
     */
    public String getContentItem(final String attr) {
        JSONObject data = this.getContentItem();
        if (ObjectUtil.isNotNull(data)) {
            if(attr.contains(".")){
                    String[] split = attr.split("\\.");
                    String s1 = split[1];
                    String s2 = split[0];
                if(data.containsKey(s2)){
                    String s = data.getJSONObject(s2).get(s1, String.class);
                    if(!StrUtil.isBlank(s)){
                        return s;
                    }
                }
            }else{
                final String val = data.get(attr, String.class);
                if (!StrUtil.isBlank(val)) {
                    return val;
                }else{
                    if(data.containsKey("ext")){
                        final String s = data.getJSONObject("ext").get(attr, String.class);
                        if(!StrUtil.isBlank(s)){
                            return s;
                        }
                    }
                }
            }
        }
        return null;
        //     throw new BussinessException(String.format("获取内容项属性值失败，, attr:%s -> content:%s ", attr, JSONUtil.toJsonStr(data)));
    }

    public String getExtAttrs(final String attr) {
        JSONObject data = this.getExtAttrs();
        if (ObjectUtil.isNotNull(data)) {
            final String val = data.get(attr, String.class);
            if (!StrUtil.isBlank(val)) {
                return val;
            }else{
                if(data.containsKey("data")){
                    JSONObject data1 = data.getJSONObject("data");
                    String s1 = data1.get(attr, String.class);
                    if(!StrUtil.isBlank(s1)){
                        return s1;
                    }else{
                        if(data1.containsKey("retweeted")){
                            String s = data1.getJSONObject("retweeted").get(attr, String.class);
                            if(!StrUtil.isBlank(s)){
                                return s;
                            }
                        }
                    }
                }
            }
        }
        return null;
        //     throw new BussinessException(String.format("获取内容项属性值失败，, attr:%s -> content:%s ", attr, JSONUtil.toJsonStr(data)));
    }

    public String getExtAttrs2(final String attr) {
        JSONObject data = this.getExtAttrs2();
        if (ObjectUtil.isNotNull(data)) {
            final String val = data.get(attr, String.class);
            if (!StrUtil.isBlank(val)) {
                return val;
            }else{
                if(data.containsKey("data")){
                    JSONObject data1 = data.getJSONObject("data");
                    String s1 = data1.get(attr, String.class);
                    if(!StrUtil.isBlank(s1)){
                        return s1;
                    }else{
                        if(data1.containsKey("retweeted")){
                            String s = data1.getJSONObject("retweeted").get(attr, String.class);
                            if(!StrUtil.isBlank(s)){
                                return s;
                            }
                        }
                    }
                }
            }
        }
        return null;
        //     throw new BussinessException(String.format("获取内容项属性值失败，, attr:%s -> content:%s ", attr, JSONUtil.toJsonStr(data)));
    }

    public String getExtAttrs3(final String attr) {
        JSONObject data = this.getExtAttrs3();
        if (ObjectUtil.isNotNull(data)) {
            final String val = data.get(attr, String.class);
            if (!StrUtil.isBlank(val)) {
                return val;
            }else{
                if(data.containsKey("data")){
                    JSONObject data1 = data.getJSONObject("data");
                    String s1 = data1.get(attr, String.class);
                    if(!StrUtil.isBlank(s1)){
                        return s1;
                    }else{
                        if(data1.containsKey("retweeted")){
                            String s = data1.getJSONObject("retweeted").get(attr, String.class);
                            if(!StrUtil.isBlank(s)){
                                return s;
                            }
                        }
                    }
                }
            }
        }
        return null;
        //     throw new BussinessException(String.format("获取内容项属性值失败，, attr:%s -> content:%s ", attr, JSONUtil.toJsonStr(data)));
    }


    public List<String> getExtAttrs4(final String attr) {
        List<String> attrValue = new ArrayList<>();
        JSONObject data = this.getExtAttrs2();
        if (ObjectUtil.isNotNull(data)) {
            String[] split = attr.split(",");
            for (String s : split){
                final String val = data.get(s, String.class);
                if(ObjectUtil.isNotEmpty(val)){
                    attrValue.add(val);
                }
                if(data.containsKey("data")){
                    JSONObject data1 = data.getJSONObject("data");
                    String s1 = data1.get(s, String.class);
                    if(!StrUtil.isBlank(s1)){
                        attrValue.add(s1);
                    }
                    if(data1.containsKey("retweeted")){
                        String s2 = data1.getJSONObject("retweeted").get(s, String.class);
                        if(!StrUtil.isBlank(s2)){
                            attrValue.add(s2);
                        }
                    }
                }
            }
        }
        return attrValue;
        //     throw new BussinessException(String.format("获取内容项属性值失败，, attr:%s -> content:%s ", attr, JSONUtil.toJsonStr(data)));
    }

    public String getExtAttrs5(final String attr){
        JSONObject data = this.getExtAttrs();
        if (ObjectUtil.isNotNull(data)) {
            if(data.containsKey("data")){
                JSONObject data1 = data.getJSONObject("data");
                String s1 = attr;
                if(attr.contains(".")){
                    String[] split = attr.split("\\.");
                    s1 = split[1];
                }
                if(data1.containsKey("retweeted")){
                    String s = data1.getJSONObject("retweeted").get(s1, String.class);
                    if(!StrUtil.isBlank(s)){
                        return s;
                    }
                }
            }
        }
        return null;
    }


    private String getExtAttrs6(String attr) {
        JSONObject data = this.getExtAttrs2();
        if (ObjectUtil.isNotNull(data)&&attr.contains(".")) {
            String[] split = attr.split("\\.");
            String s1 = split[1];
            final String val = data.get(s1, String.class);
            if (!StrUtil.isBlank(val)) {
                return val;
            }
        }
        return null;
    }

    /**
     * 重新赋值本次处理内容项的属性值
     *
     * @param attr
     * @param value
     */
    public Boolean setContentItem(JSONObject data, final String attr, String value) {
        AnlysisEventContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(attr), "setContentItem attr cannot be empty");
        Assert.isTrue(ObjectUtil.isNotNull(data), "setContentItem data cannot be empty");

        // 更新数据的方法
        final JSONObject newData;
        final String oldValue = data.get(attr, String.class);
        // 检查是否包含指定属性
        if (data.containsKey(attr)) {
            newData = data.set(attr, value);
        } else {
            // 使用同步块以确保线程安全
            synchronized (context.getFinshData()) {
                log.error("setContentItem 未找到对应字段 {}", attr);
                newData = data.putOnce(attr, value);
            }
        }
        // 如果 newData 不为 null，则更新上下文中的数据
        if (ObjectUtil.isNotNull(newData)) {
            context.getFinshData().setData(newData);
            return Boolean.TRUE;
        }
        log.info("内容替换成功: {}: [{}]->[{}]", attr, oldValue, value);
        return Boolean.FALSE;
    }

    public JSONObject getContentItem() {
        AnlysisEventContext context = this.getRequestData();
        String dataStr = String.valueOf(context.getFinshData().getData());
        return JSONUtil.parseObj(dataStr);
    }

    public JSONObject getExtAttrs() {
        AnlysisEventContext context = this.getRequestData();
        String dataStr = ObjectUtil.isNotEmpty(context.getFinshData().getBizExtAttrs())?String.valueOf(context.getFinshData().getBizExtAttrs()):null;
        if(ObjectUtil.isNull(dataStr)){
            return null;
        }
        return JSONUtil.parseObj(dataStr);
    }

    public JSONObject getExtAttrs2() {
        AnlysisEventContext context = this.getRequestData();
        String dataStr = ObjectUtil.isNotEmpty(context.getFinshData().getBizExtAttrs2())?String.valueOf(context.getFinshData().getBizExtAttrs2()):null;
        if(ObjectUtil.isNull(dataStr)){
            return null;
        }
        return JSONUtil.parseObj(dataStr);
    }

    public JSONObject getExtAttrs3() {
        AnlysisEventContext context = this.getRequestData();
        String dataStr = ObjectUtil.isNotEmpty(context.getFinshData().getBizExtAttrs3())?String.valueOf(context.getFinshData().getBizExtAttrs3()):null;
        if(ObjectUtil.isEmpty(dataStr)){
            return null;
        }
        return JSONUtil.parseObj(dataStr);
    }


    public String regularProcess(String regex, CharSequence content) {
        log.error("未实现正则处理方法 {}", this.getNodeId());
        return String.valueOf(content);
    }


    /**
     * 正则表达式计算
     *
     * @return
     */
    public boolean regularExpressionOperation(String sign, String v1, String v2) {
        Assert.isTrue(StrUtil.isNotBlank(sign), "signOperation cannot be empty");
        if(StrUtil.isBlank(v2)){
            return true;
        }
        if(cn.hutool.core.codec.Base64.isBase64(v1)) {
            v1 = cn.hutool.core.codec.Base64.decodeStr(v1);
        }
        if (RuleLogicalOperator.Contain.getCode().equalsIgnoreCase(sign)) {
            return ReUtil.contains(v1, v2);
        } else if (RuleLogicalOperator.NotContain.getCode().equalsIgnoreCase(sign)) {
            return !ReUtil.contains(v1, v2);
        } else {
            throw new BussinessException(StrUtil.format("正则表达式条件判断不支持 {}", sign));
        }
    }

    /**
     * 逻辑运算
     */
    public boolean logicalSymbolOperation(String logicalSymbol, List<ConditionItemModel> result) {
        Assert.isTrue(StrUtil.isNotBlank(logicalSymbol), "logicalSymbolOperation->logicalSymbol cannot be empty");
        Assert.isTrue(CollUtil.isNotEmpty(result), "logicalSymbolOperation->result cannot be empty");

        if (StrUtil.equalsAnyIgnoreCase(RuLerelations.Or.getCode(), logicalSymbol)) {
            return result.stream().anyMatch(rs -> Boolean.TRUE.booleanValue() == rs.isResult());
        } else if (StrUtil.equalsAnyIgnoreCase(RuLerelations.And.getCode(), logicalSymbol)) {
            return result.stream().allMatch(rs -> Boolean.TRUE.booleanValue() == rs.isResult());
        }
        return false;
    }

    private boolean signOperation2(String sign, String attrValue, Set<String> list) {
        Assert.isTrue(StrUtil.isNotBlank(sign), "signOperation2->sign cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(attrValue), "signOperation2->attrValue cannot be empty");
        Assert.isTrue(CollUtil.isNotEmpty(list), "signOperation2->list cannot be empty");

        if (RuleLogicalOperator.Equals.getCode().equalsIgnoreCase(sign)) {
            final Set<Boolean> values = list.stream().map(item -> StrUtil.equals(attrValue, item)).collect(Collectors.toSet());
            //任何一个为真就为真
            return values.stream().anyMatch(rs -> Boolean.TRUE.booleanValue() == rs);
        } else if (RuleLogicalOperator.NotEquals.getCode().equalsIgnoreCase(sign)) {
            final Set<Boolean> values = list.stream().map(item -> StrUtil.equals(attrValue, item)).collect(Collectors.toSet());
            //所有为假才为真
            return values.stream().allMatch(rs -> Boolean.FALSE.booleanValue() == rs);
        } else if (RuleLogicalOperator.Contain.getCode().equalsIgnoreCase(sign)) {
            return list.stream()
                    .anyMatch(tagItem -> {
                        // 分割逗号分隔的各个部分
                        String[] parts = tagItem.split(",");

                        // 检查 content 是否包含所有部分
                        return Arrays.stream(parts)
                                .allMatch(part -> {
                                    // 如果 part 包含竖线，则按竖线分割
                                    if (part.contains("|")) {
                                        // 分割为多个子项，检查 content 是否包含至少一个子项
                                        String[] subParts = part.split("\\|");
                                        return Arrays.stream(subParts)
                                                .anyMatch(subPart -> attrValue.contains(subPart.trim()));
                                    } else {
                                        // 不包含竖线，直接检查
                                        return attrValue.contains(part.trim());
                                    }
                                });
                    });

//            final Set<Boolean> values = list.stream().map(item -> StrUtil.contains(attrValue, item)).collect(Collectors.toSet());
            //任何一个为真就为真
//            return values.stream().anyMatch(rs -> Boolean.TRUE.booleanValue() == rs);
        } else if (RuleLogicalOperator.NotContain.getCode().equalsIgnoreCase(sign)) {
            final Set<Boolean> values = list.stream().map(item -> {
                return StrUtil.contains(attrValue.toUpperCase(), item.toUpperCase());
            }).collect(Collectors.toSet());
            //所有为假才为真
            return values.stream().allMatch(rs -> Boolean.FALSE.booleanValue() == rs);
        } else {
            throw new BussinessException(StrUtil.format("资源组判断不支持 {}", sign));
        }

    }

    public boolean signOperation(String sign, String v1, String v2) {
        Assert.isTrue(StrUtil.isNotBlank(sign), "signOperation sign cannot be empty");

        if (RuleLogicalOperator.Equals.getCode().equalsIgnoreCase(sign)) {
            return StrUtil.equals(v1, v2);
        } else if (RuleLogicalOperator.NotEquals.getCode().equalsIgnoreCase(sign)) {
            return !StrUtil.equals(v1, v2);
        } else if (RuleLogicalOperator.GreaterThen.getCode().equalsIgnoreCase(sign)) {
            if (!NumberUtil.isNumber(v1) || !NumberUtil.isNumber(v2)) {
                log.error("{}  ", String.format("%s 数值比较失败，v1:%s v2:%s", RuleLogicalOperator.GreaterThen, v1, v2));
                throw new BussinessException(String.format("%s 数值比较失败，v1:%s v2:%s", RuleLogicalOperator.GreaterThen, v1, v2));
            }
            return NumberUtil.compare(Long.valueOf(v1), Long.valueOf(v2)) > 0;
        } else if (RuleLogicalOperator.GreaterThenOrEqual.getCode().equalsIgnoreCase(sign)) {
            if (!NumberUtil.isNumber(v1) || !NumberUtil.isNumber(v2)) {
                log.error("{}  ", String.format("%s 数值比较失败，v1:%s v2:%s", RuleLogicalOperator.GreaterThenOrEqual, v1, v2));
                throw new BussinessException(String.format("%s 数值比较失败，v1:%s v2:%s", RuleLogicalOperator.GreaterThenOrEqual, v1, v2));
            }
            return NumberUtil.compare(Long.valueOf(v1), Long.valueOf(v2)) >= 0;
        } else if (RuleLogicalOperator.LessThen.getCode().equalsIgnoreCase(sign)) {
            if (!NumberUtil.isNumber(v1) || !NumberUtil.isNumber(v2)) {
                log.error("{}  ", String.format("%s 数值比较失败，v1:%s v2:%s", RuleLogicalOperator.LessThen, v1, v2));
                throw new BussinessException(String.format("%s 数值比较失败，v1:%s v2:%s", RuleLogicalOperator.LessThen, v1, v2));
            }
            return NumberUtil.compare(Long.valueOf(v1), Long.valueOf(v2)) < 0;
        } else if (RuleLogicalOperator.LessThenOrEqual.getCode().equalsIgnoreCase(sign)) {
            if (!NumberUtil.isNumber(v1) || !NumberUtil.isNumber(v2)) {
                log.error("{}  ", String.format("%s 数值比较失败，v1:%s v2:%s", RuleLogicalOperator.LessThenOrEqual, v1, v2));
                throw new BussinessException(String.format("%s 数值比较失败，v1:%s v2:%s", RuleLogicalOperator.LessThenOrEqual, v1, v2));
            }
            return NumberUtil.compare(Long.valueOf(v1), Long.valueOf(v2)) <= 0;
        } else if (RuleLogicalOperator.Empty.getCode().equalsIgnoreCase(sign)) {
            return StrUtil.isBlank(v1);
        } else if (RuleLogicalOperator.NotEmpty.getCode().equalsIgnoreCase(sign)) {
            return StrUtil.isNotBlank(v1);
        } else if (RuleLogicalOperator.Contain.getCode().equalsIgnoreCase(sign)) {
            return StrUtil.contains(v1, v2);
        } else if (RuleLogicalOperator.NotContain.getCode().equalsIgnoreCase(sign)) {
            return !StrUtil.contains(v1, v2);
        } else {
            throw new BussinessException(StrUtil.format("内容判断不支持 {}", sign));
        }

    }

    public static void main(String[] args) {
//        String v1 = "http(s)?://([w-]+.)+[w-]+(/[w- ./?%&=])?";
//        String s2 = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=])?";
//        boolean woddede好的 = ReUtil.contains(s2, "woddede好的");
//        System.out.println(cn.hutool.core.codec.Base64.isBase64(cn.hutool.core.codec.Base64.encode(v1)));
//        v1 = cn.hutool.core.codec.Base64.decodeStr(cn.hutool.core.codec.Base64.encode(v1));
//        System.out.println(v1);
//        System.out.println(woddede好的);
//        System.out.println(ReUtil.contains(s2, "我的车不知道轮胎下摆臂还是方向盘异响,去任意4S点检查是免费的吗?"));
//
//        System.out.println(v1);
//        System.out.println(s2);

        List<String> tagItems = Arrays.asList(
                "这款,必备",
                "改装,汽车用品|好物|摆件|挂件|小配件|防护神器|必备神器|汽车神器|脚垫|坐垫|防虫网|密封条|保护罩|装饰配件|收纳神器|保护套|宝藏单品|头枕|防撞条|手机支架|防护垫|钥匙电池|记录仪|汽车配件|钥匙套|雨刮片|雨刮器|减震套|方向盘套|门槛条|密封圈|专用配件|纸巾盒|储物盒|装饰配件|挡泥板|必买用品|必备用品|保护盖|靠枕",
                "专用,汽车配件,雨刮片",
                "车主,爱车,必备用品",
                "安装,这款,必备"
        );

        String content = "魏牌高山8配置|魏牌高山8汽车用品|魏牌高山8电视屏|魏牌高 #高山8改装 #魏牌高山系列 #电视屏保护套 #老六车改 25款魏牌蓝山/高山789后排车顶娱乐屏电视保护套防碰撞保护套内饰";
        boolean b = validateWithStream(content, tagItems);
        System.out.println(b);
    }


    public static boolean validateWithStream(String content, List<String> tagItems) {
        return tagItems.stream()
                .anyMatch(tagItem -> {
                    // 分割逗号分隔的各个部分
                    String[] parts = tagItem.split(",");

                    // 检查 content 是否包含所有部分
                    return Arrays.stream(parts)
                            .allMatch(part -> {
                                // 如果 part 包含竖线，则按竖线分割
                                if (part.contains("|")) {
                                    // 分割为多个子项，检查 content 是否包含至少一个子项
                                    String[] subParts = part.split("\\|");
                                    return Arrays.stream(subParts)
                                            .anyMatch(subPart -> content.contains(subPart.trim()));
                                } else {
                                    // 不包含竖线，直接检查
                                    return content.contains(part.trim());
                                }
                            });
                });
    }


}
