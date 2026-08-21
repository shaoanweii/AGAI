package com.voc.service.insights.engine.web;

import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsBasicInfoService;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 09:54
 * @描述:
 **/
@Tag(name = "基础信息服务")
@RestController
@RequestMapping("/basicInfo")
public class InsBasicInfoController {
    @Autowired
    IInsBasicInfoService basicInfoService;

    /**
     * @return com.voc.service.common.response.Result<java.util.List < com.voc.service.insights.engine.vo.EnergyInfoVo>>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 10:47
     * @描述 获取能源信息
     **/
    @AutoLog(value = "基础信息-获取能源信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取能源信息")
    @GetMapping("/findEnergyInfo")
    Result<?> findEnergyInfo(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNumber
            , @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        PageInfo pageInfo = basicInfoService.findEnergyInfo(pageNumber, pageSize);
        return Result.OK(pageInfo);
    }

    /**
     * @return com.voc.service.common.response.Result<java.util.List < com.voc.service.insights.engine.vo.ProvinceAreaInfoVo>>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 13:48
     * @描述 获取省市信息
     **/
    @AutoLog(value = "基础信息-获取省市信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取省市信息")
    @GetMapping("/findProvinceAreaInfo")
    Result<?> findProvinceAreaInfo(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNumber
            , @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        PageInfo provinceAreaInfoVo = basicInfoService.findProvinceAreaInfoVo(pageNumber, pageSize);
        return Result.OK(provinceAreaInfoVo);
    }

    @AutoLog(value = "基础信息-获取车辆信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取车辆信息")
    @GetMapping("/findVehicleInfo")
    Result<?> findVehicleInfo(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNumber
            , @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        PageInfo vehicleInfo = basicInfoService.findVehicleInfo(pageNumber, pageSize);
        return Result.OK(vehicleInfo);
    }


    @AutoLog(value = "基础信息-获取标签分类信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取标签分类信息")
    @GetMapping("/findLabelTypeInfo")
    Result<?> findLabelTypeInfo(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNumber
            , @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        PageInfo labelTypeInfo = basicInfoService.findLabelTypeInfo(pageNumber, pageSize);
        return Result.OK(labelTypeInfo);
    }

    @AutoLog(value = "基础信息-获取严重性信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取严重性信息")
    @GetMapping("/findSeriousnessInfo")
    Result<?> findSeriousnessInfo(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNumber
            , @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        PageInfo labelTypeInfo = basicInfoService.findSeriousnessInfo(pageNumber, pageSize);
        return Result.OK(labelTypeInfo);
    }

    @AutoLog(value = "基础信息-获取用户旅程信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取用户旅程信息")
    @GetMapping("/findUserJourneyInfo")
    Result<?> findUserJourneyInfo(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNumber
            , @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        PageInfo userJourneyInfo = basicInfoService.findUserJourneyInfo(pageNumber, pageSize);
        return Result.OK(userJourneyInfo);
    }

}
