package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsAttributeLabelService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsAttributeLabelModel;
import com.voc.service.insights.engine.vo.InsAttributeLabelVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2026/4/9 14:10
 * @描述:
 **/
@Tag(name = "属性标签")
@RestController
@RequestMapping("/attributeLabel")
public class InsAttributeLabelController extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsAttributeLabelController.class);

    @Autowired
    private IInsAttributeLabelService attributeLabelService;

    @AutoLog(value = "属性标签-新增")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增属性标签")
    @PostMapping("/saveAttributeLabel")
    Result<?> saveAttributeLabel(@RequestBody InsAttributeLabelModel model) {
        try {
            attributeLabelService.saveAttributeLabel(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("属性标签-新增异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("属性标签-新增异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("属性标签-新增异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "属性标签-编辑")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "编辑属性标签")
    @PostMapping("/updateAttributeLabel")
    Result<?> updateAttributeLabel(@RequestBody InsAttributeLabelModel model) {
        try {
            attributeLabelService.updateAttributeLabel(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("属性标签-编辑异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("属性标签-编辑异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("属性标签-编辑异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "属性标签-批量修改状态")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量修改属性标签状态")
    @PostMapping("/batchChangeStatus")
    Result<?> batchChangeStatus(@RequestBody InsAttributeLabelModel model) {
        try {
            attributeLabelService.batchChangeStatus(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("属性标签-批量修改状态异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("属性标签-批量修改状态异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("属性标签-批量修改状态异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "属性标签-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页获取属性标签列表")
    @PostMapping("/findAttributeLabelList")
    Result<IPage<InsAttributeLabelVo>> findAttributeLabelList(@RequestBody InsAttributeLabelModel model) {
        try {
            IPage<InsAttributeLabelVo> attributeLabelList = attributeLabelService.findAttributeLabelList(model);
            return Result.OK(attributeLabelList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("属性标签-分页查询异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("属性标签-分页查询异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("属性标签-分页查询异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "属性标签-查询列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取标签属性列表")
    @PostMapping("/findAllAttributeLabelList")
    Result<List<InsAttributeLabelVo>> findAllAttributeLabelList(@RequestBody InsAttributeLabelModel model) {
        try {
            List<InsAttributeLabelVo> attributeLabelList = attributeLabelService.findAllAttributeLabelList(model);
            return Result.OK(attributeLabelList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("属性标签-查询列表异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("属性标签-查询列表异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("属性标签-查询列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @PostMapping("/uploadExcel")
    public Result<?> uploadExcel(@RequestParam(value = "file") MultipartFile file) {
        attributeLabelService.uploadExcel(file);
        return Result.OK();
    }


    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, STOP_OR_ENABLE,STATUS)));
    }
}
