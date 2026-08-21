package com.voc.service.insights.engine.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.voc.service.insights.engine.api.IInsCarSeriesInfoService;
import com.voc.service.insights.engine.vo.CarSeriesTemplateVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/11/14 下午5:03
 * @描述:
 **/
public class CarSeriesExcelListener extends AnalysisEventListener<CarSeriesTemplateVo> {

    private static final int BATCH_COUNT = 1000;
    private static final Logger log = LoggerFactory.getLogger(CarSeriesExcelListener.class);
    private List<CarSeriesTemplateVo> list = new ArrayList<>();
    IInsCarSeriesInfoService tagLibClientService;

    public CarSeriesExcelListener(IInsCarSeriesInfoService tagLibClientService) {
        this.tagLibClientService = tagLibClientService;
    }

    @Override
    public void invoke(CarSeriesTemplateVo goods, AnalysisContext analysisContext) {
        log.debug("解析到一条数据:========================" + goods.toString());
        // 数据存储到datas，供批量处理，或后续自己业务逻辑处理。
        list.add(goods);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT) {
            // 存储完成清理datas
            saveData();
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //确保所有数据都能入库
        saveData();
        log.info("所有数据解析完成!");
    }

    private void saveData() {
        tagLibClientService.analyzeExcelData(list);
    }
}
