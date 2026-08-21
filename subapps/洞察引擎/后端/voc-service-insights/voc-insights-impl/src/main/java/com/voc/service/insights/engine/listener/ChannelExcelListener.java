package com.voc.service.insights.engine.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.voc.service.insights.engine.api.IInsChannelInfoService;
import com.voc.service.insights.engine.model.ChannelExcelModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/11/14 下午5:03
 * @描述:
 **/
public class ChannelExcelListener extends AnalysisEventListener<ChannelExcelModel> {

    private static final int BATCH_COUNT = 1000;
    private static final Logger log = LoggerFactory.getLogger(ChannelExcelListener.class);
    private List<ChannelExcelModel> list = new ArrayList<>();
    IInsChannelInfoService channelInfoService;

    public ChannelExcelListener(IInsChannelInfoService channelInfoService) {
        this.channelInfoService = channelInfoService;
    }

    @Override
    public void invoke(ChannelExcelModel goods, AnalysisContext analysisContext) {
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
        channelInfoService.analyzeExcelData(list);
    }
}
