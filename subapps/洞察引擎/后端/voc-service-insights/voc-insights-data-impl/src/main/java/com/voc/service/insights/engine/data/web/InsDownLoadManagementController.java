package com.voc.service.insights.engine.data.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsDownLoadManagementService;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.vo.DownLoadFileVo;
import com.voc.service.insights.engine.vo.InsDownAccountInfoAuthVo;
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

import java.util.List;


@Tag(name = "下载管理服务")
@RestController
@RequestMapping("/downLoad")
public class InsDownLoadManagementController {
    private static final Logger log = LoggerFactory.getLogger(InsDownLoadManagementController.class);
    @Autowired
    private IInsDownLoadManagementService downLoadManagementService;

    @AutoLog(value = "下载管理服务-查询下载列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询下载列表")
    @PostMapping("/findDownLoadFileList")
    Result<IPage<DownLoadFileVo>> findReportDownLoadFileList(@RequestBody LargeDigitaFilesModel model) {
        try {
            IPage<DownLoadFileVo> reportDownLoadFileList = downLoadManagementService.findReportDownLoadFileList(model);
            return Result.OK(reportDownLoadFileList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("下载管理服务-查询下载列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "下载管理服务-重新下载")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "重新下载")
    @PostMapping("/downloadAgain")
    Result<?> downloadAgain(@RequestBody LargeDigitaFilesModel model, HttpServletResponse response) {
        try {
            downLoadManagementService.downloadAgain(model,response);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("下载管理服务-重新下载异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "下载管理服务-获取可见用户列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取可见用户列表")
    @GetMapping("/findVisibleUserList")
    Result<List<InsDownAccountInfoAuthVo>> findVisibleUserList(@RequestParam(defaultValue = "false") Boolean isAllVisible) {
        try {
            List<InsDownAccountInfoAuthVo> visibleUserList = downLoadManagementService.findVisibleUserList(isAllVisible);
            return Result.OK(visibleUserList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("下载管理服务-获取可见用户列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "下载管理服务-下载文件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "下载文件")
    @PostMapping("/downloadFile")
    void downloadFile(HttpServletResponse response, @RequestBody LargeDigitaFilesModel model) {
        try {
            downLoadManagementService.downloadFile(model, response);
        } catch (Exception e) {
            log.error("下载管理服务-下载文件异常:", e);
            throw e;
        }
    }


}
