package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsTagLibService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsTagLibModel;
import com.voc.service.insights.engine.vo.DictInfoVo;
import com.voc.service.insights.engine.vo.TagLibVo;
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
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/20 下午4:58
 * @描述:
 **/
@RestController
@Tag(name = "标签库", description = "标签库")
@RequestMapping("/insTagLib")
public class InsTagLibController extends AbstractConditionFilters {


    private static final Logger log = LoggerFactory.getLogger(InsTagLibController.class);
    @Autowired
    private IInsTagLibService tagLibService;

    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Object conditions() {
        return Result.OK(async(CollUtil.set(false, LABEL_TYPE, ENERGY, SERIOUSNESS, USER_JOURNEY, STOP_OR_ENABLE, CAR_TYPE, TAG_LIB_ATTRIBUTE, STATUS)));
    }

    @AutoLog(value = "标签库-新增标签信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增标签信息")
    @PostMapping("/saveTagLib")
    Result<?> saveTagLib(@RequestBody InsTagLibModel tagLibModel) {
        try {
            tagLibService.saveTagLib(tagLibModel);
            return Result.OK();
        } catch (Exception e) {
            log.error("标签库-新增标签信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签库-更新标签信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新标签信息")
    @PostMapping("/updateTagLib")
    Result<?> updateTagLib(@RequestBody InsTagLibModel tagLibModel) {
        try {
            tagLibService.updateTagLib(tagLibModel);
            return Result.OK();
        } catch (Exception e) {
            log.error("标签库-更新标签信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签库-分页查询标签信息列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询标签信息列表")
    @PostMapping("/findTagLibList")
    Result<?> findTagLibList(@RequestBody InsTagLibModel tagLibModel) {
        try {
            PageInfo tagLibList = tagLibService.findTagLibList(tagLibModel);
            return Result.OK(tagLibList);
        } catch (Exception e) {
            log.error("标签库-分页查询标签信息列表异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签库-根据id获取标签详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据id获取标签详情")
    @PostMapping("/findTagLib")
    Result<?> findTagLib(@RequestBody InsTagLibModel tagLibModel) {
        try {
            TagLibVo tagLib = tagLibService.findTagLib(tagLibModel);
            return Result.OK(tagLib);
        } catch (Exception e) {
            log.error("标签库-根据id获取标签详情:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "标签库-根据标签分类获取关联项")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据标签分类获取关联项")
    @PostMapping("/findTagLibRelatedItems")
    Result<?> findTagLibRelatedItems(@RequestBody InsTagLibModel tagLibModel) {
        try {
            Map<String, List<DictInfoVo>> tagLibRelatedItems = tagLibService.findTagLibRelatedItems(tagLibModel);
            return Result.OK(tagLibRelatedItems);
        }  catch (Exception e) {
            log.error("标签库-根据标签分类获取关联项异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


}
