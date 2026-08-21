package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsRegionConfigService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsRegionConfigModel;
import com.voc.service.insights.engine.vo.RegionConfigVo;
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

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/11 上午9:03
 * @描述:
 **/
@Tag(name = "区域配置")
@RestController
@RequestMapping("/region")
public class InsRegionConfigController extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsRegionConfigController.class);
    @Autowired
    IInsRegionConfigService iInsRegionConfigService;

    /**
     * @return com.voc.service.common.response.Result<?>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 08:58
     * @描述 查询条件
     **/
    @Override
    @GetMapping("/conditions")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, STATUS, STOP_OR_ENABLE,PROVINCE)));
    }


    @AutoLog(value = "区域配置-新增区域分类")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增区域分类")
    @PostMapping("/saveRegionCategory")
    Result<?> saveRegionCategory(@RequestBody InsRegionConfigModel regionConfigModel) {
        try {
            iInsRegionConfigService.saveRegionCategory(regionConfigModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("区域配置-新增区域分类异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "区域配置-新增区域")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增区域")
    @PostMapping("/saveRegion")
    Result<?> saveRegion(@RequestBody InsRegionConfigModel regionConfigModel) {
        try {
            iInsRegionConfigService.saveRegion(regionConfigModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("区域配置-新增区域异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "区域配置-更新区域分类")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新区域分类")
    @PostMapping("/updateRegionCategory")
    Result<?> updateRegionCategory(@RequestBody InsRegionConfigModel regionConfigModel) {
        try {
            iInsRegionConfigService.updateRegionCategory(regionConfigModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("区域配置-更新区域分类异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "区域配置-更新区域")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新区域")
    @PostMapping("/updateRegion")
    Result<?> updateRegion(@RequestBody InsRegionConfigModel regionConfigModel) {
        try {
            iInsRegionConfigService.updateRegion(regionConfigModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("区域配置-更新区域异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "区域配置-删除区域分类")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除区域分类")
    @PostMapping("/deleteRegionCategory")
    Result<?> deleteRegionCategory(@RequestBody InsRegionConfigModel regionConfigModel) {
        try {
            iInsRegionConfigService.deleteRegionCategory(regionConfigModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("区域配置-删除区域分类异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "区域配置-获取区域分类列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取区域分类列表")
    @PostMapping("/findRegionCategoryList")
    Result<?> findRegionCategoryList(@RequestBody InsRegionConfigModel regionConfigModel) {
        try {
            PageInfo regionCategoryList = iInsRegionConfigService.findRegionCategoryList(regionConfigModel);
            return Result.OK(regionCategoryList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("区域配置-获取区域分类列表异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "区域配置-根据区域分类id获取区域列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据区域分类id获取区域列表")
    @PostMapping("/findRegionList")
    Result<?> findRegionList(@RequestBody InsRegionConfigModel regionConfigModel) {
        try {
            PageInfo regionList = iInsRegionConfigService.findRegionList(regionConfigModel);
            return Result.OK(regionList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("区域配置-根据区域分类id获取区域列表异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

//    @AutoLog(value = "区域配置-根据区域分类id获取区域树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据区域分类id获取区域树")
    @PostMapping("/findRegionTreeByIds")
    Result<List<RegionConfigVo>> findRegionTreeByIds(@RequestBody InsRegionConfigModel regionConfigModel) {
        try {
            List<RegionConfigVo> regionList = iInsRegionConfigService.findRegionTreeByProvinceIds(regionConfigModel);
            return Result.OK(regionList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("区域配置-根据区域分类id获取区域列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), e.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据区域分类id获取区域树(包含专营店)")
    @PostMapping("/findRegionTreeByIds1")
    Result<List<RegionConfigVo>> findRegionTreeByIds1(@RequestBody InsRegionConfigModel regionConfigModel) {
        try {
            List<RegionConfigVo> regionList = iInsRegionConfigService.findRegionTreeByProvinceIds1(regionConfigModel);
            return Result.OK(regionList);
        } catch (Exception e) {
            log.error("区域配置-根据区域分类id获取区域树(包含专营店)异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), e.getMessage());
        }
    }


}
