package com.voc.service.insights.engine.api.data;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.data.InsDataExpectModel;

import java.io.Serializable;
import java.util.List;

/**
 * 语料库数据集(InsDataExpect)表服务接口
 *
 * @author leiww
 * @since 2024-03-05 14:44:43
 */
public interface IInsDataExpectService {
    /**
     * 分页查询所有数据
     *
     * @param model 查询实体
     * @return 所有数据
     */
    Result<?> queryBySelect(InsDataExpectModel model);

    /**
     * 添加实体数据
     *
     * @param model 添加实体
     * @return 是否成功
     */
    Boolean insert(InsDataExpectModel model);

    /**
     * 修改实体数据
     *
     * @param model 修改实体
     * @return 是否成功
     */
    Boolean update(InsDataExpectModel model);

    /**
     * 删除实体数据
     *
     * @param ids 删除实体ids
     * @return 是否成功
     */
    Boolean deleteByIds(List<Serializable> ids);

    /**
     * 查询实体数据
     *
     * @param id 查询实体id
     * @return 查询数据
     */
    InsDataExpectModel queryById(Serializable id);

    /**
     * 查询实体数据
     *
     * @param model 查询实体
     * @return 查询数据
     */
    List<InsDataExpectModel> queryByParam(InsDataExpectModel model);

    /**
     * 统计所有预料库总数据
     * @param insDataExpect
     * @return
     */
    Integer countBySelect(InsDataExpectModel insDataExpect);
}

