package com.voc.service.insights.engine.alert.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.alert.entity.AlertTaskExecutionRecordEntity;
import com.voc.service.insights.engine.alert.mapper.AltTaskExecutionRecordMapper;
import com.voc.service.insights.engine.api.alert.AltTaskConfigDataService;
import com.voc.service.insights.engine.api.alert.IInsAlertTaskService;
import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Title: InsAlertCoreServiceImpl
 * @Package: com.voc.service.insights.engine.alert
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/25 9:13
 * @Version:1.0
 */

@Service
@DS("starrock_voc")
public class AlertTaskServiceImpl extends ServiceImpl<AltTaskExecutionRecordMapper, AlertTaskExecutionRecordEntity>
        implements IInsAlertTaskService {

    @Autowired
    AltTaskConfigDataService altTaskConfigDataService;

    /**
     * 读取任务配置信息，所有已启用
     *
     * @return
     */
    @Override
    public List<AlertTaskModel> findAllEnable() {
        return altTaskConfigDataService.findAllEnable();
    }

    /**
     * 查询所有未执行的任务
     *
     * @return
     */
    @Override
    public List<AlertTaskModel> getAllUnexecutedTasks() {
        //查询任务配置信息：
        final List<AlertTaskModel> list = this.findAllEnable();
        //查询已执行的任务
        final Set<String> executedTaskIds = this.getExecutedTaskIds();

        //过滤掉已执行的任务
        return  list.stream()
                .filter(task -> !executedTaskIds.contains(task.getId())).collect(Collectors.toList());
    }

    /**
     * 查询已执行的任务
     *
     * @return
     */
    public Set<String> getExecutedTaskIds() {
        //查询已执行的任务
        final String nowTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
        List<AlertTaskExecutionRecordEntity> executedTasks = this.baseMapper.executedTasks(nowTime);
        return executedTasks.stream().map(AlertTaskExecutionRecordEntity::getTaskId).collect(Collectors.toSet());
    }

    /**
     * 生成任务记录数据到数据库
     *
     * @param task
     */
    @Override
    public String recordTask(AlertTaskModel task, LocalDateTime startTime, boolean status) {
        final String id = IdWorker.getId();
        this.baseMapper.insert(AlertTaskExecutionRecordEntity.builder()
                .id(id)
                .taskId(task.getId())
                .channelId(task.getChannelId())
                .clientId(task.getClientId())
                .dataType(task.getDataType())
                .tid(ServiceContextHolder.traceId())
//                .tid(NetUtil.getLocalhostStr())
                .status(status ? "1" : "0")
                .startTime(startTime)
                .stopTime(status ? LocalDateTime.now() : null)
                .build());
        return id;
    }


    /**
     * //跟新任务记录状态数据库
     *
     * @param taskStatus
     */
    @Override
    public void updateRecord(String id, boolean taskStatus) {
        //执行更新
        this.baseMapper.updateById(AlertTaskExecutionRecordEntity.builder()
                .id(id)
                .stopTime(LocalDateTime.now())
                .status(taskStatus ? "1" : "0")
                .build());
    }
}
