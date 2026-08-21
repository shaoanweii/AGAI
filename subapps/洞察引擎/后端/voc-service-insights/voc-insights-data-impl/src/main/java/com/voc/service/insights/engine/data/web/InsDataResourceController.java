package com.voc.service.insights.engine.data.web;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsRegulationInfoService;
import com.voc.service.insights.engine.api.data.InsDataResourceService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import com.voc.service.insights.engine.model.data.InsDataResourceModel;
import com.voc.service.insights.engine.vo.RegulationInfoVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 资源库(InsDataResource)表控制层
 *
 * @author leiww
 * @since 2024-04-02 16:37:37
 */
@RestController
@Tag(name = "资源库", description = "InsDataResource")
@RequestMapping("/insDataResource")
public class InsDataResourceController extends AbstractConditionFilters {
    /**
     * 服务对象
     */
    @Resource
    private InsDataResourceService insDataResourceService;
    @Autowired
    private IInsRegulationInfoService regulationInfoService;

    /**
     * 分页查询所有数据
     *
     * @param model 查询实体
     * @return 所有数据
     */
    @AutoLog(value = "资源库-分页查询")
    @Operation(summary = "分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/list")
    public Result selectAll(@RequestBody InsDataResourceModel model) {
        Assert.hasLength(model.getCustomer(),"所属客户不允许为空");
        return this.insDataResourceService.queryBySelect(model);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "资源库-获取详情")
    @Operation(summary = "获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/selectOne")
    public Result selectOne(@RequestBody InsDataResourceModel model) {
        return Result.OK(this.insDataResourceService.queryById(model));
    }

    /**
     * 新增数据
     *
     * @param model 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "资源库-新增数据")
    @Operation(summary = "新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/insert")
    public Result insert(@RequestBody InsDataResourceModel model) {
        String userId = ServiceContextHolder.getUserId();
        model.setCreateBy(userId);
        model.setUpdateBy(userId);
        return Result.OK(this.insDataResourceService.insert(model));
    }

    @AutoLog(value = "资源库-获取资源组分类列表")
    @Operation(summary = "获取资源组分类列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/findDataResourceList")
    public Result<IPage<InsDataResourceModel>> queryByParam(@RequestBody InsDataResourceModel model) {
        String userId = ServiceContextHolder.getUserId();
//        model.setCreateBy(userId);
        return Result.OK(this.insDataResourceService.queryByParam(model));
    }

    @AutoLog(value = "资源库-获取资源组分类列表")
    @Operation(summary = "获取资源组分类列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/findAllDataResourceList")
    public Result<List<InsDataResourceModel>> findAllDataResourceList(@RequestBody InsDataResourceModel model) {
        return Result.OK(this.insDataResourceService.findAllDataResourceList(model));
    }

    /**
     * 修改数据
     *
     * @param model 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "资源库-修改数据")
    @Operation(summary = "修改数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/update")
    public Result update(@RequestBody InsDataResourceModel model) {
        String userId = ServiceContextHolder.getUserId();
        model.setUpdateBy(userId);
        return Result.OK(this.insDataResourceService.update(model));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @AutoLog(value = "资源库-删除数据")
    @Operation(summary = "删除数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/delete")
    public Result delete(@RequestBody InsDataResourceModel model) {
//        Assert.hasLength(model.getCustomer(),"所属客户不允许为空");
//        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIdList()), "idList不能为空");
        return Result.OK(this.insDataResourceService.deleteByIds(model));
    }

    @AutoLog(value = "资源库-根据所属客户获取资源组")
    @Operation(summary = "根据所属客户获取资源组")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/findResourceGroupByAppClient")
    public Result<?> findResourceGroupByAppClient(@RequestBody InsDataResourceModel model) {
        return Result.OK(insDataResourceService.findResourceGroupByAppClient(model));
    }

    @AutoLog(value = "资源库-根据类型获取资源组树")
    @Operation(summary = "根据类型获取资源组树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/findAllResourceTree")
    public Result<List<InsDataResourceModel>> findAllResourceTree(@RequestBody InsDataResourceModel model) {
        return Result.OK(insDataResourceService.findAllResourceTree(model));
    }

    @AutoLog(value = "资源库-调用详情")
    @Operation(summary = "调用详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/findResource")
    public Result<PageInfo<RegulationInfoVo>> findResource(@RequestBody InsDataResourceModel model) {
        InsRegulationInfoModel regulationInfoModel = InsRegulationInfoModel.builder()
                .resourceGroupId(model.getId())
                .clientId(model.getCustomer())
                .build();
        regulationInfoModel.setPageNum(model.getPageNum());
        regulationInfoModel.setPageSize(model.getPageSize());
        return Result.OK(regulationInfoService.findResourceGroupRegulationList(regulationInfoModel));
    }

    @AutoLog(value = "资源库-调用详情统计")
    @Operation(summary = "调用详情统计")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/findResourceCount")
    public Result<List<RegulationInfoVo>> findResourceCount(@RequestBody InsDataResourceModel model) {
        InsRegulationInfoModel regulationInfoModel = InsRegulationInfoModel.builder()
                .resourceGroupId(model.getId())
                .clientId(model.getCustomer())
                .build();
        return Result.OK(regulationInfoService.findResourceGroupRegulationStatusCount(regulationInfoModel));
    }



    @Override
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, CLOSED_RULE_TYPE,STOP_OR_ENABLE)));
    }
}

