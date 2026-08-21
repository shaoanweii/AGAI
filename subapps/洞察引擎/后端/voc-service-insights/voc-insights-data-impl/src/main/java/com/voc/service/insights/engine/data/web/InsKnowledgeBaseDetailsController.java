package com.voc.service.insights.engine.data.web;

import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.knowledgeBase.InsKnowledgeBaseDetailsService;
import com.voc.service.insights.engine.model.knowledgeBase.InsKnowledgeBaseDetailsModel;
import com.voc.service.insights.engine.model.knowledgeBase.KnowledgeBaseDetailFilterModel;
import com.voc.service.insights.engine.producer.CleanCacheEventProducer;
import com.voc.service.logs.annotation.AutoLog;
import com.voc.service.logs.dto.MessageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 知识库明细表(InsKnowledgeBaseDetails)表控制层
 *
 * @author makejava
 * @since 2024-09-06 14:51:56
 */
@RestController
@Tag(name = "知识库明细", description = "知识库明细")
@RequestMapping("/insKnowledgeBaseDetails")
@Log4j2
public class InsKnowledgeBaseDetailsController {
    /**
     * 服务对象
     */
    @Resource
    private InsKnowledgeBaseDetailsService insKnowledgeBaseDetailsService;
    @Autowired
    CleanCacheEventProducer cleanCacheEventProducer;

    /**
     * 分页查询
     *
     * @param insKnowledgeBaseDetails 筛选条件
     * @return 查询结果
     */
    @AutoLog(value = "知识库数据-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库数据-分页查询")
    @PostMapping("/list")
    public Result queryByPage(@Valid @RequestBody KnowledgeBaseDetailFilterModel insKnowledgeBaseDetails) {
        return insKnowledgeBaseDetailsService.queryByPage(insKnowledgeBaseDetails);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public Result queryById(@PathVariable("id") String id) {
        return insKnowledgeBaseDetailsService.queryById(id);
    }

    /**
     * 新增数据
     *
     * @param insKnowledgeBaseDetails 实体
     * @return 新增结果
     */
    @PostMapping
    public Result add(InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        return insKnowledgeBaseDetailsService.insert(insKnowledgeBaseDetails);
    }

    /**
     * 编辑数据
     *
     * @param insKnowledgeBaseDetails 实体
     * @return 编辑结果
     */
    @AutoLog(value = "知识库数据-编辑数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库数据-编辑数据")
    @PostMapping("edit")
    public Result edit(@RequestBody InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        return insKnowledgeBaseDetailsService.update(insKnowledgeBaseDetails);
    }
    @AutoLog(value = "知识库数据-批量编辑数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库数据-批量编辑数据")
    @PostMapping("batchEdit")
    public Result batchEdit(@RequestBody InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        return insKnowledgeBaseDetailsService.batchEdit(insKnowledgeBaseDetails);
    }
    @AutoLog(value = "知识库数据-数据移动")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库数据-数据移动")
    @PostMapping("batchMove")
    public Result batchMove(@RequestBody InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        return insKnowledgeBaseDetailsService.batchMove(insKnowledgeBaseDetails);
    }
    @AutoLog(value = "知识库数据-数据同步")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库数据-数据同步")
    @PostMapping("batchSynchronous")
    public Result batchSynchronous(@RequestBody InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        return insKnowledgeBaseDetailsService.batchSynchronous(insKnowledgeBaseDetails);
    }

    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除是否成功
     */
    @AutoLog(value = "知识库数据-删除")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库数据-删除")
    @PostMapping("delete")
    public Result deleteById(@RequestBody InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        String id = insKnowledgeBaseDetails.getId();
        try {
            cleanCacheEventProducer.pushEvent(MessageDTO.builder().type("opinion").data(true).build());
            return insKnowledgeBaseDetailsService.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("知识库数据-删除异常:{}",e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "知识库数据-批量删除")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库数据-批量删除")
    @PostMapping("batchDelete")
    public Result batchDelete(@RequestBody InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails) {
        List<String> ids = insKnowledgeBaseDetails.getIds();
        try {
            insKnowledgeBaseDetailsService.batchDelete(ids);
            cleanCacheEventProducer.pushEvent(MessageDTO.builder().type("opinion").data(true).build());
        } catch (Exception e) {
            e.printStackTrace();
            Result.error("删除失败"+e.getMessage());
        }
        return Result.OK("删除成功");

    }


    @AutoLog(value = "知识库数据-导出知识库数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出知识库数据")
    @PostMapping("/knowledgeBaseDetailsExport")
    public void knowledgeBaseDetailsExport(@Valid @RequestBody KnowledgeBaseDetailFilterModel insKnowledgeBaseDetails, HttpServletResponse response) {
        try {
            insKnowledgeBaseDetailsService.knowledgeBaseDetailsExport(insKnowledgeBaseDetails,response);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }


    @AutoLog(value = "知识库数据-下载模版")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库数据-下载模版")
    @GetMapping("/downloadKnowledgeBase")
    public Result<?> downloadKnowledgeBase(HttpServletResponse response,@RequestParam(value = "fileName") String fileName) {
        try {
            insKnowledgeBaseDetailsService.downloadKnowledgeBase(response, fileName);
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("下载模版异常:{}",e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

}

