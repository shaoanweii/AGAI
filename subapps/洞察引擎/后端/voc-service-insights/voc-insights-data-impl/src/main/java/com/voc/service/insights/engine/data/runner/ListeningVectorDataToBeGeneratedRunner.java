package com.voc.service.insights.engine.data.runner;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.clients.IOnnxVectorServiceClient;
import com.voc.service.insights.engine.data.dao.InsKnowledgeBaseDetailsDao;
import com.voc.service.insights.engine.data.entity.InsKnowledgeBase;
import com.voc.service.insights.engine.data.entity.InsKnowledgeBaseDetails;
import com.voc.service.insights.engine.data.mapper.InsKnowledgeBaseDetailsMapper;
import com.voc.service.insights.engine.data.mapper.InsKnowledgeBaseMapper;
import com.voc.service.insights.engine.producer.CleanCacheEventProducer;
import com.voc.service.logs.dto.MessageDTO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Triple;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

//@Component
public class ListeningVectorDataToBeGeneratedRunner {

    private static final Logger log = LoggerFactory.getLogger(ListeningVectorDataToBeGeneratedRunner.class);
    final String chainId = "listening_vector_data_to_be_generated_runner";
    @Resource
    private RedissonClient redissonClient;
    RLock rlock;
    @Getter
    @Value("${feign.default.token:eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYW5hbHlzaXNfYXBpIiwiaWRlbnRpdHlfdHlwZSI6ImJhc2UiLCJhcHBfaWQiOiJhbmFseXNpcyIsInVzZXJuYW1lIjoiY1Fkb094bmg2eVEwMW5lc2ZLTlhVNjFKQmx5RFg3dHc4YXhod0JjNVl4aXl1MC9CYjdDQWZwQjJ5QTFxYjQ4QiIsInN1YiI6ImFuYWx5c2lzX2FwaSIsImlhdCI6MTcxMDQxMTQyMSwiZXhwIjo0MDc1NjExNDIxfQ.G1kAeqwp0udBimnDdIAqL1nSIcgV0u6YrU0bb5OchJ0}")
    String defaultToken;
    @Autowired
    InsKnowledgeBaseDetailsMapper knowledgeBaseDetailsMapper;
    @Autowired
    InsKnowledgeBaseMapper knowledgeBaseMapper;

//    @Autowired
//    MilvusService milvusService;
    @Autowired
    InsKnowledgeBaseDetailsDao insKnowledgeBaseDetailsDao;
    @Autowired
    IOnnxVectorServiceClient onnxVectorServiceClient;
    public ListeningVectorDataToBeGeneratedRunner() {
        log.info("--->> {}", this.getClass().getSimpleName());
    }
    @PostConstruct
    public void init() {
        rlock = redissonClient.getLock(chainId);
    }
    @Autowired
    CleanCacheEventProducer cleanCacheEventProducer;
//    @XxlJob("ListeningVectorDataToBeGeneratedRunner")
    public void listeningVectorDataToBeGeneratedRunner() throws InterruptedException, ExecutionException, TimeoutException {
    try {
        if (!rlock.isLocked()) {
            rlock.lock();

            QueryWrapper<InsKnowledgeBaseDetails> wrapper = new QueryWrapper<>();
            wrapper.lambda()
                    .orderByDesc(InsKnowledgeBaseDetails::getCreateTime)
                   .isNotNull(InsKnowledgeBaseDetails::getOpinion)
                   .eq(InsKnowledgeBaseDetails::getVectorState, "0")
                    .eq(InsKnowledgeBaseDetails::getDataValidity,"1")
                    .select(InsKnowledgeBaseDetails::getId, InsKnowledgeBaseDetails::getKnowledgeBaseId, InsKnowledgeBaseDetails::getOpinion)
                   .last("LIMIT " + 1000);

            List<InsKnowledgeBaseDetails> list = knowledgeBaseDetailsMapper.selectList(wrapper);

            if (CollectionUtil.isNotEmpty(list)) {
            Map<String, List<InsKnowledgeBaseDetails>> groupklbid=list.stream().collect(Collectors.groupingBy(InsKnowledgeBaseDetails::getKnowledgeBaseId));
                ExecutorService executorService1 = Executors.newFixedThreadPool(50);
                for (String knowledgeBaseId : groupklbid.keySet()) {
                    InsKnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
                    List<InsKnowledgeBaseDetails> value=new ArrayList<>(groupklbid.get(knowledgeBaseId));
                    ConcurrentLinkedQueue<Triple<String,List<Float>, String>> queuelist = new ConcurrentLinkedQueue<>();

                    // 使用 Guava 的 Lists.partition 方法将 value 拆分成多个子列表，每个子列表包含 50 个元素
                    List<List<InsKnowledgeBaseDetails>> valueChunks = Lists.partition(value, 100);
                    ServiceContextHolder.setToken(defaultToken);
                    // 记录开始时间
                    long startTime = System.currentTimeMillis();
                    // 开始处理每个chunk
                    valueChunks.forEach(chunk -> {
                        executorService1.submit(() -> {
                            List<String> opinionList = chunk.stream().map(InsKnowledgeBaseDetails::getOpinion).collect(Collectors.toList());
                            Map<String, List<Float>> listMap = null;
                            try {
                                listMap =onnxVectorServiceClient.getOnnxRuntimeEmbeddingData(opinionList).getResult();
                            } catch (Exception e) {
                                log.error("======================>>调用onnx服务异常： {}", e.getMessage());
                                e.printStackTrace();
                                return;
                            }
                            Map<String, List<Float>> finalListMap = listMap;
                            chunk.forEach(e -> {
                                e.setVectorId(IdWorker.getId());
                                e.setCollectionName(knowledgeBase.getCollectionName());
                                e.setVectorState("1");
                                List<Float> floats = finalListMap.get(e.getOpinion());
                                queuelist.add(Triple.of(e.getVectorId(), floats, new String(e.getOpinion())));
                                e.setOpinion(null);
                                e.setKnowledgeBaseId(null);
                            });
                        });
                    });
                    executorService1.shutdown();
                    try {
                        executorService1.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }


                    // 计算并打印耗时
                    long endTime = System.currentTimeMillis();
                    long duration = (endTime - startTime) / 1000; // 转换为秒
                    int minutes = (int) (duration / 60);
                    int seconds = (int) (duration % 60);
                    log.info("执行valueChunks {}条数获取向量===>耗时：{} 分钟 {} 秒",valueChunks.size()==1?valueChunks.get(0).size():value.size(),minutes,seconds);

                    // Convert ConcurrentLinkedQueue to List if needed
                    List<Triple<String,List<Float>, String>> list1 = new ArrayList<>(queuelist);


                    long startTime1 = System.currentTimeMillis();
                    int count=processBatchData(list1, knowledgeBase.getCollectionName());
                    long endTime1 = System.currentTimeMillis();
                    long duration1 = (endTime1 - startTime1) / 1000; // 转换为秒
                    int minutes1 = (int) (duration1 / 60);
                    int seconds1 = (int) (duration1 % 60);
                    log.info("====》{}条保存到milvus===>耗时：{} 分钟 {} 秒",count,minutes1,seconds1);

                  ExecutorService executorService = Executors.newFixedThreadPool(50);
                  if (count>0){
//                      // 将 valueChunks 展开成一个扁平的列表并赋值给 value
                      for (List<InsKnowledgeBaseDetails> valueChunk : valueChunks) {
                          executorService.submit(() -> {
                              boolean stataus=  insKnowledgeBaseDetailsDao.updateBatch(valueChunk);
                              log.info("====》向量id【{}】条更新到mysql数据库{}",valueChunk.size(),stataus);

                          });
                      }
                      // 关闭线程池并等待所有任务完成
                      executorService.shutdown();
                      try {
                          executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                      } catch (InterruptedException e) {
                          Thread.currentThread().interrupt();
                          // 处理中断异常
                      }
                      long endTime2 = System.currentTimeMillis();

                      long duration2 = (endTime2 - endTime1) / 1000; // 转换为秒
                      int minutes2 = (int) (duration2 / 60);
                      int seconds2 = (int) (duration2 % 60);
                      log.info("====》{}条更新到mysql===>耗时：{} 分钟 {} 秒",value.size(),minutes2,seconds2);
                  }
                }
                cleanCacheEventProducer.pushEvent(MessageDTO.builder().type("opinion").data(true).build());
            }
        }
    } catch (Exception e) {
        log.error(e.getMessage(), e);
    } finally {
        if (rlock.isHeldByCurrentThread()) {
            rlock.unlock();
        }
        log.info("chainId {} 完成", chainId);
    }
}


    public int processBatchData(List<Triple<String, List<Float>, String>>  list1,String collectionName) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        AtomicInteger count = new AtomicInteger();
        // 将 list1 拆分成多个子列表
        List<List<Triple<String, List<Float>, String>> > partitions = Lists.partition(list1, 100); // 假设每个子列表包含 100 个元素

        // 使用 CompletableFuture 来异步处理每个子列表
//        List<CompletableFuture<Integer>> futures = partitions.stream()
//                .map(partition -> CompletableFuture.supplyAsync(() ->
//                        count.addAndGet(milvusService.saveMilvusBatchData(partition, collectionName)), executorService))
//                .toList();

        // 等待所有任务完成
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 关闭线程池
        executorService.shutdown();
        return count.get();
    }

}
