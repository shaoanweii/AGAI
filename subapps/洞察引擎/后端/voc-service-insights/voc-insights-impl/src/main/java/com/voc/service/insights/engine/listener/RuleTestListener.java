package com.voc.service.insights.engine.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.voc.service.insights.engine.api.IInsRuleTestService;
import com.voc.service.insights.engine.vo.ChannelInfoVo;
import com.voc.service.insights.engine.vo.InsRuleTestExcelVo;
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
public class RuleTestListener extends AnalysisEventListener<InsRuleTestExcelVo> {

    private static final Logger log = LoggerFactory.getLogger(RuleTestListener.class);
    private List<InsRuleTestExcelVo> list = new ArrayList<>();
    //1000条存一次
    private static final int BATCH_COUNT = 1000;
    //假设这个是一个DAO，当然有业务逻辑这个也可以是一个service。当然如果不用存储这个对象没用。
    private IInsRuleTestService iInsRuleTestService;

    public Map<String, Object> dataSourceMap;
    private AtomicInteger fail;
    private AtomicInteger success;
    private String batchId;
    private List<ChannelInfoVo> allChannelInfo;
    /**
     * 如果使用了spring,请使用这个构造方法。每次创建Listener的时候需要把spring管理的类传进来
     */
    public RuleTestListener(IInsRuleTestService iInsRuleTestService, Map<String, Object> dataSourceMap, String batchId, AtomicInteger fail, AtomicInteger success, List<ChannelInfoVo> allChannelInfo) {
        this.iInsRuleTestService = iInsRuleTestService;
        this.dataSourceMap = dataSourceMap;
        this.batchId = batchId;
        this.fail = fail;
        this.success = success;
        this.allChannelInfo = allChannelInfo;
    }


    /**
     * 这个每一条数据解析都会来调用
     */
    @Override
    public void invoke(InsRuleTestExcelVo goods, AnalysisContext analysisContext) {
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
        Map<String, Object> stringObjectMap = iInsRuleTestService.analyzeExcelData(list,batchId,fail,success, dataSourceMap, allChannelInfo);
        this.dataSourceMap.putAll(stringObjectMap);
    }

}