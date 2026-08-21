package com.voc.service.insights.engine.alert.scheduled;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.ttl.TtlWrappers;
import com.voc.service.insights.engine.alert.mapper.DataSimulationMapper;
import com.voc.service.insights.engine.api.alert.IInsAlertNotificationService;
import com.voc.service.insights.engine.api.alert.IInsAlertTaskService;
import com.voc.service.insights.engine.api.alert.abstracts.IInsAlertBaseService;
import com.voc.service.insights.engine.api.constants.AlertTaskEnum;
import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import com.voc.service.insights.engine.model.alert.AltAlarmDataDto;
import com.voc.service.insights.engine.vo.ConditionVo;
import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @Title: InsAlertCoreServiceImpl
 * @Package: com.voc.service.insights.engine.alert
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/25 9:13
 * @Version:1.0
 */

@Service
@AllArgsConstructor
@SuppressWarnings(value={"unchecked", "deprecation"})
public class InsAlertCoreTaskScheduled {
    private static final Logger log = LoggerFactory.getLogger(InsAlertCoreTaskScheduled.class);
    @Autowired
    IInsAlertTaskService alertTaskService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    IInsAlertNotificationService alertNotificationService;
    @Autowired
    private Map<String, IInsAlertBaseService> taskMap;
    @Autowired
    private DataSimulationMapper dataSimulationMapper;


//    @Scheduled(fixedDelay = 1000 * 60 * 10, initialDelay = 1 * 5000)
    public void executeAlertTasks() {
        log.info("-->开始执行告警定时任务");
        alertNotificationService.pushAlertNotification();
        log.info(">>>消息推送任务结束");
    }

    /**
     * 根据任务配置执行定时执行任务
     * 每小时 58分时执行
     */
//    @Scheduled(cron = "0 58 * * * ?")
//    @Scheduled(fixedDelay = 1000 * 60 * 10, initialDelay = 1 * 5000)
    public void executeTasks() {
        log.info("-->开始执行定时任务");

        //查询所有未执行的任务
        List<AlertTaskModel> taskList = alertTaskService.getAllUnexecutedTasks();
        log.info("读取任务配置信息");
        //判断匹配执行
        if (CollUtil.isEmpty(taskList)) {
            log.info("无任何任务匹配");
            return;
        }

        try {
            List<CompletableFuture<ConditionVo>> futureList = new CopyOnWriteArrayList<>();
            taskList.forEach(task -> {
                final String taskDataType = task.getDataType();
                log.trace("taskCode {}", taskDataType);

                if (taskMap.containsKey(taskDataType) && AlertTaskEnum.containsKey(taskDataType)) {
                    //需要执行异步操作
                    futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                        //执行任务 - 需要实现任务加锁，因为应用会有多台机器，需要保证任务执行互斥
                        RLock taskLock = redissonClient.getLock("taskLock".concat(task.getChannelId()).concat(task.getClientId()).concat(task.getDataType()));
                        //生成任务记录数据到数据库
                        boolean taskStatus = false;
                        if (!taskLock.isLocked()) {
                            taskLock.lock();
                            LocalDateTime startTime = LocalDateTime.now();
                            try {

                                final IInsAlertBaseService taskExecute = taskMap.get(taskDataType);
                                AlertTaskEnum byCode = AlertTaskEnum.getByCode(taskDataType);

                                log.info("生成任务记录数据到数据库 {}", byCode.name());
                                //执行任务
                                taskStatus = taskExecute.execute(task);

                            } catch (Exception e) {
                                log.error("执行任务失败 {}", task);
                                log.error(e.getMessage(), e);
                            } finally {
                                //跟新任务记录状态
                                alertTaskService.recordTask(task, startTime, taskStatus);
                                log.info("跟新任务记录状态数据库 {}， {}", task, taskStatus);
                                //unlock
                                if (taskLock.isHeldByCurrentThread()) {
                                    taskLock.unlock();
                                }
                            }
                        }
                        return null;
                    })));
                }
            });

            futureList.forEach(f -> {
                try {
                    f.get(1000 * 60, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            log.info("<--执行任务调度成功");
        } catch (Exception e) {
            log.error("<--执行任务调度失败 ");
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 数据模拟：每天定时插入模拟数据
     * 插入表：alt_push_data_record、ays_post_process_data、ays_meta_data_analysis
     * 前期模拟后期删除
     */
//    @Scheduled(cron = "0 0 3 * * ?")
    public void dataSimulation() {
        log.info("-->开始执行数据模拟");
        final List<AlertTaskModel> taskList = alertTaskService.getAllUnexecutedTasks();
        taskList.forEach(task -> {
            AltAlarmDataDto build = AltAlarmDataDto.builder().channelId(task.getChannelId()).createTime(LocalDateTime.now()).build();
            dataSimulationMapper.insertDataSimulation(build);
            for (int i = 0; i < Math.random() * 10; i++) {
                dataSimulationMapper.insertMetaDataAnalysis(build);
                dataSimulationMapper.insertPostProcessData(build);
            }
        });
        log.info("<--执行数据模拟成功");
    }


}
