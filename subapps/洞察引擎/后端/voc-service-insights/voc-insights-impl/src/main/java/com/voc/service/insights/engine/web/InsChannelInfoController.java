package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsChannelInfoService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.vo.ChannelInfoVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 15:31
 * @描述:
 **/
@Tag(name = "渠道信息")
@RestController
@RequestMapping("/channel")
public class InsChannelInfoController extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsChannelInfoController.class);
    @Autowired
    IInsChannelInfoService channelInfoService;


    /**
     * @return com.voc.service.common.response.Result<?>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/22 08:58
     * @描述 查询条件
     **/
    @Override
    @GetMapping("/conditions")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, STATUS, STOP_OR_ENABLE)));
    }

//    @AutoLog(value = "渠道信息-获取渠道数")
//    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
//    @Operation(summary = "获取渠道数")
//    @GetMapping("/findChannelTree")
//    Result<List<ChannelInfoVo>> findChannelTree(){
//        List<ChannelInfoVo> channelDistributionTree = channelInfoService.findChannelInfoTree();
//        return Result.OK(channelDistributionTree);
//    }

    @AutoLog(value = "渠道信息-获取渠道数")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取渠道数")
    @PostMapping("/findAllChannelInfo")
    Result<List<ChannelInfoVo>> findAllChannelInfo(@RequestBody InsChannelInfoModel insChannelInfoModel) {
        List<ChannelInfoVo> channelDistributionTree = channelInfoService.findAllChannelInfo(insChannelInfoModel);
        return Result.OK(channelDistributionTree);
    }




    @AutoLog(value = "渠道信息-获取渠道数")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取渠道数")
    @PostMapping("/findAll")
    Result<List<ChannelInfoVo>> findAll(@RequestBody InsChannelInfoModel insChannelInfoModel) {
        List<ChannelInfoVo> channelDistributionTree = channelInfoService.findAllChannelInfo(insChannelInfoModel);
        return Result.OK(channelDistributionTree);
    }


    @AutoLog(value = "渠道信息-新增渠道信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增渠道信息")
    @PostMapping("/saveChannel")
    Result<?> saveChannel(@RequestBody InsChannelInfoModel insChannelInfoModel) {
        try {
            channelInfoService.saveChannel(insChannelInfoModel);
            return Result.OK();
        }  catch (Exception e) {
            log.error("渠道信息-新增渠道信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "渠道信息-更新渠道信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "更新渠道信息")
    @PostMapping("/updateChannel")
    Result<?> updateChannel(@RequestBody InsChannelInfoModel insChannelInfoModel) {
        try {
            channelInfoService.updateChannel(insChannelInfoModel);
            return Result.OK();
        }  catch (Exception e) {
            log.error("渠道信息-更新渠道信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "渠道信息-获取渠道分类树")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取渠道分类树")
    @PostMapping("/findChannelCategoryTree")
    Result<?> findChannelCategoryTree(@RequestBody InsChannelInfoModel insChannelInfoModel) {
        try {
            List<ChannelInfoVo> channelCategoryTree = channelInfoService.findChannelCategoryTree(insChannelInfoModel);
            return Result.OK(channelCategoryTree);
        }catch (Exception e) {
            log.error("渠道信息-获取渠道分类树异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "渠道信息-根据父级id分页获取渠道列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据父级id分页获取渠道列表")
    @PostMapping("/findChannelInfoByParentId")
    Result<?> findChannelInfoByParentId(@RequestBody InsChannelInfoModel insChannelInfoModel) {
        try {
            PageInfo channelInfo = channelInfoService.findChannelInfoByParentId(insChannelInfoModel);
            return Result.OK(channelInfo);
        } catch (Exception e) {
            log.error("渠道信息-根据父级id分页获取渠道列表异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "渠道信息-删除渠道分类及其下级所有分类")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "删除渠道分类及其下级所有分类")
    @PostMapping("/deleteChannel")
    Result<?> deleteChannel(@RequestBody InsChannelInfoModel insChannelInfoModel) {
        try {
            channelInfoService.deleteChannel(insChannelInfoModel);
            return Result.OK();
        }  catch (Exception e) {
            log.error("渠道信息-删除渠道分类及其下级所有分类异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

//    @AutoLog(value = "渠道信息-根据下级渠道id查找上级渠道")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "根据下级渠道id查找上级渠道")
    @PostMapping("/findChannelByIds")
    Result<List<ChannelInfoVo>> upwardFindChannelHierarchical(@RequestBody InsChannelInfoModel insChannelInfoModel) {
        try {
            Assert.isTrue(ObjectUtils.isNotEmpty(insChannelInfoModel.getChannelIds()), "渠道id不允许为空");
            List<ChannelInfoVo> channelInfoVos = channelInfoService.upwardFindChannelHierarchicalTree(insChannelInfoModel);
            return Result.OK(channelInfoVos);
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("", illegalArgumentException);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), illegalArgumentException.getMessage());
        } catch (BussinessException bussinessException) {
            log.error("", bussinessException);
            return Result.errors(bussinessException.getCode(), bussinessException.getMessage());
        } catch (Exception e) {
            log.error("渠道信息-根据下级渠道id查找上级渠道异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getCode(), e.getMessage());
        }
    }

    @PostMapping("/uploadExcel")
    public Result<?> uploadExcel(@RequestParam(value = "file") MultipartFile file) {
        channelInfoService.uploadExcel(file);
        return Result.OK();
    }


}
