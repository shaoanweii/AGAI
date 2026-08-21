package com.voc.service.insights.engine.web;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsBatchRuleCategoryService;
import com.voc.service.insights.engine.model.InsBatchRuleCategoryModel;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 批量规则分类控制器
 * 处理分类的增删改查请求
 */
@Slf4j
@RestController
@Tag(name = "批量规则分类管理", description = "批量规则分类管理接口")
@RequestMapping("/insBatchRuleCategory")
public class InsBatchRuleCategoryController {

    @Resource
    private IInsBatchRuleCategoryService insBatchRuleCategoryService;

    @AutoLog(value = "批量规则分类-查询分类树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询分类树")
    @GetMapping("/tree")
    public Result<List<InsBatchRuleCategoryModel>> queryCategoryTree(@RequestParam(required = false) String searchKey) {
        return Result.OK(insBatchRuleCategoryService.queryCategoryTree(searchKey));
    }

    @AutoLog(value = "批量规则分类-新增分类")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增分类")
    @PostMapping("/insert")
    public Result<Boolean> insertCategory(@Valid @RequestBody InsBatchRuleCategoryModel model) {
        return Result.OK(insBatchRuleCategoryService.insertCategory(model));
    }

    @AutoLog(value = "批量规则分类-编辑分类")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "编辑分类")
    @PutMapping("/update")
    public Result<Boolean> updateCategory(@Valid @RequestBody InsBatchRuleCategoryModel model) {
        return Result.OK(insBatchRuleCategoryService.updateCategory(model));
    }

    @AutoLog(value = "批量规则分类-删除分类")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除分类")
    @DeleteMapping("/{categoryId}")
    public Result<Boolean> deleteCategory(@PathVariable String categoryId) {
        return Result.OK(insBatchRuleCategoryService.deleteCategory(categoryId));
    }


}
