package com.voc.service.insights.engine.api;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsBrandInfoModel;
import com.voc.service.insights.engine.vo.BrandInfoVo;
import com.voc.service.insights.engine.vo.InsALlBrandAndCarSeriesVo;
import com.voc.service.insights.engine.vo.InsBrandInfoVo;

import java.io.Serializable;
import java.util.List;

public interface IInsBrandInfoService {

    void addInsBrandInfo(InsBrandInfoModel insBrandInfoModel);

    void updateInsBrandInfo(InsBrandInfoModel insBrandInfoModel);

    void delInsBrandInfo(InsBrandInfoModel model);

    List<InsBrandInfoVo> queryByParam(InsBrandInfoModel insBrandInfoModel);

    List<InsBrandInfoModel> findAll();

    /**
     * 查询品牌信息
     */
    InsBrandInfoVo findInsBrandInfo(InsBrandInfoModel insBrandInfoModel);

    /**
     * 查询本品品牌
     *
     * @return 品牌列表
     */
    List<InsBrandInfoModel> findSelfBrand();

    public Result<?> queryBySelect(InsBrandInfoModel model);

    /**
     * 批量修改状态
     * @param model
     */
    void batchChangeStatus(InsBrandInfoModel model);


    InsBrandInfoModel queryByCode(Serializable id);

    String codeGenerationRules();

    List<InsALlBrandAndCarSeriesVo> findAllBrandAndCarSeries();
    BrandInfoVo findBrandByBrandName(String brand,Boolean isCompetitiveProduct);

    String getSelfBrandCodes();
}