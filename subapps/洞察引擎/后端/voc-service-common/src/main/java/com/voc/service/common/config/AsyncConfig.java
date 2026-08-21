package com.voc.service.common.config;

import cn.hutool.core.thread.GlobalThreadPool;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.voc.service.common.util.ServiceContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

//@ConditionalOnProperty(name = "threadpool.async.executor.enabled")
@Configuration
@EnableAsync(proxyTargetClass = true)
//@EnableAsync
@EnableScheduling
public class AsyncConfig {
    public static final ExecutorService TEMP_POOL;
    private static final float MEMORY_BYTE;
    private static final float MEMORY_GB;
    private static final int PROCESSOR_COUNT;
    // 调用API使用的线程池，调用API时多数时间在等待，因此可以适当放大
    private static volatile Executor API_POOL;
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);
    static {
        logger.info("--->> 初始化全局线程池!");
        MEMORY_BYTE = Runtime.getRuntime().maxMemory();
        MEMORY_GB = MEMORY_BYTE / 1024f / 1024f / 1024f;
        PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
        logger.info("--->> 全局线程池初始化!核心数={}，可用内存{}G({}byte)", PROCESSOR_COUNT, MEMORY_GB, MEMORY_BYTE);

        TEMP_POOL = TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(PROCESSOR_COUNT * 2));
    }


    @Value("${threadpool.async.executor.corePoolSize:}")
    Integer corePoolSize;
    @Value("${threadpool.async.executor.maxPoolSize:}")
    Integer maxPoolSize;
    @Value("${threadpool.async.executor.queueCapacity:}")
    Integer queueCapacity;
    @Value("${threadpool.async.executor.threadNamePrefix:voc-}")
    String threadNamePrefix;


    @Bean
    @Primary
    public Executor executor() {
        if (API_POOL == null) {
            synchronized (GlobalThreadPool.class) {
                if (API_POOL == null) {
                    if (ObjectUtil.isNull(maxPoolSize)) {
                        int maxThreads = (int) (MEMORY_GB * 32);

                        ThreadPoolExecutor executor = new ThreadPoolExecutor(maxThreads, maxThreads,
                                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
//                        executor.setThreadFactory(r -> new Thread(RunnableWrapper.of(r)));

                        API_POOL = TtlExecutors.getTtlExecutorService(executor);
                        logger.info("--->> 线程池初始化，coreSize:{} maxSize:{}", PROCESSOR_COUNT, maxThreads);
                    } else {
                        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

                        executor.setCorePoolSize(corePoolSize);
                        executor.setMaxPoolSize(maxPoolSize);
                        executor.setQueueCapacity(queueCapacity);
                        executor.setThreadNamePrefix(threadNamePrefix);
                        /**
                         * ThreadPoolExecutor.AbortPolicy 默认拒绝策略，丢弃任务并抛出RejectedExecutionException异常
                         * ThreadPoolExecutor.DiscardPolicy 直接丢弃任务，但不抛出异常
                         * ThreadPoolExecutor.DiscardOldestPolicy 丢弃任务队列最先加入的任务，再执行execute方法把新任务加入队列执行
                         * ThreadPoolExecutor.CallerRunsPolicy：由创建了线程池的线程来执行被拒绝的任务
                         */
                        // 设置策略
                        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
//                        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy()); // 设置策略
                        // 是否在任务执行完后关闭线程池
                        executor.setWaitForTasksToCompleteOnShutdown(false);
//                        executor.setThreadFactory(r -> new Thread(RunnableWrapper.of(r)));
                        executor.initialize();
                        logger.info("--->> 线程池初始化");
                        logger.info("corePoolSize: {}", corePoolSize);
                        logger.info("maxPoolSize: {}", maxPoolSize);
                        logger.info("queueCapacity: {}", queueCapacity);
                        logger.info("threadNamePrefix: {}", threadNamePrefix);

                        API_POOL = TtlExecutors.getTtlExecutor(executor);
                    }
                }
            }
        }
        ServiceContextHolder.setExecutor(API_POOL);
        return API_POOL;
    }
}
