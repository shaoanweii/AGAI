package com.voc.service.insights.engine.api.data;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.data.InsDataExpectDescModel;

import java.io.Serializable;
import java.util.List;

/**
 * 语料库数据详情(InsDataExpectDesc)表服务接口
 *
 * @author leiww
 * @since 2024-03-05 14:51:15
 */
public interface IInsDataExpectDescService {
    /**
     * 分页查询所有数据
     *
     * @param model 查询实体
     * @return 所有数据
     */
    Result<?> queryBySelect(InsDataExpectDescModel model);

    /**
     * 添加实体数据
     *
     * @param model 添加实体
     * @return 是否成功
     */
    Boolean insert(InsDataExpectDescModel model);

    /**
     * 修改实体数据
     *
     * @param model 修改实体
     * @return 是否成功
     */
    Boolean update(InsDataExpectDescModel model);

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
    InsDataExpectDescModel queryById(Serializable id);

    /**
     * 查询实体数据
     *
     * @param model 查询实体
     * @return 查询数据
     */
    List<InsDataExpectDescModel> queryByParam(InsDataExpectDescModel model);
}

