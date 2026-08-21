package com.voc.service.insights.engine.api;


import com.voc.service.insights.engine.model.InsCustomersSynchronizeModel;

public interface IInsCustomersSynchronizeService {

    Boolean syncCustomersInfo(InsCustomersSynchronizeModel model);
}
