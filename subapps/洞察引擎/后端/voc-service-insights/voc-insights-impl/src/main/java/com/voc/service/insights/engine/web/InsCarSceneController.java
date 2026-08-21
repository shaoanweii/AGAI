package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsCarSceneService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsCarSceneModel;
import com.voc.service.insights.engine.vo.InsCarSceneOperatorVo;
import com.voc.service.insights.engine.vo.InsCarSceneVo;
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

@Tag(name = "用车场景")
@RestController
@RequestMapping("/carScene")
public class InsCarSceneController extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsCarSceneController.class);

    @Autowired
    private IInsCarSceneService carSceneService;

    @AutoLog(value = "用车场景-新增用车场景")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增用车场景")
    @PostMapping("/saveCarScene")
    Result<?> saveCarScene(@RequestBody InsCarSceneModel model) {
        try {
            carSceneService.saveCarScene(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("用车场景-新增用车场景异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("用车场景-新增用车场景异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("用车场景-新增用车场景异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "用车场景-修改用车场景")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "修改用车场景")
    @PostMapping("/updateCarScene")
    Result<?> updateCarScene(@RequestBody InsCarSceneModel model) {
        try {
            carSceneService.updateCarScene(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("用车场景-修改用车场景异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("用车场景-修改用车场景异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("用车场景-修改用车场景异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "用车场景-分页查询用车场景")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询用车场景")
    @PostMapping("/findCarSceneList")
    Result<IPage<InsCarSceneVo>> findCarSceneList(@RequestBody InsCarSceneModel model) {
        try {
            IPage<InsCarSceneVo> page = carSceneService.findCarSceneList(model);
            return Result.OK(page);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("用车场景-分页查询用车场景异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("用车场景-分页查询用车场景异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("用车场景-分页查询用车场景异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "用车场景-查询操作人列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询操作人列表")
    @PostMapping("/findCarSceneOperatorList")
    Result<List<InsCarSceneOperatorVo>> findCarSceneOperatorList(@RequestParam(defaultValue = "true") Boolean isAllVisible) {
        try {
            List<InsCarSceneOperatorVo> list = carSceneService.findCarSceneOperatorList(isAllVisible);
            return Result.OK(list);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("用车场景-查询操作人列表异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("用车场景-查询操作人列表异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("用车场景-查询操作人列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "用车场景-批量修改状态")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量修改状态")
    @PostMapping("/batchChangeStatus")
    Result<?> batchChangeStatus(@RequestBody InsCarSceneModel model) {
        try {
            carSceneService.batchChangeStatus(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("用车场景-批量修改状态异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("用车场景-批量修改状态异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("用车场景-批量修改状态异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "用车场景-批量移动")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量移动")
    @PostMapping("/batchMoveCarScene")
    Result<?> batchMoveCarScene(@RequestBody InsCarSceneModel model) {
        try {
            carSceneService.batchMoveCarScene(model);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("用车场景-批量移动异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("用车场景-批量移动异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("用车场景-批量移动异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @PostMapping("/uploadExcel")
    public Result<?> uploadExcel(@RequestParam(value = "file") MultipartFile file) {
        carSceneService.uploadExcel(file);
        return Result.OK();
    }

    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, STOP_OR_ENABLE,STATUS,AUTOMARK)));
    }
}
