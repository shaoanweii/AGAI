package com.voc.service.insights.engine.api.data;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import com.voc.service.insights.engine.vo.AysCqCaMetaDataAnalysisVo;
import com.voc.service.insights.engine.vo.BaseCarSeriesDataVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;


public interface IInsCqCaDataSourceService {


    PageInfo getRawData(InsCqCaDataQueryModel InsCqCaDataQueryModel);


    PageInfo getResultData(InsCqCaDataQueryModel InsCqCaDataQueryModel);

    /**
     * 获取情感分析结果数据
     *
     * @param insCqCaDataQueryModel 查询条件
     * @return 情感分析结果数据分页列表
     */
    PageInfo getSentimentResultData(InsCqCaDataQueryModel insCqCaDataQueryModel);

    IPage<AysCqCaMetaDataAnalysisVo> getRawDataDetail(InsCqCaDataQueryModel InsCqCaDataQueryModel);

    List<BaseCarSeriesDataVo> queryCarSeriesList(InsCqCaDataQueryModel InsCqCaDataQueryModel);

    List<BaseCarSeriesDataVo> queryBrandList(InsCqCaDataQueryModel InsCqCaDataQueryModel);

    List<BaseCarSeriesDataVo> findAllFinalTagLibClientVoList(InsCqCaDataQueryModel InsCqCaDataQueryModel);

    Boolean exportRawData(InsCqCaDataQueryModel InsCqCaDataQueryModel, HttpServletResponse response);

    Boolean exportRawDataResult(InsCqCaDataQueryModel InsCqCaDataQueryModel, HttpServletResponse response);

}
