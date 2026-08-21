package com.voc.service.insights.engine.data.web;


import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.data.IInsDataExpectService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.data.InsDataExpectModel;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 语料库数据集(InsDataExpect)表控制层
 *
 * @author leiww
 * @since 2024-03-05 14:44:44
 */
@RestController
@Tag(name = "语料库数据集", description = "语料库数据集")
@RequestMapping("/insDataExpect")
public class InsDataExpectController extends AbstractConditionFilters {
    /**
     * 服务对象
     */
    @Resource
    private IInsDataExpectService insDataExpectService;

    /**
     * 分页查询所有数据
     *
     * @param insDataExpect 查询实体
     * @return 所有数据
     */
    @AutoLog(value = "语料库数据集-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询")
    @PostMapping("/list")
    public Result selectAll(@Valid @RequestBody InsDataExpectModel insDataExpect) {
        return this.insDataExpectService.queryBySelect(insDataExpect);
    }

    /**
     * 统计所有数据总数
     *
     * @param insDataExpect 查询实体
     * @return 数据总数
     */
    @AutoLog(value = "语料库数据集-统计所有数据总数")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "统计所有数据总数")
    @PostMapping("/countAll")
    public Result findCountAll(@RequestBody InsDataExpectModel insDataExpect) {
        Integer count = insDataExpectService.countBySelect(insDataExpect);
        return Result.OK(count);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "语料库数据集-获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result selectOne(@PathVariable Serializable id) {
        return Result.OK(this.insDataExpectService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param insDataExpect 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "语料库数据集-新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增数据")
    @PostMapping("/insert")
    public Result insert(@Valid @RequestBody InsDataExpectModel insDataExpect) {
        String userId = ServiceContextHolder.getUserId();
        insDataExpect.setCreateBy(userId);
        insDataExpect.setUpdateBy(userId);
        return Result.OK(this.insDataExpectService.insert(insDataExpect));
    }

    /**
     * 修改数据
     *
     * @param insDataExpect 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "语料库数据集-修改数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "修改数据")
    @PatchMapping("/update")
    public Result update(@Valid @RequestBody InsDataExpectModel insDataExpect) {
        String userId = ServiceContextHolder.getUserId();
        insDataExpect.setUpdateBy(userId);
        return Result.OK(this.insDataExpectService.update(insDataExpect));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @AutoLog(value = "语料库数据集-删除数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除数据")
    @PostMapping("/delete")
    public Result delete(@RequestBody List<Serializable> idList) {
        return Result.OK(this.insDataExpectService.deleteByIds(idList));
    }

    @Override
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, DATA_TYPE)));
    }
}

