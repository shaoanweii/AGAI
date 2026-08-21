package com.voc.service.insights.engine.api.model;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.model.InsModelInfoModel;

import java.io.Serializable;
import java.util.List;

/**
 * 模型配置数据(InsModelInfo)表服务接口
 *
 * @author leiww
 * @since 2024-02-21 14:57:09
 */
public interface IInsModelInfoService {
    /**
     * 分页查询所有数据
     *
     * @param model 查询实体
     * @return 所有数据
     */
    public Result<?> queryBySelect(InsModelInfoModel model);

    /**
     * 添加实体数据
     *
     * @param model 添加实体
     * @return 是否成功
     */
    public Boolean insert(InsModelInfoModel model);

    /**
     * 修改实体数据
     *
     * @param model 修改实体
     * @return 是否成功
     */
    public Boolean update(InsModelInfoModel model);

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
    public InsModelInfoModel queryById(Serializable id);

    /**
     * 查询实体数据
     *
     * @param model 查询实体
     * @return 查询数据
     */
    public List<InsModelInfoModel> queryByParam(InsModelInfoModel model);
}

