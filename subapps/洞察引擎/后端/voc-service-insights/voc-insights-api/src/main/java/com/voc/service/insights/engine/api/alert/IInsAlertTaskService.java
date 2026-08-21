package com.voc.service.insights.engine.api.alert;

import com.voc.service.insights.engine.model.alert.AlertTaskModel;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Title: InsAlertCoreServiceImpl
 * @Package: com.voc.service.insights.engine.alert
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/25 9:13
 * @Version:1.0
 */
public interface IInsAlertTaskService {

    List<AlertTaskModel> findAllEnable();

    List<AlertTaskModel> getAllUnexecutedTasks();

    String recordTask(AlertTaskModel task, LocalDateTime startTime, boolean status);

    void updateRecord(String id, boolean taskStatus);
}
