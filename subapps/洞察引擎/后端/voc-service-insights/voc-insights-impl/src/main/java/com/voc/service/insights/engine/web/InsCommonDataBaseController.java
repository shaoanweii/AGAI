package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsTagLibClientService;
import com.voc.service.insights.engine.api.InsCommonDataBaseService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsCommonDataBaseModel;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
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
 * @创建时间: 2024/8/28 上午9:07
 * @描述:
 **/
@Tag(name = "公域数据库")
@RestController
@RequestMapping("/commonDataBase")
public class InsCommonDataBaseController  extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsCommonDataBaseController.class);
    @Autowired
    InsCommonDataBaseService commonDataBaseService;

    @Autowired
    private IInsTagLibClientService tagLibClientService;

    @Override
    @GetMapping("/conditions")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, BRAND_CAR,META_DATA_TYPE,EMOTION,INTENTION,LABEL_TYPE)));
    }


    @AutoLog(value = "公域数据库-获取数据列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取数据列表")
    @PostMapping("/getDataList")
    public Result<?> getDataList(@RequestBody InsCommonDataBaseModel commonDataBaseModel) {
        try {
            PageInfo rawData = commonDataBaseService.getCommonDataList(commonDataBaseModel);
            return Result.OK(rawData);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


    @AutoLog(value = "公域数据库-查询标签树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询标签树")
    @GetMapping("/findTagLibTree")
    Result<?> findTagLibTree(@RequestParam(required = false) String tagLibType) {
        try {
            List<TagLibCategoryVo> tagLibClientCategoryTree = tagLibClientService.findTagLibTree(tagLibType);
            return Result.OK(tagLibClientCategoryTree);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("公域数据库-查询标签树异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }
}
