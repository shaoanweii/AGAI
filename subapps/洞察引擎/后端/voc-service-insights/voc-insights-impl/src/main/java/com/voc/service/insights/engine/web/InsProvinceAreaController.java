package com.voc.service.insights.engine.web;


import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsProvinceAreaService;
import com.voc.service.insights.engine.model.InsProvinceAreaModel;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 区域城市信息(InsProvinceArea)表控制层
 *
 * @author leiww
 * @since 2024-01-26 17:59:03
 */
@RestController
@Tag(name = "区域城市信息", description = "区域城市信息")
@RequestMapping("/insProvinceArea")
public class InsProvinceAreaController {
    /**
     * 服务对象
     */
    @Resource
    private IInsProvinceAreaService insProvinceAreaService;

    /**
     * 分页查询所有数据
     *
     * @param insProvinceArea 查询实体
     * @return 所有数据
     */
    @AutoLog(value = "区域城市信息-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询")
    @GetMapping("/list")
    public Result selectAll(InsProvinceAreaModel insProvinceArea) {
        return this.insProvinceAreaService.queryBySelect(insProvinceArea);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "区域城市信息-获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result selectOne(@PathVariable Serializable id) {
        return Result.OK(this.insProvinceAreaService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param insProvinceArea 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "区域城市信息-新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增数据")
    @PostMapping("/insert")
    public Result insert(@RequestBody InsProvinceAreaModel insProvinceArea) {
        return Result.OK(this.insProvinceAreaService.insert(insProvinceArea));
    }

    /**
     * 修改数据
     *
     * @param insProvinceArea 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "区域城市信息-修改数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "修改数据")
    @PatchMapping("/update")
    public Result update(@RequestBody InsProvinceAreaModel insProvinceArea) {
        return Result.OK(this.insProvinceAreaService.update(insProvinceArea));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @AutoLog(value = "区域城市信息-删除数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除数据")
    @PostMapping("/delete")
    public Result delete(@RequestBody List<Serializable> idList) {
        return Result.OK(this.insProvinceAreaService.deleteByIds(idList));
    }
    
    /**
     * 获取省份列表
     * 
     * @return 省份列表
     */
    @AutoLog(value = "区域城市信息-获取省份列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取省份列表")
    @GetMapping("/provinceList")
    public Result getProvinceList() {
        return Result.OK(this.insProvinceAreaService.getProvinceList());
    }
}

