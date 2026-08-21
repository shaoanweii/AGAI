package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsLabelCorrectionRecordService;
import com.voc.service.insights.engine.api.model.InsCqCaLabelCorrectionRecordModel;
import com.voc.service.insights.engine.api.model.InsertLabelCorrectionRecordModel;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsCqCaLabelCorrectionRecordQueryModel;
import com.voc.service.insights.engine.model.InsCqCaUpdateLabelRecordModel;
import com.voc.service.insights.engine.model.InsLabelCorrectionRecordQueryModel;
import com.voc.service.insights.engine.model.UpdateLabelRecordModel;
import com.voc.service.insights.engine.vo.InsCqCaCorrectionInfoVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@Tag(name = "标签纠错", description = "标签纠错")
@RequestMapping("/addLabel")
public class InsCqCaLabelCorrectionRecordController extends AbstractConditionFilters {


    private static final Logger log = LoggerFactory.getLogger(InsCqCaLabelCorrectionRecordController.class);
    @Resource
    private IInsLabelCorrectionRecordService iInsLabelCorrectionRecordService;


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "添加")
    @PostMapping("/insertLabelCorrection")
    public Result<?> insertLabelCorrection(@RequestBody @Validated InsCqCaLabelCorrectionRecordModel model) {
        try {
            final UserModel user = ServiceContextHolder.getUser();
            model.setOperateUser(user.getUsername());
            Boolean b = iInsLabelCorrectionRecordService.insertLabelCorrection(model);
            return Result.OK(b);
        } catch (Exception e) {
            log.error("标签纠错-添加:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询")
    @PostMapping("/queryLabelCorrectionList")
    public Result<?> queryLabelCorrectionList(@RequestBody @Validated InsCqCaLabelCorrectionRecordQueryModel model) {
        try {
            PageInfo  labelCorrectionList = iInsLabelCorrectionRecordService.queryLabelCorrectionList(model);
            return Result.OK(labelCorrectionList);
        } catch (Exception e) {
            log.error("标签纠错-分页查询异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "状态更改")
    @PostMapping("/auditLabelCorrection")
    public Result<?> auditLabelCorrection(@RequestBody InsCqCaUpdateLabelRecordModel model) {
        try {
            Boolean status = iInsLabelCorrectionRecordService.auditLabelCorrection(model);
            return Result.OK(status);
        } catch (Exception e) {
            log.error("标签纠错-状态更改:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询数据明细")
    @PostMapping("/queryDataInfo")
    public Result<?> queryDataInfo(@RequestBody @Validated InsCqCaLabelCorrectionRecordQueryModel model) {
        try {
            PageInfo pageInfo = iInsLabelCorrectionRecordService.queryDataInfo(model);
            return Result.OK(pageInfo);
        } catch (Exception e) {
            log.error("标签纠错-查询数据明细:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询数据明细")
    @PostMapping("/queryCorrectionInfo")
    public Result<?> queryCorrectionInfo(@RequestBody @Validated InsCqCaLabelCorrectionRecordQueryModel model) {
        try {
            InsCqCaCorrectionInfoVo insCqCaCorrectionInfoVo = iInsLabelCorrectionRecordService.queryCorrectionInfo(model);
            return Result.OK(insCqCaCorrectionInfoVo);
        } catch (Exception e) {
            log.error("标签纠错-查询数据明细:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询数据明细")
    @PostMapping("/queryCreateUserList")
    public Result<?> queryCreateUserList(@RequestBody @Validated InsCqCaLabelCorrectionRecordQueryModel model) {
        try {
            List<String> createUserList = iInsLabelCorrectionRecordService.queryCreateUserList(model);
            return Result.OK(createUserList);
        } catch (Exception e) {
            log.error("标签纠错-查询数据明细:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除数据")
    @PostMapping("/del")
    public Result<?> del(@RequestBody @Validated InsCqCaLabelCorrectionRecordQueryModel model) {
        try {
            Boolean del = iInsLabelCorrectionRecordService.del(model);
            return Result.OK(del);
        } catch (Exception e) {
            log.error("标签纠错-查询数据明细:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Override
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, AUDIT_STATUS,BRAND_CAR)));
    }


}
