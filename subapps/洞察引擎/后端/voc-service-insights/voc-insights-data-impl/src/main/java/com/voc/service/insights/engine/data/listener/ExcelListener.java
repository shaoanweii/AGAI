package com.voc.service.insights.engine.data.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.voc.service.insights.engine.api.data.IInsDataSourceService;
import com.voc.service.insights.engine.vo.ChannelInfoVo;
import com.voc.service.insights.engine.vo.InsDataSourceTemplateVo;
import com.voc.service.insights.engine.vo.ProvinceAreaVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步传输监听:
 *
 * @author lzj
 */
public class ExcelListener extends AnalysisEventListener<InsDataSourceTemplateVo> {

    private static final Logger log = LoggerFactory.getLogger(ExcelListener.class);
    private List<InsDataSourceTemplateVo> list = new ArrayList<>();
    //1000条存一次
    private static final int BATCH_COUNT = 1000;
    //假设这个是一个DAO，当然有业务逻辑这个也可以是一个service。当然如果不用存储这个对象没用。
    private IInsDataSourceService dataSourceService;

    public Map<String, Object> dataSourceMap;
    private AtomicInteger fail;
    private AtomicInteger success;
    private String batchId;
    private String clientId;
    private List<ChannelInfoVo> allChannelInfo;
    private  List<ProvinceAreaVo> proviceAndCityInfo;
    /**
     * 如果使用了spring,请使用这个构造方法。每次创建Listener的时候需要把spring管理的类传进来
     */
    public ExcelListener(IInsDataSourceService dataSourceService, Map<String, Object> dataSourceMap, String batchId, String clientId, AtomicInteger fail, AtomicInteger success, List<ChannelInfoVo> allChannelInfo, List<ProvinceAreaVo> allProvinceAreaInfo) {
        this.dataSourceService = dataSourceService;
        this.dataSourceMap = dataSourceMap;
        this.batchId = batchId;
        this.clientId = clientId;
        this.fail = fail;
        this.success = success;
        this.allChannelInfo = allChannelInfo;
        this.proviceAndCityInfo = allProvinceAreaInfo;
    }


    /**
     * 这个每一条数据解析都会来调用
     */
    @Override
    public void invoke(InsDataSourceTemplateVo goods, AnalysisContext analysisContext) {
        log.debug("解析到一条数据:========================" + goods.toString());
        // 数据存储到datas，供批量处理，或后续自己业务逻辑处理。
        list.add(goods);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT) {
            // 存储完成清理datas
            saveData();
            list.clear();
//            List<CompletableFuture<Void>> futureList = new CopyOnWriteArrayList<>();
//            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
//                saveData();
//                list.clear();
//                return null;
//            })));
//            try {
//                CompletableFuture.allOf(futureList.stream().toArray(CompletableFuture[]::new)).get();
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//                throw new RuntimeException(e);
//            }
        }
    }


    /**
     * 所有数据解析完成了 都会来调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //确保所有数据都能入库
        saveData();
        log.info("所有数据解析完成!");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        Map<String, Object> stringObjectMap = dataSourceService.analyzeExcelData(list,batchId, clientId,fail,success, dataSourceMap, allChannelInfo,proviceAndCityInfo);
        this.dataSourceMap.putAll(stringObjectMap);
    }

}