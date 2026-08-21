package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.model.UploadModel;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsRuleTestService;
import com.voc.service.insights.engine.api.model.InsRuleTestListModel;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsAddRuleTestModel;
import com.voc.service.insights.engine.vo.*;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@Tag(name = "规则测试", description = "规则测试")
@RequestMapping("/ruleTest")
public class InsClosedRuleTestController extends AbstractConditionFilters {


    private static final Logger log = LoggerFactory.getLogger(InsClosedRuleTestController.class);
    @Resource
    private IInsRuleTestService iInsRuleTestService;


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "规则测试列表")
    @PostMapping("/ruleTestList")
    public Result<?> ruleTestList(@RequestBody InsRuleTestListModel model) {
        try {
            PageInfo<RuleTestListVo> ruleTestList = iInsRuleTestService.ruleTestList(model);
            return Result.OK(ruleTestList);
        } catch (Exception e) {
            log.error("规则测试列表:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "添加规则测试")
    @PostMapping("/addRuleTestList")
    public Result<?> addRuleTestList(@RequestBody InsAddRuleTestModel model) {
        try {
            Boolean addRuleTestList = iInsRuleTestService.addRuleTestList(model);
            return Result.OK(addRuleTestList);
        } catch (Exception e) {
            log.error("标签纠错-分页查询异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取测试规则详情")
    @PostMapping("/getRuleInfo")
    public Result<?> getRuleInfo(@RequestBody InsRuleTestListModel model) {
        try {
            PageInfo<InsRuleTestInfoVo> ruleInfoList = iInsRuleTestService.getRuleInfo(model);
            return Result.OK(ruleInfoList);
        } catch (Exception e) {
            log.error("获取规则详情:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "编辑回显")
    @PostMapping("/getInfoRuleId")
    public Result<?> getInfoRuleId(@RequestBody InsRuleTestListModel model) {
        try {
            RuleTestListVo ruleInfoList = iInsRuleTestService.getInfoRuleId(model);
            return Result.OK(ruleInfoList);
        } catch (Exception e) {
            log.error("获取规则详情:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "复制规则")
    @PostMapping("/copyRuleTest")
    public Result<?> copyRuleTest(@RequestBody InsRuleTestListModel model) {
        try {
            Boolean copyRuleTest = iInsRuleTestService.copyRuleTest(model);
            return Result.OK(copyRuleTest);
        } catch (Exception e) {
            log.error("复制规则:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "规则测试")
    @PostMapping("/startRuleTest")
    public Result<?> startRuleTest(@RequestBody InsRuleTestListModel model) {
        try {
            Boolean startRuleTest = iInsRuleTestService.startRuleTest(model);
            return Result.OK(startRuleTest);
        } catch (Exception e) {
            log.error("规则测试:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除数据")
    @PostMapping("/delRuleTest")
    public Result<?> delRuleTest(@RequestBody InsRuleTestListModel model) {
        try {
            Boolean del = iInsRuleTestService.delRuleTest(model);
            return Result.OK(del);
        } catch (Exception e) {
            log.error("删除数据:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "下载模版")
    @GetMapping("/downloadRuleTest")
    public void downloadRuleTest(HttpServletResponse response) {
        try {
            Set<ConditionVo> async = async(CollUtil.set(false, CONTENT_TYPE, EMOTION, INTENTION));
            iInsRuleTestService.downloadRuleTest(response, async);
        } catch (Exception e) {
            log.error("下载模版异常:{}", e.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "本地文件上传")
    @PostMapping("/uploadRuleTest")
    public Result<?> uploadRuleTest(@RequestParam(value = "file") MultipartFile file) {
        try {
            UploadModel uploadModel = iInsRuleTestService.uploadRuleTest(file);
            return Result.OK(uploadModel);
        } catch (Exception e) {
            log.error("本地文件上传异常:{}", e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "数据校验")
    @PostMapping("/checkUploadRuleTest")
    public Result<?> checkUploadRuleTest(@RequestBody InsRuleTestListModel model) {
        try {
            InsRuleTestValidateVo dataSourceValidateVo = iInsRuleTestService.checkUploadRuleTest(model);
            return Result.OK(dataSourceValidateVo);
        } catch (Exception e) {
            log.error("数据校验异常:{}", e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取规则下拉列表")
    @PostMapping("/ruleSelect")
    public Result<?> ruleSelect() {
        try {
            Map<String, List<InsCategoryRuleVo>> ruleSelectVoList = iInsRuleTestService.ruleSelect();
            return Result.OK(ruleSelectVoList);
        } catch (Exception e) {
            log.error("获取规则下拉列表:{}", e.getMessage());
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取下拉创建用户")
    @PostMapping("/queryCreateUserList")
    public Result<?> queryCreateUserList() {
        try {
            List<String> createUserList = iInsRuleTestService.queryCreateUserList();
            return Result.OK(createUserList);
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
        return Result.OK(async(CollUtil.set(false, RULE_TEST, RULE_TYPE)));
    }


}
