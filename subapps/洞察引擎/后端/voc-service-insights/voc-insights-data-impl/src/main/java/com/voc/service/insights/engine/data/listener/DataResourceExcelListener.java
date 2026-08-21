package com.voc.service.insights.engine.data.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.voc.service.insights.engine.api.data.InsDataResourceDescService;
import com.voc.service.insights.engine.model.data.InsDataResourceExcelModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 异步传输监听:
 *
 * @author lzj
 */
public class DataResourceExcelListener extends AnalysisEventListener<InsDataResourceExcelModel> {

    private static final Logger log = LoggerFactory.getLogger(DataResourceExcelListener.class);
    private List<InsDataResourceExcelModel> list = new ArrayList<>();
    //1000条存一次
    private static final int BATCH_COUNT = 10000;
    //假设这个是一个DAO，当然有业务逻辑这个也可以是一个service。当然如果不用存储这个对象没用。
    private InsDataResourceDescService dataSourceService;

    private String clientId;

    private String resourceId;

    /**
     * 如果使用了spring,请使用这个构造方法。每次创建Listener的时候需要把spring管理的类传进来
     */
    public DataResourceExcelListener(InsDataResourceDescService dataSourceService,String clientId,String resourceId) {
        this.dataSourceService = dataSourceService;
        this.clientId = clientId;
        this.resourceId = resourceId;
    }


    /**
     * 这个每一条数据解析都会来调用
     */
    @Override
    public void invoke(InsDataResourceExcelModel goods, AnalysisContext analysisContext) {
        log.info("解析到一条数据:========================" + goods.toString());
        // 数据存储到datas，供批量处理，或后续自己业务逻辑处理。
        list.add(goods);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT) {
            // 存储完成清理datas
            saveData();
            list.clear();
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
        dataSourceService.analysisExcel(resourceId,clientId,list);
    }

}