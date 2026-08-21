package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsDictService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsDictModel;
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
 * @author zhangbing
 * @date 2024-01-24 11:12:23
 */
@Tag(name = "字典")
@RestController
@RequestMapping("/insDict")
public class InsDictController extends AbstractConditionFilters {
    @Resource
    private IInsDictService iInsDictService;

    /**
     * 分页查询所有数据
     *
     * @return 所有数据
     */
    @AutoLog(value = "字典列表查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询列表")
    @GetMapping("/list")
    public Result<?> selectAll(InsDictModel insDictModel) {
        return iInsDictService.queryBySelect(insDictModel);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param code 主键
     * @return 单条数据
     */
    @AutoLog(value = "查询字典表详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询单条字典信息")
    @GetMapping("/{code}")
    public Result selectOne(@PathVariable String code) {
        return Result.OK(iInsDictService.queryDictItemsByCode(code));
    }

    /**
     * 新增数据
     *
     * @param insDictModel 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "字典-新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增数据")
    @PostMapping("/insert")
    public Result insert(@RequestBody InsDictModel insDictModel) {
        return Result.OK(iInsDictService.save(insDictModel));
    }

    /**
     * 修改数据
     *
     * @param insDictModel 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "字典-更新数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新数据")
    @PostMapping("/update")
    public Result update(@RequestBody InsDictModel insDictModel) {
        return Result.OK(iInsDictService.updateDict(insDictModel));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @AutoLog(value = "字典-新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除数据")
    @PostMapping("/delete")
    public Result delete(@RequestBody List<Serializable> idList) {
        iInsDictService.deleteList(idList);
        return Result.OK();
    }

    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.newHashSet(PROVINCE, VEHICLE_STAGE, ENERGY, STATUS)));
    }
}
