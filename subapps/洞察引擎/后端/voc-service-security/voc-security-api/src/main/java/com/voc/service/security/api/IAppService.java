package com.voc.service.security.api;

import com.voc.service.security.model.AppModel;

import java.util.List;

public interface IAppService {
    AppModel find(AppModel model);

    List<AppModel> findAll();

    void add(AppModel app);

    String getAppIdByURL(String redirect);
}
