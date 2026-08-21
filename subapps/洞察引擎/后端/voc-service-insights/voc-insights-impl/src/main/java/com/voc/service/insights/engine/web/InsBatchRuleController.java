package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsBatchRuleService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsBatchRuleBatchOperationModel;
import com.voc.service.insights.engine.model.InsBatchRuleHisModel;
import com.voc.service.insights.engine.model.InsBatchRuleModel;
import com.voc.service.insights.engine.model.InsBatchRuleQueryModel;
import com.voc.service.insights.engine.vo.InsBatchRegulationConditionConfigVo;
import com.voc.service.insights.engine.vo.InsIndicatorConfigVo;
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
 * 批量规则控制器
 * 处理规则的增删改查、复制、批量操作等请求
 */
@Slf4j
@RestController
@Tag(name = "批量规则管理", description = "批量规则管理接口")
@RequestMapping("/insBatchRule")
public class InsBatchRuleController extends AbstractConditionFilters {

    @Resource
    private IInsBatchRuleService insBatchRuleService;

    @AutoLog(value = "批量规则-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询规则列表")
    @PostMapping("/queryRulePage")
    public Result<PageInfo<InsBatchRuleModel>> queryRulePage(@RequestBody InsBatchRuleQueryModel queryModel) {
        return Result.OK(insBatchRuleService.queryRulePage(queryModel));
    }

    @AutoLog(value = "批量规则-获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取规则详情")
    @GetMapping("/{ruleId}")
    public Result<InsBatchRuleModel> getRuleDetail(@PathVariable String ruleId) {
        return Result.OK(insBatchRuleService.queryRuleDetail(ruleId));
    }

    @AutoLog(value = "批量规则-新增规则")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增规则")
    @PutMapping("/insert")
    public Result<Boolean> insertRule(@Valid @RequestBody InsBatchRuleModel ruleModel) {
        return Result.OK(insBatchRuleService.insertRule(ruleModel));
    }

    @AutoLog(value = "批量规则-编辑规则")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "编辑规则")
    @PutMapping("/update")
    public Result<Boolean> updateRule(@Valid @RequestBody InsBatchRuleModel ruleModel) {
        return Result.OK(insBatchRuleService.updateRule(ruleModel));
    }

    @AutoLog(value = "批量规则-复制规则")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "复制规则")
    @PutMapping("/{ruleId}")
    public Result<Boolean> copyRule(@PathVariable String ruleId) {
        return Result.OK(insBatchRuleService.copyRule(ruleId));
    }

    @AutoLog(value = "批量规则-删除规则")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除规则")
    @DeleteMapping("/{ruleId}")
    public Result<Boolean> deleteRule(@PathVariable String ruleId) {
        return Result.OK(insBatchRuleService.deleteRule(ruleId));
    }

    @AutoLog(value = "批量规则-批量操作")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量操作规则")
    @PostMapping("/batchOperation")
    public Result<Integer> batchOperation(@Valid @RequestBody InsBatchRuleBatchOperationModel batchOperationModel) {
        return Result.OK(insBatchRuleService.batchOperation(batchOperationModel));
    }

    @AutoLog(value = "批量规则-根据分类ID查询规则数量")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据分类ID查询规则数量")
    @PostMapping("/countByCategoryIds")
    public Result<?> countByCategoryIds(@RequestBody Set<String> categoryIds) {
        return Result.OK(insBatchRuleService.countByCategoryIds(categoryIds));
    }

    @AutoLog(value = "批量规则-查询规则历史")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询规则历史记录")
    @GetMapping("/{ruleId}/history")
    public Result<List<InsBatchRuleHisModel>> queryRuleHistory(@PathVariable String ruleId) {
        return Result.OK(insBatchRuleService.queryRuleHistory(ruleId));
    }

    @AutoLog(value = "批量规则-查询历史详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询历史记录详情")
    @GetMapping("/history/{hisId}")
    public Result<InsBatchRuleHisModel> queryRuleHistoryDetail(@PathVariable String hisId) {
        return Result.OK(insBatchRuleService.queryRuleHistoryDetail(hisId));
    }

    @AutoLog(value = "批量规则-获取条件配置")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取条件配置")
    @GetMapping("/findConditionConfig")
    public Result<List<InsBatchRegulationConditionConfigVo>> findConditionConfig() {
        return Result.OK(insBatchRuleService.findConditionConfig());
    }

    @AutoLog(value = "批量规则-获取指标条件配置")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取指标条件配置")
    @GetMapping("/findIndicatorConditionConfig")
    public Result<List<InsIndicatorConfigVo>> findIndicatorConditionConfig() {
        return Result.OK(insBatchRuleService.findIndicatorConditionConfig());
    }

    @Override
    @GetMapping("/conditions")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, CLOSED_RULE_CONFIRM_METHOD, CLOSED_RULE_CONDITION_OPERATOR,
                CLOSED_RULE_AUDIT_METHOD, CLOSED_RULE_ALERT_CHANNEL, CLOSED_RULE_LEVEL, CLOSED_RULE_CONDITION_OPTION,
                CLOSED_RULE_ENABLED_STATUS, CLOSED_RULE_PRIORITY, BATCH_RULE_TYPE, CLOSED_RULE_CONDITION_VALUE_TYPE,
                SELF_BRAND, SELF_BRAND_CAR, EMOTION, INTENTION, CONTENT_TYPE, PROVINCE, CUSTOMER_GENDER,
                WATER_MAN, V_MAN, CUSTOMER_TYPE, INDICATOR_EFFECT_RELATION, BATCH_EMOTIONAL_LEVEL, BATCH_AD_TYPE
        )));
    }

}
