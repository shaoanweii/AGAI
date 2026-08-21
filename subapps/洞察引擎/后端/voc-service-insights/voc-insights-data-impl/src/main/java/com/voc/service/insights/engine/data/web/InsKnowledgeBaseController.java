package com.voc.service.insights.engine.data.web;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.model.UploadModel;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsTagLibClientService;
import com.voc.service.insights.engine.api.data.IInsDataSourceService;
import com.voc.service.insights.engine.api.knowledgeBase.InsKnowledgeBaseService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.knowledgeBase.InsKnowledgeBaseModel;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseValidateVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * 知识库表(InsKnowledgeBase)表控制层
 *
 * @author makejava
 * @since 2024-09-06 14:51:57
 */
@RestController
@Tag(name = "知识库", description = "知识库")
@RequestMapping("insKnowledgeBase")
public class InsKnowledgeBaseController extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsKnowledgeBaseController.class);
    /**
     * 服务对象
     */
    @Resource
    private InsKnowledgeBaseService insKnowledgeBaseService;
    @Autowired
    private IInsTagLibClientService tagLibClientService;
    @Resource
    private IInsDataSourceService insDataSourceService;

    @Override
    @GetMapping("/conditions")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库-查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false,EMOTION,INTENTION,LABEL_TYPE)));
    }

    @AutoLog(value = "知识库-查询标签树")
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
            log.error("知识库-查询标签树异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "知识库-本地文件上传")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "本地文件上传")
    @PostMapping("/uploadData")
    public Result<?> uploadDataSource(@RequestParam(value = "file") MultipartFile file) {
        try {
            UploadModel uploadModel = insDataSourceService.uploadDataSource(file);
            return Result.OK(uploadModel);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("本地文件上传异常:{}",e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "知识库-数据校验")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "数据校验")
    @PostMapping("/checkUploadData")
    public Result<?> checkUploadDataSource(@RequestBody InsKnowledgeBaseModel knowledgeBaseModel) {
        try {
            InsKnowledgeBaseValidateVo knowledgeBaseValidateVo = insKnowledgeBaseService.checkUploadDataSource(knowledgeBaseModel);
            return Result.OK(knowledgeBaseValidateVo);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("数据校验异常:{}",e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    @AutoLog(value = "知识库-本地上传数据保存")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "本地上传数据保存")
    @PostMapping("/saveUploadData")
    public Result<?> saveUploadDataSource(@RequestBody InsKnowledgeBaseModel knowledgeBaseModel) {
        try {
            insKnowledgeBaseService.saveUploadData(knowledgeBaseModel);
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("本地上传数据保存异常:{}",e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


    /**
     * 分页查询
     *
     * @param insKnowledgeBase 筛选条件
     * @return 查询结果
     */
    @AutoLog(value = "知识库-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库-分页查询")
    @PostMapping("/list")
    public Result queryByPage(@Valid @RequestBody InsKnowledgeBaseModel insKnowledgeBase) {
        return this.insKnowledgeBaseService.queryByPage(insKnowledgeBase);
    }
    @AutoLog(value = "知识库-查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库-查询")
    @PostMapping("/listSelect")
    public Result listSelect(@Valid @RequestBody InsKnowledgeBaseModel insKnowledgeBase) {
        return this.insKnowledgeBaseService.listSelect(insKnowledgeBase);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public Result queryById(@PathVariable("id") String id) {
        return insKnowledgeBaseService.queryById(id);
    }

    /**
     * 新增数据
     *
     * @param insKnowledgeBase 实体
     * @return 新增结果
     */
    @AutoLog(value = "知识库-添加")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库-添加")
    @PostMapping("add")
    public Result add(@RequestBody InsKnowledgeBaseModel insKnowledgeBase) {
        return insKnowledgeBaseService.insert(insKnowledgeBase);
    }

    /**
     * 编辑数据
     *
     * @param insKnowledgeBase 实体
     * @return 编辑结果
     */
    @AutoLog(value = "知识库-编辑")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库-编辑")
    @PostMapping("edit")
    public Result edit(@RequestBody InsKnowledgeBaseModel insKnowledgeBase) {
        return insKnowledgeBaseService.update(insKnowledgeBase);
    }

    /**
     * 删除数据
     *
     * @return 删除是否成功
     */
    @AutoLog(value = "知识库-删除")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "知识库-删除")
    @PostMapping("delete")
    public Result deleteById(@RequestBody InsKnowledgeBaseModel insKnowledgeBase) {
        String id = insKnowledgeBase.getId();
        return insKnowledgeBaseService.deleteById(id);
    }

}

