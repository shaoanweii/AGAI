package com.voc.service.insights.engine.api.alert.abstracts;

import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import com.voc.service.insights.engine.model.alert.AltAlarmDataModel;
import com.voc.service.insights.engine.model.alert.InsAltDataModel;

import java.util.List;

/**
 * @author lww
 * @since 2024/04/26
 */
public interface IInsAlertBaseService {

    String getDataType();

    boolean execute(AlertTaskModel task);

    List<InsAltDataModel> alertBarChart(AltAlarmDataModel altAlarmDataModel);


}
