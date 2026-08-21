package com.voc.service.analysis.api;

import com.voc.service.analysis.model.CityCodeModel;

import java.util.List;

public interface IStaDlrCodeCityMappingMinService {


    List<CityCodeModel> getCityCodeList(String clientId);
}
