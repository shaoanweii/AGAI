package com.voc.service.insights.engine.api.data;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.data.InsDataResourceModel;

import java.util.List;

/**
 * (InsDataResource)表接口服务层
 *
 * @author leiww
 * @since 2024-04-02 15:27:05
 */
public interface InsDataResourceService {

    /**
     * 通过ID查询单条数据
     *
     * @param model@return 实例对象
     */
    InsDataResourceModel queryById(InsDataResourceModel model);

    /**
     * 分页查询
     *
     * @param model 分页对象
     * @return 查询结果
     */
    Result<?> queryBySelect(InsDataResourceModel model);

    /**
     * 新增数据
     *
     * @param model 实例对象
     * @return 实例对象
     */
    Boolean insert(InsDataResourceModel model);

    /**
     * 修改数据
     *
     * @param model 实例对象
     * @return 实例对象
     */
    Boolean update(InsDataResourceModel model);

    /**
     * 通过主键删除数据
     *
     * @param model 删除实体ids
     * @return 是否成功
     */
    Boolean deleteByIds(InsDataResourceModel model);

    /**
     * 查询实体数据
     *
     * @param model 查询实体
     * @return 查询数据
     */
    IPage<InsDataResourceModel> queryByParam(InsDataResourceModel model);

    List<InsDataResourceModel> findAllDataResourceList(InsDataResourceModel model);

    /**
     * 查询所有数据
     *
     * @return 查询数据
     */
    List<InsDataResourceModel> listAll();

    /**
     * 根据所属客户获取资源组
     * @param model
     * @return
     */
    List<InsDataResourceModel> findResourceGroupByAppClient(InsDataResourceModel model);

    List<InsDataResourceModel> findAllResourceTree(InsDataResourceModel model);


}
