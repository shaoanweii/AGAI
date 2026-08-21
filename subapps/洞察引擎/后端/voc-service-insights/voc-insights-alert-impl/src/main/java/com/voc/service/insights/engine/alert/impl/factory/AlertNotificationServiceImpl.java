package com.voc.service.insights.engine.alert.impl.factory;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.insights.engine.alert.listeners.AltSendTaskMsgListener;
import com.voc.service.insights.engine.api.IInsChannelInfoService;
import com.voc.service.insights.engine.api.IInsCustomerInfoService;
import com.voc.service.insights.engine.api.alert.AltAlarmDataService;
import com.voc.service.insights.engine.api.alert.AltTaskConfigDataService;
import com.voc.service.insights.engine.api.alert.IInsAlertNotificationService;
import com.voc.service.insights.engine.api.alert.IInsAlertTaskService;
import com.voc.service.insights.engine.api.constants.*;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.model.InsCustomerInfoModel;
import com.voc.service.insights.engine.model.alert.AlertTaskModel;
import com.voc.service.insights.engine.model.alert.AltAlarmDataModel;
import com.voc.service.insights.engine.model.alert.AltTaskConfigDataModel;
import com.voc.service.insights.engine.vo.ChannelInfoVo;
import com.voc.service.insights.engine.vo.CustomerInfoVo;
import com.voc.service.trhird.api.FlyBookSendApi;
import com.voc.service.trhird.model.MsgContentModel;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
public class AlertNotificationServiceImpl implements IInsAlertNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AlertNotificationServiceImpl.class);
    @Autowired
    AltAlarmDataService altCoreDataService;

    @Autowired
    IInsAlertTaskService alertTaskService;
    @Autowired
    RedisTemplate redisTemplate;
    @Resource
    private FlyBookSendApi flyBookSendApi;
    @Resource
    private IInsCustomerInfoService iInsCustomerInfoService;
    @Resource
    AltTaskConfigDataService altTaskConfigDataService;
    @Resource
    IInsChannelInfoService iInsChannelInfoService;

    /**
     * 根据配置向外部发送告警信息
     */
    @Override
    public void pushAlertNotification() {
        AltAlarmDataModel model = new AltAlarmDataModel();
        model.setPushStatus(PushStatusEnum.NOT_PUSHED.getCode());
        List<AltAlarmDataModel> altAlarmDataModelList = altCoreDataService.queryByParam(model);
        //判断是否有未推送数据
        if (CollectionUtil.isEmpty(altAlarmDataModelList)) {
            log.info("没有要处理的推送数据");
            return;
        }
        List<AltTaskConfigDataModel> altTaskConfigDataModelList = altTaskConfigDataService.queryByParam(new AltTaskConfigDataModel());
        if (CollectionUtil.isEmpty(altTaskConfigDataModelList)) {
            log.error("任务配置数据异常");
            return;
        }
        log.info("推送数据条数:{}", altAlarmDataModelList.size());
        Map<String, AltTaskConfigDataModel> taskModelMap = altTaskConfigDataModelList.stream().collect(Collectors.toMap(AltTaskConfigDataModel::getId, Function.identity()));
        //推送信息组装
        List<AltAlarmDataModel> altAlarmDataModels = new ArrayList<>();
        for (AltAlarmDataModel altAlarmDataModel : altAlarmDataModelList) {
            AltTaskConfigDataModel alertTaskModel = taskModelMap.get(altAlarmDataModel.getTaskId());
            if (ObjectUtils.isEmpty(alertTaskModel)) {
                log.error("获取任务配置数据异常:{}", altAlarmDataModel.getTaskId());
                continue;
            }
            //执行推送
            try {
                List<String> msgContentList = this.messageAssembly(altAlarmDataModel, alertTaskModel);
                MsgContentModel contentModel = new MsgContentModel();
                contentModel.setContent(msgContentList);
                Boolean textMagStatus = flyBookSendApi.sendRobotTextMag(contentModel);
                //推送成功修改状态
                if (textMagStatus) {
                    altAlarmDataModel.setPushStatus(PushStatusEnum.PUSH_COMPLETED.getCode());
                    altAlarmDataModel.setPushList(PushTypeEnum.FEI_SHU.getCode());
                    altAlarmDataModel.setPushMsg(JSONUtil.toJsonStr(contentModel));
                    altAlarmDataModels.add(altAlarmDataModel);
                }
            } catch (Exception e) {
                log.error("消息推送异常:", e);
            }
        }
        log.info("消息推送成功条数:{}", altAlarmDataModels.size());
        //修改状态和消息体
        if (CollectionUtil.isNotEmpty(altAlarmDataModels)) {
            Boolean updateBoolean = altCoreDataService.updateBatchById(altAlarmDataModels);
            log.info("修改数据库推送状态:{}", updateBoolean);
        }
    }

    /**
     * 消息数据组装
     *
     * @param altAlarmDataModel
     * @return
     */
    private List<String> messageAssembly(AltAlarmDataModel altAlarmDataModel, AltTaskConfigDataModel alertTaskModel) {
        List<String> msgList = new ArrayList<>();
        String channelId = altAlarmDataModel.getChannelId();
        String clientId = altAlarmDataModel.getClientId();
        InsCustomerInfoModel customerInfoModel = new InsCustomerInfoModel();
        CustomerInfoVo customerInfo = null;
        try {
            customerInfoModel.setId(clientId);
            customerInfo = iInsCustomerInfoService.findCustomerInfo(customerInfoModel);
        } catch (Exception e) {
            log.error("获取客户数据异常:", e);
        }
        Map<String, ChannelInfoVo> channelInfoVoMap = null;
        try {
            List<ChannelInfoVo> allChannelInfo = iInsChannelInfoService.findAllChannelInfo(InsChannelInfoModel.builder().clientId(clientId).build());
            log.info("获取渠道集合:{}", allChannelInfo.size());
            channelInfoVoMap = allChannelInfo.stream().collect(Collectors.toMap(ChannelInfoVo::getId, Function.identity()));
        } catch (Exception e) {
            log.error("获取渠道集合异常:", e);
        }
        if (ObjectUtils.isNotEmpty(customerInfo)) {
            msgList.add(MsgKeyEnum.key1.getName() + customerInfo.getAbbreviation());
        }
        msgList.add(MsgKeyEnum.key2.getName() + alertTaskModel.getName());
        if (MapUtil.isNotEmpty(channelInfoVoMap) && channelInfoVoMap.containsKey(channelId)) {
            ChannelInfoVo channelInfoVo = channelInfoVoMap.get(channelId);
            msgList.add(MsgKeyEnum.key3.getName() + channelInfoVo.getName());
        }
        String formattedString = altAlarmDataModel.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        msgList.add(MsgKeyEnum.key4.getName() + formattedString);
        msgList.add(MsgKeyEnum.key5.getName() + AlertLevelEnum.getByCode(alertTaskModel.getAlarmLevel()).getName());
        msgList.add(MsgKeyEnum.key6.getName() + alertTaskModel.getTimeliness() + "小时");
        msgList.add(MsgKeyEnum.key7.getName());
        return msgList;
    }


    /**
     * wenwu  生成告警
     */
    @Override
    public void generate(AlertTaskModel task) {
        log.info("开始生成告警数据");
        try {
            //判断本周起内是否已生成了告警数据
            if (this.hasBeen(task)) {
                if (task.getDataSize() != null &&
                        (task.getOmpareReduce() <= task.getDataSize() && task.getDataSize() <= task.getOmpareRise())) {
                    log.info("不符合告警生成条件");
                    return;
                }
                if (task.getDataSize() == null && AlertTaskEnum.PUSH_DATA.getCode().equals(task.getDataType()) &&
                        (LocalTime.now().isBefore(task.getScheduledTime().toLocalTime()))
                ) {
                    log.info("不符合告警生成条件");
                    return;
                }
                //开始生成
                AltAlarmDataModel dataModel = new AltAlarmDataModel();
                BeanUtils.copyProperties(task, dataModel);
                dataModel.setPushStatus(0);
                dataModel.setTaskId(task.getId());
                dataModel.setLevel(task.getAlarmLevel());
                dataModel.setStatus(0);
                altCoreDataService.insert(dataModel);
                log.info("生成告警数据完毕");

                //TODO 判断本次是否有需推送的告警数据
                if (true) {
                    //处理报告数据 - 异步执行
                    final String str = Base64.encode(JSONUtil.toJsonStr(task), CharsetUtil.CHARSET_UTF_8);
                    log.debug("validTaskAdapter.param：{}", str);
                    redisTemplate.convertAndSend(AltSendTaskMsgListener.TOPIC, str);
                }
            } else {
                log.trace("已生成了告警数据");
            }
        } catch (Exception e) {
            log.error("生成告警异常", e);
        }
    }


    /**
     * TODO 判断是否已生成了告警数据
     *
     * @return
     */
    private boolean hasBeen(AlertTaskModel task) {
        AltAlarmDataModel dataModel = AltAlarmDataModel.builder()
                .dataType(task.getDataType())
                .channelId(task.getChannelId())
                .clientId(task.getClientId())
                .taskId(task.getId())
                .build();
        List<AltAlarmDataModel> list = altCoreDataService.queryByParamAndPeriod(dataModel, task.getPeriod());
        return CollectionUtil.isEmpty(list);
    }
}
