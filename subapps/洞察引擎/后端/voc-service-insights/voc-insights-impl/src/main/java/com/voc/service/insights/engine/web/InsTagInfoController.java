package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsTagInfoService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsTagInfoBatchModel;
import com.voc.service.insights.engine.model.InsTagInfoModel;
import com.voc.service.insights.engine.model.InsTagInfoQueryModel;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * @author zhongxl
 * @version 1.0.0
 * @ClassName InsTagInfoController
 * @Description
 * @createTime 2024年2月22日 10:18
 * @Copyright futong
 */
@RestController
@Tag(name = "标签库数据", description = "标签库数据")
@RequestMapping("/insTagInfo")
public class InsTagInfoController extends AbstractConditionFilters {

    private static final Logger log = LoggerFactory.getLogger(InsTagInfoController.class);
    @Resource
    private IInsTagInfoService insTagInfoService;

    @AutoLog(value = "标签数据-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询")
    @PostMapping("/selectInsTagInfo")
    public Result selectInsTagInfo(@RequestBody InsTagInfoQueryModel instagInfo) {
        return this.insTagInfoService.queryInsTagInfo(instagInfo);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "标签数据-获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result selectOne(@PathVariable Serializable id) {
        return Result.OK(this.insTagInfoService.queryVoById(id));
    }

    /**
     * 新增数据
     *
     * @param insTagInfoModel 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "标签数据-新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增数据")
    @PostMapping("/insert")
    public Result insert(@RequestBody InsTagInfoModel insTagInfoModel) {
        try {
            insTagInfoService.insert(insTagInfoModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("InsTagInfoModel {}", insTagInfoModel);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
        return Result.OK("OK");
    }

    /**
     * 新增数据
     *
     * @param model 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "标签数据-批量新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量新增数据")
    @PostMapping("/insertBatch")
    public Result insertBatch(@RequestBody InsTagInfoBatchModel model) {
        try {
            insTagInfoService.insertBatch(model);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("InsTagInfoBatchModel {}", model);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
        return Result.OK("OK");
    }

    /**
     * 修改数据
     *
     * @param insTagInfoModel 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "标签数据-修改数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "修改数据")
    @PatchMapping("/update")
    public Result update(@RequestBody InsTagInfoModel insTagInfoModel) {
        try {
            insTagInfoService.update(insTagInfoModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("InsTagInfoModel {}", insTagInfoModel);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
        return Result.OK("OK");
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @AutoLog(value = "标签数据-删除数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除数据")
    @PostMapping("/delete")
    public Result delete(@RequestBody List<Serializable> idList) {
        try {
            insTagInfoService.deleteByIds(idList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("idList {}", idList);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
        return Result.OK("OK");
    }

    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, STATUS, SERIOUSNESS, SOURCE)));
    }

//    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
//    @PostMapping("/tagConvert")
//    public void tagConvert(@RequestParam(value = "file") MultipartFile file,@RequestParam(value = "tagType")String tagType, HttpServletResponse response){
//        insTagInfoService.tagConvert(file, response,tagType);
//    }
}
