package com.voc.service.insights.engine.api.alert;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import com.voc.service.insights.engine.model.alert.AltAlarmDataModel;
import com.voc.service.insights.engine.model.alert.AltMonitoringDataModel;
import com.voc.service.insights.engine.model.alert.InsAltDataModel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 数据监控-监控数据表(AltMonitoringData)表接口服务层
 *
 * @author leiww
 * @since 2024-04-26 15:11:35
 */
public interface IInsAltMonitoringDataService {

    /**
     * 保存其他数据中获取的数据
     * @param task
     * @param workId
     */
    void save(AltMonitoringDataModel model, AlertTaskModel task, String workId);
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    AltMonitoringDataModel queryById(Serializable id);

    /**
     * 分页查询
     *
     * @param model 筛选条件
     * @return 查询结果
     */
    Result<?> queryBySelect(AltMonitoringDataModel model);

    /**
     * 新增数据
     *
     * @param model 实例对象
     * @return 实例对象
     */
    Boolean insert(AltMonitoringDataModel model);

    /**
     * 修改数据
     *
     * @param model 实例对象
     * @return 实例对象
     */
    Boolean update(AltMonitoringDataModel model);

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
    List<AltMonitoringDataModel> queryByParam(AltMonitoringDataModel model);

    List<AltMonitoringDataModel> findAlertBarChart(AltAlarmDataModel model);

    Map<String, Double> historicalRatioMean(AltAlarmDataModel altAlarmDataModel);

    Map<String, Double> findHistoricalAvg(AltAlarmDataModel altAlarmDataModel);

    List<InsAltDataModel> nlpDataAlertBarChart(AltAlarmDataModel model);

    List<InsAltDataModel> metaDataAlertBarChart(AltAlarmDataModel model);

    List<InsAltDataModel> pushDataAlertBarChart(AltAlarmDataModel model);
}
