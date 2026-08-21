package com.voc.service.insights.engine.api.alert;

import com.voc.service.insights.engine.model.alert.AlertTaskModel;

/**
 * @Title: InsAlertCoreServiceImpl
 * @Package: com.voc.service.insights.engine.alert
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/25 9:13
 * @Version:1.0
 */


public interface IInsAlertNotificationService {
    void generate(AlertTaskModel param);

    void pushAlertNotification();

}
