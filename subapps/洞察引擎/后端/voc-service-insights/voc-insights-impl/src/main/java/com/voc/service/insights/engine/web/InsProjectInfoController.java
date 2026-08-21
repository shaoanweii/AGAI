package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsCarSeriesInfoService;
import com.voc.service.insights.engine.api.IInsProjectInfoService;
import com.voc.service.insights.engine.api.IInsRegionConfigService;
import com.voc.service.insights.engine.api.data.IInsDataSourceService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsProjectInfoModel;
import com.voc.service.insights.engine.model.InsRegionConfigModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.*;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhongxl
 * @version 1.0.0
 * @ClassName InsProjectInfoController
 * @Description
 * @createTime 2024年3月08日 11:18
 * @Copyright futong
 */
@RestController
@Tag(name = "项目管理", description = "项目管理")
@RequestMapping("/insProjectInfo")
public class InsProjectInfoController extends AbstractConditionFilters {

    private static final Logger log = LoggerFactory.getLogger(InsProjectInfoController.class);
    @Resource
    private IInsProjectInfoService insProjectInfoService;
    @Resource
    private IInsDataSourceService dataSourceService;
    @Autowired
    IInsCarSeriesInfoService iInsCarSeriesInfoService;
    @Autowired
    IInsRegionConfigService regionConfigService;

    @Override
    @Operation(summary = "过滤条件")
    @GetMapping("/conditions")
    public Result<?> conditions() {
       return Result.OK(async(CollUtil.set(false, STATUS, STOP_OR_ENABLE,PROVINCE,EMOTION,INTENTION,DATA_TYPE,LABEL_TYPE,EARLY_WARNING_TYPE,RISK_LEVEL,INSIGHT_CYCLE,IS_APPLY,COLOR)));
    }

    @AutoLog(value = "项目管理-新增项目")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增项目")
    @PostMapping("/saveProjectInfo")
    Result<?> saveProjectInfo(@RequestBody InsProjectInfoModel insProjectInfoModel) {
        try {
            insProjectInfoService.saveProjectInfo(insProjectInfoModel);
            return Result.OK();
        }  catch (Exception e) {
            log.error("项目管理-新增项目异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "项目管理-更新项目")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新项目")
    @PostMapping("/updateProjectInfo")
    Result<?> updateProjectInfo(@RequestBody InsProjectInfoModel insProjectInfoModel) {
        try {
            insProjectInfoService.updateProjectInfo(insProjectInfoModel);
            return Result.OK();
        } catch (Exception e) {
            log.error("项目管理-更新项目异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "项目管理-获取项目列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取项目列表")
    @PostMapping("/findProjectList")
    Result<?> findProjectList(@RequestBody InsProjectInfoModel insProjectInfoModel) {
        try {
            PageInfo projectList = insProjectInfoService.findProjectList(insProjectInfoModel);
            return Result.OK(projectList);
        }catch (Exception e) {
            log.error("项目管理-获取项目列表异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "项目管理-根据id获取项目信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据id获取项目信息")
    @PostMapping("/findProjectInfo")
    Result<?> findProjectInfo(@RequestBody InsProjectInfoModel insProjectInfoModel) {
        try {
            ProjectInfoVo projectInfo = insProjectInfoService.findProjectInfo(insProjectInfoModel);
            return Result.OK(projectInfo);
        } catch (Exception e) {
            log.error("项目管理-根据id获取项目信息异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "项目管理-获取数据源信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取数据源信息")
    @PostMapping("/findDataSourceInfo")
    Result<?> findDataSourceInfo(@RequestBody InsProjectInfoModel insProjectInfoModel) {
        try {
            List<InsDataSourceTreeVo> allDataSource = dataSourceService.findAllDataSource(InsDataSourceModel.builder().clientId(insProjectInfoModel.getClientId()).build());
            return Result.OK(allDataSource);
        }catch (Exception e) {
            log.error("项目管理-获取数据源信息异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

//    @AutoLog(value = "项目管理-获取品牌车系树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取品牌车系树")
    @GetMapping("/findBrandCarSeriesInfo")
    Result<?> findBrandCarSeriesInfo() {
        try {
            List<BrandInfoVo> brandCarsTree = iInsCarSeriesInfoService.findBrandCarsTree();
            return Result.OK(brandCarsTree);
        } catch (Exception e) {
            log.error("项目管理-获取品牌车系树异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "项目管理-获取区域信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取区域信息")
    @PostMapping("/findRegionInfo")
    Result<?> findRegionInfo(@RequestBody InsProjectInfoModel insProjectInfoModel) {
        try {
            List<RegionConfigVo> regionTree = regionConfigService.findRegionTree(InsRegionConfigModel.builder().clientId(insProjectInfoModel.getClientId()).brandName(insProjectInfoModel.getBrandName()).build());
            return Result.OK(regionTree);
        } catch (Exception e) {
            log.error("项目管理-获取区域信息异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "项目管理-获取原始信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取原始信息")
    @PostMapping("/findRawData")
    Result<?> findRawData(@RequestBody InsDataSourceModel dataSourceModel) {
        try {
            PageInfo rawData = insProjectInfoService.findRawData(dataSourceModel);
            return Result.OK(rawData);
        } catch (Exception e) {
            log.error("项目管理-获取原始信息异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "项目管理-获取结果信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取结果信息")
    @PostMapping("/findResultData")
    Result<?> findResultData(@RequestBody InsDataSourceModel dataSourceModel) {
        try {
            PageInfo rawData = insProjectInfoService.findResultData(dataSourceModel);
            return Result.OK(rawData);
        }  catch (Exception e) {
            log.error("项目管理-获取结果信息异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "项目管理-获取搜索条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取搜索条件")
    @PostMapping("/findSearchCriteria")
    Result<?> findSearchCriteria(@RequestBody InsDataSourceModel dataSourceModel) {
        try {
            InsDataSourceSearchCriteriaVo searchCriteria = insProjectInfoService.findSearchCriteria(dataSourceModel);
            return Result.OK(searchCriteria);
        }  catch (Exception e) {
            log.error("项目管理-获取搜索条件异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "项目管理-导出原始数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出原始数据")
    @PostMapping("/exportProjectRawDataResult")
    public Result<?> exportProjectRawDataResult(@RequestBody InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        try {
            Boolean b = insProjectInfoService.exportRawData(insDataSourceModel, response);
            return Result.OK(b);
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "项目管理-导出结果数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出结果数据")
    @PostMapping("/exportProjectResultData")
    public Result<?> exportProjectResultData(@RequestBody InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        try {
            Boolean b = insProjectInfoService.exportResultData(insDataSourceModel, response);
            return Result.OK(b);
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "项目管理-查看风险预警数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查看风险预警数据")
    @PostMapping("/findRiskWarningData")
    Result<?> findRiskWarningData(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            PageInfo riskWarningData = insProjectInfoService.findRiskWarningData(insDataSourceModel);
            return Result.OK(riskWarningData);
        } catch (Exception e) {
            log.error("项目管理-查看风险预警数据异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "项目管理-导出结果数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出结果数据")
    @PostMapping("/exportRiskWarningData")
    public void exportRiskWarningData(@RequestBody InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        try {
            insProjectInfoService.exportRiskWarningData(insDataSourceModel, response);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

//    @AutoLog(value = "项目管理-根据客户id获取全部项目的风险预警配置")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据客户id获取全部项目的风险预警配置")
    @PostMapping("/findRiskWarningInfo")
    public Result<List<ProjectInfoVo>> findRiskWarningInfo(@RequestBody InsProjectInfoModel projectInfoModel) {
        try {
            List<ProjectInfoVo> riskWarningInfo = insProjectInfoService.findRiskWarningInfo(projectInfoModel);
            return Result.OK(riskWarningInfo);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("项目管理-根据客户id获取全部项目的风险预警配置异常:{}", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),e.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据项目id获取项目标签")
    @PostMapping("/findBrandTabLabelByProjectId")
    public Result<List<BrandVo>> findBrandTabLabelByProjectId(@RequestBody InsProjectInfoModel projectInfoModel) {
        try {
            List<BrandVo> brandVos = insProjectInfoService.findBrandTabLabelByProjectId(projectInfoModel);
            return Result.OK(brandVos);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("项目管理-根据项目id获取项目标签异常:{}", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),e.getMessage());
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据项目id获取项目品牌信息")
    @PostMapping("/findBrandInfo")
    public Result<List<BrandVo>> findBrandInfo(@RequestBody InsProjectInfoModel projectInfoModel) {
        try {
            List<BrandVo> brandVos = insProjectInfoService.findBrandInfo(projectInfoModel);
            return Result.OK(brandVos);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("项目管理-根据项目id获取项目标签异常:{}", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),e.getMessage());
        }
    }


    @AutoLog(value = "项目管理-标签")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查看风险预警数据")
    @PostMapping("/allLibClientCategoryTree")
    Result<?> allLibClientCategoryTree(@RequestParam(value = "clientId") String clientId) {
        try {
            List<TagLibCategoryVo> tagLibCategoryVos = insProjectInfoService.allLibClientCategoryTree(clientId);
            return Result.OK(tagLibCategoryVos);
        } catch (Exception e) {
            log.error("项目管理-查看风险预警数据异常:{}", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新车上市车系对比信息")
    @GetMapping("/getNewCarSeriesCondition")
    public Result<NewCarSeriesConditionVo> getNewCarSeriesCondition() {
        try {
            NewCarSeriesConditionVo newCarSeriesCondition = iInsCarSeriesInfoService.getNewCarSeriesCondition();
            return Result.OK(newCarSeriesCondition);
        } catch (Exception e) {
            log.error("新车上市-车系对比异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), e.getMessage());
        }
    }
}
