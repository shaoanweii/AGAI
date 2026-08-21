package com.voc.service.insights.engine.alert.web;


import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.alert.AltAlarmDataService;
import com.voc.service.insights.engine.api.alert.abstracts.IInsAlertBaseService;
import com.voc.service.insights.engine.api.constants.AlertTaskEnum;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.alert.AltAlarmDataModel;
import com.voc.service.insights.engine.model.alert.InsAltDataModel;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据监控-告警数据表(AltCoreData)表控制层
 *
 * @author leiww
 * @since 2024-04-26 10:42:21
 */
@RestController
@Tag(name = "数据监控-告警数据表", description = "数据监控-告警数据表")
@RequestMapping("/altCoreData")
@AllArgsConstructor
public class AltAlarmDataController extends AbstractConditionFilters {
    /**
     * 服务对象
     */
    private final AltAlarmDataService altCoreDataService;

    private final Map<String, IInsAlertBaseService> taskMap;

    /**
     * 分页查询所有数据
     *
     * @param model 查询实体
     * @return 所有数据
     */
    @AutoLog(value = "数据监控-告警数据表-分页查询")
    @Operation(summary = "数据监控-告警数据表-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/list")
    public Result selectAll(@RequestBody AltAlarmDataModel model) {
        return this.altCoreDataService.queryBySelect(model);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "数据监控-告警数据表-获取详情")
    @Operation(summary = "数据监控-告警数据表-获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/{id}")
    public Result selectOne(@PathVariable Serializable id) {
        return Result.OK(this.altCoreDataService.queryById(id));
    }

    @AutoLog(value = "数据监控-告警数据表-获取告警节点详情")
    @Operation(summary = "数据监控-告警数据表-获取告警节点详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/selectNode")
    public Result selectNode(@RequestBody AltAlarmDataModel model) {
        Assert.notNull(model.getDataType(), "告警节点不能为空！");
        AlertTaskEnum byCode = AlertTaskEnum.getByCode(model.getDataType());
        IInsAlertBaseService iInsAlertBaseService = this.taskMap.get(byCode.getCode());
        model.setCreateTime(LocalDateTime.now());
        List<InsAltDataModel> insAltDataModels = iInsAlertBaseService.alertBarChart(model);
        return Result.OK(insAltDataModels);
    }

    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, ALARM_NODE, ALARM_LEVEL)));
    }


}

