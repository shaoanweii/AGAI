package com.voc.service.insights.engine.api.knowledgeBase;


import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.knowledgeBase.InsKnowledgeBaseModel;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseTemplateVo;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseValidateVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 知识库表(InsKnowledgeBase)表服务接口
 *
 * @author makejava
 * @since 2024-09-06 14:51:58
 */
public interface InsKnowledgeBaseService {

    /**
     * 通过ID查询单条数据
     *
     * @param id
     * @return 实例对象
     */
    Result queryById(String id);

    /**
     * 分页查询
     *
     * @param insKnowledgeBase 筛选条件
     * @return 查询结果
     */
    Result queryByPage(InsKnowledgeBaseModel insKnowledgeBase);

    /**
     * 新增数据
     *
     * @param insKnowledgeBase 实例对象
     * @return 实例对象
     */
    Result insert(InsKnowledgeBaseModel insKnowledgeBase);

    /**
     * 修改数据
     *
     * @param insKnowledgeBase 实例对象
     * @return 实例对象
     */
    Result update(InsKnowledgeBaseModel insKnowledgeBase);

    /**
     * 通过主键删除数据
     *
     * @param id
     * @return 是否成功
     */
    Result deleteById(String id);

    InsKnowledgeBaseValidateVo checkUploadDataSource(InsKnowledgeBaseModel knowledgeBaseModel) throws Exception;

    Map<String, Integer> analyzeExcelData(List<InsKnowledgeBaseTemplateVo> list, String batchId, String clientId, AtomicInteger fail, AtomicInteger success, Map<String, Integer> dataSourceMap);

    void saveUploadData(InsKnowledgeBaseModel knowledgeBaseModel) throws Exception;

    Result listSelect(InsKnowledgeBaseModel insKnowledgeBase);
}
