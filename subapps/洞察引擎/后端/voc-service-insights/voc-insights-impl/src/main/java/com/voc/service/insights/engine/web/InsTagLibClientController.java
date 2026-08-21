package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsTagLibClientService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsTagLibClientModel;
import com.voc.service.insights.engine.model.InsTopicModel;
import com.voc.service.insights.engine.vo.*;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/20 下午4:58
 * @描述:
 **/
@RestController
@Tag(name = "标签应用", description = "标签应用")
@RequestMapping("/insTagLibClient")
public class InsTagLibClientController extends AbstractConditionFilters {

    private static final Logger log = LoggerFactory.getLogger(InsTagLibClientController.class);
    @Autowired
    private IInsTagLibClientService tagLibClientService;

    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Object conditions() {
        return Result.OK(async(CollUtil.set(false, LABEL_TYPE,STATUS, STOP_OR_ENABLE,EMOTION,INTENTION,CLOSED_RULE_LEVEL,ISSUE_SEVERIT
        ,EVENT_CLARITY,SUSCEPTIVE_TYPE,ACCURACY,BUSINESS_DOMAIN,COMPLAINT_FLAG_NEEDING_REPLY,NEED_FORVCLOSED_LOOP)));
    }

    @AutoLog(value = "标签应用-新增体验代码")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增体验代码")
    @PostMapping("/saveTagLibClient")
    Result<?> saveTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            tagLibClientService.saveTagLibClient(tagLibClientModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("标签应用-新增体验代码异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("标签应用-新增体验代码异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-新增体验代码异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-更新体验代码")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新体验代码")
    @PostMapping("/updateTagLibClient")
    Result<?> updateTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            tagLibClientService.updateTagLibClient(tagLibClientModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("标签应用-更新体验代码异常:", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("标签应用-更新体验代码异常:", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        }  catch (Exception e) {
            log.error("标签应用-更新体验代码异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-根据分类获取客户标签树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据分类获取客户标签树")
    @PostMapping("/findTagLibClientTree")
    Result<?> findTagLibClientTree(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getAppClient()), "应用客户不能为空");
            Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getTagType()), "标签类型不能为空");
            List<TagLibCategoryVo> tagLibClientTree = tagLibClientService.findTagLibClientTree(tagLibClientModel.getAppClient(), tagLibClientModel.getTagType());
            return Result.OK(tagLibClientTree);
        }catch (Exception e) {
            log.error("标签应用-更新客户标签信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-删除客户标签信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除客户标签信息")
    @PostMapping("/deleteTagLibClient")
    Result<?> deleteTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            tagLibClientService.deleteTagLibClient(tagLibClientModel);
            return Result.OK();
        } catch (Exception e) {
            log.error("标签应用-删除客户标签信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-批量删除客户标签信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量删除客户标签信息")
    @PostMapping("/batchDeleteTagLibClient")
    Result<?> batchDeleteTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            tagLibClientService.batchDeleteTagLibClient(tagLibClientModel);
            return Result.OK();
        } catch (Exception e) {
            log.error("标签应用-删除客户标签信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-批量移动客户标签信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量移动客户标签信息")
    @PostMapping("/batchMoveTagLibClient")
    Result<?> batchMoveTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            tagLibClientService.batchMoveTagLibClient(tagLibClientModel);
            return Result.OK();
        } catch (Exception e) {
            log.error("标签应用-删除客户标签信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-批量更新客户标签状态信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量更新客户标签状态信息")
    @PostMapping("/batchUpdateStatusTagLibClient")
    Result<?> batchUpdateStatusTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            tagLibClientService.batchUpdateStatusTagLibClient(tagLibClientModel);
            return Result.OK();
        }  catch (Exception e) {
            log.error("标签应用-删除客户标签信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-批量导出客户标签信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量导出客户标签信息")
    @PostMapping("/batchDownloadTagLibClient")
    Result<?> batchDownloadTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel, HttpServletResponse response) {
        try {
            tagLibClientService.batchDownloadTagLibClient(tagLibClientModel,response);
            return Result.OK();
        }  catch (Exception e) {
            log.error("标签应用-批量导出客户标签信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-获取全部客户末级标签信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取全部客户末级标签信息")
    @PostMapping("/findAllFinalTagLib")
    Result<?> findAllFinalTagLib(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            List<TagLibCategoryVo> allFinalTagLib = tagLibClientService.findAllFinalTagLib(tagLibClientModel);
            return Result.OK(allFinalTagLib);
        } catch (Exception e) {
            log.error("标签应用-删除客户标签信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-分页查询客户标签信息列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询客户标签信息列表")
    @PostMapping("/findTagLibClientList")
    Result<?> findTagLibClientList(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            PageInfo tagLibList = tagLibClientService.findTagLibClientList(tagLibClientModel);
            return Result.OK(tagLibList);
        } catch (Exception e) {
            log.error("标签应用-分页查询客户标签信息列表异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }



    @AutoLog(value = "标签应用-根据id获取标签详情")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据id获取标签详情")
    @PostMapping("/findTagLibClient")
    Result<?> findTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            TagLibClientVo tagLib = tagLibClientService.findTagLibClient(tagLibClientModel);
            return Result.OK(tagLib);
        } catch (Exception e) {
            log.error("标签应用-根据id获取标签详情异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "标签应用-复制标签")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "复制标签")
    @PostMapping("/copyTagLibClient")
    Result<?> copyTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            tagLibClientService.copyTagLibClient(tagLibClientModel);
            return Result.OK();
        } catch (Exception e) {
            log.error("标签应用-复制标签异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-根据标签分类获取关联项")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据标签分类获取关联项")
    @PostMapping("/findTagLibRelatedItems")
    Result<?> findTagLibRelatedItems(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            Map<String, List<DictInfoVo>> tagLibRelatedItems = tagLibClientService.findTagLibRelatedItems(tagLibClientModel);
            return Result.OK(tagLibRelatedItems);
        } catch (Exception e) {
            log.error("标签应用-根据标签分类获取关联项异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-按标签类型获取分类列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "按标签类型获取分类列表")
    @PostMapping("/findCategoryList")
    Result<?> findCategoryList(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            List<TagLibCategoryVo> categoryList = tagLibClientService.findCategoryList(tagLibClientModel);
            return Result.OK(categoryList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-按标签类型获取分类列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), e.getMessage());
        }
    }

    @AutoLog(value = "标签应用-分页获取体验代码列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页获取体验代码列表")
    @PostMapping("/findExperienceCodeList")
    Result<IPage<TagLibClientVo>> findExperienceCodeList(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            IPage<TagLibClientVo> experienceCodeList = tagLibClientService.findExperienceCodeList(tagLibClientModel);
            return Result.OK(experienceCodeList);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-分页获取体验代码列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), e.getMessage());
        }
    }


    @AutoLog(value = "标签应用-查询客户标签分类树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询客户标签分类树")
    @GetMapping("/findTagLibClientCategoryTree")
    Result<?> findTagLibClientCategoryTree(@RequestParam String clientId, @RequestParam(required = false) String tagLibType) {
        try {
            List<TagLibCategoryVo> tagLibClientCategoryTree = tagLibClientService.findTagLibClientCategoryTree(clientId, tagLibType);
            return Result.OK(tagLibClientCategoryTree);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-查询客户标签分类树异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "标签应用-查询客户标签分类树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询客户标签分类树")
    @PostMapping("/findClientCategoryTree")
    Result<?> findClientCategoryTree(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            List<TagLibCategoryVo> tagLibClientCategoryTree = tagLibClientService.findClientCategoryTree(tagLibClientModel);
            return Result.OK(tagLibClientCategoryTree);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-查询客户标签分类树异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-获取标签分类树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取标签分类树")
    @GetMapping("/findCategoryTree")
    Result<?> findCategoryTree(@RequestParam String clientId, @RequestParam(required = false) String tagLibType) {
        try {
            List<TagLibCategoryVo> tagLibClientCategoryTree = tagLibClientService.findTagLibClientCategoryTree(clientId, tagLibType);
            return Result.OK(tagLibClientCategoryTree);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-查询客户标签分类树异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-查询标签树")
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
            log.error("标签应用-查询标签树异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-根据分类查询全部末级标签")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据分类查询全部末级标签")
    @PostMapping("/findAllFinalTagLibClientVoList")
    Result<List<TagLibClientTreeVo>> findTagLibTree(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            List<TagLibClientTreeVo> tagLibClientCategoryTree = tagLibClientService.findAllFinalTagLibClientVoList(tagLibClientModel);
            return Result.OK(tagLibClientCategoryTree);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-查询标签树异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取全部禁用标签")
    @PostMapping("/findAllDisableTagLibClient")
    Result<InsTagLibVo> findAllDisableTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            org.springframework.util.Assert.isTrue(ObjectUtils.isNotEmpty(tagLibClientModel.getAppClient()), "clientId不能为空");
            InsTagLibVo tagLibClientCategoryTree = tagLibClientService.findAllDisableTagLibClient(tagLibClientModel);
            return Result.OK(tagLibClientCategoryTree);
        } catch (Exception e) {
            log.error("标签应用-获取全部禁用标签异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取标签树")
    @PostMapping("/findTagTree")
    Result<List<TagLibCategoryVo>> findTagTree(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            List<TagLibCategoryVo> tagLibClientCategoryTree = tagLibClientService.findTagLibClientTree(tagLibClientModel);
            return Result.OK(tagLibClientCategoryTree);
        } catch (Exception e) {
            log.error("标签应用-获取全部禁用标签异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取标签树")
    @PostMapping("/getTagLibClientTree")
    Result<List<TagLibCategoryVo>> getTagLibClientTree(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            List<TagLibCategoryVo> tagLibClientCategoryTree = tagLibClientService.getTagLibClientTree(tagLibClientModel);
            return Result.OK(tagLibClientCategoryTree);
        } catch (Exception e) {
            log.error("标签应用-获取全部禁用标签异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取标签树")
    @GetMapping("/test")
    Result<?> test(@RequestParam String clientId,@RequestParam String tagType) {
        try {
            tagLibClientService.test(clientId,tagType);
            return Result.OK();
        } catch (Exception e) {
            log.error("标签应用-获取全部禁用标签异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @AutoLog(value = "标签应用-获取已调用的客户标签集")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取已调用的客户标签集")
    @PostMapping("/findCalledTagLibClient")
    Result<?> findCalledTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            List<String> calledTagLibClient = tagLibClientService.findCalledTagLibClient(tagLibClientModel);
            return Result.OK(calledTagLibClient);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.error(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-获取已调用的客户标签集异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "标签应用-获取全部末级标签id")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取全部末级标签id")
    @PostMapping("/findAllTagLibClientIds")
    Result<List<String>> findAllTagLibClientIds(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            List<String> names = tagLibClientService.findAllTagLibClientIds(tagLibClientModel);
            return Result.OK(names);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-获取已调用的客户标签集异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @PostMapping("/uploadExcel")
    public Result<?> uploadExcel(@RequestParam(value = "file") MultipartFile file) {
        tagLibClientService.uploadExcel(file);
        return Result.OK();
    }

//    @AutoLog(value = "标签应用-根据末级标签获取标签树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据末级标签获取标签树")
    @PostMapping("/findUpTagLibHierarchical")
    Result<List<TagLibCategoryVo>> findUpTagLibHierarchical(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            List<TagLibCategoryVo> names = tagLibClientService.findUpTagLibHierarchical(tagLibClientModel);
            return Result.OK(names);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-获取已调用的客户标签集异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据标签id获取全部上级")
    @PostMapping("/findAllUpTagLibHierarchicalByTagId")
    Result<List<TagClientVo>> findAllUpTagLibHierarchicalByTagId(@RequestBody InsTagLibClientModel tagLibClientModel) {
        try {
            List<TagClientVo> names = tagLibClientService.findAllUpTagLibHierarchicalByTagId(tagLibClientModel);
            return Result.OK(names);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-根据标签id获取全部上级异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @AutoLog(value = "标签应用-查询观点操作人列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询观点操作人列表")
    @PostMapping("/findTopicOperatorList")
    Result<List<InsTopicOperatorVo>> findTopicOperatorList(@RequestBody InsTopicModel tagLibClientModel,
                                                           @RequestParam(defaultValue = "true") Boolean isAllVisible) {
        try {
            List<InsTopicOperatorVo> names = tagLibClientService.findTopicOperatorList(tagLibClientModel, isAllVisible);
            return Result.OK(names);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-查询观点操作人列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取观点列表")
    @PostMapping("/findAllTopicList")
    Result< IPage<TopicVo>> findAllTopicList(@RequestBody InsTopicModel tagLibClientModel) {
        try {
            IPage<TopicVo> names = tagLibClientService.findAllTopicList(tagLibClientModel);
            return Result.OK(names);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-获取观点列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取观点名称编码列表")
    @PostMapping("/findTopicList")
    Result<List<TagLibTopicVo>> findTopicList(@RequestBody InsTopicModel tagLibClientModel) {
        try {
            List<TagLibTopicVo> names = tagLibClientService.findTopicList(tagLibClientModel);
            return Result.OK(names);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-获取观点名称编码列表异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量修改观点状态")
    @PostMapping("/batchChangeTopicStatus")
    Result<?> batchChangeTopicStatus(@RequestBody InsTopicModel tagLibClientModel) {
        try {
            tagLibClientService.batchChangeTopicStatus(tagLibClientModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-批量修改观点状态异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "保存观点")
    @PostMapping("/saveTopic")
    Result<?> saveTopic(@RequestBody InsTopicModel tagLibClientModel) {
        try {
            tagLibClientService.saveTopic(tagLibClientModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-保存观点异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量编辑观点")
    @PostMapping("/batchUpdateTopic")
    Result<?> batchUpdateTopic(@RequestBody InsTopicModel tagLibClientModel) {
        try {
            tagLibClientService.batchUpdateTopic(tagLibClientModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-批量编辑观点异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "批量合并观点")
    @PostMapping("/batchMergeTopic")
    Result<?> batchMergeTopic(@RequestBody InsTopicModel tagLibClientModel) {
        try {
            tagLibClientService.batchMergeTopic(tagLibClientModel);
            return Result.OK();
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-批量合并观点异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据编码获取观点")
    @PostMapping("/findTopicByCode")
    Result<InsTopicVo> findTopicByCode(@RequestBody InsTopicModel tagLibClientModel) {
        try {
            InsTopicVo topicByCode = tagLibClientService.findTopicByCode(tagLibClientModel);
            return Result.OK(topicByCode);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("标签应用-根据编码获取观点异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

}
