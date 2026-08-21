package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsCarSeriesInfoService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsCarSeriesInfoModel;
import com.voc.service.insights.engine.vo.CarInfoVo;
import com.voc.service.insights.engine.vo.InsBrandInfoVo;
import com.voc.service.insights.engine.vo.InsCarSeriesVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @version 1.0.0
 * @author: zhongxl
 * @ClassName InsCarSeriesInfoController.java
 * @Description
 * @createTime 2024年01月29日 10:36
 * @Copyright futong
 */
@Tag(name = "汽车车系信息服务")
@RestController
@RequestMapping("/carSeriesInfo")
public class InsCarSeriesInfoController  extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsCarSeriesInfoController.class);

    @Autowired
    IInsCarSeriesInfoService iInsCarSeriesInfoService;


    @AutoLog(value = "汽车品牌-分页查询汽车车系信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询汽车车系信息")
    @PostMapping(value = "/queryBySelect")
    Result<?> queryBySelect(@RequestBody InsCarSeriesInfoModel model) {
        return iInsCarSeriesInfoService.queryBySelect(model);
    }

    @AutoLog(value = "汽车车系-添加汽车车系信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "添加汽车车系信息")
    @PostMapping(value = "/addCarSeriesInfo")
    Result<?> addCarSeriesInfo(@RequestBody InsCarSeriesInfoModel insCarSeriesInfoModel) {
        try {
            iInsCarSeriesInfoService.addInsCarSeriesInfo(insCarSeriesInfoModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("汽车车系-添加汽车车系信息异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("汽车车系-添加汽车车系信息异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("汽车车系-添加汽车车系信息异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "汽车车系-修改汽车车系信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "修改汽车车系信息")
    @PostMapping(value = "/updateCarSeriesInfo")
    Result<?> updateCarSeriesInfo(@RequestBody InsCarSeriesInfoModel insCarSeriesInfoModel) {
        try {
            iInsCarSeriesInfoService.updateInsCarSeriesInfo(insCarSeriesInfoModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("汽车车系-修改汽车车系信息异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("汽车车系-修改汽车车系信息异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("汽车车系-修改汽车车系信息异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "汽车车系-根据id汽车车系信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据id汽车车系信息")
    @PostMapping(value = "/findCarSeriesInfo")
    Result<?> findCarSeriesInfo(@RequestBody InsCarSeriesInfoModel insCarSeriesInfoModel) {
        try {
            InsCarSeriesVo carSeriesInfo = iInsCarSeriesInfoService.findCarSeriesInfo(insCarSeriesInfoModel);
            return Result.OK(carSeriesInfo);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("汽车车系-根据id汽车车系信息异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("汽车车系-根据id汽车车系信息异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("汽车车系-根据id汽车车系信息异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "汽车车系-批量更新车系状态")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量更新车系状态")
    @PostMapping(value = "/batchChangeStatus")
    Result<?> batchChangeStatus(@RequestBody InsCarSeriesInfoModel insCarSeriesInfoModel) {
        try {
            iInsCarSeriesInfoService.batchChangeStatus(insCarSeriesInfoModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("汽车车系-批量更新车系状态异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("汽车车系-批量更新车系状态异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("汽车车系-批量更新车系状态异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "汽车车系-删除汽车车系信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除汽车车系信息")
    @PostMapping(value = "/deleteCarSeriesInfo")
    Result<?> deleteCarSeriesInfo(@RequestBody InsCarSeriesInfoModel model) {
        iInsCarSeriesInfoService.delInsCarSeriesInfo(model);
        return Result.OK("OK");
    }

    @AutoLog(value = "汽车车系-查询汽车车系信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询汽车车系信息")
    @PostMapping(value = "/findByParam")
    public Result<List<InsCarSeriesInfoModel>> findByParam(@RequestBody InsCarSeriesInfoModel insCarSeriesInfoModel) {
        return Result.OK(iInsCarSeriesInfoService.queryByParam(insCarSeriesInfoModel));
    }

    @PostMapping("/uploadExcel")
    public Result<?> uploadExcel(@RequestParam(value = "file") MultipartFile file) {
        iInsCarSeriesInfoService.uploadExcel(file);
        return Result.OK();
    }

//    @AutoLog(value = "汽车车系-根据id获取车系")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据id获取车系")
    @PostMapping(value = "/findCarSeriesByIds")
    public Result<List<CarInfoVo>> findCarSeriesByIds(@RequestBody InsCarSeriesInfoModel insCarSeriesInfoModel) {
        return Result.OK(iInsCarSeriesInfoService.findCarSeriesByIds(insCarSeriesInfoModel));
    }
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取所有车系")
    @PostMapping(value = "/findAll")
    public Result<List<InsCarSeriesInfoModel> > findAll(@RequestBody InsCarSeriesInfoModel insCarSeriesInfoModel) {
        return Result.OK(iInsCarSeriesInfoService.findAll());
    }


    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, IS_CORE, COMPETITIVE_TYPE, STOP_OR_ENABLE,STATUS,BRAND,IS_NEW_CAR)));
    }
}
