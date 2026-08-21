package com.voc.service.analysis.core.v2.impl;

import com.voc.service.analysis.api.IStaDlrCodeCityMappingMinService;
import com.voc.service.analysis.core.v2.mapper.StaDlrCodeCityMappingMinMapper;
import com.voc.service.analysis.model.CityCodeModel;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class StaDlrCodeCityMappingMinServiceImpl implements IStaDlrCodeCityMappingMinService {

    @Resource
    private StaDlrCodeCityMappingMinMapper staDlrCodeCityMappingMinMapper;


    @Override
    @SwitchClientDS
    public List<CityCodeModel> getCityCodeList(String clientId) {
        return staDlrCodeCityMappingMinMapper.getCityCodeList();
    }
}
