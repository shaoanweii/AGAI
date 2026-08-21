package com.voc.service.insights.engine.alert.web;


import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.alert.AltTaskConfigDataService;
import com.voc.service.insights.engine.model.alert.AltTaskConfigDataModel;
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
 * 数据监控-任务配置表(AltTaskConfigData)表控制层
 *
 * @author leiww
 * @since 2024-04-30 17:11:55
 */
@RestController
@Tag(name = "数据监控-任务配置表", description = "数据监控-任务配置表-AltTaskConfigData")
@RequestMapping("/altTaskConfigData")
@AllArgsConstructor
public class AltTaskConfigDataController {
    /**
     * 服务对象
     */
    private final AltTaskConfigDataService altTaskConfigDataService;

    /**
     * 分页查询所有数据
     *
     * @param model 查询实体
     * @return 所有数据
     */
    @AutoLog(value = "数据监控-任务配置表-分页查询")
    @Operation(summary = "数据监控-任务配置表-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/list")
    public Result selectAll(@RequestBody AltTaskConfigDataModel model) {
        return this.altTaskConfigDataService.queryBySelect(model);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "数据监控-任务配置表-获取详情")
    @Operation(summary = "数据监控-任务配置表-获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/{id}")
    public Result selectOne(@PathVariable Serializable id) {
        return Result.OK(this.altTaskConfigDataService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param model 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "数据监控-任务配置表-新增数据")
    @Operation(summary = "数据监控-任务配置表-新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/insert")
    public Result insert(@RequestBody AltTaskConfigDataModel model) {
        return Result.OK(this.altTaskConfigDataService.insert(model));
    }

    /**
     * 修改数据
     *
     * @param model 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "数据监控-任务配置表-修改数据")
    @Operation(summary = "数据监控-任务配置表-修改数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PutMapping("/update")
    public Result update(@RequestBody AltTaskConfigDataModel model) {
        return Result.OK(this.altTaskConfigDataService.update(model));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @AutoLog(value = "数据监控-任务配置表-删除数据")
    @Operation(summary = "数据监控-任务配置表-删除数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @DeleteMapping("/delete")
    public Result delete(@RequestParam("idList") List<Serializable> idList) {
        return Result.OK(this.altTaskConfigDataService.deleteByIds(idList));
    }
}

