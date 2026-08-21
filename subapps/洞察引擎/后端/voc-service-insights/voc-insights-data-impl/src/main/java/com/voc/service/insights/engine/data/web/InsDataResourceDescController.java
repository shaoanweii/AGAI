package com.voc.service.insights.engine.data.web;


import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.data.InsDataResourceDescService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.data.InsDataResourceDescModel;
import com.voc.service.insights.engine.vo.data.ResourceDescDto;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * 资源详情(InsDataResourceDesc)表控制层
 *
 * @author leiww
 * @since 2024-04-02 17:00:18
 */
@RestController
@Tag(name = "资源详情", description = "InsDataResourceDesc")
@RequestMapping("/insDataResourceDesc")
public class InsDataResourceDescController extends AbstractConditionFilters {
    /**
     * 服务对象
     */
    @Resource
    private InsDataResourceDescService insDataResourceDescService;

    /**
     * 分页查询所有数据
     *
     * @param model 查询实体
     * @return 所有数据
     */
    @AutoLog(value = "资源详情-分页查询")
    @Operation(summary = "分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/list")
    public Result selectAll(@RequestBody InsDataResourceDescModel model) {
        return this.insDataResourceDescService.queryBySelect(model);
    }

//    @AutoLog(value = "资源详情-条件查询")
    @Operation(summary = "分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/findByConditon")
    public Result findByConditon(@RequestBody InsDataResourceDescModel model) {
        return Result.OK(this.insDataResourceDescService.queryByParam(model));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "资源详情-获取详情")
    @Operation(summary = "获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/selectOne")
    public Result selectOne(@RequestBody InsDataResourceDescModel model) {
        return Result.OK(this.insDataResourceDescService.queryById(model));
    }

    /**
     * 新增数据
     *
     * @param model 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "资源详情-新增数据")
    @Operation(summary = "新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/insert")
    public Result insert(@RequestBody InsDataResourceDescModel model) {
        String userId = ServiceContextHolder.getUserId();
        model.setCreateBy(userId);
        model.setUpdateBy(userId);
        return Result.OK(this.insDataResourceDescService.insert(model));
    }

    /**
     * 修改数据
     *
     * @param model 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "资源详情-修改数据")
    @Operation(summary = "修改数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/update")
    public Result update(@RequestBody InsDataResourceDescModel model) {
        String userId = ServiceContextHolder.getUserId();
        model.setUpdateBy(userId);
        return Result.OK(this.insDataResourceDescService.update(model));
    }

    @AutoLog(value = "资源详情-修改状态")
    @Operation(summary = "修改状态")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PutMapping("/updateStatus")
    public Result updateStatus(@RequestBody InsDataResourceDescModel model) {
        String userId = ServiceContextHolder.getUserId();
        model.setUpdateBy(userId);
        return Result.OK(this.insDataResourceDescService.updateStatus(model));
    }

    /**
     * 删除数据
     *
     * @param id         主键
     * @param resourceId 资源id
     * @return 删除结果
     */
    @AutoLog(value = "资源详情-删除数据")
    @Operation(summary = "删除数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/delete")
    public Result delete(@RequestBody InsDataResourceDescModel model) {
        return Result.OK(this.insDataResourceDescService.deleteByIdResourceId(model));
    }

    @AutoLog(value = "资源详情-获取详情集合")
    @Operation(summary = "获取详情集合")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/queryByResourceId")
    public Result queryByResourceId(@RequestBody InsDataResourceDescModel model) {
        return Result.OK(this.insDataResourceDescService.queryByResourceId(model));
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/dataResourceUpload")
    public void dataResourceUpload(@RequestParam("file") MultipartFile file, @RequestParam(value = "clientId",required = false) String clientId, @RequestParam("resourceId") String resourceId) {
        insDataResourceDescService.dataResourceUpload(file, clientId, resourceId);
    }

    @AutoLog(value = "资源详情-获取全部数据源详情")
    @Operation(summary = "获取全部数据源详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/findAllDataResourceDesc")
    public Result<List<ResourceDescDto>> findAllDataResourceDesc(@RequestBody InsDataResourceDescModel model) {
        return Result.OK(insDataResourceDescService.findAllDataResourceDesc(model));
    }

    @AutoLog(value = "资源详情-获取全部数据源详情")
    @Operation(summary = "获取全部数据源详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/changeResourceStatus")
    public Result<?> changeResourceStatus(@RequestBody InsDataResourceDescModel model) {
        insDataResourceDescService.changeResourceStatus(model);
        return Result.OK();
    }


    @Override
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, RULE_STATUS,STOP_OR_ENABLE)));
    }
}

