package com.voc.service.insights.engine.api;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsCarSeriesInfoModel;
import com.voc.service.insights.engine.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IInsCarSeriesInfoService {

    void addInsCarSeriesInfo(InsCarSeriesInfoModel insCarSeriesInfoModel);

    void updateInsCarSeriesInfo(InsCarSeriesInfoModel insCarSeriesInfoModel);

    void delInsCarSeriesInfo(InsCarSeriesInfoModel id);

    List<InsCarSeriesInfoModel> queryByParam(InsCarSeriesInfoModel insCarSeriesInfoModel);

    List<InsCarSeriesInfoModel> findAll();

    Result<?> queryBySelect(InsCarSeriesInfoModel model);

    InsCarSeriesVo findCarSeriesInfo(InsCarSeriesInfoModel model);

    void batchChangeStatus(InsCarSeriesInfoModel model);

    String codeGenerationRules(String pid);

    List<BrandInfoVo> findBrandCarsTree();

    List<BrandInfoVo> findSelfBrandCarsTree();

    List<CarInfoVo> findCarSeriesByIds(InsCarSeriesInfoModel model);

    List<CarSeriesTreeVo> findBrandCarSeriesByCarName(List<String> carName);

    void uploadExcel(MultipartFile file);

    void analyzeExcelData(List<CarSeriesTemplateVo> list);

    /**
     * 获取新车上市车系筛选条件（包含新品车系和对比车系）
     *
     * @return 新车上市车系筛选条件VO
     */
    NewCarSeriesConditionVo getNewCarSeriesCondition();

}