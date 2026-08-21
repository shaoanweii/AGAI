package com.voc.service.insights.engine.api.knowledgeBase;


import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.knowledgeBase.InsKnowledgeBaseDetailsModel;
import com.voc.service.insights.engine.model.knowledgeBase.KnowledgeBaseDetailFilterModel;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 知识库明细表(InsKnowledgeBaseDetailsModel)表服务接口
 *
 * @author makejava
 * @since 2024-09-06 14:51:56
 */
public interface InsKnowledgeBaseDetailsService {

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
     * @param insKnowledgeBaseDetails 筛选条件
     * @return 查询结果
     */
    Result queryByPage(KnowledgeBaseDetailFilterModel insKnowledgeBaseDetails);

    /**
     * 新增数据
     *
     * @param insKnowledgeBaseDetails 实例对象
     * @return 实例对象
     */
    Result insert(InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails);

    /**
     * 修改数据
     *
     * @param insKnowledgeBaseDetails 实例对象
     * @return 实例对象
     */
    Result update(InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails);

    /**
     * 通过主键删除数据
     *
     * @param id
     * @return 是否成功
     */
    Result deleteById(String id);

    Result batchMove(InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails);

    Result batchSynchronous(InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails);

    Result batchEdit(InsKnowledgeBaseDetailsModel insKnowledgeBaseDetails);

    void knowledgeBaseDetailsExport(KnowledgeBaseDetailFilterModel insKnowledgeBaseDetails, HttpServletResponse response);

    void downloadKnowledgeBase(HttpServletResponse response, String fileName);

    void batchDelete(List<String> ids);
}
