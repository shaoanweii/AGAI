package com.voc.service.insights.engine.api.alert;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import com.voc.service.insights.engine.model.alert.AltTaskConfigDataModel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据监控-任务配置表(AltTaskConfigData)表接口服务层
 *
 * @author leiww
 * @since 2024-04-30 17:11:56
 */
public interface AltTaskConfigDataService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    AltTaskConfigDataModel queryById(Serializable id);

    /**
     * 分页查询
     *
     * @param model 筛选条件
     * @return 查询结果
     */
    Result<?> queryBySelect(AltTaskConfigDataModel model);

    /**
     * 新增数据
     *
     * @param model 实例对象
     * @return 实例对象
     */
    Boolean insert(AltTaskConfigDataModel model);

    /**
     * 修改数据
     *
     * @param model 实例对象
     * @return 实例对象
     */
    Boolean update(AltTaskConfigDataModel model);

    /**
     * 通过主键删除数据
     *
     * @param ids 删除实体ids
     * @return 是否成功
     */
    Boolean deleteByIds(List<Serializable> ids);

    /**
     * 查询实体数据
     *
     * @param model 查询实体
     * @return 查询数据
     */
    List<AltTaskConfigDataModel> queryByParam(AltTaskConfigDataModel model);

    /**
     * 查询实体数据
     *
     * @param ids 根据多个id查询
     * @return 查询数据
     */
    Map<String, AltTaskConfigDataModel> queryIds(Set<String> ids);

    List<AlertTaskModel> findAllEnable();

}
