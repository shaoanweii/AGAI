package com.voc.service.insights.engine.api;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsProvinceAreaModel;
import com.voc.service.insights.engine.model.InsProvinceArea;
import java.io.Serializable;
import java.util.List;

/**
 * 区域城市信息表(InsProvinceArea)表服务接口
 *
 * @author leiww
 * @since 2024-01-25 13:56:33
 */
public interface IInsProvinceAreaService {
    /**
     * 分页查询所有数据
     *
     * @param model 查询实体
     * @return 所有数据
     */
    public Result<?> queryBySelect(InsProvinceAreaModel model);

    /**
     * 添加实体数据
     *
     * @param model 添加实体
     * @return 是否成功
     */
    public Boolean insert(InsProvinceAreaModel model);

    /**
     * 修改实体数据
     *
     * @param model 修改实体
     * @return 是否成功
     */
    public Boolean update(InsProvinceAreaModel model);

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
    public InsProvinceAreaModel queryById(Serializable id);

    /**
     * 查询实体数据
     *
     * @param model 查询实体
     * @return 查询数据
     */
    public List<InsProvinceAreaModel> queryByParam(InsProvinceAreaModel model);
    
    /**
     * 获取省份列表
     * 
     * @return 省份列表
     */
    public List<InsProvinceArea> getProvinceList();
}

