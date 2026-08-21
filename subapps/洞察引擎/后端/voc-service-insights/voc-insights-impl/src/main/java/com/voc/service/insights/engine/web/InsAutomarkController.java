package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsAutomarkService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsAutomarkModel;
import com.voc.service.insights.engine.model.InsBrandInfoModel;
import com.voc.service.insights.engine.vo.AutomarkVo;
import com.voc.service.insights.engine.vo.InsAutomarkInfoVo;
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
 * @创建时间: 2026/2/11 16:36
 * @描述:
 **/
@Tag(name = "车企")
@RestController
@RequestMapping("/automark")
public class InsAutomarkController extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsAutomarkController.class);
    @Autowired
    private IInsAutomarkService automarkService;

    @AutoLog(value = "车企-保存车企信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "保存车企信息")
    @PostMapping(value = "/saveAutomark")
    Result<?> saveAutomark(@RequestBody InsAutomarkModel model) {
        try {
            automarkService.saveAutomark(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("车企-保存车企信息异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("车企-保存车企信息异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("车企-保存车企信息异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "车企-修改车企信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "修改车企信息")
    @PostMapping(value = "/updateAutomark")
    Result<?> updateAutomark(@RequestBody InsAutomarkModel model) {
        try {
            automarkService.updateAutomark(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("车企-修改车企信息异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("车企-修改车企信息异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("车企-修改车企信息异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "车企-批量修改状态")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量修改状态")
    @PostMapping(value = "/batchChangeStatus")
    Result<?> batchChangeStatus(@RequestBody InsAutomarkModel model) {
        try {
            automarkService.batchChangeStatus(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("车企-批量修改状态异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("车企-批量修改状态异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("车企-批量修改状态异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "车企-根据id查询车企信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据id查询车企信息")
    @PostMapping(value = "/findAutomarkInfo")
    Result<InsAutomarkInfoVo> findAutomarkInfo(@RequestBody InsAutomarkModel model) {
        try {
            InsAutomarkInfoVo automarkInfo = automarkService.findAutomarkInfo(model);
            return Result.OK(automarkInfo);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("车企-根据id查询车企信息异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("车企-根据id查询车企信息异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("车企-根据id查询车企信息异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "车企-分页查询车企列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询车企列表")
    @PostMapping(value = "/findAutomarkList")
    Result<IPage<InsAutomarkInfoVo>> findAutomarkList(@RequestBody InsAutomarkModel model) {
        try {
            IPage<InsAutomarkInfoVo> automarkList = automarkService.findAutomarkList(model);
            return Result.OK(automarkList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("车企-分页查询车企列表异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("车企-分页查询车企列表异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("车企-分页查询车企列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "车企-查询车企列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询车企列表")
    @PostMapping(value = "/findAutomarkInfoList")
    Result<List<AutomarkVo>> findAutomarkInfoList(@RequestBody InsAutomarkModel model) {
        try {
            List<AutomarkVo> automarkInfoList = automarkService.findAutomarkInfoList(model);
            return Result.OK(automarkInfoList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("车企-查询车企列表异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("车企-查询车企列表异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("车企-查询车企列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @PostMapping("/uploadExcel")
    public Result<?> uploadExcel(@RequestParam(value = "file") MultipartFile file) {
        automarkService.uploadExcel(file);
        return Result.OK();
    }

    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, IS_CORE, COMPETITIVE_TYPE, STOP_OR_ENABLE,STATUS)));
    }
}
