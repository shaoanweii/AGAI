package com.voc.service.insights.engine.web;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsDictItemService;
import com.voc.service.insights.engine.model.InsDictItemModel;
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
@Tag(name = "字典明细")
@RestController
@RequestMapping("/insDictItem")
public class InsDictItemController {
    @Resource
    private IInsDictItemService iInsDictItemService;

    /**
     * 查询洞察引擎使用的全部数据字典项。
     *
     * @return 按字典编码分组的有效字典项
     */
    @Operation(summary = "查询全部数据字典项")
    @PostMapping("/insAllDictItems")
    public Result<?> insAllDictItems() {
        return iInsDictItemService.insAllDictItems();
    }

    /**
     * 分页查询所有数据
     *
     * @return 所有数据
     */
    @AutoLog(value = "字典列表查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询列表")
    @GetMapping("/list")
    public Result<?> selectAll(InsDictItemModel sysDictModel) {
        return iInsDictItemService.queryBySelect(sysDictModel);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "查询字典表详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询单条字典详细信息")
    @GetMapping("/{id}")
    public Result selectOne(@PathVariable String id) {
        return Result.OK(iInsDictItemService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysDictItemModel 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "字典-新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增数据")
    @PostMapping("/insert")
    public Result insert(@RequestBody InsDictItemModel sysDictItemModel) {
        return Result.OK(iInsDictItemService.save(sysDictItemModel));
    }

    /**
     * 修改数据
     *
     * @param sysDictModel 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "字典-更新数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新数据")
    @PostMapping("/update")
    public Result update(@RequestBody InsDictItemModel sysDictModel) {
        return Result.OK(iInsDictItemService.updateByDictId(sysDictModel));
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
        iInsDictItemService.deleteList(idList);
        return Result.OK();
    }
}
