package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsClosedRuleService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsClosedBatchOperationModel;
import com.voc.service.insights.engine.model.InsClosedRuleModel;
import com.voc.service.insights.engine.model.InsClosedRuleQueryModel;
import com.voc.service.insights.engine.vo.InsRegulationConditionConfigVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 闭环规则控制器
 */
@Slf4j
@RestController
@Tag(name = "闭环规则管理", description = "闭环规则管理接口")
@RequestMapping("/insClosedRule")
public class InsClosedRuleController extends AbstractConditionFilters {

    @Resource
    private IInsClosedRuleService insClosedRuleService;

    @AutoLog(value = "闭环规则-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询规则列表")
    @PostMapping("/queryRulePage")
    public Result<PageInfo<InsClosedRuleModel>> queryRulePage(@RequestBody InsClosedRuleQueryModel queryModel) {
        return Result.OK(insClosedRuleService.queryRulePage(queryModel));
    }

    @AutoLog(value = "闭环规则-获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取规则详情")
    @GetMapping("/{ruleId}")
    public Result<InsClosedRuleModel> getRuleDetail(@PathVariable String ruleId) {
        return Result.OK(insClosedRuleService.queryRuleDetail(ruleId));
    }

    @AutoLog(value = "闭环规则-新增规则")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增规则")
    @PutMapping("/insert")
    public Result insertRule(@Valid @RequestBody InsClosedRuleModel ruleModel) {
        return Result.OK(insClosedRuleService.insertRule(ruleModel));
    }

    @AutoLog(value = "闭环规则-编辑规则")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "编辑规则")
    @PutMapping("/update")
    public Result updateRule(@Valid @RequestBody InsClosedRuleModel ruleModel) {
        return Result.OK(insClosedRuleService.updateRule(ruleModel));
    }

    @AutoLog(value = "闭环规则-复制规则")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "复制规则")
    @PutMapping("/{ruleId}")
    public Result copyRule(@PathVariable String ruleId) {
        return Result.OK(insClosedRuleService.copyRule(ruleId));
    }

    @AutoLog(value = "闭环规则-根据分类ID查询规则数量")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据分类ID查询规则数量")
    @PostMapping("/countByCategoryIds")
    public Result countByCategoryIds(@RequestBody Set<String> categoryIds) {
        return Result.OK(insClosedRuleService.countByCategoryIds(categoryIds));
    }

    @AutoLog(value = "闭环规则-批量操作")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量操作规则")
    @PostMapping("/batchOperation")
    public Result<?> batchOperation(@Valid @RequestBody InsClosedBatchOperationModel batchOperationModel) {
        return Result.OK(insClosedRuleService.batchOperation(batchOperationModel));
    }


    @AutoLog(value = "闭环规则-获取条件配置")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取条件配置")
    @GetMapping("/findConditionConfig")
    public Result<List<InsRegulationConditionConfigVo>> findConditionConfig() {
        return Result.OK(insClosedRuleService.findConditionConfig());
    }


    @Override
    @GetMapping("/conditions")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, CLOSED_RULE_CONFIRM_METHOD, CLOSED_RULE_CONDITION_OPERATOR,
                CLOSED_RULE_AUDIT_METHOD, CLOSED_RULE_ALERT_CHANNEL, CLOSED_RULE_LEVEL, CLOSED_RULE_CONDITION_OPTION,
                CLOSED_RULE_ENABLED_STATUS, CLOSED_RULE_PRIORITY, CLOSED_RULE_TYPE, CLOSED_RULE_CONDITION_VALUE_TYPE,
                SELF_BRAND, SELF_BRAND_CAR,EMOTION,INTENTION,CONTENT_TYPE,BATCH_AD_TYPE
        )));
    }
}