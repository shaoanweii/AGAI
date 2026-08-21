package com.voc.service.insights.engine.model.web;



import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.model.InsModelInfoModel;
import com.voc.service.logs.annotation.AutoLog;
import com.voc.service.insights.engine.api.model.IInsModelInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 模型配置数据(InsModelInfo)表控制层
 *
 * @author leiww
 * @since 2024-02-22 11:34:35
 */
@RestController
@Tag(name = "模型配置数据", description = "模型配置数据")
@RequestMapping("/insModelInfo")
public class InsModelInfoController extends AbstractConditionFilters {
    /**
     * 服务对象
     */
    @Resource
    private IInsModelInfoService insModelInfoService;

    /**
     * 分页查询所有数据
     *
     * @param insModelInfo 查询实体
     * @return 所有数据
     */
    @AutoLog(value = "模型配置数据-分页查询")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "分页查询")
    @PostMapping("/list")
    public Result selectAll(@RequestBody InsModelInfoModel insModelInfo) {
        return this.insModelInfoService.queryBySelect(insModelInfo);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "模型配置数据-获取详情")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result selectOne(@PathVariable Serializable id) {
        return Result.OK(this.insModelInfoService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param insModelInfo 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "模型配置数据-新增数据")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "新增数据")
    @PostMapping("/insert")
    public Result insert(@RequestBody InsModelInfoModel insModelInfo) {
        String userId = ServiceContextHolder.getUserId();
        insModelInfo.setCreateBy(userId);
        insModelInfo.setUpdateBy(userId);
        return Result.OK(this.insModelInfoService.insert(insModelInfo));
    }

    /**
     * 修改数据
     *
     * @param insModelInfo 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "模型配置数据-修改数据")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "修改数据")
    @PatchMapping("/update")
    public Result update(@RequestBody InsModelInfoModel insModelInfo) {
        String userId = ServiceContextHolder.getUserId();
        insModelInfo.setUpdateBy(userId);
        return Result.OK(this.insModelInfoService.update(insModelInfo));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @AutoLog(value = "模型配置数据-删除数据")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "删除数据")
    @PostMapping("/delete")
    public Result delete(@RequestBody List<Serializable> idList) {
        return Result.OK(this.insModelInfoService.deleteByIds(idList));
    }

    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions(){
        return Result.OK(async(CollUtil.set(false,DATA_TYPE,MODEL_TYPE)));
    }
}

