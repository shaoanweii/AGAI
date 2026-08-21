package com.voc.service.insights.engine.model.web;



import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.logs.annotation.AutoLog;
import com.voc.service.insights.engine.model.model.InsModelDescModel;
import com.voc.service.insights.engine.api.model.IInsModelDescService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * (InsModelDesc)表控制层
 *
 * @author leiww
 * @since 2024-02-21 15:32:06
 */
@RestController
@Tag(name = "模型配置数据详情", description = "模型配置数据详情")
@RequestMapping("/insModelDesc")
public class InsModelDescController {
    /**
     * 服务对象
     */
    @Resource
    private IInsModelDescService insModelDescService;

    /**
     * 分页查询所有数据
     *
     * @param insModelDesc 查询实体
     * @return 所有数据
     */
    @AutoLog(value = "模型配置-分页查询")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "模型配置分页查询")
    @PostMapping("/list")
    public Result selectAll(@RequestBody InsModelDescModel insModelDesc) {
        return this.insModelDescService.queryBySelect(insModelDesc);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "模型配置-获取详情")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "模型配置获取详情")
    @GetMapping("/{id}")
    public Result selectOne(@PathVariable Serializable id) {
        return Result.OK(this.insModelDescService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param model 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "模型配置-新增数据")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "模型配置新增数据")
    @PostMapping("/insert")
    public Result insert(@RequestBody InsModelDescModel model) {
        String userId = ServiceContextHolder.getUserId();
        model.setCreateBy(userId);
        model.setUpdateBy(userId);
        return Result.OK(this.insModelDescService.insert(model));
    }

    /**
     * 修改数据
     *
     * @param model 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "模型配置-修改数据")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "模型配置修改数据")
    @PatchMapping("/update")
    public Result update(@RequestBody InsModelDescModel model) {
        String userId = ServiceContextHolder.getUserId();
        model.setUpdateBy(userId);
        return Result.OK(this.insModelDescService.update(model));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @AutoLog(value = "模型配置-删除数据")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "模型配置删除数据")
    @PostMapping("/delete")
    public Result delete(@RequestBody List<Serializable> idList) {
        return Result.OK(this.insModelDescService.deleteByIds(idList));
    }
}

