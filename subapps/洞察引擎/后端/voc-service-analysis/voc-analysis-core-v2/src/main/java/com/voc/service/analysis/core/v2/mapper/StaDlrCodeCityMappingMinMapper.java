package com.voc.service.analysis.core.v2.mapper;

import com.voc.service.analysis.model.CityCodeModel;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface StaDlrCodeCityMappingMinMapper {


    List<CityCodeModel> getCityCodeList();
}
