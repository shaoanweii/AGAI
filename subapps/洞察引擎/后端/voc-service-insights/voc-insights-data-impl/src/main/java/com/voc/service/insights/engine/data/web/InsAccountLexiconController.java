package com.voc.service.insights.engine.data.web;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsAccountLexiconService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsAccountLexiconModel;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;
import com.voc.service.insights.engine.vo.InsAccountLexiconVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @创建者: fanrong
 * @创建时间: 2025/11/6 17:17
 * @描述:
 **/
@Tag(name = "账号词库详情")
@RestController
@RequestMapping("/accountLexicon")
public class InsAccountLexiconController extends AbstractConditionFilters {
    @Autowired
    private IInsAccountLexiconService insAccountLexiconService;


    private static final Logger log = LoggerFactory.getLogger(InsAccountLexiconController.class);

    @AutoLog(value = "账号词库详情-保存账户词库详情")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "保存账户词库详情")
    @PostMapping("/saveAccountLexiconDetails")
    Result<?> saveAccountLexiconDetails(@RequestBody InsAccountLexiconModel insAccountLexicon){
        try {
            insAccountLexiconService.saveAccountLexiconDetails(insAccountLexicon);
            return Result.OK();
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("账号词库详情-保存账户词库详情异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "账号词库详情-更新账户词库详情")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "更新账户词库详情")
    @PostMapping("/updateAccountLexiconDetails")
    Result<?> updateAccountLexiconDetails(@RequestBody InsAccountLexiconModel insAccountLexicon){
        try {
            insAccountLexiconService.updateAccountLexiconDetails(insAccountLexicon);
            return Result.OK();
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.error(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("账号词库详情-更新账户词库详情异常:",e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "账号词库详情-根据id查询账户词库详情")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "根据id查询账户词库详情")
    @PostMapping("/findAccountLexiconInfo")
    Result<InsAccountLexiconVo> findAccountLexiconInfo(@RequestBody InsAccountLexiconModel insAccountLexicon){
        try {
            InsAccountLexiconVo accountLexiconInfo = insAccountLexiconService.findAccountLexiconInfo(insAccountLexicon);
            return Result.OK(accountLexiconInfo);
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.errors(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("账号词库详情-保存账户词库详情异常:",e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "账号词库详情-查询账户词库列表")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "查询账户词库列表")
    @PostMapping("/findAccountLexiconList")
    Result<IPage<InsAccountLexiconVo>> findAccountLexiconList(@RequestBody InsAccountLexiconModel insAccountLexicon){
        try {
            IPage<InsAccountLexiconVo> accountLexiconList = insAccountLexiconService.findAccountLexiconList(insAccountLexicon);
            return Result.OK(accountLexiconList);
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.errors(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("账号词库详情-查询账户词库列表异常:",e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "账号词库详情-批量更新账户词库状态")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "批量更新账户词库状态")
    @PostMapping("/changeAccountLexiconStatus")
    Result<?> changeAccountLexiconStatus(@RequestBody InsAccountLexiconModel insAccountLexicon){
        try {
            insAccountLexiconService.changeAccountLexiconStatus(insAccountLexicon);
            return Result.OK();
        }catch (IllegalArgumentException illegalArgumentException){
            log.error("",illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(),illegalArgumentException.getMessage());
        }catch (BussinessException bussinessException){
            log.error("",bussinessException);
            return Result.errors(bussinessException.getCode(),bussinessException.getMessage());
        }catch (Exception e){
            log.error("账号词库详情-批量更新账户词库状态异常:",e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @Override
    @GetMapping("/conditions")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, STOP_OR_ENABLE,RULE_STATUS)));
    }
    
}
