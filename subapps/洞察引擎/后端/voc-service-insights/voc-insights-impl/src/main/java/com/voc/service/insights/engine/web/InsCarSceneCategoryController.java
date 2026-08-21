package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsCarSceneCategoryService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsCarSceneCategoryModel;
import com.voc.service.insights.engine.vo.InsCarSceneCategoryVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用车场景分类")
@RestController
@RequestMapping("/carSceneCategory")
public class InsCarSceneCategoryController extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsCarSceneCategoryController.class);

    @Autowired
    private IInsCarSceneCategoryService carSceneCategoryService;

    @AutoLog(value = "用车场景分类-新增用车场景分类")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增用车场景分类")
    @PostMapping("/saveCarSceneCategory")
    Result<?> saveCarSceneCategory(@RequestBody InsCarSceneCategoryModel model) {
        try {
            carSceneCategoryService.saveCarSceneCategory(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("用车场景分类-新增用车场景分类异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("用车场景分类-新增用车场景分类异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("用车场景分类-新增用车场景分类异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "用车场景分类-修改用车场景分类")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "修改用车场景分类")
    @PostMapping("/updateCarSceneCategory")
    Result<?> updateCarSceneCategory(@RequestBody InsCarSceneCategoryModel model) {
        try {
            carSceneCategoryService.updateCarSceneCategory(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("用车场景分类-修改用车场景分类异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("用车场景分类-修改用车场景分类异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("用车场景分类-修改用车场景分类异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "用车场景分类-查询用车场景分类列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询用车场景分类列表")
    @PostMapping("/findCarSceneCategoryList")
    Result<List<InsCarSceneCategoryVo>> findCarSceneCategoryList(@RequestBody InsCarSceneCategoryModel model) {
        try {
            List<InsCarSceneCategoryVo> list = carSceneCategoryService.findCarSceneCategoryList(model);
            return Result.OK(list);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("用车场景分类-查询用车场景分类列表异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("用车场景分类-查询用车场景分类列表异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("用车场景分类-查询用车场景分类列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "用车场景分类-查询用车场景分类树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询用车场景分类树")
    @PostMapping("/findCarSceneCategoryTree")
    Result<List<InsCarSceneCategoryVo>> findCarSceneCategoryTree(@RequestBody InsCarSceneCategoryModel model) {
        try {
            List<InsCarSceneCategoryVo> list = carSceneCategoryService.findCarSceneCategoryTree(model);
            return Result.OK(list);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("用车场景分类-查询用车场景分类树异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("用车场景分类-查询用车场景分类树异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("用车场景分类-查询用车场景分类树异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "用车场景分类-删除用车场景分类")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除用车场景分类")
    @PostMapping("/deleteCarSceneCategory")
    Result<?> deleteCarSceneCategory(@RequestBody InsCarSceneCategoryModel model) {
        try {
            carSceneCategoryService.deleteCarSceneCategory(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("用车场景分类-删除用车场景分类异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("用车场景分类-删除用车场景分类异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("用车场景分类-删除用车场景分类异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, STOP_OR_ENABLE,STATUS,AUTOMARK)));
    }
}
