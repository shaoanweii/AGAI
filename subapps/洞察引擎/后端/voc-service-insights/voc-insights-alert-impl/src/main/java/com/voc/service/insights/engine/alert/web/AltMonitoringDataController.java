package com.voc.service.insights.engine.alert.web;


import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.alert.IInsAltMonitoringDataService;
import com.voc.service.insights.engine.model.alert.AltMonitoringDataModel;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * 数据监控-监控数据表(AltMonitoringData)表控制层
 *
 * @author leiww
 * @since 2024-04-26 15:11:34
 */
@RestController
@Tag(name = "数据监控-监控数据表", description = "数据监控-监控数据表")
@RequestMapping("/altMonitoringData")
@AllArgsConstructor
public class AltMonitoringDataController {
    /**
     * 服务对象
     */
    private final IInsAltMonitoringDataService IInsAltMonitoringDataService;

    /**
     * 分页查询所有数据
     *
     * @param model 查询实体
     * @return 所有数据
     */
    @AutoLog(value = "数据监控-监控数据表-分页查询")
    @Operation(summary = "数据监控-监控数据表-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/list")
    public Result selectAll(@RequestBody AltMonitoringDataModel model) {
        return this.IInsAltMonitoringDataService.queryBySelect(model);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "数据监控-监控数据表-获取详情")
    @Operation(summary = "数据监控-监控数据表-获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/{id}")
    public Result selectOne(@PathVariable Serializable id) {
        return Result.OK(this.IInsAltMonitoringDataService.queryById(id));
    }


    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @AutoLog(value = "数据监控-监控数据表-删除数据")
    @Operation(summary = "数据监控-监控数据表-删除数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @DeleteMapping("/delete")
    public Result delete(@RequestParam("idList") List<Serializable> idList) {
        return Result.OK(this.IInsAltMonitoringDataService.deleteByIds(idList));
    }
}

