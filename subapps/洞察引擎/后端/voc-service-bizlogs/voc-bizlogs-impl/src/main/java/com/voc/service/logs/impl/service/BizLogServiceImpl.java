package com.voc.service.logs.impl.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.JsonMapper;
import com.voc.service.logs.api.IBizLogService;
import com.voc.service.logs.impl.converts.BizLogConverMapperService;
import com.voc.service.logs.impl.entity.OpsRecordLogEntity;
import com.voc.service.logs.impl.mapper.OpsLogsMapper;
import com.voc.service.logs.model.OpsLogModel;
import com.voc.service.logs.model.UserMenuVisitRecordModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName BizLogService
 * @Description ckcui
 * @createTime 2023年11月15日 17:37
 * @Copyright futong
 */
@Service
/*@SofaService(
        interfaceType = IBizLogService.class,
        bindings = {@SofaServiceBinding(bindingType = "jvm"), @SofaServiceBinding(bindingType = "bolt")
        })*/
@EnableScheduling
public class BizLogServiceImpl extends ServiceImpl<OpsLogsMapper, OpsRecordLogEntity> implements IBizLogService {
    static TimedCache<String, OpsLogModel> cache = CacheUtil.newTimedCache(1000 * 60 * 30);
    @Autowired
    BizLogConverMapperService bizLogConverMapperService;
    @Autowired
    OpsLogsMapper opsLogsMapper;
    private static final Logger logger = LoggerFactory.getLogger(BizLogServiceImpl.class);

    public static final String TOPIC_DATA = "voc_ins_api_reqeust_record";
    public static final String MENU_TOPIC_DATA = "voc_ins_menu_visit_record";


    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void pushBizLogsMsg(OpsLogModel model) {
        logger.info("{}", JSONUtil.toJsonStr(model, JSONConfig.create().setIgnoreNullValue(true)));
        cache.put(UUID.randomUUID().toString(), model);
    }

    @Override
    public PageInfo findBizLogsMsg(OpsLogModel messageDTO) {
        Assert.hasLength(messageDTO.getAppId(), "appId不允许为空");
//        PageHelper.startPage(messageDTO.getPageNum(), messageDTO.getPageSize());
//        Integer pageSize = messageDTO.getPageSize()*messageDTO.getPageNum();
        Integer pageNum = 0;
        if (messageDTO.getPageNum() == 1) {
            pageNum = messageDTO.getPageNum() - 1;
        } else {
            pageNum = (messageDTO.getPageNum() - 1) * messageDTO.getPageSize();
        }
        messageDTO.setPageNum(pageNum);

        OpsRecordLogEntity opsRecordLogEntity = bizLogConverMapperService.converTo(messageDTO);
//        Integer count = opsLogsMapper.queryPageCount(opsRecordLogEntity);
//        if(count<=0){
//            return new PageInfo();
//        }
        Integer count = opsLogsMapper.queryPageCount(opsRecordLogEntity);
        if(count<=0){
            return new PageInfo();
        }

        List<OpsRecordLogEntity> records = opsLogsMapper.queryPageList(pageNum,messageDTO.getPageSize(),opsRecordLogEntity);


//        List<OpsRecordLogEntity> records = opsLogsMapper.queryPageList(opsRecordLogEntity);

        PageInfo pageInfo = new PageInfo(records);
        //总条数
        pageInfo.setTotal(count);
        //每页显示条数
        pageInfo.setPageSize(messageDTO.getPageSize());
        //当前分页总页数
        Integer pages = 0;
        if(count%messageDTO.getPageSize()!=0){
            pages = (count/messageDTO.getPageSize())+1;
        }else {
            pages = count/messageDTO.getPageSize();
        }
        pageInfo.setPages(pages);
        //当前页
        pageInfo.setPageNum(Math.toIntExact(messageDTO.getPageNum()));
        List<OpsLogModel> opsLogModels = bizLogConverMapperService.converToModelList(records);
        opsLogModels.stream().forEach(e->{
            LocalDateTime createTime = e.getCreateTime();
            String format = createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            e.setOperatorTime(format);
            if(e.getLogContent().contains("-")){
                String[] split = e.getLogContent().split("-");
                if(split.length>1){
                    e.setLogDesc(split[1]);
                }else {
                    e.setLogDesc(split[0]);
                }
                e.setLogContent(split[0]);
            }else{
                e.setLogDesc(e.getLogContent());
            }
        });
        pageInfo.setList(opsLogModels);
        return pageInfo;
    }

    @Override
    public List<OpsLogModel> findAllBizLogsMsg(OpsLogModel messageDTO) {
        List<OpsRecordLogEntity> opsRecordLogEntities = opsLogsMapper.selectList(null);
        List<OpsLogModel> opsLogModels = bizLogConverMapperService.converToModelList(opsRecordLogEntities);
        opsLogModels.stream().forEach(e->{
            LocalDateTime createTime = e.getCreateTime();
            String format = createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            e.setOperatorTime(format);
        });
        return opsLogModels;
    }

    @Override
    public void pushMenuVisitRecord(UserMenuVisitRecordModel model) {
        ObjectMapper mapper = JsonMapper.getInstances().getMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        logger.info("pushMenuVisitRecord:{}", model);
        try {
            String s = mapper.writeValueAsString(model);
            String sendText = JSONUtil.toJsonStr(s, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
            kafkaTemplate.send(MENU_TOPIC_DATA, sendText);
            logger.debug("sendText:{}", sendText);
        }catch (Exception e) {
            logger.error("kafka send pushMenuVisitRecord error", e);
            e.getStackTrace();
        }
    }

    @Scheduled(fixedDelay = 30 * 1000)
    public void pushLLogs() {
        synchronized (cache.keySet()) {
            if (cache.isEmpty()) {
                return;
            }
            List<String> sub = CollUtil.sub(cache.keySet(), 0, 200);
            if (CollUtil.isNotEmpty(sub)) {
                logger.info("执行登陆日期批量提交,本次批量处理 {}条日志", sub.size());
                ObjectMapper mapper = JsonMapper.getInstances().getMapper();
                mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
                sub.stream().filter(cache::containsKey)
                        .map(cache::get)
                        .filter(ObjectUtil::isNotNull)
                        .map(model -> {
                            OpsRecordLogEntity entity = bizLogConverMapperService.converTo(model);
                            entity.setId(IdWorker.getId());
                            entity.setOperateType(model.getOperateType());
                            return entity;
                        })
                        .forEach(e->{
                            try {
                                String s = mapper.writeValueAsString(e);
                                String sendText = JSONUtil.toJsonStr(s, JSONConfig.create().setDateFormat("yyyy-MM-dd HH:mm:ss").setIgnoreNullValue(false));
                                kafkaTemplate.send(TOPIC_DATA, sendText);
                            } catch (JsonProcessingException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                    //发送至kafka中 topic 为 voc_ins_api_reqeust_record
//                this.saveBatch(msgList);

                sub.stream().filter(cache::containsKey).forEach(key -> {
                    cache.remove(key);
                });

            }
        }
    }
}
