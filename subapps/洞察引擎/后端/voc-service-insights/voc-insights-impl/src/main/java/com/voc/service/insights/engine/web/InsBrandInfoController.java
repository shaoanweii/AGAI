package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsBrandInfoService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsBrandInfoModel;
import com.voc.service.insights.engine.vo.InsALlBrandAndCarSeriesVo;
import com.voc.service.insights.engine.vo.InsBrandInfoVo;
import com.voc.service.insights.engine.vo.InsSysDepartVo;
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

import java.util.List;

/**
 * @version 1.0.0
 * @author: zhongxl
 * @ClassName InsBrandInfoController.java
 * @Description
 * @createTime 2024年01月29日 10:36
 * @Copyright futong
 */
@Tag(name = "汽车品牌信息服务")
@RestController
@RequestMapping("/brandInfo")
public class InsBrandInfoController extends AbstractConditionFilters {

    private static final Logger log = LoggerFactory.getLogger(InsBrandInfoController.class);
    @Autowired
    IInsBrandInfoService iInsBrandInfoService;

    @AutoLog(value = "汽车品牌-分页查询汽车品牌信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询汽车品牌信息")
    @PostMapping(value = "/queryBySelect")
    Result<?> queryBySelect(@Valid @RequestBody InsBrandInfoModel insBrandInfoModel) {
        return iInsBrandInfoService.queryBySelect(insBrandInfoModel);
    }

    @AutoLog(value = "汽车品牌-添加汽车品牌信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "添加汽车品牌信息")
    @PostMapping(value = "/addBrandInfo")
    Result<?> addBrandInfo(@RequestBody InsBrandInfoModel insBrandInfoModel) {
        try {
            iInsBrandInfoService.addInsBrandInfo(insBrandInfoModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("汽车品牌-添加汽车品牌信息异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("汽车品牌-添加汽车品牌信息异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("汽车品牌-添加汽车品牌信息异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "汽车品牌-修改汽车品牌信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "修改汽车品牌信息")
    @PostMapping(value = "/updateBrandInfo")
    Result<?> updateBrandInfo(@Valid @RequestBody InsBrandInfoModel insBrandInfoModel) {
        try {
            iInsBrandInfoService.updateInsBrandInfo(insBrandInfoModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("汽车品牌-添加汽车品牌信息异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("汽车品牌-添加汽车品牌信息异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("汽车品牌-添加汽车品牌信息异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "汽车品牌-根据id获取汽车品牌信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据id获取汽车品牌信息")
    @PostMapping(value = "/findInsBrandInfo")
    Result<?> findInsBrandInfo(@Valid @RequestBody InsBrandInfoModel insBrandInfoModel) {
        try {
            InsBrandInfoVo insBrandInfo = iInsBrandInfoService.findInsBrandInfo(insBrandInfoModel);
            return Result.OK(insBrandInfo);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("汽车品牌-根据id获取汽车品牌信息异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("汽车品牌-根据id获取汽车品牌信息异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("汽车品牌-根据id获取汽车品牌信息异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "汽车品牌-批量修改状态")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量修改状态")
    @PostMapping(value = "/batchChangeStatus")
    Result<?> batchChangeStatus(@Valid @RequestBody InsBrandInfoModel insBrandInfoModel) {
        try {
            iInsBrandInfoService.batchChangeStatus(insBrandInfoModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("汽车品牌-批量修改状态异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("汽车品牌-批量修改状态异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("汽车品牌-批量修改状态异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "汽车品牌-删除汽车品牌信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除汽车品牌信息")
    @PostMapping(value = "/deleteBrandInfo")
    Result<?> deleteBrandInfo(@RequestBody InsBrandInfoModel model) {
        iInsBrandInfoService.delInsBrandInfo(model);
        return Result.OK("OK");
    }

    @AutoLog(value = "汽车品牌-查询汽车品牌信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询汽车品牌信息")
    @PostMapping(value = "/findByParam")
    public Result<List<InsBrandInfoVo>> findByParam(@RequestBody InsBrandInfoModel insBrandInfoModel) {
        try {
            List<InsBrandInfoVo> insBrandInfoVos = iInsBrandInfoService.queryByParam(insBrandInfoModel);
            return Result.OK(insBrandInfoVos);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("汽车品牌-查询汽车品牌信息异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("汽车品牌-查询汽车品牌信息异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("汽车品牌-查询汽车品牌信息异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

//    @AutoLog(value = "汽车品牌-查询全部汽车品牌及车系")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询全部汽车品牌及车系")
    @GetMapping(value = "/findAllBrandAndCarSeries")
    public Result<List<InsALlBrandAndCarSeriesVo> > findAllBrandAndCarSeries() {
        try {
            log.debug("查询全部汽车品牌及车系");
            List<InsALlBrandAndCarSeriesVo> allBrandAndCarSeries = iInsBrandInfoService.findAllBrandAndCarSeries();
            log.debug("查询全部汽车品牌及车系");
            return Result.OK(allBrandAndCarSeries);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.errors("查询全部汽车品牌及车系异常");
        }
    }


//    @AutoLog(value = "汽车品牌-查询全部汽车品牌")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询全部汽车品牌")
    @GetMapping(value = "/findAll")
    public Result<List<InsBrandInfoModel> > findAll() {
        try {
            log.debug("begin");
            List<InsBrandInfoModel> allBrandAndCarSeries = iInsBrandInfoService.findAll();
            log.debug("end");
            return Result.OK(allBrandAndCarSeries);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.errors("查询全部汽车品牌异常");
        }
    }
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询本品品牌code")
    @GetMapping(value = "/getSelfBrandCodes")
    public Result<String> getSelfBrandCodes() {
        try {
            log.debug("begin");
           String selfBrandCodes = iInsBrandInfoService.getSelfBrandCodes();
            log.debug("end");
            return Result.OK(selfBrandCodes);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.errors("查询本品品牌code异常");
        }
    }

    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, IS_CORE, COMPETITIVE_TYPE, STOP_OR_ENABLE,STATUS,AUTOMARK)));
    }

}
