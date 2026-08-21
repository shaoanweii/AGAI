package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsTagClientService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsTagClientBatchModel;
import com.voc.service.insights.engine.model.InsTagClientModel;
import com.voc.service.insights.engine.model.InsTagInfoQueryModel;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * @author zhongxl
 * @version 1.0.0
 * @ClassName InsTagClientController
 * @Description
 * @createTime 2024年2月28日 17:18
 * @Copyright futong
 */
@RestController
@Tag(name = "标签应用", description = "标签应用")
@RequestMapping("/insClientInfo")
public class InsTagClientController extends AbstractConditionFilters {

    private static final Logger log = LoggerFactory.getLogger(InsTagClientController.class);
    @Resource
    private IInsTagClientService insTagClientService;
    @Autowired
    private CompositeMeterRegistryAutoConfiguration compositeMeterRegistryAutoConfiguration;

    @AutoLog(value = "标签应用数据-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询")
    @PostMapping("/queryBySelect")
    public Result queryBySelect(@RequestBody InsTagInfoQueryModel insTagInfo) {
        return Result.OK(this.insTagClientService.queryInsClientInfo(insTagInfo));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @AutoLog(value = "标签应用数据-获取详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result selectOne(@PathVariable Serializable id) {
        return Result.OK(this.insTagClientService.queryVoById(id));
    }

    /**
     * 新增数据
     *
     * @param insTagClientModel 实体对象
     * @return 新增结果
     */
    @AutoLog(value = "标签应用数据-新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增数据")
    @PostMapping("/insert")
    public Result insert(@RequestBody InsTagClientModel insTagClientModel) {
        try {
            insTagClientService.insert(insTagClientModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("InsTagClientModel {}", insTagClientModel);
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
    @AutoLog(value = "标签应用数据-批量新增数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量新增数据")
    @PostMapping("/insertBatch")
    public Result insertBatch(@RequestBody InsTagClientBatchModel model) {
        try {
            insTagClientService.insertBatch(model);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("InsTagClientBatchModel {}", model);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
        return Result.OK("OK");
    }

    /**
     * 修改数据
     *
     * @param insTagClientModel 实体对象
     * @return 修改结果
     */
    @AutoLog(value = "标签应用数据-修改数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "修改数据")
    @PatchMapping("/update")
    public Result update(@RequestBody InsTagClientModel insTagClientModel) {
        try {
            insTagClientService.update(insTagClientModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("InsTagClientModel {}", insTagClientModel);
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
    @AutoLog(value = "标签应用数据-删除数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除数据")
    @PostMapping("/delete")
    public Result delete(@RequestBody List<Serializable> idList) {
        try {
            insTagClientService.deleteByIds(idList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("idList {}", idList);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
        return Result.OK("OK");
    }

    @AutoLog(value = "标签应用数据-四级码框")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "四级码框")
    @PostMapping("/queryTagClientTree")
    public Result queryTagClientTree(@RequestBody InsTagInfoQueryModel insTagInfo) {
        return Result.OK(this.insTagClientService.queryTagClientTree(insTagInfo));
    }

    @Override
    @Operation(summary = "过滤条件")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, STATUS, SOURCE)));
    }
}
