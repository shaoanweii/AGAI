package com.voc.service.insights.engine.api.model;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.model.InsModelDescModel;

import java.io.Serializable;
import java.util.List;

/**
 * (InsModelDesc)表服务接口
 *
 * @author leiww
 * @since 2024-02-21 15:32:06
 */
public interface IInsModelDescService {
    /**
     * 分页查询所有数据
     *
     * @param model 查询实体
     * @return 所有数据
     */
    public Result<?> queryBySelect(InsModelDescModel model);

    /**
     * 添加实体数据
     *
     * @param model 添加实体
     * @return 是否成功
     */
    public Boolean insert(InsModelDescModel model);

    /**
     * 修改实体数据
     *
     * @param model 修改实体
     * @return 是否成功
     */
    public Boolean update(InsModelDescModel model);

    /**
     * 删除实体数据
     *
     * @param ids 删除实体ids
     * @return 是否成功
     */
    public Boolean deleteByIds(List<Serializable> ids);

    /**
     * 查询实体数据
     *
     * @param id 查询实体id
     * @return 查询数据
     */
    public InsModelDescModel queryById(Serializable id);

    /**
     * 查询实体数据
     *
     * @param model 查询实体
     * @return 查询数据
     */
    public List<InsModelDescModel> queryByParam(InsModelDescModel model);
}

