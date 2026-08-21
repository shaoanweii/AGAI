package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsHighFrequencyWordsService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.AddHighFrequencyWordsModel;
import com.voc.service.insights.engine.model.InsBaseHighFrequencyQueryModel;
import com.voc.service.insights.engine.model.InsBaseTagInfoModel;
import com.voc.service.insights.engine.model.InsWordsInfoModel;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author liuhb
 * @version 1.0.0
 * @ClassName InsHighFrequencyWordsController
 * @Description
 * @createTime 2024年5月16日 10:18
 */
@RestController
@Tag(name = "高频词汇信息", description = "高频词汇信息")
@RequestMapping("/words")
public class InsHighFrequencyWordsController extends AbstractConditionFilters {

    private static final Logger log = LoggerFactory.getLogger(InsHighFrequencyWordsController.class);
    @Resource
    private IInsHighFrequencyWordsService iInsHighFrequencyWordsService;

    @AutoLog(value = "高频词汇信息-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询")
    @PostMapping("/queryWordsList")
    public Result<?> queryWordsList(@RequestBody @Validated InsBaseHighFrequencyQueryModel insHighFrequencyWordsQueryModel) {
        try {
            PageInfo wordsList = iInsHighFrequencyWordsService.queryWordsList(insHighFrequencyWordsQueryModel);
            return Result.OK(wordsList);
        } catch (Exception e) {
            log.error("高频词汇信息-分页查询异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }



    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增信息")
    @PostMapping("/addHighFrequencyWords")
    public Result<?> addHighFrequencyWords(@RequestBody AddHighFrequencyWordsModel wordsModel) {
        try {
            Boolean aBoolean = iInsHighFrequencyWordsService.addHighFrequencyWords(wordsModel);
            return Result.OK(aBoolean);
        } catch (Exception e) {
            log.error("高频词汇信息-新增信息:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "高频词汇信息-根据Id查询单条信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据Id查询单条信息")
    @GetMapping("/queryWordsInfo")
    public Result<?> queryWordsInfo(@RequestParam("id") String id, @RequestParam("clientId") @NotBlank(message = "客户标识不能为空") String clientId) {
        try {
            InsBaseHighFrequencyQueryModel insHighFrequencyWordsQueryModel = new InsBaseHighFrequencyQueryModel();
            insHighFrequencyWordsQueryModel.setClientId(clientId);
            insHighFrequencyWordsQueryModel.setId(id);
            InsWordsInfoModel insWordsInfoModel = iInsHighFrequencyWordsService.queryWordsInfo(insHighFrequencyWordsQueryModel);
            return Result.OK(insWordsInfoModel);
        } catch (Exception e) {
            log.error("高频词汇信息-根据Id查询单条信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "高频词汇信息-分配")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分配高频词汇信息")
    @PostMapping("/allocationWords")
    public Result<?> allocationWords(@RequestBody InsBaseTagInfoModel model) {
        try {
            return iInsHighFrequencyWordsService.allocationWords(model);
        } catch (Exception e) {
            log.error("高频词汇信息-分配异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Object conditions() {
        return Result.OK(async(CollUtil.set(false, LABEL_TYPE, ENERGY, SERIOUSNESS, USER_JOURNEY, STOP_OR_ENABLE, CAR_TYPE, TAG_LIB_ATTRIBUTE, ALLOCATION_STATUS)));
    }
}
