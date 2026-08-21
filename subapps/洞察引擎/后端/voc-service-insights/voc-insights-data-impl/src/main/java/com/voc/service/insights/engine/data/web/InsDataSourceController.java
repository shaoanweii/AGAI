package com.voc.service.insights.engine.data.web;


import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.model.UploadModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.StopWatch;
import com.voc.service.insights.engine.api.IInsTagLibClientService;
import com.voc.service.insights.engine.api.data.IInsDataSourceService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.enums.TagLibeType;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.model.data.DataProcessingTaskQuery;
import com.voc.service.insights.engine.vo.InsDataSourceSearchCriteriaVo;
import com.voc.service.insights.engine.vo.InsDataSourceValidateVo;
import com.voc.service.insights.engine.vo.InsDataSourceVo;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 数据源集(InsDataSource)表控制层
 *
 * @author leiww
 * @since 2024-02-27 15:31:46
 */
@RestController
@Tag(name = "数据源集", description = "数据源集")
@RequestMapping("/insDataSource")
public class InsDataSourceController extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsDataSourceController.class);
    /**
     * 服务对象
     */
    @Resource
    private IInsDataSourceService insDataSourceService;
    @Autowired
    private IInsTagLibClientService insTagLibClientService;

    @Override
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, DATA_TYPE, DATA_SOURCE_ACCESS_WAY,ORIGINAL_DATA_STATUS,RESULT_DATA_STATUS,EMOTION,INTENTION,LABEL_TYPE,LABEL_AND_MODEL,PROCESSING_MODEL)));
    }

    @AutoLog(value = "数据源-下载模版")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "下载模版")
    @GetMapping("/downloadDataSource")
    public Result<?> downloadDataSource(HttpServletResponse response,@RequestParam(value = "clientId") String clientId,@RequestParam(value = "fileName") String fileName) {
        try {
            insDataSourceService.downloadDataSource(response, clientId, fileName);
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("下载模版异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-本地文件上传")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "本地文件上传")
    @PostMapping("/uploadDataSource")
    public Result<?> uploadDataSource(@RequestParam(value = "file") MultipartFile file) {
        try {
            UploadModel uploadModel = insDataSourceService.uploadDataSource(file);
            return Result.OK(uploadModel);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("本地文件上传异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "数据源-数据校验")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "数据校验")
    @PostMapping("/checkUploadDataSource")
    public Result<?> checkUploadDataSource(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            InsDataSourceValidateVo dataSourceValidateVo = insDataSourceService.checkUploadDataSource(insDataSourceModel);
            return Result.OK(dataSourceValidateVo);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("数据校验异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }



    @AutoLog(value = "数据源-本地上传数据保存")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "本地上传数据保存")
    @PostMapping("/saveUploadDataSource")
    public Result<?> saveUploadDataSource(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            insDataSourceService.saveUploadDataSource(insDataSourceModel);
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("本地上传数据保存异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-新增数据源")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增数据源")
    @PostMapping("/saveDataSource")
    public Result<?> saveDataSource(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            insDataSourceService.saveDataSource(insDataSourceModel);
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("新增数据源异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-分页获取数据源列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页获取数据源列表")
    @PostMapping("/findDataSource")
    public Result<?> findDataSource(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            PageInfo dataSource = insDataSourceService.findDataSource(insDataSourceModel);
            return Result.OK(dataSource);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("分页获取数据源列表异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据处理-任务列表查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询数据处理任务")
    @PostMapping("/findDataProcessingTasks")
    public Result<?> findDataProcessingTasks(@RequestBody DataProcessingTaskQuery query) {
        return Result.OK(insDataSourceService.findDataProcessingTasks(query));
    }

    @AutoLog(value = "数据源-分页获取数据源详情列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页获取数据源详情列表")
    @PostMapping("/findDataSourceDetail")
    public Result<?> findDataSourceDetail(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            PageInfo dataSource = insDataSourceService.findDataSourceDetail(insDataSourceModel);
            return Result.OK(dataSource);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("分页获取数据源详情列表异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-根据批次id删除数据源详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据批次id删除数据源详情")
    @PostMapping("/deleteDataSourceDetail")
    public Result<?> deleteDataSourceDetail(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            insDataSourceService.deleteDataSourceDetail(insDataSourceModel);
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("根据批次id删除数据源详情异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "数据源-开始处理")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "开始处理")
    @PostMapping("/startProcessing")
    public Result<?> startProcessing(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            insDataSourceService.startProcessing(insDataSourceModel);
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("开始处理异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-更新数据源")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新数据源")
    @PostMapping("/updateDataSource")
    public Result<?> updateDataSource(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            insDataSourceService.updateDataSource(insDataSourceModel);
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("更新数据源异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-删除数据源")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除数据源")
    @PostMapping("/deleteDataSource")
    public Result<?> deleteDataSource(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            insDataSourceService.deleteDataSource(insDataSourceModel);
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("删除数据源异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "接收数据处理结果")
    @PostMapping("/pushResultData")
    public Result<?> pushResultData(@RequestBody List<InsDataSourceModel> insDataSourceModel) {
        try {
            insDataSourceService.pushResultData(insDataSourceModel);
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("接收数据处理结果异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-获取原始数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取原始数据")
    @PostMapping("/getRawData")
    public Result<?> getRawData(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            PageInfo rawData = insDataSourceService.getRawData(insDataSourceModel);
            return Result.OK(rawData);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取原始数据异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-获取结果数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取结果数据")
    @PostMapping("/getRawDataResult")
    public Result<?> getRawDataResult(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            PageInfo rawData = insDataSourceService.getRawDataResult(insDataSourceModel);
            return Result.OK(rawData);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取结果数据异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-导出原始数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出原始数据")
    @PostMapping("/exportRawData")
    public Result<?> exportRawData(@RequestBody InsDataSourceModel insDataSourceModel,HttpServletResponse response) {
        try {
            Boolean b = insDataSourceService.exportRawData(insDataSourceModel, response);
            return Result.OK(b);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-导出结果数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出结果数据")
    @PostMapping("/exportRawDataResult")
    public Result<?> exportRawDataResult(@RequestBody InsDataSourceModel insDataSourceModel,HttpServletResponse response) {
//    public void exportRawDataResult(@RequestParam String dataSourceId,@RequestParam String clientId,HttpServletResponse response) {
        try {
//            InsDataSourceModel insDataSourceModel = new InsDataSourceModel();
//            insDataSourceModel.setDataSourceId(dataSourceId);
//            insDataSourceModel.setClientId(clientId);
            Boolean b = insDataSourceService.exportRawDataResult(insDataSourceModel, response);
            return Result.OK(b);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-根据数据源类型获取数据源列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据数据源类型获取数据源列表")
    @PostMapping("/getDataSourceList")
    public Result<?> getDataSourceList(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            List<InsDataSourceVo> dataSourceList = insDataSourceService.getDataSourceList(insDataSourceModel);
            return Result.OK(dataSourceList);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("根据数据源类型获取数据源列表异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-获取查询条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取查询条件")
    @PostMapping("/getDataSourceSearchCriteria")
    public Result<?> getDataSourceSearchCriteria(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            StopWatch watch = new StopWatch();
            watch.start("getDataSourceSearchCriteria");
            InsDataSourceSearchCriteriaVo dataSourceSearchCriteria = insDataSourceService.getDataSourceSearchCriteria(insDataSourceModel);
            watch.start("findTagLibClientTree.BIZ");
            watch.stop();
            List<TagLibCategoryVo> BIZ = insTagLibClientService.findTagLibClientCategoryTree(insDataSourceModel.getClientId(),TagLibeType.PROD.getCode());
            watch.stop();
            watch.start("findTagLibClientTree.QY");
            List<TagLibCategoryVo> SERVICE = insTagLibClientService.findTagLibClientCategoryTree(insDataSourceModel.getClientId(), TagLibeType.SERVICE.getCode());
            List<TagLibCategoryVo> QY = insTagLibClientService.findTagLibClientCategoryTree(insDataSourceModel.getClientId(),TagLibeType.QY.getCode());
//            dataSourceSearchCriteria.setQY(QY);
//            dataSourceSearchCriteria.setBIZ(BIZ);
            watch.stop();
            TagLibCategoryVo prods = TagLibCategoryVo.builder().id(TagLibeType.PROD.getText()).tagName(TagLibeType.PROD.getText()).child(BIZ).build();
            TagLibCategoryVo services = TagLibCategoryVo.builder().id(TagLibeType.SERVICE.getText()).tagName(TagLibeType.SERVICE.getText()).child(SERVICE).build();
            TagLibCategoryVo qys = TagLibCategoryVo.builder().id(TagLibeType.QY.getText()).tagName(TagLibeType.QY.getText()).child(QY).build();
            List<TagLibCategoryVo> list = Arrays.asList(prods, services, qys);
            dataSourceSearchCriteria.setTagLibCategoryVos(list);
            log.debug("getDataSourceSearchCriteria.watch:{}",watch.prettyPrint());
            return Result.OK(dataSourceSearchCriteria);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取查询条件异常:{}",e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }



    @AutoLog(value = "数据源-导出数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出数据")
    @PostMapping("/exportRawDataByStatus")
    public void exportRawDataByStatus(@RequestBody InsDataSourceModel insDataSourceModel,HttpServletResponse response) {
        try {
            insDataSourceService.exportRawDataByStatus(insDataSourceModel,response);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }


    @AutoLog(value = "数据源-导出系统集成数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出系统集成数据")
    @PostMapping("/exportSIRawDataByStatus")
    public void exportSIRawDataByStatus(@RequestBody InsDataSourceModel insDataSourceModel,HttpServletResponse response) {
        try {
            insDataSourceService.exportSIRawDataByStatus(insDataSourceModel,response);


        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    @AutoLog(value = "数据源-获取全部workId")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取全部workId")
    @PostMapping("/findAllWorkId")
    public Result<Set<String>> findAllWorkId(@RequestBody InsDataSourceModel insDataSourceModel){
        try {
            Set<String> workIds = insDataSourceService.findAllWorkIdByDataSourceIds(insDataSourceModel.getClientId(), null);
            return Result.OK(workIds);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取全部workId异常:{}",e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "数据源-分页获取系统集成类型的数据源详情列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页获取系统集成类型的数据源详情列表")
    @PostMapping("/findSIDataSourceDetail")
    public Result<?> findSIDataSourceDetail(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            PageInfo dataSource = insDataSourceService.findSIDataSourceList(insDataSourceModel);
            return Result.OK(dataSource);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("分页获取系统集成类型的数据源详情列表异常:{}",e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "数据源-更新系统集成类型的数据源")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新系统集成类型的数据源")
    @PostMapping("/updateSIDataSource")
    public Result<?> updateSIDataSource(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            insDataSourceService.updateSIDataSource(insDataSourceModel);
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("更新系统集成类型的数据源异常:{}",e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "数据源-获取原始数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取原始数据")
    @PostMapping("/getSIRawData")
    public Result<?> getSIRawData(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            PageInfo rawData = insDataSourceService.getSIRawData(insDataSourceModel);
            return Result.OK(rawData);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取原始数据异常:{}",e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "数据源-获取结果数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取结果数据")
    @PostMapping("/getSIRawDataResult")
    public Result<?> getSIRawDataResult(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            PageInfo rawData = insDataSourceService.getSIRawDataResult(insDataSourceModel);
            return Result.OK(rawData);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取结果数据异常:{}",e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "数据源-导出原始数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出原始数据")
    @PostMapping("/exportSIRawData")
    public Result<?> exportSIRawData(@RequestBody InsDataSourceModel insDataSourceModel,HttpServletResponse response) {
        try {
            Boolean b = insDataSourceService.exportSIRawData(insDataSourceModel, response);
            return Result.OK(b);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "数据源-导出结果数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出结果数据")
    @PostMapping("/exportSIRawDataResult")
    public Result<?> exportSIRawDataResult(@RequestBody InsDataSourceModel insDataSourceModel,HttpServletResponse response) {
        try {
            Boolean b = insDataSourceService.exportSIRawDataResult(insDataSourceModel, response);
            return Result.OK(b);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "数据源-获取系统集成类型的查询条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取系统集成类型的查询条件")
    @PostMapping("/getSIDataSourceSearchCriteria")
    public Result<?> getSIDataSourceSearchCriteria(@RequestBody InsDataSourceModel insDataSourceModel) {
        try {
            StopWatch watch = new StopWatch();
            watch.start("getDataSourceSearchCriteria");
            InsDataSourceSearchCriteriaVo dataSourceSearchCriteria = insDataSourceService.getSIDataSourceSearchCriteria(insDataSourceModel);
            watch.start("findTagLibClientTree.BIZ");
            watch.stop();
            List<TagLibCategoryVo> BIZ = insTagLibClientService.findTagLibClientCategoryTree(insDataSourceModel.getClientId(),TagLibeType.PROD.getCode());
            List<TagLibCategoryVo> SERVICE = insTagLibClientService.findTagLibClientCategoryTree(insDataSourceModel.getClientId(),TagLibeType.SERVICE.getCode());
            watch.stop();
            watch.start("findTagLibClientTree.QY");
            List<TagLibCategoryVo> QY = insTagLibClientService.findTagLibClientCategoryTree(insDataSourceModel.getClientId(),TagLibeType.QY.getCode());
//            dataSourceSearchCriteria.setQY(QY);
//            dataSourceSearchCriteria.setBIZ(BIZ);
            TagLibCategoryVo prods = TagLibCategoryVo.builder().id(TagLibeType.PROD.getCode()).tagName(TagLibeType.PROD.getText()).child(BIZ).build();
            TagLibCategoryVo services = TagLibCategoryVo.builder().id(TagLibeType.SERVICE.getCode()).tagName(TagLibeType.SERVICE.getText()).child(SERVICE).build();
            TagLibCategoryVo qys = TagLibCategoryVo.builder().id(TagLibeType.QY.getCode()).tagName(TagLibeType.QY.getText()).child(QY).build();
            List<TagLibCategoryVo> list = Arrays.asList(prods, services, qys);
            dataSourceSearchCriteria.setTagLibCategoryVos(list);
            watch.stop();
            log.debug("getDataSourceSearchCriteria.watch:{}",watch.prettyPrint());
            return Result.OK(dataSourceSearchCriteria);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取系统集成类型的查询条件异常:{}",e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }
}
