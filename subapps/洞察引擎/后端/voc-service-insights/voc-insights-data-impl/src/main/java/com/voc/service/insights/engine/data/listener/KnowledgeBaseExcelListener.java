package com.voc.service.insights.engine.data.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.voc.service.insights.engine.api.knowledgeBase.InsKnowledgeBaseService;
import com.voc.service.insights.engine.vo.knowledgeBase.InsKnowledgeBaseTemplateVo;
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
public class KnowledgeBaseExcelListener extends AnalysisEventListener<InsKnowledgeBaseTemplateVo> {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseExcelListener.class);
    private List<InsKnowledgeBaseTemplateVo> list = new ArrayList<>();
    //1000条存一次
    private static final int BATCH_COUNT = 1000;
    //假设这个是一个DAO，当然有业务逻辑这个也可以是一个service。当然如果不用存储这个对象没用。
    private InsKnowledgeBaseService insKnowledgeBaseService;

    public Map<String, Integer> dataSourceMap;
    private AtomicInteger fail;
    private AtomicInteger success;
    private String batchId;
    private String knowledgeBaseId;

    /**
     * 如果使用了spring,请使用这个构造方法。每次创建Listener的时候需要把spring管理的类传进来
     */
    public KnowledgeBaseExcelListener(InsKnowledgeBaseService insKnowledgeBaseService, Map<String, Integer> dataSourceMap, String batchId,String knowledgeBaseId, AtomicInteger fail, AtomicInteger success) {
        this.insKnowledgeBaseService = insKnowledgeBaseService;
        this.dataSourceMap = dataSourceMap;
        this.batchId = batchId;
        this.knowledgeBaseId = knowledgeBaseId;
        this.fail = fail;
        this.success = success;
    }


    /**
     * 这个每一条数据解析都会来调用
     */
    @Override
    public void invoke(InsKnowledgeBaseTemplateVo goods, AnalysisContext analysisContext) {
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
        Map<String, Integer> stringObjectMap = insKnowledgeBaseService.analyzeExcelData(list,batchId, knowledgeBaseId,fail,success, dataSourceMap);
        this.dataSourceMap.putAll(stringObjectMap);
    }

}