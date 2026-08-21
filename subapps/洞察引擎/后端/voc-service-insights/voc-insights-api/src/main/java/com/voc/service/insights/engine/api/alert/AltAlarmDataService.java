package com.voc.service.insights.engine.api.alert;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.alert.AltAlarmDataDto;
import com.voc.service.insights.engine.model.alert.AltAlarmDataModel;

import java.io.Serializable;
import java.util.List;

/**
 * 数据监控-告警数据表(AltCoreData)表接口服务层
 *
 * @author leiww
 * @since 2024-04-26 10:42:23
 */
public interface AltAlarmDataService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    AltAlarmDataModel queryById(Serializable id);

    /**
     * 分页查询
     *
     * @param model 筛选条件
     * @return 查询结果
     */
    Result<?> queryBySelect(AltAlarmDataModel model);

    /**
     * 新增数据
     *
     * @param model 实例对象
     * @return 实例对象
     */
    Boolean insert(AltAlarmDataModel model);

    /**
     * 修改数据
     *
     * @param model 实例对象
     * @return 实例对象
     */
    Boolean update(AltAlarmDataModel model);


    Boolean updateBatchById(List<AltAlarmDataModel> model);

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
    List<AltAlarmDataModel> queryByParam(AltAlarmDataModel model);

    List<AltAlarmDataDto> alertBarChart(String code);

    List<AltAlarmDataModel> queryByParamAndPeriod(AltAlarmDataModel model, String period);
}
