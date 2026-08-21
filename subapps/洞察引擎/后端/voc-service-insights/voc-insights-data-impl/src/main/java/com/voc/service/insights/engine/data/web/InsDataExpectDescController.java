package com.voc.service.insights.engine.data.web;


import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.logs.annotation.AutoLog;
import com.voc.service.insights.engine.model.data.InsDataExpectDescModel;
import com.voc.service.insights.engine.api.data.IInsDataExpectDescService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 语料库数据详情(InsDataExpectDesc)表控制层
 *
 * @author leiww
 * @since 2024-03-05 14:51:15
 */
@RestController
@Tag(name = "语料库数据详情", description = "语料库数据详情")
@RequestMapping("/insDataExpectDesc")
public class InsDataExpectDescController {
    /**
     * 服务对象
     */
    @Resource
    private IInsDataExpectDescService insDataExpectDescService;

    /**
     * 分页查询所有数据
     *
     * @param insDataExpectDesc 查询实体
     * @return 所有数据
     */
    @AutoLog(value = "语料库数据详情-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询")
    @PostMapping("/list")
    public Result selectAll(@RequestBody InsDataExpectDescModel insDataExpectDesc) {
        return this.insDataExpectDescService.queryBySelect(insDataExpectDesc);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "语料库数据详情-获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result selectOne(@PathVariable Serializable id) {
        return Result.OK(this.insDataExpectDescService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param insDataExpectDesc 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "语料库数据详情-新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增数据")
    @PostMapping("/insert")
    public Result insert(@RequestBody InsDataExpectDescModel insDataExpectDesc) {
        String userId = ServiceContextHolder.getUserId();
        insDataExpectDesc.setCreateBy(userId);
        insDataExpectDesc.setUpdateBy(userId);
        return Result.OK(this.insDataExpectDescService.insert(insDataExpectDesc));
    }

    /**
     * 修改数据
     *
     * @param insDataExpectDesc 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "语料库数据详情-修改数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "修改数据")
    @PatchMapping("/update")
    public Result update(@RequestBody InsDataExpectDescModel insDataExpectDesc) {
        String userId = ServiceContextHolder.getUserId();
        insDataExpectDesc.setUpdateBy(userId);
        return Result.OK(this.insDataExpectDescService.update(insDataExpectDesc));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @AutoLog(value = "语料库数据详情-删除数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除数据")
    @PostMapping("/delete")
    public Result delete(@RequestBody List<Serializable> idList) {
        return Result.OK(this.insDataExpectDescService.deleteByIds(idList));
    }
}

