package com.voc.service.security.api;

import com.voc.service.security.model.AppModel;
import com.voc.service.security.model.LoginHistroyModel;

import java.util.List;

public interface ILoginHistoryService {

    void add(LoginHistroyModel model);

    void addAsync(LoginHistroyModel model);
}
